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
        [filteredCodeSpaces, setFilteredCodespaces] = useState();
    const History = useHistory();
    const goback = () => {
        History.goBack();
    };

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

    const onStartStopCodeSpace = (codeSpace, startSuccessCB) => {
        Tooltip.clear();
        const serverStarted = codeSpace.serverStatus === 'SERVER_STARTED';
        serverStarted ? setLoading(true) : ProgressIndicator.show();
        CodeSpaceApiClient.startStopWorkSpace(codeSpace.id, serverStarted)
            .then((res) => {
                serverStarted ? setLoading(false) : ProgressIndicator.hide();
                if (res.data.success === 'SUCCESS') {
                    Notification.show(
                        'Your Codespace for project ' +
                        codeSpace.projectDetails?.projectName +
                        ' is requested to ' +
                        (serverStarted ? 'stop' : 'start') +
                        '.',
                    );

                    startSuccessCB();

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
                    title={''}
                    hiddenTitle={true}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="80%"
                    buttonAlignment="right"
                    show={showTutorialsModel}
                    content={
                        <CodeSpaceTutorials />
                    }
                    scrollableContent={true}
                    onCancel={() => { setShowTutorialsModel(false) }}
                />
            )}
        </div>
    );
};
export default AllCodeSpaces;
