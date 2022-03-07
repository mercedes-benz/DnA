import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { IUserInfo } from '../../../globals/types';
// import { history } from '../../../router/History';
import Styles from './ReportAdministration.scss';

// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { TagHandling } from './taghandling/TagHandling';
import { ReportsApiClient } from '../../../services/ReportsApiClient';

const classNames = cn.bind(Styles);
export default class ReportAdministration extends React.Component<{ user: IUserInfo }, any> {
  constructor(props: any) {
    super(props);
    this.state = {
      addNewItem: false,
    };
  }
  public componentDidMount() {
    Tabs.defaultSetup();
  }

  public render() {
    return (
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>Report Administration</h3>
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                ProgressIndicator.show();
                ReportsApiClient.exportJSON()
                  .then((res) => {
                    if (res.data?.reports?.records?.length) {
                      const url = window.URL.createObjectURL(new Blob([JSON.stringify(res.data?.reports.records)]));
                      const d = new Date();
                      const date = `${d.getMonth() + 1}_${d.getDate()}_${d.getFullYear()}`;
                      const time = `${d.getHours()}_${d.getMinutes()}_${d.getSeconds()}`;
                      const link = document.createElement('a');
                      link.download = `Report-Data-Dump-[${date}-${time}].json`;
                      link.href = url;
                      link.click();
                    } else {
                      Notification.show('No records to export.', 'alert');
                    }
                    ProgressIndicator.hide();
                  })
                  .catch((e) => {
                    ProgressIndicator.hide();
                    Notification.show('Error downloading attachment. Please try again later.', 'alert');
                  });
              }}
            >
              <i className="icon download" />
              Export Report Data as JSON
            </a>
          </div>
        </div>
        <div className={Styles.content}>
          <div className="tabs-panel admin">
            <div className="tabs-wrapper admin">
              <nav id="admin-tabs">
                <ul className="tabs disabled ">
                  <li className="tab active">
                    <a href="#tab-content-2" id="dummy2">
                      Tag Handling
                    </a>
                  </li>
                  <li className={Styles.tab + ' tab disabled'}>
                    <a href="#tab-content-1" id="taghandling">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-3" id="dummy3">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-4" id="dummy4">
                      &nbsp;
                    </a>
                  </li>
                  <li className="tab disabled">
                    <a href="#tab-content-5" id="dummy5">
                      &nbsp;
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className={Styles.tabcontentWrrapper + ' tabs-content-wrapper'}>
              <div id="tab-content-1" className={'tab-content ' + Styles.contentTab}>
                &nbsp;
              </div>
              <div id="tab-content-2" className={'tab-content ' + Styles.contentTab}>
                <TagHandling />
              </div>
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
