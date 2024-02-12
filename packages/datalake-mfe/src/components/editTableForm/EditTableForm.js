import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useForm, FormProvider } from "react-hook-form";
import Styles from './edit-table-form.scss';
import SelectBox from 'dna-container/SelectBox';

const EditTableForm = (props) => {
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;
  
  const { formats, onEditTable, table } = props;

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [formats]);

  return (
    <FormProvider {...methods} >
      <div className={Styles.columnsWrapper}>
          <div className={Styles.columnContainer}>
              <input type="hidden" id={`name`} {...register(`name`)} />
              <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Table Name <sup>*</sup>
                  </label>
                  <div>
                  <input
                      type="text"
                      className={classNames('input-field')}
                      id={`tableName`}
                      {...register(`tableName`, { required: '*Missing entry' })}
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={table?.tableName}
                  />
                  <span className={classNames('error-message')}>{errors?.tableName?.message}</span>
                  </div>
              </div>
              <div className={Styles.configurationContainer}>
                  <div className={classNames('input-field-group include-error')}>
                  <label id="typeLabel" 
                  htmlFor={`dataFormat`} 
                  className="input-label">
                      Format <sup>*</sup>
                  </label>
                  <div className="custom-select">
                      <select 
                        id={`dataFormat`} 
                        {...register('dataFormat')}
                        defaultValue={table?.dataFormat}
                      >
                      {formats.length > 0 && 
                          formats?.map((format) => {
                          return (
                          <option id={format} key={format} value={format}>
                              {format}
                          </option>
                          )
                      })}
                      </select>
                  </div>
                  </div>
              </div>
              </div>
              <div className={classNames('input-field-group')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Table Comment <sup>*</sup>
                  </label>
                  <div>
                  <input
                      type="text"
                      className={classNames('input-field')}
                      id={`description`} 
                      {...register(`description`, { required: '*Missing entry' })}
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={table?.description}
                  />
                  </div>
                  <span className={classNames('error-message')}>{errors?.description?.message}</span>
              </div>
              <div className={Styles.btnContainer}>
                <button
                  className="btn btn-tertiary"
                  type="button"
                  onClick={handleSubmit((values) => {
                    onEditTable(values);
                  })}
                >
                  Update Table
                </button>
              </div>
          </div>
      </div>
    </FormProvider>
  );
}

export default EditTableForm;