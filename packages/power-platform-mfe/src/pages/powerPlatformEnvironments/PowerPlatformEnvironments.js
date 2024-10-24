import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './power-platform-environments.scss';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import PowerPlatformEnvironmentForm from '../../components/powerPlatformEnvironmentForm/PowerPlatformEnvironmentForm';
import PowerPlatformEnvironmentCard from '../../components/powerPlatformEnvironmentCard/PowerPlatformEnvironmentCard';
import PowerPlatformEnvironmentTable from '../../components/powerPlatformEnvironmentTable/PowerPlatformEnvironmentTable';
import SharedDevelopmentTou from '../../components/sharedDevelopmentTou/SharedDevelopmentTou';
import { powerPlatformApi } from '../../apis/power-platform.api';
import { useDispatch } from 'react-redux';
import { getLovs } from '../../redux/lovsSlice';
import { Envs } from '../../utilities/envs';
import ViewEnvironmentDetails from '../../components/viewEnvironmentDetails/ViewEnvironmentDetails';

const PowerPlatformEnvironments = ({user}) => {
  const dispatch = useDispatch();

  const [isSharedModal, setIsSharedModal] = useState(false);
  const [isTouModal, setIsTouModal] = useState(false);

  useEffect(() => {
    setIsSharedModal(localStorage.getItem('modal') === 'shared' ? true : false);
    setIsTouModal(localStorage.getItem('modal') === 'tou' ? true : false);
  }, []);

  useEffect(() => {
    dispatch(getLovs());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [environments, setEnvironments] = useState([]);
  const [orderAccount, setOrderAccount] = useState(isSharedModal);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [showDetailsModal, setShowDetailsModal]  = useState(false);
  const [showTou, setShowTou] = useState(isTouModal);

  useEffect(() => {
    setOrderAccount(isSharedModal);
  }, [isSharedModal]);

  useEffect(() => {
    setShowTou(isTouModal);
  }, [isTouModal]);

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
    getEnvironments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  // delete project
  const deleteEnvironmentContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.name}? </h3>
      <h5>It will delete the environment.</h5>
    </div>
  );

  const deleteEnvironmentClose = () => {
    setDeleteModal(false);
  };

  const deleteEnvironmentAccept = () => {
    // ProgressIndicator.show();
    // powerPlatformApi
    //   .deletePowerPlatformWorkspace(selectedItem.id)
    //   .then(() => {
    //     getEnvironments();
    //     Notification.show(`Power Platform Account ${selectedItem.name} deleted successfully.`);
    //   })
    //   .catch((e) => {
    //     Notification.show(
    //       e.response.data.errors?.lengthv
    //         ? e.response.data.errors[0].message
    //         : 'Error while deleting power platform account. Try again later!',
    //       'alert',
    //     );
    //     ProgressIndicator.hide();
    //   });
    setDeleteModal(false);
  };

  const getEnvironments = () => {      
      ProgressIndicator.show();
      powerPlatformApi
        .getPowerPlatformEnvironments(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          if(res?.status !== 204) {
            setEnvironments(res?.data?.records);
            const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          }
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
            <div className={Styles.btnContainer}>
              <button className="btn btn-primary" onClick={getEnvironments} tooltip-data="Refresh">
                <i className="icon mbc-icon refresh"></i>
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
        {environments?.length === 0 && 
          <div className={Styles.noAccounts}>
            <h5>You don&apos;t have any Power Platform Account at this time.</h5>
            <p>Please order one.</p>
            <button
              className={classNames('btn btn-tertiary')}
              type="button"
              onClick={() => window.open(`${Envs.CONTAINER_APP_URL}/#/toolDetails/powerPlatform`)}
            >
              <span>Click to Order</span>
            </button>
          </div>
        }
        {environments?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <div className={classNames(Styles.projectsContainer)}>
                {environments.map((environment) => 
                  <PowerPlatformEnvironmentCard
                    key={environment.id}
                    environment={environment}
                    onMoreInfoClick={(environment) => { 
                      setSelectedItem(environment);
                      setShowDetailsModal(true);}
                    }
                    onDeleteProject={(environment) => {
                      setSelectedItem(environment);
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
                    <span>State</span>
                  </div>
                  <div className={Styles.col3}>
                    <span>Requested On</span>
                  </div>
                  <div className={Styles.col4}>
                    <span>Environment Owner</span>
                  </div>
                </div>
                {environments?.map((environment) => 
                  <PowerPlatformEnvironmentTable
                    key={environment.id}
                    user={user}
                    environment={environment}
                    onMoreInfoClick={(environment) => { 
                      setSelectedItem(environment);
                      setShowDetailsModal(true);}
                    }
                    onDeleteProject={(environment) => {
                      setSelectedItem(environment);
                      setDeleteModal(true);
                    }}
                  />
                )}
              </div>
            }
            {environments?.length && (
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
      { orderAccount &&
        <Modal
          title={'Order Power Platform Account'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={orderAccount}
          content={<PowerPlatformEnvironmentForm user={user} onOrderAccount={() => { localStorage.setItem('modal', ''); setOrderAccount(false); getEnvironments() }} />}
          scrollableContent={true}
          onCancel={() => { localStorage.setItem('modal', ''); setOrderAccount(false) }}
        />
      }
      { showDetailsModal &&
        <Modal
          title={'Power Platform Account Details'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showDetailsModal}
          content={<ViewEnvironmentDetails environment={selectedItem} />}
          scrollableContent={true}
          onCancel={() => { setShowDetailsModal(false) }}
        />
      }
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteEnvironmentContent}
        onCancel={deleteEnvironmentClose}
        onAccept={deleteEnvironmentAccept}
      />
      { showTou &&
        <Modal
          title={'Terms of Use '}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showTou}
          content={<SharedDevelopmentTou hideAccept={true} isEn={true} />}
          scrollableContent={true}
          onCancel={() => {localStorage.setItem('modal', ''); setShowTou(false); window.location.href = `${Envs.CONTAINER_APP_URL}/#/toolDetails/powerPlatform`}}
        />
      }
    </>
  );
};
export default PowerPlatformEnvironments;
