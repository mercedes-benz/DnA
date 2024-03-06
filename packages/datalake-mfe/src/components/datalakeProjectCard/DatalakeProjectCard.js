import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './datalake-project-card.scss';
import { useHistory } from 'react-router-dom';
// import { useDispatch } from 'react-redux';
// Container Components
import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
// import ConfirmModal from 'dna-container/ConfirmModal';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
// import Notification from '../../common/modules/uilab/js/src/notification';
import DatalakeProjectForm from '../datalakeProjectForm/DatalakeProjectForm';
// import { deleteProject } from '../../redux/projectsSlice';
// import { getProjects } from '../../redux/projects.services';
import { ConnectionModal } from '../connectionInfo/ConnectionModal';

const DatalakeProjectCard = ({user,graph,onRefresh}) => {
  const [showConnectionModel, setShowConnectionModel] = useState(false);
  const [editProject, setEditProject] = useState(false);
  // const [showDeleteModal, setShowDeleteModal] = useState(false);
  const history = useHistory();
  // const dispatch = useDispatch();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const onConnectionModalClose = () => {
    setShowConnectionModel(false)
  }

  // delete project
  // const handleDeleteProject = () => {
  //   setShowDeleteModal(false);
  //   dispatch(deleteProject(graph?.id));
  //   dispatch(getProjects());
  //   Notification.show('Project successfully deleted');
  // }

  const onhandleClickConnection = () => {
    setShowConnectionModel(true);
  }

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/graph/${graph?.id}`);
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{graph?.projectName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(graph.createdOn)}</div>
            </div>
            <div>
              <div>schema</div>
              <div>{graph.schemaName}</div>
            </div>
            <div>
              <div>Classification</div>
              <div>{graph.classificationType || 'N/A'}</div>
            </div>
            <div>
              <div>Connector Type</div>
              <div>{graph.connectorType || 'N/A'}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className={classNames("btn btn-primary",graph.createdBy.id === user.id ? "" :"hide")} onClick={() => setEditProject(true)}>
              <i className="icon mbc-icon edit fill"></i>
              <span>Edit</span>
            </button>
            <button className={classNames("btn btn-primary", Styles.btnDisabled)}>
              <i className="icon delete"></i>
              <span tooltip-data={'Coming Soon'}>Delete</span>
            </button>
            <button className={'btn btn-primary'} type="button" onClick={() => onhandleClickConnection()} >
              <i className="icon mbc-icon comparison"></i>
              <span>Connect</span>
            </button>
          </div>
        </div>
      </div>
      { editProject &&
        <Modal
          title={'Edit Datalake Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={<DatalakeProjectForm edit={true} project={{ data: graph }} onSave={() => {setEditProject(false); onRefresh()}} />}
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
      {/* {
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
                  You are going to delete the Datalake Project.<br />
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
      } */}
      {showConnectionModel &&
        <InfoModal
          title={'Connect'}
          modalCSS={Styles.header}
          show={showConnectionModel}
          content={<ConnectionModal projectId={graph?.id} onOkClick={onConnectionModalClose} />}
          hiddenTitle={true}
          onCancel={onConnectionModalClose}
        />

      }
    </>
  );
};
export default DatalakeProjectCard;