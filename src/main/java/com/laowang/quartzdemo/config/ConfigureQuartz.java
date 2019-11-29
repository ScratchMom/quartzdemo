package com.laowang.quartzdemo.config;

/**
 * @author laowang
 * @date 2019/11/28 5:22 PM
 * @Description:
 */

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by EalenXie on 2018/6/4 11:02
 * Quartz的核心配置类
 * <p>
 * 1.1.qrtz_blob_triggers : 以Blob 类型存储的触发器。
 * 1.2.qrtz_calendars：存放日历信息， quartz可配置一个日历来指定一个时间范围。
 * 1.3.qrtz_cron_triggers：存放cron类型的触发器。
 * 1.4.qrtz_fired_triggers：存放已触发的触发器。
 * 1.5.qrtz_job_details：存放一个jobDetail信息。
 * 1.6.qrtz_job_listeners：job**监听器**。
 * 1.7.qrtz_locks： 存储程序的悲观锁的信息(假如使用了悲观锁)。
 * 1.8.qrtz_paused_trigger_graps：存放暂停掉的触发器。
 * 1.9.qrtz_scheduler_state：调度器状态。
 * 1.10.qrtz_simple_triggers：简单触发器的信息。
 * 1.11.qrtz_trigger_listeners：触发器监听器。
 * 1.12.qrtz_triggers：触发器的基本信息。
 */
@Slf4j
@Configuration
public class ConfigureQuartz {
    //配置JobFactory
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * SchedulerFactoryBean这个类的真正作用提供了对org.quartz.Scheduler的创建与配置，并且会管理它的生命周期与Spring同步。
     * org.quartz.Scheduler: 调度器。所有的调度都是由它控制。
     *
     * @param dataSource 为SchedulerFactory配置数据源
     * @param jobFactory 为SchedulerFactory配置JobFactory
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        //可选,QuartzScheduler启动时更新己存在的Job,这样就不用每次修改targetObject后删除qrtz_job_details表对应记录
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true); //设置自行启动
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());
        return factory;
    }

    //从quartz.properties文件中读取Quartz配置属性
    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    //配置JobFactory,为quartz作业添加自动连接支持
    public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements
            ApplicationContextAware {
        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }

        @Bean
        public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
            return schedulerFactoryBean.getObject();
        }

        @Bean
        public ApplicationListener<ContextStoppedEvent> stoppedEventApplicationListener(Scheduler scheduler) {
            return contextStartedEvent -> {
                try {
                    log.info("shutdown scheduler");
                    scheduler.shutdown();
                } catch (SchedulerException e) {
                    log.error("shutdown scheduler fail:", e);
                }
            };
        }
    }
}
