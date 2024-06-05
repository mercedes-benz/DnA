import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './data-entry-projects.scss';
import { dataEntryApi } from '../../apis/dataentry.api';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import DataEntryProjectForm from '../../components/dataEntryProjectForm/DataEntryProjectForm';
import DataEntryProjectCard from '../../components/dataEntryProjectCard/DataEntryProjectCard';
import DataEntryProjectTable from '../../components/dataEntryProjectTable/DataEntryProjectTable';

const DataEntryProjects = ({user}) => {
  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [projects, setProjects] = useState([]);
  const [createProject, setCreateProject] = useState(false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [editProject, setEditProject]  = useState(false);

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

  // delete project
  const deleteProjectContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.name}? </h3>
      <h5>It will delete the project.</h5>
    </div>
  );

  const deleteProjectClose = () => {
    setDeleteModal(false);
  };

  const deleteProjectAccept = () => {
    ProgressIndicator.show();
    dataEntryApi
      .deleteDataEntryProject(selectedItem.id)
      .then(() => {
        getProjects();
        Notification.show(`Data Entry Project ${selectedItem.name} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting data entry project. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const getProjects = () => {      
      ProgressIndicator.show();
      dataEntryApi
        .getDataEntryProjects(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          setProjects(res?.data?.data);
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
        <Caption title="Data Entry as a Service Projects">
          <div className={classNames(Styles.listHeader)}>
            <div tooltip-data="Card View">
              <span
                className={cardViewMode ? Styles.iconActive : Styles.iconInactive}
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
                className={listViewMode ? Styles.iconActive : Styles.iconInactive}
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
        {projects?.length === 0 && 
          <div className={Styles.noProjectContainer}>
            <div className={Styles.messageContainer}>
              <p className={Styles.lead}>Hi <span>{user.firstName} {user.lastName}</span>, you don&apos;t have<br/>any Data Entry as a Service project.</p>
              <p>Click on the below button to create one</p>
            </div>
            <div className={Styles.btnContainer}>
              <button className={'btn btn-tertiary'} onClick={() => setCreateProject(true)}>
                <span>Create now</span>
              </button>
            </div>
          </div>
        }
        {listViewMode && (
          <>
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
          </>
        )}
        {projects?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <div className={classNames(Styles.projectsContainer)}>
                <div className={Styles.createNewCard} onClick={() => setCreateProject(true)}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create new Data Entry Project</label>
                </div>
                {projects.map((project) => 
                  <DataEntryProjectCard
                    key={project.id}
                    user={user}
                    project={project}
                    onEditProject={(project) => { 
                      setSelectedItem(project);
                      setEditProject(true);}
                    }
                    onDeleteProject={(project) => {
                      setSelectedItem(project);
                      setDeleteModal(true);
                    }}
                  />
                )}
              </div>
            }
            {listViewMode && 
              <div className={Styles.projectTable}>
                <div className={Styles.tableHeader}>
                  <div className={Styles.col1}>
                    <span>Name</span>
                  </div>
                  <div className={Styles.col2}>
                    <span>Data Lakehouse Link</span>
                  </div>
                  <div className={Styles.col3}>
                    <span>Created On</span>
                  </div>
                  <div className={Styles.col4}>
                    <span>Data Classification</span>
                  </div>
                  <div className={Styles.col5}>
                    <span>Action</span>
                  </div>
                </div>
                {projects?.map((project) => 
                  <DataEntryProjectTable
                    key={project.id}
                    user={user}
                    project={project}
                    onEditProject={(project) => { 
                      setSelectedItem(project);
                      setEditProject(true);}
                    }
                    onDeleteProject={(project) => {
                      setSelectedItem(project);
                      setDeleteModal(true);
                    }}
                  />
                )}
              </div>
            }
            {projects?.length && (
              <Pagination
                totalPages={totalNumberOfPages}
                pageNumber={currentPageNumber}
                onPreviousClick={onPaginationPreviousClick}
                onNextClick={onPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
              />
            )}
          </div>
        )}
      </div>
      { createProject &&
        <Modal
          title={'Create Data Entry Project'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createProject}
          content={<DataEntryProjectForm user={user} edit={false} onSave={() => {setCreateProject(false); getProjects();}} />}
          scrollableContent={true}
          onCancel={() => setCreateProject(false)}
        />
      }
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteProjectContent}
        onCancel={deleteProjectClose}
        onAccept={deleteProjectAccept}
      />
      { editProject &&
        <Modal
          title={'Edit Data Entry Project'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={editProject}
          content={<DataEntryProjectForm user={user} edit={true} project={selectedItem} onSave={() => {setEditProject(false); getProjects(); }} />}
          scrollableContent={true}
          onCancel={() => setEditProject(false)}
        />
      }
    </>
  );
};
export default DataEntryProjects;
