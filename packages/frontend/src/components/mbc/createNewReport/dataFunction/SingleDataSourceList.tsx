import React from 'react';
import cn from 'classnames';
import Styles from './DataFunction.scss';
import { IDataSourceMaster, IDataWarehouseInUse, ISingleDataSources } from 'globals/types';
import { Envs } from 'globals/Envs';

const classNames = cn.bind(Styles);

interface SingleDataSourceProps {
  dataSources: IDataSourceMaster[],
  list: ISingleDataSources[];
  currentColumnToSort: string;
  currentSortOrder: string;
  onEdit: (singleDataSources: ISingleDataSources, index: number) => void;
  onDelete: (isDatawarehouse: boolean, index: number) => void;
  dataWarehouseList: IDataWarehouseInUse[];
  showDataSourceModal: () => void;
}

export const SingleDataSourceList = ({
  dataSources,
  list,
  currentColumnToSort,
  currentSortOrder,
  onEdit,
  onDelete,
  dataWarehouseList,
  showDataSourceModal,
}: SingleDataSourceProps) => {
  return list?.length ? (
    <div className={classNames(Styles.formWrapper, dataWarehouseList?.length ? Styles.singleDataSourceSection : '')}>
      <div className={classNames('expanstion-table', Styles.dataSourceList)}>
        <div className={Styles.dataSourceGrp}>
          <div className={Styles.dataSourceGrpList}>
            <div className={Styles.dataSourceGrpListItem}>
              {list.length ? (
                <div className={Styles.dataSourceCaption}>
                  <div className={classNames(Styles.dataSourceTile, Styles.singleDataSourceColWidth)}>
                    <div className={Styles.dataSourceTitleCol}>
                      <label>Single Data Source No.</label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'dataSources' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('dataSources', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Data Sources
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (currentColumnToSort === 'connectionTypes' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('connectionTypes', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Connection Type
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'dataClassification' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('dataClassification', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Data Classification
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>Action</div>
                  </div>
                </div>
              ) : (
                ''
              )}
              {list?.map((dataSourcesAndFunctions: ISingleDataSources, index: number) => {
                const connectionType = dataSourcesAndFunctions.connectionType;
                const dataClassification = dataSourcesAndFunctions.dataClassification;
                const selectedDataSources = JSON.parse(dataSourcesAndFunctions.dataSource);

                 const dsChips =
                   selectedDataSources && selectedDataSources.length > 0
                     ? selectedDataSources.map((chip: any, index: number) => {
                         const lastIndex: boolean = index === selectedDataSources.length - 1;

                         let dsBadge: any = Envs.DNA_APPNAME_HEADER;
                         if (dataSources.length > 0) {
                           const dataSource = dataSources.filter((ds: any) => ds.name === chip.dataSource);
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
                               <strong className={Styles.bold}>
                                 {chip.weightage !== 0 ? chip.weightage + '%' : ''}
                               </strong>
                             </span>
                             &nbsp;{!lastIndex && `\u002F\xa0`}&nbsp;
                           </React.Fragment>
                         );
                       })
                     : 'NA';

                return (
                  <div
                    key={index}
                    className={'expansion-panel-group airflowexpansionPanel ' + Styles.dataSourceGrpListItemPanel}
                  >
                    <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                      <span className="animation-wrapper"></span>
                      <input type="checkbox" id={index + '2'} defaultChecked={index === 0} />
                      <label
                        className={Styles.expansionLabel + ' expansion-panel-label ' + Styles.dataSourceCaption}
                        htmlFor={index + '2'}
                      >
                        <div className={classNames(Styles.dataSourceTile, Styles.singleDataSourceColWidth)}>
                          <div className={Styles.dataSourceTitleCol}>{`Data Source ${index + 1}`}</div>
                          <div className={classNames(Styles.dataSourceTitleCol, Styles.chips)}>{dsChips || '-'}</div>                          
                          <div className={Styles.dataSourceTitleCol}>{connectionType || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}>{dataClassification || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}></div>
                        </div>
                        <i tooltip-data="Expand" className="icon down-up-flip"></i>
                      </label>
                      <div className="expansion-panel-content">
                        <div className={Styles.dataSourceCollContent}>
                          <div className={Styles.dataSourceBtnGrp}>
                            <button
                              className={'btn btn-primary'}
                              type="button"
                              onClick={() => onEdit(dataSourcesAndFunctions, index)}
                            >
                              <i className="icon mbc-icon edit"></i>
                              <span>Edit Data Source</span>
                            </button>
                            <button className={'btn btn-primary'} type="button" onClick={() => onDelete(false, index)}>
                              <i className="icon delete"></i>
                              <span>Delete Data Source </span>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
              <br />
              {(dataWarehouseList?.length > 0 || list?.length > 0) && (
                <div className={Styles.addDataSourceWrapper}>
                  <button id="AddDataSourceBtn" onClick={() => showDataSourceModal()}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Data Source</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  ) : null;
};
