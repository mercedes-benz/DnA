import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './AllCodeSpaces.scss';
// import { ICodeSpaceData } from './CodeSpace';
import CodeSpaceCardItem from './codeSpaceCardItem/CodeSpaceCardItem';
// import Pagination from '../pagination/Pagination';
import Modal from 'dna-container/Modal';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
// import { IUserInfo } from 'globals/types';
import ProgressWithMessage from 'dna-container/ProgressWithMessage';
import { useHistory } from 'react-router-dom';
import Notification from '../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../apis/codespace.api';
// @ts-ignore
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import { IconGear } from 'dna-container/IconGear';
// @ts-ignore
import Tooltip from '../common/modules/uilab/js/src/tooltip';
import DeployModal from './deployModal/DeployModal';
import { history } from '../store';
import CodeSpaceTutorials from './codeSpaceTutorials/CodeSpaceTutorials';
import BuildModal from './buildModal/buildModal';
import { Envs } from '../Utility/envs';
import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';
import DeployApprovalModal from './DeployApprovalModal/DeployApprovalModal';
import CodeSpaceBlueprint from './codeSpaceBlueprint/CodeSpaceBlueprint';
import AddCodespaceGroupModal from './addCodespaceGroupModal/AddCodespaceGroupModal';
import CodeSpaceGroupCard from './codeSpaceGroupCard/CodeSpaceGroupCard';
import Spinner from './spinner/Spinner';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';

// export interface IAllCodeSpacesProps {
//   user: IUserInfo;
// }

const AllCodeSpaces = (props) => {
    const [loading, setLoading] = useState(true);
    const [codeSpaceGroups, setCodeSpaceGroups] = useState([]);
    const [codeSpaces, setCodeSpaces] = useState([]),
        // [codeSpacesListResponse, setCodeSpacesListResponse] = useState([]),
        // [pagination, setPagination] = useState({
        //   totalNumberOfPages: 1,
        //   currentPageNumber: 1,
        //   maxItemsPerPage: 15,
        // }),
        [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState(false),
        [showDeployCodeSpaceModal, setShowDeployCodeSpaceModal] = useState(false),
        [showBuildCodeSpaceModal, setShowBuildCodeSpaceModal] = useState(false),
        [showDeployApprovalModal, setShowDeployApprovalModal] = useState(false),
        [isRetryRequest, setIsRetryRequest] = useState(false),
        [isApiCallTakeTime, setIsApiCallTakeTime] = useState(false),
        [onBoardCodeSpace, setOnBoardCodeSpace] = useState(),
        [onEditCodeSpace, setOnEditCodeSpace] = useState(),
        [onDeployCodeSpace, setOnDeployCodeSpace] = useState(),
        [showTutorialsModel, setShowTutorialsModel] = useState(false),
        [codeSpaceSearchTerm , setCodeSpaceSearchTerm] = useState(''),
        [filteredCodeSpaces, setFilteredCodespaces] = useState(),
        [showAwsFAQModal, setShowAwsFAQModal] = useState(false),
        [blueprintCodespace, setBlueprintCodespace] = useState(),
        [showBlueprintModal, setShowBlueprintModal] = useState(false);
    const History = useHistory();
    const goback = () => {
        History.goBack();
    };
    const [showAWSWarningModal, setShowAWSWarningModal] = useState(false);
    const [groupLoading, setGroupLoading] = useState(true);

    const getCodeSpacesData = () => {
        setLoading(true);
        CodeSpaceApiClient.getCodeSpacesList()
            .then((res) => {
                setLoading(false);
                setCodeSpaces(Array.isArray(res.data) ? res.data : (res.data.records) || []);
                // setLastCreatedId(Array.isArray(res) ? 0 : res.totalCount);
            })
            .catch((err) => {
                setLoading(false);
                Notification.show('Error in loading your code spaces - ' + err.message, 'alert');
            });
        // setCodeSpacesListResponse([]);
    };

    const getCodeSpaceGroupsData = () => {
        CodeSpaceApiClient.getCodeSpaceGroups()
            .then((res) => {
                setGroupLoading(false);
                if(res.status !== 204) {
                    setCodeSpaceGroups(res?.data?.data?.data);
                } else {
                    setCodeSpaceGroups([]);
                }
            })
            .catch((err) => {
                setGroupLoading(false);
                Notification.show('Error in loading your code space groups - ' + err.message, 'alert');
            });
    };

    useEffect(() => {
        setShowAWSWarningModal(Envs.SHOW_AWS_MIGRATION_WARNING);
        getCodeSpacesData();
        getCodeSpaceGroupsData();
    }, []);

    useEffect(() => {
        Tooltip.defaultSetup();
        setFilteredCodespaces(codeSpaces);
    }, [codeSpaces]);

    // const onPaginationPreviousClick = () => {
    //   const currentPageNumberTemp = pagination.currentPageNumber - 1;
    //   const currentPageOffset = (currentPageNumberTemp - 1) * pagination.maxItemsPerPage;
    //   const modifiedData = codeSpacesListResponse.slice(
    //     currentPageOffset,
    //     pagination.maxItemsPerPage * currentPageNumberTemp,
    //   );
    //   setCodeSpaces(modifiedData);
    //   setPagination({ ...pagination, currentPageNumber: currentPageNumberTemp });
    // };
    // const onPaginationNextClick = () => {
    //   let currentPageNumberTemp = pagination.currentPageNumber;
    //   const currentPageOffset = pagination.currentPageNumber * pagination.maxItemsPerPage;
    //   currentPageNumberTemp = pagination.currentPageNumber + 1;
    //   const modifiedData = codeSpacesListResponse.slice(
    //     currentPageOffset,
    //     pagination.maxItemsPerPage * currentPageNumberTemp,
    //   );
    //   setCodeSpaces(modifiedData);
    //   setPagination({ ...pagination, currentPageNumber: currentPageNumberTemp });
    // };
    // const onViewByPageNum = (pageNum: number) => {
    //   const totalNumberOfPages = Math.ceil(codeSpacesListResponse?.length / pageNum);
    //   console.log(codeSpacesListResponse);
    //   const modifiedData = codeSpacesListResponse.slice(0, pageNum);
    //   setCodeSpaces(modifiedData);
    //   setPagination({
    //     totalNumberOfPages,
    //     maxItemsPerPage: pageNum,
    //     currentPageNumber: 1,
    //   });
    // };

    const onShowNewCodeSpaceModal = () => {
        setShowNewCodeSpaceModal(true);
    };

    const onShowSecurityConfigRequest = () => {
       history.push(`manageRecipes`);
    };
    
    const isCodeSpaceCreationSuccess = (status, codeSpaceData) => {
        if (showNewCodeSpaceModal) {
            setShowNewCodeSpaceModal(!status);
            history.push(`codespace/${codeSpaceData.workspaceId}`);
        } else {
            getCodeSpacesData();
            getCodeSpaceGroupsData();
        }
    };

    const toggleProgressMessage = (show) => {
        setIsApiCallTakeTime(show);
    };

    const onNewCodeSpaceModalCancel = () => {
        if (onEditCodeSpace) {
            getCodeSpacesData();
            getCodeSpaceGroupsData();
        }
        setShowNewCodeSpaceModal(false);
        setOnBoardCodeSpace(undefined);
        setIsRetryRequest(false);
        setOnEditCodeSpace(undefined);
    };

    const onDeleteSuccess = () => {
        getCodeSpacesData();
        getCodeSpaceGroupsData();
    };

    const onShowCodeSpaceOnBoard = (codeSpace, isRetryRequest = false) => { //isRetry optional
        setOnEditCodeSpace(undefined);
        setOnBoardCodeSpace(codeSpace);
        isRetryRequest && setIsRetryRequest(true);
        setShowNewCodeSpaceModal(true);
    };

    const onCodeSpaceEdit = (codeSpace) => {
        setOnBoardCodeSpace(undefined);
        setOnEditCodeSpace(codeSpace);
        setShowNewCodeSpaceModal(true);
    };

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

    const onGetCodespaceData = () => {
        getCodeSpacesData();
        getCodeSpaceGroupsData();
    }

    const onStartStopCodeSpace = (codeSpace, startSuccessCB, env, manual = false) => {
        Tooltip.clear();
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
    };

    const switchBackToCodeSpace = () => {
        setOnEditCodeSpace(undefined);
        setOnBoardCodeSpace(undefined);
        setIsRetryRequest(false);
        setShowNewCodeSpaceModal(false);
        setIsApiCallTakeTime(false);
        ProgressIndicator.hide();
        getCodeSpacesData();
        getCodeSpaceGroupsData();
    };

    const AWSWarningModalContent = (
        <div className={Styles.modalContentWrapper}>
            <div className={Styles.awsModalMainTitle}><i className="icon mbc-icon alert circle" />DnA Platform successfully migrated<i className="icon mbc-icon alert circle" /></div>
            <br/>
            <div className={Styles.awsModalTitle}>Migrating your Deployed Applications: <span className={classNames(Styles.important)}>URGENT!!</span></div>
            <p>Please be aware that you must migrate your deployed applications before <span className={classNames(Styles.warning)}> May 9th, 2025</span>.
                For migration, please reach out to us.</p>            
            <div className={Styles.awsModalTitle}>Need Assistance?:</div>
            <p>Please refer to the <span className={classNames(Styles.warning)}>AWS migration FAQs</span> on our landing page. You can also join our <a href={Envs.CODESPACE_TEAMS_LINK} target='_blank' rel='noopener noreferrer'>Teams channel</a> or <a href={Envs.CODESPACE_MATTERMOST_LINK} target='_blank' rel='noopener noreferrer'>Mattermost channel</a> for help or to discuss any concerns.</p>
            <p><strong>Note:</strong> Deployed applications will be migrated to AWS based on the support request. If there were no prior deployments before the migration, any new deployments will automatically be directed to AWS.</p>
        </div>
    );
    const FAQModalContent = (
        <div className={Styles.modalFAQContentWrapper}>
          <div className="expansion-panel-group airflowexpansionPanel">
      
         
            <div className={classNames('expansion-panel')}>
              <span className="animation-wrapper"></span>
              <input type="checkbox" className="ff-only" id="faq-1" />
              <label className={classNames('expansion-panel-label', Styles.faqHeader)} htmlFor="faq-1">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                  <span>1. I am not able to see my code post migrating to AWS</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </div>
              </label>
              <div className="expansion-panel-content">
                <div className={classNames(Styles.info)}>
                  This situation arises if the PAT token that you used to create the codespace has expired. Please follow the steps below:
                  <ul>
                    <li>
                      Clone code manually:
                      <ol>
                        <li><span className={classNames(Styles.list)}>mkdir -p /home/coder/app</span></li>
                        <li><span className={classNames(Styles.list)}>git config --global credential.helper cache</span></li>
                        <li><span className={classNames(Styles.list)}>git config --global user.email &ldquo;$SHORTID&ldquo;</span></li>
                        <li><span className={classNames(Styles.list)}>git config --global user.name &ldquo;$SHORTID&ldquo;</span></li>
                        <li>
                          <span className={classNames(Styles.list)}>
                            git clone https://$GITHUB_TOKEN@$GITHUBREPO_URL /home/coder/app
                          </span>
                          <br />
                          (e.g., git clone https://ghp_xxxx@{(Envs.CODE_SPACE_GIT_PAT_APP_URL).split('https://')[1]}org_name/repo_name.git /home/coder/app)
                          <br />
                          If cloning fails, generate a new token.
                        </li>
                      </ol>
                    </li>
                    <li>
                      Copy bashrc:
                      <ol>
                        <li><span className={classNames(Styles.list)}>cp /tmp/.bashrc /home/coder/</span></li>
                        <li><span className={classNames(Styles.list)}>chmod +x /home/coder/.bashrc</span></li>
                      </ol>
                    </li>
                    <li>
                      Install required packages:
                      <ol>
                        <li><span className={classNames(Styles.list)}>TEMP_DIR=/tmp/.codespaces/DO_NOT_DELETE_MODIFY/</span></li>
                        <li><span className={classNames(Styles.list)}>mkdir -pv $TEMP_DIR</span></li>
                        <li>
                          <span className={classNames(Styles.list)}>
                            cp /home/coder/app/.codespaces/DO_NOT_DELETE_MODIFY/pkg-install.sh $TEMP_DIR
                          </span>
                          <br />
                          <span className={classNames(Styles.listInfo)}>If additional folder:</span>
                          <br />
                          <span className={classNames(Styles.list)}>
                            cp /home/coder/app/$YOUR_FOLDER/.codespaces/DO_NOT_DELETE_MODIFY/pkg-install.sh $TEMP_DIR
                          </span>
                        </li>
                        <li><span className={classNames(Styles.list)}>cd $TEMP_DIR</span></li>
                        <li><span className={classNames(Styles.list)}>chmod +x pkg-install.sh</span></li>
                        <li><span className={classNames(Styles.list)}>./pkg-install.sh</span></li>
                      </ol>
                    </li>
                    <li>Please close and reopen terminal to verify.</li>
                    <li>
                      For <span className={classNames(Styles.warning)}>Python FastAPI</span>:
                      <br />
                      <span className={classNames(Styles.list)}>curl -sSL https://install.python-poetry.org | python3 -</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
      
            <div className={classNames('expansion-panel')}>
              <span className="animation-wrapper"></span>
              <input type="checkbox" className="ff-only" id="faq-2" />
              <label className={classNames('expansion-panel-label', Styles.faqHeader)} htmlFor="faq-2">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                  <span>2. I am getting a WebSocket error: “The workbench failed to connect to the server”</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </div>
              </label>
              <div className="expansion-panel-content">
                <div className={classNames(Styles.info)}>
                  This issue typically occurs due to proxy settings interfering with the WebSocket connection.
                  <br /><br />
                  <ul>
                    <li>Open your <strong>Windows Settings</strong>.</li>
                    <li>Go to <strong>Network & Internet</strong> &rarr; <strong>Proxy</strong>.</li>
                    <li>Click on <strong>Set up</strong> under Manual proxy setup.</li>
                    <li>Turn off the toggle for <strong>Use a proxy server</strong>.</li>
                    <li>Restart your browser and reload your CodeSpace.</li>
                  </ul>
                </div>
              </div>
            </div>
      
            <div className={classNames('expansion-panel')}>
              <span className="animation-wrapper"></span>
              <input type="checkbox" className="ff-only" id="faq-3" />
              <label className={classNames('expansion-panel-label', Styles.faqHeader)} htmlFor="faq-3">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                  <span>3. CodeSpace stopped and keeps reloading while working</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </div>
              </label>
              <div className="expansion-panel-content">
                <div className={classNames(Styles.info)}>
                  This may happen due to session timeouts or infrastructure restarts.
                  <br /><br />
                  <ul>
                    <li>Stop the CodeSpace instance manually.</li>
                    <li>Wait for <strong>10 minutes</strong> to allow services to reset.</li>
                    <li>Restart the CodeSpace.</li>
                    <li>If the problem persists, please reach out to the <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer"> Teams channel</a> or 
                    <a href={Envs.CODESPACE_MATTERMOST_LINK} target="_blank" rel="noopener noreferrer"> Mattermost channel</a>. </li>
                  </ul>
                </div>
              </div>
            </div>
      
          </div>
        </div>
      );
      
      


    const [showEditCodespaceGroupModal, setShowEditCodespaceGroupModal]  = useState(false);
    const [showCodespacesModal, setShowCodespacesModal] = useState(false);
    const [selectedCodeSpaceGroup, setSelectedCodeSpaceGroup] = useState(JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS)));

    useEffect(() => {
        const cachedGroup = JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS));
        if (cachedGroup && codeSpaceGroups?.length > 0) {
          const updatedGroup = codeSpaceGroups.find(
            (group) => group.groupId === cachedGroup.groupId
          );
          if (updatedGroup) {
            setSelectedCodeSpaceGroup(updatedGroup);
            sessionStorage.setItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS, JSON.stringify(updatedGroup));
          }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
      }, [codeSpaceGroups]);

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
                                <button className={'btn btn-tertiary'} type="button" onClick={onShowNewCodeSpaceModal}>
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
                                            onDeleteSuccess={onDeleteSuccess}
                                            onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                                            onCodeSpaceEdit={onCodeSpaceEdit}
                                            onShowDeployModal={onCodeSpaceDeploy}
                                            onShowBuildModal={onCodeSpaceBuild}
                                            onShowDeployApprovalModal={onShowDeployApprovalModal}
                                            onStartStopCodeSpace={onStartStopCodeSpace}
                                            onShowBlueprintModal={onCodeSpaceShowBlueprint}
                                            onGetCodespaceData={onGetCodespaceData}
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
                                            onDeleteSuccess={onDeleteSuccess}
                                            onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                                            onCodeSpaceEdit={onCodeSpaceEdit}
                                            onShowDeployModal={onCodeSpaceDeploy}
                                            onShowBuildModal={onCodeSpaceBuild}
                                            onShowDeployApprovalModal={onShowDeployApprovalModal}
                                            onStartStopCodeSpace={onStartStopCodeSpace}
                                            onShowBlueprintModal={onCodeSpaceShowBlueprint}
                                            onGetCodespaceData={onGetCodespaceData}
                                        />
                                    );
                                })}
                            </div>
                        </div>
                        {/* {codeSpaces?.length ? (
        <Pagination
          totalPages={pagination.totalNumberOfPages}
          pageNumber={pagination.currentPageNumber}
          onPreviousClick={onPaginationPreviousClick}
          onNextClick={onPaginationNextClick}
          onViewByNumbers={onViewByPageNum}
          displayByPage={true}
        />
      ) : null} */}
                    </>
                )}
            </div>
        </div>
    )}
    </>;

    const [showAddCodespaceGroupModal, setShowAddCodespaceGroupModal] = useState(false);
    const [showDeleteCodespaceGroupModal, setShowDeleteCodespaceGroupModal] = useState(false);

    const deleteCodeSpaceGroupContent = (
            <div>
                Do you really want to delete <br /> this Code Space Group?
            </div>
        );

    const deleteCodeSpaceGroupAccept = () => {
        ProgressIndicator.show();
        CodeSpaceApiClient.deleteCodeSpaceGroup(selectedCodeSpaceGroup?.groupId)
            .then(() => {
                setShowDeleteCodespaceGroupModal(false);
                Notification.show(`Code Space Group deleted successfully`);
                getCodeSpaceGroupsData();
                getCodeSpacesData();
                ProgressIndicator.hide();
            })
            .catch((e) => {
                ProgressIndicator.hide();
                Notification.show(
                    e.response.data.errors?.length
                    ? e.response.data.errors[0].message
                    : 'Deleting code space group failed!',
                    'alert',
                );
            });
    }

    const draggableItemRef = useRef();

    return (
        <div className={classNames(Styles.mainPanel)}>
            <div className={classNames(Styles.wrapper)}>
                <div className={classNames(Styles.caption)}>
                    <div>
                        <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
                            Back
                        </button>
                        <h3>My Code Spaces</h3>
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
                                onClick={() => { getCodeSpacesData(); getCodeSpaceGroupsData(); }}
                            >
                                <i className="icon mbc-icon refresh" />
                            </button>
                            <button
                                className={classNames('btn btn-primary', Styles.newRecipe)}
                                type="button"
                                onClick={() => { history.push('/codespaceRecipes/codespace') }}
                            >
                                <i className={'icon mbc-icon plus'} />
                                <span>&nbsp;Add New Recipe</span>
                            </button>
                            <button
                                className={classNames('btn btn-primary', Styles.configIcon)}
                                type="button"
                                onClick={onShowSecurityConfigRequest}
                            >
                                <IconGear size={'14'} />
                                <span>&nbsp;Manage Recipes</span>
                            </button>

                            <button
                                className={classNames('btn btn-primary', Styles.tutorials)}
                                tooltip-data="code space video tutorials"
                                onClick={() => { setShowTutorialsModel(true) }}
                            >
                                <i className={classNames('icon mbc-icon trainings', Styles.trainingIcon)} />
                                <span>Video Tutorials</span>
                            </button>
                            <button
                                className={classNames('btn btn-primary', Styles.awsFAQ)}
                                tooltip-data="CodeSpace FAQs"
                                onClick={() => { setShowAwsFAQModal(true) }}
                            >
                                <i className={classNames('icon mbc-icon alert circle')} />
                                <span>CodeSpace FAQ&apos;s</span>
                            </button>
                        </div>
                        <div className={classNames(Styles.codspaceSearch)}>
                            <input
                                type="text"
                                className={classNames(Styles.searchInputField)}
                                placeholder="Search CodeSpace"
                                maxLength={100}
                                value={codeSpaceSearchTerm}
                                onChange={(e) => {
                                    const value = e.target.value;
                                    setCodeSpaceSearchTerm(value);
                                    const filteredRecipes = codeSpaces.filter((val) => val.projectDetails.projectName.toLowerCase().includes(value.toLowerCase()));
                                    console.log(codeSpaces);
                                    setFilteredCodespaces(filteredRecipes)
                                }}
                            />
                            <i
                                className={classNames('icon mbc-icon', codeSpaceSearchTerm?.length ? 'close circle' : 'search', Styles.searchIcon)}
                                onClick={()=>{
                                    if(codeSpaceSearchTerm?.length ){
                                        setCodeSpaceSearchTerm(""); 
                                        setFilteredCodespaces(codeSpaces);
                                    }
                                }}
                            />
                        </div>
                    </div>
                    
                </div>
                {groupLoading &&
                    <div className={classNames(Styles.emptyGroup, Styles.csLoading)}>
                        <Spinner />
                    </div>
                }
                {!groupLoading && codeSpaces?.length > 0 && codeSpaceGroups?.length === 0 && 
                    <div className={Styles.emptyGroup}>
                        <div>
                            <p>
                                You don&apos;t have any Code Space Group at this time.
                                <br /> Please create one.
                            </p>
                        </div>
                        <div>
                            <button className={'btn btn-primary'} type="button" onClick={() => setShowAddCodespaceGroupModal(true)}>
                                <span>Create Code Space Group</span>
                            </button>
                        </div>
                    </div>
                }
                {!groupLoading && codeSpaceGroups?.length > 0 &&
                    <div className={classNames(Styles.groupContainer)}>
                        <div className={classNames(Styles.group, Styles.createNew)} onClick={() => setShowAddCodespaceGroupModal(true)}>
                            <div className={Styles.newCodeSpaceCard}>
                                <div className={Styles.addicon}> &nbsp; </div>
                                <label className={Styles.addlabel}>Create new Group</label>
                            </div>
                        </div>
                        {!loading && codeSpaceGroups?.map(group => 
                            <CodeSpaceGroupCard
                                key={group?.id}
                                group={group}
                                userInfo={props.user}
                                onShowCodeSpacesModal={(show, group) => { setShowCodespacesModal(show); setSelectedCodeSpaceGroup(group);  }}
                                onShowCodeSpaceGroupModal={(show) => { setSelectedCodeSpaceGroup(group); setShowEditCodespaceGroupModal(show); }}
                                onCodeSpaceGroupDeleteModal={(show, group) => { setSelectedCodeSpaceGroup(group); setShowDeleteCodespaceGroupModal(show); }}
                                onCodeSpaceDropped={() => { getCodeSpaceGroupsData(); getCodeSpacesData();}}
                                onStartStopCodeSpace={onStartStopCodeSpace}
                                onShowDeployModal={onCodeSpaceDeploy}
                                onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                                onShowBlueprintModal={onCodeSpaceShowBlueprint}
                                onShowBuildModal={onCodeSpaceBuild}
                                onGetCodespaceData={onGetCodespaceData}
                            />
                        )}
                    </div>
                }
                {loading ? (
                    <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                        <div className="progress infinite" />
                    </div>
                ) : (
                    <div>
                        <div>
                            {codeSpaces?.length === 0 ? (
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
                                            <button className={'btn btn-tertiary'} type="button" onClick={onShowNewCodeSpaceModal}>
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
                                            <div className={Styles.newCodeSpaceCard} onClick={onShowNewCodeSpaceModal}>
                                                <div className={Styles.addicon}> &nbsp; </div>
                                                <label className={Styles.addlabel}>Create new Code Space</label>
                                            </div>
                                            {filteredCodeSpaces?.filter((codespace) => codespace?.projectDetails?.projectOwner?.id === props.user.id)?.map((codeSpace, index) => {
                                                return (
                                                    <CodeSpaceCardItem
                                                        ref={draggableItemRef}
                                                        key={index}
                                                        userInfo={props.user}
                                                        codeSpace={codeSpace}
                                                        toggleProgressMessage={toggleProgressMessage}
                                                        onDeleteSuccess={onDeleteSuccess}
                                                        onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                                                        onCodeSpaceEdit={onCodeSpaceEdit}
                                                        onShowDeployModal={onCodeSpaceDeploy}
                                                        onShowBuildModal={onCodeSpaceBuild}
                                                        onShowDeployApprovalModal={onShowDeployApprovalModal}
                                                        onStartStopCodeSpace={onStartStopCodeSpace}
                                                        onShowBlueprintModal={onCodeSpaceShowBlueprint}
                                                        onGetCodespaceData={onGetCodespaceData}
                                                    />
                                                );
                                            })}

                                        </div>
                                    </div>
                                    {(filteredCodeSpaces?.some(codeSpace => codeSpace?.projectDetails?.projectOwner?.id !== props.user.id)) && (
                                               
                                        <div className={Styles.cardsSeparator}>
                                            <h5 className="sub-title-text">Collaborated Code Spaces</h5>
                                            <hr />
                                        </div>
                                                
                                    )}
                                    <div className={Styles.allCodeSpacesContent}>
                                        <div className={classNames('cardSolutions', Styles.allCodeSpacesCardviewContent)}>
                                            {filteredCodeSpaces?.filter((codespace) => codespace?.projectDetails?.projectOwner?.id !== props.user.id)?.map((codeSpace, index) => {
                                                return (
                                                    <CodeSpaceCardItem
                                                        key={index}
                                                        userInfo={props.user}
                                                        codeSpace={codeSpace}
                                                        toggleProgressMessage={toggleProgressMessage}
                                                        onDeleteSuccess={onDeleteSuccess}
                                                        onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                                                        onCodeSpaceEdit={onCodeSpaceEdit}
                                                        onShowDeployModal={onCodeSpaceDeploy}
                                                        onShowBuildModal={onCodeSpaceBuild}
                                                        onShowDeployApprovalModal={onShowDeployApprovalModal}
                                                        onStartStopCodeSpace={onStartStopCodeSpace}
                                                        onShowBlueprintModal={onCodeSpaceShowBlueprint}
                                                        onGetCodespaceData={onGetCodespaceData}
                                                    />
                                                );
                                            })}
                                        </div>
                                    </div>
                                    {/* {codeSpaces?.length ? (
                    <Pagination
                      totalPages={pagination.totalNumberOfPages}
                      pageNumber={pagination.currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                ) : null} */}
                                </>
                            )}
                        </div>
                    </div>
                )}
            </div>
            {showAddCodespaceGroupModal && (
                <Modal
                    title={'Add Code Space Group'}
                    hiddenTitle={true}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="800px"
                    show={showAddCodespaceGroupModal}
                    content={<AddCodespaceGroupModal onSave={() => { setShowAddCodespaceGroupModal(false); getCodeSpaceGroupsData(); getCodeSpacesData(); }}/>}
                    scrollableContent={true}
                    onCancel={() => { setShowAddCodespaceGroupModal(false) }}
                />
            )}
            {showEditCodespaceGroupModal && (
                <Modal
                    title={'Edit Code Space Group'}
                    hiddenTitle={true}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="800px"
                    show={showEditCodespaceGroupModal}
                    content={<AddCodespaceGroupModal edit={true} group={selectedCodeSpaceGroup} onSave={() => { setShowEditCodespaceGroupModal(false); getCodeSpaceGroupsData(); getCodeSpacesData(); }}/>}
                    scrollableContent={true}
                    onCancel={() => { setShowEditCodespaceGroupModal(false) }}
                />
            )}
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
                    onCancel={() => { setShowCodespacesModal(false); sessionStorage.removeItem('codeSpaceSelectedGroups') }}
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
            {showNewCodeSpaceModal && (
                <Modal
                    title={''}
                    hiddenTitle={true}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="1200px"
                    buttonAlignment="right"
                    show={showNewCodeSpaceModal}
                    content={
                        <NewCodeSpace
                            user={props.user}
                            onBoardingCodeSpace={onBoardCodeSpace}
                            onEditingCodeSpace={onEditCodeSpace}
                            isRetryRequest={isRetryRequest}
                            isCodeSpaceCreationSuccess={isCodeSpaceCreationSuccess}
                            toggleProgressMessage={toggleProgressMessage}
                            onUpdateCodeSpaceComplete={() => {
                                setOnEditCodeSpace(undefined);
                                setOnBoardCodeSpace(undefined);
                                setIsRetryRequest(false);
                                setShowNewCodeSpaceModal(false);
                                getCodeSpacesData();
                                getCodeSpaceGroupsData();
                            }}
                        />
                    }
                    scrollableContent={true}
                    onCancel={onNewCodeSpaceModalCancel}
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
                    setCodeDeploying={() => { getCodeSpacesData(); getCodeSpaceGroupsData();}}
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
                    setCodeDeploying={() => { getCodeSpacesData(); getCodeSpaceGroupsData();}}
                    setCodeBuilding={() => { getCodeSpacesData(); getCodeSpaceGroupsData();}}
                    setIsApiCallTakeTime={setIsApiCallTakeTime}
                />
            )}
            {showDeployApprovalModal && (
                    <DeployApprovalModal
                      show={showDeployApprovalModal}
                      setShowDeployApprovalModal={setShowDeployApprovalModal}
                      codeSpaceData = {onDeployCodeSpace}
                      setCodeDeploying={() => {getCodeSpacesData(); getCodeSpaceGroupsData();}}
                      setIsApiCallTakeTime={setIsApiCallTakeTime}
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
            {showTutorialsModel && (
                <Modal
                    title={'Code Space Tutorials'}
                    hiddenTitle={false}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="80%"
                    buttonAlignment="right"
                    show={showTutorialsModel}
                    content={
                        <CodeSpaceTutorials />
                    }
                    scrollableContent={false}
                    onCancel={() => { setShowTutorialsModel(false) }}
                />
            )}
            {showAWSWarningModal && (
                <ConfirmModal
                    title={''}
                    showAcceptButton={false}
                    acceptButtonTitle="OK"
                    showCancelButton={false}
                    modalWidth={'70%'}
                    modalStyle={{
                        minWidth: 'unset',
                        width: '70%',
                      }}
                    buttonAlignment="center"
                    show={showAWSWarningModal}
                    content={AWSWarningModalContent}
                    scrollableContent={true}
                    onCancel={() => setShowAWSWarningModal(false)}
                    onAccept={() => setShowAWSWarningModal(false)}
                    showIcon = {false}
                    showCloseIcon = {true}
                />
            )}
            {showAwsFAQModal && (
                <InfoModal
                    title={'CodeSpace FAQs'}
                    modalWidth={'60%'}
                    modalStyle={{
                        maxWidth: '70%',
                    }}
                    show={showAwsFAQModal}
                    content={FAQModalContent}
                    onCancel={() => setShowAwsFAQModal(false)}
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
        </div>
    );
};
export default AllCodeSpaces;
