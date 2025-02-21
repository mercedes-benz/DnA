import classNames from 'classnames';
import React, { useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
// styles
import Styles from './add-software-form.scss';
// App components
import AceEditor from 'react-ace';
//import theme
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/mode-yaml';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const AddSoftwareForm = ({ onAddSoftware }) => {
  const [scriptContent, setScriptContent] = useState('');

  const methods = useForm({ 
    defaultValues: {
      name: '',
    }
  });
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const handleAddSoftware = (values) => {
    ProgressIndicator.show();
    const data = {
      name: values?.name?.trim(),
      script: scriptContent,
    };
    CodeSpaceApiClient.addSoftware(data).then(() => {
      ProgressIndicator.hide();
      onAddSoftware();
      Notification.show('Software added successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.data?.response?.errors?.[0]?.message | error?.response?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while adding software',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>Add Software</h3>
            <p>Enter following information to add!</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={'input-label'}>
                  Name <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="name"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={100}
                  {...register('name', { required: '*Missing entry', pattern: /^(?!\s*$)[a-zA-Z\d -]+$/ })}
                />
                <span className={'error-message'}>{errors?.name?.message}{errors.name?.type === 'pattern' && 'Environment name can be only alphanumeric characters and hyphens (-), special symbols and standalone spaces are not allowed.'}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Script <sup>*</sup>
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
                handleAddSoftware(values);
              })}
            >
              Add Software
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default AddSoftwareForm;