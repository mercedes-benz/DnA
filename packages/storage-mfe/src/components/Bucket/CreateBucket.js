import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateBucket.scss';
import { useDispatch, useSelector } from 'react-redux';
import { bucketActions } from '../redux/bucket.actions';
import AddUser from 'dna-container/AddUser';

import { useParams } from 'react-router-dom';
import { history } from '../../store/storeRoot';
import { ConnectionModal } from './ConnectionModal';

import InfoModal from 'dna-container/InfoModal';
import { hideConnectionInfo } from './ConnectionInfo/redux/connection.actions';

const CreateBucket = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { bucketList } = useSelector((state) => state.bucket);
  const { connect } = useSelector((state) => state.connectionInfo);
  const [bucketName, setBucketName] = useState('');
  const [bucketPermission, setBucketPermission] = useState({
    read: true,
    write: true,
  });

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [bucketNameError, setBucketNameError] = useState('');

  const onSubmissionModalCancel = () => {
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
    return formValid;
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
      dispatch(bucketActions.setBucketList(data));
    }
  };
  const onUpdateBucket = () => {
    if (storageBucketValidation()) {
      const permissions = [];
      Object.entries(bucketPermission)?.forEach(([k, v]) => {
        if (v === true) permissions.push(k);
      });
      const bucketItem = bucketList?.find((item) => item.id === id);

      const data = {
        id: bucketItem.id,
        name: bucketName,
        createdOn: '2022-03-01',
        modifiedOn: '2022-03-23',
        permission: permissions,
        collaborators: bucketCollaborators,
        tab: {
          key_access: 'kKjksjdkjsd',
          key_secret: 'Asdjfskjd',
          url: 'http://www.abc.com/sda/dasdas',
        },
      };

      dispatch({
        type: 'UPDATE_BUCKET',
        payload: data,
      });
      history.push('/');
    }
  };

  const getCollabarators = (collaborators) => {
    const collabarationData = {
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

  useEffect(() => {
    // window.location.replace('#/unauthorised');
    if (id) {
      const bucketInfo = bucketList?.find((item) => item.bucketName === id);
      setBucketName(bucketInfo?.bucketName);
      setBucketPermission(bucketInfo?.permission);
      setBucketCollaborators(bucketInfo?.collaborators);
    }
  }, [bucketList, id]);

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

  const bucketNameErrorField = bucketNameError || '';

  return (
    <>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>{id ? 'Edit' : 'Create New'} Bucket</h3>
          </div>
        </div>
        <div className={Styles.content}>
          <div className={Styles.formGroup}>
            <div
              className={classNames(Styles.inputGrp, ' input-field-group', bucketNameErrorField.length ? 'error' : '')}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Name of Buckets <sup>*</sup>
              </label>
              <div>
                <input
                  type="text"
                  className="input-field"
                  required={true}
                  id="bucketName"
                  maxLength={64}
                  placeholder="Type here"
                  autoComplete="off"
                  onChange={(e) => {
                    setBucketName(e.target.value?.toLowerCase());
                    if (e.target.value) setBucketNameError('');
                  }}
                  defaultValue={bucketName}
                  readOnly={id}
                />
                <div>
                  <span className={classNames('error-message', bucketNameError.length ? '' : 'hide')}>
                    {bucketNameError}
                  </span>
                </div>
              </div>
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
                          <div className={Styles.collUserTitleCol}>Name</div>
                          <div className={Styles.collUserTitleCol}>Permission</div>
                          <div className={Styles.collUserTitleCol}></div>
                        </div>
                        <div className={Styles.collUserContent}>
                          {bucketCollaborators?.map((item, collIndex) => {
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
            <div className={Styles.createBtn}>
              <button className={'btn btn-tertiary'} type="button" onClick={id ? onUpdateBucket : onAddNewBucket}>
                <span>{id ? 'Update' : 'Submit'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      {connect?.modal && (
        <InfoModal title={''} show={connect?.modal} content={<ConnectionModal />} onCancel={onSubmissionModalCancel} />
      )}
    </>
  );
};

export default CreateBucket;
