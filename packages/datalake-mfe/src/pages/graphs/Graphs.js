import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './graphs.scss';
// import from DNA Container
import Modal from 'dna-container/Modal';
import Pagination from 'dna-container/Pagination';
// App components
import NoProjectScreen from '../../components/noProjectScreen/NoProjectScreen';
import DatalakeProjectCard from '../../components/datalakeProjectCard/DatalakeProjectCard';
import DatalakeProjectForm from '../../components/datalakeProjectForm/DatalakeProjectForm';
import Spinner from '../../components/spinner/Spinner';
import { getQueryParameterByName } from '../../utilities/utils';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import { datalakeApi } from '../../apis/datalake.api';

const Graphs = ({ user }) => {
  const [createProject, setCreateProject] = useState(false);
  const [graphs, setGraphs] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch all datalake projects
  const getDatalakeProjects = () => {
    datalakeApi.getDatalakeProjectsList(currentPageOffset, maxItemsPerPage)
      .then((res) => {
        if(res.status !== 204) {
          if (res.data.data) {
            const results = [...res.data.data].sort((projectA, projectB) => {
              return (projectA.projectName.toLowerCase() > projectB.projectName.toLowerCase()) ? 1 : (projectB.projectName.toLowerCase() > projectA.projectName.toLowerCase() ? -1 : 0);
            });
            setGraphs(results);
            // setGraphs(res.data.data);
            const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          }
        } else {
          setGraphs([]);
          setTotalNumberOfPages(1);
          setCurrentPageNumber(1);
          setCurrentPageOffset(0);
        }
        setLoading(false);
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error while fetching datalake projects',
          'alert',
        );
        setLoading(false);
      });
  }

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

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
    getDatalakeProjects();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const handleRefresh = ()=>{
    getDatalakeProjects();
  };

    return (
      <>
        <div className={classNames(Styles.mainPanel)}>
          <div className={classNames(Styles.wrapper)}>
            {loading ? <Spinner /> : null}
            {(!loading && graphs.length === 0) ? <NoProjectScreen user={user} openCreateProjectModal={() => setCreateProject(true)} /> : null}
            {!loading && graphs.length > 0 ? 
              <>
                <div className={classNames(Styles.caption)}>
                  <div>
                    <button className="btn btn-text back arrow" type="submit" onClick={() => { history.back() }}>Back</button>
                    <h3>My Data Lakehouse Projects</h3>
                  </div>
                  <div className={classNames(Styles.listHeader)}>
                    {graphs && graphs?.length ? (
                      <React.Fragment>
                        <button
                          className={'btn btn-primary'}
                          type="button"
                          onClick={() => { setCreateProject(true)}}
                        >
                          <i className="icon mbc-icon plus" />
                          <span>Create Datalake Project</span>
                        </button>
                      </React.Fragment>
                    ) : null}
                  </div>
                </div>
                <div className={Styles.allProjectContent}>
                  <div className={Styles.newProjectCard} onClick={() => { setCreateProject(true)}}>
                    <div className={Styles.addicon}> &nbsp; </div>
                    <label className={Styles.addlabel}>Create new project</label>
                  </div>
                  {graphs?.map((graph) => {
                    return (
                      <DatalakeProjectCard
                        key={graph.id}
                        graph={graph}
                        user={user}
                        onRefresh = {handleRefresh}
                      />
                    );
                  })}
                </div>
                {graphs?.length > 0 ? 
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
                title={'Create Datalake Project'}
                showAcceptButton={false}
                showCancelButton={false}
                modalWidth={'60%'}
                buttonAlignment="right"
                show={createProject}
                content={<DatalakeProjectForm edit={false} onSave={() => setCreateProject(false)} user={user} />}
                scrollableContent={false}
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
}

export default Graphs;
