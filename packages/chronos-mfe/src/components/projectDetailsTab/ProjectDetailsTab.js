import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './project-details-tab.scss';
// import from DNA Container
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';
import SelectBox from 'dna-container/SelectBox';
// App components
import ChronosProjectForm from '../chronosProjectForm/ChronosProjectForm';
import ChronosAccessDetails from '../chronosAccessDetails/ChronosAccessDetails';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { chronosApi } from '../../apis/chronos.api';
// import Spinner from '../spinner/Spinner';
import InputFiles from '../inputFiles/InputFiles';
import { getProjectDetails } from '../../redux/projectDetails.services';
import { Envs } from '../../utilities/envs';

const ProjectDetailsTab = () => {
  const { id: projectId } = useParams();
  const [editProject, setEditProject] = useState(false);
  const [teamMembers, setTeamMembers] = useState([]);

  const project = useSelector(state => state.projectDetails);
  const dispatch = useDispatch();
  
  useEffect(() => {
    if(!project?.isLoading && project?.data?.collaborators !== null) {
      const members = project?.data?.collaborators.map(member => ({...member, shortId: member.id, userType: 'internal'}));
      setTeamMembers(members);
    }
  }, [project]);

  useEffect(() => {
    project?.isLoading ? ProgressIndicator.show() : ProgressIndicator.hide();
  }, [project]);

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
  const [inputFileToBeDeleted, setInputFileToBeDeleted] = useState('');
  const [configFileToBeDeleted, setConfigFileToBeDeleted] = useState('');

  const showDeleteConfirmModal = (inputFile) => {
    setShowDeleteModal(true);
    setInputFileToBeDeleted(inputFile);
  };
  const showDeleteConfigFileConfirmModal = (configFile) => {
    setShowDeleteModal(true);
    setConfigFileToBeDeleted(configFile);
  };
  const onCancelDelete = () => {
    setShowDeleteModal(false);
    setConfigFileToBeDeleted('');
    setInputFileToBeDeleted('');
  };
  const onAcceptDelete = () => {
    if(inputFileToBeDeleted?.length > 0) {
      ProgressIndicator.show();
      chronosApi.deleteSavedInputFile(projectId, inputFileToBeDeleted).then(() => {
        Notification.show('Saved input file deleted');
        dispatch(getProjectDetails(projectId));
        ProgressIndicator.hide();
        setInputFileToBeDeleted('');
        SelectBox.defaultSetup();  
      }).catch(error => {
        Notification.show(
          error?.response?.data?.response?.errors[0]?.message || error?.response?.data?.response?.warnings[0]?.message || error?.response?.data?.errors[0]?.message || 'Error while deleting saved input file',
          'alert',
        );
        ProgressIndicator.hide();
        setInputFileToBeDeleted('');
      });
    }
    if(configFileToBeDeleted?.length > 0) {
      ProgressIndicator.show();
      chronosApi.deleteProjectConfigFile(projectId, configFileToBeDeleted).then(() => {
        Notification.show('Config file deleted');
        dispatch(getProjectDetails(projectId));
        ProgressIndicator.hide();
        setConfigFileToBeDeleted('');
        SelectBox.defaultSetup();
      }).catch(error => {
        Notification.show(
          error?.response?.data?.response?.errors[0]?.message || error?.response?.data?.response?.warnings[0]?.message || error?.response?.data?.errors[0]?.message || 'Error while deleting config file',
          'alert',
        );
        ProgressIndicator.hide();
        setConfigFileToBeDeleted('');
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
            {/* { loading && <Spinner /> } */}
            { !project?.isLoading && 
              <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                <div id="productDescription">
                  <label className="input-label summary">Project Name</label>
                  <br />                    
                  {project?.data?.name}
                </div>
                <div id="tags">
                  <label className="input-label summary">Created on</label>
                  <br />
                  {project?.data?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.data?.createdOn)}
                </div>
                <div id="isExistingSolution">
                  <label className="input-label summary">Created by</label>
                  <br />
                  {project?.data?.createdBy?.firstName} {project?.data?.createdBy?.lastName}
                </div>
              </div>
            }
          </div>
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={Styles.firstPanel}>
        {/* { loading && <Spinner /> } */}
        { !project?.isLoading && 
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
        {/* { loading && <Spinner /> } */}
        {!project.isLoading && <InputFiles inputFiles={project?.data?.savedInputs === null ? [] : project?.data?.savedInputs} showModal={showDeleteConfirmModal} /> }
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Configuration Files</h3>
        {/* { loading && <Spinner /> } */}
        {!project.isLoading && <InputFiles inputFiles={project?.data?.configFiles === null ? [] : project?.data?.configFiles} showModal={showDeleteConfigFileConfirmModal} addNew={true} /> }
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Access Details for Chronos Forecasting</h3>
        <p className={Styles.swagger}><a href={Envs.CHRONOS_SWAGGER_URL} target='_blank' rel="noreferrer">API swagger documentation</a></p>
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
          content={<ChronosProjectForm edit={true} onSave={() => {setEditProject(false); dispatch(getProjectDetails(projectId)); }} />}
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
