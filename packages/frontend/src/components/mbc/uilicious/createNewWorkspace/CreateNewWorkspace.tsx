import React, { useEffect, useState } from 'react';
import classNames from 'classnames';
import Styles from './CreateNewWorkspace.scss';
import { useForm, FormProvider } from 'react-hook-form';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Tags from 'components/formElements/tags/Tags';
import { IDepartment, ITag } from 'globals/types';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

export interface WorkspaceFormModel {
  workspaceId?: string;
  workspaceName?: string;
  description?: string;
  createdBy?: string;
  createdAt?: string;
  role?: 'Owner' | 'Collaborator' | 'Viewer';
  projectStatus?: 'ACTIVE' | 'CREATE_REQUESTED' | 'UPDATE_REQUESTED' | 'INACTIVE';

  internalOrder?: string;
  costCenter?: string;
  division?: string;
  subdivision?: string;
  department?: string;
  dataClassification?: string;
  pii?: string | boolean;
}

interface Props {
  project?: WorkspaceFormModel;
  edit?: boolean;
  onSave?: (workspace: WorkspaceFormModel) => void;
  onCancel?: () => void;
  departmentTags?: IDepartment[];
}

const defaultDepartmentTags: ITag[] = [
  { id: '1', name: 'IT' },
  { id: '2', name: 'ITH/IG' },
  { id: '3', name: 'IT/IP' },
];

const CreateNewWorkspace: React.FC<Props> = ({ project, edit = false, onSave, onCancel, departmentTags = [] }) => {
  const [selectedDepartmentTags, setSelectedDepartmentTags] = useState<string[]>([]);

  const methods = useForm<WorkspaceFormModel>({
    defaultValues: {
      workspaceName: '',
      description: '',
      internalOrder: '',
      costCenter: '',
      division: '0',
      subdivision: '0',
      department: '0',
      dataClassification: '0',
      pii: undefined,
    },
  });

  const { register, handleSubmit, reset, formState: { errors } } = methods;

  useEffect(() => {
    if (edit && project) {
      reset({
        workspaceName: project.workspaceName || '',
        description: project.description || '',
        internalOrder: project.internalOrder || '',
        costCenter: project.costCenter || '',
        division: project.division || '0',
        subdivision: project.subdivision || '0',
        department: project.department || '0',
        dataClassification: project.dataClassification || '0',
        pii: typeof project.pii !== 'undefined' ? project.pii : undefined,
      });
      setSelectedDepartmentTags(project.department ? [project.department] : []);
    } else {
      reset({
        workspaceName: '',
        description: '',
        internalOrder: '',
        costCenter: '',
        division: '0',
        subdivision: '0',
        department: '0',
        dataClassification: '0',
        pii: undefined,
      });
      setSelectedDepartmentTags([]);
    }
  }, [project, edit, reset]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const setDepartment = (arr: string[]) => {
    const departmentValue = arr?.map((item) => item.toUpperCase())[0];
    setSelectedDepartmentTags(arr);
    // Update the form value
    methods.setValue('department', departmentValue || '0');
  };

  const submit = (values: WorkspaceFormModel) => {
    ProgressIndicator.show();
    try {
      const newWorkspace: WorkspaceFormModel = {
        ...values,
        workspaceId: edit ? project?.workspaceId : `WS-${Date.now()}`,
        createdAt: edit ? project?.createdAt : new Date().toISOString(),
        createdBy: edit ? project?.createdBy : 'Current User', // Replace with actual user when available
        role: edit ? project?.role : 'Owner',
        projectStatus: 'ACTIVE',
      };

      Notification.show(edit ? 'Workspace updated' : 'Workspace created');
      if (typeof onSave === 'function') onSave(newWorkspace);
    } catch (err) {
      Notification.show('Error creating workspace', 'alert');
    } finally {
      ProgressIndicator.hide();
    }
  };

  return (
    <FormProvider {...methods}>
      <div className={Styles.createWorkspaceWrapper}>
        <div className={Styles.flexLayout}>

          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Workspace Name <sup>*</sup>
            </label>
            <input
              className="input-field"
              {...register('workspaceName', { required: '*Missing entry', maxLength: 100 })}
              readOnly={!!edit}
            />
            <span className={classNames('error-message', errors.workspaceName ? '' : 'hide')}>
              {errors.workspaceName?.message}
            </span>
          </div>

          <div className="input-field-group">
            <Tags
              title={'Department'}
              max={1}
              chips={selectedDepartmentTags?.map((department) => department?.toUpperCase())}
              tags={departmentTags.length > 0 ? departmentTags : defaultDepartmentTags}
              setTags={setDepartment}
              isMandatory={true}
              showMissingEntryError={!!errors.department}
            />
            <input
              type="hidden"
              {...register('department', { 
                validate: (v) => {
                  return selectedDepartmentTags.length > 0 || '*Missing entry';
                }
              })}
            />
          </div>
        </div>

        <div className="input-field-group" style={{ width: '100%', marginTop: '10px' }}>
          <label className={classNames(Styles.inputLabel, 'input-label')}>Description</label>
          <textarea
            className={classNames('input-field', Styles.largeTextBox)}
            {...register('description')}
            rows={4}
          />
        </div>

        <div className={Styles.flexLayout} style={{ marginTop: '16px' }}>
          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>Internal Order</label>
            <input
              className="input-field"
              {...register('internalOrder')}
              readOnly={!!edit}
            />
          </div>

          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>Cost Center</label>
            <input
              className="input-field"
              {...register('costCenter')}
            />
          </div>
        </div>

        <div className={Styles.flexLayout}>
          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Division <sup>*</sup>
            </label>
            <div className={classNames('custom-select')}>
              <select {...register('division', { required: '*Missing entry' })}>
                <option value="0">Choose</option>
                <option value="R&D">R&D</option>
                <option value="Engineering">Engineering</option>
                <option value="Support">Support</option>
              </select>
            </div>
            <span className={classNames('error-message', errors.division ? '' : 'hide')}>
              {errors.division?.message}
            </span>
          </div>

          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Subdivision <sup>*</sup>
            </label>
            <div className={classNames('custom-select')}>
              <select {...register('subdivision', { required: '*Missing entry' })}>
                <option value="0">Choose</option>
                <option value="Software">Software</option>
                <option value="Hardware">Hardware</option>
                <option value="Testing">Testing</option>
              </select>
            </div>
            <span className={classNames('error-message', errors.subdivision ? '' : 'hide')}>
              {errors.subdivision?.message}
            </span>
          </div>
        </div>

        {/* Data Classification + PII */}
        <div className={Styles.flexLayout}>
          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Data Classification <sup>*</sup>
            </label>
            <div className={classNames('custom-select')}>
              <select {...register('dataClassification', { required: '*Missing entry' })}>
                <option value="0">Choose</option>
                <option value="Confidential">Confidential</option>
                <option value="Restricted">Restricted</option>
                <option value="Internal">Internal</option>
                <option value="Public">Public</option>
              </select>
            </div>
            <span className={classNames('error-message', errors.dataClassification ? '' : 'hide')}>
              {errors.dataClassification?.message}
            </span>
          </div>

          <div className="input-field-group">
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              PII <sup>*</sup>
            </label>
            <div className={Styles.pIIField}>
              <label className="radio">
                <span className="wrapper">
                  <input type="radio" value="true" {...register('pii', { required: '*Missing entry' })} />
                </span>
                <span className="label">Yes</span>
              </label>
              <label className="radio">
                <span className="wrapper">
                  <input type="radio" value="false" {...register('pii', { required: '*Missing entry' })} />
                </span>
                <span className="label">No</span>
              </label>
            </div>
            <span className={classNames('error-message', errors.pii ? '' : 'hide')}>
              {errors.pii?.message}
            </span>
          </div>
        </div>

        <div className={Styles.newCodeSpaceBtn}>
          <button className={'btn btn-tertiary'} type="button" onClick={handleSubmit(submit)}>
            {edit ? 'Save Project' : 'Create Project'}
          </button>

          <button
            className={'btn btn-secondary'}
            type="button"
            onClick={() => {
              if (typeof onCancel === 'function') onCancel();
            }}
            style={{ marginLeft: 8 }}
          >
            Cancel
          </button>
        </div>
      </div>
    </FormProvider>
  );
};

export default CreateNewWorkspace;
