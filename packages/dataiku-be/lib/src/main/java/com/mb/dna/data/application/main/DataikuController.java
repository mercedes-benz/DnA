package com.mb.dna.data.application.main;

import io.micronaut.http.annotation.*;

@Controller("/dataiku")
public class DataikuController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}