import cn from 'classnames';
import * as React from 'react';
import { createPortal } from 'react-dom';
import { CSVLink } from 'react-csv';
import { Data } from 'react-csv/components/CommonPropTypes';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import SelectBox from '../../../components/formElements/SelectBox/SelectBox';
import { SESSION_STORAGE_KEYS, USER_ROLE } from '../../../globals/constants';

import {
  IAllSolutionsListItem,
  IAllSolutionsResult,
  IBookMarks,
  IDataVolume,
  IDivision,
  ILocation,
  IPhase,
  IProjectStatus,
  IProjectType,
  IRole,
  ISubDivisionSolution,
  ITag,
  IUserInfo,
  INotebookInfoSolutionId,
  IFilterParams,
} from '../../../globals/types';
import { history } from '../../../router/History';
import { ApiClient } from '../../../services/ApiClient';
import Pagination from '../pagination/Pagination';

import Styles from './AllSolutions.scss';
import SolutionListRowItem from './solutionListRowItem/SolutionListRowItem';

import { getQueryParameterByName } from '../../../services/Query';
import ConfirmModal from '../../formElements/modal/confirmModal/ConfirmModal';
import SolutionCardItem from './solutionCardItem/SolutionCardItem';
import { getDivisionsQueryValue, trackEvent } from '../../../services/utils';
import { getDataForCSV } from '../../../services/SolutionsCSV';

import SolutionsFilter from '../filters/SolutionsFilter';
import filterStyle from '../filters/Filter.scss';
import { getTranslatedLabel } from '../../../globals/i18n/TranslationsProvider';
// import {getDropDownData} from '../../../services/FetchMasterData';

const classNames = cn.bind(Styles);

export interface ISortField {
  name: string;
  currentSortType: string;
  nextSortType: string;
}

export interface IAllSolutionsState {
  phases: IPhase[];
  divisions: IDivision[];
  subDivisions: ISubDivisionSolution[];
  locations: ILocation[];
  projectStatuses: IProjectStatus[];
  projectTypes: IProjectType[];
  dataVolumes: IDataVolume[];
  solutions: IAllSolutionsListItem[];
  openFilterPanel: boolean;
  phaseFilterValues: IPhase[];
  divisionFilterValues: IDivision[];
  subDivisionFilterValues: IDivision[];
  locationFilterValues: ILocation[];
  statusFilterValue: IProjectStatus;
  typeFilterValue: IProjectType;
  tagFilterValues: ITag[];
  tagValues: ITag[];
  sortBy: ISortField;
  queryParams: IFilterParams;
  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  totalNumberOfPages: number;
  currentPageOffset: number;
  showDeleteSolutionModal: boolean;
  solutionToBeDeleted: string;
  enablePortfolioSolutionsView: boolean;
  allSolutiosFirstTimeDataLoaded: boolean;
  allSolutionsFilterApplied: boolean;
  userPreferenceDataId: string;
  tags: string[];
  csvData: any[];
  csvHeader: any[];
  clickedCsvDownload: boolean;
  flagForSubDivision: number;
  listViewMode: boolean;
  cardViewMode: boolean;
  noteBookData: INotebookInfoSolutionId;
  showSolutionsFilter: boolean;
}

export default class AllSolutions extends React.Component<
  { user: IUserInfo; match: any; location: { hash: string; pathname: string; search: string; state: object } },
  IAllSolutionsState
> {
  protected csvLink: any = React.createRef();
  // protected isTouch = false;
  constructor(props: any) {
    super(props);
    this.state = {
      phases: [],
      divisions: [],
      subDivisions: [],
      locations: [],
      projectStatuses: [],
      projectTypes: [
        { id: '1', name: 'My Bookmarks', routePath: 'bookmarks' },
        { id: '2', name: 'My Solutions', routePath: 'mysolutions' },
      ],
      dataVolumes: [],
      solutions: [],
      openFilterPanel: false,
      phaseFilterValues: [],
      divisionFilterValues: [],
      subDivisionFilterValues: [],
      locationFilterValues: [],
      statusFilterValue: null,
      typeFilterValue: null,
      tagFilterValues: [],
      tagValues: [],
      sortBy: {
        name: 'productName',
        currentSortType: 'asc',
        nextSortType: 'desc',
      },
      queryParams: {
        phase: [],
        location: [],
        division: [],
        subDivision: [],
        status: [],
        useCaseType: [],
        dataVolume: [],
        tag: [],
      },
      showContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
      totalNumberOfRecords: 0,
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      currentPageOffset: 0,
      showDeleteSolutionModal: false,
      solutionToBeDeleted: null,
      enablePortfolioSolutionsView: false,
      allSolutiosFirstTimeDataLoaded: false,
      allSolutionsFilterApplied: false,
      tags: [],
      userPreferenceDataId: null,
      csvData: [],
      csvHeader: [],
      clickedCsvDownload: false,
      flagForSubDivision: 0,
      listViewMode: false,
      cardViewMode: true,
      noteBookData: null,
      showSolutionsFilter: false,
    };
  }

  public componentWillReceiveProps(nextProps: any) {
    if (!nextProps.location.search) {
      window.location.reload();
    }
  }

  // public componentWillMount() {
  //   document.addEventListener('touchend', this.handleContextMenuOutside, true);
  //   document.addEventListener('click', this.handleContextMenuOutside, true);
  // }

  // public componentWillUnmount() {
  //   document.removeEventListener('touchend', this.handleContextMenuOutside, true);
  //   document.removeEventListener('click', this.handleContextMenuOutside, true);
  // }

  public componentDidMount() {
    if (sessionStorage.getItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE) == null) {
      this.setState({
        cardViewMode: true,
        listViewMode: false,
      });
    } else {
      this.setState({
        cardViewMode: false,
        listViewMode: true,
      });
    }
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    const enablePortfolioSolutionsView = window.location.href.indexOf('viewsolutions') !== -1;
    let { queryParams } = this.state;

    const pageNumberOnQuery = getQueryParameterByName('page');
    const maxItemsPerPage = this.state.maxItemsPerPage;
    const currentPageNumber = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffset = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    this.setState({ enablePortfolioSolutionsView, currentPageNumber, currentPageOffset }, () => {
      if (enablePortfolioSolutionsView) {
        const { kpi, value } = this.props.match.params;
        const portfolioFilterValues = JSON.parse(
          sessionStorage.getItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES),
        ) as IFilterParams;
        queryParams = portfolioFilterValues ? portfolioFilterValues : this.state.queryParams;
        if (queryParams.status.includes('0')) {
          queryParams.status = [];
        }
        if (queryParams.useCaseType.includes('0')) {
          queryParams.useCaseType = [];
        }
        queryParams.dataVolume = [];
        switch (kpi) {
          case 'phase':
            queryParams.phase = [value];
            ApiClient.get('phases')
              .then((res) => {
                this.setState({ phases: res });
              })
              .catch((error: Error) => {
                this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
              });
            break;
          case 'datavolume':
            queryParams.dataVolume = [value];
            ApiClient.get('datavolumes')
              .then((res) => {
                this.setState({ dataVolumes: res });
              })
              .catch((error: Error) => {
                this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
              });
            break;
          case 'location':
            queryParams.location = [value];
            ApiClient.get('locations')
              .then((res) => {
                this.setState({ locations: res });
              })
              .catch((error: Error) => {
                this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
              });
            break;
          default:
            break;
        }
        this.setState({ queryParams }, () => {
          // this.getSolutions(true);
        });
      } else if (window.location.href.indexOf('allsolutions') !== -1) {
        this.setState({ showSolutionsFilter: true });
      } else if (
        window.location.href.indexOf('bookmarks') !== -1 ||
        window.location.href.indexOf('mysolutions') !== -1
      ) {
        queryParams = this.state.queryParams;
        queryParams.useCaseType = window.location.href.indexOf('bookmarks') !== -1 ? ['1'] : ['2'];
        this.setState({ allSolutionsFilterApplied: false, queryParams }, () => {
          SelectBox.defaultSetup();
          // this.getSolutions();
        });
      } else {
        this.setState({ allSolutionsFilterApplied: false });
        SelectBox.defaultSetup();
        // this.getSolutions();
      }
    });
    ApiClient.getNotebooksDetails()
      .then((res) => {
        if (!Array.isArray(res)) {
          this.setState({
            noteBookData: res,
          });
        }
      })
      .catch((err) => {
        return err;
      });
    // this.getDropDownData();
  }

  public render() {
    const userInfo = this.props.user;
    const isAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
    const { openFilterPanel, enablePortfolioSolutionsView } = this.state;

    const solutionData = this.state.solutions.map((solution) => {
      return (
        <SolutionListRowItem
          key={solution.id}
          solution={solution}
          solutionId={solution.id}
          bookmarked={solution.bookmarked}
          canEdit={isAdmin !== undefined || userInfo.id === this.checkUserCanEditSolution(userInfo, solution)}
          onEdit={this.onEditSolution}
          onDelete={this.onDeleteSolution}
          updateBookmark={this.updateBookmark}
          showDigitalValue={enablePortfolioSolutionsView}
          noteBookData={this.state.noteBookData}
        />
      );
    });
    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Solution</div>
        <div className={Styles.modalContent}>This solution will be deleted permanently.</div>
      </div>
    );

    const pageTitle = this.getPageTitle(enablePortfolioSolutionsView);
    const hideFilterView =
      enablePortfolioSolutionsView ||
      window.location.href.indexOf('bookmarks') !== -1 ||
      window.location.href.indexOf('mysolutions') !== -1;

    const exportCSVIcon = () => {
      const element = hideFilterView ? (
        <div className={classNames(Styles.rightPanel, openFilterPanel ? 'expand' : '')}>
          <div className={Styles.triggerWrapper}>
            <span className={classNames(Styles.iconTrigger)} onClick={this.triggerDownloadCSVData}>
              <i tooltip-data="Export to CSV" className="icon download" />
            </span>
          </div>
        </div>
      ) : (
        <span className={classNames(filterStyle.iconTrigger)} onClick={this.triggerDownloadCSVData}>
          <i tooltip-data="Export to CSV" className="icon download" />
        </span>
      );
      const container = hideFilterView
        ? document?.querySelector('.mainPanel')
        : document?.querySelector('.triggerWrapper');
      Tooltip.defaultSetup();
      return container ? createPortal(element, container) : null;
    };

    return (
      <React.Fragment>
        {enablePortfolioSolutionsView ? (
          <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={this.goback}>
            Back
          </button>
        ) : (
          ''
        )}
        <div
          className={classNames(
            Styles.mainPanel,
            Styles.hasRightPanel,
            openFilterPanel ? Styles.righPanelOpened : '',
            'mainPanel',
          )}
        >
          <div className={Styles.wrapper}>
            <div className={Styles.caption}>
              <h3>{pageTitle}</h3>
              <div className={Styles.allSolExport}>
                {/* <div className={classNames(Styles.contextMenu, this.state.showContextMenu ? Styles.open : '')}>
                  <span
                    onClick={this.toggleContextMenu}
                    className={classNames('trigger hide', Styles.contextMenuTrigger)}
                  >
                    <i className="icon mbc-icon listItem context" tooltip-data="Export to CSV" />
                  </span>
                  <div
                    style={{
                      top: this.state.contextMenuOffsetTop + 'px',
                      right: this.state.contextMenuOffsetRight + 'px',
                    }}
                    className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}
                  >
                    <ul className="contextList">
                      <li className="contextListItem">
                        <span onClick={this.triggerDownloadCSVData}>Export to CSV</span>
                        <CSVLink
                          data={this.state.csvData}
                          headers={this.state.csvHeader}
                          ref={(r: any) => (this.csvLink = r)}
                          filename={`Solutions.csv`}
                          target="_blank"
                        />
                      </li>
                    </ul>
                  </div>
                </div> */}
                <CSVLink
                  data={this.state.csvData}
                  headers={this.state.csvHeader}
                  ref={(r: any) => (this.csvLink = r)}
                  filename={`Solutions.csv`}
                  target="_blank"
                />
                <div className={Styles.solutionsViewMode}>
                  <div tooltip-data="Card View">
                    <span
                      className={this.state.cardViewMode ? Styles.iconactive : ''}
                      onClick={this.solSetCardViewMode}
                    >
                      <i className="icon mbc-icon widgets" />
                    </span>
                  </div>
                  <span className={Styles.dividerLine}> &nbsp; </span>
                  <div tooltip-data="List View">
                    <span
                      className={this.state.listViewMode ? Styles.iconactive : ''}
                      onClick={this.solSetListViewMode}
                    >
                      <i className="icon mbc-icon listview big" />
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className={Styles.allsolutioncontent}>
              {this.state.cardViewMode && (
                <div className={classNames('cardSolutions', Styles.allsolutionCardviewContent)}>
                  {this.state.solutions.map((solution, index) => {
                    return (
                      <SolutionCardItem
                        key={index}
                        solution={solution}
                        solutionId={solution.id}
                        bookmarked={solution.bookmarked}
                        canEdit={
                          isAdmin !== undefined || userInfo.id === this.checkUserCanEditSolution(userInfo, solution)
                        }
                        onEdit={this.onEditSolution}
                        onDelete={this.onDeleteSolution}
                        updateBookmark={this.updateBookmark}
                        showDigitalValue={enablePortfolioSolutionsView}
                        noteBookData={this.state.noteBookData}
                      />
                    );
                  })}
                </div>
              )}
              {this.state.listViewMode && (
                <div className={Styles.allsolutionListviewContent}>
                  <table
                    className={classNames(
                      'ul-table solutions',
                      Styles.solutionsMarginnone,
                      this.state.solutions.length === 0 ? 'hide' : '',
                    )}
                  >
                    <thead>
                      <tr className="header-row">
                        <th onClick={this.sortSolutions.bind(null, 'productName', this.state.sortBy.nextSortType)}>
                          <label
                            className={
                              'sortable-column-header ' +
                              (this.state.sortBy.name === 'productName' ? this.state.sortBy.currentSortType : '')
                            }
                          >
                            <i className="icon sort" />
                            Name
                          </label>
                        </th>
                        <th>&nbsp;</th>
                        <th onClick={this.sortSolutions.bind(null, 'currentPhase', this.state.sortBy.nextSortType)}>
                          <label
                            className={
                              'sortable-column-header ' +
                              (this.state.sortBy.name === 'currentPhase' ? this.state.sortBy.currentSortType : '')
                            }
                          >
                            <i className="icon sort" />
                            Phase
                          </label>
                        </th>
                        <th onClick={this.sortSolutions.bind(null, 'division', this.state.sortBy.nextSortType)}>
                          <label
                            className={
                              'sortable-column-header ' +
                              (this.state.sortBy.name === 'division' ? this.state.sortBy.currentSortType : '')
                            }
                          >
                            <i className="icon sort" />
                            Division
                          </label>
                        </th>
                        {enablePortfolioSolutionsView ? (
                          <th onClick={this.sortSolutions.bind(null, 'digitalValue', this.state.sortBy.nextSortType)}>
                            <label
                              className={
                                'sortable-column-header ' +
                                (this.state.sortBy.name === 'digitalValue' ? this.state.sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Digital Value (€)
                            </label>
                          </th>
                        ) : (
                          <React.Fragment />
                        )}
                        <th onClick={this.sortSolutions.bind(null, 'locations', this.state.sortBy.nextSortType)}>
                          <label
                            className={
                              'sortable-column-header ' +
                              (this.state.sortBy.name === 'locations' ? this.state.sortBy.currentSortType : '')
                            }
                          >
                            <i className="icon sort" />
                            Location
                          </label>
                        </th>
                        <th onClick={this.sortSolutions.bind(null, 'projectStatus', this.state.sortBy.nextSortType)}>
                          <label
                            className={
                              'sortable-column-header ' +
                              (this.state.sortBy.name === 'projectStatus' ? this.state.sortBy.currentSortType : '')
                            }
                          >
                            <i className="icon sort" />
                            Status
                          </label>
                        </th>
                        <th className="actionColumn">&nbsp;</th>
                      </tr>
                    </thead>
                    <tbody>{solutionData}</tbody>
                  </table>
                </div>
              )}
              {this.state.totalNumberOfRecords ? (
                <Pagination
                  totalPages={this.state.totalNumberOfPages}
                  pageNumber={this.state.currentPageNumber}
                  onPreviousClick={this.onPaginationPreviousClick}
                  onNextClick={this.onPaginationNextClick}
                  onViewByNumbers={this.onViewByPageNum}
                  displayByPage={true}
                />
              ) : (
                ''
              )}
              {this.state.solutions.length === 0 ? (
                <div className={Styles.solutionIsEmpty}>
                  <p>
                    There is no solution available, please create solution&nbsp;
                    <a
                      target="_blank"
                      className={Styles.linkStyle}
                      onClick={() => history.push('/createnewsolution/')}
                      rel="noreferrer"
                    >
                      here.
                    </a>
                  </p>
                </div>
              ) : (
                ''
              )}
            </div>
            <ConfirmModal
              title="Delete Solution"
              acceptButtonTitle="Delete"
              cancelButtonTitle="Cancel"
              showAcceptButton={true}
              showCancelButton={true}
              show={this.state.showDeleteSolutionModal}
              content={deleteModalContent}
              onCancel={this.onCancellingDeleteChanges}
              onAccept={this.onAcceptDeleteChanges}
            />
          </div>          
          <SolutionsFilter
            userId={this.props.user.id}
            getFilterQueryParams={(queryParams: IFilterParams) => this.getFilteredSolutions(queryParams, this.state.showSolutionsFilter? false : true)}
            solutionsDataLoaded={this.state.allSolutiosFirstTimeDataLoaded}
            setSolutionsDataLoaded={(value: boolean) => this.setState({ allSolutiosFirstTimeDataLoaded: value })}
            showSolutionsFilter = {this.state.showSolutionsFilter}
            // getValuesFromFilter={(value: any) => {
            //   this.setState({ locations: value.locations ? value.locations : [] });
            //   this.setState({ phases: value.phases ? value.phases : [] });
            //   this.setState({ projectStatuses: value.projectStatuses ? value.projectStatuses : [] });
            //   this.setState({ projectTypes: value.projectTypes ? value.projectTypes : [] });
            // }}
          />
          {exportCSVIcon()}
        </div>
      </React.Fragment>
    );
  }
  protected solSetListViewMode = () => {
    this.setState(
      {
        listViewMode: true,
        cardViewMode: false,
      },
      () => {
        trackEvent(
          this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
          'Moved solution list view to',
          'List View',
        );
        sessionStorage.setItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE, 'true');
      },
    );
  };
  protected solSetCardViewMode = () => {
    this.setState(
      {
        listViewMode: false,
        cardViewMode: true,
      },
      () => {
        trackEvent(
          this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
          'Moved solution list view to',
          'Card View',
        );
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE);
      },
    );
  };

  protected triggerDownloadCSVData = () => {
    this.setState({ csvData: [], csvHeader: [] });
    ProgressIndicator.show();
    getDataForCSV(
      this.state.queryParams,
      this.state.locations.length,
      this.state.phases.length,
      this.state.projectStatuses.length,
      this.state.projectTypes.length,
      this.state.sortBy.name,
      this.state.sortBy.currentSortType,
      this.state.enablePortfolioSolutionsView,
      (csvData: Data, csvHeader: Data) => {
        this.setState(
          {
            csvData,
            csvHeader,
          },
          () => {
            if (this.csvLink) {
              setTimeout(() => {
                trackEvent(
                  this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
                  'Download CSV',
                  'Downloaded solutions list data as .csv exported file',
                );
                this.csvLink.link.click();
              }, 0);
            }
            ProgressIndicator.hide();
          },
        );
      },
    );
  };

  protected sortSolutions = (propName: string, sortOrder: string) => {
    const sortBy: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: this.state.sortBy.currentSortType,
    };
    ProgressIndicator.show();
    this.setState(
      {
        sortBy,
      },
      () => {
        this.getSolutions(this.state.enablePortfolioSolutionsView);
      },
    );
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteSolutionModal: false, solutionToBeDeleted: null });
  };

  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    ApiClient.deleteSolution(this.state.solutionToBeDeleted)
      .then((res) => {
        if (res) {
          this.getSolutions(this.state.enablePortfolioSolutionsView);
          this.showSuccess('Solution deleted successfully');
          this.setState({ showDeleteSolutionModal: false });
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ showDeleteSolutionModal: false });
      });
  };
  // protected toggleContextMenu = () => {
  //   this.setState({
  //     contextMenuOffsetTop: -13,
  //     contextMenuOffsetRight: 100,
  //     showContextMenu: !this.state.showContextMenu,
  //   });
  // };

  protected onEditSolution = (solutionId: string) => {
    history.push('/editSolution/' + solutionId + '/true');
  };

  protected onDeleteSolution = (solutionId: string) => {
    this.setState({ showDeleteSolutionModal: true, solutionToBeDeleted: solutionId });
  };

  protected updateBookmark = (solutionId: string, isRemove: boolean) => {
    const data: IBookMarks = {
      favoriteUsecases: [solutionId],
      id: this.props.user ? this.props.user.id : null,
      deleteBookmark: isRemove,
    };
    ProgressIndicator.show();
    ApiClient.bookMarkSolutions(data)
      .then((res) => {
        if (res) {
          const solutionsList = this.state.solutions;
          let solutionItemIndex = -1;
          const solutionItem = solutionsList.find((item: IAllSolutionsListItem, index: number) => {
            solutionItemIndex = index;
            return item.id === solutionId;
          });

          // For My Bookmarks type
          if (isRemove && this.state.queryParams.useCaseType && this.state.queryParams.useCaseType.includes('1')) {
            this.getSolutions(this.state.enablePortfolioSolutionsView);
            Notification.show('Removed from My Bookmarks');
          } else {
            solutionItem.bookmarked = !isRemove;
            solutionsList.splice(solutionItemIndex, 1);
            solutionsList.splice(solutionItemIndex, 0, solutionItem);

            this.setState({
              solutions: solutionsList,
            });
            ProgressIndicator.hide();
            this.showSuccess(isRemove ? 'Removed from My Bookmarks' : 'Added to My Bookmarks');
          }
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  protected getFilteredSolutions = (queryParams: IFilterParams, getPublished?: boolean) => {
    ProgressIndicator.show();
    const enablePortfolioSolutionsView = window.location.href.indexOf('viewsolutions') !== -1;

    if (enablePortfolioSolutionsView) {
      const { kpi, value } = this.props.match.params;
      if (queryParams.status.includes('0')) {
        queryParams.status = [];
      }
      if (queryParams.useCaseType.includes('0')) {
        queryParams.useCaseType = [];
      }
      queryParams.dataVolume = [];
      switch (kpi) {
        case 'phase':
          queryParams.phase = [value];
          ApiClient.get('phases')
            .then((res) => {
              this.setState({ phases: res });
            })
            .catch((error: Error) => {
              this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            });
          break;
        case 'datavolume':
          queryParams.dataVolume = [value];
          ApiClient.get('datavolumes')
            .then((res) => {
              this.setState({ dataVolumes: res });
            })
            .catch((error: Error) => {
              this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            });
          break;
        case 'location':
          queryParams.location = [value];
          ApiClient.get('locations')
            .then((res) => {
              this.setState({ locations: res });
            })
            .catch((error: Error) => {
              this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            });
          break;
        default:
          break;
      }
      this.setState({ queryParams,
        currentPageOffset: 0,
        currentPageNumber: 1, }, () => {
        this.getSolutions(true);
      });
    } 
    else if (window.location.href.indexOf('allsolutions') !== -1) {
      this.setState({ showSolutionsFilter: true,
        queryParams,
        currentPageOffset: 0,
        currentPageNumber: 1 }, () => {
        this.getSolutions();
      });
    } 
    else if (
      window.location.href.indexOf('bookmarks') !== -1 ||
      window.location.href.indexOf('mysolutions') !== -1
    ) {
      queryParams = this.state.queryParams;
      queryParams.useCaseType = window.location.href.indexOf('bookmarks') !== -1 ? ['1'] : ['2'];
      this.setState(
        {
          currentPageOffset: 0,
          currentPageNumber: 1,
          allSolutionsFilterApplied: false, 
          queryParams
        },
        () => {          
          SelectBox.defaultSetup();
          this.getSolutions();          
        },
      );
      
    } else {
      this.setState(
        {
          queryParams,
          currentPageOffset: 0,
          currentPageNumber: 1,
        },
        () => {
          this.getSolutions(getPublished);
        },
      );
      
    }
    
  };

  protected getSolutions = (getPublished?: boolean) => {
    const queryParams = this.state.queryParams;
    const locationIds = queryParams.location.join(',');
    const phaseIds = queryParams.phase.join(',');
    const divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);
    const status = queryParams.status.join(',');
    const useCaseType = queryParams.useCaseType.join(',');
    const dataVolumes = this.state.enablePortfolioSolutionsView ? queryParams.dataVolume ? queryParams.dataVolume.join(',') : '' : '';
    const tags = queryParams.tag.join(',');

    ApiClient.getSolutionsByGraphQL(
      locationIds,
      phaseIds,
      divisionIds,
      status,
      useCaseType,
      dataVolumes,
      tags,
      this.state.maxItemsPerPage,
      this.state.currentPageOffset,
      this.state.sortBy.name,
      this.state.sortBy.currentSortType,
      getPublished,
    )
      .then((res) => {
        if (res) {
          const solutions = res.data.solutions as IAllSolutionsResult;
          const { maxItemsPerPage, currentPageNumber } = this.state;
          const totalNumberOfPages = Math.ceil(solutions.totalCount / maxItemsPerPage);
          this.setState(
            {
              solutions: solutions.totalCount ? solutions.records : [],
              totalNumberOfPages,
              totalNumberOfRecords: solutions.totalCount,
              allSolutiosFirstTimeDataLoaded: true,
              tags: queryParams.tag,
              currentPageNumber: currentPageNumber > totalNumberOfPages ? 1 : currentPageNumber,
              flagForSubDivision: 0,
            },
            () => {
              const currentPageNumber = this.state.currentPageNumber;
              trackEvent(
                this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
                'View Solutions',
                'Viewed solutions list of page ' + currentPageNumber,
              );
              history.replace({
                search: `?page=${currentPageNumber}`,
              });
              ProgressIndicator.show();
              ProgressIndicator.hide();
            },
          );
        }
      })
      .catch((error) => {
        this.setState(
          {
            solutions: [],
            totalNumberOfPages: 0,
            allSolutiosFirstTimeDataLoaded: true,
            flagForSubDivision: 0,
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  protected getPageTitle = (enablePortfolioSolutionsView: boolean, forTracking?: boolean) => {
    let pageTitle = 'All Solutions';
    const solutionsCount = this.state.totalNumberOfRecords;
    if (enablePortfolioSolutionsView) {
      pageTitle = solutionsCount ? solutionsCount + ' Solutions' : '';
      const { kpi, value } = this.props.match.params;
      switch (kpi) {
        case 'phase':
          const phase = this.state.phases.find((item: IPhase) => item.id === value);
          pageTitle += phase ? ` in «${getTranslatedLabel(phase.name)}»` : '';
          break;
        case 'datavolume':
          const dataVolume = this.state.dataVolumes.find((item: IDataVolume) => item.id === value);
          pageTitle += dataVolume ? ` in «${dataVolume.name}»` : '';
          break;
        case 'location':
          const location = this.state.locations.find((item: ILocation) => item.id === value);
          pageTitle += location ? ` in «${location.name}»` : '';
          break;
        default:
          break;
      }

      if (forTracking) {
        pageTitle = 'View Solutions';
      }
    } else if (window.location.href.indexOf('allsolutions') === -1) {
      const typeFilterValue = this.state.projectTypes.find(
        (item) => item.id === this.state.queryParams.useCaseType.toString(),
      );
      pageTitle = (typeFilterValue ? typeFilterValue.name : 'Solutions') + ` (${solutionsCount})`;
    } else {
      pageTitle += ` (${solutionsCount})`;
    }

    return pageTitle;
  };

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected showSuccess(message: string) {
    //  ProgressIndicator.hide();
    Notification.show(message);
  }

  protected goback = () => {
    history.goBack();
  };

  protected onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      ProgressIndicator.show();
      this.getSolutions(this.state.enablePortfolioSolutionsView);
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      ProgressIndicator.show();
      this.getSolutions(this.state.enablePortfolioSolutionsView);
    });
  };

  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      trackEvent(
        this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
        'Change Page View',
        'Changed items per page count to ' + maxItemsPerPage,
      );
      ProgressIndicator.show();
      this.getSolutions(this.state.enablePortfolioSolutionsView);
    });
  };

  // protected handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
  //   if (event.type === 'touchend') {
  //     this.isTouch = true;
  //   }

  //   // Click event has been simulated by touchscreen browser.
  //   if (event.type === 'click' && this.isTouch === true) {
  //     return;
  //   }

  //   const target = event.target as Element;
  //   const { showContextMenu } = this.state;
  //   const elemClasses = target.classList;
  //   const contextMenuWrapper = document.querySelector('.contextMenuWrapper');
  //   if (
  //     !target.classList.contains('trigger') &&
  //     !target.classList.contains('context') &&
  //     !target.classList.contains('contextList') &&
  //     !target.classList.contains('contextListItem') &&
  //     contextMenuWrapper !== null &&
  //     contextMenuWrapper.contains(target) === false &&
  //     showContextMenu
  //   ) {
  //     this.setState({
  //       showContextMenu: false,
  //     });
  //   }

  //   if (
  //     showContextMenu &&
  //     (elemClasses.contains('contextList') ||
  //       elemClasses.contains('contextListItem') ||
  //       elemClasses.contains('contextMenuWrapper') ||
  //       elemClasses.contains('locationsText'))
  //   ) {
  //     event.stopPropagation();
  //   }
  // };

  protected checkUserCanEditSolution(userInfo: IUserInfo, solution: IAllSolutionsListItem) {
    let userId = '';
    if (solution.team.find((teamMember) => teamMember.shortId === userInfo.id)) {
      userId = solution.team.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
    } else if (solution?.createdBy?.id === userInfo.id) {
      userId = solution.createdBy.id;
    } else if (userInfo?.divisionAdmins && userInfo?.divisionAdmins.includes(solution?.division?.name)) {
      userId = userInfo.id;    
    } else {
      userId = '';
    }
    return userId;
  }

  protected goToSummary = (solutionId: string) => {
    return () => {
      history.push('/summary/' + solutionId);
    };
  };

  // protected getDropDownData = () => {
  //   getDropDownData(this.state.queryParams).then((res: any) => {
  //     this.setState(res);
  //   }).catch(err => {
  //     this.showErrorNotification(err);
  //   });
  // }
}
