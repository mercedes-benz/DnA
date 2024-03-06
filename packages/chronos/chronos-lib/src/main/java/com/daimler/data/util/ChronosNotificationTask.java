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

	//cron expression for every 3 minutes
	@Scheduled(cron = "0 0/3 * * * *")
	public void updateRunsCron() {		
		log.info("Chronos update runs cron job started");
		List<String> forecastIds = forecastCustomRepository.getAllForecastIds();		
		
		// calling for each forecast project
		for (String forecastId : forecastIds) {
			try {
				forecastService.getAllRunsForProject(0, 0, forecastId, "createdOn", "desc");
			} catch (Exception e) {
				log.error("Error while fetching forecast runs for project: " + forecastId, e);
			}
		}
	}
	
	//cron expression for every 2 minutes
	@Scheduled(cron = "0 0/2 * * * *")
	public void triggerComparisions() {		
		log.info("Chronos trigger comparisions job started");
		List<String> forecastIds = forecastCustomRepository.getAllForecastIds();		
		
		// calling for each forecast project
		for (String forecastId : forecastIds) {
			try {
				forecastService.processForecastComparision(forecastId, null);
			} catch (Exception e) {

				log.error("Error while processing comparisons for project: " + forecastId, e);
			}
		}
	}
	
	

}
