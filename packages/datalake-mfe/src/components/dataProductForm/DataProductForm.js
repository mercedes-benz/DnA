import classNames from 'classnames';
import React from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './data-product-form.scss';

const DataProductForm = ({ project, onCreate }) => {
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;
  
  return (
    <FormProvider {...methods}>
      <p className={Styles.highlightText}>You are about create a data product out of this data lakehouse project. The data product will be created in a draft state you can then go, add additional information and publish the data product. We have pre-filled the Name and Description for you. Please feel free to edit them.</p>
      <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
        <label className={classNames(Styles.inputLabel, 'input-label')}>
          Name <sup>*</sup>
        </label>
        <div>
          <input
            type="text"
            className={classNames('input-field')}
            id="name"
            placeholder="Type here"
            autoComplete="off"
            maxLength={55}
            defaultValue={project?.projectName}
            {...register('name', { required: '*Missing entry'})}
          />
          <span className={classNames('error-message')}>{errors?.name?.message}</span>
        </div>
      </div>
      <div className={classNames('input-field-group include-error area', errors.description ? 'error' : '')}>
        <label id="description" className="input-label" htmlFor="description">
          Description <sup>*</sup>
        </label>
        <textarea
          id="description"
          className="input-field-area"
          type="text"
          {...register('description', { required: '*Missing entry' })}
          rows={50}
          defaultValue={project?.description}
        />
        <span className={classNames('error-message')}>{errors?.description?.message}</span>
      </div>
      <div className={Styles.btnContainer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={handleSubmit((values) => {
            onCreate(values)
          })}
        >
          Create Data Product
        </button>
      </div>
    </FormProvider>
  );
}

export default DataProductForm;