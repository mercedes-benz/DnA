package com.daimler.data.controller.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DDXGroupsResponseMessage extends GenericMessage {

    public DDXGroupsResponseMessage() {
        super();
    }

    @Override
    @JsonProperty("status")
    public String getSuccess() {
        return super.getSuccess();
    }

    @Override
    @JsonProperty("info")
    public List<MessageDescription> getWarnings() {
        return super.getWarnings();
    }
}