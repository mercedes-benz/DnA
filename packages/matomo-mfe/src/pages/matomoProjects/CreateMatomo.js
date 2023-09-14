import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateMatomo.scss';
import { useDispatch, 
    // useSelector 
} from 'react-redux';
import { matomoActions } from '../redux/matomo.actions';
import AddUser from 'dna-container/AddUser';

import { useParams } from 'react-router-dom';
import { history } from '../../store/storeRoot';
import { hostServer } from '../../server/api';
// import { ConnectionModal } from '../ConnectionInfo/ConnectionModal';

import Modal from 'dna-container/Modal';
// import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';
import Tags from 'dna-container/Tags';

// import { hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { matomoApi } from '../../apis/matamo.api';
// import { bucketsApi } from '../../apis/buckets.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// import { Envs } from '../Utility/envs';

const CreateMatomo = ({ user }) => {
  const { id } = useParams();
  const dispatch = useDispatch();
//   const { connect } = useSelector((state) => state.connectionInfo);
  const [siteName, setSiteName] = useState('');
  const [bucketPermission, setBucketPermission] = useState({
    read: true,
    write: true,
    admin: true
  });
  const [showInfoModal, setInfoModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [ownerId, setOwnerId] = useState('');

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [siteNameError, setSiteNameError] = useState('');

  const [bucketId, setBucketId] = useState('');
  const [createdBy, setCreatedBy] = useState({});
  const [createdDate, setCreatedDate] = useState(new Date());

  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [dataClassification, setDataClassification] = useState('');
  const [dataClassificationError, setDataClassificationError] = useState('');
  const [PII, setPII] = useState(false);
  // const [termsOfUse, setTermsOfUse] = useState(false);
//   const [termsOfUseError, setTermsOfUseError] = useState(false);
  // const [editAPIResponse, setEditAPIResponse] = useState({});

  const [departments, setDepartments] = useState([]);
  const [departmentName, setDepartmentName] = useState('');
  const [departmentError, setDepartmentError] = useState('');
  const [url, setUrl] = useState('');
  const [urlError, setUrlError] = useState('');
  const [matomoDivision, setMatomoDivision] = useState('');
  const [matomoDivisionError, setMatomoDivisionError] = useState('');
  const [matomoSubDivision, setMatomoSubDivision] = useState('');
  const [matomoSubDivisionError, setMatomoSubDivisionError] = useState('');
  const [statusValue, setStatusValue] = useState('');
  const [statusError, setStatusError] = useState('');
  const statuses = [{
    id: 1,
    name: 'Active'
    }, {
        id: 2,
        name: 'In development'
    }, {
        id: 3,
        name: 'Sundowned'
  }];
  
  
  
  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);

  // const isSecretEnabled = Envs.ENABLE_DATA_CLASSIFICATION_SECRET;

  const isOwner = user.id === createdBy.id;

  useEffect(() => {
    // setDivisions([]);
    // setSubDivisions([]);
    // setDepartments([]);
  }, []);  

  useEffect(() => {
    ProgressIndicator.show();
    matomoApi.getLovData()
            .then((response) => {
                setDataClassificationDropdown(response[0]?.data?.data || []);
                setDivisions(response[1]?.data || []);
                setDepartments(response[2]?.data?.data || []);
                SelectBox.defaultSetup();
            })
            .catch((err) => {
                ProgressIndicator.hide();
                SelectBox.defaultSetup();
                if (err?.response?.data?.errors?.length > 0) {
                    err?.response?.data?.errors.forEach((err) => {
                        Notification.show(err?.message || 'Something went wrong.', 'alert');
                    });
                } else {
                    Notification.show(err?.message || 'Something went wrong.', 'alert');
                }
            })
            .finally(() => {
                // validateUser(props?.user?.id);
                
                ProgressIndicator.hide();
            });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const id = matomoDivision;
    if (id > '0') {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + id).then((res) => {
        setSubDivisions(res?.data || []);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      });
    } else {
        setSubDivisions([]);
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matomoDivision]);

  // useEffect(() => {
  //   ProgressIndicator.show();
  //   matomoApi
  //     .getDataConnectionTypes()
  //     .then((res) => {
  //       setDataClassificationDropdown(res?.data?.data || []);
  //       SelectBox.defaultSetup();
  //       ProgressIndicator.hide();
  //     })
  //     .catch(() => {
  //       ProgressIndicator.hide();
  //       setDataClassificationDropdown([{ id: '', name: 'Choose' }]);
  //       SelectBox.defaultSetup();
  //       Notification.show('Error while fetching Data Connection Type list', 'alert');
  //     });
  // }, [isSecretEnabled]);

  useEffect(() => {
    if (id) {
      ProgressIndicator.show();
      matomoApi
        .getMatomoById(id)
        .then((res) => {
          if (res?.data?.permission === 'admin') {
            setSiteName(res?.data?.siteName);
            setBucketPermission(res?.data?.permission);
            setBucketCollaborators(res?.data?.collaborators || []);
            setDataClassification(res?.data?.classificationType);
            setPII(res?.data?.piiData);
            setBucketId(res?.data?.id);
            setCreatedBy(res?.data?.createdBy);
            setCreatedDate(res?.data?.createdDate);
            if (res?.data?.department) {
              setDepartmentName([res?.data?.department]);
            }

            if (res?.data?.status) {
              setStatusValue(res?.data.status);
            }

            if (res?.data?.division) {
              setMatomoDivision(res?.data?.division);
            }

            if (res?.data?.subDivision) {
              setMatomoSubDivision(res?.data?.subDivision);
            }

            // setTermsOfUse(res?.data?.termsOfUse);
            // setEditAPIResponse(res?.data); // store to compare whether the values are changed
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

  // useEffect(() => {
  //   // check whether values are changed while edit
  //   // if changed ensure user again accepts terms of use
  //   if (id) {
  //     if (
  //       dataClassification !== editAPIResponse.classificationType ||
  //       PII !== editAPIResponse.piiData ||
  //       bucketCollaborators?.length !== (editAPIResponse?.collaborators?.length || 0)
  //     ) {
  //       setTermsOfUse(false);
  //     }
  //   }
  // }, [id, dataClassification, PII, bucketCollaborators, editAPIResponse]);

  const goBack = () => {
    // history.replace('/');
    history.goBack();
  };

//   const onConnectionModalCancel = () => {
//     history.push('/');
//     dispatch(hideConnectionInfo());
//   };

  const storageBucketValidation = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (siteName === '') {
      setSiteNameError(errorMissingEntry);
      formValid = false;
    }
    if (siteNameError) {
      formValid = false;
    }
    if (dataClassification === '0') {
      formValid = false;
      setDataClassificationError(errorMissingEntry);
    }
    if (departmentName === '') {
        setDepartmentError(errorMissingEntry);
        formValid = false;
    }
    if (url === '') {
        setUrlError(errorMissingEntry);
        formValid = false;
    }
    if (matomoDivision === '0') {
        setMatomoDivisionError(errorMissingEntry);
        formValid = false;
    }
    if (matomoSubDivision === '0') {
        setMatomoSubDivisionError(errorMissingEntry);
        formValid = false;
    }
    if (statusValue === '0') {
      setStatusError(errorMissingEntry)
      formValid = false;
    }
    // if (!matomoDivision || matomoDivision?.name === 'Choose') {
    //     setMatomoDivisionError(errorMissingEntry)
    //     formValid = false;
    // }
    // if (!termsOfUse) {
    // //   setTermsOfUseError('Please agree to terms of use');
    //   formValid = false;
    // }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);

    return formValid;
  };

  const onChangeStatus = (e) => {
    setStatusValue(e.target.value);
  }

  const handleSiteNameValidation = (value) => {
    if (value) {
      if (value?.length < 3) {
        setSiteNameError('Site name should be minimum 3 characters.');
      } else if (!/(^[a-z\d.-]*$)/g.test(value)) {
        setSiteNameError(
          'Site names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).',
        );
      } else if (!/^[a-z\d]/g.test(value)) {
        setSiteNameError('Site name must start with a lowercase letter or number.');
      } else if (/-$/.test(value)) {
        setSiteNameError('Site name must end with letter or a number.');
      } else if (/\.$/.test(value)) {
        setSiteNameError('Site name must end with letter or a number.');
      } else if (/\.+\./.test(value)) {
        setSiteNameError('Site name cant have consecutive dots.');
      } else if (/^(?:(?:^|\.)(?:2(?:5[0-5]|[0-4]\d)|1?\d?\d)){4}$/.test(value)) {
        setSiteNameError('Site name cant be an IP address.');
      } else {
        setSiteNameError('');
      }
    } else {
      setSiteNameError('');
    }
  };

  const onAddNewBucket = () => {
    if (storageBucketValidation()) {
      const permission = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permission.push(k);
      });

      const data = {
        siteName: siteName,
        collaborators: bucketCollaborators,
        classificationType: dataClassification,
        piiData: PII,
        department: departmentName[0],
        division: matomoDivision,
        permission: "admin",
        siteUrl: url,
        status: statusValue,
        subDivision: matomoSubDivision
        // createdBy: user,
        // termsOfUse: termsOfUse,
      };
      dispatch(matomoActions.createMatomo(data));
      history.replace('/');
    }
  };

  const onUpdateBucket = () => {
    if (storageBucketValidation()) {
      const permission = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permission.push(k);
      });

      const data = {
        id: bucketId,
        createdBy,
        createdDate,
        siteName: siteName,
        collaborators: bucketCollaborators,
        classificationType: dataClassification,
        piiData: PII,
        department: departmentName[0],
        division: matomoDivision,
        permission: "admin",
        siteUrl: url,
        status: statusValue,
        subDivision: matomoSubDivision
      };
      dispatch(matomoActions.updateMatomo(data));
    }
  };

  const getCollabarators = (collaborators) => {
    const collabarationData = {
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      id: collaborators.shortId,
      department: collaborators.department,
      email: collaborators.email,
      mobileNumber: collaborators.mobileNumber,
      // permission: { view: true, write: false, admin: false },
      permission: collaborators.permission,
    };

    let duplicateMember = false;
    duplicateMember = bucketCollaborators.filter((member) => member.id === collaborators.shortId)?.length
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
      return item.id == userName;
    });
    bucketList.permission = e.target.value;
    // if (e.target.checked) {
    //   bucketList.permission.write = true;
    // } else {
    //   bucketList.permission.write = false;
    // }
    setBucketCollaborators([...bucketCollaborators]);
  };

  const onCollabaratorDelete = (collId) => {
    return () => {
      const currentCollList = bucketCollaborators.filter((item) => {
        return item.id !== collId;
      });
      setBucketCollaborators(currentCollList);
    };
  };

  const onTransferOwnership = (userId) => {
    setOwnerId(userId);
    setShowConfirmModal(true);
  };

  const onAcceptTransferOwnership = () => {
    ProgressIndicator.show();
    matomoApi
      .transferOwnership(siteName, ownerId)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show('Ownership transferred successfully.');
        setShowConfirmModal(false);
        history.push('/');
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while transferring ownership', 'alert');
      });
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

//   const handleInfoModal = () => {
//     setInfoModal(true);
//   };

  const handleDataClassification = (e) => {
    setDataClassification(e.target.value);
  };

  const handleDivision = (e) => {
    setMatomoDivision(e.target.value);
  };

  const handleSubDivision = (e) => {
    setMatomoSubDivision(e.target.value);
  };

  const handlePII = (e) => {
    setPII(e.target.value === 'true' ? true : false);
  };

  const bucketNameRulesContent = (
    <div>
      <ul>
        <li>Site names must be between 3 (min) and 63 (max) characters long.</li>
        <li>Site names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).</li>
        <li>Site names must begin and end with a letter or number.</li>
        <li>Site names must not be formatted as an IP address (for example, 192.168.4.2).</li>
      </ul>
    </div>
  );

  const bucketNameErrorField = siteNameError || '';
//   const termsOfUseErrorField = termsOfUseError || '';

  return (
    <>
      <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={goBack}>
        Back
      </button>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>{id ? 'Edit' : 'Create a new'} tracking website</h3>
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
                    Site Name <sup>*</sup>
                    {/* {!id ? (
                      <div className={Styles.infoIcon}>
                        <i className="icon mbc-icon info" onClick={handleInfoModal} />
                      </div>
                    ) : null} */}
                  </label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field', Styles.bucketNameField)}
                      required={true}
                      id="siteName"
                      maxLength={63}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={(e) => {
                        setSiteName(e.target.value?.toLowerCase());
                        handleSiteNameValidation(e.target.value);
                      }}
                      defaultValue={siteName}
                      readOnly={id}
                    />
                    <span className={classNames('error-message', siteNameError?.length ? '' : 'hide')}>
                      {siteNameError}
                    </span>
                  </div>
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    matomoDivisionError?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Division <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select')}>
                  <select
                        id="divisionField"
                        required={true}
                        required-error={'*Missing entry'}
                        onChange={handleDivision} 
                        value={matomoDivision}
                    >
                        <option id="divisionOption" value={0}>
                          Choose
                        </option>
                        {divisions?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                  </div>
                  <span className={classNames('error-message', matomoDivisionError?.length ? '' : 'hide')}>
                    {matomoDivisionError}
                  </span>
                </div>
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    departmentError?.length ? 'error' : '',
                  )}
                >
                  <div>
                    <div className={Styles.departmentTags}>
                    
                        <Tags
                        title={'E2-Department'}
                        max={1}
                        chips={departmentName}
                        tags={departments}
                        setTags={(selectedTags) => {
                        let dept = selectedTags?.map((item) => item.toUpperCase());
                        setDepartmentName(dept);
                        setDepartmentError('');
                        }}
                        isMandatory={true}
                        showMissingEntryError={departmentError}
                        />
                         
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    urlError?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    URL&apos;S <sup>*</sup>
                    {/* {!id ? (
                      <div className={Styles.infoIcon}>
                        <i className="icon mbc-icon info" onClick={handleInfoModal} />
                      </div>
                    ) : null} */}
                  </label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field', Styles.bucketNameField)}
                      required={true}
                      id="url"
                      maxLength={63}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={(e) => {
                        setUrl(e.target.value);
                        // handleBucketNameValidation(e.target.value);
                      }}
                      defaultValue={url}
                      readOnly={id}
                    />
                    <span className={classNames('error-message', urlError?.length ? '' : 'hide')}>
                      {urlError}
                    </span>
                  </div>
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    matomoSubDivisionError?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Sub Division <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select')}>
                    
                    <select id="subDivisionField" 
                    onChange={handleSubDivision} 
                    value={matomoSubDivision}
                    required={true}
                    >
                        {subDivisions?.some((item) => item.id === '0' && item.name === 'None') ? (
                          <option id="subDivisionDefault" value={0}>
                            None
                          </option>
                        ) : (
                          <>
                            <option id="subDivisionDefault" value={0}>
                              Choose
                            </option>
                            {subDivisions?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                {obj.name}
                              </option>
                            ))}
                          </>
                        )}
                    </select>
                    
                  </div>
                  <span className={classNames('error-message', matomoSubDivisionError?.length ? '' : 'hide')}>
                    {matomoSubDivisionError}
                  </span>
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    statusError?.length ? 'error' : '',
                    id && !isOwner ? 'disabled' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Status <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select', id && !isOwner ? 'disabled' : '')}>
                    <select id="reportStatusField" 
                    onChange={onChangeStatus} 
                    value={statusValue}
                    required={true}
                    >
                      {statuses?.length
                      ?           
                      <>
                        <option id="reportStatusOption" value={0}>
                            Choose
                        </option>
                        {statuses?.map((obj) => (
                            <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                            </option>
                        ))}
                      </>
                        : null}
                    </select>
                  </div>
                  <span className={classNames('error-message', statusError?.length ? '' : 'hide')}>
                    {statusError}
                  </span>
                </div>
              </div>
              <div>
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
                    <select id="classificationField" 
                    onChange={handleDataClassification} 
                    value={dataClassification}
                    required={true}
                    >
                      {dataClassificationDropdown?.length
                        ?<>
                          <option id="classificationOption" value={0}>
                              Choose
                          </option>
                          {dataClassificationDropdown?.map((item) => (
                            <option
                              // disabled={item.name === 'Secret' && !isSecretEnabled}
                              id={item.id}
                              key={item.id}
                              value={item.name}
                            >
                              {item.name}
                            </option>
                          ))}
                        </> 
                        : null}
                    </select>
                  </div>
                  <span className={classNames('error-message', dataClassificationError?.length ? '' : 'hide')}>
                    {dataClassificationError}
                  </span>
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
                            checked={!!bucketPermission?.admin}
                            onChange={handleCheckbox}
                          />
                        </span>
                        {/* <span className="label">Read</span> */}
                        <span className="label">Admin</span>
                      </label>
                    </div>
                    {/* <div className={Styles.checkboxContainer}>
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
                    </div> */}
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
                          {/* <div className={Styles.collUserTitleCol}>Name</div> */}
                          <div className={Styles.collUserTitleCol}>Permission</div>
                          <div className={Styles.collUserTitleCol}></div>
                        </div>
                        <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                          {bucketCollaborators
                            ?.filter((item) => item.id !== user.id && item.id !== createdBy.id)
                            ?.map((item, collIndex) => {
                              return (
                                <div key={collIndex} className={Styles.collUserContentRow}>
                                  <div className={Styles.collUserTitleCol}>{item.id}</div>
                                  {/* <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div> */}
                                  <div className={Styles.collUserTitleCol}>
                                    <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                      <label className={classNames('checkbox', 
                                      // Styles.checkBoxDisable
                                      )}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            value="view"
                                            checked={item?.permission == 'view' ? true : false}
                                            onChange={(e) => onCollaboratorPermission(e, item.id)}
                                            // readOnly
                                            name="view"
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
                                            type="radio"
                                            className="ff-only"
                                            value="write"
                                            checked={item?.permission == 'write' ? true : false}
                                            onChange={(e) => onCollaboratorPermission(e, item.id)}
                                            name="write"
                                          />
                                        </span>
                                        <span className="label">Write</span>
                                      </label>
                                    </div>
                                    &nbsp;&nbsp;&nbsp;
                                    <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            value="admin"
                                            checked={item?.permission == 'admin' ? true : false}
                                            onChange={(e) => onCollaboratorPermission(e, item.id)}
                                            name="admin"
                                          />
                                        </span>
                                        <span className="label">Admin</span>
                                      </label>
                                    </div>
                                  </div>
                                  <div className={Styles.collUserTitleCol}>
                                    <div className={Styles.deleteEntry} onClick={onCollabaratorDelete(item.id)}>
                                      <i className="icon mbc-icon trash-outline" />
                                      Delete Entry
                                    </div>
                                    {isOwner && 
                                      <div className={Styles.deleteEntry} onClick={() => onTransferOwnership(item.id)}>
                                        <i className="icon mbc-icon comparison" />
                                        Transfer Ownership
                                      </div>
                                    }
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
            {/* <div className={classNames(Styles.termsOfUseContainer, termsOfUseErrorField?.length ? 'error' : '')}>
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
            </div> */}
            <div className={Styles.createBtn}>
              <button className={'btn btn-tertiary'} type="button" onClick={id ? onUpdateBucket : onAddNewBucket}>
                <span>{id ? 'Update' : 'Submit'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      {/* {connect?.modal && (
        <InfoModal
          title="Connect"
          show={connect?.modal}
          hiddenTitle={true}
          content={<ConnectionModal user={user} />}
          onCancel={onConnectionModalCancel}
        />
      )} */}
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
      {showConfirmModal && (
        <ConfirmModal
          title={'Transfer Ownership'}
          showAcceptButton={false}
          showCancelButton={false}
          show={showConfirmModal}
          removalConfirmation={true}
          showIcon={false}
          showCloseIcon={true}
          content={
            <div className={Styles.transferOwnership}>
              <div className={Styles.transferIcon}>
                <i className={classNames('icon mbc-icon comparison')} />
              </div>
              <div>
                You are going to transfer ownership.<br />You will no longer be the owner of this bucket.<br />
                Are you sure you want to proceed?
              </div>
              <div className={Styles.yesBtn}>
                <button
                  className={'btn btn-secondary'}
                  type="button"
                  onClick={onAcceptTransferOwnership}
                >
                  Yes
                </button>
              </div>
            </div>
          }
          onCancel={() => setShowConfirmModal(false)}
        />
      )}
    </>
  );
};

export default CreateMatomo;
