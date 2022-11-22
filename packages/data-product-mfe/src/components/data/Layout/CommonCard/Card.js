import React, { useEffect } from 'react';
import Styles from './Card.styles.scss';
import { withRouter } from 'react-router-dom';
import AddKPIIcon from 'dna-container/AddKPIIcon'
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';

const Card = ({ 
    isDisabled,
    title,
    description,
    url,
    history,
    isTextAlignLeft
}) => {
 
  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  return (
    <>
      <div className={isDisabled ? Styles.cardWrapper +' '+Styles.disabled : Styles.cardWrapper}
        onClick={() => {
            history.push(url);
        }}
      >
        <div className={Styles.cardHeaderSection}>
            {isDisabled ? (
                <button className={'btn btn-primary '+Styles.comingSoonBtn}>Coming Soon</button>
                ) : ''}
        </div>
        <div className={Styles.cardIconSection}>
            <AddKPIIcon className={Styles.avatarIcon} />
        </div>
        <div className={isTextAlignLeft ? Styles.cardDescriptonSection : (Styles.cardDescriptonSection+' '+Styles.textCenter)}>
          <div className={Styles.titleSection}> 
            {title}
          </div>
          <div className={Styles.descriptionSection}>
            {description}
          </div>
        </div>
      </div>
    </>
  );
};
export default withRouter(Card);
