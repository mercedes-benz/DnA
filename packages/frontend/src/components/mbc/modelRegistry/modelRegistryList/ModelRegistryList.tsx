import classNames from 'classnames';
import React, { useState } from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import Styles from '../ModelRegistry.scss';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import InfoModal from '../../../../components/formElements/modal/infoModal/InfoModal';
import { ModelRegistryApiClient } from '../../../../services/ModelRegistryApiClient';

export interface IModelRegistryProps {
  item: string;
}

const ModelRegistryList = (props: IModelRegistryProps) => {
  const [info, setInfo] = useState(false);
  const [accessKey, setAccessKey] = useState('');
  const [accessID, setAccessID] = useState('');
  const [showAccessKey, setShowAccessKey] = useState(false);
  const [externalUri, setExternalUri] = useState('');

  const onInfoModalShow = () => {
    setInfo(true);
  };
  const onInfoModalCancel = () => {
    setInfo(false);
  };
  const getExternalURI = () => {
    ProgressIndicator.show();
    const model = {
      data: {
        modelName: props.item,
      },
    };
    ModelRegistryApiClient.createExternalURI(model)
      .then((res: any) => {
        setExternalUri(res.data.externalUrl);
        setAccessID(res.data.appId);
        setAccessKey(res.data.appKey);
        ProgressIndicator.hide();
        onInfoModalShow();
      })
      .catch((err: any) => {
        ProgressIndicator.hide();
        Notification.show('Error while getting service details for given model', 'alert');
      });
  };

  const copyExternalURL = () => {
    navigator.clipboard.writeText(externalUri).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const copyToClipboard = (content: string) => {
    navigator.clipboard.writeText('');
    navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
  };

  const contentForInfo = (
    <div className={Styles.infoPopup}>
      <div className={Styles.modalContent}>
        <table>
          <tbody>
            <tr>
              <td>
                <strong>Access ID :</strong>
              </td>
              <td id="accessId" className={Styles.keys}>
                {accessID}
              </td>
              <td>
                <span className={Styles.copyIcon} onClick={() => copyToClipboard(accessID)}>
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
            <tr>
              <td>
                <strong>Access Key :</strong>
              </td>
              <td id="accessKey" className={Styles.keys}>
                {showAccessKey
                  ? accessKey
                  : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}
              </td>
              <td>
                {showAccessKey ? (
                  <React.Fragment>
                    <i
                      className={classNames('icon mbc-icon visibility-hide ', Styles.visibilityIcon)}
                      onClick={() => setShowAccessKey(false)}
                      tooltip-data="Hide"
                    />
                  </React.Fragment>
                ) : (
                  <React.Fragment>
                    <i
                      className={classNames('icon mbc-icon visibility-show ', Styles.visibilityIcon)}
                      onClick={() => setShowAccessKey(true)}
                      tooltip-data="Show"
                    />
                  </React.Fragment>
                )}
                <span className={Styles.copyIcon} onClick={() => copyToClipboard(accessKey)}>
                  <i className="icon mbc-icon copy" />
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <p>
          Your External API URL to consume ML models is:
          <span className={Styles.externalURI}>
            {externalUri}
            <i className={'icon mbc-icon copy'} onClick={() => copyExternalURL()} tooltip-data="Copy" />
          </span>
        </p>
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <tr className="data-row">
        <td className="wrap-text">
          <p>{props.item}</p>
        </td>
        {/* <td className="wrap-text">
          <p>id here</p>
        </td>
        <td className={Styles.apiKey}>
          <div className={Styles.appIdParentDiv}>
            <div className={Styles.refreshedKey}>
              {showApiKey ? (
                <React.Fragment>a8dlksdh8aldfdjlfnsd8fsdfsod8fs</React.Fragment>
              ) : (
                <React.Fragment>
                  &bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;
                </React.Fragment>
              )}
            </div>
            <div className={Styles.refreshedKeyIcon}>
              {showApiKey ? (
                <React.Fragment>
                  <i
                    className={Styles.showAppId + ' icon mbc-icon visibility-hide'}
                    onClick={() => setShowApiKey(false)}
                    tooltip-data="Hide"
                  />
                </React.Fragment>
              ) : (
                <React.Fragment>
                  <i
                    className={Styles.showAppId + ' icon mbc-icon visibility-show ' + Styles.visiblityshow}
                    onClick={() => setShowApiKey(true)}
                    tooltip-data="Show"
                  />
                </React.Fragment>
              )}
              <i
                className={Styles.cpyStyle + ' icon mbc-icon copy'}
                onClick={() => alert('copied')}
                tooltip-data="Copy"
              />
            </div>
          </div>
          
          <div className={Styles.usage}>
            <div className={Styles.expireOn}>
              <span className={Styles.metaLabel}>Expire On: </span>
              <span className={Styles.expireOnValue}>{!item.expireOn ? 'Never' : regionalDateAndTimeConversionSolution(item.expireOn)}</span>
              <span className={Styles.expireOnValue}>Never</span>
            </div>
            <div className={Styles.lastUsed}>
              <span className={Styles.metaLabel}>Last Used: </span>
              <span className={Styles.lastUsedOnValue}>
                {item.lastUsedOn
                  ? getDateDifferenceFromTodayUsingGetDate(item.lastUsedOn) === 0
                    ? 'Today'
                    : getDateDifferenceFromTodayUsingGetDate(item.lastUsedOn) + ' Days ago'
                  : 'NA'}
              </span>
              <span className={Styles.lastUsedOnValue}>NA</span>
            </div>
            <div className={Styles.usageCount}>
              <span className={Styles.metaLabel}>Usage count: </span> 
              <span className={Styles.metaValue}>{item.usageCount == null ? 0 : item.usageCount}</span>
              <span className={Styles.metaValue}>0</span>
            </div>
          </div>
        </td> */}
        <td className={Styles.actionMenus}>
          <button onClick={getExternalURI} className={'btn btn-primary ' + Styles.actionBtn} type="button">
            <i className="icon mbc-icon link" />
            <span>Get External API URL</span>
          </button>
        </td>
      </tr>
      {info && (
        <InfoModal
          title={'External URL'}
          modalWidth={'35vw'}
          show={info}
          content={contentForInfo}
          onCancel={onInfoModalCancel}
        />
      )}
    </React.Fragment>
  );
};

export default ModelRegistryList;
