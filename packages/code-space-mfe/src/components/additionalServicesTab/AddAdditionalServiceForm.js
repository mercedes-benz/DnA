import classNames from 'classnames';
import React, { useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
// styles
import Styles from './add-additional-service-form.scss';
// App components
import AceEditor from 'react-ace';
//import theme
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/mode-yaml';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const initialContent = {
  args: [
    'string'
  ],
  env: [
    {
      name: 'string',
      value: 'string'
    }
  ],
  image: 'string',
  imagePullPolicy: 'string',
  name: 'string',
  ports: [
    {
      containerPort: 0,
      protocol: 'string'
    }
  ],
  securityContext: {
    runAsUser: 0
  },
  volumeMounts: [
    {
      mountPath: 'string',
      name: 'string'
    }
  ]
};

const AddAdditionalServiceForm = ({ edit, additionalService, onAddAdditionalService }) => {
  const [scriptContent, setScriptContent] = useState(edit ? JSON.stringify(additionalService.additionalProperties, null, '\t') : JSON.stringify(initialContent, null, '\t'));

  const methods = useForm({ 
    defaultValues: {
      serviceName: edit ? additionalService?.serviceName : '',
      version: edit ? additionalService?.version : '',
    }
  });
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const handleAddAdditionalService = (values) => {
    ProgressIndicator.show();
    const data = {
      serviceName: values?.serviceName?.trim(),
      version: values?.version?.trim(),
      additionalProperties: JSON.parse(scriptContent),
    };
    CodeSpaceApiClient.addAdditionalService(data).then(() => {
      ProgressIndicator.hide();
      onAddAdditionalService();
      Notification.show('Additional Service added successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.data?.response?.errors?.[0]?.message | error?.response?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while adding additional service',
        'alert',
      );
    });
  };

  const handleEditAdditionalService = (values) => {
    ProgressIndicator.show();
    const data = {
      serviceName: values?.serviceName?.trim(),
      version: values?.version?.trim(),
      additionalProperties: JSON.parse(scriptContent),
    };
    CodeSpaceApiClient.editAdditionalService(additionalService?.id, data).then(() => {
      ProgressIndicator.hide();
      onAddAdditionalService();
      Notification.show('Additional Service edited successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.data?.response?.errors?.[0]?.message | error?.response?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while editing additional service',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>{edit ? 'Edit' : 'Add'} Additional Service</h3>
            <p>Enter following information to add!</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.serviceName ? 'error' : '')}>
                <label className={'input-label'}>
                  Service Name <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="serviceName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={100}
                  {...register('serviceName', { required: '*Missing entry', pattern: /^(?!\s*$)[a-zA-Z\d -]+$/ })}
                />
                <span className={'error-message'}>{errors?.serviceName?.message}{errors.serviceName?.type === 'pattern' && 'Service name can be only alphanumeric characters and hyphens (-), special symbols and standalone spaces are not allowed.'}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.version ? 'error' : '')}>
                <label className={'input-label'}>
                  Version <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="version"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={100}
                  {...register('version', { required: '*Missing entry' })}
                />
                <span className={'error-message'}>{errors?.version?.message}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Additional Properties <sup>*</sup>
                </label>
                <div>
                  <AceEditor
                    width="100%"
                    placeholder="Type here"
                    name="writeScript"
                    mode={'yaml'}
                    theme="solarized_dark"
                    fontSize={16}
                    showPrintMargin={false}
                    showGutter={false}
                    highlightActiveLine={true}
                    value={scriptContent}
                    onChange={(value) => setScriptContent(value)}
                    style={{
                      height: '65vh',
                      backgroundColor: ' #1c2026'
                    }}
                    setOptions={{
                      useWorker: false,
                      enableBasicAutocompletion: true,
                      enableLiveAutocompletion: false,
                      enableSnippets: false,
                      showLineNumbers: true,
                      tabSize: 2,
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                edit ? handleEditAdditionalService(values) : handleAddAdditionalService(values);
              })}
            >
              { edit ? 'Edit Additional Service' : 'Add Additional Service' }
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default AddAdditionalServiceForm;