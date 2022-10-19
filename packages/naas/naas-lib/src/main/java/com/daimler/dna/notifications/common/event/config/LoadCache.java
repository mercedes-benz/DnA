package com.daimler.dna.notifications.common.event.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoadCache {

	
	@Autowired
	private  ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private CacheUpdateEventListener cacheUpdater;
	
	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() {
		try {
		AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
	    cacheUpdater.init();
	    log.info("Successfully completed loading messages from topic to users cache based on preferences");
		}catch(Exception e) {
			AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
			log.error("Failed while loading messages from topic to users cache based on preferences, with exception : {} ",e.getMessage());
		}
	}


	
}
