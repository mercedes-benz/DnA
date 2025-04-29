import * as React from 'react';
//  @ts-ignore
import classNames from 'classnames';
import Caption from '../shared/caption/Caption';

// @ts-ignore
import { Envs } from 'globals/Envs';
import Styles from './Notebook.scss';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { history } from '../../../router/History';

const Notebook = () => {
  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <Caption title="Jupyter Notebook Setup Guide" />
        </div>
        <div className={Styles.content}>
          <div className={Styles.NoSubscription}>
            <div className={Styles.addApiKeys}>
              <React.Fragment>
                <p>
                  {' '}
                  To work with Jupyter Notebooks, you need to create a Codespace using the{' '}
                  <strong>Jupyter Notebook</strong> recipe.
                </p>
              </React.Fragment>
            </div>
            <div className={Styles.subsriContent}>
              <div className={Styles.malwaresubscriptionListEmpty}>
                <React.Fragment>
                  <button
                    className={Styles.addNewSubcibtn + ' btn btn-tertiary'}
                    onClick={() => {
                      history.push('/codespaces');
                      sessionStorage.setItem(SESSION_STORAGE_KEYS.NAVIGATE_CODESPACE_RECIPE, 'JupyterNotebook');
                    }}
                    type="button"
                  >
                    Go to Codespaces
                  </button>
                </React.Fragment>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Notebook;
