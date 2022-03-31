import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { BucketList } from './BucketList';
import Styles from './Buckets.scss';

// import from DNA Container
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
import { bucketsApi } from '../../apis/buckets.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';

const AllBuckets = () => {
  const dispatch = useDispatch();
  const { isLoading, bucketList } = useSelector((state) => state.bucket);
  const { isLoading: fileExplorerLoading } = useSelector((state) => state.fileExplorer);
  const { isLoading: connectionLoading } = useSelector((state) => state.connectionInfo);
  const [modal, setModal] = useState(false);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem('paginationMaxItemsPerPage'), 10) || 2,
  );
  const [bucketProjectListResponse, setBucketListResponse] = useState([]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = bucketProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = bucketProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    const totalNumberOfPages = Math.ceil(bucketProjectListResponse.length / pageNum);
    setTotalNumberOfPages(totalNumberOfPages);
    const modifiedData = bucketProjectListResponse.slice(0, pageNum);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
  };

  useEffect(() => {
    if (isLoading) {
      ProgressIndicator.show();
    } else {
      ProgressIndicator.hide();
    }
  }),
    [isLoading];

  useEffect(() => {
    if (!isLoading) {
      if (fileExplorerLoading) {
        ProgressIndicator.show();
      } else {
        ProgressIndicator.hide();
      }
    }
  }),
    [fileExplorerLoading];

  useEffect(() => {
    if (!isLoading && !fileExplorerLoading) {
      if (connectionLoading) {
        ProgressIndicator.show();
      } else {
        ProgressIndicator.hide();
      }
    }
  }),
    [connectionLoading];

  useEffect(() => {
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    bucketsApi
      .getAllBuckets()
      .then((res) => {
        setBucketListResponse(res?.data?.data);
        const totalNumberOfPages = Math.ceil(res?.data?.data.length / maxItemsPerPage);
        setTotalNumberOfPages(totalNumberOfPages);
        const modifiedData = res?.data?.data.slice(0, maxItemsPerPage);
        dispatch({
          type: 'BUCKET_DATA',
          payload: modifiedData,
        });
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(e.response.data.message ? e.reponse.data.message : 'Some error occurred!', 'alert');
      });
  }, [dispatch, maxItemsPerPage]);

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <h3>{bucketList?.length ? 'List of Buckets' : 'Buckets'}</h3>
          </div>
        </div>
        <div className={classNames(Styles.content)}>
          <div>
            <div className={classNames(Styles.listHeader)}>
              <React.Fragment>
                <div className={classNames(Styles.listHeaderContent)}>
                  {bucketList.length ? (
                    <React.Fragment>
                      <Link to="createBucket">
                        <button className={bucketList === null ? Styles.btnHide : 'btn btn-primary'} type="button">
                          <i className="icon mbc-icon plus" />
                          <span>Create New Bucket</span>
                        </button>
                      </Link>
                    </React.Fragment>
                  ) : null}
                </div>
              </React.Fragment>
            </div>
            <div className={Styles.listContent}>
              {bucketList?.length === 0 ? (
                <>
                  <div className={Styles.subscriptionListEmpty}>
                    <span>You dont have any storage accounts at this time, please create one</span>
                    <br />
                    <Link to="createBucket">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Create New Bucket</span>
                      </button>
                    </Link>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <BucketList bucketList={bucketList} />
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
      {modal && (
        <Modal
          title="Create A New Bucket"
          show={modal}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          onCancel={() => setModal(false)}
        />
      )}
    </>
  );
};

export default AllBuckets;
