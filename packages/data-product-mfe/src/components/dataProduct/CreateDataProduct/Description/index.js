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
// import Modal from 'dna-container/Modal';
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
  onChangeConfidentialityInDescription,
  onChangeAccessType, 
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
  const [currentInnerTab, setCurrentInnerTab] = useState('access-via-kafka');
  const [currentPreviewTab, setCurrentPreviewTab] = useState('access-via-kafka-Preview');
  // const [howToAccessTemplateObj, setHowToAccessTemplateObj] = useState({});
  const [numberedStep, setNumberedStep] = useState(0);
  const [numberedLiveStep, setNumberedLiveStep] = useState(0);
  const [numberedApiStep, setNumberedApiStep] = useState(0);

  const [stepsList, setStepsList] = useState([]);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [accessTypeCount, setAccessTypeCount] = useState(0);

  const { howToAccessText, tags, productOwner, 
    // useTemplate, 
    confidentialityInDescription,
    accessType } = watch();


  useEffect(() => {
    Tooltip.defaultSetup();
    Tabs.defaultSetup(document.querySelectorAll('.inner-tabs'));
    
    
    reset(watch());
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    // const kafkaStepsMaxCount = Math.max.apply(Math, kafkaFields?.map(function(o) { return o.stepNumber; }));
    // const liveAccessStepsMaxCount = Math.max.apply(Math, liveAccessFields?.map(function(o) { return o.stepNumber; }));
    // const apiStepsMaxCount = Math.max.apply(Math, apiFields?.map(function(o) { return o.stepNumber; }));

    let kafkaStepsMaxCount = 0;
    kafkaFields?.map(function(o,index) { 
      if (o.stepIconType=='Numbered')
      {
        kafkaStepsMaxCount += 1;
        setValue(`kafkaArray.${index}.stepNumber`,kafkaStepsMaxCount)
      }
      return kafkaStepsMaxCount; });


    let liveAccessStepsMaxCount = 0;
    liveAccessFields?.map(function(o, index) { 
      if (o.stepIconType=='Numbered')
      {
        liveAccessStepsMaxCount += 1;
        setValue(`liveAccessArray.${index}.stepNumber`,liveAccessStepsMaxCount)
      }
      return liveAccessStepsMaxCount; });


    let apiStepsMaxCount = 0;
    apiFields?.map(function(o, index) { 
      if (o.stepIconType=='Numbered')
      {
        apiStepsMaxCount += 1;
        setValue(`apiArray.${index}.stepNumber`,apiStepsMaxCount)
      }
      return apiStepsMaxCount; });


    



    if(kafkaStepsMaxCount > 0)
      setNumberedStep(kafkaStepsMaxCount);
    else{
      setNumberedStep(0);
    }

    if(liveAccessStepsMaxCount > 0)
    setNumberedLiveStep(liveAccessStepsMaxCount);
    else{
      setNumberedLiveStep(0);
    }

    if(apiStepsMaxCount > 0)
      setNumberedApiStep(apiStepsMaxCount);
    else{
      setNumberedApiStep(0);
    }
    //eslint-disable-next-line
  }, [kafkaFields, liveAccessFields, apiFields]);


  useEffect(()=>{
    setTagsListST(tagsList);
    //eslint-disable-next-line
  },[tagsList]);

  useEffect(()=>{
    SelectBox.defaultSetup();
    //eslint-disable-next-line
  },[]);

  useEffect(() => {
    setSelectedTags(tags);
  }, [tags]);
  

  useEffect(() => {
    if(accessType?.length > 0 && (accessType?.includes('Kafka') || accessType?.includes('API'))){
       SelectBox.defaultSetup(true);
    }



    /****************************************************************** 
     ********** Start of selecting default tab on selection ***********
     ******************************************************************/
    if(accessType?.length > 0 && accessTypeCount === 0){
      setAccessTypeCount(1);
      let tabDetails = '';
      if(accessType?.length > 0 && accessType[0] == 'Kafka'){
        tabDetails = document.getElementById('access-via-kafka');
        tabDetails?.click();
      }
      else if(accessType?.length > 0 && accessType[0] == 'Live (SAC/AFO)'){
        tabDetails = document.getElementById('live-access');
        tabDetails?.click();
      }
      else if(accessType?.length > 0 && accessType[0] == 'API')  {
        tabDetails = document.getElementById('api-access');
        tabDetails?.click();
      }
    }
    /****************************************************************** 
     ********** End of selecting default tab on selection ***********
     ******************************************************************/






    /****************************************************************** 
     ********** Start of setting default value on selection ***********
     ******************************************************************/

    if(accessType?.length > 0 && !accessType.includes('Kafka')){
      setValue('kafkaArray',[]);
    }
    if(accessType?.length > 0 && !accessType.includes('Live (SAC/AFO)')){
      setValue('liveAccessArray',[]);
    }
    if(accessType?.length > 0 && !accessType.includes('API'))  {
      setValue('apiArray',[]);
    }
    if(accessType?.length == 1 && accessType.includes('Live (SAC/AFO)')){
      setValue('confidentialityInDescription','');
    }
    
    if((accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)')) || accessType?.length == 0 || confidentialityInDescription == 'Internal'){
      setValue('personalRelatedDataInDescription', 'No');
      setValue('deletionRequirementInDescription', 'No');
      setValue('restrictDataAccess', 'No');
    }
    /****************************************************************** 
     ********** End of setting default value on selection ***********
     ******************************************************************/


     
    //eslint-disable-next-line
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
    if (currentInnerTab !== id) {
      setCurrentInnerTab(id);
    }
  };

  const setPreviewTab = (e) => {
    const id = e.target.id;
    if (currentPreviewTab !== id) {
      setCurrentPreviewTab(id);
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

  const nextPreviewTab = () => {
    let nextTab = '';
    if(currentPreviewTab === 'access-via-kafka-preview'){
      if(accessType?.includes('Live (SAC/AFO)'))
      nextTab = 'live-access-preview';
      else if(accessType?.includes('API'))
      nextTab = 'api-access-preview';
      else
      nextTab = 'access-via-kafka-preview';
    }
    if(currentPreviewTab === 'live-access-preview'){
      if(accessType?.includes('API'))
      nextTab = 'api-access-preview';
      else
      nextTab = 'live-access-preview';
    }
    const tabDetails = document.getElementById(nextTab);
    tabDetails?.click();
  };

  const previewModalContent = (
      <>
        <div className={Styles.accessModalHeader}>
          <div className={Styles.accessModalHeaderIcon}>
            <i
              className={classNames('icon mbc-icon help iconsmd', Styles.infoIcon)}
            /> 
          </div>
          <div className={Styles.accessModalHeaderText}>
            <h3>How To Access</h3>
          </div>
        </div>
        <div id="how-to-access-preview-tabs" className={"tabs-panel "+accessType?.length < 1 ? 'hidden':''}>
          <div className="tabs-wrapper">
            <nav>
              <ul className="inner-preview-tabs tabs">
                
                <li className={(accessType?.includes('Kafka') ? 'inner-preview-tab tab active' : ('inner-preview-tab tab ' + Styles.widthZero))}>  
                  <a
                    className={accessType?.includes('Kafka') ? '' : 'hidden'}
                    href="#preview-steps-tab-content-1"
                    id="access-via-kafka-preview"
                    onClick={setPreviewTab}
                  >
                    Kafka-Access
                  </a>
                </li>
                
                <li className={accessType?.includes('Live (SAC/AFO)') ? 'inner-preview-tab tab' : ('inner-preview-tab tab disabled ' + Styles.widthZero)}>
                  <a
                    className={accessType?.includes('Live (SAC/AFO)') ? '' : 'hidden'}
                    href="#preview-steps-tab-content-2"
                    id="live-access-preview"
                    onClick={setPreviewTab}
                  >
                    Live (SAC/AFO)-Access
                  </a>
                </li>
                
                <li className={accessType?.includes('API') ? 'inner-preview-tab tab' : ('inner-preview-tab tab disabled ' + Styles.widthZero)}>
                  <a
                    className={accessType?.includes('API') ? '' : 'hidden'}
                    href="#preview-steps-tab-content-3"
                    id="api-access-preview"
                    onClick={setPreviewTab}
                  >
                    API-Access
                  </a>
                </li>
                
                <li className={'inner-preview-tab tab disabled'}>
                  <a id="stepTab2-preview" className={'hidden'}>
                    `
                  </a>
                </li>
                <li className={'inner-preview-tab tab disabled'}>
                  <a id="stepTab3-preview" className={'hidden'}>
                    `
                  </a>
                </li>
                <li className={'inner-preview-tab tab disabled'}>
                  <a id="stepTab4-preview" className={'hidden'}>
                    `
                  </a>
                </li>
                
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="preview-steps-tab-content-1" className="inner-preview-tab-content tab-content">
              
              {currentPreviewTab === 'access-via-kafka-preview' && kafkaFields?.map((stepItem, index)=>{
                return(
                <fieldset key={'access-via-kafka-preview'+stepItem.id}>  
                <AccessSteps 
                value={stepItem}
                itemIndex={index}
                showMoveUp={index !== 0}
                showMoveDown={index + 1 !== kafkaFields.length}
                onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                control={control}
                // update={kafkaUpdate}
                // remove={kafkaRemove}
                numberedStep = {numberedStep}
                updateNumberedStep = {() => setNumberedStep(numberedStep+1)}
                arrayName={'kafkaArray'}
                isEditable={false}
                />
                </fieldset>
                )
              })}
            </div>
            <div id="preview-steps-tab-content-2" className="inner-preview-tab-content tab-content">
              
              {currentPreviewTab === 'live-access-preview' && liveAccessFields?.map((stepItem, index)=>{
                return(
                <fieldset key={'live-access-preview'+stepItem.id}>  
                <AccessSteps 
                value={stepItem}
                itemIndex={index}
                showMoveUp={index !== 0}
                showMoveDown={index + 1 !== liveAccessFields.length}
                onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                control={control}
                // update={liveAccessUpdate}
                // remove={liveAccessRemove}
                numberedStep = {numberedLiveStep}
                updateNumberedStep = {() => setNumberedLiveStep(numberedLiveStep+1)}
                arrayName={'liveAccessArray'}
                isEditable={false}
                />
                </fieldset>
                )
              })}
            </div>
            <div id="preview-steps-tab-content-3" className="inner-preview-tab-content tab-content">
              
              {currentPreviewTab === 'api-access-preview' && apiFields?.map((stepItem, index)=>{
                return(
                <fieldset key={'api-access-preview'+stepItem.id}>  
                <AccessSteps 
                value={stepItem}
                itemIndex={index}
                showMoveUp={index !== 0}
                showMoveDown={index + 1 !== apiFields.length}
                onMoveUp={(index)=>onTeamMemberMoveUp(index)}
                onMoveDown={(index)=>onTeamMemberMoveDown(index)}
                control={control}
                // update={apiUpdate}
                // remove={apiRemove}
                numberedStep = {numberedApiStep}
                updateNumberedStep = {() => setNumberedApiStep(numberedApiStep+1)}
                arrayName={'apiArray'}
                isEditable={false}
                />
                </fieldset>
                )
              })}
            </div>
          </div>
        </div>
        <div className={Styles.actionButtonsPreview}>
          <button
            onClick={()=>{
              setShowPreviewModal(false);
            }}
          >OK, got it</button>
          <button
          
            onClick={()=>{
              nextPreviewTab();
            }}
          >Next</button>
        </div>
      </>      
  );

  

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
                <label id="accessTypeLabel" htmlFor="accessTypeInput" className="input-label">
                  Access
                </label>
                <div className={`custom-select`}>
                  <select id="accessTypeField" multiple={true} name="accessType" {...register('accessType',{
                    onChange:(e)=>{
                      
                      const options = e.target.selectedOptions;
                      const values = Array.from(options).map(({ value }) => value);

                      if(e.target.value?.length == 1 && e.target.value?.includes('Live (SAC/AFO)')){
                        setValue('personalRelatedDataInDescription', 'No');
                        setValue('deletionRequirementInDescription', 'No');
                        setValue('restrictDataAccess', 'No');
                        setValue('kafkaArray', []);
                        setValue('liveAccessArray', []);
                        setValue('apiArray', []);
                      }
                      // if(!e.target.value?.includes('Kafka')){
                      //   setValue('kafkaArray', []);
                      // }
                      // if(!e.target.value?.includes('Live (SAC/AFO)')){
                      //   setValue('liveAccessArray', []);
                      // }
                      // if(!e.target.value?.includes('API')){
                      //   setValue('apiArray', []);
                      // }
                      onChangeAccessType(values);
                    }
                  })}>
                    {/* <option value="">Choose</option> */}
                    <option id='Kafka' key={'Kafka'} value={'Kafka'}>Kafka</option>
                    <option id='Live (SAC/AFO)' key={'Live (SAC/AFO)'} value={'Live (SAC/AFO)'}>Live (SAC/AFO)</option>
                    <option id='API' key={'API'} value={'API'}>API</option>
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
                  <select id="confidentialityField" name="confidentialityInDescription" {...register('confidentialityInDescription',{
                    onChange:(e)=>{
                      if(e.target.value == 'Internal'){
                        setValue('personalRelatedDataInDescription', 'No');
                        setValue('deletionRequirementInDescription', 'No');
                        setValue('restrictDataAccess', 'No');
                      }
                      onChangeConfidentialityInDescription(e.target.value);
                    }
                  })}>
                    <option value="">Choose</option>
                    <option id='Internal' key={'Internal'} value={'Internal'}>Internal</option>
                    <option id='Secret' key={'Secret'} value={'Secret'}>Secret</option>
                    <option id='Confidential' key={'Confidential'} value={'Confidential'}>Confidential</option>
                    <option id='Public' key={'Public'} value={'Public'}>Public</option>
                  </select>
                </div>
                {/* <span className={classNames('error-message', errors.carLAFunction?.message ? '' : 'hide')}>
                  {errors.carLAFunction?.message}
                </span> */}
              </div>
               
              }
            </div>

            {(accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)')) || accessType?.length == 0 || confidentialityInDescription == 'Internal' ?
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
                      placeholder="https://example@example.com"
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
                    className={classNames(`input-field-group include-error`)}
                    style={{ minHeight: '50px' }}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Personal data in use? <sup>*</sup>
                    </label>
                    <div className={Styles.radioBtns}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('personalRelatedDataInDescription', {
                              required: '*Missing entry',
                              // onChange: () => {
                                
                              // },
                            })}
                            type="radio"
                            className="ff-only"
                            name="personalRelatedDataInDescription"
                            value="No"
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('personalRelatedDataInDescription', { required: '*Missing entry' })}
                            type="radio"
                            className="ff-only"
                            name="personalRelatedDataInDescription"
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
                      Are there requirements for deletion? <sup>*</sup>
                    </label>
                    <div className={Styles.radioBtns}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('deletionRequirementInDescription', {
                              required: '*Missing entry',
                              // onChange: () => {
                                
                              // },
                            })}
                            type="radio"
                            className="ff-only"
                            name="deletionRequirementInDescription"
                            value="No"
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('deletionRequirementInDescription', { required: '*Missing entry' })}
                            type="radio"
                            className="ff-only"
                            name="deletionRequirementInDescription"
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
            {accessType?.length == 0 ? '': 
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
                      {(accessType?.length == 1 && accessType?.includes('Live (SAC/AFO)')) || confidentialityInDescription == 'Internal' ?
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


      <div className={accessType?.length == 0 ? ' hidden '+Styles.heightZero : Styles.wrapper}>
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









            <div id="how-to-access-tabs" className={"tabs-panel "+accessType?.length < 1 ? 'hidden':''}>
              <div className="tabs-wrapper">
                <nav>
                  <ul className="inner-tabs tabs">
                    
                    <li className={(accessType?.includes('Kafka') ? 'inner-tab tab active' : ('inner-tab tab ' + Styles.widthZero))}>  
                      <a
                        className={accessType?.includes('Kafka') ? '' : 'hidden'}
                        href="#steps-tab-content-1"
                        id="access-via-kafka"
                        onClick={setTab}
                      >
                        Kafka-Access
                      </a>
                    </li>
                    
                    <li className={accessType?.includes('Live (SAC/AFO)') ? 'inner-tab tab' : ('inner-tab tab disabled ' + Styles.widthZero)}>
                      <a
                        className={accessType?.includes('Live (SAC/AFO)') ? '' : 'hidden'}
                        href="#steps-tab-content-2"
                        id="live-access"
                        onClick={setTab}
                      >
                        Live (SAC/AFO)-Access
                      </a>
                    </li>
                   
                    <li className={accessType?.includes('API') ? 'inner-tab tab' : ('inner-tab tab disabled ' + Styles.widthZero)}>
                      <a
                        className={accessType?.includes('API') ? '' : 'hidden'}
                        href="#steps-tab-content-3"
                        id="api-access"
                        onClick={setTab}
                      >
                        API-Access
                      </a>
                    </li>
                    
                    <li className={'inner-tab tab disabled'}>
                      <a id="stepTab2" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'inner-tab tab disabled'}>
                      <a id="stepTab3" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'inner-tab tab disabled'}>
                      <a id="stepTab4" className={'hidden'}>
                        `
                      </a>
                    </li>
                    
                  </ul>
                </nav>
              </div>
              <div className="tabs-content-wrapper">
                <div id="steps-tab-content-1" className="inner-tab-content tab-content">
                  
                  {currentInnerTab === 'access-via-kafka' && kafkaFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={'access-via-kafka'+stepItem.id}>  
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
                    isEditable={true}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
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
                <div id="steps-tab-content-2" className="inner-tab-content tab-content">
                  
                  {currentInnerTab === 'live-access' && liveAccessFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={'live-access'+stepItem.id}>  
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
                    numberedStep = {numberedLiveStep}
                    updateNumberedStep = {() => setNumberedLiveStep(numberedLiveStep+1)}
                    arrayName={'liveAccessArray'}
                    isEditable={true}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
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
                <div id="steps-tab-content-3" className="inner-tab-content tab-content">
                  
                  {currentInnerTab === 'api-access' && apiFields?.map((stepItem, index)=>{
                    return(
                    <fieldset key={'api-access'+stepItem.id}>  
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
                    numberedStep = {numberedApiStep}
                    updateNumberedStep = {() => setNumberedApiStep(numberedApiStep+1)}
                    arrayName={'apiArray'}
                    isEditable={true}
                    />
                    </fieldset>
                    )
                  })}

                  <div>
                    <button
                      className={classNames('data-row', Styles.listViewContainer)}
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







            <div className={Styles.howToAccessPopupLink}>
              <span onClick={()=>{
                setShowPreviewModal(true);
                Tabs.defaultSetup(document.querySelectorAll('.inner-preview-tabs'));
                setTimeout(() => {
                  let tabDetails = '';
                  if(accessType?.length > 0 && accessType[0] == 'Kafka'){
                    tabDetails = document.getElementById('access-via-kafka-preview');
                    tabDetails?.click();
                  }
                  else if(accessType?.length > 0 && accessType[0] == 'Live (SAC/AFO)'){
                    tabDetails = document.getElementById('live-access-preview');
                    tabDetails?.click();
                  }
                  else if(accessType?.length > 0 && accessType[0] == 'API')  {
                    tabDetails = document.getElementById('api-access-preview');
                    tabDetails?.click();
                  }
                }, 100);
                }}
              className={Styles.howToAccessPopupLinkText}
              >Preview How-To-Access Pop-up</span>
            </div>
            <div>
              <InfoModal
                show={showPreviewModal}
                content={previewModalContent}
                onCancel={() => setShowPreviewModal(false)}
              />
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
