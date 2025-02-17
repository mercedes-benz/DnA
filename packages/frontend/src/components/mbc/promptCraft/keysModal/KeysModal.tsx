import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './KeysModal.scss';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { PromptCraftApiClient } from '../../../../services/PromptCraftApiClient';

interface IKeys {
  privateKey: string;
  publicKey: string;
}

export interface IKeysModalProps {
  projectName: string;
  onOk: () => void;
}

const KeysModal = ({projectName, onOk}: IKeysModalProps) => {
  const [keys, setKeys] = useState<IKeys | null>(null);
  const [showSecretKey, setShowSecretKey] = useState(false);

  useEffect(() => {
    getKeys(projectName);
  }, [projectName]);

  const getKeys = (projectName: string) => {      
      ProgressIndicator.show();
      PromptCraftApiClient
        .getSubscriptionKeys(projectName)
        .then((res: any) => {
          setKeys(res.data.data);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching prompt craft subscription keys failed!',
            'alert',
          );
        });
  };

  const handleKeysRefresh = () => {
    ProgressIndicator.show();
      PromptCraftApiClient
        .refreshSubscriptionKeys(projectName)
        .then(() => {
          Notification.show('Prompt Craft subscription keys refreshed');
          getKeys(projectName);
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Refreshing prompt craft subscription keys failed!',
            'alert',
          );
        });
  }

  const copyToClipboard = (content: string) => {
    navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
  };

  return (
    <div>
      <header>
        <h3>Secret Keys</h3>
        <button className={classNames('btn btn-primary', Styles.btnRefresh)} onClick={handleKeysRefresh}><i className="icon mbc-icon refresh" /> Refresh Keys</button>
      </header>
      <div className={Styles.keysContainer}>
        <div className={Styles.keyItem}>
          <div>
            Public Key:
          </div>
          <div id="secretKey" className={Styles.keys}>
            {showSecretKey
              ? keys?.publicKey
              : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}
          </div>
          <div>
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
            <span className={Styles.copyIcon} onClick={() => copyToClipboard(keys.publicKey)}>
              <i className="icon mbc-icon copy" />
            </span>
          </div>
        </div>
        <div className={Styles.keyItem}>
          <div>
            Private Key:
          </div>
          <div id="secretKey" className={Styles.keys}>
            {showSecretKey
              ? keys?.privateKey
              : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}
          </div>
          <div>
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
            <span className={Styles.copyIcon} onClick={() => copyToClipboard(keys.privateKey)}>
              <i className="icon mbc-icon copy" />
            </span>
          </div>
        </div>
      </div>
      <div className={Styles.pBtnContainer}>
        <button className={classNames('btn btn-tertiary')} onClick={onOk}>Okay</button>
      </div>
    </div>
  );
};
export default KeysModal;
