package com.mb.dna.data.application.main;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

@Filter(value = "/**")
public class CorsAdvice  implements HttpServerFilter {

	@Override
	public org.reactivestreams.Publisher<MutableHttpResponse<?>> doFilter(io.micronaut.http.HttpRequest<?> request,
			ServerFilterChain chain) {
		return Publishers.then(chain.proceed(request), resp -> {
			resp.getHeaders().add("Access-Control-Allow-Credentials","true");
			resp.getHeaders().add("Access-Control-Allow-Methods","*");
			//resp.getHeaders().add("Access-Control-Allow-Origin","*");
			resp.getHeaders().add("Access-Control-Allow-Headers","*");
      });
	}
}
