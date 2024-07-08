import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DataTransfers.style.scss';
import { Link, withRouter } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants.js';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
// import { setDataTransferList, setPagination } from './redux/dataTransferSlice';
import { GetDataTransfers } from './redux/dataTransfer.services';
import DataProductCardItem from './Layout/CardView/DataTransferCardItem';
import DataProductListItem from './Layout/ListView/DataTransferListItem';
import DataTransferFilter from './DataTransferFilter';

const DataProducts = ({ user, history, hostHistory }) => {
  const dispatch = useDispatch();
  const {
    // dataTransfers,
    firstDataLoaded,
    // pagination: { dataProductListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
    pagination: { dataProductListResponse },
  } = useSelector((state) => state.provideDataTransfers);

  const [totalNumberOfRecords, setTotalNumberOfRecords] = useState(0);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);
  const [paginatedRecords, setPaginatedRecords] = useState([]);

  const [cardViewMode, setCardViewMode] = useState(true);
  const [listViewMode, setListViewMode] = useState(false);
  const [isProviderCreatorFilter, setIsProviderCreatorFilter] = useState(
    JSON.parse(
      sessionStorage.getItem(SESSION_STORAGE_KEYS.MY_DATATRANSFER_FILTER)
    ) || false
  );
  const [openCloseFilter, setOpenCloseFilter] = useState(false);
  const [dataTransferFilterApplied, setdataTransferFilterApplied] = useState(false);
  const [filteredDataTransfer, setfilteredDataTransfer] = useState([]);
  const [queryParams, setQueryParams] = useState({
    division: [],
    subDivision: [],
    departments: [],
    dataStewards: [],
    informationOwners: [],
  });
  // const [departments, setDepartments] = useState([]);
  // const [divisions, setDivisions] = useState([]);
  // const [subdivisions, setSubDivisions] = useState([]);
  // const [dataStewards, setDataStewards] = useState([]);
  // const [informationOwners, setInformationOwners] = useState([]);


  // const getFilterDropdownValues = ({

  //   divisions,
  //   subDivisions,
  //   departments,
  //   informationOwners,
  //   dataStewards
  // }) => {
  //   setDepartments(departments || []);
  //   setDivisions(divisions || []);
  //   setSubDivisions(subDivisions || []);
  //   setDataStewards(dataStewards || []);
  //   setInformationOwners(informationOwners || []);
  // };

  useEffect(() => {
    dispatch(GetDataTransfers(isProviderCreatorFilter));
  }, [dispatch, isProviderCreatorFilter]);

  useEffect(() => {
    if (sessionStorage.getItem('listViewModeEnable') == null) {
      setCardViewModeFn()
    } else {
      setListViewModeFn()
    }

  }, []);

  useEffect(() => {
    const tempList = filteredDataTransfer?.length > 0 ? filteredDataTransfer : [];
    const tillRecord = 0 + maxItemsPerPage;
    const records = tempList.slice(0, tillRecord);
    setTotalNumberOfRecords(tempList.length);
    setTotalNumberOfPages(Math.ceil(tempList.length / maxItemsPerPage));
    setPaginatedRecords(records);
    setCurrentPageNumber(1);

  }, [filteredDataTransfer]);

  useEffect(() => {
    filterDataTransfer();
  }, [queryParams]);

  useEffect(() => {
    setfilteredDataTransfer(dataProductListResponse);
  }, [dataProductListResponse])

  const filterDataTransfer = () => {
    const filteredData = dataProductListResponse?.filter(inFilter);
    setfilteredDataTransfer(filteredData);
  };

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = filteredDataTransfer.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = filteredDataTransfer.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(filteredDataTransfer?.length / pageNum);
    const modifiedData = filteredDataTransfer.slice(0, pageNum);
    setPaginatedRecords(modifiedData);
    setTotalNumberOfPages(totalNumberOfPages);
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
  };

  const setCardViewModeFn = () => {
    setCardViewMode(true);
    setListViewMode(false);
    sessionStorage.removeItem('listViewModeEnable');
  };

  const setListViewModeFn = () => {
    setCardViewMode(false);
    setListViewMode(true);
    sessionStorage.setItem('listViewModeEnable', true)
  };

  const onDataFilterChange = () => {
    sessionStorage.setItem(
      SESSION_STORAGE_KEYS.MY_DATATRANSFER_FILTER,
      JSON.stringify(!isProviderCreatorFilter)
    );
    setIsProviderCreatorFilter(!isProviderCreatorFilter);
  }

  const inFilter = (transferData) => {
    const transferInfo = transferData.providerInformation.contactInformation;
    let depValue = true;
    let divValue = true;
    let subDivValue = true;
    let stewardValue = true;
    let infoOwnerValue = true;
    queryParams?.departments?.length === 0 ? depValue = true : queryParams?.departments.includes(transferInfo?.department) ? depValue = true : depValue = false;
    queryParams?.division?.length === 0 ? divValue = true : queryParams?.division.includes(transferInfo?.division?.id) ? divValue = true : divValue = false;
    let list = [];
    queryParams?.subDivision?.map((item) => { list.push(item.split('@-@')[0]) });
    queryParams?.subDivision?.length === 0 || queryParams?.division?.length === 0 ? subDivValue = true : transferInfo?.division?.subDivision?.id ? queryParams?.subDivision?.includes(transferInfo?.division?.subDivision?.id + '@-@' + transferInfo?.division?.id) ? subDivValue = true : subDivValue = false : list.includes('EMPTY') ? subDivValue = true : subDivValue = false;
    queryParams?.dataStewards?.length === 0 ? stewardValue = true : queryParams?.dataStewards?.includes(transferInfo?.name?.shortId) ? stewardValue = true : stewardValue = false;
    queryParams?.informationOwners?.length === 0 ? infoOwnerValue = true : queryParams?.informationOwners?.includes(transferInfo?.informationOwner?.shortId) ? infoOwnerValue = true : infoOwnerValue = false;

    return depValue && divValue && subDivValue && stewardValue && infoOwnerValue;

  }

  return (
    <>
      <button
        className={classNames('btn btn-text back arrow', Styles.backBtn)}
        type="submit"
        onClick={() => hostHistory.goBack()}
      >
        Back
      </button>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <h3>Data Transfer Overview</h3>
            <div className={classNames(Styles.listHeader)}>
              <div tooltip-data="Card View">
                <span
                  className={cardViewMode ? Styles.iconactive : Styles.iconInActive}
                  onClick={() => {
                    setCardViewModeFn();
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
                    setListViewModeFn();
                  }}
                >
                  <i className="icon mbc-icon listview big" />
                </span>
              </div>
              <span className={Styles.dividerLine}> &nbsp; </span>
              <div tooltip-data="Filters">
                <span
                  className={openCloseFilter ? Styles.activeFilters : ''}
                  onClick={() => { setOpenCloseFilter(!openCloseFilter) }}
                >
                  {dataTransferFilterApplied && (<i className="active-status" />)}
                  <i className="icon mbc-icon filter big" />
                </span>
              </div>
            </div>
          </div>
          {firstDataLoaded && (<DataTransferFilter
            dataTransferDataLoaded={firstDataLoaded}
            setdataTransferFilterApplied={(value) => setdataTransferFilterApplied(value)}
            getFilterQueryParams={(queryParams) => setQueryParams(queryParams)}
            // getDropdownValues={getFilterDropdownValues}
            openFilters={openCloseFilter}
          />)}
          <div>
            <button className={classNames(Styles.tagItem,
              isProviderCreatorFilter ? Styles.selectedItem : '')}
              onClick={() => { onDataFilterChange() }}>
              My Data Transfers</button>
          </div>
          <p className={'text-center'}>Click on <i className="icon mbc-icon copy-new"></i> to Create Copy</p>
          <div>
            <div>
              {dataProductListResponse?.length === 0 ? (
                <div className={classNames(Styles.content)}>
                  <div className={Styles.listContent}>
                    <div className={Styles.emptyProducts}>
                      <span>
                        You don&apos;t have any data transfers at this time.
                        <br /> Please create a new one.
                      </span>
                    </div>
                    <div className={Styles.subscriptionListEmpty}>
                      <br />
                      <Link to="datasharing/create">
                        <button className={'btn btn-tertiary'} type="button">
                          <span>Provide new data transferr</span>
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
                          <div className={Styles.cardViewContainer} onClick={() => history.push('/datasharing/create')}>
                            <div className={Styles.addicon}> &nbsp; </div>
                            <label className={Styles.addlabel}>Provide new data transfer</label>
                          </div>
                          {paginatedRecords?.map((product, index) => {
                            return <DataProductCardItem key={index} product={product} user={user} isProviderCreatorFilter={isProviderCreatorFilter} />;
                          })}
                        </>
                      ) : null}
                    </div>
                    <div>
                      {listViewMode ? (
                        <>
                          <div
                            className={classNames('ul-table dataproducts', dataProductListResponse?.length === 0 ? 'hide' : '')}
                          >
                            <div
                              className={classNames('data-row', Styles.listViewContainer)}
                              onClick={() => history.push('/datasharing/create')}
                            >
                              <span className={Styles.addicon}> &nbsp; </span>
                              <label className={Styles.addlabel}>Provide new data transfer</label>
                            </div>

                            {paginatedRecords?.map((product, index) => {
                              return <DataProductListItem key={index} product={product} user={user} isProviderCreatorFilter={isProviderCreatorFilter} />;
                            })}
                          </div>
                        </>
                      ) : null}
                    </div>
                  </div>
                  {totalNumberOfRecords ? (
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
    </>
  );
};
export default withRouter(DataProducts);
