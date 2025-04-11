import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider, Controller } from 'react-hook-form';
// styles
import Styles from './PromptCraftSubscriptionForm.scss';
// App components
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import AddUser from '../../../mbc/addUser/AddUser'; 
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';

export interface IPromptCraftSubscriptionFormProps {
  user: any;
  onSave: () => void;
}

const PromptCraftSubscriptionForm = ({ user, onSave }: IPromptCraftSubscriptionFormProps) => {
  const methods = useForm();
  const {
    control,
    register,
    handleSubmit,
    setValue,
    trigger,
    formState: { errors },
  } = methods;

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

    if (duplicateMember) {
        Notification.show('Member already added.', 'warning');
    } else {
        projectMembers?.push(projectMemberData);
        setProjectMembers([...projectMembers]);
    }
  }

  useEffect(() => {
    setValue('projectMembers', projectMembers);
    projectMembers?.length > 0 && trigger('projectMembers');
  }, [projectMembers]);

  const onProjectMemberDelete = (userId: any) => {
    return () => {
      const updatedProjectMembers = projectMembers.filter((member) => {
        return member?.id !== userId;
      });
      setProjectMembers(updatedProjectMembers);
    };
  };

  const handleCreateSubscription = (values: any) => {
    ProgressIndicator.show();
    const data = {
      projectName: values.name.trim(),
      orgName: values.orgName.trim(),
      projectMembers: projectMembers,
    };
    PromptCraftApiClient.createPromptCraftSubscription(data).then((res) => {
      onSave();
      ProgressIndicator.hide();
      Notification.show('Prompt Craft subscription request successfully placed');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.errors?.[0]?.message || 'Error while placing Prompt Craft subscription request',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>Approve Prompt Craft Subscripton</h3>
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
                  {...register('name', { required: '*Missing entry', minLength: 3 })}
                />
                <span className={'error-message'}>{errors?.name?.message}{errors?.name?.type === 'minLength' && 'Min 3 characters required'}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.orgName ? 'error' : '')}>
                <label className={'input-label'}>
                  Name of Organization <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('orgName', { required: '*Missing entry', minLength: 3 })}
                />
                <span className={'error-message'}>{errors?.orgName?.message}{errors?.orgName?.type === 'minLength' && 'Min 3 characters required'}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.projectMembers && 'error')}>
                <Controller
                  control={control}
                  name="projectMembers"
                  rules={{
                    validate: (value) => {
                      return value?.length > 0 || '*Missing entry';
                    },
                  }}
                  render={() => (
                    <AddUser dagId='' getCollabarators={getProjectMembers} isRequired={true} isUserprivilegeSearch={false} title={'Project Members'} />
                  )}
                />
                <span className={'error-message'}>{errors?.projectMembers?.message}</span>
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