import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import { useParams } from 'react-router-dom';
import Styles from './run-parameters-form.scss';
// Container components
import SelectBox from 'dna-container/SelectBox';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import { chronosApi } from '../../../apis/chronos.api';

const RunParametersForm = () => {
  const { register, resetField, formState: { errors } } = useFormContext({defaultValues: {
                                                                          hierarchy: '',
                                                                          runOnPowerfulMachines: false
                                                                        }});
  const frequencyTooltipContent = 'Please select a frequency for your data in the field below.\n Make sure the datetimes in the first column of the data you upload matches the frequency selected here.\n If your data has no inherent frequency or the frequency is not available in the list, select "No frequency".\n In this case, the first column of your data should contain sortable indices like [1, 2, 3...].';
  const forecastHorizonTooltipContent = 'Select how many data points in the future the forecast should predict.\n Note that this number should not be more than 1/5th the length of your existing data, ideally less.\n Also, forecasting gets less precise over time, so try to not predict too many points in the future.';

  const [configurationFiles, setConfigurationFiles] = useState([]);
  const [expertView, setExpertView] = useState(false);

  const { id: projectId } = useParams();

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const toggleExpertView = () => {
    setExpertView(!expertView);
    resetField('hierarchy', {defaultValue: ''});
    resetField('runOnPowerfulMachines', {defaultValue: false});
    SelectBox.defaultSetup();
  }

  useEffect(() => {
    chronosApi.getConfigurationFiles(projectId).then((res) => {
      const bucketObjects = res.data.data.bucketObjects ? [...res.data.data.bucketObjects] : [];
      // const bucketObjects = configFiles.data.bucketObjects ? [...configFiles.data.bucketObjects] : [];
      bucketObjects.sort((a, b) => {
        let fa = a.objectName.toLowerCase(),
            fb = b.objectName.toLowerCase();
        if (fa < fb) {
          return -1;
        }
        if (fa > fb) {
          return 1;
        }
        return 0;
      });
      const filteredConfigFiles = bucketObjects.filter(file => file.objectName === 'chronos-core/configs/default_config.yml');
      if(filteredConfigFiles.length === 1) {
        bucketObjects.sort((a, b) => {
          let fa = a.objectName.toLowerCase(),
              fb = b.objectName.toLowerCase();
          const first = 'chronos-core/configs/default_config.yml';
          return fa == first ? -1 : fb == first ? 1 : 0;
        });
      }
      setConfigurationFiles(bucketObjects);
      SelectBox.defaultSetup();
    }).catch(error => {
      if(error?.response?.data?.errors[0]?.message) {
        Notification.show(error?.response?.data?.errors[0]?.message, 'alert');
      } else {
        Notification.show(error.message, 'alert');
      }
    });
  }, [projectId]);
  
  useEffect(() => {
    expertView && SelectBox.defaultSetup();
  }, [expertView]);
  
  return (
    <div className={Styles.wrapper}>
      <div className={Styles.firstPanel}>
        <h3>Run Parameters</h3>
      <div className={Styles.infoIcon}>
        <label className="switch">
          <span className="label" style={{ marginRight: '5px' }}>
            Enable Expert View
          </span>
          <span className="wrapper">
            <input
              value={expertView}
              type="checkbox"
              className="ff-only"
              onChange={toggleExpertView}
              checked={expertView}
              maxLength={63}
            />
          </span>
        </label>
      </div>
      <div className={Styles.formWrapper}>
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group include-error', errors.runName ? 'error' : '')}>
            <label id="runNameLabel" htmlFor="runNameInput" className="input-label">
              Run Name
            </label>
            <input
              {...register('runName', { pattern: /^[a-z0-9-.]+$/ })}
              type="text"
              className="input-field"
              id="runNameInput"
              maxLength={55}
              placeholder="Type here"
              autoComplete="off"
            />
            <span className={classNames('error-message')}>{errors.runName?.type === 'pattern' && 'Run names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).'}</span>
          </div>
          <div className={Styles.configurationContainer}>
            <div
              className={classNames(
                `input-field-group include-error ${errors?.configurationFile ? 'error' : ''}`,
              )}
            >
              <label id="configurationLabel" htmlFor="configurationField" className="input-label">
                Configuration File <sup>*</sup>
              </label>
              <div className="custom-select" 
                // onBlur={() => trigger('configurationFile')}
                >
                <select
                  id="configurationField"
                  {...register('configurationFile', {
                    required: '*Missing entry',
                    validate: (value) => value !== '0' || '*Missing entry',
                  })}
                >
                  {
                    configurationFiles.length === 0 ? (
                      <option id="configurationOption" value={0}>
                        None
                      </option>
                      ) : (
                        <>
                          {configurationFiles.map((file) => (
                              <option key={file.objectName} value={file.objectName}>
                                {file.objectName.split("/")[2]}
                              </option>
                          ))}
                        </>
                      )
                  }
                </select>
              </div>
              <span className={classNames('error-message')}>{errors?.configurationFile?.message}</span>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div className={Styles.frequencyContainer}>
            <div
              className={classNames(
                `input-field-group include-error ${errors?.frequency ? 'error' : ''}`,
                Styles.tooltipIcon,
              )}
            >
              <label id="frequencyLabel" htmlFor="frequencyField" className="input-label">
                Frequency <sup>*</sup>
                <i className="icon mbc-icon info" tooltip-data={frequencyTooltipContent} />
              </label>
              <div className="custom-select" 
                // onBlur={() => trigger('frequency')}
                >
                <select
                  id="frequencyField"
                  {...register('frequency', {
                    required: '*Missing entry',
                    validate: (value) => value !== '0' || '*Missing entry',
                  })}
                >
                  <option id="frequencyOption" value={0}>
                    Choose
                  </option>
                  <option value={'Daily'}>Daily</option>
                  <option value={'Weekly'}>Weekly</option>
                  <option value={'Monthly'}>Monthly</option>
                  <option value={'Yearly'}>Yearly</option>
                  <option value={'No_Frequency'}>No Frequency</option>
                </select>
              </div>
              <span className={classNames('error-message')}>{errors?.frequency?.message}</span>
            </div>
          </div>
          <div className={Styles.forecastHorizonContainer}>
            <div className={classNames('input-field-group include-error', errors.forecastHorizon ? 'error' : '', Styles.tooltipIcon)}>
              <label id="forecastHorizonLabel" htmlFor="forecastHorizonField" className="input-label">
                Forecast Horizon <sup>*</sup>
                <i className="icon mbc-icon info" tooltip-data={forecastHorizonTooltipContent} />
              </label>
              <input
                {...register('forecastHorizon', { required: '*Missing entry' })}
                type="number"
                className="input-field"
                id="forecastHorizonField"
                defaultValue={12}
                placeholder="eg. 1"
                autoComplete="off"
                onWheel={(e) => e.target.blur()}
              />
              <span className={classNames('error-message')}>{errors.forecastHorizon?.message}</span>
            </div>
          </div>
        </div>
          {
            expertView ? 
            <div className={Styles.flexLayout}>
            <div className={Styles.hierarchyContainer}>
              <div
                className={classNames(
                  `input-field-group`,
                  Styles.tooltipIcon
                )}
              >
                <label id="hierarchyLabel" htmlFor="hierarchyField" className="input-label">
                  Levels of Hierarchy
                  {/* <i className="icon mbc-icon info" tooltip-data={hierarchyTooltipContent} /> */}
                </label>
                <div className="custom-select" 
                  // onBlur={() => trigger('hierarchy')}
                  >
                  <select
                    id="hierarchyField"
                    {...register('hierarchy')}
                  >
                    <option id="hierarchyOption" value={''}>No Hierarchy</option>
                    <option value={'2'}>2</option>
                    <option value={'3'}>3</option>
                    <option value={'4'}>4</option>
                    <option value={'5'}>5</option>
                    <option value={'6'}>6</option>
                    <option value={'7'}>7</option>
                    <option value={'8'}>8</option>
                    <option value={'9'}>9</option>
                    <option value={'10'}>10</option>
                    <option value={'11'}>11</option>
                    <option value={'12'}>12</option>
                    <option value={'13'}>13</option>
                    <option value={'14'}>14</option>
                    <option value={'15'}>15</option>
                    <option value={'16'}>16</option>
                    <option value={'17'}>17</option>
                    <option value={'18'}>18</option>
                    <option value={'19'}>19</option>
                    <option value={'20'}>20</option>
                  </select>
                </div>
              </div>
            </div>
            <div className={Styles.runOnPowerfulMachinesContainer}>
            <div
              className={classNames(
                `input-field-group`
              )}
              >
                <label id="runOnPowerfulMachinesLabel" htmlFor="runOnPowerfulMachinesField" className="input-label">
                  Backend
                </label>
                <div className="custom-select" 
                  // onBlur={() => trigger('frequency')}
                  >
                  <select
                    id="runOnPowerfulMachinesField"
                    {...register('runOnPowerfulMachines')}
                  >
                    <option value={false}>Normal Mode</option>
                    <option value={true}>Cluster Mode</option>
                  </select>
                </div>
              </div>
            </div>
          </div> : null
          }
        <div>
          <div
            id="comment"
            className={classNames('input-field-group include-error area')}
          >
            <label className="input-label" htmlFor="comment">
              Add comment
            </label>
            <textarea className="input-field-area" type="text" {...register('comment')} rows={50} id="comment" />
          </div>
        </div>
      </div>
    </div>
    </div>
  );
}

export default RunParametersForm;