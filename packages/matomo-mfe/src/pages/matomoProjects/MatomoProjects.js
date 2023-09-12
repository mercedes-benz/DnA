import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import Styles from './matomo-projects.scss';
import { MatomoList } from './MatomoList';
// dna-container
import Caption from 'dna-container/Caption';
import Pagination from 'dna-container/Pagination';


import { matomoActions } from '../redux/matomo.actions';

const MatomoProjects = (props) => {


  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const dispatch = useDispatch();
  const {
    // matomoList,
    pagination: { matomoListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.matomo);

  const matomoList = [
      {
        "classificationType": "string",
        "collaborators": [
          {
            "department": "string",
            "email": "string",
            "firstName": "string",
            "id": "string",
            "lastName": "string",
            "mobileNumber": "string",
            "permissions": {}
          }
        ],
        "createdBy": {
          "department": "string",
          "email": "string",
          "firstName": "string",
          "id": "string",
          "lastName": "string",
          "mobileNumber": "string"
        },
        "createdOn": "2023-09-12T06:16:49.306Z",
        "department": "string",
        "division": "string",
        "id": "string",
        "lastModified": "2023-09-12T06:16:49.306Z",
        "permission": {},
        "piiData": true,
        "siteId": "string",
        "siteName": "string",
        "siteUrl": "string",
        "status": "string",
        "subDivision": "string"
      }
    ]

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = matomoListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'MATOMO_DATA',
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
    const modifiedData = matomoListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'MATOMO_DATA',
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
    const totalNumberOfPages = Math.ceil(matomoListResponse?.length / pageNum);
    const modifiedData = matomoListResponse.slice(0, pageNum);
    dispatch({
      type: 'MATOMO_DATA',
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
    dispatch(matomoActions.getMatomoList());
  }, [dispatch, maxItemsPerPage]);

console.log(matomoList,'-----------------------------')
  return (
    // <>
    //   <div className={classNames(Styles.mainPanel)}>
    //     <div className={classNames(Styles.wrapper)}>
    //     <>
    //           <Caption title={'Matomo Projects'} />

    //           <div className={Styles.allProjectContent}>
    //             <div className={Styles.newProjectCard}>
    //               <div className={Styles.addicon}> &nbsp; </div>
    //               <label className={Styles.addlabel}>Create new project</label>
    //             </div>
    //             <div></div>
    //           </div>
    //         </>
    //     </div>
    //   </div>
    // </>
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title="Matomo Projects">
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
            {matomoList?.length ? (
              <Link to="createMatomo">
                <button className={matomoList === null ? Styles.btnHide : 'btn btn-secondary'} type="button">
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Project</span>
                </button>
              </Link>
            ) : null}
          </div>
        )}
        <div className={classNames(Styles.content, cardViewMode && Styles.cardView)}>
          <div>
            <div className={Styles.listContent}>
              {matomoList?.length === 0 ? (
                <>
                  <div className={Styles.emptyBuckets}>
                    <span>
                      You don&apos;t have any Matomo at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <Link to="createMatomo">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Create a Project</span>
                      </button>
                    </Link>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <MatomoList isCardView={cardViewMode} user={props.user} />
                  {matomoList?.length ? (
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
export default MatomoProjects;
