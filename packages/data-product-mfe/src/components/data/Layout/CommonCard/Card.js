import cn from 'classnames';
import React, { useEffect } from 'react';
import Styles from './Card.styles.scss';
import { withRouter } from 'react-router-dom';
import AddKPIIcon from 'dna-container/AddKPIIcon'
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
const classNames = cn.bind(Styles);

const DataProductCard = ({ 
    isDisabled,
    title,
    description,
    url,
    isTextAlignLeft,
    isSmallCard=false,
    history
}) => {
 
  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  return (
    <>
      <div className={classNames(Styles.cardWrapper, isSmallCard ? Styles.smallCard : '', isDisabled ?Styles.disabled : '')}
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
export default withRouter(DataProductCard);
