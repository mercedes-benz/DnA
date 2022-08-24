import React, { useState } from 'react';
import Styles from './ProjectCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
// import { chronosApi } fro../apis/chronosapi';
// import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';

import ConfirmModal from 'dna-container/ConfirmModal';

const ProjectCardItem = ({
  project,
  onEdit,
  // history
}) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const deleteProjectContent = (
    <div>
      <h3>Are you sure you want to delete {project?.name} ? </h3>
    </div>
  );

  const deleteProjectAccept = () => {
    // ProgressIndicator.show();
    // chronosApi.deleteProject(project.id).then(() => {
    //   setShowDeleteModal(false);
    // });
    setShowDeleteModal(false);
  };
  const deleteProjectClose = () => {
    setShowDeleteModal(false);
  };

  const displayPermission = (item) => {
    return Object.entries(item)
      ?.map(([key, value]) => {
        if (value === true) {
          return key;
        }
      })
      ?.filter((x) => x) // remove falsy values
      ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
      ?.join(' / ');
  };

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            /* navigate to summary page*/
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{project?.name}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Permission</div>
              <div>{displayPermission(project?.permission)}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(project?.createdDate)}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{`${project?.createdBy?.firstName} ${project?.createdBy?.lastName}`}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>{!project?.publish && <span className={Styles.draftIndicator}>DRAFT</span>}</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => onEdit(project)}>
              <i className="icon mbc-icon edit"></i>
            </button>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
            </button>
          </div>
        </div>
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteProjectContent}
        onCancel={deleteProjectClose}
        onAccept={deleteProjectAccept}
      />
    </>
  );
};
export default withRouter(ProjectCardItem);
