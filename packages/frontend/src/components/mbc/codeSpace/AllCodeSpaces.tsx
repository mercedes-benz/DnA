import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './AllCodeSpaces.scss';
import { ICodeSpaceData } from './CodeSpace';
import CodeSpaceCardItem from './codeSpaceCardItem/CodeSpaceCardItem';
import Pagination from '../pagination/Pagination';
import Modal from '../../../components/formElements/modal/Modal';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import { IUserInfo } from '../../../globals/types';
import ProgressWithMessage from '../../../components/progressWithMessage/ProgressWithMessage';
import { history } from '../../../router/History';
// import { ApiClient } from '../../../services/ApiClient';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

export interface IAllCodeSpacesProps {
  user: IUserInfo;
}

const AllCodeSpaces = (props: IAllCodeSpacesProps) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [lastCreatedId, setLastCreatedId] = useState<number>(0);
  // const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
  //   url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //   running: false
  // });
  const [codeSpaces, setCodeSpaces] = useState<ICodeSpaceData[]>([]),
    [codeSpacesListResponse, setCodeSpacesListResponse] = useState<ICodeSpaceData[]>([]),
    [pagination, setPagination] = useState({
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      maxItemsPerPage: 15,
    }),
    [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false),
    [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);

  const formatCodeSpaceData = (records: any[]) => {
    return records.map((record: any) => {
      return {
        id: record.id,
        name: record.name,
        recipe:
          record.recipeId !== 'default'
            ? `Microservice using Spring Boot (${record.operatingSystem}, ${record.ramSize}${record.ramMetrics} RAM, ${record.cpuCapacity}CPU)`
            : 'Default',
        environment: record.cloudServiceProvider,
        deployed: record.status === 'DEPLOYED',
        deployedUrl: record.deploymentUrl,
        createdDate: record.intiatedOn,
        lastDeployedDate: record.lastDeployedOn,
        url: record.workspaceUrl,
        running: !!record.intiatedOn,
        status: record.status,
      } as ICodeSpaceData;
    });
  };

  const getCodeSpacesData = () => {
    setLoading(true);
    CodeSpaceApiClient.getCodeSpacesList().then((res: any) => {
      setLoading(false);
      setCodeSpaces(Array.isArray(res) ? res : formatCodeSpaceData(res.records) as ICodeSpaceData[]);
      setLastCreatedId(Array.isArray(res) ? 0 : res.totalCount);
    }).catch((err: Error) => {
      setLoading(false);
      Notification.show("Error in loading your code spaces - " + err.message, 'alert');
    });
    setCodeSpacesListResponse([]);
  };
  
  useEffect(() => {
    // ApiClient.getCodeSpace().then((res: any) => {
    //   setLoading(false);
    //   const codeSpaceRunning = (res.success === 'true');
    //   setCodeSpaceData({
    //     ...codeSpaceData,
    //     running: codeSpaceRunning
    //   });
    //   // setShowNewCodeSpaceModal(!codeSpaceRunning);
    //   codeSpaceRunning && setCodeSpaces([
    //     {
    //       id: 'ws001',
    //       name: 'DemoSpringBootMS',
    //       recipe: 'Microservice using Spring Boot (Debian 11 OS, 2GB RAM, 1CPU)',
    //       environment: 'DHC CaaS',
    //       deployed: true,
    //       createdDate: '2022-05-17T12:15:36.606+00:00',
    //       lastDeployedDate: '2022-05-20T12:15:36.606+00:00',
    //       url: 'sample',
    //       running: true,
    //     }
    //   ]);
    // }).catch((err: Error) => {
    //   Notification.show("Error in validating code space - " + err.message, 'alert');
    // });

    getCodeSpacesData();
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = pagination.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * pagination.maxItemsPerPage;
    const modifiedData = codeSpacesListResponse.slice(currentPageOffset, pagination.maxItemsPerPage * currentPageNumberTemp);
    setCodeSpaces(modifiedData);
    setPagination({ ...pagination, currentPageNumber: currentPageNumberTemp });
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = pagination.currentPageNumber;
    const currentPageOffset = pagination.currentPageNumber * pagination.maxItemsPerPage;
    currentPageNumberTemp = pagination.currentPageNumber + 1;
    const modifiedData = codeSpacesListResponse.slice(currentPageOffset, pagination.maxItemsPerPage * currentPageNumberTemp);
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
  }

  const isCodeSpaceCreationSuccess = (status: boolean, codeSpaceData: ICodeSpaceData) => {
    if (showNewCodeSpaceModal) {
      setShowNewCodeSpaceModal(!status);
      history.push(`codespace/${codeSpaceData.name}`);
    } else {
      getCodeSpacesData();
    }
  }

  const toggleProgressMessage = (show: boolean) => {
    setIsApiCallTakeTime(show);
  }

  const onNewCodeSpaceModalCancel = () => {
    setShowNewCodeSpaceModal(false);
  }

  const onDeleteSuccess = () => {
    getCodeSpacesData();
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
          <h3>Code Spaces Overview</h3>
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
                            codeSpace={codeSpace}
                            toggleProgressMessage={toggleProgressMessage}
                            onDeleteSuccess={onDeleteSuccess}
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
              lastCreatedId={lastCreatedId}
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
              <button className="btn btn-text back arrow" onClick={switchBackToCodeSpace}>Back to Code Spaces</button>
            </>
          }
        />
      )}
    </div>
  );
};
export default AllCodeSpaces;
