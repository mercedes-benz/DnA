import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Pagination from 'components/mbc/pagination/Pagination';
import Styles from './TagHandling.scss';
const classNames = cn.bind(Styles);
import Tags from 'components/formElements/tags/Tags';

import { IFitlerCategory, ITagResult, ITag, ISubDivision, IDivisionChangeLog } from 'globals/types';
import { ApiClient } from '../../../../services/ApiClient';
import { ISortField } from 'components/mbc/allSolutions/AllSolutions';
import { TagRowItem } from './tagrowitem/TagRowItem';

import { SESSION_STORAGE_KEYS } from 'globals/constants';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import InputFieldsUtils from 'components/formElements/InputFields/InputFieldsUtils';
import { debounce } from 'lodash';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';

export interface ITagHandlingState {
  algoCategory: IFitlerCategory;
  dataSourceCategory: IFitlerCategory;
  languageCategory: IFitlerCategory;
  platformCategory: IFitlerCategory;
  tagCategory: IFitlerCategory;
  visualizationCategory: IFitlerCategory;
  customerPhaseJourneyCategory: IFitlerCategory;
  marketingCommunicationChannelCategory: IFitlerCategory;
  currentFilterCategory: IFitlerCategory;
  categories: IFitlerCategory[];
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  currentPageOffset: number;
  itemToAdd: ITagResult;
  totalNumberOfPages: number;
  sortBy: ISortField;
  results: ITagResult[];
  showDeleteTagModal: boolean;
  tagToBeDeleted: ITagResult;
  searchText: string;
  relatedProductList: IFitlerCategory;
  addNewItem: boolean;
  updateItemMode: boolean;
  addNewMarketingItem: boolean,
  updateMarketingItem: boolean,
  newItemNameCategoryError: string;
  newItemDivisionError: string;
  updateConfirmModelOverlay: boolean;
  itemToUpdate: string;
  newItemNameError: string;
  selectedDefaultCat: string;
  itemCategories: IFitlerCategory[];
  tagToBeUpdatedLocal: ITagResult;
  tags: ITag[];
  chips: string[];
  showTagsMissingError: boolean;
  isResultLoading: boolean;
  showDivisionChangeLogModal: boolean;
  divisionChangeLogs: IDivisionChangeLog[];
}

export class TagHandling extends React.Component<any, ITagHandlingState> {
  constructor(props: any) {
    super(props);
    this.state = {
      algoCategory: { id: 1, name: 'Algorithms' },
      dataSourceCategory: { id: 2, name: 'Data Sources' },
      languageCategory: { id: 3, name: 'Languages' },
      platformCategory: { id: 4, name: 'Platform' },
      tagCategory: { id: 5, name: 'Tags' },
      relatedProductList: { id: 7, name: 'Related Products' },
      visualizationCategory: { id: 6, name: 'Visualization' },
      marketingCommunicationChannelCategory: { id: 9, name: 'Marketing Communication Channel' },
      customerPhaseJourneyCategory: { id: 10, name: 'Customer Journey Phase' },
      currentFilterCategory: { id: 0, name: 'Select' },
      categories: [{ id: 0, name: 'Select' }],
      newItemNameError: null,
      sortBy: {
        name: 'name',
        currentSortType: 'desc',
        nextSortType: 'asc',
      },
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
      totalNumberOfRecords: 0,
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      currentPageOffset: 0,
      results: [],
      showDeleteTagModal: false,
      tagToBeDeleted: null,
      searchText: null,
      addNewItem: false,
      updateItemMode: false,
      addNewMarketingItem: false,
      updateMarketingItem: false,
      newItemNameCategoryError: null,
      itemToAdd: {
        id: '',
        name: '',
        category: {
          id: 8,
          name: 'Division',
        },
      },
      newItemDivisionError: null,
      showTagsMissingError: false,
      updateConfirmModelOverlay: false,
      itemToUpdate: '',
      selectedDefaultCat: 'Select',
      tags: [],
      chips: [],
      itemCategories: [
        {
          id: 0,
          name: 'Select',
        },
        {
          id: 8,
          name: 'Division',
        },
        { 
          id: 9, 
          name: 'Marketing Communication Channel' 
        },
        { 
          id: 10, 
          name: 'Customer Journey Phase' 
        }
      ],
      tagToBeUpdatedLocal: {
        id: '',
        name: '',
        category: {
          id: 8,
          name: 'Division',
        },
        subdivisions: [],
      },
      isResultLoading: false,
      showDivisionChangeLogModal: false,
      divisionChangeLogs: [],
    };
    ApiClient.getDropdownList('categories').then((dropdownList: any) => {
      dropdownList.data.push({ id: 8, name: 'Division' });
      dropdownList.data.push({ id: 9, name: 'Marketing Communication Channel' });
      dropdownList.data.push({ id: 10, name: 'Customer Journey Phase' });
      this.setState(
        {
          categories: this.state.categories.concat(dropdownList.data),
        },
        () => {
          SelectBox.defaultSetup();
        },
      );
    });
  }

  public getAlgorithms = (results: ITagResult[]) => {
    return ApiClient.getAlgorithms()
      .then((res) => {
        if (res) {
          res.forEach((algo) => {
            results.push({ category: this.state.algoCategory, id: algo.id + '', name: algo.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getDataSources = (results: ITagResult[]) => {
    return ApiClient.getMasterDataSources()
      .then((res1) => {
        if (res1) {
          res1.forEach((ds) => {
            results.push({
              category: this.state.dataSourceCategory,
              id: ds.id + '',
              name: ds.name,
              dataType: ds.dataType,
              source: ds.source,
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getPlatforms = (results: ITagResult[]) => {
    return ApiClient.getPlatforms()
      .then((res1) => {
        if (res1) {
          res1.forEach((platform) => {
            results.push({ category: this.state.platformCategory, id: platform.id + '', name: platform.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public relatedProductList = (results: ITagResult[]) => {
    return ApiClient.relatedProductList()
      .then((res1) => {
        if (res1) {
          res1.forEach((relPrd) => {
            results.push({ category: this.state.relatedProductList, id: relPrd.id + '', name: relPrd.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getDivisions = (results: ITagResult[]) => {
    return ApiClient.getDivisions()
      .then((res1) => {
        if (res1) {
          res1.forEach((divsion) => {
            results.push({
              category: { id: 8, name: 'Division' },
              id: divsion.id + '',
              name: divsion.name,
              subdivisions: divsion.subdivisions.filter((item: ISubDivision) => item.id !== 'EMPTY'),
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getLanguages = (results: ITagResult[]) => {
    return ApiClient.getLanguages()
      .then((res1) => {
        if (res1) {
          res1.forEach((language) => {
            results.push({ category: this.state.languageCategory, id: language.id + '', name: language.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getTags = (results: ITagResult[]) => {
    return ApiClient.getTags()
      .then((res1) => {
        if (res1) {
          res1.forEach((tag) => {
            results.push({ category: this.state.tagCategory, id: tag.id, name: tag.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getVisualizations = (results: ITagResult[]) => {
    return ApiClient.getVisualizations()
      .then((res1) => {
        if (res1) {
          res1.forEach((visualization) => {
            results.push({
              category: this.state.visualizationCategory,
              id: visualization.id + '',
              name: visualization.name,
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  public getMarketingCommunicationChannels = (results: ITagResult[]) => {
    return ApiClient.getMarketingCommunicationChannels()
      .then((res1) => {
        if (res1) {
          res1.forEach((marketingCommunicationChannel) => {
            results.push({
              category: this.state.marketingCommunicationChannelCategory,
              id: marketingCommunicationChannel.id + '',
              name: marketingCommunicationChannel.name,
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  public getCustomerJourneyPhases = (results: ITagResult[]) => {
    return ApiClient.getCustomerJourneyPhases()
      .then((res1) => {
        if (res1) {
          res1.forEach((customerPhaseJourney) => {
            results.push({
              category: this.state.customerPhaseJourneyCategory,
              id: customerPhaseJourney.id + '',
              name: customerPhaseJourney.name,
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  public async getResults(action: string) {
    const showProgressIndicator = ['add', 'update', 'delete', 'list'].includes(action);
    const showContentLoader = ['reset', 'categoryChange', 'search', 'pagination'].includes(action);

    showProgressIndicator && ProgressIndicator.show();
    showContentLoader && this.setState({ isResultLoading: true });

    let results: ITagResult[] = [];
    const filterCategory = this.state.currentFilterCategory;
    switch (filterCategory.id) {
      case 0:
        await this.getAlgorithms(results);
        await this.getDataSources(results);
        await this.getLanguages(results);
        await this.getPlatforms(results);
        await this.getTags(results);
        await this.getVisualizations(results);
        await this.relatedProductList(results);
        await this.getDivisions(results);
        await this.getMarketingCommunicationChannels(results);
        await this.getCustomerJourneyPhases(results);
        break;
      case 1:
        await this.getAlgorithms(results);
        break;
      case 2:
        await this.getDataSources(results);
        break;
      case 3:
        await this.getLanguages(results);
        break;
      case 4:
        await this.getPlatforms(results);
        break;
      case 5:
        await this.getTags(results);
        break;
      case 6:
        await this.getVisualizations(results);
        break;
      case 7:
        await this.relatedProductList(results);
        break;
      case 8:
        await this.getDivisions(results);
        break;
      case 9:
        await this.getMarketingCommunicationChannels(results);
        break;  
      case 10:
        await this.getCustomerJourneyPhases(results);
        break;  
    }
    if (this.state.searchText) {
      results = results.filter((result) => {
        return result.name.toLowerCase().match(this.state.searchText.toLowerCase());
      });
    }
    if (this.state.sortBy) {
      if (this.state.sortBy.name === 'name') {
        results = results.sort((a: ITagResult, b: ITagResult) => {
          if (this.state.sortBy.currentSortType === 'asc') {
            return a.name.toLowerCase() === b.name.toLowerCase() ? 0 : -1;
          } else {
            return a.name.toLowerCase() === b.name.toLowerCase() ? -1 : 0;
          }
        });
      } else if (this.state.sortBy.name === 'category') {
        results = results.sort((a: ITagResult, b: ITagResult) => {
          if (this.state.sortBy.currentSortType === 'asc') {
            return a.category.id - b.category.id;
          } else {
            return -(a.category.id - b.category.id);
          }
        });
      }
    }

    this.setState(
      {
        results: results.slice(
          this.state.currentPageOffset > results.length ? 0 : this.state.currentPageOffset,
          this.state.currentPageOffset + this.state.maxItemsPerPage < results.length
            ? this.state.currentPageOffset + this.state.maxItemsPerPage
            : results.length,
        ),
        totalNumberOfPages: Math.ceil(results.length / this.state.maxItemsPerPage),
        currentPageNumber:
          this.state.currentPageNumber > Math.ceil(results.length / this.state.maxItemsPerPage)
            ? Math.ceil(results.length / this.state.maxItemsPerPage) > 0
              ? Math.ceil(results.length / this.state.maxItemsPerPage)
              : 1
            : this.state.currentPageNumber,
      },
      () => {
        showProgressIndicator && ProgressIndicator.hide();
        showContentLoader && this.setState({ isResultLoading: false });
      },
    );
  }
  public async componentDidMount() {
    // SelectBox.defaultSetup();
    this.setState({ isResultLoading: true });
    await this.getResults('list');
    this.setState({ isResultLoading: false });
  }

  public onSearchInput = debounce((e: React.FormEvent<HTMLInputElement>) => {
    const input = e.target as HTMLInputElement;
    const searchText = input.value;
    this.setState({ searchText }, () => {
      this.getResults('search');
    });
  }, 500);
  public onCategoryChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        // tslint:disable-next-line: radix
        this.setState(
          {
            currentFilterCategory: { id: parseInt(option.value, 0), name: option.label },
            currentPageOffset: 0,
            currentPageNumber: 1,
          },
          () => {
            this.getResults(option.value === '0' ? 'reset' : 'categoryChange');
            SelectBox.defaultSetup(true);
          },
        );
      });
    }
  };
  public sortTags = (propName: string, sortOrder: string) => {
    const sortBy: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: this.state.sortBy.currentSortType,
    };
    this.setState(
      {
        sortBy,
      },
      () => {
        this.getResults('sort');
      },
    );
  };
  public showUpdateConfirmModal = (tagItem: ITagResult) => {
    if(tagItem.category.id === 9 || tagItem.category.id === 10){
      this.setState(
        {
          itemToUpdate: tagItem.category.name,
          // addNewItem: true,
          // updateItemMode: true,
          addNewMarketingItem: true,
          updateMarketingItem: true,
          tagToBeUpdatedLocal: tagItem,
          newItemNameError: null,
          newItemNameCategoryError: null,
        },
        () => {
          InputFieldsUtils.resetErrors('#solutionAddOrUpdateFormWrapper');
          SelectBox.defaultSetup(true);
        },
      );
    }else{
      this.setState(
        {
          itemToUpdate: tagItem.category.name,
          addNewItem: true,
          chips: tagItem.subdivisions.map((item: ISubDivision) => item.name),
          updateItemMode: true,
          tagToBeUpdatedLocal: Object.assign({}, tagItem),
          newItemNameError: null,
          newItemNameCategoryError: null,
        },
        () => {
          InputFieldsUtils.resetErrors('#solutionAddOrUpdateFormWrapper');
          SelectBox.defaultSetup(true);
        },
      );
    }
    
    
  };

  public showDeleteConfirmModal = (tagItem: ITagResult) => {
    this.setState({ tagToBeDeleted: tagItem, showDeleteTagModal: true });
  };

  public onAddItemModalOpen = () => {
    const itemToAdd = this.state.itemToAdd;
    itemToAdd.name = '';
    this.setState(
      {
        itemToAdd,
        addNewItem: true,
        addNewMarketingItem: false,
        updateMarketingItem: false,
        updateItemMode: false,
        newItemNameError: null,
        newItemDivisionError: null,
        chips: [],
      },
      () => {
        InputFieldsUtils.resetErrors('#solutionAddOrUpdateFormWrapper');
      },
    );
  };

  public onAddMarketingItemModalOpen = ()  => {
    const itemToAdd = this.state.itemToAdd;
    itemToAdd.name = '';
    this.setState(
      {
        itemToAdd,
        addNewItem: false,
        addNewMarketingItem: true,
        updateItemMode: false,
        updateMarketingItem: false,
        newItemNameError: null,
        newItemDivisionError: null,
        chips: [],
      },
      () => {
        InputFieldsUtils.resetErrors('#solutionAddOrUpdateFormWrapper');
      },
    );
  }

  protected onAddItemModalCancel = () => {
    this.setState({
      addNewItem: false,
      newItemNameError: null,
      newItemNameCategoryError: null,
      chips: [],
    });
  };

  protected onAddMarketingModalCancel = () => {
    this.setState({
      addNewItem: false,
      addNewMarketingItem: false,
      newItemNameError: null,
      newItemNameCategoryError: null,
      chips: [],
    });
  };

  protected onChangeTagAddItem = (e: React.FormEvent<HTMLInputElement>) => {
    const itemToAdd = this.state.itemToAdd;
    itemToAdd.name = e.currentTarget.value.toLocaleUpperCase();
    this.setState({ itemToAdd });
  };

  protected onChangeUpdateItem = (e: React.FormEvent<HTMLInputElement>) => {
    const tagToBeUpdatedLocal = this.state.tagToBeUpdatedLocal;
    tagToBeUpdatedLocal.name = e.currentTarget.value;
    this.setState({ tagToBeUpdatedLocal });
  };

  protected onChooseCategories = (e: React.ChangeEvent<HTMLSelectElement>) => {
    this.setState({ selectedDefaultCat: e.target.value });
  };

  protected updateConfirmModelOverlayCancel = () => {
    this.setState({
      updateConfirmModelOverlay: false,
    });
  };
  protected updateConfirmModelOverlayUpdate = () => {
    this.setState({
      updateConfirmModelOverlay: this.addItemFormValidation(),
    });
  };

  protected setTags = (arr: string[]) => {
    const tagToBeUpdatedLocal = this.state.tagToBeUpdatedLocal;
    tagToBeUpdatedLocal.subdivisions = arr.map((item: string) => ({ id: '', name: item }));
    this.setState({ tagToBeUpdatedLocal });
  };

  protected addItemFormValidation = () => {
    let formValidationStatus = true;
    const errorMissingEntry = '*Missing entry';
    const noItemValue = 
    this.state.currentFilterCategory.id === 9 || this.state.currentFilterCategory.id === 10 ?
    (this.state.updateMarketingItem ?  this.state.tagToBeUpdatedLocal.name === '' : this.state.itemToAdd.name === '')
    :
    (this.state.updateItemMode ? this.state.tagToBeUpdatedLocal.name === '' : this.state.itemToAdd.name === '');
    if (noItemValue) {
      this.setState({
        newItemNameError: errorMissingEntry,
      });
      formValidationStatus = false;
    }
    if (this.state.itemToAdd.category.name === 'Select' || this.state.itemToAdd.category.name === '') {
      this.setState({
        newItemNameCategoryError: errorMissingEntry,
      });
      formValidationStatus = false;
    }
    return formValidationStatus;
  };

  protected onMarketingAddItem = () => {
    if (this.addItemFormValidation()) {
      ProgressIndicator.show();
      const data = {
        data: {
          name: this.state.itemToAdd.name?.toUpperCase()?.trim(),
        },
      };

      const requestBody = JSON.parse(JSON.stringify(data));
      if(this.state.currentFilterCategory.id === 9){
        return ApiClient.createMarketingCommunicationChannels(requestBody)
        .then((res: any) => {
          this.setState(
            {
              addNewMarketingItem: false,
            },
            () => {
              this.showNotification('Marketing Channel Added Successfully!');
              this.getResults('add');
              ProgressIndicator.hide();
            },
          );
        })
        .catch((error: any) => {
          ProgressIndicator.hide();
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
      } else if(this.state.currentFilterCategory.id === 10){
        return ApiClient.createCustomerJourneyPhases(requestBody)
        .then((res: any) => {
          this.setState(
            {
              addNewMarketingItem: false,
            },
            () => {
              this.showNotification('Customer Journey Phase Added Successfully!');
              this.getResults('add');
              ProgressIndicator.hide();
            },
          );
        })
        .catch((error: any) => {
          ProgressIndicator.hide();
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
      }
      
    }
  };

  protected onDivisionAddItem = () => {
    if (this.addItemFormValidation()) {
      ProgressIndicator.show();
      const data = {
        data: {
          name: this.state.itemToAdd.name?.toUpperCase()?.trim(),
          subdivisions: this.state.tagToBeUpdatedLocal.subdivisions,
        },
      };

      const requestBody = JSON.parse(JSON.stringify(data));
      return ApiClient.postDivision(requestBody)
        .then((res: any) => {
          this.setState(
            {
              addNewItem: false,
            },
            () => {
              this.showNotification('Division Added Successfully!');
              this.getResults('add');
              ProgressIndicator.hide();
            },
          );
        })
        .catch((error: any) => {
          // this.setState(
          //   {
          //     results: [],
          //     addNewItem: false,
          //   },
          //   () => {
          ProgressIndicator.hide();
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          //this.getResults();
          //   },
          // );
        });
    }
  };

  protected onTagUpdateItem = () => {
    const itemToUpdate = this.state.tagToBeUpdatedLocal;
    const data = {
      data: {
        id: itemToUpdate.id,
        name: itemToUpdate.name.toUpperCase()?.trim(),
        subdivisions: itemToUpdate.subdivisions,
      },
    };
    ProgressIndicator.show();
    const requestBody = JSON.parse(JSON.stringify(data));
    if(this.state.currentFilterCategory.id === 9) {
      return ApiClient.putMarketingCommunicationChannels(requestBody)
      .then((res: any) => {
        this.setState(
          {
            updateConfirmModelOverlay: false,
            addNewItem: false,
          },
          () => {
            this.showNotification('Marketing Communication Channels Updated Successfully!');
            this.getResults('update');
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error: any) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    } else if(this.state.currentFilterCategory.id === 10){
      return ApiClient.putCustomerJourneyPhases(requestBody)
      .then((res: any) => {
        this.setState(
          {
            updateConfirmModelOverlay: false,
            addNewItem: false,
          },
          () => {
            this.showNotification('Customer Journey Phase Updated Successfully!');
            this.getResults('update');
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error: any) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    } else {
    return ApiClient.putDivision(requestBody)
      .then((res: any) => {
        this.setState(
          {
            updateConfirmModelOverlay: false,
            addNewItem: false,
          },
          () => {
            this.showNotification('Division Updated Successfully!');
            this.getResults('update');
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error: any) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    }  
  };

  protected handleChangeLogModal = () => {
    ProgressIndicator.show();
    ApiClient.getDivisionChangeLogs().then((res: any) => {
      this.setState({
        divisionChangeLogs: res.records,
        showDivisionChangeLogModal: true,
      });
      ProgressIndicator.hide();
    });
  };

  public render() {
    const requiredError = '*Missing entry';
    const newItemNameCategoryError = this.state.newItemNameCategoryError || '';
    const newItemNameError = this.state.newItemNameError || '';

    const resultData = this.state.results.map((result) => {
      return (
        <TagRowItem
          tagItem={result}
          key={result.id + '' + result.category.id}
          showDeleteConfirmModal={this.showDeleteConfirmModal}
          showUpdateConfirmModal={this.showUpdateConfirmModal}
        />
      );
    });

    const contentForAddNewItem = (
      <div id="solutionAddOrUpdateFormWrapper" className={Styles.infoPopup}>
        <div className={classNames(Styles.modalContent, Styles.formWrapperMain)}>
          <div>
            <div
              className={classNames(
                'input-field-group include-error',
                newItemNameCategoryError?.length ? 'error' : ' ',
                this.state.updateItemMode ? ' inactive ' : '',
                'hide',
              )}
            >
              <label id="itemCategories" htmlFor="solutionItemCategoriesField" className="input-label">
                Category<sup>*</sup>
              </label>
              <div className="custom-select">
                <select
                  id="solutionItemCategoriesField"
                  onChange={this.onChooseCategories}
                  required={true}
                  required-error={requiredError}
                  value={this.state.currentFilterCategory.name}
                >
                  {this.state.updateItemMode ? (
                    <option value={this.state.itemToUpdate}> {this.state.itemToUpdate} </option>
                  ) : (
                    this.state.itemCategories.map((obj) => (
                      <option key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))
                  )}
                </select>
              </div>
              <span className={classNames('error-message', this.state.newItemNameCategoryError?.length ? '' : 'hide')}>
                {this.state.newItemNameCategoryError}
              </span>
            </div>
            {(this.state.currentFilterCategory.id === 9 || 
                this.state.currentFilterCategory.id === 10) ? (
              <React.Fragment>
                <div
                  id="reportDecsription"
                  className={classNames('input-field-group include-error ', newItemNameError.length ? 'error' : '')}
                >
                  <label id="itemName" htmlFor="itemNameInput" className="input-label">
                    Item Name<sup>*</sup>
                  </label>
                  {this.state.updateMarketingItem ? (
                    <input
                      type="text"
                      className="input-field"
                      required={true}
                      required-error={requiredError}
                      id="itemNameInput"
                      maxLength={200}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={this.onChangeUpdateItem}
                      value={this.state.tagToBeUpdatedLocal.name}
                    />
                  ) : (
                    <input
                      type="text"
                      className="input-field"
                      required={true}
                      required-error={requiredError}
                      id="itemNameInput"
                      maxLength={200}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={this.onChangeTagAddItem}
                      value={this.state.itemToAdd.name}
                    />
                  )}
                  <span className={classNames('error-message', newItemNameError.length ? '' : 'hide')}>
                    {newItemNameError}
                  </span>
                </div>
              </React.Fragment>
            ) : (
              <div className={classNames('input-field-group include-error', newItemNameError.length ? 'error' : ' ')}>
                <label id="itemName" htmlFor="itemNameInput" className="input-label">
                  Division<sup>*</sup>
                </label>
                {this.state.updateItemMode ? (
                  <input
                    type="text"
                    className="input-field"
                    required={true}
                    required-error={requiredError}
                    id="itemNameInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                    onChange={this.onChangeUpdateItem}
                    value={this.state.tagToBeUpdatedLocal.name}
                  />
                ) : (
                  <input
                    type="text"
                    className="input-field"
                    required={true}
                    required-error={requiredError}
                    id="itemNameInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                    onChange={this.onChangeTagAddItem}
                    value={this.state.itemToAdd.name}
                  />
                )}
                <span className={classNames('error-message', this.state.newItemNameCategoryError?.length ? '' : 'hide')}>
                  {this.state.newItemNameCategoryError}
                </span>
              </div>
            )}
            
            {(this.state.currentFilterCategory.id === 9 || 
                this.state.currentFilterCategory.id === 10) ? '' :
            <Tags
              title={'Sub Division'}
              max={100}
              tags={this.state.tags}
              chips={this.state.chips}
              setTags={this.setTags}
              isMandatory={false}
              showMissingEntryError={this.state.showTagsMissingError}
              enableUppercase={true}
              {...this.props}
            /> }
          </div>
          <br />
          <div className={Styles.addBtn}>
            <button
              onClick={ 
                this.state.currentFilterCategory.id === 9 || 
                  this.state.currentFilterCategory.id === 10?
                  (this.state.updateMarketingItem ? this.updateConfirmModelOverlayUpdate : this.onMarketingAddItem)
                  :
                  (this.state.updateItemMode ? this.updateConfirmModelOverlayUpdate : this.onDivisionAddItem)}
              className={Styles.actionBtn + ' btn btn-tertiary'}
              type="button"
            >
              {(this.state.currentFilterCategory.id === 9 || 
                  this.state.currentFilterCategory.id === 10)? 
                    (this.state.updateMarketingItem ? <span>Update</span> : <span>Add</span>) : 
                  (this.state.updateItemMode ? <span>Update</span> : <span>Add</span>)}
            </button>
          </div>
        </div>
        {this.state.updateConfirmModelOverlay && (
          <div className={Styles.updateModelOverlayContent}>
            <p>
              Updating &lt;&lt; {this.state.tagToBeUpdatedLocal.name} &gt;&gt; would also update all the associated
              solutions{this.state.tagToBeUpdatedLocal.category.name === 'Division' && ' and reports'}. <br /> Do you
              want to proceed?
            </p>
            <div>
              <button
                className={Styles.actionBtn + ' btn btn-default'}
                type="button"
                onClick={this.updateConfirmModelOverlayCancel}
              >
                Cancel
              </button>{' '}
              &nbsp;
              <button className={Styles.actionBtn + ' btn btn-tertiary'} type="button" onClick={this.onTagUpdateItem}>
                Update
              </button>
            </div>
          </div>
        )}
      </div>
    );

    const modalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Tag</div>
        <div className={Styles.modalContent}>
          The tag &laquo;{this.state.tagToBeDeleted ? this.state.tagToBeDeleted.name : ''}&raquo; will be removed
          permanently.
        </div>
      </div>
    );

    const divisionChangeLog = (
      <table className="ul-table solutions">
        <tbody>
          <tr className="header-row">
            <th colSpan={8}>
              <span className="hidden">`</span>
            </th>
          </tr>
          {this.state.divisionChangeLogs
            ? this.state.divisionChangeLogs?.map((data: IDivisionChangeLog, index: number) => {
                return (
                  <tr key={index} className="data-row">
                    <td className="wrap-text">{regionalDateAndTimeConversionSolution(data.createdOn)}</td>
                    <td className="wrap-text">
                      {data.createdBy.firstName}&nbsp;{data.createdBy.lastName}
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td className="wrap-text">{data.message}</td>
                  </tr>
                );
              })
            : ''}
        </tbody>
      </table>
    );

    return (
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.searchPanel}>
            <div>
              <div className={`input-field-group search-field ${this.state.isResultLoading ? 'disabled' : ''}`}>
                <label id="searchLabel" className="input-label" htmlFor="searchInput">
                  Search Entries
                </label>
                <input
                  type="text"
                  className="input-field search"
                  required={false}
                  id="searchInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                  onChange={this.onSearchInput}
                  disabled={!this.state.searchText && this.state.isResultLoading}
                />
              </div>
            </div>
            <div>
              <div id="statusContainer" className={`input-field-group ${this.state.isResultLoading ? 'disabled' : ''}`}>
                <label id="statusLabel" className="input-label" htmlFor="statusSelect">
                  Filter by
                </label>
                <div className={Styles.customContainer}>
                  <div className={`custom-select ${this.state.isResultLoading ? 'disabled' : ''}`}>
                    <select id="filterBy" onChange={this.onCategoryChange}>
                      {/* {this.state.categories && */}
                      {this.state.categories.map((category: IFitlerCategory) => (
                        <option key={category.id} id={'' + category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                      {/* }  */}
                    </select>
                  </div>
                </div>
              </div>
            </div>
            <div>
              <div>
                <div
                  className={classNames(
                    Styles.addItemButton,
                    this.state.isResultLoading ? Styles.disabledAddItemBtn : '',
                  )}
                >{(this.state.currentFilterCategory.id == 9 || this.state.currentFilterCategory.id == 10)?(
                  <>
                    <button onClick={this.onAddMarketingItemModalOpen}>
                      <i className="icon mbc-icon plus" />
                      <span>Add New Marketing Values</span>
                    </button>
                  </>
                ):(
                  <>
                    <button onClick={this.onAddItemModalOpen}>
                      <i className="icon mbc-icon plus" />
                      <span>Add New Division</span>
                    </button>
                    <button className={Styles.changeLog} onClick={this.handleChangeLogModal}>
                      <i className="icon mbc-icon link" />
                      <span>Division Change Logs</span>
                    </button>
                  </>
                )}
                  
                </div>
              </div>
            </div>
          </div>
          {resultData.length === 0 ? (
            <div className={Styles.tagIsEmpty}>There is no tag available</div>
          ) : (
            <div className={Styles.tablePanel}>
              {!this.state.isResultLoading ? (
                <table className="ul-table users">
                  <thead>
                    <tr className="header-row">
                      <th onClick={this.sortTags.bind(null, 'name', this.state.sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' +
                            (this.state.sortBy.name === 'name' ? this.state.sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Name
                        </label>
                      </th>
                      <th onClick={this.sortTags.bind(null, 'category', this.state.sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' +
                            (this.state.sortBy.name === 'category' ? this.state.sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Category
                        </label>
                      </th>

                      <th className="actionColumn">
                        <label>Action</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>{resultData}</tbody>
                </table>
              ) : (
                <div className={classNames('text-center', Styles.spinner)}>
                  <div className="progress infinite" />
                </div>
              )}
              {this.state.results.length ? (
                <Pagination
                  totalPages={this.state.totalNumberOfPages}
                  pageNumber={this.state.currentPageNumber}
                  onPreviousClick={this.onPaginationPreviousClick}
                  onNextClick={this.onPaginationNextClick}
                  onViewByNumbers={this.onViewByPageNum}
                  displayByPage={true}
                />
              ) : (
                ''
              )}
            </div>
          )}
          <ConfirmModal
            title="Delete Tag"
            acceptButtonTitle="Delete"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.showDeleteTagModal}
            content={modalContent}
            onCancel={this.onCancellingDeleteChanges}
            onAccept={this.onAcceptDeleteChanges}
          />
          <InfoModal
            title={this.state.updateItemMode ? 'Update Division' : 'Add New Division'}
            modalWidth={'35vw'}
            show={this.state.addNewItem}
            content={contentForAddNewItem}
            onCancel={this.onAddItemModalCancel}
          />
          <InfoModal
            title={!this.state.updateMarketingItem ? 
              this.state.currentFilterCategory.id === 9 ? 'Add Marketing Communication Channel' : 'Add Customer Journey Phase' : 
            this.state.currentFilterCategory.id === 9 ? 'Update Marketing Communication Channel' : 'Update Customer Journey Phase'}
            modalWidth={'35vw'}
            show={this.state.addNewMarketingItem}
            content={contentForAddNewItem}
            onCancel={this.onAddMarketingModalCancel}
          />
          <InfoModal
            title={'Change Logs'}
            show={this.state.showDivisionChangeLogModal}
            content={this.state.divisionChangeLogs ? divisionChangeLog : ''}
            onCancel={this.onDivisionChangeLogModalCancel}
          />
        </div>
      </div>
    );
  }
  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteTagModal: false });
  };
  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    const tagToBeDeleted = this.state.tagToBeDeleted;
    const handleSuccess = () => {
      this.setState({ showDeleteTagModal: false }, () => {
        ProgressIndicator.hide();
        this.showNotification('Tag deleted successfully');
        this.getResults('delete');
      });
    };
    const handleFailure = () => {
      this.setState({ showDeleteTagModal: false }, () => {
        ProgressIndicator.hide();
        this.showErrorNotification('Error during tag delete');
      });
    };
    if (tagToBeDeleted.category.id === 1) {
      ApiClient.deleteAlgorithm(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 2) {
      ApiClient.deleteDataSource(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 3) {
      ApiClient.deleteLanguage(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 4) {
      ApiClient.deletePlatform(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 5) {
      ApiClient.deleteTag(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 6) {
      ApiClient.deleteVisualization(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 7) {
      ApiClient.deleterelatedProductList(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 8) {
      ApiClient.deleteDivision(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 9) {
      ApiClient.deleteMarketingCommunicationChannels(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 10) {
      ApiClient.deleteCustomerJourneyPhases(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    }
  };
  protected onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getResults('pagination');
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getResults('pagination');
    });
  };

  protected showErrorNotification(message: string) {
    Notification.show(message, 'alert');
  }

  protected showNotification(message: string) {
    Notification.show(message);
  }
  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      this.getResults('pagination');
    });
  };

  protected onDivisionChangeLogModalCancel = () => this.setState({ showDivisionChangeLogModal: false });
}
