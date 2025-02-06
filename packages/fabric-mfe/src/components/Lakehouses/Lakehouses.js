import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './lakehouses.scss';
import Modal from 'dna-container/Modal';
import SelectBox from 'dna-container/SelectBox';
import ConfirmModal from 'dna-container/ConfirmModal';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Pagination from 'dna-container/Pagination';
import Tags from 'dna-container/Tags';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import { getQueryParameterByName } from '../../utilities/utils';
import { fabricApi } from '../../apis/fabric.api';
import Popper from 'popper.js';
import { Envs } from '../../utilities/envs';

const CreateShortcutModalContent = ({ workspaceId, lakehouseId, onCreateShortcut }) => {
  const [bucketName, setBucketName] = useState('');
  const [bucketNameError, setBucketNameError] = useState('');
  const [buckets, setBuckets] = useState([]);
  const [accessKey, setAccessKey] = useState('');
  const [secretKey, setSecretKey] = useState('');

  let popperObj, tooltipElem = null;

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      fabricApi
        .getAllBuckets()
        .then((res) => {
          if(res.status !== 204) {
            const sortedBuckets = res?.data?.data?.map((bucket) => { return {...bucket, name: bucket?.bucketName} })
            setBuckets(sortedBuckets);
          } else {
            setBuckets([]);
          }
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
              e?.response?.data?.errors?.length
                ? e?.response?.data?.errors[0]?.message
                : 'Fetching buckets failed!',
              'alert',
            );
          }
        });
  }, []);

  useEffect(() => {
    if(bucketName) {
      ProgressIndicator.show();
        fabricApi
          .getConnectionInfo(bucketName)
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
    }
  }, [bucketName]);

  const handleCreateShortcut = () => {
    if(bucketName.length === 0) {
      setBucketNameError(true);
    } else {
      const data = {
        bucketId: buckets?.filter(bucket => bucket?.bucketName === bucketName)[0]?.id || '',
        bucketname: bucketName,
        accessKey,
        secretKey
      }
      ProgressIndicator.show();
      fabricApi
        .createShortcut(workspaceId, lakehouseId, data)
        .then(() => {
          Notification.show('Shortcut created successfully');
          ProgressIndicator.hide();
          onCreateShortcut();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Shortcut creation failed', 'alert');
        });
    }
  }

  const bucketDetails = bucketName?.length ? buckets?.filter(bucket => bucket?.bucketName === bucketName) : [];
  
  const onCollabsIconMouseOver = (e) => {
    const targetElem = e.target;
    tooltipElem = targetElem.nextElementSibling;
    if (tooltipElem) {
      tooltipElem.classList.add('tooltip', 'show');
      tooltipElem.classList.remove('hide');
      popperObj = new Popper(targetElem, tooltipElem, {
        placement: 'top',
      });
    }
  };

  const onCollabsIconMouseOut = () => {
    if (tooltipElem) {
      tooltipElem.classList.add('hide');
      tooltipElem.classList.remove('tooltip', 'show');
    }
    popperObj?.destroy();
  };

  return (
    <div className={Styles.lakehouseModalContent}>
      <div className={classNames('input-field-group include-error')}>
        <Tags
          title={'Select Storage Bucket'}
          max={1}
          chips={bucketName.length ? [bucketName] : []}
          tags={buckets}
          setTags={(selectedTags) => {
            setBucketName(selectedTags[0] || '');
          }}
          isMandatory={true}
          showMissingEntryError={bucketNameError}
          showAllTagsOnFocus={true}
        />
      </div>
      <p className={Styles.warning}><i className={'icon mbc-icon info'}></i> S3 shortcuts are currently read-only, as Microsoft Fabric does not support write operations at this time. Write support will be enabled once it becomes available.</p>
      {bucketDetails.length === 1 && (
        <div key={'card-'} className={classNames(Styles.storageCard)}>
          <div className={Styles.cardHead}>
            <div className={classNames(Styles.cardHeadInfo)}>
              <a target='_blank' href={`/#/storage/explorer/${bucketName}`} rel="noreferrer">
                <div
                  className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}>
                  {bucketName}
                </div>
              </a>
            </div>
          </div>
          <hr />
          <div className={Styles.cardBodySection}>
            <div>
              <div>
                <div>Created on</div>
                <div>{bucketDetails[0]?.createdDate ? new Date(bucketDetails[0].createdDate).toLocaleString() : 'N/A'}</div>
              </div>
              <div>
                <div>Last modified</div>
                <div>{bucketDetails[0]?.lastModifiedDate ? new Date(bucketDetails[0].lastModifiedDate).toLocaleString() : 'N/A'}</div>
              </div>
              <div>
                <div>Classification</div>
                <div>{bucketDetails[0]?.classificationType}</div>
              </div>
              <div>
                <div>Permission</div>
                <div>{bucketDetails[0]?.permission ? `${bucketDetails[0].permission.read ? 'Read' : ''} ${bucketDetails[0].permission.write ? '/ Write' : ''}`.trim() : 'None'}</div>
              </div>
              <div>
                <div>Created By</div>
                <div>{bucketDetails[0]?.createdBy?.id}</div>
              </div>

              <div className={Styles.cardCollabSection}>
                <div>Collaborators</div>
                {bucketDetails[0]?.collaborators?.length > 0 ? (
                  <div>
                    <i className="icon mbc-icon profile" />
                    <span className={Styles.cardCollabIcon} onMouseOver={onCollabsIconMouseOver} onMouseOut={onCollabsIconMouseOut}>
                      {bucketDetails[0]?.collaborators?.length}
                    </span>
                    <div className={classNames(Styles.collabsList, 'hide')}>
                      <ul>
                        {bucketDetails[0]?.collaborators?.map((bucketItem, bucketIndex) => {
                          // Check if lastName is more than 12 characters
                          let lastName = bucketItem.lastName || '-';
                          if (lastName?.length > 12) {
                            lastName = lastName.substring(0, 12) + " ...";
                          }
                          const details = bucketItem?.accesskey + ' (' + bucketItem?.firstName + ' ' + lastName + ')';
                          return (
                            <li key={'collab' + bucketIndex}>
                              <span>
                                {bucketItem.firstName ? details : bucketItem?.accesskey}
                              </span>
                            </li>
                          );
                        })}
                      </ul>
                    </div>
                  </div>
                ) : <div>None</div>}
              </div>
            </div>
          </div>
        </div>
      )}
      <button className={classNames('btn btn-tertiary', Styles.submitBtn)} onClick={handleCreateShortcut}>
        Create Shortcut
      </button>
    </div>
  )
}
const ViewShortcutsModalContent = ({ workspaceId, lakehouseId }) => {
  const [shortcuts, setShortcuts] = useState([]);

  useEffect(() => {
    getShortcuts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getShortcuts = () => {
    ProgressIndicator.show();
    fabricApi
      .getAllShortcuts(workspaceId, lakehouseId)
      .then((res) => {
        if (res.status === 204) {
          setShortcuts([]);
        } else {
          setShortcuts(res?.data?.records);
        }
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
  }

  const handleDeleteShortcut = (id) => {
    ProgressIndicator.show();
    fabricApi
      .deleteShortcut(workspaceId, lakehouseId, id)
      .then(() => {
        Notification.show('Shortcut deleted successfully');
        ProgressIndicator.hide();
        getShortcuts();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        if(e?.response?.status === 403) {
          Notification.show('Permision denied. Only Admins can delete shortcuts.', 'alert');
        } else {
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Shortcut deletion failed', 'alert');
        }
      });
  }

  return (
    <div className={Styles.shortcutsContainer}>
      <h4>Shortcuts</h4>
      {shortcuts?.length === 0 && <p>No shortcuts are present. Create a shortcut.</p>}
      {shortcuts?.length > 0 && <>
        <div className={classNames(Styles.shortcut, Styles.thead)}>
          <div className={classNames(Styles.col1)}>Bucket Name</div>
          <div className={classNames(Styles.col2)}>Shortcut Name & Path</div>
          <div className={classNames(Styles.col3)}>Actions</div>
        </div>
        {shortcuts?.map((shortcut) => 
          <div key={shortcut?.name} className={Styles.shortcut}>
            <div className={classNames(Styles.col1)}>{shortcut?.bucketname}<br />{shortcut?.connectionName && 'Connection Name: ' + shortcut?.connectionName}</div>
            <div className={classNames(Styles.col2)}>{shortcut?.name} - {shortcut?.path}</div>
            <div className={classNames(Styles.col3)}>
              { shortcut?.bucketpath && <button className={'btn'}><i className="icon mbc-icon new-tab" onClick={() => window.open(shortcut?.bucketpath)} /> Go to Bucket</button> }
              <button className={'btn'} onClick={() => handleDeleteShortcut(shortcut?.name)}><i className="icon delete" /> Delete</button>
            </div>
          </div>
        )}
      </>}
    </div>
  );
}

function Lakehouses({ user, workspace, lakehouses, onDeleteLakehouse }) {
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
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(0);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  useEffect(() => {
    setTotalNumberOfPages(Math.ceil(lakehouses.length / maxItemsPerPage));
  }, [lakehouses, maxItemsPerPage]);

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const paginatedLakehousesTemp = lakehouses?.slice(
    currentPageOffset,
    currentPageOffset + maxItemsPerPage
  );

  const [paginatedLakehouses, setPaginatedLakehouses] = useState(paginatedLakehousesTemp);

  const onPaginationPreviousClick = () => {
    if (currentPageNumber > 1) {
      const newPageNumber = currentPageNumber - 1;
      const newOffset = (newPageNumber - 1) * maxItemsPerPage;
      setCurrentPageNumber(newPageNumber);
      setCurrentPageOffset(newOffset);
    }
  };
  const onPaginationNextClick = () => {
    if (currentPageNumber < totalNumberOfPages) {
      const newPageNumber = currentPageNumber + 1;
      const newOffset = newPageNumber * maxItemsPerPage - maxItemsPerPage;
      setCurrentPageNumber(newPageNumber);
      setCurrentPageOffset(newOffset);
    }
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    const paginatedData = lakehouses?.slice(
      currentPageOffset,
      currentPageOffset + maxItemsPerPage
    );
    setPaginatedLakehouses(paginatedData);
  }, [currentPageNumber, maxItemsPerPage, lakehouses, currentPageOffset]);

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
          setShowDeleteModal(false);
          onDeleteLakehouse();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(e.response.data.errors?.length ? e.response.data.errors[0].message : 'Lakehouse deletion failed', 'alert');
        });
  }
  const isAdmin = workspace?.status?.entitlements?.filter(entitlement =>
    entitlement?.displayName?.split('_')[0]==='FC' && entitlement?.displayName?.split('_')[2]==='Admin'
  ).length===1;
  const isOwner = user?.id === workspace?.createdBy?.id; 
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
        {paginatedLakehouses?.length === 0 &&
          <div className={Styles.noLakehouse}>
            <p>No lakehouse present in this workspace. Please create one.</p>
          </div>
        }
        <div className={Styles.lakehouseContent}>
          {paginatedLakehouses?.length > 0 && paginatedLakehouses?.map((lakehouse) =>
            <div key={lakehouse?.id} className={Styles.lakehouse}>
              <h4>
                <span>
                  {lakehouse?.name} 
                  {/* {lakehouse?.sensitivityLabel !== workspace?.dataClassification && <i className="icon mbc-icon info" tooltip-data={'Sensitivity label for lakehouse is not set or\nset higher than Workspace Data Classification. Please update.'} />} */}
                </span>
                {user?.id === workspace?.createdBy?.id && 
                  <button className={classNames('btn', Styles.deleteBtn)} onClick={() => { setSelectedLakehouse(lakehouse); setShowDeleteModal(true) }}>
                    <i className="icon delete" />
                  </button>
                }
              </h4>
              <button className={classNames('btn btn-primary', Styles.outlineBtn, !(isAdmin || isOwner) && Styles.disabledBtn)} onClick={() => { setSelectedLakehouse(lakehouse); setShowCreateShortcutModal(true) }}>
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
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'600px'}
          modalStyle={{maxWidth: '600px', paddingTop: '40px'}}
          buttonAlignment="right"
          show={showCreateShortcutModal}
          content={<CreateShortcutModalContent workspaceId={workspace?.id} lakehouseId={selectedLakehouse?.id} onCreateShortcut={() => setShowCreateShortcutModal(false)} />}
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