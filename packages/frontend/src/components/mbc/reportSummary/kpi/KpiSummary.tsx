import cn from 'classnames';
import * as React from 'react';
import { IKpis } from 'globals/types';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './KpiSummary.scss';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';

const classNames = cn.bind(Styles);

interface IKpiProps {
  kpis: IKpis[];
}

export default class KpiSummary extends React.Component<IKpiProps> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
  }

  public componentDidMount() {
    Tooltip.defaultSetup();
  }

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={Styles.firstPanel}>
            <h3 className={Styles.kpiHeading}>Content & Functions</h3>
              {this.props.kpis?.map((kpi, index) => {
                return (
                  <React.Fragment key={index}>
                    <div className={Styles.kpiListView}>
                      <span className={Styles.kpiNo}>{`KPI ${index + 1}`}</span>
                      <div className={Styles.flexLayout}>
                        <div id="kpiName">
                          <label className="input-label summary">KPI Name</label>
                          <br />
                          <div className={Styles.tagColumn}>{kpi?.name?.kpiName ? kpi.name?.kpiName : 'NA'}</div>
                        </div>
                        <div id="kpiClassification">
                          <label className="input-label summary">KPI Classification</label>
                          <br />
                          <div>{kpi?.name?.kpiClassification ? kpi.name?.kpiClassification : 'NA'}</div>
                        </div>
                        <div id="reportingCase">
                          <label className="input-label summary">Reporting Cause</label>
                          <br />
                          <div>{kpi.reportingCause ? kpi.reportingCause?.join(', ') : 'NA'}</div>
                        </div>                      
                      </div>
                      <div className={Styles.flexLayout}>
                        <div id="kpiLink">
                          <label className="input-label summary">Link to KPI Information</label>
                            <br />
                            <div className = {Styles.breakWords}>
                              {kpi.kpiLink ? (
                                <span>
                                  <a href={kpi.kpiLink} target="_blank" rel="noopener noreferrer">
                                    {kpi.kpiLink}
                                  </a>{' '}
                                  <i
                                    tooltip-data="Open in New Tab"
                                    className={'icon mbc-icon new-tab'}
                                    onClick={() => window.open(kpi.kpiLink, '_blank', 'noopener,noreferrer')}
                                  />
                                </span>
                              ) : (
                                'NA'
                              )}
                            </div>
                          </div>
                        <div id="kpiDescription">
                          <label className="input-label summary">KPI Description</label>
                          <p>
                            <pre className={Styles.commentPre}>{kpi.description ? kpi.description : 'NA'}</pre>
                          </p>
                        </div>
                        <div></div>
                      </div>
                      
                      {this.props.kpis?.length > 1 && <hr className="divider1" />}
                    </div>
                  </React.Fragment>
                );
              })}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
