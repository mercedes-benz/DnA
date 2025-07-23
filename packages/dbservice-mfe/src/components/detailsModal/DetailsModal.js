import classNames from 'classnames';
import React from 'react';
import Styles from './details-modal.scss';
import { IconAvatarNew } from '../icons/iconAvatarNew/IconAvatarNew.js';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils.js';
import { TEAMS_PROFILE_LINK_URL_PREFIX } from '../../../src/utilities/utils.js';

const DetailsModal = ({ dbservice }) => {
  
  return (
    <div className={Styles.panel}>
      <h3>DB Service Details</h3>
        <div className={classNames(Styles.flex)}>
          <div className={Styles.col3}>
            <p className={Styles.label}>DB Service Name</p> {dbservice?.serviceName || 'null'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>DB Name</p> {dbservice?.dbName || 'null'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Created On</p>
            {dbservice?.createdOn && regionalDateAndTimeConversionSolution(dbservice?.createdOn)}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Created by</p>
            {dbservice?.projectOwner?.firstName} {dbservice?.projectOwner?.lastName}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>Type of Project</p>
            {dbservice?.dataGovernance?.typeOfProject}
          </div>
            <div className={Styles.col3}>
            <p className={Styles.label}>DB Type</p>
            {dbservice?.dbType}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Description</p>
            {dbservice?.description ? dbservice?.description : 'Test description'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Division</p>
            {dbservice?.dataGovernance?.division === '0' || !dbservice?.dataGovernance?.division ? 'N/A' : dbservice?.dataGovernance?.division}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Sub Division</p>
            {dbservice?.dataGovernance?.subDivision === '0' || !dbservice?.dataGovernance?.subDivision ? 'N/A' : dbservice?.dataGovernance?.subDivision}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>Department</p>
            {dbservice?.dataGovernance?.department ? dbservice?.dataGovernance?.department : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Tags</p>
            {dbservice?.dataGovernance?.tags?.length > 0 ? dbservice.dataGovernance?.tags?.map((chip) =>
                <><label className="chips">{chip}</label>&nbsp;&nbsp;</>
              ) : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Data Classification</p>
            {dbservice?.dataClassification === '0' || !dbservice?.dataClassification ? 'Internal' : dbservice?.dataClassification}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>PII</p>
            {dbservice?.dataGovernance?.hasPii === true ? 'Yes' : 'No'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Archer ID</p>
            {dbservice?.dataGovernance?.archerId ? dbservice?.dataGovernance?.archerId : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Procedure ID</p>
            {dbservice?.dataGovernance?.procedureId ? dbservice?.dataGovernance?.procedureId : 'N/A'}
          </div>
          <div className={Styles.col}>
            <p className={Styles.label}>Project Collaborators</p>
            {dbservice?.projectCollaborators?.length === 0 &&
              <div className={Styles.noLincense}>
                <p>No Collaborators</p>
              </div>
            }
          <div className={classNames(Styles.flex, Styles.userContainer)}>
            {dbservice?.projectCollaborators?.length > 0 && dbservice?.projectCollaborators?.map((projectCollaborator) => (
              <div key={projectCollaborator?.id} className={classNames(Styles.col3, Styles.userCard)}>
                <div><IconAvatarNew /></div>
                <div>
                  <p>
                    <a
                      href={TEAMS_PROFILE_LINK_URL_PREFIX + projectCollaborator?.accesskey}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {projectCollaborator?.firstName + ' ' + projectCollaborator?.lastName} ({projectCollaborator?.accesskey})
                    </a>
                    {' '}
                    <span>
                      Read
                      {projectCollaborator?.permission?.write && ', Write'}
                      {projectCollaborator?.permission?.admin && ', Admin'}
                    </span>
                  </p>
                </div>
              </div>
            ))}
          </div>

          </div>
        </div>
    </div>
  );
};

export default DetailsModal;