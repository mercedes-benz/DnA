import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './dataProductList.style.scss';
import { Link, withRouter } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import HeadingSection from 'dna-container/HeadingSection';
// import TagSection from 'dna-container/TagSection';
import DataProductFilter from 'dna-container/DataProductFilter';

import { setPagination ,setFilterQueryParams} from './redux/dataProductSlice';
import { GetDataProducts } from './redux/dataProduct.services';
import DataCardItem from './Layout/CardView/DataProductCardItem';
import DataListItem from './Layout/ListView/DataProductListItem';
import { dataProductApi } from '../../apis/dataproducts.api';

import headerImageUrl from '../../assets/Data-Products-Landing.png';
import { Envs } from '../../Utility/envs';



const DataProductList = ({ user, history }) => {
  const dispatch = useDispatch();
  const {
    data,
    pagination: {  totalNumberOfPages, currentPageNumber, maxItemsPerPage },
    FilterQueryParams
  } = useSelector((state) => state.dataProduct);

  const [cardViewMode, setCardViewMode] = useState(true);
  const [listViewMode, setListViewMode] = useState(false);
  const [dpFilterApplied, setDpFilterApplied] = useState(false);

  const getFilteredData = (FilterQueryValues) => {
    const queryData = {
      arts: FilterQueryValues?.artIds,
      platforms: FilterQueryValues?.platformIds,
      frontendTools: FilterQueryValues?.frontendToolIds,
      divisions: FilterQueryValues?.divisionIds,
      departments: FilterQueryValues?.departmentIds,
      productOwners: FilterQueryValues?.productOwners,
      informationOwners: FilterQueryValues?.informationOwners,
      dataStewards: FilterQueryValues?.dataStewards,
      offset: 0,
      limit: maxItemsPerPage
    };
    dispatch(setPagination({ currentPageNumber: 1 }));
    dispatch(setFilterQueryParams(queryData));
    dispatch(GetDataProducts(queryData));
  }


  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    let queryData = {...FilterQueryParams}
    queryData.offset = currentPageOffset;
    dispatch(setFilterQueryParams(queryData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
    dispatch(GetDataProducts(queryData));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1; 
    let queryData = {...FilterQueryParams}
    queryData.offset = currentPageOffset;
    dispatch(setFilterQueryParams(queryData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
    dispatch(GetDataProducts(queryData));
  };
  const onViewByPageNum = (pageNum) => {
    let queryData = {...FilterQueryParams}
    queryData.limit = pageNum;
    queryData.offset = 0;
    dispatch(setFilterQueryParams(queryData));
    dispatch(GetDataProducts(queryData));
    dispatch(
      setPagination({
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      }),
    );
  };

  const [openFilters, setOpenFilters]  = useState(false);

  const [tagValues, setTagValues] = useState([]);
  // const [tagFilterValues, setTagFilterValues] = useState([]);
  // const [selectedTags, setSelectedTags] = useState([]);
  const [, setSelectedTags] = useState([]);
  const [selectedTagsToPass] = useState([]);
  // const setSelectedFilter = (values) => {
  //   setSelectedTags(values);
  //   setSelectedTagsToPass(values);
  // }

  // useEffect(() => {
  //   dispatch(GetDataProducts(`&carlafunction=`+selectedTagsToPass.join(',')));
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, [selectedTagsToPass]);

  useEffect(() => {
    dataProductApi.getAllCarlaFunctions().then((res) => {
      setTagValues(res.data.data);
    }).catch((err) => {
      console.log(err);
    });
    if (sessionStorage.getItem('listViewModeEnable') == null) {
      setCardViewModeFn();
    } else {
      setListViewModeFn();
    }
  }, []);

  const [allDataProductsFirstTimeDataLoaded, setAllDataProductsFirstTimeDataLoaded] = useState(false);

  console.log(setAllDataProductsFirstTimeDataLoaded);


  const [showDataProductsFilter] = useState(false);

  // setShowDataProductsFilter(false);

  const setCardViewModeFn = () => {
    setCardViewMode(true);
    setListViewMode(false);
    sessionStorage.removeItem('listViewModeEnable');
  };

  const setListViewModeFn = () => {
    setCardViewMode(false);
    setListViewMode(true);
    sessionStorage.setItem('listViewModeEnable',true)
  };

  return (
    <>
      <div className={Styles.mainPanel}>
        <HeadingSection
          title="Data Products"
          subTitle="Data is one of the most valuable assets in our company, therefore we treat our data as product! We offer you a growing selection of intuitive to use and well documented data products - check it out!"
          headerImage={headerImageUrl}
          isBackButton={true}
        />
        <p>Tip: All cross-functional Data products are published in 
        <a href={Envs.DDX_URL} target='_blank' rel="noreferrer"> DDX</a>.</p>
      </div>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <h3>Data Products</h3>
            <div>
              {/* <TagSection tags={tagValues.map(item=>item.name)} selectedTags={selectedTags} setSeletedTags={setSelectedFilter}></TagSection> */}
            </div>
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
                  className={openFilters ? Styles.activeFilters : ''}
                  onClick={() => setOpenFilters(!openFilters)}
                >
                  {dpFilterApplied && <i className="active-status" />}
                  <i className="icon mbc-icon filter big" />
                </span>
            </div>
          </div>
          </div>
          <DataProductFilter
            user={user}
            getFilteredData={(FilterQueryValues) => getFilteredData(FilterQueryValues)}
            dataProductsDataLoaded={true}
            setDataProductsDataLoaded={(value) => allDataProductsFirstTimeDataLoaded(value)}
            showDataProductsFilter={showDataProductsFilter}
            openFilters={openFilters}
            getAllTags={(tags)=>{setTagValues(tags)}}
            getValuesFromFilter={(value) => {
              //setTagFilterValues(value.tagFilterValues ? value.tagFilterValues : []);
              setSelectedTags(value.tagFilterValues ? value.tagFilterValues.map((item)=>item.name) : []);
            }}
            setDpFilterApplied = {(value) => setDpFilterApplied(value) }
            tagsList={tagValues.map(item=>item.name)}
            setSelectedTags={selectedTagsToPass}
            />
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
                      <Link to="/dataproduct/create">
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
                          <div className={Styles.cardViewContainer} onClick={() => history.push('/dataproduct/create')}>
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
                              onClick={() => history.push('/dataproduct/create')}
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
    </>
  );
};
export default withRouter(DataProductList);
