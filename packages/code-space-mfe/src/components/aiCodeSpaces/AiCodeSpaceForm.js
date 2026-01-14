import React, { useState, useEffect } from 'react';
import cn from 'classnames';
import Styles from './AiCodeSpaceForm.scss';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// @ts-ignore
// import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import SelectBox from 'dna-container/SelectBox';
import TextBox from 'dna-container/TextBox';
import { useHistory } from 'react-router-dom';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { hostServer } from '../../server/api';
import Tags from 'dna-container/Tags';
import { Envs } from '../../Utility/envs';

const classNames = cn.bind(Styles);

const AiCodeSpaceForm = (props) => {
  console.log(props?.user);
  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);

  const [projectName, setProjectName] = useState('');
  const [projectNameError, setProjectNameError] = useState('');

  const [environment, setEnvironment] = useState('DHC-CaaS-AWS');

  const [typeOfProject, setTypeOfProject] = useState('0');
  const [typeOfProjectError, setTypeOfProjectError] = useState('');
  const isPlayground = typeOfProject === 'Playground';

  const [description, setDescription] = useState('');
  const [descriptionError, setDescriptionError] = useState('');

  const [division, setDivision] = useState('0');
  const [divisionError, setDivisionError] = useState('');

  const [subDivision, setSubDivision] = useState('0');

  const [department, setDepartment] = useState([]);
  const [departmentError, setDepartmentError] = useState(false);

  const [classificationType, setClassificationType] = useState('0');
  const [classificationTypeError, setClassificationTypeError] = useState('');

  const [PII, setPII] = useState(false);

  // const tags = [];

  const [archerId, setArcherId] = useState('');
  const [archerIdError, setArcherIdError] = useState('');

  const [procedureID, setProcedureID] = useState('');
  const [procedureIDError, setProcedureIDError] = useState('');

  const [enableDeployApproval, setEnableDeployApproval] = useState(false);

  const [modelSource, setModelSource] = useState('Azure');
  const [modelSourceError, setModelSourceError] = useState('');

  const [agentApiKey, setAgentApiKey] = useState('');
  const [agentApiKeyError, setAgentApiKeyError] = useState('');

  const [uiApiKey, setUiApiKey] = useState('');
  const [uiApiKeyError, setUiApiKeyError] = useState('');

  const requiredError = '*Missing entry';
  // const namePrefix = props.user.firstName;

  const History = useHistory();
  const goback = () => {
    History.goBack();
  };

  useEffect(() => {
    // if (!onBoadingMode) {
    ProgressIndicator.show();
    CodeSpaceApiClient.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data.data || []);
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data.data || []);
        // onEditingMode && setDivision(projectDetails?.dataGovernance?.division ? projectDetails?.dataGovernance?.divisionId + '@-@' + projectDetails?.dataGovernance?.division : '0');
        // onEditingMode && setClassificationType(projectDetails?.dataGovernance?.classificationType ? projectDetails?.dataGovernance?.classificationType : '0');
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
    // }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const divId = division.includes('@-@') ? division.split('@-@')[0] : division;
    if (divId && divId !== '0') {
      ProgressIndicator.show();
      hostServer
        .get('/subdivisions/' + divId)
        .then((res) => {
          setSubDivisions(res?.data || []);
          // onEditingMode && setSubDivision(projectDetails?.dataGovernance?.subDivision ? projectDetails?.dataGovernance?.subDivisionId + '@-@' + projectDetails?.dataGovernance?.subDivision : '0');
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        })
        .catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
  }, [division]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, [typeOfProject]);

  const sanitizedRepositoryName = (name) => {
    return name.replace(/[\s_-]/g, '-');
  };

  const onProjectNameOnChange = (evnt) => {
    const projectNameVal = sanitizedRepositoryName(evnt.currentTarget.value);
    setProjectName(projectNameVal);
    const hasSpecialChars = /[^A-Za-z0-9-]/.test(projectNameVal);
    const startsOrEndswith = /^-|-$|(--)|^\d+$/i.test(projectNameVal);
    const startsWithNumber = /^\d/.test(projectNameVal);

    if (hasSpecialChars) {
      setProjectNameError('Invalid name: Should not contain any special characters except for "-".');
    } else if (!projectNameVal.length) {
      setProjectNameError(requiredError);
    } else if (startsWithNumber) {
      setProjectNameError('Invalid name: Should not start with a number.');
    } else if (startsOrEndswith) {
      setProjectNameError('Invalid name: Should not start or end with "-" or name contains only numbers.');
    } else {
      setProjectNameError('');
    }
  };

  const onTypeOfProjectChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setTypeOfProject(selectedOption);
  };

  const onDescriptionChange = (e) => {
    const currentValue = e.currentTarget.value;
    setDescription(currentValue);
    setDescriptionError(currentValue.length !== 0 ? '' : requiredError);
  };

  const onDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setDivision(selectedOption);
  };

  const onSubDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setSubDivision(selectedOption);
  };

  const onClassificationChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setClassificationType(selectedOption);
  };

  const onPIIChange = (e) => {
    const currentValue = e.currentTarget.value;
    if (currentValue === 'true') {
      setPII(true);
    } else {
      setPII(false);
    }
  };

  const onArcherIdChange = (e) => {
    const currentValue = e.currentTarget.value;
    setArcherId(currentValue);
    const pattern = /^(INFO)-\d{1,10}$/.test(currentValue);
    setArcherIdError(currentValue.length && !pattern ? 'Archer ID should be of type INFO-XXXXX' : '');
  };

  const onProcedureIDChange = (e) => {
    const currentValue = e.currentTarget.value;
    setProcedureID(currentValue);
    const pattern = /^(PO|ITPLC)-\d{1,10}$/.test(currentValue);
    setProcedureIDError(currentValue.length && !pattern ? 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX' : '');
  };

  const onEnvironmentChange = (evnt) => {
    setEnvironment(evnt.currentTarget.value.trim());
  };

  const onModelSourceChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setModelSource(selectedOption);
  };

  const onCreateAgent = () => {
    let formValid = true;
    if (!projectName.length) {
      setProjectNameError(requiredError);
      formValid = false;
    }
    if (typeOfProject === '0') {
      setTypeOfProjectError(requiredError);
      formValid = false;
    }
    if (!description.length) {
      setDescriptionError(requiredError);
      formValid = false;
    }
    if (!isPlayground && division === '0') {
      setDivisionError(requiredError);
      formValid = false;
    }
    if (!department.length) {
      setDepartmentError(true);
      formValid = false;
    }
    if (!isPlayground && classificationType === '0') {
      setClassificationTypeError(requiredError);
      formValid = false;
    }
    if (modelSource === '0') {
      setModelSourceError(requiredError);
      formValid = false;
    }
    if (!agentApiKey.length) {
      setAgentApiKeyError(requiredError);
      formValid = false;
    }
    if (!uiApiKey.length) {
      setUiApiKeyError(requiredError);
      formValid = false;
    }
    if (formValid) {
      const createForm = {
        data: [{
          gitUserName: props.user.id,
          projectDetails: {
            projectCollaborators: [],
            projectName: projectName + '-server',
            recipeName: Envs.MCP_SERVER_RECIPE_ID,
            isAgentCreation: true,
            isAgentInitialized: false,
            // gitRepoName: null,
            dataGovernance: {
              typeOfProject: typeOfProject,
              description: description,
              divisionId: division.split('@-@')[0],
              division: division.split('@-@')[1],
              subDivisionId: subDivision.split('@-@')[0],
              subDivision: subDivision.split('@-@')[1],
              department: department[0],
              classificationType: classificationType,
              piiData: PII,
              archerId: archerId,
              procedureID: procedureID,
              enableDeployApproval: enableDeployApproval,
            },
            recipeDetails: {

              cloudServiceProvider: environment,
            },
          },
        },
        {
          gitUserName: props.user.id,
          projectDetails: {
            projectCollaborators: [],
            projectName: projectName + '-agent',
            recipeName: Envs.AI_AGENT_RECIPE_ID,
            isAgentCreation: true,
            isAgentInitialized: false,
            // gitRepoName: null,
            dataGovernance: {
              typeOfProject: typeOfProject,
              description: description,
              divisionId: division.split('@-@')[0],
              division: division.split('@-@')[1],
              subDivisionId: subDivision.split('@-@')[0],
              subDivision: subDivision.split('@-@')[1],
              department: department[0],
              classificationType: classificationType,
              piiData: PII,
              archerId: archerId,
              procedureID: procedureID,
              enableDeployApproval: enableDeployApproval,
            },
            recipeDetails: {

              cloudServiceProvider: environment,
            },
          },
        },
        {
          gitUserName: props.user.id,
          projectDetails: {
            projectCollaborators: [],
            projectName: projectName + '-host',
            recipeName: Envs.AI_HOST_RECIPE_ID,
            isAgentCreation: true,
            isAgentInitialized: false,
            // gitRepoName: null,
            dataGovernance: {
              typeOfProject: typeOfProject,
              description: description,
              divisionId: division.split('@-@')[0],
              division: division.split('@-@')[1],
              subDivisionId: subDivision.split('@-@')[0],
              subDivision: subDivision.split('@-@')[1],
              department: department[0],
              classificationType: classificationType,
              piiData: PII,
              archerId: archerId,
              procedureID: procedureID,
              enableDeployApproval: enableDeployApproval,
            },
            recipeDetails: {
              cloudServiceProvider: environment,
            },
          },
        }],
        agentVault: {
          AZURE_OPENAI_API_KEY: modelSource === 'Azure' ? agentApiKey : '',
          AZURE_OPENAI_API_VERSION: modelSource === 'Azure' ? '2024-02-01' : '',
          AZURE_OPENAI_DEPLOYMENT_NAME: modelSource === 'Azure' ? 'gpt-4o' : '',
          AZURE_OPENAI_ENDPOINT: modelSource === 'Azure' ? Envs.AZURE_OPENAI_ENDPOINT : '',
          GOOGLE_API_KEY: modelSource === 'Google' ? agentApiKey : '',
          TOOL_LLM_URL: modelSource === 'Google' ? Envs.TOOL_LLM_URL : '',
          TOOL_LLM_NAME: modelSource === 'Google' ? 'gemini-1.5-pro' : '',
          MODEL_SOURCE: modelSource,
        },
        hostVault: {
          GOOGLE_API_KEY: uiApiKey,
          GEMINI_ENDPOINT: Envs.GEMINI_ENDPOINT,
          GEMINI_MODEL: 'gemini-1.5-pro',
          AGENT_HOSTNAME: Envs.CODESPACE_AWS_DEPLOYMENT_URL + "/" + projectName + "-agent/prod/",
        },
      }
      ProgressIndicator.show();
      CodeSpaceApiClient.createAiAgentWorkflow(createForm)
        .then((res) => {
          console.log("res is : ", res);
          ProgressIndicator.hide();
          History.goBack();
          Notification.show("AI agent spaces created successfully");
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show('Error in creating new code space. Please try again later.\n' + err?.response?.data?.errors[0]?.message, 'alert');
        });
    }

  };

  return (
    //before aiCodeSpacePanel add onboard and below panel there is not edit condition
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <div>
              <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>
                Back
              </button>
              <h3>New AI Agent Code Space</h3>
            </div>
          </div>
          <div className={classNames(Styles.content)}>
            <div className={Styles.aiFormContent}>
              <span className={classNames('label')}>
                <p className={Styles.contentHeader}>Lean Governance Fields</p>
              </span>
              <div className={Styles.flexLayout}>
                <div>
                  <TextBox
                    type="text"
                    controlId={'productNameInput'}
                    labelId={'productNameLabel'}
                    label={'Code Space Name'}
                    placeholder={'Type here'}
                    value={projectName}
                    errorText={projectNameError}
                    required={true}
                    maxLength={39}
                    onChange={onProjectNameOnChange}
                  />
                </div>
                <div className={Styles.flexLayout}>
                  <div
                    className={classNames('input-field-group include-error', typeOfProjectError?.length ? 'error' : '')}
                  >
                    <label className={classNames('input-label')}>
                      Type of Project <sup>*</sup>
                    </label>

                    <div className={classNames('custom-select')}>
                      <select
                        id="reportStatusField"
                        defaultValue={typeOfProject}
                        required={true}
                        required-error={requiredError}
                        onChange={onTypeOfProjectChange}
                        value={typeOfProject}
                      >
                        <option id="typeOfProjectOption" value={0}>
                          Choose
                        </option>
                        <option value={'Playground'}>Playground</option>
                        <option value={'Proof of Concept'}>Proof of Concept</option>
                        <option value={'Production'}>Production</option>
                      </select>
                    </div>
                    <p
                      style={{ color: 'var(--color-orange)' }}
                      className={classNames(typeOfProject !== 'Playground' ? ' hide' : '')}
                    >
                      <i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of
                      not being used.
                    </p>
                    <span className={classNames('error-message', typeOfProjectError.length ? '' : 'hide')}>
                      {typeOfProjectError}
                    </span>
                  </div>
                  <div className={classNames('input-field-group')}>
                    <label className={classNames('input-label')}>
                      Enable deployment approval workflow <sup>*</sup>
                    </label>
                    <div>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="true"
                            name="enableDeployApproval"
                            defaultChecked={enableDeployApproval === true}
                            onChange={() => {
                              setEnableDeployApproval(true);
                            }}
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="false"
                            name="enableDeployApproval"
                            defaultChecked={enableDeployApproval === false}
                            onChange={() => {
                              setEnableDeployApproval(false);
                            }}
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div
                  className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '')}
                >
                  <label id="description" className="input-label" htmlFor="description">
                    Description <sup>*</sup>
                  </label>
                  <textarea
                    id="description"
                    className="input-field-area"
                    // type="text"
                    defaultValue={description}
                    required={true}
                    required-error={requiredError}
                    onChange={onDescriptionChange}
                    rows={50}
                  />
                  <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
                    {descriptionError}
                  </span>
                </div>
                <div>
                  {!isPlayground && (
                    <div>
                      <div className={Styles.flexLayout}>
                        <div
                          className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}
                        >
                          <label className={classNames('input-label')}>
                            Division <sup>*</sup>
                          </label>
                          <div className={classNames('custom-select')}>
                            <select
                              id="divisionField"
                              defaultValue={division}
                              required={true}
                              required-error={requiredError}
                              onChange={onDivisionChange}
                              value={division}
                            >
                              <option id="divisionOption" value={0}>
                                Choose
                              </option>
                              {divisions?.map((obj) => {
                                return (
                                  <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                                    {obj.name}
                                  </option>
                                );
                              })}
                            </select>
                          </div>
                          <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                            {divisionError}
                          </span>
                        </div>

                        <div className={classNames('input-field-group include-error')}>
                          <label className={classNames('input-label')}>Sub Division</label>
                          <div className={classNames('custom-select')}>
                            <select
                              id="subDivisionField"
                              defaultValue={subDivision}
                              value={subDivision}
                              required={false}
                              onChange={onSubDivisionChange}
                            >
                              {subDivisions?.some((item) => item.id === '0' && item.name === 'None') ? (
                                <option id="subDivisionDefault" value={0}>
                                  None
                                </option>
                              ) : (
                                <>
                                  <option id="subDivisionDefault" value={0}>
                                    Choose
                                  </option>
                                  {subDivisions?.map((obj) => (
                                    <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                                      {obj.name}
                                    </option>
                                  ))}
                                </>
                              )}
                            </select>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                  <div className={Styles.flexLayout}>
                    <div className={classNames('input-field-group include-error', archerIdError.length ? 'error' : '')}>
                      <label className={classNames('input-label')}>Archer ID</label>
                      <div>
                        <input
                          type="text"
                          className={classNames('input-field')}
                          id="archerId"
                          placeholder="Type here eg.[INFO-XXXXX]"
                          autoComplete="off"
                          maxLength={55}
                          defaultValue={archerId}
                          onChange={onArcherIdChange}
                        />
                        <span className={classNames('error-message', archerIdError.length ? '' : 'hide')}>
                          {archerIdError}
                        </span>
                      </div>
                    </div>
                    <div
                      className={classNames('input-field-group include-error', procedureIDError.length ? 'error' : '')}
                    >
                      <label className={classNames('input-label')}>Procedure ID</label>
                      <div>
                        <input
                          type="text"
                          className={classNames('input-field')}
                          id="procedureID"
                          placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                          autoComplete="off"
                          maxLength={55}
                          defaultValue={procedureID}
                          onChange={onProcedureIDChange}
                        />
                        <span className={classNames('error-message', procedureIDError.length ? '' : 'hide')}>
                          {procedureIDError}
                        </span>
                      </div>
                    </div>
                  </div>
                  {isPlayground && (
                    <div
                      className={classNames(
                        Styles.bucketNameInputField,
                        'input-field-group include-error',
                        departmentError ? 'error' : '',
                      )}
                    >
                      <div>
                        <div className={Styles.departmentTags}>
                          <Tags
                            title={'Department'}
                            max={1}
                            chips={department}
                            tags={departments}
                            setTags={(selectedTags) => {
                              const dept = selectedTags?.map((item) => item.toUpperCase());
                              setDepartment(dept);
                              setDepartmentError(false);
                            }}
                            isMandatory={true}
                            showMissingEntryError={departmentError}
                          />
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
              {!isPlayground && (
                <div className={Styles.flexLayout}>
                  <div
                    className={classNames(
                      Styles.bucketNameInputField,
                      'input-field-group include-error',
                      departmentError ? 'error' : '',
                    )}
                  >
                    <div>
                      <div>
                        <Tags
                          title={'Department'}
                          max={1}
                          chips={department}
                          tags={departments}
                          setTags={(selectedTags) => {
                            const dept = selectedTags?.map((item) => item.toUpperCase());
                            setDepartment(dept);
                            setDepartmentError(false);
                          }}
                          isMandatory={true}
                          showMissingEntryError={departmentError}
                        />
                      </div>
                    </div>
                  </div>
                  <div>
                    <div className={Styles.flexLayout}>
                      <div
                        className={classNames(
                          'input-field-group include-error',
                          classificationTypeError?.length ? 'error' : '',
                        )}
                      >
                        <label className={classNames('input-label')}>
                          Data Classification <sup>*</sup>
                        </label>
                        <div className={classNames('custom-select')}>
                          <select
                            id="classificationField"
                            defaultValue={classificationType}
                            required={true}
                            required-error={requiredError}
                            onChange={onClassificationChange}
                            value={classificationType}
                          >
                            <option id="classificationOption" value={0}>
                              Choose
                            </option>
                            {dataClassificationDropdown?.map((item) => (
                              <option id={item.id} key={item.id} value={item.name}>
                                {item.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className={classNames('error-message', classificationTypeError.length ? '' : 'hide')}>
                          {classificationTypeError}
                        </span>
                      </div>

                      <div className={classNames('input-field-group include-error')}>
                        <label className={classNames('input-label')}>
                          PII (Personally Identifiable Information) <sup>*</sup>
                        </label>
                        <div>
                          <label className={classNames('radio')}>
                            <span className="wrapper">
                              <input
                                type="radio"
                                className="ff-only"
                                value="true"
                                name="pii"
                                defaultChecked={PII === true}
                                onChange={onPIIChange}
                              />
                            </span>
                            <span className="label">Yes</span>
                          </label>
                          <label className={classNames('radio')}>
                            <span className="wrapper">
                              <input
                                type="radio"
                                className="ff-only"
                                value="false"
                                name="pii"
                                defaultChecked={PII === false}
                                onChange={onPIIChange}
                              />
                            </span>
                            <span className="label">No</span>
                          </label>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              <div>
                <div id="environmentContainer" className={classNames('input-field-group include-error')}>
                  <label className={classNames('input-label')}>
                    Environment<sup>*</sup>
                  </label>
                  <div>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="DHC-CaaS-AWS"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={environment === 'DHC-CaaS-AWS'}
                        />
                      </span>
                      <span className="label">DyP CaaS (AWS)</span>
                    </label>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="azure"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={false}
                          disabled={true}
                        />
                      </span>
                      <span className="label">Azure (Coming Soon)</span>
                    </label>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="extollo"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={false}
                          disabled={true}
                        />
                      </span>
                      <span className="label">eXtollo (Coming Soon)</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className={classNames(Styles.content)}>
            <div className={Styles.aiFormContent}>
              <span className={classNames('label')}>
                <p className={Styles.contentHeader}>a2a Agentic Recipe Config</p>
              </span>
              <div className={Styles.flexLayout}>
                <div
                  className={classNames('input-field-group include-error', modelSourceError?.length ? 'error' : '')}
                >
                  <label className={classNames('input-label')}>
                    Model Source <sup>*</sup>
                  </label>

                  <div className={classNames('custom-select')}>
                    <select
                      id="modelSourceField"
                      defaultValue={modelSource}
                      required={true}
                      required-error={requiredError}
                      onChange={onModelSourceChange}
                      value={modelSource}
                    >
                      <option id="modelSourceOption" value={0}>Choose</option>
                      <option value={'Azure'}>Azure openAI</option>
                      <option value={'Google'}>Google Gemini</option>
                    </select>
                  </div>
                  <span className={classNames('error-message', modelSourceError.length ? '' : 'hide')}>
                    {modelSourceError}
                  </span>
                </div>
                <div
                  className={classNames('input-field-group include-error', agentApiKeyError.length ? 'error' : '')}
                >
                  <label className={classNames('input-label')}>{modelSource === 'Azure' ? 'Azure OpenAI API key' : 'Google API key'}</label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field')}
                      id="apiKeyID"
                      placeholder="Type here..."
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={agentApiKey}
                      onChange={(e) => { setAgentApiKey(e.currentTarget.value); }}
                    />
                    <span className={classNames('error-message', agentApiKeyError.length ? '' : 'hide')}>
                      {agentApiKeyError}
                    </span>
                  </div>
                </div>
              </div>
              {modelSource === 'Azure' && (<div className={Styles.flexLayout}>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Azure openAI Endpoint</label>
                  <div>
                    <label className={classNames('chips')}>{Envs.AZURE_OPENAI_ENDPOINT}</label>
                  </div>
                </div>
                <div className={Styles.flexLayout}>
                  <div className={classNames('input-field-group')}>
                    <label className={classNames('input-label')}>Azure openAI API Version</label>
                    <div>
                      <label className={classNames('chips')}>2024-02-01</label>
                    </div>
                  </div>
                  <div className={classNames('input-field-group')}>
                    <label className={classNames('input-label')}>Azure openAI Deployment Name</label>
                    <div>
                      <label className={classNames('chips')}>gpt-4o</label>
                    </div>
                  </div>
                </div>
              </div>)}
              {modelSource === 'Google' && (<div className={Styles.flexLayout}>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Tool LLM Endpoint</label>
                  <div>
                    <label className={classNames('chips')}>{Envs.TOOL_LLM_URL}</label>
                  </div>
                </div>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Tool LLM Name</label>
                  <div>
                    <label className={classNames('chips')}>gemini-1.5-pro</label>
                  </div>
                </div>
              </div>)}
            </div>
          </div>
          <div className={classNames(Styles.content)}>
            <div className={Styles.aiFormContent}>
              <span className={classNames('label')}>
                <p className={Styles.contentHeader}>FCOS Agentic UI Config</p>
              </span>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Model Source</label>
                  <div>
                    <label className={classNames('chips')}>Google Gemini</label>
                  </div>
                </div>
                <div
                  className={classNames('input-field-group include-error', uiApiKeyError.length ? 'error' : '')}
                >
                  <label className={classNames('input-label')}>Google Gemini API key</label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field')}
                      id="uiApiKeyID"
                      placeholder="Type here..."
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={uiApiKey}
                      onChange={(e) => { setUiApiKey(e.currentTarget.value); }}
                    />
                    <span className={classNames('error-message', uiApiKeyError.length ? '' : 'hide')}>
                      {uiApiKeyError}
                    </span>
                  </div>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Google Gemini Endpoint</label>
                  <div>
                    <label className={classNames('chips')}>{Envs.GEMINI_ENDPOINT}</label>
                  </div>
                </div>
                <div className={classNames('input-field-group')}>
                  <label className={classNames('input-label')}>Google Gemini Model Name</label>
                  <div>
                    <label className={classNames('chips')}>gemini-2.5-flash</label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.createBtn}>
            <button
              className={'btn btn-tertiary'}
              type="button"
              onClick={onCreateAgent}
            >
              <span>Create AI Agent</span>
            </button>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default AiCodeSpaceForm;
