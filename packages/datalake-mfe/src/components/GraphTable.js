/* eslint-disable react/no-unknown-property */ 
import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './graph-table.scss';
import { tableWidth, titleHeight, commentHeight, fieldHeight } from '../data/settings';
import { useSelector, useDispatch } from 'react-redux';
import Tooltip from '../common/modules/uilab/js/src/tooltip';
import { setTables } from '../redux/graphSlice';

/**
 * It renders a table with a title, a list of columns, and a button to edit the table
 * @param props - {
 *            table,
 *            onTableMouseDown,
 *            onGripMouseDown,
 *        }
 * @returns A table component with a title and a list of columns.
 */
const GraphTable = (props) => {
    const dispatch = useDispatch();
    const { table, onTableMouseDown, onGripMouseDown, tableSelectedId, setTableSelectId, onDeleteTable, onAddColumn, onEditColumn, isOwner } = props;
    const { project } = useSelector(state => state.graph);

    useEffect(() => {
        Tooltip.defaultSetup();
        return Tooltip.clear();
        //eslint-disable-next-line
    }, []);

    const editable = isOwner;

    const handlerContextMenu = e => {
        e.preventDefault();
        e.stopPropagation();
    };

    const removeColumn = (table, index) => {
        const tempTable = {...table};
        const columns = [...tempTable.columns];
        columns.splice(index, 1);
        delete tempTable.columns;
        tempTable.columns = [...columns];

        const projectTemp = {...project};
        const tempTables = projectTemp.tables.filter(item => item.tableName !== tempTable.tableName);
        projectTemp.tables = [...tempTables, tempTable];
        dispatch(setTables(projectTemp.tables));
    }

    // 12: box-shadow
    const height = table.columns.length * fieldHeight + titleHeight + commentHeight + 12;

    return (
        <>
        <foreignObject
            x={table.xcoOrdinate}
            y={table.ycoOrdinate}
            width={tableWidth}
            height={height}
            key={table.tableName}
            onMouseDown={e => onTableMouseDown(e, table)}
            // onMouseUp={(e) => onTableMouseUp(e, table)}
            onContextMenu={handlerContextMenu}
        >
            <div
                className={`table ${editable ? 'editable' : ''} ${
                    tableSelectedId === table.tableName ? 'table-selected' : ''
                }`}
                onMouseOver={() => setTableSelectId(table.tableName)}
                onMouseOut={() => setTableSelectId(null)}
            >
                <div
                    className="table-title"
                    style={{
                        height: titleHeight,
                        lineHeight: `${titleHeight}px`,
                    }}
                >
                    <span className="table-name">{table.tableName}</span>

                    {editable && (
                        <div className="table-settings">
                            <button 
                                tooltip-data={'View Collaborators'}
                                onClick={() => props.onCollabClick(table)}
                            >
                                <i className="icon mbc-icon profile"></i>
                            </button>
                            <button 
                            // onClick={() => dispatch(setEditingTable(table))}
                                tooltip-data={'Edit Table'}
                            >
                                <i className="icon mbc-icon edit fill"></i>
                            </button>
                            <button tooltip-data={'Delete Table'} className={Styles.btnDelete} onClick={() => onDeleteTable(table.tableName)}>
                                <i className="icon delete"></i>
                            </button>
                        </div>
                    )}

                    <div className="table-format">
                        <p>{table.dataFormat}</p>
                    </div>
                </div>
                <p className={Styles.tableComment}>
                    {table.description}
                </p>
                {table.columns &&
                    table.columns.map((field, index) => (
                        <div
                            className="popover"
                            key={field.columnName}
                            // content={<RenderTableTips field={field} />}
                        >
                            <div
                                className={`row ${editable ? 'editable' : ''}`}
                                key={field.columnName}
                                tableid={table.tableName}
                                fieldid={field.columnName}
                                style={{ height: fieldHeight, lineHeight: `${fieldHeight}px` }}
                            >
                                <div
                                    className="start-grip grip"
                                    onMouseDown={onGripMouseDown}
                                ></div>
                                <div className="field-content">
                                    <div className={Styles.columnName} tooltip-data={`${field.columnName}\n${field.comment}`}>
                                        {field.columnName}
                                    </div>
                                    <div  className={classNames('field-type', Styles.columnType)}>
                                        <span>{field.dataType}</span>
                                        {field.notNullConstraintEnabled && <span className={Styles.notNull}>Not Null</span>}
                                    </div>
                                </div>
                                <div className="grip-setting">
                                    <button className={classNames('grip-setting-btn')} 
                                        onClick={() => onEditColumn(field, table, index)}
                                    >
                                        <i className="icon mbc-icon edit fill"></i>
                                    </button>
                                    <button className={classNames('grip-setting-btn', Styles.btnAdd)}  
                                        onClick={() => onAddColumn(table)}
                                    >
                                        <i className="icon mbc-icon plus"></i>
                                    </button>
                                    <button className={classNames('grip-setting-btn', Styles.btnDelete)} 
                                        onClick={() => removeColumn(table, index)}
                                    >
                                        <i className="icon delete"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
            </div>
        </foreignObject>
        </>
    );
}

export default GraphTable;
