import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useLocation } from 'react-router-dom';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import Styles from './ConnectionModal.scss';

const copyToClipboard = (id) => {
  const content = document.getElementById(id)?.innerText;
  navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
};

export const ConnectionModal = () => {
  const { bucketList, submission } = useSelector((state) => state.bucket);
  const bucketInfo = bucketList.find((bucket) => bucket.id === submission?.bucketId);

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
    if (!submission.modal) {
      tabs[0].classList?.add('active');
      tabs[1].classList?.remove('active');
    }
  }, [submission.modal]);

  return (
    <div>
      <div className={Styles.accessDetails}>
        {isCreatePage ? (
          <>
            <h5>Your Bucket {bucketInfo?.name} is provisioned successfully.</h5>
            <h6>Below are the access details</h6>
          </>
        ) : (
          <>
            <h5>Access Details for Bucket - {bucketInfo?.name}</h5>
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
                kjaskaisajkkakak132
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
                aasjhfafkasjf
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
      <div className="tabs-panel">
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
        <div className={'tabs-content-wrapper'}>
          <div id="tab-content-1" className={'tab-content'}>
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
            {' { '}
            {Object.keys(bucketInfo?.tab || [])?.map((item, index) => {
              return (
                <pre key={index}>
                  <span>{`${item}: ${bucketInfo.tab[item]}`}</span>
                </pre>
              );
            })}
            {' } '}
          </div>
          <div id="tab-content-2" className={'tab-content'}>
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
            {' { '}
            {Object.keys(bucketInfo?.tab || [])?.map((item, index) => {
              return (
                <pre key={index}>
                  <span>{`${item}: ${bucketInfo.tab[item]}`}</span>
                </pre>
              );
            })}
            {' } '}
          </div>
        </div>
      </div>
    </div>
  );
};
