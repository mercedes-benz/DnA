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
    const [showSundownWarningModal, setShowSundownWarningModal] = useState(false);
    const [showMigrationWarningModal, setShowMigrationWarningModal] = useState(false);
    const [pendingStartCodeSpace, setPendingStartCodeSpace] = useState(null);
    const [pendingStartParams, setPendingStartParams] = useState(null);
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
        setShowSundownWarningModal(true);
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
        
        if (!serverStarted && codeSpace.isWorkspaceMigratedToGHE) {
            setPendingStartCodeSpace(codeSpace);
            setPendingStartParams({ startSuccessCB, env, manual });
            setShowMigrationWarningModal(true);
            return;
        }
        
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
    
    const handleMigrationWarningAccept = () => {
        setShowMigrationWarningModal(false);
        
        if (pendingStartCodeSpace && pendingStartParams) {
            const { startSuccessCB, env, manual } = pendingStartParams;
            ProgressIndicator.show();
            
            CodeSpaceApiClient.startStopWorkSpace(pendingStartCodeSpace.id, false, env, manual)
                .then((res) => {
                    ProgressIndicator.hide();
                    if (res.data.success === 'SUCCESS') {
                        Notification.show(
                            'Your Codespace for project ' +
                            pendingStartCodeSpace.projectDetails?.projectName +
                            ' is requested to start.',
                        );
                        !manual && startSuccessCB();
                    } else {
                        Notification.show(
                            'Error in starting your code spaces. Please try again later.',
                            'alert',
                        );
                    }
                })
                .catch((err) => {
                    ProgressIndicator.hide();
                    Notification.show(
                        'Error in starting your code spaces - ' + err.message,
                        'alert',
                    );
                }).finally(() => {
                    Tooltip.defaultSetup();
                    setPendingStartCodeSpace(null);
                    setPendingStartParams(null);
                });
        }
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
                  <span>1. My PAT token is invalid</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </div>
              </label>
              <div className="expansion-panel-content">
                <div className={classNames(Styles.info)}>
                  This issue usually occurs when the Personal Access Token (PAT) you generated is not authorized with DnA CodeSpaces.
                  <br /><br />
                  To resolve this issue, please generate and authorize a new PAT token by following the steps below.
                  <br /><br />
                  <strong>Steps to Generate a Personal Access Token</strong>
                  <ol>
                    <li>Log in to <a href={Envs.CODE_SPACE_GHE_PAT_APP_URL} target="_blank" rel="noopener noreferrer">{Envs.CODE_SPACE_GHE_PAT_APP_URL}</a>.</li>
                    <li>Click on your Account Icon in the top-right corner and select <strong>Settings</strong>.</li>
                    <li>Navigate to <strong>Developer Settings</strong>.</li>
                    <li>Select <strong>Personal access tokens</strong> → <strong>Tokens (classic)</strong>.</li>
                    <li>Click <strong>Generate new token</strong> → <strong>Generate new token (classic)</strong>.</li>
                    <li>Provide the required expiry period.</li>
                    <li>Select all required scopes.</li>
                    <li>Click <strong>Generate Token</strong>.</li>
                    <li>Copy and securely save the generated token for future use (you will not be able to see it again).</li>
                  </ol>
                  <br />
                  <strong>Steps to Configure SSO for Your Personal Access Token</strong>
                  <ol>
                    <li>After generating the token, click <strong>Configure SSO</strong> next to the token.</li>
                    <li>Select <strong>DNA-CodeSpaces</strong> as the authorizer.</li>
                    <li>Authorize the token.</li>
                  </ol>
                  <br />
                  <strong>Final Step</strong>
                  <ul>
                    <li>Use the authorized PAT token when creating your CodeSpace.</li>
                  </ul>
                </div>
              </div>
            </div>

            <div className={classNames('expansion-panel')}>
              <span className="animation-wrapper"></span>
              <input type="checkbox" className="ff-only" id="faq-2" />
              <label className={classNames('expansion-panel-label', Styles.faqHeader)} htmlFor="faq-2">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                  <span>2. I am unable to pull or push code after the GHE migration</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </div>
              </label>
              <div className="expansion-panel-content">
                <div className={classNames(Styles.info)}>
                  After the migration from GHES to GHEC, your local repository may still be pointing to the old remote URL, which can prevent you from pulling or pushing code.
                  <br /><br />
                  You need to update the remote origin URL in your local repository.
                  <br /><br />
                  <strong>Steps to Update the Remote Repository URL</strong>
                  <ol>
                    <li>
                      Check the current remote URL:
                      <br />
                      <span className={classNames(Styles.list)}>git remote -v</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}>If the migration has not been updated locally, this command may show the old remote (git.i).</span>
                    </li>
                    <li>
                      Update the remote origin to the new repository URL:
                      <br />
                      <span className={classNames(Styles.list)}>git remote set-url origin &lt;new_repo_url&gt;</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}>Example:</span>
                      <br />
                      <span className={classNames(Styles.list)}>git remote set-url origin https://mercedes-benz.ghe.com/&lt;org&gt;/&lt;repo&gt;.git</span>
                    </li>
                    <li>
                      Verify the updated remote:
                      <br />
                      <span className={classNames(Styles.list)}>git remote -v</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}>You should now see the new GHE repository URL.</span>
                    </li>
                    <li>
                      Pull the latest changes:
                      <br />
                      <span className={classNames(Styles.list)}>git pull</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}>When prompted for credentials:</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}><strong>Username:</strong> Your official Mercedes-Benz email ID</span>
                      <br />
                      <span className={classNames(Styles.listInfo)}><strong>Password:</strong> Your GHE Personal Access Token (PAT)</span>
                    </li>
                  </ol>
                  <br />
                  <strong>Alternative Method</strong>
                  <ul>
                    <li>
                      You can also configure the remote URL with your SSO-authorized PAT directly:
                      <br />
                      <span className={classNames(Styles.list)}>git remote set-url origin https://&lt;DnA-Codespaces SSO authorized classic PAT from GHE&gt;@&lt;repo_url&gt;</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
      
            <div className={classNames('expansion-panel')}>
              <span className="animation-wrapper"></span>
              <input type="checkbox" className="ff-only" id="faq-3" />
              <label className={classNames('expansion-panel-label', Styles.faqHeader)} htmlFor="faq-3">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                  <span>3. I am not able to see my code post migrating</span>
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
                                                <li><span className={classNames(Styles.list)}>git config --global user.email &lt;EMAILID&gt;</span></li>
                                                <li><span className={classNames(Styles.list)}>git config --global user.name &lt;SHORTID&gt;</span></li>
                        <li>
                          <span className={classNames(Styles.list)}>
                                                        git clone https://&lt;GITHUB_TOKEN&gt;@&lt;GITHUBREPO_URL&gt; /home/coder/app
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
                  <span>4. I am getting a WebSocket error: “The workbench failed to connect to the server”</span>
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
                  <span>5. CodeSpace stopped and keeps reloading while working</span>
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

    const sundownWarningContent = (
        <div className={Styles.sundownWarning}>
            <p>
                Your repositories under the <a href={Envs.CODE_SPACE_GHEC_ORG_URL} target="_blank" rel="noopener noreferrer">DNA-CodeSpaces</a> organization have been successfully migrated from GHES to GitHub Cloud (GHEC). 🎉 All repositories can now be accessed using the following link{' '}
                <a
                    href={Envs.CODE_SPACE_GHEC_ORG_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ whiteSpace: 'nowrap' }}
                >
                    {Envs.CODE_SPACE_GHEC_ORG_URL}
                </a>
                .
                <br /><br />
            </p>
            <p>
                As part of the migration, please create a Personal Access Token (PAT) from{' '}
                <a
                    href={Envs.CODE_SPACE_GHE_PAT_SETTINGS_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ whiteSpace: 'nowrap' }}
                >
                    {Envs.CODE_SPACE_GHE_PAT_SETTINGS_URL}
                </a>
                {' '}by selecting &quot;Generate new token (classic)&quot;. Under Scopes, ensure the token has at least <strong>repo</strong> access. After creating the token, make sure to authorize it for SSO under the <strong>DnA-Codespaces</strong> organization. <br /><br />Also, update your Git remote URL to point to the new host using<br /><code>git remote set-url origin {Envs.CODE_SPACE_GHEC_ORG_URL}/&lt;REPO_NAME&gt;.git</code><br /><br />For any queries, please refer to the Codespaces FAQs or contact us via the <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">DnA Codespaces Teams channel</a>.
            </p>
        </div>
    );

    useEffect(() => {
        // if (selectedCodeSpaceGroup) {
        //   const updatedGroup = codeSpaceGroups.find(
        //     (group) => group.id === selectedCodeSpaceGroup.id
        //   );
        //   if (updatedGroup && updatedGroup !== selectedCodeSpaceGroup) {
        //     setSelectedCodeSpaceGroup(updatedGroup);
        //   }
        // }
        setSelectedCodeSpaceGroup(JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS)));
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
            {showSundownWarningModal && (
                <ConfirmModal
                    title={''}
                    acceptButtonTitle="OK"
                    showAcceptButton={true}
                    showCancelButton={false}
                    show={showSundownWarningModal}
                    content={sundownWarningContent}
                    onCancel={() => setShowSundownWarningModal(false)}
                    onAccept={() => setShowSundownWarningModal(false)}
                    
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
            {showMigrationWarningModal && (
                <ConfirmModal
                    title={'Important: Workspace Migrated to GitHub Cloud'}
                    acceptButtonTitle="OK"
                    showAcceptButton={true}
                    showCancelButton={false}
                    modalWidth="50%"
                    show={showMigrationWarningModal}
                    content={
                        <div className={Styles.modalContentWrapper}>
                            <p>
                                This workspace has been migrated to GitHub Enterprise Cloud (GHEC).
                            </p>
                            <p>
                                <strong>Important:</strong> You may experience issues with pulling or pushing code if your local repository is still pointing to the old remote URL.
                            </p>
                            <p>
                                Please refer to <strong>FAQ #2: &quot;I am unable to pull or push code after the GHE migration&quot;</strong> in the{' '}
                                <a 
                                    href="#" 
                                    onClick={(e) => {
                                        e.preventDefault();
                                        setShowMigrationWarningModal(false);
                                        setShowAwsFAQModal(true);
                                    }}
                                    style={{ textDecoration: 'underline', cursor: 'pointer' }}
                                >
                                    CodeSpace FAQs
                                </a>
                                {' '}for detailed instructions on updating your remote repository URL.
                            </p>
                        </div>
                    }
                    onCancel={() => {
                        setShowMigrationWarningModal(false);
                        setPendingStartCodeSpace(null);
                        setPendingStartParams(null);
                    }}
                    onAccept={handleMigrationWarningAccept}
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
