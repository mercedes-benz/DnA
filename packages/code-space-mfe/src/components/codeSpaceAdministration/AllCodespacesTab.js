import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './all-codespaces-tab.scss';
import AddUser from 'dna-container/AddUser';
import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';

const getUserName = (user) => {
  const name = `${user?.firstName || ''} ${user?.lastName || ''}`.trim();
  return name || user?.id || '-';
};

const AllCodespacesTab = () => {
  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState([]);
  const [refreshCount, setRefreshCount] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [appliedSearchTerm, setAppliedSearchTerm] = useState('');
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [selectedProject, setSelectedProject] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [showTransferModal, setShowTransferModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);

  const getProjects = () => {
    ProgressIndicator.show();
    setLoading(true);
    const offset = (currentPageNumber - 1) * maxItemsPerPage;
    CodeSpaceApiClient.getResourceCapExemptions(offset, maxItemsPerPage, appliedSearchTerm)
      .then((res) => {
        const records = Array.isArray(res?.data?.data) ? res.data.data : [];
        const totalCount = res?.data?.totalCount || records.length;
        setProjects(records);
        setTotalNumberOfPages(Math.ceil(totalCount / maxItemsPerPage) || 1);
      })
      .catch((err) => {
        setProjects([]);
        Notification.show(err?.response?.data?.errors?.[0]?.message || err?.message || 'Something went wrong.', 'alert');
      })
      .finally(() => {
        setLoading(false);
        ProgressIndicator.hide();
      });
  };

  useEffect(() => {
    getProjects();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, appliedSearchTerm, refreshCount]);

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPageNumber(1);
    setAppliedSearchTerm(searchTerm.trim());
  };

  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setMaxItemsPerPage(pageNum);
  };

  const closeTransferModal = () => {
    setShowTransferModal(false);
    setShowConfirmModal(false);
    setSelectedProject(null);
    setSelectedUser(null);
  };

  const onTransferClick = (project) => {
    setSelectedProject(project);
    setSelectedUser(null);
    setShowTransferModal(true);
  };

  const getCollabarators = (collaborator) => {
    if (!collaborator?.shortId) {
      return;
    }
    setSelectedUser({
      firstName: collaborator.firstName,
      lastName: collaborator.lastName,
      id: collaborator.shortId,
      department: collaborator.department,
      email: collaborator.email,
      mobileNumber: collaborator.mobileNumber,
      gitUserName: collaborator.shortId,
      isAdmin: false,
      isApprover: false,
    });
  };

  const transferOwnership = () => {
    if (!selectedProject?.ownerWorkspaceId || !selectedUser?.id) {
      return;
    }
    setIsTransferring(true);
    ProgressIndicator.show();
    CodeSpaceApiClient.transferOwnership(selectedProject.ownerWorkspaceId, selectedUser)
      .then((res) => {
        if (res?.data?.success !== 'SUCCESS') {
          Notification.show(
            res?.data?.errors?.[0]?.message || 'Error in transferring ownership. Please try again later.',
            'alert',
          );
          return;
        }
        Notification.show(`Ownership of '${selectedProject.projectName}' transferred to ${getUserName(selectedUser)}.`);
        (res?.data?.warnings || []).forEach((warning) => {
          if (warning?.message) {
            Notification.show(warning.message, 'warning');
          }
        });
        closeTransferModal();
        setRefreshCount((count) => count + 1);
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error in transferring ownership. Please try again later.',
          'alert',
        );
      })
      .finally(() => {
        setIsTransferring(false);
        setShowConfirmModal(false);
        ProgressIndicator.hide();
      });
  };

  return (
    <>
      <div className={Styles.headerRow}>
        <p className={Styles.info}>
          Codespace admins can transfer the ownership of any codespace to any DnA user. A user who isn&apos;t a
          collaborator yet is added as collaborator first.
        </p>
        <form className={Styles.searchForm} onSubmit={onSearchSubmit}>
          <div className={classNames('input-field-group', Styles.searchField)}>
            <input
              type="text"
              className="input-field"
              value={searchTerm}
              placeholder="Search project"
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <button className="btn btn-primary" type="submit">
            Search
          </button>
        </form>
      </div>
      <div className={Styles.content}>
        {!loading && projects.length === 0 && <div className={Styles.csempty}>No codespace projects found.</div>}
        {!loading && projects.length > 0 && (
          <>
            <table className={classNames('ul-table')}>
              <thead>
                <tr className={classNames('header-row')}>
                  <th>
                    <label>Project Name</label>
                  </th>
                  <th>
                    <label>Owner</label>
                  </th>
                  <th className={Styles.countColumn}>
                    <label>Workspaces</label>
                  </th>
                  <th className={Styles.actionColumn}>
                    <label>Action</label>
                  </th>
                </tr>
              </thead>
              <tbody>
                {projects.map((project) => (
                  <tr key={project.projectName} className={classNames('data-row')}>
                    <td>{project.projectName}</td>
                    <td>{getUserName(project.projectOwner)}</td>
                    <td className={Styles.countColumn}>{project.workspaceCount}</td>
                    <td className={Styles.actionColumn}>
                      <button
                        type="button"
                        className="btn btn-primary"
                        disabled={!project.ownerWorkspaceId || isTransferring}
                        onClick={() => onTransferClick(project)}
                      >
                        Transfer Ownership
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination
              totalPages={totalNumberOfPages}
              pageNumber={currentPageNumber}
              onPreviousClick={() => setCurrentPageNumber(currentPageNumber - 1)}
              onNextClick={() => setCurrentPageNumber(currentPageNumber + 1)}
              onViewByNumbers={onViewByPageNum}
              displayByPage={true}
            />
          </>
        )}
      </div>
      <Modal
        title={`Transfer Ownership - ${selectedProject?.projectName || ''}`}
        showAcceptButton={false}
        showCancelButton={true}
        cancelButtonTitle="Cancel"
        modalWidth="60%"
        buttonAlignment="right"
        show={showTransferModal}
        content={
          <div className={Styles.transferModalContent}>
            <p>
              Current owner: <strong>{getUserName(selectedProject?.projectOwner)}</strong>
            </p>
            <div className={Styles.userPicker}>
              <AddUser getCollabarators={getCollabarators} dagId={''} isRequired={false} isUserprivilegeSearch={false} />
            </div>
            {selectedUser && (
              <div className={Styles.selectedUser}>
                Selected user: <strong>{getUserName(selectedUser)}</strong>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={!selectedProject?.ownerWorkspaceId || isTransferring}
                  onClick={() => setShowConfirmModal(true)}
                >
                  Transfer
                </button>
              </div>
            )}
          </div>
        }
        onCancel={closeTransferModal}
      />
      <ConfirmModal
        title=""
        acceptButtonTitle="Transfer"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showConfirmModal}
        content={
          <p>
            Transfer ownership of &apos;{selectedProject?.projectName}&apos; from {getUserName(selectedProject?.projectOwner)} to{' '}
            {getUserName(selectedUser)}?
          </p>
        }
        onCancel={() => setShowConfirmModal(false)}
        onAccept={transferOwnership}
      />
    </>
  );
};

export default AllCodespacesTab;
