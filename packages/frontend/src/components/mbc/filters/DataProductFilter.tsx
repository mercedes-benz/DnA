import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import {
  IDataProductListItem,
  IDataProductFilterParams,
  IUserPreferenceRequest,
  IUserInfo,
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
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { trackEvent } from '../../../services/utils';
// import Tags from 'components/formElements/tags/Tags';

import FilterWrapper from './FilterWrapper';

type DataProductsFilterType = {
  user: IUserInfo;
  getValuesFromFilter?: Function;
  getFilterQueryParams?:Function;
  dataProductsDataLoaded: boolean;
  setDataProductsDataLoaded: Function;
  showDataProductsFilter?: boolean;
  openFilters?: boolean;
  setSelectedTags?: string[]; // this prop is used to set selected tags from tags component
};

const DataProductFilter = ({
  user,
  // getValuesFromFilter,
  getFilterQueryParams,
  dataProductsDataLoaded,
  openFilters,
}: DataProductsFilterType) => {

  // dropdown values
  const [arts, setArts] = useState<IDataProductListItem[]>([]);
  const [frontendTools, setFrontendTools] = useState<IDataProductListItem[]>([]);
  const [platforms, setPlatforms] = useState<IDataProductListItem[]>([]);
  const [productOwners, setProductOwners] = useState<IDataProductListItem[]>([]);
  // const [tagValues] = useState<ITag[]>([]);
  // const [tagFilterValues, setTagFilterValues] = useState<ITag[]>(tagsList);
  const [queryParams, setQueryParams] = useState<IDataProductFilterParams>({
    art: [],
    platform: [],
    frontendTool: [],
    productOwner: [],
    carlaFunction: [],
    tag: [],
  });

  const portfolioFilterValues: any = useRef();
  const [dataFilterApplied, setFilterApplied] = useState(false);
  const [userPreferenceDataId, setUserPreferenceDataId] = useState<string>(null);
  const [focusedItems, setFocusedItems] = useState({});
  const [userPreference, setRunUserPreference] = useState<boolean>(false);

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
    ProgressIndicator.show();
    DataProductFilterApiClient.getFilterMasterData()
      .then((response: any) => {
        if (response) {
          portfolioFilterValues.current = JSON.parse(
            sessionStorage.getItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES),
          ) as IDataProductFilterParams;

          const arts = response[0].data;
          const platforms = response[1].data;
          const frontendTools = response[2].data;
          const productOwners = response[3].records;

          // const tagValues: ITag[] = response[4].data;
          // getAllTags(response[4].data);
          
          let newQueryParams = queryParams;
          if (portfolioFilterValues.current) {
            newQueryParams = portfolioFilterValues.current;
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
            newQueryParams.productOwner = productOwners.map((productOwner: IDataProductListItem) => {
              return productOwner.id;
            });
            // newQueryParams.productOwner = [];
            // newQueryParams.tag = [];
            setFilterApplied(false);
          }

          setArts(arts);
          setPlatforms(platforms);
          setFrontendTools(frontendTools);
          setProductOwners(productOwners);
          // setTagValues(tagValues);
          setQueryParams(newQueryParams);
          SelectBox.defaultSetup();
          setRunUserPreference(true);
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
            if (!portfolioFilterValues.current) {
              queryParams.art = filterPreferences.arts.map((art: IDataProductListItem) => {
                return art.id;
              });
              queryParams.platform = filterPreferences.platforms.map((platform: IDataProductListItem) => {
                return platform.id;
              });
              queryParams.frontendTool = filterPreferences.frontendTools.map((frontendTool: IDataProductListItem) => {
                return frontendTool.id;
              });
              queryParams.productOwner = filterPreferences.productOwners.map((productOwner: IDataProductListItem) => {
                return productOwner.id;
              });
              setFilterApplied(true);
            }
            userPreferenceDataId = userPreference.id;
          }
          // sessionStorage.setItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES, JSON.stringify(queryParams));
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
      // ProgressIndicator.show();
      setFilterApplied(true);
      queryParams[filterName] = values;
      getDataProductsByQueryParams(queryParams);
    }
  };

  const onHandleFocus = (e: any, targetElement: string) => {
    //to ensure user action is done on the form fields
    setFocusedItems({ [targetElement]: true });
  };

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
    focusedItems['productOwners'] && applyFilter('productOwners', ids);
    setProductOwnerFilterValues(selectedValues);
  };

  const getDataProductsByQueryParams = (filterQueryParams: IDataProductFilterParams) => {
    const queryParams: IDataProductFilterParams = { ...filterQueryParams };

    let query = queryParams.art?.length > 0 ? '&art=' + queryParams.art?.join(',') : '';
    query = queryParams.frontendTool?.length > 0 ? query + '&frontendTools=' + queryParams.frontendTool?.join(',') : query;
    query = queryParams.platform?.length > 0 ? query + '&platform=' + queryParams.platform?.join(',') : query;
    query = queryParams.productOwner?.length > 0 ? query + '&productOwner=' + queryParams.productOwner?.join(',') : query;

    typeof getFilterQueryParams === 'function' && getFilterQueryParams(query);
  };

  const saveFilterPreference = () => {
    const filterPreferences: IDataProductFilterParams = {
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
    setArts([]);
    setPlatforms([]);
    setFrontendTools([]);
    setProductOwners([]);
    const newQueryParams = queryParams;
    newQueryParams.art = arts.map((phase: IDataProductListItem) => {
      return phase.id;
    });
    newQueryParams.platform = platforms.map((phase: IDataProductListItem) => {
      return phase.id;
    });
    newQueryParams.frontendTool = frontendTools.map((phase: IDataProductListItem) => {
      return phase.id;
    });
    newQueryParams.productOwner = productOwners.map((phase: IDataProductListItem) => {
      return phase.id;
    });

    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES), 50);
    ProgressIndicator.show();

    if (userPreferenceDataId) {
      DataProductFilterApiClient.removeUserPreference(userPreferenceDataId)
        .then((res) => {
          onResetFilterCompleted(newQueryParams, true);
        })
        .catch((error: Error) => {
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    } else {
      onResetFilterCompleted(newQueryParams);
    }
  };

  const onResetFilterCompleted = (queryParams: IDataProductFilterParams, showMessage?: boolean) => {
    setFilterApplied(false);
    setFocusedItems({});
    setQueryParams(queryParams);
    setUserPreferenceDataId(null);
    SelectBox.defaultSetup(false);
    getDataProductsByQueryParams(queryParams);
    setTimeout(() => sessionStorage.removeItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES), 50);
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

  return (
    <FilterWrapper openFilters={openFilters}>
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
              <select id="productOwnerSelect" multiple={true} onChange={onProductOwnerChange} value={queryParams?.productOwner.join('')}>
                  <option id="defaultStatus" value={0}>
                  Choose
                  </option>
                  {productOwners.map((obj: IDataProductListItem) => (
                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                      {obj.name}
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

export default DataProductFilter;
