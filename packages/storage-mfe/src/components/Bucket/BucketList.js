import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Styles from './BucketList.scss';
import classNames from 'classnames';
import { history } from '../../store/storeRoot';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';

import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';

import { ConnectionModal } from './ConnectionModal';
import moment from 'moment';
import { setFiles } from '../Explorer/redux/fileExplorer.actions';
import { getConnectionInfo, hideConnectionInfo } from './ConnectionInfo/redux/connection.actions';

export const BucketList = ({ bucketList }) => {
  const dispatch = useDispatch();
  const { connect } = useSelector((state) => state.connectionInfo);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  const deleteBucketContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.bucketName} bucket? </h3>
      <h5>A bucket can only be deleted if its empty.</h5>
    </div>
  );

  const deleteBucketClose = () => {
    setDeleteModal(false);
  };

  const deleteBucketAccept = () => {
    dispatch({
      type: 'DELETE_BUCKET',
      payload: selectedItem,
    });
    setDeleteModal(false);
  };

  const onSubmissionModalCancel = () => {
    dispatch(hideConnectionInfo());
  };

  return (
    <>
      <div className={classNames('expanstion-table', Styles.bucketList)}>
        <div className={Styles.bucketGrp}>
          <div className={Styles.bucketGrpList}>
            <div className={Styles.bucketGrpListItem}>
              <div className={Styles.bucketCaption}>
                <div className={Styles.bucketTile}>
                  <div className={Styles.bucketTitleCol}>
                    <label>
                      <i className="icon sort" />
                      Bucket Name
                    </label>
                  </div>
                  <div className={Styles.bucketTitleCol}>
                    <label>
                      <i className="icon sort" />
                      Permission
                    </label>
                  </div>
                  <div className={Styles.bucketTitleCol}>
                    <label>
                      <i className="icon sort" />
                      Created On
                    </label>
                  </div>
                  <div className={Styles.bucketTitleCol}>
                    <label>
                      <i className="icon sort" />
                      Last Modified On
                    </label>
                  </div>
                  <div className={Styles.bucketTitleCol}>Action</div>
                </div>
              </div>
              {bucketList?.map((item, index) => {
                return (
                  <div
                    key={index}
                    className={'expansion-panel-group airflowexpansionPanel ' + Styles.bucketGrpListItemPanel}
                  >
                    <div className={classNames('expansion-panel ', index === 0 ? 'open' : '')}>
                      <span className="animation-wrapper"></span>
                      <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                      <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                        <div className={Styles.bucketTile}>
                          <div className={Styles.bucketTitleCol}>
                            <a
                              href={'#/storage/'}
                              onClick={() => {
                                dispatch(setFiles(item.bucketName));
                              }}
                            >
                              {item.bucketName}
                            </a>
                          </div>
                          <div className={Styles.bucketTitleCol}>
                            {Object.entries(item?.permission)
                              .map(([k, v]) => {
                                if (v === true) {
                                  return k;
                                }
                              })
                              ?.filter((x) => !!x)
                              ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1))
                              ?.join(' / ')}
                          </div>
                          <div className={Styles.bucketTitleCol}>{moment(item.creationDate).format('YYYY-MM-DD')}</div>
                          <div className={Styles.bucketTitleCol}>{item.modifiedOn}</div>
                          <div className={Styles.bucketTitleCol}></div>
                        </div>

                        <i tooltip-data="Expand" className="icon down-up-flip"></i>
                      </label>
                      <div className="expansion-panel-content">
                        <div className={Styles.bucketColContent}>
                          {item.collaborators?.length ? (
                            <div className={Styles.projectList}>
                              <div className={Styles.bucketTile + ' ' + Styles.bucketTileCaption}>
                                <div className={Styles.bucketTitleCol}>User Id</div>
                                {/* <div className={Styles.bucketTitleCol}>Name</div> */}
                                <div className={Styles.bucketTitleCol}>Permission</div>
                                <div className={Styles.bucketTitleCol}></div>
                              </div>
                              {item.collaborators?.map((bucketItem, bucketIndex) => {
                                return (
                                  <div key={bucketIndex} className={Styles.bucketTile}>
                                    <div className={Styles.bucketTitleCol}>{bucketItem.accesskey}</div>
                                    {/* <div
                                      className={Styles.bucketTitleCol}
                                    >{`${bucketItem.firstName} ${bucketItem.lastName}`}</div> */}

                                    <div className={Styles.bucketTitleCol}>
                                      {Object.entries(bucketItem?.permission)
                                        .map(([k, v]) => {
                                          if (v === true) {
                                            return k;
                                          }
                                        })
                                        ?.filter((x) => !!x)
                                        ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1))
                                        ?.join(' / ')}
                                    </div>

                                    <div className={Styles.bucketTitleCol}></div>
                                  </div>
                                );
                              })}
                            </div>
                          ) : (
                            <div className={Styles.projectList}>
                              <div className={Styles.noCollaborators}>Collaborators Not Exist!</div>
                            </div>
                          )}

                          <div className={Styles.projectListAction}>
                            <div className={Styles.actionBtnGrp}>
                              {item?.permission?.write && (
                                <>
                                  <button
                                    className={'btn btn-primary'}
                                    type="button"
                                    onClick={() => {
                                      setSelectedItem(item);
                                      history.push(`/editBucket/${item.bucketName}`);
                                    }}
                                  >
                                    <i className="icon mbc-icon edit"></i>
                                    <span>Edit</span>
                                  </button>
                                  <button
                                    className={'btn btn-primary'}
                                    type="button"
                                    onClick={() => {
                                      setSelectedItem(item);
                                      setDeleteModal(true);
                                    }}
                                  >
                                    <i className="icon delete"></i>
                                    <span>Delete</span>
                                  </button>
                                </>
                              )}
                              <button
                                className={'btn btn-primary'}
                                type="button"
                                onClick={() => {
                                  dispatch(getConnectionInfo(item.bucketName));
                                }}
                              >
                                <i className="icon mbc-icon comparison"></i>
                                <span>Connect</span>
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
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
        content={deleteBucketContent}
        onCancel={deleteBucketClose}
        onAccept={deleteBucketAccept}
      />
      <InfoModal
        modalCSS={Styles.header}
        show={connect?.modal}
        content={<ConnectionModal />}
        hiddenTitle={true}
        onCancel={onSubmissionModalCancel}
      />
    </>
  );
};
