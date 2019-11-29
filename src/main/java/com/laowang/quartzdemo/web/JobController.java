package com.laowang.quartzdemo.web;

/**
 * @author laowang
 * @date 2019/11/28 5:26 PM
 * @Description:
 */

import com.laowang.quartzdemo.entity.JobEntity;
import com.laowang.quartzdemo.service.DynamicJobService;
import com.laowang.quartzdemo.service.JobCommandService;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Created by EalenXie on 2018/6/4 16:12
 */
@RestController
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    @Autowired
    private DynamicJobService jobService;
    @Autowired
    private JobCommandService jobCommandService;


    /**
     * 动态创建quartz
     *
     * @param jobName
     * @param jobGroup
     * @param interval
     * @param type=1   simple; type=2 corn
     * @throws SchedulerException
     */
    @GetMapping("/dynamicCreateJob/{type}")
    public void dynamicCreateJob(
            @RequestParam("jobName") String jobName,
            @RequestParam("jobGroup") String jobGroup,
            @RequestParam("interval") Integer interval,
            @PathVariable("type") Integer type) throws SchedulerException {
        jobService.dynamicCreateJob(jobName, jobGroup, interval, type);
    }


    @GetMapping("/triggerJob/{type}")
    public void triggr(@RequestParam("jobName") String jobName,
                       @RequestParam("jobGroup") String jobGroup,
                       @PathVariable("type") Integer type) throws SchedulerException {

        switch (type) {
            case 1:
                jobCommandService.triggerJob(jobName, jobGroup);
                break;
            case 2:
                jobCommandService.pause(jobName, jobGroup);
                break;
            case 3:
                jobCommandService.delete(jobName, jobGroup);
                break;
            case 4:
                jobCommandService.resume(jobName, jobGroup);
                break;
            case 5:
                jobCommandService.pauseAll();
                break;
            case 6:
                jobCommandService.resumeAll();
                break;
        }


    }

    //初始化启动所有的Job
//    @PostConstruct
//    public void initialize() {
//        try {
//            reStartAllJobs();
//            logger.info("INIT SUCCESS");
//        } catch (SchedulerException e) {
//            logger.info("INIT EXCEPTION : " + e.getMessage());
//            e.printStackTrace();
//        }
//    }


    /**
     * 根据ID重启某个Job
     *
     * @param id
     * @return
     * @throws SchedulerException
     */
    @RequestMapping("/refresh/{id}")
    public String refresh(@PathVariable Integer id) throws SchedulerException {
        String result;
        JobEntity entity = jobService.getJobEntityById(id);
        if (entity == null) {
            return "error: id is not exist ";
        }
        synchronized (logger) {
            JobKey jobKey = jobService.getJobKey(entity);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
            scheduler.deleteJob(jobKey);
            JobDataMap map = jobService.getJobDataMap(entity);
            JobDetail jobDetail = jobService.geJobDetail(jobKey, entity.getDescription(), map);
            if (entity.getStatus().equals("OPEN")) {
                scheduler.scheduleJob(jobDetail, jobService.getTrigger(entity));
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " success !";
            } else {
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " failed ! , " +
                        "Because the Job status is " + entity.getStatus();
            }
        }
        return result;
    }


    //重启数据库中所有的Job
    @RequestMapping("/refresh/all")
    public String refreshAll() {
        String result;
        try {
            reStartAllJobs();
            result = "SUCCESS";
        } catch (SchedulerException e) {
            result = "EXCEPTION : " + e.getMessage();
        }
        return "refresh all jobs : " + result;
    }

    /**
     * 重新启动所有的job
     */
    private void reStartAllJobs() throws SchedulerException {
        synchronized (logger) {                                                         //只允许一个线程进入操作
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Set<JobKey> set = scheduler.getJobKeys(GroupMatcher.anyGroup());
            scheduler.pauseJobs(GroupMatcher.anyGroup());                               //暂停所有JOB
            for (JobKey jobKey : set) {                                                 //删除从数据库中注册的所有JOB
                scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                scheduler.deleteJob(jobKey);
            }
            for (JobEntity job : jobService.loadJobs()) {                               //从数据库中注册的所有JOB
                logger.info("Job register name : {} , group : {} , cron : {}", job.getName(), job.getGroup(), job.getCron());
                JobDataMap map = jobService.getJobDataMap(job);
                JobKey jobKey = jobService.getJobKey(job);
                JobDetail jobDetail = jobService.geJobDetail(jobKey, job.getDescription(), map);
                if (job.getStatus().equals("OPEN")) {
                    scheduler.scheduleJob(jobDetail, jobService.getTrigger(job));
                } else {
                    logger.info("Job jump name : {} , Because {} status is {}", job.getName(), job.getName(), job.getStatus());
                }
            }
        }
    }
}
