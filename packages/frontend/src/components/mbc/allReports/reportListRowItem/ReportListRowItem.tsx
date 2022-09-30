import cn from 'classnames';
import * as React from 'react';
import { IAllReportsListItem, ITeams } from 'globals/types';
import { history } from '../../../../router/History';
import Styles from './ReportListRowItem.scss';
// import LogoImage from '../../createNewSolution/description/logoManager/LogoImage/LogoImage';
// import { SOLUTION_LOGO_IMAGE_TYPES } from '../../../../globals/constants';
// import { DataFormater } from '../../../../services/utils';

const classNames = cn.bind(Styles);

export interface IReportListRowItemProps {
  report: IAllReportsListItem;
  reportId: string;
  canEdit: boolean;
  bookmarked: boolean;
  onEdit: (reportId: string) => void;
  onDelete: (reportId: string) => void;
  updateBookmark?: (reportId: string, isRemove: boolean) => void;
}

export interface IReportListRowItemState {
  showContextMenu: boolean;
  showLocationsContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
}

export default class ReportListRowItem extends React.Component<IReportListRowItemProps, IReportListRowItemState> {
  protected isTouch = false;
  protected listRowElement: HTMLTableRowElement;

  constructor(props: any) {
    super(props);
    this.state = {
      showContextMenu: false,
      showLocationsContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
    };
  }

  public toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable: ClientRect = document.querySelector('table.reports').getBoundingClientRect();
    this.setState({
      contextMenuOffsetTop: elemRect.top - (relativeParentTable.top + 10),
      contextMenuOffsetRight: 10,
      showLocationsContextMenu: false,
      showContextMenu: !this.state.showContextMenu,
    });
  };

  public toggleLocationsContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable: ClientRect = document.querySelector('table.reports').getBoundingClientRect();
    this.setState({
      contextMenuOffsetTop: elemRect.top - relativeParentTable.top,
      contextMenuOffsetRight: elemRect.left - relativeParentTable.left - 200,
      showContextMenu: false,
      showLocationsContextMenu: !this.state.showLocationsContextMenu,
    });
  };

  public onEditReport = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onEdit(this.props.reportId);
      },
    );
  };
  public onDeleteReport = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onDelete(this.props.reportId);
      },
    );
  };
  public addToBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.updateBookmark(this.props.reportId, false);
      },
    );
  };

  public removeFromBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.updateBookmark(this.props.reportId, true);
      },
    );
  };

  public componentWillMount() {
    document.addEventListener('touchend', this.handleContextMenuOutside, true);
    document.addEventListener('click', this.handleContextMenuOutside, true);
  }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleContextMenuOutside, true);
    document.removeEventListener('click', this.handleContextMenuOutside, true);
  }

  public render() {
    const report = this.props.report;
    const { showContextMenu, showLocationsContextMenu } = this.state;
    return (
      <React.Fragment>
        <tr
          id={report?.id}
          key={report?.id}
          className={classNames(
            'data-row',
            Styles.reportRow,
            showContextMenu || showLocationsContextMenu ? Styles.contextOpened : null,
          )}
          ref={this.listRow}
          onClick={this.goToSummary}
        >
          <td className={'wrap-text ' + classNames(Styles.reportName)}>
            <div className={Styles.solIcon}>
              {/* <div className={Styles.solIconimg}>
                <LogoImage displayType={SOLUTION_LOGO_IMAGE_TYPES.THUMBNAIL} logoDetails={report.logoDetails} />
              </div>{' '} */}
              {report?.productName}
            </div>
          </td>
          <td className={Styles.draftIndicatorCol}>
            {!report?.publish ? <span className={Styles.draftIndicator}>DRAFT</span> : ''}
          </td>
          <td className="wrap-text">{report?.description.department || 'NA'}</td>
          <td>
            {report?.members.productOwners?.length
              ? report?.members.productOwners?.map((item: ITeams) => `${item.firstName} ${item.lastName}`)?.toString()
              : 'NA'}
          </td>
          <td>
            <div className={Styles.locationDataWrapper}>
              {report?.description.agileReleaseTrains?.[0] ? report?.description.agileReleaseTrains[0] : ''}
              {report?.description.agileReleaseTrains?.length > 1 ? (
                <div
                  className={classNames(
                    Styles.contextMenu,
                    Styles.locations,
                    this.state.showLocationsContextMenu ? Styles.open : '',
                  )}
                >
                  <span
                    onClick={this.toggleLocationsContextMenu}
                    className={classNames('trigger', Styles.contextMenuTrigger)}
                  >
                    <i className="icon mbc-icon listItem context elipse" />
                  </span>
                  <div
                    style={{
                      top: this.state.contextMenuOffsetTop + 'px',
                      left: this.state.contextMenuOffsetRight + 'px',
                      right: 'auto',
                    }}
                    className={classNames(
                      'contextMenuWrapper',
                      Styles.locations,
                      this.state.showLocationsContextMenu ? '' : 'hide',
                    )}
                  >
                    <ul className="contextList">
                      <li className="contextListItem">
                        <p className="locationsText">
                          {report?.description.agileReleaseTrains?.length
                            ? report?.description.agileReleaseTrains?.join(', ')
                            : ''}
                        </p>
                      </li>
                    </ul>
                  </div>
                </div>
              ) : null}
            </div>
          </td>
          <td
            className={
              report?.description.status
                ? report?.description.status === 'Live'
                  ? classNames(Styles.active)
                  : report?.description.status === 'Closed'
                  ? classNames(Styles.closed)
                  : ''
                : ''
            }
          >
            {report?.description.status}
          </td>
          <td>
            <div
              className={classNames(
                Styles.contextMenu,
                this.state.showContextMenu ? Styles.open : '',
                this.props.canEdit ? '' : 'hidden',
              )}
            >
              <span onClick={this.toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
                <i className="icon mbc-icon listItem context" />
              </span>
              <div
                style={{
                  top: this.state.contextMenuOffsetTop + 'px',
                  right: this.state.contextMenuOffsetRight + 'px',
                }}
                className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}
              >
                <ul className="contextList">
                  {/* {this.props.bookmarked ? (
                    <li className="contextListItem">
                      <span onClick={this.removeFromBookmarks}>Remove from My Bookmarks</span>
                    </li>
                  ) : (
                    <li className="contextListItem">
                      <span onClick={this.addToBookmarks}>Add to My Bookmarks</span>
                    </li>
                  )} */}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onEditReport}>Edit Report</span>
                    </li>
                  )}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onDeleteReport}>Delete Report</span>
                    </li>
                  )}
                </ul>
              </div>
            </div>
          </td>
        </tr>
      </React.Fragment>
    );
  }

  protected goToSummary = () => {
    history.push('/reportsummary/' + this.props.reportId);
  };

  protected listRow = (element: HTMLTableRowElement) => {
    this.listRowElement = element;
  };

  protected handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      this.isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && this.isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const { showContextMenu, showLocationsContextMenu } = this.state;
    const elemClasses = target.classList;
    const listRowElement = this.listRowElement;
    const contextMenuWrapper = listRowElement.querySelector('.contextMenuWrapper');
    if (
      listRowElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      this.setState({
        showContextMenu: false,
        showLocationsContextMenu: false,
      });
    } else if (this.listRowElement.contains(target) === false) {
      this.setState({
        showContextMenu: false,
        showLocationsContextMenu: false,
      });
    }

    if (
      (showContextMenu || showLocationsContextMenu) &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
}
