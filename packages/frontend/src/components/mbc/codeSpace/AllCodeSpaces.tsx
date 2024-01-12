import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AllCodeSpaces.scss';
import { ICodeSpaceData } from './CodeSpace';
import CodeSpaceCardItem from './codeSpaceCardItem/CodeSpaceCardItem';
// import Pagination from '../pagination/Pagination';
import Modal from 'components/formElements/modal/Modal';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import { IUserInfo } from 'globals/types';
import ProgressWithMessage from 'components/progressWithMessage/ProgressWithMessage';
import { useHistory } from 'react-router-dom';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { IconGear } from 'components/icons/IconGear';
import { USER_ROLE } from 'globals/constants';
export interface IAllCodeSpacesProps {
  user: IUserInfo;
}

const AllCodeSpaces = (props: IAllCodeSpacesProps) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [codeSpaces, setCodeSpaces] = useState<ICodeSpaceData[]>([]),
    // [codeSpacesListResponse, setCodeSpacesListResponse] = useState<ICodeSpaceData[]>([]),
    // [pagination, setPagination] = useState({
    //   totalNumberOfPages: 1,
    //   currentPageNumber: 1,
    //   maxItemsPerPage: 15,
    // }),
    [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false),
    [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false),
    [onBoardCodeSpace, setOnBoardCodeSpace] = useState<ICodeSpaceData>(),
    [onEditCodeSpace, setOnEditCodeSpace] = useState<ICodeSpaceData>();
  const isCodeSpaceAdmin = props.user.roles.some((role) => role.id === USER_ROLE.CODESPACEADMIN);
  const history = useHistory();
  const goback = () => {
    history.goBack();
  };

  const getCodeSpacesData = () => {
    setLoading(true);
    CodeSpaceApiClient.getCodeSpacesList()
      .then((res: any) => {
        setLoading(false);
        setCodeSpaces(Array.isArray(res) ? res : (res.records as ICodeSpaceData[]));
        // setLastCreatedId(Array.isArray(res) ? 0 : res.totalCount);
      })
      .catch((err: Error) => {
        setLoading(false);
        Notification.show('Error in loading your code spaces - ' + err.message, 'alert');
      });
    // setCodeSpacesListResponse([]);
  };

  useEffect(() => {
    getCodeSpacesData();
  }, []);

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
    history.push('/codespace/adminSecurityConfigs');
  };

  const isCodeSpaceCreationSuccess = (status: boolean, codeSpaceData: ICodeSpaceData) => {
    if (showNewCodeSpaceModal) {
      setShowNewCodeSpaceModal(!status);
      history.push(`codespace/${codeSpaceData.workspaceId}`);
    } else {
      getCodeSpacesData();
    }
  };

  const toggleProgressMessage = (show: boolean) => {
    setIsApiCallTakeTime(show);
  };

  const onNewCodeSpaceModalCancel = () => {
    if (onEditCodeSpace) {
      getCodeSpacesData();
    }
    setShowNewCodeSpaceModal(false);
    setOnBoardCodeSpace(undefined);
    setOnEditCodeSpace(undefined);
  };

  const onDeleteSuccess = () => {
    getCodeSpacesData();
  };

  const onShowCodeSpaceOnBoard = (codeSpace: ICodeSpaceData) => {
    setOnBoardCodeSpace(codeSpace);
    setShowNewCodeSpaceModal(true);
  };

  const onCodeSpaceEdit = (codeSpace: ICodeSpaceData) => {
    setOnEditCodeSpace(codeSpace);
    setShowNewCodeSpaceModal(true);
  };

  const switchBackToCodeSpace = () => {
    setOnEditCodeSpace(undefined);
    setOnBoardCodeSpace(undefined);
    setShowNewCodeSpaceModal(false);
    setIsApiCallTakeTime(false);
    ProgressIndicator.hide();
    getCodeSpacesData();
  };

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
          <div className={classNames(Styles.listHeader)}>
            {codeSpaces?.length ? (
              <>
                <button
                  className={codeSpaces?.length === null ? Styles.btnHide : 'btn btn-icon-circle'}
                  tooltip-data="Refresh"
                  onClick={getCodeSpacesData}
                >
                  <i className="icon mbc-icon refresh" />
                </button>
              </>
            ) : null}
            {isCodeSpaceAdmin ? (
              <>
                <button
                  className={classNames('btn btn-primary', Styles.configIcon)}
                  type="button"
                  onClick={onShowSecurityConfigRequest}
                >
                  <IconGear size={'14'} />
                  <span>&nbsp;Manage Security Configs</span>
                </button>
              </>
            ) : null}
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
                      {codeSpaces?.map((codeSpace: ICodeSpaceData, index: number) => {
                        return (
                          <CodeSpaceCardItem
                            key={index}
                            userInfo={props.user}
                            codeSpace={codeSpace}
                            toggleProgressMessage={toggleProgressMessage}
                            onDeleteSuccess={onDeleteSuccess}
                            onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard}
                            onCodeSpaceEdit={onCodeSpaceEdit}
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
          modalWidth="800px"
          buttonAlignment="right"
          show={showNewCodeSpaceModal}
          content={
            <NewCodeSpace
              user={props.user}
              onBoardingCodeSpace={onBoardCodeSpace}
              onEditingCodeSpace={onEditCodeSpace}
              isCodeSpaceCreationSuccess={isCodeSpaceCreationSuccess}
              toggleProgressMessage={toggleProgressMessage}
              onUpdateCodeSpaceComplete={() => {
                setOnEditCodeSpace(undefined);
                setOnBoardCodeSpace(undefined);
                setShowNewCodeSpaceModal(false);
                getCodeSpacesData();
              }}
            />
          }
          scrollableContent={true}
          onCancel={onNewCodeSpaceModalCancel}
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
    </div>
  );
};
export default AllCodeSpaces;
