import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

// components from container app
import SelectBox from 'dna-container/SelectBox';
import InfoModal from 'dna-container/InfoModal';

import { useFormContext } from 'react-hook-form';

const Description = ({ onSave, artList, carlaFunctionList, dataCatalogList }) => {
  const {
    register,
    formState: { errors, isSubmitting },
    watch,
    handleSubmit,
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  useEffect(() => {
    SelectBox.defaultSetup();
    reset(watch());
    //eslint-disable-next-line
  }, []);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={classNames(Styles.firstPanel, 'descriptionSection')}>
          <div>
            <h3>Please give a detailed data product description</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.productName ? 'error' : '')}>
                  <label id="productNameLabel" htmlFor="productNameInput" className="input-label">
                    Name of Data Product <sup>*</sup>
                  </label>
                  <input
                    {...register('productName', { required: '*Missing entry' })}
                    type="text"
                    className="input-field"
                    id="productNameInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message')}>{errors.productName?.message}</span>
                </div>
                <div className={classNames('input-field-group include-error', errors.carLAFunction ? 'error' : '')}>
                  <label id="connectionTypeLabel" htmlFor="connectionTypeInput" className="input-label">
                    carLA Function
                  </label>
                  <div className={`custom-select`}>
                    <select id="connectionTypeField" name="connectionType" {...register('carLAFunction')}>
                      <option value="">Choose</option>
                      {carlaFunctionList?.map((item, ind) => (
                        <option id={item + ind} key={item.id} value={item.name}>
                          {item.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', errors.carLAFunction?.message ? '' : 'hide')}>
                    {errors.carLAFunction?.message}
                  </span>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.artError?.message ? 'error' : '')}>
                  <label id="ARTLabel" htmlFor="ARTField" className="input-label">
                    Agile Release Train
                  </label>
                  <div className={classNames('custom-select')}>
                    <select {...register('ART')} id="ARTField" multiple={false} required={false}>
                      <option id="agileReleaseTrainOption" value={''}>
                        Choose
                      </option>
                      {artList?.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  {/* <span className={classNames('error-message', artError ? '' : 'hide')}>{artError}</span> */}
                </div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    errors.frontEndTechError?.message ? 'error' : '',
                  )}
                >
                  <label id="CorporateDataCatalog" htmlFor="CorporateDataCatalogField" className="input-label">
                    Corporate Data Catalog
                  </label>
                  <div id="CorporateDataCatalog" className="custom-select">
                    <select id="CorporateDataCatalogField" multiple={false} {...register('corportateDataCatalog')}>
                      <option id="CorporateDataCatalogFieldOption" value={''}>
                        Choose
                      </option>
                      {dataCatalogList?.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', errors.corportateDataCatalog?.message ? '' : 'hide')}>
                    {errors.corportateDataCatalog?.message}
                  </span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
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
                />
                <span className={classNames('error-message')}>{errors?.description?.message}</span>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error area', errors.howToAccess ? 'error' : '')}>
                <label id="howToAccess" className="input-label" htmlFor="howToAccess">
                  How to access
                </label>
                <textarea
                  id="description"
                  className="input-field-area"
                  type="text"
                  {...register('howToAccess')}
                  rows={50}
                />
                <span className={classNames('error-message')}>{errors?.howToAccess?.message}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
          onClick={handleSubmit((values) => {
            onSave(watch());
            reset(values, {
              keepDirty: false,
            });
          })}
        >
          Save & Next
        </button>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
    </>
  );
};

export default Description;
