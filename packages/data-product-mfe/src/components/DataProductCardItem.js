import React from 'react';
import Styles from './DataProductCardItem.styles.scss';
import { withRouter } from 'react-router-dom';

const DataProductCardItem = ({ product, history }) => {
  return (
    <div className={Styles.dataProductCard}>
      <div className={Styles.cardHead} onClick={() => history.push('/edit/id')}>
        <div className={Styles.cardHeadInfo}>
          <div>
            <div className={Styles.cardHeadTitle}>{product.productName}</div>
            <div className="btn btn-text forward arrow"></div>
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <ul>
              <li>Data Classification</li>
              <li>Created on</li>
              <li>Created by</li>
              <li>Data ID</li>
            </ul>
          </div>
          <div>
            <ul>
              <li>{product.confidentiality || '-'}</li>
              <li>27/01/1992</li>
              <li>John Doe</li>
              <li>XJ884</li>
            </ul>
          </div>
        </div>
      </div>
      <div className={Styles.btnGrp}>
        <button className="btn btn-primary" onClick={() => history.push('/edit/id')}>
          <i className="icon mbc-icon edit"></i>
        </button>
        <button className="btn btn-primary">
          <i className="icon delete"></i>
        </button>
      </div>
    </div>
  );
};
export default withRouter(DataProductCardItem);
