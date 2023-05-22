import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './CreateOrEditProject.scss';
import AddUser from 'dna-container/AddUser';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { dataikuApi } from '../../apis/dataiku.api';
import SelectBox from 'dna-container/SelectBox';
import Tags from 'dna-container/Tags';

const classNames = cn.bind(Styles);

const CreateOrEditProject = (props) => {
    const [projectErrorMessage, setProjectErrorMessage] = useState('');
    const [projectKey, setProjectKey] = useState('');
    const [projectDescription, setProjectDescription] = useState('');
    const [projectDescriptionErrorMessage, setProjectDescriptionErrorMessage] = useState('');

    const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
    const [dataClassification, setDataClassification] = useState('Choose');
    const [dataClassificationError, setDataClassificationError] = useState('');
    const [PII, setPII] = useState(false);
    const [divisionValue, setDivisionValue] = useState({});
    const [divisions, setDivisions] = useState([]);
    const [subDivisions, setSubDivisions] = useState([]);

    const [editDataikuProjectDetails, setEditDataikuProjectDetails] = useState({});
    const [currentUserCollab, setCurrentUserCollab] = useState([]);

    const [departmentTags, setDepartmentTags] = useState([]);
    const [selectedDepartmentTags, setSelectedDepartmentTags] = useState([]);
    const [statusValue, setStatusValue] = useState('Choose');
    const [statusError, setStatusError] = useState('');
    const [divisionError, setDivisionError] = useState('');
    const departmentValue = selectedDepartmentTags?.map((department) => department?.toUpperCase());
    const [showDepartmentMissingError, setShowDepartmentMissingError] = useState(false);
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
    const [dataikuCollaborators, setDataikuCollaborators] = useState(props.isEdit && props?.editDataikuProjectDetail?.collaborators?.length > 0 ? props?.editDataikuProjectDetail.collaborators : []);
    const [projectGroupErrorMessage, setProjectGroupErrorMessage] = useState('');
    const [projectGroup, setProjectGroup] = useState('');
    const errorMissingEntry = '*Missing entry';
    let isFormValid = false;

    useEffect(() => {
        ProgressIndicator.show();
        dataikuApi.getLovData()
            .then((response) => {
                setDataClassificationDropdown(response[0]?.data?.data || []);
                setDivisions(response[1]?.data || []);
                setDepartmentTags(response[2]?.data?.data || []);
                SelectBox.defaultSetup();
            })
            .catch((err) => {
                ProgressIndicator.hide();
                SelectBox.defaultSetup();
                if (err?.response?.data?.errors?.length > 0) {
                    err?.response?.data?.errors.forEach((err) => {
                        showErrorNotification(err?.message || 'Something went wrong.');
                    });
                } else {
                    showErrorNotification('Error while fetching Initial', 'alert');
                }
            })
            .finally(() => {
                // validateUser(props?.user?.id);
                if (props.isEdit) {
                    getProjectDetailsByName(props?.editDataikuProjectDetail?.projectKey, props?.editDataikuProjectDetail?.cloudProfile);
                }
                ProgressIndicator.hide();
            });
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    // get project details by name for edit.
    const getProjectDetailsByName = (projectKey, cloudProfile) => {
        ProgressIndicator.show();
        dataikuApi.getDnaDataikuProjectByName(projectKey, cloudProfile)
            .then((response) => {
                if (response?.data?.data) {
                    const data = response?.data?.data;
                    setEditDataikuProjectDetails(data);
                    if (data?.projectName) {
                        setProjectKey(data.projectName);
                    }
                    if (data?.classificationType) {
                        setDataClassification(data.classificationType);
                    }
                    if (data?.description) {
                        setProjectDescription(data.description);
                    }
                    if (data?.hasPii) {
                        setPII(data.hasPii);
                    }
                    if (data?.divisionId && data?.divisionName) {
                        const division = { id: data.divisionId, name: data.divisionName, subdivision: { id: null, name: null } };
                        ProgressIndicator.show();
                        dataikuApi.getSubDivisions(division.id).then((subDivisions) => {
                            if (!subDivisions?.data?.length) {
                                subDivisions = [{ id: '0', name: 'None' }];
                            } else {
                                subDivisions = subDivisions.data;
                                division.subdivision = { id: '0', name: 'Choose' };
                                if (data?.subdivisionId && data?.subdivisionName) {
                                    division.subdivision.id = data.subdivisionId;
                                    division.subdivision.name = data.subdivisionName;
                                }
                            }
                            setSubDivisions(subDivisions);
                            setDivisionValue(division);
                            SelectBox.defaultSetup();
                        }).catch((err) => {
                            ProgressIndicator.hide();
                            if (err?.response?.data?.response?.errors?.length > 0) {
                                err?.response?.data?.response?.errors.forEach((err) => {
                                    showErrorNotification(err?.message || 'Something went wrong.');
                                });
                            } else {
                                showErrorNotification('Something went wrong.');
                            }
                        }).finally(() => {
                            ProgressIndicator.hide();
                        });
                    }

                    if (data?.department) {
                        setSelectedDepartmentTags([data?.department]);
                    }

                    if (data?.status) {
                        setStatusValue(data.status);
                    }

                    if (data?.cloudProfile) {
                        setProjectGroup(data?.cloudProfile);
                    }

                    if (data?.collaborators && data?.collaborators.length > 0) {
                        const currentUserCollabList = [];
                        const remainingUsersColloab = [];

                        data?.collaborators.forEach((user) => {
                            if (user.userId === props?.user?.id) {
                                currentUserCollabList.push(user);
                            } else {
                                remainingUsersColloab.push(user);
                            }
                        });
                        setCurrentUserCollab(currentUserCollabList);
                        setDataikuCollaborators(remainingUsersColloab);
                    }

                    SelectBox.defaultSetup();
                }
            })
            .catch((err) => {
                ProgressIndicator.hide();
                if (err?.response?.data?.errors?.length > 0) {
                    err?.response?.data?.errors.forEach((err) => {
                        showErrorNotification(err?.message || 'Something went wrong.');
                    });
                } else {
                    showErrorNotification('Something went wrong.');
                }
                props.callDnaDataList();
            });
    }

    const onChangeStatus = (e) => {
        setStatusValue(e.target.value);
    }

    const validateForm = () => {
        if (!projectKey) {
            setProjectErrorMessage('Project name is required *');
            isFormValid = false;
        }
        if (statusValue === '0') {
            setStatusError(errorMissingEntry)
            isFormValid = false;
        }
        if (!divisionValue || divisionValue?.name === 'Choose') {
            setDivisionError(errorMissingEntry)
            isFormValid = false;
        }
        if (!projectGroup) {
            setProjectGroupErrorMessage('Please select dataiku instance *');
            isFormValid = false;
        }
        if (!projectDescription) {
            setProjectDescriptionErrorMessage('Project Description is required *');
            isFormValid = false;
        }
        if (dataClassification === '0') {
            setDataClassificationError(errorMissingEntry);
            isFormValid = false;
        }
        if (selectedDepartmentTags.length === 0) {
            setShowDepartmentMissingError(true);
            isFormValid = false;
        }

        if (
            (dataClassification && !(dataClassification === '0'))
            && (statusValue && !(statusValue === '0'))
            && (divisionValue && !(divisionValue?.name === 'Choose'))
            && projectGroup
            && projectKey
            && projectDescription
            && selectedDepartmentTags.length > 0
        ) {
            isFormValid = true;
        } else {
            isFormValid = false;
        }
    }

    const handleCreateDataikuSubmit = () => {
        validateForm();
        if (isFormValid) {
            setProjectGroupErrorMessage('');
            setProjectErrorMessage('');
            setStatusError('');
            if (!props.isEdit) {
                createDataikuProject();
            } else {
                updtateDataikuProject();
            }
        }
    }

    const updtateDataikuProject = () => {
        ProgressIndicator.show();
        const data = {
            data: {
                id: editDataikuProjectDetails?.id,
                projectName: editDataikuProjectDetails.projectKey,
                cloudProfile: editDataikuProjectDetails.cloudProfile,
                collaborators: [...currentUserCollab, ...dataikuCollaborators],
                createdBy: editDataikuProjectDetails.createdBy,
                createdOn: editDataikuProjectDetails.createdOn,
                description: projectDescription,
                status: statusValue,
                classificationType: dataClassification,
                hasPii: PII,
                divisionId: divisionValue?.id,
                divisionName: divisionValue?.name,
                subdivisionId: divisionValue.subdivision.id === '0' ? '' : divisionValue.subdivision.id,
                subdivisionName:
                    divisionValue.subdivision.name === 'Choose'
                        ? '' : divisionValue.subdivision.name === 'None'
                            ? '' : divisionValue.subdivision.name,
                department: selectedDepartmentTags[0]
            }
        }
        dataikuApi
            .updateDataikuProjects(data, editDataikuProjectDetails.id)
            .then((response) => {
                const data = response.data;
                if (data.response.success === 'SUCCESS') {
                    Notification.show('Dataiku project updated successfully');
                    setProjectKey('');
                    setProjectGroup('');
                    setDataikuCollaborators([]);
                    ProgressIndicator.hide();
                    props.callDnaDataList();
                } else {
                    ProgressIndicator.hide();
                    Notification.show(
                        'Error while creating dataiku project.\n' + data?.response?.errors[0]?.message,
                        'alert',
                    );
                }
            })
            .catch((err) => {
                err;
                if (err?.response?.data?.errors?.length > 0) {
                    err?.response?.data?.errors.forEach((err) => {
                        showErrorNotification(err?.message || 'Something went wrong.');
                    });
                } else {
                    showErrorNotification('Something went wrong.');
                }
                ProgressIndicator.hide();
            });
    }

    const createDataikuProject = () => {
        ProgressIndicator.show();
        const data = {
            data: {
                id: "",
                projectName: projectKey,
                description: projectDescription,
                cloudProfile: projectGroup,
                collaborators: dataikuCollaborators,
                createdBy: "",
                createdOn: "",
                status: statusValue,
                classificationType: dataClassification,
                hasPii: PII,
                divisionId: divisionValue?.id,
                divisionName: divisionValue?.name,
                subdivisionId: divisionValue.subdivision.id === '0' ? '' : divisionValue.subdivision.id,
                subdivisionName:
                    divisionValue.subdivision.name === 'Choose'
                        ? '' : divisionValue.subdivision.name === 'None'
                            ? '' : divisionValue.subdivision.name,
                department: selectedDepartmentTags[0]
            }
        }
        dataikuApi
            .createNewDataikuProjects(data)
            .then((response) => {
                const data = response.data;
                if (data.response.success === 'SUCCESS') {
                    Notification.show('Dataiku project created successfully');
                    setProjectKey('');
                    setDataikuCollaborators([]);
                    ProgressIndicator.hide();
                    props.callDnaDataList();
                } else {
                    ProgressIndicator.hide();
                    Notification.show(
                        'Error while creating dataiku project.\n' + data.response.errors[0].message,
                        'alert',
                    );
                }
            })
            .catch((err) => {
                err;
                if (err?.response?.data?.errors?.length > 0) {
                    err?.response?.data?.errors.forEach((err) => {
                        showErrorNotification(err?.message || 'Something went wrong.');
                    });
                } else {
                    showErrorNotification('Something went wrong.');
                }
                ProgressIndicator.hide();
            });
    }

    const showErrorNotification = (message) => {
        ProgressIndicator.hide();
        Notification.show(message, 'alert');
    };

    const getCollabarators = (collaborators) => {
        const collabarationData = {
            id: collaborators.id,
            userId: collaborators.userId,
            profile: collaborators.profile,
            givenName: collaborators.givenName,
            surName: collaborators.surName,
            permission: 'reader'
        };

        let duplicateMember = false;
        duplicateMember = dataikuCollaborators?.filter((member) => member.userId === collaborators.userId)?.length
            ? true
            : false;

        const isCreator = props?.user?.id === collaborators?.userId;

        if (duplicateMember) {
            Notification.show('Collaborator Already Exist.', 'warning');
        } else if (isCreator) {
            Notification.show(
                `${collaborators.givenName} ${collaborators.surName} is a creator. Creator can't be added as collaborator.`,
                'warning',
            );
        } else {
            dataikuCollaborators.push(collabarationData);
            setDataikuCollaborators([...dataikuCollaborators]);
        }
    }

    const handleDataClassification = (e) => {
        setDataClassification(e.target.value);
    };

    const handlePII = (e) => {
        setTimeout(() => {
            setPII(e.target.value === 'true' ? true : false);
        });
    };

    const setDepartment = (arr) => {
        const department = arr?.map((item) => item.toUpperCase());
        setSelectedDepartmentTags(department);
        setShowDepartmentMissingError(arr.length === 0);
    };

    const onDivisionChange = (e) => {
        const selectedOptions = e.currentTarget.selectedOptions;
        const division = { id: '0', name: null, subdivision: { id: divisionValue?.subdivision?.id || null, name: divisionValue?.subdivision?.name || null } };
        if (selectedOptions.length) {
            division.id = selectedOptions[0].value;
            division.name = selectedOptions[0].textContent;
            if (division.id !== '0' && division.id !== divisionValue.id) {
                ProgressIndicator.show();
                dataikuApi.getSubDivisions(division.id).then((subDivisions) => {
                    if (!subDivisions?.data?.length) {
                        subDivisions = [{ id: '0', name: 'None' }];
                    } else {
                        subDivisions = subDivisions.data;
                        division.subdivision = { id: '0', name: 'Choose' };
                    }
                    setSubDivisions(subDivisions);
                    SelectBox.defaultSetup();
                    ProgressIndicator.hide();
                });
            }
        }
        setDivisionValue(division);
    };

    const onSubDivisionChange = (e) => {
        const selectedOptions = e.currentTarget.selectedOptions;
        const subDivision = { id: null, name: null };
        if (selectedOptions.length) {
            subDivision.id = selectedOptions[0].value;
            subDivision.name = selectedOptions[0].textContent;
            const divisions = divisionValue;
            divisions.subdivision = subDivision;
            setDivisionValue(divisions)
        }
    };

    let timeoutId = null;
    const onCollaboratorPermission = (value, userName) => {
        const updatedPermissionList = dataikuCollaborators.map(obj => {
            if (obj.userId === userName) {
                return Object.assign({}, obj, { permission: value });
            }
            return obj;
        });

        // Clear the previous timeout
        if (timeoutId) {
            clearTimeout(timeoutId);
        }

        // Set a new timeout to update the state after a delay
        timeoutId = setTimeout(() => {
            setDataikuCollaborators(updatedPermissionList);
            timeoutId = null; // Clear the timeoutId after it has been executed
        });
    };

    const onCollabaratorDelete = (userName) => {
        return () => {
            const currentCollList = dataikuCollaborators.filter((item) => {
                return item.userId !== userName;
            });
            setDataikuCollaborators(currentCollList);
        };
    };

    return (
        <div className={Styles.createDataikuWrapper}>
            <div>
                <div className={Styles.flexLayout}>
                    <div className={classNames('input-field-group include-error', projectErrorMessage.length ? 'error' : '')}>
                        <label id="projectNameLabel" htmlFor="projectNameInput" className="input-label">
                            Project Name <sup>*</sup>
                        </label>
                        {props.isEdit ? <div>{props.editDataikuProjectDetail.projectKey}</div> : <input
                            type="text"
                            className="input-field"
                            id="projectNameInput"
                            maxLength={22}
                            placeholder="Type here"
                            autoComplete="off"
                            value={projectKey}
                            onChange={(e) => {
                                if (e.target.value.trim().length > 0) {
                                    setProjectErrorMessage('');
                                }
                                setProjectKey(e.target.value?.toUpperCase());
                            }}
                        />}
                        <span className={classNames('error-message')}>{projectErrorMessage}</span>
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <div className={classNames('input-field-group include-error', projectDescriptionErrorMessage.length ? 'error' : '')}>
                        <label id="projectDescriptionLabel" htmlFor="projectDescriptionInput" className="input-label">
                            Project description <sup>*</sup>
                        </label>
                        <input
                            type="text"
                            className="input-field"
                            id="projectDescriptionInput"
                            maxLength={22}
                            placeholder="Type here"
                            autoComplete="off"
                            value={projectDescription}
                            onChange={(e) => {
                                if (e.target.value?.trim().length > 0) {
                                    setProjectDescriptionErrorMessage('');
                                }
                                setProjectDescription(e.target.value);
                            }}
                        />
                        <span className={classNames('error-message')}>{projectDescriptionErrorMessage}</span>
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <div
                        className={classNames(
                            'input-field-group include-error',
                            dataClassificationError?.length ? 'error' : '',
                        )}
                    >
                        <label className={classNames(Styles.inputLabel, 'input-label')}
                            htmlFor="ClassificationField">
                            Data Classification  <sup>*</sup>
                        </label>
                        <div className={classNames('custom-select')}>
                            <select
                                id="ClassificationField"
                                required={true}
                                required-error={errorMissingEntry}
                                onChange={handleDataClassification}
                                value={dataClassification}>
                                <option id="classificationOption" value={0}>
                                    Choose
                                </option>
                                {dataClassificationDropdown?.length
                                    ? dataClassificationDropdown?.map((item) => (
                                        <option
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
                </div>
                <div className={classNames(Styles.flexLayout, Styles.secondFlexLayout)}>
                    <div className={Styles.divisionContainer}>
                        <div
                            className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}>
                            <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                                Division <sup>*</sup> &nbsp;
                            </label>
                            <div className="custom-select">
                                <select
                                    id="divisionField"
                                    required={true}
                                    required-error={errorMissingEntry}
                                    onChange={(e) => onDivisionChange(e)}
                                    value={divisionValue?.id}
                                >
                                    <option id="divisionOption" value={0}>
                                        Choose
                                    </option>
                                    {divisions?.map((obj) => (
                                        <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                            {obj.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                                {divisionError}
                            </span>
                        </div>
                    </div>&nbsp;&nbsp;&nbsp;
                    <div className={Styles.subDivisionContainer}>
                        <div className={classNames('input-field-group')}>
                            <label id="subDivisionLabel" htmlFor="subDivisionField" className="input-label">
                                Sub Division &nbsp;
                            </label>
                            <div className="custom-select">
                                <select
                                    id="subDivisionField"
                                    onChange={(e) => onSubDivisionChange(e)}
                                    value={divisionValue?.subdivision?.id || '0'}
                                >
                                    {subDivisions.some((item) => item.id === '0' && item.name === 'None') ? (
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
                        </div>
                    </div>&nbsp;&nbsp;&nbsp;
                    <div className={classNames('input-field-group include-error')}>
                        <label className={classNames(Styles.inputLabel, 'input-label')}>
                            PII (Personally Identifiable Information) <sup>*</sup>
                        </label>
                        <div className={Styles.pIIField}>
                            <label className={classNames('radio')}>
                                <span className="wrapper">
                                    <input
                                        type="radio"
                                        className="ff-only"
                                        value={true}
                                        name="pii"
                                        onChange={(e) => handlePII(e)}
                                        checked={PII === true}
                                    />
                                </span>
                                <span className="label">Yes</span>
                            </label>
                            <label className={classNames('radio')}>
                                <span className="wrapper">
                                    <input
                                        type="radio"
                                        className="ff-only"
                                        value={false}
                                        name="pii"
                                        onChange={(e) => handlePII(e)}
                                        checked={PII === false}
                                    />
                                </span>
                                <span className="label">No</span>
                            </label>
                        </div>
                    </div>
                </div>

                <div className={classNames(Styles.flexLayout, Styles.secondFlexLayout)}>

                    <div className={Styles.departmentTags}>
                        <Tags
                            title={'E2-Department'}
                            max={1}
                            chips={departmentValue}
                            tags={departmentTags}
                            setTags={setDepartment}
                            isMandatory={true}
                            showMissingEntryError={showDepartmentMissingError}
                        />
                    </div>&nbsp;&nbsp;&nbsp;

                    <div>
                        <div
                            className={classNames('input-field-group include-error', statusError.length ? 'error' : '')}
                        >
                            <label id="reportStatusLabel" htmlFor="reportStatusField" className="input-label">
                                Status <sup>*</sup>
                            </label>
                            <div className="custom-select">
                                <select
                                    id="reportStatusField"
                                    required={true}
                                    required-error={errorMissingEntry}
                                    onChange={onChangeStatus}
                                    value={statusValue}
                                >
                                    <option id="reportStatusOption" value={0}>
                                        Choose
                                    </option>
                                    {statuses?.map((obj) => (
                                        <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                            {obj.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <span className={classNames('error-message', statusError.length ? '' : 'hide')}>
                                {statusError}
                            </span>
                        </div>
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <div className={classNames('input-field-group include-error', projectGroupErrorMessage.length ? 'error' : '')}>
                        <label id="Instance" className="input-label" htmlFor="Instance">
                            Instance <sup>*</sup>
                        </label>
                        <div className={Styles.radioBtnsGrid}>
                            <div key={'on-Premise'}>
                                <label className={'radio'}>
                                    <span className="wrapper">
                                        <input
                                            type="radio"
                                            className="ff-only"
                                            name="projectGroup"
                                            value={projectGroup}
                                            onChange={() => {
                                                setProjectGroup('onPremise');
                                                setProjectGroupErrorMessage('');
                                            }}
                                            checked={projectGroup === 'onPremise'}
                                            disabled={props.isEdit}
                                        />
                                    </span>
                                    <span className="label">{'on-Premise'}</span>
                                </label>
                            </div>
                            <div key={'eXtollo'}>
                                <label className={'radio'}>
                                    <span className="wrapper">
                                        <input
                                            type="radio"
                                            className="ff-only"
                                            name="projectGroup"
                                            value={projectGroup}
                                            onChange={() => {
                                                setProjectGroup('eXtollo');
                                                setProjectGroupErrorMessage('');
                                            }}
                                            checked={projectGroup === 'eXtollo'}
                                            disabled={props.isEdit}
                                        />
                                    </span>
                                    <span className="label">{'eXtollo'}</span>
                                </label>
                            </div>
                        </div>
                        <span className={classNames('error-message')}> {projectGroupErrorMessage}</span>
                    </div>
                </div>
            </div>
            <div className={classNames('input-field-group include-error', Styles.adduser)}>
                <AddUser getCollabarators={getCollabarators} isRequired={false} isUserprivilegeSearch={true} />
            </div>
            <div className={Styles.bucketColUsersList}>
                {dataikuCollaborators?.length > 0 ? (
                    <React.Fragment>
                        <div className={Styles.collUserTitle}>
                            <div className={Styles.collUserTitleCol}>User ID</div>
                            <div className={Styles.collUserTitleCol}>Name</div>
                            <div className={Styles.collUserTitleCol}>Permission</div>
                            <div className={Styles.collUserTitleCol}></div>
                        </div>
                        <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                            {dataikuCollaborators
                                ?.map((item) => {
                                    return (
                                        <div key={item.userId} className={Styles.collUserContentRow}>
                                            <div className={Styles.collUserTitleCol}>{item.userId}</div>
                                            <div className={Styles.collUserTitleCol}>{item.givenName + ' ' + item.surName}</div>
                                            <div className={Styles.collUserTitleCol} key={item.userId}>
                                                <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                                        <span className="wrapper">
                                                            <input
                                                                type="radio"
                                                                className="ff-only"
                                                                name={item.userId}
                                                                value="administrator"
                                                                checked={item.permission === 'administrator'}
                                                                onChange={() => onCollaboratorPermission("administrator", item?.userId)}
                                                            />
                                                        </span>
                                                        <span className="label">Administrator</span>
                                                    </label>
                                                </div>
                                                &nbsp;&nbsp;&nbsp;
                                                <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                                    <label className={'checkbox'}>
                                                        <span className="wrapper">
                                                            <input
                                                                type="radio"
                                                                className="ff-only"
                                                                name={item.userId}
                                                                value="contributor"
                                                                checked={item.permission === 'contributor'}
                                                                onChange={() => onCollaboratorPermission('contributor', item.userId)}
                                                            />
                                                        </span>
                                                        <span className="label">contributor</span>
                                                    </label>
                                                </div>
                                                &nbsp;&nbsp;&nbsp;
                                                <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                                    <label className={'checkbox'}>
                                                        <span className="wrapper">
                                                            <input
                                                                type="radio"
                                                                className="ff-only"
                                                                name={item.userId}
                                                                value="reader"
                                                                checked={item.permission === 'reader'}
                                                                onChange={() => onCollaboratorPermission('reader', item.userId)}
                                                            />
                                                        </span>
                                                        <span className="label">reader</span>
                                                    </label>
                                                </div>
                                            </div>
                                            <div className={Styles.collUserTitleCol}>
                                                <div className={Styles.deleteEntry}
                                                    onClick={onCollabaratorDelete(item.userId)}
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
            <div className={Styles.submitButtton}>
                <button
                    className={classNames(!props.isUserCanCreateDataiku ? 'btn indraft' : 'btn btn-tertiary')}
                    type="button"
                    onClick={(() => {
                        handleCreateDataikuSubmit();
                    })}
                    title='users with a access can only create a dataiku project'
                    disabled={!props.isUserCanCreateDataiku}
                >
                    {'Submit'}
                </button>
            </div>
            <div>
            </div>
        </div>
    );
};

export default CreateOrEditProject;
