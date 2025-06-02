import cn from 'classnames';
import React, { useState, useEffect, forwardRef, useImperativeHandle, Ref } from 'react';
import { INotebookInfo } from 'globals/types';
import Styles from './NotebookInfo.scss';

import { CodeSpaceApiClient } from '../../../../../../src/services/CodeSpaceApiClient';
import NotebookProjects from './NotebookProjects';
import { history } from '../../../../../router/History';
const classNames = cn.bind(Styles);

export interface INotebookInfoProps {
  userFirstName: string;
  notebookId: string;
  solutionId: string;
  onNoteBookLinkSuccess: (status: boolean, noteBookData: INotebookInfo) => void;
  onNoteBookLinkRemove: () => void;
}

export interface INotebookInfoRef {
  triggerNoteBookCreation: () => void;
}

const NotebookInfo = forwardRef((props: INotebookInfoProps, ref: Ref<INotebookInfoRef>) => {
  const [selectionError, setSelectionError] = useState<boolean>(false);
  const [selectedProject, setSelectedProject] = useState<any | null>(null);
  const [selectedNotebook, setSelectedNotebook] = useState<any | null>(null);



  const getWorkspaceByID = () => {
    CodeSpaceApiClient.getWorkspaceById(props?.notebookId).then((res:any) =>{
      if(res){
        setSelectedNotebook({
          projectName: res?.projectDetails.projectName,
          Descriptions: res?.projectDetails?.dataGovernance?.description,
          ProjectOwner: res?.projectDetails?.projectOwner?.id,
          WorkspaceId: res?.workspaceId
        });
    }
  });
  }

  useEffect(() => {
    setSelectionError(false);
    getWorkspaceByID();
  }, [props.notebookId]);

  useImperativeHandle(ref, () => ({
    triggerNoteBookCreation() {
        if(selectedProject){
          props.onNoteBookLinkSuccess(true, selectedProject);
          }
         else{
          setSelectionError(true);
        }
    },
  }));

  return (
    <React.Fragment>
      {props?.notebookId && !selectedNotebook ? 
      (
        <div className="text-center">
          <div className="progress infinite" />
        </div>
      ) : props.notebookId !== null ? 
      (     
   <>
  <div className={classNames(Styles.jupeterCard)}>
    <div className={Styles.jupeterIcon}>
      <i className="icon mbc-icon jupyter" />
    </div>
    <div className={Styles.jupeterCardContent}>
      <h6>
<a
  href="#"
  onClick={(e) => {
    e.preventDefault(); 
    history.push(`/codespaces/codespace/${selectedNotebook.WorkspaceId}`);
  }}
>
  {selectedNotebook?.projectName}
      </a>
        {selectedNotebook?.ProjectOwner && (
    <span> <small>{` (${selectedNotebook.ProjectOwner})`}</small></span>
        )}
      </h6>
      <label>
        {selectedNotebook.Description}
      </label>
      <div className={Styles.JuperterCardDesc}>
        {selectedNotebook.Descriptions}
      </div>
      {props.notebookId && (
        <span className={Styles.closeICon} onClick={props.onNoteBookLinkRemove}>
          <i className="icon mbc-icon close thin" />
        </span>
      )}
    </div>
  </div>
   </> ) : 
      (  <>
        <NotebookProjects
          currSolutionId={props.notebookId}
          onProjectSelection={(project) => {
            setSelectedProject(project);
            setSelectionError(false);
          }}
          showError={selectionError}
        />
        </>
      )}
    </React.Fragment>
  );
});

export default NotebookInfo;
