import * as React from 'react';
import { Envs } from '../../..//globals/Envs';
import Styles from './NotebookFrame.scss';

export function NotebookWorkspace(props: any) {
  const isNeed = props.alternateNotebookisNeed;
  const userName = props.alternateNotebookUserName;
  // const origin = window.location.origin;
  // tslint:disable-next-line: no-unused-expression
  const alternateUrl = `${Envs.JUPYTER_NOTEBOOK_URL}/user/${userName}/lab`;
  return (
    <React.Fragment>
      <div className={Styles.notebookframe}>
        <iframe src={isNeed ? alternateUrl : Envs.JUPYTER_NOTEBOOK_URL} title="Notebook Workspace" />
      </div>
    </React.Fragment>
  );
}
