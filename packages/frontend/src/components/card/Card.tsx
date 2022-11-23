import cn from 'classnames';
import React, { useEffect } from 'react';
import Styles from './Card.scss';
// import { withRouter } from 'react-router-dom';
// import AddKPIIcon from 'dna-container/AddKPIIcon';
// import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import IconAddKPI from 'components/icons/IconAddKPI';
import { history } from '../../router/History';
const classNames = cn.bind(Styles);

export interface IDNACardProps {
    isDisabled: boolean;
    title: string;
    description: string;
    url: string;
    isTextAlignLeft: boolean;
    isSmallCard: boolean;
}
const DNACard = (props: IDNACardProps) => {
 
  useEffect(() => {
    // Tooltip.defaultSetup();
  }, []);

  return (
    <>
      <div className={classNames(Styles.cardWrapper, props.isSmallCard ? Styles.smallCard : '', props.isDisabled ?Styles.disabled : '')}
        onClick={() => {
            history.push(props.url);
        }}
      >
        <div className={Styles.cardHeaderSection}>
            {props.isDisabled ? (
                <button className={'btn btn-primary '+Styles.comingSoonBtn}>Coming Soon</button>
                ) : ''}
        </div>
        <div className={Styles.cardIconSection}>
            <IconAddKPI className={Styles.avatarIcon} />
        </div>
        <div className={props.isTextAlignLeft ? Styles.cardDescriptonSection : (Styles.cardDescriptonSection+' '+Styles.textCenter)}>
          <div className={Styles.titleSection}> 
            {props.title}
          </div>
          <div className={Styles.descriptionSection}>
            {props.description}
          </div>
        </div>
      </div>
    </>
  );
};
export default DNACard;
