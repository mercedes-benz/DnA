import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './Roles.scss';
import { Notification, ProgressIndicator } from '../../../../../../src/assets/modules/uilab/bundle/js/uilab.bundle';
import { CODE_SPACE_STATUS, SESSION_STORAGE_KEYS } from 'globals/constants';
import Pagination from 'components/mbc/pagination/Pagination';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { CodeSpaceApiClient } from '../../../../../../src/services/CodeSpaceApiClient';

const classNames = cn.bind(Styles);

const Roles = (props: any) => {
    const [config, setConfig] = React.useState<any>([]);
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
        if (props?.id && config) {
            ProgressIndicator.show();
            CodeSpaceApiClient.getEntitlements(props.id)
                .then((response: any) => {
                    setallEntitelmentList(response?.data || []);
                    SelectBox.defaultSetup();
                    ProgressIndicator.hide();
                })
                .catch((err: any) => {
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

    const onViewByPageNum = (pageNum: number) => {

        const totalNumberOfPages = Math.ceil(allRoleList?.length / pageNum);
        const modifiedData = allRoleList.slice(0, pageNum);

        setCurrentPageNumber(1);
        setMaxItemsPerPage(pageNum);
        setTotalNumberOfPages(totalNumberOfPages);
        setRoleList(modifiedData);
    };

    const editRole = (item: any) => {
        setEditRoleCol(true);
        setShowEditOrCreateModal(true);
        setRoleName(item.name);
        setTempRoleName(item.name);
        setRoleId(item.id);
        setEntitelmentList(item.roleEntitlements);
        setMissingRoleName('');
        setEntitlementErr('');
        setTimeout(() => {
            SelectBox.defaultSetup();
        }, 100);
    }

    const deleteRole = (item: any) => {
        const updatedList = roleList.filter((role: any) => role.id !== item.id);
        const updatedAllRoleList = allRoleList.filter((role: any) => role.id !== item.id);
        setRoleList(updatedList);
        setAllRoleList(updatedAllRoleList);
    };

    const onEntilementChange = (e: any) => {
        setEntitlementErr('');
        const selectedOptions = e.currentTarget.selectedOptions;
        const selectedValues: any = [];
        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option: any) => {
                const entitlement: any = { id: null, name: null };
                entitlement.id = option.value;
                entitlement.name = option.label;
                selectedValues.push(entitlement);
            });
        }
        setEntitelmentList(selectedValues);
    }

    const onEntitlementNameOnChange = (e: any) => {
        setMissingRoleName('');
        setRoleName(e.target.value);
    }

    const cancelEditOrCreateModal = () => {
        setEditRoleCol(false);
        setShowEditOrCreateModal(false);
        setRoleName('');
        setTempRoleName('');
        setRoleId('');
        setEntitelmentList([]);
        setTimeout(() => {
            SelectBox.defaultSetup();
        }, 100);
    }

    const roleSubmit = () => {
        const updatedConfig = {
            ...config,
            roles: allRoleList
        };
        setConfig(updatedConfig);
        props.onSaveDraft('roles', updatedConfig);
    };

    const isFormValid = () => {
        let isFormValid = true;
        if (!roleName) {
            setMissingRoleName('Missing Role Name');
            isFormValid = false;
        }
        if (!entitelmentList.length) {
            setEntitlementErr('Missing Entitlement');
            isFormValid = false;
        }
        const isRoleNameExist = allRoleList?.find((role: any) => role.name === roleName);
        if (isRoleNameExist && (tempRoleName !== roleName)) {
            setMissingRoleName('Role Name already exist');
            isFormValid = false;
        }
        return isFormValid;
    }

    const addORUpdateRoles = () => {
        if (!isFormValid()) {
            return;
        };
        if (!editRoleCol) {
            const updatedRoleList = roleList || [];
            const newRole = {
                name: roleName,
                roleEntitlements: entitelmentList
            };
            updatedRoleList.push(newRole);
            setRoleList(updatedRoleList);
            setAllRoleList([...allRoleList, newRole]);
        } else {
            const updatedRoleList = roleList?.map((role: any) => {
                if (role.id === roleId) {
                    return {
                        ...role,
                        name: roleName,
                        roleEntitlements: entitelmentList
                    };
                }
                return role;
            });
            const updatedAllRoleList = allRoleList?.map((role: any) => {
                if (role.id === roleId) {
                    return {
                        ...role,
                        name: roleName,
                        roleEntitlements: entitelmentList
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
                                {!CODE_SPACE_STATUS.includes(config?.status) &&
                                    <p style={{ color: 'var(--color-orange)' }}
                                        className={classNames((props.readOnlyMode ? ' hidden' : ''))}>
                                        <i className="icon mbc-icon alert circle">
                                        </i> Once the config is in published state, Can Add / Edit Roles</p>}
                            </div>
                            <div className={classNames(Styles.createEntitlementButton)}>
                                {allentitelmentList?.length > 0 && <button
                                    className={classNames('btn add-dataiku-container btn-primary',
                                        Styles.createButton + (props.readOnlyMode ? ' hidden' : ''))} type="button"
                                    onClick={() => {
                                        setEditRoleCol(false);
                                        setShowEditOrCreateModal(true);
                                    }}
                                    title={!CODE_SPACE_STATUS.includes(props?.config?.status) ? 'Once the config is in published state, can add New Role.' : ''}
                                    disabled={!CODE_SPACE_STATUS.includes(props?.config?.status)}
                                >
                                    <i className="icon mbc-icon plus" />
                                    <span>Create New Role</span>
                                </button>}
                            </div>
                        </div>
                        {allRoleList?.length > 0
                            ? <div className={Styles.row}>
                                <table className="ul-table users">
                                    <thead>
                                        <tr className="header-row">
                                            <th>
                                                <label>
                                                    Name
                                                </label>
                                            </th>
                                            <th>
                                                <label>
                                                    Entitlements
                                                </label>
                                            </th>
                                            <th className="actionColumn">
                                                <label>Action</label>
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {roleList?.map((item: any, index) => (
                                            <tr
                                                key={index + item?.id}
                                                className={classNames('data-row')}>
                                                <td className="wrap-text">
                                                    <span >{item.name}</span>
                                                </td>
                                                <td className="wrap-text">
                                                    <div className={Styles.tagColumn}>
                                                        {item.roleEntitlements?.map((entitlement: any, index: number) => (
                                                            <div className="chips read-only" key={index}>
                                                                <label className="name">{entitlement.name}</label>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </td>
                                                <td className={classNames("wrap-text", Styles.actionBtn)}>
                                                    <button
                                                        onClick={() => editRole(item)}
                                                        className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                                                        type="button"
                                                        title={!CODE_SPACE_STATUS.includes(props?.config?.status) ? 'Once the config is in published state, can edit Role.' : ''}
                                                        disabled={!CODE_SPACE_STATUS.includes(props?.config?.status)}
                                                    >
                                                        <i className="icon mbc-icon edit" />
                                                    </button>
                                                    &nbsp; &nbsp;
                                                    <button
                                                        onClick={() => deleteRole(item)}
                                                        className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                                                        type="button"
                                                        title={!CODE_SPACE_STATUS.includes(props?.config?.status) ? 'Once the config is in published state, can delete Role.' : ''}
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
                            : <div className={classNames('no-data', Styles.noData)}> No Roles found</div>}
                    </div>
                </div>
            </div>
            {<div className="btnConatiner">
                <button className="btn btn-primary" type="button" onClick={roleSubmit}>
                    {!CODE_SPACE_STATUS.includes(config?.status) || props.readOnlyMode ? 'Next' : 'Save & Next'}
                </button>
            </div>}
            <Modal
                title={editRoleCol ? 'Edit Role' : 'Create a New Role'}
                showAcceptButton={false}
                showCancelButton={false}
                modalWidth={'40%'}
                buttonAlignment="right"
                show={showEditOrCreateModal}
                content={
                    <div className={classNames(Styles.editCreateModal)}>
                        <div
                            className={classNames(
                                Styles.inputGrpChild + ' input-field-group include-error ',
                                missingRoleName?.length ? 'error' : '',
                            )}
                        >
                            <label id="RolName" htmlFor="RolName" className="input-label">
                                Role Name<sup>*</sup>
                            </label>
                            <input
                                type="text"
                                className="input-field"
                                required={true}
                                id="RolName"
                                maxLength={64}
                                placeholder="Type here"
                                autoComplete="off"
                                onChange={onEntitlementNameOnChange}
                                value={roleName}
                            />
                            <span className={classNames('error-message', missingRoleName?.length ? '' : 'hide')}>
                                {missingRoleName}
                            </span>
                        </div>
                        <div
                            id="entitelmentListContainer"
                            className={classNames('input-field-group', entitlementErr?.length ? 'error' : '')}
                        >
                            <label id="entitelmentListLabel" className="input-label" htmlFor="entitelmentListSelect">
                                Entitlement <sup>*</sup>
                            </label>
                            <div id="entitelmentList"
                                className=" custom-select"
                            >
                                <select
                                    id="entitelmentListSelect"
                                    multiple={true}
                                    required={true}
                                    required-error={requiredError}
                                    onChange={onEntilementChange}
                                    value={entitelmentList.map(entitlement => entitlement.id)}
                                >
                                    {allentitelmentList?.map((obj: any) => (
                                        <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                            {obj.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <span className={classNames('error-message', entitlementErr.length ? '' : 'hide')}>
                                {entitlementErr}
                            </span>
                        </div>
                        <div className={Styles.AddorUpdatebtn}>
                            <button
                                className={'btn btn-tertiary'}
                                type="button"
                                onClick={() => addORUpdateRoles()}
                            >
                                <span>{editRoleCol ? 'Update' : 'Add'}</span>
                            </button>
                        </div>
                    </div>
                }
                scrollableContent={true}
                onCancel={() => cancelEditOrCreateModal()}
            />
        </React.Fragment>
    );
}
export default Roles;
