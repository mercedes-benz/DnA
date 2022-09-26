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
  IIntegratedInPortal,
  IDesignGuide,
  IFrontEndTech,
  IIntegratedPortal,
  IDescriptionRequest,
  IART,
  IDivision,
  ISubDivision,
  IDivisionAndSubDivision,
  IDepartment,
} from '../../../../globals/types';
import Styles from './Description.scss';
import Tags from '../../../formElements/tags/Tags';
import SelectBox from '../../../../components/formElements/SelectBox/SelectBox';
import { ApiClient } from '../../../../services/ApiClient';
import TextBox from '../../shared/textBox/TextBox';
import TextArea from '../../shared/textArea/TextArea';
const classNames = cn.bind(Styles);

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
  artValue: IART[];
  artError: string;
  integratedPortalsValue: IIntegratedInPortal[];
  integratedPortalError: string;
  designGuideValue: IDesignGuide[];
  designGuideError: string;
  frontEndTechValue: IFrontEndTech[];
  frontEndTechError: string;
  chips: string[];
  showTagsMissingError: boolean;
  tags: string[];
  showDepartmentMissingError: boolean;
  departmentTags: string[];
  reportLink: string;
  reportLinkError: string;
}

export default class Description extends React.Component<IDescriptionProps, IDescriptionState> {
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
      artValue: props.description.agileReleaseTrains,
      integratedPortalsValue: props.description.integratedPortal,
      tags: props.description.tags,
      departmentTags: props.description.department,
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
      reportLinkError: null
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  componentDidUpdate(prevProps: IDescriptionProps, prevState: IDescriptionState) {
    if (prevProps.enableQuickPath !== this.props.enableQuickPath) {
      if(!this.props.enableQuickPath){
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
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IART[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const art: IART = { id: null, name: null };
        art.id = option.value;
        art.name = option.textContent;
        selectedValues.push(art);
      });
    }
    const description = this.props.description;
    description.agileReleaseTrains = selectedValues;
    this.setState({ artValue: selectedValues });
  };

  public onChangeItegratedPortal = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IIntegratedPortal[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const integratedPortal: IIntegratedPortal = { id: null, name: null };
        integratedPortal.id = option.value;
        integratedPortal.name = option.textContent;
        selectedValues.push(integratedPortal);
      });
    }
    const description = this.props.description;
    description.integratedPortal = selectedValues;
    this.setState({ integratedPortalsValue: selectedValues });
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
    this.setState({ statusValue: selectedValues });
  };
  public onChangeDesignGuideInpl = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IDesignGuide[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const designGuide: IDesignGuide = { id: null, name: null };
        designGuide.id = option.value;
        designGuide.name = option.textContent;
        selectedValues.push(designGuide);
      });
    }
    const description = this.props.description;
    description.designGuideImplemented = selectedValues;
    this.setState({ designGuideValue: selectedValues });
  };

  public onChangeFrontTechnologies = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IFrontEndTech[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const frontEndTech: IFrontEndTech = { id: null, name: null };
        frontEndTech.id = option.value;
        frontEndTech.name = option.textContent;
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
    const productPhaseError = this.state.productPhaseError || '';
    const statusError = this.state.statusError || '';
    const artError = this.state.artError || '';
    const integratedPortalError = this.state.integratedPortalError || '';
    const designGuidError = this.state.designGuideError || '';
    const frontEndTechError = this.state.frontEndTechError || '';
    const reportLinkError = this.state.reportLinkError || '';

    const requiredError = '*Missing entry';

    const productPhaseValue = this.state.productPhaseValue
      ?.map((productPhase: IProductPhase) => {
        return productPhase.name;
      })
      ?.toString();

    const frontEndTechValue = this.state.frontEndTechValue
      ?.map((frontEndTech: IFrontEndTech) => {
        return frontEndTech.name;
      })
      ?.toString();

    const statusValue = this.state.statusValue
      ?.map((statusValue: IProductStatus) => {
        return statusValue.name;
      })
      ?.toString();

    const designGuideValue = this.state.designGuideValue
      ?.map((designGuide: IDesignGuide) => {
        return designGuide.name;
      })
      ?.toString();

    const artValue = this.state.artValue?.map((art: IART) => {
      return art.name;
    });

    const integratedInPortalValue = this.state.integratedPortalsValue?.map((integrated: IIntegratedPortal) => {
      return integrated.name;
    });

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
              <h3>Please give a detailed report description</h3>
              <div className={classNames(Styles.formWrapper)}>
                <div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'reportNameInput'}
                      labelId={'reportNameLabel'}
                      label={'Report Name'}
                      placeholder={"Type here"}
                      value={this.state.productName}
                      errorText={productNameError}
                      required={true}
                      maxLength={200}
                      onChange={this.onProductNameOnChange}
                    />
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
                <div className={classNames(Styles.flexLayout)}>
                  {!this.props.enableQuickPath ? 
                  <div>
                    <div>
                      <div className={classNames(Styles.flexLayout)}>
                        <div className={Styles.divisionContainer}>
                          <div
                            className={classNames(
                              'input-field-group include-error',
                              divisionError.length ? 'error' : '',
                            )}
                          >
                            <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                              Division<sup>*</sup>
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
                        <div className={Styles.subDivisionContainer}>
                          <div className={classNames('input-field-group')}>
                            <label id="subDivisionLabel" htmlFor="subDivisionField" className="input-label">
                              Sub Division
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

                      <div>
                        <div
                          className={classNames(
                            'input-field-group include-error',
                            productPhaseError.length ? 'error' : '',
                          )}
                        >
                          <label id="reportProductPhaseLabel" htmlFor="reportStatusField" className="input-label">
                            Product Phase<sup>*</sup>
                          </label>
                          <div className="custom-select">
                            <select
                              id="reportProductPhaseField"
                              required={true}
                              required-error={requiredError}
                              onChange={this.onProductPhaseChange}
                              value={productPhaseValue}
                            >
                              <option id="reportProductPhaseOption" value={0}>
                                Choose
                              </option>
                              {this.props.productPhases?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', productPhaseError.length ? '' : 'hide')}>
                            {productPhaseError}
                          </span>
                        </div>
                      </div>
                      <div>
                        <div
                          className={classNames(
                            'input-field-group include-error',
                            integratedPortalError.length ? 'error' : '',
                          )}
                        >
                          <label id="integratedPortalLabel" htmlFor="integratedPortalField" className="input-label">
                            Integrated In Portal
                          </label>
                          <div className="custom-select">
                            <select
                              id="integratedPortalField"
                              multiple={true}
                              required={false}
                              onChange={this.onChangeItegratedPortal}
                              value={integratedInPortalValue}
                            >
                              {this.props.integratedPortals?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', integratedPortalError.length ? '' : 'hide')}>
                            {integratedPortalError}
                          </span>
                        </div>
                      </div>
                      <div>
                        <div
                          className={classNames(
                            'input-field-group include-error',
                            designGuidError.length ? 'error' : '',
                          )}
                        >
                          <label id="designGuidImplLabel" htmlFor="designGuidImplField" className="input-label">
                            Design Guide Implemented<sup>*</sup>
                          </label>
                          <div className="custom-select">
                            <select
                              id="designGuidImplField"
                              // multiple={true}
                              required={true}
                              required-error={requiredError}
                              onChange={this.onChangeDesignGuideInpl}
                              value={designGuideValue}
                            >
                              <option value={''}>Choose</option>
                              {this.props.designGuideImplemented?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', designGuidError.length ? '' : 'hide')}>
                            {designGuidError}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  :''}
                  <div>
                    <div>
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
                            Status<sup>*</sup>
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
                      {!this.props.enableQuickPath ?
                      <div className={classNames('input-field-group include-error', artError.length ? 'error' : '')}>
                        <label id="ARTLabel" htmlFor="ARTField" className="input-label">
                          Agile Release Train
                        </label>
                        <div className="custom-select">
                          <select
                            id="ARTField"
                            multiple={true}
                            required={false}
                            onChange={this.onChangeART}
                            value={artValue}
                          >
                            {this.props.arts?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className={classNames('error-message', artError.length ? '' : 'hide')}>{artError}</span>
                      </div>
                      : ''}
                      <div>
                        <div
                          className={classNames(
                            'input-field-group include-error',
                            frontEndTechError.length ? 'error' : '',
                          )}
                        >
                          <label id="FrontEndTechnogies" htmlFor="FrontEndTechnogiesField" className="input-label">
                            Frontend Technologies <sup>*</sup>
                          </label>
                          <div className="custom-select">
                            <select
                              id="FrontEndTechnogiesField"
                              // multiple={true}
                              required={true}
                              required-error={requiredError}
                              onChange={this.onChangeFrontTechnologies}
                              value={frontEndTechValue}
                            >
                              <option id="reportFrontEnTechsOption" value={0}>
                                Choose
                              </option>
                              {this.props.frontEndTechnologies?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', frontEndTechError.length ? '' : 'hide')}>
                            {frontEndTechError}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          {!this.props.enableQuickPath ?
          (
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
          ): ''}
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
                      placeholder={"Type here"}
                      value={this.state.reportLink}
                      required={true}
                      maxLength={200}
                      onChange={this.onGitUrl}
                      errorText={reportLinkError}
                    />
                  </div>
                </div>
              </div>
            </div>
          <div className="btnConatiner">
          {!this.props.enableQuickPath ?
            <button className="btn btn-primary" type="button" onClick={this.onDescriptionSubmit}>
              Save & Next
            </button>
          : 
          <button className="btn btn-primary" type="button" onClick={this.onDescriptionSubmitWithQuickPath}>
            Publish Report
          </button>
          }  
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
    // if (this.validateDescriptionForm()) {
      this.props.modifyReportDescription(this.props.description);
      this.props.onSaveDraft('quickpath');
    // }
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
      formValid = true;
    }
    if (!this.state.description || this.state.description === '') {
      this.setState({ descriptionError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.descriptionError && this.state.description) {
      this.setState({ descriptionError: '' });
      formValid = true;
    }
    if (!this.state.productPhaseValue || this.state.productPhaseValue[0].name === 'Choose') {
      this.setState({ productPhaseError: errorMissingEntry });
      formValid = false;
    }
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
    if (!this.state.designGuideValue || this.state.designGuideValue[0].name === 'Choose') {
      this.setState({ designGuideError: errorMissingEntry });
      formValid = false;
    }
    if (!this.state.frontEndTechValue || this.state.frontEndTechValue[0].name === 'Choose') {
      this.setState({ frontEndTechError: errorMissingEntry });
      formValid = false;
    }
    if (!this.state.reportLinkError || this.state.reportLink === '') {
      this.setState({ reportLinkError: errorMissingEntry });
      formValid = false;
    }
    if (this.state.reportLinkError && this.state.reportLink) {
      this.setState({ reportLinkError: '' });
      formValid = true;
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

  protected onGitUrl = (e: React.FormEvent<HTMLInputElement>) => {
    const gitUrl = e.currentTarget.value;
    const description = this.props.description;
    description.reportLink = gitUrl;
    // this.setState({
    //   description,
    // });
    this.props.modifyReportDescription(description);
  };
}
