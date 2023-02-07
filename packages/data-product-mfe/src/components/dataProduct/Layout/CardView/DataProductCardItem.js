import classNames from 'classnames';
import React from 'react';
import Styles from './DataProductCardItem.styles.scss';
import { withRouter } from 'react-router-dom';

const DataCardItem = ({ product, history }) => {
  const tags = [
    product?.agileReleaseTrain?.name,
    product?.carLaFunction?.name,
    product?.corporateDataCatalog?.name,
  ].filter((x) => !!x);
  const maxTagItem = 4;

  return (
    <>
      <div
        className={Styles.dataProductCard}
        onClick={() => {
          history.push(`/dataproduct/summary/${product?.dataProductId}`);
        }}
      >
        {!product?.isPublish ? (
          <div>
            <button className={classNames('btn btn-primary', Styles.upperTag)}>Coming Soon</button>
          </div>
        ) : product?.isCarlaCertified ? (
          <div>
            <button className={classNames('btn btn-primary', Styles.upperTag)}>CARLA Certified</button>
          </div>
        ) : null}
        <div className={Styles.cardBodySection}>
          <div className={Styles.title}>{product?.dataProductName}</div>
          <div className={Styles.description}>{product?.description}</div>
        </div>
        <div className={Styles.cardFooter}>
          <div className={Styles.tags}>
            {tags?.slice(0, maxTagItem)?.map((item) => {
              return (
                <span className={Styles.tagItem} key={item}>
                  {item}
                </span>
              );
            })}
            {tags?.length > maxTagItem ? <span className={Styles.tagItem}>...</span> : null}
          </div>
        </div>
      </div>
    </>
  );
};
export default withRouter(DataCardItem);
