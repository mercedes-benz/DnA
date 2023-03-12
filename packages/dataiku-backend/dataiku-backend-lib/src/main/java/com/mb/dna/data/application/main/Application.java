package com.mb.dna.data.application.main;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "dataiku-service",
                version = "1.0",
                description = "Dataiku APIs",
                contact = @Contact(url = "", name = "BEALURI")))
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
    
}