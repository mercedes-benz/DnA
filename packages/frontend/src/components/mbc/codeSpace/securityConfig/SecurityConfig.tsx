import cn from 'classnames';
import * as React from 'react';
import { getParams, getPath, getQueryParam } from '../../../../router/RouterUtils';

// @ts-ignore
import InputFields from '../../../../assets/modules/uilab/js/src/input-fields';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../../assets/modules/uilab/js/src/tabs';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

// @ts-ignore
import * as _ from 'lodash';
import { IUserInfo } from 'globals/types';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import Styles from './SecurityConfig.scss';
import Entitlement from './entitlement/Entitlement';
import Caption from '../../shared/caption/Caption';
// @ts-ignore
import { CodeSpaceApiClient } from '../../../../../src/services/CodeSpaceApiClient';
import { history } from '../../../../router/History';

const classNames = cn.bind(Styles);
export interface ICreateNewSecurityConfigState {
  id: string;
  projectName: string;
  editMode: boolean;
  currentTab: string;
  nextTab: string;
  clickedTab: string;
  // isSaved: boolean;
  saveActionType: string;
  tabClassNames: Map<string, string>;
  currentState: any;
  showAlertChangesModal: boolean;
  config: any;
  readOnlyMode: boolean;
  editModeNavigateModal: boolean;
  showStagingModal: boolean;
}

export interface ICreateNewSecurityConfigProps {
  user: IUserInfo;
}

export default class SecurityConfig extends React.Component<
  ICreateNewSecurityConfigProps,
  ICreateNewSecurityConfigState
> {
  constructor(props: ICreateNewSecurityConfigProps) {
    super(props);
    this.state = {
      id: '',
      projectName: '',
      editMode: false,
      currentTab: 'stagingEntitlement',
      nextTab: 'productionEntitlement',
      clickedTab: '',
      saveActionType: '',
      tabClassNames: new Map<string, string>(),
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

  public componentDidMount() {
    const params = getParams();
    let id = params?.id;
    if (id.includes('?name=')) {
      id = params?.id.split('?name=')[0];
    }
    const name = getQueryParam('name');
    this.setState({ projectName: name });
    const path = getPath();
    SelectBox.defaultSetup();
    InputFields.defaultSetup();
    this.setState({ id: id });
    if (path.includes('publishedSecurityconfig')) {
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

  public componentDidUpdate(prevProps: ICreateNewSecurityConfigProps, prevState: ICreateNewSecurityConfigState) {
    if (this.state.currentTab !== prevState.currentTab) {
      // this.setState({ isSaved: false });
      const params = getParams();
      let id = params?.id;
      if (id.includes('?name=')) {
        id = params?.id.split('?name=')[0];
      }
      const name = getQueryParam('name');
      this.setState({ projectName: name });
      const path = getPath();
      SelectBox.defaultSetup();
      InputFields.defaultSetup();
      this.setState({ id: id });
      if (path.includes('publishedSecurityconfig')) {
        this.setState({
          readOnlyMode: true,
        });
        !this.state.showStagingModal ? this.getPublishedConfig(id, 'prod') : this.getPublishedConfig(id, 'int');
      } else {
        !this.state.showStagingModal ? this.getConfig(id, 'prod') : this.getConfig(id, 'int');
      }
    }
  }

  public getPublishedConfig = (id: string, env: string) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getPublishedConfig(id, env)
      .then((res: any) => {
        const response = {
          ...res,
          entitlements: res.entitlements || [],
          appId: res.appID || '',
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

  public getConfig = (id: string, env: string) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpaceConfig(id, env)
      .then((res: any) => {
        const response = {
          ...res,
          entitlements: res.entitlements || [],
          appId: res.appID || '',
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

  public setOpenTabs = () => {
    const tabClasses = new Map<string, string>();
    tabClasses.set('ProductionEntitlement', 'tab valid');
    tabClasses.set('StagingEntitlement', 'tab valid');
    this.setState({ tabClassNames: tabClasses });
  };

  public onCancellingUpdateChanges = () => {
    document.getElementById(this.state.currentTab).click();
    const clickedTab = this.state.clickedTab;
    this.setState({
      showAlertChangesModal: false,
      clickedTab: clickedTab === 'stagingEntitlement' ? 'productionEntitlement' : 'stagingEntitlement',
      showStagingModal: clickedTab === 'stagingEntitlement' ? false : true,
    });
  };

  public onAcceptUpdateChanges = () => {
    const clickedTab = this.state.clickedTab;
    this.setState({
      currentTab: clickedTab,
      saveActionType: '',
      nextTab: clickedTab === 'stagingEntitlement' ? 'productionEntitlement' : 'stagingEntitlement',
      showStagingModal: clickedTab === 'stagingEntitlement' ? true : false,
      showAlertChangesModal: false,
    });
  };

  protected onSaveDraft = (tabToBeSaved: string, config: any, previousTab?: string) => {
    this.setState(
      {
        config: config,
        // isSaved: true,
      },
      () => {
        if (tabToBeSaved === 'stagingEntitlement') {
          this.callApiToSave('int');
        } else if (tabToBeSaved === 'productionEntitlement') {
          this.callApiToSave('prod');
        }
      },
    );
  };

  protected onPublish = (config: any, env: string) => {
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

  protected setCurrentTab = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    const newState = this.state.config;
    const saveActionType = this.state.saveActionType;
    const currentState = this.state.currentState;
    // const showAlertChangesModal = !this.state.isSaved && !this.state.readOnlyMode;
    const showAlertChangesModal = !this.state.readOnlyMode;

    if (!currentState || saveActionType === 'btn' || _.isEqual(newState, currentState)) {
      if (target.id !== this.state.currentTab) {
        !this.state.readOnlyMode
          ? this.setState({
              clickedTab: target.id,
              showAlertChangesModal: showAlertChangesModal,
            })
          : this.setState({
            currentTab: target.id,
            saveActionType: '',
            nextTab: target.id === 'stagingEntitlement' ? 'productionEntitlement' : 'stagingEntitlement',
            showStagingModal: target.id === 'stagingEntitlement' ? true : false,
            showAlertChangesModal: false,
          });
      }
    }
  };

  protected callApiToSave = (env: string, callPublishApi?: boolean) => {
    const config = this.state.config;
    const data: any = {
      data: {
        entitlements: config.entitlements,
        appID: config.appId,
      },
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.createOrUpdateCodeSpaceConfig(this.state.id, data, env)
      .then((response: any) => {
        if (response) {
          // if (response?.response?.success === 'SUCCESS') {
          //   this.setState({
          //     config: response?.data,
          //   });
            // this.getConfig(this.state.id, env);
          // }
          Notification.show('Saved successfully.');

          if (callPublishApi) {
            ProgressIndicator.show();
            CodeSpaceApiClient.addCodeSpaceRequest(this.state.id, env)
              .then((res: any) => {
                ProgressIndicator.hide();
                // this.getConfig(this.state.id, env);
                Notification.show('Published successfully.');
              })
              .catch((error: any) => {
                ProgressIndicator.hide();
                this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
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

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected navigateEditOrReadOnlyMode = () => {
    if (this.state.readOnlyMode) {
      history.push(`/codespace/securityconfig/${this.state.id}?name=${this.state.projectName}`);
    } else {
      this.setState({
        editModeNavigateModal: true,
      });
    }
  };

  public render() {
    const currentTab = this.state.currentTab;
    const { readOnlyMode, projectName } = this.state;
    const publishedSuffix = readOnlyMode ? ' (Published)' : '';
    const title = `${projectName} - Security config${publishedSuffix}`;
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <Caption title={title} />
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
            // content={<div id="contentparentdiv">Please save your changes before Navigating.</div>}
            content={<div id="contentparentdiv">Unsaved Changes if any will be discared on navigation. Are you sure you want to Navigate ?</div>}
            onCancel={() => {
              this.setState({
                editModeNavigateModal: !this.state.editModeNavigateModal,
              });
            }}
            onAccept={() => {
              history.push(`/codespace/publishedSecurityconfig/${this.state.id}?name=${this.state.projectName}`);
            }}
          />
        </div>
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </React.Fragment>
    );
  }
}
