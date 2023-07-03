import classNames from 'classnames';
import React, { 
  // createRef, 
  useEffect, useState, 
  // useRef 
} from 'react';
import Styles from './styles.scss';

// import MDEditor from '@uiw/react-md-editor';
import rehypeSanitize from 'rehype-sanitize';
import { unified } from 'unified';
import rehypeParse from 'rehype-parse';
import rehypeStringify from 'rehype-stringify';

// components from container app
import InfoModal from 'dna-container/InfoModal';
import Tags from 'dna-container/Tags';

import { Controller, useFieldArray, useFormContext } from 'react-hook-form';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import Tabs from '../../../../common/modules/uilab/js/src/tabs';
import { isValidURL } from '../../../../Utility/utils';
import TeamSearch from 'dna-container/TeamSearch';
import AccessSteps from '../../../accessSteps';
// import AccessSteps2 from '../../../accessSteps2';

import SelectBox from 'dna-container/SelectBox';

const Description = ({ 
  // onSave, 
  artList, carlaFunctionList, dataCatalogList, platformList, 
  frontEndToolList, tagsList }) => {
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


  const { 
    fields: kafkaFields, append: kafkaAppend, 
    update: kafkaUpdate, remove: kafkaRemove } = useFieldArray({
    control,
    name: 'kafkaArray'
  });

  const { 
    fields: liveAccessFields, append: liveAccessAppend, 
    update: liveAccessUpdate, remove: liveAccessRemove } = useFieldArray({
    control,
    name: 'liveAccessArray',
  });

  const { 
    fields: apiFields, append: apiAppend, 
    update: apiUpdate, remove: apiRemove } = useFieldArray({
    control,
    name: 'apiArray',
  });


  const [showInfoModal, setShowInfoModal] = useState(false);
  const [tagsListST, setTagsListST] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);
  const [productOwnerSearchTerm, setProductOwnerSearchTerm] = useState('');
  const [productOwnerFieldValue, setProductOwnerFieldValue] = useState('');
  const [currentTab, setCurrentTab] = useState('access-via-kafka');
  // const [howToAccessTemplateObj, setHowToAccessTemplateObj] = useState({});
  const [numberedStep, setNumberedStep] = useState(0);

  const [stepsList, setStepsList] = useState([]);

  const { howToAccessText, tags, productOwner, 
    // useTemplate, 
    accessType } = watch();


  useEffect(() => {
    Tooltip.defaultSetup();
    // setTimeout(() => {
      Tabs.defaultSetup();
    // }, 100);
    
    reset(watch());
    //eslint-disable-next-line
    const kafkaStepsMaxCount = Math.max.apply(Math, kafkaFields?.map(function(o) { return o.stepNumber; }));
    
    if(kafkaStepsMaxCount > 0)
      setNumberedStep(kafkaStepsMaxCount);
    else{
      setNumberedStep(0);
    }
    //eslint-disable-next-line
  }, []);

  useEffect(()=>{
    setTagsListST(tagsList);
    //eslint-disable-next-line
  },[tagsList]);


  useEffect(() => {
    setSelectedTags(tags);
  }, [tags]);

  useEffect(() => {
    if(accessType?.length > 0 && (accessType?.includes('Kafka') || accessType?.includes('API'))){
       SelectBox.defaultSetup(true);
    }
  }, [accessType]);

  useEffect(() => {
    let nameStr =
      typeof productOwner === 'string'
        ? productOwner
        : `${productOwner?.firstName} ${productOwner?.lastName}`;
    productOwner && setProductOwnerFieldValue(nameStr);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productOwner]);

  // useEffect(() => {
  //   // update colors for the markdown editor
  //   const mdEditor = document.querySelector('[data-color-mode="dark"]>div.wmde-markdown-var');

  //   mdEditor.style.setProperty('--color-canvas-default', '#1c2026');
  //   mdEditor.style.setProperty('--color-accent-fg', '#00adef');
  //   mdEditor.style.setProperty('--color-fg-default', '#c0c8d0');
  // }, []);

  useEffect(() => {
    // if(currentTab=='')
    // const stepperTab = document?.getElementById('access-via-kafka');
    // stepperTab?.click();
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
  
  
  const setReportTags = (selectedTags, field) => {
    // let dept = selectedTags?.map((item) => item.toUpperCase());
    setSelectedTags([...selectedTags]);
    setValue('tags', selectedTags);
    field.onChange(selectedTags);
  }

  const handleProductOwner = (field, value) => {
    let name = '';
    if(value) {
      value['addedByProvider'] = true;
      name =  `${value.firstName} ${value.lastName}`;
    }
    setValue('productOwner', value);
    field.onChange(value);
    setProductOwnerFieldValue(name);
  }

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      setCurrentTab(id);
    }
  };

  const onTeamMemberMoveUp = (index) => {
    const teamMembers = stepsList;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index - 1, 0, teamMember);
    setStepsList( teamMembers );
    // const tempTeamsObj = { team: teamMembers };
    // this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
    // console.log('Going up.................',stepsList);
    // setStepsList(prevCache => prevCache.map((val, i) => i !== index ? val : !val));
  };

  const onTeamMemberMoveDown = (index) => {
    const teamMembers = stepsList;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index + 1, 0, teamMember);
    setStepsList( teamMembers );
    // const tempTeamsObj = { team: teamMembers };
    // this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
    // console.log('Going down.................',stepsList);
    // setStepsList(prevCache => prevCache.map((val, i) => i !== index ? val : !val));
  };

  // console.log(Math.max(...kafkaFields?.map(o => o.stepNumber)),'=-=-=-=-=-=-=');

  

  

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
            {/* <div className={Styles.flexLayout}> */}
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
            {/* </div> */}
            {/* <div className={Styles.flexLayout}> */}
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
                <div className={classNames('input-field-group')}>
                    <Controller
                        control={control}
                        name="productOwner"
                        render={({ field }) => (
                          <TeamSearch
                            label={<>Product Owner</>}
                            fieldMode={true}
                            fieldValue={productOwnerFieldValue}
                            setFieldValue={(val) => setProductOwnerFieldValue(val)}
                            onAddTeamMember={(value) => handleProductOwner(field, value)}
                            btnText="Save"
                            searchTerm={productOwnerSearchTerm}
                            setSearchTerm={(value) => setProductOwnerSearchTerm(value)}
                            showUserDetails={false}
                            setShowUserDetails={() => {}}
                          />
                        )}
                      />
                  </div>
              </div>
            {/* </div> */}
            {/* <div className={Styles.flexLayout}> */}
              {/* <div className={Styles.flexLayout}>
                
                <div>
                  
                </div>
              </div> */}
            {/* </div> */}
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
              <Controller
                  control={control}
                  name="tags"
                  render={({ field }) => (
                    <Tags
                      title={'Tags'}
                      chips={selectedTags}
                      tags={tagsListST}
                      setTags={(selectedTags) => setReportTags(selectedTags, field)}
                      // {...register('tags')}
                    />
                  )}
                /> 
            </div>
          </div>
        </div>
      </div>

      
      <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
        <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
          <h3 id="tagHeading">Access &nbsp;
          <i
            className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
            tooltip-data="Access"
          />
          </h3>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', 
              // errors.carLAFunction ? 'error' : ''
              )}>
                <label id="connectionTypeLabel" htmlFor="connectionTypeInput" className="input-label">
                  Access
                </label>
                <div className={`custom-select`}>
                  <select id="connectionTypeField" multiple={true} name="connectionType" {...register('accessType')}>
                    <option value="">Choose</option>
                    <option id='Kafka0' key={'Kafka'} value={'Kafka'}>Kafka</option>
                    <option id='Live (SAC/AFO)1' key={'Live (SAC/AFO)'} value={'Live (SAC/AFO)'}>Live (SAC/AFO)</option>
                    <option id='API0' key={'API'} value={'API'}>API</option>
                  </select>
                </div>
                {/* <span className={classNames('error-message', errors.carLAFunction?.message ? '' : 'hide')}>
                  {errors.carLAFunction?.message}
                </span> */}
              </div>

              {(accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)')) || accessType?.length == 0 ?
              <div></div>
               : 
               
              <div className={classNames('input-field-group include-error', 
              // errors.carLAFunction ? 'error' : ''
              )}>
                <label id="confidentialityLabel" htmlFor="confidentialityInput" className="input-label">
                  Classification
                </label>
                <div className={`custom-select`}>
                  <select id="confidentialityField" name="confidentiality" {...register('confidentiality')}>
                    <option value="">Choose</option>
                    <option id='Internal0' key={'Internal'} value={'Internal'}>Internal</option>
                    <option id='Secret1' key={'Secret'} value={'Secret'}>Secret</option>
                    <option id='Confidential2' key={'Confidential'} value={'Confidential'}>Confidential</option>
                    <option id='Public2' key={'Public'} value={'Public'}>Public</option>
                  </select>
                </div>
                {/* <span className={classNames('error-message', errors.carLAFunction?.message ? '' : 'hide')}>
                  {errors.carLAFunction?.message}
                </span> */}
              </div>
               
              }
            </div>

            {(accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)')) || accessType?.length == 0 ?
              <div></div>
               : 
              <>
                <div className={Styles.flexLayout}>

                {accessType?.includes('Kafka')?
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
                : <div></div>}


                {accessType?.includes('API')?
                  
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

                  : <div></div>}

                  
                </div>
            

                <div className={Styles.flexLayout}>
                  <div
                    className={classNames(`input-field-group include-error ${errors?.personalRelatedData ? 'error' : ''}`)}
                    style={{ minHeight: '50px' }}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Personal data in use? <sup>*</sup>
                    </label>
                    <div className={Styles.radioBtns}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('personalRelatedData', {
                              required: '*Missing entry',
                              // onChange: () => {
                                
                              // },
                            })}
                            type="radio"
                            className="ff-only"
                            name="personalRelatedData"
                            value="No"
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('personalRelatedData', { required: '*Missing entry' })}
                            type="radio"
                            className="ff-only"
                            name="personalRelatedData"
                            value="Yes"
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                    </div>
                    <span className={classNames('error-message')}>{errors?.personalRelatedData?.message}</span>
                  </div>
                  <div
                    className={classNames(`input-field-group include-error`)}
                    style={{ minHeight: '50px' }}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Are there requirements for deletion? <sup>*</sup>
                    </label>
                    <div className={Styles.radioBtns}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('deletionRequirements', {
                              required: '*Missing entry',
                              // onChange: () => {
                                
                              // },
                            })}
                            type="radio"
                            className="ff-only"
                            name="deletionRequirements"
                            value="No"
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('deletionRequirements', { required: '*Missing entry' })}
                            type="radio"
                            className="ff-only"
                            name="deletionRequirements"
                            value="Yes"
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                    </div>
                  </div>
                  <div
                    className={classNames(`input-field-group include-error`)}
                    style={{ minHeight: '50px' }}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Are there any other internal/external policies to restrict access of data? <sup>*</sup>
                    </label>
                    <div className={Styles.radioBtns}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('restrictDataAccess', {
                              required: '*Missing entry',
                              // onChange: () => {
                                
                              // },
                            })}
                            type="radio"
                            className="ff-only"
                            name="restrictDataAccess"
                            value="No"
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('restrictDataAccess', { required: '*Missing entry' })}
                            type="radio"
                            className="ff-only"
                            name="restrictDataAccess"
                            value="Yes"
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                    </div>
                  </div>
                  <div>
                  </div>
                </div>
              </>
            }

            <div>
            {accessType?.length == 0 ? <></>: 
              <div
                className={classNames(`input-field-group include-error`)}
                style={{ minHeight: '50px' }}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Minimum Information Check
                </label>
                
                <div className={Styles.minimumInformationCheckSection}>
                  <div className={Styles.minimumInformationCheckWrapper}>
                    <div className={Styles.infoWrapper}>
                      <i className={'icon mbc-icon info'} onClick={() => {}} />
                    </div>
                    <div className={Styles.descriptionWrapper}>
                      <p>
                      {accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)') ?
                        <><b>No Minimum information required.</b> Please make sure to comply with A22 policies when using SAP/IDM.</>
                      :
                        <><b>Minimum information required.</b> You can either move on by selecting an existing Minimum information to review/edit or fill out the required provider-form in the next few steps. We already selected a fitting How-To-Access information to show your consumers in the next section.</>
                      }
                      </p>
                    </div>
                  </div>
                </div>
                
              </div>
            }  
            </div>
          </div>
          
        </div>
      </div>


      <div className={accessType?.length == 0 ? ' hidden' : Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>How to access - Template&nbsp;
                <i
                  className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                  tooltip-data="How to access"
                />
            </h3>
          </div>
          <div className={Styles.formWrapper}>
            {/* <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error')}>
                <label id="connectionTypeLabel" htmlFor="connectionTypeInput" className="input-label">
                  Use Template
                </label>
                <div className={`custom-select`}>
                  <select id="connectionTypeField" multiple={true} disabled={true} name="connectionType" {...register('useTemplate')}>
                    <option value="">Choose</option>
                    <option id='Kafka0' key={'Kafka'} value={'Kafka'}>Kafka</option>
                    <option id='Live (SAC/AFO)1' key={'Live (SAC/AFO)'} value={'Live (SAC/AFO)'}>Live (SAC/AFO)</option>
                    <option id='API0' key={'API'} value={'API'}>API</option>
                  </select>
                </div>
                
              </div>
              <div></div>
            </div>  */}





            <div id="how-to-access-tabs" className="tabs-panel">
              <div className="tabs-wrapper">
                <nav>
                  <ul className="tabs">
                    <li className={(accessType?.includes('Kafka') ? 'tab' : 'tab')+' active'}>  
                      <a
                        className={accessType?.includes('Kafka') ? '' : 'hidden'}
                        href="#steps-tab-content-1"
                        id="access-via-kafka"
                        onClick={setTab}
                      >
                        Access Via Kafka
                      </a>
                    </li>
                    <li className={accessType?.includes('Live (SAC/AFO)') ? 'tab' : 'tab disabled'}>
                      <a
                        className={accessType?.includes('Live (SAC/AFO)') ? '' : 'hidden'}
                        href="#steps-tab-content-2"
                        id="live-access"
                        onClick={setTab}
                      >
                        Live Access
                      </a>
                    </li>
                    <li className={accessType?.includes('API') ? 'tab' : 'tab disabled'}>
                      <a
                        className={accessType?.includes('API') ? '' : 'hidden'}
                        href="#steps-tab-content-3"
                        id="api-access"
                        onClick={setTab}
                      >
                        API-Access
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="stepTab2" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="stepTab3" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="stepTab4" className={'hidden'}>
                        `
                      </a>
                    </li>
                    
                  </ul>
                </nav>
              </div>
              <div className="tabs-content-wrapper">
                <div id="steps-tab-content-1" className="tab-content">
                  

                  {currentTab === 'access-via-kafka' && kafkaFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={stepItem.id}>  
                    <AccessSteps 
                    value={stepItem}
                    itemIndex={index}
                    showMoveUp={index !== 0}
                    showMoveDown={index + 1 !== kafkaFields.length}
                    onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                    onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                    control={control}
                    update={kafkaUpdate}
                    remove={kafkaRemove}
                    numberedStep = {numberedStep}
                    updateNumberedStep = {() => setNumberedStep(numberedStep+1)}
                    arrayName={'kafkaArray'}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
                      // onClick={addSteps}
                      onClick={ ()=>{
                        kafkaAppend({
                        "stepNumber": '',
                        "stepIconType": "",
                        "stepText": ""
                        })
                      }}
                    >
                      <span className={Styles.addicon}> &nbsp; </span>
                      <label className={Styles.addlabel}>Add Step</label>
                    </button>
                  </div>


                </div>
                <div id="steps-tab-content-2" className="tab-content">
                  
                  {currentTab === 'live-access' && liveAccessFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={stepItem.id}>  
                    <AccessSteps 
                    value={stepItem}
                    itemIndex={index}
                    showMoveUp={index !== 0}
                    showMoveDown={index + 1 !== liveAccessFields.length}
                    onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                    onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                    control={control}
                    update={liveAccessUpdate}
                    remove={liveAccessRemove}
                    numberedStep = {numberedStep}
                    updateNumberedStep = {() => setNumberedStep(numberedStep+1)}
                    arrayName={'liveAccessArray'}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
                      // onClick={addSteps}
                      onClick={ ()=>{
                        liveAccessAppend({
                        "stepNumber": '',
                        "stepIconType": "",
                        "stepText": ""
                        })
                      }}
                    >
                      <span className={Styles.addicon}> &nbsp; </span>
                      <label className={Styles.addlabel}>Add Step</label>
                    </button>
                  </div>
                </div>
                <div id="steps-tab-content-3" className="tab-content">
                  
                  {currentTab === 'api-access' && apiFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={stepItem.id}>  
                    <AccessSteps 
                    value={stepItem}
                    itemIndex={index}
                    showMoveUp={index !== 0}
                    showMoveDown={index + 1 !== apiFields.length}
                    onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                    onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                    control={control}
                    update={apiUpdate}
                    remove={apiRemove}
                    numberedStep = {numberedStep}
                    updateNumberedStep = {() => setNumberedStep(numberedStep+1)}
                    arrayName={'apiArray'}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
                      // onClick={addSteps}
                      onClick={ ()=>{
                        apiAppend({
                        "stepNumber": '',
                        "stepIconType": "",
                        "stepText": ""
                        })
                      }}
                    >
                      <span className={Styles.addicon}> &nbsp; </span>
                      <label className={Styles.addlabel}>Add Step</label>
                    </button>
                  </div>
                </div>
              </div>
            </div>




  


            












          






            {/* <div className={Styles.flexLayout1}>
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
            </div> */}






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
