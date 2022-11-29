import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './DataCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import ConfirmModal from 'dna-container/ConfirmModal';

const DataCardItem = ({ product, history }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const tags =
    product?.tags || [product?.ART, product?.carLAFunction, product?.corportateDataCatalog].filter((x) => !!x);
  const maxTagItem = 4;

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {product?.dataProductName} ? </h3>
    </div>
  );

  const deleteDataProductAccept = () => {
    setShowDeleteModal(false);
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };

  return (
    <>
      <div
        className={Styles.dataProductCard}
        onClick={() => {
          history.push(`/dataSummary/${product?.dataProductId}`);
        }}
      >
        {product?.isCarlaCertified ? (
          <div>
            <button className={classNames('btn btn-primary', Styles.isCarlaCertified)}>CARLA Certified</button>
          </div>
        ) : null}
        <div className={Styles.cardBodySection}>
          <div className={Styles.title}>{product?.productName}</div>
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
export default withRouter(DataCardItem);
