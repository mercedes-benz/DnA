import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './fabric-workspaces.scss';
import { fabricApi } from '../../apis/fabric.api';

// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import Workspaces from '../../components/workspaces/Workspaces';
import FabricWorkspaceForm from '../../components/fabricWorkspaceForm/FabricWorkspaceForm';

const FabricWorkspaces = (props) => {

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [workspaces, setWorkspaces] = useState([]);
  
  const [createWorkspace, setCreateWorkspace] = useState(false);

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);
  
  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetTemp = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onPaginationNextClick = () => {
    const currentPageOffsetTemp = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };


  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    getWorkspaces();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const getWorkspaces = () => {      
      ProgressIndicator.show();
      fabricApi
        .getFabricWorkspaces(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          const sortedWorkspaces = res?.data?.records.sort((x, y) => {
              let fx = x.name.toLowerCase(), fy = y.name.toLowerCase();
              if (fx < fy) {
                  return -1;
              }
              if (fx > fy) {
                  return 1;
              }
              return 0;
          });
          setWorkspaces(sortedWorkspaces);
          const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
          setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
          setTotalNumberOfPages(totalNumberOfPagesTemp);
          
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching fabric workspaces failed!',
            'alert',
          );
        });
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title="Fabric Workspaces">
            <div className={classNames(Styles.listHeader)}>
              <div className={Styles.btnContainer}>
                <button className="btn btn-primary" onClick={getWorkspaces} tooltip-data="Refresh">
                  <i className="icon mbc-icon refresh"></i>
                </button>
              </div>
              <span className={Styles.dividerLine}> &nbsp; </span>
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
            {workspaces && workspaces?.length ? (
              <div className={Styles.createNewArea}>
                <button className={workspaces === null ? Styles.btnHide : 'btn btn-secondary'} type="button" onClick={() => setCreateWorkspace(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Fabric Workspace</span>
                </button>
              </div>
            ) : null}
          </div>
        )}
        <div className={classNames(Styles.content, cardViewMode && Styles.cardView)}>
          <div>
            <div className={Styles.listContent}>
              {!workspaces || workspaces?.length === 0 ? (
                <>
                  <div className={Styles.emptyBuckets}>
                    <span>
                      You don&apos;t have any Fabric Workspace at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <button className={'btn btn-tertiary'} type="button" onClick={() => setCreateWorkspace(true)}>
                      <span>Create new Fabric Workspace</span>
                    </button>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <Workspaces isCardView={cardViewMode} user={props.user} workspaces={workspaces} callWorkspaces={getWorkspaces} onCreateWorkspace={(val) => setCreateWorkspace(val)} />
                  {workspaces?.length ? (
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
      { createWorkspace &&
        <Modal
          title={'Create Fabric Workspace'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createWorkspace}
          content={<FabricWorkspaceForm edit={false} onSave={() => {setCreateWorkspace(false); getWorkspaces();}} />}
          scrollableContent={true}
          onCancel={() => setCreateWorkspace(false)}
        />
      }
    </>
  );
};
export default FabricWorkspaces;
