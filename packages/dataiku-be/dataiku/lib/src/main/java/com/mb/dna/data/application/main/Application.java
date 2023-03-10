package com.mb.dna.data.application.main;

import com.mb.dna.data.application.config.filter.UserStore;
import com.mb.dna.data.application.config.filter.UserStore.UserInfo;
import com.mb.dna.data.application.config.filter.UserStore.UserRole;

import io.micronaut.context.annotation.Bean;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.context.scope.ThreadLocal;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
    
    @Bean
	@ThreadLocal
	UserStore userStore() {
		return new UserStore();
	}
    
    @Bean
	@ThreadLocal
	UserInfo userInfo() {
		return new UserInfo();
	}
    
    @Bean
	@ThreadLocal
	UserRole userRole() {
		return new UserRole();
	}
}