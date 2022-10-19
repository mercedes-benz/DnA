import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './DataSourcesSummary.scss';
const classNames = cn.bind(Styles);

import { IAnalytics, IDataSource, IPortfolio, ISharing } from 'globals/types';
import { Envs } from 'globals/Envs';

export interface IDataSourcesSummaryProps {
  datasources: IDataSource;
  portfolio: IPortfolio;
  analytics: IAnalytics;
  sharing: ISharing;
  dsList?: any;
}
export interface IDataSourcesSummaryState {
  canShowAnalytics: boolean;
  canShowSharing: boolean;
  canShowDataSources: boolean;
}
export default class DataSourcesSummary extends React.Component<IDataSourcesSummaryProps, IDataSourcesSummaryState> {
  constructor(props: any) {
    super(props);
    this.state = {
      canShowAnalytics: false,
      canShowSharing: false,
      canShowDataSources: false,
    };
  }
  public componentDidMount() {
    this.setState({
      canShowDataSources:
        (this.props.datasources &&
          this.props.datasources.dataSources &&
          this.props.datasources.dataSources.length > 0) ||
        (this.props.datasources &&
          this.props.datasources.dataVolume &&
          this.props.datasources.dataVolume.name &&
          this.props.datasources.dataVolume.name !== 'Choose'),
      canShowAnalytics:
        (this.props.analytics && this.props.analytics.algorithms && this.props.analytics.algorithms.length > 0) ||
        (this.props.analytics && this.props.analytics.languages && this.props.analytics.languages.length > 0) ||
        (this.props.analytics && this.props.analytics.visualizations && this.props.analytics.visualizations.length > 0),
      canShowSharing:
        this.props.sharing &&
        ((this.props.sharing.gitUrl && this.props.sharing.gitUrl !== '') ||
          (this.props.sharing.result &&
            this.props.sharing.result.name &&
            this.props.sharing.result.name !== 'Choose') ||
          (this.props.sharing.resultUrl && this.props.sharing.resultUrl !== '')),
    });
  }
  public render() {
    const dsChips =
      this.props.datasources.dataSources && this.props.datasources.dataSources.length > 0
        ? this.props.datasources.dataSources.map((chip: any, index: number) => {
            const lastIndex: boolean = index === this.props.datasources.dataSources.length - 1;

            let dsBadge: any = Envs.DNA_APPNAME_HEADER;
            if (this.props.dsList.length > 0) {
              const dataSource = this.props.dsList.filter((ds: any) => ds.name === chip.dataSource);
              if (dataSource.length === 1) {
                if (dataSource[0].source !== null && dataSource[0].dataType !== null) {
                  if (dataSource[0].dataType !== undefined && dataSource[0].source !== undefined) {
                    if (dataSource[0].dataType === 'Not set') {
                      dsBadge = dataSource[0].source;
                    } else {
                      dsBadge =
                        dataSource[0].source +
                        '-' +
                        dataSource[0].dataType.charAt(0).toUpperCase() +
                        dataSource[0].dataType.slice(1);
                    }
                  }
                }
              }
            }

            return (
              <React.Fragment key={index}>
                {chip.dataSource}{' '}
                <span className={Styles.badge}>
                  {dsBadge}
                  {chip.weightage !== 0 && ' / '}
                  <strong className={Styles.bold}>{chip.weightage !== 0 ? chip.weightage + '%' : ''}</strong>
                </span>
                &nbsp;{!lastIndex && `\u002F\xa0`}&nbsp;
              </React.Fragment>
            );
          })
        : 'NA';
    const languageChips =
      this.props.analytics.languages && this.props.analytics.languages.length > 0
        ? this.props.analytics.languages.map((chip: any, index: any) => {
            const lastIndex: boolean = index === this.props.analytics.languages.length - 1;
            return (
              <React.Fragment key={index}>
                {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
              </React.Fragment>
            );
          })
        : 'NA';
    const algoChips =
      this.props.analytics.algorithms && this.props.analytics.algorithms.length > 0
        ? this.props.analytics.algorithms.map((chip: any, index: any) => {
            const lastIndex: boolean = index === this.props.analytics.algorithms.length - 1;
            return (
              <React.Fragment key={index}>
                {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
              </React.Fragment>
            );
          })
        : 'NA';
    const visualizationChips =
      this.props.analytics.visualizations && this.props.analytics.visualizations.length > 0
        ? this.props.analytics.visualizations.map((chip: any, index: any) => {
            const lastIndex: boolean = index === this.props.analytics.visualizations.length - 1;
            return (
              <React.Fragment key={index}>
                {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
              </React.Fragment>
            );
          })
        : 'NA';
    const formatEmptyText = (displayVal: string) => {
      return displayVal && displayVal !== '' ? displayVal : 'NA';
    };
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={classNames(Styles.firstPanel)}>
              <div className={classNames(Styles.formWrapper)}>
                {this.state.canShowDataSources && (
                  <div>
                    <div className={classNames(Styles.flexLayout)}>
                      <div className={classNames(Styles.firstColumn)}>
                        <h3>Data Sources</h3>
                      </div>
                      <div id="dataSourcesTags">
                        <label className="input-label summary">Data Sources</label>
                        <br />
                        <div className={classNames(Styles.row)}>{this.props.dsList.length > 0 ? dsChips : 'NA'}</div>
                      </div>
                      <div id="totalDataVolume">
                        <label className="input-label summary">Total Data Volume</label>
                        <br />
                        <label>{formatEmptyText(this.props.datasources.dataVolume.name)}</label>
                      </div>
                      <div>
                        <label className="input-label summary">&nbsp;</label>
                      </div>
                    </div>
                    <hr className="divider1" />
                  </div>
                )}
                {this.state.canShowAnalytics && (
                  <div>
                    <div className={classNames(Styles.flexLayout)}>
                      <div className={classNames(Styles.firstColumn)}>
                        <h3>Analytics</h3>
                      </div>
                      <div id="languageTags">
                        <label className="input-label summary">Languages</label>
                        <br />
                        <div className={classNames(Styles.row)}>{languageChips}</div>
                      </div>
                      <div id="algorithmTags">
                        <label className="input-label summary">Models/Algorithms</label>
                        <br />
                        <div className={classNames(Styles.row)}>{algoChips}</div>
                      </div>
                      <div id="visualizationTags">
                        <label className="input-label summary">Visualization</label>
                        <br />
                        <div className={classNames(Styles.row)}>{visualizationChips}</div>
                      </div>
                    </div>
                    <hr className="divider1" />
                  </div>
                )}
                {this.state.canShowSharing && (
                  <div>
                    <div className={classNames(Styles.flexLayout)}>
                      <div className={classNames(Styles.firstColumn)}>
                        <h3>Sharing</h3>
                      </div>
                      <div id="gitRepoInfo">
                        <label className="input-label summary">Git Repository</label>
                        <br />
                        <div className={classNames(Styles.row)}>
                          {this.props.sharing.gitUrl && this.props.sharing.gitUrl !== '' ? (
                            <a href={this.props.sharing.gitUrl} target="_blank" rel="noreferrer">
                              {this.props.sharing.gitUrl}
                            </a>
                          ) : (
                            'NA'
                          )}
                        </div>
                      </div>
                      <div id="results">
                        <label className="input-label summary">Results</label>
                        <br />
                        <div className={classNames(Styles.row)}>{formatEmptyText(this.props.sharing.result.name)}</div>
                      </div>
                      <div id="commentInfo">
                        <label className="input-label summary">Comment</label>
                        <br />
                        <div className={classNames(Styles.row)}>
                          {this.props.sharing.resultUrl && this.props.sharing.resultUrl !== '' ? (
                            <a href={this.props.sharing.resultUrl} target="_blank" rel="noreferrer">
                              {this.props.sharing.resultUrl}
                            </a>
                          ) : (
                            'NA'
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
