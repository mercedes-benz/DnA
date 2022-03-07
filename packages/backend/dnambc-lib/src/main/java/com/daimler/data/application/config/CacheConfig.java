package com.daimler.data.application.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/*import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;*/

@Configuration
@EnableCaching
public class CacheConfig {

	/*
	 * @Bean public Config dnaMetadataCacheConfig() { return new
	 * Config().setInstanceName("hazel-instance") .addMapConfig(new MapConfig()
	 * .setName("dna-metadata-cache") .setTimeToLiveSeconds(3000)
	 * .setEvictionConfig(new EvictionConfig()
	 * .setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE)
	 * .setEvictionPolicy(EvictionPolicy.LRU)) ) ; }
	 */

}
