import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './data-entry-projects.scss';
import { dataEntryApi } from '../../apis/dataentry.api';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import Projects from '../../components/projects/Projects';
import DataEntryProjectForm from '../../components/dataEntryProjectForm/DataEntryProjectForm';

const DataEntryProjects = (props) => {

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [projects, setProjects] = useState([]);
  
  const [createProject, setCreateProject] = useState(false);

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
    getProjects();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const getProjects = () => {      
      ProgressIndicator.show();
      dataEntryApi
        .getDataEntryProjects(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          setProjects(res?.data?.records);
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
              : 'Fetching data entry projects failed!',
            'alert',
          );
        });
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <Caption title="Data Entry Projects">
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
            {projects && projects?.length ? (
              <div className={Styles.createNewArea}>
                <button className={projects === null ? Styles.btnHide : 'btn btn-secondary'} type="button" onClick={() => setCreateProject(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Data Entry Project</span>
                </button>
              </div>
            ) : null}
          </div>
        )}
        <div className={classNames(Styles.content, cardViewMode && Styles.cardView)}>
          <div>
            <div className={Styles.listContent}>
              {!projects || projects?.length === 0 ? (
                <>
                  <div className={Styles.emptyBuckets}>
                    <span>
                      You don&apos;t have any Data Entry Project at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <button className={'btn btn-tertiary'} type="button" onClick={() => setCreateProject(true)}>
                      <span>Create new Data Entry Project</span>
                    </button>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <Projects isCardView={cardViewMode} user={props.user} projects={projects} callWorkspaces={getProjects} onCreateWorkspace={(val) => setCreateProject(val)} />
                  {projects?.length ? (
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
      { createProject &&
        <Modal
          title={'Create Data Entry Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={createProject}
          content={<DataEntryProjectForm edit={false} onSave={() => {setCreateProject(false); getProjects();}} />}
          scrollableContent={true}
          onCancel={() => setCreateProject(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </>
  );
};
export default DataEntryProjects;
