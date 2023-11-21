import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Styles from './graphs.scss';
// import from DNA Container
import Modal from 'dna-container/Modal';
// App components
import NoProjectScreen from '../../components/noProjectScreen/NoProjectScreen';
import DatalakeProjectCard from '../../components/datalakeProjectCard/DatalakeProjectCard';
import DatalakeProjectForm from '../../components/datalakeProjectForm/DatalakeProjectForm';
import { getProjects } from '../../redux/projects.services';

const Graphs = ({ user }) => {
    const dispatch = useDispatch();
    const graphs = useSelector(state => state.graphs.projects);
    const [createProject, setCreateProject] = useState(false);

    useEffect(() => {
        dispatch(getProjects());
    }, [dispatch]);

    return (
      <>
        <div className={classNames(Styles.mainPanel)}>
          <div className={classNames(Styles.wrapper)}>
            {graphs && graphs.length > 0 ? 
              <>
                <div className={classNames(Styles.caption)}>
                  <div>
                    <button className="btn btn-text back arrow" type="submit" onClick={() => { history.back() }}>Back</button>
                    <h3>My Datalake Projects</h3>
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
                      />
                    );
                  })}
                </div>
              </> : <NoProjectScreen user={user} openCreateProjectModal={() => setCreateProject(true)} />
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
                content={<DatalakeProjectForm edit={false} onSave={() => setCreateProject(false)} />}
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
