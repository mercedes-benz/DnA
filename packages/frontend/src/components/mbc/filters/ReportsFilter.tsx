import React, { useState, useEffect, useRef } from 'react';
import cn from 'classnames/bind';
import {
  IDivision,
  IFilterParams,
  ISubDivisionSolution,
  IART,
  IReportFilterParams,
  IReportUserPreference,
  IReportFilterPreferences,
  IDivisionFilterPreference,
  IDepartment,
  IReportUserPreferenceRequest,
  ITeams,
  ITag,
} from 'globals/types';
// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { ApiClient } from '../../../services/ApiClient';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { trackEvent } from '../../../services/utils';
// import { useLocation } from 'react-router-dom';

import Styles from './Filter.scss';
import { ReportsApiClient } from '../../../services/ReportsApiClient';
import FilterWrapper from './FilterWrapper';
import Tags from 'components/formElements/tags/Tags';
const classNames = cn.bind(Styles);

interface IMember {
  id: string;
  name: string;
}

type ReportsFilterType = {
  userId: string;
  getFilterQueryParams?: Function;
  reportsDataLoaded: boolean;
  setReportsDataLoaded: Function;
  getDropdownValues?: Function;
  getValuesFromFilter?: Function;
  openFilters?: boolean;
  getAllTags?: Function;
  setSelectedTags?: string[]; // this prop is used to set selected tags from tags component
};

/**
 * Reports Filter Panel
 * @param {string} userId logged in user's id
 * @param {Function} getFilterQueryParams callback function to fetch reports result
 * @param {boolean} reportsDataLoaded reports data loaded
 * @param {Function} setReportsDataLoaded setter for reports data loaded
 * @param {Function} getDropdownValues callback function to get filter dropdown values
 * @returns
 */

const ReportsFilter = ({
  userId,
  getFilterQueryParams,
  reportsDataLoaded,
  setReportsDataLoaded,
  getDropdownValues,
  openFilters,
  getAllTags,
  getValuesFromFilter,
  setSelectedTags
}: ReportsFilterType) => {
//   const [openFilterPanel, setFilterPanel] = useState(false);

  // dropdown values
  const [divisions, setDivisions] = useState<IDivision[]>([]);
  const [subDivisions, setSubDivisions] = useState<ISubDivisionSolution[]>([]);
  const [departments, setDepartments] = useState<IDepartment[]>([]);
  const [arts, setArts] = useState<IART[]>([]);
  const [processOwners, setProcessOwners] = useState<ITeams[]>([]);
  const [productOwners, setProductOwners] = useState<ITeams[]>([]);
  const [tagValues, setTagValues] = useState<ITag[]>([]);
  const [queryParams, setQueryParams] = useState<IReportFilterParams>({
    agileReleaseTrains: [],
    division: [],
    subDivision: [],
    departments: [],
    productOwners: [],
    processOwners: [],
    tag: [],
  });

  // selected filter values
  const [artFilterValues, setArtFilterValues] = useState([]);
  const [productOwnerFilterValues, 
    // setProductOwnerFilterValues
  ] = useState([]);
  const [processOwnerFilterValues, setProcessOwnerFilterValues] = useState([]);
  const [divisionFilterValues, setDivisionFilterValues] = useState([]);
  const [subDivisionFilterValues, setSubDivisionFilterValues] = useState([]);
  const [departmentFilterValues, setDepartmentFilterValues] = useState([]);
  const [tagFilterValues, setTagFilterValues] = useState<ITag[]>([]);

  const [userPreferenceDataId, setUserPreferenceDataId] = useState<string>(null);

  const [userPreference, setRunUserPreference] = useState<boolean>(false);

  const portfolioFilterValues: any = useRef();

  const [focusedItems, setFocusedItems] = useState({});

  const [dataFilterApplied, setFilterApplied] = useState(false);

  useEffect(()=>{
    onsetTags(setSelectedTags);
  },[setSelectedTags])

  useEffect(() => {
    ProgressIndicator.show();
    ReportsApiClient.getFilterMasterData()
      .then((response) => {
        if (response) {
          portfolioFilterValues.current = JSON.parse(
            sessionStorage.getItem(SESSION_STORAGE_KEYS.REPORT_FILTER_VALUES),
          ) as IFilterParams;
          const arts: IART[] = response[0].data;
          const processOwners: ITeams[] = response[3].records;
          // const producatOwners: ITeams[] = response[4].records;
          const divisions = response[1];
          const divisionsToPass =
            portfolioFilterValues.current && portfolioFilterValues.current.division?.length > 0
              ? divisions?.filter((element: any) => portfolioFilterValues.current.division.includes(element.id))
              : divisions;
          const allTags = response[4].data; 
          getAllTags(allTags);
          ApiClient.getSubDivisionsData(divisionsToPass).then((subDivisionsList) => {
            const departments: IDepartment[] = response[2].data;
            const subDivisions: ISubDivisionSolution[] = [].concat(...subDivisionsList);
            let newQueryParams = queryParams;
            if (portfolioFilterValues.current) {
              newQueryParams = portfolioFilterValues.current;
              setFilterApplied(true);
            } else {
              newQueryParams.agileReleaseTrains = arts?.map((art: IART) => {
                return art.name;
              });
              newQueryParams.division = divisions?.map((division: IDivision) => {
                return division.id;
              });
              newQueryParams.subDivision = subDivisions?.map((subDivision: ISubDivisionSolution) => {
                return subDivision.id;
              });
              newQueryParams.departments = departments?.map((department: IDepartment) => {
                return department.name;
              });
              newQueryParams.processOwners = processOwners?.map((processOwner: ITeams) => processOwner.shortId);
              newQueryParams.productOwners = productOwners?.map((productOwner: ITeams) => productOwner.shortId);              
              setFilterApplied(false);
            }
            setDivisions(divisions);
            setSubDivisions(subDivisions);
            setDepartments(departments);
            setProcessOwners(processOwners);
            setProductOwners(productOwners);
            setArts(arts);
            setTagValues(allTags);
            setQueryParams(newQueryParams);
            setRunUserPreference(true);
          });
        }
      })
      .catch((error: Error) => {
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  }, []);

  useEffect(() => {
    let userPreferenceDataId: string = null;
    userPreference &&
      ReportsApiClient.getUserPreference(userId)
        .then((res) => {
          if (res.length) {
            const userPreference = res[0];
            const savedSubDivisionsList: ISubDivisionSolution[] = [];
            const filterPreferences = userPreference.filterPreferences;
            // if (!portfolioFilterValues.current) {
              queryParams.agileReleaseTrains = filterPreferences.arts?.map((art: IART) => {
                return art.name;
              });
              queryParams.division = filterPreferences.divisions?.map((division: IDivisionFilterPreference) => {
                division.subdivisions.forEach((subdivision: ISubDivisionSolution) => {
                  subdivision.id = subdivision.id + '@-@' + division.id;
                  subdivision.division = division.id;
                  savedSubDivisionsList.push(subdivision);
                });
                return division.id;
              });
              queryParams.subDivision = savedSubDivisionsList?.map((subdivision: ISubDivisionSolution) => {
                return subdivision.id;
              });
              queryParams.departments = departments
                ?.filter((item: any) => filterPreferences.departments.includes(item.name))
                ?.map((item) => item.name) as any || [];
              queryParams.agileReleaseTrains = filterPreferences.arts?.map((art: IART) => art.name);
              queryParams.processOwners = processOwners
                ?.filter((item: any) => filterPreferences.processOwners.includes(item.shortId))
                ?.map((item) => item.shortId) as any || [];
              // queryParams.productOwners = filterPreferences.productOwners as any;
              // populate subDivision dropdown values
              if (!portfolioFilterValues.current) {
                ApiClient.getSubDivisionsData(filterPreferences.divisions).then((subDivisionsList) => {
                  setSubDivisions(subDivisionsList);
                  SelectBox.defaultSetup();
                });
                setFilterApplied(true);
              }
            //}
            userPreferenceDataId = userPreference.id;
          }

          setQueryParams(queryParams);
          setUserPreferenceDataId(userPreferenceDataId);
          Button.defaultSetup();
          SelectBox.defaultSetup();
          Tooltip.defaultSetup();
          getReportsByQueryParams(queryParams);
        })
        .catch((error: Error) => {
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
  }, [userPreference]);

  // useEffect(() => {
  //   if (reportsDataLoaded && focusedItems['division']) {
  //     ProgressIndicator.show();
  //     const values = getIdValuesInArray(divisionFilterValues);
  //     ApiClient.getSubDivisionsDataWithDivision(values).then((list) => {
  //       queryParams.subDivision = divisionFilterValues.length
  //         ? [].concat(...list)?.map((l: ISubDivisionSolution) => {
  //             return l.id;
  //           })
  //         : [];
  //       setQueryParams(queryParams);
  //       setSubDivisions(divisionFilterValues.length ? [].concat(list) : []);
  //       applyFilter('division', values);
  //     });
  //   }
  // }, [divisionFilterValues.length]);

  useEffect(() => {
    typeof getDropdownValues === 'function' &&
      getDropdownValues({
        arts,
        divisions,
        subDivisions,
        departments,
        processOwners,
        productOwners,
      });
  }, [arts, divisions, subDivisions, departments, processOwners, productOwners]);

  useEffect(() => {
    typeof getValuesFromFilter === 'function' &&
      getValuesFromFilter({
        arts,
        divisions,
        subDivisions,
        departments,
        processOwners,
        productOwners,
        tagFilterValues
      });
  }, [arts, divisions, subDivisions, departments, processOwners, productOwners, tagFilterValues]);


  const setPortfolioFilterValuesInSession = (queryParams: IReportFilterParams) => {
    sessionStorage.setItem(SESSION_STORAGE_KEYS.REPORT_FILTER_VALUES, JSON.stringify(queryParams));
  };

  // function getIdValuesInArray(arrayValue: any) {
  //   const ids: string[] = [];
  //   Array.from(arrayValue).forEach((item: any) => {
  //     ids.push(item.id);
  //   });
  //   return ids;
  // }

  const showErrorNotification = (message: string) => {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  const applyFilter = (filterName: string, values: string[]) => {
    if (reportsDataLoaded) {
      ProgressIndicator.show();
      setFilterApplied(true);
      queryParams[filterName] = values;
      setPortfolioFilterValuesInSession(queryParams);
      getReportsByQueryParams(queryParams);
      // if (filterName === 'division') SelectBox.defaultSetup(true);
      trackEvent(`All Reports`, 'Filter Reports List', 'From Filter Panel - ' + filterName);
    }
  };

  const onHandleFocus = (e: any, targetElement: string) => {
    //to ensure user action is done on the form fields
    setFocusedItems({ [targetElement]: true });
  };

  const onArtChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IART[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const art: IART = { id: '0', name: null };
        art.id = option.value;
        art.name = option.textContent;

        selectedValues.push(art);
        ids.push(option.value);
      });
    }
    focusedItems['art'] && applyFilter('agileReleaseTrains', ids);
    setArtFilterValues(selectedValues);
  };

  const onDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDivision[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const division: IDivision = { id: '0', name: null };
        division.id = option.value;
        division.name = option.textContent;
        selectedValues.push(division);
        ids.push(option.value);
      });
    }
    focusedItems['division'] && applyFilter('division', ids);
    setDivisionFilterValues(selectedValues);
  };

  const onSubDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: ISubDivisionSolution[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const subdivision: ISubDivisionSolution = { id: '0', name: null, division: null };
        subdivision.id = option.value;
        subdivision.name = option.textContent;
        subdivision.division = option.value.split('@-@')[1];
        selectedValues.push(subdivision);
        ids.push(option.value);
      });
    }
    focusedItems['subDivision'] && applyFilter('subDivision', ids);
    setSubDivisionFilterValues(selectedValues);
  };

  const onDepartmentChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDepartment[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const department: IDepartment = { id: '0', name: null };
        department.id = option.value;
        department.name = option.textContent;

        selectedValues.push(department);
        ids.push(option.value);
      });
    }
    focusedItems['departments'] && applyFilter('departments', ids);
    setDepartmentFilterValues(selectedValues);
  };

  // const onProductOwnerChange = (e: React.FormEvent<HTMLSelectElement>) => {
  //   const selectedValues: IMember[] = [];
  //   const selectedOptions = e.currentTarget.selectedOptions;
  //   const ids: string[] = [];

  //   if (selectedOptions.length) {
  //     Array.from(selectedOptions).forEach((option) => {
  //       const productOwner: IMember = { id: '0', name: null };
  //       productOwner.id = option.value;
  //       productOwner.name = option.textContent;
  //       selectedValues.push(productOwner);
  //       ids.push(option.value);
  //     });
  //   }

  //   focusedItems['productOwners'] && applyFilter('productOwners', ids);
  //   setProductOwnerFilterValues(selectedValues);
  // };

  const onProcessOwnerChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IMember[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const processOwner: IMember = { id: '0', name: null };
        processOwner.id = option.value;
        processOwner.name = option.textContent;
        selectedValues.push(processOwner);
        ids.push(option.value);
      });
    }

    focusedItems['processOwners'] && applyFilter('processOwners', ids);
    setProcessOwnerFilterValues(selectedValues);
  };

  const getReportsByQueryParams = (filterQueryParams: IReportFilterParams) => {
    const queryParams: IReportFilterParams = { ...filterQueryParams };
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
    if (queryParams.agileReleaseTrains?.length === arts?.length) {
      queryParams.agileReleaseTrains = [];
    }
    if (queryParams.departments?.length === departments?.length) {
      queryParams.departments = [];
    }
    
    if (queryParams.productOwners?.length === productOwners?.length) {
      queryParams.productOwners = [];
    }

    typeof getFilterQueryParams === 'function' && getFilterQueryParams(queryParams);
  };

  const saveFilterPreference = () => {
    let divisionsWithSubDivisions;
    if (subDivisionFilterValues.length > 0) {
      const tempArr: any[] = [];
      divisionFilterValues.forEach((item) => {
        const tempSubdiv = subDivisionFilterValues?.map((value: any) => {
          const tempSubDivId = value.id.split('@-@')[1];
          if (item.id === tempSubDivId) {
            const tempSubDivObj: IDivision = { id: '', name: '' };
            tempSubDivObj.id = value.id.split('@-@')[0];
            tempSubDivObj.name = value.name;
            return tempSubDivObj;
          }
        });
        const tempObj: any = { id: '', name: '', subdivisions: [] };
        tempObj.id = item.id;
        tempObj.name = item.name;
        tempObj.subdivisions = tempSubdiv?.filter((div) => div);
        tempArr.push(tempObj);
      });
      divisionsWithSubDivisions = tempArr;
    }

    const filterPreferences: IReportFilterPreferences = {
      divisions: divisionsWithSubDivisions,
      subDivisions: subDivisionFilterValues,
      arts: arts?.filter((item) => artFilterValues?.map((art: IART) => art.name).indexOf(item.name) > -1),
      departments: departmentFilterValues?.map((department) => department.name),
      productOwners: productOwnerFilterValues?.map((productOwner) => productOwner.id),
      processOwners: processOwnerFilterValues?.map((processOwner) => processOwner.id),
      tags: tagFilterValues?.map((tag) => tag.name),
    };

    const userPreference: IReportUserPreference = {
      filterPreferences,
      userId: userId,
      widgetPreferences: [],
    };

    // let userPreferenceDataId = userPreferenceDataId;
    if (userPreferenceDataId) {
      userPreference.id = userPreferenceDataId;
    }

    const userPreferenceRequest: IReportUserPreferenceRequest = {
      data: userPreference,
    };

    ProgressIndicator.show();
    ReportsApiClient.saveUserPreference(userPreferenceRequest)
      .then((response) => {
        setUserPreferenceDataId(response.id);
        trackEvent(`All Reports`, 'Filter', 'Saved filter preferences');
        ProgressIndicator.hide();
        Notification.show('Filter preference has been saved.');
      })
      .catch((error: Error) => {
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const resetDataFilters = () => {
    setTagFilterValues([]);
    const newQueryParams = JSON.parse(JSON.stringify(queryParams));
    newQueryParams.agileReleaseTrains = arts?.map((phase: IART) => {
      return phase.name;
    });
    newQueryParams.division = divisions?.map((division: IDivision) => {
      return division.id;
    });
    newQueryParams.processOwners = processOwners?.map((processOwner: ITeams) => processOwner.shortId);
    newQueryParams.productOwners = productOwners?.map((productOwner: ITeams) => productOwner.shortId);

    ApiClient.getSubDivisionsData(divisions).then((subDivisionsList) => {
      const subDivisionsToReset = [].concat(...subDivisionsList);
      setSubDivisions(subDivisionsToReset);
      newQueryParams.subDivision = subDivisionsToReset?.map((subDivision: ISubDivisionSolution) => {
        return subDivision.id;
      });

      newQueryParams.departments = departments?.map((department: IDepartment) => {
        return department.name;
      });

      newQueryParams.tag = [];

      setTimeout(() => {
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.REPORT_FILTER_VALUES)
      }, 500);

      ProgressIndicator.show();

        if (userPreferenceDataId) {
          ReportsApiClient.removeUserPreference(userPreferenceDataId)
            .then((res) => {
              onResetFilterCompleted(newQueryParams, true);
            })
            .catch((error: Error) => {
              showErrorNotification(error.message ? error.message : 'Some Error Occured');
            });
        } else {
          onResetFilterCompleted(newQueryParams);
        }
    });
  };

  const onResetFilterCompleted = (queryParams: IReportFilterParams, showMessage?: boolean) => {
    setReportsDataLoaded(false);
    setFilterApplied(false);
    setFocusedItems({});
    setQueryParams(queryParams);
    setUserPreferenceDataId(null);
    SelectBox.defaultSetup(false);
    getReportsByQueryParams(queryParams);
    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.REPORT_FILTER_VALUES), 50);
    if (showMessage) {
      trackEvent(`All Reports`, 'Filter', 'Removed or Resetted filter preferences');
      Notification.show('Filter preference has been removed.');
    }
  };

  const onsetTags = (arr: string[]) => {
    const selectedValues: ITag[] = [];
    arr.forEach((a) => {
      const tag: ITag = { id: null, name: null };
      tag.id = a;
      tag.name = a;
      selectedValues.push(tag);
    });

    applyFilter('tag', arr);
    // setTags(arr);
    setTagFilterValues(selectedValues);
  };

  if(openFilters){
    if(document.getElementById("filterContainer")){
      const height = document?.getElementById('filterContainerDiv')?.clientHeight; // taking height of child div
      document.getElementById("filterContainer").setAttribute("style", "height:"+height+"px"); // assigning height to parent div
    }
  }else{
    if(document.getElementById("filterContainer")){
      document.getElementById("filterContainer").setAttribute("style", "height:"+0+"px");
    } 
  }

  return (
    <FilterWrapper openFilters={openFilters}>
        <div>
            <div id="divisionContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'division')}>
            <label id="divisionLabel" className="input-label" htmlFor="divisionSelect">
                Division
            </label>
            <div className=" custom-select">
                <select id="divisionSelect" multiple={true} onChange={onDivisionChange} value={queryParams?.division}>
                {divisions?.map((obj: IDivision) => (
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
                {subDivisions?.map((obj: ISubDivisionSolution) => (
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
                {departments?.map((obj: IDivision) => (
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
            id="artContainer"
            className={`input-field-group ${arts.length ? '' : 'disabled'}`}
            onFocus={(e) => onHandleFocus(e, 'art')}
            >
            <label id="artLabel" className="input-label" htmlFor="artSelect">
                ART
            </label>
            <div className={`custom-select ${arts.length ? '' : 'disabled'}`}>
                <select id="artSelect" multiple={true} onChange={onArtChange} value={queryParams?.agileReleaseTrains}>
                {arts?.map((obj: IART) => (
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
            id="processOwnerContainer"
            className={`input-field-group ${processOwners?.length ? '' : 'disabled'}`}
            onFocus={(e) => onHandleFocus(e, 'processOwners')}
            >
            <label id="processOwnerLabel" className="input-label" htmlFor="processOwnerSelect">
                Process Owner
            </label>
            <div className={`custom-select ${processOwners?.length ? '' : 'disabled'}`}>
                <select
                id="processOwnerSelect"
                onChange={onProcessOwnerChange}
                value={queryParams?.processOwners?.join('')}
                >
                <option id="defaultprocessOwner" value={''}>
                    Choose
                </option>
                {processOwners?.map((obj: ITeams, index) => (
                    <option key={index} value={obj.shortId}>
                    {`${obj.firstName} ${obj.lastName}`}
                    </option>
                ))}
                </select>
            </div>
            </div>
        </div>
        <div>
            <div>
                <Tags
                title={'Tags'}
                max={100}
                chips={queryParams?.tag}
                setTags={onsetTags}
                tags={tagValues}
                isMandatory={false}
                showMissingEntryError={false}
                />
            </div>
        </div>
        <div className={classNames(Styles.actionWrapper, dataFilterApplied ? '' : 'hidden')}>
            <button className={classNames('btn btn-primary', Styles.saveSettingsBtn)} onClick={saveFilterPreference}>
            Save settings
            </button>
            <div className="icon-tile">
            <button className="btn btn-icon-circle" tooltip-data="Reset Filters" onClick={resetDataFilters}>
                <i className="icon mbc-icon refresh" />
            </button>
            </div>
        </div>
    </FilterWrapper>      
  );
};

export default ReportsFilter;
