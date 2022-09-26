import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { regionalDateFormat } from '../../../Utility/utils';
import Styles from './DataProductListItem.styles.scss';
import ConfirmModal from 'dna-container/ConfirmModal';
import { dataProductsApi } from '../../../apis/dataproducts.api';
import { GetDataProducts } from '../../redux/dataProduct.services';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';

const DataProductListItem = ({ product, history, user }) => {
  const dispatch = useDispatch();
  const isProviderFormSubmitted = product?.providerInformation?.providerFormSubmitted;
  const isCreator = product.providerInformation?.createdBy?.id === user?.id;
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    const elemRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable = document.querySelector('.ul-table.dataproducts').getBoundingClientRect();
    setContextMenuOffsetTop(elemRect.top - (relativeParentTable.top + 10));
    setContextMenuOffsetRight(10);
    setShowContextMenu(!showContextMenu);
  };

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {product?.dataProductName} ? </h3>
    </div>
  );
  const deleteDataProductAccept = () => {
    ProgressIndicator.show();
    dataProductsApi.deleteDataProduct(product?.id).then(() => {
      dispatch(GetDataProducts());
      setShowContextMenu(false);
      setShowDeleteModal(false);
    });
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };

  const onShare = (id) => {
    setShowContextMenu(false);
    navigator.clipboard.writeText(`${window.location.href}consume/${id}`).then(() => {
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
            onClick={() => history.push(`/summary/${product?.dataProductId}`)}
          >
            {product?.dataProductName}
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
                {isCreator ? (
                  <>
                    <li className="contextListItem">
                      <span onClick={() => history.push(`/edit/${product?.dataProductId}`)}>Edit</span>
                    </li>
                    <li className="contextListItem">
                      <span onClick={() => setShowDeleteModal(true)}>Delete</span>
                    </li>
                  </>
                ) : null}
                <li className="contextListItem">
                  <span
                    onClick={() => history.push({ pathname: '/create', state: { copyId: product?.dataProductId } })}
                  >
                    Create Copy
                  </span>
                </li>
                <li className={classNames('contextListItem', !isProviderFormSubmitted ? Styles.disabled : '')}>
                  <span
                    disabled={!isProviderFormSubmitted}
                    onClick={() => {
                      isProviderFormSubmitted && onShare(product?.dataProductId);
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
                  <span>{product?.dataProductId}</span>
                </div>
                <div>
                  <label className={Styles.label}>Data Classification</label>
                  <span>{product?.providerInformation?.classificationConfidentiality?.confidentiality || '-'}</span>
                </div>
              </div>
              <div>
                <div>
                  <label className={Styles.label}>Created by</label>
                  <span>{product?.providerInformation?.contactInformation?.name}</span>
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
                  <span>{product.providerInformation?.contactInformation?.appId}</span>
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
                  <span>{product.consumerInformation?.contactInformation?.appId}</span>
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
        content={deleteDataProductContent}
        onCancel={deleteDataProductClose}
        onAccept={deleteDataProductAccept}
      />
    </React.Fragment>
  );
};
export default withRouter(DataProductListItem);
