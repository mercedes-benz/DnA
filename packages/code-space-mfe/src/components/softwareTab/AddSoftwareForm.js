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

const AddSoftwareForm = ({ edit, software, onAddSoftware }) => {
  const [scriptContent, setScriptContent] = useState(edit ? software.additionalProperties : '');

  const methods = useForm({ 
    defaultValues: {
      softwareName: edit ? software.softwareName : '',
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
      softwareName: values?.softwareName?.trim(),
      additionalProperties: scriptContent,
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

  const handleEditSoftware = (values) => {
    ProgressIndicator.show();
    const data = {
      softwareName: values?.softwareName?.trim(),
      additionalProperties: scriptContent,
    };
    CodeSpaceApiClient.editSoftware(software?.id, data).then(() => {
      ProgressIndicator.hide();
      onAddSoftware();
      Notification.show('Software edited successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.data?.response?.errors?.[0]?.message | error?.response?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while editing software',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>{edit ? 'Edit Software' : 'Add Software'}</h3>
            <p>Enter following information to add!</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.softwareName ? 'error' : '')}>
                <label className={'input-label'}>
                  Name <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="softwareName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={100}
                  {...register('softwareName', { required: '*Missing entry', pattern: /^(?!\s*$)[a-zA-Z\d -]+$/ })}
                />
                <span className={'error-message'}>{errors?.softwareName?.message}{errors.softwareName?.type === 'pattern' && 'Environment name can be only alphanumeric characters and hyphens (-), special symbols and standalone spaces are not allowed.'}</span>
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
                edit ? handleEditSoftware(values) : handleAddSoftware(values);
              })}
            >
              {edit ? 'Edit Software' : 'Add Software'}
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default AddSoftwareForm;