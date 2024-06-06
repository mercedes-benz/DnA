import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useForm, FormProvider, Controller } from 'react-hook-form';
// styles
import Styles from './data-entry-users.scss';
import AddUser from 'dna-container/AddUser';
import DatePicker from 'dna-container/DatePicker';
import SelectBox from 'dna-container/SelectBox';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { dataEntryApi } from '../../apis/dataentry.api';
import { formatDateToISO } from '../../utilities/utils';

const DataEntryUsers = ({ user, surveyData, project }) => {
  const { id } = useParams();

  const methods = useForm({ 
    defaultValues: { 
      type: 'DnA',
      dataLakeDetails: '0',
      fillingInstructions: '',
      dueDate: new Date()
    }
  });
  const {
    register,
    handleSubmit,
    watch,
    control,
    formState: { errors },
  } = methods;

  const [dataEntryUsers, setDataEntryUsers] = useState([]);
  const [datalakeProjects, setDatalakeProjects] = useState([]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      dataEntryApi
        .getDatalakeProjects(0, 0)
        .then((res) => {
          setDatalakeProjects(res?.data?.data);
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching data lakehouse projects failed!',
            'alert',
          );
        });
  }, []);
  
  const getCollabarators = (collaborators) => {
    const collabarationData = {
      id: collaborators.shortId,
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      email: collaborators.email,
      department: collaborators.department,
      mobileNumber: collaborators.mobileNumber
    };

    let duplicateMember = false;
    duplicateMember = dataEntryUsers.filter((member) => member.id === collaborators.shortId)?.length
      ? true
      : false;
    const isCreator = id ? project?.createdBy.id === collaborators.shortId : user.id === collaborators.shortId;

    if (duplicateMember) {
      Notification.show('User Already Exist.', 'warning');
    } else if (isCreator) {
      Notification.show(
        `${collaborators.firstName} ${collaborators.lastName} is a creator. Creator can't be added as user.`,
        'warning',
      );
    } else {
      dataEntryUsers.push(collabarationData);
      setDataEntryUsers([...dataEntryUsers]);
    }
  };
  const removeDeUser = (index) => {
    const temp = dataEntryUsers.splice(index, 1);
    setDataEntryUsers(temp);
  };

  const handlePublish = (values) => {
    ProgressIndicator.show();
    const datalake = values.dataLakeDetails.split('@-@');
    const surveyDataTemp = surveyData(); 
    const data = {
      dataLakeDetails: {
        id: datalake[0],
        type: values.type,
        name: datalake[1],
        link: 'null'
      },
      fillingInstructions: values.fillingInstructions,
      dueDate: formatDateToISO(new Date(values.dueDate)),
      dataEntryUsers: dataEntryUsers,
      surveyData: surveyDataTemp.sheets['sheet-01'].cellData,
      id: project?.id,
      name: project?.name,
      tags: project?.tags,
      hasPii: project?.hasPii,
      archerId: project?.archerId,
      divisionId: project?.divisionId,
      division: project?.division,
      subDivisionId: project?.subDivisionId,
      subDivision: project?.subDivision,
      description: project?.description,
      department: project?.department,
      procedureId: project?.procedureId,
      termsOfUse: project?.termsOfUse,
      typeOfProject: project?.typeOfProject,
      dataClassification: project?.dataClassification,
      createdBy: project?.createdBy,
      createdOn: project?.createdOn,
      state: project?.state,
    }
    dataEntryApi.updateDataEntryProject(id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data Entry Project successfully published');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating data entry project',
        'alert',
      );
    });
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.container)}>
        <div className={Styles.header}>
          <h3>Search, Add and Send to Data Entry Users</h3>
          <p>Enter the information to start!</p>
        </div>
        <div className={Styles.flex}>
          <div className={Styles.col2}>
            <div
              className={classNames(
                'input-field-group include-error',
                errors?.type?.message ? 'error' : '',
              )}
            >
              <label className={'input-label'}>
                Type <sup>*</sup>
              </label>
              <div className={classNames('custom-select')}>
                <select
                  {...register('type', {
                    validate: (value) => value !== '0' || '*Missing entry'
                  })}
                >
                  <option value={'DnA'}>DnA Data Lakehouse</option>
                  <option value={'Fabric'}>Fabric (Coming Soon)</option>
                </select>
              </div>
              <span className={classNames('error-message', errors?.type?.message ? '' : 'hide')}>
                {errors?.type?.message}
              </span>
            </div>
          </div>
          <div className={Styles.col2}>
            <div
              className={classNames(
                'input-field-group include-error',
                errors?.dataLakeDetails?.message ? 'error' : '',
              )}
            >
              <label className={'input-label'}>
                Data Lakehouse Project <sup>*</sup>
              </label>
              <div className={classNames('custom-select')}>
                <select
                  id="classificationField"
                  {...register('dataLakeDetails', {
                    validate: (value) => value !== '0' || '*Missing entry'
                  })}
                >
                  <option value={'0'}>Choose</option>
                  <option value={'id@-@datalakename'}>Datalake</option>
                  {datalakeProjects?.map((item) => (
                    <option
                      id={item.id}
                      key={item.id}
                      value={`${item.id}@-@${item.projectName}`}
                    >
                      {item.projectName}
                    </option>
                  ))}

                </select>
              </div>
              <span className={classNames('error-message', errors?.dataLakeDetails?.message ? '' : 'hide')}>
                {errors?.dataLakeDetails?.message}
              </span>
            </div>
          </div>
          <div className={Styles.col}>
            <AddUser
              title={'User'} 
              getCollabarators={getCollabarators} 
              dagId={''} 
              isRequired={false} 
              isUserprivilegeSearch={false} 
            />
            <div className={Styles.deUserList}>
              {dataEntryUsers.length > 0 ? 
                <table>
                  <thead>
                    <tr>
                      <th className={Styles.id}>ID</th>
                      <th className={Styles.name}>Name</th>
                      <th className={Styles.action}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dataEntryUsers.map((dataEntryUser, index) => {
                      <tr>
                        <td>{dataEntryUser?.shortId}</td>
                        <td>{dataEntryUser?.firstName} {dataEntryUser?.lastName}</td>
                        <td>
                          <button className={classNames('btn', Styles.btnAction)} onClick={() => removeDeUser(index)}>
                            <i className="icon mbc-icon close thin"></i> Remove
                          </button>
                        </td>
                      </tr>
                    })}
                  </tbody>
                </table>
                : 
                <div className={Styles.noUsers}>
                  <p>No Data Entry users added</p>
                </div>
              }
            </div>
          </div>
          <div className={Styles.col}>
            <div className={classNames('input-field-group area')}>
              <label className="input-label" htmlFor="instructions">
                Filling Instructions
              </label>
              <textarea
                id="instructions"
                className={'input-field-area'}
                type="text"
                rows={50}
                placeholder="Type here"
                autoComplete="off"
                {...register('fillingInstructions')}
              />
            </div>
          </div>
          <div className={Styles.col2}>
            <div className={'input-field-group'}>
              <label htmlFor="dueDate" className="input-label">
                Due Date
              </label>
              <Controller
                control={control}
                name="dueDate"
                // rules={{
                //   validate: validateDate,
                // }}
                render={({ field }) => (
                  <DatePicker
                    label="Due Date"
                    value={watch('dueDate')}
                    name={field.name}
                    // minDate={minDate}
                    onChange={(value) => {
                      field.onChange(value);
                    }}
                  />
                )}
              />
            </div>
          </div>
        </div>
        <div className={Styles.footer}>
          <button
            className="btn btn-tertiary"
            onClick={handleSubmit(values => handlePublish(values))}
          >
            Publish
          </button>
        </div>
      </div>
    </FormProvider>
  );
}

export default DataEntryUsers;