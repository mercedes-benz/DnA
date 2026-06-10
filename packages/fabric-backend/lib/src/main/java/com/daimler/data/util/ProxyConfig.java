package com.daimler.data.util;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProxyConfig {

    private String host;
    private int port;

}
