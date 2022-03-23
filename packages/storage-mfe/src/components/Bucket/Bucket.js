import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { BucketList } from './BucketList';
import Styles from './Buckets.scss';

import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';

// import from DNA Container
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';

const AllBuckets = () => {
  const bucketList = useSelector((state) => state.bucket.bucketList);

  const [modal, setModal] = useState(false);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem('paginationMaxItemsPerPage'), 10) || 15,
  );
  const [bucketProjectListResponse, setBucketListResponse] = useState([]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = bucketProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setBucketListResponse([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = bucketProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setBucketListResponse([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    const totalNumberOfPages = Math.ceil(bucketProjectListResponse.length / pageNum);
    setTotalNumberOfPages(totalNumberOfPages);
    const modifiedData = bucketProjectListResponse.slice(0, pageNum);
    setBucketListResponse([...modifiedData]);
  };

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

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
