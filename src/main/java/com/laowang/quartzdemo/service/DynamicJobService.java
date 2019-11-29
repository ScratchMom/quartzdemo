package com.laowang.quartzdemo.service;

/**
 * @author laowang
 * @date 2019/11/28 5:26 PM
 * @Description:
 */

import com.laowang.quartzdemo.dao.JobEntityRepository;
import com.laowang.quartzdemo.entity.JobEntity;
import com.laowang.quartzdemo.job.DynamicJob;
import com.laowang.quartzdemo.job.DynamicMyJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by EalenXie on 2018/6/4 14:25
 */
@Slf4j
@Service
public class DynamicJobService {
    @Autowired
    private JobEntityRepository repository;

    @Autowired
    private Scheduler scheduler;

    //通过Id获取Job
    public JobEntity getJobEntityById(Integer id) {
        return repository.getById(id);
    }

    //从数据库中加载获取到所有Job
    public List<JobEntity> loadJobs() {
        List<JobEntity> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }

    //获取JobDataMap.(Job参数对象)
    public JobDataMap getJobDataMap(JobEntity job) {
        JobDataMap map = new JobDataMap();
        map.put("name", job.getName());
        map.put("group", job.getGroup());
        map.put("cronExpression", job.getCron());
        map.put("parameter", job.getParameter());
        map.put("JobDescription", job.getDescription());
        map.put("vmParam", job.getVmParam());
        map.put("jarPath", job.getJarPath());
        map.put("status", job.getStatus());
        return map;
    }

    //获取JobDetail,JobDetail是任务的定义,而Job是任务的执行逻辑,JobDetail里会引用一个Job Class来定义
    public JobDetail geJobDetail(JobKey jobKey, String description, JobDataMap map) {
        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobKey)
                .withDescription(description)
                .setJobData(map)
                .storeDurably()
                .build();
    }

    //获取Trigger (Job的触发器,执行规则)
    public Trigger getTrigger(JobEntity job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getName(), job.getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()))
                .build();
    }

    //获取JobKey,包含Name和Group
    public JobKey getJobKey(JobEntity job) {
        return JobKey.jobKey(job.getName(), job.getGroup());
    }

    /**
     * 动态创建定时任务 SimpleScheduleBuilder模式
     */
    public void dynamicCreateJob(String jobName, String jobGroup, Integer interval, Integer type) throws SchedulerException {

        JobBuilder jobBuilder = null;
        JobDetail jobDetail = null;
        switch (type) {
            case 1:
                jobBuilder = JobBuilder.newJob(DynamicMyJob.class).withIdentity(jobName, jobGroup);
                jobBuilder.usingJobData("name", "simpleJon");
                jobBuilder.usingJobData("context", "******");
                jobDetail = jobBuilder.build();
                log.info("simple pattern ...");
                SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().repeatForever().withIntervalInSeconds(interval);
                SimpleTrigger trigger = TriggerBuilder.newTrigger().withSchedule(simpleScheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger);
                log.info(">>> date[{}]", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;
            case 2:
                jobBuilder = JobBuilder.newJob(DynamicMyJob.class).withIdentity(jobName, jobGroup);
                jobBuilder.usingJobData("name", "cornJob");
                jobBuilder.usingJobData("context", "^^^^^^^^^^");
                jobDetail = jobBuilder.build();
                log.info("cron pattern ...");
                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0/5 * * * * ?");
                CronTrigger cronTrigger = TriggerBuilder.newTrigger().withSchedule(cronScheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, cronTrigger);
                log.info(">>> date[{}]", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                break;
            default:
                return;
        }


    }
}
