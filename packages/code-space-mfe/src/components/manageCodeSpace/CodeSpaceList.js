import cn from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceList.scss';
// import { ICodeCollaborator } from 'globals/types';
import { history } from '../../store';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { Notification, ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import ViewRecipe from '../codeSpaceRecipe/viewRecipe';
import Modal from 'dna-container/Modal';

// export interface ICodeSpaceList {
//   id: any;
//   projectName: string;
//   projectOwner: ICodeCollaborator;
//   projectStatus?: string;
//   requestedDate: string;
//   onDataChanged: () => void;
//   isConfigList: boolean;
// }

const CodeSpaceList = (props) => {
  const [viewInfoModel, setviewInfoModel] = useState(false);
  const classNames = cn.bind(Styles);
  const onSecrityConfigClick = () => {
    history.push(`/codespace/adminSecurityconfig/${props.id}?name=${props.projectName}`);
  };
  const showErrorNotification = (message) => {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  const onNewRecipeClick = () => {
    setviewInfoModel(true);
  };

  const onPublish = (e) => {
    e.stopPropagation();
    ProgressIndicator.show();
    if (props.isConfigList) {
      const id = props.id;
      CodeSpaceApiClient.publishSecurityConfigRequest(id)
        .then(() => {
          Notification.show('Published successfully.');
          ProgressIndicator.hide();
          props.onDataChanged();
        })
        .catch((error) => {
          ProgressIndicator.hide();
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    } else {
      const name = props.projectName;
      CodeSpaceApiClient.publishCodeSpaceRecipeRequest(name)
        .then(() => {
          Notification.show('Published successfully.');
          ProgressIndicator.hide();
          props.onDataChanged();
        })
        .catch((error) => {
          ProgressIndicator.hide();
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    }
    ProgressIndicator.hide();
  };

  const onAccept = (e) => {
    e.stopPropagation();
    ProgressIndicator.show();
    if (props.isConfigList) {
      const id = props.id;
      CodeSpaceApiClient.acceptSecurityConfigRequest(id)
        .then(() => {
          Notification.show('Request Accepted.');
          ProgressIndicator.hide();
          props.onDataChanged();
        })
        .catch((error) => {
          ProgressIndicator.hide();
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    } else {
      const name = props.projectName;
      CodeSpaceApiClient.acceptCodeSpaceRecipeRequest(name)
        .then(() => {
          Notification.show('Request Accepted.');
          ProgressIndicator.hide();
          props.onDataChanged();
        })
        .catch((error) => {
          ProgressIndicator.hide();
          showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    }
    ProgressIndicator.hide();
  };


  // const formatDate = (date: string) => {
  //   const [datePart] = date.split('T');
  //   const [year, month, day] = datePart.split('-');
  //   const formattedDate = `${day}-${month}-${year}`;
  //   return formattedDate;
  // };
  return (
    <React.Fragment>
      <tr
        id={props.id}
        key={props.id}
        className={classNames('data-row', Styles.securityConfigRow)}
        onClick={props.isConfigList ? onSecrityConfigClick : onNewRecipeClick}
      >
        <td className={'wrap-text ' + classNames(Styles.securityConfigName)}>
          <div className={Styles.securityConfigNameDivide}>{props.projectName}</div>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span className={Styles.securityConfig}>
            {props.projectOwner?.firstName + ' ' + props.projectOwner.lastName}
          </span>
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span>{props.projectStatus}</span>
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span>{props.requestedDate !== null ? regionalDateAndTimeConversionSolution(props.requestedDate) : ''}</span>
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
      {viewInfoModel&&(<Modal
        title={''}
        hiddenTitle={true}
        showAcceptButton={false}
        showCancelButton={false}
        modalWidth="60vw"
        buttonAlignment="right"
        show={viewInfoModel}
        content={<ViewRecipe recipeName={props.projectName} />}
        onCancel={() => {
          setviewInfoModel(false);
        }}
      />)}
    </React.Fragment>
  );
};
export default CodeSpaceList;
