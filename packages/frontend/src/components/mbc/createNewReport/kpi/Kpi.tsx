import cn from 'classnames';
import * as React from 'react';
import Styles from './Kpi.scss';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { IKpis, IKpiNames, IReportingCauses } from 'globals/types';
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { ErrorMsg } from 'globals/Enums';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import TextArea from 'components/mbc/shared/textArea/TextArea';

const classNames = cn.bind(Styles);
export interface IKpiProps {
  kpis: IKpis[];
  kpiNames: IKpiNames[];
  reportingCause: IReportingCauses[];
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyKpi: (modifyKpi: IKpis[]) => void;
}

export interface IKpiState {
  kpis: IKpis[];
  kpiInfo: IKpis;
  errors: IKpis;
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
}
export interface IKpiList {
  name: string;
  reportingCase: string;
  kpiLink: string;
  comment: string;
}
export default class Kpi extends React.Component<IKpiProps, IKpiState> {
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
        name: '',
        reportingCause: '',
        kpiLink: '',
        comment: '',
      },
      errors: {
        name: '',
        reportingCause: '',
        kpiLink: '',
        comment: '',
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
    };
  }

  public componentDidMount() {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }

  public render() {
    const requiredError = '*Missing entry';
    const addKpiModalContent = (
      <div className={Styles.addKpiModalContent}>
        <br />
        <div>
          <div className={Styles.flexLayout}>
            <div>
              <div className={classNames('input-field-group include-error', this.state.errors.name ? 'error' : '')}>
                <label id="kpinames" htmlFor="kpinames" className="input-label">
                  Name<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="kpinames"
                    name="name"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    value={this.state.kpiInfo.name || ''}
                    onChange={this.handleChange}
                  >
                    <option value={''}>Choose</option>
                    {this.props.kpiNames?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.name.length ? '' : 'hide')}>
                  {this.state.errors.name}
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
                  Reporting Cause<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="reportingCauseField"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    name="reportingCause"
                    value={this.state.kpiInfo.reportingCause || ''}
                    onChange={this.handleChange}
                  >
                    <option value={''}>Choose</option>
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
                  KPI-Link
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
              name={'comment'}
              labelId={'reportKpiCommentLabel'}
              label={'Comment'}
              rows={50}
              value={this.state.kpiInfo.comment}
              required={false}
              onChange={this.handleChange}
              onBlur={this.validateKpiModal}
            />
            {this.state.duplicateKpiAdded ? <span className={'error-message'}>KPI already exist</span> : ''}
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
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>KPIs</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={classNames('expanstion-table', Styles.kpiList)}>
                <div className={Styles.kpiGrp}>
                  <div className={Styles.kpiGrpList}>
                    <div className={Styles.kpiGrpListItem}>
                      {this.state.kpis?.length ? (
                        <div className={Styles.kpiCaption}>
                          <div className={Styles.kpiTile}>
                            <div className={Styles.kpiTitleCol}>
                              <label>KPI No.</label>
                            </div>
                            <div className={Styles.kpiTitleCol}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort === 'name' ? this.state.currentSortOrder : '')
                                }
                                onClick={this.sortByColumn('name', this.state.nextSortOrder)}
                              >
                                <i className="icon sort" />
                                KPI Name
                              </label>
                            </div>
                            <div className={Styles.kpiTitleCol}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort === 'reportingCause'
                                    ? this.state.currentSortOrder
                                    : '')
                                }
                                onClick={this.sortByColumn('reportingCause', this.state.nextSortOrder)}
                              >
                                <i className="icon sort" />
                                Reporting Cause
                              </label>
                            </div>
                            <div className={Styles.kpiTitleCol}>KPI-Link</div>
                            <div className={Styles.kpiTitleCol}>Action</div>
                          </div>
                        </div>
                      ) : (
                        ''
                      )}
                      {this.state.kpis?.map((kpi: IKpis, index: number) => {
                        return (
                          <div
                            key={index}
                            className={'expansion-panel-group airflowexpansionPanel ' + Styles.kpiGrpListItemPanel}
                          >
                            <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                              <span className="animation-wrapper"></span>
                              <input type="checkbox" id={index + '1'} defaultChecked={index === 0} />
                              <label
                                className={Styles.expansionLabel + ' expansion-panel-label '}
                                htmlFor={index + '1'}
                              >
                                <div className={Styles.kpiTile}>
                                  <div className={Styles.kpiTitleCol}>{`KPI ${index + 1}`}</div>
                                  <div className={Styles.kpiTitleCol}>{kpi.name || '-'}</div>
                                  <div className={Styles.kpiTitleCol}>{kpi.reportingCause || '-'}</div>
                                  <div className={Styles.kpiTitleCol}>
                                    {kpi.kpiLink ? (
                                      <span>
                                        <a href={kpi.kpiLink} target="_blank" rel="noopener noreferrer">
                                          {kpi.kpiLink}
                                        </a>{' '}
                                        <i
                                          style={{ position: 'relative' }}
                                          tooltip-data="Open in New Tab"
                                          className={'icon mbc-icon new-tab'}
                                          onClick={() => window.open(kpi.kpiLink, '_blank', 'noopener,noreferrer')}
                                        />
                                      </span>
                                    ) : (
                                      '-'
                                    )}
                                  </div>
                                  <div className={Styles.kpiTitleCol}></div>
                                </div>
                                <i tooltip-data="Expand" className="icon down-up-flip"></i>
                              </label>
                              <div className="expansion-panel-content">
                                <div className={Styles.kpiCollContent}>
                                  <div className={Styles.kpiDesc}>
                                    <pre className={Styles.commentPre}>{kpi.comment}</pre>
                                  </div>
                                  <div className={Styles.kpiBtnGrp}>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => this.onEditKpiOpen(kpi)}
                                    >
                                      <i className="icon mbc-icon edit"></i>
                                      <span>Edit KPI</span>
                                    </button>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => this.onDeleteKpi(kpi)}
                                    >
                                      <i className="icon delete"></i>
                                      <span>Delete KPI </span>
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>
              {this.state.kpis?.length < 1 && (
                <div className={Styles.kpiWrapper}>
                  <div className={Styles.kpiWrapperNoList}>
                    <div className={Styles.addKpiWrapper}>
                      <button id="AddTeamMemberBtn" onClick={this.addKpiModel}>
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
                  <button id="AddKpiBtn" onClick={this.addKpiModel}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Kpi</span>
                  </button>
                </div>
              )}
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
        name: '',
        reportingCause: '',
        kpiLink: '',
        comment: '',
      },
      errors: {
        name: '',
        reportingCause: '',
        kpiLink: '',
        comment: '',
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
    const { name, reportingCause, kpiLink, comment } = this.state.kpiInfo;
    const { kpis } = this.state;
    const selectedValues: IKpis[] = [];
    selectedValues.push({
      name,
      reportingCause,
      kpiLink,
      comment,
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
          kpiInfo: {
            name: '',
            reportingCause: '',
            kpiLink: '',
            comment: '',
          },
          errors: {
            name: '',
            reportingCause: '',
            kpiLink: '',
            comment: '',
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
    const { name, reportingCause, kpiLink, comment } = kpi;
    const { kpis } = this.state;
    // const selectedItemIndex = kpis.findIndex(item=> item.name === name && item.reportingCause === reportingCause);
    const selectedItemIndex = kpis.findIndex(
      (item) =>
        item.name === name &&
        item.reportingCause === reportingCause &&
        item.kpiLink === kpiLink &&
        item.comment === comment,
    );
    this.setState(
      {
        addKpi: false,
        editKpi: true,
        selectedItemIndex,
        kpiInfo: {
          name,
          reportingCause,
          kpiLink,
          comment,
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
    const { name, reportingCause, comment, kpiLink } = kpi;
    const selectedItemIndex = this.state.kpis.findIndex(
      (item) =>
        item.name === name &&
        item.reportingCause === reportingCause &&
        item.kpiLink === kpiLink &&
        item.comment === comment,
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
    const { name, reportingCause, kpiLink, comment } = this.state.kpiInfo;
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
      kpiList[selectedItemIndex] = { name, reportingCause, kpiLink, comment }; // modify copied array
      this.props.modifyKpi(kpiList);
      this.setState(
        {
          editKpi: false,
          duplicateKpiAdded: false,
          kpis: kpiList,
          errors: {
            name: '',
            reportingCause: '',
            kpiLink: '',
            comment: '',
          },
          kpiInfo: {
            name: '',
            reportingCause: '',
            kpiLink: '',
            comment: '',
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

    if (!this.state.kpiInfo.name) {
      errors.name = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.kpiInfo.reportingCause) {
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
      errors.comment = '';
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
}
