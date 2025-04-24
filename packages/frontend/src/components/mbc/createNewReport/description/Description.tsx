import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import {
  ITag,
  IProductPhase,
  IProductStatus,
  // IIntegratedInPortal,
  IDesignGuide,
  IFrontEndTech,
  IIntegratedPortal,
  IDescriptionRequest,
  IART,
  IDivision,
  ISubDivision,
  IDivisionAndSubDivision,
  IDepartment,
  ISimilarSearchListItem,
  IDataClassification,

} from 'globals/types';
import Styles from './Description.scss';
import Tags from 'components/formElements/tags/Tags';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { ApiClient } from '../../../../services/ApiClient';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import TextArea from 'components/mbc/shared/textArea/TextArea';
import { Envs } from 'globals/Envs';
import SimilarSearchListModal from 'components/mbc/shared/similarSearchListModal/SimilarSearchListModal';

const classNames = cn.bind(Styles);
const procedureIdEnvs = Envs.ROPA_PROCEDURE_ID_PREFIX?.split(',');

export interface IDescriptionProps {
  reportId?: string;
  divisions: IDivision[];
  subDivisions: ISubDivision[];
  productPhases: IProductPhase[];
  statuses: IProductStatus[];
  arts: IART[];
  integratedPortals: IIntegratedPortal[];
  designGuideImplemented: IDesignGuide[];
  frontEndTechnologies: IFrontEndTech[];
  description: IDescriptionRequest;
  modifyReportDescription: (description: IDescriptionRequest) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
  tags: ITag[];
  departmentTags: IDepartment[];
  setSubDivisions: (subDivisions: ISubDivision[]) => void;
  enableQuickPath: boolean;
  refineReport?: () => void;
  dataClassifications: IDataClassification[];
  allSolutions: ITag[];
}

export interface IDescriptionState {
  loading: boolean;
  subDivisions: ISubDivision[];
  divisionValue: IDivisionAndSubDivision;
  divisionError: string;
  productName: string;
  productNameError: string;
  description: string;
  descriptionError: string;
  productPhaseValue: IProductPhase[];
  productPhaseError: string;
  statusValue: IProductStatus[];
  statusError: string;
  artValue: string;
  artError: string;
  integratedPortalsValue: string;
  integratedPortalError: string;
  designGuideValue: IDesignGuide[];
  designGuideError: string;
  frontEndTechValue: string[];
  showFrontEndTechError: boolean;
  chips: string[];
  showTagsMissingError: boolean;
  tags: string[];
  showDepartmentMissingError: boolean;
  departmentTags: string[];
  reportLink: string;
  reportLinkError: string;
  piiValue: string;
  piiError: string;
  procedureId: string;
  procedureIdError: string;
  selectedSimilarReportsType: string;
  showSimilarReportsModal: boolean;
  lastSearchedDescriptionInput: string;
  similarReportsBasedOnDescription: ISimilarSearchListItem[];
  lastSearchedProductNameInput: string;
  similarReportsBasedOnProductName: ISimilarSearchListItem[];
  similarReportstoShow: ISimilarSearchListItem[];
  dataClassificationValue: string;
  dataClassificationError: string;
  archerId: string;
  archerIdError: string;
  relatedSolutions: ITag[];
  relatedSolutionTags: string[]
}

export default class Description extends React.PureComponent<IDescriptionProps, IDescriptionState> {
  public static getDerivedStateFromProps(props: IDescriptionProps, state: IDescriptionState) {
    return {
      productName: props.description.productName,
      description: props.description.productDescription,
      divisionValue: props.description.division,
      subDivisions: props.subDivisions,
      productPhaseValue: props.description.productPhase,
      frontEndTechValue: props.description.frontendTechnologies,
      statusValue: props.description.status,
      designGuideValue: props.description.designGuideImplemented,
      artValue: props.description.agileReleaseTrain,
      tags: props.description.tags,
      departmentTags: props.description.department,
      reportLink: props.description.reportLink,
      piiValue: props.description.piiData,
      procedureId: props.description.procedureId,
      dataClassificationValue: props?.description.dataClassification,
      archerId: props?.description?.archerId
    }
  }
  constructor(props: IDescriptionProps) {
    super(props);
    this.state = {
      subDivisions: [],
      divisionValue: null,
      divisionError: '',
      loading: true,
      productName: null,
      productNameError: null,
      description: null,
      descriptionError: null,
      productPhaseValue: null,
      productPhaseError: null,
      statusValue: null,
      statusError: null,
      artValue: null,
      artError: null,
      integratedPortalError: null,
      integratedPortalsValue: null,
      designGuideError: null,
      designGuideValue: null,
      showFrontEndTechError: false ,
      frontEndTechValue: null,
      chips: [],
      showTagsMissingError: false,
      tags: [],
      showDepartmentMissingError: false,
      departmentTags: [],
      reportLink: null,
      reportLinkError: null,
      piiValue: null,
      piiError: null,
      procedureId: '',
      procedureIdError: null,
      selectedSimilarReportsType: "Description",
      showSimilarReportsModal: false,
      lastSearchedDescriptionInput: '',
      similarReportsBasedOnDescription: [],
      lastSearchedProductNameInput: '',
      similarReportsBasedOnProductName: [],
      similarReportstoShow: [],
      dataClassificationValue: null,
      dataClassificationError: null,
      archerId: null,
      archerIdError: '',
      relatedSolutions: [],
      relatedSolutionTags: []
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
    if(this.props?.description?.relatedSolutions?.length){
      const solTag = this.props?.description?.relatedSolutions.map(solution => solution.name);
      this.setState({relatedSolutionTags: solTag});
    }
  }

  componentDidUpdate(prevProps: IDescriptionProps, prevState: IDescriptionState) {
    if (prevProps.enableQuickPath !== this.props.enableQuickPath) {
      if (!this.props.enableQuickPath) {
        SelectBox.defaultSetup();
      }
    }
  }

  public onProductNameOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const productName = e.currentTarget.value;
    const description = this.props.description;
    description.productName = productName;
    // this.props.onStateChange();
    if (productName === '' || productName === null) {
      this.setState({ productNameError: '*Missing Entry' });
    } else {
      this.setState({ productNameError: '' });
    }
    this.setState({
      productName,
    });
  };

  public onProcedureIdOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const procedureId = e.currentTarget.value;
    const description = this.props.description;
    description.procedureId = procedureId;
    setTimeout(()=>{
      if(procedureId?.length > 0){
        if(!(procedureId?.startsWith(procedureIdEnvs[0]) || procedureId?.startsWith(procedureIdEnvs[1])) || (this.state.procedureId === procedureIdEnvs[0] || this.state.procedureId === procedureIdEnvs[1]) ) {
          this.setState({ procedureIdError: '*Please provide valid Procedure Id (' + procedureIdEnvs[0] + 'xxx) or (' + procedureIdEnvs[1]+'xxx)' });
        }else{
          this.setState({ procedureIdError: '' });
        }
        }else{
        this.setState({ procedureIdError: '' });
      }
    },10)
    this.setState({
      procedureId,
    });
  };

  public onArcherIdChange = (e: React.FocusEvent<HTMLInputElement>) => {
    const archerId = e.currentTarget.value;
    const pattern = /^(INFO)-\d{10}$/.test(archerId);
    const description = this.props.description;
    description.archerId = archerId;
    this.setState({ archerId: archerId, archerIdError: (archerId.length && !pattern ? 'Archer ID should be of type INFO-XXXXX' : '') });

  }

  public onRelatedSolutionsChange = (arr: string[]) => {
    const description = this.props.description;
    const tempSolutions = this.props.allSolutions.filter(solution => {
      if (arr.includes(solution.name)) {
        return solution;
      }
    });
    description.relatedSolutions = tempSolutions;
    this.setState({relatedSolutions: tempSolutions});

  }

  public onDataClassificationChange = (e: React.FocusEvent<HTMLSelectElement>) => {
    const dataClassification = e.currentTarget.value;
    const description = this.props.description;
    description.dataClassification = dataClassification;
    this.setState({
      dataClassificationValue: dataClassification,
    });

  }


  public onDescChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const desc = e.currentTarget.value;
    const description = this.props.description;
    description.productDescription = desc;
    // this.props.onStateChange();
    if (desc === '' || desc === null) {
      this.setState({ descriptionError: '*Missing Entry' });
    } else {
      this.setState({ descriptionError: '' });
    }
    this.setState({
      description: desc,
    });
  };




  public onDivisionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const target = e.target as HTMLSelectElement;
    const description = this.props.description;
    const division: IDivisionAndSubDivision = { id: '0', name: null, subdivision: { id: null, name: null } };
    const selectedOptions = target.selectedOptions;
    // this.props.onStateChange();
    if (selectedOptions.length) {
      division.id = selectedOptions[0].value;
      division.name = selectedOptions[0].textContent;
      if (division.id !== this.state.divisionValue.id) {
        if (division.id !== '0') {
          ProgressIndicator.show();

          ApiClient.getSubDivisions(division.id).then((subDivisions) => {
            if (!subDivisions.length) {
              subDivisions = [{ id: '0', name: 'None' }];
            }
            this.props.setSubDivisions(subDivisions);
            ProgressIndicator.hide();
          });
        }
        description.division = division;
        this.setState({ divisionValue: division });
      }
    }
  };

  public onSubDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const subDivision: ISubDivision = { id: null, name: null };
    // this.props.onStateChange();
    if (selectedOptions.length) {
      subDivision.id = selectedOptions[0].value;
      subDivision.name = selectedOptions[0].textContent;
    }
    const description = this.props.description;
    description.division.subdivision = subDivision;
    description.subdivision = subDivision.name;
    this.setState({ divisionValue: description.division });
  };

  public onProductPhaseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IProductPhase[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const productPhase: IProductPhase = { id: null, name: null };
        productPhase.id = option.value;
        productPhase.name = option.textContent;
        selectedValues.push(productPhase);
      });
    }
    const description = this.props.description;
    description.productPhase = selectedValues;
    this.setState({ productPhaseValue: selectedValues });
  };

  public onChangeART = (e: React.ChangeEvent<HTMLSelectElement>) => {
    // const selectedOptions = e.currentTarget.selectedOptions;
    // const selectedValues: IART[] = [];
    // if (selectedOptions.length) {
    //   Array.from(selectedOptions).forEach((option) => {
    //     const art: IART = { id: null, name: null };
    //     art.id = option.value;
    //     art.name = option.textContent;
    //     selectedValues.push(art);
    //   });
    // }
    // const description = this.props.description;
    // description.agileReleaseTrains = selectedValues;
    // this.setState({ artValue: selectedValues });

    const selectedOptions = e.currentTarget.selectedOptions;
    let selectedValue = '';
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValue = option.value;
      });
    }
    const description = this.props.description;
    description.agileReleaseTrain = selectedValue;
    this.setState({ piiValue: selectedValue });
  };


  public onChangeStatus = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IProductStatus[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const status: IProductStatus = { id: null, name: null };
        status.id = option.value;
        status.name = option.textContent;
        selectedValues.push(status);
      });
    }
    const description = this.props.description;
    description.status = selectedValues;
    this.setState({ statusValue: selectedValues, reportLinkError: '' });
  };
  public onChangePii = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    let selectedValues = '';
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        // const designGuide: IDesignGuide = { id: null, name: null };
        // designGuide.id = option.value;
        // designGuide.name = option.textContent;
        selectedValues = option.value;
      });
    }
    const description = this.props.description;
    description.piiData = selectedValues;
    this.setState({ piiValue: selectedValues });
  };

  public onChangeFrontTechnologies = (selectedValues: string[]) => {
    const description = this.props.description;
    description.frontendTechnologies = selectedValues;
    this.setState({ frontEndTechValue: selectedValues, showFrontEndTechError: false });
  }

  public showSimilarReports = (type: string) => {
    let similarReportstoShow: ISimilarSearchListItem[] = [];
    switch (type) {
      case 'Name': 
        similarReportstoShow = this.state.similarReportsBasedOnProductName;
        break;
      case 'Description':
        similarReportstoShow = this.state.similarReportsBasedOnDescription;
        break;
    }
    this.setState({ selectedSimilarReportsType: type, showSimilarReportsModal: true, similarReportstoShow });
  };

  public onFocusOut = (fieldType: string, data: string) => {
    const inputData = data.trim();
    if(inputData !== '') {
      switch(fieldType) {
        case 'Name':
          if(inputData === this.state.lastSearchedProductNameInput) return;
          this.setState({similarReportsBasedOnProductName: []});
          break;   
        case 'Description':
          if(inputData === this.state.lastSearchedDescriptionInput) return;
          this.setState({similarReportsBasedOnDescription: []});
          break;  
      }

      ApiClient.getSimilarSearchResult(`reportssearch?input=${inputData}`).then((res: any) => {
        if(res?.result?.length) {
          const similarReportsBasedOnInputData:ISimilarSearchListItem[] = [];
          res?.result.forEach((item: any) => {
            const reportItem = item[0];
            const selectedReportId = this.props.reportId;
            if (selectedReportId !== reportItem.id) {
              const score = item[1];
              similarReportsBasedOnInputData.push({
                id: reportItem.id,
                productName: reportItem.productName,
                description: reportItem.description,
                score
              });
            }
          });

          switch(fieldType) {
            case 'Name':
              this.setState({similarReportsBasedOnProductName: similarReportsBasedOnInputData, lastSearchedProductNameInput: inputData});
              break;    
            case 'Description':
              this.setState({similarReportsBasedOnDescription: similarReportsBasedOnInputData, lastSearchedDescriptionInput: inputData}); 
          }

          Notification.show(`Similar reports found based on your Report ${fieldType}.`);
          Tooltip.defaultSetup();
        }
      });
    }
  };

  public render() {
    const divisionError = this.state.divisionError || '';
    const productNameError = this.state.productNameError || '';
    const descriptionError = this.state.descriptionError || '';
    // const productPhaseError = this.state.productPhaseError || '';
    const statusError = this.state.statusError || '';
    const artError = this.state.artError || '';
    const piiError = this.state.piiError || '';
    const reportLinkError = this.state.reportLinkError || '';
    const procedureIdError = this.state.procedureIdError || '';

    const requiredError = '*Missing entry';

    // const productPhaseValue = this.state.productPhaseValue
    //   ?.map((productPhase: IProductPhase) => {
    //     return productPhase.name;
    //   })
    //   ?.toString();

    const frontEndTechValue = this.state.frontEndTechValue;

    const statusValue = this.state.statusValue
      ?.map((statusValue: IProductStatus) => {
        return statusValue.name;
      })
      ?.toString();


    const piiValue = this.state.piiValue;

    const artValue = this.state.artValue;


    const departmentValue = this.state.departmentTags?.map((department) => department?.toUpperCase());

    const scrollableContent = document.querySelector('.decriptionSection');
    if (scrollableContent) {
      scrollableContent.scrollTop = 0;
    }

    return (
      <React.Fragment>
        <div>
          <div className={classNames(Styles.wrapper)}>
            <div className={classNames(Styles.firstPanel, 'decriptionSection')}>
              <h3>Please give a report description</h3>
              <div className={classNames(Styles.formWrapper, this.props.enableQuickPath ? Styles.flexLayout : '')}>
                <div>
                  <div className={!this.props.enableQuickPath ? Styles.flexLayout : ''}>
                    <div>
                      <TextBox
                        type="text"
                        controlId={'reportNameInput'}
                        labelId={'reportNameLabel'}
                        label={'Report Name'}
                        placeholder={'Type here'}
                        value={this.state.productName}
                        errorText={productNameError}
                        required={true}
                        maxLength={200}
                        onChange={this.onProductNameOnChange}
                        onBlur={() => this.onFocusOut("Name", this.state.productName)}
                        infoContent={
                          this.state.similarReportsBasedOnProductName.length > 0 && (
                            <span
                              className={Styles.similarReportInfo}
                              onClick={() => this.showSimilarReports('Name')}
                              tooltip-data="Click to view"
                            >
                              <i className="icon mbc-icon alert circle" />
                              Similar Reports Found
                            </span>
                          )
                        }
                      />
                    </div>

                  </div>
                  <div>
                    <TextArea
                      controlId={'reportDecsriptionField'}
                      containerId={'reportDecsription'}
                      labelId={'reportDescriptionLabel'}
                      label={'Description'}
                      rows={50}
                      value={this.state.description}
                      errorText={descriptionError}
                      required={true}
                      onChange={this.onDescChange}
                      onBlur={() => this.onFocusOut("Description", this.state.description)}
                      infoContent={
                        this.state.similarReportsBasedOnDescription.length > 0 && (
                          <span
                            className={Styles.similarReportInfo}
                            onClick={() => this.showSimilarReports('Description')}
                            tooltip-data="Click to view"
                          >
                            <i className="icon mbc-icon alert circle" />
                            Similar Reports Found
                          </span>
                        )
                      }
                    />
                  </div>
                  <div>
                    <div className={Styles.frontEndTechTags}>
                          <Tags
                            title={'Frontend Technologies'}
                            max= {100}
                            chips={frontEndTechValue}
                            tags={this.props.frontEndTechnologies}
                            setTags={this.onChangeFrontTechnologies}
                            isMandatory={true}
                            showMissingEntryError={this.state.showFrontEndTechError}
                            showAllTagsOnFocus={true}
                            disableOnBlurAdd={true}
                            disableSelfTagAdd={true}
                          />
                        </div>
                  </div>
                </div>
                <div className={classNames(!this.props.enableQuickPath ? Styles.flexLayout : '')}>
                  <div>
                  <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '')}>
                  <div>
                    <div>
                      <div className={Styles.divisionContainer}>
                        <div
                          className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}
                        >
                          <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                            Division <sup>*</sup> &nbsp;
                            <i className="icon mbc-icon info" tooltip-data="Please select your Division" />
                          </label>
                          <div className="custom-select">
                            <select
                              id="divisionField"
                              required={true}
                              required-error={requiredError}
                              onChange={this.onDivisionChange}
                              value={this.state.divisionValue?.id}
                            >
                              <option id="divisionOption" value={0}>
                                Choose
                              </option>
                              {this.props.divisions?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                            {divisionError}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                    <div className={Styles.subDivisionContainer}>
                      <div className={classNames('input-field-group')}>
                        <label id="subDivisionLabel" htmlFor="subDivisionField" className="input-label">
                          Sub Division &nbsp;
                          <i className="icon mbc-icon info" tooltip-data="Please select your Sub Division" />
                        </label>
                        <div className="custom-select">
                          <select
                            id="subDivisionField"
                            onChange={this.onSubDivisionChange}
                            value={this.state.divisionValue?.subdivision?.id || '0'}
                          >
                            {this.state.subDivisions.some((item) => item.id === '0' && item.name === 'None') ? (
                              <option id="subDivisionDefault" value={0}>
                                None
                              </option>
                            ) : (
                              <>
                                <option id="subDivisionDefault" value={0}>
                                  Choose
                                </option>
                                {this.state.subDivisions?.map((obj) => (
                                  <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                    {obj.name}
                                  </option>
                                ))}
                              </>
                            )}
                          </select>
                        </div>
                      </div>
                    </div>
                    </div>
                    <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '')}>
                    <div
                      className={classNames(
                        'input-field-group include-error',
                        this.state.dataClassificationError ? 'error' : '',
                      )}
                    >
                      <label id="dataClassificationLabel" htmlFor="dataClassificationInput" className="input-label">
                        Data Classification<sup>*</sup>
                      </label>
                      <div className={`custom-select`}>
                        <select
                          required={true}
                          required-error={requiredError}
                          id="dataClassificationField"
                          name="dataClassification"
                          onChange={this.onDataClassificationChange}
                          value={this.state.dataClassificationValue}
                        >
                          <option id="dataProd" value={''}> Choose </option>
                          {this.props.dataClassifications?.map((item: any) => (
                            <option id={item.name + item.id} key={item.id} value={item.name}>
                              {item.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      <span className={classNames('error-message', this.state.dataClassificationError ? '' : 'hide')}>
                        {this.state.dataClassificationError}
                      </span>
                    </div>
                    <div className={classNames('input-field-group include-error', this.state.archerIdError.length ? 'error' : '')}>
                      <label className={classNames(Styles.inputLabel, 'input-label')}>Archer ID</label>
                      <div>
                        <input
                          type="text"
                          className={classNames('input-field', Styles.projectNameField)}
                          id="archerId"
                          placeholder="Type here eg.[INFO-XXXXX]"
                          autoComplete="off"
                          maxLength={55}
                          defaultValue={this.state.archerId}
                          value={this.state.archerId}
                          onChange={this.onArcherIdChange}
                        />
                        <span className={classNames('error-message', this.state.archerIdError.length ? '' : 'hide')}>
                          {this.state.archerIdError}
                        </span>
                      </div>
                    </div>
                  </div>

                    {!this.props.enableQuickPath ? (
                      <>
                        <div>
                          <div
                            className={classNames('input-field-group include-error', piiError.length ? 'error' : '')}
                          >
                            <label id="piiLabel" htmlFor="piiField" className="input-label">
                              PII(Personally Identifiable Information) <sup>*</sup>
                            </label>
                            <div className="custom-select">
                              <select
                                id="piiField"
                                required={true}
                                required-error={requiredError}
                                onChange={this.onChangePii}
                                value={piiValue}
                              >
                                <option value={''}>Choose</option>
                                <option id="Yes" key={'Yes'} value={'Yes'}>
                                  Yes
                                </option>
                                <option id="No" key={'No'} value={'No'}>
                                  No
                                </option>
                              </select>
                            </div>
                            <span className={classNames('error-message', piiError.length ? '' : 'hide')}>
                              {piiError}
                            </span>
                          </div>
                        </div>
                      </>
                    ) : (
                      ''
                    )}
                  </div>

                  <div>
                    <div>
                      <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '')}>
                        <div className={Styles.departmentTags}>
                          <Tags
                            title={'Department'}
                            max={1}
                            chips={departmentValue}
                            tags={this.props.departmentTags}
                            setTags={this.setDepartment}
                            isMandatory={true}
                            showMissingEntryError={this.state.showDepartmentMissingError}
                          />
                        </div>

                        <div>
                          <div
                            className={classNames('input-field-group include-error', statusError.length ? 'error' : '')}
                          >
                            <label id="reportStatusLabel" htmlFor="reportStatusField" className="input-label">
                              Status <sup>*</sup>
                            </label>
                            <div className="custom-select">
                              <select
                                id="reportStatusField"
                                // multiple={true}
                                required={true}
                                required-error={requiredError}
                                onChange={this.onChangeStatus}
                                value={statusValue}
                              >
                                <option id="reportStatusOption" value={0}>
                                  Choose
                                </option>
                                {this.props.statuses?.map((obj) => (
                                  <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                    {obj.name}
                                  </option>
                                ))}
                              </select>
                            </div>
                            <span className={classNames('error-message', statusError.length ? '' : 'hide')}>
                              {statusError}
                            </span>
                          </div>
                        </div>
                      </div>
                      {!this.props.enableQuickPath ? (
                        <div className={classNames('input-field-group include-error', artError ? 'error' : '')}>
                          <label id="ARTLabel" htmlFor="ARTField" className="input-label">
                            Agile Release Train
                          </label>
                          <div className={classNames('custom-select')}>
                            <select
                              id="ARTField"
                              multiple={false}
                              required={false}
                              onChange={this.onChangeART}
                              value={artValue}
                            >
                              <option id="agileReleaseTrainOption" value={0}>
                                Choose
                              </option>
                              {this.props.arts?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', artError ? '' : 'hide')}>{artError}</span>
                        </div>
                      ) : (
                        ''
                      )}
                  <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '' , Styles.divWrapper )}>
                      <div>
                        <div className={classNames('input-field-group')}>
                          <Tags
                            title={'Related Solutions'}
                            max={100}
                            chips={this.state.relatedSolutionTags}
                            tags={this.props.allSolutions.length > 0 ? this.props.allSolutions : []}
                            setTags={this.onRelatedSolutionsChange}
                            isMandatory={false}
                            showMissingEntryError = {false}
                            disableSelfTagAdd={true}
                            placeholder={this.props.allSolutions.length > 0 ? 'Type here' : 'Loading...' }
                          />
                        </div>
                      </div>
                      <div  className={classNames('input-field-group include-error', procedureIdError ? 'error' : '', !this.props.enableQuickPath ? Styles.procedureId :'' )}>
                        <TextBox
                          type="text"
                          controlId={'procedureIdInput'}
                          labelId={'procedureIdLabel'}
                          label={'Procedure ID'}
                          placeholder={'Type here'}
                          infoTip={
                            'Procedure ID ' +
                            (procedureIdEnvs ? '(' + procedureIdEnvs[0] + 'xxx) or (' + procedureIdEnvs[1] + 'xxx)' : '') +
                            ' from Records of Processing Activities (RoPA)'
                          }
                          value={this.state.procedureId}
                          required={false}
                          maxLength={200}
                          onChange={(e)=>this.onProcedureIdOnChange(e)}
                        />
                       <span className={classNames('error-message', procedureIdError ? '' : 'hide', Styles.procedureIdError)} >{procedureIdError}</span>
                      </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          {!this.props.enableQuickPath ? (
            <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
              <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
                <div id="tagsContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
                  <h3 id="tagHeading">Tags</h3>
                  <span id="tagDesc" className={classNames(Styles.textDesc)}>
                    Use tags to make it easier to find your report for other people
                  </span>
                  <div>
                    <Tags
                      title={'Tags'}
                      max={100}
                      chips={this.state.tags}
                      setTags={this.setTags}
                      isMandatory={false}
                      showMissingEntryError={false}
                      {...this.props}
                    />
                  </div>
                </div>
              </div>
            </div>
          ) : (
            ''
          )}
          <div id="linkWrapper" className={classNames(Styles.wrapper)}>
            <div id="linkPanel" className={classNames(Styles.firstPanel)}>
              <div id="linkContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
                <h3 id="linkHeading">Link</h3>
                <span id="linkDesc" className={classNames(Styles.textDesc)}>
                  Link the report here
                </span>
                <div>
                  <TextBox
                    type="text"
                    controlId={'reportLinkInput'}
                    labelId={'reportLinkLabel'}
                    label={'Report Link'}
                    placeholder={'Type here'}
                    value={this.state.reportLink}
                    errorText={reportLinkError}
                    required={this.state.statusValue ? this.state.statusValue[0]?.id === 'Active' : false}
                    maxLength={200}
                    onChange={this.onChangeUrl}
                  />
                </div>
              </div>
            </div>
          </div>
          {this.state.showSimilarReportsModal && (
            <SimilarSearchListModal
              setShowSimilarSearchListModal={(showSimilarReportsModal: boolean) => this.setState({ showSimilarReportsModal })}
              similarSearchList={this.state.similarReportstoShow}
              searchBasedOnInputType={this.state.selectedSimilarReportsType}
              isReportSearchType={true}
            />
          )}
          <div className="btnConatiner">
            {!this.props.enableQuickPath ? (
              <button className="btn btn-primary" type="button" onClick={this.onDescriptionSubmit}>
                Save & Next
              </button>
            ) : (
              <div>
                <button
                  className={classNames('btn btn-primary', Styles.refineReportButton)}
                  type="button"
                  onClick={this.props.refineReport}
                >
                  Refine Report
                </button>
                <button className="btn btn-tertiary" type="button" onClick={this.onDescriptionSubmitWithQuickPath}>
                  Publish Report
                </button>
              </div>
            )}
          </div>
        </div>
      </React.Fragment>
    );
  }

  protected onDescriptionSubmit = () => {
    if (this.validateDescriptionForm()) {
      this.props.modifyReportDescription(this.props.description);
      this.props.onSaveDraft('description');
    }
  };

  protected onDescriptionSubmitWithQuickPath = () => {
    if (this.validateQuickpathDescriptionForm()) {
      this.props.modifyReportDescription(this.props.description);
      this.props.onSaveDraft('quickpath');
    }
  };

  protected validateDescriptionForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if(this.state.procedureId){
      if (this.state.procedureId === procedureIdEnvs[0] || this.state.procedureId === procedureIdEnvs[1]) {
        this.setState({ procedureIdError: '*Please provide valid Procedure Id (' + procedureIdEnvs[0] + 'xxx) or (' + procedureIdEnvs[1] + 'xxx)' });
        formValid = false;
      } else if (!(this.state.procedureId?.startsWith(procedureIdEnvs[0]) || this.state.procedureId?.startsWith(procedureIdEnvs[1]))) {
        this.setState({ procedureIdError: '*Please provide valid Procedure Id (' + procedureIdEnvs[0] + 'xxx) or (' + procedureIdEnvs[1] + 'xxx)' });
        formValid = false;
      } else {
        this.setState({ procedureIdError: '' });
      } 

    }

    if (!this.state.divisionValue || this.state.divisionValue.id === '0' || this.state.divisionValue.id === '') {
      this.setState({ divisionError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.divisionValue || this.state.divisionValue.subdivision.id === '0') {
      const subdivision = this.state.divisionValue.subdivision;
      subdivision.id = null;
      subdivision.name = null;
      this.setState({
        divisionValue: {
          id: this.state.divisionValue.id,
          name: this.state.divisionValue.name,
          subdivision,
        },
      });
    }

    if (!this.state.productName || this.state.productName === '') {
      this.setState({ productNameError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.productNameError && this.state.productName) {
      this.setState({ productNameError: '' });
      // formValid = true;
    }
    if (!this.state.description || this.state.description === '') {
      this.setState({ descriptionError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.descriptionError && this.state.description) {
      this.setState({ descriptionError: '' });
      // formValid = true;
    }
    // if (!this.state.productPhaseValue || this.state.productPhaseValue[0].name === 'Choose') {
    //   this.setState({ productPhaseError: errorMissingEntry });
    //   formValid = false;
    // }
    if (!this.state.statusValue || this.state.statusValue[0].name === 'Choose') {
      this.setState({ statusError: errorMissingEntry });
      formValid = false;
    }
    // if (!this.state.tags.length) {
    //   this.setState({ showTagsMissingError: true });
    //   formValid = false;
    // }
    if (!this.state.departmentTags?.length) {
      this.setState({ showDepartmentMissingError: true });
      formValid = false;
    }
    if(this.state.procedureIdError && this.state.procedureId){
      formValid = false;
    }
    // if (!this.state.artValue?.length) {
    //   this.setState({ artError: errorMissingEntry });
    //   formValid = false;
    // }
    // if (!this.state.integratedPortalsValue?.length) {
    //   this.setState({ integratedPortalError: errorMissingEntry });
    //   formValid = false;
    // }
    if (!this.state.piiValue || this.state.piiValue === 'Choose') {
      this.setState({ piiError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.frontEndTechValue.length === 0) {
      this.setState({ showFrontEndTechError: true });
      formValid = false;
    }
    // if (this.state.reportLinkError && this.state.reportLink) {
    //   this.setState({ reportLinkError: '' });
    //   // formValid = true;
    // }
    if (
      (!this.state.reportLink || this.state.reportLink === null || this.state.reportLink === '') &&
      this.state.statusValue[0]?.id === 'Active'
    ) {
      this.setState({ reportLinkError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.dataClassificationValue === '' || this.state.dataClassificationValue === null) {
      this.setState({ dataClassificationError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.archerIdError !== '') {
      formValid = false;
    }

    // if (procedureIdEnvs && (this.state.procedureId.split('-')[0]!== procedureIdEnvs || this.state.procedureId.split('-')[1] === '')) {

    if (this.state.procedureIdError) {
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };

  protected validateQuickpathDescriptionForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';

    if (!this.state.divisionValue || this.state.divisionValue.id === '0' || this.state.divisionValue.id === '') {
      this.setState({ divisionError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.divisionValue || this.state.divisionValue.subdivision.id === '0') {
      const subdivision = this.state.divisionValue.subdivision;
      subdivision.id = null;
      subdivision.name = null;
      this.setState({
        divisionValue: {
          id: this.state.divisionValue.id,
          name: this.state.divisionValue.name,
          subdivision,
        },
      });
    }

    if (!this.state.productName || this.state.productName === '') {
      this.setState({ productNameError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.productNameError && this.state.productName) {
      this.setState({ productNameError: '' });
      // formValid = true;
    }
    if (!this.state.description || this.state.description === '') {
      this.setState({ descriptionError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.descriptionError && this.state.description) {
      this.setState({ descriptionError: '' });
      // formValid = true;
    }

    if (!this.state.statusValue || this.state.statusValue[0].name === 'Choose') {
      this.setState({ statusError: errorMissingEntry });
      formValid = false;
    }
  
    if (this.state.dataClassificationValue === '' || this.state.dataClassificationValue === null) {
      this.setState({ dataClassificationError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.departmentTags?.length) {
      this.setState({ showDepartmentMissingError: true });
      formValid = false;
    }
    if (!this.state.frontEndTechValue || this.state.frontEndTechValue.length === 0) {
      this.setState({ showFrontEndTechError: true });
      formValid = false;
    }
    if (
      (!this.state.reportLink || this.state.reportLink === null || this.state.reportLink === '') &&
      this.state.statusValue[0]?.id === 'Active'
    ) {
      this.setState({ reportLinkError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.reportLinkError && this.state.reportLink) {
      this.setState({ reportLinkError: '' });
      // formValid = true;
    }
    if(this.state.procedureIdError && this.state.procedureId){
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };

  protected clear = () => {
    const selectElemets = document.querySelectorAll('.select-selected');
    // tslint:disable-next-line: prefer-for-of
    for (let i = 0; i < selectElemets.length; i++) {
      selectElemets[i].remove();
    }
  };
  protected setTags = (arr: string[]) => {
    const description = this.props.description;
    description.tags = arr;
    // this.props.onStateChange();
    this.setState({ showTagsMissingError: arr.length === 0 });
  };

  protected setDepartment = (arr: string[]) => {
    const description = this.props.description;
    description.department = arr?.map((item) => item.toUpperCase());
    this.setState({ showDepartmentMissingError: arr.length === 0 });
  };

  protected onChangeUrl = (e: React.FormEvent<HTMLInputElement>) => {
    const reportLink = e.currentTarget.value;
    const description = this.props.description;
    description.reportLink = reportLink;
    if (
      (reportLink === '' || reportLink === null) &&
      (this.state.statusValue ? this.state.statusValue[0]?.id === 'Active' : false)
    ) {
      this.setState({ reportLinkError: '*Missing Entry' });
    } else {
      this.setState({ reportLinkError: '' });
    }
    // this.setState({
    //   reportLink,
    // });
    this.props.modifyReportDescription(description);
  };
}