import cn from 'classnames';
import * as React from 'react';
// import { createPortal } from 'react-dom';
import { CSVLink } from 'react-csv';
import { Data } from 'react-csv/components/CommonPropTypes';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { SESSION_STORAGE_KEYS, USER_ROLE } from 'globals/constants';

// @ts-ignore
import ImgDataikuIcon from '../../../assets/images/dataiku-icon.png';
// @ts-ignore
import ImgNotebookIcon from '../../../assets/images/notenook-icon.png';

import {
  IDivision,
  ILocation,
  IPhase,
  IProjectStatus,
  IProjectType,
  IRole,
  ISubDivisionSolution,
  ITag,
  IUserInfo,
  IAllReportsListItem,
  IReportFilterParams,
  IART,
  IDepartment,
  ITeams,
} from 'globals/types';
import { history } from '../../../router/History';
import Pagination from '../pagination/Pagination';

import Styles from './AllReports.scss';
import ReportListRowItem from './reportListRowItem/ReportListRowItem';

import { getQueryParameterByName } from '../../../services/Query';
import ConfirmModal from '../../formElements/modal/confirmModal/ConfirmModal';
import ReportCardItem from './reportCardItem/ReportCardItem';
import { getDivisionsQueryValue, trackEvent } from '../../../services/utils';
import { getDataForCSV } from '../../../services/ReportsCSV';

import filterStyle from '../filters/Filter.scss';
import { ReportsApiClient } from '../../../services/ReportsApiClient';
import ReportsFilter from '../filters/ReportsFilter';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Data-Layer-Landing.png';
import TagSection from '../shared/landingSummary/tagSection/TagSection';

const classNames = cn.bind(Styles);

export interface ISortField {
  name: string;
  currentSortType: string;
  nextSortType: string;
}

export interface IAllReportsState {
  phases: IPhase[];
  divisions: IDivision[];
  subDivisions: ISubDivisionSolution[];
  arts: IART[];
  departments: IDepartment[];
  processOwners: ITeams[];
  productOwners: ITeams[];
  reports: IAllReportsListItem[];
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
  queryParams: IReportFilterParams;
  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  totalNumberOfPages: number;
  currentPageOffset: number;
  showDeleteReportModal: boolean;
  reportToBeDeleted: string;
  allReportsFirstTimeDataLoaded: boolean;
  allReportsFilterApplied: boolean;
  userPreferenceDataId: string;
  tags: string[];
  csvData: any[];
  csvHeader: any[];
  clickedCsvDownload: boolean;
  flagForSubDivision: number;
  listViewMode: boolean;
  cardViewMode: boolean;
  showReportsFilter: boolean;
  openFilters: boolean;
  selectedTags: string[];
  selectedTagsToPass: string[];
}

interface IDropdownValues {
  arts: IART[];
  departments: IDepartment[];
  divisions: IDivision[];
  subDivisions: ISubDivisionSolution[];
  processOwners: ITeams[];
  productOwners: ITeams[];
}

export default class AllReports extends React.Component<
  { user: IUserInfo; match: any; location: { hash: string; pathname: string; search: string; state: object } },
  IAllReportsState
> {
  protected csvLink: any = React.createRef();
  // protected isTouch = false;
  constructor(props: any) {
    super(props);
    this.state = {
      phases: [],
      divisions: [],
      subDivisions: [],
      arts: [],
      departments: [],
      processOwners: [],
      productOwners: [],
      reports: [],
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
        agileReleaseTrains: [],
        division: [],
        subDivision: [],
        departments: [],
        productOwners: [],
        processOwners: [],
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
      showDeleteReportModal: false,
      reportToBeDeleted: null,
      allReportsFirstTimeDataLoaded: false,
      allReportsFilterApplied: false,
      tags: [],
      userPreferenceDataId: null,
      csvData: [],
      csvHeader: [],
      clickedCsvDownload: false,
      flagForSubDivision: 0,
      listViewMode: false,
      cardViewMode: true,
      showReportsFilter: false,
      openFilters: false,
      selectedTags: [],
      selectedTagsToPass: [],
    };
  }

  // public componentWillReceiveProps(nextProps: any) {
  //   if (!nextProps.location.search) {
  //     window.location.reload();
  //   }
  // }

  public componentDidMount() {
    if (sessionStorage.getItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE) == null) {
      this.setState({
        cardViewMode: true,
        listViewMode: false,
      });
    } else {
      this.setState({
        cardViewMode: true,
        listViewMode: false,
      });
    }
    ProgressIndicator.show();
    Tooltip.defaultSetup();

    const pageNumberOnQuery = getQueryParameterByName('page');
    const maxItemsPerPage = this.state.maxItemsPerPage;
    const currentPageNumber = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffset = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      if (window.location.href.indexOf('allreports') !== -1) {
        this.setState({ showReportsFilter: true });
      }
    });
  }

  protected openCloseFilter = () => {
    this.setState({
      openFilters: !this.state.openFilters,
    });
  };

  protected setSelectedFilter = (values: string[]) => {
    this.setState({ selectedTags: values, selectedTagsToPass: values });
  };

  public render() {
    const userInfo = this.props.user;
    const isSuperAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
    const isReportAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.REPORTADMIN);
    const { openFilterPanel } = this.state;

    const reportData = this.state.reports?.map((report) => {
      // const isProductOwner = report.members.reportOwners?.find(
      //   (teamMember: ITeams) => teamMember.shortId === userInfo.id,
      // )?.shortId;

      return (
        <ReportListRowItem
          key={report?.id}
          report={report}
          reportId={report?.reportId}
          bookmarked={false}
          canEdit={
            isReportAdmin !== undefined ||
            isSuperAdmin !== undefined ||
            // isProductOwner !== undefined ||
            userInfo.id === this.checkUserCanEditReport(userInfo, report)
          }
          onEdit={() => this.onEditReport(report?.reportId)}
          onDelete={() => this.onDeleteReport(report?.id)}
        />
      );
    });
    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Report</div>
        <div className={Styles.modalContent}>This report will be deleted permanently.</div>
      </div>
    );

    const reportCount = this.state.totalNumberOfRecords;

    const pageTitle = 'All Reports ('+ reportCount +')';

    const exportCSVIcon = () => {
      const element = (
        <span className={classNames(filterStyle.iconTrigger)} onClick={this.triggerDownloadCSVData}>
          <i tooltip-data="Export to CSV" className="icon download" />
        </span>
      );
      // const container = document?.querySelector('.triggerWrapper');
      // return container ? createPortal(element, container) : null;
      return element;
    };

    const getFilterDropdownValues = ({
      arts,
      departments,
      divisions,
      subDivisions,
      processOwners,
      productOwners,
    }: IDropdownValues) => this.setState({ arts, departments, divisions, subDivisions, processOwners, productOwners });

    return (
      <React.Fragment>
        <LandingSummary
          title={pageTitle}
          subTitle={'Full transparency about reports from various sources. Process of collection to be defined.'}
          tags={['Lorem Ipsum', 'ABC', 'XYZ']}
          headerImage={headerImageURL}
          isBackButton={true}
          isTagsFilter={false}
        >
          <div className={Styles.Workspaces}>
            <div
              className={classNames(
                Styles.mainPanel,
                Styles.hasRightPanel,
                openFilterPanel ? Styles.righPanelOpened : '',
                'mainPanel',
              )}
            >
              <div className={Styles.wrapper}>
                <div className={classNames(Styles.caption, Styles.filterSection)}>
                {(reportCount > 0 || this.state.allReportsFilterApplied) && (
                  <>
                    <div>
                      <TagSection
                        tags={this.state?.tagValues?.map((item) => item.name)}
                        selectedTags={this.state.selectedTags}
                        setSeletedTags={this.setSelectedFilter}
                      ></TagSection>
                    </div>
                    <div className={Styles.allSolExport}>
                      <CSVLink
                        data={this.state.csvData}
                        headers={this.state.csvHeader}
                        ref={(r: any) => (this.csvLink = r)}
                        filename={`Reports.csv`}
                        target="_blank"
                      />
                      <div className={Styles.reportsViewMode}>
                        <div tooltip-data="Card View">
                          <span
                            className={this.state.cardViewMode ? Styles.iconactive : ''}
                            onClick={this.setCardViewMode}
                          >
                            <i className="icon mbc-icon widgets" />
                          </span>
                        </div>
                        <span className={Styles.dividerLine}> &nbsp; </span>
                        <div tooltip-data="List View">
                          <span
                            className={this.state.listViewMode ? Styles.iconactive : ''}
                            onClick={this.setListViewMode}
                          >
                            <i className="icon mbc-icon listview big" />
                          </span>
                        </div>
                        <span className={Styles.dividerLine}> &nbsp; </span>
                        <div className="triggerWrapper">{exportCSVIcon()}</div>
                        <span className={Styles.dividerLine}> &nbsp; </span>
                        <div tooltip-data="Filters">
                          <span
                            className={this.state.openFilters ? Styles.activeFilters : ''}
                            onClick={this.openCloseFilter}
                          >
                            {this.state.allReportsFilterApplied && (<i className="active-status"/>)}
                            <i className="icon mbc-icon filter big" />
                          </span>
                        </div>
                      </div>
                    </div>
                  </>
                )}
                </div>

                <ReportsFilter
                  userId={this.props.user.id}
                  reportsDataLoaded={this.state.allReportsFirstTimeDataLoaded}
                  setReportsDataLoaded={(value: boolean) => this.setState({ allReportsFirstTimeDataLoaded: value })}
                  getFilterQueryParams={(queryParams: IReportFilterParams) => this.getFilteredReports(queryParams)}
                  getDropdownValues={getFilterDropdownValues}
                  openFilters={this.state.openFilters}
                  getAllTags={(tags: any) => {
                    this.setState({ tagValues: tags });
                  }}
                  getValuesFromFilter={(value: any) => {
                    this.setState({
                      tagFilterValues: value.tagFilterValues ? value.tagFilterValues : [],
                      selectedTags: value.tagFilterValues ? value.tagFilterValues.map((item: any) => item.name) : [],
                    });
                  }}
                  setSelectedTags={this.state.selectedTagsToPass}
                />

                <div className={Styles.allReportContent}>
                  {this.state.cardViewMode && (
                    <div className={classNames('cardSolutions', Styles.allReportCardViewContent)}>
                      {this.state.reports.length > 0 ? (
                        <div className={Styles.cardViewContainer} onClick={() => history.push('/createnewreport')}>
                          <div className={Styles.addicon}> &nbsp; </div>
                          <label className={Styles.addlabel}>Create new report</label>
                        </div>
                      ) : (
                        ''
                      )}
                      {this.state.reports?.map((report) => {
                        // const isProductOwner = report.members.reportOwners?.find(
                        //   (teamMember: ITeams) => teamMember.shortId === userInfo.id,
                        // )?.shortId;
                        return (
                          <ReportCardItem
                            key={report?.id}
                            report={report}
                            reportId={report?.id}
                            bookmarked={false}
                            canEdit={
                              isReportAdmin !== undefined ||
                              isSuperAdmin !== undefined ||
                              // isProductOwner !== undefined ||
                              userInfo.id === this.checkUserCanEditReport(userInfo, report)
                            }
                            onEdit={() => this.onEditReport(report?.reportId)}
                            onDelete={() => this.onDeleteReport(report?.id)}
                          />
                        );
                      })}
                    </div>
                  )}
                  {this.state.listViewMode && (
                    <div className={Styles.allReportListViewContent}>
                      <table
                        className={classNames(
                          'ul-table reports',
                          Styles.reportsMarginNone,
                          this.state.reports?.length === 0 ? 'hide' : '',
                        )}
                      >
                        <thead>
                          <tr className="header-row">
                            <th onClick={this.sortReports.bind(null, 'productName', this.state.sortBy.nextSortType)}>
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
                            <th
                              style={{ minWidth: '115px' }}
                              onClick={this.sortReports.bind(null, 'department', this.state.sortBy.nextSortType)}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.sortBy.name === 'department' ? this.state.sortBy.currentSortType : '')
                                }
                              >
                                <i className="icon sort" />
                                E2-Department
                              </label>
                            </th>
                            {/* <th onClick={this.sortReports.bind(null, 'productOwner', this.state.sortBy.nextSortType)}>
                                <label
                                  className={
                                    'sortable-column-header ' +
                                    (this.state.sortBy.name === 'productOwner' ? this.state.sortBy.currentSortType : '')
                                  }
                                >
                                  <i className="icon sort" />
                                  Report Owner
                                </label>
                              </th> */}
                            <th onClick={this.sortReports.bind(null, 'art', this.state.sortBy.nextSortType)}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.sortBy.name === 'art' ? this.state.sortBy.currentSortType : '')
                                }
                              >
                                <i className="icon sort" />
                                ART
                              </label>
                            </th>
                            <th
                              style={{ minWidth: '90px' }}
                              onClick={this.sortReports.bind(null, 'status', this.state.sortBy.nextSortType)}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.sortBy.name === 'status' ? this.state.sortBy.currentSortType : '')
                                }
                              >
                                <i className="icon sort" />
                                Status
                              </label>
                            </th>
                            <th className="actionColumn">&nbsp;</th>
                          </tr>
                          <tr>
                            <th
                              colSpan={6}
                              className={classNames(Styles.listViewContainer)}
                              onClick={() => history.push('/createnewreport')}
                            >
                              <div className={Styles.addicon}> &nbsp; </div>
                              <label className={Styles.addlabel}>Create new report</label>
                            </th>
                          </tr>
                        </thead>
                        <tbody>{reportData}</tbody>
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
                  {this.state.reports?.length === 0 ? (
                    <div className={Styles.reportIsEmpty}>
                      <p>
                        {this.state.allReportsFilterApplied ? <>No report available for the fillter applied to the report list,</> : <>There is no report available,</>} please create report&nbsp;
                        <a
                          target="_blank"
                          className={Styles.linkStyle}
                          onClick={() => history.push('/createnewreport/')}
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
                  title="Delete Report"
                  acceptButtonTitle="Delete"
                  cancelButtonTitle="Cancel"
                  showAcceptButton={true}
                  showCancelButton={true}
                  show={this.state.showDeleteReportModal}
                  content={deleteModalContent}
                  onCancel={this.onCancellingDeleteChanges}
                  onAccept={this.onAcceptDeleteChanges}
                />
              </div>
              {/* {this.state.showReportsFilter && (
                  <>
                    <ReportsFilter
                      userId={this.props.user.id}
                      reportsDataLoaded={this.state.allReportsFirstTimeDataLoaded}
                      setReportsDataLoaded={(value: boolean) => this.setState({ allReportsFirstTimeDataLoaded: value })}
                      getFilterQueryParams={(queryParams: IReportFilterParams) => this.getFilteredReports(queryParams)}
                      getDropdownValues={getFilterDropdownValues}
                    />
                  </>
                )} */}
            </div>
          </div>
        </LandingSummary>
      </React.Fragment>
    );
  }
  protected setListViewMode = () => {
    this.setState(
      {
        listViewMode: true,
        cardViewMode: false,
      },
      () => {
        trackEvent('All Reports', 'Moved report list view to', 'List View');
        sessionStorage.setItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE, 'true');
      },
    );
  };
  protected setCardViewMode = () => {
    this.setState(
      {
        listViewMode: false,
        cardViewMode: true,
      },
      () => {
        trackEvent('All Reports', 'Moved report list view to', 'Card View');
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE);
      },
    );
  };

  protected triggerDownloadCSVData = () => {
    this.setState({ csvData: [], csvHeader: [] });
    ProgressIndicator.show();

    getDataForCSV(
      this.state.queryParams,
      this.state.arts?.length,
      this.state.departments?.length,
      this.state.processOwners?.length,
      this.state.productOwners?.length,
      this.state.sortBy.name,
      this.state.sortBy.currentSortType,
      (csvData: Data, csvHeader: Data) => {
        this.setState(
          {
            csvData,
            csvHeader,
          },
          () => {
            if (this.csvLink) {
              setTimeout(() => {
                trackEvent('All Reports', 'Download CSV', 'Downloaded reports list data as .csv exported file');
                this.csvLink.link.click();
              }, 0);
            }
            ProgressIndicator.hide();
          },
        );
      },
    );
  };

  protected sortReports = (propName: string, sortOrder: string) => {
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
        this.getReports();
      },
    );
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteReportModal: false, reportToBeDeleted: null });
  };

  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    ReportsApiClient.deleteReportById(this.state.reportToBeDeleted)
      .then((res) => {
        if (res) {
          this.getReports();
          this.showSuccess('Report deleted successfully');
          this.setState({ showDeleteReportModal: false });
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ showDeleteReportModal: false });
      });
  };

  protected onEditReport = (reportId: string) => {
    history.push('/editreport/' + reportId + '/true');
  };

  protected onDeleteReport = (reportId: string) => {
    this.setState({ showDeleteReportModal: true, reportToBeDeleted: reportId });
  };

  protected getFilteredReports = (queryParams: IReportFilterParams, getPublished?: boolean) => {
    ProgressIndicator.show();
    this.setState(
      {
        queryParams,
        currentPageOffset: 0,
        currentPageNumber: 1,
      },
      () => {
        this.getReports(getPublished);
      },
    );
  };

  protected getReports = (getPublished?: boolean) => {
    const queryParams = this.state.queryParams;
    const agileReleaseTrains = queryParams.agileReleaseTrains?.join(',');
    const processOwners = queryParams.processOwners?.join(',');
    const productOwners = queryParams.productOwners?.join(',');
    const departments = queryParams.departments?.join(',');
    const tags = queryParams.tag?.join(',');
    const divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);

    ReportsApiClient.getReportsByGraphQL(
      divisionIds,
      agileReleaseTrains,
      departments,
      processOwners,
      productOwners,
      tags,
      this.state.maxItemsPerPage,
      this.state.currentPageOffset,
      this.state.sortBy.name,
      this.state.sortBy.currentSortType,
      getPublished,
    )
      .then((res) => {
        if (res) {
          const reports = res.data?.reports as any;
          const { maxItemsPerPage, currentPageNumber } = this.state;
          const totalNumberOfPages = Math.ceil(reports?.totalCount / maxItemsPerPage);
          
          this.setState(
            {
              allReportsFilterApplied: this.IsFilterApplied(queryParams),
              reports: reports?.totalCount ? reports.records : [],
              totalNumberOfPages,
              totalNumberOfRecords: reports?.totalCount,
              allReportsFirstTimeDataLoaded: true,
              // tags: queryParams.tag,
              currentPageNumber: currentPageNumber > totalNumberOfPages ? 1 : currentPageNumber,
              flagForSubDivision: 0,
            },
            () => {
              const currentPageNumber = this.state.currentPageNumber;
              trackEvent('All Reports', 'View Reports', 'Viewed reports list of page ' + currentPageNumber);
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
            reports: [],
            totalNumberOfPages: 0,
            allReportsFirstTimeDataLoaded: true,
            flagForSubDivision: 0,
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  protected IsFilterApplied = (queryParams: IReportFilterParams) => {
    const { division, subDivision, agileReleaseTrains, processOwners, productOwners, departments, tag } = queryParams;
    let filterApplied = false;

    if (division.length && division.length !== this.state.divisions.length) {
      filterApplied = true;
    }

    if (subDivision.length && subDivision.length !== this.state.subDivisions.length) {
      filterApplied = true;
    }

    if (agileReleaseTrains.length && agileReleaseTrains.length !== this.state.arts.length) {
      filterApplied = true;
    }

    if (processOwners.length && processOwners.length !== this.state.processOwners.length) {
      filterApplied = true;
    }
    
    if (productOwners.length && productOwners.length !== this.state.productOwners.length) {
      filterApplied = true;
    }

    if (departments.length && departments.length !== this.state.departments.length) {
      filterApplied = true;
    }

    if (tag.length && tag.length !== this.state.tags.length) {
      filterApplied = true;
    }

    return filterApplied;
  }

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
      this.getReports();
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      ProgressIndicator.show();
      this.getReports();
    });
  };

  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      trackEvent('All Reports', 'Change Page View', 'Changed items per page count to ' + maxItemsPerPage);
      ProgressIndicator.show();
      this.getReports();
    });
  };

  protected checkUserCanEditReport(userInfo: IUserInfo, report: IAllReportsListItem) {
    let userId = '';
    if (report?.members.reportAdmins.find((teamMember) => teamMember.shortId === userInfo.id)) {
      userId = report?.members.reportAdmins.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
    } else if (userInfo?.divisionAdmins && userInfo?.divisionAdmins.includes(report?.description?.division?.name)) {
      userId = userInfo.id;
    }
    // else if (report.createdBy) {
    //   userId = report.createdBy.id;
    // }
    else {
      userId = '';
    }
    return userId;
  }
  protected goToSummary = (reportId: string) => {
    return () => {
      history.push('/reportsummary/' + reportId);
    };
  };
}
