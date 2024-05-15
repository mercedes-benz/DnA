import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import {
  IDivision,
  ISubDivisionSolution,
  IDataProductListItem,
  IDataProductFilterParams,
  IUserPreferenceRequest,
  IUserInfo,
  IDivisionFilterPreference,
  // ITag,
} from 'globals/types';
// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './Filter.scss';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { DataProductFilterApiClient } from '../../../services/DataProductFilterApiClient';
import { ApiClient } from '../../../services/ApiClient';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import {getDivisionsQueryValue, trackEvent } from '../../../services/utils';
// import Tags from 'components/formElements/tags/Tags';

import FilterWrapper from './FilterWrapper';

type DataProductsFilterType = {
  user: IUserInfo;
  getValuesFromFilter?: Function;
  getFilteredData?:Function;
  dataProductsDataLoaded: boolean;
  setDataProductsDataLoaded: Function;
  showDataProductsFilter?: boolean;
  setDpFilterApplied : Function;
  openFilters?: boolean;
  setSelectedTags?: string[]; // this prop is used to set selected tags from tags component
};

const DataProductFilter = ({
  user,
  // getValuesFromFilter,
  getFilteredData,
  dataProductsDataLoaded,
  openFilters,
  setDpFilterApplied,
}: DataProductsFilterType) => {

  // dropdown values
  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataStewards, setDataStewards] = useState([]);
  const [informationOwners, setInformationOwners] = useState([]);
  const [arts, setArts] = useState<IDataProductListItem[]>([]);
  const [frontendTools, setFrontendTools] = useState<IDataProductListItem[]>([]);
  const [platforms, setPlatforms] = useState<IDataProductListItem[]>([]);
  const [productOwners, setProductOwners] = useState<IDataProductListItem[]>([]);
  // const [tagValues] = useState<ITag[]>([]);
  // const [tagFilterValues, setTagFilterValues] = useState<ITag[]>(tagsList);
  const [queryParams, setQueryParams] = useState<IDataProductFilterParams>({
    division: [],
    subDivision: [],
    department: [],
    dataSteward: [],
    informationOwner: [],
    art: [],
    platform: [],
    frontendTool: [],
    productOwner: [],
    //carlaFunction: [],
    //tag: [],
  });

  const dataProductFilterValues: any = useRef();
  const [dataFilterApplied, setFilterApplied] = useState(false);
  const [userPreferenceDataId, setUserPreferenceDataId] = useState<string>(null);
  const [focusedItems, setFocusedItems] = useState({});
  const [userPreference, setRunUserPreference] = useState<boolean>(false);

  const [divisionFilterValues, setDivisionFilterValues] = useState([]);
  const [subDivisionValues, setSubDivisionValues] = useState([]);
  const [departmentValues, setDepartmentValues] = useState([]);
  const [dataStewardValues, setDataStewardValues] = useState([]);
  const [informationOwnerValues, setInformationOwnerValues] = useState([]);

  const [artFilterValues, setArtFilterValues] = useState([]);
  const [platformFilterValues, setPlatformFilterValues] = useState([]);
  const [frontendToolFilterValues, setFrontendToolFilterValues] = useState([]);
  const [productOwnerFilterValues, setProductOwnerFilterValues] = useState([]);

  // const onsetTags = (arr: string[]) => {
  //   const selectedValues: ITag[] = [];
  //   arr.forEach((a) => {
  //     const tag: ITag = { id: null, name: null };
  //     tag.id = a;
  //     tag.name = a;
  //     selectedValues.push(tag);
  //   });

  //   applyFilter('tag', arr);
  //   // setTags(arr);
  //   setTagFilterValues(selectedValues);
  // };

  // useEffect(()=>{
  //   onsetTags(setSelectedTags);
  // },[setSelectedTags])

  useEffect(() => {
    SelectBox.refresh('subDivisionSelect'); // Refresh the sub division select box
  }, [divisionFilterValues]);

  useEffect(() => {
    ProgressIndicator.show();
    DataProductFilterApiClient.getFilterMasterData()
      .then((response: any) => {
        if (response) {
          dataProductFilterValues.current = JSON.parse(
            sessionStorage.getItem(SESSION_STORAGE_KEYS.DATAPRODUCT_FILTER_VALUE),
          ) as IDataProductFilterParams;
          const arts = response[0].data;
          const platforms = response[1].data;
          const frontendTools = response[2].data;
          const productOwners = response[3].data;
          const divisions = response[4];
          const departments = response[5].data;
          const dataSteward = response[6].data;
          const informationOwner = response[7].data;

          const divisionsToPass =
          dataProductFilterValues.current && dataProductFilterValues.current.division?.length > 0
              ? divisions?.filter((element : any) => dataProductFilterValues.current.division.includes(element.id))
              : divisions;
          ApiClient.getSubDivisionsData(divisionsToPass).then((subDivisionsList) => {

            const subDivisions = [].concat(...subDivisionsList);

            // const tagValues: ITag[] = response[4].data;
          // getAllTags(response[4].data);
          
          let newQueryParams = queryParams;
          if (dataProductFilterValues.current) {
            newQueryParams = dataProductFilterValues.current;
            // Solved the issue on tag if user has already values in session
            // if (!newQueryParams.tag) {
            //   newQueryParams.tag = [];
            // }
            // const selectedValues: ITag[] = [];
            // newQueryParams.tag.forEach((a: any) => {
            //   const tag: ITag = { id: null, name: null };
            //   tag.id = a;
            //   tag.name = a;
            //   selectedValues.push(tag);
            // });
            // setTagFilterValues(selectedValues);
            // setTags(newQueryParams.tag);
            setFilterApplied(true);
            setDpFilterApplied(true);
          } else {
            newQueryParams.art = arts.map((phase: IDataProductListItem) => {
              return phase.id;
            });
            newQueryParams.platform = platforms.map((platform: IDataProductListItem) => {
              return platform.id;
            });
            newQueryParams.frontendTool = frontendTools.map((frontendTool: IDataProductListItem) => {
              return frontendTool.id;
            });
            // newQueryParams.productOwner = productOwners.map((productOwner: any) => {
            //   return productOwner.shortId;
            // });
            newQueryParams.division = divisions.map((division : IDataProductListItem) =>{
              return division?.id;
            });
            newQueryParams.subDivision = subDivisions?.map((subDivision) => {
              return subDivision?.id;
            });
            newQueryParams.department = departments.map((department : IDataProductListItem) =>{
              return department?.name;
            });
            // newQueryParams.dataSteward = dataSteward.map((dataSteward : any) =>{
            //   return dataSteward?.teamMemeber.shortId;
            // });
            // newQueryParams.informationOwner = informationOwner.map((informationOwnwer : any) =>{
            //   return informationOwnwer?.teamMemeber.shortId;
            // });

            // newQueryParams.productOwner = [];
            // newQueryParams.tag = [];
            setFilterApplied(false);
            setDpFilterApplied(false);
          }
          setArts(arts);
          setPlatforms(platforms);
          setFrontendTools(frontendTools);
          setProductOwners(productOwners);
          setDivisions(divisions);
          setSubDivisions(subDivisions);
          setDepartments(departments);
          setDataStewards(dataSteward);
          setInformationOwners(informationOwner);
          // setTagValues(tagValues);
          setQueryParams(newQueryParams);
          SelectBox.defaultSetup();
          setRunUserPreference(true);


          })
        }
      })
      .catch((error: Error) => {
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  }, []);

  useEffect(() => {
    let userPreferenceDataId: string = null;
    userPreference &&
      DataProductFilterApiClient.getUserPreference(user.id)
        .then((res) => {
          if (res.length) {
            const userPreference = res[0];
            const filterPreferences:any = userPreference.filterPreferences;
            if (!dataProductFilterValues.current) {
              queryParams.division = filterPreferences.divisions.map((division: IDataProductListItem) => {
                return division.id;
              });
              queryParams.subDivision = filterPreferences.subDivisions.map((subDivision: IDataProductListItem) => {
                return subDivision.id;
              });
              queryParams.department = filterPreferences.departments.map((department: IDataProductListItem) => {
                return department.id;
              });
              queryParams.dataSteward = filterPreferences.dataStewards.map((dataSteward: IDataProductListItem) => {
                return dataSteward.id;
              });
              queryParams.informationOwner = filterPreferences.informationOwners.map((informationOwner: IDataProductListItem) => {
                return informationOwner.id;
              });
              queryParams.art = filterPreferences.arts.map((art: IDataProductListItem) => {
                return art.id;
              });
              queryParams.platform = filterPreferences.platforms.map((platform: IDataProductListItem) => {
                return platform.id;
              });
              queryParams.frontendTool = filterPreferences.frontendTools.map((frontendTool: IDataProductListItem) => {
                return frontendTool.id;
              });
              queryParams.productOwner = filterPreferences.productOwners.map((productOwner: any) => {
                return productOwner.shortId;
              });
              setFilterApplied(true);
              setDpFilterApplied(true);
            }
            userPreferenceDataId = userPreference.id;
          }
          // sessionStorage.setItem(SESSION_STORAGE_KEYS.DATAPRODUCT_FILTER_VALUE, JSON.stringify(queryParams));
          setQueryParams(queryParams);
          setUserPreferenceDataId(userPreferenceDataId);
          SelectBox.defaultSetup();
          getDataProductsByQueryParams(queryParams);
        })
        .catch((error: Error) => {
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
  }, [userPreference]);

  const showErrorNotification = (message: string) => {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  const applyFilter = (filterName: string, values: string[]) => {
    if (dataProductsDataLoaded) {
      setFilterApplied(true);
      setDpFilterApplied(true);
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
      setDataProductFilterValuesSession(queryParams)
      getDataProductsByQueryParams(queryParams);
    }
  };

  const onHandleFocus = (e: any, targetElement: string) => {
    //to ensure user action is done on the form fields
    setFocusedItems({ [targetElement]: true });
  };

  const setDataProductFilterValuesSession = (queryParams : any) => {
    sessionStorage.setItem(SESSION_STORAGE_KEYS.DATAPRODUCT_FILTER_VALUE, JSON.stringify(queryParams));
  };

  const onDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const division: IDataProductListItem = { id: '0', name: null };
        division.id = option.value;
        division.name = option.label;
        selectedValues.push(division);
        ids.push(option.value);
      });
    }

    focusedItems['division'] && applyFilter('division', ids);
    setDivisionFilterValues(selectedValues);

  }

  const onSubDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const subDivision: IDataProductListItem = { id: '0', name: null };
        subDivision.id = option.value;
        subDivision.name = option.label;
        selectedValues.push(subDivision);
        ids.push(option.value);
      });
    }
    focusedItems['subDivision'] && applyFilter('subDivision', ids);
    setSubDivisionValues(selectedValues);
    
  }

  const onDepartmentChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
        Array.from(selectedOptions).forEach((option) => {
            const department: IDataProductListItem = { id: '0', name: null };
            department.id = option.value;
            department.name = option.textContent;
            selectedValues.push(department);
            ids.push(option.textContent);
        });
    }
    focusedItems['department'] && applyFilter('department', ids);
  }

  const onDataStewardChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const dataSteward: IDataProductListItem = { id: '0', name: null };
        dataSteward.id = option.value;
        dataSteward.name = option.label;
        selectedValues.push(dataSteward);
        ids.push(option.value);
      });
    }
    focusedItems['dataSteward'] && applyFilter('dataSteward', ids);
    setDataStewardValues(selectedValues);

  }

  const onInformationOwnerChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const informationOwner: IDataProductListItem = { id: '0', name: null };
        informationOwner.id = option.value;
        informationOwner.name = option.label;
        selectedValues.push(informationOwner);
        ids.push(option.value);
      });
    }
    focusedItems['informationOwner'] && applyFilter('informationOwner', ids);
    setInformationOwnerValues(selectedValues);

  }

  const onArtChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const art: IDataProductListItem = { id: '0', name: null };
        art.id = option.value;
        art.name = option.label;
        selectedValues.push(art);
        ids.push(option.value);
      });
    }
    focusedItems['art'] && applyFilter('art', ids);
    setArtFilterValues(selectedValues);
    // typeof getValuesFromFilter === 'function' && getValuesFromFilter({ artFilterValues: selectedValues });
  };

  const onPlatformChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const platform: IDataProductListItem = { id: '0', name: null };
        platform.id = option.value;
        platform.name = option.label;
        selectedValues.push(platform);
        ids.push(option.value);
      });
    }
    focusedItems['platform'] && applyFilter('platform', ids);
    setPlatformFilterValues(selectedValues);
  };

  const onFrontendToolChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const frontendTool: IDataProductListItem = { id: null, name: null };
        frontendTool.id = option.value;
        frontendTool.name = option.label;
        selectedValues.push(frontendTool);
        ids.push(option.value);
      });
    }
    focusedItems['frontendTool'] && applyFilter('frontendTool', ids);
    setFrontendToolFilterValues(selectedValues);
  };

  const onProductOwnerChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedValues: IDataProductListItem[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    const ids: string[] = [];

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const productOwner: IDataProductListItem = { id: '0', name: null };
        productOwner.id = option.value;
        productOwner.name = option.label;
        selectedValues.push(productOwner);
        ids.push(option.value);
      });
    }
    focusedItems['productOwner'] && applyFilter('productOwner', ids);
    setProductOwnerFilterValues(selectedValues);
  };

  const getDataProductsByQueryParams = (filterQueryParams: IDataProductFilterParams) => {
    const queryParams: IDataProductFilterParams = { ...filterQueryParams };
    // const divisions = queryParams.division?.length > 0 ? queryParams.division?.join(','): '';
    // const subDivisions = queryParams.subDivision?.length > 0 ? queryParams.subDivision?.join(','): '';
    let divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);
    let departmentIds = queryParams.department?.length > 0 ? queryParams.department?.join(','): '';
    let artIds = queryParams.art?.length > 0 ?  queryParams.art?.join(',') : '';
    let platformIds = queryParams.platform?.length > 0 ? queryParams.platform?.join(',') : '';
    let frontendToolIds = queryParams.frontendTool?.length > 0 ? queryParams.frontendTool?.join(',') : '';
    const productOwners = queryParams.productOwner?.length > 0 ? queryParams.productOwner.join(',') : '';
    const dataStewards = queryParams.dataSteward?.length > 0 ? queryParams.dataSteward.join(',') : '';
    const informationOwners = queryParams.informationOwner?.length > 0 ? queryParams.informationOwner.join(',') : '';

    if (queryParams.division.length === 0) {
      queryParams.division = [];
      queryParams.subDivision = [];
    }

    if(queryParams.division.length === divisions.length  && queryParams.subDivision.length === subDivisions.length){
      divisionIds = '';
      queryParams.division = [];
      queryParams.subDivision = [];
    }
    if(queryParams.department.length === departments.length){
      departmentIds = '';
      queryParams.department = []

    }
    if(queryParams.art.length === arts.length){
      artIds = '';
      queryParams.art = [];
    }
    if(queryParams.platform.length === platforms.length){
      platformIds = '';
      queryParams.platform = []
    }
    if(queryParams.frontendTool.length === frontendTools.length){
      frontendToolIds = '';
      queryParams.frontendTool = []
    }
    const data = {
      divisionIds,
      departmentIds,
      artIds,
      platformIds,
      frontendToolIds,
      productOwners,
      dataStewards,
      informationOwners
  };

    typeof getFilteredData === 'function' && getFilteredData(data);
  };

  const saveFilterPreference = () => {
    const filterPreferences: IDataProductFilterParams = {
      division: divisionFilterValues,
      subDivision: subDivisionValues,
      department: departmentValues,
      dataSteward: dataStewardValues,
      informationOwner: informationOwnerValues,
      art: artFilterValues,
      platform: platformFilterValues,
      frontendTool: frontendToolFilterValues,
      productOwner: productOwnerFilterValues,
      // carlaFunction: carlaFunctionFilterValue,
      // tags: tagFilterValues,
    };

    const userPreference: any = {
      filterPreferences,
      userId: user.id,
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
    DataProductFilterApiClient.saveUserPreference(userPreferenceRequest)
      .then((response) => {
        setUserPreferenceDataId(response.id);
        trackEvent('Data Products', 'Filter', 'Saved filter preferences');
        ProgressIndicator.hide();
        Notification.show('Filter preference has been saved.');
      })
      .catch((error: Error) => {
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const resetDataFilters = () => {
    setArtFilterValues([]);
    setPlatformFilterValues([]);
    setFrontendToolFilterValues([]);
    setProductOwnerFilterValues([]);
    setDivisionFilterValues([]);
    setSubDivisionValues([]);
    setDataStewardValues([]);
    setInformationOwnerValues([]);
    setDepartmentValues([]);
    const newQueryParams = queryParams;

    newQueryParams.art = arts.map((phase: IDataProductListItem) => {
      return phase.id;
    });
    newQueryParams.platform = platforms.map((platform: IDataProductListItem) => {
      return platform.id;
    });
    newQueryParams.frontendTool = frontendTools.map((frontendTool: IDataProductListItem) => {
      return frontendTool.id;
    });
    newQueryParams.division = divisions.map((division : IDataProductListItem) =>{
      return division?.id;
    });
    newQueryParams.subDivision = subDivisions?.map((subDivision) => {
      return subDivision?.id;
    });
    newQueryParams.department = departments.map((department : IDataProductListItem) =>{
      return department?.name;
    });
    newQueryParams.dataSteward = [];
    newQueryParams.informationOwner = [];
    newQueryParams.productOwner = [];

    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.DATAPRODUCT_FILTER_VALUE), 50);
    ProgressIndicator.show();

    if (userPreferenceDataId) {
      DataProductFilterApiClient.removeUserPreference(userPreferenceDataId)
        .then((res) => {
          setTimeout(()=>onResetFilterCompleted(newQueryParams),50)
        })
        .catch((error: Error) => {
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    } else {
      setTimeout(()=>onResetFilterCompleted(newQueryParams),50)
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


  const onResetFilterCompleted = (queryParams: IDataProductFilterParams, showMessage?: boolean) => {
    setFilterApplied(false);
    setDpFilterApplied(false);
    setFocusedItems({});
    setQueryParams(queryParams);
    setUserPreferenceDataId(null);
    SelectBox.defaultSetup(false);
    getDataProductsByQueryParams(queryParams);
    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.DATAPRODUCT_FILTER_VALUE), 50);
    if (showMessage) {
      trackEvent(
        'Data Products',
        'Filter',
        'Removed or Resetted filter preferences',
      );
      Notification.show('Filter preference has been removed.');
    }
  };

  if(openFilters){
    if(document.getElementById("filterContainer")){
      const height = document?.getElementById('filterContainerDiv')?.clientHeight; // taking height of child div
      document.getElementById("filterContainer").setAttribute("style", "height:"+height+"px"); //assigning height to parent div
    }
  }else{
    if(document.getElementById("filterContainer")){
      document.getElementById("filterContainer").setAttribute("style", "height:"+0+"px");
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
              <div className="custom-select">
              <select id="divisionSelect" multiple={true} onChange={onDivisionChange} value={queryParams?.division}>
                  {divisions.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="subDivisionContainer" className={`input-field-group ${divisionFilterValues?.length ? '' : 'disabled'}`} onFocus={(e) => onHandleFocus(e, 'subDivision')}>
              <label id="subDivisionLabel" className="input-label" htmlFor="subDivisionSelect">
                Sub Division
              </label>
              <div className={`custom-select ${divisionFilterValues.length ? '' : 'disabled'}`}>
              <select id="subDivisionSelect" multiple={true} onChange={onSubDivisionChange} value={queryParams?.subDivision}>
                  {subDivisionsOfSelectedDivision.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="departmentContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'department')}>
              <label id="departmentLabel" className="input-label" htmlFor="departmentSelect">
              Department
              </label>
              <div className=" custom-select">
              <select id="departmentSelect" multiple={true} onChange={onDepartmentChange} value={queryParams?.department}>
                  {departments.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="artContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'art')}>
              <label id="artLabel" className="input-label" htmlFor="artSelect">
              ART
              </label>
              <div className=" custom-select">
              <select id="artSelect" multiple={true} onChange={onArtChange} value={queryParams?.art}>
                  {arts.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="platformContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'platform')}>
              <label id="platformLabel" className="input-label" htmlFor="platformSelect">
              Platform
              </label>
              <div className=" custom-select">
              <select id="platformSelect" multiple={true} onChange={onPlatformChange} value={queryParams?.platform}>
                  {platforms.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="frontendToolContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'frontendTool')}>
              <label id="frontendToolLabel" className="input-label" htmlFor="frontendToolSelect">
              Frontend Tool
              </label>
              <div id="frontendTool" className=" custom-select">
              <select id="frontendToolSelect" multiple={true} onChange={onFrontendToolChange} value={queryParams?.frontendTool}>
                  {frontendTools.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
                  </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="productOwnerContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'productOwner')}>
              <label id="productOwnerLabel" className="input-label" htmlFor="productOwnerSelect">
              Product Owner
              </label>
              <div className=" custom-select">
              <select id="productOwnerSelect" multiple={false} onChange={onProductOwnerChange} value={queryParams?.productOwner.join('')}>
              <option id="defaultStatus" value={''}>
                  Choose
                  </option>
                {productOwners?.map((obj: any, index) => (
                <option key={index} value={obj?.teamMemeber.shortId}>
                  {`${obj?.teamMemeber.firstName} ${obj?.teamMemeber.lastName}`}
                </option>
              ))}
              </select>
              </div>
          </div>
       </div>
       <div>
          <div id="dataStewardContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'dataSteward')}>
              <label id="dataStewardLabel" className="input-label" htmlFor="dataStewardSelect">
                Data Steward
              </label>
              <div className=" custom-select">
              <select id="dataStewardSelect" multiple={false} onChange={onDataStewardChange} value={queryParams?.dataSteward?.join('')}>
              <option id="defaultStatus" value={''}>
                  Choose
                  </option>
                  {dataStewards.map((obj, index) => (
                   <option key={index} value={obj?.teamMemeber?.shortId}>
                   {`${obj?.teamMemeber?.firstName} ${obj?.teamMemeber?.lastName}`}
               </option>
                  ))}
              </select>
              </div>
          </div>
      </div>
      <div>
          <div id="informationOwnerContainer" className="input-field-group" onFocus={(e) => onHandleFocus(e, 'informationOwner')}>
              <label id="informationOwnerLabel" className="input-label" htmlFor="informationOwnerSelect">
              Responsible Manager
              </label>
          <div className=" custom-select">
            <select id="informationOwnerSelect" multiple={false} onChange={onInformationOwnerChange} value={queryParams?.informationOwner?.join('')}>
                <option id="defaultStatus" value={''}>
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

      {/* <div>
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
      </div> */}
      {/* <div>
          <div>
              <Tags
                title={'CarLA Function'}
                max={100}
                chips={queryParams?.tag}
                setTags={onsetTags}
                tags={tagFilterValues}
                isMandatory={false}
                showMissingEntryError={false}
              />
          </div>
      </div> */}
      <div className={classNames(Styles.actionWrapper, dataFilterApplied ? '' : 'hidden')}>
          <div className="icon-tile">
              <button className="btn btn-icon-circle" tooltip-data="Reset Filters" onClick={resetDataFilters}>
              <i className="icon mbc-icon refresh" />
              </button>
          </div>
          <button className={classNames('btn btn-primary', Styles.saveSettingsBtn, 'hidden')} onClick={saveFilterPreference}>
              Save settings
          </button>
      </div> 
    </FilterWrapper>
    
  );
};

export default DataProductFilter;
