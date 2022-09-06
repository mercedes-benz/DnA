import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DataProductCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { useDispatch } from 'react-redux';

import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
import { dataProductsApi } from '../apis/dataproducts.api';
import { GetDataProducts } from './redux/dataProduct.services';

import ConfirmModal from 'dna-container/ConfirmModal';

import Tooltip from '../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import Notification from '../common/modules/uilab/js/src/notification';

const DataProductCardItem = ({ product, history }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const dispatch = useDispatch();

  const isProviderFormSubmitted = product.openSegments.includes('SpecifyDeletionRequirements');

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

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

  const onShare = (id) => {
    navigator.clipboard.writeText(`${window.location.href}consume/${id}`).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  return (
    <>
      <div className={Styles.dataProductCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/summary/${product.id}`);
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
              <div>Data Transfer ID</div>
              <div>{product?.id}</div>
            </div>
            <div>
              <div>Data Classification</div>
              <div>{product?.classificationConfidentiality?.confidentiality || '-'}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{`${product?.createdBy?.firstName} ${product?.createdBy?.lastName}`}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(product?.createdDate)}</div>
            </div>
          </div>
          <hr />
          <div className={Styles.stagesInfo}>
            <div>
              <label>Provider</label>
              {!isProviderFormSubmitted ? (
                <span className={Styles.draft}>Draft</span>
              ) : (
                <span>{product.contactInformation.appId}</span>
              )}
            </div>
            <div>
              {isProviderFormSubmitted && !product.publish ? (
                <div className={Styles.customIcon}>
                  <div></div> {/** renders custom arrow icon */}
                </div>
              ) : (
                <div className={Styles.icon}>
                  <i
                    className={classNames('icon mbc-icon comparison', !isProviderFormSubmitted ? Styles.disabled : '')}
                  />
                </div>
              )}
            </div>
            <div className={!product.publish ? Styles.disabled : ''}>
              <label>Consumer</label>
              {product.publish ? <span>{product.contactInformation.appId}</span> : <span>pending...</span>}
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          {/* <div>{!product?.publish && <span className={Styles.draftIndicator}>DRAFT</span>}</div> */}
          <div className={Styles.btnGrp}>
            <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
              <i className="icon delete" tooltip-data="Delete"></i>
            </button>
            <button className="btn btn-primary" onClick={() => history.push(`/edit/${product?.id}`)}>
              <i className="icon mbc-icon edit" tooltip-data="Edit"></i>
            </button>
            <button className="btn btn-primary" onClick={() => {}}>
              <i className="icon mbc-icon copy" tooltip-data="Create Copy"></i>
            </button>
            <button
              className={classNames(
                'btn btn-primary',
                Styles.shareIcon,
                !isProviderFormSubmitted ? Styles.disabled : '',
              )}
              disabled={!isProviderFormSubmitted}
              onClick={() => onShare(product.id)}
            >
              <span tooltip-data="Share"></span>
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
