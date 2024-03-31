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

import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';
import Tags from 'dna-container/Tags';

import { matomoApi } from '../../apis/matamo.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { isValidURL } from '../../utilities/utils';

const CreateMatomo = ({ user }) => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const [siteName, setSiteName] = useState('');
  const [bucketPermission, setBucketPermission] = useState('admin');
  const [showInfoModal, setInfoModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [ownerId, setOwnerId] = useState('');

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [existingCollaboratorsUpdate, setExistingCollaboratorsUpdate] = useState([]);
  const [addCollaboratorsUpdate, setAddCollaboratorsUpdate] = useState([]);
  const [removeCollaboratorsUpdate, setRemoveCollaboratorsUpdate] = useState([]);
  const [siteNameError, setSiteNameError] = useState('');

  const [bucketId, setBucketId] = useState('');
  const [createdBy, setCreatedBy] = useState({});
  const [createdDate, setCreatedDate] = useState(new Date());

  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [dataClassification, setDataClassification] = useState('');
  const [dataClassificationError, setDataClassificationError] = useState('');
  const [PII, setPII] = useState(false);

  const [departments, setDepartments] = useState([]);
  const [departmentName, setDepartmentName] = useState('');
  const [departmentError, setDepartmentError] = useState('');
  const [url, setUrl] = useState('');
  const [urlError, setUrlError] = useState('');
  const [matomoDivision, setMatomoDivision] = useState('');
  const [matomoDivisionError, setMatomoDivisionError] = useState('');
  const [matomoSubDivision, setMatomoSubDivision] = useState('');
  // const [matomoSubDivisionError, setMatomoSubDivisionError] = useState('');
  const [statusValue, setStatusValue] = useState('');
  const [statusError, setStatusError] = useState('');
  const [permissionError, setPermissionError] = useState('');
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
  const [callOnGetByID, setCallOnGetByID] = useState(false);

  // const isSecretEnabled = Envs.ENABLE_DATA_CLASSIFICATION_SECRET;

  const isOwner = user.id === createdBy.id;


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
                if (id) {
                  ProgressIndicator.show();
                  matomoApi
                    .getMatomoById(id)
                    .then((res) => {
                      if (res?.data?.permission === 'admin') {
                        setSiteName(res?.data?.siteName);
                        setUrl(res?.data?.siteUrl);
                        setBucketPermission(res?.data?.permission);
                        setBucketCollaborators(res?.data?.collaborators || []);
                        setExistingCollaboratorsUpdate(JSON.parse(JSON.stringify(res?.data?.collaborators)) || []);
                        setDataClassification(res?.data?.classificationType);
                        // SelectBox.defaultSetup();
                        setPII(res?.data?.piiData);
                        setBucketId(res?.data?.id);
                        setCreatedBy(res?.data?.createdBy);
                        setCreatedDate(res?.data?.createdDate);
                        if (res?.data?.department) {
                          setDepartmentName([res?.data?.department]);
                        }
            
                        if (res?.data?.status) {
                          setStatusValue(res?.data.status);
                          // SelectBox.defaultSetup();
                        }
            
                        if (res?.data?.division) {
                          setMatomoDivision(res?.data?.division);
                          if(res?.data?.division > '0'){
                            hostServer.get('/subdivisions/' + res?.data?.division)
                            .then((res) => {
                              setSubDivisions(res?.data || []);
                            })
                            .finally(() => {
                              setMatomoDivision(res?.data?.division);
                              setMatomoSubDivision(res?.data?.subDivision);
                              setCallOnGetByID(true);
                              SelectBox.defaultSetup();  
                              ProgressIndicator.hide();
                            });
                          }
                          
                        }
            
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
                else{
                  ProgressIndicator.hide();
                }

            });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if(!callOnGetByID){
      const divId = matomoDivision;
      if (divId > '0') {
        ProgressIndicator.show();
        hostServer.get('/subdivisions/' + divId)
        .then((res) => {
          setSubDivisions(res?.data || []);
          SelectBox.defaultSetup();  
          ProgressIndicator.hide();
          setCallOnGetByID(false);
        });
      } else {
          setSubDivisions([]);
      }
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matomoDivision]);

  const goBack = () => {
    // history.replace('/');
    history.goBack();
  };

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
    if(url !== ''){
      if(!isValidURL(url)){
        setUrlError('Please provide valid url');
        formValid = false;
      }
    }
    
    if (matomoDivision === '0') {
        setMatomoDivisionError(errorMissingEntry);
        formValid = false;
    }
    // if (matomoSubDivision === '0') {
    //     setMatomoSubDivisionError(errorMissingEntry);
    //     formValid = false;
    // }
    if (statusValue === '0') {
      setStatusError(errorMissingEntry)
      formValid = false;
    }

    if(bucketCollaborators?.length > 0){
      bucketCollaborators?.map((item) => {
        if(!item.permission || !['view', 'write', 'admin'].includes(item.permission)){
          setPermissionError('Please provide permission to collaborators');
          formValid=false;
        }
      })
    }

    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);

    return formValid;
  };

  const onChangeStatus = (e) => {
    setStatusValue(e.target.value);
  }

  const handleURLChange = (value) => {
    setUrlError('');
    setUrl(value);
  }

  const checkURL = (e) => {
    if(!isValidURL(e.target.value)){
      setUrlError('Please provide valid url');
    }
  }

  const handleSiteNameValidation = (value) => {
    setSiteNameError('');
    setSiteName(value);
    // if (value) {
    //   if (value?.length < 3) {
    //     setSiteNameError('Site name should be minimum 3 characters.');
    //   } else if (!/(^[a-z\d.-]*$)/g.test(value)) {
    //     setSiteNameError(
    //       'Site names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).',
    //     );
    //   } else if (!/^[a-z\d]/g.test(value)) {
    //     setSiteNameError('Site name must start with a lowercase letter or number.');
    //   } else if (/-$/.test(value)) {
    //     setSiteNameError('Site name must end with letter or a number.');
    //   } else if (/\.$/.test(value)) {
    //     setSiteNameError('Site name must end with letter or a number.');
    //   } else if (/\.+\./.test(value)) {
    //     setSiteNameError('Site name cant have consecutive dots.');
    //   } else if (/^(?:(?:^|\.)(?:2(?:5[0-5]|[0-4]\d)|1?\d?\d)){4}$/.test(value)) {
    //     setSiteNameError('Site name cant be an IP address.');
    //   } else {
    //     setSiteNameError('');
    //   }
    // } else {
    //   setSiteNameError('');
    // }
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
      };
      dispatch(matomoActions.createMatomo(data));
    }
  };

  const onUpdateBucket = () => {
    if (storageBucketValidation()) {
      const permission = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permission.push(k);
      });

      user.permission = "admin";
      existingCollaboratorsUpdate.push(user);

      const data = {
        id: bucketId,
        createdBy,
        createdDate,
        siteName: siteName,
        existingCollaborators: existingCollaboratorsUpdate,
        addCollaborators: addCollaboratorsUpdate,
        removeCollaborators: removeCollaboratorsUpdate,
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
    }else if (bucketId) {
      bucketCollaborators.push(collabarationData);
      // addCollaboratorsUpdate.push(bucketCollaborators);
      setAddCollaboratorsUpdate([...addCollaboratorsUpdate,collabarationData]);
    } else {
      bucketCollaborators.push(collabarationData);
      setBucketCollaborators([...bucketCollaborators]);
    }
  };

  const onCollaboratorPermission = (e, userName) => {
    setPermissionError('');
    bucketCollaborators.map((item) => {
      if(item.id == userName)
      {
        item.permission = e.currentTarget.value;
      }
      return item;
    });
    existingCollaboratorsUpdate.map((item) => {
      if(item.id == userName)
      {
        item.permission = e.currentTarget.value;
      }
      return item;
    });
    addCollaboratorsUpdate.map((item) => {
      if(item.id == userName)
      {
        item.permission = e.currentTarget.value;
      }
      return item;
    });

    setBucketCollaborators([...bucketCollaborators]);
    setExistingCollaboratorsUpdate([...existingCollaboratorsUpdate]);
    setAddCollaboratorsUpdate([...addCollaboratorsUpdate]);
  };

  const onCollabaratorDelete = (collId) => {
    return () => {
      setPermissionError('');
      const currentCollList = bucketCollaborators.filter((item) => {
        return item.id !== collId;
      });
      if(bucketId){
        setAddCollaboratorsUpdate(addCollaboratorsUpdate.filter(item => item.id !== collId));
        setRemoveCollaboratorsUpdate([...removeCollaboratorsUpdate, bucketCollaborators.filter((item) => {
          if(item.id === collId){
            item.permission = 'noaccess';
            return item;
          }
        })[0]]);
      }
      setBucketCollaborators(currentCollList);
    };
  };

  const onTransferOwnership = (userId) => {
    setOwnerId(userId);
    // setShowConfirmModal(true);
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
    const selectedOptions = e.currentTarget.selectedOptions;
    const divId = selectedOptions[0].value
    setMatomoDivision(divId);
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
        <li>Site names must not be formatted as an IP address (for example, 1.1.1.1).</li>
      </ul>
    </div>
  );

  const bucketNameErrorField = siteNameError || '';

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
                        handleSiteNameValidation(e.target.value);
                      }}
                      defaultValue={siteName}
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
                        {divisions?.map((obj) => {
                          return (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                          )
                        })}
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
                      onBlur={(e) => checkURL(e)}
                      onChange={(e) => {
                        handleURLChange(e.target.value);
                      }}
                      defaultValue={url}
                    />
                    <span className={classNames('error-message', urlError?.length ? '' : 'hide')}>
                      {urlError}
                    </span>
                  </div>
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    // matomoSubDivisionError?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Sub Division 
                  </label>
                  <div className={classNames('custom-select')}>
                    
                    <select id="subDivisionField" 
                    onChange={handleSubDivision} 
                    value={matomoSubDivision}
                    required={false}
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
                  {/* <span className={classNames('error-message', matomoSubDivisionError?.length ? '' : 'hide')}>
                    {matomoSubDivisionError}
                  </span> */}
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
                      
                          <option id="classificationOption" value={0}>
                              Choose
                          </option>
                          {dataClassificationDropdown?.map((item) => (
                            <option
                              id={item.id}
                              key={item.id}
                              value={item.name}
                            >
                              {item.name}
                            </option>
                          ))}
                        
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
                            checked={bucketPermission === 'admin'}
                            onChange={handleCheckbox}
                          />
                        </span>
                        <span className="label">Admin</span>
                      </label>
                    </div>
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
                                            name={"permission-"+collIndex}
                                          />
                                        </span>
                                        <span className="label">View</span>
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
                                            name={"permission-"+collIndex}
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
                                            name={"permission-"+collIndex}
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
                                      <div className={classNames(Styles.deleteEntry,'hidden')} onClick={() => onTransferOwnership(item.id)}>
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
                    <span className={classNames('error-message', permissionError?.length ? '' : 'hide')}>
                      {permissionError}
                    </span>
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
