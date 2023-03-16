package com.mb.dna.data.application.main;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;

@Filter(value = "/**")
public class CorsAdvice  implements HttpServerFilter {

    @Override
    public org.reactivestreams.Publisher<MutableHttpResponse<?>> doFilter(io.micronaut.http.HttpRequest<?> request,
                                                                          ServerFilterChain chain) {
        MutableHttpResponse<?> response = HttpResponse.ok();
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
        response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");

        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            return Publishers.just(response);
        }

        return Publishers.then(chain.proceed(request), resp -> {
            resp.getHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
            resp.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        });
    }
}