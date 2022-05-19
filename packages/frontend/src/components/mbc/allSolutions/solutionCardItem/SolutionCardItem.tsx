import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import { SOLUTION_LOGO_IMAGE_TYPES } from '../../../../globals/constants';
import Styles from './SolutionCardItem.scss';
import { IAllSolutionsListItem, ILocation, INotebookInfoSolutionId } from '../../../../globals/types';
import { history } from '../../../..//router/History';
import LogoImage from '../../createNewSolution/description/logoManager/LogoImage/LogoImage';
import { attachEllipsis } from '../../../../services/utils';
import { Envs } from '../../../../globals/Envs';

const classNames = cn.bind(Styles);

export interface ISolutionCardItemProps {
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

const goToSummary = (solutionId: string) => {
  return () => {
    history.push('/summary/' + solutionId);
  };
};

const goTonotebook = (event: any) => {
  history.push('/notebook/');
  event.stopPropagation();
};

let isTouch = false;

const SolutionCardItem = (props: ISolutionCardItemProps) => {
  const solution = props.solution;
  const locations = solution.locations.map((item: ILocation) => item.name);
  const [showContextMenu, setShowContextMenu] = useState<boolean>(false);
  const [showLocationsContextMenu, setShowLocationsContextMenu] = useState<boolean>(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState<number>(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState<number>(0);

  const handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const elemClasses = target.classList;
    const cardDivElement = document?.querySelector('#card-' + props.solutionId);
    const contextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');
    const locationContextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper.locations');

    if (
      cardDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement?.contains(target) === false) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (!locationContextMenuWrapper?.contains(target)) {
      setShowLocationsContextMenu(false);
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

  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);
  }, []);

  useEffect(() => {
    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []);

  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowLocationsContextMenu(false);
    setShowContextMenu(!showContextMenu);
  };

  const toggleLocationsContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowContextMenu(false);
    setShowLocationsContextMenu(!showLocationsContextMenu);
  };

  const onEditSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.onEdit(props.solutionId);
  };

  const onDeleteSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.onDelete(props.solutionId);
  };

  const addToBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.updateBookmark(props.solutionId, false);
  };

  const removeFromBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.updateBookmark(props.solutionId, true);
  };
  return (
    <div id={'card-' + solution.id} key={solution.id} className={Styles.solCard}>
      <div className={Styles.solHead} onClick={goToSummary(solution.id)}>
        <LogoImage displayType={SOLUTION_LOGO_IMAGE_TYPES.TILE} logoDetails={solution.logoDetails} />
        <div className={Styles.solHeadInfo}>
          <div className={Styles.solTitle}>{solution.productName}</div>
        </div>
      </div>
      <div className={Styles.solbodysection}>
        <div className={Styles.subsolHead}>
          <div>
            <span
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
            </span>{' '}
            {!solution.publish && <span className={Styles.draftIndicator}>DRAFT</span>}
          </div>
          <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
            <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div
              style={{
                top: contextMenuOffsetTop + 'px',
                left: contextMenuOffsetLeft + 'px',
              }}
              className={classNames('contextMenuWrapper', showContextMenu ? '' : 'hide')}
            >
              <ul className="contextList">
                {props.bookmarked ? (
                  <li className="contextListItem">
                    <span onClick={removeFromBookmarks}>Remove from My Bookmarks</span>
                  </li>
                ) : (
                  <li className="contextListItem">
                    <span onClick={addToBookmarks}>Add to My Bookmarks</span>
                  </li>
                )}
                {props.canEdit && (
                  <li className="contextListItem">
                    <span onClick={onEditSolution}>Edit Solution</span>
                  </li>
                )}
                {props.canEdit && (
                  <li className="contextListItem">
                    <span onClick={onDeleteSolution}>Delete Solution</span>
                  </li>
                )}
              </ul>
            </div>
          </div>
        </div>

        <div className={Styles.solRegin}>
          <span>{solution.division?.name || 'N/A'}</span>
          <span className={Styles.locationDataWrapper}>
            {locations[0] ? locations[0] : ''}
            {locations.length > 1 ? (
              <div
                className={classNames(
                  Styles.contextMenu,
                  Styles.locations,
                  showLocationsContextMenu ? Styles.open : '',
                )}
              >
                <span onClick={toggleLocationsContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
                  <i className="icon mbc-icon listItem context elipse" />
                </span>
                <div
                  style={{
                    top: contextMenuOffsetTop + 'px',
                    left: contextMenuOffsetLeft + 'px',
                    right: 'auto',
                  }}
                  className={classNames(
                    'contextMenuWrapper locations',
                    Styles.locations,
                    showLocationsContextMenu ? '' : 'hide',
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
          </span>{' '}
          <span>{solution.currentPhase ? solution.currentPhase.name : ''}</span>
        </div>
        <div className={Styles.solInfo}>{attachEllipsis(solution.description, 125)}</div>
        <div className={Styles.solLink}>
          {/* <label className='hidden'>
              <i className="icon mbc-icon dataiku" /> Dataiku
                  </label> */}
          {solution.portfolio?.dnaNotebookId != null && (
            <React.Fragment>
              {props.noteBookData?.solutionId === solution.id ? (
                <label className={Styles.goToLink} title="Go to notebook" onClick={goTonotebook}>
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
            >
              <label className={Styles.goToLink} title="Go to dataiku project">
                <i className="icon mbc-icon dataiku" />
              </label>
            </a>
          )}
          <div className={Styles.solBm}>
            {solution.bookmarked ? <i title="Bookmarked solution" className="icon mbc-icon bookmark-fill" /> : null}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SolutionCardItem;
