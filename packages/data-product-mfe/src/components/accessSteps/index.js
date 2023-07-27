import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useForm, useWatch } from 'react-hook-form';

import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/Modal';
import { markdownParser, htmlToMarkdownParser } from 'dna-container/MarkdownParser';

const AccessSteps = (
  { 
    value,
    itemIndex,
    showMoveUp,
    showMoveDown,
    onMoveUp,
    onMoveDown,
    control,
    update,
    remove,
    numberedStep,
    updateNumberedStep,
    arrayName,
    isEditable
}
  ) => {
  const {
    register,
    handleSubmit,
    setValue,
  } = useForm({
    defaultValues: {value}
  });
  const [enableEdit, setEnableEdit] = useState(false);
  const [showLinkModal, setShowLinkModal] = useState(false);
  const [linkLabel, setLinkLabel] = useState('');
  const [linkUrl, setLinkUrl] = useState('');
  const [showDesc, setShowDesc] = useState(true);
  const [markdownParserText, setMarkdownParserText] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemIndexToDelete, setItemIndexToDelete] =useState('');

  const data = useWatch({
    control,
    name: `${arrayName}.${itemIndex}`
  });

  useEffect(() => {
    SelectBox.defaultSetup();
    console.log(value,'Showing value coming from parent');
    //eslint-disable-next-line
  }, [enableEdit]);

  useEffect(() => {
    setValue(`stepNumber`, numberedStep );
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    // setValue(`stepText`, data?.stepText );
    if(data?.stepText)
    setMarkdownParserText(markdownParser(data?.stepText));
    //eslint-disable-next-line
  }, []);

  const onMoveUpSide = () => {
    onMoveUp(itemIndex);
  };

  const onMoveDownSide = () => {
    onMoveDown(itemIndex);
  };


  const addLink = () => {
    setShowDesc(false);
    let tempText = markdownParserText;
    tempText += ' ['+linkLabel+']('+linkUrl+') ';
    
    setMarkdownParserText(markdownParser(tempText));
    setValue('stepText', tempText);
    
    setShowDesc(true);
    setShowLinkModal(false);
    setLinkLabel('');
    setLinkUrl('');
  };

  const addLinkModalContent = (
    <div className={Styles.addLinkModalContent}>
      <br />
      <div>
        <div className={Styles.flexLayout}>    
          <div className={classNames('input-field-group include-error', 
          // this.state.errors.kpiLink ? 'error' : ''
          )}>
            <label id="kpiLinkLabel" htmlFor="kpiLinkField" className="input-label">
            Label for Link
            </label>
            <input
              type="text"
              className="input-field"
              name="kpiLink"
              id="kpiLinkInput"
              maxLength={200}
              // placeholder=""
              autoComplete="off"
              value={linkLabel}
              onChange={(e)=>setLinkLabel(e.currentTarget.value)}
            />
            {/* <span className={classNames('error-message', this.state.errors.kpiLink.length ? '' : 'hide')}>
              {this.state.errors.kpiLink}
            </span> */}
          </div> 
          <div className={classNames('input-field-group include-error', 
          // this.state.errors.kpiLink ? 'error' : ''
          )}>
            <label id="kpiLinkLabel" htmlFor="kpiLinkField" className="input-label">
            Url
            </label>
            <input
              type="text"
              className="input-field"
              name="kpiLink"
              id="kpiLinkInput"
              maxLength={200}
              placeholder="https://www.example.com"
              autoComplete="off"
              value={linkUrl}
              onChange={(e)=>setLinkUrl(e.currentTarget.value)}
            />
            {/* <span className={classNames('error-message', this.state.errors.kpiLink.length ? '' : 'hide')}>
              {this.state.errors.kpiLink}
            </span> */}
          </div>            
        </div>
        <div>
          <div className="btnConatiner">
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => {addLink()}}
            >
              Add
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <>
        <div className={Styles.memberListWrapper}>
        

            <div
              className={classNames(`input-field-group include-error`)}
              style={{ minHeight: '50px' }}
            >
              <div className={Styles.howToAccessStepsSection}>
                <div className={Styles.howToAccessStepsWrapper}>
                  <div className={Styles.infoWrapper}>
                    {enableEdit?
                        <div className={`custom-select`}>
                            <select id="stepIconTypeField" name="stepIconType" 
                            {...register(`stepIconType`,{value: data?.stepIconType ? data?.stepIconType : 'Numbered'})}
                            >
                                <option id='Numbered' key={'Numbered'} value={'Numbered'}>Numbered</option>
                                <option id='Unnumbered' key={'Unnumbered'} value={'Unnumbered'}>Unnumbered</option>
                            </select>
                            <input type='hidden' {...register(`stepNumber`,{value:
                            numberedStep
                            })}/>
                        </div>
                        :
                         data?.stepNumber && data?.stepIconType === 'Numbered' ? 
                          <div className={Styles.stepCounter}>
                            { data?.stepNumber }
                          </div>

                          : 

                            data?.stepIconType === 'Unnumbered'?

                            <i
                              className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                              tooltip-data="How to access"
                            /> 
                            : '' 
                        
                    }    
                  </div>
                  <div className={Styles.editIcon}>
                  {!enableEdit && isEditable ?
                        <i
                        className={classNames('icon mbc-icon edit iconsmd', Styles.infoIcon)}
                        tooltip-data="Edit step"
                        onClick={()=>{                            
                            setEnableEdit(true);
                            }}
                        />
                        : ''}
                  </div>
                  <div className={Styles.descriptionWrapper}>     
                  
                        <div className={Styles.descriptionField}>
                            {showDesc ? 
                            <p contentEditable={enableEdit ? "true" : "false"} {...register('stepText')}
                              className={Styles.stepDescription}
                              onInput={(e) => {
                                setValue('stepText',htmlToMarkdownParser(e.target.innerHTML))
                                setMarkdownParserText(markdownParserText, ...e.target.innerHTML);
                              }}
                              onBlur={(e) => {
                                let tempText2 = '';
                                tempText2 += e.target.innerHTML;                        
                                setValue('stepText',htmlToMarkdownParser(tempText2))
                                setMarkdownParserText(tempText2);
                              }}
                              dangerouslySetInnerHTML={{
                                __html: markdownParserText,
                              }}
                              placeholder="Description..."
                            ></p>
                            : ''}
                        </div>
                    {!enableEdit ?   
                    '' 
                      :
                      <>  
                        <div>
                            <span className={Styles.saveEntry}
                            onClick={() => {
                              // setMarkdownParserText(markdownParserText, ...markdownParserText)
                              setShowLinkModal(true)}}
                            >Add Link</span>
                            {' '}&nbsp;&nbsp;
                            <span className={Styles.saveEntry} 
                            onClick={handleSubmit((data1) => {
                              if(data1?.stepIconType == 'Numbered'){
                                if(!data?.stepNumber){
                                  updateNumberedStep(numberedStep+1);
                                  data1.stepNumber = numberedStep+1;
                                } 
                                else{
                                  updateNumberedStep(data?.stepNumber);
                                  data1.stepNumber = data?.stepNumber;
                                }
                                
                              }
                              data1.stepText = htmlToMarkdownParser(markdownParserText)
                              update(itemIndex, data1);
                              setEnableEdit(false);
                            })}
                            >Save entry</span>
                            
                        </div>
                   </>
                                      
                    }            
                    
                    
                  </div>
                  <div className={Styles.removeAction}>
                    {isEditable ? 
                      <i
                        onClick={ () => {
                          setItemIndexToDelete(itemIndex);
                          setShowDeleteModal(true);
                        }}
                        className={classNames(Styles.deleteIcon, 'icon delete')}
                      />
                    : ''}
                  </div>
                </div>
              </div>
              
            </div>



            <span 
            onClick={onMoveUpSide} 
            className={classNames(Styles.orderUp, 
            showMoveUp ? 'hide' : 'hide'
            )}>
            <i className="icon mbc-icon arrow small up" />
            </span>
            <span
            onClick={onMoveDownSide}
            className={classNames(Styles.orderDown, 
            showMoveDown ? 'hide' : 'hide'
            )}
            >
            <i className="icon mbc-icon arrow small down" />
            </span>
        </div>
        {showLinkModal && (
          <Modal
            title={"Add Link"}
            show={showLinkModal}
            content={addLinkModalContent}
            onCancel={() => setShowLinkModal(false)}
          />
        )}
        <ConfirmModal
            title="Are you sure you want to delete this?"
            acceptButtonTitle="Delete"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={showDeleteModal}
            // content={
            //   <div id="contentparentdiv">
            //     Are you sure you want to delete this?
            //   </div>
            // }
            onCancel={() => {
              setShowDeleteModal(false);
            }}
            onAccept={() => {
              remove(itemIndexToDelete);
            }}
          />
    </>
  );
};

export default AccessSteps;
