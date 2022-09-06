import classNames from 'classnames';
import React, { useRef, useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';

// components from container app
import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import IconAvatarNew from 'dna-container/IconAvatarNew';

import { Envs } from '../../../Utility/envs';
import { withRouter } from 'react-router-dom';

const OtherRelevantInfo = ({ onSave, history }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useFormContext();

  const [showInfoModal, setShowInfoModal] = useState(false);
  const [showAddConsumersModal, setShowAddConsumersModal] = useState(false);
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  const addTeamMemberModalRef = useRef();

  const [teamMembers, setTeamMembers] = useState([]);
  const [teamMemberObj, setTeamMemberObj] = useState({
    shortId: '',
    company: '',
    department: '',
    email: '',
    firstName: '',
    lastName: '',
    userType: '',
    teamMemberPosition: '',
  });
  const [editTeamMember, setEditTeamMember] = useState(false);
  const [editTeamMemberIndex, setEditTeamMemberIndex] = useState(-1);

  const isDisabled = !teamMembers?.length;

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

  const onTeamMemberEdit = (index) => {
    setTeamMemberObj(teamMembers[index]);
    setEditTeamMember(true);
    setEditTeamMemberIndex(index);
    setShowAddTeamMemberModal(true);
  };

  const onTeamMemberDelete = (index) => {
    const teamMembersCpy = [...teamMembers];
    teamMembersCpy.splice(index, 1);
    setTeamMembers(teamMembersCpy);
  };

  const updateTeamMemberList = (teamMemberObj) => {
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    setTeamMembers(teamMembers);
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(-1);
  };

  useEffect(() => {
    if (showAddTeamMemberModal) {
      if (!editTeamMember) {
        addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, false);
      } else addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, true);
    }
  }, [showAddTeamMemberModal, teamMemberObj, addTeamMemberModalRef, editTeamMember]);

  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(-1);
  };

  const validateMembersList = (teamMemberObj) => {
    let duplicateMember = false;
    duplicateMember = teamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    return duplicateMember;
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers.length}
        onMoveUp={onTeamMemberMoveUp}
        onMoveDown={onTeamMemberMoveDown}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });
  const resetTeamsState = () => {
    setEditTeamMemberIndex(-1);
    setEditTeamMember(false);
    setTeamMemberObj({
      shortId: '',
      department: '',
      email: '',
      firstName: '',
      lastName: '',
      userType: '',
      mobileNumber: '',
      teamMemberPosition: '',
    });
  };

  const showAddTeamMemberModalView = () => {
    resetTeamsState();
    setShowAddTeamMemberModal(true);
  };

  const addMembersContent = (
    <div className={Styles.addMembersContainer}>
      <p>
        Added members will be informed about your initiated Data Transfer to give their information and finalize the
        Minimum Information Documentation.
      </p>
      <hr />
      <div className={classNames(Styles.firstPanel)}>
        <div className={Styles.teamListWrapper}>
          <div className={Styles.addTeamMemberWrapper}>
            <IconAvatarNew className={Styles.avatarIcon} />
            <button id="AddTeamMemberBtn" onClick={showAddTeamMemberModalView}>
              <i className="icon mbc-icon plus" />
              <span>Add team member</span>
            </button>
          </div>
          {teamMembersList}
        </div>
      </div>
    </div>
  );

  const handleCancel = () => {
    history.push('/');
    setShowAddConsumersModal(false);
  };

  const handleForwardMinInfo = () => {
    // trigger notification
    setShowAddConsumersModal(false);
    history.push('/');
  };

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Specifying other relevant information</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              id="personalRelatedDataDescription"
              className={classNames('input-field-group include-error area', errors.otherRelevantInfo ? 'error' : '')}
            >
              <label className="input-label" htmlFor="otherRelevantInfo">
                Please provide any other relevant & app specific restrictions that might apply to the corresponding
                data, examples being individual deletion requirements, antitrust regulations, contractual restrictions
                etc. <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('otherRelevantInfo', {
                  required: '*Missing entry',
                })}
                rows={50}
                id="otherRelevantInfo"
              />
              <span className={classNames('error-message')}>{errors?.otherRelevantInfo?.message}</span>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.termsOfUseContainer}>
            <div className={classNames(Styles.termsOfUseContent)}>
              <label className={classNames('checkbox', errors?.tou ? 'error' : '')}>
                <span className="wrapper">
                  <input {...register('tou', { required: '*Missing entry' })} type="checkbox" className="ff-only" />
                </span>
                <div
                  className={classNames(Styles.termsOfUseText, 'mbc-scroll')}
                  style={{
                    ...(errors?.tou ? { color: '#e84d47' } : ''),
                  }}
                  dangerouslySetInnerHTML={{
                    __html: Envs.DATA_PRODUCT_TOU_HTML,
                  }}
                ></div>
              </label>
            </div>
            <span className={classNames('error-message', Styles.errorMsg)}>{errors?.tou?.message}</span>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <div className="btn-set">
          <button
            className={'btn btn-tertiary'}
            type="button"
            onClick={handleSubmit((data) => {
              console.log(data);
              onSave(data);
              setShowAddConsumersModal(true);
              reset(data, {
                keepDirty: false,
              });
            })}
          >
            Save and Forward Minimum Information
          </button>
        </div>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
      <Modal
        title={'Select members of the data receiving side'}
        showAcceptButton={false}
        showCancelButton={false}
        buttonAlignment="right"
        show={showAddConsumersModal}
        content={addMembersContent}
        scrollableContent={false}
        onCancel={handleCancel}
        footer={
          <div className={Styles.footerContainer}>
            <button className="btn btn-secondary" onClick={handleCancel}>
              Skip
            </button>
            <button
              className={isDisabled ? 'btn' : 'btn btn-tertiary'}
              disabled={isDisabled}
              onClick={handleForwardMinInfo}
            >
              Forward Minimum Information
            </button>
          </div>
        }
      />
      <AddTeamMemberModal
        ref={addTeamMemberModalRef}
        editMode={editTeamMember}
        showAddTeamMemberModal={showAddTeamMemberModal}
        teamMember={teamMemberObj}
        onUpdateTeamMemberList={updateTeamMemberList}
        onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
        validateMemebersList={validateMembersList}
      />
    </>
  );
};

export default withRouter(OtherRelevantInfo);
