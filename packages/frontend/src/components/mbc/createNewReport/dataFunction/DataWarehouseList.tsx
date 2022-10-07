import React from 'react';
import cn from 'classnames';
import Styles from './DataFunction.scss';
import { IDataWarehouseInUse, ISingleDataSources } from 'globals/types';

const classNames = cn.bind(Styles);

interface DataWarehouseProps {
  dataWarehouselist: IDataWarehouseInUse[];
  currentColumnToSort: string;
  currentSortOrder: string;
  onEdit: (singleDataSources: IDataWarehouseInUse, index: number) => void;
  onDelete: (isDatawarehouse: boolean, index: number) => void;
  singleDataSourceList: ISingleDataSources[];
  showDataSourceModal: () => void;
  dataAndFunctionTabError: string;
}

export const DataWarehouseList = ({
  singleDataSourceList,
  dataWarehouselist,
  showDataSourceModal,
  currentColumnToSort,
  currentSortOrder,
  onEdit,
  onDelete,
  dataAndFunctionTabError,
}: DataWarehouseProps) => {
  return (
    <div
      className={classNames(
        Styles.formWrapper,
        Styles.dataWarehouseSection,
        singleDataSourceList.length ? (!dataWarehouselist.length ? 'hide' : '') : '',
      )}
    >
      <div className={classNames('expanstion-table', Styles.dataSourceList)}>
        <div className={Styles.dataSourceGrp}>
          <div className={Styles.dataSourceGrpList}>
            <div className={Styles.dataSourceGrpListItem}>
              {dataWarehouselist?.length ? (
                <div className={Styles.dataSourceCaption}>
                  <div className={classNames(Styles.dataSourceTile, Styles.dataWarehouseColWidth)}>
                    <div className={Styles.dataSourceTitleCol}>
                      <label>Data Warehouse</label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (currentColumnToSort === 'commonFunctions' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('commonFunctions', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Common Functions
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (currentColumnToSort === 'specificFunctions' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('specificFunctions', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Specific Functions
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'queries' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('queries', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Queries
                      </label>
                    </div>
                    <div className={Styles.dataSourceTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'dataSources' ? currentSortOrder : '')
                        }
                        // onClick={this.sortByColumn('dataSources', this.state.nextSortOrder)}
                      >
                        {/* <i className="icon sort" /> */}
                        Original Data Sources
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
              {dataWarehouselist?.map((dataSourcesAndFunctions: IDataWarehouseInUse, index: number) => {
                const { commonFunctions, dataClassification, connectionTypes } =
                  dataSourcesAndFunctions;
                return (
                  <div
                    key={index}
                    className={'expansion-panel-group airflowexpansionPanel ' + Styles.dataSourceGrpListItemPanel}
                  >
                    <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                      <span className="animation-wrapper"></span>
                      <input type="checkbox" id={index + '1'} defaultChecked={index === 0} />
                      <label
                        className={Styles.expansionLabel + ' expansion-panel-label ' + Styles.dataSourceCaption}
                        htmlFor={index + '1'}
                      >
                        <div className={classNames(Styles.dataSourceTile, Styles.dataWarehouseColWidth)}>
                          <div className={Styles.dataSourceTitleCol}>
                            {dataSourcesAndFunctions.dataWarehouse || '-'}
                          </div>
                          <div className={Styles.dataSourceTitleCol}>{commonFunctions?.join(', ') || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}>{dataClassification?.join(', ') || '-'}</div>
                          <div className={Styles.dataSourceTitleCol}>{connectionTypes?.join(', ') || '-'}</div>
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
                            <button className={'btn btn-primary'} type="button" onClick={() => onDelete(true, index)}>
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
              {dataWarehouselist?.length
                ? !singleDataSourceList?.length && (
                    <div className={Styles.addDataSourceWrapper}>
                      <button id="AddDataSourceBtn" onClick={showDataSourceModal}>
                        <i className="icon mbc-icon plus" />
                        <span>Add Data Source</span>
                      </button>
                    </div>
                  )
                : null}
            </div>
          </div>
        </div>
      </div>
      {dataWarehouselist?.length < 1 && singleDataSourceList?.length < 1 && (
        <div className={Styles.dataSourceWrapper}>
          <div className={Styles.dataSourceWrapperNoList}>
            <div className={Styles.addDataSourceWrapper}>
              <button id="AddDataSourceBtn" onClick={showDataSourceModal}>
                <i className="icon mbc-icon plus" />
                <span>Add Data Source</span>
              </button>
            </div>
            <div className={classNames(dataAndFunctionTabError ? '' : 'hide')}>
              <span className="error-message">{dataAndFunctionTabError}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
