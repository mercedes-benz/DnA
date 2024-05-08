import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import FilterWrapper from 'dna-container/FilterWrapper';
import Styles from './Filter.scss';
import SelectBox from 'dna-container/SelectBox';
import { dataTransferApi } from '../../apis/datatransfers.api';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { trackEvent } from '../../Utility/utils';

/**
 * Datatransfer Filter Panel
 * @param {boolean} dataTransferDataLoaded datatransfer data loaded
 * @param {Function} getFilterQueryParams callback function to fetch datatransfer result
 * @param {Function} setdataTransferFilterApplied callback function to see if filter is applied
 * @returns
 */

// * @param {Function} setdatatransferDataLoaded setter for datatransfer data loaded

const DataTransferFilter = ({
    dataTransferDataLoaded,
    setdataTransferFilterApplied,
    getFilterQueryParams,
    // getDropdownValues,
    openFilters,
}) => {

    const [divisions, setDivisions] = useState([]);
    const [subDivisions, setSubDivisions] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [dataStewards, setDataStewards] = useState([]);
    const [informationOwners, setInformationOwners] = useState([]);
    const [queryParams, setQueryParams] = useState({
        division: [],
        subDivision: [],
        departments: [],
        dataStewards: [],
        informationOwners: [],

    });

    const [divisionFilterValues, setDivisionFilterValues] = useState([]);

    const portfolioFilterValues = useRef();
    const [focusedItems, setFocusedItems] = useState({});
    const [dataFilterApplied, setFilterApplied] = useState(false);

    useEffect(() => {
        ProgressIndicator.show();
        dataTransferApi.getFilterMasterData()
            .then((response) => {
                if (response) {
                    portfolioFilterValues.current = JSON.parse(
                        sessionStorage.getItem(SESSION_STORAGE_KEYS.DATATRANSFER_FILTER_VALUES),
                    );
                    const divisions = response[0].data;
                    const departments = response[1].data.data;
                    const dataStewards = response[2].data.data;
                    const informationOwners = response[3].data.data;
                    const divisionsToPass =
                        portfolioFilterValues.current && portfolioFilterValues.current.division?.length > 0
                            ? divisions?.filter((element) => portfolioFilterValues.current.division.includes(element.id))
                            : divisions;
                    dataTransferApi.getSubDivisionsData(divisionsToPass).then((subDivisionsList) => {
                        const subDivisions = [].concat(...subDivisionsList);

                        let newQueryParams = queryParams;
                        if (portfolioFilterValues.current) {
                            newQueryParams = portfolioFilterValues.current;
                            setFilterApplied(true);
                            getFilterQueryParams(newQueryParams);
                            setdataTransferFilterApplied(true);

                        } else {
                            newQueryParams.dataStewards = dataStewards?.map((dataSteward) => {
                                return dataSteward?.teamMemeber?.shortId;
                            });
                            newQueryParams.informationOwners = informationOwners?.map((informationOwner) => {
                                return informationOwner?.teamMemeber?.shortId;
                            });
                            newQueryParams.division = divisions?.map((division) => {
                                return division?.id;
                            });
                            newQueryParams.subDivision = subDivisions?.map((subDivision) => {
                                return subDivision?.id;
                            });
                            newQueryParams.departments = departments?.map((department) => {
                                return department?.name;
                            });
                            setFilterApplied(false);
                            setdataTransferFilterApplied(false);
                        }
                        setDivisions(divisions);
                        setSubDivisions(subDivisions);
                        setDepartments(departments);
                        setDataStewards(dataStewards);
                        setInformationOwners(informationOwners);
                        setQueryParams(newQueryParams);
                        setTimeout(() => { SelectBox.defaultSetup(); }, 100);
                    });
                }
                ProgressIndicator.hide();
            })
            .catch((error) => {
                showErrorNotification(error.message ? error.message : 'Some Error Occured');
            });
    }, []);

    useEffect(() => {
        SelectBox.refresh('subDivisionSelect'); // Refresh the sub division select box
    }, [divisionFilterValues]);

    // useEffect(() => {
    //     typeof getDropdownValues === 'function' &&
    //         getDropdownValues({
    //             divisions,
    //             subDivisions,
    //             departments,
    //             informationOwners, 
    //             dataStewards
    //         });
    //     // SelectBox.defaultSetup();
    // }, [divisions, subDivisions, departments, informationOwners, dataStewards]);

    const setPortfolioFilterValuesInSession = (queryParams) => {
        sessionStorage.setItem(SESSION_STORAGE_KEYS.DATATRANSFER_FILTER_VALUES, JSON.stringify(queryParams));
    };

    const showErrorNotification = (message) => {
        ProgressIndicator.hide();
        Notification.show(message, 'alert');
    };

    const onDataStewardChange = (e) => {
        const selectedValues = [];
        const selectedOptions = e.currentTarget.selectedOptions;
        const ids = [];

        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option) => {
                if(option.value === '') return; 
                const dataStewards = { id: '0', name: null };
                dataStewards.id = option.value;
                dataStewards.name = option.textContent;
                selectedValues.push(dataStewards);
                ids.push(option.value);
            });
        }

        focusedItems['dataStewards'] && applyFilter('dataStewards', ids);
    };

    const onInformationOwnerChange = (e) => {
        const selectedValues = [];
        const selectedOptions = e.currentTarget.selectedOptions;
        const ids = [];

        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option) => {
                if(option.value === '') return;
                const informationOwners = { id: '0', name: null };
                informationOwners.id = option.value;
                informationOwners.name = option.textContent;
                selectedValues.push(informationOwners);
                ids.push(option.value);
            });
        }

        focusedItems['informationOwners'] && applyFilter('informationOwners', ids);
    };


    const applyFilter = (filterName, values) => {
        if (dataTransferDataLoaded) {
            ProgressIndicator.show();
            setFilterApplied(true);
            queryParams[filterName] = values;
            if (filterName === 'division') {
                let subDivisionValues = [];
                let hasNoneValue = false;
                values.forEach((divisionId) => {
                    const subDivisions = divisions.find((division) => division.id === divisionId)?.subdivisions;
                    if (!hasNoneValue) {
                        hasNoneValue = subDivisions.some((subDiv) => subDiv.name === 'None');
                    }
                    if (subDivisions.length) {
                        const subDivVals = subDivisions.map((subDivision) => subDivision.id.indexOf('@-@') !== -1 ? subDivision.id : `${subDivision.id}@-@${divisionId}`);
                        subDivisionValues = subDivisionValues.concat(subDivVals);
                    }
                });
                if (!hasNoneValue && values.length) {
                    subDivisionValues.unshift(`EMPTY@-@${values[values.length - 1]}`);
                }
                queryParams['subDivision'] = subDivisionValues;
            }
            setPortfolioFilterValuesInSession(queryParams);
            getDatatransferByQueryParams(queryParams);
            setdataTransferFilterApplied(true);
            ProgressIndicator.hide();
            trackEvent(`All Datatransfer`, 'Filter Datatransfer List', 'From Filter Panel - ' + filterName);
        }
    };

    const onHandleFocus = (e, targetElement) => {
        //to ensure user action is done on the form fields
        setFocusedItems({ [targetElement]: true });
    };

    const onDivisionChange = (e) => {
        const selectedValues = [];
        const selectedOptions = e.currentTarget.selectedOptions;
        const ids = [];

        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option) => {
                const division = { id: '0', name: null };
                division.id = option.value;
                division.name = option.textContent;
                selectedValues.push(division);
                ids.push(option.value);
            });
        }
        focusedItems['division'] && applyFilter('division', ids);
        setDivisionFilterValues(selectedValues);
    };

    const onSubDivisionChange = (e) => {
        const selectedValues = [];
        const selectedOptions = e.currentTarget.selectedOptions;
        const ids = [];

        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option) => {
                const subdivision = { id: '0', name: null, division: null };
                subdivision.id = option.value;
                subdivision.name = option.textContent;
                subdivision.division = option.value.split('@-@')[1];
                selectedValues.push(subdivision);
                ids.push(option.value);
            });
        }
        focusedItems['subDivision'] && applyFilter('subDivision', ids);
    };

    const onDepartmentChange = (e) => {
        const selectedValues = [];
        const selectedOptions = e.currentTarget.selectedOptions;
        const ids = [];
        if (selectedOptions.length) {
            Array.from(selectedOptions).forEach((option) => {
                const department = { id: '0', name: null };
                department.id = option.value;
                department.name = option.textContent;

                selectedValues.push(department);
                ids.push(option.value);
            });
        }
        focusedItems['departments'] && applyFilter('departments', ids);
    };

    const getDatatransferByQueryParams = (filterQueryParams) => {
        const queryParams = { ...filterQueryParams };
        if (queryParams.division?.length === 0) {
            queryParams.division = [];
            queryParams.subDivision = [];
        }

        if (
            queryParams.division?.length === divisions?.length &&
            queryParams.subDivision?.length === subDivisions?.length
        ) {
            queryParams.division = [];
            queryParams.subDivision = [];
        }
        if (queryParams.departments?.length === departments?.length) {
            queryParams.departments = [];
        }
        getFilterQueryParams(queryParams);
    };

    const resetDataFilters = () => {
        const newQueryParams = JSON.parse(JSON.stringify(queryParams));
        newQueryParams.division = divisions?.map((division) => {
            return division.id;
        });
        newQueryParams.dataStewards = [];
        newQueryParams.informationOwners = [];
        dataTransferApi.getSubDivisionsData(divisions).then((subDivisionsList) => {
            const subDivisionsToReset = [].concat(...subDivisionsList);
            setSubDivisions(subDivisionsToReset);
            newQueryParams.subDivision = subDivisionsToReset?.map((subDivision) => {
                return subDivision.id;
            });
            newQueryParams.departments = departments?.map((department) => {
                return department.name;
            });

            setTimeout(() => {
                sessionStorage.removeItem(SESSION_STORAGE_KEYS.DATATRANSFER_FILTER_VALUES)
            }, 100);
            ProgressIndicator.show();
            onResetFilterCompleted(newQueryParams);


        });

    };

    const onResetFilterCompleted = (queryParams, showMessage) => {
        setFilterApplied(false);
        setFocusedItems({});
        setQueryParams(queryParams);
        getDatatransferByQueryParams(queryParams);
        setdataTransferFilterApplied(false);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
        setTimeout(() => { sessionStorage.removeItem(SESSION_STORAGE_KEYS.DATATRANSFER_FILTER_VALUES); }, 500);
        if (showMessage) {
            trackEvent(`All Datatransfer`, 'Filter', 'Removed or Resetted filter preferences');
            Notification.show('Filter preference has been removed.');
        }
    };

    const getSubDivisionsOfSelectedDivision = () => {
        let subDivisionsOfSelectedDivision = divisionFilterValues.length ? [] : subDivisions;
        divisionFilterValues.forEach((div) => {
            const subDivisionsFromDivision = divisions.find((masterDiv) => masterDiv.id === div.id)?.subdivisions;
            subDivisionsFromDivision?.forEach((subdivision) => {
                if (subdivision.id.indexOf('@-@') === -1) { // Making sure if divisiona and subdivision mappping already performed donot do again
                    subdivision.id = subdivision.id + '@-@' + div.id;
                    subdivision.division = div.id;
                }
                subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subdivision);
            });
            // subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subDivisionsFromDivision);
            // subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subDivisions.filter((subDiv: ISubDivisionSolution) => subDiv.division === div.id) as ISubDivisionSolution[]);
        });

        if (subDivisionsOfSelectedDivision.length && !subDivisionsOfSelectedDivision.some((item) => item.name === 'None')) {
            const lastDivisionId = divisionFilterValues[divisionFilterValues.length - 1].id;
            subDivisionsOfSelectedDivision.unshift({ id: `EMPTY@-@${lastDivisionId}`, name: 'None', division: lastDivisionId });
        } else {
            subDivisionsOfSelectedDivision.sort((item) => item.name === 'None' ? -1 : 0);
        }

        return subDivisionsOfSelectedDivision;
    };


    if (openFilters) {
        if (document.getElementById('filterContainer')) {
            const height = document?.getElementById('filterContainerDiv')?.clientHeight; // taking height of child div
            document.getElementById('filterContainer').setAttribute('style', 'height:' + height + 'px'); //assigning height to parent div
        }
    } else {
        if (document.getElementById('filterContainer')) {
            document.getElementById('filterContainer').setAttribute('style', 'height:' + 0 + 'px');
        }
    }

    const subDivisionsOfSelectedDivision = getSubDivisionsOfSelectedDivision();


    return (
        <FilterWrapper openFilters={openFilters}>
            <div>
                <div id="divisionContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'division')}>
                    <label id="divisionLabel" className="input-label" htmlFor="divisionSelect">
                        Division
                    </label>
                    <div className=" custom-select">
                        <select id="divisionSelect" multiple={true} onChange={onDivisionChange} value={queryParams?.division}>
                            {divisions?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                    {obj.name}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
            <div>
                <div
                    id="subDivisionContainer"
                    className={`input-field-group ${divisionFilterValues?.length ? '' : 'disabled'}`}
                    onFocus={(e) => onHandleFocus(e, 'subDivision')}
                >
                    <label id="subDivisionLabel" className="input-label" htmlFor="subDivisionSelect">
                        Sub Division
                    </label>
                    <div className={`custom-select ${divisionFilterValues?.length ? '' : 'disabled'}`}>
                        <select
                            id="subDivisionSelect"
                            multiple={true}
                            onChange={onSubDivisionChange}
                            value={queryParams?.subDivision}
                        >
                            {subDivisionsOfSelectedDivision?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                    {obj.name}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
            <div>
                <div
                    id="departmentContainer"
                    className={`input-field-group ${departments?.length ? '' : 'disabled'}`}
                    onFocus={(e) => onHandleFocus(e, 'departments')}
                >
                    <label id="departmentLabel" className="input-label" htmlFor="departmentSelect">
                        Department
                    </label>
                    <div className={`custom-select ${departments?.length ? '' : 'disabled'}`}>
                        <select
                            id="departmentSelect"
                            multiple={true}
                            onChange={onDepartmentChange}
                            value={queryParams?.departments}
                        >
                            {departments?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                    {obj.name}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
            <div>
                <div
                    id="dataStewardContainer"
                    className={`input-field-group ${dataStewards?.length ? '' : 'disabled'}`}
                    onFocus={(e) => onHandleFocus(e, 'dataStewards')}
                >
                    <label id="dataStewardLabel" className="input-label" htmlFor="dataStewardSelect">
                        Data Steward
                    </label>
                    <div className={`custom-select ${dataStewards?.length ? '' : 'disabled'}`}>
                        <select
                            id="dataStewardSelect"
                            onChange={onDataStewardChange}
                            value={queryParams?.dataStewards?.join('')}
                        >
                            <option id="defaultdataSteward" value={''}>
                                Choose
                            </option>
                            {dataStewards?.map((obj, index) => (
                                <option key={index} value={obj?.teamMemeber?.shortId}>
                                    {`${obj?.teamMemeber?.firstName} ${obj?.teamMemeber?.lastName}`}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
            <div>
                <div
                    id="informationOwnerContainer"
                    className={`input-field-group ${informationOwners?.length ? '' : 'disabled'}`}
                    onFocus={(e) => onHandleFocus(e, 'informationOwners')}
                >
                    <label id="informationOwnerLabel" className="input-label" htmlFor="informationOwnerSelect">
                        Information Owner
                    </label>
                    <div className={`custom-select ${informationOwners?.length ? '' : 'disabled'}`}>
                        <select
                            id="informationOwnerSelect"
                            onChange={onInformationOwnerChange}
                            value={queryParams?.informationOwners?.join('')}
                        >
                            <option id="defaultinformationOwner" value={''}>
                                Choose
                            </option>
                            {informationOwners?.map((obj, index) => (
                                <option key={index} value={obj?.teamMemeber?.shortId}>
                                    {`${obj?.teamMemeber?.firstName} ${obj?.teamMemeber?.lastName}`}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
            <div className={classNames(Styles.actionWrapper, dataFilterApplied ? '' : 'hidden')}>
                <div className="icon-tile">
                    <button className="btn btn-icon-circle" tooltip-data="Reset Filters" onClick={resetDataFilters}>
                        <i className="icon mbc-icon refresh" />
                    </button>
                </div>
            </div>
        </FilterWrapper>
    )

};
export default DataTransferFilter;