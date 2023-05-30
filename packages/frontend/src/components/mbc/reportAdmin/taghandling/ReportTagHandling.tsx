import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Pagination from 'components/mbc/pagination/Pagination';
import Styles from './ReportTagHandling.scss';

import { IFitlerCategory, ITagResult, IDatawarehouseInItem } from 'globals/types';
import { ReportsApiClient } from '../../../../services/ReportsApiClient';
import { ISortField } from 'components/mbc/allSolutions/AllSolutions';
import { TagRowItem } from './tagrowitem/TagRowItem';

import { SESSION_STORAGE_KEYS } from 'globals/constants';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import Tags from 'components/formElements/tags/Tags';
import InputFieldsUtils from 'components/formElements/InputFields/InputFieldsUtils';

const classNames = cn.bind(Styles);
// @ts-ignore
import InputFields from '../../../../assets/modules/uilab/js/src/input-fields';
import { debounce } from 'lodash';
import TextBox from 'components/mbc/shared/textBox/TextBox';
// import { workerData } from 'worker_threads';

export interface ITagHandlingState {
  // productPhases: IFitlerCategory;
  // designGuideImplementation: IFitlerCategory;
  statuses: IFitlerCategory;
  fronEndTechnologies: IFitlerCategory;
  legalEntity: IFitlerCategory;
  currentFilterCategory: IFitlerCategory;
  categories: IFitlerCategory[];
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  currentPageOffset: number;
  totalNumberOfPages: number;
  sortBy: ISortField;
  results: ITagResult[];
  showDeleteTagModal: boolean;
  showUpdateTagModal: boolean;
  tagToBeDeleted: ITagResult;
  tagToBeUpdated: ITagResult;
  tagToBeUpdatedLocal: ITagResult;
  searchText: string;
  integratedPortal: IFitlerCategory;
  addNewItem: boolean;
  departments: IFitlerCategory;
  // commonFunctions: IFitlerCategory;
  connectionTypes: IFitlerCategory;
  agileReleaseTrain: IFitlerCategory;
  level: IFitlerCategory;
  getKpiName: IFitlerCategory;
  dataSource: IFitlerCategory;
  reportingCause: IFitlerCategory;
  datawerehouse: IFitlerCategory;
  dataClassification: IFitlerCategory;
  itemCategories: IFitlerCategory[];
  selectedDefaultCat: string;
  itemToAdd: string;
  itemToAddCategories: string;
  itemToUpdate: string;
  newItemNameError: string;
  newItemNameCategoryError: string;
  dataWareHouseNameError: string;
  updateItemMode: boolean;
  dataFunctionNotEnable: boolean;
  tabActive: string;
  tags: string[];
  showTagsMissingError: boolean;
  showCommanFnTagsMissingError: boolean;
  showSpecificFnTagsMissingError: boolean;
  showDataSourcesTagsMissingError: boolean;
  showQueriesTagsMissingError: boolean;
  showConnectionTypesTagsMissingError: boolean;
  singleDataSource: string;
  datawerehouseinuse: string;
  datawareHouseItems: IDatawarehouseInItem;
  tagsList: IFitlerCategory;
  updateConfirmModelOverlay: boolean;
  dropdownTouched: boolean;
  isResultLoading: boolean;
}

export class ReportTagHandling extends React.Component<any, ITagHandlingState> {
  constructor(props: any) {
    super(props);
    this.state = {
      categories: [{ id: 0, name: 'Select' }],
      departments: { id: 1, name: 'Departments' },
      fronEndTechnologies: { id: 2, name: 'FrontEnd Technologies' },
      integratedPortal: { id: 3, name: 'Integrated In Portal' },
      statuses: { id: 4, name: 'Statuses' },
      agileReleaseTrain: { id: 5, name: 'Agile Release Train' },
      tagsList: { id: 6, name: 'Tags' },
      level: { id: 7, name: 'Levels' },
      legalEntity: { id: 8, name: 'MB Legal Entity' },
      getKpiName: { id: 9, name: 'Kpi Names' },
      reportingCause: { id: 10, name: 'Reporting Cause' },
      datawerehouse: { id: 11, name: 'Dataware house' },
      // commonFunctions: { id: 12, name: 'Common Functions' },
      dataSource: { id: 13, name: 'Data Source' },
      connectionTypes: { id: 14, name: 'Connection Types' },
      dataClassification: { id: 15, name: 'Data Classifications' },
      currentFilterCategory: { id: 0, name: 'Select' },
      selectedDefaultCat: 'Select',
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
      showUpdateTagModal: false,
      tagToBeDeleted: null,
      tagToBeUpdated: {
        id: '',
        name: '',
        category: {
          id: -1,
          name: '',
        },
      },
      tagToBeUpdatedLocal: {
        id: '',
        name: '',
        category: {
          id: -1,
          name: '',
        },
      },
      searchText: null,
      addNewItem: false,
      itemCategories: [
        {
          id: 0,
          name: 'Select',
        },
        {
          id: 1,
          name: 'E2-Departments',
        },
        {
          id: 2,
          name: 'Description - Front End Technologies',
        },
        {
          id: 3,
          name: 'Description - Integrated In Portal',
        },
        {
          id: 4,
          name: 'Description - Statuses',
        },
        {
          id: 5,
          name: 'Description - Agile Release Train',
        },
        {
          id: 6,
          name: 'Description - Tags',
        },
        {
          id: 7,
          name: 'Customer - Levels',
        },
        {
          id: 8,
          name: 'Customers - MB Legal Entities',
        },
        {
          id: 9,
          name: 'Kpi - KPI Name',
        },
        {
          id: 10,
          name: 'Kpi - Reporting Cause',
        },
        {
          id: 11,
          name: 'Data & Function - Data Warehouse - Data Warehouse',
        },
        // {
        //   id: 12,
        //   name: 'Data & Function - Data Warehouse - Common Functions',
        // },
        // {
        //   id: 13,
        //   name: 'Data & Function - Single Data Source - Data Sources',
        // },
        {
          id: 14,
          name: 'Data & Function - Connection Types',
        },
        {
          id: 15,
          name: 'Data & Function - Data Classifications',
        },
      ],
      datawareHouseItems: {
        id: '',
        dataWarehouse: '',
        // commonFunctions: [],
        dataClassification: '',
        connectionType: '',
      },
      itemToAdd: '',
      itemToAddCategories: '',
      itemToUpdate: '',
      newItemNameError: null,
      newItemNameCategoryError: null,
      dataWareHouseNameError: null,
      updateItemMode: false,
      dataFunctionNotEnable: true,
      tabActive: 'DW',
      tags: [],
      showTagsMissingError: false,
      showCommanFnTagsMissingError: false,
      showSpecificFnTagsMissingError: false,
      showDataSourcesTagsMissingError: false,
      showQueriesTagsMissingError: false,
      showConnectionTypesTagsMissingError: false,
      singleDataSource: '',
      datawerehouseinuse: '',
      updateConfirmModelOverlay: false,
      dropdownTouched: false,
      isResultLoading: false,
    };
  }
  protected onDatawarehouseInuse = (e: React.FormEvent<HTMLInputElement>) => {
    const datawareHouseItem = this.state.datawareHouseItems;
    datawareHouseItem.dataWarehouse = e.currentTarget.value;
    if (datawareHouseItem.dataWarehouse !== '') {
      this.setState({
        dataWareHouseNameError: '',
      });
    } else {
      this.setState({
        dataWareHouseNameError: '*Missing entry',
      });
    }
  };
  protected setCommonFnTags = (arr: string[]) => {
    // const chipsItemAdd = this.state.datawareHouseItems;
    // chipsItemAdd.commonFunctions = arr;
    // if (chipsItemAdd.commonFunctions.length > 0) {
    //   this.setState({
    //     showCommanFnTagsMissingError: false,
    //   });
    // } else {
    //   this.setState({
    //     showCommanFnTagsMissingError: true,
    //   });
    // }
  };
  protected setConnectionTypesTags = (arr: string[]) => {
    // const chipsItemAdd = this.state.datawareHouseItems;
    // chipsItemAdd.connectionType = arr;
    // if (chipsItemAdd.connectionType.length > 0) {
    //   this.setState({
    //     showConnectionTypesTagsMissingError: false,
    //   });
    // } else {
    //   this.setState({
    //     showConnectionTypesTagsMissingError: true,
    //   });
    // }
  };
  // protected setDataSoucrsTags = (arr: string[]) => {
  //   const chipsItemAdd = this.state.datawareHouseItems;
  //   chipsItemAdd.dataSources = arr;
  //   if (chipsItemAdd.dataSources.length > 0) {
  //     this.setState({
  //       showDataSourcesTagsMissingError: false,
  //     });
  //   } else {
  //     this.setState({
  //       showDataSourcesTagsMissingError: true,
  //     });
  //   }
  // };
  public getStatuses = (results: ITagResult[]) => {
    return ReportsApiClient.getStatuses()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((statuses: IFitlerCategory) => {
            results.push({ category: this.state.statuses, id: statuses.id + '', name: statuses.name });
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
  public getIntegratedPortal = (results: ITagResult[]) => {
    return ReportsApiClient.getIntegratedPortal()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((integratedPortal: IFitlerCategory) => {
            results.push({
              category: this.state.integratedPortal,
              id: integratedPortal.id + '',
              name: integratedPortal.name,
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
  public getDataClassifications = (results: ITagResult[]) => {
    return ReportsApiClient.getDataClassifications()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((desigGuide: IFitlerCategory) => {
            results.push({
              category: this.state.dataClassification,
              id: desigGuide.id + '',
              name: desigGuide.name,
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
  public getFronEndTechnologies = (results: ITagResult[]) => {
    return ReportsApiClient.getFronEndTechnologies()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((frontEndTech: IFitlerCategory) => {
            results.push({
              category: this.state.fronEndTechnologies,
              id: frontEndTech.id + '',
              name: frontEndTech.name,
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
  public getLegalEntities = (results: ITagResult[]) => {
    return ReportsApiClient.getLegalEntities()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((legalEntity: IFitlerCategory) => {
            results.push({
              category: this.state.legalEntity,
              id: legalEntity.id + '',
              name: legalEntity.name,
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

  public getDepartments = (results: ITagResult[]) => {
    return ReportsApiClient.getDepartments()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((dep: IFitlerCategory) => {
            results.push({ category: this.state.departments, id: dep.id + '', name: dep.name });
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
    return ReportsApiClient.getTags()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((dep: IFitlerCategory) => {
            results.push({ category: this.state.tagsList, id: dep.id + '', name: dep.name });
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
  public getCommonFunctions = (results: ITagResult[]) => {
    // return ReportsApiClient.getCommonFunctions()
    //   .then((res: any) => {
    //     if (res) {
    //       res.data?.forEach((dep: IFitlerCategory) => {
    //         results.push({ category: this.state.commonFunctions, id: dep.id + '', name: dep.name });
    //       });
    //     }
    //   })
    //   .catch((error) => {
    //     this.setState(
    //       {
    //         results: [],
    //       },
    //       () => {
    //         this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
    //       },
    //     );
    //   });
  };

  public getConnectionType = (results: ITagResult[]) => {
    return ReportsApiClient.getConnectionType()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((sf: IFitlerCategory) => {
            results.push({ category: this.state.connectionTypes, id: sf.id + '', name: sf.name });
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
  public getAgileReleaseTrain = (results: ITagResult[]) => {
    return ReportsApiClient.getAgileReleaseTrain()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((sf: IFitlerCategory) => {
            results.push({ category: this.state.agileReleaseTrain, id: sf.id + '', name: sf.name });
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
  public getLevels = (results: ITagResult[]) => {
    return ReportsApiClient.getLevels()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((sf: IFitlerCategory) => {
            results.push({ category: this.state.level, id: sf.id + '', name: sf.name });
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
  public getDatawareHouses = (results: ITagResult[]) => {
    return ReportsApiClient.getDatawareHouses()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((dw: IFitlerCategory) => {
            results.push({
              category: this.state.datawerehouse,
              id: dw.id + '',
              name: dw.name,
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
  // public getDataSource = (results: ITagResult[]) => {
  //   return ReportsApiClient.getDataSource()
  //     .then((res: any) => {
  //       if (res) {
  //         res.data?.forEach((ds: IFitlerCategory) => {
  //           results.push({ category: this.state.dataSource, id: ds.id + '', name: ds.name });
  //         });
  //       }
  //     })
  //     .catch((error) => {
  //       this.setState(
  //         {
  //           results: [],
  //         },
  //         () => {
  //           this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
  //         },
  //       );
  //     });
  // };

  public getReportingCause = (results: ITagResult[]) => {
    return ReportsApiClient.getReportingCause()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((rc: IFitlerCategory) => {
            results.push({ category: this.state.reportingCause, id: rc.id + '', name: rc.name });
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
  public getKpiName = (results: ITagResult[]) => {
    return ReportsApiClient.getKpiName()
      .then((res: any) => {
        if (res) {
          res.data?.forEach((rc: IFitlerCategory) => {
            results.push({ category: this.state.getKpiName, id: rc.id + '', name: rc.name });
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

  public onTagAddCategoryItem = (categoryType: string) => {
    ProgressIndicator.show();
    const isDescriptionDepartment = this.state.itemToAddCategories === 'Description - Departments';
    // re-ensure description sections Department value is in upper case while saving
    const value = isDescriptionDepartment ? this.state.itemToAdd?.toUpperCase()?.trim() : this.state.itemToAdd?.trim();
    const data = {
      data: {
        name: value,
      },
    };
    const requestBody = JSON.parse(JSON.stringify(data));
    return ReportsApiClient.addCategoryItem(categoryType, requestBody)
      .then((res: any) => {
        this.setState(
          {
            addNewItem: false,
          },
          () => {
            this.getResults('add');
            this.showNotification('New Item Added Successfully!');
            this.setState({
              itemToAdd: '',
            });
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            ProgressIndicator.hide();
          },
        );
      });
  };
  public addDataWareHouseInUse = () => {
    ProgressIndicator.show();
    const data = {
      data: {
        ...this.state.datawareHouseItems,
        dataWarehouse: this.state.datawareHouseItems.dataWarehouse?.trim(),
      },
    };
    const requestBody = JSON.parse(JSON.stringify(data));
    return ReportsApiClient.addDataWareHouseInUse(requestBody)
      .then((res: any) => {
        this.setState(
          {
            addNewItem: false,
          },
          () => {
            this.getResults('add');
            this.showNotification('New Item Added Successfully!');
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            ProgressIndicator.hide();
          },
        );
      });
  };

  public onTagUpdateCategoryItem = (categoryType: string) => {
    ProgressIndicator.show();
    const itemToUpdate = this.state.tagToBeUpdated;
    const data = {
      data: {
        id: itemToUpdate.id,
        name: itemToUpdate.name?.trim(),
      },
    };
    const requestBody = JSON.parse(JSON.stringify(data));
    return ReportsApiClient.updateCategoryItem(categoryType, requestBody)
      .then((res: any) => {
        this.setState(
          (prevState) => ({
            addNewItem: false,
            updateConfirmModelOverlay: false,
            tagToBeUpdatedLocal: {
              ...prevState.tagToBeUpdatedLocal,
              name: prevState.tagToBeUpdatedLocal.name?.trim(),
            },
          }),
          () => {
            this.getResults('update');
            this.showNotification('Item Updated Successfully!');
            ProgressIndicator.hide();
          },
        );
      })
      .catch((error) => {
        this.setState(
          {
            updateConfirmModelOverlay: false,
            addNewItem: false,
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            ProgressIndicator.hide();
            this.getResults('update');
          },
        );
      });
  };

  public updateDataWareHouseInUse = (categoryType: string) => {
    ProgressIndicator.show();
    const data = {
      data: {
        ...this.state.datawareHouseItems,
        dataWarehouse: this.state.datawareHouseItems.dataWarehouse?.trim(),
      },
    };
    const requestBody = JSON.parse(JSON.stringify(data));
    return ReportsApiClient.updateCategoryItemDatawareHouseInUse(categoryType, requestBody)
      .then((res: any) => {
        this.setState(
          {
            addNewItem: false,
          },
          () => {
            this.setState(
              {
                addNewItem: false,
              },
              () => {
                this.showNotification('Item Updated Successfully!');
                this.getResults('update');
                ProgressIndicator.hide();
              },
            );
          },
        );
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
            addNewItem: false,
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            this.getResults('update');
            ProgressIndicator.hide();
          },
        );
      });
  };
  public onTagDeleteCategoryItem = (categoryType: string) => {
    ProgressIndicator.show();
    const tagToBeDeleted = this.state.tagToBeDeleted;
    const handleSuccess = () => {
      this.setState({ showDeleteTagModal: false }, () => {
        this.showNotification('Tag deleted successfully');
        this.getResults('delete');
        // ProgressIndicator.hide();
      });
    };
    const handleFailure = () => {
      this.showErrorNotification('Error during tag delete');
      ProgressIndicator.hide();
    };
    return ReportsApiClient.deleteCategoryItem(categoryType, tagToBeDeleted.id)
      .then((res) => {
        if (res) {
          handleSuccess();
        }
      })
      .catch((error) => {
        handleFailure();
      });
  };

  public async getResults(action: string) {
    const showProgressIndicator = ['add', 'update', 'delete'].includes(action);
    const showContentLoader = ['reset', 'categoryChange', 'search', 'pagination'].includes(action);

    showProgressIndicator && ProgressIndicator.show();
    showContentLoader && this.setState({ isResultLoading: true });

    let results: ITagResult[] = [];
    const filterCategory = this.state.currentFilterCategory;
    switch (filterCategory.id) {
      case 0:
        await this.getFronEndTechnologies(results);
        await this.getIntegratedPortal(results);
        await this.getReportingCause(results);
        await this.getKpiName(results);
        await this.getStatuses(results);
        await this.getLegalEntities(results);
        await this.getDepartments(results);
        await this.getDatawareHouses(results);
        // await this.getDataSource(results);
        await this.getConnectionType(results);
        await this.getAgileReleaseTrain(results);
        await this.getLevels(results);
        await this.getTags(results);
        break;

      case 1:
        await this.getDepartments(results);
        break;  
      case 2:
        await this.getFronEndTechnologies(results);
        break;
      case 3:
        await this.getIntegratedPortal(results);
        break;
      case 4:
        await this.getStatuses(results);
        break;
      case 5:
        await this.getAgileReleaseTrain(results); 
        break;
      case 6:
        await this.getTags(results); 
        break;
      case 7:
        await this.getLevels(results);
        break;
      case 8:
        await this.getLegalEntities(results); 
        break;
      case 9:
        await this.getKpiName(results);
        break;
      case 10:
        await this.getReportingCause(results);
        break;
      case 11:
        await this.getDatawareHouses(results);
        break;
      // case 12:
      //   await this.getCommonFunctions(results);
      //   break;  
      // case 13:
      //   await this.getDataSource(results);
      //   break;  
      case 14:
        await this.getConnectionType(results);
        break;  
      case 15:
        await this.getDataClassifications(results);
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
    SelectBox.defaultSetup();
    InputFields.defaultSetup();
    this.setState({ isResultLoading: true });
    await this.getResults('');
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

    if (this.state.dropdownTouched && selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        this.setState(
          {
            currentFilterCategory: { id: parseInt(option.value, 0), name: option.label },
            currentPageOffset: 0,
            currentPageNumber: 1,
            selectedDefaultCat: option.label,
            itemToAddCategories: option.label,
          },
          () => {
            if (this.state.itemToAddCategories !== 'Data & Function - Data warehouse in use') {
              this.setState({
                dataFunctionNotEnable: true,
              });
            }
            this.getResults(option.value === '0' ? 'reset' : 'categoryChange');
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
  public showDeleteConfirmModal = (tagItem: ITagResult) => {
    this.setState({ tagToBeDeleted: tagItem, showDeleteTagModal: true });
  };
  public showUpdateConfirmModal = (tagItem: ITagResult) => {
    this.setState(
      {
        tagToBeUpdated: tagItem,
        itemToUpdate: tagItem.category.name,
        addNewItem: true,
        updateItemMode: true,
        tagToBeUpdatedLocal: tagItem,
        datawareHouseItems: tagItem.datawareHouseItems,
        newItemNameError: null,
        newItemNameCategoryError: null,
      },
      () => {
        if (tagItem.category.name === 'Dataware house in use') {
          this.setState({
            dataFunctionNotEnable: false,
          });
        }
        InputFieldsUtils.resetErrors('#addOrUpdateFormWrapper');
        SelectBox.defaultSetup(true);
      },
    );
  };
  public render() {
    const newItemNameError = this.state.newItemNameError || '';
    const newItemNameCategoryError = this.state.newItemNameCategoryError || '';

    const dataWareHouseNameError = this.state.dataWareHouseNameError || '';
    const requiredError = '*Missing entry';

    const contentForAddNewItem = (
      <div id="addOrUpdateFormWrapper" className={Styles.infoPopup}>
        <div className={classNames(Styles.modalContent, Styles.formWrapperMain)}>
          <div>
            <div
              className={classNames(
                'input-field-group include-error',
                newItemNameCategoryError.length ? 'error' : ' ',
                this.state.updateItemMode ? ' inactive ' : '',
              )}
            >
              <label id="itemCategories" htmlFor="itemCategoriesField" className="input-label">
                Category<sup>*</sup>
              </label>
              <div className="custom-select">
                <select
                  id="itemCategoriesField"
                  onChange={this.onChooseCategories}
                  required={true}
                  required-error={requiredError}
                  value={this.state.selectedDefaultCat}
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
              <span className={classNames('error-message', newItemNameCategoryError.length ? '' : 'hide')}>
                {newItemNameCategoryError}
              </span>
            </div>
          </div>
          {this.state.dataFunctionNotEnable && (
            <React.Fragment>
              <div
                id="reportDecsription"
                className={classNames('input-field-group include-error ', newItemNameError.length ? 'error' : '')}
              >
                <label id="itemName" htmlFor="itemNameInput" className="input-label">
                  Item Name<sup>*</sup>
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
                    value={this.state.itemToAdd}
                  />
                )}
                <span className={classNames('error-message', newItemNameError.length ? '' : 'hide')}>
                  {newItemNameError}
                </span>
              </div>
            </React.Fragment>
          )}
          {!this.state.dataFunctionNotEnable && (
            <React.Fragment>
              <div className={classNames(Styles.flexLayout)}>
                <TextBox
                  type="text"
                  controlId={'carlaInputField'}
                  labelId={'ApplicationName'}
                  label={'Data warehouse'}
                  placeholder={'Type here'}
                  value={this.state.datawareHouseItems?.dataWarehouse}
                  errorText={dataWareHouseNameError}
                  required={true}
                  maxLength={200}
                  onChange={this.onDatawarehouseInuse}
                />
              </div>
              <div className={classNames(Styles.flexLayout)}>
                {/* <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
                  <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
                    <div id="tagsContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
                      <div>
                        <Tags
                          tags={undefined}
                          title={'Commom Function'}
                          max={100}
                          chips={this.state.datawareHouseItems?.commonFunctions}
                          setTags={this.setCommonFnTags}
                          isMandatory={true}
                          showMissingEntryError={this.state.showCommanFnTagsMissingError}
                          {...this.props}
                        />
                      </div>
                    </div>
                  </div>
                </div> */}
                <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
                  <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
                    <div id="tagsContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
                      <div>
                        <Tags
                          tags={undefined}
                          title={'Connection Types'}
                          max={100}
                          chips={[this.state.datawareHouseItems?.connectionType]}
                          setTags={this.setConnectionTypesTags}
                          isMandatory={true}
                          showMissingEntryError={this.state.showConnectionTypesTagsMissingError}
                          {...this.props}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </React.Fragment>
          )}
          <div className={Styles.addBtn}>
            <button
              onClick={this.state.updateItemMode ? this.updateConfirmModelOverlayUpdate : this.onTagAddItem}
              className={Styles.actionBtn + ' btn btn-tertiary'}
              type="button"
            >
              {this.state.updateItemMode ? <span>Update</span> : <span>Add</span>}
            </button>
          </div>
        </div>
        {this.state.updateConfirmModelOverlay && (
          <div className={Styles.updateModelOverlayContent}>
            <p>
              Updating &lt;&lt; {this.state.tagToBeUpdatedLocal.name} &gt;&gt; would also update all the associated
              solutions. <br /> Do you want to proceed?
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
    const modalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Tag</div>
        <div className={Styles.modalContent}>
          The tag &laquo;{this.state.tagToBeDeleted ? this.state.tagToBeDeleted.name : ''}&raquo; will be removed
          permanently.
        </div>
      </div>
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
                  <div
                    className={`custom-select ${this.state.isResultLoading ? 'disabled' : ''}`}
                    onFocus={() => this.setState({ dropdownTouched: true })}
                  >
                    <select id="filterBy" onChange={this.onCategoryChange}>
                      {/* {this.state.categories && */}
                      {this.state.itemCategories.map((category: IFitlerCategory) => (
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
                >
                  <button onClick={this.onAddItemModalOpen}>
                    <i className="icon mbc-icon plus" />
                    <span>Add New Item</span>
                  </button>
                </div>
              </div>
            </div>
            <div className={Styles.exportLinkWrapper}>
              <div>
                <a
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    ProgressIndicator.show();
                    ReportsApiClient.exportJSON()
                      .then((res) => {
                        if (res.data?.reports?.records?.length) {
                          const url = window.URL.createObjectURL(new Blob([JSON.stringify(res.data?.reports.records)]));
                          const d = new Date();
                          const date = `${d.getMonth() + 1}_${d.getDate()}_${d.getFullYear()}`;
                          const time = `${d.getHours()}_${d.getMinutes()}_${d.getSeconds()}`;
                          const link = document.createElement('a');
                          link.download = `Report-Data-Dump-[${date}-${time}].json`;
                          link.href = url;
                          link.click();
                        } else {
                          Notification.show('No records to export.', 'alert');
                        }
                        ProgressIndicator.hide();
                      })
                      .catch((e) => {
                        ProgressIndicator.hide();
                        Notification.show('Error downloading attachment. Please try again later.', 'alert');
                      });
                  }}
                >
                  <i className="icon download" />
                  Export Report Data as JSON
                </a>
              </div>
            </div>
          </div>
          {!this.state.isResultLoading ? (
            resultData.length === 0 ? (
              <div className={Styles.tagIsEmpty}>There is no tag available</div>
            ) : (
              <div className={Styles.tablePanel}>
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
                      <th>
                        <label>Action</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>{resultData}</tbody>
                </table>
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
            )
          ) : (
            <div className={classNames('text-center', Styles.spinner)}>
              <div className="progress infinite" />
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
            title={this.state.updateItemMode ? 'Update Item' : 'Add New Item'}
            modalWidth={'35vw'}
            show={this.state.addNewItem}
            content={contentForAddNewItem}
            onCancel={this.onAddItemModalCancel}
          />
        </div>
      </div>
    );
  }
  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteTagModal: false });
  };
  protected onCancellingUpdateChanges = () => {
    this.setState({ showUpdateTagModal: false });
  };
  protected onAcceptDeleteChanges = () => {
    const tagToBeDeleted = this.state.tagToBeDeleted;
    switch (tagToBeDeleted.category.id) {
      case 1:
        this.onTagDeleteCategoryItem('departments');
        break;
      case 2:
        this.onTagDeleteCategoryItem('frontendtechnologies');
        break;
      case 3:
        this.onTagDeleteCategoryItem('integratedportals');
        break;
      case 4:
        this.onTagDeleteCategoryItem('statuses');
        break;
      case 5:
        this.onTagDeleteCategoryItem('agilereleasetrains');
        break; 
      case 6:
        this.onTagDeleteCategoryItem('tags');
        break;
      case 7:
        this.onTagDeleteCategoryItem('levels');
        break;
      case 8:
        this.onTagDeleteCategoryItem('legalentities');
        break;
      case 9:
        this.onTagDeleteCategoryItem('kpinames');
        break;
      case 10:
        this.onTagDeleteCategoryItem('reportingcauses');
        break;
      case 11:
        this.onTagDeleteCategoryItem('datawarehouses');
        break;
      // case 12:
      //   this.onTagDeleteCategoryItem('commonfunctions');
      //   break;  
      // case 13:
      //   this.onTagDeleteCategoryItem('datasources');
      //   break;
      case 14:
        this.onTagDeleteCategoryItem('connectiontypes');
        break;
      case 15:
        this.onTagDeleteCategoryItem('dataclassifications');
        break;
      default:
        break;
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

  protected addItemFormValidation = () => {
    let formValidationStatus = true;
    const errorMissingEntry = '*Missing entry';
    if (this.state.itemToAdd === '') {
      this.setState({
        newItemNameError: errorMissingEntry,
      });
      formValidationStatus = false;
    }
    if (this.state.itemToAddCategories === 'Select' || this.state.itemToAddCategories === '') {
      this.setState({
        newItemNameCategoryError: errorMissingEntry,
      });
      formValidationStatus = false;
    }
    return formValidationStatus;
  };

  protected updateItemFormValidation = () => {
    let formValidationStatus = true;
    const errorMissingEntry = '*Missing entry';
    if (this.state.tagToBeUpdatedLocal.name === '') {
      this.setState({
        newItemNameError: errorMissingEntry,
      });
      formValidationStatus = false;
    }
    return formValidationStatus;
  };
  protected addDatawarehouseItemFormValidation = () => {
    let formValidationStatus = true;
    const errorMissingEntry = '*Missing entry';
    if (this.state.datawareHouseItems.dataWarehouse === '') {
      this.setState({
        dataWareHouseNameError: errorMissingEntry,
      });
      formValidationStatus = false;
    } else {
      this.setState({
        dataWareHouseNameError: '',
      });
    }
    // if (!this.state.datawareHouseItems.commonFunctions.length) {
    //   this.setState({
    //     showCommanFnTagsMissingError: true,
    //   });
    //   formValidationStatus = false;
    // } else {
    //   this.setState({
    //     showCommanFnTagsMissingError: false,
    //   });
    // }
    if (!this.state.datawareHouseItems.connectionType.length) {
      this.setState({
        showConnectionTypesTagsMissingError: true,
      });
      formValidationStatus = false;
    } else {
      this.setState({
        showConnectionTypesTagsMissingError: false,
      });
    }
    return formValidationStatus;
  };
  protected onTagAddItem = () => {
    this.setState({
      updateConfirmModelOverlay: false,
    });
    const itemToAddCategories = this.state.itemToAddCategories;
    if (this.addItemFormValidation()) {
      if (itemToAddCategories === 'Description - Front End Technologies') {
        this.onTagAddCategoryItem('frontendtechnologies');
      } else if (itemToAddCategories === 'Description - Integrated In Portal') {
        this.onTagAddCategoryItem('integratedportals');
      } else if (itemToAddCategories === 'Kpi - Reporting Cause') {
        this.onTagAddCategoryItem('reportingcauses');
      } else if (itemToAddCategories === 'Description - Statuses') {
        this.onTagAddCategoryItem('statuses');
      } else if (itemToAddCategories === 'Customers - MB Legal Entities') {
        this.onTagAddCategoryItem('legalentities');
      } else if (itemToAddCategories === 'E2-Departments') {
        this.onTagAddCategoryItem('departments');
      } else if (itemToAddCategories === 'Data & Function - Single Data Source - Data Source') {
        this.onTagAddCategoryItem('datasources');
      } else if (itemToAddCategories === 'Data & Function - Connection Types') {
        this.onTagAddCategoryItem('connectiontypes');
      } else if (itemToAddCategories === 'Description - Agile Release Train') {
        this.onTagAddCategoryItem('agilereleasetrains');
      } else if (itemToAddCategories === 'Description - Tags') {
        this.onTagAddCategoryItem('tags');
      } else if (itemToAddCategories === 'Customer - Levels') {
        this.onTagAddCategoryItem('levels');
      } else if (itemToAddCategories === 'Kpi - KPI Name') {
        this.onTagAddCategoryItem('kpinames');
      } else if (itemToAddCategories === 'Data & Function - Data Warehouse - Data Warehouse') {
        this.onTagAddCategoryItem('datawarehouses');
      // } else if (itemToAddCategories === 'Data & Function - Data Warehouse - Common Functions') {
      //   this.onTagAddCategoryItem('commonfunctions');
      } else if (itemToAddCategories === 'Data & Function - Data Classifications') {
        this.onTagAddCategoryItem('dataclassifications');
      }
    }
  };

  protected onTagUpdateItem = () => {
    this.setState({
      updateConfirmModelOverlay: false,
    });
    const itemToUpdate = this.state.tagToBeUpdated;
    if (this.updateItemFormValidation()) {
      switch (itemToUpdate.category.id) {
        case 1:
          this.onTagUpdateCategoryItem('departments');
          break;
        case 2:
          this.onTagUpdateCategoryItem('frontendtechnologies');
          break;
        case 3:
          this.onTagUpdateCategoryItem('integratedportals');
          break;
        case 4:
          this.onTagUpdateCategoryItem('statuses');
          break;
        case 5:
          this.onTagUpdateCategoryItem('agilereleasetrains');
          break;        
        case 6:
          this.onTagUpdateCategoryItem('tags');
          break;
        case 7:
          this.onTagUpdateCategoryItem('levels');
          break;
        case 8:
          this.onTagUpdateCategoryItem('legalentities');
          break;
        case 9:
          this.onTagUpdateCategoryItem('kpinames');
          break;
        case 10:
          this.onTagUpdateCategoryItem('reportingcauses');
          break;
        case 11:
          this.onTagUpdateCategoryItem('datawarehouses');
          break;
        // case 12:
        //   this.onTagUpdateCategoryItem('commonfunctions');
        //   break;  
        // case 13:
        //   this.onTagUpdateCategoryItem('datasources');
        //   break;
        case 14:
          this.onTagUpdateCategoryItem('connectiontypes');
          break;
        case 15:
          this.onTagUpdateCategoryItem('dataclassifications');
          break;
        default:
          break;
      }
    }
    
  };
  protected updateConfirmModelOverlayCancel = () => {
    this.setState({
      updateConfirmModelOverlay: false,
    });
  };
  protected updateConfirmModelOverlayUpdate = () => {
    this.setState({
      updateConfirmModelOverlay: true,
    });
  };
  protected onChangeTagAddItem = (e: React.FormEvent<HTMLInputElement>) => {
    const isDescriptionDepartment = this.state.itemToAddCategories === 'Description - Departments';
    this.setState(
      {
        itemToAdd: isDescriptionDepartment ? e.currentTarget.value?.toUpperCase() : e.currentTarget.value,
      },
      () => {
        if (this.state.itemToAdd.length > 0) {
          this.setState({
            newItemNameError: null,
          });
        } else {
          this.setState({
            newItemNameError: '*Missing entry',
          });
        }
      },
    );
  };
  protected onChangeUpdateItem = (e: React.FormEvent<HTMLInputElement>) => {
    const isDescriptionDepartment = this.state.itemToAddCategories === 'Description - Departments';
    const tagToBeUpdatedLocal = this.state.tagToBeUpdatedLocal;
    tagToBeUpdatedLocal.name = isDescriptionDepartment ? e.currentTarget.value.toUpperCase() : e.currentTarget.value;
    this.setState({
      tagToBeUpdatedLocal,
    });
  };
  protected onChooseCategories = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const changeCatVal = e.currentTarget.value;
    this.setState({
      selectedDefaultCat: changeCatVal,
      itemToAddCategories: changeCatVal,
      itemToAdd: '',
    });
    if (changeCatVal == 'Data & Function - Data warehouse in use') {
      this.setState(
        {
          datawerehouseinuse: changeCatVal,
        },
        () => {
          this.setState({
            dataFunctionNotEnable: false,
            datawerehouseinuse: changeCatVal,
          });
        },
      );
    } else {
      this.setState({
        dataFunctionNotEnable: true,
      });
    }
  };
  protected onAddItemModalOpen = () => {
    const enableDataAndFunction = this.state.itemToAddCategories !== 'Data & Function - Data warehouse in use';
    this.setState(
      {
        addNewItem: true,
        updateItemMode: false,
        dataFunctionNotEnable: enableDataAndFunction,
        datawareHouseItems: {
          id: '',
          dataWarehouse: '',
          // commonFunctions: [],
          dataClassification: '',
          connectionType: '',
        },
        newItemNameError: null,
        newItemNameCategoryError: null,
      },
      () => {
        InputFieldsUtils.resetErrors('#addOrUpdateFormWrapper');
        SelectBox.defaultSetup(true);
      },
    );
  };
  protected onAddItemModalCancel = () => {
    this.setState({
      addNewItem: false,
      newItemNameError: null,
      newItemNameCategoryError: null,
      itemToAdd: '',
      tagToBeUpdated: {
        id: '',
        name: '',
        category: {
          id: -1,
          name: '',
        },
      },
      tagToBeUpdatedLocal: {
        id: '',
        name: '',
        category: {
          id: -1,
          name: '',
        },
      }
    });
  };
}
