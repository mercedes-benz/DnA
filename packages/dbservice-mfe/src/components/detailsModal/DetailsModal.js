import classNames from 'classnames';
import React from 'react';
import Styles from './details-modal.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils.js';

const DetailsModal = ({ dbservice }) => {
  console.log(dbservice);
  return (
    <div className={Styles.panel}>
      <h3>DB Service Details</h3>
        <div className={classNames(Styles.flex)}>
          <div className={Styles.col3}>
            <p className={Styles.label}>DB Service Name</p> {dbservice?.bucketName || 'null'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Created on</p>
            {dbservice?.createdDate !== undefined && regionalDateAndTimeConversionSolution(dbservice?.createdDate)}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Created by</p>
            {dbservice?.createdBy?.firstName} {dbservice?.createdBy?.lastName}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>Type of Project</p>
            {dbservice?.typeOfProject ? dbservice?.typeOfProject : 'N/A'}
          </div>
          <div className={Styles.col2}>
            <p className={Styles.label}>Description</p>
            {dbservice?.description ? dbservice?.description : 'Test description'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Permission</p>
            Read, {dbservice.permission.write && 'Write'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Division</p>
            {dbservice?.division === '0' || !dbservice?.division ? 'N/A' : dbservice?.division}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Sub Division</p>
            {dbservice?.subDivision === '0' || !dbservice?.subDivision ? 'N/A' : dbservice?.subDivision}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>Department</p>
            {dbservice?.department ? dbservice?.department : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Tags</p>
            {dbservice?.tags?.length > 0 ? dbservice.tags?.map((chip) =>
                <><label className="chips">{chip}</label>&nbsp;&nbsp;</>
              ) : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Data Classification</p>
            {dbservice?.dataClassification === '0' || !dbservice?.dataClassification ? 'Internal' : dbservice?.dataClassification}
          </div>

          <div className={Styles.col3}>
            <p className={Styles.label}>PII</p>
            {dbservice?.hasPii === true ? 'Yes' : 'No'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Archer ID</p>
            {dbservice?.archerId ? dbservice?.archerId : 'N/A'}
          </div>
          <div className={Styles.col3}>
            <p className={Styles.label}>Procedure ID</p>
            {dbservice?.procedureId ? dbservice?.procedureId : 'N/A'}
          </div>
          <div className={Styles.col2}>
            <p className={Styles.label}>Collaborators</p>
            {dbservice?.collaborators?.length === 0 &&
              <div className={Styles.noLincense}>
                <p>No Collaborators</p>
              </div>
            }
            {dbservice?.collaborators?.length > 0 && dbservice?.collaborators?.map((collaborator) => {
              return (
                <p key={collaborator?.accessKey}>
                  {collaborator?.firstName + ' ' + collaborator?.lastName} ({collaborator?.accesskey}) - Read, {collaborator?.permission?.write && 'Write'}
                </p>
              );
            })}
          </div>
        </div>
    </div>
  );
};

export default DetailsModal;