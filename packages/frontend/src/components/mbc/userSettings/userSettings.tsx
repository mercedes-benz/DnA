import React from 'react';
import cn from 'classnames';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { IUserInfo } from 'globals/types';
import Styles from './userSettings.scss';
import NotificationsSettings from './notificationsSettings/notificationsSettings';
import SubscriptionsSettings from './subscriptionsSettings/subscriptionsSettings';

const classNames = cn.bind(Styles);

class UserSettings extends React.Component<{ user: IUserInfo }, any> {
  constructor(props: any) {
    super(props);
    this.state = {
      currentTab: 'notications',
    };
  }

  componentDidMount() {
    Tabs.defaultSetup();
  }

  protected setCurrentTabFun = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    this.setState({ currentTab: target.id });
  };

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <div className={Styles.wrapper}>
            <div className={Styles.caption}>
              <h3>User Settings</h3>
            </div>
            <div id="user-settings-tabs" className="tabs-panel">
              <div className="tabs-wrapper">
                <nav>
                  <ul className="tabs">
                    <li className={this.state.currentTab === 'notications' ? 'tab active' : 'tab'}>
                      <a href="#tab-content-1" id="notications" onClick={this.setCurrentTabFun}>
                        Notifications
                      </a>
                    </li>
                    <li
                      className={classNames(
                        'disabled',
                        this.state.currentTab === 'subscriptions' ? 'tab active ' : 'tab ',
                      )}
                    >
                      <a href="#tab-content-2" id="subscriptions" onClick={this.setCurrentTabFun} className={'hidden'}>
                        Subscriptions
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="digitalValue2" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="digitalValue3" className={'hidden'}>
                        `
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
              <div className={classNames('tabs-content-wrapper', Styles.tabContent)}>
                <div id="tab-content-1" className="tab-content">
                  <NotificationsSettings user={this.props.user}></NotificationsSettings>
                </div>
                <div id="tab-content-2" className="tab-content">
                  <SubscriptionsSettings></SubscriptionsSettings>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
// const UserSettings = ({user: IUserInfo}) => {
//     // const [tabClassNames, setTabClassNames] = useState(new Map<string, string>());
//     const [currentTab, setCurrentTab] = useState('notications');

//     useEffect(() => {
//         Tabs.defaultSetup();
//     }, []);

//     const setCurrentTabFun = (event: React.MouseEvent) => {
//         const target = event.target as HTMLLinkElement;
//         setCurrentTab(target.id);
//     };

//     return (
//         <React.Fragment>

//         <div
//           className={classNames(
//             Styles.mainPanel,
//           )}
//         >
//             <div className={Styles.wrapper}>
//                 <div className={Styles.caption}>
//                     <h3>User Settings</h3>
//                 </div>
//                 <div id="user-settings-tabs" className="tabs-panel">
//                     <div className="tabs-wrapper">
//                         <nav>
//                             <ul className="tabs">
//                                 <li className={currentTab === 'notications' ? 'tab active' : 'tab'}>
//                                     <a href="#tab-content-1" id="notications" onClick={setCurrentTabFun}>
//                                     Notifications
//                                     </a>
//                                 </li>
//                                 <li
//                                     className={
//                                     (currentTab === 'subscriptions' ? 'tab active ' : 'tab ')
//                                     }
//                                 >
//                                     <a
//                                     href="#tab-content-2"
//                                     id="subscriptions"
//                                     onClick={setCurrentTabFun}
//                                     >
//                                     Subscriptions
//                                     </a>
//                                 </li>
//                                 <li className={'tab disabled'}>
//                                     <a id="digitalValue2" className={'hidden'}>
//                                     `
//                                     </a>
//                                 </li>
//                                 <li className={'tab disabled'}>
//                                     <a id="digitalValue3" className={'hidden'}>
//                                     `
//                                     </a>
//                                 </li>
//                             </ul>
//                         </nav>
//                     </div>
//                     <div className={classNames("tabs-content-wrapper", Styles.tabContent)}>
//                         <div id="tab-content-1" className="tab-content">
//                             <NotificationsSettings user={props.user}></NotificationsSettings>
//                         </div>
//                         <div id="tab-content-2" className="tab-content">
//                             <SubscriptionsSettings></SubscriptionsSettings>
//                         </div>
//                     </div>
//                 </div>

//             </div>
//         </div>

//         </React.Fragment>
//     )

// }

export default UserSettings;
