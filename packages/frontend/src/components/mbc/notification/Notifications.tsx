import cn from 'classnames';
import React, { useEffect, useState, useContext } from 'react';
import { withRouter } from 'react-router-dom';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { IChangeLogData, IUserInfo } from 'globals/types';
import Styles from './Notifications.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

const classNames = cn.bind(Styles);

// import { ApiClient } from '../../../services/ApiClient';
import { NotificationApiClient } from '../../../services/NotificationApiClient';
import { history } from '../../../router/History';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import Pagination from '../pagination/Pagination';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { getQueryParameterByName } from '../../../services/Query';
import NotificationListItem from './notificationListItem/NotificationListItem';
import { IconGear } from 'components/icons/IconGear';
// import { INotificationDetails } from '../../../globals/types';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import AppContext from 'components/context/ApplicationContext';
import { markdownParser } from '../../../utils/MarkdownParser';

export interface INotificationProps {
  user: IUserInfo;
}

const Notifications = (props: any) => {
  Tooltip.defaultSetup();
  const [notificationsList, setNotificationsList] = useState([]);

  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );

  const [hideDrawer, setHideDrawer] = useState(true);
  const [checkAll, setCheckAll] = useState(false);
  const [checkAllWithException, setCheckAllWithException] = useState(false);
  const [selectedNotifications, setSelectedNotifications] = useState([]);
  // const [notificationRead, setNotificationRead] = useState(false);
  const [notificationIdsNotTobeDeleted, setNotificationIdsNotTobeDeleted] = useState([]);
  const [checkedAllCount, setCheckedAllCount] = useState(0);

  // const [searchTerm, setSearchTerm] = useState('');
  // const [canShowSearch] = useState(true);

  const [notificationDetails, setNotificationDetails] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const { setMessage } = useContext(AppContext);

  useEffect(() => {
    Tooltip.defaultSetup();
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberInit = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetInit = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    setCurrentPageNumber(currentPageNumberInit);
    setCurrentPageOffset(currentPageOffsetInit);
  }, []);

  useEffect(() => {
    getNotifications();
    SelectBox.defaultSetup();
  }, [currentPageOffset, maxItemsPerPage]);

  const getNotifications = () => {
    ProgressIndicator.show();
    NotificationApiClient.getNotifications(props.user.id, maxItemsPerPage, currentPageOffset)
      .then((response) => {
        const totalNumberOfPagesInner = Math.ceil(response.totalRecordCount / maxItemsPerPage);
        setNotificationsList(response.records);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesInner);
        ProgressIndicator.hide();
        history.replace({
          search: `?page=${currentPageNumber}`,
        });
      })
      .catch((error) => {
        showErrorNotification('Something went wrong!');
        ProgressIndicator.hide();
      });
  };

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetInner = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetInner);
  };

  const onPaginationNextClick = async () => {
    let currentPageNum = currentPageNumber;
    const currentPageOffsetInner = currentPageNum * maxItemsPerPage;
    setCurrentPageOffset(currentPageOffsetInner);
    currentPageNum = currentPageNum + 1;
    setCurrentPageNumber(currentPageNum);
  };

  const onViewByPageNum = (pageNum: number) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  // const onTypeChange = () => {};

  const toggleDrawer = () => {
    setHideDrawer(!hideDrawer);
  };

  const markNotificationAsRead = (notificationIds: any, showMessage = true) => {
    if (showMessage) {
      ProgressIndicator.show();
    }
    NotificationApiClient.markAsReadNotifications(notificationIds, props.user.id)
      .then((response) => {
        setMessage('UPDATE_NOTIFICATIONS');
        getNotifications();
        if (showMessage) {
          showNotification('Notification marked as viewed successfully.');
        }
      })
      .catch((err) => {
        if (showMessage) {
          showErrorNotification('Something went wrong');
          ProgressIndicator.hide();
        }
      });
  };

  const openDetails = (notificationDetails: any) => {
    setNotificationDetails(JSON.stringify(notificationDetails));
    setHideDrawer(false);
    if (!notificationDetails.isRead || notificationDetails.isRead === 'false') {
      markNotificationAsRead([notificationDetails.id], false);
    }
  };

  const selectNotification = (notificationId: any) => {
    const tempNotifId = notificationId.split('box-')[1];
    setSelectedNotifications((prevArray) => [...prevArray, tempNotifId]);
  };

  const deselectNotification = (notificationId: any) => {
    const tempNotifId = notificationId.split('box-')[1];
    setSelectedNotifications(selectedNotifications.filter((item) => item !== tempNotifId));
    setNotificationIdsNotTobeDeleted((prevArray) => [...prevArray, tempNotifId]);
  };

  const onChangeSelectAll = (event: React.FormEvent<HTMLInputElement>) => {
    if (event.currentTarget.checked) {
      setCheckedAllCount(1);
      setCheckAllWithException(true);
      setSelectedNotifications(
        notificationsList.filter((item) => item.eventType !== 'Announcement').map((item) => item.id),
      );
      setCheckAll(!checkAll);
    } else {
      setCheckedAllCount(0);
      setCheckAllWithException(false);
      setSelectedNotifications([]);
      setNotificationIdsNotTobeDeleted([]);
      setCheckAll(!checkAll);
    }
  };

  const removeSelected = () => {
    ProgressIndicator.show();
    NotificationApiClient.deleteNotifications(selectedNotifications, props.user.id)
      .then((response) => {
        setMessage('UPDATE_NOTIFICATIONS');
        setSelectedNotifications([]);
        setCheckAll(false);
        getNotifications();
        showNotification('Notification(s) deleted successfully.');
      })
      .catch((err) => {
        console.log('Something went wrong');
        showErrorNotification('Something went wrong');
      });
  };

  // const markAsRead = () => {
  //   setNotificationRead(false);
  //   console.log('============= Mark Read ==============');
  // };

  // const markAsUnread = () => {
  //   setNotificationRead(true);
  //   console.log('============= Mark Unread ==============');
  // };

  const unCheckAll = () => {
    setCheckedAllCount(0);
    setCheckAll(false);
  };

  // const onSearchIconButtonClick = () => {
  //   console.log('Clicked Search ++++++++++++++++++');
  // };

  // const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
  //   setSearchTerm(event.currentTarget.value);
  //   console.log('Change in search box $$$$$$$$$$$$');
  // };

  const showErrorNotification = (message: string) => {
    // ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  const showNotification = (message: string) => {
    // ProgressIndicator.hide();
    Notification.show(message);
  };

  const onInfoModalCancel = () => {
    setShowDeleteModal(false);
  };

  const onAccept = () => {
    onInfoModalCancel();
    removeSelected();
  };

  const openDeleteModal = () => {
    setShowDeleteModal(true);
  };

  const getParsedDate = (strDate: any) => {
    const date = new Date(strDate);
    let dd: any = date.getDate();
    let mm: any = date.getMonth() + 1; // January is 0!

    const yyyy = date.getFullYear();
    if (dd < 10) {
      dd = '0' + dd;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (dd + '.' + mm + '.' + yyyy).toString();
  };

  const getParsedTime = (strDate: any) => {
    const date = new Date(strDate);
    let hh: any = date.getHours();
    let mm: any = date.getMinutes(); // January is 0!

    if (hh < 10) {
      hh = '0' + hh;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (hh + ':' + mm).toString();
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>Notifications</h3>
          </div>
        </div>
        <div className={Styles.content}>
          <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
            <div>
              <div
                id="notificationTypeContainer"
                className={classNames('input-field-group', Styles.notificationTypeSelect)}
              >
                {/* <div id="notificationType" className="custom-select">
                  <select id="notificationTypeSelect" value={0} onChange={onTypeChange}>
                    <option id="notificationCategories1" key={0} value={0}>
                      Show all
                    </option>
                    {notificationCategories.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div> */}
              </div>
            </div>
            <div>
              <div className={Styles.searchPanel}>
                {/* <input
                  type="text"
                  className={classNames(Styles.searchInputField, canShowSearch ? '' : Styles.hide)}
                  ref={(searchInputLocal) => {
                    searchInputLocal;
                  }}
                  placeholder="Search Notifications"
                  onChange={onSearchInputChange}
                  maxLength={200}
                  value={searchTerm}
                />
                <button onClick={onSearchIconButtonClick}>
                  <i className={classNames('icon mbc-icon search', canShowSearch ? Styles.active : '')} />
                </button> */}
              </div>
            </div>
            <div>
              <IconGear></IconGear>
            </div>
            <div>
              <div className={Styles.removeBlock} onClick={openDeleteModal}>
                {checkAll || selectedNotifications.length > 0 ? (
                  <React.Fragment>
                    <i className={classNames('icon delete')} />
                    Remove selected
                  </React.Fragment>
                ) : (
                  ''
                )}
              </div>
              <div
                className={Styles.settingsBlock}
                onClick={() => {
                  history.push('/usersettings/');
                }}
              >
                {/* <i className={classNames('icon mbc-icon search')} /> */}
                Settings
              </div>
            </div>
          </div>

          <div className={Styles.notificationListWrapper}>
            <div className={Styles.listContent}>
              {notificationsList == null ? (
                <div className={Styles.notificationListEmpty}>Notifications are not available</div>
              ) : notificationsList.length == 0 ? (
                <div className={Styles.notificationListEmpty}>Notifications are not available</div>
              ) : (
                <React.Fragment>
                  <div className={Styles.notificationList}>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          <th>
                            <div>
                              <label className={classNames('checkbox', Styles.checkboxItem)}>
                                <span className={classNames('wrapper', Styles.thCheckbox)}>
                                  <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={checkAll}
                                    onChange={(event: React.FormEvent<HTMLInputElement>) => onChangeSelectAll(event)}
                                  />
                                </span>
                                <span className={classNames('label', Styles.checkboxLabel)}>Select all </span>
                              </label>
                            </div>
                            {/* <label className="sortable-column-header">Select all </label> */}
                          </th>
                          <th>
                            <label className="sortable-column-header desc">
                              {/* <i className="icon sort" /> */}
                              Event
                            </label>
                          </th>
                          <th>
                            <label className="sortable-column-header desc">
                              {/* <i className="icon sort" /> */}
                              Date / Time
                            </label>
                          </th>
                          <th> </th>
                        </tr>
                      </thead>
                      <tbody>
                        {notificationsList?.map((item, index) => {
                          return (
                            <NotificationListItem
                              item={item}
                              key={item.id}
                              openDetails={openDetails}
                              markNotificationAsRead={markNotificationAsRead}
                              checkedAll={checkAll}
                              checkAllWithException={checkAllWithException}
                              selectedNotifications={selectedNotifications}
                              selectNotification={selectNotification}
                              deselectNotification={deselectNotification}
                              unCheckAll={unCheckAll}
                              notificationIdsNotTobeDeleted={notificationIdsNotTobeDeleted}
                              checkedAllCount={checkedAllCount}
                              currentItemNotToBeDeleted={notificationIdsNotTobeDeleted.includes(item.id)}
                            />
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </React.Fragment>
              )}
            </div>

            <div className={classNames(Styles.slider, hideDrawer ? Styles.close : '')}>
              <div className={Styles.panelBorder} onClick={toggleDrawer}>
                <div></div>
              </div>
              <div className={Styles.contentWrapper}>
                <div className={Styles.contentHeader}>
                  <span className={Styles.detailsNotificationType}>
                    <i className="icon mbc-icon notification" />
                    {notificationDetails ? JSON.parse(notificationDetails).eventType : ''}
                  </span>
                  {notificationDetails ? (
                    JSON.parse(notificationDetails).eventType === 'Solution Updated' ? (
                      <a
                        className={Styles.goToSolution}
                        onClick={() => {
                          history.push('/summary/' + JSON.parse(notificationDetails).resourceId);
                        }}
                      >
                        Go To Solution
                      </a>
                    ) : (
                      ''
                    )
                  ) : (
                    ''
                  )}
                  {notificationDetails ? (
                    JSON.parse(notificationDetails).eventType === 'Storage - Bucket Creation' ? (
                      <a
                        className={Styles.goToSolution}
                        onClick={() => {
                          history.push('/storage/explorer/' + JSON.parse(notificationDetails).resourceId);
                        }}
                      >
                        Go To My Storage
                      </a>
                    ) : (
                      ''
                    )
                  ) : (
                    ''
                  )}
                  <i className={classNames('icon mbc-icon close thin', Styles.closeDrawer)} onClick={toggleDrawer} />
                  {/* {notificationRead ? (
                    <span className={Styles.detailsMarkAsRead} onClick={() => markAsRead()}>
                      <i className={'icon mbc-icon visibility-show'} />
                      &nbsp; Mark as read
                    </span>
                  ) : (
                    <span className={Styles.detailsMarkAsRead} onClick={() => markAsUnread()}>
                      <i className={'icon mbc-icon visibility-hide'} /> 
                      &nbsp; Mark as unread
                    </span>
                  )} */}
                </div>
                <div className={Styles.notificationTitle}>
                  <p
                    dangerouslySetInnerHTML={{
                      __html: notificationDetails
                        ? JSON.parse(notificationDetails).message
                          ? markdownParser(JSON.parse(notificationDetails).message)
                          : ''
                        : '',
                    }}
                  />
                </div>
                <div className={Styles.notificationContent}>
                  {/* <p>Hey John Doe,</p> */}

                  <p
                    dangerouslySetInnerHTML={{
                      __html: notificationDetails
                        ? JSON.parse(notificationDetails).messageDetails
                          ? markdownParser(JSON.parse(notificationDetails).messageDetails)
                          : ''
                        : '',
                    }}
                  />

                  {notificationDetails ? (
                    (JSON.parse(notificationDetails).eventType === 'Solution Updated' || 
                    JSON.parse(notificationDetails).eventType.includes('Dashboard-Report Update')) ? (
                      JSON.parse(notificationDetails)?.changeLogs ? (
                        <ul>
                          {JSON.parse(notificationDetails)?.changeLogs?.map((data: IChangeLogData, index: number) => {
                            return (
                              <li key={index}>
                                {data.changeDescription} at {getParsedDate(data.changeDate)} /{' '}
                                {getParsedTime(data.changeDate)}, by {data.modifiedBy.firstName}&nbsp;
                                {data.modifiedBy.lastName}
                              </li>
                            );
                          })}
                        </ul>
                      ) : (
                        <div className={Styles.noChangeLogs}>Change logs are not available!</div>
                      )
                    ) : (
                      ''
                    )
                  ) : (
                    ''
                  )}
                </div>
                {/* <div className={Styles.btnConatiner}>
                  <button className="btn btn-primary" type="button">
                    Go to provision
                  </button>
                </div> */}
              </div>
            </div>
          </div>
        </div>
        {notificationsList.length ? (
          <Pagination
            totalPages={totalNumberOfPages}
            pageNumber={currentPageNumber}
            onPreviousClick={onPaginationPreviousClick}
            onNextClick={onPaginationNextClick}
            onViewByNumbers={onViewByPageNum}
            displayByPage={true}
          />
        ) : (
          ''
        )}
      </div>
      <ConfirmModal
        title={''}
        showAcceptButton={true}
        showCancelButton={true}
        acceptButtonTitle={'Confirm'}
        cancelButtonTitle={'Cancel'}
        show={showDeleteModal}
        removalConfirmation={true}
        content={
          <div className={Styles.deleteNotification}>
            {/* <div>Delete Notification(s)</div> */}
            <div className={classNames(Styles.removeConfirmationContent)}>
              Are you sure to delete selected notification(s)?
            </div>
          </div>
        }
        onCancel={onInfoModalCancel}
        onAccept={onAccept}
      />
    </React.Fragment>
  );
};
Notifications.contextType = AppContext;
export default withRouter(Notifications);
delete Notifications.contextType;
