import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DataTransferCardItem.styles.scss';
import { withRouter } from 'react-router-dom';
import { useDispatch } from 'react-redux';

import { regionalDateFormat, setTooltipIfEllipsisShown } from '../../../../Utility/utils';
import { dataTransferApi } from '../../../../apis/datatransfers.api';
import { GetDataTransfers } from '../../redux/dataTransfer.services';

import ConfirmModal from 'dna-container/ConfirmModal';

import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../../common/modules/uilab/js/src/notification';

const DataProductCardItem = ({ product, history, user, isDataProduct = false }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const dispatch = useDispatch();

  const isProviderFormSubmitted = product?.providerInformation?.providerFormSubmitted;
  const isCreator = product?.providerInformation?.createdBy?.id === user?.id;

  const name = product?.providerInformation?.contactInformation?.name;
  const productOwnerName = `${name?.firstName} ${name?.lastName}`;

  const usersAllowedToModify =
    product?.providerInformation?.contactInformation?.informationOwner?.shortId === user?.id ||
    name?.shortId === user?.id;

  const consumerFormCreatedBy = product?.consumerInformation?.createdBy;
  const consumerName = `${consumerFormCreatedBy?.firstName} ${consumerFormCreatedBy?.lastName}`;

  useEffect(() => {
    const dataProductList = document.querySelectorAll('[class*="cardHeadTitle"]');
    setTooltipIfEllipsisShown(dataProductList);
  }, []);

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const deleteDataTransferContent = (
    <div>
      <h3>Are you sure you want to delete {product?.dataTransferName} ? </h3>
    </div>
  );

  const deleteDataTransferAccept = () => {
    ProgressIndicator.show();
    dataTransferApi
      .deleteDataTransfer(product?.id)
      .then(() => {
        dispatch(GetDataTransfers());
        setShowDeleteModal(false);
        Notification.show(`${product?.dataTransferName} deleted successfully.`);
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while deleting the data transfer', 'alert');
      });
  };
  const deleteDataTransferClose = () => {
    setShowDeleteModal(false);
  };

  const onShare = (id) => {
    navigator.clipboard.writeText(`${window.location.href}/consume/${id}`).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  return (
    <>
      <div className={Styles.dataProductCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/datasharing/summary/${product?.dataTransferId}`);
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{product?.dataTransferName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Data Transfer ID</div>
              <div>{product?.dataTransferId}</div>
            </div>
            <div>
              <div>Data Classification</div>
              <div>{product?.providerInformation?.classificationConfidentiality?.confidentiality || '-'}</div>
            </div>
            <div>
              <div>Created by</div>
              <div>{productOwnerName}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateFormat(product?.providerInformation?.createdDate)}</div>
            </div>
            {isDataProduct ? (
              <div>
                <div>Consumed by</div>
                <div>{consumerName}</div>
              </div>
            ) : null}
          </div>
          <hr />
          <div className={Styles.stagesInfo}>
            <div>
              <label>Provider</label>
              {!isProviderFormSubmitted ? (
                <span className={Styles.draft}>Draft</span>
              ) : (
                <span>{product.providerInformation?.contactInformation?.appId || '-'}</span>
              )}
            </div>
            <div>
              {isProviderFormSubmitted && !product?.publish ? (
                <div className={Styles.customIcon}>
                  <div></div> {/** renders custom arrow icon */}
                </div>
              ) : (
                <div className={Styles.icon}>
                  <i
                    className={classNames(
                      'icon mbc-icon data-sharing',
                      !isProviderFormSubmitted ? Styles.disabled : '',
                    )}
                  />
                </div>
              )}
            </div>
            <div className={!product?.publish ? Styles.disabled : ''}>
              <label>Consumer</label>
              {product?.publish ? (
                <span>{product?.consumerInformation?.contactInformation?.appId || '-'}</span>
              ) : (
                <span>pending...</span>
              )}
            </div>
          </div>
        </div>
        {!isDataProduct ? (
          <div className={Styles.cardFooter}>
            <div className={Styles.btnGrp}>
              {isCreator || usersAllowedToModify ? (
                <>
                  <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
                    <i className="icon mbc-icon delete-new" tooltip-data="Delete"></i>
                  </button>
                  <button
                    className="btn btn-primary"
                    onClick={() => history.push(`/datasharing/edit/${product?.dataTransferId}`)}
                  >
                    <i className="icon mbc-icon edit fill" tooltip-data="Edit"></i>
                  </button>
                </>
              ) : null}
              <button
                className="btn btn-primary"
                onClick={() =>
                  history.push({ pathname: '/datasharing/create', state: { copyId: product?.dataTransferId } })
                }
              >
                <i className="icon mbc-icon copy-new" tooltip-data="Create Copy"></i>
              </button>
              <button
                className={classNames('btn btn-primary', !isProviderFormSubmitted ? Styles.disabled : '')}
                disabled={!isProviderFormSubmitted}
                onClick={() => onShare(product?.dataTransferId)}
              >
                <i className={'icon mbc-icon share'} tooltip-data="Share"></i>
              </button>
            </div>
          </div>
        ) : null}
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteDataTransferContent}
        onCancel={deleteDataTransferClose}
        onAccept={deleteDataTransferAccept}
      />
    </>
  );
};
export default withRouter(DataProductCardItem);
