import cn from 'classnames';
import * as React from 'react';
import { IAllSolutionsListItem, ILocation } from 'globals/types';
import { history } from '../../../../router/History';
import Styles from './SearchListRowItem.scss';
import LogoImage from 'components/mbc/createNewSolution/description/logoManager/LogoImage/LogoImage';
import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';

const classNames = cn.bind(Styles);

export interface ISearchListRowItemProps {
  solution: IAllSolutionsListItem;
}

export interface ISearchListRowItemState {
  showLocationsContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
}

export default class SearchListRowItem extends React.Component<ISearchListRowItemProps, ISearchListRowItemState> {
  protected isTouch = false;
  protected listRowElement: HTMLTableRowElement;

  constructor(props: any) {
    super(props);
    this.state = {
      showLocationsContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
    };
  }

  public toggleLocationsContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    this.setState({
      contextMenuOffsetTop: elemRect.top + window.scrollY - 258,
      contextMenuOffsetRight: elemRect.left + window.scrollX - 390,
      showLocationsContextMenu: !this.state.showLocationsContextMenu,
    });
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
    return (
      <React.Fragment>
        <tr
          id={solution.id}
          key={solution.id}
          className={classNames('data-row', Styles.solutionRow)}
          ref={this.listRow}
          onClick={this.goToSummary}
        >
          <td className={classNames('wrap-text', Styles.solutionName)}>
            <div className={Styles.solIcon}>
              <div className={Styles.solIconimg}>
                <LogoImage displayType={SOLUTION_LOGO_IMAGE_TYPES.THUMBNAIL} logoDetails={solution.logoDetails} />
              </div>{' '}
              {solution.productName}
            </div>
          </td>
          <td className={Styles.draftIndicatorCol}>
            {!solution.publish ? <span className={Styles.draftIndicator}>DRAFT</span> : ''}
          </td>
          <td className="wrap-text">{solution.currentPhase ? solution.currentPhase.name : ''}</td>
          <td>{solution.division?.name || 'N/A'}</td>
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
              ) : (
                ''
              )}
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
        </tr>
      </React.Fragment>
    );
  }

  protected goToSummary = () => {
    history.push('/summary/' + this.props.solution.id);
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
    const { showLocationsContextMenu } = this.state;
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
      showLocationsContextMenu
    ) {
      this.setState({
        showLocationsContextMenu: false,
      });
    } else if (listRowElement.contains(target) === false) {
      this.setState({
        showLocationsContextMenu: false,
      });
    }

    if (
      showLocationsContextMenu &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
}
