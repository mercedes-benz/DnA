import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { regionalDateFormat, setTooltipIfEllipsisShown } from '../../../../Utility/utils';
import Styles from './DataTransferListItem.styles.scss';

import ConfirmModal from 'dna-container/ConfirmModal';

import { dataTransferApi } from '../../../../apis/datatransfers.api';
import { GetDataTransfers } from '../../redux/dataTransfer.services';

import ProgressIndicator from '../../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';

const DataTransferListItem = ({ product, history, user, isProviderCreatorFilter }) => {
  const dispatch = useDispatch();
  const isProviderFormSubmitted = product?.providerInformation?.providerFormSubmitted;
  const isCreator = product.providerInformation?.createdBy?.id === user?.id;
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);

  const name = product?.providerInformation?.contactInformation?.name;
  const productOwnerName = `${name?.firstName} ${name?.lastName}`;

  const usersAllowedToModify =
    product.providerInformation?.contactInformation?.informationOwner?.shortId === user?.id ||
    name?.shortId === user?.id;

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    const elemRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable = document.querySelector('.ul-table.dataproducts').getBoundingClientRect();
    setContextMenuOffsetTop(elemRect.top - (relativeParentTable.top + 10));
    setContextMenuOffsetRight(10);
    setShowContextMenu(!showContextMenu);
  };

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
        dispatch(GetDataTransfers(isProviderCreatorFilter));
        setShowContextMenu(false);
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
    setShowContextMenu(false);
    navigator.clipboard.writeText(`${window.location.href}/consume/${id}`).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const handleContextMenuOutside = (e) => {
    const withinContextMenuWrapper =
      e.target.parentElement.classList.contains('contextMenuWrapper') ||
      e.target.parentElement.classList.contains('contextList');
    !withinContextMenuWrapper && setShowContextMenu(false);
  };

  useEffect(() => {
    const dataTransferList = document.querySelectorAll('[class*="arrowBtn"]');
    setTooltipIfEllipsisShown(dataTransferList);
  }, []);

  useEffect(() => {
    document.addEventListener('click', handleContextMenuOutside, true);
    return () => document.removeEventListener('click', handleContextMenuOutside, true);
  }, []);

  return (
    <React.Fragment>
      <div
        id={product?.id}
        key={product?.id}
        className={classNames('data-row', Styles.listContainer, showContextMenu ? Styles.contextOpened : null)}
      >
        <div className={Styles.titleSection}>
          <button
            className={classNames('btn btn-text arrow', Styles.arrowBtn)}
            type="submit"
            onClick={() => history.push(`/summary/${product?.dataTransferId}`)}
          >
            {product?.dataTransferName}
          </button>
          <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
            <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div
              style={{
                top: contextMenuOffsetTop + 'px',
                right: contextMenuOffsetRight + 'px',
              }}
              className={classNames('contextMenuWrapper', showContextMenu ? '' : 'hide')}
            >
              <ul className="contextList">
                {isCreator || usersAllowedToModify ? (
                  <>
                    <li className="contextListItem">
                      <span onClick={() => history.push(`/datasharing/edit/${product?.dataTransferId}`)}>Edit</span>
                    </li>
                    <li className="contextListItem">
                      <span onClick={() => setShowDeleteModal(true)}>Delete</span>
                    </li>
                  </>
                ) : null}
                <li className="contextListItem">
                  <span
                    onClick={() =>
                      history.push({ pathname: '/datasharing/create', state: { copyId: product?.dataTransferId } })
                    }
                  >
                    Create Copy
                  </span>
                </li>
                <li className={classNames('contextListItem', !isProviderFormSubmitted ? Styles.disabled : '')}>
                  <span
                    disabled={!isProviderFormSubmitted}
                    onClick={() => {
                      isProviderFormSubmitted && onShare(product?.dataTransferId);
                    }}
                  >
                    Share
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <div className={Styles.dataProductContent}>
          <div>
            <hr />
            <div>
              <div>
                <div>
                  <label className={Styles.label}>Data Transfer ID</label>
                  <span>{product?.dataTransferId}</span>
                </div>
                <div>
                  <label className={Styles.label}>Data Classification</label>
                  <span>{product?.providerInformation?.classificationConfidentiality?.confidentiality || '-'}</span>
                </div>
              </div>
              <div>
                <div>
                  <label className={Styles.label}>Created by</label>
                  <span>{productOwnerName}</span>
                </div>

                <div>
                  <label className={Styles.label}>Created on</label>
                  <span>{regionalDateFormat(product?.providerInformation?.createdDate)}</span>
                </div>
              </div>
            </div>
          </div>
          <div className={classNames(Styles.stagesInfo)}>
            <hr />
            <div>
              <div>
                <label>Provider</label>
                {!isProviderFormSubmitted ? (
                  <span className={Styles.draft}>Draft</span>
                ) : (
                  <span>{product.providerInformation?.contactInformation?.appId || '-'}</span>
                )}
              </div>
              {isProviderFormSubmitted && !product?.publish ? (
                <div className={Styles.customIcon}>
                  <div></div> {/** renders custom arrow icon */}
                </div>
              ) : (
                <div style={{ margin: '30px 0' }}>
                  <div className={Styles.icon}>
                    <i
                      className={classNames(
                        'icon mbc-icon data-sharing',
                        !isProviderFormSubmitted ? Styles.disabled : '',
                      )}
                    />
                  </div>
                </div>
              )}
              <div className={!product.publish ? Styles.disabled : ''}>
                <label>Consumer</label>
                {product.publish ? (
                  <span>{product.consumerInformation?.contactInformation?.appId || '-'}</span>
                ) : (
                  <span>pending...</span>
                )}
              </div>
            </div>
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
        content={deleteDataTransferContent}
        onCancel={deleteDataTransferClose}
        onAccept={deleteDataTransferAccept}
      />
    </React.Fragment>
  );
};
export default withRouter(DataTransferListItem);
