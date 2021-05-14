package io.papermc.hangar.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.papermc.hangar.service.internal.JobService;

@Component
public class JobUpdateTask {

    private final JobService service;

    @Autowired
    public JobUpdateTask(JobService service) {
        this.service = service;
    }

    @Scheduled(fixedRateString = "${hangar.jobs.check-interval}", initialDelayString = "${hangar.jobs.check-interval}")
    public void checkAndProcess() {
        service.checkAndProcess();
    }
}
