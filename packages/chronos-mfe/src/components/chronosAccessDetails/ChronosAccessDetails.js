import React, { useState, useEffect } from 'react';
import Styles from './ChronosAccessDetails.scss';
// import from DNA Container
import Modal from 'dna-container/Modal';
// app components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// utils
import { Envs } from '../../utilities/envs';
// services
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../spinner/Spinner';
import { useParams } from 'react-router-dom';

const ChronosAccessDetails = () => {
  const { id: projectId } = useParams();
  const [loading, setLoading] = useState(true);
  const [apiKey, setApiKey] = useState('');
  const [generateNewApiKey, setGenerateNewApiKey] = useState(false); 
  const [showApiKey, setShowApiKey] = useState(false);

  useEffect(() => {
    chronosApi.getApiKeyById(projectId).then((res) => {
      if (res?.data?.data?.apiKey) {
        setApiKey(res.data.data.apiKey);
      }
      setLoading(false);
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
    });
  }, [projectId]);

  const generateApiKey = () => {
    ProgressIndicator.show();
    chronosApi.generateApiKeyById(projectId).then((res) => {
      if (res?.data?.data?.apiKey) {
        setApiKey(res.data.data.apiKey);
      }
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.response?.data?.response?.errors[0]?.message || 'Failed to generate API Key', 'alert');
      ProgressIndicator.hide();
    });
  };

  const copyApiKey = () => {
    navigator.clipboard.writeText(apiKey).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const copyApiID = () => {
    navigator.clipboard.writeText(projectId).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const generateNewApiKeyContent = (
    <div className={Styles.modalContent}>
      <div className={Styles.projectWrapper}>
        <div>
          Old API Key will be invalidated once you proceed. Do you want to continue?.
        </div>
        <br />
        <div className={Styles.btnContainer}>
          <button
            className="btn btn-tertiary"
            type="button"
            onClick={(() => {
              generateApiKey();
              setGenerateNewApiKey(false);
            })}
          >
            {'Generate a New API Key'}
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <>
      { loading && <Spinner /> }
      <div className={Styles.firstPanel}>
          <div className={Styles.flexLayout}>
      { !loading && apiKey && <div>
        <div className={Styles.apiId}>
            <div className={Styles.appIdParentDiv}>
              <div className={Styles.refreshedAppId}>
              Api Id: <span className={Styles.refreshedAppId}>{projectId}</span>
              </div>
              <div className={Styles.refreshedKeyIcon}>
                <i
                  className={Styles.cpyStyle + ' icon mbc-icon copy'}
                  onClick={copyApiID}
                  tooltip-data="Copy"
                />
              </div>
            </div>
          </div>
          <div className={Styles.apiKey}>
            <div className={Styles.appIdParentDiv}>
              <div className={Styles.refreshedKey}>
              Api Key: { showApiKey ? (
                  <span className={Styles.refreshedKey}>{apiKey}</span>
                ) : (
                  <React.Fragment>
                      <span className={Styles.refreshedKey}>&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;</span>
                  </React.Fragment>
                )}
              </div>
              <div className={Styles.refreshedKeyIcon}>
                {showApiKey ? (
                  <React.Fragment>
                    <i
                      className={Styles.showAppId + ' icon mbc-icon visibility-hide'}
                      onClick={() => { setShowApiKey(!showApiKey) }}
                      tooltip-data="Hide"
                    />
                  </React.Fragment>
                ) : (
                  <React.Fragment>
                    <i
                      className={Styles.showAppId + ' icon mbc-icon visibility-show ' + Styles.visiblityshow}
                      onClick={() => { setShowApiKey(!showApiKey) }}
                      tooltip-data="Show"
                    />
                  </React.Fragment>
                )}
                <i
                  className={Styles.cpyStyle + ' icon mbc-icon copy'}
                  onClick={copyApiKey}
                  tooltip-data="Copy"
                />
              </div>
            </div>
          </div>
        </div> }
        { !loading &&
        <div>
          <div className={Styles.apiKey}>
            { apiKey ? 
            <button className={Styles.generateApiKeyBtn} onClick={() => setGenerateNewApiKey(true)}>
              Generate a New API Key
            </button> : 
            <button className={Styles.generateApiKeyBtn} onClick={() => generateApiKey()}>
              Generate a API Key
            </button> }
            { Envs.ENABLE_CHRONOS_ONEAPI && <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p> }
          </div>
        </div>
        }
        </div>
        {/* <div className={Styles.infoSection}>
          <i className="icon mbc-icon info" /> 
          <span>Some info here</span>
        </div> */}
      </div>
      { generateNewApiKey &&
          <Modal
            title={"Are you sure you want to generate a New API Key ?"}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'60%'}
            buttonAlignment="right"
            show={generateNewApiKey}
            content={generateNewApiKeyContent}
            scrollableContent={false}
            onCancel={() => {
              setGenerateNewApiKey(false);
            }}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '60%',
              maxWidth: '50vw'
            }}
          />
        }
    </>
  );
}

export default ChronosAccessDetails;