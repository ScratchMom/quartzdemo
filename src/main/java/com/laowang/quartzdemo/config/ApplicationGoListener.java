package com.laowang.quartzdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.annotation.Resource;

/**
 * @author laowang
 * @date 2019/11/29 5:03 PM
 * @Description:
 */
@Slf4j
public class ApplicationGoListener implements ApplicationListener<ContextClosedEvent> {
    @Resource
    private Scheduler scheduler;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        try {
            if (!scheduler.isStarted()) {
                scheduler.start();
                log.info("scheduler acquire ok");
            }
        } catch (SchedulerException e) {
            log.info("scheduler acquire failed, begin to shudown", e);

        }
    }
}
