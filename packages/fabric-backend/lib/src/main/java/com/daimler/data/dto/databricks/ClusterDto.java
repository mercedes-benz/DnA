package com.daimler.data.dto.databricks;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterDto {

	@JsonProperty("cluster_id")
	private String clusterId;

	@JsonProperty("creator_user_name")
	private String creatorUserName;

	@JsonProperty("spark_context_id")
	private Long sparkContextId;

	@JsonProperty("driver_healthy")
	private Boolean driverHealthy;

	@JsonProperty("cluster_name")
	private String clusterName;

	@JsonProperty("spark_version")
	private String sparkVersion;

	@JsonProperty("spark_conf")
	private Map<String, String> sparkConf;

	@JsonProperty("azure_attributes")
	private AzureAttributesDto azureAttributes;

	@JsonProperty("node_type_id")
	private String nodeTypeId;

	@JsonProperty("driver_node_type_id")
	private String driverNodeTypeId;

	@JsonProperty("custom_tags")
	private Map<String, String> customTags;

	@JsonProperty("cluster_log_conf")
	private Map<String, Object> clusterLogConf;

	@JsonProperty("spark_env_vars")
	private Map<String, String> sparkEnvVars;

	@JsonProperty("autotermination_minutes")
	private Integer autoterminationMinutes;

	@JsonProperty("enable_elastic_disk")
	private Boolean enableElasticDisk;

	@JsonProperty("disk_spec")
	private Map<String, Object> diskSpec;

	@JsonProperty("cluster_source")
	private String clusterSource;

	@JsonProperty("enable_local_disk_encryption")
	private Boolean enableLocalDiskEncryption;

	@JsonProperty("instance_source")
	private Map<String, Object> instanceSource;

	@JsonProperty("driver_instance_source")
	private Map<String, Object> driverInstanceSource;

	@JsonProperty("data_security_mode")
	private String dataSecurityMode;

	@JsonProperty("runtime_engine")
	private String runtimeEngine;

	@JsonProperty("effective_spark_version")
	private String effectiveSparkVersion;

	@JsonProperty("kind")
	private String kind;

	@JsonProperty("is_single_node")
	private Boolean isSingleNode;

	@JsonProperty("release_version")
	private String releaseVersion;

	@JsonProperty("state")
	private String state;

	@JsonProperty("state_message")
	private String stateMessage;

	@JsonProperty("start_time")
	private Long startTime;

	@JsonProperty("terminated_time")
	private Long terminatedTime;

	@JsonProperty("last_state_loss_time")
	private Long lastStateLossTime;

	@JsonProperty("last_activity_time")
	private Long lastActivityTime;

	@JsonProperty("last_restarted_time")
	private Long lastRestartedTime;

	@JsonProperty("num_workers")
	private Integer numWorkers;

	@JsonProperty("default_tags")
	private Map<String, String> defaultTags;

	@JsonProperty("autoscale")
	private Map<String, Integer> autoscale;

	@JsonProperty("cluster_log_status")
	private Map<String, Object> clusterLogStatus;

	@JsonProperty("termination_reason")
	private Map<String, Object> terminationReason;

	@JsonProperty("assigned_principal")
	private String assignedPrincipal;

	@JsonProperty("single_user_name")
	private String singleUserName;

	@JsonProperty("pinned_by_user_name")
	private String pinnedByUserName;

	@JsonProperty("init_scripts_safe_mode")
	private Boolean initScriptsSafeMode;

}

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class AzureAttributesDto {

	@JsonProperty("first_on_demand")
	private Integer firstOnDemand;

	@JsonProperty("availability")
	private String availability;

	@JsonProperty("spot_bid_max_price")
	private Double spotBidMaxPrice;

}
