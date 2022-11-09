import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './dataList.style.scss';
import { Link, withRouter } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import { setData, setPagination } from './redux/dataSlice';
import { GetData } from './redux/data.services';
import DataCardItem from './Layout/CardView/DataCardItem';
import DataListItem from './Layout/ListView/DataListItem';

const DataList = ({ user, history }) => {
  const dispatch = useDispatch();
  const {
    data,
    pagination: { dataListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.data);

  const [cardViewMode, setCardViewMode] = useState(true);
  const [listViewMode, setListViewMode] = useState(false);

  useEffect(() => {
    dispatch(GetData());
  }, [dispatch]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = dataListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setData(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = dataListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setData(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(dataListResponse?.length / pageNum);
    const modifiedData = dataListResponse.slice(0, pageNum);
    dispatch(setData(modifiedData));
    dispatch(
      setPagination({
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      }),
    );
  };

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={classNames(Styles.wrapper)}>
        <div className={classNames(Styles.caption)}>
          <h3>Data Products</h3>
          <div className={classNames(Styles.listHeader)}>
            <div tooltip-data="Card View">
              <span
                className={cardViewMode ? Styles.iconactive : Styles.iconInActive}
                onClick={() => {
                  setCardViewMode(true);
                  setListViewMode(false);
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
                }}
              >
                <i className="icon mbc-icon listview big" />
              </span>
            </div>
          </div>
        </div>
        <div>
          <div>
            {data?.length === 0 ? (
              <div className={classNames(Styles.content)}>
                <div className={Styles.listContent}>
                  <div className={Styles.emptyProducts}>
                    <span>
                      You don&apos;t have any data products at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <Link to="create">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Create Data Product</span>
                      </button>
                    </Link>
                  </div>
                </div>
              </div>
            ) : (
              <>
                <div className={Styles.allDataproductContent}>
                  <div className={classNames(Styles.allDataproductCardviewContent)}>
                    {cardViewMode ? (
                      <>
                        <div className={Styles.cardViewContainer} onClick={() => history.push('/createData')}>
                          <div className={Styles.addicon}> &nbsp; </div>
                          <label className={Styles.addlabel}>Create new data product</label>
                        </div>
                        {data?.map((product, index) => {
                          return <DataCardItem key={index} product={product} user={user} />;
                        })}
                      </>
                    ) : null}
                  </div>
                  <div>
                    {listViewMode ? (
                      <>
                        <div className={classNames('ul-table dataproducts', data?.length === 0 ? 'hide' : '')}>
                          <div
                            className={classNames('data-row', Styles.listViewContainer)}
                            onClick={() => history.push('/createData')}
                          >
                            <span className={Styles.addicon}> &nbsp; </span>
                            <label className={Styles.addlabel}>Create new data product</label>
                          </div>
                          {data?.map((product, index) => {
                            return <DataListItem key={index} product={product} user={user} />;
                          })}
                        </div>
                      </>
                    ) : null}
                  </div>
                </div>
                {data?.length ? (
                  <Pagination
                    totalPages={totalNumberOfPages}
                    pageNumber={currentPageNumber}
                    onPreviousClick={onPaginationPreviousClick}
                    onNextClick={onPaginationNextClick}
                    onViewByNumbers={onViewByPageNum}
                    displayByPage={true}
                  />
                ) : null}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
export default withRouter(DataList);
