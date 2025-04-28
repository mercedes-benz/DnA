import cn from 'classnames';
import React, { useState, useRef, useEffect, forwardRef, useImperativeHandle, Ref } from 'react';
import { ApiClient } from '../../../../../services/ApiClient';
import { INotebookInfo } from 'globals/types';
import Styles from './NotebookInfo.scss';
import Newsandbox, { INewSandBoxRef } from 'components/mbc/newSandbox/NewSandbox';
import { getDateFromTimestamp } from '../../../../../services/utils';
import { Link } from 'react-router-dom';

const classNames = cn.bind(Styles);

export interface INotebookInfoProps {
  userFirstName: string;
  notebookId: string;
  solutionId: string;
  onNoteBookCreationSuccess: (status: boolean, noteBookData: INotebookInfo) => void;
  onNoteBookLinkRemove: () => void;
}

export interface INotebookInfoRef {
  triggerNoteBookCreation: () => void;
}

const NotebookInfo = forwardRef((props: INotebookInfoProps, ref: Ref<INotebookInfoRef>) => {
  const newSandBoxRef = useRef<INewSandBoxRef>(null);
  const [notebookInfo, setNotebookInfo] = useState<INotebookInfo>();
  const [restrictNotebookLink, setRestrictNotebookLink] = useState<boolean>(false);

  const getNotebookInfo = () => {
    ApiClient.getNotebooksDetails(props.notebookId).then((res: INotebookInfo) => {
      if (Array.isArray(res)) {
        setNotebookInfo(null);
      } else {
        setNotebookInfo(res);
        if (res.solutionId) {
          setRestrictNotebookLink(props.solutionId.toLowerCase() !== res.solutionId.toLowerCase());
        }
      }
    });
  };

  useEffect(() => {
    getNotebookInfo();
  }, [props.notebookId]);

  useImperativeHandle(ref, () => ({
    triggerNoteBookCreation() {
      if (notebookInfo) {
        if (!restrictNotebookLink) {
          props.onNoteBookCreationSuccess(true, notebookInfo);
        }
      } else {
        newSandBoxRef.current.validateAndCreateSandBox();
      }
    },
  }));

  return (
    <React.Fragment>
      {notebookInfo ? (
        <>
          <div className={classNames(Styles.jupeterCard)}>
            <div className={Styles.jupeterIcon}>
              <i className="icon mbc-icon jupyter" />
            </div>
            <div className={Styles.jupeterCardContent}>
              <h6>{notebookInfo.name}</h6>
              <label>
                Created on {getDateFromTimestamp(notebookInfo.createdOn, '.')} by {notebookInfo?.createdBy?.firstName}
              </label>
              <div className={Styles.JuperterCardDesc}>{notebookInfo.description}</div>
              {restrictNotebookLink && (
                <Link target="_blank" to={`/summary/${notebookInfo.solutionId}`}>
                  Open linked solution
                </Link>
              )}
              {props.notebookId && (
                <span className={Styles.closeICon} onClick={props.onNoteBookLinkRemove}>
                  <i className="icon mbc-icon close thin" />
                </span>
              )}
            </div>
          </div>
        </>
      ) : notebookInfo !== null ? (
        <div className="text-center">
          <div className="progress infinite" />
        </div>
      ) : (
        <div className={Styles.noteBookWrapper}>
          
             <p>
        To create and use Jupyter Notebooks, you need to launch a{' '}
        <strong>Codespace</strong> with the <strong>Jupyter Notebook</strong> recipe.
      </p>
      <p>
        👉 Click below to open the Codespaces dashboard and get started:
      </p>

       <Link to="/codespaces?jupyter=true">
        Open Codespaces
      </Link>
          <Newsandbox
            ref={newSandBoxRef}
            namePrefix={props.userFirstName}
            inComputeTab={true}
            isNotebookCreationSuccess={props.onNoteBookCreationSuccess}
          />
        </div>
      )}
    </React.Fragment>
  );
});

export default NotebookInfo;
