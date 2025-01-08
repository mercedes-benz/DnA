import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
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
import { Envs } from '../Utility/envs';
import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';

// export interface IAllCodeSpacesProps {
//   user: IUserInfo;
// }

const AllCodeSpaces = (props) => {
    const [loading, setLoading] = useState(true);
    const [codeSpaces, setCodeSpaces] = useState([]),
        // [codeSpacesListResponse, setCodeSpacesListResponse] = useState([]),
        // [pagination, setPagination] = useState({
        //   totalNumberOfPages: 1,
        //   currentPageNumber: 1,
        //   maxItemsPerPage: 15,
        // }),
        [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState(false),
        [showDeployCodeSpaceModal, setShowDeployCodeSpaceModal] = useState(false),
        [isRetryRequest, setIsRetryRequest] = useState(false),
        [isApiCallTakeTime, setIsApiCallTakeTime] = useState(false),
        [onBoardCodeSpace, setOnBoardCodeSpace] = useState(),
        [onEditCodeSpace, setOnEditCodeSpace] = useState(),
        [onDeployCodeSpace, setOnDeployCodeSpace] = useState(),
        [showTutorialsModel, setShowTutorialsModel] = useState(false),
        [codeSpaceSearchTerm , setCodeSpaceSearchTerm] = useState(''),
        [filteredCodeSpaces, setFilteredCodespaces] = useState(),
        [showAwsFAQModal, setShowAwsFAQModal] = useState(false);
    const History = useHistory();
    const goback = () => {
        History.goBack();
    };
    const [showAWSWarningModal, setShowAWSWarningModal] = useState(false);

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

    useEffect(() => {
        setShowAWSWarningModal(Envs.SHOW_AWS_MIGRATION_WARNING);
        getCodeSpacesData();
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
        }
    };

    const toggleProgressMessage = (show) => {
        setIsApiCallTakeTime(show);
    };

    const onNewCodeSpaceModalCancel = () => {
        if (onEditCodeSpace) {
            getCodeSpacesData();
        }
        setShowNewCodeSpaceModal(false);
        setOnBoardCodeSpace(undefined);
        setIsRetryRequest(false);
        setOnEditCodeSpace(undefined);
    };

    const onDeleteSuccess = () => {
        getCodeSpacesData();
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
    };

    const navigateSecurityConfig = () => {
        const projectDetails = onDeployCodeSpace?.projectDetails;
        if (projectDetails?.publishedSecuirtyConfig) {
            window.open(`${window.location.pathname}#/codespaces/codespace/publishedSecurityconfig/${onDeployCodeSpace?.id}?name=${projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}`, '_blank');
            return;
        }
        window.open(`${window.location.pathname}#/codespaces/codespace/securityconfig/${onDeployCodeSpace.id}?name=${projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}`, '_blank');
    }

    const AWSWarningModalContent = (
        <div className={Styles.modalContentWrapper}>
            <div className={Styles.modalMainTitle}><i className="icon mbc-icon alert circle" />Heads Up! Codespaces is Moving to DyPCaaS AWS <i className="icon mbc-icon alert circle" /></div>
            <p>We&apos;re improving Codespaces! Here&apos;s what you need to know:</p>
            <div className={Styles.modalTitle}>DyPCaaS On-Prem is Retiring</div>
            <p>On October 31st, 2025, DyPCaaS On-Prem will no longer be available (details here: <a href={Envs.AWS_MOVE_DOC_URL} target='_blank' rel='noopener noreferrer'>DyP CaaS Moves to AWS</a>).</p>
            <div className={Styles.modalTitle}>Moving to DyPCaaS AWS</div>
            <p>To keep things running smoothly, we&apos;ll be migrating everything to DyPCaaS AWS. This means better performance and more features for you!</p>
            <div className={Styles.modalTitle}>What this means for you (if you use Codespaces):</div>
            <p>
                <ul>
                    <li><b>New Workspaces:</b> All new Codespaces will automatically be created on DyPCaaS AWS.</li>
                    <li><b>Existing Workspaces:</b> You&apos;ll need to migrate your current Codespaces to DyPCaaS AWS before January 10th, 2025. We&apos;ve made it easy with a <b>self-service migration process</b> that starts when you launch your workspace. There&apos;s also a helpful <a href={`#/codespaces/tutorials/awsMigration`} target="_blank" rel="noreferrer">video guide</a> to walk you through it.</li>
                </ul>
            </p>
            <div className={Styles.modalTitle}>Migrating your Existing Codespace:</div>
            <p>
                <ol>
                    <li><b>Don&apos;t forget your changes!</b> Before migrating, commit all changes (including untracked files) to your Git repository.</li>
                    <li><b>Easy Migration:</b> Use our self-service migration flow to move your workspace to DyPCaaS AWS.</li>
                    <li><b>Old Workspace Access:</b> You can still access your old workspace on DyPCaaS On-Prem (from the context menu) until January 10th, 2025.</li>
                </ol>
            </p>
            <div className={Styles.modalTitle}>Need Assistance?:</div>
            <p>Please refer to the AWS migration FAQs on our landing page. You can also join our <a href={Envs.CODESPACE_TEAMS_LINK} target='_blank' rel='noopener noreferrer'>Teams channel</a> or <a href={Envs.CODESPACE_MATTERMOST_LINK} target='_blank' rel='noopener noreferrer'>Mattermost channel</a> for help or to discuss any concerns.</p>
            <p><strong>Note:</strong> Upon initiating the migration, only your workspace will be migrated. Deployed applications will be migrated to AWS based on the support request. If there were no prior deployments before the migration, any new deployments will automatically be directed to AWS.</p>
        </div>
    );

    const FAQModalContent = (
        <div className={Styles.modalFAQContentWrapper}>
            <div>
                <ol>
                    <li>
                        <div>I am not able to see my code post migrating to AWS</div>
                        <div className={classNames(Styles.info)}>
                            This situation arises if the pat token that you have used to create the codespace has expired. Please follow the below steps :
                            <ul>
                                <br />
                                <li>
                                    Run the following commands in your terminal for cloning code manually
                                    <ol>
                                        <li><span className={classNames(Styles.list)}>mkdir -p /home/coder/app</span></li>
                                        <li><span className={classNames(Styles.list)}>echo &ldquo;Cloning the workspace&ldquo;</span></li>
                                        <li><span className={classNames(Styles.list)}>git config --global credential.helper cache</span></li>
                                        <li><span className={classNames(Styles.list)}>git config --global user.email &ldquo;$SHORTID&ldquo;</span></li>
                                        <li><span className={classNames(Styles.list)}>git config --global user.name &ldquo;$SHORTID&ldquo;</span></li>
                                        <li>
                                            <span className={classNames(Styles.list)}>git clone https://$GITHUB_TOKEN@$GITHUBREPO_URL /home/coder/app</span>
                                            <br />(eg: git clone https://ghp_xxxx@{(Envs.CODE_SPACE_GIT_PAT_APP_URL).split('https://')[1]}org_name/repo_name.git /home/coder/app)
                                            <br />You can find your org name and repo name by using the go to code repo option in the context menu.
                                            <br />If the cloning is not happening with the current token then generate a new token and try again.

                                        </li>
                                    </ol>
                                </li>
                                <br />
                                <li>
                                    Once your code is cloned, run the following commands in terminal to copy .bashrc
                                    <ol>
                                        <li><span className={classNames(Styles.list)}>cp /tmp/.bashrc /home/coder/</span></li>
                                        <li><span className={classNames(Styles.list)}>chmod +x /home/coder/.bashrc</span></li>
                                    </ol>
                                </li>
                                <br />
                                <li>
                                    Please execute the following commands in the given order in your terminal to install the softwares
                                    <ol>
                                        <li><span className={classNames(Styles.list)}>TEMP_DIR=&ldquo;/tmp/.codespaces/DO_NOT_DELETE_MODIFY/&ldquo;</span></li>
                                        <li><span className={classNames(Styles.list)}>mkdir -pv $TEMP_DIR</span></li>
                                        <li><span className={classNames(Styles.list)}>cp /home/coder/app/.codespaces/DO_NOT_DELETE_MODIFY/pkg-install.sh $TEMP_DIR</span></li>
                                        <li><span className={classNames(Styles.list)}>cd $TEMP_DIR</span></li>
                                        <li><span className={classNames(Styles.list)}>chmod +x pkg-install.sh</span></li>
                                        <li><span className={classNames(Styles.list)}>./pkg-install.sh</span></li>
                                    </ol>
                                </li>
                                <br />
                                <li>
                                    If you have a <span className={classNames(Styles.warning)}>Python FastAPI</span> workspace please run the following additional commands
                                    <ol>
                                        <li><span className={classNames(Styles.list)}>echo &ldquo;Installing Poetry...&ldquo;</span></li>
                                        <li><span className={classNames(Styles.list)}>curl -sSL https://install.python-poetry.org | python3 -</span></li>
                                    </ol>
                                </li>
                            </ul>
                        </div>
                    </li>
                </ol>
            </div>
        </div>
    );

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
                                onClick={getCodeSpacesData}
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
                                tooltip-data="AWS migration FAQs"
                                onClick={() => { setShowAwsFAQModal(Envs.SHOW_AWS_MIGRATION_WARNING) }}
                            >
                                <i className={classNames('icon mbc-icon alert circle')} />
                                <span>AWS Migration FAQ&apos;s</span>
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
                                    <div className={Styles.allCodeSpacesContent}>
                                        <div className={classNames('cardSolutions', Styles.allCodeSpacesCardviewContent)}>
                                            <div className={Styles.newCodeSpaceCard} onClick={onShowNewCodeSpaceModal}>
                                                <div className={Styles.addicon}> &nbsp; </div>
                                                <label className={Styles.addlabel}>Create new Code Space</label>
                                            </div>
                                            {filteredCodeSpaces?.filter((codespace) => codespace?.projectDetails?.projectOwner?.id === props.user.id)?.map((codeSpace, index) => {
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
                                                        onStartStopCodeSpace={onStartStopCodeSpace}
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
                                                        onStartStopCodeSpace={onStartStopCodeSpace}
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
                    enableSecureWithIAM={
                        onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
                        onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
                        onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
                        onDeployCodeSpace?.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven'
                    }
                    setShowCodeDeployModal={(isVisible) => setShowDeployCodeSpaceModal(isVisible)}
                    setCodeDeploying={() => getCodeSpacesData()}
                    setIsApiCallTakeTime={setIsApiCallTakeTime}
                    navigateSecurityConfig={navigateSecurityConfig}
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
                    title={'AWS migration FAQs'}
                    modalWidth={'60%'}
                    modalStyle={{
                        maxWidth: '70%',
                    }}
                    show={showAwsFAQModal}
                    content={FAQModalContent}
                    onCancel={() => setShowAwsFAQModal(false)}
                />
            )}
        </div>
    );
};
export default AllCodeSpaces;
