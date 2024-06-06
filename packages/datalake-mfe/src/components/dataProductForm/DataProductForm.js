import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './data-product-form.scss';
import SelectBox from 'dna-container/SelectBox';
import { datalakeApi } from '../../apis/datalake.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
const DataProductForm = ({ project, onCreate }) => {
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;
  const [dataProductType, setDataProductType] = useState('');
  const [existingDpId, setExistingDpId] = useState('');
  const [existingDp, setExistingDp] = useState([]);
  useEffect(() => {
    if (dataProductType === 'EXISTING') {
      SelectBox.defaultSetup();
      ProgressIndicator.show();
      datalakeApi.getExistingDataProduts().then((res) => {
        const data = res.data.records;
        setExistingDp(data);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      }).catch((error) => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating dataProduct',
          'alert',
        );
      });
    }
  }, [dataProductType])


  const onLinkDataProduct = () => {
    const dataProductDetails = existingDp?.find((dp)=> dp.dataProductId === existingDpId);
    const isnewDataProduct = false;
    onCreate(dataProductDetails,isnewDataProduct);
    
  }

  return (
    <FormProvider {...methods}>
      <p className={Styles.highlightText}>You are about to create a data product out of this data lakehouse project.You can either create a new data product or link your existing data product. If you create a new data product, it will be created in a draft state you can then go, add additional information and publish the data product. We have pre-filled the Name and Description for you. Please feel free to edit them.</p>
      <label id="dpType" className={classNames('input-label', Styles.label)} htmlFor="Instance">
        Type of data product
      </label>
      <div className={Styles.radioBtnsGrid}>
        <div key={'NEW'}>
          <label className={'radio'}>
            <span className="wrapper">
              <input
                type="radio"
                className="ff-only"
                name="dpType"
                value={dataProductType}
                onChange={() => {
                  setDataProductType('NEW');
                }}
                checked={dataProductType === 'NEW'}
              />
            </span>
            <span className="label">{'New'}</span>
          </label>
        </div>
        <div key={'EXISTING'}>
          <label className={'radio'}>
            <span className="wrapper">
              <input
                type="radio"
                className="ff-only"
                name="dpType"
                value={dataProductType}
                onChange={() => {
                  setDataProductType('EXISTING')
                }}
                checked={dataProductType === 'EXISTING'}
              />
            </span>
            <span className="label">{'Existing'}</span>
          </label>
        </div>
      </div>

      {dataProductType === 'NEW' ? (
        <div>
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
                {...register('name', { required: '*Missing entry' })}
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
          <div>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                const isNewDataProduct = true;
                onCreate(values,isNewDataProduct);
              })}
            >
              Create Data Product
            </button>
          </div>
        </div>
      ) :
        dataProductType === 'EXISTING' ? (
        <div className={classNames(Styles.ExistingDpContainer)}>
          {existingDp.length > 0 ?(<div className={classNames('input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}
              htmlFor="dataProducts">
              Your Data Products
            </label>
            <div className={classNames('custom-select')}>
              <select
                id="dataProducts"
                {...register('existingDp', {
                  onChange: (e) => { setExistingDpId(e.target.value);}
                })}
              >
                <option id="dataProd" value={''}>
                  Choose
                </option>
                {existingDp?.map((obj) => {
                  return (
                    <option id={obj.dataProductName + obj.id} key={obj.id} value={obj.dataProductId}>
                      {obj.dataProductName}
                    </option>
                  )
                })}
              </select>
            </div>
            <div className={Styles.submitBtn}>
              <button
                className={classNames("btn btn-tertiary")}
                type="button"
                onClick={onLinkDataProduct}
              >
                Link Data Product
              </button>
            </div>
          </div>) : <span>No data products found </span> }
        </div>) :
        <></>
      }
    </FormProvider>
  );
}

export default DataProductForm;