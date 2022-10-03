import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './styles.scss';

// import from DNA Container
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';

import Notification from '../../../common/modules/uilab/js/src/notification';
// import { IconAvatarNew } from '../../shared/icons/iconAvatarNew/IconAvatarNew';
import { regionalDateAndTimeConversionSolution } from '../../../Utility/utils';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../../apis/chronos.api';
import Spinner from '../../shared/spinner/Spinner';

const ProjectDetails = () => {
  const {id: projectId} = useParams();
  
  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();
  const [teamMembers, setTeamMembers] = useState([]);
  const [showApiKey, setShowApiKey] = useState(false);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
      chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res);
      const members = res?.collaborators.map(member => ({...member, userType: 'internal'}));
      setTeamMembers(members);
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const copyApiKey = () => {
    navigator.clipboard.writeText('dummy api key').then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const onTeamMemberEdit = (index) => {
    console.log(index);
  };

  const onTeamMemberDelete = (index) => {
    const teamMembersTemp = [...teamMembers];
    teamMembersTemp.splice(index, 1);
    setTeamMembers(teamMembersTemp);
  };

  const onTeamMemberMoveUp = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index - 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const onTeamMemberMoveDown = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index + 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        hidePosition={true}
        showInfoStacked={true}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
        onMoveUp={onTeamMemberMoveUp}
        onMoveDown={onTeamMemberMoveDown}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  const addTeamMemberModalRef = React.createRef();
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  // const showAddTeamMemberModalView = () => {
  //   setShowAddTeamMemberModal(true);
  // }
  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
  }
  const updateTeamMemberList = () => {
    onAddTeamMemberModalCancel();
  }

  return (
    <React.Fragment>
      <div className={Styles.content}>
        <div className={classNames(Styles.contextMenu)}>
          <span className={classNames('trigger', Styles.contextMenuTrigger)}>
            <i className="icon mbc-icon edit context" />
          </span>
        </div>
        <h3 id="productName">Project Details</h3>
        { loading && <Spinner /> }
        { !loading && 
          <div className={Styles.firstPanel}>
            <div className={Styles.formWrapper}>
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
            </div>
          </div>
        }
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={Styles.firstPanel}>
        <div className={Styles.collabAvatar}>
          <div className={Styles.teamListWrapper}>
            <div className={Styles.membersList}>
              { loading && <Spinner /> }
              {!loading && teamMembersList}
            </div>
          </div>
        </div>
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">API Key</h3>
        <div className={Styles.firstPanel}>
          <div className={classNames(Styles.flexLayout)}>
            <div>
              <div className={Styles.apiKey}>
                <div className={Styles.appIdParentDiv}>
                  <div className={Styles.refreshedKey}>
                    { showApiKey ? (
                      <p>2983432j38293nf9sdjfsdhfs98</p>
                    ) : (
                      <React.Fragment>
                        &bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;
                      </React.Fragment>
                    )}
                  </div>
                  <div className={Styles.refreshedKeyIcon}>
                    {showApiKey ? (
                      <React.Fragment>
                        <i
                          className={Styles.showAppId + ' icon mbc-icon visibility-hide'}
                          onClick={() => { setShowApiKey(!showApiKey) }}
                          tooltip-data="Hide"
                        />
                      </React.Fragment>
                    ) : (
                      <React.Fragment>
                        <i
                          className={Styles.showAppId + ' icon mbc-icon visibility-show ' + Styles.visiblityshow}
                          onClick={() => { setShowApiKey(!showApiKey) }}
                          tooltip-data="Show"
                        />
                      </React.Fragment>
                    )}
                    <i
                      className={Styles.cpyStyle + ' icon mbc-icon copy'}
                      onClick={copyApiKey}
                      tooltip-data="Copy"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div>
              <div className={Styles.apiKey}>
                <button className={Styles.generateApiKeyBtn} onClick={() => console.log('generate api key')}>
                  Generate API Key
                </button>
                <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
      {showAddTeamMemberModal && (
        <AddTeamMemberModal
          ref={addTeamMemberModalRef}
          modalTitleText={'Collaborator'}
          hideTeamPosition={true}
          showOnlyInteral={true}
          // editMode={editTeamMember}
          showAddTeamMemberModal={showAddTeamMemberModal}
          // teamMember={teamMemberObj}
          onUpdateTeamMemberList={updateTeamMemberList}
          onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
          // validateMemebersList={validateMembersList}
        />
      )}
    </React.Fragment>
  );
}
export default ProjectDetails;

