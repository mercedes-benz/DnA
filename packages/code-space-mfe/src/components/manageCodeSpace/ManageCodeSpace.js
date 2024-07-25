import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './ManageCodeSpace.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
// import { ICodeCollaborator } from 'globals/types';
import CodeSpaceList from './CodeSpaceList';
import Pagination from 'dna-container/Pagination';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import { getQueryParameterByName } from 'dna-container/Query';
import Caption from 'dna-container/Caption';
import { Notification } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
// export interface IWorkSpaceConfigs {
//   id?: string;
//   projectName?: string;
//   projectOwner?: ICodeCollaborator;
//   securityConfig?: ISecurityConfig;
// }

// export interface ISecurityConfig {
//   entitlements?: [];
//   openSegments?: [];
//   projectName?: string;
//   requestedDate?: string;
//   roles?: [];
//   status?: string;
//   userRoleMappings?: [];
// }

// export interface ISortField {
//   name: string;
//   currentSortType: string;
//   nextSortType: string;
// }

// export interface InewRecipeField {
//   createdBy: ICodeCollaborator;
//   createdOn: string;
//   diskSpace: string;
//   maxCpu: string;
//   maxRam: string;
//   minCpu: string;
//   minRam: string;
//   oSName: string;
//   recipeName: string;
//   status: string;
//   recipeType: string;
//   repodetails: string;
//   software: string[];
//   isPublic: boolean;
// }

const ManageCodeSpace = () => {
  const classNames = cn.bind(Styles);
  const [loading, setLoading] = useState(true);
  //const [workSpaceConfigs, setWorkSpaceConfigs] = useState([]);
  const [newRecipes, setNewRecipes] = useState([]);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [sortBy, setSortBy] = useState({ name: 'requestedDate', currentSortType: 'desc', nextSortType: 'asc' });
  // const [currentTab, setCurrentTab] = useState('securityConfig');
  // const isConfigTab = currentTab === 'securityConfig' ? true : false;


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

  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    Tabs.defaultSetup();
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberInit = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetInit = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    setCurrentPageNumber(currentPageNumberInit);
    setCurrentPageOffset(currentPageOffsetInit);
  }, [currentPageNumber, maxItemsPerPage]);
  useEffect(() => {
    // if (currentTab === 'securityConfig') {
    //   getRequestedSecurityConfig();
    // } else {
    //   getRequestedNewCodeSpaces();
    // }
    getRequestedNewCodeSpaces();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPageOffset, maxItemsPerPage]);

  const showErrorNotification = (message) => {
    setLoading(false);
    Notification.show(message, 'alert');
  };

  // const getRequestedSecurityConfig = () => {
  //   setLoading(true);
  //   CodeSpaceApiClient.getWorkspaceConfigs()
  //     .then((res: any) => {
  //       setLoading(false);
  //       ProgressIndicator.hide();
  //       const totalNumberOfPagesInner = Math.ceil(res.totalCount / maxItemsPerPage);
  //       setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
  //       setTotalNumberOfPages(totalNumberOfPagesInner);
  //       setWorkSpaceConfigs(Array.isArray(res) ? res : (res.data as IWorkSpaceConfigs[]));
  //     })
  //     .catch((error: any) => {
  //       setLoading(false);
  //       showErrorNotification(error.message ? error.message : 'Some Error Occured');
  //     });
  // };

  const [additionalServices, setAdditionalServices] = useState([]);

  useEffect(() => {
    CodeSpaceApiClient.getAdditionalServicesLov()
      .then((response) => {
        setAdditionalServices(response.data.data);
      })
      .catch((err) => {
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);

  const getRequestedNewCodeSpaces = () => {
    setLoading(true);
    CodeSpaceApiClient.getCodeSpaceRecipes()
      .then((res) => {
        setLoading(false);
        ProgressIndicator.hide();
        const totalNumberOfPagesInner = Math.ceil(res.data.count / maxItemsPerPage);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesInner);
        setNewRecipes(Array.isArray(res.data) ? res.data : (res.data.data));
      })
      .catch((error) => {
        setLoading(false);
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const handleDataChange = () => {
    // if (isConfigTab) {
    //   getRequestedSecurityConfig();
    // } else {
    //   getRequestedNewCodeSpaces();
    // }
    getRequestedNewCodeSpaces();
  };

  const sortByColumn = (propName, sortOrder) => {
    if (!propName && !sortOrder) {
      propName = 'requestedDate';
      sortOrder = 'desc';
    }
    const newSortType = sortOrder === 'asc' ? 'desc' : 'asc';
    const newSortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: newSortType,
    };
    const convertToDateObj = (date) => {
      // console.log(date);
      const parts = date.split(', ');
      const dateParts = parts[0].split('/');
      const timeParts = parts[1].split(':');
      return new Date(dateParts[2], dateParts[1] - 1, dateParts[0], timeParts[0], timeParts[1], timeParts[2]);
    };
    // if (isConfigTab) {
    //   let data = workSpaceConfigs;
    //   if (propName === 'projectName') {
    //     data = data.sort((a, b) => {
    //       if (sortOrder === 'asc') {
    //         return a.projectName.toLowerCase().localeCompare(b.projectName.toLowerCase());
    //       } else {
    //         return b.projectName.toLowerCase().localeCompare(a.projectName.toLowerCase());
    //       }
    //     });
    //   } else if (propName === 'projectOwner') {
    //     data = data.sort((a, b) => {
    //       if (sortOrder === 'asc') {
    //         return a.projectOwner.firstName.toLowerCase().localeCompare(b.projectOwner.firstName.toLowerCase());
    //       } else {
    //         return b.projectOwner.firstName.toLowerCase().localeCompare(a.projectOwner.firstName.toLowerCase());
    //       }
    //     });
    //   } else if (propName === 'projectStatus') {
    //     data = data.sort((a, b) => {
    //       if (sortOrder === 'asc') {
    //         return a.securityConfig.status.toLowerCase().localeCompare(b.securityConfig.status.toLowerCase());
    //       } else {
    //         return b.securityConfig.status.toLowerCase().localeCompare(a.securityConfig.status.toLowerCase());
    //       }
    //     });
    //   } else if (propName === 'requestedDate') {
    //     data = data.sort((a, b) => {
    //       const dateA = convertToDateObj(
    //         regionalDateAndTimeConversionSolution(a.securityConfig.requestedDate),
    //       ).getTime();
    //       const dateB = convertToDateObj(
    //         regionalDateAndTimeConversionSolution(b.securityConfig.requestedDate),
    //       ).getTime();
    //       if (sortOrder === 'asc') {
    //         return dateA - dateB;
    //       } else {
    //         return dateB - dateA;
    //       }
    //     });
    //     setWorkSpaceConfigs(data);
    //   }
    // } else {
      let data = newRecipes;
      if (propName === 'recipeName') {
        data = data.sort((a, b) => {
          if (sortOrder === 'asc') {
            return a.recipeName.toLowerCase().localeCompare(b.recipeName.toLowerCase());
          } else {
            return b.recipeName.toLowerCase().localeCompare(a.recipeName.toLowerCase());
          }
        });
      } else if (propName === 'createdBy') {
        data = data.sort((a, b) => {
          if (sortOrder === 'asc') {
            return a.createdBy.firstName.toLowerCase().localeCompare(b.createdBy.firstName.toLowerCase());
          } else {
            return b.createdBy.firstName.toLowerCase().localeCompare(a.createdBy.firstName.toLowerCase());
          }
        });
      } else if (propName === 'projectStatus') {
        data = data.sort((a, b) => {
          if (sortOrder === 'asc') {
            return a.status.toLowerCase().localeCompare(b.status.toLowerCase());
          } else {
            return b.status.toLowerCase().localeCompare(a.status.toLowerCase());
          }
        });
      } else if (propName === 'createdOn') {
        console.log(data);
        data = data.sort((a, b) => {
          const dateA = convertToDateObj(regionalDateAndTimeConversionSolution(a.createdOn)).getTime();
          const dateB = convertToDateObj(regionalDateAndTimeConversionSolution(b.createdOn)).getTime();
          if (sortOrder === 'asc') {
            return dateA - dateB;
          } else {
            return dateB - dateA;
          }
        });
      }
      setNewRecipes(data);
    // }

    setSortBy(newSortField);
  };

 
  const recipeData = newRecipes?.map((recipe) => {
    return (
      <CodeSpaceList
        key={recipe.recipeName}
        id={recipe.recipeName}
        projectName={recipe.recipeName}
        maxRam={recipe.maxRam}
        maxCpu={recipe.maxCpu}
        diskSpace={recipe.diskSpace}
        onDataChanged={handleDataChange}
        software={recipe.software}
        additionalServices={additionalServices?.filter(service => recipe.additionalServices.includes(service.serviceName))}
        isConfigList={false}
        isPublic={recipe.isPublic}
      />
    );
  });

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <Caption title="Recipe Management" />
          {/* <div id="manage-codeSpace-tabs" className="tabs-panel"> */}
          {/* <div className="tabs-wrapper">
            <nav>
              <React.Fragment>
                <ul className="tabs">
                  <li className={'tab active'} onClick={() => setCurrentTab('securityConfig')}>
                    <a href="#tab-content-1" id="securityConfigs">
                      Security Configs
                    </a>
                  </li>
                  <li className={'tab'} onClick={() => setCurrentTab('newRecipe')}>
                    <a href="#tab-content-2" id="newRecipes">
                      Recipe Management
                    </a>
                  </li>
                </ul>
              </React.Fragment>
            </nav>
          </div> */}
          <div className="tabs-content-wrapper">
            {/* <div id="tab-content-1" className="tab-content">
              {currentTab === 'securityConfig' ? (
                <div className={Styles.content}>
                  {loading ? (
                    <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                      <div className="progress infinite" />
                    </div>
                  ) : workSpaceConfigs.length > 0 ? (
                    <div className={Styles.allCodeSpace}>
                      <div className={Styles.allcodeSpaceListviewContent}>
                        <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                          <thead>
                            <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                              <th onClick={() => sortByColumn('projectName', sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (sortBy.name === 'projectName' ? sortBy.currentSortType : '')
                                  }
                                >
                                  <i className="icon sort" />
                                  Project Name
                                </label>
                              </th>
                              <th onClick={() => sortByColumn('projectOwner', sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (sortBy.name === 'projectOwner' ? sortBy.currentSortType : '')
                                  }
                                >
                                  <i className="icon sort" />
                                  Project Owner
                                </label>
                              </th>
                              <th onClick={() => sortByColumn('projectStatus', sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (sortBy.name === 'projectStatus' ? sortBy.currentSortType : '')
                                  }
                                >
                                  <i className="icon sort" />
                                  Status
                                </label>
                              </th>
                              <th onClick={() => sortByColumn('requestedDate', sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (sortBy.name === 'requestedDate' ? sortBy.currentSortType : '')
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
                        <Pagination
                          totalPages={totalNumberOfPages}
                          pageNumber={currentPageNumber}
                          onPreviousClick={onPaginationPreviousClick}
                          onNextClick={onPaginationNextClick}
                          onViewByNumbers={onViewByPageNum}
                          displayByPage={true}
                        />
                    </div>
                  ) : (
                    <div className={Styles.noRequests}>
                      <h5>No Requests Found</h5>
                    </div>
                  )}
                </div>
              ) : (
                ''
              )}
            </div> */}
            <div id="tab-content-2" className="tab-content">
              {/* {currentTab === 'newRecipe' ? ( */}
                <div className={Styles.content}>
                  {loading ? (
                    <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                      <div className="progress infinite" />
                    </div>
                  ) : newRecipes !== null ? (
                    <div className={Styles.allCodeSpace}>
                      <div className={Styles.allcodeSpaceListviewContent}>
                        <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                          <thead>
                            <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                              <th className={Styles.softwareColumn} onClick={() => sortByColumn('', sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (sortBy.name === 'recipeName' ? sortBy.currentSortType : '')
                                  }
                                >
                                  <i className="icon sort" />
                                  Recipe Name
                                </label>
                              </th>
                              <th>
                                <label>
                                  Hardware Configuration
                                </label>
                              </th>
                              <th className={Styles.softwareColumn} >
                                <label>
                                  Software Configuration
                                </label>
                              </th>
                              <th className={Styles.softwareColumn} >
                                <label>
                                  Additional Services
                                </label>
                              </th>
                              <th className={Styles.ciColumn}>
                                <label>                            
                                  CI/CD Management
                                </label>
                              </th>
                              <th className={Styles.actionColumn}>
                                <label>Action</label>
                              </th>
                            </tr>
                          </thead>
                          <tbody>{recipeData}</tbody>
                        </table>
                      </div>
                        <Pagination
                          totalPages={totalNumberOfPages}
                          pageNumber={currentPageNumber}
                          onPreviousClick={onPaginationPreviousClick}
                          onNextClick={onPaginationNextClick}
                          onViewByNumbers={onViewByPageNum}
                          displayByPage={true}
                        />
                    </div>
                  ) : (
                    <div className={Styles.noRequests}>
                      <h5>No Requests Found</h5>
                    </div>
                  )}
                </div>
              {/* ) : (
                ''
              )} */}
            </div>
          </div>
        {/* </div> */}
      </div>
    </div>
  );
};
export default ManageCodeSpace;