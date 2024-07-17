import React from 'react';
import { titleHeight, fieldHeight } from '../../data/settings';
import Styles from './tableNav.scss';
import classNames from 'classnames';

const TableNav = ({ tables, onTableSelected }) => {

    const height = tables.length * fieldHeight + titleHeight;
    return (

        <div>
            <div
                className={classNames('table', Styles.tableNav)}
                style={{  height: height}}
            >
                <div
                    className={classNames("table-title", Styles.tableTitle)}
                    style={{
                        height: titleHeight,
                    }}
                >
                    <span className="table-name" >{`Tables (${tables?.length})`}</span>

                </div>
                {tables &&
                    tables.map((field, index) => (
                        <div
                            className={classNames("popover", Styles.columns)}
                            key={field.tableName}
                        >
                            <div
                                className={`row`}
                                key={field.tableName + index}
                                style={{ height: fieldHeight, lineHeight: `${fieldHeight}px` }}
                                onClick={() => { onTableSelected(field) }}
                            >
                                <div className="field-content">
                                    <div className={Styles.columnName} tooltip-data={field.tableName}>
                                        {field.tableName}
                                    </div>

                                </div>
                            </div>
                        </div>
                    ))}
            </div>

        </div>


    );
};

export default TableNav;
