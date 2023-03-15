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
	
	public DataikuSql() {
		super();
	}

	public DataikuSql(String id, @NotNull String projectName, String description, String cloudProfile,
			List<CollaboratorSql> collaborators, String createdBy, Date createdOn) {
		super();
		this.id = id;
		this.projectName = projectName;
		this.description = description;
		this.cloudProfile = cloudProfile;
		this.collaborators = collaborators;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
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
	
	public class DataikuProjectNameCompartor implements Comparator<DataikuSql> {
	    @Override
	    public int compare(DataikuSql o1, DataikuSql o2) {
	        return o1.getProjectName().compareTo(o2.getProjectName());
	    }
	}
	
}
