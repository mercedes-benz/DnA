import React, { forwardRef, Ref } from 'react';
import Styles from './NewSandbox.scss';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { INotebookInfo } from 'globals/types';
import { history } from '../../../router/History';
import { SESSION_STORAGE_KEYS } from 'globals/constants';

export interface INewSanboxProps {
  namePrefix: string;
  isNotebookCreationSuccess?: (status: boolean, notebookdata: INotebookInfo) => void;
  inComputeTab?: boolean;
}

export interface INewSandBoxRef {
  validateAndCreateSandBox: () => void;
}

const Newsandbox = forwardRef((props: INewSanboxProps, ref: Ref<INewSandBoxRef>) => {

  return (
    <React.Fragment>
      <div className={Styles.sandboxpanel}>
        {props.inComputeTab ? (
          ''
        ) : (
          <>   
            <h3> Jupyter Notebook Setup Guide </h3>
            <p>
            To work with Jupyter Notebooks, you need to create a Codespace using the{' '}
            <strong>Jupyter Notebook</strong> recipe.
            </p>
          <p>
      👉 Click the link below to go to the Codespaces dashboard and start your workspace:
          </p>
         <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={() => {history.push('/codespaces'); sessionStorage.setItem(SESSION_STORAGE_KEYS.NAVIGATE_CODESPACE_RECIPE, "JupyterNotebook")}}>
              Go to Codespaces
            </button>
          </>
        )}
      </div>
    </React.Fragment>
  );
});

export default Newsandbox;
