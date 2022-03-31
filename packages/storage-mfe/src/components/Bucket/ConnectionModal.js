import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useLocation } from 'react-router-dom';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import Styles from './ConnectionModal.scss';
import { omit } from 'lodash';

const copyToClipboard = (id) => {
  const content = document.getElementById(id)?.innerText;
  navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
};

export const ConnectionModal = () => {
  const { connect } = useSelector((state) => state.connectionInfo);

  const [bucketInfo, setBucketInfo] = useState({
    bucketName: '',
    accessInfo: [],
  });

  const { pathname } = useLocation();
  const isCreatePage = pathname === '/createBucket';

  useEffect(() => {
    Tabs.defaultSetup();
    // reset
    // ensure only single tab indicator is shown
    // on close , reset active tab to tab-1
    const activeTabIndicator = document.querySelectorAll('.active-indicator');

    if (activeTabIndicator?.length > 1) {
      activeTabIndicator?.[0]?.remove();
    }
    const tabs = document.querySelectorAll('.tabs')?.[0]?.childNodes;
    if (!connect.modal) {
      tabs[0].classList?.add('active');
      tabs[1].classList?.remove('active');
    }
  }, [connect.modal]);

  useEffect(() => {
    setBucketInfo({
      bucketName: connect?.bucketName,
      accessInfo: omit(connect?.accessInfo, ['permission']),
    });
  }, [connect?.bucketName, connect?.accessInfo]);
  return (
    <div>
      <div className={Styles.accessDetails}>
        {isCreatePage ? (
          <>
            <h5>Your Bucket {bucketInfo?.bucketName} is provisioned successfully.</h5>
            <h6>Below are the access details</h6>
          </>
        ) : (
          <>
            <h5>Access Details for Bucket - {bucketInfo?.bucketName}</h5>
          </>
        )}
        <table>
          <tbody>
            <tr>
              <td>
                <strong>Access Key</strong>
              </td>
              <td>:</td>
              <td id="accessKey" className={Styles.keys}>
                {bucketInfo?.accessInfo?.accesskey}
              </td>
              <td>
                <span
                  className="copy-icon"
                  style={{
                    cursor: 'pointer',
                  }}
                  onClick={() => copyToClipboard('accessKey')}
                >
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
            <tr>
              <td>
                <strong>Secret Key</strong>
              </td>
              <td>:</td>
              <td id="secretKey" className={Styles.keys}>
                {bucketInfo?.accessInfo?.secretKey}
              </td>
              <td>
                <span
                  className="copy-icon"
                  style={{
                    cursor: 'pointer',
                  }}
                  onClick={() => copyToClipboard('secretKey')}
                >
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div className={'tabs-panel'}>
        <div className="tabs-wrapper">
          <nav>
            <ul className="tabs">
              <li className={'tab active'}>
                <a href="#tab-content-1" id="userRoles">
                  <strong>How to Connect Jupiter NoteBook</strong>
                </a>
              </li>
              <li className={'tab'}>
                <a href="#tab-content-2" id="tagHandling">
                  <strong>How to Connect Dataiku</strong>
                </a>
              </li>
            </ul>
          </nav>
        </div>
        <div className={classNames('tabs-content-wrapper', Styles.tabsContentWrapper)}>
          <div id="tab-content-1" className={classNames('tab-content', Styles.tabContentContainer)}>
            <span
              className="copy-icon"
              style={{
                float: 'right',
                cursor: 'pointer',
              }}
              onClick={() => copyToClipboard('tab-content-1')}
            >
              <i className="icon mbc-icon copy" />
            </span>
            <pre>{JSON.stringify(bucketInfo?.accessInfo, undefined, 2)}</pre>
          </div>
          <div id="tab-content-2" className={classNames('tab-content', Styles.tabContentContainer)}>
            <span
              className="copy-icon"
              style={{
                float: 'right',
                cursor: 'pointer',
              }}
              onClick={() => copyToClipboard('tab-content-2')}
            >
              <i className="icon mbc-icon copy" />
            </span>
            <pre>{JSON.stringify(bucketInfo?.accessInfo, undefined, 2)}</pre>
          </div>
        </div>
      </div>
    </div>
  );
};
