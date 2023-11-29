import cn from 'classnames';
import * as React from 'react';
import { getParams } from '../../../../router/RouterUtils';

// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import InputFields from '../../../../assets/modules/uilab/js/src/input-fields';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../../assets/modules/uilab/js/src/tabs';
import { CODE_SPACE_STATUS } from 'globals/constants';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

// @ts-ignore
import * as _ from 'lodash';
import { IUserInfo } from 'globals/types';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import Styles from './SecurityConfig.scss';
import Entitlement from './entitlement/Entitlement';
import Caption from '/Users/ajshiva/Documents/Code/UI/DnA/packages/frontend/src/components/mbc/shared/caption/Caption';
import Roles from './roles/Roles';
// @ts-ignore
import RoleMapping from './roleMapping/RoleMapping';
import { CodeSpaceApiClient } from '../../../../../src/services/CodeSpaceApiClient';

const classNames = cn.bind(Styles);
export interface ICreateNewSecurityConfigState {
    id: string;
    editMode: boolean;
    currentTab: string;
    nextTab: string;
    clickedTab: string;
    saveActionType: string;
    tabClassNames: Map<string, string>;
    currentState: any;
    showAlertChangesModal: boolean;
    config: any;
}

export interface ICreateNewSecurityConfigProps {
    user: IUserInfo;
}

export default class SecurityConfig extends React.Component<ICreateNewSecurityConfigProps, ICreateNewSecurityConfigState> {
    constructor(props: ICreateNewSecurityConfigProps) {
        super(props);
        this.state = {
            id: '',
            editMode: false,
            currentTab: 'entitlement',
            nextTab: 'roles',
            clickedTab: '',
            saveActionType: '',
            tabClassNames: new Map<string, string>(),
            currentState: null,
            showAlertChangesModal: false,
            config: {
                entitlements: [],
                roles: [],
                userRoleMappings: [],
                openSegments: []
            }
        };
    }

    public componentDidMount() {
        const params = getParams();
        this.setState({ id: params?.id });
        SelectBox.defaultSetup();
        Tabs.defaultSetup();
        InputFields.defaultSetup();
        this.getConfig(params?.id);
    }

    public getConfig = (id: string) => {
        ProgressIndicator.show();
        CodeSpaceApiClient.getCodeSpaceConfig(id)
            .then((res: any) => {
                const response = {
                    ...res,
                    entitlements: res.entitlements || [],
                    roles: res.roles || [],
                    userRoleMappings: res.userRoleMappings || [],
                    openSegments: res.openSegments || []
                }
                this.setState({
                    config: response,
                }, () => {
                    this.setOpenTabs(this.state.config.openSegments);
                })
                ProgressIndicator.hide();
            })
            .catch((error) => {
                this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
                ProgressIndicator.hide();
            });
    };

    public setOpenTabs = (openSegments: string[]) => {
        if (openSegments != null && openSegments.length > 0) {
            const tabClasses = new Map<string, string>();
            openSegments.forEach((openSegment) => {
                tabClasses.set(openSegment, 'tab valid');
            });
            this.setState({ tabClassNames: tabClasses });
        } else {
            const tabClasses = new Map<string, string>();
            tabClasses.set('Roles', 'tab disabled');
            tabClasses.set('RoleMappings', 'tab disabled');
            this.setState({ tabClassNames: tabClasses });
        }
    };

    public onCancellingUpdateChanges = () => {
        this.getConfig(this.state.id);
    };

    public onAcceptUpdateChanges = () => {
        document.getElementById(this.state.currentTab).click();
        this.setState({
            showAlertChangesModal: false,
        });
    };

    protected onSaveDraft = (tabToBeSaved: string, config: any) => {
        if (config?.entitlements?.length === 0) {
            config.openSegments = [];
        };
        this.setState({
            config: config,
        }, () => {
            if (this.state.config?.entitlements?.length === 0) {
                this.callApiToSave(this.state.config.status, 'entitlement');
                Notification.show('Please add atleast one entitlement to go to next tab', 'warning');
                return;
            }

            if (tabToBeSaved === 'roles') {
                if (this.state.config?.roles?.length === 0) {
                    this.callApiToSave(this.state.config.status, 'roles');
                    Notification.show('Please add atleast one role to go to next tab', 'warning');
                    return;
                }
            }

            // const currentTab = this.state.currentTab;
            const currentTab = tabToBeSaved;
            this.setState({ saveActionType: 'btn' });
            if (currentTab === 'entitlement') {
                this.saveEntitlement();
            } else if (currentTab === 'roles') {
                this.saveRole();
            } else if (currentTab === 'rolemapping') {
                this.saveRoleMapping();
            } else {
                // If multiple clicks on save happens then the currenttab doesnt get updated in that case
                // just save not moving to another tab.
                this.callApiToSave("DRAFT", "rolemapping");
            }
        });
    };

    protected onPublish = (config: any) => {
        this.setState({
            config: config,
        }, () => {
            if (CODE_SPACE_STATUS.includes(this.state.config?.status)) {
                this.callApiToSave(this.state.config.status, 'rolemapping', true);
            } else {
                ProgressIndicator.show();
                CodeSpaceApiClient.addCodeSpaceRequest(this.state.id)
                    .then((res: any) => {
                        this.getConfig(this.state.id);
                        Notification.show('Published successfully.');
                        ProgressIndicator.hide();
                    })
                    .catch((error: any) => {
                        ProgressIndicator.hide();
                        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
                    });
            }
        });
    };

    protected setTabsAndClick = (nextTab: string) => {
        const nextTabToClick = nextTab;
        this.setState({
            currentTab: nextTab,
            nextTab: nextTabToClick,
        });
        if (nextTabToClick) {
            document.getElementById(nextTabToClick).click();
        }
    };

    protected setCurrentTab = (event: React.MouseEvent) => {
        const target = event.target as HTMLLinkElement;
        const newState = this.state.config;
        const saveActionType = this.state.saveActionType;
        const currentState = this.state.currentState;

        if (!currentState || saveActionType === 'btn' || _.isEqual(newState, currentState)) {
            this.setState({ currentTab: target.id, saveActionType: '' });
        } else {
            if (!this.state.showAlertChangesModal) {
                // this.setState({ showAlertChangesModal: true, clickedTab: target.id });
            }
        }
    };

    protected saveEntitlement = () => {
        this.state.config.openSegments.push('Entitlement');
        if (CODE_SPACE_STATUS.includes(this.state.config?.status)) {
            this.callApiToSave(this.state.config.status, 'roles');
        } else {
            this.goToNextPageWithoutSaving('roles');
        }

    };

    protected saveRole = () => {
        this.state.config.openSegments.push('Roles');
        if (CODE_SPACE_STATUS.includes(this.state.config?.status)) {
            this.callApiToSave(this.state.config.status, 'rolemapping');
        } else {
            this.goToNextPageWithoutSaving('rolemapping');
        }
    };

    protected saveRoleMapping = () => {
        this.state.config.openSegments.push('RoleMappings');
        if (CODE_SPACE_STATUS.includes(this.state.config?.status)) {
            this.callApiToSave(this.state.config.status, 'rolemapping');
        } else {
            this.goToNextPageWithoutSaving('rolemapping');
        }
    };

    protected goToNextPageWithoutSaving = (nextTab: string) => {
        const config = this.state.config;
        const distinct = (value: any, index: any, self: any) => {
            return self.indexOf(value) === index;
        };
        this.state.config.openSegments = config.openSegments?.filter(distinct);

        this.setState({
            config: { ...this.state.config },
        });
        this.setOpenTabs(this.state.config.openSegments);
        this.setTabsAndClick(nextTab);
    };

    protected callApiToSave = (status: string, nextTab: string, callPublishApi?: boolean) => {
        const config = this.state.config;
        const distinct = (value: any, index: any, self: any) => {
            return self.indexOf(value) === index;
        };
        this.state.config.openSegments = config.openSegments?.filter(distinct);
        const data: any = {
            data: {
                status: status || "DRAFT",
                entitlements: config.entitlements,
                roles: config.roles,
                userRoleMappings: config.userRoleMappings,
                openSegments: config.openSegments,
                isProtectedByDna: config.isProtectedByDna,
            }
        };
        ProgressIndicator.show();
        CodeSpaceApiClient.createOrUpdateCodeSpaceConfig(this.state.id, data)
            .then((response: any) => {
                if (response) {
                    if (response?.response?.success === "SUCCESS") {
                        this.setState({
                            config: response?.data,
                        });
                    }
                    Notification.show('Saved successfully.');

                    if (callPublishApi) {
                        ProgressIndicator.show();
                        CodeSpaceApiClient.addCodeSpaceRequest(this.state.id)
                            .then((res: any) => {
                                ProgressIndicator.hide();
                                this.getConfig(this.state.id);
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
        this.setOpenTabs(config?.openSegments);
        this.setTabsAndClick(nextTab);

    };

    protected showErrorNotification(message: string) {
        ProgressIndicator.hide();
        Notification.show(message, 'alert');
    }

    public render() {
        const currentTab = this.state.currentTab;
        return (
            <React.Fragment>
                <div className={classNames(Styles.mainPanel)}>
                    <Caption
                        title={'Codespace access management'}
                    />
                    <div id="create-security-tabs" className="tabs-panel">
                        <div className="tabs-wrapper">
                            <nav>
                                <ul className="tabs">
                                    <li
                                        className={
                                            this.state.tabClassNames.has('Entitlement')
                                                ? this.state.tabClassNames.get('Entitlement')
                                                : 'tab active'
                                        }
                                    >
                                        <a href="#tab-content-1" id="entitlement" onClick={this.setCurrentTab}>
                                            Entitlement<sup>*</sup>
                                        </a>
                                    </li>
                                    <li
                                        className={
                                            this.state.tabClassNames.has('Roles')
                                                ? this.state.tabClassNames.get('Roles')
                                                : 'tab disabled'
                                        }
                                    >
                                        <a href="#tab-content-2" id="roles" onClick={this.setCurrentTab}>
                                            Roles<sup>*</sup>
                                        </a>
                                    </li>
                                    <li
                                        className={
                                            this.state.tabClassNames.has('RoleMappings') ? this.state.tabClassNames.get('RoleMappings') : 'tab disabled'
                                        }
                                    >
                                        <a href="#tab-content-3" id="rolemapping" onClick={this.setCurrentTab}>
                                            User-Role Mappings<sup>*</sup>
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                        <div className="tabs-content-wrapper">
                            <div id="tab-content-1" className="tab-content">
                                <Entitlement
                                    onSaveDraft={this.onSaveDraft}
                                    id={this.state.id}
                                    config={this.state.config}
                                />
                            </div>
                            <div id="tab-content-2" className="tab-content">
                                {currentTab === 'roles' && (
                                    <Roles
                                        config={this.state.config}
                                        user={this.props.user}
                                        onSaveDraft={this.onSaveDraft}
                                        id={this.state.id}
                                    />
                                )}
                            </div>
                            <div id="tab-content-3" className="tab-content">
                                {currentTab === 'rolemapping' && (
                                    <RoleMapping
                                        onSaveDraft={this.onSaveDraft}
                                        id={this.state.id}
                                        config={this.state.config}
                                        onPublish={this.onPublish}
                                    />
                                )}
                            </div>
                        </div>
                    </div>
                    <ConfirmModal
                        title="Save Changes?"
                        acceptButtonTitle="Close"
                        cancelButtonTitle="Cancel"
                        showAcceptButton={true}
                        showCancelButton={true}
                        show={this.state.showAlertChangesModal}
                        content={
                            <div id="contentparentdiv">
                                Press &#187;Close&#171; to save your changes or press
                                <br />
                                &#187;Cancel&#171; to discard changes.
                            </div>
                        }
                        onCancel={this.onCancellingUpdateChanges}
                        onAccept={this.onAcceptUpdateChanges}
                    />
                </div>
                <div className={Styles.mandatoryInfo}>* mandatory fields</div>
            </React.Fragment>
        );
    }

}
