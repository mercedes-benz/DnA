import cn from 'classnames';
import * as React from 'react';
import { IDataAndFunctions } from 'globals/types';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './DataFunctionSummary.scss';

const classNames = cn.bind(Styles);

interface IDataAndFunctionsProps {
  dataAndFunctions: IDataAndFunctions;
}
export default class DataFunctionSummary extends React.Component<IDataAndFunctionsProps> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
  }

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={Styles.firstPanel}>
              <h3 className={Styles.dataAndFunction}>Data Sources & Functions</h3>
              {this.props.dataAndFunctions?.dataWarehouseInUse?.map((data, index) => {
                return (
                  <React.Fragment key={`dataWarehouse${index}`}>
                    <div className={Styles.dataFunctionListView}>
                      <div className={Styles.dataWarehouse}>
                        <label className="input-label summary">Data Warehouse</label>
                        <br />
                        <span>{data.dataWarehouse}</span>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div id="commonFunctions">
                          <label className="input-label summary">Common Functions</label>
                          <br />
                          {data.commonFunctions?.join(', ')}
                        </div>
                        {/* <div id="specificFunctions">
                          <label className="input-label summary">Specific Functions</label>
                          <br />
                          {data.specificFunctions?.join(', ')}
                        </div> */}
                        {/* <div id="queries">
                          <label className="input-label summary">Queries</label>
                          <br />
                          {data.queries?.join(', ')}
                        </div> */}
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        {/* <div id="dataSources">
                          <label className="input-label summary">Original Data Sources</label>
                          <br />
                          {data.dataSources?.join(', ')}
                        </div> */}
                        <div id="connectionType">
                          <label className="input-label summary">Connection Type</label>
                          <br />
                          {data.connectionType}
                        </div>
                        <div></div>
                      </div>
                    </div>
                    {this.props.dataAndFunctions.dataWarehouseInUse.length > 1 ||
                    this.props.dataAndFunctions.singleDataSources.length ? (
                      <hr className="divider1" />
                    ) : null}
                  </React.Fragment>
                );
              })}
              {this.props.dataAndFunctions?.singleDataSources?.map((data, index) => {
                return (
                  <React.Fragment key={`singleDataSource${index}`}>
                    <div className={Styles.dataFunctionListView}>
                      <span className={Styles.dataAndFunction}>{`Single Data Source ${index + 1}`}</span>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div id="dataSource">
                          <label className="input-label summary">Data Sources</label>
                          <br />
                          {JSON.parse(data.dataSource)?.map((item:any) => item.dataSource).join(' / ')}
                        </div>
                        <div id="commonFunctions">
                          <label className="input-label summary">Data Classification</label>
                          <br />
                          {data.dataClassification}
                        </div>
                        <div id="specificFunctions">
                          <label className="input-label summary">Connection Type</label>
                          <br />
                          {data.connectionType}
                        </div>
                      </div>
                    </div>
                    {this.props.dataAndFunctions.singleDataSources.length > 1 ? <hr className="divider1" /> : null}
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
