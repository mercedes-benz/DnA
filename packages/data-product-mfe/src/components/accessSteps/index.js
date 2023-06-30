import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useForm, useWatch } from 'react-hook-form';

// import InfoModal from 'dna-container/InfoModal';
// import { useDispatch } from 'react-redux';
// import TextArea from 'dna-container/TextArea';
import SelectBox from 'dna-container/SelectBox';
// import { getLegalBasis } from '../../../redux/getDropdowns.services';
// import Tabs from '../../common/modules/uilab/js/src/tabs';

const AccessSteps = (
  { 
    //   onSave 
    // description,
    // stepNumber,
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
    arrayName
}
  ) => {
  const {
    register,
    handleSubmit,
    // formState: { errors },
    // reset,
    // watch,
    // clearErrors,
    setValue,
    // getValues,
  } = useForm({
    defaultValues: {value}
  });
  const [enableEdit, setEnableEdit] = useState(false);
  // const [numberedStep, setNumberedStep] = useState(0);

  const data = useWatch({
    control,
    name: `${arrayName}.${itemIndex}`
  });

//   const dispatch = useDispatch();

//   useEffect(() => {
//     setDescriptionText(description);
//     //eslint-disable-next-line
//   }, [dispatch]);

  useEffect(() => {
    SelectBox.defaultSetup();
    console.log(value,'Showing value coming from parent');
    // dispatch(getLegalBasis());
    //eslint-disable-next-line
  }, [enableEdit]);

  useEffect(() => {
    setValue(`stepNumber`, numberedStep );
    // Tabs.defaultSetup();
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    setValue(`stepText`, data?.stepText );
    //eslint-disable-next-line
  }, [data?.stepText]);

  const onMoveUpSide = () => {
    onMoveUp(itemIndex);
  };

  const onMoveDownSide = () => {
    onMoveDown(itemIndex);
  };

  // const onDescChange = (val) => {
  //   setValue(`stepText`, val.target.value );
  // };

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
                            // {...register(`stepsArray.${itemIndex}.stepIconType`)}
                            {...register(`stepIconType`,{value: data?.stepIconType})}
                            >
                                <option id='Numbered0' key={'Numbered'} value={'Numbered'}>Numbered</option>
                                <option id='Unnumbered1' key={'Unnumbered'} value={'Unnumbered'}>Unnumbered</option>
                            </select>
                            <input type='hidden' {...register(`stepNumber`,{value:
                            numberedStep
                            })}/>
                        </div>
                        :
                         data?.stepNumber && data?.stepIconType === 'Numbered' ? 
                          <div className={Styles.stepCounter}>
                            {/* { data?.stepIconType } */}
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
                  {!enableEdit ?
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
                  {!enableEdit ?
                  
                    <p>
                        { data?.stepText }
                    </p>
                  
                   :
                   <>
                        <div className={Styles.descriptionField}>
                            {/* <TextArea
                            controlId={'stepTextField'}
                            containerId={'stepText'}
                            rows={4}
                            value = {value?.stepText}
                            required={false}
                            onChange={onDescChange}
                            /> */}
                            <textarea
                              id="description"
                              className={"input-field-area "+Styles.textArea}
                              type="text"
                              rows={4}
                              {...register('stepText')}
                            />
                        </div>
                        <div>
                            {/* <span className={Styles.addLink}>Add link</span>&nbsp;&nbsp;&nbsp;&nbsp; */}
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
                              update(itemIndex, data1);
                              setEnableEdit(false);
                            })}
                            >Save entry</span>
                            
                        </div>
                   </>
                                      
                    }            
                    
                    
                  </div>
                  <div className={Styles.removeAction}>
                    {/* <span className={Styles.saveEntry} 
                      onClick={() => {
                        remove(itemIndex);
                      }}
                    >Remove</span> */}
                    <i
                      onClick={ () => {
                        remove(itemIndex)
                      }}
                      className={classNames(Styles.deleteIcon, 'icon delete')}
                    />
                  </div>
                </div>
              </div>
              
            </div>



            <span 
            onClick={onMoveUpSide} 
            className={classNames(Styles.orderUp, 
            // showMoveUp ? '' : 'hide'
            showMoveUp ? 'hide' : 'hide'
            )}>
            <i className="icon mbc-icon arrow small up" />
            </span>
            <span
            onClick={onMoveDownSide}
            className={classNames(Styles.orderDown, 
            // showMoveDown ? '' : 'hide'
            showMoveDown ? 'hide' : 'hide'
            )}
            >
            <i className="icon mbc-icon arrow small down" />
            </span>
        </div>
    </>
  );
};

export default AccessSteps;
