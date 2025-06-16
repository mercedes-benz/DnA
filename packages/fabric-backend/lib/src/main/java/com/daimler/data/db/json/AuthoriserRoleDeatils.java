package com.daimler.data.db.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.daimler.data.dto.fabricWorkspace.CreatedByVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthoriserRoleDeatils implements Serializable{

	private static final long serialVersionUID = 1L;

    private List<UserDetails> ownerDetails;
    private Boolean isDynamic;
}
