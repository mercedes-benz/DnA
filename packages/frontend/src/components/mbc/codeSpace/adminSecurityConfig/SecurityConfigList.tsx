import cn from 'classnames';
import React from 'react';
import Styles from './SecurityConfigList.scss';
import { ICodeCollaborator } from 'globals/types';
import { history } from '../../../../router/History';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { Notification, ProgressIndicator } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';

export interface ISecurityConfigList {
  configId: any;
  projectName: String;
  projectOwner: ICodeCollaborator;
  projectStatus: string;
  requestedDate: string;
  onDataChanged: () => void;
}

const SecurityConfigList = (props: ISecurityConfigList) => {
  const classNames = cn.bind(Styles);
  const onSecrityConfigClick = () => {
    history.push(`/codespace/adminSecurityconfig/${props.configId}?pub=true`);
  };
  const showErrorNotification = (message: string) => {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  const onPublish = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    ProgressIndicator.show();
    const id = props.configId;
    CodeSpaceApiClient.publishSecurityConfigRequest(id)
      .then((res: any) => {
        Notification.show('Published successfully.');
        ProgressIndicator.hide();
        props.onDataChanged();
      })
      .catch((error: any) => {
        ProgressIndicator.hide();
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  const onAccept = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    ProgressIndicator.show();
    const id = props.configId;
    CodeSpaceApiClient.acceptSecurityConfigRequest(id)
      .then((res: any) => {
        Notification.show('Request Accepted.');
        ProgressIndicator.hide();
        props.onDataChanged();
      })
      .catch((error: any) => {
        ProgressIndicator.hide();
        showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };
  return (
    <React.Fragment>
      <tr
        id={props.configId}
        key={props.configId}
        className={classNames('data-row', Styles.securityConfigRow)}
        onClick={onSecrityConfigClick}
      >
        <td className={'wrap-text ' + classNames(Styles.securityConfigName)}>
          <div className={Styles.securityConfigNameDivide}>{props.projectName}</div>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span className={Styles.securityConfig}>
            {props.projectOwner.firstName + ' ' + props.projectOwner.lastName}
          </span>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span>{props.projectStatus}</span>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span>{props.requestedDate !== null ? props?.requestedDate.split(' ')[0] : ''}</span>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <button
            className={
              props.projectStatus === 'REQUESTED' ? 'btn btn-primary ' : 'btn btn-tertiary ' + Styles.actionBtn
            }
            type="button"
            onClick={props.projectStatus === 'REQUESTED' ? onAccept : onPublish}
          >
            {props.projectStatus === 'REQUESTED' ? 'Accept' : props.projectStatus === 'ACCEPTED' ? 'Publish' : ''}
          </button>
        </td>
      </tr>
    </React.Fragment>
  );
};
export default SecurityConfigList;
