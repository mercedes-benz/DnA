import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './chronos-projects.scss';
// import from DNA Container
import Modal from 'dna-container/Modal';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
import FirstRun from '../../components/firstRun/FirstRun';
import ChronosProjectCard from '../../components/chronosProjectCard/ChronosProjectCard';
import ChronosProjectForm from '../../components/chronosProjectForm/ChronosProjectForm';
import Spinner from '../../components/spinner/Spinner';
import ChronosPagination from '../../components/chronosPagination/ChronosPagination';
// Api
import { chronosApi } from '../../apis/chronos.api';

const ChronosProjects = ({ user }) => {
  const [loading, setLoading] = useState(true);
  const [originalProjects, setOriginalProjects] = useState([]);
  const [forecastProjects, setForecastProjects] = useState([]);
  const [createProject, setCreateProject] = useState(false);

  // Fetch all chronos projects
  const getChronosProjects = () => {
    chronosApi.getAllForecastProjects()
      .then((res) => {
        if(res.status !== 204) {
          if (res.data.records) {
            const results = [...res.data.records].sort((projectA, projectB) => {
              return projectA.createdOn.toLowerCase() === projectB.createdOn.toLowerCase() ? 0 : -1;
            });
            setOriginalProjects(results);
            setForecastProjects(results);
          }
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
  
  useEffect(() => {
    getChronosProjects();
  }, []);

  const handleRefresh = () => {
    getChronosProjects();
  }

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
              {forecastProjects?.length > 0 ? <ChronosPagination projects={originalProjects} setForecastProjects={setForecastProjects} /> : null}
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
};
export default ChronosProjects;
