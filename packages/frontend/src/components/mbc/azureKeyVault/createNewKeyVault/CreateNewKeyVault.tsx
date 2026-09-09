import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateNewKeyVault.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Tags from 'components/formElements/tags/Tags';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { ApiClient } from '../../../../services/ApiClient';
import TextBox from '../../shared/textBox/TextBox';
import { IKeyVault, IKeyVaultAccessLevel, IKeyVaultCollaborator, IKeyVaultPrincipal } from 'globals/types';

const ACCESS_LEVELS: IKeyVaultAccessLevel[] = ['Reading', 'Contributing'];

interface Props {
  edit?: boolean;
  project?: IKeyVault;
  setShowCreateModal?: () => void;
  getKeyVaultList?: () => void;
}

const CreateNewWorkspace = ({ edit, project, setShowCreateModal, getKeyVaultList }: Props) => {
  const [keyVaultName, setKeyVaultName] = useState(edit ? project?.keyVaultName || '' : '');
  const [keyVaultNameError, setKeyVaultNameError] = useState('');

  const [dataClassification, setDataClassification] = useState(edit ? project?.dataClassification || '' : '');
  const [dataClassificationError, setDataClassificationError] = useState('');

  const [department, setDepartment] = useState(edit && project?.department ? [project?.department] : []);
  const [departmentError, setDepartmentError] = useState(false);

  const [description, setDescription] = useState(edit ? project?.description || '' : '');
  const [descriptionError, setDescriptionError] = useState('');

  const [division, setDivision] = useState(project?.division ? project?.divisionId + '@-@' + project?.division : '0');
  const [divisionError, setDivisionError] = useState('');

  const [subDivision, setSubDivision] = useState(
    project?.subDivision ? project?.subDivisionId + '@-@' + project?.subDivision : '0',
  );

  const [hasPii, setHasPii] = useState(project?.hasPii || false);

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [collaborators, setCollaborators] = useState<IKeyVaultCollaborator[]>(project?.collaborators || []);
  const [principalSearch, setPrincipalSearch] = useState('');
  const [principalResults, setPrincipalResults] = useState<IKeyVaultPrincipal[]>([]);
  const [searching, setSearching] = useState(false);
  const [accessLevel, setAccessLevel] = useState<IKeyVaultAccessLevel>('Reading');

  const requiredError = '*Missing entry';
  const keyVaultNameErrorText = '*Key Vault Name should start with kv-';
  const keyVaultNameCriteriaErrorText =
    "*Key Vault Name must be 3-24 characters, start with a letter, end with a letter or digit, and not contain '_' or consecutive '-'";

  const getKeyVaultNameValidationError = (value: string) => {
    if (!value.length) {
      return requiredError;
    }

    if (!value.startsWith('kv-')) {
      return keyVaultNameErrorText;
    }

    const validFormat = /^[A-Za-z][A-Za-z0-9-]{1,22}[A-Za-z0-9]$/.test(value);
    if (!validFormat || value.includes('--')) {
      return keyVaultNameCriteriaErrorText;
    }

    return '';
  };

  const getDisplayErrorMessage = (message?: string) => {
    if (!message) {
      return 'Something went wrong.';
    }

    const cleanedMessage = message.replace(/\s*Follow this link for more information:[\s\S]*$/i, '').trim();
    return cleanedMessage || message;
  };

  useEffect(() => {
    SelectBox.defaultSetup();
    ProgressIndicator.show();
    CodeSpaceApiClient.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data || []);
        setDivisions(response[1] || []);
        setDepartments(response[2]?.data || []);
        edit && setDivision(project?.division ? project?.divisionId + '@-@' + project?.division : '0');
        edit && setDataClassification(project?.dataClassification ? project?.dataClassification : '0');
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
        Notification.show(err?.message || 'Something went wrong.', 'alert');
      });
  }, []);

  useEffect(() => {
    const divId = division.includes('@-@') ? division.split('@-@')[0] : division;
    if (divId && divId !== '0') {
      ProgressIndicator.show();
      ApiClient.getSubDivisions(divId)
        .then((res: any) => {
          setSubDivisions(res || []);
          edit && setSubDivision(project?.subDivision ? project?.subDivisionId + '@-@' + project?.subDivision : '0');
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        })
        .catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
  }, [division]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [collaborators]);

  const onKeyVaultNameChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    setKeyVaultName(currentValue);
    setKeyVaultNameError(getKeyVaultNameValidationError(currentValue));
  };

  const onClassificationChange = (e: any) => {
    const selectedOption = e.currentTarget.value;
    setDataClassification(selectedOption);
  };

  const onDescriptionChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    setDescription(currentValue);
    setDescriptionError(currentValue.length !== 0 ? '' : requiredError);
  };

  const onDivisionChange = (e: any) => {
    const selectedOption = e.currentTarget.value;
    setDivision(selectedOption);
  };

  const onSubDivisionChange = (e: any) => {
    const selectedOption = e.currentTarget.value;
    setSubDivision(selectedOption);
  };

  const onPIIChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    if (currentValue === 'true') {
      setHasPii(true);
    } else {
      setHasPii(false);
    }
  };

  const searchPrincipals = () => {
    const term = principalSearch.trim();
    if (term.length < 3) {
      setPrincipalResults([]);
      Notification.show('Please enter at least 3 characters to search.', 'warning');
      return;
    }
    setSearching(true);
    ApiClient.searchKeyVaultPrincipals(term)
      .then((results: IKeyVaultPrincipal[]) => {
        setSearching(false);
        setPrincipalResults(results || []);
        if (!results?.length) {
          Notification.show('No users, service principals or managed identities found.', 'warning');
        }
      })
      .catch(() => {
        setSearching(false);
        Notification.show('Unable to search Entra ID principals.', 'alert');
      });
  };

  const addCollaborator = (principal: IKeyVaultPrincipal) => {
    const identifier = principal.identifier || principal.mail || principal.appId || principal.displayName;
    if (!identifier) {
      Notification.show('Selected principal has no identifier and cannot be added as collaborator.', 'warning');
      return;
    }
    if (collaborators.some((item) => item.identifier?.toLowerCase() === identifier.toLowerCase())) {
      Notification.show('Collaborator Already Exist.', 'warning');
      return;
    }
    setCollaborators([
      ...collaborators,
      {
        identifier,
        objectId: principal.id,
        displayName: principal.displayName || identifier,
        kind: principal.kind,
        principalType: principal.principalType,
        accessLevel,
      },
    ]);
    setPrincipalResults([]);
    setPrincipalSearch('');
  };

  const onAccessLevelChange = (identifier: string, level: IKeyVaultAccessLevel) => {
    setCollaborators(
      collaborators.map((item) => (item.identifier === identifier ? { ...item, accessLevel: level } : item)),
    );
  };

  const onCollaboratorDelete = (identifier: string) => {
    setCollaborators(collaborators.filter((item) => item.identifier !== identifier));
  };

  const createKeyVault = () => {
    let formValid = true;
    const keyVaultNameValidationError = getKeyVaultNameValidationError(keyVaultName);
    if (keyVaultNameValidationError.length) {
      setKeyVaultNameError(keyVaultNameValidationError);
      formValid = false;
    }
    if (!description.length) {
      setDescriptionError(requiredError);
      formValid = false;
    }
    if (division === '0') {
      setDivisionError(requiredError);
      formValid = false;
    }
    if (!department.length) {
      setDepartmentError(true);
      formValid = false;
    }
    if (dataClassification === '0') {
      setDataClassificationError(requiredError);
      formValid = false;
    }
    if (formValid) {
      const formData = {
        data: {
          keyVaultName: keyVaultName,
          description: description,
          divisionId: division.split('@-@')[0],
          division: division.split('@-@')[1],
          subDivisionId: subDivision.split('@-@')[0],
          subDivision: subDivision.split('@-@')[1],
          department: department[0],
          dataClassification: dataClassification,
          hasPii: hasPii,
          collaborators: collaborators,
        },
      };
      ProgressIndicator.show();
      if (edit) {
        ApiClient.updateKeyVault(project?.id, formData)
          .then((res: any) => {
            ProgressIndicator.hide();
            const errors = res?.responses?.errors;
            if (errors?.length) {
              Notification.show(getDisplayErrorMessage(errors[0]?.message), 'alert');
              return;
            }
            Notification.show('Key Vault updated successfully.');
            setShowCreateModal();
            getKeyVaultList();
          })
          .catch((err: any) => {
            ProgressIndicator.hide();
            Notification.show(getDisplayErrorMessage(err?.message), 'alert');
          });
      } else {
        ApiClient.createKeyVault(formData)
          .then((res: any) => {
            ProgressIndicator.hide();
            const errors = res?.responses?.errors;
            if (errors?.length) {
              Notification.show(getDisplayErrorMessage(errors[0]?.message), 'alert');
              return;
            }
            Notification.show('Key Vault created successfully.');
            setShowCreateModal();
            getKeyVaultList();
          })
          .catch((err: any) => {
            ProgressIndicator.hide();
            Notification.show(getDisplayErrorMessage(err?.message), 'alert');
          });
      }
    }
  };

  return (
    <>
      <div className={Styles.newWorkSpacePanel}>
        {!edit ? (
          <>
            <div className={Styles.addicon}> &nbsp; </div>
            <h3>Hello, Create your Key Vault</h3>
            <p>Enter the information to start creating!</p>
          </>
        ) : (
          <>
            <div className={Styles.editicon}>
              <i className="icon mbc-icon edit small " />
            </div>
            <h3>Edit Lean Governance</h3>
          </>
        )}
        <div className={Styles.flexLayout}>
          {edit ? (
            <div>
              <label className={classNames(Styles.nameLabel)}>Key Vault Name</label>
              <div>
                <label className={classNames('chips')}>{project?.keyVaultName}</label>
              </div>
            </div>
          ) : (
            <div>
              <TextBox
                type="text"
                controlId={'keyVaultNameInput'}
                labelId={'keyVaultNameLabel'}
                label={'Key Vault Name'}
                placeholder={'eg: kv-mykeyvault'}
                value={keyVaultName}
                errorText={keyVaultNameError}
                required={true}
                maxLength={24}
                onChange={onKeyVaultNameChange}
              />
            </div>
          )}
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
        </div>
        <div className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '')}>
          <label id="description" className="input-label" htmlFor="description">
            Description <sup>*</sup>
          </label>
          <textarea
            id="description"
            className="input-field-area"
            defaultValue={description}
            required={true}
            required-error={requiredError}
            onChange={onDescriptionChange}
            rows={50}
          />
          <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>{descriptionError}</span>
        </div>
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
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
            <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>{divisionError}</span>
          </div>
          <div className={classNames('input-field-group include-error')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>Sub Division</label>
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
        <div className={Styles.flexLayout}>
          <div
            className={classNames('input-field-group include-error', dataClassificationError?.length ? 'error' : '')}
          >
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Data Classification <sup>*</sup>
            </label>
            <div className={classNames('custom-select')}>
              <select
                id="classificationField"
                defaultValue={dataClassification}
                required={true}
                required-error={requiredError}
                onChange={onClassificationChange}
                value={dataClassification}
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
            <span className={classNames('error-message', dataClassificationError?.length ? '' : 'hide')}>
              {dataClassificationError}
            </span>
          </div>
          <div className={classNames('input-field-group include-error')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              PII (Personally Identifiable Information) <sup>*</sup>
            </label>
            <div className={Styles.pIIField}>
              <label className={classNames('radio')}>
                <span className="wrapper">
                  <input
                    type="radio"
                    className="ff-only"
                    value="true"
                    name="pii"
                    defaultChecked={hasPii === true}
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
                    defaultChecked={hasPii === false}
                    onChange={onPIIChange}
                  />
                </span>
                <span className="label">No</span>
              </label>
            </div>
          </div>
        </div>
        <div className={classNames('input-field-group include-error')}>
          <label htmlFor="userId" className="input-label">
            Find and add the collaborators you want to share this Key Vault with (Optional)
          </label>
          <div className={Styles.collaboratorSection}>
            <div className={Styles.collaboratorSectionList}>
              <div className={Styles.collaboratorSectionListAdd}>
                <div className={Styles.principalSearch}>
                  <div className={classNames('input-field-group')}>
                    <label className="input-label">Search Entra ID</label>
                    <input
                      type="text"
                      className="input-field"
                      value={principalSearch}
                      placeholder="Search users, service principals or managed identities"
                      onChange={(event) => setPrincipalSearch(event.currentTarget.value)}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter') {
                          searchPrincipals();
                        }
                      }}
                    />
                  </div>
                  <div className={classNames('input-field-group')}>
                    <label className="input-label">Access</label>
                    <div className={classNames('custom-select')}>
                      <select
                        id="accessLevelField"
                        value={accessLevel}
                        onChange={(event) => setAccessLevel(event.currentTarget.value as IKeyVaultAccessLevel)}
                      >
                        {ACCESS_LEVELS.map((level) => (
                          <option key={level} value={level}>
                            {level}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <button className="btn btn-primary" type="button" onClick={searchPrincipals} disabled={searching}>
                    Search
                  </button>
                </div>
                {principalResults.length > 0 && (
                  <div className={classNames('mbc-scroll', Styles.principalResults)}>
                    {principalResults.map((principal) => (
                      <button
                        className={Styles.principalResult}
                        type="button"
                        key={principal.id}
                        onClick={() => addCollaborator(principal)}
                      >
                        <span>{principal.displayName || principal.identifier}</span>
                        <span>{principal.identifier}</span>
                        <span>{principal.kind}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
              <div className={Styles.collaboratorList}>
                {collaborators?.length > 0 ? (
                  <>
                    <div className={Styles.collaboratorTitle}>
                      <div className={Styles.collaboratorTitleCol}>Identifier</div>
                      <div className={Styles.collaboratorTitleCol}>Name</div>
                      <div className={Styles.collaboratorTitleCol}>Type</div>
                      <div className={Styles.collaboratorTitleCol}>Access</div>
                      <div className={Styles.collaboratorTitleCol}></div>
                    </div>
                    <div className={classNames('mbc-scroll', Styles.collaboratorContent)}>
                      {collaborators.map((collaborator) => (
                        <div key={collaborator.identifier} className={Styles.collaboratorContentRow}>
                          <div className={Styles.collaboratorTitleCol}>{collaborator.identifier}</div>
                          <div className={Styles.collaboratorTitleCol}>
                            {collaborator.displayName || collaborator.identifier}
                          </div>
                          <div className={Styles.collaboratorTitleCol}>{collaborator.kind}</div>
                          <div className={Styles.collaboratorTitleCol}>
                            <div className={classNames('custom-select', Styles.collaboratorAccessSelect)}>
                              <select
                                value={collaborator.accessLevel || 'Reading'}
                                onChange={(event) =>
                                  onAccessLevelChange(
                                    collaborator.identifier,
                                    event.currentTarget.value as IKeyVaultAccessLevel,
                                  )
                                }
                              >
                                {ACCESS_LEVELS.map((level) => (
                                  <option key={level} value={level}>
                                    {level}
                                  </option>
                                ))}
                              </select>
                            </div>
                          </div>
                          <div className={Styles.collaboratorTitleCol}>
                            <button
                              className={classNames('btn btn-primary', Styles.deleteBtn)}
                              type="button"
                              aria-label={`Remove ${collaborator.identifier}`}
                              onClick={() => onCollaboratorDelete(collaborator.identifier)}
                            >
                              <i className="icon delete" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                ) : (
                  <div className={Styles.collaboratorSectionEmpty}>
                    <p>No collaborator added.</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.newCodeSpaceBtn}>
          <button className={' btn btn-tertiary '} onClick={createKeyVault}>
            {edit ? 'Update' : 'Create'}
          </button>
        </div>
      </div>
    </>
  );
};

export default CreateNewWorkspace;
