import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AiCodeSpaces.scss';
import { useHistory } from 'react-router-dom';
import { aiHistory } from '../../store';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
// import AiCodeSpaceForm from './AiCodeSpaceForm';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import DeployApprovalModal from '../DeployApprovalModal/DeployApprovalModal';
import CodeSpaceBlueprint from '../codeSpaceBlueprint/CodeSpaceBlueprint';
import CodeSpaceGroupCard from '../codeSpaceGroupCard/CodeSpaceGroupCard';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import DeployModal from '../deployModal/DeployModal';
import BuildModal from '../buildModal/buildModal';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import CodeSpaceCardItem from '../codeSpaceCardItem/CodeSpaceCardItem';
import ProgressWithMessage from 'dna-container/ProgressWithMessage';
import { Envs } from '../../Utility/envs';
import TextBox from 'dna-container/TextBox';

const AiCodeSpaces = (props) => {
  const [loading, setLoading] = useState(true);
  const [aiCodeSpaces, setAiCodeSpaces] = useState([]);
  const [showCodespacesModal, setShowCodespacesModal] = useState(false);
  const [selectedCodeSpaceGroup, setSelectedCodeSpaceGroup] = useState(JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.AI_CODE_SPACE_SELECTED_GROUPS)));
  const [showDeleteCodespaceGroupModal, setShowDeleteCodespaceGroupModal] = useState(false);
  const [showDeployCodeSpaceModal, setShowDeployCodeSpaceModal] = useState(false);
  const [showBuildCodeSpaceModal, setShowBuildCodeSpaceModal] = useState(false);
  const [showDeployApprovalModal, setShowDeployApprovalModal] = useState(false);
  const [onDeployCodeSpace, setOnDeployCodeSpace] = useState();
  const [blueprintCodespace, setBlueprintCodespace] = useState();
  const [showBlueprintModal, setShowBlueprintModal] = useState(false);
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState(false);
  const [showInitializeModal, setShowInitializeModal] = useState(false);
  const [initializeId, setInitializeId] = useState(false);
  const [patToken, setPatToken] = useState("");
  const [patTokenError, setPatTokenError] = useState("");
  // const [showAiCodeSpaceForm, setShowAiCodeSpaceForm] = useState(false);

  const getAiCodeSpacesData = () => {
    setLoading(false);
    setAiCodeSpaces([]);
    setLoading(true);
    CodeSpaceApiClient.getAiAgentGroups()
      .then((res) => {
        setLoading(false);
        if (res.status !== 204) {
          setAiCodeSpaces(res?.data?.data?.data);
        } else {
          setAiCodeSpaces([]);
        }
      })
      .catch((err) => {
        setLoading(false);
        Notification.show('Error in loading your code space groups - ' + err.message, 'alert');
      });
  };

  useEffect(() => {
    getAiCodeSpacesData();
  }, []);

  useEffect(() => {
    setSelectedCodeSpaceGroup(JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.AI_CODE_SPACE_SELECTED_GROUPS)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aiCodeSpaces]);

  const toggleProgressMessage = (show) => {
    setIsApiCallTakeTime(show);
  };

  const deleteCodeSpaceGroupContent = (
    <div>
      Do you really want to delete <br /> this Code Space Group?
    </div>
  );

  const onCodeSpaceDeploy = (codeSpace) => {
    setOnDeployCodeSpace(codeSpace);
    setShowDeployCodeSpaceModal(true);
  };

  const onCodeSpaceShowBlueprint = (codeSpace) => {
    setBlueprintCodespace(codeSpace);
    setShowBlueprintModal(true);
  };

  const onCodeSpaceBuild = (codeSpace) => {
    setOnDeployCodeSpace(codeSpace);
    setShowBuildCodeSpaceModal(true);
  };

  const onShowDeployApprovalModal = (codeSpace) => {
    setOnDeployCodeSpace(codeSpace);
    setShowDeployApprovalModal(true);
  };

  const onStartStopCodeSpace = (codeSpace, startSuccessCB, env, manual = false) => {
    Tooltip.clear();
    if (codeSpace?.projectDetails?.isAgentCreation && !codeSpace?.projectDetails?.isAgentInitialized) {
      setShowInitializeModal(true);
      setInitializeId(codeSpace?.id);
    } else {
      const serverStarted = codeSpace.serverStatus === 'SERVER_STARTED';
      serverStarted ? setLoading(true) : ProgressIndicator.show();
      CodeSpaceApiClient.startStopWorkSpace(codeSpace.id, serverStarted, env, manual)
        .then((res) => {
          serverStarted ? setLoading(false) : ProgressIndicator.hide();
          if (res.data.success === 'SUCCESS') {
            Notification.show(
              'Your Codespace for project ' +
              codeSpace.projectDetails?.projectName +
              ' is requested to ' +
              ((serverStarted && !manual) ? 'stop' : 'start') +
              '.',
            );

            !manual && startSuccessCB();

          } else {
            Notification.show(
              'Error in ' + (serverStarted ? 'stopping' : 'starting') + ' your code spaces. Please try again later.',
              'alert',
            );
          }
        })
        .catch((err) => {
          serverStarted ? setLoading(false) : ProgressIndicator.hide();
          Notification.show(
            'Error in ' + (serverStarted ? 'stopping' : 'starting') + ' your code spaces - ' + err.message,
            'alert',
          );
        }).finally(() => {
          Tooltip.defaultSetup();
        });
    }

  };

  const deleteCodeSpaceGroupAccept = () => {
    console.log('Delete all codespaces');
  }

  const codespacesModalContent = <>
    <h2 className={classNames(Styles.modalTitle)}>{selectedCodeSpaceGroup?.name}</h2>
    {loading ? (
      <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
        <div className="progress infinite" />
      </div>
    ) : (
      <div className={Styles.csCardsContainer}>
        <div>
          {selectedCodeSpaceGroup?.workspaces?.length === 0 ? (
            <div className={classNames(Styles.content)}>
              <div className={Styles.listContent}>
                <div className={Styles.emptyCodeSpaces}>
                  <span>
                    You don&apos;t have any code space at this time.
                    <br /> Please create a new one.
                  </span>
                </div>
                <div className={Styles.subscriptionListEmpty}>
                  <br />
                  <button className={'btn btn-tertiary'} type="button" onClick={() => {
                    aiHistory.push('aicodespaceform');
                  }}>
                    <span>Create new Code Space</span>
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <>
              <div className={Styles.cardsSeparator}>
                <h5 className="sub-title-text">My Code Spaces</h5>
                <hr />
              </div>
              <div className={Styles.allCodeSpacesContent}>
                <div className={classNames('cardSolutions', Styles.allCodeSpacesCardviewContent)}>
                  {selectedCodeSpaceGroup?.workspaces?.filter((workspace) => workspace?.projectDetails?.projectOwner?.id === props.user.id)?.map((workspace, index) => {
                    return (
                      <CodeSpaceCardItem
                        key={index}
                        userInfo={props.user}
                        codeSpace={workspace}
                        toggleProgressMessage={toggleProgressMessage}
                        onDeleteSuccess={() => { }}
                        onShowCodeSpaceOnBoard={() => { }}
                        onCodeSpaceEdit={() => { }}
                        onShowDeployModal={onCodeSpaceDeploy}
                        onShowBuildModal={onCodeSpaceBuild}
                        onShowDeployApprovalModal={onShowDeployApprovalModal}
                        onStartStopCodeSpace={onStartStopCodeSpace}
                        onShowBlueprintModal={onCodeSpaceShowBlueprint}
                        onGetCodespaceData={() => { getAiCodeSpacesData(); }}
                        isAiGroupModal={true}
                      />
                    );
                  })}

                </div>
              </div>
              {(selectedCodeSpaceGroup?.workspaces?.some(workspace => workspace?.projectDetails?.projectOwner?.id !== props.user.id)) && (

                <div className={Styles.cardsSeparator}>
                  <h5 className="sub-title-text">Collaborated Code Spaces</h5>
                  <hr />
                </div>

              )}
              <div className={Styles.allCodeSpacesContent}>
                <div className={classNames('cardSolutions', Styles.allCodeSpacesCardviewContent)}>
                  {selectedCodeSpaceGroup?.workspaces?.filter((workspace) => workspace?.projectDetails?.projectOwner?.id !== props.user.id)?.map((workspace, index) => {
                    return (
                      <CodeSpaceCardItem
                        key={index}
                        userInfo={props.user}
                        codeSpace={workspace}
                        toggleProgressMessage={toggleProgressMessage}
                        onDeleteSuccess={() => { }}
                        onShowCodeSpaceOnBoard={() => { }}
                        onCodeSpaceEdit={() => { }}
                        onShowDeployModal={onCodeSpaceDeploy}
                        onShowBuildModal={onCodeSpaceBuild}
                        onShowDeployApprovalModal={onShowDeployApprovalModal}
                        onStartStopCodeSpace={onStartStopCodeSpace}
                        onShowBlueprintModal={onCodeSpaceShowBlueprint}
                        onGetCodespaceData={() => { getAiCodeSpacesData(); }}
                        isAiGroupModal={true}
                      />
                    );
                  })}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    )}
  </>;

  const initializeModalContent = <>
    <p>Enter the information to start your workspace!</p>
    <div>
      <div>
        <TextBox
          type="password"
          controlId={'patTokenInput'}
          labelId={'patTokenLabel'}
          label={`Your Github(${Envs.CODE_SPACE_GIT_PAT_APP_URL}) Personal Access Token`}
          infoTip="Not stored only used for Code Space initial setup"
          placeholder={'Type here'}
          value={patToken}
          errorText={patTokenError}
          required={true}
          maxLength={50}
          onChange={(e) => {
            const githubTokenVal = e.currentTarget.value.trim();
            setPatToken(githubTokenVal);
            setPatTokenError(githubTokenVal.length ? '' : "*Missing entry");
          }}
        />
      </div>
    </div>
  </>;

  const onInitializeWorkspace = () => {
    let formValid = true;
    if (patToken === "") {
      formValid = false;
      setPatTokenError("*Missing Entry");
    }
    if (formValid) {
      const onBoardCodeSpaceRequest = {
        pat: patToken
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.initializeAiWorkflowSpace(initializeId, onBoardCodeSpaceRequest)
        .then((res) => {
          if (res.data?.data?.projectDetails?.isAgentInitialized) {
            ProgressIndicator.hide();
            Notification.show("Workspace successfully initialized. Please start your workspace after some time.");
            getAiCodeSpacesData();
          } else {
            ProgressIndicator.hide();
            Notification.show(
              'Error in creating new code space. Please try again later.\n' + res.data.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err) => {
          ProgressIndicator.hide();
          if (err.response.status === 409) {
            Notification.show(
              `Given workspace is already initialized`,
              'alert',
            );
          } else {
            Notification.show('Error in initializing code space. Please try again later.\n' + err.message, 'alert');
          }
        });
    }
  };

  const History = useHistory();
  const goback = () => {
    History.goBack();
  };

  const switchBackToCodeSpace = () => {
    setIsApiCallTakeTime(false);
    ProgressIndicator.hide();
    getAiCodeSpacesData();
  };

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={classNames(Styles.wrapper)}>
        <div className={classNames(Styles.caption)}>
          <div>
            <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
              Back
            </button>
            <h3>My AI Agent Code Spaces</h3>
            <small>
              Made with{' '}
              <svg
                stroke="#e84d47"
                fill="#e84d47"
                strokeWidth="0"
                viewBox="0 0 512 512"
                height="1em"
                width="1em"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z"></path>
              </svg>{' '}
              by Developers for Developers
            </small>
          </div>
          <div className={classNames(Styles.leftHeader)}>
            <div className={classNames(Styles.listHeader)}>
              <button
                className={'btn btn-primary'}
                tooltip-data="Refresh"
                onClick={() => {
                  getAiCodeSpacesData();
                }}
              >
                <i className="icon mbc-icon refresh" />
              </button>
            </div>
          </div>
        </div>
        {loading ? (
          <div className={'progress-block-wrapper '}>
            <div className="progress infinite" />
          </div>
        ) : (
          <div>
            {aiCodeSpaces?.length === 0 ? (
              <div className={classNames(Styles.content)}>
                <div className={Styles.listContent}>
                  <div className={Styles.emptyCodeSpaces}>
                    <span>
                      You don&apos;t have any AI Agent code spaces at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <button
                      className={'btn btn-tertiary'}
                      type="button"
                      onClick={() => {
                        aiHistory.push('aicodespaceform');
                      }}
                    >
                      <span>Create new AI Agent Code Space</span>
                    </button>
                    {/* <button
                      className={'btn btn-tertiary'}
                      type="button"
                      onClick={() => {
                        setShowAiCodeSpaceForm(true);
                      }}
                    >
                      <span>Create new AI Agent Code Space</span>
                    </button> */}
                  </div>
                </div>
              </div>
            ) : (
              <div className={classNames(Styles.groupContainer)}>
                <div className={classNames(Styles.group, Styles.createNew)} onClick={() => {
                  aiHistory.push('aicodespaceform');
                }}>
                  <div className={Styles.newCodeSpaceCard}>
                    <div className={Styles.addicon}> &nbsp; </div>
                    <label className={Styles.addlabel}>Create new AI Agent</label>
                  </div>
                </div>
                {!loading && aiCodeSpaces?.map(group =>
                  <CodeSpaceGroupCard
                    key={group?.id}
                    group={group}
                    userInfo={props.user}
                    onShowCodeSpacesModal={(show, group) => { setShowCodespacesModal(show); setSelectedCodeSpaceGroup(group); }}
                    onShowCodeSpaceGroupModal={() => { setSelectedCodeSpaceGroup(group); }}
                    onCodeSpaceGroupDeleteModal={(show, group) => { setSelectedCodeSpaceGroup(group); setShowDeleteCodespaceGroupModal(show); }}
                    onCodeSpaceDropped={() => { getAiCodeSpacesData(); }}
                    onStartStopCodeSpace={onStartStopCodeSpace}
                    onShowDeployModal={onCodeSpaceDeploy}
                    onShowCodeSpaceOnBoard={() => { }}
                    onShowBlueprintModal={onCodeSpaceShowBlueprint}
                    onShowBuildModal={onCodeSpaceBuild}
                    onGetCodespaceData={() => { getAiCodeSpacesData(); }}
                    isAiGroupModal={true}
                  />
                )}
              </div>
            )}
          </div>
        )}
      </div>
      {showCodespacesModal && (
        <Modal
          title={'Codespaces'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="90%"
          show={showCodespacesModal}
          content={codespacesModalContent}
          scrollableContent={true}
          onCancel={() => { setShowCodespacesModal(false); sessionStorage.removeItem('aiCodeSpaceSelectedGroups') }}
        />
      )}
      {showDeleteCodespaceGroupModal && (
        <ConfirmModal
          title={'Delete Code Space Group'}
          acceptButtonTitle="Yes"
          cancelButtonTitle={'No'}
          showAcceptButton={true}
          showCancelButton={true}
          show={showDeleteCodespaceGroupModal}
          content={deleteCodeSpaceGroupContent}
          onCancel={() => setShowDeleteCodespaceGroupModal(false)}
          onAccept={deleteCodeSpaceGroupAccept}
        />
      )}
      {showDeployCodeSpaceModal && (
        <DeployModal
          userInfo={props.user}
          codeSpaceData={onDeployCodeSpace}
          // enableSecureWithIAM={
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven'
          // }
          // isUIRecipe={
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'dash' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'streamlit' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'nestjs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'vuejs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'angular' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'react'
          // }
          setShowCodeDeployModal={(isVisible) => setShowDeployCodeSpaceModal(isVisible)}
          setCodeDeploying={() => { getAiCodeSpacesData(); }}
          setIsApiCallTakeTime={setIsApiCallTakeTime}
        />
      )}
      {showBuildCodeSpaceModal && (
        <BuildModal
          userInfo={props.user}
          codeSpaceData={onDeployCodeSpace}
          setShowCodeBuildModal={(isVisible) => setShowBuildCodeSpaceModal(isVisible)}
          // enableSecureWithIAM={
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven'
          // }
          // isUIRecipe={
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'dash' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'streamlit' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'nestjs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'vuejs' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'angular' ||
          //     onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'react'
          // }
          setShowCodeDeployModal={(isVisible) => setShowDeployCodeSpaceModal(isVisible)}
          setCodeDeploying={() => { getAiCodeSpacesData(); }}
          setCodeBuilding={() => { getAiCodeSpacesData(); }}
          setIsApiCallTakeTime={setIsApiCallTakeTime}
        />
      )}
      {showDeployApprovalModal && (
        <DeployApprovalModal
          show={showDeployApprovalModal}
          setShowDeployApprovalModal={setShowDeployApprovalModal}
          codeSpaceData={onDeployCodeSpace}
          setCodeDeploying={() => { getAiCodeSpacesData(); }}
          setIsApiCallTakeTime={setIsApiCallTakeTime}
        />
      )}
      {showBlueprintModal && (
        <Modal
          title={'Code Space Blueprint'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="80%"
          buttonAlignment="right"
          show={showBlueprintModal}
          content={<CodeSpaceBlueprint codespace={blueprintCodespace} />}
          scrollableContent={true}
          onCancel={() => { setShowBlueprintModal(false) }}
        />
      )}
      {isApiCallTakeTime && (
        <ProgressWithMessage
          message={
            <>
              Please wait as this process can take up to a minute....
              <br />
              <button className="btn btn-text back arrow" onClick={switchBackToCodeSpace}>
                Back to Code Spaces
              </button>
            </>
          }
        />
      )}
      {showInitializeModal && (
        <Modal
          title={'Initialize WorkBench'}
          hiddenTitle={true}
          showAcceptButton={true}
          showCancelButton={true}
          cancelButtonTitle={'Cancel'}
          acceptButtonTitle={'Initialize'}
          modalWidth="50%"
          show={showInitializeModal}
          content={initializeModalContent}
          scrollableContent={true}
          onCancel={() => { setShowInitializeModal(false); setPatTokenError(false); setPatToken("");}}
          onAccept={onInitializeWorkspace}
        />
      )}
      {/* {showAiCodeSpaceForm && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="1200px"
          buttonAlignment="right"
          show={showAiCodeSpaceForm}
          content={<AiCodeSpaceForm user={props.user} />}
          scrollableContent={true}
          onCancel={() => {
            setShowAiCodeSpaceForm(false);
            getAiCodeSpacesData();
          }}
        />
      )} */}
    </div>
  );
};
export default AiCodeSpaces;
