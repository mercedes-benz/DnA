import React from 'react';
import cn from 'classnames';
import Styles from './DataFunction.scss';
import { IDataWarehouseInUse, ISingleDataSources } from 'globals/types';

const classNames = cn.bind(Styles);

interface SingleDataSourceProps {
  list: ISingleDataSources[];
  currentColumnToSort: string;
  currentSortOrder: string;
  onEdit: (singleDataSources: ISingleDataSources, index: number) => void;
  onDelete: (isDatawarehouse: boolean, index: number) => void;
  dataWarehouseList: IDataWarehouseInUse[];
  showDataSourceModal: () => void;
}

export const SingleDataSourceList = ({
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
                          'sortable-column-header ' + (currentColumnToSort === 'subsystems' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('subsystems', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Subsystem
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
                    <div className={Styles.dataSourceTitleCol}>Action</div>
                  </div>
                </div>
              ) : (
                ''
              )}
              {list?.map((dataSourcesAndFunctions: ISingleDataSources, index: number) => {
                const connectionTypes = dataSourcesAndFunctions.connectionTypes?.map((item) => item.name)?.join(', ');
                const dataClassifications = dataSourcesAndFunctions.dataClassifications?.map((item) => item.name)?.join(', ');
                const dataSources = dataSourcesAndFunctions.dataSources?.map((item) => item.name)?.join(', ');

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
                          <div className={Styles.dataSourceTitleCol}>{dataSources || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}>{dataClassifications || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}>{connectionTypes || '-'}</div>
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
