import React from 'react';
import Styles from './project-details.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const ProjectDetails = ({ project }) => {
  return (
    <div className={Styles.container}>
      <div className={Styles.header}>
        <h3>{project?.name} Details</h3>
        <p>Project and Lean Governance details</p>
      </div>
      <div className={Styles.flex}>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Type of Project</p>
            <p className={Styles.value}>{project?.typeOfProject ? project?.typeOfProject : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Project Name</p>
            <p className={Styles.value}>{project?.name}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Created on</p>
            <p className={Styles.value}>{project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Created by</p>
            <p className={Styles.value}>{project?.createdBy?.firstName} {project?.createdBy?.lastName}</p>
          </div>
        </div>
        <div className={Styles.col}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Description</p>
            <p className={Styles.value}>{project?.decription ? project?.decription : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Division</p>
            <p className={Styles.value}>{project?.division === '0' || !project?.division ? 'N/A' : project?.division}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Sub Division</p>
            <p className={Styles.value}>{project?.subDivision === '0' || !project?.subDivision ? 'N/A' : project?.subDivision}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Department</p>
            <p className={Styles.value}>{project?.department ? project?.department : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Tags</p>
            <p className={Styles.value}>
              {project?.tags ? project?.tags?.split(',').map((chip) => {
                return (
                  <>
                    <label className="chips">{chip}</label>&nbsp;&nbsp;
                  </>
                );
              }) : 'N/A'}
            </p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Data Classification</p>
            <p className={Styles.value}>{project?.dataClassification === '0' || !project?.dataClassification ? 'N/A' : project?.dataClassification}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>PII</p>
            <p className={Styles.value}>{project?.piiData === true ? 'Yes' : 'No'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Archer ID</p>
            <p className={Styles.value}>{project?.archerId ? project?.archerId : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Procedure ID</p>
            <p className={Styles.value}>{project?.procedureId ? project?.procedureId : 'N/A'}</p>
          </div>
        </div>
        <div className={Styles.col3}>&nbsp;</div>
      </div>
    </div>
  )
}

export default ProjectDetails;