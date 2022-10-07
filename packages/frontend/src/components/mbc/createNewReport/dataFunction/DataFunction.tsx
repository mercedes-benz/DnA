import cn from 'classnames';
import * as React from 'react';
import Styles from './DataFunction.scss';
import {
  IDataAndFunctions,
  IDataSourceMaster,
  IConnectionType,
  IDataWarehouse,
  ISubSystems,
  IDataWarehouseInUse,
  ISingleDataSources,
  IDataClassification,
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

const classNames = cn.bind(Styles);
export interface IDataWarehouseErrors {
  commonFunctions: string;
  connectionTypes: string;
  dataWarehouse: string;
  dataClassification: string;
}

interface IDataWarehouseDropdownValues {
  commonFunctions: string[];
  dataClassifications: string[];
  connectionTypes: string[];
}
export interface ISingleDataSourceErrors {
  connectionTypes: string;
  dataSources: string;
  dataClassification: string;
}
export interface IDataFunctionProps {
  dataAndFunctions: IDataAndFunctions;
  dataSources: IDataSourceMaster[];
  connectionTypes: IConnectionType[];
  dataClassifications: IDataClassification[];
  dataWarehouses: IDataWarehouse[];
  subSystems: ISubSystems[];
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
        connectionTypes: [],
        commonFunctions: [],
        dataClassification: []
      },
      dataWarehouseDropdownValues: {
        commonFunctions: [],
        connectionTypes: [],
        dataClassifications: []
      },
      singleDataSourceInfo: {
        connectionTypes: [],
        dataSources: [],
        dataClassifications: [],
      },
      singleDataSourceErrors: {
        connectionTypes: '',
        dataSources: '',
        dataClassification: '',
      },
      errors: {
        dataWarehouse: '',
        connectionTypes: '',
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
    if (this.props.dataAndFunctions) {
      this.setState({
        dataAndFunctions: this.props.dataAndFunctions,
      });
    }
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
    this.setState(
      (prevState) => ({
        dataSource: e.target.value,
        dataWarehouseInUseInfo: {
          ...prevState.dataWarehouseInUseInfo,
          dataWarehouse: '',
        },
      }),
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    const isEmpty = e.target.selectedOptions[e.target.selectedIndex]?.label === 'Choose';
    isEmpty
      ? this.setState({
          dataWarehouseDropdownValues: {
            commonFunctions: [],
            connectionTypes: [],
            dataClassifications: []
          },
        })
      : this.props.dataWarehouses?.map((item) => {
          if (item.dataWarehouse.toLowerCase() === value.toLowerCase()) {
            const isCarla = item.dataWarehouse.toLowerCase() === 'carla';
            const connectionTypes =
              isCarla && item.connectionTypes?.filter((item) => item.toLowerCase() === 'live connection')?.length
                ? item.connectionTypes
                : isCarla
                ? [...item.connectionTypes, 'live connection']
                : item.connectionTypes;
            const dataClassifications = this.props.dataClassifications.map((item: IDataClassification)=> item.name);    
            this.setState((prevState) => ({
              dataWarehouseDropdownValues: {
                ...prevState.dataWarehouseDropdownValues,
                commonFunctions: item.commonFunctions,
                connectionTypes: connectionTypes,
                dataClassifications: dataClassifications
              },
              dataWarehouseInUseInfo: {
                ...prevState.dataWarehouseInUseInfo,
                [name]: value,
                ...(prevState.dataWarehouseInUseInfo?.dataWarehouse && { connectionTypes: [] }), //reset if value is not carLa
              },
              errors: {
                ...prevState.errors,
                connectionTypes: '',
              },
            }));
          }
        });
  };

  protected showDataSourceModal = () => {
    this.setState(
      {
        addDataSource: true,
        dataSource: 'datawarehouse',
      },
      () => {
        SelectBox.defaultSetup();
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
        connectionTypes: [],
        commonFunctions: [],
        dataClassification: []
      },
      dataWarehouseDropdownValues: {
        commonFunctions: [],
        connectionTypes: [],
        dataClassifications: ['11']
      },
      errors: {
        dataWarehouse: '',
        connectionTypes: '',
        commonFunctions: '',
        dataClassification: ''
      },
      singleDataSourceInfo: {
        connectionTypes: [],
        dataSources: [],
        dataClassifications: [],
      },
      singleDataSourceErrors: {
        connectionTypes: '',
        dataSources: '',
        dataClassification: ''
      },
    });
  };

  protected onAddDatasource = () => {
    const {
      connectionTypes: dataWarehouseConnectionTypes,
      commonFunctions,
      dataClassification,
      dataWarehouse,
    } = this.state.dataWarehouseInUseInfo;
    const { connectionTypes, dataSources, dataClassifications } = this.state.singleDataSourceInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    const isDataWarehouse = this.state.dataSource === 'datawarehouse';

    const selectedValues: any = [];
    isDataWarehouse
      ? selectedValues.push({
          dataWarehouse,
          connectionTypes: dataWarehouseConnectionTypes,
          commonFunctions,
          dataClassification
        })
      : selectedValues.push({
          connectionTypes,
          dataSources,
          dataClassifications,
        });

    if (this.validateDatasourceModal()) {
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
            connectionTypes: [],
            commonFunctions: [],
            dataFunction: [],
            dataClassification: []
          },
          dataWarehouseDropdownValues: {
            commonFunctions: [],
            connectionTypes: [],
            dataClassifications: []
          },
          singleDataSourceInfo: {
            connectionTypes: [],
            dataSources: [],
            dataClassifications: [],
          },
          singleDataSourceErrors: {
            connectionTypes: '',
            dataSources: '',
            dataClassification: ''
          },
          errors: {
            dataWarehouse: '',
            connectionTypes: '',
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
  };

  protected onEditDatasourceOpen = (dataSourcesAndFunctions: IDataWarehouseInUse, index: number) => {
    const { connectionTypes, commonFunctions, dataClassification, dataWarehouse } =
      dataSourcesAndFunctions;
    this.props.dataWarehouses?.map((item) => {
      if (item.dataWarehouse.toLowerCase() === dataWarehouse.toLowerCase()) {
        const isCarla = item.dataWarehouse.toLowerCase() === 'carla';
        const connectionTypes =
          isCarla && item.connectionTypes?.filter((item) => item.toLowerCase() === 'live connection')?.length
            ? item.connectionTypes
            : isCarla
            ? [...item.connectionTypes, 'live connection']
            : item.connectionTypes;
        this.setState((prevState) => ({
          dataWarehouseDropdownValues: {
            ...prevState.dataWarehouseDropdownValues,
            commonFunctions: item.commonFunctions,
            connectionTypes: connectionTypes,
            dataClassifications: item.dataClassifications,
          },
        }));
      }
    });
    this.setState(
      {
        addDataSource: false,
        editDataSource: true,
        editDataSourceIndex: index,
        dataWarehouseInUseInfo: {
          dataWarehouse,
          connectionTypes,
          commonFunctions,
          dataClassification,
        },
        dataSource: 'datawarehouse',
      },
      () => {
        SelectBox.defaultSetup();
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
    const { connectionTypes, commonFunctions, dataClassification, dataWarehouse } =
      this.state.dataWarehouseInUseInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    if (this.validateDatasourceModal()) {
      const dataSourceList = [...dataWarehouseInUse]; // create copy of original array
      dataSourceList[editDataSourceIndex] = {
        connectionTypes,
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
          connectionTypes: [],
          commonFunctions: [],
          dataClassification: [],
        },
        dataWarehouseDropdownValues: {
          commonFunctions: [],
          connectionTypes: [],
          dataClassifications: []
        },
        errors: {
          dataWarehouse: '',
          connectionTypes: '',
          commonFunctions: '',
          dataClassification: ''
        },
        dataSource: 'datawarehouse',
      });
    }
  };

  protected onEditSingleDataSourceOpen = (dataSourcesAndFunctions: ISingleDataSources, index: number) => {
    const { connectionTypes, dataClassifications, dataSources } = dataSourcesAndFunctions;
    this.setState(
      {
        addDataSource: false,
        editDataSource: true,
        editDataSourceIndex: index,
        singleDataSourceInfo: {
          connectionTypes,
          dataClassifications,
          dataSources,
        },
        dataSource: 'singledatasource',
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected onEditSingleDataSource = () => {
    const { editDataSourceIndex } = this.state;
    const { connectionTypes, dataSources, dataClassifications } = this.state.singleDataSourceInfo;
    const { dataWarehouseInUse, singleDataSources } = this.state.dataAndFunctions;
    if (this.validateDatasourceModal()) {
      const dataSourceList = [...singleDataSources]; // create copy of original array
      dataSourceList[editDataSourceIndex] = { connectionTypes, dataSources, dataClassifications }; // modify copied array

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
          connectionTypes: [],
          dataSources: [],
          dataClassifications: [],
        },
        singleDataSourceErrors: {
          connectionTypes: '',
          dataSources: '',
          dataClassification: ''
        },
        dataSource: 'singledatasource',
      });
    }
  };

  public onChangeSourceAndFunction = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: any[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        if (this.state.dataSource === 'singledatasource') {
          const dataWarehouse: any = { id: null, name: null };
          dataWarehouse.id = option.value;
          dataWarehouse.name = option.textContent;
          selectedValues.push(dataWarehouse);
        } else {
          selectedValues.push(option.textContent);
        }
      });
    }

    this.setState((prevState) => ({
      ...(this.state.dataSource === 'datawarehouse'
        ? {
            ...prevState,
            dataWarehouseInUseInfo: {
              ...prevState.dataWarehouseInUseInfo,
              [name]: selectedValues,
            },
          }
        : {
            ...prevState,
            singleDataSourceInfo: {
              ...prevState.singleDataSourceInfo,
              [name]: selectedValues,
            },
          }),
    }));
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
        !this.state.dataWarehouseInUseInfo.dataClassification?.length ||
        this.state.dataWarehouseInUseInfo.dataClassification[0] === 'Choose'
      ) {
        errors.dataClassification = errorMissingEntry;
        formValid = false;
      }

      if (
        !this.state.dataWarehouseInUseInfo.connectionTypes?.length ||
        this.state.dataWarehouseInUseInfo.connectionTypes[0] === 'Choose'
      ) {
        errors.connectionTypes = errorMissingEntry;
        formValid = false;
      }
    } else {
      if (
        !this.state.singleDataSourceInfo.connectionTypes?.length ||
        this.state.singleDataSourceInfo.connectionTypes[0].name === 'Choose'
      ) {
        singleDataSourceErrors.connectionTypes = errorMissingEntry;
        formValid = false;
      }
      if (
        !this.state.singleDataSourceInfo.dataClassifications?.length ||
        this.state.singleDataSourceInfo.dataClassifications[0].name === 'Choose'
      ) {
        errors.dataClassification = errorMissingEntry;
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

  sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: IDataWarehouseInUse[] = [];

      if (columnName === columnName) {
        sortedArray = this.state.dataAndFunctions.dataWarehouseInUse?.sort((a, b) => {
          const nameA = a[columnName]?.toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          const nameB = b[columnName]?.toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          if (nameA < nameB) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA > nameB) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      }
      this.setState({
        nextSortOrder: sortOrder == 'asc' ? 'desc' : 'asc',
      });
      this.setState({
        currentSortOrder: sortOrder,
      });
      this.setState({
        currentColumnToSort: columnName,
      });
      this.setState((prevState) => ({
        dataAndFunctions: {
          ...prevState.dataAndFunctions,
          dataWarehouseInUse: sortedArray,
        },
      }));
    };
  };

  public render() {
    const requiredError = '*Missing error';

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
          onDropdownChange={this.onChangeSourceAndFunction}
          requiredError={requiredError}
          dataWarehouses={this.props.dataWarehouses}
          commonFunctions={this.state.dataWarehouseDropdownValues.commonFunctions}
          connectionTypes={this.state.dataWarehouseDropdownValues.connectionTypes}
          dataClassifications={this.state.dataWarehouseDropdownValues.dataClassifications}
          onDataWarehouseChange={this.handleChange} 
        />
        <SingleDataSource
          dataSourceType={this.state.dataSource}
          errors={this.state.singleDataSourceErrors}
          requiredError={requiredError}
          onDropdownChange={this.onChangeSourceAndFunction}
          connectionTypes={this.props.connectionTypes}
          dataClassifications={this.props.dataClassifications}
          dataSources={this.props.dataSources}
          singleDataSourceInfo={this.state.singleDataSourceInfo}
        />
        <div className="btnConatiner">
          <button
            className="btn btn-primary"
            type="button"
            onClick={
              this.state.addDataSource
                ? this.onAddDatasource
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
            <h3>Data Sources Information </h3>
            <DataWarehouseList
              dataWarehouselist={this.state.dataAndFunctions.dataWarehouseInUse}
              singleDataSourceList={this.state.dataAndFunctions.singleDataSources}
              currentColumnToSort={this.state.currentColumnToSort}
              currentSortOrder={this.state.currentSortOrder}
              onEdit={this.onEditDatasourceOpen}
              onDelete={this.onDeleteDatasource}
              showDataSourceModal={this.showDataSourceModal}
              dataAndFunctionTabError={this.state.dataAndFunctionTabError}
            />
            <SingleDataSourceList
              dataWarehouseList={this.state.dataAndFunctions.dataWarehouseInUse}
              list={this.state.dataAndFunctions.singleDataSources}
              currentColumnToSort={this.state.currentColumnToSort}
              currentSortOrder={this.state.currentSortOrder}
              onEdit={this.onEditSingleDataSourceOpen}
              onDelete={this.onDeleteDatasource}
              showDataSourceModal={this.showDataSourceModal}
            />
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
