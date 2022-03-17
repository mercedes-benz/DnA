import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateBucket.scss';
import { useDispatch } from 'react-redux';
import { bucketActions } from '../redux/bucket.actions';

const CreateBucket = () => {
  const dispatch = useDispatch();
  const [bucketName, setBucketName] = useState('');
  const [permission, setPermission] = useState({
    read: false,
    write: false,
  });

  useEffect(() => {
    // window.location.replace('#/unauthorised');
  }, []);

  const handleSubmit = () => {
    const permissions = [];
    Object.entries(permission)?.forEach(([k, v]) => {
      if (v === true) permissions.push(k);
    });
    const data = {
      name: bucketName,
      permission: permissions,
      createdOn: '2022-03-01',
      modifiedOn: '2022-03-10',
    };
    dispatch(bucketActions.setBucketList(data));
  };

  const handleCheckbox = (e) => {
    if (e.target.checked) {
      setPermission({
        ...permission,
        [e.target.name]: true,
      });
    } else {
      setPermission({
        ...permission,
        [e.target.name]: false,
      });
    }
  };

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.caption}>
          <h3>Create New Bucket</h3>
        </div>
      </div>
      <div className={Styles.content}>
        <div className={Styles.formGroup}>
          <div className={classNames(Styles.inputGrp, ' input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>Name of Buckets</label>
            <input
              type="text"
              className="input-field"
              required={true}
              id="PrjName"
              maxLength={64}
              placeholder="Type here"
              autoComplete="off"
              onChange={(e) => setBucketName(e.target.value)}
              value={bucketName}
            />
          </div>
          <br />
          <div className={classNames(Styles.inputGrp, ' input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>Permission</label>
            <div>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    name="read"
                    type="checkbox"
                    className="ff-only"
                    checked={permission.read}
                    onChange={handleCheckbox}
                  />
                </span>
                <span className="label">Read</span>
              </label>
            </div>
            <div>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    name="write"
                    type="checkbox"
                    className="ff-only"
                    checked={permission.write}
                    onChange={handleCheckbox}
                  />
                </span>
                <span className="label">Write</span>
              </label>
            </div>
          </div>
          <div className={Styles.createBtn}>
            <button className={'btn btn-tertiary'} type="button" onClick={handleSubmit}>
              <span>{'Create Bucket'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateBucket;
