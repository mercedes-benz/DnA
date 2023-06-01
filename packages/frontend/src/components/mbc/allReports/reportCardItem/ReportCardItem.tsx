import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import Styles from './ReportCardItem.scss';
import { IAllReportsListItem } from 'globals/types';
import { history } from '../../../../router/History';
// import LogoImage from 'components/mbc/createNewSolution/description/logoManager/LogoImage/LogoImage';
// @ts-ignore
import ImgDataikuIcon from '../../../../assets/images/dataiku-icon.png';
// @ts-ignore
import ImgNotebookIcon from '../../../../assets/images/notenook-icon.png';
import { attachEllipsis } from '../../../../services/utils';

const classNames = cn.bind(Styles);

export interface IReportCardItemProps {
  report: IAllReportsListItem;
  reportId: string;
  canEdit: boolean;
  bookmarked: boolean;
  onEdit: (reportId: string) => void;
  onDelete: (reportId: string) => void;
  updateBookmark?: (reportId: string, isRemove: boolean) => void;
}

const goToSummary = (reportId: string) => {
  return () => {
    history.push('/reportsummary/' + reportId);
  };
};

let isTouch = false;

const ReportCardItem = (props: IReportCardItemProps) => {
  const report = props.report;
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
    const cardDivElement = document?.querySelector('#card-' + props.reportId);
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

  // const toggleLocationsContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
  //   e.stopPropagation();
  //   setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
  //   setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
  //   setShowContextMenu(false);
  //   setShowLocationsContextMenu(!showLocationsContextMenu);
  // };

  const onEditReport = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.onEdit(props.reportId);
  };

  const onDeleteReport = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setShowContextMenu(false);
    props.onDelete(props.reportId);
  };

  // const addToBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
  //   e.stopPropagation();
  //   setShowContextMenu(false);
  //   props.updateBookmark(props.reportId, false);
  // };

  // const removeFromBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
  //   e.stopPropagation();
  //   setShowContextMenu(false);
  //   props.updateBookmark(props.reportId, true);
  // };

  return (
    <div id={'card-' + report?.id} className={Styles.solCard}>
      <div className={Styles.solHead} onClick={goToSummary(report?.id)}>
        {/* <LogoImage displayType={SOLUTION_LOGO_IMAGE_TYPES.TILE} logoDetails={report.logoDetails} /> */}
        {/* <div className={Styles.solHeadInfo}>
          <div className={Styles.solTitle}>{report.productName}</div>
        </div> */}
        <div>
          <div className={Styles.solTitle}>{report?.productName}</div>
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
              {/* {props.bookmarked ? (
                <li className="contextListItem">
                  <span onClick={removeFromBookmarks}>Remove from My Bookmarks</span>
                </li>
              ) : (
                <li className="contextListItem">
                  <span onClick={addToBookmarks}>Add to My Bookmarks</span>
                </li>
              )} */}
              {props.canEdit && (
                <li className="contextListItem">
                  <span onClick={onEditReport}>Edit Report</span>
                </li>
              )}
              {props.canEdit && (
                <li className="contextListItem">
                  <span onClick={onDeleteReport}>Delete Report</span>
                </li>
              )}
            </ul>
          </div>
        </div>
      </div>
      <div className={Styles.solbodysection}>
        <div className={Styles.subsolHead}>
          <div>
            <span
              className={
                report?.description.status
                  ? report?.description?.status === 'Live'
                    ? classNames(Styles.active)
                    : report.description?.status === 'Closed'
                    ? classNames(Styles.closed)
                    : ''
                  : ''
              }
            >
              {report?.description?.status}
            </span>{' '}
            {!report?.publish && <span className={Styles.draftIndicator}>DRAFT</span>}
          </div>
          
        </div>

        <div className={Styles.solRegin}>
          <span>{report?.description.division?.name && 
          report?.description.division?.name != 'Choose' ? 
          report?.description.division?.name : 'N/A'}</span>
          <span>{report?.description?.productPhase || ''}</span>
        </div>
        <div className={Styles.solInfo}>{attachEllipsis(report?.description?.productDescription, 125)}</div>
        <div className={Styles.solLink}></div>
      </div>
    </div>
  );
};

export default ReportCardItem;
