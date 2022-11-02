package com.daimler.data.dto.storage;

import java.io.Serializable;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDownloadResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;
	private ByteArrayResource data;
	private List<MessageDescription> errors;
	private List<MessageDescription> warnings;
}
