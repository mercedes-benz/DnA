import classNames from 'classnames';
import React, { useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
// styles
import Styles from './PromptCraftSubscriptionForm.scss';
// App components
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import AddUser from '../../../mbc/addUser/AddUser'; 
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';

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

  const [userLincenses, setUserLicenses] = useState([]);

  const getDevelopers = (developer: any) => {
    const userLicenseData = {
      userDetails: {
        id: developer?.shortId,
        firstName: developer?.firstName,
        lastName: developer?.lastName,
        department: developer?.department,
        mobileNumber: developer?.mobileNumber,
        email: developer?.email,
      },
      license: 'POWER-APPS-PREMIUM-USER'
    };

    let duplicateMember = false;
    duplicateMember = userLincenses?.filter((license) => license.userDetails.id === developer.shortId)?.length ? true : false;

    // const isCreator = user?.id === developer?.id;
    const isCreator = false;

    if (duplicateMember) {
        Notification.show('User License already added.', 'warning');
    } else if (isCreator) {
        Notification.show(
            `${developer.givenName} ${developer.surName} is a creator. Creator can't be added to user lincense.`,
            'warning',
        );
    } else {
        userLincenses?.push(userLicenseData);
        setUserLicenses([...userLincenses]);
    }
  }

  const onUserLicenseClick = (value: any, userId: any) => {
    const updatedUserLincenses = userLincenses.map(userLicense => {
      if (userLicense?.userDetails?.id === userId) {
        return Object.assign({}, userLicense, { license: value });
      }
      return userLicense;
    });
    setUserLicenses(updatedUserLincenses);
  };

  const onUserLicenseDelete = (userId: any) => {
    return () => {
      const updatedUserLicenses = userLincenses.filter((userLicense) => {
        return userLicense?.userDetails?.id !== userId;
      });
      setUserLicenses(updatedUserLicenses);
    };
  };

  const handleCreateSubscription = (values: any) => {
    ProgressIndicator.show();
    const data = {
      name: values.name.trim(),
      orgname: values.orgname.trim()
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
                <AddUser dagId='' getCollabarators={getDevelopers} isRequired={false} isUserprivilegeSearch={false} title={'User Licenses to Add'} />
              </div>
              {userLincenses?.length === 0 &&
                <div className={Styles.noLincense}>
                  <p>No User License Selected</p>
                </div>
              }
              <div>
                {userLincenses?.length > 0 && (
                  <>
                    <div className={Styles.colHeader}>
                        <div className={Styles.column1}>User ID</div>
                        <div className={Styles.column2}>Name</div>
                        <div className={Styles.column3}>License</div>
                        <div className={Styles.column4}></div>
                    </div>
                    <div>
                        {userLincenses?.map((userLicense) => {
                          return (
                              <div key={userLicense?.userDetails?.id} className={Styles.userRow}>
                                  <div className={Styles.column1}>
                                    <p>{userLicense?.userDetails?.id}</p>
                                  </div>
                                  <div className={Styles.column2}>
                                    <p>{userLicense?.userDetails?.firstName + ' ' + userLicense?.userDetails?.lastName}</p>
                                  </div>
                                  <div className={classNames(Styles.column3, Styles.lincenseContainer)}>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.id}
                                            value="POWER-VIRTUAL-AGENT-USER"
                                            onChange={() => onUserLicenseClick("POWER-VIRTUAL-AGENT-USER", userLicense?.userDetails?.id)}
                                          />
                                        </span>
                                        <span>Power Virtual Agent User</span>
                                      </label>
                                    </div>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.id}
                                            value="POWER-AUTOMATE-PREMIUM"
                                            onChange={() => onUserLicenseClick('POWER-AUTOMATE-PREMIUM', userLicense?.userDetails?.id)}
                                          />
                                        </span>
                                        <span>Power Automate Premium</span>
                                      </label>
                                    </div>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.id}
                                            value="POWER-APPS-PREMIUM-USER"
                                            defaultChecked={true}
                                            onChange={() => onUserLicenseClick('POWER-APPS-PREMIUM-USER', userLicense?.userDetails?.id)}
                                          />
                                        </span>
                                        <span>Power Apps Premium User</span>
                                      </label>
                                    </div>
                                  </div>
                                  <div className={Styles.column4}>
                                    <div className={Styles.deleteEntry} onClick={onUserLicenseDelete(userLicense?.userDetails?.id)}>
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
              onClick={handleSubmit((values) => handleCreateSubscription(values))}
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