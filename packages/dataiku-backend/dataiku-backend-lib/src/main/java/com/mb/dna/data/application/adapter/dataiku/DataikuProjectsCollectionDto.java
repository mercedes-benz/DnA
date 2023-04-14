package com.mb.dna.data.application.adapter.dataiku;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectsCollectionDto extends ArrayList<DataikuProjectDetailsDto> implements Serializable{

}
