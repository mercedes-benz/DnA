import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useForm, FormProvider } from "react-hook-form";
import Styles from './column-form.scss';
import SelectBox from 'dna-container/SelectBox';

const ColumnForm = (props) => {
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;
  
  const { dataTypes, onAddColumn, onEditColumn, edit, column } = props;

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [dataTypes]);

  return (
    <FormProvider {...methods} >
      <div className={Styles.columnsWrapper}>
          <div className={Styles.columnContainer}>
              <input type="hidden" id={`name`} {...register(`name`)} />
              <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Name <sup>*</sup>
                  </label>
                  <div>
                  <input
                      type="text"
                      className={classNames('input-field')}
                      id={`columnName`}
                      {...register(`columnName`, { required: '*Missing entry' })}
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={edit ? column?.columnName : ''}
                  />
                  <span className={classNames('error-message')}>{errors?.columnName?.message}</span>
                  </div>
              </div>
              <div className={Styles.configurationContainer}>
                  <div className={classNames('input-field-group include-error')}>
                  <label id="typeLabel" 
                  htmlFor={`dataType`} 
                  className="input-label">
                      Type <sup>*</sup>
                  </label>
                  <div className="custom-select">
                      <select 
                        id={`dataType`} 
                        {...register('dataType')}
                        defaultValue={edit ? column?.dataType : 'BOOLEAN'}
                      >
                      {dataTypes.length > 0 && 
                          dataTypes?.map((datatype) => {
                          return (
                          <option id={datatype} key={datatype} value={datatype}>
                              {datatype}
                          </option>
                          )
                      })}
                      </select>
                  </div>
                  </div>
              </div>
              </div>
              <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Comment
                  </label>
                  <div>
                  <input
                      type="text"
                      className={classNames('input-field')}
                      id={`comment`} 
                      {...register(`comment`)}
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={edit ? column?.comment : ''}
                  />
                  </div>
              </div>
              <div className={classNames('input-field-group')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Constraint
                  </label>
                  <div>
                  <label className="checkbox">
                      <span className="wrapper">
                      <input type="checkbox" className="ff-only" 
                        id={`notNullConstraintEnabled`} 
                        {...register(`notNullConstraintEnabled`)}
                        defaultChecked={edit ? column?.notNullConstraintEnabled : true}
                      />
                      </span>
                      <span className="label">Not Null</span>
                  </label>
                  </div>
              </div>
              </div>
              <div className={Styles.btnContainer}>
                <button
                  className="btn btn-tertiary"
                  type="button"
                  onClick={handleSubmit((values) => {
                    edit ? onEditColumn(values) : onAddColumn(values);
                  })}
                >
                  {edit ? 'Update Column' : 'Add Column'}
                </button>
              </div>
          </div>
      </div>
    </FormProvider>
  );
}

export default ColumnForm;