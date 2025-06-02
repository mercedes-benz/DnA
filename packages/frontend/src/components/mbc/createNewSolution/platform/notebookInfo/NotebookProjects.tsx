import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './NotebookProjects.scss';
import { CodeSpaceApiClient } from '../../../../../services/CodeSpaceApiClient';
import { history } from '../../../../../router/History';
import { SESSION_STORAGE_KEYS } from 'globals/constants';

const classNames = cn.bind(Styles);

export interface INotebookProjectsProps {
  currSolutionId: string;
  onProjectSelection?: (project: any) => void;
  showError: boolean;
}

const NotebookProjects = (props: INotebookProjectsProps) => {
  const [notebookProjects, setNotebookProjects] = useState<any | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProject, setSelectedProject] = useState<any | null>(null);

  const fetchNotebookProjects = () => {
    CodeSpaceApiClient.getWorkspaceByRecipe().then((res: any) => {
      if (res?.records) {
        setNotebookProjects(res.records);
      } else {
        setNotebookProjects([]);
      }
    });
  };

  useEffect(() => {
    setNotebookProjects(null);
    fetchNotebookProjects();
  }, []);

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
  };

  const onProjectSelect = (project: any) => () => {
    setSelectedProject(project);
    props.onProjectSelection?.(project);
  };

  const filteredProjects = (notebookProjects || []).filter(
    (project: any) =>
      project?.projectDetails?.projectName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <React.Fragment>
      {
      notebookProjects === null ? (
        <div className="text-center">
          <div className="progress infinite" />
        </div>
      ) : 
      notebookProjects.length ? (
        <div className={Styles.projectListPanel}>
          <p>Please select the Notebook project that you want to link to the solution.</p>
          <div className={Styles.searchPanel}>
            <input
              type="text"
              className={classNames(Styles.searchInputField)}
              placeholder="Search Project"
              value={searchTerm}
              onChange={onSearchInputChange}
              maxLength={200}
            />
            <button>
              <i className="icon mbc-icon search" />
            </button>
          </div>
          <ul className={classNames('list-item-group divider mbc-scroll', Styles.projectList)}>
            {filteredProjects.map((project : any, index : any) => (
              <li
                key={`notebook-${index}`}
                className={classNames(
                  'list-item',
                  selectedProject?.id === project.id ? Styles.active : ''
                )}
                onClick={onProjectSelect(project)}
              >
                <div className="item-text-wrap">
                  <h3 className="item-text-title">{project.projectDetails.projectName} ({project.projectDetails.projectOwner.id})</h3>
                  <label className="item-text">
                 {project.projectDetails.dataGovernance.description}
                  </label>
                </div>
              </li>
            ))}
          </ul>
        </div>
      ) : 
      (
          <div className={Styles.noteBookWrapper}>
                  
                     <p> <strong>You don't have any existing Jupyter-Notebook.</strong></p>
                <p>To create and use Jupyter Notebooks, you need to launch a{' '}
                <strong>Codespace</strong> with the <strong>Jupyter Notebook</strong> recipe.
              </p>
              <p>
                👉 Click below to open the Codespaces dashboard and get started:
              </p>
              <button
                      className={'btn btn-tertiary'}
                      type="button"
                      onClick={() => {history.push('/codespaces'); sessionStorage.setItem(SESSION_STORAGE_KEYS.NAVIGATE_CODESPACE_RECIPE, "JupyterNotebook")}}>
                      Go to Codespaces
                    </button>
                </div>
      )}
    </React.Fragment>
  );
};

export default NotebookProjects;
