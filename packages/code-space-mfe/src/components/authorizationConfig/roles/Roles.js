import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './Roles.scss';
import { Notification, ProgressIndicator } from '../../../common/modules/uilab/bundle/js/uilab.bundle';
import { CODE_SPACE_STATUS, SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import Pagination from 'dna-container/Pagination';
import Modal from 'dna-container/Modal';
import SelectBox from 'dna-container/SelectBox';
import { CodeSpaceApiClient } from '../../../apis/codespace.api';
import { Envs } from '../../../Utility/envs';

const classNames = cn.bind(Styles);

const Roles = (props) => {
  const [config, setConfig] = React.useState([]);
  const [roleList, setRoleList] = React.useState([]);
  const [allentitelmentList, setallEntitelmentList] = React.useState([]);
  const [allRoleList, setAllRoleList] = React.useState([]);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [editRoleCol, setEditRoleCol] = useState(false);
  const [entitlementErr, setEntitlementErr] = useState('');
  const [roleName, setRoleName] = useState('');
  const [roleFullName, setRoleFullName] = useState(props.projectName);
  const [tempRoleName, setTempRoleName] = useState('');
  const [roleId, setRoleId] = useState('');
  const [entitelmentList, setEntitelmentList] = useState([]);
  const [missingRoleName, setMissingRoleName] = useState('');
  const [showEditOrCreateModal, setShowEditOrCreateModal] = useState(false);
  const requiredError = '*Missing entry';

  useEffect(() => {
    ProgressIndicator.show();
    setConfig(props.config);
    const records = props.config?.roles || [];
    if (records?.length > 0) {
      const totalNumberOfPages = Math.ceil(records.length / maxItemsPerPage);
      const modifiedData = records.slice(0, maxItemsPerPage);
      setTotalNumberOfPages(totalNumberOfPages);
      setRoleList(modifiedData);
      setAllRoleList(records);
      ProgressIndicator.hide();
    } else {
      ProgressIndicator.hide();
    }
  }, [props.config, maxItemsPerPage]);

  useEffect(() => {
    if (props?.id && config && !props.isCodeSpaceAdminPage) {
      ProgressIndicator.show();
      CodeSpaceApiClient.getEntitlements(props.id)
        .then((response) => {
          setallEntitelmentList(response?.data?.data || []);
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(err?.message ? err.message : 'Some Error Occured', 'alert');
        });
    }
  }, [props?.id, config]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = allRoleList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setRoleList(modifiedData);
    setCurrentPageNumber(currentPageNumberTemp);
  };

  const onPaginationNextClick = async () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = allRoleList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setRoleList(modifiedData);
    setCurrentPageNumber(currentPageNumberTemp);
  };

  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(allRoleList?.length / pageNum);
    const modifiedData = allRoleList.slice(0, pageNum);

    setCurrentPageNumber(1);
    setMaxItemsPerPage(pageNum);
    setTotalNumberOfPages(totalNumberOfPages);
    setRoleList(modifiedData);
  };

  const editRole = (item) => {
    setEditRoleCol(true);
    setShowEditOrCreateModal(true);
    if (item.name.startsWith(props.projectName + "_")) {
      const roleName = item.name.substring(props.projectName.length + 1);
      setRoleName(roleName);
    } else {
      setRoleName(item.name);
    }
    setTempRoleName(item.name);
    setRoleId(item.id);
    setEntitelmentList(item.roleEntitlements);
    setMissingRoleName('');
    setEntitlementErr('');
    setTimeout(() => {
      SelectBox.defaultSetup();
    }, 100);
  };

  const deleteRole = (item) => {
    const updatedList = roleList.filter((role) => role.name !== item.name);
    const updatedAllRoleList = allRoleList.filter((role) => role.name !== item.name);
    setRoleList(updatedList);
    setAllRoleList(updatedAllRoleList);
  };

  const onEntilementChange = (e) => {
    setEntitlementErr('');
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const entitlement = { id: null, name: null };
        entitlement.id = option.value;
        entitlement.name = option.label.split('.')[1];
        selectedValues.push(entitlement);
      });
    }
    setEntitelmentList(selectedValues);
  };

  const onRoleNameOnChange = (e) => {
    const val = e.target.value.toUpperCase();
    setRoleName(val);
    setRoleFullName(props.projectName+'_' + val);
    validateRole(val);
  };

  const validateRole = (value) => {
    const pattern = /^[A-Z0-9][A-Z0-9_-]*$/;
    const isValid = pattern.test(value);
    const spclCharValidation = /[^A-Z0-9_-]/;
    if (!isValid) {
      setTimeout(() => {
        if (value.startsWith('-') || value.startsWith('_')) {
          setMissingRoleName('Role Id cannot start with special character');
        } else if (value.includes(' ')) {
          setMissingRoleName('Role Id cannot have whitespaces');
        } else if (spclCharValidation.test(value)) {
          setMissingRoleName('Role Id cannot have special characters other than - and _');
        }
      })
    } else if (allRoleList.some((role) => role.name === (props.projectName + '_' + value))) {
      setMissingRoleName('A role with this name already exists.');
    } else {
      setMissingRoleName('');
    }
  }

  const cancelEditOrCreateModal = () => {
    setEditRoleCol(false);
    setShowEditOrCreateModal(false);
    setRoleName('');
    setTempRoleName('');
    setRoleFullName(props.projectName);
    setRoleId('');
    setEntitelmentList([]);
    setTimeout(() => {
      SelectBox.defaultSetup();
    }, 100);
  };

  const roleSubmit = () => {
    const updatedConfig = {
      ...config,
      roles: allRoleList,
    };
    setConfig(updatedConfig);
    props.onSaveDraft('roles', updatedConfig);
  };

  const isFormValid = () => {
    let isFormValid = true;
    if (!roleName) {
      setMissingRoleName('Missing Role Id');
      isFormValid = false;
    }
    if(roleName.endsWith('-') || roleName.endsWith('_')){
      setMissingRoleName('Role Name cannot end with - or _');
      isFormValid = false;
    }
    if (!entitelmentList.length) {
      setEntitlementErr('Missing Entitlement');
      isFormValid = false;
    }
    const isRoleNameExist = allRoleList?.find((role) => role.name === roleName);
    if (isRoleNameExist && tempRoleName !== roleName) {
      setMissingRoleName('Role already exist');
      isFormValid = false;
    }
    return isFormValid;
  };

  const addORUpdateRoles = () => {
    if (!isFormValid() || missingRoleName !== '') {
      return;
    }
    if (!editRoleCol) {
      const updatedRoleList = roleList || [];
        const newRole = {
        name: roleFullName,
        roleEntitlements: entitelmentList,
      };
      updatedRoleList.push(newRole);
      setRoleList(updatedRoleList);
      setAllRoleList([...allRoleList, newRole]);
    } else {
      const updatedRoleList = roleList?.map((role) => {
        if (role.id === roleId) {
          return {
            ...role,
            name: roleFullName,
            roleEntitlements: entitelmentList,
          };
        }
        return role;
      });
      const updatedAllRoleList = allRoleList?.map((role) => {
        if (role.id === roleId) {
          return {
            ...role,
            name: roleFullName,
            roleEntitlements: entitelmentList,
          };
        }
        return role;
      });
      setRoleList(updatedRoleList);
      setAllRoleList(updatedAllRoleList);
    }
    setShowEditOrCreateModal(false);
    setRoleName('');
    setTempRoleName('');
    setRoleId('');
    setEntitelmentList([]);
    setTimeout(() => {
      SelectBox.defaultSetup();
    }, 100);
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.provisionStyles)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames('decriptionSection', 'mbc-scroll')}>
            <h3 className={classNames(Styles.title)}>Roles</h3>
            <div className={classNames(Styles.parentRole)}>
              <div className={Styles.warningWrapper}>
                {!props.readOnlyMode && !CODE_SPACE_STATUS.includes(config?.status) && (
                  <p style={{ color: 'var(--color-orange)' }}>
                    <i className="icon mbc-icon alert circle"></i> Once the config is in published state, Can Add / Edit
                    Roles
                  </p>
                )}
              </div>
              <div className={classNames(Styles.createEntitlementButton)}>
                {!props.readOnlyMode && allentitelmentList?.length > 0 && (
                  <button
                    className={classNames('btn add-dataiku-container btn-primary', Styles.createButton)}
                    type="button"
                    onClick={() => {
                      setEditRoleCol(false);
                      setShowEditOrCreateModal(true);
                    }}
                    title={
                      !CODE_SPACE_STATUS.includes(props?.config?.status)
                        ? 'Once the config is in published state, can add New Role.'
                        : ''
                    }
                    disabled={!CODE_SPACE_STATUS.includes(props?.config?.status)}
                  >
                    <i className="icon mbc-icon plus" />
                    <span>Create New Role</span>
                  </button>
                )}
              </div>
            </div>
            {allRoleList?.length > 0 ? (
              <div className={Styles.row}>
                <table className="ul-table users">
                  <thead>
                    <tr className={classNames('header-row', Styles.configRow)}>
                      <th>
                        <label>Name</label>
                      </th>
                      <th>
                        <label>Entitlements</label>
                      </th>
                      <th className={'actionColumn' + (props.readOnlyMode ? ' hidden' : '')}>
                        <label>Action</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {roleList?.map((item, index) => (
                      <tr key={index + item?.id} className={classNames('data-row', Styles.configRow)}>
                        <td className={classNames('wrap-text', Styles.configColumn)}>
                          <span>{item.name}</span>
                        </td>
                        <td id="config-column" className={classNames('wrap-text', Styles.configColumn)}>
                          <div className={Styles.tagColumn}>
                            {item.roleEntitlements?.map((entitlement, index) => (
                              <div className="chips read-only" key={index}>
                                <label className="name">
                                  {!props.isCodeSpaceAdminPage ? Envs.CODESPACE_SECURITY_APP_ID + '.' + entitlement.name : entitlement.name }
                                </label>
                              </div>
                            ))}
                          </div>
                        </td>
                        <td
                          id="config-column"
                          className={classNames('wrap-text', Styles.configColumn, Styles.actionBtn)}
                        >
                          <button
                            onClick={() => editRole(item)}
                            className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                            type="button"
                            title={
                              !CODE_SPACE_STATUS.includes(props?.config?.status)
                                ? 'Once the config is in published state, can edit Role.'
                                : ''
                            }
                            disabled={!CODE_SPACE_STATUS.includes(props?.config?.status)}
                          >
                            <i className="icon mbc-icon edit" />
                          </button>
                          &nbsp; &nbsp;
                          <button
                            onClick={() => deleteRole(item)}
                            className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                            type="button"
                            title={
                              !CODE_SPACE_STATUS.includes(props?.config?.status)
                                ? 'Once the config is in published state, can delete Role.'
                                : ''
                            }
                            disabled={!CODE_SPACE_STATUS.includes(props?.config?.status)}
                          >
                            <i className="icon mbc-icon trash-outline" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <Pagination
                  totalPages={totalNumberOfPages}
                  pageNumber={currentPageNumber}
                  onPreviousClick={onPaginationPreviousClick}
                  onNextClick={onPaginationNextClick}
                  onViewByNumbers={onViewByPageNum}
                  displayByPage={true}
                />
              </div>
            ) : (
              <div className={classNames('no-data', Styles.noData)}> No Roles found</div>
            )}
          </div>
        </div>
      </div>
      {
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={roleSubmit}>
            {!CODE_SPACE_STATUS.includes(config?.status) || props.readOnlyMode ? 'Next' : 'Save & Next'}
          </button>
        </div>
      }
      <Modal
        title={editRoleCol ? 'Edit Role' : 'Create a New Role'}
        showAcceptButton={false}
        showCancelButton={false}
        modalWidth={'50%'}
        buttonAlignment="right"
        show={showEditOrCreateModal}
        content={
          <div className={classNames(Styles.editCreateModal)}>
            <div className={classNames(Styles.inlineTextField)}>
            <div
              className={classNames(
              ' input-field-group include-error ',
                missingRoleName?.length ? 'error' : '',
              )}
            >
              <label id="RolId" htmlFor="RolId" className="input-label">
                Role Id<sup>*</sup>
              </label>
              <input
                type="text"
                className="input-field"
                required={true}
                id="RolId"
                maxLength={19}
                placeholder="Type here"
                autoComplete="off"
                onChange={(e)=>onRoleNameOnChange(e)}
                value={roleName}
              />
              <span className={classNames('error-message', missingRoleName?.length ? '' : 'hide')}>
                {missingRoleName}
              </span>
            </div>
            <div
              className={classNames(
                Styles.textField, ' input-field-group disabled'
              )}
            >
              <label id="RolName" htmlFor="RolName" className="input-label">
                Role Name
              </label>
              <input
                type="text"
                className="input-field"
                id="RolName"
                disabled ={true}
                onChange={onRoleNameOnChange}
                value={roleFullName}
              />
            </div>
            </div>
            <div
              id="entitelmentListContainer"
              className={classNames('input-field-group', entitlementErr?.length ? 'error' : '')}
            >
              <label id="entitelmentListLabel" className="input-label" htmlFor="entitelmentListSelect">
                Entitlement <sup>*</sup>
              </label>
              <div id="entitelmentList" className=" custom-select">
                <select
                  id="entitelmentListSelect"
                  multiple={true}
                  required={true}
                  required-error={requiredError}
                  onChange={onEntilementChange}
                  value={entitelmentList.map((entitlement) => entitlement.id)}
                >
                  {allentitelmentList?.map((obj) => (
                    <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {Envs.CODESPACE_SECURITY_APP_ID + '.' + obj.name}
                    </option>
                  ))}
                </select>
              </div>
              <span className={classNames('error-message', entitlementErr.length ? '' : 'hide')}>{entitlementErr}</span>
            </div>
            <div className={Styles.AddorUpdatebtn}>
              <button className={'btn btn-tertiary'} type="button" onClick={() => addORUpdateRoles()}>
                <span>{editRoleCol ? 'Update' : 'Add'}</span>
              </button>
            </div>
          </div>
        }
        scrollableContent={true}
        onCancel={() => {
          cancelEditOrCreateModal();
          setEntitlementErr('');
          setMissingRoleName('');
        }}
      />
    </React.Fragment>
  );
};
export default Roles;
