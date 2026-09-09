import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './AzureKeyVault.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
const classNames = cn.bind(Styles);
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { ApiClient } from '../../../../src/services/ApiClient';
import CreateNewKeyVault from './createNewKeyVault/CreateNewKeyVault';
import Modal from '../../formElements/modal/Modal';
import { useHistory } from 'react-router-dom';
import { IKeyVault, IUserInfo } from 'globals/types';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import AzureKeyVaultCard from './AzureKeyVaultCard';
import Pagination from '../pagination/Pagination';

interface Props {
  user: IUserInfo;
}

const AzureKeyVault = ({ user }: Props) => {
  const [keyVaultList, setKeyVaultList] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedKeyVault, setSelectedKeyVault] = useState<IKeyVault | null>(null);

  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset((currentPageNum - 1) * maxItemsPerPage);
  };

  const onPaginationNextClick = () => {
    setCurrentPageOffset(currentPageNumber * maxItemsPerPage);
    setCurrentPageNumber(currentPageNumber + 1);
  };

  const onViewByPageNum = (pageNum: number) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  const History = useHistory();
  const goback = () => {
    History.goBack();
  };

  useEffect(() => {
    getKeyVaultList();
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const getKeyVaultList = () => {
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    ApiClient.getKeyVaults(currentPageOffset, maxItemsPerPage)
      .then((response) => {
        setKeyVaultList(response?.records || []);
        const totalPages = Math.ceil((response?.totalCount || 0) / maxItemsPerPage) || 1;
        setTotalNumberOfPages(totalPages);
        setCurrentPageNumber(currentPageNumber > totalPages ? 1 : currentPageNumber);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  };

  const isCreator = (keyVault: IKeyVault) =>
    !!user?.id && keyVault?.createdBy?.id?.toLowerCase() === user.id.toLowerCase();

  const onEditWorkspace = (keyVault : IKeyVault) => {
    setSelectedKeyVault(keyVault);
    setIsEditMode(true);
    setShowCreateModal(true);
  }

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
            Back
          </button>
          <div className={classNames(Styles.caption)}>
            <h3>My Azure Key Vaults</h3>
          </div>
          {!keyVaultList?.length ? (
            <div className={classNames(Styles.content)}>
              <div className={Styles.listContent}>
                <div className={Styles.emptyCodeSpaces}>
                  <span>
                    You don&apos;t have any key vaults at this time.
                    <br /> Please create a new one.
                  </span>
                </div>
                <div className={Styles.subscriptionListEmpty}>
                  <br />
                  <button className={'btn btn-tertiary'} type="button" onClick={() => { setShowCreateModal(true); setIsEditMode(false); setSelectedKeyVault(null);}}>
                    <span>Create new Key Vault</span>
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className={Styles.allProjectContent}>
              <div className={Styles.newProjectCard} onClick={() => { setShowCreateModal(true); setIsEditMode(false); setSelectedKeyVault(null);}}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create New Key Vault</label>
                </div>
              {keyVaultList?.map((project, index) => {
                return (
                  <AzureKeyVaultCard
                    key={index}
                    project={project}
                    canEdit={isCreator(project)}
                    onEditWorkspace={() => onEditWorkspace(project)}
                  />
                );
              })}
            </div>
          )}
          {keyVaultList?.length > 0 && (
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
      </div>
      {showCreateModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="70%"
          buttonAlignment="right"
          show={showCreateModal}
          content={
            <CreateNewKeyVault
              edit={isEditMode}
              project={selectedKeyVault}
              setShowCreateModal={() => setShowCreateModal(false)}
              getKeyVaultList={getKeyVaultList}
            />
          }
          scrollableContent={true}
          onCancel={() => { setShowCreateModal(false); getKeyVaultList(); }}
        />
      )}
    </React.Fragment>
  );
};

export default AzureKeyVault;
