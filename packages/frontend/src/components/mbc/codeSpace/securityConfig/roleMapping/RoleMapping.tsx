import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './RoleMapping.scss';
import { Notification, ProgressIndicator } from '../../../../../../src/assets/modules/uilab/bundle/js/uilab.bundle';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import AddUser from 'components/mbc/addUser/AddUser';
import { CODE_SPACE_STATUS, SESSION_STORAGE_KEYS } from 'globals/constants';
import Pagination from 'components/mbc/pagination/Pagination';

const classNames = cn.bind(Styles);

const RoleMapping = (props: any) => {
    const [config, setConfig] = React.useState<any>([]);
    const [allUserRoleMappingList, setAllUserRoleMappingList] = React.useState<any>([]);
    const [userRoleMappingList, setUserRoleMappingList] = React.useState([]);
    const [allRoleList, setAllRoleList] = React.useState([]);

    const [editRoleCol, setEditRoleCol] = useState(false);
    const [showEditOrCreateModal, setShowEditOrCreateModal] = useState(false);
    const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
    const [currentPageNumber, setCurrentPageNumber] = useState(1);
    const [maxItemsPerPage, setMaxItemsPerPage] = useState(
        parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
    );
    const [roleContributor, setRoleContributor] = useState([]);
    const [rolesError, setRolesError] = useState('');
    const [userError, setuserError] = useState('');
    const [roleValues, setRoleValues] = useState<any>([]);

    const requiredError = '*Missing entry';

    useEffect(() => {
        ProgressIndicator.show();
        setConfig(props.config);
        const records = props.config?.userRoleMappings || [];
        if (records?.length > 0) {
            const totalNumberOfPages = Math.ceil(records.length / maxItemsPerPage);
            const modifiedData = records.slice(0, maxItemsPerPage);
            setTotalNumberOfPages(totalNumberOfPages);
            setAllUserRoleMappingList(records);
            setUserRoleMappingList(modifiedData);
        }
        setAllRoleList(props.config.roles);
        setTimeout(() => {
            SelectBox.defaultSetup();
            ProgressIndicator.hide();
        }, 100);
    }, [props.config, maxItemsPerPage]);

    useEffect(() => {
        SelectBox.defaultSetup();
    }, []);

    const onViewByPageNum = (pageNum: number) => {

        const totalNumberOfPages = Math.ceil(allUserRoleMappingList?.length / pageNum);
        const modifiedData = allUserRoleMappingList.slice(0, pageNum);

        setCurrentPageNumber(1);
        setMaxItemsPerPage(pageNum);
        setTotalNumberOfPages(totalNumberOfPages);
        setUserRoleMappingList(modifiedData);
    };

    const onPaginationNextClick = async () => {
        let currentPageNumberTemp = currentPageNumber;
        const currentPageOffset = currentPageNumber * maxItemsPerPage;
        currentPageNumberTemp = currentPageNumber + 1;
        const modifiedData = allUserRoleMappingList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
        setUserRoleMappingList(modifiedData);
        setCurrentPageNumber(currentPageNumberTemp);
    };


    const onPaginationPreviousClick = () => {
        const currentPageNumberTemp = currentPageNumber - 1;
        const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
        const modifiedData = allUserRoleMappingList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
        setUserRoleMappingList(modifiedData);
        setCurrentPageNumber(currentPageNumberTemp);
    };

    const deleteRoleMapping = (item: any) => {
        const updatedUserRoleList = userRoleMappingList.filter((user: any) => user.shortId !== item.shortId);
        const updatedAllRoleList = allUserRoleMappingList.filter((member: any) => member.shortId !== item.shortId);
        setAllUserRoleMappingList(updatedAllRoleList);
        setUserRoleMappingList(updatedUserRoleList);
        setUserRoleMappingList(updatedUserRoleList.slice(0, maxItemsPerPage));
    }

    const editRoleMapping = (item: any) => {
        setRoleValues([...item.roles]);
        const collabarationData = {
            firstName: item.firstName,
            lastName: item.lastName,
            shortId: item.shortId,
            id: item.shortId,
            department: item.department,
            email: item.email,
            mobileNumber: item.mobileNumber
        };
        setRoleContributor([{ ...collabarationData }]);
        setEditRoleCol(true);
        setTimeout(() => {
            SelectBox.defaultSetup();
        }, 100);
        setShowEditOrCreateModal(true);

    }

    const cancelEditOrCreateModal = () => {
        setEditRoleCol(false);
        setShowEditOrCreateModal(false);
    }

    const userRoleSubmit = () => {
        const updatedConfig = {
            ...config,
            userRoleMappings: allUserRoleMappingList
        };
        setConfig(updatedConfig);
        props.onSaveDraft('rolemapping', updatedConfig);
    };

    const backPress = () => {
        const updatedConfig = {
            ...config,
            userRoleMappings: allUserRoleMappingList
        };
        setConfig(updatedConfig);
        props.onSaveDraft('rolemapping', updatedConfig, 'roles');
    };

    const onPublish = () => {
        const updatedConfig = {
            ...config,
            userRoleMappings: allUserRoleMappingList
        };
        setConfig(updatedConfig);
        if (updatedConfig?.userRoleMappings?.length === 0) {
            Notification.show('Please add atleast one role mapping', 'warning');
            return;
        }
        props.onPublish(updatedConfig);
    }

    const getCollabarators = (collaborators: any) => {
        setuserError('')
        const collabarationData = {
            firstName: collaborators.firstName,
            lastName: collaborators.lastName,
            shortId: collaborators.shortId,
            id: collaborators.shortId,
            department: collaborators.department,
            email: collaborators.email,
            mobileNumber: collaborators.mobileNumber,
            userType: 'internal'
        };

        let duplicateMember = false;
        duplicateMember = roleContributor?.filter((member) => member.shortId === collaborators.shortId)?.length
            ? true
            : false;

        if (duplicateMember) {
            Notification.show('Collaborator Already Exist.', 'warning');
        } else {
            setRoleContributor([...roleContributor, collabarationData]);
        }
    };

    const deleteCollaborator = (collaborator: any) => {
        const updatedList = roleContributor.filter((member: any) => member.id !== collaborator.id);
        setRoleContributor(updatedList);
        setuserError('');
    };

    const onChangeRole = (e: any) => {
        setRolesError('');
        const selectedOptions = e.currentTarget.selectedOptions;
        const selectedValues: any = [];
        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option: any) => {
                const role: any = { id: null, name: null };
                role.id = option.value;
                role.name = option.label;
                selectedValues.push(role);
            });
        }
        setRoleValues(selectedValues);
    };

    const isFormValid = () => {
        let isFormValid = true;
        if (!roleValues?.length) {
            setRolesError('Please select role');
            isFormValid = false;
        }
        const isUserExist = allUserRoleMappingList?.filter((user: any) => user.shortId === roleContributor[0].shortId)?.length;
        if (isUserExist && !editRoleCol) {
            setuserError('User already exist');
            isFormValid = false;
        }
        return isFormValid;
    }

    const onRoleSubmit = () => {
        if (!isFormValid()) {
            return;
        };
        const roleMappingData = {
            firstName: roleContributor[0].firstName,
            lastName: roleContributor[0].lastName,
            shortId: roleContributor[0].shortId,
            id: roleContributor[0].shortId,
            department: roleContributor[0].department,
            email: roleContributor[0].email,
            mobileNumber: roleContributor[0].mobileNumber,
            roles: roleValues
        };
        if (!editRoleCol) {
            const updatedUserRoleList = userRoleMappingList || [];
            updatedUserRoleList.push(roleMappingData);
            setUserRoleMappingList(updatedUserRoleList);
            setAllUserRoleMappingList([...allUserRoleMappingList, roleMappingData]);
        } else {
            const updatedUserRoleList = userRoleMappingList.map((user: any) => {
                if (user.shortId === roleMappingData.shortId) {
                    return {
                        ...user,
                        roles: roleValues
                    };
                }
                return user;
            });
            const updatedAllUserRoleList = allUserRoleMappingList.map((user: any) => {
                if (user.shortId === roleMappingData.shortId) {
                    return {
                        ...user,
                        roles: roleValues
                    };
                }
                return user;
            });
            setUserRoleMappingList(updatedUserRoleList);
            setAllUserRoleMappingList(updatedAllUserRoleList);
        }
        setShowEditOrCreateModal(false);
        setRoleContributor([]);
        setRoleValues([]);
        setEditRoleCol(false);
        setTimeout(() => {
            SelectBox.defaultSetup();
        }, 100);
    };

    return (
        <React.Fragment>
            <div className={classNames(Styles.provisionStyles)}>
                <div className={classNames(Styles.wrapper)}>
                    <div className={classNames('decriptionSection', 'mbc-scroll')}>
                        <h3 className={classNames(Styles.title)}>Role Mapping</h3>
                        <div className={classNames(Styles.parentRoleMapping)}>
                            <div className={Styles.warningWrapper}>
                                {!CODE_SPACE_STATUS.includes(config?.status) &&
                                    <p style={{ color: 'var(--color-orange)' }}
                                        className={classNames((props.readOnlyMode ? ' hidden' : ''))}>
                                        <i className="icon mbc-icon alert circle">
                                        </i> Once the config is in published state, Can Add / Edit Role Mapping</p>}
                            </div>
                            <div className={classNames(Styles.createEntitlementButton)}>
                                {props.config?.roles?.length > 0 && <button className={classNames('btn add-dataiku-container btn-primary',
                                    Styles.createButton + (props.readOnlyMode ? ' hidden' : ''))} type="button"
                                    onClick={() => {
                                        setEditRoleCol(false);
                                        setShowEditOrCreateModal(true);
                                    }}
                                    title={!CODE_SPACE_STATUS.includes(config?.status) ? 'Once the config is in published state, can add New Role mapping.' : ''}
                                    disabled={!CODE_SPACE_STATUS.includes(config?.status)}>
                                    <i className="icon mbc-icon plus" />
                                    <span>Create New Role Mapping</span>
                                </button>}
                            </div>
                        </div>
                        {allUserRoleMappingList?.length > 0 ? <div className={Styles.row}>
                            <table className="ul-table users">
                                <thead>
                                    <tr className="header-row">
                                        <th>
                                            <label>
                                                User
                                            </label>
                                        </th>
                                        <th>
                                            <label>
                                                Role
                                            </label>
                                        </th>
                                        <th className="actionColumn">
                                            <label>Action</label>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {userRoleMappingList?.map((item: any, index) => (
                                        <tr
                                            key={index}
                                            className={classNames('data-row')}>
                                            <td className="wrap-text">
                                                <span >{item.firstName} {item.lastName}</span>
                                            </td>
                                            <td className="wrap-text">
                                                <div className={Styles.tagColumn}>
                                                    {item.roles?.map((role: any, index: number) => (
                                                        <div className="chips read-only" key={index}>
                                                            <label className="name">{role.name}</label>
                                                        </div>
                                                    ))}

                                                </div>
                                            </td>
                                            <td className={classNames("wrap-text", Styles.actionBtn)}>
                                                <button
                                                    onClick={() => editRoleMapping(item)}
                                                    className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                                                    type="button"
                                                    title={!CODE_SPACE_STATUS.includes(config?.status) ? 'Once the config is in published state, can edit Role mapping.' : ''}
                                                    disabled={!CODE_SPACE_STATUS.includes(config?.status)}
                                                >
                                                    <i className="icon mbc-icon edit" />
                                                </button>
                                                &nbsp; &nbsp;
                                                <button
                                                    onClick={() => deleteRoleMapping(item)}
                                                    className={Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')}
                                                    type="button"
                                                    title={!CODE_SPACE_STATUS.includes(config?.status) ? 'Once the config is in published state, can delete Role mapping.' : ''}
                                                    disabled={!CODE_SPACE_STATUS.includes(config?.status)}
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
                        </div> : <div className={classNames('no-data', Styles.noData)}> No Role Mapping found</div>}
                    </div>
                </div>
            </div>

            <div className="btnConatiner">
                <div className="btn-set">
                    {
                        CODE_SPACE_STATUS.includes(config?.status) ?
                            <button className={"btn btn-primary" + (props.readOnlyMode ? ' hidden' : '')} type="button" onClick={userRoleSubmit}>
                                Save
                            </button> : ''
                    }
                    <button
                        className={'btn btn-tertiary ' + classNames(Styles.publishBtn) + (props.readOnlyMode ? ' hidden' : '')}
                        type="button"
                        onClick={onPublish}
                        disabled={!CODE_SPACE_STATUS.includes(config?.status)}
                    >
                        {CODE_SPACE_STATUS.includes(config?.status) ? 'Request' : config?.status === 'REQUESTED' ? 'Publish' : 'Accept'}
                    </button>
                    {props.readOnlyMode ?
                        <button  className={'btn btn-primary ' + classNames(Styles.publishBtn)} type="button" onClick={backPress}>
                            Back
                        </button> : ''
                    }
                </div>
            </div>
            <Modal
                title={editRoleCol ? 'Edit Role Mapping' : 'Create a New Role mapping'}
                showAcceptButton={false}
                showCancelButton={false}
                modalWidth={'40%'}
                buttonAlignment="right"
                show={showEditOrCreateModal}
                content={
                    <div>
                        <div className={Styles.bucketColContent}>
                            <div className={Styles.bucketColContentList}>
                                <div className={Styles.bucketColContentListAdd}>
                                    {userError.length ?
                                        <> <div>
                                            <span className={classNames('error-message', userError.length ? '' : 'hide')}>
                                                {userError}
                                            </span>
                                        </div> <br /></> : ''
                                    }
                                    {roleContributor?.length === 0 && <AddUser getCollabarators={getCollabarators} dagId={''} isRequired={false} isUserprivilegeSearch={false} />}
                                </div>
                                <div className={Styles.bucketColUsersList}>
                                    {roleContributor?.length > 0 ? (
                                        <React.Fragment>
                                            <div className={Styles.collUserTitle}>
                                                <div className={Styles.collUserTitleCol}>User ID</div>
                                                <div className={Styles.collUserTitleCol}>Name</div>
                                                <div className={Styles.collUserTitleCol}></div>
                                            </div>
                                            <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                                                {roleContributor
                                                    ?.map((item: any, collIndex: any) => {
                                                        return (
                                                            <>
                                                                <div key={'team-member-' + collIndex} className={Styles.collUserContentRow}>
                                                                    <div className={Styles.collUserTitleCol}>{item.shortId}</div>
                                                                    <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                                                                    {!editRoleCol && <div className={Styles.collUserTitleCol}>
                                                                        <div className={Styles.deleteEntry} onClick={() => deleteCollaborator(item)}>
                                                                            <i className="icon mbc-icon trash-outline" />
                                                                        </div>
                                                                    </div>}
                                                                </div>
                                                                <br />
                                                            </>
                                                        );
                                                    })}
                                            </div>
                                        </React.Fragment>
                                    ) : (
                                        <div className={Styles.bucketColContentEmpty}>
                                            <h6> Users Not Exist!</h6>
                                        </div>
                                    )}
                                </div>
                                <div
                                    id="roleContainer"
                                    className={classNames('input-field-group include-error', rolesError.length ? 'error' : '')}
                                >
                                    <label id="roleLabel" className="input-label" htmlFor="roleSelect">
                                        Roles <sup>*</sup>
                                    </label>
                                    <div id="location" className="custom-select">
                                        <select
                                            id="locationSelect"
                                            multiple={true}
                                            required={true}
                                            required-error={requiredError}
                                            onChange={onChangeRole}
                                            value={roleValues?.map((role: any) => role.id)}
                                        >
                                            {allRoleList.map((obj) => (
                                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                                    {obj.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <span className={classNames('error-message', rolesError.length ? '' : 'hide')}>
                                        {rolesError}
                                    </span>
                                </div>

                                <div className={Styles.createBtn}>
                                    <button
                                        className={'btn btn-tertiary'}
                                        type="button"
                                        onClick={onRoleSubmit}
                                    >
                                        <span>{'Submit Changes'}</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                }
                scrollableContent={true}
                onCancel={() => cancelEditOrCreateModal()}
            />
        </React.Fragment>
    );
}
export default RoleMapping;
