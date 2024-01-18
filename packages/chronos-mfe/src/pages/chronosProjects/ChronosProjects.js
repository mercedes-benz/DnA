import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './chronos-projects.scss';
// import from DNA Container
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
import FirstRun from '../../components/firstRun/FirstRun';
import ChronosProjectCard from '../../components/chronosProjectCard/ChronosProjectCard';
import ChronosProjectForm from '../../components/chronosProjectForm/ChronosProjectForm';
import Spinner from '../../components/spinner/Spinner';
import { getQueryParameterByName } from '../../utilities/utils';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
// Api
import { chronosApi } from '../../apis/chronos.api';

const ChronosProjects = ({ user }) => {
  const [loading, setLoading] = useState(true);
  const [forecastProjects, setForecastProjects] = useState([]);
  const [createProject, setCreateProject] = useState(false);

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  // Fetch all chronos projects
  const getChronosProjects = () => {
    chronosApi.getAllForecastProjects()
      .then((res) => {
        if(res.status !== 204) {
          if (res.data.records) {
            const results = [...res.data.records].sort((projectA, projectB) => {
              return (projectA.name.toLowerCase() > projectB.name.toLowerCase()) ? 1 : (projectB.name.toLowerCase() > projectA.name.toLowerCase() ? -1 : 0);
            });
            setForecastProjects(results);
            const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          }
        } else {
          setForecastProjects([]);
          setTotalNumberOfPages(1);
          setCurrentPageNumber(1);
          setCurrentPageOffset(0);
        }
        setLoading(false);
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error while fetching forecast projects',
          'alert',
        );
        setLoading(false);
      });
  }

  const handleRefresh = () => {
    getChronosProjects();
  }

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetTemp = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onPaginationNextClick = () => {
    const currentPageOffsetTemp = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    getChronosProjects();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          {loading ? <Spinner /> : null}
          {(!loading && forecastProjects.length === 0) ? <FirstRun openCreateProjectModal={() => setCreateProject(true)} user={user} /> : null}
          {(!loading && forecastProjects.length !== 0) ?
            <>
              <Breadcrumb>
                <li>Chronos Forecasting</li>
              </Breadcrumb>

              <div className={classNames(Styles.caption)}>
                <h3>My Forecasting Projects</h3>
                <div className={classNames(Styles.listHeader)}>
                  {forecastProjects?.length ? (
                    <React.Fragment>
                      <button
                        className={'btn btn-primary'}
                        type="button"
                        onClick={() => { setCreateProject(true)}}
                      >
                        <i className="icon mbc-icon plus" />
                        <span>Create Forecasting Project</span>
                      </button>
                    </React.Fragment>
                  ) : null}
                </div>
              </div>

              <div className={Styles.allProjectContent}>
                <div className={Styles.newProjectCard} onClick={() => setCreateProject(true)}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create new project</label>
                </div>
                {forecastProjects?.map((project, index) => {
                  return (
                    <ChronosProjectCard
                      key={index}
                      project={project}
                      onRefresh={handleRefresh}
                    />
                  );
                })}
              </div>
              {forecastProjects?.length > 0 ? 
                <Pagination
                  totalPages={totalNumberOfPages}
                  pageNumber={currentPageNumber}
                  onPreviousClick={onPaginationPreviousClick}
                  onNextClick={onPaginationNextClick}
                  onViewByNumbers={onViewByPageNum}
                  displayByPage={true}
                /> : null
              }
            </> : null
          }
        </div>
      </div>
      { createProject &&
        <Modal
          title={'Create Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={createProject}
          content={<ChronosProjectForm edit={false} onSave={() => {setCreateProject(false); handleRefresh()}} />}
          scrollableContent={true}
          onCancel={() => setCreateProject(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </>
  );
};
export default ChronosProjects;
