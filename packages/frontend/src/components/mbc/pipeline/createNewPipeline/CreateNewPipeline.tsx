import cn from 'classnames';
import React, { useState, useEffect } from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import {
  IUserDetails,
  IPipelineProjectDagsCollabarator,
  IPipelineProjectDag,
  IPipelineProjectDetail,
  IError,
  IUserInfo,
} from 'globals/types';
import Styles from './CreateNewPipeline.scss';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Modal from 'components/formElements/modal/Modal';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { getParams } from '../../../../router/RouterUtils';
import AddUser from './addUser/AddUser';
const classNames = cn.bind(Styles);
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/ext-language_tools';

import { PipelineApiClient } from '../../../../services/PipelineApiClient';
import { history } from '../../../../router/History';
import FullScreenModeIcon from 'components/icons/fullScreenMode/FullScreenModeIcon';

// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
// @ts-ignore
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';

// @ts-ignore
import InputFields from '../../../../assets/modules/uilab/js/src/input-fields';
import ProgressWithMessage from 'components/progressWithMessage/ProgressWithMessage';
import { Envs } from 'globals/Envs';
import Caption from 'components/mbc/shared/caption/Caption';

interface ICreateNewPipelineProps {
  user: IUserInfo;
}

const CreateNewPipeline = (props: ICreateNewPipelineProps) => {
  const { id } = getParams();
  const [collabaration, setCollabaration] = useState(false);
  const [codeEditor, setCodeEditor] = useState(false);
  const [deleteDag, setDeleteDag] = useState(false);
  const [deleteDagLocal, setDeleteDagLocal] = useState<boolean>(false);
  const [deleteDagIndex, setDeleteDagIndex] = useState<number>();
  const [projectDescription, setProjectDescription] = useState<string>('');
  const [projectName, setProjectName] = useState<string>('');
  const [missingEntryPrjName, setMissingEntryPrjName] = useState<string>('');
  const [missingEntryDesc, setMissingEntryDesc] = useState<string>('');
  const [missingEntryDag, setMissingEntryDag] = useState<string>('');
  const [dagsList, setDagsList] = useState<IPipelineProjectDag[]>([]);
  const [dagNameStatus, setDagNameStatus] = useState<boolean>(true);
  const [dagEditorContent, setDagEditorContent] = useState<string>('');
  const [dagNameEditSave, setDagNameEditSave] = useState<boolean>(true);
  const [dagNameFieldInput, setDagNameFieldInput] = useState<string>('');
  const [dagNameFieldInputError, setDagNameFieldInputError] = useState<string>('');
  const [dagEditorContentError, setDagEditorContentError] = useState<string>('');
  const [newUniquePID, setNewUniquePID] = useState<string>('');
  const [isUpdate, setIsUpdate] = useState<boolean>(false);
  const [currentProjectId, setCurrentProjectId] = useState<string>('');
  const [isLoad, setIsLoad] = useState<boolean>(true);
  const [dagNameNotExisteDagContent, setDagNameNotExisteDagContent] = useState<boolean>(false);
  const [isActiveDag, setIsActiveDag] = useState<boolean>(false);
  const [dagNameExisted, setDagNameExisted] = useState<boolean>(false);
  const [currDagIndex, setCurrDagIndex] = useState<number>(-1);
  const [copyAlert, setCopyAlert] = useState<boolean>(false);
  const [projectIdReceived, setProjectIdReceived] = useState<boolean>(true);
  const [deletingProjectName, setDeletingProjectName] = useState<string>('');
  const [deletingDagName, setDeletingDagName] = useState<string>('');
  const [deletingDagIndex, setDeletingDagIndex] = useState<number>();
  const [uniqDagTemp, setUniqDagTemp] = useState<string>('');
  const [createProjectError, setCreateProjectError] = useState<string>('');
  const [addDagShow, setAddDagShow] = useState<boolean>(true);
  const [isFsEnable, setIsFsEnable] = useState<boolean>(false);
  const [activeDagLength, setActiveDagLength] = useState<number>();
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);

  const fsActive = () => {
    setIsFsEnable(!isFsEnable);
  };
  const collabarationClose = () => {
    setCollabaration(false);
    setCodeEditor(false);
  };
  const addDagClose = () => {
    setCollabaration(false);
    setCodeEditor(false);
  };
  const addDag = () => {
    const defaultName = 'DAG_' + (dagsList.length + 1);
    const defaultDAGCode =
      'from airflow import DAG\n' + 'dag = DAG(\n' + "dag_id='" + (newUniquePID + '_' + defaultName) + "'\n" + ')';
    setDagNameFieldInput(defaultName);
    setDagEditorContent(defaultDAGCode);
    setDagNameStatus(true);
    setDagNameEditSave(true);
    setDagNameNotExisteDagContent(false);
    setIsActiveDag(false);
    setCodeEditor(true);
    setCurrDagIndex(-1);
  };
  const deleteDagAcceptLocal = () => {
    dagsList.splice(deleteDagIndex, 1);
    setDagsList([...dagsList]);
    setDeleteDagLocal(false);
  };
  const deleteDagServer = (dagName: string, projectName: string, dagIndex: number) => {
    return () => {
      const activeDagCount = dagsList.filter((item) => item.active !== undefined).length;
      setActiveDagLength(activeDagCount);
      setDeleteDag(true);
      setDeletingProjectName(projectName);
      setDeletingDagName(dagName);
      setDeletingDagIndex(dagIndex);
    };
  };
  const deleteDagLocall = (index: number) => {
    return () => {
      setDeleteDagLocal(true);
      setDeleteDagIndex(index);
      setActiveDagLength(0);
    };
  };

  const deleteDagClose = () => {
    setDeleteDag(false);
    setIsFsEnable(false);
    setDeleteDagLocal(false);
  };

  const deleteDagAccept = () => {
    ProgressIndicator.show();
    PipelineApiClient.deleteDag(deletingDagName, deletingProjectName)
      .then(() => {
        dagsList.splice(deletingDagIndex, 1);
        setDagsList([...dagsList]);
        setDeleteDag(false);
        Notification.show('DAG deleted successfully!');
        if (activeDagLength === 1) {
          history.push('/pipeline');
        }
        ProgressIndicator.hide();
      })
      .catch((err: Error) => {
        setDeleteDag(false);
        Notification.show(err.message, 'alert');
        ProgressIndicator.hide();
      });
  };
  const onProjectNameOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    setProjectName(e.currentTarget.value);
  };
  const onProjectDescriOnChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setProjectDescription(e.currentTarget.value);
  };
  const onEditDagName = () => {
    setDagNameStatus(true);
    setDagNameEditSave(true);
  };
  const setSameDAGNameExistError = () => {
    const currentDagName = newUniquePID + '_' + dagNameFieldInput;
    const existingDag = dagsList.some((item: IPipelineProjectDag) => {
      if (item.dagName === currentDagName) {
        return true;
      } else {
        setDagNameExisted(false);
        return false;
      }
    });
    setDagNameExisted(existingDag);
    setDagNameEditSave(existingDag);
    setDagNameStatus(existingDag);
    return existingDag;
  };
  const onEditDagNameSave = () => {
    setDagNameFieldInput(dagNameFieldInput);
    setDagNameEditSave(false);
    setUniqDagTemp(newUniquePID);
    setDagNameStatus(false);
    setSameDAGNameExistError();
    Tooltip.defaultSetup();
  };
  const onDagNameInput = (e: React.FormEvent<HTMLInputElement>) => {
    setDagNameFieldInput(e.currentTarget.value);
  };
  const onDagCodeChange = (newValue: any, e: any) => {
    setDagEditorContent(newValue);
    setDagEditorContentError(' ');
  };

  const onCollabaratorDelete = (dagName: string, index: number) => {
    const currectDagsList = dagsList.filter((item: IPipelineProjectDag) => {
      return item.dagName == dagName;
    });
    return () => {
      currectDagsList[0]?.collaborators?.splice(index, 1);
      setDagsList(dagsList);
      setDagsList([...dagsList]);
      setIsLoad(false);
    };
  };
  const copyDagName = () => {
    navigator.clipboard.writeText('');
    navigator.clipboard.writeText(uniqDagTemp + '_' + dagNameFieldInput.replace(newUniquePID + '_', ''));
    setCopyAlert(true);
    setTimeout(() => {
      setCopyAlert(false);
    }, 500);
  };
  const copyDagNameStaticCodeEditor = () => {
    navigator.clipboard.writeText('');
    navigator.clipboard.writeText(dagNameFieldInput);
    setCopyAlert(true);
    setTimeout(() => {
      setCopyAlert(false);
    }, 500);
  };

  const validateAddNewDag = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (dagNameFieldInput === '') {
      setDagNameFieldInputError(errorMissingEntry);
      formValid = false;
    } else {
      setDagNameFieldInputError('');
    }
    if (dagEditorContent === '') {
      setDagEditorContentError(errorMissingEntry);
      formValid = false;
    }

    const dagNameToValidate = isActiveDag
      ? dagNameFieldInput
      : newUniquePID + '_' + dagNameFieldInput.replace(newUniquePID + '_', '');
    const dagIdMatched =
      dagEditorContent.includes(`'${dagNameToValidate}'`) || dagEditorContent.includes(`"${dagNameToValidate}"`);
    if (!dagIdMatched) {
      setDagNameNotExisteDagContent(true);
      formValid = false;
    } else {
      setDagNameNotExisteDagContent(false);
    }
    return formValid;
  };
  const addDagToPrj = () => {
    if (validateAddNewDag() && !setSameDAGNameExistError()) {
      if (isActiveDag) {
        dagsList[currDagIndex].dagContent = dagEditorContent;
      } else {
        if (currDagIndex !== -1) {
          dagsList[currDagIndex].dagName = newUniquePID + '_' + dagNameFieldInput.replace(newUniquePID + '_', '');
          // dagsList[currDagIndex].dagName  = dagNameFieldInput; // for local DAG creation Name format
          dagsList[currDagIndex].dagContent = dagEditorContent;
        } else {
          const data = {
            dagName: newUniquePID + '_' + dagNameFieldInput,
            // dagName: dagNameFieldInput, // for local DAG creation Name format
            dagContent: dagEditorContent,
            collaborators: [],
          } as unknown as IPipelineProjectDag;
          dagsList.push(data);
        }
        setDagsList([...dagsList]);
      }
      setCodeEditor(false);
      setDagsList([...dagsList]);
      setIsFsEnable(false);
    }
  };

  const getCollabarators = (collaborators: IUserDetails, dagId: string) => {
    const collabarationData = {
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      email: collaborators.email,
      username: collaborators.shortId,
      permissions: ['can_dag_read', 'can_dag_edit'] as string[],
    };
    const modDagList = dagsList.filter((item: IPipelineProjectDag) => {
      return item.dagName === dagId;
    });
    let isError = false;
    modDagList.forEach((item) => {
      item.collaborators.forEach((collabItem) => {
        if (collaborators.shortId === collabItem.username) {
          isError = true;
        }
      });
    });

    const isCreator = props.user.id === collaborators.shortId;

    if (isCreator) {
      Notification.show(
        `${collaborators.firstName} ${collaborators.lastName} is a creator. Creator can't be added as collaborator.`,
        'warning',
      );
    } else if (!isError) {
      modDagList[0].collaborators.push(collabarationData);
      setIsLoad(false);
      setDagsList([...dagsList]);
    } else {
      Notification.show('Collaborator Already Exist.', 'warning');
    }
  };

  const validateNewProjectForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (projectName.length === 0) {
      setMissingEntryPrjName(errorMissingEntry);
      formValid = false;
    }
    if (projectDescription.length === 0) {
      setMissingEntryDesc(errorMissingEntry);
      formValid = false;
    }
    if (dagsList.length === 0) {
      setMissingEntryDag(errorMissingEntry);
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };
  const onNewProjectSubmit = () => {
    if (validateNewProjectForm()) {
      addNewPrj();
    }
  };
  const onExistingProjectSubmit = (id: string) => {
    return () => {
      if (validateNewProjectForm()) {
        updateProject(id);
      }
    };
  };

  const onPermissionEdit = (dagId: string, index: number) => {
    return () => {
      let dagIndex = -1;
      const dagItem = dagsList.find((item: IPipelineProjectDag, itemIndex: number) => {
        dagIndex = itemIndex;
        return item.dagName == dagId;
      });
      if (dagItem.collaborators[index].permissions.includes('can_dag_edit')) {
        dagItem.collaborators[index].permissions.splice(
          dagItem.collaborators[index].permissions.indexOf('can_dag_edit'),
          1,
        );
      } else {
        dagItem.collaborators[index].permissions.push('can_dag_edit');
      }
      dagsList[dagIndex] = dagItem;
      setDagsList([...dagsList]);
    };
  };
  const updateProject = (id: string) => {
    const modDagList = dagsList.map((item: IPipelineProjectDag) => {
      item.collaborators?.map((coll: IPipelineProjectDagsCollabarator) => {
        delete coll.id;
      });
      return item;
    });
    const data = {
      data: {
        projectId: id,
        projectName: projectName,
        projectDescription: projectDescription,
        dags: modDagList,
      },
    };
    ProgressIndicator.show();
    setIsApiCallTakeTime(true);
    PipelineApiClient.putExistingProject(id, data)
      .then((response) => {
        history.push('/pipeline');
        Notification.show('Project Updated successfully!');
        setCreateProjectError('');
        setIsApiCallTakeTime(false);
        window.location.reload();
        ProgressIndicator.hide();
      })
      .catch((err: Error) => {
        setCreateProjectError(err.message);
        setIsApiCallTakeTime(false);
        ProgressIndicator.hide();
      });
  };
  const addNewPrj = () => {
    const data = {
      data: {
        projectId: newUniquePID,
        projectName: projectName,
        projectDescription: projectDescription,
        dags: dagsList,
      },
    };
    ProgressIndicator.show();
    setIsApiCallTakeTime(true);
    PipelineApiClient.addNewProject(data)
      .then((response) => {
        history.push(`/pipeline`);
        Notification.show('New Project Created successfully!');
        setCreateProjectError('');
        setIsApiCallTakeTime(false);
        ProgressIndicator.hide();
      })
      .catch((err: IError) => {
        setCreateProjectError(err.message);
        setIsApiCallTakeTime(false);
        ProgressIndicator.hide();
      });
  };
  const editCodeCurrentDag = (dagId: string, dagContent: string, isActive: boolean, dagIndex: number) => {
    return () => {
      setCodeEditor(true);
      setCurrDagIndex(dagIndex);
      setDagNameFieldInput(dagId);
      setDagEditorContent(dagContent);
      setIsActiveDag(isActive);
      setDagNameNotExisteDagContent(false);
    };
  };
  useEffect(() => {
    Tooltip.defaultSetup();
    if (id) {
      if (isLoad) {
        ProgressIndicator.show();
        setIsApiCallTakeTime(true);
        PipelineApiClient.getExistingProject(id)
          .then((response: IPipelineProjectDetail) => {
            setIsLoad(false);
            setProjectName(response.projectName);
            setProjectDescription(response.projectDescription);
            setCurrentProjectId(response.projectId);
            setNewUniquePID(response.projectId);
            setIsUpdate(true);
            const dagsDataTemp: any = [];
            response.dags?.forEach((item) => {
              if (item.collaborators === null) {
                item.collaborators = [];
              }
              dagsDataTemp.push(item);
            });
            setDagsList(dagsDataTemp);
            setIsApiCallTakeTime(false);
            ProgressIndicator.hide();
          })
          .catch((err: Error) => {
            Notification.show(err.message, 'alert');
            setIsApiCallTakeTime(false);
            ProgressIndicator.hide();
          });
      }
    } else {
      if (projectIdReceived) {
        ProgressIndicator.show();
        PipelineApiClient.getUniquePID()
          .then((response) => {
            setNewUniquePID(response.projectId);
            setProjectIdReceived(false);
            ProgressIndicator.hide();
          })
          .catch(() => {
            ProgressIndicator.hide();
          });
      }
      if (dagsList.length > 0) {
        setAddDagShow(false);
      } else {
        setAddDagShow(true);
      }
    }
    ExpansionPanel.defaultSetup();
  }, [dagsList]);
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [codeEditor]);

  const deleteDagContent = (
    <div className={Styles.DeleteDag}>
      <h3>Are you sure want to delete DAG?</h3>
      {activeDagLength === 1 && (
        <h6>
          Your are about to delete last Active DAG in the Project <br />
          This will also delete the Project(Including New Dags).
        </h6>
      )}
    </div>
  );
  const contentForCodeEditor = (
    <div className={isFsEnable ? Styles.codeEditorFs : ' '}>
      <div className={Styles.contentForCodeEditor}>
        <div className={Styles.modalContent}>
          <div className={Styles.dagCodeEditorTile}>
            {isActiveDag ? (
              <React.Fragment>
                {dagNameFieldInput}
                <div className={Styles.editIconCopy}>
                  &nbsp;
                  <div onClick={copyDagNameStaticCodeEditor} tooltip-data="Copy">
                    <i className="icon mbc-icon copy" />
                  </div>
                  {copyAlert && <label> DAG Name Copied</label>}
                </div>
              </React.Fragment>
            ) : (
              <React.Fragment>
                <img className={Styles.brandLogo} src={Envs.DNA_BRAND_LOGO_URL} />
                <div className={Styles.dagNameModifyField}>
                  DAG Name: &nbsp;{' '}
                  <div className={Styles.dagRenameField}>
                    {newUniquePID}_
                    {dagNameStatus ? (
                      <React.Fragment>
                        <input
                          type="text"
                          value={
                            currDagIndex === -1 ? dagNameFieldInput : dagNameFieldInput.replace(newUniquePID + '_', '')
                          }
                          placeholder="Enter Dag Name"
                          maxLength={200}
                          onChange={onDagNameInput}
                        />
                      </React.Fragment>
                    ) : dagNameFieldInput !== null ? (
                      currDagIndex === -1 ? (
                        dagNameFieldInput.replace(newUniquePID + '_', '')
                      ) : (
                        dagNameFieldInput.replace(newUniquePID + '_', '')
                      )
                    ) : (
                      ''
                    )}
                    <div className={Styles.fullScr} onClick={fsActive}>
                      <FullScreenModeIcon fsNeed={isFsEnable} />
                    </div>
                  </div>
                  <br />
                  <React.Fragment>
                    <div className={!dagNameEditSave ? ' ' : Styles.iconHide + ' '}>
                      <div className={Styles.editIconCopy}>
                        <div onClick={onEditDagName} tooltip-data="Edit">
                          <i className="icon mbc-icon edit" />
                        </div>{' '}
                        &nbsp;
                        <div onClick={copyDagName} tooltip-data="Copy">
                          <i className="icon mbc-icon copy" />
                        </div>
                        {copyAlert && <label> DAG Name Copied</label>}
                      </div>
                    </div>
                  </React.Fragment>
                  <div
                    tooltip-data="Save"
                    className={dagNameEditSave ? ' ' : Styles.iconHide}
                    onClick={onEditDagNameSave}
                  >
                    <i className="icon mbc-icon check circle" />
                  </div>
                </div>
              </React.Fragment>
            )}
          </div>
          {dagNameExisted && (
            <div className={Styles.dagExistedError}>Dag with this name already exist in the project.</div>
          )}
          <span className={classNames('error-message', dagNameFieldInputError.length ? '' : 'hide')}>
            {dagNameFieldInputError}
          </span>
          <div className={Styles.dagCodeEditorContent}>
            <AceEditor
              width="100%"
              height={isFsEnable ? '60vh' : '100%'}
              placeholder="Type here"
              mode="python"
              theme="solarized_dark"
              name="dagEditor"
              fontSize={16}
              showPrintMargin={false}
              showGutter={true}
              highlightActiveLine={true}
              value={dagEditorContent}
              onChange={onDagCodeChange}
              setOptions={{
                enableBasicAutocompletion: true,
                enableLiveAutocompletion: false,
                enableSnippets: false,
                showLineNumbers: true,
                tabSize: 2,
              }}
            />
            <br /> <br />
            <span className={classNames('error-message', dagEditorContentError.length ? '' : 'hide')}>
              {dagEditorContentError}
            </span>
            {dagNameNotExisteDagContent && (
              <div className={Styles.dagNameNotExisteDagContent}>
                Your DAG Code is invalid. DAG Name({dagNameFieldInput}) is not found in code as dag_id!
              </div>
            )}
          </div>
          <div className={Styles.addBtn}>
            <button className={Styles.actionBtn + ' btn btn-tertiary'} onClick={addDagToPrj} type="button">
              <span>{isActiveDag || currDagIndex !== -1 ? 'Update' : 'Add'} </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title={id ? 'Edit Pipeline Project' : 'New Pipeline Project'} />
        <div className={Styles.content}>
          <div className={Styles.CreateNewAirflowForm}>
            {/* <div className={Styles.editicon}>
              <i className="icon mbc-icon edit small " />
            </div>
            <h6>Short Description - Lorem ipsum dolor sit amet</h6> */}
            <div className={Styles.row}>
              <div className={classNames(Styles.inputGrp)}>
                <div
                  className={classNames(
                    Styles.inputGrpChild + ' input-field-group include-error ',
                    missingEntryPrjName.length ? 'error' : '',
                  )}
                >
                  <label id="PrjName" htmlFor="PrjName" className="input-label">
                    Project Name<sup>*</sup>
                  </label>
                  <input
                    type="text"
                    className="input-field"
                    required={true}
                    id="PrjName"
                    maxLength={64}
                    placeholder="Type here"
                    autoComplete="off"
                    onChange={onProjectNameOnChange}
                    value={projectName}
                  />
                  <span className={classNames('error-message', missingEntryPrjName.length ? '' : 'hide')}>
                    {missingEntryPrjName}
                  </span>
                </div>
              </div>
              <div
                className={classNames(
                  Styles.inputGrp + ' input-field-group include-error ',
                  missingEntryDesc.length ? 'error' : '',
                )}
              >
                <label id="descriptionLabel" htmlFor="descriptionLabel" className="input-label">
                  Description<sup>*</sup>
                </label>
                <textarea
                  className="input-field-area"
                  required={true}
                  id="descriptionLabel"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                  cols={20}
                  onChange={onProjectDescriOnChange}
                  value={projectDescription}
                />
                <span className={classNames('error-message', missingEntryDesc.length ? '' : 'hide')}>
                  {missingEntryDesc}
                </span>
              </div>
            </div>
            <div className={Styles.dagGrp}>
              <div className={Styles.dagGrpTitle}>
                {addDagShow && (
                  <button onClick={addDag} className={Styles.addDagbtn + ' btn btn-primary'} type="button">
                    <i className="icon mbc-icon plus" />
                    <span>Add DAG</span>
                  </button>
                )}
              </div>

              <div className={Styles.dagGrpList}>
                {dagsList.length ? (
                  <div className={Styles.dagGrpListItem}>
                    <div className={Styles.dagCaption}>
                      <div className={Styles.dagTile}>
                        <div className={Styles.dagTitleCol}>DAG Id</div>
                        <div className={Styles.dagTitleCol}>Your Permission</div>
                        <div className={Styles.dagTitleCol}>Collaboration</div>
                        <div className={Styles.dagTitleCol}>Status</div>
                        <div className={Styles.dagTitleCol}>Action</div>
                      </div>
                    </div>
                    {dagsList.map((dagItem: IPipelineProjectDag, index) => {
                      return (
                        <div
                          key={index}
                          className={'expansion-panel-group airflowexpansionPanel ' + Styles.dagGrpListItemPanel}
                        >
                          <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                            <span className="animation-wrapper"></span>
                            <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                            <label
                              className={'expansion-panel-label' + ' ' + Styles.dagContentCaption}
                              htmlFor={index + '1'}
                            >
                              <div className={Styles.dagTile}>
                                <div className={Styles.dagTitleCol}> {dagItem.dagName}</div>
                                <div className={Styles.dagTitleCol}>Owner</div>
                                <div className={Styles.dagTitleCol}>
                                  {dagItem.collaborators?.length > 0 ? (
                                    <React.Fragment>
                                      {dagItem.collaborators.map((collItem, index) => {
                                        return (
                                          <React.Fragment key={index}>
                                            {collItem.username} {dagItem.collaborators.length === index + 1 ? '' : ','}
                                          </React.Fragment>
                                        );
                                      })}
                                    </React.Fragment>
                                  ) : (
                                    'NA'
                                  )}{' '}
                                </div>
                                <div className={Styles.dagTitleCol}>
                                  {dagItem?.active !== undefined ? (
                                    <span className={Styles.activeDag}>Active</span>
                                  ) : (
                                    'New'
                                  )}
                                </div>
                                <div className={Styles.dagTitleCol}></div>
                              </div>
                              <i tooltip-data="Expand" className="icon down-up-flip"></i>
                            </label>

                            <div className="expansion-panel-content">
                              <div className={Styles.dagTile + ' ' + Styles.dagTileContentCollabarationPanel}>
                                <div className={Styles.dagCollTile}>
                                  <h6>{''}</h6>
                                  <div className={Styles.actionBtnGrp}>
                                    {dagItem?.active !== undefined ? (
                                      ''
                                    ) : (
                                      <button
                                        className={Styles.actionBtn + ' btn btn-primary'}
                                        onClick={editCodeCurrentDag(
                                          dagItem.dagName,
                                          dagItem.dagContent,
                                          dagItem.active,
                                          index,
                                        )}
                                        type="button"
                                      >
                                        <i className="icon mbc-icon edit" />
                                        <span>Edit Code</span>
                                      </button>
                                    )}
                                    &nbsp;&nbsp;
                                    <button
                                      onClick={
                                        dagItem?.active !== undefined
                                          ? deleteDagServer(dagItem.dagName, projectName, index)
                                          : deleteDagLocall(index)
                                      }
                                      className={Styles.actionBtn + ' btn btn-primary'}
                                      type="button"
                                    >
                                      <i className="icon delete" />
                                      <span>Delete DAG</span>
                                    </button>
                                  </div>
                                </div>
                              </div>
                              <div className={Styles.dagCollContent}>
                                <div className={Styles.dagCollContentList}>
                                  <div className={Styles.dagCollContentListAdd}>
                                    <AddUser getCollabarators={getCollabarators} dagId={dagItem.dagName} />
                                  </div>
                                  <div className={Styles.dagCollUsersList}>
                                    {dagItem.collaborators?.length > 0 ? (
                                      <React.Fragment>
                                        <div className={Styles.collUserTitle}>
                                          <div className={Styles.collUserTitleCol}>User ID</div>
                                          <div className={Styles.collUserTitleCol}>Name</div>
                                          <div className={Styles.collUserTitleCol}>Permission</div>
                                          <div className={Styles.collUserTitleCol}></div>
                                        </div>
                                        <div className={Styles.collUserContent}>
                                          {dagItem.collaborators.map(
                                            (item: IPipelineProjectDagsCollabarator, collIndex: any) => {
                                              return (
                                                <div
                                                  key={index + '-' + collIndex}
                                                  className={Styles.collUserContentRow}
                                                >
                                                  <div className={Styles.collUserTitleCol}>{item.username}</div>
                                                  <div className={Styles.collUserTitleCol}>
                                                    {item.firstName + ' ' + item.lastName}
                                                  </div>
                                                  <div className={Styles.collUserTitleCol}>
                                                    <div
                                                      className={classNames(
                                                        'input-field-group include-error ' + Styles.inputGrp,
                                                      )}
                                                    >
                                                      <label className={'checkbox ' + Styles.checkBoxDisable}>
                                                        <span className="wrapper">
                                                          <input
                                                            type="checkbox"
                                                            className="ff-only"
                                                            value="can_dag_read"
                                                            checked={true}
                                                          />
                                                        </span>
                                                        <span className="label">Read</span>
                                                      </label>
                                                    </div>
                                                    &nbsp;&nbsp;&nbsp;
                                                    <div
                                                      className={classNames(
                                                        'input-field-group include-error ' + Styles.inputGrp,
                                                      )}
                                                    >
                                                      <label className={'checkbox ' + Styles.writeAccess}>
                                                        <span className="wrapper">
                                                          <input
                                                            type="checkbox"
                                                            className="ff-only"
                                                            value="can_dag_edit"
                                                            checked={
                                                              item.permissions !== null
                                                                ? item.permissions.includes('can_dag_edit')
                                                                : false
                                                            }
                                                            onClick={onPermissionEdit(dagItem.dagName, collIndex)}
                                                          />
                                                        </span>
                                                        <span className="label">Write</span>
                                                      </label>
                                                    </div>
                                                  </div>
                                                  <div className={Styles.collUserTitleCol}>
                                                    <div
                                                      className={Styles.deleteEntry}
                                                      onClick={onCollabaratorDelete(dagItem.dagName, collIndex)}
                                                    >
                                                      <i className="icon mbc-icon trash-outline" />
                                                      Delete Entry
                                                    </div>
                                                  </div>
                                                </div>
                                              );
                                            },
                                          )}
                                        </div>
                                      </React.Fragment>
                                    ) : (
                                      <div className={Styles.dagCollContentEmpoty}>
                                        <h6> Collaborators Not Exist!</h6>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className={Styles.dagGrpListEmpty}>
                    <h6> DAG file Not Exist!</h6>
                    <div className={Styles.dagError}>
                      <span className={classNames('error-message', missingEntryDag.length ? '' : 'hide')}>
                        {missingEntryDag}
                      </span>
                    </div>
                  </div>
                )}
              </div>
            </div>
            <p className={Styles.DagCodeError}> {createProjectError !== '' && createProjectError} </p>
            <div className={Styles.createBtn}>
              <button
                className={'btn btn-tertiary'}
                type="button"
                onClick={isUpdate ? onExistingProjectSubmit(currentProjectId) : onNewProjectSubmit}
              >
                <span>{isUpdate ? 'Update Project' : 'Create Project'}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      {collabaration && (
        <Modal
          title={'Add Collabaration'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={collabaration}
          content={''}
          scrollableContent={false}
          onCancel={collabarationClose}
        />
      )}
      {codeEditor && (
        <Modal
          title={'DAG Code Editor'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'90%'}
          buttonAlignment="right"
          show={codeEditor}
          content={contentForCodeEditor}
          scrollableContent={false}
          onCancel={addDagClose}
        />
      )}
      {deleteDag && (
        <ConfirmModal
          title={''}
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={deleteDag}
          content={deleteDagContent}
          onCancel={deleteDagClose}
          onAccept={deleteDagAccept}
        />
      )}
      {deleteDagLocal && (
        <ConfirmModal
          title={''}
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={deleteDagLocal}
          content={deleteDagContent}
          onCancel={deleteDagClose}
          onAccept={deleteDagAcceptLocal}
        />
      )}
      {isApiCallTakeTime && <ProgressWithMessage message={'Please wait as this process can take up to a minute....'} />}
    </React.Fragment>
  );
};

export default CreateNewPipeline;
