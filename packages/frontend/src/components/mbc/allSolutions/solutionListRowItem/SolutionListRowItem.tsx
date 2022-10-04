import cn from 'classnames';
import * as React from 'react';
import { IAllSolutionsListItem, ILocation, INotebookInfoSolutionId } from 'globals/types';
import { history } from '../../../../router/History';
import Styles from './SolutionListRowItem.scss';
import LogoImage from 'components/mbc/createNewSolution/description/logoManager/LogoImage/LogoImage';
import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import { DataFormater } from '../../../../services/utils';
import { Envs } from 'globals/Envs';

const classNames = cn.bind(Styles);

export interface ISolutionListRowItemProps {
  solution: IAllSolutionsListItem;
  solutionId: string;
  canEdit: boolean;
  bookmarked: boolean;
  showDigitalValue: boolean;
  onEdit: (solutionId: string) => void;
  onDelete: (solutionId: string) => void;
  updateBookmark: (solutionId: string, isRemove: boolean) => void;
  noteBookData: INotebookInfoSolutionId;
}

export interface ISolutionListRowItemState {
  showContextMenu: boolean;
  showLocationsContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
}

export default class SolutionListRowItem extends React.Component<ISolutionListRowItemProps, ISolutionListRowItemState> {
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
    const relativeParentTable: ClientRect = document.querySelector('table.solutions').getBoundingClientRect();
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
    const relativeParentTable: ClientRect = document.querySelector('table.solutions').getBoundingClientRect();
    this.setState({
      contextMenuOffsetTop: elemRect.top - relativeParentTable.top,
      contextMenuOffsetRight: elemRect.left - relativeParentTable.left - 200,
      showContextMenu: false,
      showLocationsContextMenu: !this.state.showLocationsContextMenu,
    });
  };

  public onEditSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onEdit(this.props.solutionId);
      },
    );
  };
  public onDeleteSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onDelete(this.props.solutionId);
      },
    );
  };
  public goToNotebook = (event: any) => {
    history.push('/notebook/');
    event.stopPropagation();
  };
  public goToDataiku = (event: any) => {
    event.stopPropagation();
  };
  public addToBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.updateBookmark(this.props.solutionId, false);
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
        this.props.updateBookmark(this.props.solutionId, true);
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
    const solution = this.props.solution;
    const locations = solution.locations.map((item: ILocation) => item.name);
    const { showContextMenu, showLocationsContextMenu } = this.state;
    return (
      <React.Fragment>
        <tr
          id={solution.id}
          key={solution.id}
          className={classNames(
            'data-row',
            Styles.solutionRow,
            showContextMenu || showLocationsContextMenu ? Styles.contextOpened : null,
          )}
          ref={this.listRow}
          onClick={this.goToSummary}
        >
          <td className={'wrap-text ' + classNames(Styles.solutionName)}>
            <div className={Styles.solIcon}>
              <div className={Styles.solIconimg}>
                <LogoImage displayType={SOLUTION_LOGO_IMAGE_TYPES.THUMBNAIL} logoDetails={solution.logoDetails} />
              </div>{' '}
              <div className={Styles.solutionNameDivide}>
                {solution.productName}
                {solution.portfolio?.dnaNotebookId && (
                  <React.Fragment>
                    {this.props.noteBookData?.solutionId === solution.id ? (
                      <label className={Styles.goToLink} title="Go to notebook" onClick={this.goToNotebook}>
                        <i className="icon mbc-icon jupyter" />
                      </label>
                    ) : (
                      <label title="Has notebook">
                        <i className="icon mbc-icon jupyter" />
                      </label>
                    )}
                  </React.Fragment>
                )}
                {solution.portfolio?.dnaDataikuProjectId !== null && (
                  <a
                    href={Envs.DATAIKU_LIVE_APP_URL + '/projects/' + solution.portfolio?.dnaDataikuProjectId + '/'}
                    target="_blank"
                    rel="noreferrer"
                    onClick={this.goToDataiku}
                  >
                    <label title="Go to dataiku project">
                      <i className="icon mbc-icon dataiku" />
                    </label>
                  </a>
                )}
              </div>
            </div>
          </td>
          <td className={Styles.draftIndicatorCol}>
            {!solution.publish ? <span className={Styles.draftIndicator}>DRAFT</span> : ''}
          </td>
          <td className="wrap-text">{solution.currentPhase ? solution.currentPhase.name : ''}</td>
          <td>{solution.division?.name || 'N/A'}</td>
          {this.props.showDigitalValue ? (
            <td>
              {solution.digitalValue && solution.digitalValue.digitalValue
                ? `${DataFormater(solution.digitalValue.digitalValue)}`
                : 'NA'}
            </td>
          ) : null}
          <td>
            <div className={Styles.locationDataWrapper}>
              {locations[0] ? locations[0] : ''}
              {locations.length > 1 ? (
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
                    <ul className="contextList mbc-scroll sub">
                      <li className="contextListItem">
                        <p className="locationsText">{locations.length ? locations.join(', ') : ''}</p>
                      </li>
                    </ul>
                  </div>
                </div>
              ) : null}
            </div>
          </td>
          <td
            className={
              solution.projectStatus
                ? solution.projectStatus.name === 'Active'
                  ? classNames(Styles.active)
                  : solution.projectStatus.name === 'Closed'
                  ? classNames(Styles.closed)
                  : ''
                : ''
            }
          >
            {solution.projectStatus.name}
          </td>
          <td>
            <div className={classNames(Styles.contextMenu, this.state.showContextMenu ? Styles.open : '')}>
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
                  {this.props.bookmarked ? (
                    <li className="contextListItem">
                      <span onClick={this.removeFromBookmarks}>Remove from My Bookmarks</span>
                    </li>
                  ) : (
                    <li className="contextListItem">
                      <span onClick={this.addToBookmarks}>Add to My Bookmarks</span>
                    </li>
                  )}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onEditSolution}>Edit Solution</span>
                    </li>
                  )}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onDeleteSolution}>Delete Solution</span>
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
    history.push('/summary/' + this.props.solutionId);
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
