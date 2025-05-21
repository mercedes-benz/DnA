import cn from 'classnames';
import * as React from 'react';
import { getParams, getPath, getQueryParam } from '../../Utility/utils';

// @ts-ignore
import InputFields from '../../common/modules/uilab/js/src/input-fields';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../common/modules/uilab/js/src/tabs';
import ConfirmModal from 'dna-container/ConfirmModal';

// @ts-ignore
import * as _ from 'lodash';
// import { IUserInfo } from 'globals/types';
import SelectBox from 'dna-container/SelectBox';
import Styles from './SecurityConfig.scss';
import Entitlement from './entitlement/Entitlement';
import Caption from 'dna-container/Caption';
// @ts-ignore
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { history } from '../../store';

const classNames = cn.bind(Styles);
// export interface ICreateNewSecurityConfigState {
//   id: string;
//   projectName: string;
//   editMode: boolean;
//   currentTab: string;
//   nextTab: string;
//   clickedTab: string;
//   // isSaved: boolean;
//   saveActionType: string;
//   tabClassNames: Map<string, string>;
//   currentState: any;
//   showAlertChangesModal: boolean;
//   config: any;
//   readOnlyMode: boolean;
//   editModeNavigateModal: boolean;
//   showStagingModal: boolean;
// }

// export interface ICreateNewSecurityConfigProps {
//   user: IUserInfo;
// }

export default class SecurityConfig extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      id: '',
      projectName: '',
      intIAM: 'false',
      prodIAM: 'false',
      showRedeployWarningModal: false,
      editMode: false,
      currentTab: 'stagingEntitlement',
      nextTab: 'productionEntitlement',
      clickedTab: '',
      saveActionType: '',
      tabClassNames: new Map(),
      currentState: null,
      showAlertChangesModal: false,
      //isSaved: false,
      config: {
        entitlements: [],
      },      
      readOnlyMode: false,
      editModeNavigateModal: false,
      showStagingModal: true,
    };
  }

  componentDidMount() {
    const params = getParams();
    let id = params?.id;
    if (id?.includes('?name=')) {
      id = params?.id.split('?name=')[0];
    }
    const name = getQueryParam('name');
    const intIAMEnabled = getQueryParam('intIAM');
    const prodIAMEnabled = getQueryParam('prodIAM');
    this.setState({ projectName: name, intIAM: intIAMEnabled, prodIAM: prodIAMEnabled });
    const path = getPath();
    SelectBox.defaultSetup();
    InputFields.defaultSetup();
    this.setState({ id: id });
    if (path?.includes('publishedSecurityconfig')) {
      this.setState({
        readOnlyMode: true,
      });
      Tabs.defaultSetup();
      this.getPublishedConfig(id, 'int');
    } else {
      this.getConfig(id, 'int');
      Tabs.defaultSetup();
    }
  }

  componentDidUpdate(prevProps, prevState) {
    const path = getPath();
  
    if (this.state.currentTab !== prevState.currentTab) {
      const envKey = this.state.currentTab === 'stagingEntitlement' ? 'int' : 'prod';
  
      SelectBox.defaultSetup();
      InputFields.defaultSetup();
  
      if (path.includes('publishedSecurityconfig')) {
        this.getPublishedConfig(this.state.id, envKey);
      } else {
        this.getConfig(this.state.id, envKey);
      }
    }
  }
  


  getPublishedConfig = (id, env) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getPublishedConfig(id, env)
      .then((res) => {
        const response = {
          ...res.data,
          entitlements: res.data.entitlements || [],
          appId: res.data.appID || '',
        };
        this.setState(
          {
            config: response,
          },
          () => {
            this.setOpenTabs();
          },
        );
        ProgressIndicator.hide();
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        ProgressIndicator.hide();
      });
  };


  getConfig = (id, env) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpaceConfig(id, env)
      .then((res) => {
        const response = {
          ...res.data,
          entitlements: res.data.entitlements || [],
          appId: res.data.appID || '',
        };
        this.setState(
          {
            config: response,
          },
          () => {
            this.setOpenTabs();
          },
        );
        ProgressIndicator.hide();
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        ProgressIndicator.hide();
      });
  };

  setOpenTabs = () => {
    const tabClasses = new Map();
    tabClasses.set('ProductionEntitlement', 'tab valid');
    tabClasses.set('StagingEntitlement', 'tab valid');
    this.setState({ tabClassNames: tabClasses });
  };

  onCancellingUpdateChanges = () => {
    document.getElementById(this.state.currentTab).click();
    const clickedTab = this.state.clickedTab;
    this.setState({
      showAlertChangesModal: false,
      clickedTab: clickedTab === 'stagingEntitlement' ? 'productionEntitlement' : 'stagingEntitlement',
      showStagingModal: clickedTab === 'stagingEntitlement' ? false : true,
    });
  };

  onAcceptUpdateChanges = () => {
    const clickedTab = this.state.clickedTab;
    this.setState({
      currentTab: clickedTab,
      saveActionType: '',
      nextTab: clickedTab === 'stagingEntitlement' ? 'productionEntitlement' : 'stagingEntitlement',
      showStagingModal: clickedTab === 'stagingEntitlement' ? true : false,
      showAlertChangesModal: false,
    });
  };

  onSaveDraft = (tabToBeSaved, config) => {
    this.setState(
      {
        config: config,
        // isSaved: true,
      },
      () => {
        if (tabToBeSaved === 'stagingEntitlement') {
          this.setState({ configStaging: config }, () => this.callApiToSave('int'));
        } else {
          this.setState({ configProduction: config }, () => this.callApiToSave('prod'));
        }        
      },
    );
  };

  onPublish = (config, env) => {
    CodeSpaceApiClient.getPublishedConfig(this.state.id, env)
      .then((res) => {
        let IAMEnabled = 'false';
        if (env === 'int') {
          IAMEnabled = this.state.intIAM
        }
        else {
          IAMEnabled = this.state.prodIAM
        }
        ((!res?.data) && IAMEnabled === 'true') ? this.setState({ showRedeployWarningModal: true }) : this.setState({ showRedeployWarningModal: false });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    this.setState(
      {
        config: config,
        // isSaved: true,
      },
      () => {
        this.callApiToSave(env, true);
      },
    );
  };

  setCurrentTab = (event) => {
    const target = event.target;
    const newState = this.state.config;
    const saveActionType = this.state.saveActionType;
    const currentState = this.state.currentState;
    const showAlertChangesModal = !this.state.readOnlyMode;
  
    if (!currentState || saveActionType === 'btn' || _.isEqual(newState, currentState)) {
      if (target.id !== this.state.currentTab) {
        this.setState({
          clickedTab: target.id,
          currentTab: target.id, 
          showStagingModal: target.id === 'stagingEntitlement', 
          showAlertChangesModal: showAlertChangesModal,
        });
      }
    }
  };
  

  callApiToSave = (env, callPublishApi) => {
    const config = this.state.config;
    const data = {
      data: {
        entitlements: config.entitlements,
        appID: config.appId,
      },
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.createOrUpdateCodeSpaceConfig(this.state.id, data, env)
      .then((response) => {
        if (response) {
          // if (response?.response?.success === 'SUCCESS') {
          //   this.setState({
          //     config: response?.data,
          //   });
          this.getConfig(this.state.id, env);
          // }
          Notification.show('Saved successfully.');

          if (callPublishApi) {
            ProgressIndicator.show();
            CodeSpaceApiClient.addCodeSpaceRequest(this.state.id, env)
              .then(() => {
                ProgressIndicator.hide();
                this.getConfig(this.state.id, env);
                Notification.show('Published successfully.');
              })
              .catch((error) => {
                ProgressIndicator.hide();
                this.showErrorNotification(error.response.status === 400 ? 'APPID and Entitlement should not be empty while publishing.' : error.message ? error.message : 'Some Error Occured');
              });
          }
        }
        ProgressIndicator.hide();
      })
      .catch((error) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error && error[0]?.length > 0 ? error[0].message : 'Some Error Occured');
      });
  };

  showErrorNotification(message) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  navigateEditOrReadOnlyMode = () => {
    if (this.state.readOnlyMode) {
      history.push(`/codespace/securityconfig/${this.state.id}?name=${this.state.projectName}?intIAM=${this.state.intIAM === 'true' ? 'true' : 'false'}?prodIAM=${this.state.prodIAM === 'true' ? 'true' : 'false'}`);
    } else {
      this.setState({
        editModeNavigateModal: true,
      });
    }
  };

  render() {
    const currentTab = this.state.currentTab;
    const { readOnlyMode, projectName } = this.state;
    const publishedSuffix = readOnlyMode ? ' (Published)' : '';
    const title = `${projectName} - Security config${publishedSuffix}`;
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <Caption title={title} />
          <span>
            <p style={{ color: 'var(--color-orange)' }}>
              <i className="icon mbc-icon alert circle"></i>
              Note: These features are currently only enabled for API recipes. They will be made available for the UI recipes in the future.
            </p>
            <p style={{ color: 'var(--color-orange)' }}>
              <i className="icon mbc-icon alert circle"></i>
              Note: Please ensure that all your API&apos;s are prefixed with /api.
            </p>
          </span>
          <div className={classNames(Styles.publishedConfig)}>
            <button
              className={classNames('btn add-dataiku-container btn-primary', Styles.editOrViewMode)}
              type="button"
              onClick={this.navigateEditOrReadOnlyMode}
            >
              {this.state.readOnlyMode ? (
                <>
                  <i className="icon mbc-icon edit" />
                  <span> Edit security config</span>
                </>
              ) : (
                <>
                  <i className="icon mbc-icon visibility-show" />
                  <span> View published security config</span>
                </>
              )}
            </button>
          </div>
          <div id="create-security-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li
                    className={
                      this.state.tabClassNames.has('StagingEntitlement')
                        ? this.state.tabClassNames.get('StagingEntitlement')
                        : 'tab active'
                    }
                  >
                    <a href="#tab-content-1" id="stagingEntitlement" onClick={this.setCurrentTab}>
                      Staging Entitlement
                      <sup>*</sup>
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('ProductionEntitlement')
                        ? this.state.tabClassNames.get('ProductionEntitlement')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-2" id="productionEntitlement" onClick={this.setCurrentTab}>
                      Production Entitlement
                      <sup>*</sup>
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                {currentTab === 'stagingEntitlement' && (
                  <Entitlement
                    onSaveDraft={this.onSaveDraft}
                    onPublish={this.onPublish}
                    env="int"
                    id={this.state.id}
                    config={this.state.config}
                    readOnlyMode={this.state.readOnlyMode}
                    projectName={projectName}
                  />
                )}
              </div>

              <div id="tab-content-2" className="tab-content">
                {currentTab === 'productionEntitlement' && (
                  <Entitlement
                    onSaveDraft={this.onSaveDraft}
                    onPublish={this.onPublish}
                    env="prod"
                    id={this.state.id}
                    config={this.state.config}
                    readOnlyMode={this.state.readOnlyMode}
                    projectName={projectName}
                  />
                )}
              </div>
            </div>
          </div>
          <ConfirmModal
            title=""
            acceptButtonTitle="OK"
            showAcceptButton={true}
            showCancelButton={false}
            show={this.state.showRedeployWarningModal}
            content={<div id="contentparentdiv">Please redeploy by reentering the client id, client secret and the required ignore paths, scopes and redirect uri if any for the authorization changes to be reflected. Note that only authentication will be handled unless you redeploy.</div>}
            onAccept={() => { this.setState({ showRedeployWarningModal: false }) }}
          />
          <ConfirmModal
            title="Are you sure you want to Navigate ?"
            acceptButtonTitle="Navigate"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.showAlertChangesModal}
            content={<div id="contentparentdiv">Unsaved Changes if any will be discared on navigation. Are you sure you want to Navigate ?</div>}
            onCancel={this.onCancellingUpdateChanges}
            onAccept={this.onAcceptUpdateChanges}
          />

          <ConfirmModal
            title="Are you sure you want to Navigate ?"
            acceptButtonTitle="Navigate"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.editModeNavigateModal}
            content={<div id="contentparentdiv">Unsaved Changes if any will be discared on navigation. Are you sure you want to Navigate ?</div>}
            onCancel={() => {
              this.setState({ 
                editModeNavigateModal: !this.state.editModeNavigateModal,
              });
            }}
            onAccept={() => {
              history.push(`/codespace/publishedSecurityconfig/${this.state.id}?name=${this.state.projectName}?intIAM=${this.state.intIAM === 'true' ? 'true' : 'false'}?prodIAM=${this.state.prodIAM === 'true' ? 'true' : 'false'}`);
            }}
          />
        </div>
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </React.Fragment>
    );
  }
}

