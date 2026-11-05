package com.daimler.data.application.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "code-server.redis")
public class PatRedisProperties {
	@NotBlank
	private String host = "XXXX";
	@Min(1)
	@Max(65535)
	private int port = 6379;
	private String user = "";
	private String password = "";
	@Min(0)
	private int database = 1;
	@Min(1)
	private long timeout = 2000;
}
