import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './AzureKeyVault.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
const classNames = cn.bind(Styles);
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { ApiClient } from '../../../../src/services/ApiClient';
import CreateNewKeyVault from './createNewKeyVault/CreateNewKeyVault';
import Modal from '../../formElements/modal/Modal';
import { useHistory } from 'react-router-dom';
import { IKeyVault } from 'globals/types';
import AzureKeyVaultCard from './AzureKeyVaultCard';

const AzureKeyVault = () => {
  const [keyVaultList, setKeyVaultList] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedKeyVault, setSelectedKeyVault] = useState<IKeyVault | null>(null);

  const History = useHistory();
  const goback = () => {
    History.goBack();
  };

  useEffect(() => {
    getKeyVaultList();
  }, []);

  const getKeyVaultList = () => {
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    ApiClient.getKeyVaults()
      .then((response) => {
        setKeyVaultList(response?.records || []);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  };

  const onEditWorkspace = (keyVault : IKeyVault) => {
    setSelectedKeyVault(keyVault);
    setIsEditMode(true);
    setShowCreateModal(true);
  }

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
            Back
          </button>
          <div className={classNames(Styles.caption)}>
            <h3>My Azure Key Vaults</h3>
          </div>
          {!keyVaultList?.length ? (
            <div className={classNames(Styles.content)}>
              <div className={Styles.listContent}>
                <div className={Styles.emptyCodeSpaces}>
                  <span>
                    You don&apos;t have any key vaults at this time.
                    <br /> Please create a new one.
                  </span>
                </div>
                <div className={Styles.subscriptionListEmpty}>
                  <br />
                  <button className={'btn btn-tertiary'} type="button" onClick={() => { setShowCreateModal(true); setIsEditMode(false); setSelectedKeyVault(null);}}>
                    <span>Create new Key Vault</span>
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className={Styles.allProjectContent}>
              <div className={Styles.newProjectCard} onClick={() => { setShowCreateModal(true); setIsEditMode(false); setSelectedKeyVault(null);}}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create New Key Vault</label>
                </div>
              {keyVaultList?.map((project, index) => {
                return (
                  <AzureKeyVaultCard
                    key={index}
                    project={project}
                    onEditWorkspace={() => onEditWorkspace(project)}
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
            <CreateNewKeyVault
              edit={isEditMode}
              project={selectedKeyVault}
              setShowCreateModal={() => setShowCreateModal(false)}
              getKeyVaultList={getKeyVaultList}
            />
          }
          scrollableContent={true}
          onCancel={() => { setShowCreateModal(false); getKeyVaultList(); }}
        />
      )}
    </React.Fragment>
  );
};

export default AzureKeyVault;
