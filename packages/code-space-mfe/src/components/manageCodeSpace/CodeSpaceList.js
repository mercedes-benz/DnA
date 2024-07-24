import cn from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceList.scss';
// import { ICodeCollaborator } from 'globals/types';
import { history } from '../../store';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { Notification, ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
// import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import ViewRecipe from '../codeSpaceRecipe/ViewRecipe';
import Modal from 'dna-container/Modal';

// export interface IRecipeList {
//   id: any;
//   projectName: string;
//   maxRam?: string;
//   maxCpu?: string;
//   diskSpace?: string;
//   onDataChanged: () => void;
//   software?: string[];
//   isConfigList: boolean;
//   isPublic: boolean;
// }

const CodeSpaceList = (props) => {
  const [viewInfoModel, setviewInfoModel] = useState(false);
  const [notificationMsg, setNotificationMsg] = useState(false);
  const classNames = cn.bind(Styles);
  const onSecrityConfigClick = () => {
    history.push(`/codespace/adminSecurityconfig/${props.id}?name=${props.projectName}`);
  };

  const onNewRecipeClick = () => {
    setviewInfoModel(true);
  };

  const chips =
    props?.software && props?.software?.length
      ? props?.software?.map((chip) => {
        return (
          <>
            <label className="chips">{chip}</label>&nbsp;&nbsp;
          </>
        );
      })
      : 'N/A';

  const onNotificationMsgAccept = () => {
    setNotificationMsg(false);
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpaceRecipe(props.projectName)
    .then(() => {
      ProgressIndicator.hide();
      Notification.show("Recipe Deleted Successfully")
    }).catch((err) => {
      ProgressIndicator.hide();
      Notification.show(err.message, 'alert');
    });
  };

  const convertRam = (value) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
  };

  const onNotificationMsgCancel = () => {
    setNotificationMsg(false);
  }

  const deleteModal = () => {
    setNotificationMsg(true);
  };
  return (
    <React.Fragment>
      <div>
        <Modal
          title="Public Visibility"
          show={notificationMsg}
          showAcceptButton={false}
          showCancelButton={false}
          acceptButtonTitle="Confirm"
          cancelButtonTitle="Cancel"
          buttonAlignment='right'
          scrollableContent={false}
          hideCloseButton={true}
          content={
            <div>
              <header>
                <button className="modal-close-button" onClick={onNotificationMsgCancel}><i className="icon mbc-icon close thin"></i></button>
              </header>
              <div>
                <p>The Recipe will be visible to all users. Are you sure to make it Public?</p>
              </div>
              <div className="btn-set footerRight">
                <button className="btn btn-primary" type="button" onClick={onNotificationMsgCancel}>Cancel</button>
                <button className="btn btn-tertiary" type="button" onClick={onNotificationMsgAccept}>Confirm</button>
              </div>
            </div>
          } />
      </div>
      <tr
        id={props.id}
        key={props.id}
        className={classNames('data-row', Styles.securityConfigRow)}
      >
        <td className={'wrap-text ' + classNames(Styles.securityConfigName)}>
          <div className={Styles.securityConfigNameDivide} onClick={props.isConfigList ? onSecrityConfigClick : onNewRecipeClick}>{props.projectName} </div>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol}>
          <span className={Styles.securityConfig} onClick={props.isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
            {"DiskSpace- " + props.diskSpace + "GB" + " CPU- " + props.maxCpu + " RAM- " + convertRam(props.maxRam)+"GB"}
          </span>
        </td>

        <td className={'wrap-text' + Styles.securityConfigCol} onClick={props.isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
          {chips}
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol} onClick={props.isConfigList ? onSecrityConfigClick : onNewRecipeClick}>
          <span>{props.isPublic ? "Yes" : 'No'}</span>
        </td>
        <td className={'wrap-text' + Styles.securityConfigCol + Styles.actionColumn}>
          <button
            className={
              'btn btn-primary ' + Styles.actionBtn
            }
            type="button"
            onClick={deleteModal}
          >
            <i className='icon delete'></i>
          </button>

        </td>
      </tr>
      {viewInfoModel && (<Modal
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
