import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './PromptCraftSubscriptions.scss';
import Caption from '../../shared/caption/Caption';
import Modal from '../../../formElements/modal/Modal';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';
import Pagination from '../../pagination/Pagination';
import { getQueryParameterByName } from '../../../../services/Query';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { Envs } from 'globals/Envs';
import PromptCraftSubscriptionCard from '../promptCraftSubscriptionCard/PromptCraftSubscriptionCard';
import PromptCraftSubscriptionRow from '../promptCraftSubscriptionRow/PromptCraftSubscriptionRow';
import PromptCraftSubscriptionForm from '../promptCraftSubscriptionForm/PromptCraftSubscriptionForm';
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';
import KeysModal from '../keysModal/KeysModal';
import { USER_ROLE } from 'globals/constants';

interface ISubscription {
  id: string;
  projectName: string;
  status: any;
}

const PromptCraftSubscriptions = ({ user }: any) => {
  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [subscriptions, setSubscriptions] = useState([]);
  const [createSubscription, setCreateSubscription] = useState(false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedSubscription, setSelectedSubscription] = useState<ISubscription | null>(null);
  const [showKeysModal, setShowKeysModal] = useState(false);

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
  const onViewByPageNum = (pageNum: number) => {
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
  }, []);

  useEffect(() => {
    console.log('user: ', user);
  }, []);

  useEffect(() => {
    getSubscriptions();
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const isAdmin = user.roles.find((role:any) => role.id === USER_ROLE.ADMIN) !== undefined;

  // delete subscription
  const deleteSubscriptionContent = (
    <div>
      <h3>Are you sure you want to delete {selectedSubscription?.projectName}? </h3>
      <h5>It will delete the subscription.</h5>
    </div>
  );

  const deleteSubscription = () => {
    ProgressIndicator.show();
    PromptCraftApiClient
      .deletePromptCraftSubscription(selectedSubscription?.id)
      .then(() => {
        getSubscriptions();
        Notification.show(`Prompt Craft subscription deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting Prompt Craft subscription. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const getSubscriptions = () => {      
      ProgressIndicator.show();
      PromptCraftApiClient
        .getPromptCraftSubscriptions(currentPageOffset, maxItemsPerPage)
        .then((res) => {
          if(res.status !== 204) {
            setSubscriptions(res?.data ? res?.data : []);
            const totalNumberOfPagesTemp = Math.ceil(res.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          } else {
            setSubscriptions([]);
          }
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e?.response?.data?.errors?.length
              ? e?.response?.data?.errors[0]?.message
              : 'Fetching prompt craft subscriptions failed!',
            'alert',
          );
        });
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title="Prompt Craft Subscriptions">
          <div className={classNames(Styles.listHeader)}>
            <div>
              <button
                className={classNames('btn btn-primary', Styles.howTo)}
                type="button"
                onClick={() => window.open(`${Envs.ONEAPI_PROMPT_CRAFT_USAGE_LINK}`)}
              >
                <i className={'icon mbc-icon info'} />
                <span>&nbsp;How to use?</span>
              </button>
            </div>
            <span className={Styles.dividerLine}> &nbsp; </span>
            <div>
              <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={getSubscriptions}>
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
                  sessionStorage.setItem('storageListViewModeEnable', 'true');
                }}
              >
                <i className="icon mbc-icon listview big" />
              </span>
            </div>
          </div>
        </Caption>
        {subscriptions?.length === 0 && 
          <div className={Styles.noAccounts}>
            <h5>You don&apos;t have any Prompt Craft subscriptions at this time.</h5>
            <p>Please subscribe to one.</p>
            { isAdmin ? 
              <button
                className={classNames('btn btn-tertiary')}
                type="button"
                onClick={() => setCreateSubscription(true)}
              >
                <span>Approve Prompt Craft Subscripton</span>
              </button> : 
              <button
                className={classNames('btn btn-tertiary')}
                type="button"
                onClick={() => window.open(`${Envs.ONEAPI_SUBSCRIPTION_URL}`)}
              >
                <span>Go to OneAPI</span>
              </button>
          }
          </div>
        }
        {listViewMode && (
          <>
            {subscriptions && subscriptions?.length && isAdmin ? (
              <div className={Styles.createNewArea}>
                <button className={'btn btn-secondary'} type="button" onClick={() => setCreateSubscription(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Approve Prompt Craft Subscripton</span>
                </button>
              </div>
            ) : null}
          </>
        )}
        {subscriptions?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode &&
              <>
                <div className={classNames(Styles.projectsContainer)}>
                  {isAdmin &&
                    <div className={Styles.createNewCard} onClick={() => setCreateSubscription(true)}>
                      <div className={Styles.addicon}> &nbsp; </div>
                      <label className={Styles.addlabel}>Approve Prompt Craft Subscripton</label>
                    </div>
                  }
                  {subscriptions.map((subscription) => 
                    <PromptCraftSubscriptionCard
                      key={subscription.id}
                      subscription={subscription}
                      onShowKeys={(subscription: any) => {
                        setSelectedSubscription(subscription);
                        setShowKeysModal(true);
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
                    <span>Project Name</span>
                  </div>
                  {/* <div className={Styles.col2}>
                    <span>Subscription Link</span>
                  </div> */}
                  <div className={Styles.col3}>
                    <span>Organization Name</span>
                  </div>
                  <div className={Styles.col4}>
                    <span>Created on</span>
                  </div>
                  <div className={Styles.col5}>
                    <span>Project Members</span>
                  </div>
                  <div className={Styles.col6}>
                    <span>Action</span>
                  </div>
                </div>
                {subscriptions?.map((subscription) => 
                  <PromptCraftSubscriptionRow
                    key={subscription.id}
                    subscription={subscription}
                    onShowKeys={(subscription: any) => {
                      setSelectedSubscription(subscription);
                      setShowKeysModal(true);
                    }}
                  />
                )}
              </div>
            }
            {subscriptions?.length && (
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
      { createSubscription &&
        <Modal
          title={'Approve Prompt Craft Subscripton'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createSubscription}
          content={<PromptCraftSubscriptionForm user={user} onSave={() => {setCreateSubscription(false); getSubscriptions();}} />}
          scrollableContent={true}
          onCancel={() => setCreateSubscription(false)}
        />
      }
      { showKeysModal &&
        <Modal
          title={'Subscription Keys'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showKeysModal}
          content={<KeysModal projectName={selectedSubscription?.projectName} onOk={() => {setShowKeysModal(false)}} />}
          scrollableContent={true}
          onCancel={() => setShowKeysModal(false)}
        />
      }
      { showDeleteModal &&
        <ConfirmModal
          title={''}
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={showDeleteModal}
          content={deleteSubscriptionContent}
          onCancel={() => setDeleteModal(false)}
          onAccept={deleteSubscription}
        />
      }
    </>
  );
};
export default PromptCraftSubscriptions;
