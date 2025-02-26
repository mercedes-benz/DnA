import React, { useState, useEffect, useRef } from 'react';
import cn from 'classnames/bind';
import {
  IDivision,
  IDivisionFilterPreference,
  IFilterParams,
  ILocation,
  IPhase,
  IProjectStatus,
  IProjectType,
  ISubDivisionSolution,
  ITag,
  IUserPreferenceRequest,
  IFilterPreferences,
  IUserPreference,
  IDataVolume,
  IUserInfo,
  IRole,
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
import { SESSION_STORAGE_KEYS, USER_ROLE} from 'globals/constants';
import Tags from 'components/formElements/tags/Tags';
import { getDivisionsQueryValue, trackEvent, isSolutionFilterApplied } from '../../../services/utils';
import { useLocation } from 'react-router-dom';
import { getPath } from '../../../router/RouterUtils';
import Modal from 'components/formElements/modal/Modal';

import Styles from './Filter.scss';
import FilterWrapper from './FilterWrapper';
const classNames = cn.bind(Styles);

type SolutionsFilterType = {
  isGenAI?: boolean;
  user?: IUserInfo;
  userId: string;
  getSolutions?: Function;
  getFilterQueryParams?: Function;
  getValuesFromFilter?: Function;
  solutionsDataLoaded: boolean;
  setSolutionsDataLoaded: Function;
  setSolutionsFilterApplied: Function;
  showSolutionsFilter?: boolean;
  openFilters?: boolean;
  getAllTags?: Function;
  setSelectedTags?: string[]; // this prop is used to set selected tags from tags component
};

/**
 * Solutions Filter Panel
 * @param {string} logged in user id
 * @param {Function} getSolutions callback function to fetch solutions result
 * @param {Function} getValuesFromFilter callback function to get access to all the filter values, that can be used in the main page
 * @param {boolean} solutionsDataLoaded solutions data loaded
 * @param {Function} setSolutionsDataLoaded setter for solutions data loaded
 * @param {Function} setSolutionsFilterApplied setter for solutions filter applied
 * @param {boolean} showSolutionsFilter filter should be visible or not
 * @returns
 */

const SolutionsFilter = ({
  isGenAI,
  user,
  userId,
  getSolutions,
  getFilterQueryParams,
  getValuesFromFilter,
  solutionsDataLoaded,
  setSolutionsDataLoaded,
  setSolutionsFilterApplied,
  showSolutionsFilter,
  openFilters,
  getAllTags,
  setSelectedTags,
}: SolutionsFilterType) => {
  const { pathname } = useLocation();
  //   const [openFilterPanel,
  //     // setFilterPanel
  // ] = useState(false);

  // dropdown values
  const [phases, setPhases] = useState<IPhase[]>([]);
  const [locations, setLocations] = useState<ILocation[]>([]);
  const [projectStatuses, setProjectStatuses] = useState<IProjectStatus[]>([]);
  const [divisions, setDivisions] = useState<IDivisionFilterPreference[]>([]);
  const [subDivisions, setSubDivisions] = useState<ISubDivisionSolution[]>([]);
  const [tagValues, setTagValues] = useState<ITag[]>([]);
  // const [myDivisions, setMyDivisions] = useState(false);
  const [emailBody, setEmailBody] = useState('');
  const [emailBodyError, setEmailBodyError] = useState('');
  const [showMailingModal, setShowMailingModal] = useState(false);
  const currentYear = new Date().getFullYear();
  const defaultStartYear = currentYear - 2 + '';
  const defaultEndYear =currentYear + 1 + '';
  const [queryParams, setQueryParams] = useState<IFilterParams>({
    phase: [],
    location: [],
    division: [],
    subDivision: [],
    status: [],
    useCaseType: [],
    tag: [],
    dataValueRange: {
      startYear: defaultStartYear ,
      endYear: defaultEndYear
    }
  });
  const [projectTypes] = useState<IProjectType[]>([
    { id: '1', name: 'My Bookmarks', routePath: 'bookmarks' },
    { id: '2', name: 'My Solutions', routePath: 'mysolutions' },
  ]);

  const [dataVolumes, setDataVolumes] = useState<IDataVolume[]>([]);

  // selected filter values
  const [phaseFilterValues, setPhaseFilterValues] = useState([]);
  const [divisionFilterValues, setDivisionFilterValues] = useState([]);
  const [subDivisionFilterValues, setSubDivisionFilterValues] = useState([]);
  const [locationFilterValues, setLocationFilterValues] = useState([]);
  const [statusFilterValue, setStatusFilterValue] = useState<IProjectStatus>(null);
  const [typeFilterValue, setTypeFilterValue] = useState<IProjectType>(null);
  const [tagFilterValues, setTagFilterValues] = useState<ITag[]>([]);
  const [, setTags] = useState<string[]>([]);

  const [userPreferenceDataId, setUserPreferenceDataId] = useState<string>(null);

  const [userPreference, setRunUserPreference] = useState<boolean>(false);

  const portfolioFilterValues: any = useRef();

  const [focusedItems, setFocusedItems] = useState({});

  const [dataFilterApplied, setFilterApplied] = useState(false);

  const isPortfolioPage = pathname === '/portfolio';
  const isAllSolutionsPage = pathname === '/allsolutions';

  const [genAIPage, setGenAIPage] = useState(false);

  const [years ,setYears] = useState([]);
  const [isDvRangeValid, setisDvRangeValid]= useState(true);

  const isDivisionAdmin = user.roles.find((role: IRole) => role.id === USER_ROLE.DIVISIONADMIN) !== undefined;

  useEffect(() => {
    onsetTags(setSelectedTags);
  }, [setSelectedTags]);

  useEffect(() => {
    SelectBox.refresh('subDivisionSelect'); // Refresh the sub division select box
  }, [divisionFilterValues]);

  useEffect(() => {
    const path = getPath().split('?')[0].split('/')[3] || getPath().split('/')[1];
    setGenAIPage(path === 'GenAI' ? true : false);
    ProgressIndicator.show();
    ApiClient.getFiltersMasterData()
      .then((response) => {
        if (response) {
          portfolioFilterValues.current = JSON.parse(
            sessionStorage.getItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES),
          ) as IFilterParams;
          const locations = response[0];
          const divisions = response[1];
          const divisionsToPass =
            portfolioFilterValues.current && portfolioFilterValues.current.division.length > 0
              ? divisions.filter((element: any) => portfolioFilterValues.current.division.includes(element.id))
              : divisions;
          const minMaxYears = response[6];
          const data = [];
          for (let year = minMaxYears.minYear; year <= minMaxYears.maxYear; year++) {
            data.push(year);
          }
          setYears(data);
          ApiClient.getSubDivisionsData(divisionsToPass).then((subDivisionsList) => {
            const projectStatuses = response[2];
            const phases: IPhase[] = response[3];
            const tagValues: ITag[] = response[5];
            getAllTags(response[5]);
            const subDivisions: ISubDivisionSolution[] = [].concat(...subDivisionsList);
            phases.forEach((phase) => {
              switch (phase.id) {
                case '1':
                  phase.name = 'Kick-off';
                  break;
                case '2':
                  phase.name = 'Ideation';
                  break;
                case '3':
                  phase.name = 'Concept Development/Proof of concept';
                  break;
                case '4':
                  phase.name = 'Pilot';
                  break;
                case '5':
                  phase.name = 'Professionalization';
                  break;
                case '6':
                  phase.name = 'Operations/Rollout';
                  break;
                default:
                  break;
              }
            });

            const dataVolumes: IDataVolume[] = response[4];

            let newQueryParams = queryParams;
            if (portfolioFilterValues.current) {
              newQueryParams = portfolioFilterValues.current;
              if (newQueryParams.status.includes('0')) {
                newQueryParams.status = [];
              }
              if (newQueryParams.useCaseType.includes('0')) {
                newQueryParams.useCaseType = [];
              }
              // Solved the issue on tag if user has already values in session
              if (!newQueryParams.tag) {
                newQueryParams.tag = [];
              }
              const selectedValues: ITag[] = [];
              newQueryParams.tag.forEach((a: any) => {
                const tag: ITag = { id: null, name: null };
                tag.id = a;
                tag.name = a;
                selectedValues.push(tag);
              });
              setTagFilterValues(selectedValues);
              setTags(newQueryParams.tag);
              setFilterApplied(true);
            } else {
              newQueryParams.phase = phases.map((phase: IPhase) => {
                return phase.id;
              });
              newQueryParams.division = divisions.map((division: IDivisionFilterPreference) => {
                return division.id;
              });
              newQueryParams.subDivision = subDivisions.map((subDivision: ISubDivisionSolution) => {
                return subDivision.id;
              });
              newQueryParams.location = locations.map((location: ILocation) => {
                return location.id;
              });
              newQueryParams.status = [];
              newQueryParams.useCaseType = [];
              newQueryParams.tag = [];
              setFilterApplied(false);
            }
            setLocations(locations);
            setDivisions(divisions);
            setSubDivisions(subDivisions);
            setProjectStatuses(projectStatuses);
            setPhases(phases);
            setDataVolumes(dataVolumes);
            setTagValues(tagValues);
            setQueryParams(newQueryParams);
            setDataVolumes(dataVolumes);
            // typeof getValuesFromFilter === 'function' &&
            //   getValuesFromFilter({
            //     phaseFilterValues,
            //     divisionFilterValues,
            //     locationFilterValues,
            //     subDivisionFilterValues,
            //     tagFilterValues,
            //     statusFilterValue,
            //     typeFilterValue,
            //     dataVolumes,
            //     phases,
            //     locations,
            //   });
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
      ApiClient.getUserPreference(userId)
        .then((res) => {
          if (res.length) {
            const userPreference = res[0];
            const savedSubDivisionsList: ISubDivisionSolution[] = [];
            const filterPreferences = userPreference.filterPreferences;
            if (!portfolioFilterValues.current) {              
              queryParams.phase = filterPreferences.phases.map((phase: IPhase) => {
                return phase.id;
              });
              queryParams.division = filterPreferences.divisions.map((division: IDivisionFilterPreference) => {
                division.subdivisions.forEach((subdivision: ISubDivisionSolution) => {
                  subdivision.id = subdivision.id + '@-@' + division.id;
                  subdivision.division = division.id;
                  savedSubDivisionsList.push(subdivision);
                });
                return division.id;
              });
              queryParams.subDivision = savedSubDivisionsList.map((subdivision: ISubDivisionSolution) => {
                return subdivision.id;
              });
              queryParams.location = filterPreferences.locations.map((location: ILocation) => {
                return location.id;
              });
              queryParams.status =
                filterPreferences.solutionStatus && filterPreferences.solutionStatus.id !== '0'
                  ? [filterPreferences.solutionStatus.id]
                  : [];
              queryParams.useCaseType = [filterPreferences.useCaseType];
              queryParams.tag = filterPreferences.tags.map((tag: ITag) => {
                return tag.name;
              });

              if(filterPreferences.dataValueRange) {
                const prefStartYear = filterPreferences.dataValueRange.split(',')[0];
                const prefEndYear = filterPreferences.dataValueRange.split(',')[1];
                queryParams.dataValueRange.startYear = years.includes(prefStartYear) ? prefStartYear : defaultStartYear;
                queryParams.dataValueRange.endYear = years.includes(prefEndYear) ? prefEndYear : defaultEndYear;
              } else {
                queryParams.dataValueRange.startYear = defaultStartYear;
                queryParams.dataValueRange.endYear = defaultEndYear;
              }
              // populate subDivision dropdown values
              ApiClient.getSubDivisionsData(filterPreferences.divisions).then((subDivisionsList) => {
                setSubDivisions(subDivisionsList);
                SelectBox.defaultSetup();
              });
              setFilterApplied(true);
            }
            userPreferenceDataId = userPreference.id;
          } else {
            queryParams.dataValueRange.startYear = defaultStartYear;
            queryParams.dataValueRange.endYear = defaultEndYear;
          }
          // sessionStorage.setItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES, JSON.stringify(queryParams));
        
          if (!years.includes(queryParams.dataValueRange.startYear)) {
            queryParams.dataValueRange.startYear = years.includes(Number(defaultStartYear))
              ? defaultStartYear
              : years[0];
          }
          if (!years.includes(queryParams.dataValueRange.endYear)) {
            queryParams.dataValueRange.endYear = years.includes(Number(defaultEndYear))
              ? defaultEndYear
              : years[years.length - 1];
          }

          setQueryParams(queryParams);
          setUserPreferenceDataId(userPreferenceDataId);
          Button.defaultSetup();
          SelectBox.defaultSetup();
          Tooltip.defaultSetup();
          getSolutionsByQueryParams(queryParams);
        })
        .catch((error: Error) => {
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
  }, [userPreference]);


  useEffect(() => {
    typeof getValuesFromFilter === 'function' &&
      getValuesFromFilter({
        phaseFilterValues,
        divisionFilterValues,
        locationFilterValues,
        subDivisionFilterValues,
        tagFilterValues,
        statusFilterValue,
        typeFilterValue,
        dataVolumes,
        phases,
        locations,
        divisions,
        projectStatuses,
        projectTypes,
      });
  }, [
    phaseFilterValues,
    divisionFilterValues,
    locationFilterValues,
    subDivisionFilterValues,
    tagFilterValues,
    statusFilterValue,
    typeFilterValue,
    dataVolumes,
    phases,
    locations,
    divisions,
    projectStatuses,
    projectTypes,
  ]);

  // useEffect(() => {
  //   if (solutionsDataLoaded && focusedItems['division']) {
  //     ProgressIndicator.show();
  //     const values = getIdValuesInArray(divisionFilterValues);
  //     ApiClient.getSubDivisionsDataWithDivision(values).then((list) => {
  //       queryParams.subDivision = divisionFilterValues.length
  //         ? [].concat(...list).map((l: ISubDivisionSolution) => {
  //             return l.id;
  //           })
  //         : [];
  //       setQueryParams(queryParams);
  //       setSubDivisions(divisionFilterValues.length ? [].concat(list) : []);
  //       typeof getValuesFromFilter === 'function' && getValuesFromFilter({ subDivisionFilterValues: [].concat(list) });
  //       applyFilter('division', values);
  //     });
  //   }
  // }, [divisionFilterValues.length]);

  const setPortfolioFilterValuesInSession = (queryParams: IFilterParams) => {
    sessionStorage.setItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES, JSON.stringify(queryParams));
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

  const applyFilter = (filterName: string, values: any) => {
    if (solutionsDataLoaded) {
      ProgressIndicator.show();
      setFilterApplied(true);
      queryParams[filterName] = values;
      if (filterName === 'division') {
        let subDivisionValues: string[] = [];
        let hasNoneValue = false;
        values.forEach((divisionId: string) => {
          const subDivisions = divisions.find((division: IDivision) => division.id === divisionId)?.subdivisions;
          if (!hasNoneValue) {
            hasNoneValue = subDivisions.some((subDiv: ISubDivisionSolution) => subDiv.name === 'None');
          }
          if (subDivisions.length) {
            const subDivVals = subDivisions.map((subDivision: ISubDivisionSolution) =>
              subDivision.id.indexOf('@-@') !== -1 ? subDivision.id : (`${subDivision.id}@-@${divisionId}` as string),
            ) as string[];
            subDivisionValues = subDivisionValues.concat(subDivVals);
          }
        });
        if (!hasNoneValue && values.length) {
          subDivisionValues.unshift(`EMPTY@-@${values[values.length - 1]}`);
        }
        queryParams['subDivision'] = subDivisionValues;
      }
      setPortfolioFilterValuesInSession(queryParams);
      getSolutionsByQueryParams(queryParams);
      // if (filterName === 'division') SelectBox.defaultSetup(true);
      trackEvent(
        `${isAllSolutionsPage ? 'All Solutions' : 'Portfolio'}`,
        'Filter Chart Data',
        'From Filter Panel - ' + filterName,
      );
    }
  };

  const onHandleFocus = (e: any, targetElement: string) => {
    //to ensure user action is done on the form fields
    setFocusedItems({ [targetElement]: true });
  };

  const onPhaseChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IPhase[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const phase: IPhase = { id: '0', name: null };
        phase.id = option.value;
        phase.name = option.label;

        selectedValues.push(phase);
        ids.push(option.value);
      });
    }
    focusedItems['phase'] && applyFilter('phase', ids);
    setPhaseFilterValues(selectedValues);
    typeof getValuesFromFilter === 'function' && getValuesFromFilter({ phaseFilterValues: selectedValues });
  };

  const onDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDivision[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const division: IDivision = { id: '0', name: null };
        division.id = option.value;
        division.name = option.label;
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
        subdivision.name = option.label;
        subdivision.division = option.value.split('@-@')[1];
        selectedValues.push(subdivision);
        ids.push(option.value);
      });
      // const divisionId = selectedValues[0].division;
      // const noneSubDivValue = `EMPTY@-@${divisionId}`;
      // if (!ids.includes(noneSubDivValue)) {
      //   const subdivision: ISubDivisionSolution = { id: '0', name: null, division: divisionId };
      //   subdivision.id = noneSubDivValue;
      //   subdivision.name = 'None';
      //   selectedValues.unshift(subdivision);
      //   ids.unshift(noneSubDivValue);
      // }
    }
    focusedItems['subDivision'] && applyFilter('subDivision', ids);
    setSubDivisionFilterValues(selectedValues);
  };

  const onLocationChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: ILocation[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const location: ILocation = { id: null, name: null };
        location.id = option.value;
        location.name = option.label;
        selectedValues.push(location);
        ids.push(option.value);
      });
    }

    focusedItems['location'] && applyFilter('location', ids);
    setLocationFilterValues(selectedValues);
  };

  const onStatusChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const status: IProjectStatus = { id: null, name: null };
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        status.id = option.value;
        status.name = option.label;
        ids.push(option.value === '0' ? '' : option.value);
      });
    }

    focusedItems['status'] && applyFilter('status', ids);
    setStatusFilterValue(status);
  };

  const onTypeChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const type: IProjectType = { id: null, name: null, routePath: null };
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        type.id = option.value;
        type.name = option.label;
        ids.push(option.value);
      });
    }

    focusedItems['useCaseType'] && applyFilter('useCaseType', ids);
    setTypeFilterValue(type);
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
    setTags(arr);
    setTagFilterValues(selectedValues);
  };

  const saveFilterPreference = () => {
    let divisionsWithSubDivisions;
    if (queryParams.subDivision.length > 0) {
      const tempArr: any[] = [];
      divisionFilterValues.forEach((item) => {
        const tempSubdiv = queryParams.subDivision.map((value: string) => {
          const tempSubDivId = value.split('@-@')[1];
          const subDivObject = subDivisionsOfSelectedDivision.find(
            (subDiv: ISubDivisionSolution) => subDiv.id === value,
          );
          if (item.id === tempSubDivId && subDivObject) {
            const tempSubDivObj: IDivision = { id: '', name: '' };
            tempSubDivObj.id = value.split('@-@')[0];
            tempSubDivObj.name = subDivObject.name;
            return tempSubDivObj;
          }
        });
        const tempObj: any = { id: '', name: '', subdivisions: [] };
        tempObj.id = item.id;
        tempObj.name = item.name;
        tempObj.subdivisions = tempSubdiv.filter((div) => div);
        if (
          !tempObj.subdivisions.some((tempSubDiv: any) => tempSubDiv.name === 'None') &&
          queryParams.subDivision.some((subDivId: string) => subDivId === `EMPTY@-@${item.id}`)
        ) {
          tempObj.subdivisions.unshift({
            id: 'EMPTY',
            name: 'None',
          });
        }
        tempArr.push(tempObj);
      });
      divisionsWithSubDivisions = tempArr;
    }
    const dVRange = queryParams.dataValueRange.startYear +','+ queryParams.dataValueRange.endYear;
    const filterPreferences: IFilterPreferences = {
      divisions: divisionsWithSubDivisions,
      subDivisions: subDivisionFilterValues,
      locations: locationFilterValues,
      phases: phaseFilterValues,
      solutionStatus: statusFilterValue,
      useCaseType: typeFilterValue.id === '0' ? null : typeFilterValue.id,
      tags: tagFilterValues,
      dataValueRange : dVRange,
    };

    const userPreference: IUserPreference = {
      filterPreferences,
      userId: userId,
      widgetPreferences: [],
    };

    // let userPreferenceDataId = userPreferenceDataId;
    if (userPreferenceDataId) {
      userPreference.id = userPreferenceDataId;
    }

    const userPreferenceRequest: IUserPreferenceRequest = {
      data: userPreference,
    };

    ProgressIndicator.show();
    ApiClient.saveUserPreference(userPreferenceRequest)
      .then((response) => {
        setUserPreferenceDataId(response.id);
        trackEvent(`${isPortfolioPage ? 'Portfolio' : 'All Solutions'}`, 'Filter', 'Saved filter preferences');
        ProgressIndicator.hide();
        Notification.show('Filter preference has been saved.');
      })
      .catch((error: Error) => {
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const getSolutionsByQueryParams = (filterQueryParams: IFilterParams) => {
    const queryParams: IFilterParams = { ...filterQueryParams };
    let locationIds = queryParams.location.join(',');
    let phaseIds = queryParams.phase.join(',');
    let divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);
    let status = queryParams.status.join(',');
    let useCaseType = queryParams.useCaseType.join(',');
    const tags = queryParams.tag.join(',');
    const dataValueRange = `${queryParams.dataValueRange?.startYear},${queryParams.dataValueRange?.endYear}`;

    if (queryParams.division.length === 0) {
      queryParams.division = [];
      queryParams.subDivision = [];
    }

    if (queryParams.division.length === divisions.length && queryParams.subDivision.length === subDivisions.length) {
      divisionIds = '';
      queryParams.division = [];
      queryParams.subDivision = [];
    }

    if (queryParams.location.length === locations.length) {
      locationIds = '';
      queryParams.location = [];
    }

    if (queryParams.phase.length === phases.length) {
      phaseIds = '';
      queryParams.phase = [];
    }

    if (queryParams.status.length === projectStatuses.length) {
      status = '';
      queryParams.status = [];
    }

    if (queryParams.useCaseType.length === projectTypes.length) {
      useCaseType = '';
      queryParams.useCaseType = [];
    }

    typeof getFilterQueryParams === 'function' && getFilterQueryParams(queryParams);

    typeof getSolutions === 'function' && getSolutions(locationIds, phaseIds, divisionIds, status, useCaseType, tags, dataValueRange);

    setSolutionsFilterApplied(
      isSolutionFilterApplied(
        queryParams,
        divisions.length,
        subDivisions.length,
        phases.length,
        locations.length,
        tagValues.length,
      ),
    );
  };

  const resetDataFilters = () => {
    setTagFilterValues([]);
    setDivisionFilterValues([]);
    setLocationFilterValues([]);
    setPhaseFilterValues([]);
    setStatusFilterValue(null);
    setSubDivisionFilterValues([]);
    setTypeFilterValue(null);
    const newQueryParams = queryParams;
    newQueryParams.phase = phases.map((phase: IPhase) => {
      return phase.id;
    });
    newQueryParams.division = divisions.map((division: IDivisionFilterPreference) => {
      return division.id;
    });
    ApiClient.getSubDivisionsData(divisions).then((subDivisionsList) => {
      const subDivisionsToReset = [].concat(...subDivisionsList);
      setSubDivisions(subDivisionsToReset);
      queryParams.subDivision = subDivisionsToReset.map((subDivision: ISubDivisionSolution) => {
        return subDivision.id;
      });
      queryParams.location = locations.map((location: ILocation) => {
        return location.id;
      });
      queryParams.status = [];
      queryParams.useCaseType = [];
      queryParams.tag = [];
      queryParams.dataValueRange.startYear = defaultStartYear;
      queryParams.dataValueRange.endYear=defaultEndYear;
      setTags([]);
      setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES), 50);
      ProgressIndicator.show();

      if (userPreferenceDataId) {
        ApiClient.removeUserPreference(userPreferenceDataId)
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

  const onResetFilterCompleted = (queryParams: IFilterParams, showMessage?: boolean) => {
    setSolutionsDataLoaded(false);
    setFilterApplied(false);
    setSolutionsFilterApplied(false);
    setFocusedItems({});
    setQueryParams(queryParams);
    setUserPreferenceDataId(null);
    SelectBox.defaultSetup(false);
    getSolutionsByQueryParams(queryParams);
    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES), 50);
    if (showMessage) {
      trackEvent(
        `${isAllSolutionsPage ? 'All Solutions' : 'Portfolio'}`,
        'Filter',
        'Removed or Resetted filter preferences',
      );
      Notification.show('Filter preference has been removed.');
    }
  };

  const getSubDivisionsOfSelectedDivision = () => {
    let subDivisionsOfSelectedDivision: ISubDivisionSolution[] = divisionFilterValues.length ? [] : subDivisions;
    divisionFilterValues.forEach((div: IDivision) => {
      const subDivisionsFromDivision = divisions.find(
        (masterDiv: IDivisionFilterPreference) => masterDiv.id === div.id,
      )?.subdivisions;
      subDivisionsFromDivision?.forEach((subdivision: ISubDivisionSolution) => {
        if (subdivision.id.indexOf('@-@') === -1) {
          // Making sure if divisiona and subdivision mappping already performed donot do again
          subdivision.id = subdivision.id + '@-@' + div.id;
          subdivision.division = div.id;
        }
        subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subdivision);
      });
      // subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subDivisionsFromDivision);
      // subDivisionsOfSelectedDivision = subDivisionsOfSelectedDivision.concat(subDivisions.filter((subDiv: ISubDivisionSolution) => subDiv.division === div.id) as ISubDivisionSolution[]);
    });

    if (
      subDivisionsOfSelectedDivision.length &&
      !subDivisionsOfSelectedDivision.some((item: ISubDivisionSolution) => item.name === 'None')
    ) {
      const lastDivisionId = divisionFilterValues[divisionFilterValues.length - 1].id;
      subDivisionsOfSelectedDivision.unshift({
        id: `EMPTY@-@${lastDivisionId}`,
        name: 'None',
        division: lastDivisionId,
      } as ISubDivisionSolution);
    } else {
      subDivisionsOfSelectedDivision.sort((item) => (item.name === 'None' ? -1 : 0));
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

  const onDataValueChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const { id, value } = e.currentTarget;
    const data = { ...queryParams.dataValueRange };
    data[id] = value;
    setQueryParams(prevParams => ({
      ...prevParams,
      dataValueRange: data
    }));
    if (parseInt(data.startYear, 10) < parseInt(data.endYear, 10)) {
      focusedItems['dataValueRange'] && applyFilter('dataValueRange', data);
      setisDvRangeValid(true);
    } else {
      setisDvRangeValid(false);
    }
  };


  const subDivisionsOfSelectedDivision: ISubDivisionSolution[] = getSubDivisionsOfSelectedDivision();

  const requiredError = "*Missing entry";

  const notifyUseCaseOwners = () =>{ 
    genAIPage && queryParams.tag.push('#GenAI');
    const divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);
    const emailText = '<p>'+emailBody.replace(/\n/g,"<br/>")+'</p>'
    const notifyData = {
      data: {
        location: queryParams.location,
        division: divisionIds,
        phase: queryParams.phase,
        status: queryParams.status,
        useCaseType: queryParams.useCaseType.join(','),
        tags: queryParams.tag,
        emailText: emailText,
      },
    };
    ProgressIndicator.show();
    ApiClient.notifyUseCaseOwners(notifyData)
    .then((res: any) => {
      if (res.success === 'SUCCESS'){
        Notification.show("Email sent successfully");
        ProgressIndicator.hide();
      }
      else{
        Notification.show("Error while sending email. Please try again later.");
        ProgressIndicator.hide();
      }
    })
    .catch((err: Error) => {
      ProgressIndicator.hide();
      Notification.show('Error in deleting code space. Please try again later.\n' + err.message, 'alert');
    });
    setShowMailingModal(false); 
    setEmailBody('');
  };

  const onMailBodyChange = (e: React.FormEvent<HTMLTextAreaElement>) =>{
    setEmailBody(e.currentTarget.value);
    setEmailBodyError(e.currentTarget.value.length !== 0 ? '' : requiredError)
  };

  const mailingContent = (
    <div className={Styles.modalContent}>
      <div
        className={classNames('input-field-group include-error area', emailBodyError.length ? 'error' : '')}
      >
        <label id="description" className="input-label" htmlFor="description">
          Enter mail body <i className="icon mbc-icon alert circle" tooltip-data="Mail will be recieved by all the use case owners of the filtered Solutions. Please ensure you have selected the right filters." />
        </label>
        <textarea
          id="description"
          className="input-field-area"
          // type="text"
          // defaultValue={''}
          value={emailBody}
          required={true}
          required-error={requiredError}
          onChange={onMailBodyChange}
          rows={50}
        />
        <span className={classNames('error-message', emailBodyError.length ? '' : 'hide')}>
          {emailBodyError}
        </span>
      </div>
      <div>
        <button className={classNames('btn btn-primary', Styles.saveSettingsBtn )} tooltip-data="Email Use Case Owners" onClick={emailBody.length !== 0 ? notifyUseCaseOwners : () => setEmailBodyError(requiredError)}>
          Send
        </button>
      </div>
    </div>
  );

  return (
    <>
    <FilterWrapper openFilters={openFilters}>
      <div>
        <div id="phaseContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'phase')}>
          <label id="phaseLabel" className="input-label" htmlFor="phaseSelect">
            Phase
          </label>
          <div className=" custom-select">
            <select id="phaseSelect" multiple={true} onChange={onPhaseChange} value={queryParams?.phase}>
              {phases.map((obj: IPhase) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
      <div>
        <div id="divisionContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'division')}>
          <label id="divisionLabel" className="input-label" htmlFor="divisionSelect">
            Division
          </label>
          <div className=" custom-select">
            <select id="divisionSelect" multiple={true} onChange={onDivisionChange} value={queryParams?.division}>
              {divisions.map((obj: IDivisionFilterPreference) => (
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
          className={`input-field-group ${divisionFilterValues.length ? '' : 'disabled'}`}
          onFocus={(e) => onHandleFocus(e, 'subDivision')}
        >
          <label id="subDivisionLabel" className="input-label" htmlFor="subDivisionSelect">
            Sub Division
          </label>
          <div className={`custom-select ${divisionFilterValues.length ? '' : 'disabled'}`}>
            <select
              id="subDivisionSelect"
              multiple={true}
              onChange={onSubDivisionChange}
              value={queryParams?.subDivision}
            >
              {subDivisionsOfSelectedDivision.map((obj: ISubDivisionSolution) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
      <div>
        <div id="locationContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'location')}>
          <label id="locationLabel" className="input-label" htmlFor="locationSelect">
            Location
          </label>
          <div id="location" className=" custom-select">
            <select id="locationSelect" multiple={true} onChange={onLocationChange} value={queryParams?.location}>
              {locations.map((obj: ILocation) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
      <div>
        <div id="statusContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'status')}>
          <label id="statusLabel" className="input-label" htmlFor="statusSelect">
            Status
          </label>
          <div className=" custom-select">
            <select id="statusSelect" onChange={onStatusChange} value={queryParams?.status.join('')}>
              <option id="defaultStatus" value={0}>
                Choose
              </option>
              {projectStatuses.map((obj: IProjectStatus) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
      <div>
        <div id="typeContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'useCaseType')}>
          <label id="typeLabel" className="input-label" htmlFor="typeSelect">
            Type
          </label>
          <div className=" custom-select">
            <select id="typeSelect" onChange={onTypeChange} value={queryParams?.useCaseType.join('')}>
              <option id="defaultType" value={0}>
                Choose
              </option>
              {projectTypes.map((obj: IProjectType) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
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
            max={genAIPage ? 1 : 100}
            chips={genAIPage ? ['#GENAI'] : queryParams?.tag}
            setTags={onsetTags}
            tags={tagValues}
            isMandatory={false}
            showMissingEntryError={false}
          />
        </div>
      </div>
      {isPortfolioPage && (
        <div className={classNames(Styles.dvFilterWrapper)}>
          <div className={classNames(Styles.dvRange)}>
            <div>
              <div id="dataValueContainer"  className={classNames('input-field-group', Styles.dvStartRange)} onFocus={(e) => onHandleFocus(e, 'dataValueRange')}>
                <label id="dataValueLabel" className="input-label" htmlFor="dvRangeSelect">
                  Data Value Range
                </label>
                <div className="custom-select">
                  <select id="startYear" onChange={onDataValueChange} value={queryParams?.dataValueRange?.startYear} >
                    {years.map((obj: string, key: any) => (
                      <option id={obj + key} key={key} value={obj}>
                        {obj}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
            <i className={classNames('icon mbc-icon minus', Styles.speratorIcon)} />
            <div>
              <div id="dataValueContainer" className={classNames('input-field-group ', Styles.dvEndRange)} onFocus={(e) => onHandleFocus(e, 'dataValueRange')}>
                <div className=" custom-select">
                  <select id="endYear" multiple={false} onChange={onDataValueChange} value={queryParams?.dataValueRange?.endYear}>
                    {years.map((obj: string, key: any) => (
                      <option id={obj + key} key={key} value={obj}>
                        {obj}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </div>
          <span className={classNames('error-message', isDvRangeValid ? 'hide' : '')}>
            Enter valid range
          </span>
        </div>
      )}
      <div className={classNames(Styles.actionWrapper, dataFilterApplied ? '' : 'hidden')}>
        {!isGenAI && (
          <button className={classNames('btn btn-primary', Styles.saveSettingsBtn , !isDvRangeValid ? 'disabled' : '')} onClick={saveFilterPreference}>
            Save settings
          </button>
        )}
        <div className="icon-tile">
          <button className="btn btn-icon-circle" tooltip-data="Reset Filters" onClick={resetDataFilters}>
            <i className="icon mbc-icon refresh" />
          </button>
        </div>
      </div>
    </FilterWrapper>
    {isDivisionAdmin && !isPortfolioPage && (
      <FilterWrapper openFilters={openFilters} divisionAdminSection={true}>
        <p>Please note that you are the division admin of the divisions <span>{user?.divisionAdmins?.toString()}</span>. Ensure you have the right divisions set in your filter in order to notify the use case owners of your divisions.</p>
        <div className={classNames(Styles.actionWrapper)}>
        <button className={classNames('btn btn-primary', Styles.saveSettingsBtn )} tooltip-data="Email Use Case Owners" onClick={() => setShowMailingModal(true)}>
          Email Use Case Owners
        </button>
        </div>
      </FilterWrapper>
    )}
    <Modal
      title={'Email Content'}
      hiddenTitle={false}
      showAcceptButton={false}
      showCancelButton={false}
      // acceptButtonTitle="Send"
      // onAccept={sendEmail}
      modalWidth="800px"
      // modalStyle={{ minHeight: '86%' }}
      buttonAlignment="right"
      show={showMailingModal}
      content={mailingContent}
      scrollableContent={true}
      onCancel={() => setShowMailingModal(false)}
    />
     
    </>
  );
};

export default SolutionsFilter;
