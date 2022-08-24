import { PDFDownloadLink } from '@react-pdf/renderer';
import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import InfoModal from '../../../../components/formElements/modal/infoModal/InfoModal';
import { ApiClient } from '../../../../services/ApiClient';
import TeamMemberListItem from '../team/teamMemberListItem/TeamMemberListItem';
import DigitalValuePopupContent from './DigitalValuePopupContent';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';

import {
  IAttachment,
  IChangeLogData,
  ICostFactor,
  ICostRampUp,
  IDigitalValue,
  ITeams,
  IValueFactor,
  IValueRampUp,
} from '../../../../globals/types';
import Styles from './DigitalValueSummary.scss';
import {IntlProvider, FormattedNumber} from 'react-intl';

const classNames = cn.bind(Styles);

export interface IDigitalValueProps {
  digitalValue: IDigitalValue;
  bookmarked: boolean;
  canEdit: boolean;
  solutionId: string;
  solutionName: string;
  onEdit: (solutionId: string) => void;
  onDelete: (solutionId: string) => void;
  updateBookmark: (solutionId: string, isRemove: boolean) => void;
  onExportToPDFDocument: JSX.Element;
}

export interface IDigitalValueSummaryState {
  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  showChangeLogModal: boolean;
  changeLogs: IChangeLogData[];
  onDigitalValueInfoModal: boolean;
}
export default class DigitalValueSummary extends React.Component<IDigitalValueProps, any> {
  constructor(props: any) {
    super(props);
    this.state = {
      showContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
      showChangeLogModal: false,
      changeLogs: [],
      onDigitalValueInfoModal: false,
    };
  }
  // public toMillions = (value: string | number) => {
  //   return value ? Math.abs(Number(value)) / 1.0e6 : '';
  // };

  public toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState({
      contextMenuOffsetTop: 10,
      contextMenuOffsetRight: 10,
      showContextMenu: !this.state.showContextMenu,
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

  public openChangeLog = (e: React.FormEvent<HTMLSpanElement>) => {
    ProgressIndicator.show();
    ApiClient.getChangeLogs(this.props.solutionId).then((result) => {
      ProgressIndicator.hide();
      this.setState({ changeLogs: result.data, showChangeLogModal: true });
    });
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
    const valueDrivers = this.props.digitalValue ? this.props.digitalValue.valueDrivers : [];
    const costDrivers = this.props.digitalValue ? this.props.digitalValue.costDrivers : [];
    const pdfFileName = this.props.solutionName.replace(/[/|\\:*?"<>]/g, '').replace(/ /g, '-');
    const attachments = this.props.digitalValue ? this.props.digitalValue.attachments : [];
    const projectControllers = this.props.digitalValue ? this.props.digitalValue.projectControllers : [];
    const maturityLevel = this.props.digitalValue ? this.props.digitalValue.maturityLevel : '';

    const teamMembersList = projectControllers
      ? projectControllers.map((member: ITeams, index: number) => {
          return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} useFullWidth={true} />;
        })
      : [];

    const valueCalculator = this.props.digitalValue ? this.props.digitalValue.valueCalculator : null;
    const costFactorSummary = valueCalculator ? valueCalculator.costFactorSummary : null;
    const valueFactorSummary = valueCalculator ? valueCalculator.valueFactorSummary : null;
    const calculatedDigitalValue = valueCalculator ? valueCalculator.calculatedDigitalValue : null;
    const breakEvenPoint = valueCalculator ? valueCalculator.breakEvenPoint : '';
    const permissions = this.props.digitalValue ? this.props.digitalValue.permissions : [];
    const assessment = this.props.digitalValue ? this.props.digitalValue.assessment : '';
    const sharingTeamMembersList = permissions
      ? permissions.map((member: ITeams, index: number) => {
          return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
        })
      : [];

    const canShowAssessment =
      assessment && (assessment.benefitRealizationRisk || assessment.strategicRelevance) ? true : false;
    const canShowAttachments = attachments ? (attachments.length > 0 ? true : false) : false;
    const canShowValueCalculator =
      costFactorSummary && valueFactorSummary
        ? costFactorSummary.value || valueFactorSummary.value
          ? true
          : false
        : false;
    const canShowValueDrivers = valueDrivers ? (valueDrivers.length > 0 ? true : false) : false;
    const canShowCostDrivers = costDrivers ? (costDrivers.length > 0 ? true : false) : false;
    const canShowMaturityLevel = teamMembersList.length > 0 || maturityLevel ? true : false;

    const changeLog = (
      <table className="ul-table solutions">
        <tbody>
          <tr className="header-row">
            <th colSpan={8}>
              <span className="hidden">`</span>
            </th>
          </tr>
          {this.state.changeLogs
            ? this.state.changeLogs.map((data: IChangeLogData, index: number) => {
                return (
                  <tr key={index} className="data-row">
                    <td className="wrap-text">
                      {regionalDateAndTimeConversionSolution(data.changeDate)}
                      {/* {this.getParsedDate(data.changeDate)} / {this.getParsedTime(data.changeDate)} */}
                    </td>
                    <td className="wrap-text">
                      {data.modifiedBy.firstName}&nbsp;{data.modifiedBy.lastName}
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td>
                      <span className="hidden">`</span>
                    </td>
                    <td className="wrap-text">{data.changeDescription}</td>
                  </tr>
                );
              })
            : ''}
        </tbody>
      </table>
    );
    return (
      <React.Fragment>
        {canShowMaturityLevel ? (
          <div className={classNames(Styles.mainPanel, Styles.primaryPanel)}>
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
                    <li className="contextListItem">
                      <span onClick={this.openChangeLog}>Change Log</span>
                    </li>
                    <li className="contextListItem">
                      {// @ts-ignore
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
              <h3>{this.props.solutionName}</h3>
              <span className={Styles.digitalValue}>Digital Value Summary</span>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  <div className={classNames(Styles.maturityLevelSection, Styles.flexLayout, Styles.twoColumn)}>
                    <div id="maturityLevel">
                      <label className="input-label summary">Maturity Level</label>
                      <br />
                      <div>{maturityLevel}</div>
                    </div>
                    <div id="controllers">
                      <label className="input-label summary">Controllers</label>
                      <br />
                      <div>{teamMembersList.length > 0 ? teamMembersList : ''}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}
        {canShowCostDrivers ? (
          <div className={classNames(Styles.mainPanel, Styles.costFactorSection)}>
            <div className={Styles.wrapper}>
              <h3>Cost Driver</h3>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  <div className={Styles.digitalValueWrapper}>
                    <div id="costDriversWrapper" className={Styles.expansionListWrapper}>
                      {costDrivers ? (
                        costDrivers.length ? (
                          <div className="expansion-panel-group">
                            {costDrivers.map((item: ICostFactor, index: number) => {
                              const expansionPanelId = 'costFactorExpPanel' + index;
                              return (
                                <div id={'costFactorPanel_' + index} key={index} className="expansion-panel">
                                  <span className="animation-wrapper" />
                                  <input type="checkbox" id={expansionPanelId} />
                                  <label className="expansion-panel-label" htmlFor={expansionPanelId}>
                                    {`Cost Factor ${index + 1} ${item.description}`}
                                    <i className="icon down-up-flip" />
                                  </label>
                                  <div className="expansion-panel-content">
                                    <div className={Styles.expansionnPanelContent}>
                                      <div className={classNames(Styles.flexLayout, Styles.factorInfo)}>
                                        <div>
                                          <label>Description</label>
                                          <div>{item.description}</div>
                                        </div>
                                        <div>
                                          <label>Category</label>
                                          <div>{item.category}</div>
                                        </div>
                                        <div>
                                          <label>Value</label>
                                            <div>
                                              <IntlProvider locale={navigator.language} defaultLocale="en">
                                                {item.value ? <FormattedNumber value={Number(item.value)} /> : ''}
                                              </IntlProvider>
                                              &euro;
                                            </div>
                                        </div>
                                        <div>
                                          <label>Source</label>
                                          <div>{item.source}</div>
                                        </div>
                                      </div>
                                      <div>
                                        <label>Ramp-up</label>
                                        <div className={Styles.rampUpScrollableWrapper}>
                                          {/* <div className={Styles.scrollerLeft}>
                                    <span>
                                        <i className="icon mbc-icon arrow small left" />
                                    </span>
                                    </div> */}
                                          <div className={Styles.rampUpContainer}>
                                            {item.rampUp.map((costDriver: ICostRampUp, indexVal: number) => {
                                              return (
                                                <div className={Styles.rampUpItem} key={indexVal}>
                                                  <strong>{costDriver.year}</strong>
                                                  <div>
                                                    <IntlProvider locale={navigator.language} defaultLocale="en">
                                                      {costDriver.value ? <FormattedNumber value={Number(costDriver.value)} /> : ''}
                                                    </IntlProvider>
                                                    &euro;
                                                  </div>
                                                </div>
                                              );
                                            })}
                                          </div>
                                          {/* <div className={Styles.scrollerRight}>
                                    <span>
                                        <i className="icon mbc-icon arrow small right" />
                                    </span>
                                    </div> */}
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        ) : (
                          ''
                        )
                      ) : (
                        ''
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}

        {canShowValueDrivers ? (
          <div className={classNames(Styles.mainPanel, Styles.valueFactorSection)}>
            <div className={Styles.wrapper}>
              <h3>Value Driver</h3>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  <div className={Styles.digitalValueWrapper}>
                    <div id="valueDriversWrapper" className={Styles.expansionListWrapper}>
                      {valueDrivers ? (
                        valueDrivers.length ? (
                          <div className="expansion-panel-group">
                            {valueDrivers.map((item: IValueFactor, index: number) => {
                              const expansionPanelId = 'valueFactorExpPanel' + index;
                              return (
                                <div id={'valueFactorPanel_' + index} key={index} className="expansion-panel">
                                  <span className="animation-wrapper" />
                                  <input type="checkbox" id={expansionPanelId} />
                                  <label className="expansion-panel-label" htmlFor={expansionPanelId}>
                                    {`Value Factor ${index + 1} ${item.description}`}
                                    <i className="icon down-up-flip" />
                                  </label>
                                  <div className="expansion-panel-content">
                                    <div className={Styles.expansionnPanelContent}>
                                      <div className={classNames(Styles.flexLayout, Styles.factorInfo)}>
                                        <div>
                                          <label>Description</label>
                                          <div>{item.description}</div>
                                        </div>
                                        <div>
                                          <label>Category</label>
                                          <div>{item.category}</div>
                                        </div>
                                        <div>
                                          <label>Value</label>
                                          <div>
                                            <IntlProvider locale={navigator.language} defaultLocale="en">
                                              {item.value ? <FormattedNumber value={Number(item.value)} /> : ''}
                                            </IntlProvider>
                                            &euro;
                                          </div>
                                        </div>
                                        <div>
                                          <label>Source</label>
                                          <div>{item.source}</div>
                                        </div>
                                      </div>
                                      <div>
                                        <label>Ramp-up</label>
                                        <div className={Styles.rampUpScrollableWrapper}>
                                          {/* <div className={Styles.scrollerLeft}>
                                    <span>
                                        <i className="icon mbc-icon arrow small left" />
                                    </span>
                                    </div> */}
                                          <div className={Styles.rampUpContainer}>
                                            {item.rampUp.map((valueDriver: IValueRampUp, indexVal: number) => {
                                              return (
                                                <div className={Styles.rampUpItem} key={indexVal}>
                                                  <strong>{valueDriver.year}</strong>
                                                  <div>
                                                    <IntlProvider locale={navigator.language} defaultLocale="en">
                                                      {valueDriver.percent ? <FormattedNumber value={Number(valueDriver.percent)} /> : ''}
                                                    </IntlProvider>%
                                                  </div>
                                                  <div>
                                                    <IntlProvider locale={navigator.language} defaultLocale="en">
                                                      {valueDriver.value ? <FormattedNumber value={Number(valueDriver.value)} /> : ''}
                                                    </IntlProvider>
                                                    &euro;
                                                  </div>
                                                </div>
                                              );
                                            })}
                                          </div>
                                          {/* <div className={Styles.scrollerRight}>
                                    <span>
                                        <i className="icon mbc-icon arrow small right" />
                                    </span>
                                    </div> */}
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        ) : (
                          ''
                        )
                      ) : (
                        ''
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}

        {canShowValueCalculator ? (
          <div className={classNames(Styles.mainPanel, Styles.digitalValueSection)}>
            <div id="digtalValueWrapper" className={Styles.wrapper}>
              <h3>Digital Value</h3>
              <div className={Styles.infoIcon}>
                <i className="icon mbc-icon info" onClick={this.onDigitalValueInfoModal} />
              </div>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  <div className={Styles.digitalValueWrapper}>
                    <div className={Styles.rampUpScrollableWrapper}>
                      {/* <div className={Styles.scrollerLeft}>
                <span>
                    <i className="icon mbc-icon arrow small left" />
                </span>
                </div> */}
                      <div id="valueRampUpContainer" className={Styles.rampUpContainer}>
                        {valueCalculator
                          ? valueCalculator.calculatedValueRampUpYears.map(
                              (valueDriver: IValueRampUp, indexVal: number) => {
                                return (
                                  <div id={'valueRampUp_' + indexVal} className={Styles.rampUpItem} key={indexVal}>
                                    <strong>{valueDriver.year}</strong>
                                    <div>
                                      <IntlProvider locale={navigator.language} defaultLocale="en">
                                        {valueDriver.percent ? <FormattedNumber value={Number(valueDriver.percent)} /> : ''}
                                      </IntlProvider>%
                                    </div>
                                    <div>
                                      <IntlProvider locale={navigator.language} defaultLocale="en">
                                        {valueDriver.value ? <FormattedNumber value={Number(valueDriver.value)} /> : ''}
                                      </IntlProvider>
                                      &euro;                                    
                                    </div>
                                  </div>
                                );
                              },
                            )
                          : ''}
                      </div>
                      {/* <div className={Styles.scrollerRight}>
                <span>
                    <i className="icon mbc-icon arrow small right" />
                </span>
                </div> */}
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.calculatedValue)}>
                      <div id="valueAt100Percent">
                        <label>Digital Value at {calculatedDigitalValue ? calculatedDigitalValue.valueAt : ''}%</label>
                        <div>  
                        {calculatedDigitalValue.year} {calculatedDigitalValue ? '(' : ''}                           
                          {calculatedDigitalValue ?                             
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              <FormattedNumber value={Number(calculatedDigitalValue.value)} /> &euro;
                            </IntlProvider>                             
                          : 'N/A'}  
                          {calculatedDigitalValue ? ')' : ''}                   
                        </div>
                      </div>

                      <div id="totalCostDriver">
                        <label>Cost Drivers ({costFactorSummary ? costFactorSummary.year : ''})</label>
                        <div>
                          <IntlProvider locale={navigator.language} defaultLocale="en">
                            {costFactorSummary ? <FormattedNumber value={Number(costFactorSummary.value)} /> : ''}
                          </IntlProvider>
                          &euro; 
                        </div>
                      </div>

                      <div id="totalValueDriver">
                        <label>Value Drivers ({valueFactorSummary ? valueFactorSummary.year : ''})</label>
                        <div>
                          <IntlProvider locale={navigator.language} defaultLocale="en">
                            {valueFactorSummary ? <FormattedNumber value={Number(valueFactorSummary.value)} /> : ''}
                          </IntlProvider>
                          &euro;
                        </div>
                      </div>

                      <div id="breakEvenPoint">
                        <label>Break Even Point</label>
                        <div>{breakEvenPoint}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}

        {canShowAttachments ? (
          <div className={classNames(Styles.mainPanel, Styles.attachmentSection)}>
            <div id="dvAttachmetsWrapper" className={Styles.wrapper}>
              <h3>Attached Files</h3>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  {attachments
                    ? attachments.map((attachment: IAttachment, index: number) => {
                        return (
                          <div key={attachment.id} className={Styles.attachmentListView}>
                            <div className={Styles.attachmentContainer}>
                              <div className={Styles.attachmentWrapper}>
                                <i className={classNames(Styles.attachmentIcon, 'icon document')} />
                                <span className={Styles.fileNameText}>{attachment.fileName}</span>
                                <i
                                  onClick={this.downloadAttachment(attachment)}
                                  className={classNames(Styles.downloadIcon, 'icon download')}
                                />
                                <br />
                                <span className={Styles.fileSizeText}>{attachment.fileSize}</span>
                              </div>
                            </div>
                          </div>
                        );
                      })
                    : ''}
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}

        {canShowAssessment ? (
          <div className={classNames(Styles.mainPanel, Styles.assessmentSection)}>
            <div className={Styles.wrapper}>
              <h3>Strategy & Risk Assessment</h3>
              <div className={Styles.firstPanel}>
                <div className={Styles.formWrapper}>
                  <div className={classNames(Styles.flexLayout, Styles.twoColumn)}>
                    <div id="strategicRelevance">
                      <label>Strategic Relevance</label>
                      <br />
                      <div className={Styles.fileNameText}>{assessment ? assessment.strategicRelevance : ''}</div>
                      <br />
                      <br />
                      <div id="strategicRelevanceComment" className={Styles.fileNameText}>
                        {assessment ? assessment.commentOnStrategicRelevance : ''}
                      </div>
                    </div>
                    <div id="benefitRealizationRisk">
                      <label>Benefit Realization Risk</label>
                      <br />
                      <div className={Styles.fileNameText}>{assessment ? assessment.benefitRealizationRisk : ''}</div>
                      <br />
                      <br />
                      <div id="benefitRealizationRiskComment" className={Styles.fileNameText}>
                        {assessment ? assessment.commentOnBenefitRealizationRisk : ''}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}

        {sharingTeamMembersList.length > 0 ? (
          <div className={classNames(Styles.mainPanel, Styles.assessmentSection)}>
            <div id="sharePermissionWrapper" className={Styles.wrapper}>
              <h3>Share / Permission</h3>
              <div className={Styles.firstPanel}>
                <div className={Styles.sharingWrapper}>
                  <label className="input-label summary">Authorised viewers</label>
                  <br />
                  <div>{sharingTeamMembersList}</div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          ''
        )}
        <InfoModal
          title={'Digital Value'}
          show={this.state.onDigitalValueInfoModal}
          content={<DigitalValuePopupContent />}
          onCancel={this.onDigitalValueInfoModalCancel}
        />
        <InfoModal
          title={'Change Log'}
          show={this.state.showChangeLogModal}
          content={this.state.changeLogs ? changeLog : ''}
          onCancel={this.onRiskAssesmentInfoModalCancel}
        />
      </React.Fragment>
    );
  }

  protected downloadAttachment = (attachment: IAttachment) => {
    return () => {
      ProgressIndicator.show();
      ApiClient.downloadAttachment(attachment)
        .then((res: any) => {
          if (res.ok) {
            res.blob().then((blob: any) => {
              const link = document.createElement('a');
              const url = URL.createObjectURL(blob);
              link.href = url;
              link.download = attachment.fileName;
              link.click();
              ProgressIndicator.hide();
            });
          } else {
            ProgressIndicator.hide();
            Notification.show('Error downloading attachment. Please try again later.', 'alert');
          }
        })
        .catch((err: Error) => {
          ProgressIndicator.hide();
          Notification.show('Error downloading attachment. Error - ' + err.message, 'alert');
        });
    };
  };

  protected onRiskAssesmentInfoModalCancel = () => {
    this.setState({ showChangeLogModal: false });
  };
  protected onDigitalValueInfoModal = () => {
    this.setState({ onDigitalValueInfoModal: true });
  };

  protected onDigitalValueInfoModalCancel = () => {
    this.setState({ onDigitalValueInfoModal: false });
  };
}
