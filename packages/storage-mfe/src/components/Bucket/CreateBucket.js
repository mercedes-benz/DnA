import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateBucket.scss';
import { useDispatch, useSelector } from 'react-redux';
import { bucketActions } from './redux/bucket.actions';
import AddUser from 'dna-container/AddUser';
import Tags from 'dna-container/Tags';

import { useParams } from 'react-router-dom';
import { history } from '../../store/storeRoot';
import { ConnectionModal } from '../ConnectionInfo/ConnectionModal';

import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import { hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { bucketsApi } from '../../apis/buckets.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../Utility/envs';
import { hostServer } from '../../server/api';

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
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [ownerId, setOwnerId] = useState('');

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [bucketNameError, setBucketNameError] = useState('');

  const [bucketId, setBucketId] = useState('');
  const [createdBy, setCreatedBy] = useState({});
  const [createdDate, setCreatedDate] = useState(new Date());

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);

  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [dataClassification, setDataClassification] = useState('Internal');
  const [dataClassificationError, setDataClassificationError] = useState('');
  const [PII, setPII] = useState(false);
  const [termsOfUse, setTermsOfUse] = useState(false);
  const [termsOfUseError, setTermsOfUseError] = useState(false);
  const [editAPIResponse, setEditAPIResponse] = useState({});

  const [typeOfProject, setTypeOfProject] = useState('0');
  const [typeOfProjectError, setTypeOfProjectError] = useState('');
  const isPlayground = typeOfProject === 'Playground';
  const [description, setDescription] = useState('');
  const [descriptionError, setDescriptionError] = useState('');

  const [division, setDivision] = useState('0');
  const [divisionError, setDivisionError] = useState('');

  const [subDivision, setSubDivision] = useState('0');

  const [department, setDepartment] = useState([]);
  const [departmentError, setDepartmentError] = useState(false);

  const [archerId, setArcherId] = useState('');
  const [archerIdError, setArcherIdError] = useState('');

  const [procedureID, setProcedureID] = useState('');
  const [procedureIDError, setProcedureIDError] = useState('');
  const [enablePublicAccess, setEnablePublicAccess] = useState(false);
  const [isOwner, setIsOwner] = useState(true);

  const isSecretEnabled = Envs.ENABLE_DATA_CLASSIFICATION_SECRET;
  const requiredError = '*Missing entry';


  const chips =
    department && department?.length
      ? department?.map((chip) => {
        return (
          <>
            <label className="chips">{chip}</label>
          </>
        );
      })
      : 'N/A';

  useEffect(() => {
    ProgressIndicator.show();
    bucketsApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDivisions(response[0]?.data || []);
        setDepartments(response[1]?.data?.data || []);
        setTimeout(() => {
          SelectBox.defaultSetup();
        }, 200);
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
      });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setIsOwner(createdBy?.id?.length ? user?.id === createdBy.id : true);
  },[createdBy])

  useEffect(() => {
    const divId = division.includes('@-@') ? division.split('@-@')[0] : division;
    if (divId && divId !== '0') {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + divId)
        .then((res) => {
          setSubDivisions(res?.data || []);
          ProgressIndicator.hide();
        }).catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
  }, [division]);

  useEffect(() => {
    setTimeout(() => {
      SelectBox.defaultSetup(true);
    }, 200);
  }, [typeOfProject]);

  useEffect(() => {
    subDivisions.length > 0 &&
      id && setSubDivision(editAPIResponse?.subDivisionId + '@-@' + editAPIResponse?.subDivision || '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
    setTimeout(() => {
      SelectBox.defaultSetup();
    }, 100);
  }, [subDivisions]);

  // useEffect(() => {
  //   setTimeout(() => {
  //     SelectBox.defaultSetup(true);
  //   }, 200);
  // }, [division, subDivision]);

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
            setDescription(res?.data?.description || '');
            setTypeOfProject(res?.data?.typeOfProject || '0');
            setDepartment(res?.data?.department ? [res?.data?.department] : []);
            setDivision(res?.data?.divisionId + '@-@' + res?.data?.division || '0');
            setSubDivision(res?.data?.subDivisionId + '@-@' + res?.data?.subDivision || '0');
            setArcherId(res?.data?.archerId || '');
            setProcedureID(res?.data?.procedureId || '');
            setEditAPIResponse(res?.data); // store to compare whether the values are changed
            setEnablePublicAccess(res?.data?.enablePublicAccess || false);
            setTimeout(() => {
              SelectBox.defaultSetup();
            }, 200);
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
        bucketCollaborators?.length !== (editAPIResponse?.collaborators?.length || 0) ||
        description !== editAPIResponse?.description ||
        typeOfProject !== editAPIResponse?.typeOfProject ||
        division.split('@-@')[0] !== editAPIResponse?.divisionId ||
        subDivision.split('@-@')[0] !== editAPIResponse?.subDivisionId ||
        department[0] !== editAPIResponse?.department ||
        archerId !== editAPIResponse?.archerId ||
        procedureID !== editAPIResponse.procedureId
      ) {
        setTermsOfUse(false);
      }
    }
  }, [id, dataClassification, PII, bucketCollaborators, editAPIResponse, description, typeOfProject, division, subDivision, department, archerId, procedureID]);

  const goBack = () => {
    // history.replace('/');
    history.goBack();
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
    if (isOwner && typeOfProject === '0') {
      setTypeOfProjectError(errorMissingEntry);
      formValid = false;
    }
    if (bucketNameError) {
      formValid = false;
    }
    if (isOwner && !description.length) {
      setDescriptionError(errorMissingEntry);
      formValid = false;
    }
    if (isOwner && !isPlayground && division === '0') {
      setDivisionError(errorMissingEntry);
      formValid = false;
    }
    if (isOwner && !department.length) {
      setDepartmentError(true);
      formValid = false;
    }
    if (isOwner && !isPlayground && dataClassification === 'Choose') {
      formValid = false;
      setDataClassificationError(errorMissingEntry);
    }
    if ( id && !termsOfUse) {
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
      }  else if(value.startsWith('dna-datalake')) {
        setBucketNameError('Reserved bucket name');
      }  else if (/-$/.test(value)) {
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
        description: description,
        typeOfProject: typeOfProject,
        divisionId: division?.split('@-@')[0] || '',
        division: division?.split('@-@')[1] || '',
        subDivisionId: subDivision?.split('@-@')[0] || '',
        subDivision: subDivision?.split('@-@')[1] || '',
        department: department[0],
        archerId: archerId,
        procedureId: procedureID,
       enablePublicAccess: enablePublicAccess,
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
        description: description,
        typeOfProject: typeOfProject,
        divisionId: division?.split('@-@')[0] || '',
        division: division?.split('@-@')[1] || '',
        subDivisionId: subDivision?.split('@-@')[0] || '',
        subDivision: subDivision?.split('@-@')[1] || '',
        department: department[0],
        archerId: archerId,
        procedureId: procedureID,
        enablePublicAccess: enablePublicAccess,
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

  const onTransferOwnership = (userId) => {
    setOwnerId(userId);
    setShowConfirmModal(true);
  };

  const onAcceptTransferOwnership = () => {
    ProgressIndicator.show();
    bucketsApi
      .transferOwnership(bucketName, ownerId)
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

  const handleInfoModal = () => {
    setInfoModal(true);
  };

  const handleDataClassification = (e) => {
    if (!e.target.value === dataClassification) {
      setEnablePublicAccess(false);
    }
    setDataClassification(e.target.value);
  };

  const onTypeOfProjectChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setTypeOfProject(selectedOption);
  };

  const onDescriptionChange = (e) => {
    const currentValue = e.currentTarget.value;
    setDescription(currentValue);
    setDescriptionError(currentValue.length !== 0 ? '' : requiredError);
  };

  const onDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setDivision(selectedOption);
  }

  const onSubDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setSubDivision(selectedOption);
  }

  const onArcherIdChange = (e) => {
    const currentValue = e.currentTarget.value;
    setArcherId(currentValue);
    const pattern = /^(INFO)-\d{5}$/.test(currentValue);
    setArcherIdError(currentValue.length && !pattern ? 'Archer ID should be of type INFO-XXXXX' : '');
  };

  const onProcedureIDChange = (e) => {
    const currentValue = e.currentTarget.value;
    setProcedureID(currentValue);
    const pattern = /^(PO|ITPLC)-\d{5}$/.test(currentValue);
    setProcedureIDError(currentValue.length && !pattern ? 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX' : '');
  };

  const handlePII = (e) => {
    setPII(e.target.value === 'true' ? true : false);
  };

  const handleEnablePublic = (e) => {
    setEnablePublicAccess(e.target.checked);
  }

  const bucketNameRulesContent = (
    <div>
      <ul>
        <li>Bucket names must be between 3 (min) and 63 (max) characters long.</li>
        <li>Bucket names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).</li>
        <li>Bucket names must begin and end with a letter or number.</li>
        <li>Bucket names must not be formatted as an IP address (for example, 1.1.1.1).</li>
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

              <div
                className={classNames('input-field-group include-error', typeOfProjectError?.length ? 'error' : '', id && !isOwner ? 'disabled' : '',)}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Type of Project <sup>*</sup>
                </label>
                <div className={classNames('custom-select', id && !isOwner ? 'disabled' : '',)}>
                  <select
                    id="reportStatusField"
                    defaultValue={typeOfProject}
                    required={true}
                    onChange={onTypeOfProjectChange}
                    value={typeOfProject}
                  >
                    <option id="typeOfProjectOption" value={0}>
                      Choose
                    </option>
                    {(!id || editAPIResponse?.typeOfProject === 'Playground' || !editAPIResponse?.typeOfProject) && <option value={'Playground'}>Playground</option>}
                    <option value={'Proof of Concept'}>Proof of Concept</option>
                    <option value={'Production'}>Production</option>
                  </select>
                </div>
                <p
                  style={{ color: 'var(--color-orange)' }}
                  className={classNames(typeOfProject !== 'Playground' ? ' hide' : '')}
                >
                  <i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of not
                  being used.
                </p>
                <span className={classNames('error-message', typeOfProjectError.length ? '' : 'hide')}>
                  {typeOfProjectError}
                </span>
              </div>

              <div
                className={classNames(
                  Styles.bucketNameInputField,
                  'input-field-group include-error',
                  bucketNameErrorField?.length ? 'error' : '',
                  id && !isOwner ? 'disabled' : '',
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

            </div>

            <div className={Styles.flexLayout}>

              <div
                className={classNames(
                  Styles.bucketNameInputField,
                  'input-field-group include-error',
                  departmentError ? 'error' : '',
                  id && !isOwner ? 'disabled' : '',
                )}
              >
                <div>
                  {id && !isOwner ? (<div
                    className={classNames('input-field-group disabled')}
                  >
                    <label id="description" className="input-label" htmlFor="description">
                      Department <sup>*</sup>
                    </label> <div>{chips}</div></div>) : (<div className={classNames(Styles.departmentTags)}>
                      <Tags
                        title={'Department'}
                        max={1}
                        chips={department}
                        tags={departments}
                        setTags={(selectedTags) => {
                          const dept = selectedTags?.map((item) => item.toUpperCase());
                          setDepartment(dept);
                          setDepartmentError(false);
                        }}
                        isMandatory={true}
                        showMissingEntryError={departmentError}
                      />

                    </div>)}
                </div>
              </div>

              <div className={classNames('input-field-group include-error', id && !isOwner ? 'disabled' : '',)}>
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

            </div>

            <div
              className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '', id && !isOwner ? 'disabled' : '',)}
            >
              <label id="description" className="input-label" htmlFor="description">
                Description <sup>*</sup>
              </label>
              <textarea
                id="description"
                className={classNames('input-field-area', id && !isOwner ? 'disabled' : '',)}
                type="text"
                defaultValue={description}
                required={true}
                onChange={onDescriptionChange}
                rows={50}
                readOnly={id && !isOwner}
              />
              <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
                {descriptionError}
              </span>
            </div>

            {!isPlayground && <div className={Styles.flexLayout}>

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
                  <select id="reportStatusField" onChange={(e)=>{handleDataClassification(e)}} value={dataClassification}>
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

              <div className={classNames('input-field-group include-error', id && !isOwner ? 'disabled' : '',)}>
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

            </div>}

            {!isPlayground && <div className={Styles.flexLayout}>

              <div
                className={classNames('input-field-group include-error',
                  divisionError.length ? 'error' : '', id && !isOwner ? 'disabled' : '',
                )}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Division <sup>*</sup>
                </label>
                <div className={classNames('custom-select', id && !isOwner ? 'disabled' : '',)}>
                  <select
                    id="divisionField"
                    defaultValue={division}
                    required={true}
                    required-error={requiredError}
                    onChange={(e)=>{onDivisionChange(e)}}
                    value={division}
                  >
                    <option id="divisionOption" value={0}>
                      Choose
                    </option>
                    {divisions?.map((obj) => {
                      return (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                          {obj.name}
                        </option>
                      )
                    })}
                  </select>
                </div>
                <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                  {divisionError}
                </span>
              </div>

              <div className={classNames('input-field-group include-error', id && !isOwner ? 'disabled' : '',)}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>Sub Division</label>
                <div className={classNames('custom-select', id && !isOwner ? 'disabled' : '',)}>
                  <select
                    id="subDivisionField"
                    defaultValue={subDivision}
                    value={subDivision}
                    required={false}
                    onChange={onSubDivisionChange}
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
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </>
                    )}
                  </select>
                </div>
              </div>

            </div>}

            <div className={Styles.flexLayout}>

              <div className={classNames('input-field-group include-error', archerIdError.length ? 'error' : '', id && !isOwner ? 'disabled' : '',)}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>Archer ID</label>
                <div>
                  <input
                    type="text"
                    className={classNames('input-field', Styles.projectNameField, id && !isOwner ? 'disabled' : '',)}
                    id="archerId"
                    placeholder="Type here eg.[INFO-XXXXX]"
                    autoComplete="off"
                    maxLength={55}
                    defaultValue={archerId}
                    onChange={onArcherIdChange}
                    readOnly={id && !isOwner}
                  />
                  <span className={classNames('error-message', archerIdError.length ? '' : 'hide')}>
                    {archerIdError}
                  </span>
                </div>
              </div>

              <div className={classNames('input-field-group include-error', procedureIDError.length ? 'error' : '', id && !isOwner ? 'disabled' : '',)}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>Procedure ID</label>
                <div>
                  <input
                    type="text"
                    className={classNames('input-field', Styles.projectNameField, id && !isOwner ? 'disabled' : '',)}
                    id="procedureID"
                    placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                    autoComplete="off"
                    maxLength={55}
                    defaultValue={procedureID}
                    onChange={onProcedureIDChange}
                    readOnly={id && !isOwner}
                  />
                  <span className={classNames('error-message', procedureIDError.length ? '' : 'hide')}>
                    {procedureIDError}
                  </span>
                </div>
              </div>

            </div>
            {dataClassification === 'Public' && (
              <div className={classNames('input-field-group include-error')}>
                <div className={classNames(Styles.accessSection)}>
                  <h3 className={classNames(Styles.title)}>Public Access</h3>
                  <label className={classNames('checkbox', Styles.accessCheckBox)}>
                    <span className="wrapper">
                      <input
                        name="enablePublicAccess"
                        type="checkbox"
                        className="ff-only"
                        checked={enablePublicAccess}
                        value={enablePublicAccess}
                        onChange={handleEnablePublic}
                      />
                    </span>
                    <span className="label">Enable public read access to the storage bucket</span>
                  </label>
                  {enablePublicAccess && (
                    <div className={Styles.acccessInfoSection}>
                      <div className={Styles.accessInfoCheckWrapper}>
                        <div className={Styles.infoWrapper}>
                          <i className={'icon mbc-icon info'} />
                        </div>
                        <div className={Styles.descriptionWrapper}>
                          <p><>Enabling public access for the storage bucket will make the contents of your storage bucket publicly accessible without any authentication. </></p>
                          <p>
                            {`You can use `}
                            <strong>
                              {`${Envs.BUCKET_PUBLIC_ACCESS_BASEURL}${bucketName?.length ? bucketName : 'YOUR_BUCKET_NAME'}`}
                            </strong>
                            {` base URL to read the contents from your bucket.`}
                          </p>
                          <p>Please make sure that you do not store any confidential data in this bucket as it is publicly accessible.</p>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
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
                                    {isOwner &&
                                      <div className={Styles.deleteEntry} onClick={() => onTransferOwnership(item.accesskey)}>
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
            {id && <div className={classNames(Styles.termsOfUseContainer, termsOfUseErrorField?.length ? 'error' : '')}>
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
            </div>}
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

export default CreateBucket;
