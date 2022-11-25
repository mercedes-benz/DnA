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
    isSmallCard?: boolean;
    isMediumCard?: boolean;
    tags?: string[];
}
const DNACard = (props: IDNACardProps) => {
 
  useEffect(() => {
    // Tooltip.defaultSetup();
  }, []);

  const maxTagItem = 4;

  return (
    <>
      <div className={classNames(Styles.cardWrapper, props.isMediumCard ? Styles.mediumCard : '', props.isSmallCard ? Styles.smallCard : '', props.isDisabled ?Styles.disabled : '')}
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
        <div className={Styles.tagSection}>
            {props.tags?.slice(0, maxTagItem)?.map((item: any) => {
                return (
                <span className={Styles.tagItem} key={item}>
                    {item}
                </span>
                );
            })}
            {props?.tags?.length > maxTagItem ? <span className={Styles.tagItem}>...</span> : null}
        </div>
      </div>
      
    </>
  );
};
export default DNACard;
