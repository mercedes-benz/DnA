import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AliceRoleRequest.scss';
import { history } from '../../../router/History';
import { Envs } from 'globals/Envs';
import TextBox from '../shared/textBox/TextBox';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { ApiClient } from '../../../services/ApiClient';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Modal from 'components/formElements/modal/Modal';
import { TEAMS_PROFILE_LINK_URL_PREFIX } from 'globals/constants';

const AliceRoleRequest = () => {
  const goback = () => {
    history.goBack();
  };
  const [roleName, setRoleName] = useState('');
  const [roleNameError, setRoleNameError] = useState('');
  const [rolesCreated, setRolesCreated] = useState({ static: [], dynamic: [] });
  const [isDynamicRole, setIsDynamicRole] = useState(false);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [selectedRoleDetails, setSelectedRoleDetails] = useState<any>(null);

  const onRoleNameChange = (e: React.FormEvent<HTMLInputElement>) => {
    const roleNameVal = e.currentTarget.value;
    setRoleName(roleNameVal);
    setRoleNameError('');
  };

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
    if (specialCharPattern.test(roleName)) {
      setRoleNameError('Role name can only contain letters, numbers, and the following characters: . _ -');
      return false;
    }
    return true;
  }

  useEffect(() => {
    fetchRole();
  }, []);

  const createRole = () => {
    if (validateRole()) {
      const value = Envs.ALICE_APP_ID + "_" + roleName
      const data = {
        "data": {
          "roleName": value,
          "isDynamic": isDynamicRole
        }
      }

      ProgressIndicator.show();
      ApiClient.createAliceRole(data)
        .then((res: any) => {
          ProgressIndicator.hide();
          if (res.success === 'SUCCESS') {
            const updatedStatic = isDynamicRole ? rolesCreated.static : [...rolesCreated.static, value];
            const updatedDynamic = isDynamicRole ? [...rolesCreated.dynamic, value] : rolesCreated.dynamic;
            setRolesCreated({ static: updatedStatic, dynamic: updatedDynamic });
            setRoleName('');
            setRoleNameError('');
            Notification.show('Role created successfully')
          } else {
            if (res?.errors[0]?.message?.length > 0) {
              Notification.show(res?.errors[0]?.message, 'alert')
            }
            if (res?.warnings[0]?.message?.length > 0) {
              Notification.show(res?.warnings[0]?.message, 'warning');
            }
          }
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(err?.message || 'Something went wrong', 'alert');
        });
    }
  };

  const fetchRole = () => {
    ProgressIndicator.show();
    ApiClient.getExistingRoles(Envs.ALICE_APP_ID)
      .then((res: any) => {
        ProgressIndicator.hide();
        if (Array.isArray(res)) return;
        if (res?.data?.roles) {
          const allRoles = res.data.roles;
          const staticRoles = allRoles.filter((role: string) => !role.toLowerCase().includes('dyn'));
          const dynamicRoles = allRoles.filter((role: string) => role.toLowerCase().includes('dyn'));
          setRolesCreated({ static: staticRoles, dynamic: dynamicRoles });
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
        Notification.show(err?.message || 'Something went wrong', 'alert');
      });
  };

  const handleRoleClick = (role: string) => {
    setShowRoleModal(true);
    setSelectedRoleDetails(null);

    ApiClient.getRoleDetails(role)
      .then((res: any) => {
        if (res?.data) {
          setSelectedRoleDetails({
            id: res.data.id,
            name: res.data.name,
            description: res.data.description,
            isDynamic: res.data.isDynamic,
            isSelfRequestable: res.data.isSelfRequestable,
            roleOwners: res.data.roleOwners || [],
            roleMembers: res.data.roleMembers || []
          });
        } else {
          Notification.show(res?.errors[0]?.message || 'Error fetching role details', 'alert');
        }
      })
      .catch((err) => {
        Notification.show(err?.message || 'Error fetching role details', 'alert');
      });
  };

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
            <a href={Envs.ALICE_BASE_URL + "/access/accessRequest"} target="_blank" rel="noreferrer">
              {Envs.ALICE_BASE_URL + "/access/accessRequest"}
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
                  <div className="form-group">
                    <label className="checkbox">
                      <span className="wrapper">
                      <input
                        type="checkbox"
                          className="ff-only"
                        checked={isDynamicRole}
                        onChange={(e) => setIsDynamicRole(e.target.checked)}
                      />
                      </span>
                      <span className="label">Dynamic Role</span>
                    </label>
                  </div>
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

            <div className={classNames(Styles.rolesListSection)}>
              {(rolesCreated.static.length > 0 || rolesCreated.dynamic.length > 0) ? (
                <>
                  {rolesCreated.static.length > 0 && (
                    <div className={classNames(Styles.rolesList)}>
                      <div className={classNames(Styles.header)}>
                        <h5>Static Roles managed by you</h5>
                      </div>
                      <div className={Styles.infoLinks}>
                        {rolesCreated.static.map((item, key) => (
                          <div key={key} className={classNames(Styles.roleItem)}>
                            <h3 className={classNames('btn btn-primary', Styles.outlineBtn)} onClick={() => handleRoleClick(item)} style={{ cursor: 'pointer' }}>{item}</h3>
                            <div>
                              <a
                                href={`${Envs.ALICE_BASE_URL}/admin/roles/${item}`}
                                target="_blank"
                                rel="noreferrer"
                              >
                                View in Alice <i className="icon mbc-icon new-tab" style={{ fontSize: '10px' }}/>
                              </a>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {rolesCreated.dynamic.length > 0 && (
                    <div className={classNames(Styles.rolesList)}>
                      <div className={classNames(Styles.header)}>
                        <h5>Dynamic Roles managed by you</h5>
                      </div>
                      <div className={Styles.infoLinks}>
                        {rolesCreated.dynamic.map((item, key) => (
                          <div key={key} className={classNames(Styles.roleItem)}>
                            <h3 className={classNames('btn btn-primary', Styles.outlineBtn)} onClick={() => handleRoleClick(item)} style={{ cursor: 'pointer' }}>{item}</h3>
                            <div>
                              <a
                                href={`${Envs.ALICE_BASE_URL}/admin/roles/${item}`}
                                target="_blank"
                                rel="noreferrer"
                              >
                                View in Alice <i className="icon mbc-icon new-tab" style={{ fontSize: '10px' }}/>
                              </a>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              ) : (
                <div className={classNames(Styles.noRolesCreated)}><p>No Roles Created</p></div>
              )}
            </div>
          </div>
        </div>
      </div>
      <Modal
        title={selectedRoleDetails?.id}
        content={
          <div>
            <div className={Styles.flexDetailsSection}>
              <div id="Sub-Title" >
                <h5>Role Details</h5>
              </div>
              <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                <div id="Description" >
                  <label className="input-label summary">Description</label>
                  <br />
                  {selectedRoleDetails?.description || 'No description available.'}
                </div>
                <div id="Dynamic Role">
                  <label className="input-label summary">Dynamic Role</label>
                  <br />
                  {selectedRoleDetails?.isDynamic ? 'Yes' : 'No'}
                </div>
                <div id="Self-Requestable">
                  <label className="input-label summary">Self Requestable Role</label>
                  <br />
                  {selectedRoleDetails?.isSelfRequestable ? 'Yes' : 'No'}
                </div>
              </div>
            </div>
            <hr className={Styles.divider} />
            <h5>Role Owners</h5>
            <div className={Styles.modalContentSection}>
              {selectedRoleDetails?.roleOwners?.length > 0 ? (
                selectedRoleDetails.roleOwners.map((owner: any, index: number) => (
                  <div key={index} className={Styles.roleItem}>
                    <div className={Styles.userDetails}>
                      <a
                        href={`${TEAMS_PROFILE_LINK_URL_PREFIX}${owner.id}`}
                      >{owner.completeName}</a>
                      <p>{owner.departmentNumber}</p>
                    </div>
                  </div>
                ))
              ) : (
                <p>No owners found.</p>
              )}
            </div>
            <hr className={Styles.divider} />
            <h5>Members of this role</h5>
            <div className={Styles.modalContentSection}>
              {selectedRoleDetails?.roleMembers?.length > 0 ? (
                selectedRoleDetails.roleMembers.map((member: any, index: number) => (
                  <div key={index} className={Styles.roleItem}>
                    <div className={Styles.userDetails}>
                      <a
                        href={`${TEAMS_PROFILE_LINK_URL_PREFIX}${member.id}`}
                      >{member.completeName}</a>
                      <p>{member.departmentNumber}</p>
                    </div>
                  </div>
                ))
              ) : (
                <p>No members found.</p>
              )}
            </div>
          </div>
        }
        show={showRoleModal}
        onCancel={() => setShowRoleModal(false)}
        buttonAlignment="right"
        showAcceptButton={false}
        showCancelButton={false}
        scrollableContent={true}
      />
    </div>
  );
};

export default AliceRoleRequest;
