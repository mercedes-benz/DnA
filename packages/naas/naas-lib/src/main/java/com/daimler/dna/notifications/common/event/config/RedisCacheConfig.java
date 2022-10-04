package com.daimler.dna.notifications.common.event.config;

import java.time.Duration;
import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.daimler.dna.notifications.dto.NotificationVO;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

@EnableCaching
@Configuration
@Slf4j
public class RedisCacheConfig {

	@Value(value = "${spring.redis.host}")
	private String redisHost;
	
	@Value(value = "${spring.redis.port}")
	private String redisPort;
	
	@Value(value = "${spring.redis.user}")
	private String redisUser;
	
	@Value(value = "${spring.redis.password}")
	private String redisPassword;
	
	private RedisClient redisClient = null;
	
	private RedisCommands<String, NotificationVO> syncCommands = null;
	
	private StatefulRedisConnection<String, NotificationVO> statefulRedisConnection = null;
	
	@Bean
	public LettuceConnectionFactory getConnectionFactory(){
		LettuceConnectionFactory connectionFactory = null;
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(redisHost);
		redisConfig.setPort(Integer.parseInt(redisPort));
		redisConfig.setUsername(redisUser);
		redisConfig.setPassword(redisPassword);
		redisConfig.setDatabase(0);
	    connectionFactory = new LettuceConnectionFactory(redisConfig);
	    return connectionFactory ;
	}
	
	@Bean
	public RedisCacheManager redisCacheManager() {
		 RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
	      .entryTtl(Duration.ZERO)
	      .disableCachingNullValues()
	      .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
	    redisCacheConfiguration.disableKeyPrefix();
	    
	    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(getConnectionFactory())
	    		.cacheDefaults(redisCacheConfiguration).build();
	}  
	
	@Bean
	public RedisClient getRedisLettuceClient(){
		String redisUrl = "redis://"+redisUser+":"+redisPassword+"@"+redisHost+":"+redisPort+"/0";
		try {
		redisClient = RedisClient.create(redisUrl);
		log.info("Successfully fetched redis client for host {} and port {} and user {}", redisHost, redisPort, redisUser);
		}catch(Exception e) {
			log.error("Failed while fetching redis client for host {} and port {} and user {} with exception {}", redisHost, 
					redisPort, redisUser, e.getMessage());
		}
		try {
		statefulRedisConnection = redisClient.connect(new NotificationRedisCodec());
		syncCommands = statefulRedisConnection.sync();
		log.info("Successfully fetched statefulRedisConnection and syncCommands");
		}catch(Exception e) {
			log.error("Failed while fetching statefulRedisConnection with exception {}", e.getMessage());
		}
		return redisClient;
	}
	
	public List<String> getKeys(String userId){
		List<String> keys = syncCommands.keys(userId+"*");
		return keys;
	}
	
	@PreDestroy
	public void destroy() {
		try {
			if(statefulRedisConnection!=null) {
				statefulRedisConnection.close();
				log.info("Stateful Redis Connection is closed");
			}
			if(redisClient!=null) {
				redisClient.shutdown();
				log.info("Redis client is shutdown");
			}
		}catch(Exception e) {
			log.error("Failed while closing redis connection and client shutdown with exception {}", e.getMessage());
		}
	}
	

	
}
