import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './exception-list-tab.scss';
import Pagination from 'dna-container/Pagination';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';

const ExceptionListTab = () => {
  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [appliedSearchTerm, setAppliedSearchTerm] = useState('');
  const [updatingProject, setUpdatingProject] = useState('');

  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const getExemptions = () => {
    ProgressIndicator.show();
    setLoading(true);
    const offset = (currentPageNumber - 1) * maxItemsPerPage;
    CodeSpaceApiClient.getResourceCapExemptions(offset, maxItemsPerPage, appliedSearchTerm)
      .then((res) => {
        setLoading(false);
        ProgressIndicator.hide();
        const records = Array.isArray(res?.data?.data) ? res.data.data : [];
        const totalCount = res?.data?.totalCount || records.length;
        setProjects(records);
        setTotalNumberOfPages(Math.ceil(totalCount / maxItemsPerPage) || 1);
      })
      .catch((err) => {
        setLoading(false);
        ProgressIndicator.hide();
        setProjects([]);
        Notification.show(err?.response?.data?.errors?.[0]?.message || err?.message || 'Something went wrong.', 'alert');
      });
  };

  useEffect(() => {
    getExemptions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, appliedSearchTerm]);

  const onPaginationPreviousClick = () => {
    setCurrentPageNumber(currentPageNumber - 1);
  };
  const onPaginationNextClick = () => {
    setCurrentPageNumber(currentPageNumber + 1);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setMaxItemsPerPage(pageNum);
  };

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPageNumber(1);
    setAppliedSearchTerm(searchTerm.trim());
  };

  const onExemptionChange = (project, env, exempt) => {
    setUpdatingProject(project.projectName + env);
    ProgressIndicator.show();
    CodeSpaceApiClient.updateResourceCapExemption(project.projectName, env, exempt)
      .then(() => {
        ProgressIndicator.hide();
        setUpdatingProject('');
        getExemptions();
        Notification.show(
          `${project.projectName} is ${exempt ? 'added to' : 'removed from'} the ${
            env === 'prod' ? 'Production' : 'Staging'
          } exception list.`,
        );
      })
      .catch((err) => {
        ProgressIndicator.hide();
        setUpdatingProject('');
        Notification.show(err?.response?.data?.errors?.[0]?.message || err?.message || 'Something went wrong.', 'alert');
      });
  };

  return (
    <React.Fragment>
      <div className={Styles.headerRow}>
        <p className={Styles.info}>
          Projects on the exception list keep the resource values from their values file and are not capped. The
          exception applies to every workspace of the project and is set separately for Staging and Production.
        </p>
        <form className={Styles.searchForm} onSubmit={onSearchSubmit}>
          <div className={classNames('input-field-group', Styles.searchField)}>
            <input
              type="text"
              className="input-field"
              value={searchTerm}
              placeholder="Search project"
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <button className="btn btn-primary" type="submit">
            Search
          </button>
        </form>
      </div>
      <div className={Styles.content}>
        <div>
          {!loading && projects?.length === 0 && <div className={Styles.csempty}>No codespace projects found.</div>}
          {!loading && projects?.length > 0 && (
            <>
              <table className={classNames('ul-table')}>
                <thead>
                  <tr className={classNames('header-row')}>
                    <th>
                      <label>Project Name</label>
                    </th>
                    <th>
                      <label>Owner</label>
                    </th>
                    <th className={Styles.countColumn}>
                      <label>Workspaces</label>
                    </th>
                    <th className={Styles.checkColumn}>
                      <label>Staging (int)</label>
                    </th>
                    <th className={Styles.checkColumn}>
                      <label>Production (prod)</label>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {projects.map((project) => (
                    <tr key={project.projectName} className={classNames('data-row')}>
                      <td>{project.projectName}</td>
                      <td>
                        {project?.projectOwner
                          ? `${project.projectOwner.firstName || ''} ${project.projectOwner.lastName || ''}`.trim() ||
                            project.projectOwner.id
                          : '-'}
                      </td>
                      <td className={Styles.countColumn}>{project.workspaceCount}</td>
                      <td className={Styles.checkColumn}>
                        <label className={'checkbox'}>
                          <span className="wrapper">
                            <input
                              type="checkbox"
                              className="ff-only"
                              checked={project.exemptInt === true}
                              disabled={updatingProject === project.projectName + 'int'}
                              onChange={(e) => onExemptionChange(project, 'int', e.target.checked)}
                            />
                          </span>
                        </label>
                      </td>
                      <td className={Styles.checkColumn}>
                        <label className={'checkbox'}>
                          <span className="wrapper">
                            <input
                              type="checkbox"
                              className="ff-only"
                              checked={project.exemptProd === true}
                              disabled={updatingProject === project.projectName + 'prod'}
                              onChange={(e) => onExemptionChange(project, 'prod', e.target.checked)}
                            />
                          </span>
                        </label>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <Pagination
                totalPages={totalNumberOfPages}
                pageNumber={currentPageNumber}
                onPreviousClick={onPaginationPreviousClick}
                onNextClick={onPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
              />
            </>
          )}
        </div>
      </div>
    </React.Fragment>
  );
};

export default ExceptionListTab;