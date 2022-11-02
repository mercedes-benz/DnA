import React, { useState } from 'react';
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

  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);
  


  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable: ClientRect = document.querySelector('.dataWarehouseList').getBoundingClientRect();
    setContextMenuOffsetTop(elemRect.top - (relativeParentTable.top + 10));
    setContextMenuOffsetRight(10);
    setShowContextMenu(!showContextMenu);
  };
  


  return (
    // <div
    //   className={classNames(
    //     Styles.formWrapper,
    //     Styles.dataWarehouseSection,
    //     singleDataSourceList?.length ? (!dataWarehouselist?.length ? 'hide' : '') : '',
    //   )}
    // >
    //   <div className={classNames('expanstion-table', Styles.dataSourceList)}>
    //     <div className={Styles.dataSourceGrp}>
    //       <div className={Styles.dataSourceGrpList}>
    //         <div className={Styles.dataSourceGrpListItem}>
    //           {dataWarehouselist?.length ? (
    //             <div className={Styles.dataSourceCaption}>
    //               <div className={classNames(Styles.dataSourceTile, Styles.dataWarehouseColWidth)}>
    //                 <div className={Styles.dataSourceTitleCol}>
    //                   <label>Data Warehouse</label>
    //                 </div>
    //                 <div className={Styles.dataSourceTitleCol}>
    //                   <label
    //                     className={
    //                       'sortable-column-header ' +
    //                       (currentColumnToSort === 'commonFunctions' ? currentSortOrder : '')
    //                     }
    //                   >
    //                     Common Functions
    //                   </label>
    //                 </div>
    //                 <div className={Styles.dataSourceTitleCol}>
    //                   <label
    //                     className={
    //                       'sortable-column-header ' +
    //                       (currentColumnToSort === 'connectionTypes' ? currentSortOrder : '')
    //                     }
    //                   >
    //                     Connection Type
    //                   </label>
    //                 </div>
    //                 <div className={Styles.dataSourceTitleCol}>
    //                   <label
    //                     className={
    //                       'sortable-column-header ' +
    //                       (currentColumnToSort === 'dataClassification' ? currentSortOrder : '')
    //                     }
    //                   >
    //                     Data Classification
    //                   </label>
    //                 </div>
    //                 <div className={Styles.dataSourceTitleCol}>Action</div>
    //               </div>
    //             </div>
    //           ) : (
    //             ''
    //           )}
    //           {dataWarehouselist?.map((dataSourcesAndFunctions: IDataWarehouseInUse, index: number) => {
    //             const { commonFunctions, dataClassification, connectionType } =
    //               dataSourcesAndFunctions;
    //             return (
    //               <div
    //                 key={index}
    //                 className={'expansion-panel-group airflowexpansionPanel ' + Styles.dataSourceGrpListItemPanel}
    //               >
    //                 <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
    //                   <span className="animation-wrapper"></span>
    //                   <input type="checkbox" id={index + '1'} defaultChecked={index === 0} />
    //                   <label
    //                     className={Styles.expansionLabel + ' expansion-panel-label ' + Styles.dataSourceCaption}
    //                     htmlFor={index + '1'}
    //                   >
    //                     <div className={classNames(Styles.dataSourceTile, Styles.dataWarehouseColWidth)}>
    //                       <div className={Styles.dataSourceTitleCol}>
    //                         {dataSourcesAndFunctions.dataWarehouse || '-'}
    //                       </div>
    //                       <div className={Styles.dataSourceTitleCol}>{commonFunctions?.join(', ') || '-'}</div>                          
    //                       <div className={Styles.dataSourceTitleCol}>{connectionType || '-'}</div>
    //                       <div className={Styles.dataSourceTitleCol}>{dataClassification || '-'}</div>
    //                       <div className={Styles.dataSourceTitleCol}></div>
    //                     </div>
    //                     <i tooltip-data="Expand" className="icon down-up-flip"></i>
    //                   </label>
    //                   <div className="expansion-panel-content">
    //                     <div className={Styles.dataSourceCollContent}>
    //                       <div className={Styles.dataSourceBtnGrp}>
    //                         <button
    //                           className={'btn btn-primary'}
    //                           type="button"
    //                           onClick={() => onEdit(dataSourcesAndFunctions, index)}
    //                         >
    //                           <i className="icon mbc-icon edit"></i>
    //                           <span>Edit Data Source</span>
    //                         </button>
    //                         <button className={'btn btn-primary'} type="button" onClick={() => onDelete(true, index)}>
    //                           <i className="icon delete"></i>
    //                           <span>Delete Data Source </span>
    //                         </button>
    //                       </div>
    //                     </div>
    //                   </div>
    //                 </div>
    //               </div>
    //             );
    //           })}
    //           <br />
    //           {dataWarehouselist?.length
    //             ? !singleDataSourceList?.length && (
    //                 <div className={Styles.addDataSourceWrapper}>
    //                   <button id="AddDataSourceBtn" onClick={showDataSourceModal}>
    //                     <i className="icon mbc-icon plus" />
    //                     <span>Add Data Source</span>
    //                   </button>
    //                 </div>
    //               )
    //             : null}
    //         </div>
    //       </div>
    //     </div>
    //   </div>
    //   {dataWarehouselist?.length < 1 && singleDataSourceList?.length < 1 && (
    //     <div className={Styles.dataSourceWrapper}>
    //       <div className={Styles.dataSourceWrapperNoList}>
    //         <div className={Styles.addDataSourceWrapper}>
    //           <button id="AddDataSourceBtn" onClick={showDataSourceModal}>
    //             <i className="icon mbc-icon plus" />
    //             <span>Add Data Source</span>
    //           </button>
    //         </div>
    //         <div className={classNames(dataAndFunctionTabError ? '' : 'hide')}>
    //           <span className="error-message">{dataAndFunctionTabError}</span>
    //         </div>
    //       </div>
    //     </div>
    //   )}
    // </div>



    <table
    className={classNames(
      'ul-table dataWarehouseList',
      Styles.dataWarehouseTable,
      dataWarehouselist?.length === 0 ? 'hide' : '',
    )}
    >
      <tbody>
      {dataWarehouselist?.map((datawarehouse: IDataWarehouseInUse, index: number) => {
      return (
        <tr
        id={'datawarehouse-'+index}
        key={index}
        className={classNames(
          'data-row',
          Styles.reportRow,
          showContextMenu ? Styles.contextOpened : null,
        )}
        // ref={this.listRow}
        // onClick={this.goToSummary}
      >
        <td className={'wrap-text ' + classNames(Styles.reportName)}>
          <div className={Styles.solIcon}>
            {datawarehouse?.dataWarehouse}
          </div>
        </td>
        <td className="wrap-text">{datawarehouse?.connectionType || 'NA'}</td>
        <td className="wrap-text">{datawarehouse?.dataClassification  || 'NA'}</td>
        <td>
          <div
            className={classNames(
              Styles.contextMenu,
              showContextMenu ? Styles.open : '',
            )}
          >
            <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div
              style={{
                top: contextMenuOffsetTop + 'px',
                right: contextMenuOffsetRight + 'px',
              }}
              className={classNames('contextMenuWrapper', showContextMenu ? '' : 'hide')}
            >
              <ul className="contextList">                  
                <li className="contextListItem">
                  <span onClick={() => onEdit(datawarehouse, index)}>Edit KPI</span>
                </li>
              
                <li className="contextListItem">
                  <span onClick={() => onDelete(true, index)}>Delete KPI</span>
                </li>
              </ul>
            </div>
          </div>
        </td>
      </tr>
      )}
      
      )}
      
      </tbody>
    </table>


  );
};
