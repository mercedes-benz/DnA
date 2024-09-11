import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './power-platform-workspaces.scss';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import PowerPlatformWorkspaceForm from '../../components/powerPlatformWorkspaceForm/PowerPlatformWorkspaceForm';
import PowerPlatformWorkspaceCard from '../../components/powerPlatformWorkspaceCard/PowerPlatformWorkspaceCard';
import PowerPlatformWorkspaceTable from '../../components/powerPlatformWorkspacetTable/PowerPlatformWorkspaceTable';
import SharedDevelopmentTou from '../../components/sharedDevelopmentTou/SharedDevelopmentTou';
import { powerPlatformApi } from '../../apis/power-platform.api';
import { useDispatch } from 'react-redux';
import { getLovs } from '../../redux/lovsSlice';
import { Envs } from '../../utilities/envs';

const PowerPlatformWorkspaces = ({user}) => {
  const dispatch = useDispatch();
  
  const { account } = useParams();

  useEffect(() => {
    dispatch(getLovs());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [projects, setProjects] = useState([]);
  const [createProject, setCreateProject] = useState(account === 'shared' ? true : false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [editProject, setEditProject]  = useState(false);
  const [showTou, setShowTou] = useState(account === 'tou' ? true : false);

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
    powerPlatformApi
      .deletePowerPlatformWorkspace(selectedItem.id)
      .then(() => {
        getProjects();
        Notification.show(`Power Platform Account ${selectedItem.name} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting power platform account. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const getProjects = () => {      
      ProgressIndicator.show();
      powerPlatformApi
        .getPowerPlatformWorkspaces(currentPageOffset, maxItemsPerPage)
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
              : 'Fetching power platform accounts failed!',
            'alert',
          );
        });
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title="Power Platform Accounts">
          <div className={classNames(Styles.listHeader)}>
            <div className={classNames(Styles.headerLink)}>
              <button className={'btn btn-primary'} onClick={() => { window.open(`${Envs.CONTAINER_APP_URL}/#/toolDetails/powerPlatform`) }}>
                <i className="icon mbc-icon info" /> <span>Show Power Platform Details Page</span>
              </button>
            </div>
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
        {listViewMode && (
          <>
            {projects && projects?.length ? (
              <div className={Styles.createNewArea}>
                <button className={projects === null ? Styles.btnHide : 'btn btn-secondary'} type="button" onClick={() => setCreateProject(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Power Platform Workspace</span>
                </button>
              </div>
            ) : null}
          </>
        )}
        {projects?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <div className={classNames(Styles.projectsContainer)}>
                {/* <div className={Styles.createNewCard} onClick={() => setCreateProject(true)}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create new Power Platform Workspace</label>
                </div> */}
                {projects.map((project) => 
                  <PowerPlatformWorkspaceCard
                    key={project.id}
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
                <h2 className={Styles.sectionTitle}>Shared Development Account Environments</h2>
                {projects.map((project) => 
                  <PowerPlatformWorkspaceCard
                    key={project.id}
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
                <h2 className={Styles.sectionTitle}>Full Development Account Environments</h2>
                {projects.map((project) => 
                  <PowerPlatformWorkspaceCard
                    key={project.id}
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
                  <PowerPlatformWorkspaceTable
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
          title={'Order Power Platform Account'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createProject}
          content={<PowerPlatformWorkspaceForm edit={false} onSave={() => {setCreateProject(false); getProjects();}} />}
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
          content={<PowerPlatformWorkspaceForm edit={true} project={selectedItem} onSave={() => {setEditProject(false); getProjects(); }} />}
          scrollableContent={true}
          onCancel={() => setEditProject(false)}
        />
      }
      { showTou &&
        <Modal
          title={'Terms of Use'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showTou}
          content={<SharedDevelopmentTou hideAccept={true} />}
          scrollableContent={true}
          onCancel={() => setShowTou(false)}
        />
      }
    </>
  );
};
export default PowerPlatformWorkspaces;
