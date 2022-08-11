import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './DataProducts.style.scss';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import { setDataProducts, setPagination } from './redux/dataProductSlice';
import { GetDataProducts } from './redux/dataProduct.services';
import DataProductCardItem from './DataProductCardItem';

const DataProducts = () => {
  const dispatch = useDispatch();
  const {
    dataProducts,
    pagination: { dataProductListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.provideDataProducts);

  useEffect(() => {
    dispatch(GetDataProducts());
  }, [dispatch]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = dataProductListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setDataProducts(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = dataProductListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setDataProducts(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(dataProductListResponse?.length / pageNum);
    const modifiedData = dataProductListResponse.slice(0, pageNum);
    dispatch(setDataProducts(modifiedData));
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
          <h3>Data Product Overview</h3>
          <div className={classNames(Styles.listHeader)}>
            {dataProducts?.length ? (
              <React.Fragment>
                <Link to="create">
                  <button className={dataProducts?.length === null ? Styles.btnHide : 'btn btn-primary'} type="button">
                    <i className="icon mbc-icon plus" />
                    <span>Provide Data Product</span>
                  </button>
                </Link>
              </React.Fragment>
            ) : null}
          </div>
        </div>
        <div>
          <div>
            {dataProducts?.length === 0 ? (
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
                        <span>Provide Data Product</span>
                      </button>
                    </Link>
                  </div>
                </div>
              </div>
            ) : (
              <>
                <div className={Styles.allDataproductContent}>
                  <div className={classNames('cardSolutions', Styles.allDataproductCardviewContent)}>
                    {dataProducts?.map((product, index) => {
                      return <DataProductCardItem key={index} product={product} />;
                    })}
                  </div>
                </div>
                {dataProducts?.length ? (
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
export default DataProducts;
