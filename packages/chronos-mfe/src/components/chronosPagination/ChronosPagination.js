import React, { useState, useEffect } from 'react';
// import from DNA Container
import Pagination from 'dna-container/Pagination';
// App components
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';

const ChronosPagination = ({projects, setForecastProjects}) => {
  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  // get results
  const getResults = (action) => {
    const showProgressIndicator = ['pagination'].includes(action);

    showProgressIndicator && ProgressIndicator.show();

    let results = projects?.length > 0 ? projects : [];

    setForecastProjects(
      results.slice(
        currentPageOffset > results.length ? 0 : currentPageOffset,
        currentPageOffset + maxItemsPerPage < results.length ? currentPageOffset + maxItemsPerPage : results.length,
      ),
    );
    setTotalNumberOfPages(Math.ceil(results.length / maxItemsPerPage) === 0 ? 1 : Math.ceil(results.length / maxItemsPerPage));
    setCurrentPageNumber(
      currentPageNumber > Math.ceil(results.length / maxItemsPerPage)
        ? Math.ceil(results.length / maxItemsPerPage) > 0
          ? Math.ceil(results.length / maxItemsPerPage)
          : 1
        : currentPageNumber,
    );
    showProgressIndicator && ProgressIndicator.hide();
  };

  useEffect(() => {
    getResults('pagination');
  }, [maxItemsPerPage, currentPageOffset, currentPageNumber]); // eslint-disable-line react-hooks/exhaustive-deps

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffset = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffset);
  };
  const onPaginationNextClick = () => {
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffset);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  return (
    <Pagination
      totalPages={totalNumberOfPages}
      pageNumber={currentPageNumber}
      onPreviousClick={onPaginationPreviousClick}
      onNextClick={onPaginationNextClick}
      onViewByNumbers={onViewByPageNum}
      displayByPage={true}
    />
  );
}

export default ChronosPagination;