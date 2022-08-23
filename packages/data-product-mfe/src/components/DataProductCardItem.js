import React, { useState } from 'react';
import Styles from './DataProductCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
import { dataProductsApi } from '../apis/dataproducts.api';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import { useDispatch } from 'react-redux';
import { GetDataProducts } from './redux/dataProduct.services';

import ConfirmModal from 'dna-container/ConfirmModal';

const DataProductCardItem = ({ product, history }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const dispatch = useDispatch();

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {product.dataProductName} ? </h3>
    </div>
  );

  const deleteDataProductAccept = () => {
    ProgressIndicator.show();
    dataProductsApi.deleteDataProduct(product.id).then(() => {
      dispatch(GetDataProducts());
      setShowDeleteModal(false);
    });
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };
  return (
    <>
      <div className={Styles.dataProductCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            /* navigate to summary page*/
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{product?.dataProductName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Data Classification</div>
              <div>{product?.classificationConfidentiality?.confidentiality || '-'}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(product?.createdDate)}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{`${product?.createdBy?.firstName} ${product?.createdBy?.lastName}`}</div>
            </div>
            <div>
              <div>Data ID</div>
              <div>{product?.id}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>{!product?.publish && <span className={Styles.draftIndicator}>DRAFT</span>}</div>
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => history.push(`/edit/${product?.id}`)}>
              <i className="icon mbc-icon edit"></i>
            </button>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete"></i>
            </button>
          </div>
        </div>
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteDataProductContent}
        onCancel={deleteDataProductClose}
        onAccept={deleteDataProductAccept}
      />
    </>
  );
};
export default withRouter(DataProductCardItem);
