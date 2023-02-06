package com.daimler.data.db.jsonb;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketingRole implements Serializable{

	private static final long serialVersionUID = -1286935831276675498L;
	private String name;

}
