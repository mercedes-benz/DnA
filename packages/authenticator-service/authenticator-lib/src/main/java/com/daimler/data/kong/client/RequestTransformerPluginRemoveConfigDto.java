package com.daimler.data.kong.client;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestTransformerPluginRemoveConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;
    List<String> headers;
}
