package com.daimler.data.task;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@SuppressWarnings(value = "unused")
public class PublishExpiryAlertsTask {

	// alert x days before expiry
	@Value("${task.delete.inactive-solutions-duration}")
	private String inActiveYears;

	// @Scheduled(cron = "0 0 0 * * *")
	public void deleteInActiveSolutionsTask() {
//        log.info("Running task to delete In-Active solutions that are more than " + inActiveYears + " years old at:" + new Date().toString());
//        Calendar now = Calendar.getInstance();
//        now.add(Calendar.YEAR, -Integer.parseInt(inActiveYears));
//        solutionService.deleteInActiveSolutionsOlderThan(now);

	}
}
