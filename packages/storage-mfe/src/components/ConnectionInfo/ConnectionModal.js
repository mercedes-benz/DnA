import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation } from 'react-router-dom';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import Styles from './ConnectionModal.scss';
import { hideConnectionInfo } from './redux/connection.actions';
import { history } from '../../store/storeRoot';

import Tags from 'dna-container/Tags';

import { bucketsApi } from '../../apis/buckets.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../Utility/envs';

const copyToClipboard = (content) => {
  navigator.clipboard.writeText('');
  navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
};

export const ConnectionModal = (props) => {
  const dispatch = useDispatch();
  const { connect } = useSelector((state) => state.connectionInfo);

  const [bucketInfo, setBucketInfo] = useState({
    bucketName: '',
    accessInfo: [],
    creator: {},
  });
  const [showSecretKey, setShowSecretKey] = useState(false);
  const [dataikuProjectList, setDataikuProjectList] = useState([]);
  const [dataikuNotificationPortal, setDataikuNotificationPortal] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedDataikuProjects, setSelectedDataikuProjects] = useState([]);

  const isDataikuEnabled = Envs.ENABLE_DATAIKU;

  const disableMakeConnectionBtn =
    !bucketInfo.accessInfo.permission?.write || dataikuProjectList?.length > 0
      ? connect?.dataikuProjects?.length === selectedDataikuProjects?.length &&
        (selectedDataikuProjects?.length
          ? dataikuProjectList
              ?.filter((item) => selectedDataikuProjects.indexOf(item.projectKey) > -1)
              ?.map((item) => item.name)
              ?.every((i) => connect?.dataikuProjects?.indexOf(i) > -1)
          : true)
      : true;

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
      accessInfo: connect?.accessInfo,
      creator: connect?.creator,
    });
  }, [connect?.bucketName, connect?.accessInfo, connect?.creator]);

  useEffect(() => {
    if (connect.modal && isDataikuEnabled) {
      setIsLoading(true);
      bucketsApi
        .getDataikuProjects(true)
        .then((res) => {
          res.data?.data?.map((item) => {
            item['id'] = item.projectKey; // add 'id' for each of the object item
            item['name'] = `${item.name} (${item.projectKey})`;
            return item;
          });
          setDataikuProjectList(res.data?.data);
          setIsLoading(false);
        })
        .catch(() => {
          Notification.show('Error while fetching dataiku projects.', 'alert');
          setIsLoading(false);
        });
    }
  }, [connect.modal, isDataikuEnabled]);

  useEffect(() => {
    connect?.modal && setSelectedDataikuProjects(connect?.dataikuProjects);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [connect.modal]);

  useEffect(() => {
    // deserialize the response to show value in dropdown
    const data = dataikuProjectList?.length
      ? dataikuProjectList
          ?.filter((item) => connect?.dataikuProjects.includes(item.projectKey))
          ?.map((item) => item.name)
      : connect?.dataikuProjects;
    dispatch({
      type: 'CONNECTION_INFO',
      payload: {
        dataikuProjects: data,
      },
    });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, dataikuProjectList]);

  // dataiku tag container
  const dataikuTagContainer = document?.querySelectorAll(
    '#tab-content-2[class*="ConnectionModal"] .input-field-group',
  )[0];

  // Dataiku project connection notification msg
  const NotificationMsg = (success = true) => {
    let portal;
    const successMsg = `Selected project(s) ${
      connect.dataikuProjects?.length ? 'connected to' : 'connection removed from'
    } the bucket successfully!`;
    const errorMsg = 'Error while connecting to dataiku project(s)';
    if (success) {
      portal = ReactDOM.createPortal(<span className={Styles.portal}>{successMsg}</span>, dataikuTagContainer);
      setDataikuNotificationPortal(portal);
      Notification.show(successMsg);
    } else {
      portal = ReactDOM.createPortal(
        <span className={classNames('error-message', Styles.portal)}>{errorMsg}</span>,
        dataikuTagContainer,
      );
      setDataikuNotificationPortal(portal);
      Notification.show(errorMsg, 'alert');
    }
  };

  const dataikuConnectionNotification = (portal) => {
    return portal;
  };

  const handleOK = () => {
    if (isCreatePage) {
      history.replace('/');
    }
    dispatch(hideConnectionInfo());
    setDataikuNotificationPortal(null);
  };

  const handleMakeConnection = () => {
    ProgressIndicator.show();
    const data = {
      data: {
        bucketName: bucketInfo.bucketName,
        dataikuProjects: connect.dataikuProjects?.map((item) => item.match(/\((.*?)\)/)[1]), // pick projectKey within paranthesis
      },
    };
    bucketsApi
      .connectToDataikuProjects(data)
      .then(() => {
        setSelectedDataikuProjects(connect?.dataikuProjects);
        NotificationMsg();
        ProgressIndicator.hide();
      })
      .catch(() => {
        NotificationMsg(false);
        ProgressIndicator.hide();
      });
  };

  const handleNotebookMakeConnection = () => {
    // ProgressIndicator.show();
    // const data = {
    //   data: {
    //     bucketName: bucketInfo.bucketName,
    //     dataikuProjects: connect.dataikuProjects?.map((item) => item.match(/\((.*?)\)/)[1]), // pick projectKey within paranthesis
    //   },
    // };
    // bucketsApi
    //   .connectToDataikuProjects(data)
    //   .then(() => {
    //     setSelectedDataikuProjects(connect?.dataikuProjects);
    //     NotificationMsg();
    //     ProgressIndicator.hide();
    //   })
    //   .catch(() => {
    //     NotificationMsg(false);
    //     ProgressIndicator.hide();
    //   });
    console.log('handle notebook connection');
  };

  /**
   * @param {array} arr selected array list
   * @param {string} key to be matched
   */
  const filterDataikuProjectList = (arr) => {
    return dataikuProjectList?.filter((item) => arr?.includes(item['name']))?.map((item) => item.name);
  };

  const connectToJupyter = (
    <>
      <code>
        {`from minio import Minio
          import pandas as pd
          MINIO_BUCKET = "${bucketInfo.bucketName}"
          minio_client = Minio('${bucketInfo.accessInfo.hostName}', access_key='${bucketInfo.accessInfo.accesskey}', secret_key='YOUR_BUCKET_SECRET_KEY', secure=True)
          y_file_obj = minio_client.get_object(MINIO_BUCKET, <<filepath>>)
          y = pd.read_csv(y_file_obj)`}
          {console.log('creatorInfo: ', bucketInfo)}
          {console.log('currentUser: ', props.user)}
      </code>
      <div className={Styles.dataikuConnectionBtn}>
        {' '}
        <button className="btn btn-tertiary" onClick={handleNotebookMakeConnection} disabled={bucketInfo.creator?.id !== props.user.id}>
          Make Connection
        </button>
      </div>
    </>
  );

  const connectToDataiku = (
    <>
      {dataikuProjectList?.length ? (
        <Tags
          title={'Please select the Dataiku project(s) that you want to link to the bucket.'}
          max={100}
          chips={filterDataikuProjectList(connect?.dataikuProjects)}
          tags={dataikuProjectList?.filter((item) => item.projectKey)}
          setTags={(selectedTags) => {
            const data = filterDataikuProjectList(selectedTags);
            dispatch({
              type: 'SELECTED_DATAIKU_PROJECTS',
              payload: data,
            });
            setDataikuNotificationPortal(null);
          }}
          suggestionRender={(item) => (
            <div className={Styles.optionContainer}>
              <span className={Styles.optionTitle}>{item?.name}</span>
              <span className={Styles.optionText}>{item?.shortDesc}</span>
            </div>
          )}
          isMandatory={false}
          showMissingEntryError={false}
          disableOnBlurAdd={true}
          suggestionPopupHeight={120}
          isDisabled={!bucketInfo.accessInfo.permission?.write}
        />
      ) : connect?.dataikuProjects?.length ? (
        <Tags
          title={`Dataiku project(s) linked to the bucket. You don't have projects to make new connection.`}
          chips={connect?.dataikuProjects}
          isDisabled={true}
        />
      ) : (
        <div className={Styles.emptyDataikuProjectsList}>No project(s) to connect</div>
      )}
      {dataikuTagContainer && dataikuConnectionNotification(dataikuNotificationPortal)}
      <div className={Styles.dataikuConnectionBtn}>
        {' '}
        <button className="btn btn-tertiary" onClick={handleMakeConnection} disabled={disableMakeConnectionBtn}>
          Make Connection
        </button>
      </div>
    </>
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
                <li className={`tab ${!isDataikuEnabled ? 'disable' : ''}`}>
                  <a href="#tab-content-2" id="dataiku">
                    <strong style={{ ...(!isDataikuEnabled && { color: '#99a5b3' }) }}>
                      {`Connect to Dataiku project(s)${!isDataikuEnabled ? ' ( Coming soon )' : ''}`}
                    </strong>
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
              <div className={classNames(Styles.connectionCode)}>
                {isLoading ? (
                  <div className={classNames('text-center', Styles.spinner)}>
                    <div className="progress infinite" />
                  </div>
                ) : (
                  connectToDataiku
                )}
              </div>
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
