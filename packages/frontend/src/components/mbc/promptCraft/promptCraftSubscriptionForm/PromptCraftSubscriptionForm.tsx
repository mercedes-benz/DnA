import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
// styles
import Styles from './PromptCraftSubscriptionForm.scss';
// App components
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import AddUser from '../../../mbc/addUser/AddUser'; 
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';
import { ApiClient } from '../../../../services/ApiClient';
import { Envs } from 'globals/Envs';

export interface IPromptCraftSubscriptionFormProps {
  onSave: () => void;
}

const PromptCraftSubscriptionForm = ({ onSave }: IPromptCraftSubscriptionFormProps) => {
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const [projectOwner, setProjectOwner] = useState({});
  const [projectMembers, setProjectMembers] = useState([]);

  const getProjectMembers = (member: any) => {
    const projectMemberData = {
        id: member?.shortId,
        firstName: member?.firstName,
        lastName: member?.lastName,
        department: member?.department,
        mobileNumber: member?.mobileNumber,
        email: member?.email
    };

    let duplicateMember = false;
    duplicateMember = projectMembers?.filter((projectMember) => projectMember.id === member.shortId)?.length ? true : false;

    // const isCreator = user?.id === developer?.id;
    const isCreator = false;

    if (duplicateMember) {
        Notification.show('Member already added.', 'warning');
    } else if (isCreator) {
        Notification.show(
            `${member.firstName} ${member.lastName} is a creator. Creator can't be added to user lincense.`,
            'warning',
        );
    } else {
        projectMembers?.push(projectMemberData);
        setProjectMembers([...projectMembers]);
    }
  }

  const onProjectMemberDelete = (userId: any) => {
    return () => {
      const updatedProjectMembers = projectMembers.filter((member) => {
        return member?.id !== userId;
      });
      setProjectMembers(updatedProjectMembers);
    };
  };

  useEffect(() => {
    ApiClient.getUsersBySearchTerm(Envs.PC_CREATOR_ID)
          .then((response) => {
            if (response) {
              if (response.records !== undefined) {
                setProjectOwner(response.records[0]);
              } else {
                setProjectOwner({});
              }
            }
          })
          .catch((error: any) => {
            Notification.show(error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message ||'Error while fetching project owner details', 'alert');
          });
  }, []);

  const handleCreateSubscription = (values: any) => {
    ProgressIndicator.show();
    const data = {
      projectName: values.name.trim(),
      orgname: values.orgname.trim(),
      projectMembers: projectMembers,
      projectOwner: projectOwner,
    };
    PromptCraftApiClient.createPromptCraftSubscription(data).then((res) => {
      console.log(res);
      ProgressIndicator.hide();
      Notification.show('Prompt Craft subscription request successfully placed');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while placing Prompt Craft subscription request',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>Create Prompt Craft Subscription</h3>
            <p>Enter the information to start creating!</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={'input-label'}>
                  Name of Project <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('name', { required: '*Missing entry' })}
                />
                <span className={'error-message'}>{errors?.name?.message}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.orgname ? 'error' : '')}>
                <label className={'input-label'}>
                  Name of Organization <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('orgname', { required: '*Missing entry' })}
                />
                <span className={'error-message'}>{errors?.orgname?.message}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error')}>
                <AddUser dagId='' getCollabarators={getProjectMembers} isRequired={false} isUserprivilegeSearch={false} title={'Project Members'} />
              </div>
              {projectMembers?.length === 0 &&
                <div className={Styles.noLincense}>
                  <p>No Project Members Selected</p>
                </div>
              }
              <div>
                {projectMembers?.length > 0 && (
                  <>
                    <div className={Styles.colHeader}>
                        <div className={Styles.column1}>User ID</div>
                        <div className={Styles.column2}>Name</div>
                        <div className={Styles.column4}></div>
                    </div>
                    <div>
                        {projectMembers?.map((member) => {
                          return (
                              <div key={member?.id} className={Styles.userRow}>
                                  <div className={Styles.column1}>
                                    <p>{member?.id}</p>
                                  </div>
                                  <div className={Styles.column2}>
                                    <p>{member?.firstName + ' ' + member?.lastName}</p>
                                  </div>
                                  <div className={Styles.column4}>
                                    <div className={Styles.deleteEntry} onClick={onProjectMemberDelete(member?.id)}>
                                      <i className="icon mbc-icon trash-outline" tooltip-data={'Delete'} />
                                    </div>
                                  </div>
                              </div>
                          );
                      })}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values: any) => handleCreateSubscription(values))}
            >
              Create Subscription
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default PromptCraftSubscriptionForm;