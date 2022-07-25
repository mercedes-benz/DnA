import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';

// import { history } from '../../../router/History';
import Styles from './Administration.scss';
import { IUserInfo, IRole } from '../../../globals/types';
import { USER_ROLE } from '../../../globals/constants';

// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { TagHandling } from './taghandling/TagHandling';
import { UserRoleManagement } from './userrole/UserRoleManagement';
import { MalwareScanapikeys } from './malwarescanapikeys/MalwareScanapikeys';
import { ReportTagHandling } from '../reportAdmin/taghandling/ReportTagHandling';
import { AdminNotifications } from './notifications/AdminNotifications';

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
      currentUserRole: '',
    };
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
                  {isAdmin && (
                    <React.Fragment>
                      <li className={Styles.tab + ' tab active'}>
                        <a href="#tab-content-1" id="userRoles">
                          User Roles
                        </a>
                      </li>
                      <li className={Styles.tab + ' tab'}>
                        <a href="#tab-content-2" id="tagHandling">
                          Solution MDM
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
                  )}
                  {(isAdmin || isReportAdmin) && (
                    <li className={Styles.tab + ' tab ' + (isReportAdmin && 'tab active')}>
                      <a href="#tab-content-4" id="reporttagHandling">
                        Report MDM
                      </a>
                    </li>
                  )}
                  {isAdmin && (
                    <li className={Styles.tab + ' tab ' + (isReportAdmin && 'tab active')}>
                      <a href="#tab-content-5" id="notificationTagHandling">
                        Notification
                      </a>
                    </li>
                  )}
                </ul>
              </nav>
            </div>
            <div className={Styles.tabcontentWrrapper + ' tabs-content-wrapper'}>
              {isAdmin && (
                <>
                  <div id="tab-content-1" className={'tab-content ' + Styles.contentTab}>
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
                </>
              )}
              {(isAdmin || isReportAdmin) && (
                <div id="tab-content-4" className={'tab-content ' + Styles.contentTab}>
                  <ReportTagHandling />
                </div>
              )}
              {isAdmin && (
                <div id="tab-content-5" className={'tab-content ' + Styles.contentTab}>
                  <AdminNotifications userId={this.props.user.id} />
                </div>
              )}
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
