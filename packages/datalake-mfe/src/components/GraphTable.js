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
            x={table.x}
            y={table.y}
            width={tableWidth}
            height={height}
            key={table.id}
            onMouseDown={e => onTableMouseDown(e, table)}
            // onMouseUp={(e) => onTableMouseUp(e, table)}
            onContextMenu={handlerContextMenu}
        >
            <div
                className={`table ${editable ? 'editable' : ''} ${
                    tableSelectedId === table.id ? 'table-selected' : ''
                }`}
                onMouseOver={() => setTableSelectId(table.id)}
                onMouseOut={() => setTableSelectId(null)}
            >
                <div
                    className="table-title"
                    style={{
                        height: titleHeight,
                        lineHeight: `${titleHeight}px`,
                    }}
                >
                    <span className="table-name">{table.name}</span>

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
                            key={field.id}
                            // content={<RenderTableTips field={field} />}
                        >
                            <div
                                className={`row ${editable ? 'editable' : ''}`}
                                key={field.id}
                                tableid={table.id}
                                fieldid={field.id}
                                style={{ height: fieldHeight, lineHeight: `${fieldHeight}px` }}
                            >
                                <div
                                    className="start-grip grip"
                                    onMouseDown={onGripMouseDown}
                                ></div>
                                <div className="field-content">
                                    <div>
                                        {field.name}
                                        {field.pk && (
                                            <img
                                                alt="pk"
                                                style={{
                                                    width: 10,
                                                    height: 10,
                                                    marginLeft: 4,
                                                }}
                                                src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB30lEQVR4AYySg44eURxHm75MbdvmCzQso0a1bdsKatu2MTNre3fMT6c3t1gjyS+6Oef+1QpoMgVXR+4X4W/2131vEZwIs2UakjQPe5+JKo4Ttx8DKSnJuTD8aJOCmnBYso2o8tS/3wHIOdWPt/v6PWlCIMuWcFC4nLBsr8hDpOTicFIJnayD3aWkUYEoWcIyRaukJG49xEnfgpc1jmT4jPSdXWm8guCXqGATQlBDtBpbGYP5ua9o7zLK0nYNwq1F9pe/nCuAlVjfBuFlTMPPmYOjTsX41JO4sQ5lRTvur++p1d/39VGUv5pPTL8ofusn4Eni9xnYnwdivOsuYXVVBx5s7PUTwdUUiOFYxK0H+IVLML+3xfvZBz93DQXnBpNwToqyz6Gu7sCTLb2+/ONqXhpR+QUcbTLGuw44yiCCvM2839xdv7umR6ayrB3auo482977Q82W//5sipKv4moj0N+2xVWHEeRv4sOW7lXXN/Y909CgawrsmPEA61s/9Fdt8TMnC3gd77d0129t6XekUbhmBXH3NZYyAi9jLF7eat5u7Gbe2dbvYHOwFORdHP4pqHiJn7eCoPgE7zZ3N+7v7L+rJfDvgQ04Ndeu+uI0s6dnJhi/Bvn50FTrdpAEsRgA3aj/RFymC/IAAAAASUVORK5CYII="
                                            />
                                        )}
                                        {(field.unique || field.index) && (
                                            <img
                                                alt="index"
                                                style={{
                                                    width: 10,
                                                    height: 10,
                                                    marginLeft: 4,
                                                }}
                                                src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAA7UlEQVR4AW2Qg26GURQEb/rWtW3bts2otm27jdpouv39YTeeuTjH2FNdUo1a4gBB/KVaFQt+Zp9bkFJe5Yo3OKBahUJSZ60CX8KLbHMhoZIfsq1Kta5eVJel3LDIEA+kYxHeWBPwSn20ckkMQRxWXTLEEh1MMMsgzWwSS9Rh4Hs1jHJKJ0PM0U6jcDzRu4HLf7lmgUYJi5RzzLlw3EbI5o51aT3drJDwHnEaSyIJKyGnz+ijll7hxLfIRvt6vq9op4ZhlnU6ptK5Xh7oYZBF4j5jy4wzFWv6HrskfMTnG7dkRWbcJb8kvqWkGtf8ARG87I1KSSe4AAAAAElFTkSuQmCC"
                                            />
                                        )}
                                    </div>
                                    <div className="field-type">{field.type}</div>
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
