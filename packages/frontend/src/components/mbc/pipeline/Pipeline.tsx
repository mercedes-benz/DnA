import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { IPipelineProjectDetail } from 'globals/types';
import Styles from './Pipeline.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { Link } from 'react-router-dom';
const classNames = cn.bind(Styles);
// import Modal from '../../formElements/modal/Modal';
// import PipelineSubModel from './pipelineSubModel/PipelineSubModel';
import PipelineSubList from './pipelineSubList/PipelineSubList';
import Pagination from '../pagination/Pagination';
import Caption from '../shared/caption/Caption';
import { SESSION_STORAGE_KEYS } from 'globals/constants';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { PipelineApiClient } from '../../../services/PipelineApiClient';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import PipelineCardItem from './PipelineCardItem';

const Pipeline = () => {
  // const [subscribePopup, setSubscribePopup] = useState(false);
  const [pipelineProjectList, setPipelineProjectList] = useState([]);
  const [pipelineProjectListResponse, setPipelineProjectListResponse] = useState([]);

  const [totalNumberOfPages, setTotalNumberOfPages] = useState<number>(1);
  const [currentPageNumber, setCurrentPageNumber] = useState<number>(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState<number>(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );

  const [info, setInfo] = useState(false);
  const [cardViewMode, setCardViewMode] = useState(
    sessionStorage.getItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE) === null
  );
  const [listViewMode, setListViewMode] = useState(
    sessionStorage.getItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE) !== null
  );

  const onInfoModalCancel = () => {
    setInfo(false);
  };

  const getRefreshedDagPermission = (projectId: string, dagIndex: number) => {
    const modDagList: IPipelineProjectDetail[] = pipelineProjectList.map((item: IPipelineProjectDetail) => {
      if (item.projectId === projectId) {
        item.dags[dagIndex].permissions = ['can_read', 'can_edit'];
      }
      return item;
    });

    setPipelineProjectList([...modDagList]);
  };
  const openInfo = () => {
    setInfo(true);
  };
  const getProjectSorted = (prjIdSortVal: any) => {
    setPipelineProjectList([...prjIdSortVal]);
  };

  const toggleToCardView = () => {
    setCardViewMode(true);
    setListViewMode(false);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE);
  };
  const toggleToListView = () => {
    setCardViewMode(false);
    setListViewMode(true);
    sessionStorage.setItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE, 'true');
  };

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = pipelineProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setPipelineProjectList([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = pipelineProjectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setPipelineProjectList([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onViewByPageNum = (pageNum: number) => {
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    const totalNumberOfPages = Math.ceil(pipelineProjectListResponse.length / pageNum);
    setTotalNumberOfPages(totalNumberOfPages);
    const modifiedData = pipelineProjectListResponse.slice(0, pageNum);
    setPipelineProjectList([...modifiedData]);
  };

  useEffect(() => {
    getPipelineProjectList();
  }, []);

  const getPipelineProjectList = () => {
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    PipelineApiClient.getPipelineProjectList()
      .then((response) => {
        const records = response.data;
        const prjIdSortVal = records.sort(function (item1: any, item2: any) {
          return parseInt(item2.projectId.replace('P', '')) - parseInt(item1.projectId.replace('P', ''));
        });
        setPipelineProjectListResponse(prjIdSortVal);
        const totalNumberOfPages = Math.ceil(prjIdSortVal.length / maxItemsPerPage);
        setTotalNumberOfPages(totalNumberOfPages);
        const modifiedData = prjIdSortVal.slice(0, maxItemsPerPage);
        setPipelineProjectList(modifiedData);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  };

  const contentForInfo = (
    <div className={Styles.infoPopup}>
      <div className={Styles.modalContent}>
        A pipeline project represents a single data flow or multiple data flows and each can be mapped to a specific
        Airflow DAG. This project can later be provisioned as a solution in the DnA portal to enable organisation wide
        transparency
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <Caption title="Pipeline">
            <div className={Styles.listHeader} style={{ "display": "flex", "justifyContent": "end" }}>
              <div tooltip-data="Card View">
                <span
                  className={cardViewMode ? Styles.iconactive : Styles.iconInActive}
                  onClick={toggleToCardView}
                >

                  <i className="icon mbc-icon widgets" />
                  <span className={Styles.dividerLine}> &nbsp; </span>
                </span>
              </div>
              <div tooltip-data="List View">
                <span
                  className={listViewMode ? Styles.iconactive : Styles.iconInActive}
                  onClick={toggleToListView}
                >
                  <i className="icon mbc-icon listview big" />
                </span>
              </div>
            </div>
          </Caption>
        </div>
        <div className={Styles.content}>
          <div className={Styles.NoSubscription}>
            <div className={Styles.addNewSubscrHeader} style={{ "backgroundColor": "#000", "border": "none" }}>
              <div className={Styles.appHeaderDetailsRow}>
                {listViewMode && (
                  <div className={classNames(Styles.listHeaderContent)}>
                    {pipelineProjectList?.length ? (
                      <Link to="createnewpipeline">
                        <button className="btn btn-secondary" type="button">
                          <span className={Styles.addCircle}>
                            <i className="icon mbc-icon plus" />
                          </span>
                          <span>Create new Pipeline Project</span>
                        </button>
                      </Link>
                    ) : null}
                  </div>
                )}
              </div>
            </div>

            <div className={Styles.subsriContent} style={{ "backgroundColor": "rgb(22 25 30)" }}>
              {pipelineProjectList.length === 0 ? (
                <div className={Styles.pipelineDescription}>
                  <p>
                    Pipeline service helps in creating data workflows that can have multiple data processing steps in
                    order to perform data transformation and to later identify data patterns using AI and ML.
                  </p>
                  <i
                    className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                    onClick={openInfo}
                    tooltip-data="Info"
                  />
                </div>
              ) : (
                ''
              )}
              {pipelineProjectList.length === 0 ? (
                <div className={Styles.subscriptionListEmpty}>
                </div>
              ) : (
                <div className={Styles.subscriptionList}>

                  {cardViewMode && (
                    <>


                      <div className={Styles.cardViewWrapper} style={{ "display": "flex", "flexWrap": "wrap" }}>
                        <div className={Styles.newStorageCard} style={{ "backgroundColor": "rgb(22 25 30)" }}>
                          <Link to="createnewpipeline">
                            <div className={Styles.addicon}> &nbsp; </div>
                            <label className={Styles.addlabel}>Create new Pipeline Project</label>
                          </Link>
                        </div>

                        {pipelineProjectList.map((project, index) => (
                          <PipelineCardItem
                            key={index}
                            project={project}
                            getRefreshedDagPermission={getRefreshedDagPermission}
                          />
                        ))}
                      </div>
                    </>
                  )}

                  {listViewMode && (
                    <PipelineSubList
                      listOfProject={pipelineProjectList}
                      getRefreshedDagPermission={getRefreshedDagPermission}
                      getProjectSorted={getProjectSorted}
                    />
                  )}


                  {pipelineProjectList ? (
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : (
                    ''
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      {info && (
        <InfoModal
          title={'About Pipeline'}
          modalWidth={'35vw'}
          show={info}
          content={contentForInfo}
          onCancel={onInfoModalCancel}
        />
      )}
    </React.Fragment>
  );
};

export default Pipeline;
