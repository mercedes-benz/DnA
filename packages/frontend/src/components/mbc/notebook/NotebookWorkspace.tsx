import * as React from 'react';
import { Envs } from 'globals/Envs';
import Styles from './NotebookFrame.scss';

interface INotebookWorkspaceProps {
  isLabView: boolean;
  enableFullScreenMode: boolean;
  userName: string;
}

export default function NotebookWorkspace(props: INotebookWorkspaceProps) {
  const notebookUrl = props.isLabView
    ? `${Envs.JUPYTER_NOTEBOOK_URL}/user/${props.userName}/lab`
    : Envs.JUPYTER_NOTEBOOK_URL;
  return (
    <div className={Styles.notebookframe}>
      <iframe
        className={props.enableFullScreenMode ? Styles.fullscreen : ''}
        src={notebookUrl}
        title="Notebook Workspace"
        allow="clipboard-read; clipboard-write"
      />
    </div>
  );
}
