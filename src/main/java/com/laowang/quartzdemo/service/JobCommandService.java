package com.laowang.quartzdemo.service;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author laowang
 * @date 2019/11/29 5:15 PM
 * @Description:
 */
@Service
public class JobCommandService {
    @Autowired
    private Scheduler scheduler;

    /**
     * 只执行一次
     *
     * @param jobName
     * @param jobGroup
     * @throws SchedulerException
     */
    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.triggerJob(jobKey(jobName, jobGroup));
    }

    public boolean delete(String name, String group) throws SchedulerException {
        return scheduler.deleteJob(jobKey(name, group));
    }

    public void pause(String name, String group) throws SchedulerException {
        scheduler.pauseJob(jobKey(name, group));
    }

    public void resume(String name, String group) throws SchedulerException {
        scheduler.resumeJob(jobKey(name, group));
    }

    public void pauseAll() throws SchedulerException {
        scheduler.pauseAll();
    }

    public void resumeAll() throws SchedulerException {
        scheduler.resumeAll();
    }

    private JobKey jobKey(String jobName, String jobGroup) {
        return JobKey.jobKey(jobName, jobGroup);
    }
}
