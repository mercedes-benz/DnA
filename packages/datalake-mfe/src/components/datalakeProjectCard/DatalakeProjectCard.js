import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './datalake-project-card.scss';
import { useHistory } from 'react-router-dom';
import { useDispatch } from 'react-redux';
// Container Components
import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Notification from '../../common/modules/uilab/js/src/notification';
import Popper from 'popper.js';
import DatalakeProjectForm from '../datalakeProjectForm/DatalakeProjectForm';
import { deleteProject } from '../../redux/projectsSlice';
import { getProjects } from '../../redux/projects.services';
import { ConnectionModal } from '../connectionInfo/ConnectionModal';

const DatalakeProjectCard = ({graph}) => {
  const [showConnectionModel, setShowConnectionModel] = useState(false);
  const [editProject, setEditProject] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const history = useHistory();
  const dispatch = useDispatch();

  let popperObj, tooltipElem = null;

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const onConnectionModalClose = () => {
    setShowConnectionModel(false)
  }

  const onCollabsIconMouseOver = (e) => {
    const targetElem = e.target;
    tooltipElem = targetElem.nextElementSibling;
    if (tooltipElem) {
      tooltipElem.classList.add('tooltip', 'show');
      tooltipElem.classList.remove('hide');
      popperObj = new Popper(targetElem, tooltipElem, {
        placement: 'top',
      });
    }
  };

  const onCollabsIconMouseOut = () => {
    if (tooltipElem) {
      tooltipElem.classList.add('hide');
      tooltipElem.classList.remove('tooltip', 'show');
    }
    popperObj?.destroy();
  };

  // delete project
  const handleDeleteProject = () => {
    setShowDeleteModal(false);
    dispatch(deleteProject(graph?.id));
    dispatch(getProjects());
    Notification.show('Project successfully deleted');
  }

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
              <div>Last modified</div>
              <div>{regionalDateAndTimeConversionSolution(graph.createdOn)}</div>
            </div>
            <div>
              <div>Classification</div>
              <div>{graph.classificationType || 'N/A'}</div>
            </div>
            <div className={Styles.cardCollabSection}>
              <div>Collaborators</div>
              {graph.collaborators?.length > 0 ? (
                <div>
                  <i className="icon mbc-icon profile"/>
                  <span className={Styles.cardCollabIcon} onMouseOver={onCollabsIconMouseOver} onMouseOut={onCollabsIconMouseOut}>
                    {graph.collaborators?.length}
                  </span>
                  <div className={classNames(Styles.collabsList, 'hide')}>
                    <ul>
                      {graph.collaborators?.map((bucketItem, bucketIndex) => {
                          // Check if lastName is more than 12 characters
                          let lastName = bucketItem.lastName;
                          if (lastName?.length > 12) {
                            lastName = lastName.substring(0, 12) + " ...";
                          }
                        return (
                          <li key={'collab' + bucketIndex}>
                            <span>
                              {`${bucketItem.firstName} ${lastName}`}
                              {graph.createdBy?.id === bucketItem.accesskey ? ' (Owner)' : ''}
                            </span>
                          </li>
                        );
                      })}
                    </ul>
                  </div>
                </div>
              ) : <div>None</div>}
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div style={{paddingLeft: '20px'}}>
            <a href="#" target="_blank" rel="noreferrer noopener">REST</a>&nbsp;&nbsp;
            <a href="#" target="_blank" rel="noreferrer noopener">GRAPHQL</a>
          </div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => setEditProject(true)}>
              <i className="icon mbc-icon edit fill"></i>
              <span>Edit</span>
            </button>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
              <span>Delete</span>
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
          content={<DatalakeProjectForm edit={true} project={{ data: graph }} onSave={() => setEditProject(false)} />}
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
      }
      {showConnectionModel &&
        <InfoModal
          title={'Connect'}
          modalCSS={Styles.header}
          show={showConnectionModel}
          content={<ConnectionModal onOkClick={onConnectionModalClose} />}
          hiddenTitle={true}
          onCancel={onConnectionModalClose}
        />

      }
    </>
  );
};
export default DatalakeProjectCard;
