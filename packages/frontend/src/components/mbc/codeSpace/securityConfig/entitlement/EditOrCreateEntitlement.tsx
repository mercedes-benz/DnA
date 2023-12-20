import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './EditOrCreateEntitlement.scss';
import { HTTP_OPTIONS } from 'globals/constants';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

const classNames = cn.bind(Styles);

const EditOrCreateEntitlement = (props: any) => {
    const [EntitlName, setEntitlName] = useState<string>('');
    const [beforeUpdateEntitlName, setbeforeUpdateEntitlName] = useState<string>('');
    const [missingEntryEntitlName, setmissingEntryEntitlName] = useState<string>('');
    const [entitlPath, setEntitlPath] = useState<string>('');
    const [missingEntryEntlPath, setmissingEntryEntlPath] = useState<string>('');
    const [missingEntryEntlMethod, setmissingEntryEntlMethod] = useState<string>('');
    const [missingAddMesage, setMissingAddMesage] = useState<string>('');
    const [httpMethod, setHttpMethod] = useState<string>('');
    const [currentEntitlement, setCurrentEntitlement] = useState<any>({ apiList: [] });

    const onEntitlementNameOnChange = (e: React.FormEvent<HTMLInputElement>) => {
        setEntitlName(e.currentTarget.value);
        setmissingEntryEntitlName('');
    };

    const validateForm = () => {
        let formValid = true;
        const errorMissingEntry = '*Missing entry';
        if (EntitlName.length === 0) {
            setmissingEntryEntitlName(errorMissingEntry);
            formValid = false;
        }
        if (entitlPath.length === 0) {
            setmissingEntryEntlPath(errorMissingEntry);
            formValid = false;
        }
        if (httpMethod === '0' || httpMethod?.trim()?.length === 0) {
            setmissingEntryEntlMethod(errorMissingEntry);
            formValid = false;
        }
        setTimeout(() => {
            const anyErrorDetected = document.querySelector('.error');
            anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 100);
        return formValid;
    };

    const onEntitlementSubmit = () => {
        let formValid = true;
        const errorMissingEntry = '*Missing entry';
        if (props.isProtectedByDna && (currentEntitlement?.apiList?.length === 0 || !currentEntitlement)) {
            formValid = validateForm();
            if ((httpMethod !== '0' || httpMethod?.trim()?.length > 0) && entitlPath?.length !== 0) {
                setMissingAddMesage(" <- Click on Add to add API Path/Pattern and Http Method.");
                formValid = false;
            }
        } else {
            if (EntitlName.length === 0) {
                setmissingEntryEntitlName(errorMissingEntry);
                formValid = false;
            }
        }

        if (formValid) {
            props.submitEntitlement({ ...currentEntitlement, beforeUpdateEntitlName: beforeUpdateEntitlName, name: EntitlName });
        }
    };
    const onExistingEntitlementSubmit = () => {
        setMissingAddMesage('');
        if (validateForm()) {
            let currentEntitlementList = currentEntitlement || { apiList: [] };
            if (currentEntitlementList?.apiList?.length === 0) {
                currentEntitlementList = {
                    name: EntitlName,
                    apiList: [{
                        apiPattern: entitlPath,
                        httpMethod: httpMethod
                    }]
                }
            } else {
                if (currentEntitlementList.apiList) {
                    currentEntitlementList.apiList.push({
                        apiPattern: entitlPath,
                        httpMethod: httpMethod
                    });
                } else {
                    currentEntitlementList.apiList = [{
                        apiPattern: entitlPath,
                        httpMethod: httpMethod
                    }];
                }
            }

            setCurrentEntitlement({ ...currentEntitlementList });
            setHttpMethod('');
            setEntitlPath('');
            setTimeout(() => {
                SelectBox.defaultSetup();
            }, 100);
        }
    };

    const ontEntitlPatOnChange = (e: any) => {
        setEntitlPath(e.currentTarget.value);
        setmissingEntryEntlPath('');
    };

    const deleteApiElement = (apiPattern: string, httpMethod: string) => {
        const updatedEntitlementList = currentEntitlement;
        updatedEntitlementList?.apiList?.splice(
            currentEntitlement?.apiList?.findIndex((item: any) => item.apiPattern === apiPattern && item.httpMethod === httpMethod),
            1
        );
        setCurrentEntitlement({ ...updatedEntitlementList });
    };

    const onChangeHttp = (e: any) => {
        setHttpMethod(e.currentTarget.value);
        setmissingEntryEntlMethod('');
    }

    useEffect(() => {
        if (props?.editEntitlementModal) {
            setEntitlName(props.editEntitlementList.name);
            setbeforeUpdateEntitlName(props.editEntitlementList.name);
        }
        setCurrentEntitlement(props.editEntitlementList);
    }, [props?.editEntitlementModal, props?.editEntitlementList?.name]);

    useEffect(() => {
        setmissingEntryEntitlName(props.entitlementNameErrorMessage);
    }, [props?.entitlementNameErrorMessage]);

    useEffect(() => {
        SelectBox.defaultSetup();
    }, []);

    return (
        <React.Fragment>
            <div className={classNames(Styles.mainPanel)}>
                <div className={Styles.content}>
                    <div className={Styles.CreateNewAirflowForm}>
                        <div className={Styles.row}>
                            <div className={classNames(Styles.inputGrp)}>
                                <div
                                    className={classNames(
                                        Styles.inputGrpChild + ' input-field-group include-error ',
                                        missingEntryEntitlName?.length ? 'error' : '',
                                    )}
                                >
                                    <label id="PrjName" htmlFor="PrjName" className="input-label">
                                        Entitlement Name<sup>*</sup>
                                    </label>
                                    <input
                                        type="text"
                                        className="input-field"
                                        required={true}
                                        id="PrjName"
                                        maxLength={64}
                                        placeholder="Type here"
                                        autoComplete="off"
                                        onChange={onEntitlementNameOnChange}
                                        value={EntitlName}
                                    />
                                    <span className={classNames('error-message', missingEntryEntitlName?.length ? '' : 'hide')}>
                                        {missingEntryEntitlName}
                                    </span>
                                </div>
                                {props.isProtectedByDna && <>
                                    <div className={classNames(Styles.fieldWrapper)}><div
                                        className={classNames(
                                            Styles.inputGrp + ' input-field-group include-error ',
                                            missingEntryEntlPath.length ? 'error' : '',
                                        )}
                                    >
                                        <label id="descriptionLabel" htmlFor="descriptionLabel" className="input-label">
                                            API Path/Pattern<sup>*</sup>
                                        </label>
                                        <input
                                            type="text"
                                            className="input-field"
                                            required={true}
                                            id="descriptionLabel"
                                            maxLength={200}
                                            placeholder="Type here"
                                            autoComplete="off"
                                            onChange={ontEntitlPatOnChange}
                                            value={entitlPath}
                                        />
                                        <span className={classNames('error-message', missingEntryEntlPath?.length ? '' : 'hide')}>
                                            {missingEntryEntlPath}
                                        </span>
                                    </div>
                                    </div>
                                    <div className={classNames(Styles.fieldWrapper)}>
                                        <div
                                            className={classNames('input-field-group include-error', missingEntryEntlMethod.length ? 'error' : '')}
                                        >
                                            <label id="reportHttpLabel" htmlFor="entitlementhttpInput" className={classNames("input-label")}>
                                                Http Method  <sup>{props.isProtectedByDna ? '*' : ''}</sup>
                                            </label>
                                            <div
                                                className="custom-select"
                                            >
                                                <select
                                                    id="entitlementhttpInput"
                                                    required={true}
                                                    required-error={'*Missing entry'}
                                                    onChange={onChangeHttp}
                                                    value={httpMethod}
                                                >
                                                    <option id="entitlemenHttpOption" value={0}>
                                                        Choose
                                                    </option>
                                                    {HTTP_OPTIONS?.map((obj: any, index: number) => (
                                                        <option id={obj.name + obj.id} key={index} value={obj.name}>
                                                            {obj.name}
                                                        </option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={Styles.AddBtn}>
                                        <button
                                            className={'btn btn-tertiary'}
                                            type="button"
                                            onClick={() => onExistingEntitlementSubmit()}
                                        >
                                            <span>Add</span>
                                        </button>
                                        <span className={classNames('error-message', missingAddMesage.length ? '' : 'hide')}>
                                            {missingAddMesage}
                                        </span>
                                    </div>
                                    {currentEntitlement?.apiList?.length > 0 &&
                                        <div className={Styles.entTileContainer}>
                                            <div className={`${Styles.entTile} ${Styles.tableHeader}`}>
                                                <div className={Styles.dagTitleCol}>API Path/Pattern</div>
                                                <div className={Styles.dagTitleCol}> Http Method</div>
                                                <div className={Styles.dagTitleCol}>Actions</div>
                                            </div>
                                            {currentEntitlement?.apiList?.map((item: any, index: number) => (
                                                <div key={index} className={Styles.entTile}>
                                                    <div className={Styles.dagTitleCol}>{item.apiPattern}</div>
                                                    <div className={Styles.dagMethodCol}>{item.httpMethod}</div>
                                                    <div className={Styles.dagTitleCol}>
                                                        <div className={Styles.actionBtnGrp}>
                                                            <React.Fragment>
                                                                <button
                                                                    onClick={() => deleteApiElement(item.apiPattern, item.httpMethod)}
                                                                    className={Styles.actionBtn + ' btn btn-primary'}
                                                                    type="button"
                                                                >
                                                                    <i className="icon mbc-icon trash-outline" />
                                                                </button>
                                                            </React.Fragment>
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    }</>}
                            </div>
                        </div>
                    </div>
                </div>
                <div className={Styles.createBtn}>
                    <button
                        className={'btn btn-tertiary'}
                        type="button"
                        onClick={onEntitlementSubmit}
                    >
                        <span>{'Submit Changes'}</span>
                    </button>
                </div>
            </div>
        </React.Fragment>
    );
}
export default EditOrCreateEntitlement;
