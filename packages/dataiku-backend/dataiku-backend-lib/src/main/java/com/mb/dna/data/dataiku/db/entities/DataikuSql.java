package com.mb.dna.data.dataiku.db.entities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "dataiku_sql")
public class DataikuSql implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@NotNull
    @Column(name = "project_name", nullable = false, unique = true)
	private String projectName;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "cloud_profile")
	private String cloudProfile;
	
	@JsonIgnore
    @OneToMany(mappedBy = "dataiku", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
	private List<CollaboratorSql> collaborators;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "created_on")
	private Date createdOn;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "classification_type")
	private String classificationType;
	
	@Column(name = "has_pii")
	private Boolean hasPii;
	
	@Column(name = "division_id")
	private String divisionId;
	
	@Column(name = "division_name")
	private String divisionName;
	
	@Column(name = "subdivision_id")
	private String subdivisionId;
	
	@Column(name = "subdivision_name")
	private String subdivisionName;
	
	@Column(name = "department")
	private String department;
	
	@Column(name = "solution_id")
	private String solutionId;
	
	public DataikuSql() {
		super();
	}

	public DataikuSql(String id, @NotNull String projectName, String description, String cloudProfile,
			List<CollaboratorSql> collaborators, String createdBy, Date createdOn, String status,
			String classificationType, Boolean hasPii, String divisionId, String divisionName, String subdivisionId,
			String subdivisionName, String department, String solutionId) {
		super();
		this.id = id;
		this.projectName = projectName;
		this.description = description;
		this.cloudProfile = cloudProfile;
		this.collaborators = collaborators;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.status = status;
		this.classificationType = classificationType;
		this.hasPii = hasPii;
		this.divisionId = divisionId;
		this.divisionName = divisionName;
		this.subdivisionId = subdivisionId;
		this.subdivisionName = subdivisionName;
		this.department = department;
		this.solutionId = solutionId;
	}


	public String getSolutionId() {
		return solutionId;
	}

	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCloudProfile() {
		return cloudProfile;
	}

	public void setCloudProfile(String cloudProfile) {
		this.cloudProfile = cloudProfile;
	}

	public List<CollaboratorSql> getCollaborators() {
		return collaborators;
	}

	public void setCollaborators(List<CollaboratorSql> collaborators) {
		this.collaborators = collaborators;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getClassificationType() {
		return classificationType;
	}

	public void setClassificationType(String classificationType) {
		this.classificationType = classificationType;
	}

	public Boolean getHasPii() {
		return hasPii;
	}

	public void setHasPii(Boolean hasPii) {
		this.hasPii = hasPii;
	}

	public String getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getSubdivisionId() {
		return subdivisionId;
	}

	public void setSubdivisionId(String subdivisionId) {
		this.subdivisionId = subdivisionId;
	}

	public String getSubdivisionName() {
		return subdivisionName;
	}

	public void setSubdivisionName(String subdivisionName) {
		this.subdivisionName = subdivisionName;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public class DataikuProjectNameCompartor implements Comparator<DataikuSql> {
	    @Override
	    public int compare(DataikuSql o1, DataikuSql o2) {
	        return o1.getProjectName().compareTo(o2.getProjectName());
	    }
	}
	
}
