import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './project-details-tab.scss';
// import from DNA Container
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';
// App components
import ChronosProjectForm from '../chronosProjectForm/ChronosProjectForm';
import ChronosAccessDetails from '../chronosAccessDetails/ChronosAccessDetails';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../spinner/Spinner';
import InputFiles from '../inputFiles/InputFiles';

const ProjectDetailsTab = () => {
  const { id: projectId } = useParams();
  const [project, setProject] = useState();
  const [loading, setLoading] = useState(true);
  const [editProject, setEditProject] = useState(false);
  const [teamMembers, setTeamMembers] = useState([]);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res.data);
      ProgressIndicator.hide();
      setLoading(false);
    }).catch(error => {
      Notification.show(error.message, 'alert');
      ProgressIndicator.hide();
      setLoading(false);
    });
  };
  
  useEffect(() => {
    if(!loading && project?.collaborators !== null) {
      const members = project?.collaborators.map(member => ({...member, shortId: member.id, userType: 'internal'}));
      setTeamMembers(members);
    }
  }, [loading, project]);

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        hidePosition={true}
        hideContextMenu={!editProject}
        showInfoStacked={true}
        showMoveUp={false}
        showMoveDown={false}
      />
    );
  });

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [inputFileToBeDeleted, setInputFileToBeDeleted] = useState();

  const showDeleteConfirmModal = (inputFile) => {
    setShowDeleteModal(true);
    setInputFileToBeDeleted(inputFile);
  };
  const onCancelDelete = () => {
    setShowDeleteModal(false);
  };
  const onAcceptDelete = () => {
    if(inputFileToBeDeleted !== '' || inputFileToBeDeleted !== null) {
      ProgressIndicator.show();
      chronosApi.deleteSavedInputFile(projectId, inputFileToBeDeleted).then(() => {
        Notification.show('Saved input file deleted');
        getProjectById();
        ProgressIndicator.hide();
      }).catch(error => {
        Notification.show(
          error?.response?.data?.response?.errors[0]?.message || error?.response?.data?.response?.warnings[0]?.message || error?.response?.data?.errors[0]?.message || 'Error while deleting saved input file',
          'alert',
        );
        ProgressIndicator.hide();
      });
    }
    setShowDeleteModal(false);
  }

  return (
    <React.Fragment>
      <div className={Styles.content}>
        <div className={classNames(Styles.contextMenu)}>
          <span className={classNames('trigger', Styles.contextMenuTrigger)} onClick={() => setEditProject(true)}>
            <i className="icon mbc-icon edit context" />
          </span>
        </div>
        <h3 id="productName">Project Details</h3>
        <div className={Styles.firstPanel}>
          <div className={Styles.formWrapper}>
            { loading && <Spinner /> }
            { !loading && 
              <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                <div id="productDescription">
                  <label className="input-label summary">Project Name</label>
                  <br />                    
                  {project?.name}
                </div>
                <div id="tags">
                  <label className="input-label summary">Created on</label>
                  <br />
                  {project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}
                </div>
                <div id="isExistingSolution">
                  <label className="input-label summary">Created by</label>
                  <br />
                  {project?.createdBy?.firstName} {project?.createdBy?.lastName}
                </div>
              </div>
            }
          </div>
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={Styles.firstPanel}>
        { loading && <Spinner /> }
        { !loading && 
          <div className={Styles.collabAvatar}>
            <div className={Styles.teamListWrapper}>
              {teamMembers.length === 0 ? <p className={Styles.noCollaborator}>No Collaborators</p> : null}
              {teamMembers.length !== 0 ?
                <div className={Styles.membersList}>
                  {teamMembersList}
                </div> : null
              }
            </div>
          </div>
        }
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Input Files</h3>
        { loading && <Spinner /> }
        {!loading && <InputFiles inputFiles={project.savedInputs === null ? [] : project.savedInputs} showModal={showDeleteConfirmModal} /> }
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Access Details for Chronos Forecasting</h3>
        <ChronosAccessDetails />
      </div>
      { editProject &&
        <Modal
          title={'Edit Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={<ChronosProjectForm edit={true} project={project} onSave={() => {setEditProject(false); getProjectById()}} />}
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
                  You are going to delete the Input File.<br />
                  Are you sure you want to proceed?
                </div>
                <div className={Styles.deleteBtn}>
                  <button
                    className={'btn btn-secondary'}
                    type="button"
                    onClick={onAcceptDelete}
                  >
                    Delete
                  </button>
                </div>
              </div>
            }
            onCancel={onCancelDelete}
          />
        )
      }
    </React.Fragment>
  );
}
export default ProjectDetailsTab;
