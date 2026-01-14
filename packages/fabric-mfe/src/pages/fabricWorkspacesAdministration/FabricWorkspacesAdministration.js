import classNames from 'classnames';
import React, { useState, useEffect, useCallback } from 'react';
import Styles from './fabric-workspaces-administration.scss';
import { fabricApi} from '../../apis/fabric.api';
import { useDispatch } from 'react-redux';
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
import { debounce } from 'lodash';

const FabricWorkspacesAdministration = ({ user }) => {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getLovs());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [workspaces, setWorkspaces] = useState([]);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedWorkspace, setSelectedWorkspace] = useState({});
  const [editWorkspace, setEditWorkspace]  = useState(false);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15
  );

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
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(pageNumberOnQuery, 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    getWorkspaces();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

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

const getWorkspaces = (query = '') => {
  ProgressIndicator.show();

  fabricApi
    .getFabricWorkspacesForAdmin(
      currentPageOffset,
      maxItemsPerPage,
      query || ''
    )
    .then((res) => {
      if (res.status !== 204) {
        setWorkspaces(res?.data?.records);

        const totalNumberOfPagesTemp = Math.ceil(
          res.data.totalCount / maxItemsPerPage
        );

        setCurrentPageNumber(
          currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber
        );
        setTotalNumberOfPages(totalNumberOfPagesTemp);
      } else {
        setWorkspaces([]);
      }
      ProgressIndicator.hide();
    })
    .catch((e) => {
      ProgressIndicator.hide();
      Notification.show(
        e?.response?.data?.errors?.length
          ? e.response.data.errors[0].message
          : 'Fetching fabric workspaces failed!',
        'alert'
      );
    });
};

// eslint-disable-next-line react-hooks/exhaustive-deps
  const handleSearchInput = useCallback(
    debounce((value) => {
      const trimmedValue = value.trim();
      setSearchTerm(trimmedValue);
      if (trimmedValue.length > 0) {
        getWorkspaces(trimmedValue); 
      } else {
        getWorkspaces(); 
      }
    }, 500),
    [getWorkspaces]
  );

  useEffect(() => {
    return () => {
      handleSearchInput.cancel();
    };
  }, [handleSearchInput]);

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title="Fabric Workspaces Administration">
          <div className={classNames(Styles.listHeader)}>
            <span className={Styles.dividerLine}> &nbsp; </span>
            <div>
              <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={() => getWorkspaces('')}>
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
          <div className={Styles.searchContainer}>
            <div className="input-field-group search-field">
              <input
                type="text"
                id="workspaceSearch"
                className="input-field search"
                placeholder="Search workspaces..."
                maxLength={100}
                autoComplete="off"
                onChange={(e) => handleSearchInput(e.target.value)}
              />
            </div>
          </div>
          {searchTerm?.length > 0 && (
            <div className={Styles.cardsSeparator}>
              <h5 className="sub-title-text">Search Results</h5>
              <hr  />
            </div>
          )}
        </Caption>

        {workspaces?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <div className={classNames(Styles.projectsContainer)}>
                {workspaces.map((workspace) => 
                  <FabricWorkspaceCard
                    key={workspace.id}
                    user={user}
                    workspace={workspace}
                    onSelectWorkspace={(workspace) => { setSelectedWorkspace(workspace); setShowStatusModal(true); }}
                    onEditWorkspace={(workspace) => { setSelectedWorkspace(workspace); setEditWorkspace(true); }}
                    onDeleteWorkspace={(workspace) => { setSelectedWorkspace(workspace); setDeleteModal(true); }}
                  />
                )}
              </div>
            }

            {listViewMode && 
              <div className={Styles.projectTable}>
                <div className={Styles.tableHeader}>
                  <div className={Styles.col1}><span>Name</span></div>
                  <div className={Styles.col2}><span>Workspace Link</span></div>
                  <div className={Styles.col3}><span>Created by</span></div>
                  <div className={Styles.col4}><span>Created on</span></div>
                  <div className={Styles.col5}><span>Data Classification</span></div>
                  <div className={Styles.col6}><span>Action</span></div>
                </div>
                {workspaces.map((workspace) => 
                  <FabricWorkspaceRow
                    key={workspace.id}
                    user={user}
                    workspace={workspace}
                    onSelectWorkspace={(workspace) => { setSelectedWorkspace(workspace); setShowStatusModal(true); }}
                    onEditWorkspace={(workspace) => { setSelectedWorkspace(workspace); setEditWorkspace(true); }}
                    onDeleteWorkspace={(workspace) => { setSelectedWorkspace(workspace); setDeleteModal(true); }}
                  />
                )}
              </div>
            }

            {workspaces.length > 0 && (
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

      <ConfirmModal
        title=""
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton
        showCancelButton
        show={showDeleteModal}
        content={deleteWorkspaceContent}
        onCancel={() => setDeleteModal(false)}
        onAccept={deleteWorkspaceAccept}
      />

      {editWorkspace &&
        <Modal
          title="Edit Fabric Workspace"
          hiddenTitle
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="800px"
          buttonAlignment="right"
          show={editWorkspace}
          content={
            <FabricWorkspaceForm
              edit
              workspace={selectedWorkspace}
              user={user}
              onSave={() => { setEditWorkspace(false); getWorkspaces(); }}
            />
          }
          scrollableContent
          onCancel={() => setEditWorkspace(false)}
        />
      }

      {showStatusModal &&
        <Modal
          title="Role Creation Status"
          hiddenTitle
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="80%"
          buttonAlignment="right"
          show={showStatusModal}
          content={<RoleCreationModal workspace={selectedWorkspace} onClose={() => setShowStatusModal(false)} />}
          scrollableContent
          onCancel={() => setShowStatusModal(false)}
        />
      }
    </>
  );
};

export default FabricWorkspacesAdministration;
