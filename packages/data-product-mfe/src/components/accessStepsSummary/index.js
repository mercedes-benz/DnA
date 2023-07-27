import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';
import { markdownParser } from 'dna-container/MarkdownParser';

const AccessStepsSummary = (
  { 
    value,
    itemIndex,
    showMoveUp,
    showMoveDown,
    onMoveUp,
    onMoveDown,
}
  ) => {
  const [markdownParserText, setMarkdownParserText] = useState('');


  useEffect(() => {
    setMarkdownParserText(markdownParser(value?.stepText));
    //eslint-disable-next-line
  }, []);

  const onMoveUpSide = () => {
    onMoveUp(itemIndex);
  };

  const onMoveDownSide = () => {
    onMoveDown(itemIndex);
  };


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
                    {
                      value?.stepNumber && value?.stepIconType === 'Numbered' ? 
                      <div className={Styles.stepCounter}>
                        { value?.stepNumber }
                      </div>

                      : 

                        value?.stepIconType === 'Unnumbered'?

                        <i
                          className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                          tooltip-data="How to access"
                        /> 
                        : '' 
                    }    
                  </div>
                  <div className={Styles.editIcon}>
                  
                  </div>
                  <div className={Styles.descriptionWrapper}>  
                        <div className={Styles.descriptionField}>
                            <p
                              className={Styles.stepDescription}
                              dangerouslySetInnerHTML={{
                                __html: markdownParserText,
                              }}
                            ></p>
                        </div>
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
    </>
  );
};

export default AccessStepsSummary;
