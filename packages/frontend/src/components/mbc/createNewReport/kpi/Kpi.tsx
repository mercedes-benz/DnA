import cn from 'classnames';
import * as React from 'react';
import Styles from './Kpi.scss';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { IKpis, IKpiNameList, IReportingCauses } from 'globals/types';
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { ErrorMsg } from 'globals/Enums';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import TextArea from 'components/mbc/shared/textArea/TextArea';
import IconAddKPI from 'components/icons/IconAddKPI';
import Tags from 'components/formElements/tags/Tags';
// import ReportListRowItem from 'components/mbc/allReports/reportListRowItem/ReportListRowItem';

const classNames = cn.bind(Styles);

export interface IKpiProps {
  kpis: IKpis[];
  kpiNames: IKpiNameList[];
  kpiClassifications: any[];
  reportingCause: IReportingCauses[];
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyKpi: (modifyKpi: IKpis[]) => void;
}

export interface IKpiState {
  kpis: IKpis[];
  kpiInfo: IKpis;
  errors: IKpisError;
  comment: string;
  addKpi: boolean;
  editKpi: boolean;
  selectedItemIndex: number;
  duplicateKpiAdded: boolean;
  currentColumnToSort: string;
  currentSortOrder: string;
  nextSortOrder: string;
  kpiTabError: string;
  showDeleteModal: boolean;

  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  selectedContextMenu: string;
  dataSources: IDataSources;
  enableClassification: boolean;
  selectedClassification: string;
}

export interface IDataSources {
  kpiName: string;
  kpiClassification: string;
}

export interface IKpiList {
  name: string;
  reportingCase: string[];
  kpiLink: string;
  comment: string;
}
export interface IKpisError {
  description: string;
  name: string;
  kpiClassification: string;
  reportingCause: string;
  kpiLink: string;
}
export default class Kpi extends React.Component<IKpiProps, IKpiState> {
  protected isTouch = false;
  protected listRowElement: HTMLTableRowElement;
  public static getDerivedStateFromProps(props: IKpiProps, state: IKpiState) {
    return {
      kpis: props.kpis,
    };
  }

  constructor(props: IKpiProps) {
    super(props);
    this.state = {
      kpis: [],
      kpiInfo: {
        name: {kpiName: '', kpiClassification: ''},
        names: [],
        reportingCause: [],
        kpiLink: '',
        description: '',
      },
      errors: {
        name: '',
        kpiClassification: '',
        reportingCause: '',
        kpiLink: '',
        description: '',
      },
      comment: null,
      addKpi: false,
      editKpi: false,
      selectedItemIndex: -1,
      duplicateKpiAdded: false,
      currentColumnToSort: 'name',
      currentSortOrder: 'desc',
      nextSortOrder: 'asc',
      kpiTabError: '',
      showDeleteModal: false,

      showContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
      selectedContextMenu: '',
      dataSources: {kpiName: '', kpiClassification: ''},
      enableClassification: false,
      selectedClassification: ''
    };
  }

  public componentDidMount() {
    ExpansionPanel.defaultSetup();
    // this.setState({selectedClassification: this.state.kpiInfo?.names[0]?.dataType},
    //   () => {-
    //     SelectBox.defaultSetup();
    //     });
    // document.addEventListener('touchend', this.handleContextMenuOutside, true);
    // document.addEventListener('click', this.handleContextMenuOutside, true);
    Tooltip.defaultSetup();
  }

  // public componentWillUnmount() {
  //   document.removeEventListener('touchend', this.handleContextMenuOutside, true);
  //   document.removeEventListener('click', this.handleContextMenuOutside, true);
  // }

  public toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>, index: number) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable: ClientRect = document.querySelector(`#kpi-${index}`).getBoundingClientRect();
    this.setState({
      contextMenuOffsetTop: elemRect.top - (relativeParentTable.top + 10),
      contextMenuOffsetRight: 10,
      showContextMenu: !this.state.showContextMenu,
      selectedContextMenu: `#kpi-${index}`
    });
  };

  public render() {
    const requiredError = '*Missing entry';
    const addKpiModalContent = (
      <div className={Styles.addKpiModalContent}>
        <br />
        <div>
          <div className={Styles.flexLayout}>
            <div>
              <div className={classNames('input-field-group include-error', this.state.errors.name ? 'error' : '')}>
                <div>
                  <Tags
                    title={'KPI Name'}
                    max={1}
                    chips={
                      this.state?.kpiInfo?.name?.kpiName
                        ? this.state.kpiInfo?.name?.kpiName ? [this.state.kpiInfo?.name?.kpiName] : []
                        : this.state.dataSources?.kpiName ? [this.state.dataSources?.kpiName] : []
                    }
                    setTags={this.setDataSources}
                    isMandatory={true}
                    removeTag={this.removeDataSource}
                    tags={this.props.kpiNames}
                    // tags={this.props.kpiNames}
                    showMissingEntryError={false}
                    isDataSource={false}
                    suggestionPopupHeight={300}
                    {...this.props}
                  />
                </div>
                <span className={classNames('error-message', this.state.errors.name.length ? '' : 'hide')}>
                  {this.state.errors.name}
                </span>
              </div>
              
              <div className={classNames('input-field-group include-error', this.state.errors.kpiClassification ? 'error' : '')}>
                <label id="kpiclassificationlabel" htmlFor="kpiclassification" className="input-label">
                  KPI Classification <sup>*</sup> &nbsp;
                  <i className="icon mbc-icon info" tooltip-data="Please assign the KPI to a respective Focus Area" />
                </label>
                <div className={classNames("custom-select",!this.state?.enableClassification ? Styles.disabledDiv : '')}>
                  <select
                    id="kpiClassification"
                    name="kpiClassification"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    // disabled={!this.state?.enableClassification}
                    // value={this.state.kpiInfo?.names.map((item: any) => item.classification) || ''}
                    value={this.state.kpiInfo?.name?.kpiClassification
                      ? this.state.kpiInfo?.name?.kpiClassification
                      : this.state.dataSources?.kpiClassification}
                    onChange={this.handleChangeClassification}
                  >
                    <option value={''}>Choose</option>
                    {this.props.kpiClassifications?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.kpiClassification.length ? '' : 'hide')}>
                  {this.state.errors.kpiClassification}
                </span>
              </div>
              
              
            </div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  this.state.errors.reportingCause ? 'error' : '',
                )}
              >
                <label id="reportingCauseLabel" htmlFor="reportingCauseField" className="input-label">
                  Reporting Cause <sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="reportingCauseField"
                    multiple={true}
                    required-error={requiredError}
                    required={true}
                    name="reportingCause"
                    value={this.state.kpiInfo.reportingCause}
                    onChange={this.onChangeReportingCause}
                  >
                    {/* <option value={''}>Choose</option> */}
                    {this.props.reportingCause?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.reportingCause.length ? '' : 'hide')}>
                  {this.state.errors.reportingCause}
                </span>
              </div>
            </div>
          </div>
          <div className={Styles.flexLayout}>
            <div>
              <div className={classNames('input-field-group include-error', this.state.errors.kpiLink ? 'error' : '')}>
                <label id="kpiLinkLabel" htmlFor="kpiLinkField" className="input-label">
                Link to KPI Information
                </label>
                <input
                  type="text"
                  className="input-field"
                  name="kpiLink"
                  id="kpiLinkInput"
                  maxLength={200}
                  placeholder="https://www.example.com"
                  autoComplete="off"
                  value={this.state.kpiInfo.kpiLink}
                  onChange={this.handleChange}
                />
                <span className={classNames('error-message', this.state.errors.kpiLink.length ? '' : 'hide')}>
                  {this.state.errors.kpiLink}
                </span>
              </div>
            </div>
          </div>
          <div>
            <TextArea
              controlId={'reportKpiComment'}
              containerId={'reportKpiComment'}
              name={'description'}
              labelId={'reportKpiCommentLabel'}
              label={'KPI Description'}
              rows={50}
              value={this.state.kpiInfo.description}
              required={false}
              onChange={this.handleChange}
              onBlur={this.validateKpiModal}
            />
            {/* {this.state.duplicateKpiAdded ? <span className={'error-message'}>KPI already exist</span> : ''} */}
            <div className="btnConatiner">
              <button
                className="btn btn-primary"
                type="button"
                onClick={this.state.addKpi ? this.onAddKpi : this.onEditKpi}
              >
                {this.state.addKpi ? 'Add' : this.state.editKpi && 'Save'}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete KPI</div>
        <div className={Styles.modalContent}>This KPI will be deleted permanently.</div>
      </div>
    );

    const kpiData = this.state.kpis?.map((kpi: IKpis, index: number) => {

      return (
        <React.Fragment key={index}>
          <tr
            id={'kpi-'+index}
            key={index}
            className={classNames(
              'data-row',
              'kpi-'+index,
              Styles.reportRow,
              this.state.showContextMenu ? Styles.contextOpened : null,
            )}
            ref={this.listRow}
            // onClick={this.goToSummary}
          >
            <td className={'wrap-text ' + classNames(Styles.reportName)}>
              <div className={Styles.solIcon}>
                {kpi?.name?.kpiName}
              </div>
            </td>
            <td className="wrap-text">{kpi?.reportingCause.length > 0? Array(kpi?.reportingCause).join(', ') : 'NA'}</td>
            <td className="wrap-text">{kpi?.kpiLink ? <a href={kpi?.kpiLink} target='_blank' rel="noreferrer">{kpi?.kpiLink}</a> : 'NA'}</td>
            <td>              
              <div
                className={classNames(
                  Styles.contextMenu,
                  this.state.showContextMenu && this.state.selectedContextMenu == '#kpi-'+index ? Styles.open : '',
                )}
              >
                <span onClick={(e: React.FormEvent<HTMLSpanElement>) => {this.toggleContextMenu(e, index)}} className={classNames('trigger', Styles.contextMenuTrigger)}>
                  <i className="icon mbc-icon listItem context" />
                </span>
                {this.state.selectedContextMenu == '#kpi-'+index ?
                  <div
                    style={{
                      top: this.state.contextMenuOffsetTop + 'px',
                      right: this.state.contextMenuOffsetRight + 'px',
                    }}
                    className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}
                  >
                    <ul className="contextList">                               
                      <li className="contextListItem">
                        <span onClick={() => this.onEditKpiOpen(kpi)}>Edit KPI</span>
                      </li>
                    
                      <li className="contextListItem">
                        <span onClick={() => this.onDeleteKpi(kpi)}>Delete KPI</span>
                      </li>                 
                    </ul>
                  </div>
                : ''}
              </div>              
            </td>
          </tr>
        </React.Fragment>
      );
    });
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Please add or edit the report's KPIs</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={classNames('expanstion-table', Styles.kpiList)}>
              {this.state.kpis?.length < 1 && (
                <div className={Styles.kpiWrapper}>
                  <div className={Styles.kpiWrapperNoList}>
                    <div className={Styles.addKpiWrapper}>
                      <IconAddKPI className={Styles.avatarIcon} />
                      <button id="AddKpiBtn" onClick={this.addKpiModel}>
                        <i className="icon mbc-icon plus" />
                        <span>Add Kpi</span>
                      </button>
                    </div>
                    <div className={classNames(this.state.kpiTabError ? '' : 'hide')}>
                      <span className="error-message">{this.state.kpiTabError}</span>
                    </div>
                  </div>
                </div>
              )}
              <br />
              {this.state.kpis?.length > 0 && (
                <div className={Styles.addKpiWrapper}>
                  <IconAddKPI className={Styles.avatarIcon} />
                  <button id="AddKpiBtn" onClick={this.addKpiModel}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Kpi</span>
                  </button>
                </div>
              )}
              <br />
                <div className={Styles.kpiGrp}>
                  <div className={Styles.kpiGrpList}>
                    <div className={Styles.kpiGrpListItem}>                      
                      <table
                        className={classNames(
                          'ul-table kpiList',
                          Styles.reportsMarginNone,
                          this.state.kpis?.length === 0 ? 'hide' : '',
                        )}
                      >
                        <tbody>{kpiData}</tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onSaveKpi}>
            Save & Next
          </button>
        </div>
        <Modal
          title={this.state.addKpi ? 'Add KPI' : this.state.editKpi && 'Edit KPI'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={this.state.addKpi || this.state.editKpi}
          content={addKpiModalContent}
          scrollableContent={false}
          onCancel={this.addKpiModalClose}
        />
        <ConfirmModal
          title="Delete KPI"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={this.state.showDeleteModal}
          content={deleteModalContent}
          onCancel={this.onCancellingDeleteChanges}
          onAccept={this.onAcceptDeleteChanges}
        />
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.kpis) {
      this.setState({
        kpis: this.props.kpis,
      });
    }
  };

  protected handleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement | HTMLInputElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    const urlRegEx = /http(s)?:\/\//g;
    this.setState((prevState) => ({
      kpiInfo: {
        ...prevState.kpiInfo,
        [name]: value,
      },
      ...(name === 'kpiLink' && {
        errors: {
          ...prevState.errors,
          kpiLink: value ? (urlRegEx.test(value) ? '' : 'Invalid URL') : '',
        },
      }),
    }));
  };

  protected handleChangeClassification = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement | HTMLInputElement>) => {
    // const name = e.target.name;
    const value = e.target.value;
    this.setState((prevState) => ({
      kpiInfo: {
        ...prevState.kpiInfo,
        selectedClassification: value,
        name:{
          ...prevState.kpiInfo.name,
          kpiClassification: value,
        }
      }
    }));
  };

  public onChangeReportingCause = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        let reportingCause = '';
        reportingCause = option.textContent;
        selectedValues.push(reportingCause);
      });
    }

    this.setState((prevState) => ({
      kpiInfo: {
        ...prevState.kpiInfo,
        reportingCause: selectedValues,
      }
    }));
  };

  protected onSaveKpi = () => {
    if (this.validateKpiTab()) {
      this.props.modifyKpi(this.state.kpis);
      this.props.onSaveDraft('kpi');
    }
  };

  protected validateKpiTab = () => {
    !this.state.kpis.length && this.setState({ kpiTabError: ErrorMsg.KPI_TAB });
    return this.state.kpis.length;
  };

  protected addKpiModel = () => {
    this.setState(
      {
        addKpi: true,
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected addKpiModalClose = () => {
    this.setState({
      addKpi: false,
      editKpi: false,
      duplicateKpiAdded: false,
      kpiInfo: {
        name: {kpiName: '', kpiClassification: ''},
        names: [],
        reportingCause: [],
        kpiLink: '',
        description: '',
      },
      errors: {
        name: '',
        kpiClassification: '',
        reportingCause: '',
        kpiLink: '',
        description: '',
      },
    });
  };

  protected isKpiExist = (kpiList: IKpis[]) => {
    const { name, reportingCause } = this.state.kpiInfo;
    let kpiExists = false;
    for (let i = 0; i < kpiList.length; i++) {
      if (kpiList[i].name === name && kpiList[i].reportingCause === reportingCause) {
        kpiExists = true;
        break;
      } else {
        kpiExists = false;
      }
    }
    return kpiExists;
  };

  protected onAddKpi = () => {
    const { name, names, reportingCause, kpiLink, description } = this.state.kpiInfo;
    const { kpis } = this.state;
    const selectedValues: IKpis[] = [];
    selectedValues.push({
      name,
      names,
      reportingCause,
      kpiLink,
      description,
    });

    // const kpiExists = this.isKpiExist(kpis);

    // if(!kpiExists && this.validateKpiModal()){
    if (this.validateKpiModal()) {
      this.props.modifyKpi([...kpis, ...selectedValues]);
      this.setState(
        (prevState: any) => ({
          addKpi: false,
          duplicateKpiAdded: false,
          kpis: [...prevState.kpis, ...selectedValues],
          enableClassification: false,
          kpiInfo: {
            name: {kpiName: '', kpiClassification: ''},
            names: [],
            reportingCause: [],
            kpiLink: '',
            description: '',
          },
          errors: {
            name: '',
            kpiClassification: '',
            reportingCause: '',
            kpiLink: '',
            description: '',
          },
        }),
        () => {
          ExpansionPanel.defaultSetup();
          Tooltip.defaultSetup();
        },
      );
    } else {
      this.state.kpis.length &&
        this.setState({
          duplicateKpiAdded: true,
        });
    }
  };

  protected onEditKpiOpen = (kpi: IKpis) => {
    const { name, names, reportingCause, kpiLink, description } = kpi;
    const { kpis } = this.state;
    // const selectedItemIndex = kpis.findIndex(item=> item.name === name && item.reportingCause === reportingCause);
    const selectedItemIndex = kpis.findIndex(
      (item) =>
        item.name === name &&
        item.reportingCause === reportingCause &&
        item.kpiLink === kpiLink &&
        item.description === description,
    );
    this.setState(
      {
        addKpi: false,
        editKpi: true,
        selectedItemIndex,
        enableClassification: false,
        kpiInfo: {
          name,
          names,
          reportingCause,
          kpiLink,
          description,
        },
      },
      () => {
        SelectBox.defaultSetup();
        // ExpansionPanel.defaultSetup();
        // Tooltip.defaultSetup();
      },
    );
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteModal: false });
  };

  protected onAcceptDeleteChanges = () => {
    const kpiList = [...this.state.kpis];
    kpiList.splice(this.state.selectedItemIndex, 1);
    this.props.modifyKpi(kpiList);
    this.setState({
      kpis: kpiList,
      showDeleteModal: false,
    });
  };

  protected onDeleteKpi = (kpi: IKpis) => {
    const { name, reportingCause, description, kpiLink } = kpi;
    const selectedItemIndex = this.state.kpis.findIndex(
      (item) =>
        item.name === name &&
        item.reportingCause === reportingCause &&
        item.kpiLink === kpiLink &&
        item.description === description,
    );
    this.setState(
      {
        showDeleteModal: true,
        selectedItemIndex,
      },
      () => {
        // Tooltip.defaultSetup();
        // ExpansionPanel.defaultSetup();
      },
    );
  };

  protected onEditKpi = () => {
    const { selectedItemIndex } = this.state;
    const { name, names, reportingCause, kpiLink, description } = this.state.kpiInfo;
    const { kpis } = this.state;
    // const kpiExists = this.isKpiExist(kpis);
    // const newIndex = kpis.findIndex(item=>item.name === name && item.reportingCause === reportingCause);
    if (this.validateKpiModal()) {
      // if((
      // kpiExists &&
      // (selectedItemIndex === newIndex))
      // || !kpiExists
      // ) {
      const kpiList = [...kpis]; // create copy of original array
      kpiList[selectedItemIndex] = { name, names, reportingCause, kpiLink, description }; // modify copied array
      this.props.modifyKpi(kpiList);
      this.setState(
        {
          editKpi: false,
          duplicateKpiAdded: false,
          kpis: kpiList,
          errors: {
            name: '',
            kpiClassification: '',
            reportingCause: '',
            kpiLink: '',
            description: '',
          },
          kpiInfo: {
            name: {kpiName: '', kpiClassification: ''},
            names: [],
            reportingCause: [],
            kpiLink: '',
            description: '',
          },
        },
        () => {
          // ExpansionPanel.defaultSetup();
          //   Tooltip.defaultSetup();
        },
      );
      // } else if(kpiExists && (this.state.selectedItemIndex !== newIndex)){
      //     // Do not edit as user already exists
      //     this.setState({
      //       duplicateKpiAdded: true
      //     })
      // }
    }
  };

  protected validateKpiModal = () => {
    let formValid = true;
    const errors = this.state.errors;
    const errorMissingEntry = '*Missing entry';

    if (!this.state.kpiInfo.name.kpiName) {
      errors.name = errorMissingEntry;
      formValid = false;
    }
    if (this.state.kpiInfo.name.kpiClassification.length === 0) {
      errors.kpiClassification = errorMissingEntry;
      formValid = false;
    }
    if (this.state.kpiInfo.reportingCause.length === 0) {
      errors.reportingCause = errorMissingEntry;
      formValid = false;
    }
    if (this.state.errors.kpiLink) {
      formValid = false;
    }
    // if (!this.state.kpiInfo.comment) {
    //   errors.comment = errorMissingEntry;
    //   formValid = false;
    // }
    else {
      errors.description = '';
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ errors });
    return formValid;
  };

  sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: IKpis[] = [];

      if (columnName === columnName) {
        sortedArray = this.state.kpis?.sort((a, b) => {
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
      this.setState({
        kpis: sortedArray,
      });
    };
  };


  protected handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      this.isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && this.isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const { showContextMenu } = this.state;
    const elemClasses = target.classList;
    const listRowElement = this.listRowElement;
    const contextMenuWrapper = listRowElement.querySelector('.contextMenuWrapper');
    if (
      listRowElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu)
    ) {
      this.setState({
        showContextMenu: false,
      });
    } else if (this.listRowElement.contains(target) === false) {
      this.setState({
        showContextMenu: false,
      });
    }

    if (
      (showContextMenu) &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };

  protected listRow = (element: HTMLTableRowElement) => {
    this.listRowElement = element;
  };

  protected setDataSources = (arr: string[]) => {

    let dataSources = this.state.dataSources;

    arr.forEach((element) => {
      const matchedKpiName = this.props?.kpiNames.filter((item: any) => {
        return item.kpiName == element;
      });
      
      const isNameExist = matchedKpiName.length > 0;
      if(isNameExist){
        if(matchedKpiName[0]?.dataType){
          this.setState({
            enableClassification: false, 
            selectedClassification: matchedKpiName[0]?.dataType},
            () => {
              SelectBox.defaultSetup();
            });
        } else {
          this.setState({
            enableClassification: true, 
            selectedClassification: ''},
          () => {
            SelectBox.defaultSetup();
          });
        }
        
      } else {
        this.setState({
          enableClassification: true, 
          selectedClassification: ''},
        () => {
          SelectBox.defaultSetup();
          })
      }
      
      dataSources = { kpiName: element, kpiClassification: matchedKpiName[0]?.dataType };
      
    });

    this.setState((prevState) => ({
      kpiInfo: {
        ...prevState.kpiInfo,
        ['name']: dataSources,
      },
    }));
  };

  protected removeDataSource = (index: number) => {
    // const dataSources = this.state.kpiInfo?.names.filter((ds, dsIndex) => index !== dsIndex);

    // if (dataSources.length > 0) {
    //   const totalWeightage = dataSources.map((i) => i.weightage).reduce((current, next) => current + next);
    //   this.setState({
    //     totalWeightage,
    //   });
    // } else {
    //   this.setState({
    //     totalWeightage: 0,
    //   });
    // }

    // this.props.modifyDataSources({
    //   dataSources,
    //   dataVolume: this.state.dataVolumeValue,
    // });
  };
}
