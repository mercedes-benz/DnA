import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import { regionalDateFormat, setTooltipIfEllipsisShown } from '../../../../Utility/utils';
import Styles from './DataListItem.styles.scss';
import ConfirmModal from 'dna-container/ConfirmModal';

import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';

const DataListItem = ({ product, history }) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);

  const name = product?.createdBy;
  const productOwnerName = `${name?.firstName} ${name?.lastName}`;

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

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {product?.dataProductName} ? </h3>
    </div>
  );
  const deleteDataProductAccept = () => {
    setShowContextMenu(false);
    setShowDeleteModal(false);
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };

  const handleContextMenuOutside = (e) => {
    const withinContextMenuWrapper =
      e.target.parentElement.classList.contains('contextMenuWrapper') ||
      e.target.parentElement.classList.contains('contextList');
    !withinContextMenuWrapper && setShowContextMenu(false);
  };

  useEffect(() => {
    const dataProductList = document.querySelectorAll('[class*="arrowBtn"]');
    setTooltipIfEllipsisShown(dataProductList);
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
            onClick={() => history.push(`/dataSummary/${product?.dataProductId}`)}
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
                <>
                  <li className="contextListItem">
                    <span onClick={() => history.push(`/editData/${product?.dataProductId}`)}>Edit</span>
                  </li>
                  <li className="contextListItem">
                    <span onClick={() => setShowDeleteModal(true)}>Delete</span>
                  </li>
                </>
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
                  <label className={Styles.label}>Data Product ID</label>
                  <span>{product?.dataProductId}</span>
                </div>
                <div>
                  <label className={Styles.label}>Data Classification</label>
                  <span>{product?.confidentiality || '-'}</span>
                </div>
              </div>
              <div>
                <div>
                  <label className={Styles.label}>Created by</label>
                  <span>{productOwnerName}</span>
                </div>

                <div>
                  <label className={Styles.label}>Created on</label>
                  <span>{regionalDateFormat(product?.createdDate)}</span>
                </div>
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
export default withRouter(DataListItem);
