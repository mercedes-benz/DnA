import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './SubscriptionCard.scss';
import IconNameRenderer from 'components/icons/IconNameRenderer';
import { getParams } from '../../../../../router/RouterUtils';
import { SubscriptionDetails } from './SubscriptionInfo';
const classNames = cn.bind(Styles);

export interface ISubscriptionData {
  id?: string;
  name?: string;
  tiles?: {};
}

const SubscriptionCard = (ISubscriptionData: any) => {
  const history = useHistory();
  const [pageDetails, setPageDetails] = useState(ISubscriptionData);

  useEffect(() => {
    const params = getParams();
    const id = params.id;
    for (const data of SubscriptionDetails) {
      if (data.id === id) {
        setPageDetails(data);
      }
    }
  }, []);

  const onOderClick = (url : any) =>{
    if(url.length > 0){
      window.open(url)
    } else {
      localStorage.setItem('modal', 'shared');
      history.push('/powerplatform');
    }
  }

  return (
    <div className={classNames(Styles.wrapper)}>
      {
        pageDetails?.tiles && (
          pageDetails.tiles.map((tile: any, key: any) =>
            <div
              key={key}
              className={classNames(Styles.cardWrapper)}
            >
              <div className={classNames(Styles.titleSection)}>
                <div className={classNames(Styles.title)}><span className={classNames(Styles.titletext)}>{tile.title}</span></div>
                <div className={classNames(Styles.subTitle)}><span>{tile.subTitle}</span></div>
              </div>
              <div className={Styles.cardIconSection}>
                <IconNameRenderer name={tile.icon} />
              </div>
              <div className={classNames(Styles.infoSection)}>
                <span>{tile.info.text}</span>
                <pre>{tile.info.cost}</pre>
              </div>
              <div className={Styles.actionWrapper}>
              <button className={classNames("btn btn-primary", Styles.Btn)} onClick={() => onOderClick(tile.link)} type="button">
                {tile.id === 'default-environment' ? 'Get Started' : 'Order'}
              </button>
            </div>
            </div>
          )
        )}
    </div>
  );
};
export default SubscriptionCard;