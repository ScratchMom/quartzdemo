package com.laowang.quartzdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author laowang
 * @date 2019/11/28 5:54 PM
 * @Description:
 */
@Slf4j
public class DynamicMyJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
        String name = mergedJobDataMap.getString("name");
        String context = mergedJobDataMap.getString("context");
        log.info(">>> name[{}],context[{}],time[{}]", name, context, format.format(new Date()));
    }
}
