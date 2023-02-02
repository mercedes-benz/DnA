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

  // const subscriPopupClose = () => {
  //   setSubscribePopup(false);
  // };
  // const addAirflowSuccessFn = (action: string) => {
  //   setSubscribePopup(false);
  //   setAirflowSuccess(action);
  //   Notification.show('Subscription created Successfully!');
  // };
  const onInfoModalCancel = () => {
    setInfo(false);
  };
  const getRefreshedDagPermission = (projectId: string, dagIndex: number) => {
    const modDagList: IPipelineProjectDetail[] = pipelineProjectList.map((item: IPipelineProjectDetail) => {
      if (item.projectId === projectId) {
        item.dags[dagIndex].permissions = ['can_dag_read', 'can_dag_edit'];
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
  }, []);

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
          <Caption title="Pipeline" />
        </div>
        <div className={Styles.content}>
          <div className={Styles.NoSubscription}>
            <div className={Styles.addNewSubscrHeader}>
              <React.Fragment>
                <div className={Styles.appHeaderDetails}>
                  {pipelineProjectList.length === 0 ? (
                    ''
                  ) : (
                    <React.Fragment>
                      <Link to="createnewpipeline">
                        <button
                          className={
                            pipelineProjectList === null
                              ? Styles.btnHide
                              : ' ' + ' btn btn-primary ' + Styles.addNewSubcibtn
                          }
                          type="button"
                        >
                          <i className="icon mbc-icon plus" />
                          <span>Create New Pipeline Project</span>
                        </button>
                      </Link>
                      <i
                        className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                        onClick={openInfo}
                        tooltip-data="Info"
                      />
                    </React.Fragment>
                  )}
                </div>
              </React.Fragment>
            </div>
            <div className={Styles.subsriContent}>
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
                  <Link to="createnewpipeline">
                    <button className={Styles.addNewSubcibtn + ' btn btn-tertiary'} type="button">
                      <span>Create New Pipeline Project</span>
                    </button>
                  </Link>
                </div>
              ) : (
                <div className={Styles.subscriptionList}>
                  <PipelineSubList
                    listOfProject={pipelineProjectList}
                    getRefreshedDagPermission={getRefreshedDagPermission}
                    getProjectSorted={getProjectSorted}
                  />
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
      {/* {subscribePopup && (
        <Modal
          title={'Create New Pipeline Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={subscribePopup}
          content={<PipelineSubModel addAirflowSuccessFn={addAirflowSuccessFn} />}
          scrollableContent={false}
          onCancel={subscriPopupClose}
        />
      )} */}
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
