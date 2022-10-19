import React, { useState, useEffect } from 'react';
import Styles from './PipelineSubList.scss';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import {
  IPipelineProjectDetail,
  IPipelineProjectDag,
  IPipelineProjectDagsCollabarator,
  IPipelineProjectDagData,
} from 'globals/types';
import { history } from '../../../../router/History';
import { PipelineApiClient } from '../../../../services/PipelineApiClient';
import Modal from 'components/formElements/modal/Modal';

// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import classNames from 'classnames';
import { Envs } from 'globals/Envs';
// import { TagRowItem } from '../../admin/taghandling/tagrowitem/TagRowItem';
// import { isEmpty, iteratee, sortBy } from 'lodash';

export interface IPipelineProjectProps {
  listOfProject: IPipelineProjectDetail[];
  getRefreshedDagPermission: (projectId: string, dagIndex: number) => void;
  getProjectSorted: (prjIdSortVal: IPipelineProjectDetail[]) => void;
}

const PipelineSubList = (props: IPipelineProjectProps) => {
  const [collPermissionModel, setCollPermissionModel] = useState<boolean>(false);
  const [dagCollExist, setDagCollExist] = useState<IPipelineProjectDagsCollabarator[]>([]);
  const [projectIdTemp, setProjectIdTemp] = useState<string>('');
  const [dagNameTemp, setDagNameTemp] = useState<string>();
  const [dagIndexTemp, setDagIndexTemp] = useState<number>();
  const [currentSortOrder, setCurrentSortOrder] = useState<string>('desc');
  const [nextSortOrder, setNextSortOrder] = useState<string>('asc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState<string>('projectId');

  const onPermissionEdit = (collUserId: string, index: number) => {
    return () => {
      const collItem = dagCollExist.map((item: IPipelineProjectDagsCollabarator, itemIndex: number) => {
        if (item.username === collUserId) {
          if (item.permissions?.includes('can_dag_edit')) {
            item.permissions.splice(item.permissions.indexOf('can_dag_edit'), 1);
          } else {
            item.permissions.push('can_dag_edit');
          }
        }
        return item;
      });
      setDagCollExist(collItem);
    };
  };
  const onEditProject = (prjId: string) => {
    return () => {
      history.push('/createnewpipeline/' + prjId + '/true');
    };
  };
  const editCodeCurrentDag = (dagId: string) => {
    return () => {
      history.push('/editcode/' + dagId);
    };
  };
  const collPermissionModelClose = () => {
    setCollPermissionModel(false);
  };
  const viewCodeCurrentDag = (dagId: string) => {
    return () => {
      history.push('/editcode/' + dagId);
    };
  };
  const getDagPermissionRefresh = (dagId: string, projectId: string, dagIndex: number) => {
    return () => {
      setDagNameTemp(dagId);
      setProjectIdTemp(projectId);
      setDagIndexTemp(dagIndex);
      ProgressIndicator.show();
      PipelineApiClient.getDagPermissions(dagId, projectId)
        .then((response: IPipelineProjectDagData) => {
          if (response.data.collaborators === null) {
            props.getRefreshedDagPermission(projectId, dagIndex);
            Notification.show('Dag Permission Updated!');
          } else {
            setDagCollExist(response.data.collaborators);
            setCollPermissionModel(true);
          }
          ProgressIndicator.hide();
        })
        .catch((err: Error) => {
          Notification.show(err.message, 'alert');
          ProgressIndicator.hide();
        });
    };
  };

  // Sorting table data
  const sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: IPipelineProjectDetail[] = [];
      if (columnName === 'projectId') {
        if (sortOrder === 'asc') {
          sortedArray = props.listOfProject.sort(function (item1: any, item2: any) {
            return parseInt(item1.projectId.replace('P', '')) - parseInt(item2.projectId.replace('P', ''));
          });
        } else if (sortOrder === 'desc') {
          sortedArray = props.listOfProject.sort(function (item1: any, item2: any) {
            return parseInt(item2.projectId.replace('P', '')) - parseInt(item1.projectId.replace('P', ''));
          });
        }
      } else {
        sortedArray = props.listOfProject.sort((a, b) => {
          const nameA = a[columnName].toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          const nameB = b[columnName].toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          if (nameA < nameB) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA > nameB) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      }
      setNextSortOrder(sortOrder == 'asc' ? 'desc' : 'asc');
      setCurrentSortOrder(sortOrder);
      setCurrentColumnToSort(columnName);
      props.getProjectSorted(sortedArray);
    };
  };

  const updateDagPermission = (dagId: string, projectId: string) => {
    const data = {
      data: {
        collaborators: dagCollExist,
        dagName: dagId,
      },
    };
    return () => {
      ProgressIndicator.show();
      PipelineApiClient.updateDagPermissions(dagId, projectId, data)
        .then((response: IPipelineProjectDag) => {
          setCollPermissionModel(false);
          props.getRefreshedDagPermission(projectId, dagIndexTemp);
          Notification.show('Dag Permission Updated!');
          ProgressIndicator.hide();
        })
        .catch((err: Error) => {
          Notification.show(err.message, 'alert');
          ProgressIndicator.hide();
        });
    };
  };
  useEffect(() => {
    Tooltip.defaultSetup();
    ExpansionPanel.defaultSetup();
  }, []);

  const dagCollUsersListContent = (
    <div className={Styles.dagCollUsersListParent}>
      <div className={Styles.dagCollUsersListParent}>
        <div className={Styles.dagCollUsersList}>
          <div className={Styles.collUserTitle}>
            <div className={Styles.collUserTitleCol}>User ID</div>
            <div className={Styles.collUserTitleCol}>Name</div>
            <div className={Styles.collUserTitleCol}>Permission</div>
            <div className={Styles.collUserTitleCol}></div>
          </div>
          <div className={Styles.collUserContent}>
            {dagCollExist === null
              ? ''
              : dagCollExist.map((item: IPipelineProjectDagsCollabarator, collIndex: any) => {
                  return (
                    <div key={collIndex} className={Styles.collUserContentRow}>
                      <div className={Styles.collUserTitleCol}>{item.username}</div>
                      <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                      <div className={Styles.collUserTitleCol}>
                        <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                          <label className={'checkbox ' + Styles.checkBoxDisable}>
                            <span className="wrapper">
                              <input type="checkbox" className="ff-only" value="can_dag_read" checked={true} />
                            </span>
                            <span className="label">Read</span>
                          </label>
                        </div>
                        &nbsp;&nbsp;&nbsp;
                        <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                          <label className={'checkbox ' + Styles.writeAccess}>
                            <span className="wrapper">
                              <input
                                type="checkbox"
                                className="ff-only"
                                value="can_dag_edit"
                                checked={item.permissions !== null ? item.permissions?.includes('can_dag_edit') : false}
                                onClick={onPermissionEdit(item.username, collIndex)}
                              />
                            </span>
                            <span className="label">Write</span>
                          </label>
                        </div>
                      </div>
                    </div>
                  );
                })}
          </div>
        </div>
      </div>
      <div className={Styles.addBtn}>
        <button
          onClick={updateDagPermission(dagNameTemp, projectIdTemp)}
          className={Styles.actionBtn + ' btn btn-tertiary'}
          type="button"
        >
          <span>Update</span>
        </button>
      </div>
    </div>
  );
  return (
    <React.Fragment>
      <div className={classNames('expanstion-table', Styles.airflowSubscriptionList)}>
        <div className={Styles.dagGrp}>
          <div className={Styles.dagGrpList}>
            <div className={Styles.dagGrpListItem}>
              <div className={Styles.dagCaption}>
                <div className={Styles.dagTile}>
                  <div className={Styles.dagTitleCol}>
                    <label
                      className={
                        'sortable-column-header ' + (currentColumnToSort === 'projectId' ? currentSortOrder : '')
                      }
                      onClick={sortByColumn('projectId', nextSortOrder)}
                    >
                      <i className="icon sort" />
                      Project Id
                    </label>
                  </div>
                  <div className={Styles.dagTitleCol}>
                    <label
                      className={
                        'sortable-column-header ' + (currentColumnToSort === 'projectName' ? currentSortOrder : '')
                      }
                      onClick={sortByColumn('projectName', nextSortOrder)}
                    >
                      <i className="icon sort" />
                      Project Name
                    </label>
                  </div>
                  <div className={Styles.dagTitleCol}>
                    <label
                      className={
                        'sortable-column-header ' + (currentColumnToSort === 'isOwner' ? currentSortOrder : '')
                      }
                      onClick={sortByColumn('isOwner', nextSortOrder)}
                    >
                      <i className="icon sort" />
                      Permission
                    </label>
                  </div>
                  <div className={Styles.dagTitleCol}>Action</div>
                </div>
              </div>
              {props.listOfProject.map((item: IPipelineProjectDetail, index: number) => {
                return (
                  <div
                    key={index}
                    className={'expansion-panel-group airflowexpansionPanel ' + Styles.dagGrpListItemPanel}
                  >
                    <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                      <span className="animation-wrapper"></span>
                      <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                      <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                        <div className={Styles.dagTile}>
                          <div className={Styles.dagTitleCol}>{item.projectId}</div>
                          <div className={Styles.dagTitleCol}>{item.projectName}</div>
                          <div className={Styles.dagTitleCol}>{item.isOwner ? 'Owner' : 'Collaborator'}</div>
                          <div className={Styles.dagTitleCol}></div>
                        </div>
                        <i tooltip-data="Expand" className="icon down-up-flip"></i>
                      </label>
                      <div className="expansion-panel-content">
                        <div className={Styles.dagCollContent}>
                          <div className={Styles.projectList}>
                            <div className={Styles.dagTile + ' ' + Styles.dagTileCption}>
                              <div className={Styles.dagTitleCol}>DAG Id</div>
                              <div className={Styles.dagTitleCol}>Your Permission</div>
                              <div className={Styles.dagTitleCol}>Action</div>
                            </div>
                            <div className={Styles.projectDagList}>
                              {item.dags.map((dagItem: IPipelineProjectDag, dagIndex: number) => {
                                return (
                                  <div key={dagIndex} className={Styles.dagTile}>
                                    <div className={Styles.dagTitleCol}>{dagItem.dagName}</div>
                                    <div className={Styles.dagTitleCol}>
                                      {dagItem.permissions === null ? (
                                        <span className={Styles.noPermission}>
                                          Not Granted
                                          <i
                                            className={Styles.noPermissionInfo + ' icon mbc-icon info'}
                                            tooltip-data="Permission mapping failed. Please click try again."
                                          />
                                        </span>
                                      ) : (
                                        'Read' + (dagItem.permissions?.includes('can_dag_edit') ? ' / Edit  ' : '')
                                      )}
                                    </div>

                                    <div className={Styles.dagTitleCol}>
                                      <div className={Styles.actionBtnGrp}>
                                        {dagItem.permissions === null ? (
                                          <span className={Styles.noPermission}>
                                            {item.isOwner ? (
                                              <div
                                                className={Styles.tryAgain}
                                                onClick={getDagPermissionRefresh(
                                                  dagItem.dagName,
                                                  item.projectId,
                                                  dagIndex,
                                                )}
                                              >
                                                {' '}
                                                <i className="icon mbc-icon refresh" />
                                                Try Again
                                              </div>
                                            ) : (
                                              'Please Contact Admin.'
                                            )}
                                          </span>
                                        ) : (
                                          <React.Fragment>
                                            {dagItem.permissions?.includes('can_dag_edit') ? (
                                              <button
                                                onClick={editCodeCurrentDag(dagItem.dagName)}
                                                className={Styles.actionBtn + ' btn btn-primary'}
                                                type="button"
                                              >
                                                <i className="icon mbc-icon edit" />
                                                <span>Edit Code </span>
                                              </button>
                                            ) : (
                                              <button
                                                onClick={viewCodeCurrentDag(dagItem.dagName)}
                                                className={Styles.actionBtn + ' btn btn-primary'}
                                                type="button"
                                              >
                                                <i className="icon mbc-icon document" />
                                                <span>View Code </span>
                                              </button>
                                            )}
                                            &nbsp; &nbsp;
                                            <a
                                              className={Styles.airflowLink}
                                              href={`${Envs.DATA_PIPELINES_APP_BASEURL}/graph?dag_id=${dagItem.dagName}`}
                                              target="_blank"
                                              rel="noreferrer"
                                            >
                                              <i
                                                tooltip-data="Open in New Tab"
                                                className={Styles.airflowNewTab + ' icon mbc-icon new-tab'}
                                              />
                                            </a>
                                          </React.Fragment>
                                        )}
                                      </div>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          </div>
                          {item.isOwner && (
                            <div className={Styles.prjListAction}>
                              <div className={Styles.actionBtnGrp}>
                                <button
                                  onClick={onEditProject(item.projectId)}
                                  className={Styles.actionBtn + ' btn btn-primary'}
                                  type="button"
                                >
                                  <i className="icon mbc-icon edit" />
                                  <span>Edit Project</span>
                                </button>{' '}
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
      {collPermissionModel && (
        <Modal
          title={'Please confirm collabarators permission'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'50%'}
          buttonAlignment="right"
          show={collPermissionModel}
          content={dagCollUsersListContent}
          scrollableContent={false}
          onCancel={collPermissionModelClose}
        />
      )}
    </React.Fragment>
  );
};

export default PipelineSubList;
