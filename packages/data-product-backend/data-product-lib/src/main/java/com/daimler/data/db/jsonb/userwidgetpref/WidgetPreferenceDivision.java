package com.daimler.data.db.jsonb.userwidgetpref;

import java.io.Serializable;
import java.util.List;

import com.daimler.data.db.jsonb.dataproduct.Subdivision;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetPreferenceDivision implements Serializable{

    private static final long serialVersionUID = -2211420228360901362L;
	private String id;
	private String name;
	private List<Subdivision> subdivisions;
    
}
