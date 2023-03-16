import classNames from 'classnames';
import React from 'react';
import { withRouter } from 'react-router-dom';

import Styles from './DataProductListItem.styles.scss';

const DataListItem = ({ product, history }) => {
  const tags = [
    product?.agileReleaseTrain?.name,
    product?.carLaFunction?.name,
    product?.corporateDataCatalog?.name,
  ].filter((x) => !!x);

  return (
    <React.Fragment>
      <div
        id={product?.id}
        key={product?.id}
        className={classNames('data-row', Styles.listContainer)}
        onClick={() => history.push(`/dataproduct/summary/${product?.dataProductId}`)}
      >
        <div className={Styles.titleSection}>
          {!product?.isPublish ? (
            <div>
              <button className={classNames('btn btn-primary', Styles.upperTag)}>Coming Soon</button>
            </div>
          ) : null}
        </div>
        <div className={Styles.dataProductContent}>
          <div className={Styles.iconContainer}></div>
          <div className={Styles.flexItem}>
            <div className={Styles.heading}>{product?.dataProductName}</div>
            <div className={Styles.description}>{product?.description}</div>
          </div>
          <div className={classNames(Styles.tags, Styles.flexItem)}>
            {tags?.map((item) => {
              return (
                <span className={Styles.tagItem} key={item}>
                  {item}
                </span>
              );
            })}
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};
export default withRouter(DataListItem);
