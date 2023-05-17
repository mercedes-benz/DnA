import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

import MDEditor from '@uiw/react-md-editor';
import rehypeSanitize from 'rehype-sanitize';
import { unified } from 'unified';
import rehypeParse from 'rehype-parse';
import rehypeStringify from 'rehype-stringify';

// components from container app
import InfoModal from 'dna-container/InfoModal';
import Tags from 'dna-container/Tags';

import { useFormContext, Controller } from 'react-hook-form';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import { isValidURL } from '../../../../Utility/utils';

const Description = ({ 
  // onSave, 
  artList, carlaFunctionList, dataCatalogList, platformList, 
  frontEndToolList }) => {
  const {
    register,
    formState: { errors, 
      // isSubmitting 
    },
    watch,
    // handleSubmit,
    reset,
    control,
    setValue,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const { howToAccessText } = watch();

  useEffect(() => {
    Tooltip.defaultSetup();
    reset(watch());
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    // update colors for the markdown editor
    const mdEditor = document.querySelector('[data-color-mode="dark"]>div.wmde-markdown-var');

    mdEditor.style.setProperty('--color-canvas-default', '#1c2026');
    mdEditor.style.setProperty('--color-accent-fg', '#00adef');
    mdEditor.style.setProperty('--color-fg-default', '#c0c8d0');
  }, []);

  const validateURL = (value) => {
    return !value || isValidURL(value) || 'Not a valid URL';
  };

  useEffect(() => {
    if (howToAccessText?.length) {
      // sanitize HTML
      const processor = async () =>
        await unified().use(rehypeParse).use(rehypeSanitize).use(rehypeStringify).process(watch('howToAccessText'));
      processor().then((res) => {
        setValue('howToAccessText', res.value);
      });
    }
  }, [howToAccessText, setValue, watch]);
  
  const [tags, setTags] = useState([]);

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
                    CarLA Function
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
                    <select id="CorporateDataCatalogField" multiple={false} {...register('corporateDataCatalog')}>
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
                  <span className={classNames('error-message', errors.corporateDataCatalog?.message ? '' : 'hide')}>
                    {errors.corporateDataCatalog?.message}
                  </span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.platform ? 'error' : '')}>
                  <label id="platformLabel" htmlFor="platformInput" className="input-label">
                    Platform
                  </label>
                  <div className={`custom-select`}>
                    <select id="platformField" name="platform" multiple={true} {...register('platform')}>
                      {platformList?.map((item, ind) => (
                        <option id={item + ind} key={item.id} value={item.name}>
                          {item.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', errors.platform?.message ? '' : 'hide')}>
                    {errors.platform?.message}
                  </span>
                </div>
                <div className={classNames('input-field-group include-error', errors.frontendTools ? 'error' : '')}>
                  <label id="frontendToolsLabel" htmlFor="frontendToolsInput" className="input-label">
                    Front-End Tools
                  </label>
                  <div className={`custom-select`}>
                    <select id="frontendToolsField" name="frontendTools" multiple={true} {...register('frontEndTools')}>
                      {frontEndToolList?.map((item, ind) => (
                        <option id={item + ind} key={item.id} value={item.name}>
                          {item.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', errors.frontendTools?.message ? '' : 'hide')}>
                    {errors.frontendTools?.message}
                  </span>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.ddx ? 'error' : '')}>
                  <label id="ddxLabel" htmlFor="ddxInput" className="input-label">
                    DDX
                  </label>
                  <input
                    {...register('ddx', {
                      validate: validateURL,
                    })}
                    type="text"
                    className="input-field"
                    id="ddxInput"
                    maxLength={200}
                    placeholder="https://example@example.com"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message', errors.ddx?.message ? '' : 'hide')}>
                    {errors.ddx?.message}
                  </span>
                </div>
                <div className={classNames('input-field-group include-error', errors.kafka ? 'error' : '')}>
                  <label id="kafkaLabel" htmlFor="kafkaInput" className="input-label">
                    Kafka
                  </label>
                  <input
                    {...register('kafka')}
                    type="text"
                    className="input-field"
                    id="kafkaInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                  />
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.oneAPI ? 'error' : '')}>
                  <label id="oneAPILabel" htmlFor="oneAPIInput" className="input-label">
                    oneAPI
                  </label>
                  <input
                    {...register('oneApi', {
                      validate: validateURL,
                    })}
                    type="text"
                    className="input-field"
                    id="oneAPIInput"
                    maxLength={200}
                    placeholder="https://example@example.com"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message', errors.oneApi?.message ? '' : 'hide')}>
                    {errors.oneApi?.message}
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
              <div className={classNames('input-field-group area')}>
                <label id="additionalInformation" className="input-label" htmlFor="additionalInformation">
                  Additional Information
                  <i
                    className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                    tooltip-data="Add additional information or notes about the data product"
                  />
                </label>
                <textarea
                  id="additionalInformation"
                  className="input-field-area"
                  type="text"
                  {...register('additionalInformation')}
                  rows={50}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
        <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
          <h3 id="tagHeading">Tags</h3>
          <div id="tagsContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
            <span id="tagDesc" className={classNames(Styles.textDesc)}>
              Use tags to make it easier to find your data product for other people
            </span>
            <div>
              <Tags
                title={'Tags'}
                max={100}
                chips={tags}
                setTags={setTags}
                isMandatory={false}
                // showMissingEntryError={showTagsMissingError}
              />
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>How to access</h3>
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout1}>
              <div
                className={classNames(
                  'input-field-group include-error area',
                  errors.howToAccessText ? 'error' : '',
                  Styles.howToAccess,
                )}
              >
                <Controller
                  control={control}
                  name="howToAccessText"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <>
                      <label id="howToAccessText" className="input-label" htmlFor="howToAccessText">
                        How to access <sup>*</sup>
                        <i
                          className="icon mbc-icon info"
                          tooltip-data={'The content will be displayed under "How to Access" modal \n in Summary page'}
                        />
                      </label>
                      <div data-color-mode="dark">
                        <MDEditor
                          value={field.value}
                          onChange={field.onChange}
                          previewOptions={{
                            rehypePlugins: [[rehypeSanitize]],
                          }}
                        />
                      </div>
                    </>
                  )}
                />
                <span className={classNames('error-message')}>{errors?.howToAccessText?.message}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* <div className="btnContainer">
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
      </div> */}
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
