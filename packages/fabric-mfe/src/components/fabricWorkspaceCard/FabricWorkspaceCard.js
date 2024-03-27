import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './fabric-workspace-card.scss';
import { useHistory } from 'react-router-dom';
// Container Components
import Modal from 'dna-container/Modal';
// import ConfirmModal from 'dna-container/ConfirmModal';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
// import Notification from '../../common/modules/uilab/js/src/notification';
import FabricWorkspaceForm from '../fabricWorkspaceForm/FabricWorkspaceForm';

const FabricWorkspaceCard = ({user,workspace,onRefresh}) => {
  const [editWorkspace, setEditWorkspace] = useState(false);
  // const [showDeleteModal, setShowDeleteModal] = useState(false);
  const history = useHistory();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  return (
    <>
      <div className={Styles.projectCard}>
        <div
          className={Styles.cardHead}
          onClick={() => {
            history.push(`/workspace/${workspace?.id}`);
          }}
        >
          <div className={Styles.cardHeadInfo}>
            <div>
              <div className={Styles.cardHeadTitle}>{workspace?.workspaceName}</div>
              <div className="btn btn-text forward arrow"></div>
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(workspace.createdOn)}</div>
            </div>
            <div>
              <div>schema</div>
              <div>{workspace.schemaName}</div>
            </div>
            <div>
              <div>Classification</div>
              <div>{workspace.classificationType || 'N/A'}</div>
            </div>
            <div>
              <div>Connector Type</div>
              <div>{workspace.connectorType || 'N/A'}</div>
            </div>
          </div>
        </div>
        <div className={Styles.cardFooter}>
          <div>&nbsp;</div>
          <div className={Styles.btnGrp}>
            <button className={classNames("btn btn-primary",workspace.createdBy.id === user.id ? "" :"hide")} onClick={() => setEditWorkspace(true)}>
              <i className="icon mbc-icon edit fill"></i>
              <span>Edit</span>
            </button>
            <button className={classNames("btn btn-primary", Styles.btnDisabled)}>
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
          </div>
        </div>
      </div>
      { editWorkspace &&
        <Modal
          title={'Edit Fabric Workspace'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editWorkspace}
          content={<FabricWorkspaceForm edit={true} workspace={workspace} onSave={() => {setEditWorkspace(false); onRefresh()}} />}
          scrollableContent={false}
          onCancel={() => setEditWorkspace(false)}
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
};
export default FabricWorkspaceCard;