import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';

import { IUserInfo } from '../../../globals/types';
// import { history } from '../../../router/History';
import Styles from './Administration.scss';

// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { TagHandling } from './taghandling/TagHandling';
import { UserRoleManagement } from './userrole/UserRoleManagement';
import { MalwareScanapikeys } from './malwarescanapikeys/MalwareScanapikeys';
import { Envs } from '../../../globals/Envs';

const classNames = cn.bind(Styles);

export default class Administration extends React.Component<{ user: IUserInfo }, any> {
  constructor(props: any) {
    super(props);
  }
  public componentDidMount() {
    Tabs.defaultSetup();
  }

  public render() {
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
                  <li className={Styles.tab + ' tab active'}>
                    <a href="#tab-content-1" id="userRoles">
                      User Roles
                    </a>
                  </li>
                  <li className={Styles.tab + ' tab'}>
                    <a href="#tab-content-2" id="tagHandling">
                      Tag Handling
                    </a>
                  </li>
                  {Envs.ENABLEMALWARESERVICE ? (
                    <li className={Styles.tab + ' tab'}>
                      <a href="#tab-content-6" id="malwarescanapikeys">
                        Malware Scan API Keys
                      </a>
                    </li>
                  ) : (
                    ''
                  )}
                  {/* <li className="tab">
                    <a href="#tab-content-3" id="settings">
                      Settings
                    </a>
                  </li> */}
                  <li className="tab disabled">
                    <a href="#tab-content-4" id="dummy2">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-5" id="dummy3">
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
                <UserRoleManagement />
              </div>
              <div id="tab-content-2" className={'tab-content ' + Styles.contentTab}>
                <TagHandling />
              </div>
              {Envs.ENABLEMALWARESERVICE ? (
                <div id="tab-content-6" className={'tab-content ' + Styles.contentTab}>
                  <MalwareScanapikeys />
                </div>
              ) : (
                ''
              )}
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
