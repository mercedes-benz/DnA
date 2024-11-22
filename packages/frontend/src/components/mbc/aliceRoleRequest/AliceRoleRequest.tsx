import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AliceRoleRequest.scss';
import { history } from '../../../router/History';
import { Envs } from 'globals/Envs';
import TextBox from '../shared/textBox/TextBox';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { ApiClient } from '../../../services/ApiClient';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { SESSION_STORAGE_KEYS } from 'globals/constants';

const AliceRoleRequest = () => {
  const goback = () => {
    history.goBack();
  };
  const [roleName, setRoleName] = useState('');
  const [roleNameError, setRoleNameError] = useState('');
  const [rolesCreated, setRolesCreated] = useState([]);

  const onRoleNameChange = (e: React.FormEvent<HTMLInputElement>) => {
    const roleNameVal = e.currentTarget.value;
    setRoleName(roleNameVal.toUpperCase());
    setRoleNameError('');
  }

  const validateRole = () => {
    const specialCharPattern = /[^A-Za-z0-9\-_./]/;
    if (roleName.trim() === "") {
      setRoleNameError('Role name cannot be empty');
      return false
    }
    if (roleName.includes(" ")) {
      setRoleNameError('Role name cannot contain spaces');
      return false;
    }
    if (specialCharPattern.test(roleName.toUpperCase())) {
      setRoleNameError('Role name can only contain letters, numbers, and the following characters: . _ -');
      return false;
    }
    return true;
  }

  useEffect(() => {
    const storedRoles = sessionStorage.getItem(SESSION_STORAGE_KEYS.ALICE_ROLES_CREATED);
    if (storedRoles) {
      setRolesCreated(JSON.parse(storedRoles));
    }
  }, []);

  const createRole = () => {
    if (validateRole()) {
      const value = Envs.ALICE_APP_ID + "_" + roleName
      const data = {
        "data": {
          "roleName": value
        }
      }
      ProgressIndicator.show();
      ApiClient.createAliceRole(data)
        .then((res: any) => {
          ProgressIndicator.hide();
          if (res.success === 'SUCCESS') {
            const updatedRoles = [...rolesCreated, value]
            setRolesCreated(updatedRoles);
            setRoleName('');
            sessionStorage.setItem(SESSION_STORAGE_KEYS.ALICE_ROLES_CREATED, JSON.stringify(updatedRoles));
            setRoleNameError('');
            Notification.show('Role created successfully')
          } else {
            if (res?.errors[0]?.message?.length > 0) {
              Notification.show(res?.errors[0]?.message, 'alert')
            }
            if (res?.warnings[0]?.message?.length > 0) {
              Notification.show(res?.warnings[0]?.message, 'warning')
            }
          }

        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(
            err?.message || "Something went wrong", 
            "alert"
          );
        });
    }

  }
  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.backButtonWapper}>
          <button className="btn btn-text back arrow" type="submit" onClick={goback}>
            Back
          </button>
          <h3>
            {"Create Roles in Alice ("}
            <span style={{ fontSize: '0.8em' }}>{Envs.ALICE_APP_ID}</span>
            {")"}
          </h3>
        </div>
        <div className={classNames(Styles.content)}>
          <p>
            On this page, you can create Alice roles within the DNA platform (Application ID: {Envs.ALICE_APP_ID}) .
          </p>

          <p>
            To create a new role with application ID other than {Envs.ALICE_APP_ID}, use the following link:{' '}
            <a href={Envs.ALICE_BASE_URL+"/access/accessRequest"} target="_blank" rel="noreferrer">
              {Envs.ALICE_BASE_URL+"/access/accessRequest"}
            </a>.
          </p>

          <p>
            To view the roles that have been created, visit the Alice portal at{' '}
            <a href={`${Envs.ALICE_BASE_URL}/admin/roles`} target="_blank" rel="noreferrer">
              {`${Envs.ALICE_BASE_URL}/admin/roles`}
            </a>.
          </p>
        </div>

        <div className={classNames(Styles.content)}>
          <div className={classNames(Styles.inputWrapper)}>
            <div className={classNames(Styles.roleRequest)}>
              <div className={classNames(Styles.header)}>
                <h5>Enter the role</h5>
              </div>
              <div className={classNames(Styles.roleSection)}>
                <div className={classNames(Styles.roleName)}>
                  <TextBox
                    type="text"
                    controlId={'productNameInput'}
                    labelId={'productNameLabel'}
                    label={'Role Name'}
                    placeholder={'Type here'}
                    value={roleName}
                    errorText={roleNameError}
                    required={true}
                    maxLength={50}
                    onChange={onRoleNameChange}
                  />
                </div>
                <div className={classNames(Styles.roleName, Styles.disabledSection)}>
                  <TextBox
                    type="text"
                    controlId={'productNameInput'}
                    labelId={'productNameLabel'}
                    label={'Role to be created'}
                    placeholder={'Type here'}
                    value={roleName?.length > 0 ? Envs.ALICE_APP_ID + "_" + roleName : ''}
                    required={false}
                    maxLength={50}
                    onChange={onRoleNameChange}
                  />
                </div>
              </div>

              <button className="btn btn-tertiary" type="button" onClick={createRole}>
                create Role
              </button>
            </div>
            <div className={classNames(Styles.rolesListSection)} >
              {rolesCreated?.length ? (<div className={classNames(Styles.rolesList)} >
                <div className={classNames(Styles.header)}>
                  <h5>IDs of roles created in this session</h5>
                </div>
                <div className={Styles.infoLinks}>
                  {rolesCreated.map((item: any, key: any) => {
                    return (
                      <div className="chips read-only" key={key} onClick={() => {
                        const url = Envs.ALICE_BASE_URL+"/admin/roles/"+item;
                        window.open( url, '_blank');
                      }}>
                        <label className="name">{item}</label>
                      </div>
                    );
                  })}
                </div>
              </div>
              ) :
                <div className={classNames(Styles.noRolesCreated)}><p>No Roles Created</p></div>}
            </div>
          </div>

        </div>
      </div>

    </div>
  );
};

export default AliceRoleRequest;
