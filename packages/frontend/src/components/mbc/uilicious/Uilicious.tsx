import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './Uilicious.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
const classNames = cn.bind(Styles);
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { ApiClient } from '../../../../src/services/ApiClient';
import UiliciousCardItem from './UiliciousCardItem';
import Modal from '../../formElements/modal/Modal';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import CreateNewWorkspace from './createNewWorkspace/CreateNewWorkspace';
import { useHistory } from 'react-router-dom';
import { IUiliciousLeanGovernance } from 'globals/types';
import { Envs } from 'globals/Envs';

const Uilicious = () => {
  const [workspaceList, setWorkspaceList] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [leanGovernance, setLeanGovernance] = useState<IUiliciousLeanGovernance>();
  const [selectedSpaceId, setSelectedSpaceId] = useState('');
  const [showContactModal, setShowContactModal] = useState(false);

  const History = useHistory();
  const goback = () => {
    History.goBack();
  };

  useEffect(() => {
    getWorkspaceList();
  }, []);

  const getWorkspaceList = () => {
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    ApiClient.getUiliciousWorkspaces()
      .then((response) => {
        setWorkspaceList(response?.items);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  };

  const onEditWorkspace = (spaceId: string, leanGovernance: IUiliciousLeanGovernance) => {
    setSelectedSpaceId(spaceId);
    setLeanGovernance(leanGovernance);
    setIsEditMode(true);
    setShowCreateModal(true);
  }

  const infoModalContent = (
    <div className={Styles.infoPopup}>
        For further assistance please contact <span className={Styles.info}>{Envs.UILICIOUS_CONTACT}</span>.
    </div>
  );

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
            Back
          </button>
          <div className={classNames(Styles.caption)}>
            <h3>My Uilicious Workspaces</h3>
            <div className={classNames(Styles.listHeader)}>
              {workspaceList?.length ? (
                <React.Fragment>
                  <button
                    className={'btn btn-primary'}
                    type="button"
                    onClick={() => { setShowContactModal(true); }}
                  >
                    <i className="icon mbc-icon info" tooltip-data="More information"/>
                  </button>
                </React.Fragment>
              ) : null}
            </div> 
          </div>
          {!workspaceList?.length ? (
            <div className={classNames(Styles.content)}>
              <div className={Styles.listContent}>
                <div className={Styles.emptyCodeSpaces}>
                  <span>
                    You don&apos;t have any workspaces at this time.
                    <br /> Please create a new one.
                  </span>
                </div>
                <div className={Styles.subscriptionListEmpty}>
                  <br />
                  <button className={'btn btn-tertiary'} type="button" onClick={() => { setShowCreateModal(true); setIsEditMode(false); }}>
                    <span>Create new Workspace</span>
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className={Styles.allProjectContent}>
              {/* <div className={Styles.newProjectCard} onClick={() => { setShowCreateModal(true); setIsEditMode(true); }}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Edit Lean Governance</label>
                </div> */}
              {workspaceList?.map((project, index) => {
                return (
                  <UiliciousCardItem
                    key={index}
                    project={project}
                    onEditWorkspace={() => onEditWorkspace(project?.spaceId, project?.leanGovernance)}
                  />
                );
              })}
            </div>
          )}
        </div>
      </div>
      {showCreateModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="70%"
          buttonAlignment="right"
          show={showCreateModal}
          content={
            <CreateNewWorkspace
              project={leanGovernance}
              edit={isEditMode}
              spaceId={selectedSpaceId}
              setShowCreateModal={() => setShowCreateModal(false)}
              getWorkspaceList={getWorkspaceList}
            />
          }
          scrollableContent={true}
          onCancel={() => { setShowCreateModal(false); setIsEditMode(false); getWorkspaceList(); }}
        />
      )}
      {showContactModal && (
        <InfoModal
          title={''}
          modalWidth={'40vw'}
          show={showContactModal}
          content={infoModalContent}
          modalStyle={{ minHeight: '30%' }}
          onCancel={() => {setShowContactModal(false);}}
        />
      )}
    </React.Fragment>
  );
};

export default Uilicious;
