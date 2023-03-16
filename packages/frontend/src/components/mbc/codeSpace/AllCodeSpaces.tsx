import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AllCodeSpaces.scss';
import { ICodeSpaceData } from './CodeSpace';
import CodeSpaceCardItem from './codeSpaceCardItem/CodeSpaceCardItem';
import Pagination from '../pagination/Pagination';
import Modal from 'components/formElements/modal/Modal';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import { IUserInfo } from 'globals/types';
import ProgressWithMessage from 'components/progressWithMessage/ProgressWithMessage';
import { useHistory } from "react-router-dom";
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

export interface IAllCodeSpacesProps {
  user: IUserInfo;
}

const AllCodeSpaces = (props: IAllCodeSpacesProps) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [codeSpaces, setCodeSpaces] = useState<ICodeSpaceData[]>([]),
    [codeSpacesListResponse, setCodeSpacesListResponse] = useState<ICodeSpaceData[]>([]),
    [pagination, setPagination] = useState({
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      maxItemsPerPage: 15,
    }),
    [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false),
    [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false),
    [onBoardCodeSpace, setOnBoardCodeSpace] = useState<ICodeSpaceData>();

  const history = useHistory();
  const goback = () => {
    history.goBack();
  };

  const getCodeSpacesData = () => {
    setLoading(true);
    CodeSpaceApiClient.getCodeSpacesList()
      .then((res: any) => {
        setLoading(false);
        setCodeSpaces(Array.isArray(res) ? res : res.records as ICodeSpaceData[]);
        // setLastCreatedId(Array.isArray(res) ? 0 : res.totalCount);
      })
      .catch((err: Error) => {
        setLoading(false);
        Notification.show('Error in loading your code spaces - ' + err.message, 'alert');
      });
    setCodeSpacesListResponse([]);
  };

  useEffect(() => {
    getCodeSpacesData();
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = pagination.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * pagination.maxItemsPerPage;
    const modifiedData = codeSpacesListResponse.slice(
      currentPageOffset,
      pagination.maxItemsPerPage * currentPageNumberTemp,
    );
    setCodeSpaces(modifiedData);
    setPagination({ ...pagination, currentPageNumber: currentPageNumberTemp });
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = pagination.currentPageNumber;
    const currentPageOffset = pagination.currentPageNumber * pagination.maxItemsPerPage;
    currentPageNumberTemp = pagination.currentPageNumber + 1;
    const modifiedData = codeSpacesListResponse.slice(
      currentPageOffset,
      pagination.maxItemsPerPage * currentPageNumberTemp,
    );
    setCodeSpaces(modifiedData);
    setPagination({ ...pagination, currentPageNumber: currentPageNumberTemp });
  };
  const onViewByPageNum = (pageNum: number) => {
    const totalNumberOfPages = Math.ceil(codeSpacesListResponse?.length / pageNum);
    const modifiedData = codeSpacesListResponse.slice(0, pageNum);
    setCodeSpaces(modifiedData);
    setPagination({
      totalNumberOfPages,
      maxItemsPerPage: pageNum,
      currentPageNumber: 1,
    });
  };

  const onShowNewCodeSpaceModal = () => {
    setShowNewCodeSpaceModal(true);
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
    setShowNewCodeSpaceModal(false);
    setOnBoardCodeSpace(undefined);
  };

  const onDeleteSuccess = () => {
    getCodeSpacesData();
  };

  const onShowCodeSpaceOnBoard = (codeSpace: ICodeSpaceData) => {
    setOnBoardCodeSpace(codeSpace);
    setShowNewCodeSpaceModal(true);
  };

  const switchBackToCodeSpace = () => {
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
            <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>Back</button>
            <h3>Code Spaces Overview</h3>
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
                <button
                  className={codeSpaces?.length === null ? Styles.btnHide : 'btn btn-primary hide'}
                  type="button"
                  onClick={onShowNewCodeSpaceModal}
                >
                  <i className="icon mbc-icon plus" />
                  <span>Create new Code Space</span>
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
                          />
                        );
                      })}
                    </div>
                  </div>
                  {codeSpaces?.length ? (
                    <Pagination
                      totalPages={pagination.totalNumberOfPages}
                      pageNumber={pagination.currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : null}
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
              isCodeSpaceCreationSuccess={isCodeSpaceCreationSuccess}
              toggleProgressMessage={toggleProgressMessage}
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
              'Please wait as this process can take up to a minute....'
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
