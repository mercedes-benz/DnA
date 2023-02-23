import cn from 'classnames';
import * as React from 'react';
import Styles from './DataFunction.scss';
import {
  IDataAndFunctions,
  IDataSourceMaster,
  IConnectionType,
  IDataWarehouse,
  ICommonFunctions,
  IDataWarehouseInUse,
  ISingleDataSources,
  IDataClassification,
  IDataSources,
} from 'globals/types';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Modal from 'components/formElements/modal/Modal';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { ErrorMsg } from 'globals/Enums';
import { SingleDataSourceList } from './SingleDataSourceList';
import { DataWarehouseList } from './DataWarehouseList';
import { DataWarehouse } from './DataFunctionModal/DataWarehouse';
import { SingleDataSource } from './DataFunctionModal/SingleDataSource';
import { IconDataSource } from 'components/icons/IconIDataSource';

const classNames = cn.bind(Styles);
export interface IDataWarehouseErrors {
  commonFunctions: string;
  connectionType: string;
  dataWarehouse: string;
  dataClassification: string;
}

interface IDataWarehouseDropdownValues {
  commonFunctions: string[];
  dataClassifications: string[];
  connectionTypes: string[];
}
export interface ISingleDataSourceErrors {
  connectionType: string;
  dataSources: string;
  dataClassification: string;
}
export interface IDataFunctionProps {
  dataAndFunctions: IDataAndFunctions;
  dataSources: IDataSourceMaster[];
  connectionTypes: IConnectionType[];
  dataClassifications: IDataClassification[];
  dataWarehouses: IDataWarehouse[];
  commonFunctions: ICommonFunctions[];
  modifyDataFunction: (modifyDataFunction: IDataAndFunctions) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
}
export interface IDataAndFunctionsState {
  dataAndFunctions: IDataAndFunctions;
  dataWarehouseInUseInfo: IDataWarehouseInUse;
  singleDataSourceInfo: ISingleDataSources;
  dataWarehouseDropdownValues: IDataWarehouseDropdownValues;
  errors: IDataWarehouseErrors;
  singleDataSourceErrors: ISingleDataSourceErrors;
  dataSource: string;
  addDataSource: boolean;
  editDataSource: boolean;
  editDataSourceIndex: number;
  duplicateDataSouceAdded: boolean;
  currentColumnToSort: string;
  currentSortOrder: string;
  nextSortOrder: string;
  dataAndFunctionTabError: string;
  showDeleteModal: boolean;
  isDataWarehouseSection: boolean;
  dataSources: IDataSources[];

  isSingleDataSourceContextMenuOpened: boolean;
  isDataWarehouseContextMenuOpened: boolean;
}
export interface IDataFunction {
  carLaPlatform: string;
  otherDataFunction: string;
  commonFunctions: string[];
  specificFunctions: string[];
  queries: string[];
  daraSources: string[];
}
export default class DataFunction extends React.Component<IDataFunctionProps, IDataAndFunctionsState> {
  public static getDerivedStateFromProps(props: IDataFunctionProps, state: IDataAndFunctionsState) {
    return {
      dataAndFunctions: props.dataAndFunctions,
    };
  }

  constructor(props: any) {
    super(props);
    this.state = {
      dataAndFunctions: {
        dataWarehouseInUse: [],
        singleDataSources: [],
      },
      dataWarehouseInUseInfo: {
        dataWarehouse: '',
        connectionType: '',
        commonFunctions: [],
        dataClassification: ''
      },
      dataWarehouseDropdownValues: {
        commonFunctions: [],
        connectionTypes: [],
        dataClassifications: []
      },
      singleDataSourceInfo: {
        connectionType: '',
        dataSources: [],
        dataClassification: '',
      },
      singleDataSourceErrors: {
        connectionType: '',
        dataSources: '',
        dataClassification: '',
      },
      errors: {
        dataWarehouse: '',
        connectionType: '',
        commonFunctions: '',
        dataClassification: ''
      },
      dataSource: 'datawarehouse',
      addDataSource: false,
      editDataSource: false,
      editDataSourceIndex: -1,
      duplicateDataSouceAdded: false,
      currentColumnToSort: 'name',
      currentSortOrder: 'desc',
      nextSortOrder: 'asc',
      dataAndFunctionTabError: '',
      showDeleteModal: false,
      isDataWarehouseSection: false,
      dataSources: [],
      isDataWarehouseContextMenuOpened: false,
      isSingleDataSourceContextMenuOpened: false
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }

  public componentDidUpdate(props: any, state: any) {
    if (this.state.dataWarehouseInUseInfo.dataWarehouse !== state.dataWarehouseInUseInfo.dataWarehouse) {
      SelectBox.defaultSetup();
    }
  }

  public resetChanges = () => {
    // if (this.props.dataAndFunctions) {
    //   this.setState({
    //     dataAndFunctions: this.props.dataAndFunctions,
    //   });
    // }
  };

  protected onDataFunction = () => {
    if (this.validateDataAndFunctionTab()) {
      this.props.modifyDataFunction(this.state.dataAndFunctions);
      this.props.onSaveDraft('datafunction');
    }
  };

  protected validateDataAndFunctionTab = () => {
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    (!dataWarehouseInUse?.length || !singleDataSources?.length) &&
      this.setState({ dataAndFunctionTabError: ErrorMsg.DATAFUNCTION_TAB });
    return dataWarehouseInUse?.length || singleDataSources?.length;
  };

  protected handleRadio = (e: React.ChangeEvent<HTMLInputElement>) => {
    // this.setState(
    //   (prevState) => ({
    //     dataSource: e.target.value,
    //     dataWarehouseInUseInfo: {
    //       ...prevState.dataWarehouseInUseInfo,
    //       dataWarehouse: '',
    //     },
    //   }),
    //   () => {
    //     SelectBox.defaultSetup();
    //   },
    // );
    this.setState(
      (prevState) => ({
        dataSource: e.target.value,
      }),
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    // const name = e.target.name;
    // const value = e.target.value;
    // const isEmpty = e.target.selectedOptions[e.target.selectedIndex]?.label === 'Choose';
    // isEmpty
    //   ? this.setState({
    //       dataWarehouseDropdownValues: {
    //         commonFunctions: [],
    //         connectionTypes: [],
    //         dataClassifications: []
    //       },
    //     })
    //   : this.props.dataWarehouses?.map((item) => {
    //       if (item.dataWarehouse.toLowerCase() === value.toLowerCase()) {
    //         // const isCarla = item.dataWarehouse.toLowerCase() === 'carla';
    //         // const connectionTypes =
    //         //   isCarla && item.connectionTypes?.filter((item) => item.toLowerCase() === 'live connection')?.length
    //         //     ? item.connectionTypes
    //         //     : isCarla
    //         //     ? [...item.connectionTypes, 'live connection']
    //         //     : item.connectionTypes;
    //         // const dataClassifications = this.props.dataClassifications.map((item: IDataClassification)=> item.name);    
    //         // this.setState((prevState) => ({
    //         //   dataWarehouseDropdownValues: {
    //         //     ...prevState.dataWarehouseDropdownValues,
    //         //     commonFunctions: item.commonFunctions,
    //         //     connectionTypes: connectionTypes,
    //         //     dataClassifications: dataClassifications
    //         //   },
    //         //   dataWarehouseInUseInfo: {
    //         //     ...prevState.dataWarehouseInUseInfo,
    //         //     [name]: value,
    //         //     ...(prevState.dataWarehouseInUseInfo?.dataWarehouse && { connectionTypes: [] }), //reset if value is not carLa
    //         //   },
    //         //   errors: {
    //         //     ...prevState.errors,
    //         //     connectionTypes: '',
    //         //   },
    //         // }));
    //       }
    //     });
  };

  protected showDataSourceModal = () => {
    this.setState(
      {
        addDataSource: true,
        dataSource: 'datawarehouse',
      },
      () => {
        SelectBox.defaultSetup(true);
      },
    );
  };

  protected handleModalClose = () => {
    this.setState({
      addDataSource: false,
      editDataSource: false,
      duplicateDataSouceAdded: false,
      dataWarehouseInUseInfo: {
        dataWarehouse: '',
        connectionType: '',
        commonFunctions: [],
        dataClassification: ''
      },
      errors: {
        dataWarehouse: '',
        connectionType: '',
        commonFunctions: '',
        dataClassification: ''
      },
      singleDataSourceInfo: {
        connectionType: '',
        dataSources: [],
        dataClassification: '',
      },
      singleDataSourceErrors: {
        connectionType: '',
        dataSources: '',
        dataClassification: ''
      },
    });
  };

  protected onAddDatasourceNew = () => {
    const {
      connectionType: dataWarehouseConnectionTypes,
      commonFunctions,
      dataClassification: dataWarehouseDataClassification,
      dataWarehouse,
    } = this.state.dataWarehouseInUseInfo;
    const { connectionType, dataSources, dataClassification } = this.state.singleDataSourceInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    const isDataWarehouse = this.state.dataSource === 'datawarehouse';
    const selectedValues: any = [];
    isDataWarehouse
      ? selectedValues.push({
          dataWarehouse,
          connectionType: dataWarehouseConnectionTypes,
          commonFunctions,
          dataClassification: dataWarehouseDataClassification,
        })
      : selectedValues.push({
          connectionType,
          dataSources: dataSources,
          dataClassification,
        });
    
    if (this.validateDatasourceObject()) {
      this.props.modifyDataFunction({
        dataWarehouseInUse: isDataWarehouse ? [...dataWarehouseInUse, ...selectedValues] : dataWarehouseInUse,
        singleDataSources: !isDataWarehouse ? [...singleDataSources, ...selectedValues] : singleDataSources,
      });
      this.setState(
        (prevState: any) => ({
          addDataSource: false,
          duplicateDataSouceAdded: false,
          dataAndFunctions: {
            ...prevState.dataAndFunctions,
            dataWarehouseInUse: isDataWarehouse
              ? [...prevState.dataAndFunctions.dataWarehouseInUse, ...selectedValues]
              : prevState.dataAndFunctions.dataWarehouseInUse,
            singleDataSources: !isDataWarehouse
              ? [...prevState.dataAndFunctions.singleDataSources, ...selectedValues]
              : prevState.dataAndFunctions.singleDataSources,
          },
          dataWarehouseInUseInfo: {
            dataWarehouse: '',
            connectionType: '',
            commonFunctions: [],
            dataClassification: '',
          },
          singleDataSourceInfo: {
            connectionType: '',
            dataSources: [],
            dataClassification: '',
          },
          singleDataSourceErrors: {
            connectionType: '',
            dataSources: '',
            dataClassification: ''
          },
          errors: {
            dataWarehouse: '',
            connectionType: '',
            commonFunctions: '',
            dataClassification: ''
          },
          dataSource: '',
        }),
        () => {
          ExpansionPanel.defaultSetup();
          Tooltip.defaultSetup();
        },
      );
    }
  }

  protected onAddDatasource = () => {
    // const {
    //   connectionType: dataWarehouseConnectionTypes,
    //   commonFunctions,
    //   dataClassification: dataWarehouseDataClassification,
    //   dataWarehouse,
    // } = this.state.dataWarehouseInUseInfo;
    // const { connectionType, dataSource, dataClassification } = this.state.singleDataSourceInfo;
    // const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    // const isDataWarehouse = this.state.dataSource === 'datawarehouse';

    // const selectedValues: any = [];
    // isDataWarehouse
    //   ? selectedValues.push({
    //       dataWarehouse,
    //       connectionType: dataWarehouseConnectionTypes,
    //       commonFunctions,
    //       dataClassification: dataWarehouseDataClassification
    //     })
    //   : selectedValues.push({
    //       connectionType,
    //       dataSources: dataSource, // this toString will be removed once object is getting captured
    //       dataClassification,
    //     });

  //   if (this.validateDatasourceModal()) {
  //     this.props.modifyDataFunction({
  //       dataWarehouseInUse: isDataWarehouse ? [...dataWarehouseInUse, ...selectedValues] : dataWarehouseInUse,
  //       singleDataSources: !isDataWarehouse ? [...singleDataSources, ...selectedValues] : singleDataSources,
  //     });
  //     this.setState(
  //       (prevState: any) => ({
  //         addDataSource: false,
  //         duplicateDataSouceAdded: false,
  //         dataAndFunctions: {
  //           ...prevState.dataAndFunctions,
  //           dataWarehouseInUse: isDataWarehouse
  //             ? [...prevState.dataAndFunctions.dataWarehouseInUse, ...selectedValues]
  //             : prevState.dataAndFunctions.dataWarehouseInUse,
  //           singleDataSources: !isDataWarehouse
  //             ? [...prevState.dataAndFunctions.singleDataSources, ...selectedValues]
  //             : prevState.dataAndFunctions.singleDataSources,
  //         },
  //         dataWarehouseInUseInfo: {
  //           dataWarehouse: '',
  //           connectionTypes: [],
  //           commonFunctions: [],
  //           dataFunction: [],
  //           dataClassification: ''
  //         },
  //         dataWarehouseDropdownValues: {
  //           commonFunctions: [],
  //           connectionTypes: [],
  //           dataClassifications: []
  //         },
  //         singleDataSourceInfo: {
  //           connectionType: '',
  //           dataSource: '[]',
  //           dataClassification: '',
  //         },
  //         singleDataSourceErrors: {
  //           connectionType: '',
  //           dataSources: '',
  //           dataClassification: ''
  //         },
  //         errors: {
  //           dataWarehouse: '',
  //           connectionTypes: '',
  //           commonFunctions: '',
  //           dataClassification: ''
  //         },
  //         dataSource: '',
  //       }),
  //       () => {
  //         ExpansionPanel.defaultSetup();
  //         Tooltip.defaultSetup();
  //       },
  //     );
  //   }
  };

  protected onEditDatasourceOpen = (dataSourcesAndFunctions: IDataWarehouseInUse, index: number) => {
    const { connectionType, commonFunctions, dataClassification, dataWarehouse } =
      dataSourcesAndFunctions;
    this.setState(
      {
        addDataSource: false,
        editDataSource: true,
        editDataSourceIndex: index,
        dataWarehouseInUseInfo: {
          dataWarehouse,
          connectionType,
          commonFunctions,
          dataClassification,
        },
        dataSource: 'datawarehouse',
      },
      () => {
        SelectBox.defaultSetup(true);
      },
    );
  };

  protected onDeleteDatasource = (isDataWarehouse: boolean, index: number) => {
    this.setState({
      showDeleteModal: true,
      isDataWarehouseSection: isDataWarehouse,
      editDataSourceIndex: index,
    });
  };

  protected onEditDatasource = () => {
    const { editDataSourceIndex } = this.state;
    const { connectionType, commonFunctions, dataClassification, dataWarehouse } =
      this.state.dataWarehouseInUseInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    if (this.validateDatasourceObject()) {
      const dataSourceList = [...dataWarehouseInUse]; // create copy of original array
      dataSourceList[editDataSourceIndex] = {
        connectionType,
        commonFunctions,
        dataClassification,
        dataWarehouse,
      }; // modify copied array

      this.props.modifyDataFunction({
        dataWarehouseInUse: dataSourceList,
        singleDataSources,
      });
      this.setState({
        editDataSource: false,
        duplicateDataSouceAdded: false,
        dataAndFunctions: {
          dataWarehouseInUse: dataSourceList,
          singleDataSources,
        },
        dataWarehouseInUseInfo: {
          dataWarehouse: '',
          connectionType: '',
          commonFunctions: [],
          dataClassification: '',
        },
        errors: {
          dataWarehouse: '',
          connectionType: '',
          commonFunctions: '',
          dataClassification: ''
        },
        dataSource: 'datawarehouse',
      });
    }
  };

  protected onEditSingleDataSourceOpen = (dataSourcesAndFunctions: ISingleDataSources, index: number) => {
    const { connectionType, dataClassification, dataSources } = dataSourcesAndFunctions;
    this.setState(
      {
        addDataSource: false,
        editDataSource: true,
        editDataSourceIndex: index,
        singleDataSourceInfo: {
          connectionType,
          dataClassification,
          dataSources,
        },
        dataSource: 'singledatasource',
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected onDeleteSingleDatasource = (isDataWarehouseSection: boolean, index: number) => {
    this.setState({
      showDeleteModal: true,
      isDataWarehouseSection: isDataWarehouseSection,
      editDataSourceIndex: index,
    });
  };

  protected onEditSingleDataSource = () => {
    const { editDataSourceIndex } = this.state;
    const { connectionType, dataSources, dataClassification } = this.state.singleDataSourceInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    if (this.validateDatasourceModal()) {
      const dataSourceList = [...singleDataSources]; // create copy of original array
      dataSourceList[editDataSourceIndex] = { connectionType, dataSources, dataClassification }; // modify copied array

      this.props.modifyDataFunction({
        dataWarehouseInUse,
        singleDataSources: dataSourceList,
      });
      this.setState({
        editDataSource: false,
        duplicateDataSouceAdded: false,
        dataAndFunctions: {
          dataWarehouseInUse,
          singleDataSources: dataSourceList,
        },
        singleDataSourceInfo: {
          connectionType: '',
          dataSources: [],
          dataClassification: '',
        },
        singleDataSourceErrors: {
          connectionType: '',
          dataSources: '',
          dataClassification: ''
        },
        dataSource: 'singledatasource',
      });
    }
  };

  public onDataSource = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const value = e.currentTarget.value;
    this.setState((prevState) => ({
      ...prevState,
      dataWarehouseInUseInfo: {
        ...prevState.dataWarehouseInUseInfo,
        [name]: value,
      }
    }),
    () => {

      },
    );
  };

  public onCommonFunctionsChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const selectedValue:string[] = [];
    const selectedOptions = e.currentTarget.selectedOptions;
    Array.from(selectedOptions).forEach((option) => {
      selectedValue.push(option.value);
    });
    this.setState((prevState) => ({
      ...prevState,
      dataWarehouseInUseInfo: {
        ...prevState.dataWarehouseInUseInfo,
        [name]: selectedValue,
      }
    }),
    () => {
        
      },
    );
  };

  public onChangeSingleDataSource = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const value = e.currentTarget.value;

    this.setState((prevState) => ({
      ...{
            ...prevState,
            singleDataSourceInfo: {
              ...prevState.singleDataSourceInfo,
              [name]: value,
            },
          },
    }));
  };

  public setDataSources = (arr: string[]) => {
    const dataSources: IDataSources[] = [];
    arr.forEach((element) => {
      dataSources.push({ dataSource: element, weightage: 0 });
    });
    const singleDataSourceInfo = this.state.singleDataSourceInfo;
    singleDataSourceInfo.dataSources = dataSources;
    const singleDataSourceErrors = this.state.singleDataSourceErrors;
    singleDataSourceErrors.dataSources = dataSources.length ? '' : '*Missing entry';
    this.setState({dataSources, singleDataSourceInfo, singleDataSourceErrors});
  }

  protected validateDatasourceObject = () => {
    let formValid = true;
    const errors = this.state.errors;
    const singleDataSourceErrors = this.state.singleDataSourceErrors;
    const errorMissingEntry = '*Missing entry';

    if (this.state.dataSource === 'datawarehouse') {
      if (!this.state.dataWarehouseInUseInfo.dataWarehouse) {
        errors.dataWarehouse = errorMissingEntry;
        formValid = false;
      }

      // if (!this.state.dataWarehouseInUseInfo.commonFunctions?.length) {
      //   errors.commonFunctions = errorMissingEntry;
      //   formValid = false;
      // }

      if (
        !this.state.dataWarehouseInUseInfo.dataClassification ||
        this.state.dataWarehouseInUseInfo.dataClassification === 'Choose'
      ) {
        errors.dataClassification = errorMissingEntry;
        formValid = false;
      }

      // if (
      //   !this.state.dataWarehouseInUseInfo.connectionType ||
      //   this.state.dataWarehouseInUseInfo.connectionType === 'Choose'
      // ) {
      //   errors.connectionType = errorMissingEntry;
      //   formValid = false;
      // }
    } else {
      // if (
      //   !this.state.singleDataSourceInfo.connectionType ||
      //   this.state.singleDataSourceInfo.connectionType === 'Choose'
      // ) {
      //   singleDataSourceErrors.connectionType = errorMissingEntry;
      //   formValid = false;
      // }
      if (
        !this.state.singleDataSourceInfo.dataClassification ||
        this.state.singleDataSourceInfo.dataClassification === 'Choose'
      ) {
        singleDataSourceErrors.dataClassification = errorMissingEntry;
        formValid = false;
      }
      if (this.state.singleDataSourceInfo.dataSources?.length === 0) {
        singleDataSourceErrors.dataSources = errorMissingEntry;
        formValid = false;
      }
    }
    

    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ errors });
    return formValid;
  };

  protected validateDatasourceModal = () => {
    let formValid = true;
    const errors = this.state.errors;
    const singleDataSourceErrors = this.state.singleDataSourceErrors;
    const errorMissingEntry = '*Missing entry';

    if (this.state.dataSource === 'datawarehouse') {
      if (!this.state.dataWarehouseInUseInfo.dataWarehouse) {
        errors.dataWarehouse = errorMissingEntry;
        formValid = false;
      }

      if (!this.state.dataWarehouseInUseInfo.commonFunctions?.length) {
        errors.commonFunctions = errorMissingEntry;
        formValid = false;
      }

      // if (!this.state.dataWarehouseInUseInfo.dataSources?.length) {
      //   errors.dataSources = errorMissingEntry;
      //   formValid = false;
      // }

      // if (!this.state.dataWarehouseInUseInfo.queries?.length) {
      //   errors.queries = errorMissingEntry;
      //   formValid = false;
      // }

      // if (!this.state.dataWarehouseInUseInfo.specificFunctions?.length) {
      //   errors.specificFunctions = errorMissingEntry;
      //   formValid = false;
      // }

      if (
        !this.state.dataWarehouseInUseInfo.dataClassification ||
        this.state.dataWarehouseInUseInfo.dataClassification === 'Choose'
      ) {
        errors.dataClassification = errorMissingEntry;
        formValid = false;
      }

      if (
        !this.state.dataWarehouseInUseInfo.connectionType ||
        this.state.dataWarehouseInUseInfo.connectionType === 'Choose'
      ) {
        errors.connectionType = errorMissingEntry;
        formValid = false;
      }
    } else {
      // if (
      //   !this.state.singleDataSourceInfo.connectionType ||
      //   this.state.singleDataSourceInfo.connectionType === 'Choose'
      // ) {
      //   singleDataSourceErrors.connectionType = errorMissingEntry;
      //   formValid = false;
      // }
      if (
        !this.state.singleDataSourceInfo.dataClassification ||
        this.state.singleDataSourceInfo.dataClassification === 'Choose'
      ) {
        singleDataSourceErrors.dataClassification = errorMissingEntry;
        formValid = false;
      }
      if (!this.state.singleDataSourceInfo.dataSources?.length) {
        singleDataSourceErrors.dataSources = errorMissingEntry;
        formValid = false;
      }
    }

    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ errors });
    return formValid;
  };

  protected deleteModalContent: React.ReactNode = (
    <div id="contentparentdiv" className={Styles.modalContentWrapper}>
      <div className={Styles.modalTitle}>Delete Data Sources Information</div>
      <div className={Styles.modalContent}>This will be deleted permanently.</div>
    </div>
  );

  protected onAcceptDeleteChanges = () => {
    const { singleDataSources, dataWarehouseInUse } = this.state.dataAndFunctions;
    if (this.state.isDataWarehouseSection) {
      const dataSourceList = [...this.state.dataAndFunctions.dataWarehouseInUse];
      dataSourceList.splice(this.state.editDataSourceIndex, 1);
      this.props.modifyDataFunction({
        dataWarehouseInUse: dataSourceList,
        singleDataSources,
      });
      this.setState((prevState) => ({
        dataAndFunctions: {
          ...prevState.dataAndFunctions,
          dataWarehouseInUse: dataSourceList,
        },
        showDeleteModal: false,
      }));
    } else {
      const dataSourceList = [...this.state.dataAndFunctions.singleDataSources];
      dataSourceList.splice(this.state.editDataSourceIndex, 1);
      this.props.modifyDataFunction({
        dataWarehouseInUse,
        singleDataSources: dataSourceList,
      });
      this.setState((prevState) => ({
        dataAndFunctions: {
          ...prevState.dataAndFunctions,
          singleDataSources: dataSourceList,
        },
        showDeleteModal: false,
      }));
    }
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteModal: false });
  };

  // sortByColumn = (columnName: string, sortOrder: string) => {
  //   return () => {
  //     let sortedArray: IDataWarehouseInUse[] = [];

  //     if (columnName === columnName) {
  //       sortedArray = this.state.dataAndFunctions.dataWarehouseInUse?.sort((a, b) => {
  //         const nameA = a[columnName]?.toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
  //         const nameB = b[columnName]?.toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
  //         if (nameA < nameB) {
  //           return sortOrder === 'asc' ? -1 : 1;
  //         } else if (nameA > nameB) {
  //           return sortOrder === 'asc' ? 1 : -1;
  //         }
  //         return 0;
  //       });
  //     }
  //     this.setState({
  //       nextSortOrder: sortOrder == 'asc' ? 'desc' : 'asc',
  //     });
  //     this.setState({
  //       currentSortOrder: sortOrder,
  //     });
  //     this.setState({
  //       currentColumnToSort: columnName,
  //     });
  //     this.setState((prevState) => ({
  //       dataAndFunctions: {
  //         ...prevState.dataAndFunctions,
  //         dataWarehouseInUse: sortedArray,
  //       },
  //     }));
  //   };
  // };

  protected setDataWarehouseContextMenuStatus = (status: boolean) => {
    this.setState({
      isDataWarehouseContextMenuOpened: status
    })
  }

  protected setSingleDataSourceContextMenuStatus = (status: boolean) => {
    this.setState({
      isSingleDataSourceContextMenuOpened: status
    })
  }

  public render() {
    const requiredError = '*Missing entry';

    const dataSourceModalContent = (
      <div>
        <br />
        <div className={Styles.flexLayout}>
          <div>
            <div className={classNames('input-field-group include-error')} style={{ minHeight: '50px' }}>
              <div className={Styles.flexLayout}>
                <div style={{ minWidth: '175px' }}>
                  <label
                    className={`radio ${
                      this.state.editDataSource && this.state.dataSource === 'singledatasource' ? 'disabled' : ''
                    }`}
                  >
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        name="dataSource"
                        value="datawarehouse"
                        id="dataWareHouseInUse"
                        checked={this.state.dataSource === 'datawarehouse'}
                        disabled={this.state.editDataSource && this.state.dataSource === 'singledatasource'}
                        onChange={this.handleRadio}
                      />
                    </span>
                    <span className="label">Data Warehouse</span>
                  </label>
                </div>
                <div style={{ minWidth: '162px' }}>
                  <label
                    className={`radio ${
                      this.state.editDataSource && this.state.dataSource === 'datawarehouse' ? 'disabled' : ''
                    }`}
                  >
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        name="dataSource"
                        id="singleDataSource"
                        value="singledatasource"
                        checked={this.state.dataSource === 'singledatasource'}
                        disabled={this.state.editDataSource && this.state.dataSource === 'datawarehouse'}
                        onChange={this.handleRadio}
                      />
                    </span>
                    <span className="label">Single Data Source</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
        <DataWarehouse
          dataSourceType={this.state.dataSource}
          dataWarehouseInUseInfo={this.state.dataWarehouseInUseInfo}
          errors={this.state.errors}
          onDropdownChange={this.onDataSource}
          onCommonFunctionsChange={this.onCommonFunctionsChange}
          requiredError={requiredError}
          dataWarehouses={this.props.dataWarehouses}
          commonFunctions={this.props.commonFunctions.map(item=>item.name)}
          connectionTypes={this.props.connectionTypes.map(item=>item.name)}
          dataClassifications={this.props.dataClassifications.map(item=>item.name)}
          // onDataWarehouseChange={()=>this.handleChange} 
        />
        <SingleDataSource
          dataSourceType={this.state.dataSource}
          errors={this.state.singleDataSourceErrors}
          requiredError={requiredError}
          onDropdownChange={this.onChangeSingleDataSource}
          connectionTypes={this.props.connectionTypes}
          dataClassifications={this.props.dataClassifications}
          dataSources={this.props.dataSources}
          singleDataSourceInfo={this.state.singleDataSourceInfo}
          setDataSources={this.setDataSources}
        />
        <div className="btnConatiner">
          <button
            className="btn btn-primary"
            type="button"
            onClick={
              this.state.addDataSource
               ? this.onAddDatasourceNew
              : this.state.dataSource === 'datawarehouse'
                ? this.onEditDatasource
              : this.onEditSingleDataSource
            }
          >
            {this.state.addDataSource ? 'Add' : this.state.editDataSource && 'Save'}
          </button>
        </div>
      </div>
    );

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Please add data source & functions </h3>            
            <div className={Styles.listWrapper}>
            {(this.state.dataAndFunctions.dataWarehouseInUse?.length < 1 && this.state.dataAndFunctions.singleDataSources?.length < 1) && (
                <div className={Styles.dataSourceWrapper}>
                  <div className={Styles.dataSourceWrapperNoList}>
                    <div className={Styles.addDataSourceWrapper}>
                      <IconDataSource className={Styles.avatarIcon} />
                      <button id="AddDataSourceBtn" onClick={() => this.showDataSourceModal()}>
                        <i className="icon mbc-icon plus" />
                        <span>Add Data Source</span>
                      </button>
                      {(!this.state.dataAndFunctions.dataWarehouseInUse?.length && !this.state.dataAndFunctions.singleDataSources?.length) && (
                        <div className={classNames(this.state.dataAndFunctionTabError ? '' : 'hide')}>
                          <span className="error-message">{this.state.dataAndFunctionTabError}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}
              <br />
              {(this.state.dataAndFunctions.dataWarehouseInUse?.length > 0 || this.state.dataAndFunctions.singleDataSources?.length > 0) && (
                <div className={Styles.addDataSourceWrapper}>
                  <IconDataSource className={Styles.avatarIcon} />
                  <button id="AddDataSourceBtn" onClick={() => this.showDataSourceModal()}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Data Source</span>
                  </button>
                </div>              
              )}
              <br />
              <DataWarehouseList
                dataWarehouselist={this.state.dataAndFunctions.dataWarehouseInUse}
                singleDataSourceList={this.state.dataAndFunctions.singleDataSources ? this.state.dataAndFunctions.singleDataSources : []}
                currentColumnToSort={this.state.currentColumnToSort}
                currentSortOrder={this.state.currentSortOrder}
                onEdit={this.onEditDatasourceOpen}
                onDelete={this.onDeleteDatasource}
                showDataSourceModal={this.showDataSourceModal}
                dataAndFunctionTabError={this.state.dataAndFunctionTabError}
                isSingleDataSourceContextMenuOpened = {this.state.isSingleDataSourceContextMenuOpened}
                setDataWarehouseContextMenuStatus = {this.setDataWarehouseContextMenuStatus}
                setSingleDataSourceContextMenuStatus = {this.setSingleDataSourceContextMenuStatus}
              />
              <SingleDataSourceList
                dataSources={this.props.dataSources}
                dataWarehouseList={this.state.dataAndFunctions.dataWarehouseInUse}
                list={this.state.dataAndFunctions.singleDataSources ? this.state.dataAndFunctions.singleDataSources : []}
                currentColumnToSort={this.state.currentColumnToSort}
                currentSortOrder={this.state.currentSortOrder}
                onEdit={this.onEditSingleDataSourceOpen}
                onDelete={this.onDeleteSingleDatasource}
                showDataSourceModal={this.showDataSourceModal}
                isDataWarehouseContextMenuOpened = {this.state.isDataWarehouseContextMenuOpened}
                setSingleDataSourceContextMenuStatus = {this.setSingleDataSourceContextMenuStatus}
                setDataWarehouseContextMenuStatus = {this.setDataWarehouseContextMenuStatus}
              />
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onDataFunction}>
            Save & Next
          </button>
        </div>
        <Modal
          title={
            this.state.addDataSource
              ? 'Add Data Source Information'
              : this.state.editDataSource && 'Edit Data Sources Information'
          }
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          modalStyle={{ maxHeight: '96%' }}
          buttonAlignment="right"
          show={this.state.addDataSource || this.state.editDataSource}
          content={dataSourceModalContent}
          scrollableContent={false}
          onCancel={this.handleModalClose}
        />
        <ConfirmModal
          title="Delete Data Sources Information"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={this.state.showDeleteModal}
          content={this.deleteModalContent}
          onCancel={this.onCancellingDeleteChanges}
          onAccept={this.onAcceptDeleteChanges}
        />
      </React.Fragment>
    );
  }
}
