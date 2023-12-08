/* eslint-disable react/no-unknown-property */ 
import React from 'react';
import { tableWidth, titleHeight, commentHeight, fieldHeight } from '../data/settings';
import { useSelector } from 'react-redux';

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
    // const dispatch = useDispatch();
    const { table, onTableMouseDown, onGripMouseDown, tableSelectedId, setTableSelectId } = props;
    const { version } = useSelector(state => state.graph);

    const editable = version === 'currentVersion';

    const handlerContextMenu = e => {
        e.preventDefault();
        e.stopPropagation();
    };

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
                              onClick={() => props.onCollabClick(table)}
                            >
                                <i className="icon mbc-icon profile"></i>
                            </button>
                            <button 
                            // onClick={() => dispatch(setEditingTable(table))}
                            >
                                <i className="icon mbc-icon edit fill"></i>
                            </button>
                            <button onClick={() => alert('delete')}>
                                <i className="icon delete"></i>
                            </button>
                        </div>
                    )}
                </div>
                {table.columns &&
                    table.columns.map((field) => (
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
                                    <div>
                                        {field.columnName}
                                    </div>
                                    <div className="field-type">{field.dataType}</div>
                                </div>
                                <div className="grip-setting">
                                    <button className="grip-setting-btn" 
                                    // onClick={() => dispatch(setEditingField({ field, table }))}
                                    >
                                        <i className="icon mbc-icon edit fill"></i>
                                    </button>
                                    <button className="grip-setting-btn" 
                                    // onClick={() => dispatch(addField(table, index))}
                                    >
                                        <i className="icon mbc-icon plus"></i>
                                    </button>
                                    <button className="grip-setting-btn" 
                                    // onClick={() => dispatch(removeField(table, index))}
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
