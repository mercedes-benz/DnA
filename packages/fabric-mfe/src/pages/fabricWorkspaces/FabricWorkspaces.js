import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './fabric-workspaces.scss';
import { fabricApi } from '../../apis/fabric.api';
import { useDispatch } from 'react-redux';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import { getLovs } from '../../redux/lovsSlice';
import FabricWorkspaceCard from '../../components/fabricWorkspaceCard/FabricWorkspaceCard';
import FabricWorkspaceRow from '../../components/fabricWorkspaceRow/FabricWorkspaceRow';
import FabricWorkspaceForm from '../../components/fabricWorkspaceForm/FabricWorkspaceForm';
import RoleCreationModal from '../../components/roleCreationModal/RoleCreationModal';
import RequestWorkspace from '../../components/requestWorkspace/RequestWorkspace';
import { Envs } from '../../utilities/envs';

const FabricWorkspaces = ({user}) => {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getLovs());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [workspaces, setWorkspaces] = useState([]);
  const [createWorkspace, setCreateWorkspace] = useState(false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedWorkspace, setSelectedWorkspace] = useState({});
  const [editWorkspace, setEditWorkspace]  = useState(false);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [requestWorkspace, setRequestWorkspace] = useState(false);

  const ownWorkspaces = workspaces.filter(workspace => user.id === workspace?.createdBy?.id);
  const requestedWorkspaces = workspaces.filter(workspace => user.id !== workspace?.createdBy?.id);

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

  // delete workspace
  const deleteWorkspaceContent = (
    <div>
      <h3>Are you sure you want to delete {selectedWorkspace.name}? </h3>
      <h5>It will delete the workspace.</h5>
    </div>
  );

  const deleteWorkspaceAccept = () => {
    ProgressIndicator.show();
    fabricApi
      .deleteFabricWorkspace(selectedWorkspace.id)
      .then(() => {
        getWorkspaces();
        Notification.show(`Fabric workspace ${selectedWorkspace.name} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting fabric workspace. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const getWorkspaces = () => {      
      ProgressIndicator.show();
      fabricApi
        .getFabricWorkspaces(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          if(res.status !== 204) {
            // const sortedProjects = res?.data?.records.sort((x, y) => {
            //     let fx = x.name.toLowerCase(), fy = y.name.toLowerCase();
            //     if (fx < fy) {
            //         return -1;
            //     }
            //     if (fx > fy) {
            //         return 1;
            //     }
            //     return 0;
            // });
            setWorkspaces(res?.data?.records);
            const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          } else {
            setWorkspaces([]);
          }
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
        <Caption title="Fabric Workspaces">
          <div className={classNames(Styles.listHeader)}>
            <div>
              <button className={classNames('btn btn-primary', Styles.trackRequestLink)} onClick={() => window.open(Envs.ALICE_URL)}>
                <i className="icon mbc-icon new-tab"></i> Track your requests
              </button>
            </div>
            <div>
              <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={getWorkspaces}>
                <i className="icon mbc-icon refresh"></i>
              </button>
            </div>
            <span className={Styles.dividerLine}> &nbsp; </span>
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
        {workspaces?.length === 0 && 
          <div className={Styles.noProjectContainer}>
            <div className={Styles.messageContainer}>
              <p className={Styles.lead}>Hi <span>{user.firstName} {user.lastName}</span>, you don&apos;t have<br/>any Fabric workspace.</p>
              <p>You can create a new workspace or request one</p>
            </div>
            <div className={Styles.btnContainer}>
              <button className={'btn btn-tertiary'} onClick={() => setCreateWorkspace(true)}>
                <span>Create now</span>
              </button>
              <button className={'btn btn-tertiary'} onClick={() => setRequestWorkspace(true)}>
                <span>Request Access now</span>
              </button>
            </div>
          </div>
        }
        {listViewMode && (
          <>
            {workspaces && workspaces?.length ? (
              <div className={Styles.createNewArea}>
                <button className={'btn btn-secondary'} type="button" onClick={() => setCreateWorkspace(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new Fabric Workspace</span>
                </button>
                <button className={'btn btn-secondary'} type="button" onClick={() => setRequestWorkspace(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon document" />
                  </span>
                  <span>Request Fabric Workspace Access</span>
                </button>
              </div>
            ) : null}
          </>
        )}
        {workspaces?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <>
                <div className={classNames(Styles.projectsContainer)}>
                  <div className={Styles.createNewCard} onClick={() => setCreateWorkspace(true)}>
                    <div className={Styles.addicon}> &nbsp; </div>
                    <label className={Styles.addlabel}>Create new Fabric Workspace</label>
                  </div>
                  <div className={Styles.createNewCard} onClick={() => setRequestWorkspace(true)}>
                    <div className={Styles.requestIcon}><i className="icon mbc-icon document" /></div>
                    <label className={Styles.addlabel}>Request Fabric Workspace Access</label>
                  </div>
                  {ownWorkspaces.map((workspace) => 
                    <FabricWorkspaceCard
                      key={workspace.id}
                      user={user}
                      workspace={workspace}
                      onSelectWorkspace={(workspace) => { 
                        setSelectedWorkspace(workspace);
                        setShowStatusModal(true);}
                      }
                      onEditWorkspace={(workspace) => { 
                        setSelectedWorkspace(workspace);
                        setEditWorkspace(true);}
                      }
                      onDeleteWorkspace={(workspace) => {
                        setSelectedWorkspace(workspace);
                        setDeleteModal(true);
                      }}
                    />
                  )}
                  {requestedWorkspaces.length > 0 && <h2 className={Styles.sectionTitle}>Workspaces with Requested Access</h2> }
                  {requestedWorkspaces.map((workspace) => 
                    <FabricWorkspaceCard
                      key={workspace.id}
                      user={user}
                      workspace={workspace}
                      onSelectWorkspace={(workspace) => { 
                        setSelectedWorkspace(workspace);
                        setShowStatusModal(true);}
                      }
                      onEditWorkspace={(workspace) => { 
                        setSelectedWorkspace(workspace);
                        setEditWorkspace(true);}
                      }
                      onDeleteWorkspace={(workspace) => {
                        setSelectedWorkspace(workspace);
                        setDeleteModal(true);
                      }}
                    />
                  )}
                </div>
              </>
            }
            {listViewMode && 
              <div className={Styles.projectTable}>
                <div className={Styles.tableHeader}>
                  <div className={Styles.col1}>
                    <span>Name</span>
                  </div>
                  <div className={Styles.col2}>
                    <span>Workspace Link</span>
                  </div>
                  <div className={Styles.col3}>
                    <span>Created by</span>
                  </div>
                  <div className={Styles.col4}>
                    <span>Created on</span>
                  </div>
                  <div className={Styles.col5}>
                    <span>Data Classification</span>
                  </div>
                  <div className={Styles.col6}>
                    <span>Action</span>
                  </div>
                </div>
                {workspaces?.map((workspace) => 
                  <FabricWorkspaceRow
                    key={workspace.id}
                    user={user}
                    workspace={workspace}
                    onSelectWorkspace={(workspace) => { 
                      setSelectedWorkspace(workspace);
                      setShowStatusModal(true);}
                    }
                    onEditWorkspace={(workspace) => { 
                      setSelectedWorkspace(workspace);
                      setEditWorkspace(true);}
                    }
                    onDeleteWorkspace={(workspace) => {
                      setSelectedWorkspace(workspace);
                      setDeleteModal(true);
                    }}
                  />
                )}
              </div>
            }
            {workspaces?.length && (
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
      { createWorkspace &&
        <Modal
          title={'Create Fabric Workspace'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createWorkspace}
          content={<FabricWorkspaceForm user={user} edit={false} onSave={() => {setCreateWorkspace(false); getWorkspaces();}} />}
          scrollableContent={true}
          onCancel={() => setCreateWorkspace(false)}
        />
      }
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteWorkspaceContent}
        onCancel={() => setDeleteModal(false)}
        onAccept={deleteWorkspaceAccept}
      />
      { editWorkspace &&
        <Modal
          title={'Edit Fabric Workspace'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={editWorkspace}
          content={<FabricWorkspaceForm edit={true} workspace={selectedWorkspace} onSave={() => {setEditWorkspace(false); getWorkspaces(); }} />}
          scrollableContent={true}
          onCancel={() => setEditWorkspace(false)}
        />
      }
      { showStatusModal &&
        <Modal
          title={'Role Creation Status'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'80%'}
          buttonAlignment="right"
          show={showStatusModal}
          content={<RoleCreationModal workspace={selectedWorkspace} onClose={() => setShowStatusModal(false)} />}
          scrollableContent={true}
          onCancel={() => setShowStatusModal(false)}
        />
      }
      { requestWorkspace &&
        <Modal
          title={'Request Fabric Workspace Access'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={requestWorkspace}
          content={<RequestWorkspace onRefresh={() => { setRequestWorkspace(false); getWorkspaces()}} />}
          scrollableContent={true}
          onCancel={() => setRequestWorkspace(false)}
        />
      }
    </>
  );
};
export default FabricWorkspaces;
