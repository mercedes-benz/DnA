import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { BucketList } from './BucketList';
import Styles from './Buckets.scss';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import Caption from 'dna-container/Caption';

import { bucketActions } from './redux/bucket.actions';

/**
 * List all buckets corresponding to the user
 * @visibleName All Buckets
 */
const AllBuckets = (props) => {
  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const dispatch = useDispatch();
  const {
    bucketList,
    pagination: { bucketListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.bucket);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = bucketListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        currentPageNumber: currentPageNumberTemp,
      },
    });
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = bucketListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        currentPageNumber: currentPageNumberTemp,
      },
    });
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(bucketListResponse?.length / pageNum);
    const modifiedData = bucketListResponse.slice(0, pageNum);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      },
    });
  };

  useEffect(() => {
    dispatch(bucketActions.getBucketList());
  }, [dispatch, maxItemsPerPage]);

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title="My Storage">
            <div className={classNames(Styles.listHeader)}>
              <div tooltip-data="Card View">
                <span
                  className={cardViewMode ? Styles.iconactive : Styles.iconInActive}
                  onClick={() => {
                    setCardViewMode(true);
                    setListViewMode(false);
                    sessionStorage.removeItem('storageListViewModeEnable');
                  }}
                >
                  <i className="icon mbc-icon widgets" />
                </span>
              </div>
              <span className={Styles.dividerLine}> &nbsp; </span>
              <div tooltip-data="List View">
                <span
                  className={listViewMode ? Styles.iconactive : Styles.iconInActive}
                  onClick={() => {
                    setCardViewMode(false);
                    setListViewMode(true);
                    sessionStorage.setItem('storageListViewModeEnable', true);
                  }}
                >
                  <i className="icon mbc-icon listview big" />
                </span>
              </div>
            </div>
          </Caption>
        </div>
        {listViewMode && (
          <div className={classNames(Styles.listHeaderContent)}>
            {bucketList?.length ? (
              <Link to="createBucket">
                <button className={bucketList === null ? Styles.btnHide : 'btn btn-secondary'} type="button">
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Storage Bucket</span>
                </button>
              </Link>
            ) : null}
          </div>
        )}
        <div className={classNames(Styles.content, cardViewMode && Styles.cardView)}>
          <div>
            <div className={Styles.listContent}>
              {bucketList?.length === 0 ? (
                <>
                  <div className={Styles.emptyBuckets}>
                    <span>
                      You don&apos;t have any storage accounts at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <Link to="createBucket">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Create a Storage Bucket</span>
                      </button>
                    </Link>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <BucketList isCardView={cardViewMode} user={props.user} />
                  {bucketList?.length ? (
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : null}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AllBuckets;
