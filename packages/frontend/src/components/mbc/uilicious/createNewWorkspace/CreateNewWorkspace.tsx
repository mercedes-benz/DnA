import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateNewWorkspace.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Tags from 'components/formElements/tags/Tags';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { ApiClient } from '../../../../services/ApiClient';

export interface leanGovernance {
  costCenter?: string;
  dataClassification?: string;
  department?: string;
  description?: string;
  division?: string;
  divisionId?: string;
  hasPii?: boolean;
  internalOrder?: string;
  subDivision?: string;
  subDivisionId?: string;
}

interface Props {
  project?: leanGovernance;
  edit?: boolean;
  accountId?: string;
  setShowCreateModal?: () => void;
  getWorkspaceList?: () => void;
}

const CreateNewWorkspace = ({ project, edit, accountId, setShowCreateModal, getWorkspaceList }: Props) => {

  const [costCenter, setCostCenter] = useState(project?.costCenter || '');

  const [dataClassification, setDataClassification] = useState(project?.dataClassification || '');
  const [dataClassificationError, setDataClassificationError] = useState('');

  const [department, setDepartment] = useState(edit && project?.department ? [project?.department] : []);
  const [departmentError, setDepartmentError] = useState(false);

  const [description, setDescription] = useState(project?.description || '');
  const [descriptionError, setDescriptionError] = useState('');

  const [division, setDivision] = useState(project?.division ? project?.divisionId + '@-@' + project?.division : '0');
  const [divisionError, setDivisionError] = useState('');

  const [subDivision, setSubDivision] = useState(project?.subDivision ? project?.subDivisionId + '@-@' + project?.subDivision : '0');

  const [hasPii, setHasPii] = useState(project?.hasPii || false);

  const [internalOrder, setInternalOrder] = useState(project?.internalOrder || '');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);

  const requiredError = '*Missing entry';

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
        }).catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
  }, [division]);

  const onClassificationChange = (e: any) => {
    const selectedOption = e.currentTarget.value;
    setDataClassification(selectedOption);
  };

  const onDescriptionChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    setDescription(currentValue);
    setDescriptionError(currentValue.length !== 0 ? '' : requiredError);
  };

  const onCostCenterChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    setCostCenter(currentValue);
  };

  const onInternalOrderChange = (e: any) => {
    const currentValue = e.currentTarget.value;
    setInternalOrder(currentValue);
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

  const saveLeanGovernance = () => {
    let formValid = true;
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
        accountId: accountId || '',
        leanGovernance: {
          costCenter: costCenter,
          internalOrder: internalOrder,
          description: description,
          divisionId: division.split('@-@')[0],
          division: division.split('@-@')[1],
          subDivisionId: subDivision.split('@-@')[0],
          subDivision: subDivision.split('@-@')[1],
          department: department[0],
          dataClassification: dataClassification,
          hasPii: hasPii,
        },
      };
      ProgressIndicator.show();
      ApiClient.updateUiliciousWorkspace(formData)
        .then(() => {
          ProgressIndicator.hide();
          Notification.show('Lean Governance details saved successfully.');
          setShowCreateModal();
          getWorkspaceList();
        })
        .catch((err: any) => {
          ProgressIndicator.hide();
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        });
    }
  };

  const createWorkSpace = () => {
    let formValid = true;
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
        leanGovernance: {
          costCenter: costCenter,
          internalOrder: internalOrder,
          description: description,
          divisionId: division.split('@-@')[0],
          division: division.split('@-@')[1],
          subDivisionId: subDivision.split('@-@')[0],
          subDivision: subDivision.split('@-@')[1],
          department: department[0],
          dataClassification: dataClassification,
          hasPii: hasPii,
        },
      };
      ProgressIndicator.show();
      ApiClient.createUiliciousWorkspace(formData)
        .then(() => {
          ProgressIndicator.hide();
          Notification.show('Lean Governance details saved successfully.');
          setShowCreateModal();
          getWorkspaceList();
        })
        .catch((err: any) => {
          ProgressIndicator.hide();
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        });
    }
  };

    return (
      <>
        <div className={Styles.newWorkSpacePanel}>
          {!edit ? (
            <>
              <div className={Styles.addicon}> &nbsp; </div>
              <h3>Hello, Create your Work Space</h3>
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
            <div
              className={classNames(
                'input-field-group include-error',
                dataClassificationError?.length ? 'error' : '',
              )}
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
          <div
            className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '')}
          >
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
            <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
              {descriptionError}
            </span>
          </div>
          <div className={Styles.flexLayout}>
            <div className={classNames('input-field-group')}>
              <label className={classNames(Styles.inputLabel, 'input-label')}>Cost Center</label>
              <div>
                <input
                  type="text"
                  className={classNames('input-field', Styles.projectNameField)}
                  id="archerId"
                  placeholder="Type here..."
                  autoComplete="off"
                  maxLength={55}
                  defaultValue={costCenter}
                  onChange={onCostCenterChange}
                />
              </div>
            </div>
            <div className={classNames('input-field-group')}>
              <label className={classNames(Styles.inputLabel, 'input-label')}>Internal Order</label>
              <div>
                <input
                  type="text"
                  className={classNames('input-field', Styles.projectNameField)}
                  id="internalOrder"
                  placeholder="Type here..."
                  autoComplete="off"
                  maxLength={55}
                  defaultValue={internalOrder}
                  onChange={onInternalOrderChange}
                />
              </div>
            </div>
          </div>
          <div className={Styles.flexLayout}>
            <div
              className={classNames('input-field-group include-error',
                divisionError.length ? 'error' : '',
              )}
            >
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
                    )
                  })}
                </select>
              </div>
              <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                {divisionError}
              </span>
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
          <div className={Styles.newCodeSpaceBtn}>
            <button className={' btn btn-tertiary '} onClick={edit ? saveLeanGovernance : createWorkSpace}>
              {edit ? 'Save' : 'Create'}
            </button>
          </div>
        </div>
      </>
    );
  };

  export default CreateNewWorkspace;
