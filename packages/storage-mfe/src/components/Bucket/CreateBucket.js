import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateBucket.scss';
import { useDispatch, useSelector } from 'react-redux';
import { bucketActions } from '../redux/bucket.actions';
import AddUser from 'dna-container/AddUser';

import { useParams } from 'react-router-dom';
import { history } from '../../store/storeRoot';
import { ConnectionModal } from './ConnectionModal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

const InfoModal = React.lazy(() => import('dna-container/InfoModal'));

const CreateBucket = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { bucketList, isLoading, submission } = useSelector((state) => state.bucket);
  const [bucketName, setBucketName] = useState('');
  const [bucketPermission, setBucketPermission] = useState({
    read: true,
    write: true,
  });

  const [bucketCollaborators, setBucketCollaborators] = useState([]);
  const [bucketNameError, setBucketNameError] = useState('');
  const [bucketPermissionError, setBucketPermissionError] = useState('');

  useEffect(() => {
    if (isLoading) {
      ProgressIndicator.show();
    } else {
      ProgressIndicator.hide();
    }
  }, [isLoading]);

  const onSubmissionModalCancel = () => {
    history.push('/');
    dispatch({
      type: 'SUBMISSION_MODAL',
      payload: {
        bucketId: '',
        modal: false,
      },
    });
  };

  const storageBucketValidation = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (bucketName === '') {
      setBucketNameError(errorMissingEntry);
      formValid = false;
    }

    if (bucketPermission.read || bucketPermission.write) {
      setBucketPermissionError('');
    } else {
      // setBucketPermissionError(errorMissingEntry);
      // formValid = false;
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
        id: `${Math.floor(Math.random() * 100)}`,
        name: bucketName,
        createdOn: '2022-03-01',
        modifiedOn: '2022-03-23',
        permission: permissions,
        collaborators: bucketCollaborators,
        tab: {
          key_access: 'AsdjfskjkKjksjdkjsd',
          key_secret: 'AsdjfskjkKjksjdkjsd',
          url: 'http://www.abc.com/sda/dasdas/dasd/asda//asdasd/asd/asdas/',
        },
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
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      email: collaborators.email,
      username: collaborators.shortId,
      permissions: ['can_coll_read', 'can_coll_write'],
    };
    bucketCollaborators.push(collabarationData);
    setBucketCollaborators([...bucketCollaborators]);
  };

  const onCollaboratorPermission = (userName) => {
    const bucketList = bucketCollaborators.find((item) => {
      return item.username == userName;
    });

    if (bucketList.permissions.includes('can_coll_write')) {
      bucketList.permissions.splice(bucketList.permissions.indexOf('can_coll_write'), 1);
    } else {
      bucketList.permissions.push('can_coll_write');
    }

    setBucketCollaborators([...bucketCollaborators]);
  };

  const onCollabaratorDelete = (collId) => {
    return () => {
      const currentCollList = bucketCollaborators.filter((item) => {
        return item.username !== collId;
      });
      setBucketCollaborators(currentCollList);
    };
  };

  useEffect(() => {
    // window.location.replace('#/unauthorised');
    if (id) {
      const bucketInfo = bucketList?.find((item) => item.id === id);
      setBucketName(bucketInfo?.name);
      const permission = bucketInfo?.permission?.reduce((prev, curr) => {
        return {
          ...prev,
          [curr]: true,
        };
      }, {});
      setBucketPermission(permission);
      setBucketCollaborators(bucketInfo?.collaborators);
    }
  }, []);

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
                    setBucketName(e.target.value);
                    if (e.target.value) setBucketNameError('');
                  }}
                  value={bucketName}
                />
                <div>
                  <span className={classNames('error-message', bucketNameError.length ? '' : 'hide')}>
                    {bucketNameError}
                  </span>
                </div>
              </div>
            </div>
            <br />
            <div
              className={classNames(Styles.inputGrp, ' input-field-group', bucketPermissionError.length ? 'error' : '')}
            >
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
                      checked={!!bucketPermission.read}
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
                      checked={!!bucketPermission.write}
                      onChange={handleCheckbox}
                    />
                  </span>
                  <span className="label">Write</span>
                </label>
              </div>
              <div>
                <span
                  className={classNames('error-message', bucketPermissionError.length ? '' : 'hide', Styles.errorMsg)}
                >
                  {bucketPermissionError}
                </span>
              </div>
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
                                <div className={Styles.collUserTitleCol}>{item.username}</div>
                                <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                                <div className={Styles.collUserTitleCol}>
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="can_coll_read"
                                          checked={true}
                                          onChange={() => onCollaboratorPermission(item.username, collIndex)}
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
                                          value="can_coll_write"
                                          checked={
                                            item?.permissions !== null
                                              ? item?.permissions.includes('can_coll_write')
                                              : false
                                          }
                                          onChange={() => onCollaboratorPermission(item.username, collIndex)}
                                        />
                                      </span>
                                      <span className="label">Write</span>
                                    </label>
                                  </div>
                                </div>
                                <div className={Styles.collUserTitleCol}>
                                  <div
                                    className={Styles.deleteEntry}
                                    onClick={onCollabaratorDelete(item.username, collIndex)}
                                  >
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
      {submission?.modal && (
        <InfoModal
          title={''}
          show={submission?.modal}
          content={<ConnectionModal />}
          onCancel={onSubmissionModalCancel}
        />
      )}
    </>
  );
};

export default CreateBucket;
