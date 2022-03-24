import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';

// import { history } from '../../../router/History';
import Styles from './Administration.scss';
import { IUserInfo , IRole,  }  from '../../../globals/types';
import { USER_ROLE } from '../../../globals/constants';

// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { TagHandling } from './taghandling/TagHandling';
import { UserRoleManagement } from './userrole/UserRoleManagement';
import { MalwareScanapikeys } from './malwarescanapikeys/MalwareScanapikeys';
import { ReportTagHandling } from '../reportAdmin/taghandling/ReportTagHandling';

import { Envs } from '../../../globals/Envs';

export interface IAdministrationProps {
  user: IUserInfo;
}
export interface IAdministrationState {
  currentUserRole: string;
}
const classNames = cn.bind(Styles);

export default class Administration extends React.Component<IAdministrationProps, IAdministrationState> {
  constructor(props: any) {
    super(props);
    this.state = {
      currentUserRole:''
    }
  }
  public componentDidMount() {
    Tabs.defaultSetup();
  }

  public render() {
    const isAdmin = this.props.user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN) !== undefined;
    const isReportAdmin = this.props.user.roles.find((role: IRole) => role.id === USER_ROLE.REPORTADMIN) !== undefined;
    return (
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>Administration</h3>
          </div>
        </div>
        <div className={Styles.content}>
          <div className="tabs-panel admin">
            <div className="tabs-wrapper admin">
              <nav id="admin-tabs">
                <ul className="tabs">
                  { isAdmin  && 
                  <React.Fragment>
                    <li className={Styles.tab + ' tab active'}>
                      <a href="#tab-content-1" id="userRoles">
                        User Roles
                      </a>
                    </li>
                    <li className={Styles.tab + ' tab'}>
                      <a href="#tab-content-2" id="tagHandling">
                        Solution Tag Handling
                      </a>
                    </li>
                    {Envs.ENABLE_MALWARE_SCAN_SERVICE ? (
                    <li className={Styles.tab + ' tab'}>
                      <a href="#tab-content-3" id="malwarescanapikeys">
                        Malware Scan API Keys
                      </a>
                    </li>
                  ) : (
                    ''
                  )}
                  </React.Fragment>
                  }
                  { (isAdmin || isReportAdmin) &&
                   <li className={Styles.tab + ' tab ' + (isReportAdmin && 'tab active') }>
                    <a href="#tab-content-4" id="reporttagHandling">
                      Report Tag Handling    
                    </a>
                  </li>}
                  <li className="tab disabled">
                    <a href="#tab-content-5" id="dummy2">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-6" id="dummy3">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-7" id="dummy4">
                      &nbsp;
                    </a>
                  </li>
                  
                </ul>
              </nav>
            </div>
            <div className={Styles.tabcontentWrrapper + ' tabs-content-wrapper'}>
              <div id="tab-content-1" className={'tab-content ' + Styles.contentTab}>
              {this.state.currentUserRole}
                <UserRoleManagement />
              </div>
              <div id="tab-content-2" className={'tab-content ' + Styles.contentTab}>
                <TagHandling />
              </div>
              {Envs.ENABLE_MALWARE_SCAN_SERVICE ? (
                <div id="tab-content-3" className={'tab-content ' + Styles.contentTab}>
                  <MalwareScanapikeys />
                </div>
              ) : (
                ''
              )}
              <div id="tab-content-4" className={'tab-content ' + Styles.contentTab}>
                <ReportTagHandling />
              </div>
              {/* <div id="tab-content-3" className="tab-content">
                <h6>Application Settings</h6>
              </div> */}
            </div>
          </div>
        </div>
      </div>
    );
  }

  protected showErrorNotification(message: string) {
    Notification.show(message, 'alert');
  }
}
