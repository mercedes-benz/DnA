import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './ProjectCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import Notification from '../common/modules/uilab/js/src/notification';
// Container Components
import ConfirmModal from 'dna-container/ConfirmModal';
import { chronosApi } from '../apis/chronos.api';

const ProjectCardItem = ({project, onRefresh, onEdit, history}) => {

  // const displayPermission = (item) => {
  //   return Object.entries(item)
  //     ?.map(([key, value]) => {
  //       if (value === true) {
  //         return key;
  //       }
  //     })
  //     ?.filter((x) => x) // remove falsy values
  //     ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
  //     ?.join(' / ');
  // };
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const handleDeleteProject = () => {
    ProgressIndicator.show();
    chronosApi.deleteForecastProject(project?.id).then(() => {
      ProgressIndicator.hide();
      Notification.show('Forecasting Project successfully deleted');
      onRefresh();
      setShowDeleteModal(false);
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while deleting forecast project',
        'alert',
      );
      setShowDeleteModal(false);
    });
  }

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/project/${project?.id}`);
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
            {/* <div>
              <div>Permission</div>
              <div>{project?.permission === null ? 'N/A' : displayPermission(project?.permission)}</div>
            </div> */}
            <div>
              <div>Created on</div>
              <div>{project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{`${project?.createdBy?.firstName} ${project?.createdBy?.lastName}`}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => onEdit(project)}>
              <i className="icon mbc-icon edit fill"></i>
            </button>
            {/* <button className="btn btn-primary" disabled={true}>
              <i className="icon mbc-icon share"></i>
            </button>
            <button className="btn btn-primary" disabled={true}>
              <i className="icon mbc-icon data-sharing"></i>
            </button> */}
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
            </button>
          </div>
        </div>
      </div>
      {
        showDeleteModal && (
          <ConfirmModal
            title={'Delete'}
            showAcceptButton={false}
            showCancelButton={false}
            show={showDeleteModal}
            removalConfirmation={true}
            showIcon={false}
            showCloseIcon={true}
            content={
              <div className={Styles.deleteForecastResult}>
                <div className={Styles.closeIcon}>
                  <i className={classNames('icon mbc-icon close thin')} />
                </div>
                <div>
                  You are going to delete the Forecast Project.<br />
                  Are you sure you want to proceed?
                </div>
                <div className={Styles.deleteBtn}>
                  <button
                    className={'btn btn-secondary'}
                    type="button"
                    onClick={handleDeleteProject}
                  >
                    Delete
                  </button>
                </div>
              </div>
            }
            onCancel={() => setShowDeleteModal(false)}
          />
        )
      }
    </>
  );
};
export default withRouter(ProjectCardItem);
