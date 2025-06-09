import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams, useHistory } from 'react-router-dom';
import Styles from './code-space-administration.scss';
import Caption from 'dna-container/Caption';
// App components
import Tabs from '../../common/modules/uilab/js/src/tabs';
import RecipesTab from '../recipesTab/RecipesTab';
import { USER_ROLE } from '../../Utility/constants';
import SoftwareTab from '../softwareTab/SoftwareTab';
import AdditionalServicesTab from '../additionalServicesTab/AdditionalServicesTab';

const tabs = {
  recipesTab: {},
  softwareTab: {},
  additionalServicesTab: {},
};

const CodeSpaceAdministration = ({ user }) => {
  const history = useHistory();
  const { tabName } = useParams();

  const isAdmin = user.roles.find((role) => role.id === USER_ROLE.ADMIN) !== undefined ||
                  user.roles.find((role) => role.id === USER_ROLE.CODESPACEADMIN) !== undefined;

  const [currentTab, setCurrentTab] = useState(tabName !== undefined ? tabName : 'recipesTab');
  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  useEffect(() => {
    Tabs.defaultSetup();
  }, [user]);

  useEffect(() => {
    !isAdmin && history.push('/manageRecipes');
    //eslint-disable-next-line
  }, [isAdmin]);

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      setCurrentTab(id);
    }
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title="Administration" onBackClick={() => history.push('/manageRecipes')} />
        <div id="recipe-administration-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={classNames('tab', tabName === undefined && 'active', tabName !== undefined && currentTab === tabName && 'active')}>
                  <a href="#tab-content-1" id="recipesTab" ref={elementRef} onClick={setTab}>
                    All Recipes
                  </a>
                </li>
                <li className={classNames('tab', tabName !== undefined && tabName === 'softwareTab' && 'active')}>
                  <a
                    href="#tab-content-2"
                    id="softwareTab"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Software
                  </a>
                </li>
                <li className={classNames('tab', tabName !== undefined && tabName === 'additionalServicesTab' && 'active')}>
                  <a
                    href="#tab-content-3"
                    id="additionalServicesTab"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[2] = ref;
                    }}
                    onClick={setTab}
                  >
                    Additional Services
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              {currentTab === 'recipesTab' ? (
                <RecipesTab />
              ): null}
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'softwareTab' ? (
                <SoftwareTab />
              ) : null}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'additionalServicesTab' ? (
                <AdditionalServicesTab />
              ) : null}
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.mandatoryInfo}>* mandatory fields</div>
    </>
  );
};
export default CodeSpaceAdministration;
