import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './Notifications.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

const classNames = cn.bind(Styles);

import { ApiClient } from '../../../services/ApiClient';
import { history } from '../../../router/History';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { Pagination } from '../pagination/Pagination';
import SelectBox from '../../formElements/SelectBox/SelectBox';
import { SESSION_STORAGE_KEYS } from '../../../globals/constants';
import { getQueryParameterByName } from '../../../services/Query';
import NotificationListItem from './notificationListItem/NotificationListItem';
import { IconGear } from '../../icons/IconGear';

const Notifications = () => {
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
  const [notificationRead, setNotificationRead] = useState(false);
  const [notificationIdsNotTobeDeleted, setNotificationIdsNotTobeDeleted] = useState([]);
  const [checkedAllCount, setCheckedAllCount] = useState(0);

  const [searchTerm, setSearchTerm] = useState('');
  const [canShowSearch] = useState(true);

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
    ApiClient.getSolutionsByGraphQL(
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      maxItemsPerPage,
      currentPageOffset,
      'productName',
      'asc',
    )
      .then((response) => {
        const res = response.data.solutions;
        const totalNumberOfPagesInner = Math.ceil(res.totalCount / maxItemsPerPage);
        setNotificationsList(res.records);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesInner);
        ProgressIndicator.hide();
        history.replace({
          search: `?page=${currentPageNumber}`,
        });
      })
      .catch((err) => err);
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

  const onTypeChange = () => {};

  const toggleDrawer = () => {
    setHideDrawer(!hideDrawer);
  };

  const openDetails = (notificationDetails: any) => {
    setHideDrawer(false);
    // toggleDrawer();
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
      setSelectedNotifications(notificationsList.map((item) => item.id));
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
    console.log(selectedNotifications, '===========================');
  };

  const markAsRead = () => {
    setNotificationRead(false);
    console.log('============= Mark Read ==============');
  };

  const markAsUnread = () => {
    setNotificationRead(true);
    console.log('============= Mark Unread ==============');
  };

  const unCheckAll = () => {
    setCheckedAllCount(0);
    setCheckAll(false);
  };

  const onSearchIconButtonClick = () => {
    console.log('Clicked Search ++++++++++++++++++');
  };

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
    console.log('Change in search box $$$$$$$$$$$$');
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
                <div id="notificationType" className="custom-select">
                  <select id="notificationTypeSelect" value={0} onChange={onTypeChange}>
                    <option id="notificationCategories1" key={0} value={0}>
                      Show all
                    </option>
                    <option id="notificationCategories2" key={1} value={1}>
                      System Notification
                    </option>
                    {/* {notificationCategories.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                        {obj.name}
                      </option>
                    ))} */}
                  </select>
                </div>
              </div>
            </div>
            <div>
              <div className={Styles.searchPanel}>
                <input
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
                </button>
              </div>
            </div>
            <div>
              <IconGear></IconGear>
            </div>
            <div>
              <div className={Styles.removeBlock} onClick={removeSelected}>
                {checkAll || selectedNotifications.length > 0 ? (
                  <React.Fragment>
                    <i className={classNames('icon delete')} />
                    Remove selected
                  </React.Fragment>
                ) : (
                  ''
                )}
              </div>
              {/* <div className={Styles.settingsBlock}>
                <i className={classNames('icon mbc-icon search')} />
                Settings
              </div> */}
            </div>
          </div>

          <div className={Styles.notificationListWrapper}>
            <div className={Styles.listContent}>
              {notificationsList == null ? (
                <div className={Styles.notificationListEmpty}>Notifications not available</div>
              ) : (
                <React.Fragment>
                  <div className={Styles.notificationList}>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          <th>
                            <div>
                              <label className={classNames('checkbox', Styles.checkboxItem)}>
                                <span className="wrapper">
                                  <input
                                    type="checkbox"
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
                              <i className="icon sort" />
                              Category
                            </label>
                          </th>
                          <th>
                            <label className="sortable-column-header desc">
                              <i className="icon sort" />
                              Date / Time
                            </label>
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {notificationsList?.map((item, index) => {
                          return (
                            <NotificationListItem
                              item={item}
                              key={item.id}
                              openDetails={openDetails}
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
          </div>

          <div className={classNames(Styles.slider, hideDrawer ? Styles.close : '')}>
            <div className={Styles.panelBorder} onClick={toggleDrawer}>
              <div></div>
            </div>
            <div className={Styles.contentWrapper}>
              <div className={Styles.contentHeader}>
                <span className={Styles.detailsNotificationType}>
                  <i className="icon mbc-icon notification" />
                  System Notification
                </span>

                {notificationRead ? (
                  <span className={Styles.detailsMarkAsRead} onClick={() => markAsRead()}>
                    <i className={'icon mbc-icon visibility-show'} />
                    &nbsp; Mark as read
                  </span>
                ) : (
                  <span className={Styles.detailsMarkAsRead} onClick={() => markAsUnread()}>
                    <i className={'icon mbc-icon visibility-hide'} />
                    &nbsp; Mark as unread
                  </span>
                )}
              </div>
              <div className={Styles.notificationTitle}>
                <p>3 days left to provision your dummy solution</p>
              </div>
              <div className={Styles.notificationContent}>
                <p>Hey John Doe,</p>
                <p>
                  you have 3 days left to provision your dummy solution Solution Title and integrate it fully into the
                  DnA-App. Lorem Ipsum dolor sit amet selling point 01, selling point 02, selling point 03.
                </p>
              </div>
              {/* <div className={Styles.btnConatiner}>
                <button className="btn btn-primary" type="button">
                  Go to provision
                </button>
              </div> */}
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
    </React.Fragment>
  );
};

export default Notifications;
