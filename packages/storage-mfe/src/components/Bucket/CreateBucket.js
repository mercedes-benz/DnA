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
import SelectBox from 'dna-container/SelectBox';

import { hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { bucketsApi } from '../../apis/buckets.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../Utility/envs';

const CreateBucket = ({ user }) => {
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

  const [bucketId, setBucketId] = useState('');
  const [createdBy, setCreatedBy] = useState({});
  const [createdDate, setCreatedDate] = useState(new Date());

  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [dataClassification, setDataClassification] = useState('Internal');
  const [dataClassificationError, setDataClassificationError] = useState('');
  const [PII, setPII] = useState(false);
  const [termsOfUse, setTermsOfUse] = useState(false);
  const [termsOfUseError, setTermsOfUseError] = useState(false);
  const [editAPIResponse, setEditAPIResponse] = useState({});

  const isSecretEnabled = Envs.ENABLE_DATA_CLASSIFICATION_SECRET;

  const isOwner = user.id === createdBy.id;

  useEffect(() => {
    ProgressIndicator.show();
    bucketsApi
      .getDataConnectionTypes()
      .then((res) => {
        setDataClassificationDropdown(res?.data?.data || []);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      })
      .catch(() => {
        ProgressIndicator.hide();
        setDataClassificationDropdown([{ id: '', name: 'Choose' }]);
        SelectBox.defaultSetup();
        Notification.show('Error while fetching Data Connection Type list', 'alert');
      });
  }, [isSecretEnabled]);

  useEffect(() => {
    if (id) {
      ProgressIndicator.show();
      bucketsApi
        .getBucketByName(id)
        .then((res) => {
          if (res?.data?.permission.write) {
            setBucketName(res?.data?.bucketName);
            setBucketPermission(res?.data?.permission);
            setBucketCollaborators(res?.data?.collaborators || []);
            setDataClassification(res?.data?.classificationType);
            setPII(res?.data?.piiData);
            setBucketId(res?.data?.id);
            setCreatedBy(res?.data?.createdBy);
            setCreatedDate(res?.data?.createdDate);
            setTermsOfUse(res?.data?.termsOfUse);
            setEditAPIResponse(res?.data); // store to compare whether the values are changed
            SelectBox.defaultSetup();
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

  useEffect(() => {
    // check whether values are changed while edit
    // if changed ensure user again accepts terms of use
    if (id) {
      if (
        dataClassification !== editAPIResponse.classificationType ||
        PII !== editAPIResponse.piiData ||
        bucketCollaborators?.length !== (editAPIResponse?.collaborators?.length || 0)
      ) {
        setTermsOfUse(false);
      }
    }
  }, [id, dataClassification, PII, bucketCollaborators, editAPIResponse]);

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
    if (dataClassification === 'Choose') {
      formValid = false;
      setDataClassificationError(errorMissingEntry);
    }
    if (!termsOfUse) {
      setTermsOfUseError('Please agree to terms of use');
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
        classificationType: dataClassification,
        piiData: PII,
        creator: user,
        termsOfUse: termsOfUse,
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
        id: bucketId,
        createdBy,
        createdDate,
        collaborators: bucketCollaborators,
        classificationType: dataClassification,
        piiData: PII,
        termsOfUse: termsOfUse,
      };
      dispatch(bucketActions.updateBucket(data));
    }
  };

  const getCollabarators = (collaborators) => {
    const collabarationData = {
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      accesskey: collaborators.shortId,
      department: collaborators.department,
      email: collaborators.email,
      mobileNumber: collaborators.mobileNumber,
      permission: { read: true, write: false },
    };

    let duplicateMember = false;
    duplicateMember = bucketCollaborators.filter((member) => member.accesskey === collaborators.shortId)?.length
      ? true
      : false;
    const isCreator = id ? createdBy.id === collaborators.shortId : user.id === collaborators.shortId;

    if (duplicateMember) {
      Notification.show('Collaborator Already Exist.', 'warning');
    } else if (isCreator) {
      Notification.show(
        `${collaborators.firstName} ${collaborators.lastName} is a creator. Creator can't be added as collaborator.`,
        'warning',
      );
    } else {
      bucketCollaborators.push(collabarationData);
      setBucketCollaborators([...bucketCollaborators]);
    }
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

  const handleDataClassification = (e) => {
    setDataClassification(e.target.value);
  };

  const handlePII = (e) => {
    setPII(e.target.value === 'true' ? true : false);
  };

  const bucketNameRulesContent = (
    <div>
      <ul>
        <li>Bucket names must be between 3 (min) and 63 (max) characters long.</li>
        <li>Bucket names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).</li>
        <li>Bucket names must begin and end with a letter or number.</li>
        <li>Bucket names must not be formatted as an IP address (for example, 192.168.4.2).</li>
      </ul>
    </div>
  );

  const bucketNameErrorField = bucketNameError || '';
  const termsOfUseErrorField = termsOfUseError || '';

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
            <div className={Styles.flexLayout}>
              <div>
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    bucketNameErrorField?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Name of Bucket <sup>*</sup>
                    {!id ? (
                      <div className={Styles.infoIcon}>
                        <i className="icon mbc-icon info" onClick={handleInfoModal} />
                      </div>
                    ) : null}
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
                    <span className={classNames('error-message', bucketNameError?.length ? '' : 'hide')}>
                      {bucketNameError}
                    </span>
                  </div>
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    dataClassificationError?.length ? 'error' : '',
                    id && !isOwner ? 'disabled' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Data Classification <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select', id && !isOwner ? 'disabled' : '')}>
                    <select id="reportStatusField" onChange={handleDataClassification} value={dataClassification}>
                      {dataClassificationDropdown?.length
                        ? dataClassificationDropdown?.map((item) => (
                            <option
                              disabled={item.name === 'Secret' && !isSecretEnabled}
                              id={item.id}
                              key={item.id}
                              value={item.name}
                            >
                              {item.name}
                            </option>
                          ))
                        : null}
                    </select>
                  </div>
                  <span className={classNames('error-message', dataClassificationError?.length ? '' : 'hide')}>
                    {dataClassificationError}
                  </span>
                </div>
              </div>
              <div>
                <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Permission <sup>*</sup>
                  </label>
                  <div className={Styles.permissionField}>
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
                  </div>
                </div>
                <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    PII (Personally Identifiable Information) <sup>*</sup>
                  </label>
                  <div className={Styles.pIIField}>
                    <label className={classNames('radio', id && !isOwner ? Styles.checkBoxDisable : '')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value={true}
                          name="pii"
                          onChange={handlePII}
                          checked={PII === true}
                        />
                      </span>
                      <span className="label">Yes</span>
                    </label>
                    <label className={classNames('radio', id && !isOwner ? Styles.checkBoxDisable : '')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value={false}
                          name="pii"
                          onChange={handlePII}
                          checked={PII === false}
                        />
                      </span>
                      <span className="label">No</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            <div className={classNames('input-field-group include-error')}>
              <div className={Styles.bucketColContent}>
                <div className={Styles.bucketColContentList}>
                  <div className={Styles.bucketColContentListAdd}>
                    <AddUser getCollabarators={getCollabarators} dagId={''} isRequired={false} isUserprivilegeSearch={false} />
                  </div>
                  <div className={Styles.bucketColUsersList}>
                    {bucketCollaborators?.length > 0 ? (
                      <React.Fragment>
                        <div className={Styles.collUserTitle}>
                          <div className={Styles.collUserTitleCol}>User ID</div>
                          <div className={Styles.collUserTitleCol}>Name</div>
                          <div className={Styles.collUserTitleCol}>Permission</div>
                          <div className={Styles.collUserTitleCol}></div>
                        </div>
                        <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                          {bucketCollaborators
                            ?.filter((item) => item.accesskey !== user.id && item.accesskey !== createdBy.id)
                            ?.map((item, collIndex) => {
                              return (
                                <div key={collIndex} className={Styles.collUserContentRow}>
                                  <div className={Styles.collUserTitleCol}>{item.accesskey}</div>
                                  <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
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
            <div className={classNames(Styles.termsOfUseContainer, termsOfUseErrorField?.length ? 'error' : '')}>
              <div className={Styles.termsOfUseContent}>
                <div>
                  <label className={classNames('checkbox', termsOfUseErrorField?.length ? 'error' : '')}>
                    <span className="wrapper">
                      <input
                        name="write"
                        type="checkbox"
                        className="ff-only"
                        checked={termsOfUse}
                        onChange={(e) => {
                          setTermsOfUse(e.target.checked);
                          e.target.checked
                            ? setTermsOfUseError('')
                            : setTermsOfUseError('Please agree to terms of use');
                        }}
                      />
                    </span>
                  </label>
                </div>
                <div
                  className={classNames(Styles.termsOfUseText)}
                  style={{
                    ...(termsOfUseErrorField?.length ? { color: '#e84d47' } : ''),
                  }}
                >
                  <div dangerouslySetInnerHTML={{ __html: Envs.TOU_HTML }}></div>
                  <sup>*</sup>
                </div>
              </div>
              <span
                style={{ marginTop: 0 }}
                className={classNames('error-message', termsOfUseError?.length ? '' : 'hide')}
              >
                {termsOfUseError}
              </span>
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
          content={<ConnectionModal user={user} />}
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
