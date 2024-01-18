import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './AdminSecurityConfig.scss';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { ICodeCollaborator } from 'globals/types';
import SecurityConfigList from './SecurityConfigList';
import Pagination from '../../pagination/Pagination';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { getQueryParameterByName } from '../../../../services/Query';
import Caption from '../../shared/caption/Caption';
import { Notification } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import { ProgressIndicator } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';

export interface IWorkSpaceConfigs {
  id?: string;
  projectName?: string;
  projectOwner?: ICodeCollaborator;
  securityConfig?: ISecurityConfig;
}

export interface ISecurityConfig {
  entitlements?: [];
  openSegments?: [];
  projectName?: string;
  requestedDate?: string;
  roles?: [];
  status?: string;
  userRoleMappings?: [];
}

export interface ISortField {
  name: string;
  currentSortType: string;
  nextSortType: string;
}

const AdminSecurityConfig = () => {
  const classNames = cn.bind(Styles);
  const [loading, setLoading] = useState<boolean>(true);
  const [workSpaceConfigs, setWorkSpaceConfigs] = useState<IWorkSpaceConfigs[]>([]);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [sortBy, setSortBy] = useState({ name: 'requestedDate', currentSortType: 'desc', nextSortType: 'asc' });

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

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberInit = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetInit = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    setCurrentPageNumber(currentPageNumberInit);
    setCurrentPageOffset(currentPageOffsetInit);
  }, []);
  useEffect(() => {
    getRequestedSecurityConfig();
  }, [currentPageOffset, maxItemsPerPage]);

  const showErrorNotification = (message: string) => {
    setLoading(false);
    Notification.show(message, 'alert');
  };

  const getRequestedSecurityConfig = () => {
    setLoading(true);
    CodeSpaceApiClient.getWorkspaceConfigs()
      .then((res: any) => {
        setLoading(false);
        ProgressIndicator.hide();
        const totalNumberOfPagesInner = Math.ceil(res.totalCount / maxItemsPerPage);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesInner);
        setWorkSpaceConfigs(Array.isArray(res) ? res : (res.data as IWorkSpaceConfigs[]));
      })
      .catch((error: any) => {
        setLoading(false);
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const handleDataChanged = () => {
    getRequestedSecurityConfig();
  };

  const sortByColumn = (propName: string, sortOrder: string) => {
    if (!propName && !sortOrder) {
      propName = 'requestedDate';
      sortOrder = 'desc';
    }
    const newSortType = sortOrder === 'asc' ? 'desc' : 'asc';
    const newSortField: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: newSortType,
    };
    let data = workSpaceConfigs;
    if (propName === 'projectName') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.projectName.toLowerCase().localeCompare(b.projectName.toLowerCase());
        } else {
          return b.projectName.toLowerCase().localeCompare(a.projectName.toLowerCase());
        }
      });
    } else if (propName === 'projectOwner') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.projectOwner.firstName.toLowerCase().localeCompare(b.projectOwner.firstName.toLowerCase());
        } else {
          return b.projectOwner.firstName.toLowerCase().localeCompare(a.projectOwner.firstName.toLowerCase());
        }
      });
    } else if (propName === 'projectStatus') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.securityConfig.status.toLowerCase().localeCompare(b.securityConfig.status.toLowerCase());
        } else {
          return b.securityConfig.status.toLowerCase().localeCompare(a.securityConfig.status.toLowerCase());
        }
      });
    } else if (propName === 'requestedDate') {
      data = data.sort((a, b) => {
        const dateA = new Date(a.securityConfig.requestedDate).getTime();
        const dateB = new Date(b.securityConfig.requestedDate).getTime();
        if (sortOrder === 'asc') {
          return dateA - dateB;
        } else {
          return dateB - dateA;
        }
      });
    }
    setWorkSpaceConfigs(data);
    setSortBy(newSortField);
  };
  const configData = workSpaceConfigs.map((config) => {
    return (
      <SecurityConfigList
        key={config.id}
        configId={config.id}
        projectName={config.projectName}
        projectOwner={config.projectOwner}
        projectStatus={config.securityConfig.status}
        requestedDate={config.securityConfig?.requestedDate}
        onDataChanged={handleDataChanged}
      />
    );
  });
  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <Caption title="Admin Security Config" />
        <div className={Styles.content}>
          {loading ? (
            <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
              <div className="progress infinite" />
            </div>
          ) : workSpaceConfigs.length > 0 ? (
            <div className={Styles.allConfigContent}>
              <div className={Styles.allconfigListviewContent}>
                <table className={classNames('ul-table solutions', Styles.configsMarginnone)}>
                  <thead>
                    <tr className={classNames('header-row', Styles.securityConfigRow)}>
                      <th onClick={() => sortByColumn('projectName', sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' + (sortBy.name === 'projectName' ? sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Project Name
                        </label>
                      </th>
                      <th onClick={() => sortByColumn('projectOwner', sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' + (sortBy.name === 'projectOwner' ? sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Project Owner
                        </label>
                      </th>
                      <th onClick={() => sortByColumn('projectStatus', sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' + (sortBy.name === 'projectStatus' ? sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Status
                        </label>
                      </th>
                      <th onClick={() => sortByColumn('requestedDate', sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' + (sortBy.name === 'requestedDate' ? sortBy.currentSortType : '')
                          }
                        >
                          <i className={'icon sort'} />
                          Requested Date
                        </label>
                      </th>
                      <th className="actionColumn">Action</th>
                    </tr>
                  </thead>
                  <tbody>{configData}</tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className={Styles.noRequests}>
              <h5>No Requests Found</h5>
            </div>
          )}
          {workSpaceConfigs.length ? (
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
      </div>
    </div>
  );
};
export default AdminSecurityConfig;
