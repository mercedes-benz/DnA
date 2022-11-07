import React, { useEffect, useState } from 'react';
import Styles from './DataCardItem.styles.scss';
import { withRouter } from 'react-router-dom';

import { regionalDateFormat, setTooltipIfEllipsisShown } from '../../../../Utility/utils';

import ConfirmModal from 'dna-container/ConfirmModal';

import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';

const DataCardItem = ({ product, history }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const name = product?.createdBy;
  const productOwnerName = `${name?.firstName} ${name?.lastName}`;

  useEffect(() => {
    const dataProductList = document.querySelectorAll('[class*="cardHeadTitle"]');
    setTooltipIfEllipsisShown(dataProductList);
  }, []);

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

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
      <div className={Styles.dataProductCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/dataSummary/${product?.dataProductId}`);
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
              <div>Data Product ID</div>
              <div>{product?.dataProductId}</div>
            </div>
            <div>
              <div>Data Classification</div>
              <div>{product?.confidentiality || '-'}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{productOwnerName}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateFormat(product?.createdDate)}</div>
            </div>
          </div>
          <hr />
        </div>
        <div className={Styles.cardFooter}>
          <div className={Styles.btnGrp}>
            <>
              <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
                <i className="icon mbc-icon delete-new" tooltip-data="Delete"></i>
              </button>
              <button className="btn btn-primary" onClick={() => history.push(`/editData/${product?.dataProductId}`)}>
                <i className="icon mbc-icon edit fill" tooltip-data="Edit"></i>
              </button>
            </>
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
