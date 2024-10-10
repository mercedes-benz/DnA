package com.daimler.dna.notifications.common.db.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventPushException implements Serializable {

	private static final long serialVersionUID = 1879509288866057161L;

	private String exceptionMsg;
	private GenericEventRecord record;
//	private String appId;
//	private String userId;
	private BigDecimal retryCount;
	private boolean retrySuccess;
	private String outBinder;

}
