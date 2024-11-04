import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './lakehouses.scss';
import Modal from 'dna-container/Modal';
import SelectBox from 'dna-container/SelectBox';
import ConfirmModal from 'dna-container/ConfirmModal';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Pagination from 'dna-container/Pagination';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import { getQueryParameterByName } from '../../utilities/utils';
import { fabricApi } from '../../apis/fabric.api';

const CreateShortcutModalContent = ({ workspaceId, lakehouseId }) => {
  const [bucketName, setBucketName] = useState('');
  const [bucketNameError, setBucketNameError] = useState('');
  const [buckets, setBuckets] = useState([]);
  const [accessKey, setAccessKey] = useState('');
  const [secretKey, setSecretKey] = useState('');

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      fabricApi
        .getAllBuckets()
        .then((res) => {
          setBuckets(res?.data?.data);
          SelectBox.defaultSetup(true);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          if(e?.response?.status === 403) {
            Notification.show('Unauthorized to view this page or not found', 'alert');
            history.push(`/`);
          } else {
            Notification.show(
              e.response.data.errors?.length
                ? e.response.data.errors[0].message
                : 'Fetching buckets failed!',
              'alert',
            );
          }
        });
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      fabricApi
        .getConnectionInfo(bucketName?.split('@-@')[1])
        .then((res) => {
          setAccessKey(res?.data?.data?.userVO?.accesskey);
          setSecretKey(res?.data?.data?.userVO?.secretKey);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          if(e?.response?.status === 403) {
            Notification.show('Unauthorized to view this page or not found', 'alert');
            history.push(`/`);
          } else {
            Notification.show(
              e.response.data.errors?.length
                ? e.response.data.errors[0].message
                : 'Fetching bucket connection details failed!',
              'alert',
            );
          }
        });
  }, [bucketName]);

  const handleCreateShortcut = () => {
    if(bucketName.length === 0) {
      setBucketNameError(true);
    } else {
      const data = {
        bucketId: bucketName?.includes('@-@') ? bucketName?.split('@-@')[0] : '',
        bucketname: bucketName?.includes('@-@') ? bucketName?.split('@-@')[1] : '',
        accessKey,
        secretKey
      }
      ProgressIndicator.show();
      fabricApi
        .createShortcut(workspaceId, lakehouseId, data)
        .then(() => {
          Notification.show('Shortcut created successfully');
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Shortcut creation failed', 'alert');
        });
    }
  }

  return (
    <div className={Styles.lakehouseModalContent}>
      <div className={classNames('input-field-group include-error', bucketNameError ? 'error' : '')}>
        <label className={'input-label'}>
          Select Storage Bucket <sup>*</sup>
        </label>
        <div className={classNames('custom-select')}>
          <select
            id="storageBucketField"
            value={bucketName}
            onChange={(e) => setBucketName(e.target.value)}
          >
            <option value={0}>Choose</option>
            {buckets?.map((bucket) => {
              return (
                <option id={bucket?.bucketName} key={bucket?.name} value={bucket?.id + '@-@' + bucket?.bucketName}>
                  {bucket?.bucketName}
                </option>
              )
            })}
          </select>
        </div>
      </div>
      <button className={classNames('btn btn-tertiary', Styles.submitBtn)} onClick={handleCreateShortcut}>
        Create Shortcut
      </button>
    </div>
  )
}

const ViewShortcutsModalContent = ({ workspaceId, lakehouseId }) => {
  const [shortcuts, setShortcuts] = useState([]);

  useEffect(() => {
    ProgressIndicator.show();
      fabricApi
        .getAllShortcuts(workspaceId, lakehouseId)
        .then((res) => {
          setShortcuts(res?.data?.records);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          if(e?.response?.status === 403) {
            Notification.show('Unauthorized to view this page or not found', 'alert');
            history.push(`/`);
          } else {
            Notification.show(
              e.response.data.errors?.length
                ? e.response.data.errors[0].message
                : 'Fetching shortcuts failed!',
              'alert',
            );
          }
        });
  }, [workspaceId, lakehouseId]);

  const handleDeleteShortcut = (id) => {
    ProgressIndicator.show();
    fabricApi
      .deleteShortcut(workspaceId, lakehouseId, id)
      .then(() => {
        Notification.show('Shortcut deleted successfully');
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Shortcut deletion failed', 'alert');
      });
  }

  return (
    <div className={Styles.shortcutsContainer}>
      <h4>Shortcuts</h4>
      {shortcuts.length === 0 && <p>No shortcuts are present. Create a shortcut.</p>}
      {shortcuts.length > 0 && <>
        <div className={classNames(Styles.shortcut, Styles.thead)}>
          <div className={classNames(Styles.col1)}>Bucket Name</div>
          <div className={classNames(Styles.col2)}>Shortcut Name & Path</div>
          <div className={classNames(Styles.col3)}>Actions</div>
        </div>
        {shortcuts?.map((shortcut) => 
          <div key={shortcut?.id} className={Styles.shortcut}>
            <div className={classNames(Styles.col1)}>{shortcut?.bucketname}<br />Connection Name: {shortcut?.connectionName}</div>
            <div className={classNames(Styles.col2)}>{shortcut?.name} - {shortcut?.path}</div>
            <div className={classNames(Styles.col3)}>
              { shortcut?.bucketpath && <button className={'btn'}><i className="icon mbc-icon new-tab" onClick={() => window.open(shortcut?.bucketpath)} /> Go to Bucket</button> }
              <button className={'btn'}><i className="icon delete" onClick={() => handleDeleteShortcut(shortcut?.id)} /> Delete</button>
            </div>
          </div>
        )}
      </>}
    </div>
  );
}

function Lakehouses({ workspace, lakehouses }) {
  const [showCreateLakehouseModal, setShowCreateLakehouseModal] = useState(false);
  const [lakehouseName, setLakehouseName] = useState('');
  const [lakehouseNameError, setLakehouseNameError] = useState(false);
  const [selectedLakehouse, setSelectedLakehouse] = useState();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showViewShortcutsModal, setShowViewShortcutsModal] = useState(false);
  const [showCreateShortcutModal, setShowCreateShortcutModal] = useState(false);

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);
  
  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, [workspace]);

  // Pagination 
  const [totalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetTemp = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onPaginationNextClick = () => {
    const currentPageOffsetTemp = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    // get lakehouses here
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const handleCreateLakehouse = () => {
    if(lakehouseName.length === 0) {
      setLakehouseNameError(true);
    } else {
      const data = {
        name: lakehouseName,
        description: ''
      }
      ProgressIndicator.show();
      fabricApi
        .createLakehouse(workspace?.id, data)
        .then(() => {
          Notification.show('Lakehouse created successfully');
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Lakehouse creation failed', 'alert');
        });
    }
  }

  const createLakehouseModalContent = <>
    <div className={Styles.lakehouseModalContent}>
      <div className={classNames('input-field-group include-error', lakehouseNameError ? 'error' : '')}>
        <label className={'input-label'}>
          Name of Lakehouse <sup>*</sup>
        </label>
        <input
          type="text"
          className={'input-field'}
          id="lakehouseName"
          placeholder="Type here"
          autoComplete="off"
          maxLength={256}
          value={lakehouseName}
          onChange={(e) => { setLakehouseNameError(false); setLakehouseName(e.target.value) }}
        />
        <span className={'error-message'}>{lakehouseNameError && '*Missing entry'}</span>
      </div>
      <button className={classNames('btn btn-tertiary', Styles.submitBtn)} onClick={handleCreateLakehouse}>
        Create Lakehouse
      </button>
    </div>
  </>

  const deleteLakehouseModalContent = <>
    <div className={Styles.shortcutsContainer}>
      <p>Are you sure you want to delete this Lakehouse?</p>
    </div>
  </>

  const deleteLakehouseAccept = () => {
    ProgressIndicator.show();
      fabricApi
        .deleteLakehouse(workspace?.id, selectedLakehouse?.id)
        .then(() => {
          Notification.show('Lakehouse deleted successfully');
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Lakehouse deletion failed', 'alert');
        });
  }

  return (
    <>
      <div className={Styles.lakehouseContainer}>
        <div className={Styles.header}>
          <h3>Lakehouses</h3>
          {/* <div className={Styles.btnContainer}>
            <button className={classNames('btn btn-primary', Styles.outlineBtn)} onClick={() => setShowCreateLakehouseModal(true)}>
              <i className="icon mbc-icon plus" />
              <span>Create Lakehouse</span>
            </button>
          </div> */}
        </div>
        {lakehouses?.length === 0 &&
          <div className={Styles.noLakehouse}>
            <p>No lakehouse present in this workspace. Please create one.</p>
          </div>
        }
        <div className={Styles.lakehouseContent}>
          {lakehouses?.length > 0 && lakehouses?.map((lakehouse) =>
            <div key={lakehouse?.id} className={Styles.lakehouse}>
              <h4>
                <span>
                  {lakehouse?.name} 
                  {/* {lakehouse?.sensitivityLabel !== workspace?.dataClassification && <i className="icon mbc-icon info" tooltip-data={'Sensitivity label for lakehouse is not set or\nset higher than Workspace Data Classification. Please update.'} />} */}
                </span>
                <button className={classNames('btn', Styles.deleteBtn)} onClick={() => setShowDeleteModal(true)}>
                  <i className="icon delete" />
                </button>
              </h4>
              <button className={classNames('btn btn-primary', Styles.outlineBtn)} onClick={() => { setShowCreateShortcutModal(true) }}>
                <i className="icon mbc-icon plus" />
                <span>Create Shortcut</span>
              </button>&nbsp;&nbsp;&nbsp;&nbsp;
              <button className={classNames('btn btn-primary', Styles.outlineBtn)} onClick={() => { setSelectedLakehouse(lakehouse); setShowViewShortcutsModal(true) }}>
                <i className="icon mbc-icon new-tab" />
                <span>View Shortcuts</span>
              </button>
            </div>
            )
          }
        </div>
        {/* {!loading && forecastRuns?.length > 0 && */}
        <Pagination
          totalPages={totalNumberOfPages}
          pageNumber={currentPageNumber}
          onPreviousClick={onPaginationPreviousClick}
          onNextClick={onPaginationNextClick}
          onViewByNumbers={onViewByPageNum}
          displayByPage={true}
        />
      </div>
      { showCreateLakehouseModal &&
        <Modal
          title={'Create Lakehouse'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'400px'}
          buttonAlignment="right"
          show={showCreateLakehouseModal}
          content={createLakehouseModalContent}
          scrollableContent={true}
          onCancel={() => { setLakehouseNameError(false); setShowCreateLakehouseModal(false) }}
        />
      }
      { showCreateShortcutModal &&
        <Modal
          title={'Create Shortcut'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'400px'}
          buttonAlignment="right"
          show={showCreateShortcutModal}
          content={<CreateShortcutModalContent workspaceId={workspace?.id} lakehouseId={selectedLakehouse?.id} />}
          scrollableContent={false}
          onCancel={() => { setShowCreateShortcutModal(false) }}
        />
      }
      { showViewShortcutsModal &&
        <Modal
          title={'Shortcuts'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={showViewShortcutsModal}
          content={<ViewShortcutsModalContent workspaceId={workspace?.id} lakehouseId={selectedLakehouse?.id} />}
          scrollableContent={true}
          onCancel={() => { setSelectedLakehouse(); setShowViewShortcutsModal(false) }}
        />
      }
      { showDeleteModal &&
        <ConfirmModal
          title={''}
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={showDeleteModal}
          content={deleteLakehouseModalContent}
          onCancel={() => setShowDeleteModal(false)}
          onAccept={deleteLakehouseAccept}
        />
      }
    </>
  )
}

export default Lakehouses;