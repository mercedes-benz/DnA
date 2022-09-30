import cn from 'classnames';
import * as React from 'react';
import { CSVLink } from 'react-csv';
import { Data } from 'react-csv/components/CommonPropTypes';
import { trackEvent } from '../../../services/utils';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { IAllSolutionsListItem, IAllSolutionsResult, IUserInfo } from 'globals/types';
import { history } from '../../../router/History';
import { ApiClient } from '../../../services/ApiClient';
import { getQueryParameterByName } from '../../../services/Query';
import Pagination from '../pagination/Pagination';
import SearchListRowItem from './searchListRowItem/SearchListRowItem';
import Styles from './SearchResults.scss';
import { getDataForCSV } from '../../../services/SolutionsCSV';

const classNames = cn.bind(Styles);

export interface ISortField {
  name: string;
  currentSortType: string;
  nextSortType: string;
}

export interface ISearchResultsState {
  solutions: IAllSolutionsListItem[];
  sortBy: ISortField;
  queryParam: string;
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  totalNumberOfPages: number;
  currentPageOffset: number;
  csvData: any[];
  csvHeader: any[];
}

export default class SearchResults extends React.Component<{ user: IUserInfo; match: any }, ISearchResultsState> {
  protected csvLink: any = React.createRef();
  constructor(props: any) {
    super(props);
    this.state = {
      solutions: [],
      sortBy: {
        name: 'name',
        currentSortType: 'desc',
        nextSortType: 'asc',
      },
      queryParam: '',
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
      totalNumberOfRecords: 0,
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      currentPageOffset: 0,
      csvData: [],
      csvHeader: [],
    };
  }

  public componentWillReceiveProps(nextProps: any) {
    if (!nextProps.location.search) {
      const queryParam = nextProps.match.params.query;
      this.setState({ queryParam }, () => {
        this.getSolutions();
      });
    }
  }

  public componentDidMount() {
    const queryParam = this.props.match.params.query;
    const pageNumberOnQuery = getQueryParameterByName('page');
    const maxItemsPerPage = this.state.maxItemsPerPage;
    const currentPageNumber = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffset = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    this.setState({ currentPageNumber, currentPageOffset, queryParam }, () => {
      this.getSolutions(queryParam);
    });
    Tooltip.defaultSetup();
  }

  public render() {
    const solutionData = this.state.solutions.map((solution) => {
      return <SearchListRowItem key={solution.id} solution={solution} />;
    });

    return (
      <div className={classNames(Styles.mainPanel, Styles.hasRightPanel)}>
        <div className={Styles.wrapper}>
          <h3>
            {this.state.totalNumberOfRecords} Search Results for '{this.state.queryParam}'
          </h3>
          <table className={classNames('ul-table solutions', this.state.totalNumberOfRecords === 0 ? 'hide' : '')}>
            <thead>
              <tr className="header-row">
                <th>
                  <label className="sortable-column-header no-pointer">Name</label>
                </th>
                <th>&nbsp;</th>
                <th>
                  <label className="sortable-column-header no-pointer">Phase</label>
                </th>
                <th>
                  <label className="sortable-column-header no-pointer">Division</label>
                </th>
                <th>
                  <label className="sortable-column-header no-pointer">Location</label>
                </th>
                <th>
                  <label className="sortable-column-header no-pointer">Status</label>
                </th>
              </tr>
            </thead>
            <tbody>{solutionData}</tbody>
          </table>
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
        </div>
        <div className={classNames(Styles.rightPanel)}>
          <div className={Styles.triggerWrapper}>
            <span className={classNames(Styles.iconTrigger)} onClick={this.triggerDownloadCSVData}>
              <i tooltip-data="Export to CSV" className="icon download" />
            </span>
          </div>
          <CSVLink
            data={this.state.csvData}
            headers={this.state.csvHeader}
            ref={(r: any) => (this.csvLink = r)}
            filename={`DnASolutionsSearchResults.csv`}
            target="_blank"
          />
        </div>
      </div>
    );
  }

  protected getSolutions = (query?: string) => {
    const queryParam = query || this.state.queryParam;
    ProgressIndicator.show();
    ApiClient.getSolutionsBySearchTerm(queryParam, this.state.maxItemsPerPage, this.state.currentPageOffset, true)
      .then((res) => {
        if (res) {
          const solutions = res.data.solutions as IAllSolutionsResult;
          let currentPageNumber = this.state.currentPageNumber;
          const totalNumberOfPages = Math.ceil(solutions.totalCount / this.state.maxItemsPerPage);
          currentPageNumber = currentPageNumber > totalNumberOfPages ? 1 : currentPageNumber;
          this.setState(
            {
              solutions: solutions.totalCount ? solutions.records : [],
              totalNumberOfPages,
              totalNumberOfRecords: solutions.totalCount,
              currentPageNumber,
            },
            () => {
              trackEvent(
                'Search Solutions',
                'Viewed Solution search result',
                'Page ' + currentPageNumber + ' results for search key ' + queryParam,
              );
              history.replace({
                search: `?page=${currentPageNumber}`,
              });
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
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  protected triggerDownloadCSVData = () => {
    const searchKey = this.state.queryParam;
    this.setState({ csvData: [], csvHeader: [] });
    ProgressIndicator.show();
    getDataForCSV(
      {
        phase: [],
        location: [],
        division: [],
        subDivision: [],
        status: [],
        useCaseType: [],
        dataVolume: [],
        tag: [],
      },
      0,
      0,
      0,
      0,
      this.state.sortBy.name,
      this.state.sortBy.currentSortType,
      false,
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
                  'Search Results for ' + searchKey,
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
      searchKey,
    );
  };

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getSolutions();
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getSolutions();
    });
  };

  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      trackEvent('Search Solutions', 'Change Page View', 'Changed items per page count to ' + maxItemsPerPage);
      this.getSolutions();
    });
  };
}
