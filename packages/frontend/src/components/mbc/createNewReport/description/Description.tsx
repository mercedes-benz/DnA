import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
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
} from 'globals/types';
import Styles from './Description.scss';
import Tags from 'components/formElements/tags/Tags';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { ApiClient } from '../../../../services/ApiClient';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import TextArea from 'components/mbc/shared/textArea/TextArea';
import { Envs } from 'globals/Envs';

const classNames = cn.bind(Styles);
const procedureIdEnvs = Envs.ROPA_PROCEDURE_ID_PREFIX;

export interface IDescriptionProps {
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
  frontEndTechError: string;
  chips: string[];
  showTagsMissingError: boolean;
  tags: string[];
  showDepartmentMissingError: boolean;
  departmentTags: string[];
  reportLink: string;
  reportLinkError: string;
  reportTypeValue: string;
  reportTypeError: string;
  piiValue: string;
  piiError: string;
  procedureId: string;
  procedureIdError: string;
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
      integratedPortalsValue: props.description.integratedPortal,
      tags: props.description.tags,
      departmentTags: props.description.department,
      reportLink: props.description.reportLink,
      reportTypeValue: props.description.reportType,
      piiValue: props.description.piiData,
      procedureId: props.description.procedureId 
    };
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
      frontEndTechError: null,
      frontEndTechValue: null,
      chips: [],
      showTagsMissingError: false,
      tags: [],
      showDepartmentMissingError: false,
      departmentTags: [],
      reportLink: null,
      reportLinkError: null,
      reportTypeValue: 'Self Service Report',
      reportTypeError: null,
      piiValue: null,
      piiError: null,
      procedureId: '',
      procedureIdError: null
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
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
    // this.props.onStateChange();
    if (procedureId === '' || procedureId === null) {
      this.setState({ procedureIdError: '*Missing Entry' });
    } else {
      this.setState({ procedureIdError: '' });
    }
    this.setState({
      procedureId,
    });
  };

  public onProcedureIdOnBlur = (e: React.FormEvent<HTMLInputElement>) => {
    if(procedureIdEnvs){
      const procedureId = e.currentTarget.value;
      if (procedureId.split('-')[0]!== procedureIdEnvs || procedureId.split('-')[1] === '') {
        this.setState({ procedureIdError: '*Please provide valid Procedure Id ('+procedureIdEnvs+'-xxx).' });
      } else {
        this.setState({ procedureIdError: '' });
      }
    }
  };

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

  public onReportTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    let selectedValues = '';
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        // const reportType: IReportType = { id: null, name: null };
        // reportType.id = option.value;
        // reportType.name = option.textContent;
        // selectedValues.push(reportType);
        selectedValues = option.value;
      });
    }
    const description = this.props.description;
    description.reportType = selectedValues;
    this.setState({ reportTypeValue: selectedValues });
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
      if (division.id !== '0' && division.id !== this.state.divisionValue.id) {
        ProgressIndicator.show();

        ApiClient.getSubDivisions(division.id).then((subDivisions) => {
          if (!subDivisions.length) {
            subDivisions = [{ id: '0', name: 'None' }];
          }
          this.props.setSubDivisions(subDivisions);
          ProgressIndicator.hide();
        });
      }
    }
    description.division = division;
    this.setState({ divisionValue: division });
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

  public onChangeItegratedPortal = (e: React.ChangeEvent<HTMLSelectElement>) => {
    // const selectedOptions = e.currentTarget.selectedOptions;
    // const selectedValues: IIntegratedPortal[] = [];
    // if (selectedOptions.length) {
    //   Array.from(selectedOptions).forEach((option) => {
    //     const integratedPortal: IIntegratedPortal = { id: null, name: null };
    //     integratedPortal.id = option.value;
    //     integratedPortal.name = option.textContent;
    //     selectedValues.push(integratedPortal);
    //   });
    // }
    // const description = this.props.description;
    // description.integratedPortal = selectedValues;
    // this.setState({ integratedPortalsValue: selectedValues });
    
    const selectedOptions = e.currentTarget.selectedOptions;
    let selectedValue = '';
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValue = option.value;
      });
    }
    const description = this.props.description;
    description.integratedPortal = selectedValue;
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


  public onChangeFrontTechnologies = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        let frontEndTech = '';
        frontEndTech = option.textContent;
        selectedValues.push(frontEndTech);
      });
    }
    const description = this.props.description;
    description.frontendTechnologies = selectedValues;
    this.setState({ frontEndTechValue: selectedValues });
  };

  public render() {
    const divisionError = this.state.divisionError || '';
    const productNameError = this.state.productNameError || '';
    const descriptionError = this.state.descriptionError || '';
    // const productPhaseError = this.state.productPhaseError || '';
    const statusError = this.state.statusError || '';
    const artError = this.state.artError || '';
    const integratedPortalError = this.state.integratedPortalError || '';
    const piiError = this.state.piiError || '';
    const frontEndTechError = this.state.frontEndTechError || '';
    const reportLinkError = this.state.reportLinkError || '';
    const reportTypeError = this.state.reportTypeError || '';
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

    const reportTypeValue = this.state.reportTypeValue ? this.state.reportTypeValue : 'Self Service Report';  

    const piiValue = this.state.piiValue;

    const artValue = this.state.artValue;

    const integratedInPortalValue = this.state.integratedPortalsValue;

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
                      />
                    </div>
                    {!this.props.enableQuickPath ? (
                      // <div>
                      //   <TextBox
                      //     type="text"
                      //     controlId={'reportTypeInput'}
                      //     labelId={'reportTypeLabel'}
                      //     label={'Report Type'}
                      //     placeholder={'Type here'}
                      //     value={this.state.productName}
                      //     errorText={productNameError}
                      //     required={true}
                      //     maxLength={200}
                      //     onChange={this.onProductNameOnChange}
                      //   />
                      // </div>

                      <div className={classNames('input-field-group include-error', reportTypeError ? 'error' : '')}>
                        <label id="reportTypeLabel" htmlFor="reportTypeField" className="input-label">
                          Report Type<sup>*</sup>
                        </label>
                        <div className="custom-select">
                          <select
                            id="reportTypeField"
                            required={true}
                            required-error={requiredError}
                            onChange={this.onReportTypeChange}
                            value={reportTypeValue}
                          >
                            <option id="reportTypeOption" value={0}>
                              Choose
                            </option>
                            <option id="StandardReport" value={'Standard Report'}>
                              Standard Report
                            </option>
                            <option id="SelfServiceReport" value={'Self Service Report'}>
                              Self Service Report
                            </option>
                            {/* {this.props.divisions?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                {obj.name}
                              </option>
                            ))} */}
                          </select>
                        </div>
                        <span className={classNames('error-message', reportTypeError ? '' : 'hide')}>
                          {reportTypeError}
                        </span>
                      </div>
                    ) : (
                      ''
                    )}
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
                    />
                  </div>
                </div>
                <div className={classNames(!this.props.enableQuickPath ? Styles.flexLayout : '')}>
                  <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '')}>
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
                    {!this.props.enableQuickPath ? (
                      <>
                        <div>
                          <div
                            className={classNames(
                              'input-field-group include-error',
                              integratedPortalError ? 'error' : '',
                            )}
                          >
                            <label
                              id="integratedPortalLabel"
                              htmlFor="integratedPortalField"
                              className="input-label"
                            >
                              Integrated In Portal
                            </label>
                            <div className="custom-select">
                              <select
                                id="integratedPortalField"
                                multiple={false}
                                required={false}
                                onChange={this.onChangeItegratedPortal}
                                value={integratedInPortalValue}
                              >
                                <option id="integratedPortalOption" value={0}>
                                  Choose
                                </option>
                                {this.props.integratedPortals?.map((obj) => (
                                  <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                    {obj.name}
                                  </option>
                                ))}
                              </select>
                            </div>
                            <span className={classNames('error-message', integratedPortalError ? '' : 'hide')}>
                              {integratedPortalError}
                            </span>
                          </div>
                        </div>
                        <div>
                          <div
                            className={classNames(
                              'input-field-group include-error',
                              piiError.length ? 'error' : '',
                            )}
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
                    <div>
                      <div
                        className={classNames('input-field-group include-error', frontEndTechError ? 'error' : '')}
                      >
                        <label id="FrontEndTechnogies" htmlFor="FrontEndTechnogiesField" className="input-label">
                          Frontend Technologies <sup>*</sup>
                        </label>
                        <div id="FrontEndTechnogies" className="custom-select">
                          <select
                            id="FrontEndTechnogiesField"
                            multiple={true}
                            required={true}
                            required-error={requiredError}
                            onChange={this.onChangeFrontTechnologies}
                            value={frontEndTechValue}
                          >
                            {this.props.frontEndTechnologies?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className={classNames('error-message', frontEndTechError ? '' : 'hide')}>
                          {frontEndTechError}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div>
                    <div>
                      <div className={classNames(this.props.enableQuickPath ? Styles.flexLayout : '')}>
                        <div className={Styles.departmentTags}>
                          <Tags
                            title={'E2-Department'}
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
                      <div>
                        <TextBox
                          type="text"
                          controlId={'procedureIdInput'}
                          labelId={'procedureIdLabel'}
                          label={'Procedure ID'}
                          placeholder={'Type here'}
                          infoTip={'Procedure ID '+ (procedureIdEnvs ? ('('+procedureIdEnvs+'-xxx)'): '')+' from Records of Processing Activities (RoPA)'}
                          value={this.state.procedureId}
                          errorText={procedureIdError}
                          required={true}
                          maxLength={200}
                          onChange={this.onProcedureIdOnChange}
                          onBlur={this.onProcedureIdOnBlur}
                        />                        
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

    if (!this.state.divisionValue || this.state.divisionValue.id === '0') {
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
    // if (!this.state.artValue?.length) {
    //   this.setState({ artError: errorMissingEntry });
    //   formValid = false;
    // }
    // if (!this.state.integratedPortalsValue?.length) {
    //   this.setState({ integratedPortalError: errorMissingEntry });
    //   formValid = false;
    // }
    if (!this.state.reportTypeValue || this.state.reportTypeValue === '0') {
      this.setState({ reportTypeError: errorMissingEntry });
      formValid = false;
    }
    if (!this.state.piiValue || this.state.piiValue === 'Choose') {
      this.setState({ piiError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.frontEndTechValue.length === 0) {
      this.setState({ frontEndTechError: errorMissingEntry });
      formValid = false;
    }
    if ((!this.state.reportLink || this.state.reportLink === null || this.state.reportLink === '') && this.state.statusValue[0]?.id === 'Active') {
      this.setState({ reportLinkError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.reportLinkError && this.state.reportLink) {
      this.setState({ reportLinkError: '' });
      // formValid = true;
    }
    if (procedureIdEnvs && (this.state.procedureId.split('-')[0]!== procedureIdEnvs || this.state.procedureId.split('-')[1] === '')) {
      this.setState({ procedureIdError: '*Please provide valid Procedure Id ('+procedureIdEnvs+'-xxx).' });
      formValid = false;
    }
    if ((!procedureIdEnvs || procedureIdEnvs == '' || procedureIdEnvs == null) && (this.state.procedureId ==='' || !this.state.procedureId)) {
      this.setState({ procedureIdError: errorMissingEntry });
      formValid = false;
    }
    if(this.state.procedureIdError) {
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

    if (!this.state.divisionValue || this.state.divisionValue.id === '0') {
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

    if (!this.state.departmentTags?.length) {
      this.setState({ showDepartmentMissingError: true });
      formValid = false;
    }
    if (!this.state.frontEndTechValue || this.state.frontEndTechValue.length === 0) {
      this.setState({ frontEndTechError: errorMissingEntry });
      formValid = false;
    }
    if ((!this.state.reportLink || this.state.reportLink === null || this.state.reportLink === '') && this.state.statusValue[0]?.id === 'Active') {
      this.setState({ reportLinkError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.reportLinkError && this.state.reportLink) {
      this.setState({ reportLinkError: '' });
      // formValid = true;
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
    if ((reportLink === '' || reportLink === null) && (this.state.statusValue ? this.state.statusValue[0]?.id === 'Active' : false)) {
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
