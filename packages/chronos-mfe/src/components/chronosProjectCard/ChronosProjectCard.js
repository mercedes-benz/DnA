import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './chronos-project-card.scss';
import { withRouter } from 'react-router-dom';
// Container Components
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
// app components
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import ChronosProjectForm from '../chronosProjectForm/ChronosProjectForm';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
// services
import { chronosApi } from '../../apis/chronos.api';

const ChronosProjectCard = ({project, onRefresh, history}) => {
  const [editProject, setEditProject] = useState(false);

  // delete project
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
        error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while deleting forecast project',
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
            <button className="btn btn-primary" onClick={() => setEditProject(true)}>
              <i className="icon mbc-icon edit fill"></i>
            </button>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
            </button>
          </div>
        </div>
      </div>
      { editProject &&
        <Modal
          title={'Edit Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={<ChronosProjectForm edit={true} project={project} onSave={() => {setEditProject(false); onRefresh()}} />}
          scrollableContent={false}
          onCancel={() => setEditProject(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
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
export default withRouter(ChronosProjectCard);
