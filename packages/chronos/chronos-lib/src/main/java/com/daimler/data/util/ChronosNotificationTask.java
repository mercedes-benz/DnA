package com.daimler.data.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daimler.data.db.repo.forecast.ForecastCustomRepository;
import com.daimler.data.service.forecast.ForecastService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChronosNotificationTask {
	
	@Autowired
	private ForecastCustomRepository forecastCustomRepository;
	
	@Autowired
	private ForecastService forecastService;

	//cron expression for every 2 minutes
	@Scheduled(cron = "0 0/2 * * * *")
	public void notificationsCron() {		
		log.info("Chronos Notification triggered");
		List<String> forecastIds = forecastCustomRepository.getAllForecastIds();		
		
		// calling for each forecast project
		for (String forecastId : forecastIds) {
			forecastService.getAllRunsForProject(0, 0, forecastId);
		}
	}
	
	

}
