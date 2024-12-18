package com.daimler.data.auth.client;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteResponseVO  implements Serializable {
	
	private static final long serialVersionUID = 1L;
    private Integer regex_priority;
    private List<String> hosts; 
    private String name;
    private String id;
    private List<String> paths;
}
