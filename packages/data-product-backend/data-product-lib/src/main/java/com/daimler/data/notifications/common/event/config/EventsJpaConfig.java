package com.daimler.data.notifications.common.event.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.daimler.data.notifications.common.db.repo")
@EntityScan("com.daimler.data.notifications.common.db.entities")
public class EventsJpaConfig {

}
