package com.daimler.data.dto.userinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

@Validated
public class TransparencyVO {

    @JsonProperty("count")
    private Integer count = 0;


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
