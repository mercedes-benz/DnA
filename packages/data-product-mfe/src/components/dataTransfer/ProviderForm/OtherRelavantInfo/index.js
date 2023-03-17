import classNames from 'classnames';
import React, { useRef, useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';

// components from container app
import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import IconAvatarNew from 'dna-container/IconAvatarNew';

// import { Envs } from '../../../../Utility/envs';
import { withRouter } from 'react-router-dom';
import { setSelectedDataProduct } from '../../../dataProduct/redux/dataProductSlice';

const OtherRelevantInfo = ({ onSave, onPublish, onDescriptionTabErrors, history, user, isDataProduct,
currentTab }) => {
  const {
    // register,
    handleSubmit,
    formState: { 
      // errors,
      isSubmitting
     },
    reset,
    setValue,
    watch,
  } = useFormContext();
  const provideDataTransfers = useSelector((state) => state.provideDataTransfers);

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

  const isDisabled = !teamMembers.length && !provideDataTransfers.selectedDataTransfer.users?.length ? true : false;
  const hasUsers = watch('users');

  const [isCreator, setIsCreator] = useState(false);
  const [isInformationOwner, setIsInformationOwner] = useState(false);
  const [isCreatedBy, setIsCreatedBy] = useState(false);

  const creator = provideDataTransfers.selectedDataTransfer?.name;
  const informationOwner = provideDataTransfers.selectedDataTransfer?.informationOwner;
  const createdBy = provideDataTransfers.selectedDataTransfer?.createdBy;

  const errorValidationMsg = () => {
    let userInfo = isCreator || isCreatedBy ? 'Creator' : isInformationOwner ? 'Data responsible IO and/or Business Owner for application' : '';
    let loggedInUser = false;

    if (isCreator && creator.shortId === user.id) {
      loggedInUser = true;
    } else if (isInformationOwner && informationOwner.shortId === user.id) {
      loggedInUser = true;
    } else if (isCreatedBy && createdBy.id === user.id) {
      loggedInUser = true;
    } else {
      loggedInUser = false;
    }
    if (loggedInUser) {
      return `You are the ${userInfo} and not allowed to consume the data product`;
    } else {
      return `User is already ${userInfo} and can not be allowed to consume the data product`;
    }
  };

  const dispatch = useDispatch();

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
    teamMemberObj['addedByProvider'] = true;
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

  useEffect(() => {
    hasUsers?.length && setTeamMembers(hasUsers);
  }, [hasUsers]);

  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(-1);
  };

  const validateMembersList = (teamMemberObj) => {
    let duplicateMember = false;
    duplicateMember = teamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    const isCreatedBy = teamMemberObj.shortId === createdBy.id;
    const isCreator = teamMemberObj.shortId === creator.shortId;
    const isInformationOwner = teamMemberObj.shortId === informationOwner.shortId;
    setIsInformationOwner(isInformationOwner);
    setIsCreator(isCreator);
    setIsCreatedBy(isCreatedBy);
    return isCreator || duplicateMember || isInformationOwner || isCreatedBy;
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers.length}
        editOptionText={'Edit point of contact'}
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
        Added point of contacts will be informed about your initiated Data Transfer to give their information and finalize the
        Minimum Information Documentation.
      </p>
      <hr />
      <div className={classNames(Styles.firstPanel)}>
        <div className={Styles.teamListWrapper}>
          <div className={Styles.addTeamMemberWrapper}>
            <IconAvatarNew className={Styles.avatarIcon} />
            <button id="AddTeamMemberBtn" onClick={showAddTeamMemberModalView}>
              <i className="icon mbc-icon plus" />
              <span>Add point of contact (data receiving side)</span>
            </button>
          </div>
          {teamMembersList}
        </div>
      </div>
    </div>
  );

  const handleCancel = () => {
    history.push('/datasharing');
    setShowAddConsumersModal(false);
  };

  const handleForwardMinInfo = () => {
    // trigger notification
    setValue('notifyUsers', true);
    !watch('providerFormSubmitted') && setValue('providerFormSubmitted', true);
    setValue('users', teamMembers);
    onPublish(watch(), () => {
      setShowAddConsumersModal(false);
      history.push('/datasharing');
    });
  };

  return (
    <>
      
      <div className="btnContainer">
        <div className="btn-set">
          <button
            className={'btn btn-primary'}
            type="button"
            disabled={isSubmitting}
            onClick={handleSubmit((data) => {
              const isPublished = watch('publish');
              setValue('notifyUsers', isPublished ? true : false);
              onSave(watch(),()=>{
                  reset(data, {
                  keepDirty: false,
                });
              });
              
            },(errors) => onDescriptionTabErrors(errors))}
          >
            {currentTab == 'deletion-requirements' ?'Save':'Save & move to next tab'}
          </button>
          {isDataProduct ? (
            <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={handleSubmit((data) => {
                setValue('notifyUsers', true);
                setValue('publish', true);
                setValue('providerFormSubmitted', true);
                onPublish(watch(), () => {
                  dispatch(setSelectedDataProduct({}));
                  history.push('/dataproducts');
                });
                reset(data, {
                  keepDirty: false,
                });
              },(errors) => {onDescriptionTabErrors(errors)})}
            >
              Publish
            </button>
          ) : (
            <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={handleSubmit((data) => {
                setValue('publish', true);
                setValue('providerFormSubmitted', true);
                onPublish(watch(),()=>{
                  setShowAddConsumersModal(true);
                  
                });
                reset(data, {
                    keepDirty: false,
                  });
              },(errors) => {onDescriptionTabErrors(errors)})}
            >
              Finalize & forward
            </button>
          )}
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
        title={'Select point of contacts of the data receiving side'}
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
        modalTitleText={'point of contact'}
        editMode={editTeamMember}
        showAddTeamMemberModal={showAddTeamMemberModal}
        teamMember={teamMemberObj}
        onUpdateTeamMemberList={updateTeamMemberList}
        onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
        validateMemebersList={validateMembersList}
        hideTeamPosition={true}
        showOnlyInteral={true}
        customUserErrorMsg={isCreator || isInformationOwner || isCreatedBy ? errorValidationMsg() : ''}
      />
    </>
  );
};

export default withRouter(OtherRelevantInfo);
