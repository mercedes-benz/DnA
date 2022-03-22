import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import Styles from './BucketList.scss';
import classNames from 'classnames';
import { history } from '../../store/storeRoot';
import ProgressIndicator from '../../../../common/modules/uilab/js/src/progress-indicator';

import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';

import { ConnectionModal } from './ConnectionModal';

export const BucketList = ({ bucketList }) => {
  const dispatch = useDispatch();
  const { isLoading, submission } = useSelector((state) => state.bucket);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});

  const deleteBucketContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.name} bucket </h3>
    </div>
  );

  useEffect(() => {
    if (isLoading) {
      ProgressIndicator.show();
    } else {
      ProgressIndicator.hide();
    }
  }, [isLoading]);

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
    dispatch({
      type: 'SUBMISSION_MODAL',
      payload: {
        bucketId: '',
        modal: false,
      },
    });
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
                            <Link to={`explorer/${item.name}`}>{item.name}</Link>
                          </div>
                          <div className={Styles.bucketTitleCol}>
                            {item?.permission
                              ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm.slice(1))
                              ?.join(' / ')}
                          </div>
                          <div className={Styles.bucketTitleCol}>{item.createdOn}</div>
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
                                <div className={Styles.bucketTitleCol}>Name</div>
                                <div className={Styles.bucketTitleCol}>Permission</div>
                                <div className={Styles.bucketTitleCol}></div>
                              </div>
                              {item.collaborators?.map((bucketItem, bucketIndex) => {
                                return (
                                  <div key={bucketIndex} className={Styles.bucketTile}>
                                    <div className={Styles.bucketTitleCol}>{bucketItem.username}</div>
                                    <div
                                      className={Styles.bucketTitleCol}
                                    >{`${bucketItem.firstName} ${bucketItem.lastName}`}</div>

                                    <div className={Styles.bucketTitleCol}>
                                      {bucketItem.permissions?.length &&
                                        'Read' +
                                          (bucketItem.permissions?.includes('can_coll_write') ? ' / Write  ' : '')}
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
                          {item?.permission?.includes('write') && (
                            <div className={Styles.projectListAction}>
                              <div className={Styles.actionBtnGrp}>
                                <button
                                  className={'btn btn-primary'}
                                  type="button"
                                  onClick={() => {
                                    setSelectedItem(item);
                                    history.push(`/editBucket/${item.id}`);
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
                                <button
                                  className={'btn btn-primary'}
                                  type="button"
                                  onClick={() => {
                                    dispatch({
                                      type: 'SUBMISSION_MODAL',
                                      payload: {
                                        modal: true,
                                        bucketId: item.id,
                                      },
                                    });
                                  }}
                                >
                                  <i className="icon code"></i>
                                  <span>Connect</span>
                                </button>
                              </div>
                            </div>
                          )}
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
      <InfoModal title={''} show={submission?.modal} content={<ConnectionModal />} onCancel={onSubmissionModalCancel} />
    </>
  );
};
