import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateBucket.scss';
import { useDispatch, useSelector } from 'react-redux';
import { bucketActions } from './redux/bucket.actions';
import AddUser from 'dna-container/AddUser';

import { useParams } from 'react-router-dom';
import { history } from '../../store/storeRoot';
import { ConnectionModal } from '../ConnectionInfo/ConnectionModal';

import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
import { hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { bucketsApi } from '../../apis/buckets.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

const CreateBucket = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { connect } = useSelector((state) => state.connectionInfo);
  const [bucketName, setBucketName] = useState('');
  const [bucketPermission, setBucketPermission] = useState({
    read: true,
    write: true,
  });
  const [showInfoModal, setInfoModal] = useState(false);

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [bucketNameError, setBucketNameError] = useState('');

  useEffect(() => {
    if (id) {
      ProgressIndicator.show();
      bucketsApi
        .getBucketByName(id)
        .then((res) => {
          if (res?.data?.permission.write) {
            setBucketName(res?.data?.bucketName);
            setBucketPermission(res?.data?.permission);
            setBucketCollaborators(res?.data?.collaborators);
          } else {
            // reset history to base page before accessing container app's public routes;
            history.replace('/');
            window.location.replace('#/unauthorised');
          }
          ProgressIndicator.hide();
        })
        .catch((e) => {
          Notification.show(
            e.response.data.errors?.length ? e.response.data.errors[0].message : 'Something went wrong!',
            'alert',
          );
          ProgressIndicator.hide();
          history.push('/');
        });
    }
  }, [id]);

  const goBack = () => {
    history.replace('/');
  };

  const onConnectionModalCancel = () => {
    history.push('/');
    dispatch(hideConnectionInfo());
  };

  const storageBucketValidation = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (bucketName === '') {
      setBucketNameError(errorMissingEntry);
      formValid = false;
    }
    if (bucketNameError) {
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);

    return formValid;
  };

  const handleBucketNameValidation = (value) => {
    if (value) {
      if (value?.length < 3) {
        setBucketNameError('Bucket name should be minimum 3 characters.');
      } else if (!/(^[a-z\d.-]*$)/g.test(value)) {
        setBucketNameError(
          'Bucket names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).',
        );
      } else if (!/^[a-z\d]/g.test(value)) {
        setBucketNameError('Bucket name must start with a lowercase letter or number.');
      } else if (/-$/.test(value)) {
        setBucketNameError('Bucket name must end with letter or a number.');
      } else if (/\.$/.test(value)) {
        setBucketNameError('Bucket name must end with letter or a number.');
      } else if (/\.+\./.test(value)) {
        setBucketNameError('Bucket name cant have consecutive dots.');
      } else if (/^(?:(?:^|\.)(?:2(?:5[0-5]|[0-4]\d)|1?\d?\d)){4}$/.test(value)) {
        setBucketNameError('Bucket name cant be an IP address.');
      } else {
        setBucketNameError('');
      }
    } else {
      setBucketNameError('');
    }
  };

  const onAddNewBucket = () => {
    if (storageBucketValidation()) {
      const permissions = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permissions.push(k);
      });

      const data = {
        bucketName: bucketName,
        collaborators: bucketCollaborators,
      };
      dispatch(bucketActions.createBucket(data));
    }
  };

  const onUpdateBucket = () => {
    if (storageBucketValidation()) {
      const permissions = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permissions.push(k);
      });

      const data = {
        bucketName: bucketName,
        collaborators: bucketCollaborators,
      };
      dispatch(bucketActions.updateBucket(data));
    }
  };

  const getCollabarators = (collaborators) => {
    const collabarationData = {
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      accesskey: collaborators.shortId,
      permission: { read: true, write: false },
    };
    bucketCollaborators.push(collabarationData);
    setBucketCollaborators([...bucketCollaborators]);
  };

  const onCollaboratorPermission = (e, userName) => {
    const bucketList = bucketCollaborators.find((item) => {
      return item.accesskey == userName;
    });

    if (e.target.checked) {
      bucketList.permission.write = true;
    } else {
      bucketList.permission.write = false;
    }
    setBucketCollaborators([...bucketCollaborators]);
  };

  const onCollabaratorDelete = (collId) => {
    return () => {
      const currentCollList = bucketCollaborators.filter((item) => {
        return item.accesskey !== collId;
      });
      setBucketCollaborators(currentCollList);
    };
  };

  const handleCheckbox = (e) => {
    if (e.target.checked) {
      setBucketPermission({
        ...bucketPermission,
        [e.target.name]: true,
      });
    } else {
      setBucketPermission({
        ...bucketPermission,
        [e.target.name]: false,
      });
    }
  };

  const handleInfoModal = () => {
    setInfoModal(true);
  };

  const bucketNameRulesContent = (
    <div>
      <ul>
        <li>Bucket names must be between 3 (min) and 63 (max) characters long.</li>
        <li>Bucket names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).</li>
        <li>Bucket names must begin and end with a letter or number.</li>
        <li>Bucket names must not be formatted as an IP address (for example, xxxxx).</li>
      </ul>
    </div>
  );

  const bucketNameErrorField = bucketNameError || '';

  return (
    <>
      <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={goBack}>
        Back
      </button>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>{id ? 'Edit' : 'Create New'} Bucket</h3>
          </div>
        </div>
        <div className={Styles.content}>
          <div className={Styles.formGroup}>
            <div
              className={classNames(Styles.inputGrp, ' input-field-group', bucketNameErrorField?.length ? 'error' : '')}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Name of Bucket <sup>*</sup>
              </label>
              <div>
                <input
                  type="text"
                  className={classNames('input-field', Styles.bucketNameField)}
                  required={true}
                  id="bucketName"
                  maxLength={63}
                  placeholder="Type here"
                  autoComplete="off"
                  onChange={(e) => {
                    setBucketName(e.target.value?.toLowerCase());
                    handleBucketNameValidation(e.target.value);
                  }}
                  defaultValue={bucketName}
                  readOnly={id}
                />
                <div style={{ width: '300px' }}>
                  <span className={classNames('error-message', bucketNameError?.length ? '' : 'hide')}>
                    {bucketNameError}
                  </span>
                </div>
              </div>
              {!id ? (
                <div className={Styles.infoIcon}>
                  <i className="icon mbc-icon info" onClick={handleInfoModal} />
                </div>
              ) : null}
            </div>
            <br />
            <div className={classNames(Styles.inputGrp, ' input-field-group')}>
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Permission <sup>*</sup>
              </label>
              <div className={Styles.checkboxContainer}>
                <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                  <span className="wrapper">
                    <input
                      name="read"
                      type="checkbox"
                      className="ff-only"
                      checked={!!bucketPermission?.read}
                      onChange={handleCheckbox}
                    />
                  </span>
                  <span className="label">Read</span>
                </label>
              </div>
              <div className={Styles.checkboxContainer}>
                <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                  <span className="wrapper">
                    <input
                      name="write"
                      type="checkbox"
                      className="ff-only"
                      checked={!!bucketPermission?.write}
                      onChange={handleCheckbox}
                    />
                  </span>
                  <span className="label">Write</span>
                </label>
              </div>
              <div></div>
            </div>
            <div className={classNames('input-field-group include-error')}>
              <div className={Styles.bucketColContent}>
                <div className={Styles.bucketColContentList}>
                  <div className={Styles.bucketColContentListAdd}>
                    <AddUser getCollabarators={getCollabarators} dagId={''} />
                  </div>
                  <div className={Styles.bucketColUsersList}>
                    {bucketCollaborators?.length > 0 ? (
                      <React.Fragment>
                        <div className={Styles.collUserTitle}>
                          <div className={Styles.collUserTitleCol}>User ID</div>
                          {!id ? <div className={Styles.collUserTitleCol}>Name</div> : null}
                          <div className={Styles.collUserTitleCol}>Permission</div>
                          <div className={Styles.collUserTitleCol}></div>
                        </div>
                        <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                          {bucketCollaborators?.map((item, collIndex) => {
                            return (
                              <div key={collIndex} className={Styles.collUserContentRow}>
                                <div className={Styles.collUserTitleCol}>{item.accesskey}</div>
                                {!id ? (
                                  <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                                ) : null}
                                <div className={Styles.collUserTitleCol}>
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="read"
                                          checked={true}
                                          readOnly
                                        />
                                      </span>
                                      <span className="label">Read</span>
                                    </label>
                                  </div>
                                  &nbsp;&nbsp;&nbsp;
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={'checkbox'}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="write"
                                          checked={item?.permission !== null ? item?.permission?.write : false}
                                          onChange={(e) => onCollaboratorPermission(e, item.accesskey)}
                                        />
                                      </span>
                                      <span className="label">Write</span>
                                    </label>
                                  </div>
                                </div>
                                <div className={Styles.collUserTitleCol}>
                                  <div className={Styles.deleteEntry} onClick={onCollabaratorDelete(item.accesskey)}>
                                    <i className="icon mbc-icon trash-outline" />
                                    Delete Entry
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </React.Fragment>
                    ) : (
                      <div className={Styles.bucketColContentEmpty}>
                        <h6> Collaborators Not Exist!</h6>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
            <div className={Styles.createBtn}>
              <button className={'btn btn-tertiary'} type="button" onClick={id ? onUpdateBucket : onAddNewBucket}>
                <span>{id ? 'Update' : 'Submit'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      {connect?.modal && (
        <InfoModal
          title="Connect"
          show={connect?.modal}
          hiddenTitle={true}
          content={<ConnectionModal />}
          onCancel={onConnectionModalCancel}
        />
      )}
      {showInfoModal && (
        <Modal
          title="Bucket Name Rules"
          show={showInfoModal}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          content={bucketNameRulesContent}
          onCancel={() => setInfoModal(false)}
        />
      )}
    </>
  );
};

export default CreateBucket;
