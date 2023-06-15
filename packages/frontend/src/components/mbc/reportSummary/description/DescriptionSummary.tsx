import { PDFDownloadLink } from '@react-pdf/renderer';
import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { history } from '../../../../router/History';
import { IDescriptionRequest, ILogoDetails } from 'globals/types';
import Styles from './DescriptionSummary.scss';

const classNames = cn.bind(Styles);

export interface IDescriptionReportProps {
  reportId: string;
  productName: string;
  description: IDescriptionRequest;
  canEdit: boolean;
  onEdit: (reportId: string) => void;
  onDelete: (reportId: string) => void;
  onExportToPDFDocument: JSX.Element;
  bookmarked?: boolean;
  reportLink?: string;
}
export interface IDescriptionReportRequest {
  reportName: string;
  description: string;
  statusDetails: string;
  integratedInPortals: string;
  designGuids: string;
  frontEndTechs: string;
  logoDetails: ILogoDetails;
}
export interface IDescriptionReportState {
  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  showChangeLogModal: boolean;
}

export default class DescriptionSummary extends React.Component<IDescriptionReportProps, IDescriptionReportState> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
    this.state = {
      showContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
      showChangeLogModal: false,
    };
  }

  public componentWillMount() {
    document.addEventListener('touchend', this.handleContextMenuOutside, true);
    document.addEventListener('click', this.handleContextMenuOutside, true);
  }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleContextMenuOutside, true);
    document.removeEventListener('click', this.handleContextMenuOutside, true);
  }

  public toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState({
      contextMenuOffsetTop: 10,
      contextMenuOffsetRight: 10,
      showContextMenu: !this.state.showContextMenu,
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

  public openChangeLog = (e: React.FormEvent<HTMLSpanElement>) => {
    this.setState({ showChangeLogModal: true });
  };

  public getParsedDate = (strDate: any) => {
    const date = new Date(strDate);
    let dd: any = date.getDate();
    let mm: any = date.getMonth() + 1; // January is 0!

    const yyyy = date.getFullYear();
    if (dd < 10) {
      dd = '0' + dd;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (dd + '.' + mm + '.' + yyyy).toString();
  };

  public getParsedTime = (strDate: any) => {
    const date = new Date(strDate);
    let hh: any = date.getHours();
    let mm: any = date.getMinutes(); // January is 0!

    if (hh < 10) {
      hh = '0' + hh;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (hh + ':' + mm).toString();
  };

  public render() {
    const reportName = this.props.productName;
    const description = this.props.description;
    const chips =
      description.tags && description.tags?.length
        ? description.tags?.map((chip: any, index: any) => {
            return (
              <div className="chips read-only" key={index}>
                <label className="name">{chip}</label>
              </div>
            );
          })
        : 'NA';
    const pdfFileName = reportName?.replace(/[/|\\:*?"<>]/g, '').replace(/ /g, '-');
    const reportLink = this.props.reportLink;

    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          {/* <button className="btn btn-text back arrow" type="submit" onClick={this.goback}>
            Back
          </button> */}
          <div className={Styles.wrapper}>
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
                  <li className="contextListItem">
                    {
                      // @ts-ignore
                      <PDFDownloadLink
                        document={this.props.onExportToPDFDocument}
                        className={Styles.pdfLink}
                        fileName={`${pdfFileName}.pdf`}
                      >
                        {(doc: any) => (doc.loading ? 'Loading...' : 'Export to PDF')}
                      </PDFDownloadLink>
                    }
                  </li>
                </ul>
              </div>
            </div>
            <h3 id="reportName" className={Styles.reportName}>{reportName}</h3>
            <span className={Styles.description}>Report Summary</span>
            <div className={Styles.firstPanel}>
              <div className={Styles.formWrapper}>
                <div className={Styles.flexLayout}>
                  <div id="reportDescription">
                    <label className="input-label summary">Description</label>
                    <br />
                    <div>
                      <pre className={Styles.reportPre}>{description.productDescription ? description.productDescription : 'NA'}</pre>
                    </div>
                  </div>
                  <div id="tags">
                    <label className="input-label summary">Tags</label>
                    <br />
                    <div className={Styles.tagColumn}>{chips}</div>
                  </div>
                  <div id="reportLink">
                    <label className="input-label summary">Report Link</label>
                    <br />
                    <div className={Styles.reportLinkColumn}>
                      <a href={reportLink} target="_blank" rel="noreferrer">
                        {reportLink ? reportLink : 'NA'}
                      </a>
                    </div>
                  </div>
                </div>
                <hr className="divider1" />
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="division">
                    <label className="input-label summary">Division</label>
                    <br />
                    {!description.division?.name || description.division?.name === 'Choose'
                      ? 'NA'
                      : description.division?.name}
                  </div>
                  <div id="subdivision">
                    <label className="input-label summary">Sub Division</label>
                    <br />
                    {description.division?.subdivision?.name ? description.division.subdivision.name : 'NA'}
                  </div>
                  <div id="department">
                    <label className="input-label summary">E2-Department</label>
                    <br />
                    {description.department ? description.department : 'NA'}
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="productPhase">
                    <label className="input-label summary">Report Type</label>
                    <br />
                    {description.reportType && description?.reportType != '0' ? description.reportType : 'NA'}
                  </div>
                  <div id="status">
                    <label className="input-label summary">Status </label>
                    <br />
                    {description.status ? description.status : 'NA'}
                  </div>
                  <div id="integratedinportal">
                    <label className="input-label summary">Integrated In Portal</label>
                    <br />
                    {description.integratedPortal && description?.integratedPortal != '0' ? description.integratedPortal : 'NA'}
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="agileReleaseTrain">
                    <label className="input-label summary">Agile Release Train</label>
                    <br />
                    {description.agileReleaseTrain && description?.agileReleaseTrain != '0' ? description.agileReleaseTrain : 'NA'}
                  </div>
                  <div id="procedureId">
                    <label className="input-label summary">Procedure Id</label>
                    <br />
                    {description.procedureId ? description.procedureId : 'NA'}
                  </div>
                  <div id="frmonEndTech">
                    <label className="input-label summary">Frontend Technologies</label>
                    <br />
                    {description.frontendTechnologies?.join(', ')}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  protected goback = () => {
    history.goBack();
  };

  protected onRiskAssesmentInfoModalCancel = () => {
    this.setState({ showChangeLogModal: false });
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
    const { showContextMenu } = this.state;
    const elemClasses = target.classList;
    const contextMenuWrapper = document.querySelector('.contextMenuWrapper');
    if (
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      showContextMenu
    ) {
      this.setState({
        showContextMenu: false,
      });
    }

    if (
      showContextMenu &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
}
