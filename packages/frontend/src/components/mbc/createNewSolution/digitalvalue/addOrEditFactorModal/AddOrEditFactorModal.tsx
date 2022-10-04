import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import {
  ICostFactor,
  ICostRampUp,
  ICostRampUpError,
  IValueFactor,
  IValueRampUp,
  IValueRampUpError,
} from 'globals/types';
// @ts-ignore
import InputFieldsUtils from 'components/formElements/InputFields/InputFieldsUtils';
import Modal from 'components/formElements/modal/Modal';
import Styles from './AddOrEditFactorModal.scss';
import NumberFormat from 'react-number-format';
import { thousandSeparator, decimalSeparator } from '../../../../../services/utils';
import TextBox from 'components/mbc/shared/textBox/TextBox';

const classNames = cn.bind(Styles);

export interface IAddOrEditFactorModalProps {
  factorId?: string;
  editMode: boolean;
  showAddOrEditFactorModal: boolean;
  factorItem: ICostFactor | IValueFactor;
  onAddOrEditFactorItem: (factorItem: ICostFactor | IValueFactor, isEdit: boolean) => void;
  onAddOrEditFactorModalCancel: (costDrivers: ICostFactor[] | IValueFactor[]) => void;
}

export interface IAddOrEditFactorModalState {
  description: string;
  descriptionError: string;
  category: string;
  categoryError: string;
  value: string;
  valueError: string;
  source: string;
  sourceError: string;
  rampUp: ICostRampUp[];
  tempRampUp: string;
  rampUpValue: IValueRampUp[];
  tempRampUpValue: string;
  rampUpError: string;
  isEditMode: boolean;
  costFactorItemError: ICostRampUpError[];
  valueFactorItemErrors: IValueRampUpError[];
}

export default class AddOrEditFactorModal extends React.Component<
  IAddOrEditFactorModalProps,
  IAddOrEditFactorModalState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      description: '',
      descriptionError: '',
      category: '',
      categoryError: '',
      value: '',
      valueError: '',
      source: '',
      sourceError: '',
      rampUp: [],
      tempRampUp: '',
      rampUpValue: [],
      tempRampUpValue: '',
      rampUpError: '',
      isEditMode: false,
      costFactorItemError: [],
      valueFactorItemErrors: [],
    };
    this.validateYearLength = this.validateYearLength.bind(this);
  }

  public render() {
    if (this.state.rampUp.length === 0 && this.props.factorId === 'Cost') {
      this.setState({
        rampUp: [
          { year: '', value: '' },
          { year: '', value: '' },
          { year: '', value: '' },
          { year: '', value: '' },
          { year: '', value: '' },
        ],
        costFactorItemError: [
          { year: null, value: null },
          { year: null, value: null },
          { year: null, value: null },
          { year: null, value: null },
          { year: null, value: null },
        ],
      });
    }

    if (this.state.rampUpValue.length === 0 && this.props.factorId === 'Value') {
      this.setState({
        rampUpValue: [
          { year: '', percent: '', value: '' },
          { year: '', percent: '', value: '' },
          { year: '', percent: '', value: '' },
          { year: '', percent: '', value: '' },
          { year: '', percent: '', value: '' },
        ],
        valueFactorItemErrors: [
          { year: null, percent: null, value: null },
          { year: null, percent: null, value: null },
          { year: null, percent: null, value: null },
          { year: null, percent: null, value: null },
          { year: null, percent: null, value: null },
        ],
      });
    }

    const requiredError = '*Missing entry';
    const {
      description,
      descriptionError,
      category,
      categoryError,
      value,
      valueError,
      source,
      sourceError,
      rampUp,
      rampUpValue,
      rampUpError,
      costFactorItemError,
      valueFactorItemErrors,
    } = this.state;

    if (rampUp.length !== costFactorItemError.length && this.props.factorId === 'Cost') {
      const tempCostFactorErrorArray: ICostRampUpError[] = [];
      rampUp.forEach((item: ICostRampUp, index: number) => {
        tempCostFactorErrorArray.push({ year: null, value: null });
      });
      this.setState({ costFactorItemError: tempCostFactorErrorArray });
    }

    if (rampUpValue.length !== valueFactorItemErrors.length && this.props.factorId === 'Value') {
      const tempCostFactorErrorArray: IValueRampUpError[] = [];
      rampUpValue.forEach((item: IValueRampUp, index: number) => {
        tempCostFactorErrorArray.push({ year: null, percent: null, value: null });
      });
      this.setState({ valueFactorItemErrors: tempCostFactorErrorArray });
    }

    const costsList =
      rampUp && rampUp.length > 0
        ? rampUp.map((costFactor: ICostRampUp, index: number) => {
            return (
              <div className={Styles.costFlexLayout} key={index}>
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      costFactorItemError[index]
                        ? costFactorItemError[index].year
                          ? costFactorItemError[index].year.length
                            ? 'error'
                            : ''
                          : ''
                        : '',
                    )}
                  >
                    <label htmlFor={'factorYear-' + index} className="input-label">
                      Year<sup>*</sup>
                    </label>
                    {/* <input
                      type="text"
                      className="input-field numeric"
                      required={true}
                      required-error={requiredError}
                      id={'factorYear-' + index}
                      name="year"
                      placeholder="Type here"
                      autoComplete="off"
                      value={costFactor.year}
                      maxLength={4}
                      minLength={4}
                      onChange={(e) => this.textOnChangeRampup({value: e.currentTarget.value},e)}
                      onBlur={this.validateYearLength}
                    /> */}
                    {/* @ts-ignore */}
                    <NumberFormat
                      className={classNames('input-field', Styles.fteField)}
                      id={'factorYear-' + index}
                      required={true}
                      required-error={requiredError}
                      name="year"
                      placeholder="Type here"
                      value={costFactor.year === null ? '' : costFactor.year}
                      thousandSeparator={false}
                      decimalScale={0}
                      maxLength={4}
                      minLength={4}
                      // decimalSeparator={''}
                      onValueChange={(values: any, sourceInfo: any) => this.textOnChangeRampup(values, sourceInfo)}
                      onBlur={this.validateYearLength}
                    />
                    <span
                      className={classNames(
                        'error-message',
                        costFactorItemError[index]
                          ? costFactorItemError[index].year
                            ? costFactorItemError[index].year.length
                              ? ''
                              : 'hide'
                            : 'hide'
                          : 'hide',
                      )}
                    >
                      {costFactorItemError[index] ? costFactorItemError[index].year : ''}
                    </span>
                  </div>
                </div>

                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      costFactorItemError[index]
                        ? costFactorItemError[index].value
                          ? costFactorItemError[index].value.length
                            ? 'error'
                            : ''
                          : ''
                        : '',
                    )}
                  >
                    <label htmlFor={'factorValue-' + index} className="input-label">
                      Value in €<sup>*</sup>
                    </label>
                    {/* <input
                      type="text"
                      className="input-field numeric"
                      required={true}
                      required-error={requiredError}
                      id={'factorValue-' + index}
                      name="value"
                      placeholder="Type here"
                      autoComplete="off"
                      value={costFactor.value === '' ? '' : costFactor.value}
                      maxLength={10}
                      onChange={(e) => this.textOnChangeRampup({value: e.currentTarget.value},e)}
                    />*/}
                    {/* @ts-ignore */}
                    <NumberFormat
                      className={classNames('input-field', Styles.fteField)}
                      id={'factorValue-' + index}
                      required={true}
                      required-error={requiredError}
                      name="value"
                      placeholder="Type here"
                      value={
                        costFactor.value === ''
                          ? ''
                          : new Intl.NumberFormat(navigator.language).format(Number(costFactor.value))
                      }
                      decimalScale={2}
                      thousandSeparator={thousandSeparator(navigator.language)}
                      decimalSeparator={decimalSeparator(navigator.language)}
                      onValueChange={(values: any, sourceInfo: any) => this.textOnChangeRampup(values, sourceInfo)}
                    />
                    <span
                      className={classNames(
                        'error-message',
                        costFactorItemError[index]
                          ? costFactorItemError[index].value
                            ? costFactorItemError[index].value.length
                              ? ''
                              : 'hide'
                            : 'hide'
                          : 'hide',
                      )}
                    >
                      {costFactorItemError[index] ? costFactorItemError[index].value : ''}
                    </span>
                  </div>
                </div>

                <div className={Styles.iconWrapper}>
                  <i
                    className={classNames(Styles.deleteIcon, 'icon delete')}
                    onClick={this.onRemoveDatasetClick(index)}
                  />
                </div>
              </div>
            );
          })
        : '';

    const valuesList =
      rampUpValue && rampUpValue.length > 0
        ? rampUpValue.map((valueFactor: IValueRampUp, index: number) => {
            return (
              <div
                className={this.props.factorId === 'Value' ? Styles.valueFlexLayout : Styles.costFlexLayout}
                key={index}
              >
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      valueFactorItemErrors[index]
                        ? valueFactorItemErrors[index].year
                          ? valueFactorItemErrors[index].year.length
                            ? 'error'
                            : ''
                          : ''
                        : '',
                    )}
                  >
                    <label htmlFor={'factorYear-' + index} className="input-label">
                      Year<sup>*</sup>
                    </label>
                    {/* <input
                      type="text"
                      className="input-field numeric"
                      required={true}
                      required-error={requiredError}
                      id={'factorYear-' + index}
                      name="year"
                      placeholder="Type here"
                      autoComplete="off"
                      value={valueFactor.year}
                      maxLength={4}
                      minLength={4}
                      onChange={(e) => this.textOnChangeRampup({value: e.currentTarget.value},e)}
                      onBlur={this.validateYearLength}
                    /> */}
                    {/* @ts-ignore */}
                    <NumberFormat
                      className={classNames('input-field', Styles.fteField)}
                      id={'factorYear-' + index}
                      required={true}
                      required-error={requiredError}
                      name="year"
                      placeholder="Type here"
                      value={valueFactor.year === null ? '' : valueFactor.year}
                      thousandSeparator={false}
                      decimalScale={0}
                      maxLength={4}
                      minLength={4}
                      // decimalSeparator={''}
                      onValueChange={(values: any, sourceInfo: any) => this.textOnChangeRampup(values, sourceInfo)}
                      onBlur={this.validateYearLength}
                    />
                    <span
                      className={classNames(
                        'error-message',
                        valueFactorItemErrors[index]
                          ? valueFactorItemErrors[index].year
                            ? valueFactorItemErrors[index].year.length
                              ? ''
                              : 'hide'
                            : 'hide'
                          : 'hide',
                      )}
                    >
                      {valueFactorItemErrors[index] ? valueFactorItemErrors[index].year : ''}
                    </span>
                  </div>
                </div>

                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      valueFactorItemErrors[index]
                        ? valueFactorItemErrors[index].percent
                          ? valueFactorItemErrors[index].percent.length
                            ? 'error'
                            : ''
                          : ''
                        : '',
                    )}
                  >
                    <label htmlFor={'factorPercent-' + index} className="input-label">
                      Percent<sup>*</sup>
                    </label>
                    {/* <input
                      type="text"
                      className="input-field numeric"
                      required={true}
                      required-error={requiredError}
                      id={'factorPercent-' + index}
                      name="percent"
                      placeholder="Type here"
                      autoComplete="off"
                      value={valueFactor.percent === '' ? '' : valueFactor.percent}
                      maxLength={5}
                      onChange={(e) => this.textOnChangeRampup({value: e.currentTarget.value},e)}
                    /> */}
                    {/* @ts-ignore */}
                    <NumberFormat
                      className={classNames('input-field', Styles.fteField)}
                      id={'factorPercent-' + index}
                      required={true}
                      required-error={requiredError}
                      name="percent"
                      placeholder="Type here"
                      value={
                        valueFactor.percent === ''
                          ? ''
                          : new Intl.NumberFormat(navigator.language).format(Number(valueFactor.percent))
                      }
                      decimalScale={2}
                      thousandSeparator={thousandSeparator(navigator.language)}
                      decimalSeparator={decimalSeparator(navigator.language)}
                      onValueChange={(values: any, sourceInfo: any) => this.textOnChangeRampup(values, sourceInfo)}
                    />
                    <span
                      className={classNames(
                        'error-message',
                        valueFactorItemErrors[index]
                          ? valueFactorItemErrors[index].percent
                            ? valueFactorItemErrors[index].percent.length
                              ? ''
                              : 'hide'
                            : 'hide'
                          : 'hide',
                      )}
                    >
                      {valueFactorItemErrors[index] ? valueFactorItemErrors[index].percent : ''}
                    </span>
                  </div>
                </div>

                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      valueFactorItemErrors[index]
                        ? valueFactorItemErrors[index].value
                          ? valueFactorItemErrors[index].value.length
                            ? 'error'
                            : ''
                          : ''
                        : '',
                    )}
                  >
                    <label htmlFor={'factorValue-' + index} className="input-label">
                      Value in €<sup>*</sup>
                    </label>
                    {/* <input
                      type="text"
                      className="input-field numeric"
                      required={true}
                      required-error={requiredError}
                      id={'factorValue-' + index}
                      name="value"
                      placeholder="Type here"
                      autoComplete="off"
                      value={valueFactor.value === '' ? '' : valueFactor.value}
                      maxLength={10}
                      onChange={(e) => this.textOnChangeRampup({value: e.currentTarget.value},e)}
                    /> */}
                    {/* @ts-ignore */}
                    <NumberFormat
                      className={classNames('input-field', Styles.fteField)}
                      id={'factorValue-' + index}
                      required={true}
                      required-error={requiredError}
                      name="value"
                      placeholder="Type here"
                      value={
                        valueFactor.value === ''
                          ? ''
                          : new Intl.NumberFormat(navigator.language).format(Number(valueFactor.value))
                      }
                      decimalScale={2}
                      thousandSeparator={thousandSeparator(navigator.language)}
                      decimalSeparator={decimalSeparator(navigator.language)}
                      onValueChange={(values: any, sourceInfo: any) => this.textOnChangeRampup(values, sourceInfo)}
                    />
                    <span
                      className={classNames(
                        'error-message',
                        valueFactorItemErrors[index]
                          ? valueFactorItemErrors[index].value
                            ? valueFactorItemErrors[index].value.length
                              ? ''
                              : 'hide'
                            : 'hide'
                          : 'hide',
                      )}
                    >
                      {valueFactorItemErrors[index] ? valueFactorItemErrors[index].value : ''}
                    </span>
                  </div>
                </div>

                <div className={Styles.iconWrapper}>
                  <i
                    className={classNames(Styles.deleteIcon, 'icon delete')}
                    onClick={this.onRemoveDatasetClick(index)}
                  />
                </div>
              </div>
            );
          })
        : '';

    const AddOrEditFactorModalContent: React.ReactNode = (
      <div id="addOrEditFactorModalDiv" className={classNames(Styles.firstPanel, Styles.addOrEditFactorModal)}>
        <div className={Styles.formWrapper}>
          <div className={Styles.flexLayout}>
            <div>
              <TextBox
                type="text"
                controlId={'factorDescription'}
                name={'description'}
                label={'Description'}
                placeholder={'Type here'}
                value={description}
                errorText={descriptionError}
                required={true}
                maxLength={200}
                onChange={this.textInputOnChange}
              />
            </div>
            <div>
              <TextBox
                type="text"
                controlId={'factorCategory'}
                name={'category'}
                label={'Category'}
                placeholder={'Type here'}
                value={category}
                errorText={categoryError}
                required={true}
                maxLength={100}
                onChange={this.textInputOnChange}
              />
            </div>
          </div>
          <div className={Styles.flexLayout}>
            <div>
              <div className={classNames('input-field-group include-error', valueError.length ? 'error' : '')}>
                <label htmlFor="factorValue" className="input-label">
                  Value in €<sup>*</sup>
                </label>
                {/* <input
                  type="text"
                  className="input-field numeric"
                  required={true}
                  required-error={requiredError}
                  id="factorValue"
                  name="value"
                  placeholder="Type here"
                  autoComplete="off"
                  value={value}
                  maxLength={10}
                  onChange={this.textInputOnChange}
                /> */}
                {/* @ts-ignore */}
                <NumberFormat
                  className={classNames('input-field', Styles.fteField)}
                  id={'factorValue'}
                  required={true}
                  required-error={requiredError}
                  name="value"
                  placeholder="Type here"
                  value={value === '' ? '' : new Intl.NumberFormat(navigator.language).format(Number(value))}
                  thousandSeparator={thousandSeparator(navigator.language)}
                  decimalSeparator={decimalSeparator(navigator.language)}
                  decimalScale={2}
                  onValueChange={(values: any, sourceInfo: any) => this.textInputOnChangeValueField(values, sourceInfo)}
                />
                <span className={classNames('error-message', valueError.length ? '' : 'hide')}>{valueError}</span>
              </div>
            </div>
            <div>
              <TextBox
                type="text"
                controlId={'factorSource'}
                name={'source'}
                label={'Source'}
                placeholder={'Type here'}
                value={source}
                errorText={sourceError}
                required={true}
                maxLength={100}
                onChange={this.textInputOnChange}
              />
            </div>
          </div>

          <div className={Styles.rampupWrapper}>
            <div className={Styles.rampupTitle}>
              Ramp Up<sup>*</sup>
            </div>
            <hr />

            <div>
              <div className={classNames(Styles.costList, 'mbc-scroll')}>
                {this.props.factorId === 'Cost' ? costsList : valuesList}
              </div>

              <div>
                <div className={classNames(Styles.addButtonWrapper)}>
                  <button id="addCostFactorBtn" onClick={this.onAddDatasetClick}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Dataset</span>
                  </button>
                </div>
                <span className={classNames('error-message', rampUpError.length ? '' : 'hide')}>{rampUpError}</span>
              </div>
            </div>
          </div>

          <div className={Styles.actionWrapper}>
            <button className="btn btn-tertiary" onClick={this.onAddFactorClick}>
              {(this.state.isEditMode ? 'Update ' : 'Add ') + this.props.factorId + ' Factor'}
            </button>
          </div>
        </div>
      </div>
    );

    return (
      <Modal
        title={(this.state.isEditMode ? 'Edit' : 'Add') + ' ' + this.props.factorId + ' Factor'}
        showAcceptButton={false}
        showCancelButton={false}
        buttonAlignment="right"
        show={this.props.showAddOrEditFactorModal}
        content={AddOrEditFactorModalContent}
        onCancel={this.onModalCancel}
      />
    );
  }

  public setFactorData = (factorItemObj: any, editMode: boolean) => {
    if (editMode) {
      // Factor item edit mode set values code comes here
      if (this.props.factorId === 'Cost') {
        this.setState({
          description: factorItemObj.description,
          category: factorItemObj.category,
          value: factorItemObj.value,
          source: factorItemObj.source,
          rampUp: factorItemObj.rampUp,
          tempRampUp: JSON.stringify(factorItemObj.rampUp),
          isEditMode: editMode,
        });
      }

      if (this.props.factorId === 'Value') {
        this.setState({
          description: factorItemObj.description,
          category: factorItemObj.category,
          value: factorItemObj.value,
          source: factorItemObj.source,
          rampUpValue: factorItemObj.rampUp,
          tempRampUpValue: JSON.stringify(factorItemObj.rampUp),
          isEditMode: editMode,
        });
      }
    } else {
      this.setState({
        isEditMode: editMode,
      });
      this.clearModalFields();
    }
  };

  protected onModalCancel = () => {
    this.clearModalFields();
    if (this.state.isEditMode) {
      if (this.props.factorId === 'Cost') {
        this.props.onAddOrEditFactorModalCancel(JSON.parse(this.state.tempRampUp));
      } else {
        this.props.onAddOrEditFactorModalCancel(JSON.parse(this.state.tempRampUpValue));
      }
    } else {
      this.props.onAddOrEditFactorModalCancel([]);
    }
  };

  protected textInputOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const name: string = e.currentTarget.name;
    const value: string = e.currentTarget.value;
    this.setState((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  protected textInputOnChangeValueField = (values: any, e: any) => {
    const name: string = e?.event?.target?.name;
    const { value } = values;
    this.setState((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  protected textOnChangeRampup = (values: any, e: any) => {
    const name: string = e?.event?.target?.name;
    const { value } = values;
    const index: number = parseInt(e?.event?.target?.id.split('-')[1], 0);
    const { rampUp, rampUpValue } = this.state;
    if (Number(value).toString() !== NaN.toString()) {
      if (this.props.factorId === 'Cost') {
        rampUp.forEach((item: any, itemIndex) => {
          if (index === itemIndex && name === 'value') {
            // if (!this.validateFloatKeyPress(e.target, e)) {
            //   e.preventDefault();
            // } else {
            item[name] = value;
            // }
          }

          if (index === itemIndex && name === 'year') {
            if (!this.validateYearKeyPress(e)) {
              e.preventDefault();
            } else {
              item[name] = value;
            }
          }

          if (name === 'year' && index !== itemIndex && value.length === 4) {
            if (this.validateYearKeyPress(e)) {
              const indexDifference = index - itemIndex;
              item[name] = value - indexDifference;
            }
          }
          return item;
        });
        this.setState({ rampUp });
      }

      if (this.props.factorId === 'Value') {
        rampUpValue.forEach((item: any, itemIndex) => {
          if (index === itemIndex && name === 'value') {
            // if (!this.validateFloatKeyPress(e.target, e)) {
            //   e.preventDefault();
            // } else {
            item[name] = value;
            // }
          }

          if (index === itemIndex && name === 'percent') {
            // if (!this.validateFloatKeyPress(e.target, e)) {
            //   e.preventDefault();
            // } else {
            item[name] = value;
            // }
          }

          if (index === itemIndex && name === 'year') {
            if (!this.validateYearKeyPress(e)) {
              e.preventDefault();
            } else {
              item[name] = value;
            }
          }

          if (name === 'year' && index !== itemIndex && value.length === 4) {
            if (this.validateYearKeyPress(e)) {
              const indexDifference = index - itemIndex;
              item[name] = value - indexDifference;
            }
          }
          return item;
        });
        this.setState({ rampUpValue });
      }
    }
  };

  protected addFactorItem = () => {
    let factorItemObj: ICostFactor | IValueFactor;
    let proceedSave = false;
    if (this.validateFactorForm()) {
      factorItemObj = {
        ...this.state,
      };
      proceedSave = true;
    }
    if (proceedSave) {
      this.props.onAddOrEditFactorItem(factorItemObj, this.props.editMode);
    }
  };

  protected clearModalFields() {
    this.setState({
      description: '',
      descriptionError: '',
      category: '',
      categoryError: '',
      value: '',
      valueError: '',
      source: '',
      sourceError: '',
      rampUpError: '',
      rampUp: [],
      rampUpValue: [],
    });

    InputFieldsUtils.resetErrors('#addOrEditFactorModalDiv');
  }

  protected validateFactorForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    const { rampUp, costFactorItemError, rampUpValue, valueFactorItemErrors } = this.state;

    this.setState({ descriptionError: '', categoryError: '', sourceError: '', valueError: '' });

    if (this.state.description === '') {
      this.setState({ descriptionError: errorMissingEntry });
      formValid = false;
    }

    if (this.state.category === '') {
      this.setState({ categoryError: errorMissingEntry });
      formValid = false;
    }

    if (this.state.source === '') {
      this.setState({ sourceError: errorMissingEntry });
      formValid = false;
    }

    if (this.state.value === '') {
      this.setState({ valueError: errorMissingEntry });
      formValid = false;
    }

    if (this.props.factorId === 'Cost') {
      rampUp.forEach((rampUpData, index) => {
        costFactorItemError[index].year = '';
        costFactorItemError[index].value = '';
        if (rampUpData.year === null || !rampUpData.year) {
          costFactorItemError[index].year = errorMissingEntry;
          this.setState({ costFactorItemError });
          formValid = false;
        }
        if (rampUpData.value === null || rampUpData.value === undefined || rampUpData.value === '') {
          costFactorItemError[index].value = errorMissingEntry;
          this.setState({ costFactorItemError });
          formValid = false;
        }
      });
    }

    if (this.props.factorId === 'Value') {
      rampUpValue.forEach((rampUpData, index) => {
        valueFactorItemErrors[index].year = '';
        valueFactorItemErrors[index].value = '';
        valueFactorItemErrors[index].percent = '';
        if (rampUpData.year === null || !rampUpData.year) {
          valueFactorItemErrors[index].year = errorMissingEntry;
          this.setState({ valueFactorItemErrors });
          formValid = false;
        }
        if (rampUpData.percent === null || rampUpData.percent === undefined || rampUpData.percent === '') {
          valueFactorItemErrors[index].percent = errorMissingEntry;
          this.setState({ valueFactorItemErrors });
          formValid = false;
        }
        if (rampUpData.value === null || rampUpData.value === undefined || rampUpData.value === '') {
          valueFactorItemErrors[index].value = errorMissingEntry;
          this.setState({ valueFactorItemErrors });
          formValid = false;
        }
      });
    }

    return formValid;
  };

  protected onAddFactorClick = () => {
    if (this.validateFactorForm()) {
      const { description, category, value, source, rampUp, rampUpValue } = this.state;
      const rampUpData = this.props.factorId === 'Cost' ? rampUp : rampUpValue;
      this.setState({ rampUpError: '' });
      this.props.onAddOrEditFactorItem(
        {
          description,
          category,
          value,
          source,
          rampUp: rampUpData,
        },
        this.state.isEditMode,
      );
    }
  };

  protected onAddDatasetClick = () => {
    let data: ICostRampUp;
    let dataValueFactor: IValueRampUp;
    let dataForCostFactorError: ICostRampUpError;
    let dataForValueFactorError: IValueRampUpError;
    const { rampUp, costFactorItemError, rampUpValue, valueFactorItemErrors } = this.state;
    if (this.props.factorId === 'Cost') {
      data = { year: '', value: '' };
      dataForCostFactorError = { year: null, value: null };
      rampUp.push(data);
      costFactorItemError.push(dataForCostFactorError);
      rampUp.forEach((rampUpData, currentIndex) => {
        const lastIndex = rampUp.length - 1;
        if (currentIndex === lastIndex && rampUp[lastIndex - 1].year) {
          rampUpData.year = rampUp[lastIndex - 1].year + 1;
        }
        return rampUpData;
      });
      if (rampUp.length > 1) {
        this.setState({ rampUpError: '' });
      }
      this.setState({ rampUp });
    }

    if (this.props.factorId === 'Value') {
      dataValueFactor = { year: '', percent: '', value: '' };
      dataForValueFactorError = { year: null, percent: null, value: null };
      rampUpValue.push(dataValueFactor);
      valueFactorItemErrors.push(dataForValueFactorError);
      rampUpValue.forEach((rampUpData, currentIndex) => {
        const lastIndex = rampUpValue.length - 1;
        if (currentIndex === lastIndex && rampUpValue[lastIndex - 1].year) {
          rampUpData.year = rampUpValue[lastIndex - 1].year + 1;
        }
        return rampUpData;
      });
      if (rampUpValue.length > 1) {
        this.setState({ rampUpError: '' });
      }
      this.setState({ rampUpValue });
    }
  };

  protected onRemoveDatasetClick = (index: number) => {
    return () => {
      const { rampUp, costFactorItemError, rampUpValue, valueFactorItemErrors } = this.state;
      if (this.props.factorId === 'Cost') {
        if (rampUp.length > 5) {
          const removedItemFromRampup = rampUp.filter((item: any, itemIndex) => {
            if (itemIndex !== index && index === 0) {
              item.value = item.value > 0 ? item.value : 0;
              return item;
            }
            if (itemIndex !== index && index !== 0) {
              if (itemIndex > index && index !== rampUp.length - 1) {
                item.year = item.year - 1;
              }

              return item;
            }
          });
          if (rampUp[rampUp.length - 1].value === null) {
            removedItemFromRampup[removedItemFromRampup.length - 1].value = '';
          }
          const removeCostFactorItemError = costFactorItemError.filter((item, itemIndex) => {
            if (itemIndex !== index) {
              return item;
            }
          });
          this.setState({ rampUp: removedItemFromRampup, costFactorItemError: removeCostFactorItemError });
        } else {
          this.setState({ rampUpError: 'Atleast 5 records should be available.' });
        }
      }

      if (this.props.factorId === 'Value') {
        if (rampUpValue.length > 5) {
          const removedItemFromRampup = rampUpValue.filter((item: any, itemIndex) => {
            if (itemIndex !== index && index === 0) {
              item.value = item.value > 0 ? item.value : 0;
              item.percent = item.percent > 0 ? item.percent : 0;
              return item;
            }
            if (itemIndex !== index && index !== 0) {
              if (itemIndex > index && index !== rampUpValue.length - 1) {
                item.year = item.year - 1;
              }
              return item;
            }
          });
          if (rampUpValue[rampUpValue.length - 1].value === null) {
            removedItemFromRampup[removedItemFromRampup.length - 1].value = '';
          }
          if (rampUpValue[rampUpValue.length - 1].percent === null) {
            removedItemFromRampup[removedItemFromRampup.length - 1].percent = '';
          }
          const removeValueFactorItemError = valueFactorItemErrors.filter((item, itemIndex) => {
            if (itemIndex !== index) {
              return item;
            }
          });
          this.setState({ rampUpValue: removedItemFromRampup, valueFactorItemErrors: removeValueFactorItemError });
        } else {
          this.setState({ rampUpError: 'Atleast 5 records should be available.' });
        }
      }
    };
  };

  /***************************************************************************************
   ****************** Following method is not getting used now, will delete  *************
   ***************************************************************************************/

  protected validateFloatKeyPress(el: any, evt: any) {
    const charCode = evt.which ? evt.which : evt.keyCode;
    const numberVal = el.value.split('.');
    if (charCode !== 46 && charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }

    // just one dot (thanks ddlab)
    if (numberVal.length > 1 && charCode === 46) {
      return false;
    }

    if (numberVal[1] && numberVal[1].length > 2) {
      return false;
    }

    return true;
  }

  protected validateYearKeyPress(el: any) {
    const numberVal = el?.event?.target?.value;
    // Only number should be allowed
    const str = '^[0-9]*$';
    const match = numberVal.match(str) ? true : false;
    return match;
  }

  protected validateYearLength(el: any) {
    const { rampUp, rampUpValue } = this.state;
    const numberVal = el?.target?.value;
    const index: number = parseInt(el?.target?.id.split('-')[1], 0);
    if (numberVal.length < 4 && this.props.factorId === 'Cost') {
      rampUp[index].year = null;
      this.setState({
        rampUp,
      });
    }

    if (numberVal.length < 4 && this.props.factorId === 'Value') {
      rampUpValue[index].year = null;
      this.setState({
        rampUpValue,
      });
    }

    return;
  }
}
