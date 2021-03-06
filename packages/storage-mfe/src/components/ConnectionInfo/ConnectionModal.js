import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation } from 'react-router-dom';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import Styles from './ConnectionModal.scss';
import { omit } from 'lodash';
import { hideConnectionInfo } from './redux/connection.actions';
import { history } from '../../store/storeRoot';

const copyToClipboard = (content) => {
  navigator.clipboard.writeText('');
  navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
};

export const ConnectionModal = () => {
  const dispatch = useDispatch();
  const { connect } = useSelector((state) => state.connectionInfo);

  const [bucketInfo, setBucketInfo] = useState({
    bucketName: '',
    accessInfo: [],
  });
  const [showSecretKey, setShowSecretKey] = useState(false);

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

  const handleOK = () => {
    if (isCreatePage) {
      history.replace('/');
    }
    dispatch(hideConnectionInfo());
  };

  const connectToJupyter = (
    <code>
      {`from minio import Minio
MINIO_BUCKET = "${bucketInfo.bucketName}"
minio_client = Minio('${bucketInfo.accessInfo.hostName}', access_key='${bucketInfo.accessInfo.accesskey}', secret_key='YOUR_BUCKET_SECRET_KEY', secure=False)
y_file_obj = minio_client.get_object(MINIO_BUCKET, <<filepath>>)
y = pd.read_csv(y_file_obj)`}
    </code>
  );

  const connectToDataiku = (
    <code>
      {`1. Go to Administration.
2. Select 'Connection' tab and click on '+ NEW CONNECTION'.
3. Select 'Amazon S3' connection.
4. Provide the below-required information:
 - New connection name: <<Name of the connection>>
 - Access Key: ${bucketInfo.accessInfo.accesskey}
 - Secret Key: YOUR_BUCKET_SECRET_KEY
 - Region / Endpoint: ${bucketInfo.accessInfo.uri}
5. Click on 'Create' and use the connection in project.
        `}
    </code>
  );

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
                <strong>Access Key :</strong>
              </td>
              <td id="accessKey" className={Styles.keys}>
                {bucketInfo?.accessInfo?.accesskey}
              </td>
              <td>
                <span className={Styles.copyIcon} onClick={() => copyToClipboard(bucketInfo?.accessInfo?.accesskey)}>
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
            <tr>
              <td>
                <strong>Secret Key :</strong>
              </td>
              <td id="secretKey" className={Styles.keys}>
                {showSecretKey
                  ? bucketInfo?.accessInfo?.secretKey
                  : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}
              </td>
              <td>
                {showSecretKey ? (
                  <React.Fragment>
                    <i
                      className={classNames('icon mbc-icon visibility-hide ', Styles.visibilityIcon)}
                      onClick={() => setShowSecretKey(false)}
                      tooltip-data="Hide"
                    />
                  </React.Fragment>
                ) : (
                  <React.Fragment>
                    <i
                      className={classNames('icon mbc-icon visibility-show ', Styles.visibilityIcon)}
                      onClick={() => setShowSecretKey(true)}
                      tooltip-data="Show"
                    />
                  </React.Fragment>
                )}
                <span className={Styles.copyIcon} onClick={() => copyToClipboard(bucketInfo?.accessInfo?.secretKey)}>
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div className={'tabs-panel'}>
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={'tab active'}>
                  <a href="#tab-content-1" id="jupyterNotebook">
                    <strong>How to Connect from DNA Jupyter NoteBook</strong>
                  </a>
                </li>
                <li className={'tab disable'}>
                  <a href="#tab-content-2" id="dataiku">
                    <strong style={{ color: '#99a5b3' }}>How to Connect from Dataiku (Coming soon)</strong>
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className={classNames('tabs-content-wrapper', Styles.tabsContentWrapper)}>
            <div id="tab-content-1" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <span
                className={Styles.copyIcon}
                onClick={() => {
                  const content = document.getElementById('tab-content-1')?.innerText;
                  copyToClipboard(content);
                }}
              >
                <i className="icon mbc-icon copy" />
              </span>
              <div className={Styles.connectionCode}>{connectToJupyter}</div>
            </div>
            <div id="tab-content-2" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
              <div className={classNames(Styles.connectionCode)}>{connectToDataiku}</div>
            </div>
          </div>
        </div>
        <button className={classNames('btn btn-primary', Styles.OkBtn)} onClick={handleOK}>
          OK
        </button>
      </div>
    </div>
  );
};
