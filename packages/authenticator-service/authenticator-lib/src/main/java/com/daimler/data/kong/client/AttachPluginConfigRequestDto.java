package com.daimler.data.kong.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachPluginConfigRequestDto implements Serializable{
	
	  private static final long serialVersionUID = 1L;

	  private String client_secret;

	  private String discovery;
	  
	  private String introspection_endpoint_auth_method;

	  private String response_type;

	  private String token_endpoint_auth_method;

	  private String logout_path;

	  private String redirect_after_logout_uri;

	  private String ssl_verify;

	  private String introspection_endpoint;

	  private String bearer_only;

	  private String client_id;

	  private String realm;

	  private String scope;
	  
	  private String redirect_uri;

	  private String recovery_page_path;
	  
	  private String revoke_tokens_on_logout;

	  private String filters;

	  private String ignore_auth_filters;

	  private String access_token_as_bearer;

	  private String access_token_header_name;
	  
//	  private String claims_to_verify;
//	  
//	  private String key_claim_name;


}
