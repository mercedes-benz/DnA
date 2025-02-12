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
  canShowDataSources: boolean;
}
export default class DataSourcesSummary extends React.Component<IDataSourcesSummaryProps, IDataSourcesSummaryState> {
  constructor(props: any) {
    super(props);
    this.state = {
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
          this.props.datasources.dataVolume.name !== 'Choose')
    });
  }
  public render() {
    const dsChips =
      this.props.datasources.dataSources && this.props.datasources.dataSources.length > 0
        ? this.props.datasources.dataSources.map((chip: any, index: number) => {
          const lastIndex: boolean = index === this.props.datasources.dataSources.length - 1;

          let dsBadge: any = Envs.DNA_APPNAME_HEADER;
          let dsId = '';
          if (this.props.dsList.length > 0) {
            const dataSource = this.props.dsList.filter((ds: any) => ds.name === chip.dataSource);
            if (dataSource.length === 1) {
              dsId = dataSource[0]?.externalRefId !== null ? dataSource[0]?.externalRefId : '';
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
              {dsBadge.startsWith('DNA-DataProduct') ? <a href ={Envs.APP_URL+'/#/data/dataproduct/summary/'+dsId} target='_blank' rel="noreferrer"> {chip.dataSource} </a>:chip.dataSource}{' '}
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
    const formatEmptyText = (displayVal: string) => {
      return displayVal && displayVal !== '' && displayVal.toLowerCase() !== "choose" ? displayVal : 'NA';
    };
    return (
      <React.Fragment>
        <div className={classNames(Styles.flexLayout, Styles.mainPanel, 'mainPanelSection')}>
          <div id="analyticsSummary" className={Styles.wrapper}>
            <div>
              <h3>Data Source</h3>
            </div>
            <div className={Styles.firstPanel}>
              <div className={classNames(Styles.flexLayout)}>
                <div id="dataSourcesTags" className={classNames(Styles.solutionSection)}>
                  <label className="input-label summary">Data Sources</label>
                  <br />
                  <div className={classNames(Styles.row)}>{this.props.dsList.length > 0 ? dsChips : 'NA'}</div>
                </div>
                <div id="totalDataVolume" className={classNames(Styles.solutionSection)}>
                  <label className="input-label summary">Total Data Volume</label>
                  <br />
                  <label>{formatEmptyText(this.props.datasources.dataVolume.name)}</label>
                </div>
                <div>
                  <label className="input-label summary">&nbsp;</label>
                </div>
              </div>
            </div>
          </div>
        </div>{' '}
      </React.Fragment>
    );
  }
}
