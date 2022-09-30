import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './ModuleUsage.scss';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
const classNames = cn.bind(Styles);
import ModuleUsageJsonList from './moduleUsage.json';

import Pagination from 'components/mbc/pagination/Pagination';
import { IModuleUsage } from 'globals/types';
import { SESSION_STORAGE_KEYS } from 'globals/constants';

const ModuleUsage = () => {
  const [totalNumberOfPages, setTotalNumberOfPages] = useState<number>(1);
  const [currentPageNumber, setCurrentPageNumber] = useState<number>(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState<number>(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [ModuleUsageDataLocal, setModuleUsageDataLocal] = useState([]);
  const [currentSortOrder, setCurrentSortOrder] = useState<string>('desc');
  const [nextSortOrder, setNextSortOrder] = useState<string>('asc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState<string>('component');

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = ModuleUsageJsonList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setModuleUsageDataLocal([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = ModuleUsageJsonList.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setModuleUsageDataLocal([...modifiedData]);
    setCurrentPageNumber(currentPageNumberTemp);
  };
  const onViewByPageNum = (pageNum: number) => {
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    const totalNumberOfPages = Math.ceil(ModuleUsageJsonList.length / pageNum);
    setTotalNumberOfPages(totalNumberOfPages);
    const modifiedData = ModuleUsageJsonList.slice(0, pageNum);
    setModuleUsageDataLocal([...modifiedData]);
  };

  // Sorting table data
  const sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      ModuleUsageDataLocal.sort((a, b) => {
        const nameA = a[columnName].toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
        const nameB = b[columnName].toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder === 'asc' ? -1 : 1;
        } else if (nameA > nameB) {
          return sortOrder === 'asc' ? 1 : -1;
        }
        return 0;
      });
      setNextSortOrder(sortOrder == 'asc' ? 'desc' : 'asc');
      setCurrentSortOrder(sortOrder);
      setCurrentColumnToSort(columnName);
    };
  };

  useEffect(() => {
    const moduleUsageComponents = ModuleUsageJsonList.sort(function (item1: any, item2: any) {
      return item2.component - item1.component;
    });
    const totalNumberOfPages = Math.ceil(moduleUsageComponents.length / maxItemsPerPage);
    setTotalNumberOfPages(totalNumberOfPages);
    const modifiedData = moduleUsageComponents.slice(0, maxItemsPerPage);
    setModuleUsageDataLocal(modifiedData);
  }, []);

  return (
    <React.Fragment>
      <div className={Styles.ModuleUsage}>
        <table className={classNames('ul-table ')}>
          <thead>
            <tr className="header-row">
              <th>
                <label
                  className={'sortable-column-header ' + (currentColumnToSort === 'component' ? currentSortOrder : '')}
                  onClick={sortByColumn('component', nextSortOrder)}
                >
                  <i className="icon sort" />
                  Components
                </label>
              </th>
              <th>
                <label
                  className={'sortable-column-header ' + (currentColumnToSort === 'version' ? currentSortOrder : '')}
                  onClick={sortByColumn('version', nextSortOrder)}
                >
                  <i className="icon sort" />
                  Version
                </label>
              </th>
              <th>
                <label
                  className={'sortable-column-header ' + (currentColumnToSort === 'license' ? currentSortOrder : '')}
                  onClick={sortByColumn('license', nextSortOrder)}
                >
                  <i className="icon sort" />
                  License
                </label>
              </th>
            </tr>
          </thead>
          <tbody>
            {ModuleUsageDataLocal.map((item: IModuleUsage, index: number) => {
              return (
                <tr key={index} className={classNames('data-row')}>
                  <td>{item.component}</td>
                  <td>{item.version}</td>
                  <td>{item.license}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {ModuleUsageDataLocal ? (
          <React.Fragment>
            <Pagination
              totalPages={totalNumberOfPages}
              pageNumber={currentPageNumber}
              onPreviousClick={onPaginationPreviousClick}
              onNextClick={onPaginationNextClick}
              onViewByNumbers={onViewByPageNum}
              displayByPage={true}
            />
            <br />
          </React.Fragment>
        ) : (
          ''
        )}
      </div>
    </React.Fragment>
  );
};

export default ModuleUsage;
