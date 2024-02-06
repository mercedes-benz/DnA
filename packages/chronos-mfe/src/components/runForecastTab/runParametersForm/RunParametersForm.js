import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import { useSelector } from 'react-redux';
import Styles from './run-parameters-form.scss';
// Container components
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import { Envs } from '../../../utilities/envs';

const RunParametersForm = () => {
  const { register, resetField, formState: { errors } } = useFormContext({defaultValues: {
                                                                          hierarchy: '',
                                                                          runOnPowerfulMachines: false
                                                                        }});
  const frequencyTooltipContent = 'Please select a frequency for your data in the field below.\n Make sure the datetimes in the first column of the data you upload matches the frequency selected here.\n More frequencies will be supported soon in the GUI, please use the API or contact the Chronos team if you need a specific frequency.';
  const forecastHorizonTooltipContent = 'Select how many data points in the future the forecast should predict.\n Note that this number should not be more than 1/5th the length of your existing data, ideally less.\n Also, forecasting gets less precise over time, so try to not predict too many points in the future.';
  const chronosVersionTooltipContent = `This is an experimental feature used for testing or as a fallback option. To use an older Chronos version, type the version number, e.g. "2.3.0" (without the quotes).\nTo get a list of available Chronos versions, check this link [${Envs.CHRONOS_RELEASES_INFO_URL}].\nNote that we currently offer no support for this feature. Available versions differ between environments and versions might be discontinued without previous warning.`;
  const configurationFileTooltipContent = `You can upload your own Configuration\nFiles in the "Manage Project" Tab`;
  const backtestingTooltipContent = `This will run Chronos the selected amount of times, while ignoring progressively more recent data. \nPlease be aware that job runtime effectively multiplies with your selected backtesting window. \nLearn more in the documentation under "Backtesting".`;

  const [expertView, setExpertView] = useState(false);

  const {isLoading, configFiles} = useSelector(state => state.chronosForm);
  const [showCustomConfigModal, setShowCustomConfigModal] = useState(false);
  const [configRecommendation, setConfigRecommendation] = useState('');

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
    expertView && SelectBox.defaultSetup(); Tooltip.defaultSetup();
  }, [expertView]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [isLoading]);

  useEffect(() => {
    configRecommendation.includes('OPTIMISATION_CONFIG') && setShowCustomConfigModal(true)
  }, [configRecommendation]);

  const customConfigFileContent = <div className={Styles.customConfigContainer}>
    <p>The optimization finds a custom configuration file tailored to your data. <strong>Please read the following hints and restrictions carefully:</strong></p>
    <p><strong>Single Target KPI only:</strong> Since the configuration is tailored to your target KPI, this feature only works with single-KPI data files. In case you have multiple target KPIs that you want to predict, please split them into different files. However, you can provide drivers data.</p>
    <p><strong>Runtime:</strong> The feature will run hundreds of configurations to find the optimum one for you. This means that an optimization run takes a lot longer than a normal run (from an hour to a few).</p>
    <p><strong>Forecast horizon:</strong> The feature will optimize your configuration based on your forecast horizon, meaning it will try to achieve the lowest error averaged over your forecast horizon setting. Make sure to select the forecast horizon that is important to you. You can still forecast more steps with the generated configuration.</p>
    <p><strong>Evaluate results:</strong> Make sure to evaluate your results and check whether forecasts with the generated configuration meet your expectations. Feel free to contact the ADS team (<a href={`mailto:${Envs.ADS_EMAIL}`}>{Envs.ADS_EMAIL}</a>) for further needs or questions.</p>
    <p>After the configuration optimization run finishes successfully, the proposed configuration file will be available in the project&apos;s configuration dropdown as &ldquo;CustomConfig&rdquo; with your Target KPI name and date of creation.</p>
    <button className='btn btn-tertiary' onClick={() => setShowCustomConfigModal(false)}>Ok</button>
  </div>;
  
  return (
    <div className={Styles.wrapper}>
      <div className={Styles.firstPanel}>
        <h3>Run Parameters</h3>
      <div className={Styles.infoIcon}>
        <label className="switch">
          <span className="label" style={{ marginRight: '5px' }}>
            Expert Options
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
                <i className="icon mbc-icon info" tooltip-data={configurationFileTooltipContent} />
              </label>
              <div className="custom-select" 
                // onBlur={() => trigger('configurationFile')}
                >
                <select
                  id="configurationField"
                  {...register('configurationFile', {
                    required: '*Missing entry',
                    validate: (value) => value !== '0' || '*Missing entry',
                    onChange: (e) => { setConfigRecommendation(e.target.value) }
                  })}
                >
                  {
                    isLoading && configFiles.length === 0 ? (
                      <option id="configurationOption" value={0}>
                        None
                      </option>
                      ) : (
                        <>
                          {configFiles.map((file) => (
                              <option key={file.objectName} value={file.objectName}>
                                {file?.objectName?.includes('OPTIMISATION_CONFIG') ? 'Generate custom configuration' : file?.objectName?.includes('chronos-core') ? 'General > ' + file?.objectName?.split("/")[2] : 'Project > ' + file?.objectName?.split("/")[2]}
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
                  {/* <option value={'Daily'}>Daily</option> */}
                  <option value={'Weekly'}>Weekly</option>
                  <option value={'Monthly'}>Monthly</option>
                  {/* <option value={'Yearly'}>Yearly</option> */}
                  {/* <option value={'No_Frequency'}>No Frequency</option> */}
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
            <>
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
          </div>
          <div className={Styles.flexLayout}>
            <div className={Styles.hierarchyContainer}>
            <div className={classNames('input-field-group')}>
              <label id="chronosVersionLabel" htmlFor="chronosVersionInput" className="input-label">
                Chronos Version
                <i className="icon mbc-icon info" tooltip-data={chronosVersionTooltipContent} />
              </label>
              <input
                {...register('chronosVersion')}
                type="text"
                className="input-field"
                id="chronosVersionInput"
                maxLength={55}
                placeholder="Leave empty for default version"
                autoComplete="off"
              />
            </div>
            </div>
            <div className={Styles.runOnPowerfulMachinesContainer}>
              <div
                className={classNames(
                  `input-field-group`,
                  Styles.tooltipIcon
                )}
              >
                <label id="backtestingLabel" htmlFor="backtestingField" className="input-label">
                  Backtesting
                  <i className="icon mbc-icon info" tooltip-data={backtestingTooltipContent} />
                </label>
                <div className="custom-select" 
                  // onBlur={() => trigger('hierarchy')}
                  >
                  <select
                    id="backtestingField"
                    {...register('backtesting')}
                  >
                    <option value={''}>No Backtesting</option>
                    <option value={'1'}>1</option>
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
                  </select>
                </div>
              </div>
            </div>
          </div>
          </>  : null
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

    { showCustomConfigModal &&
        <Modal
          title={'Generate custom configuration'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={showCustomConfigModal}
          content={customConfigFileContent}
          scrollableContent={false}
          onCancel={() => {
            setShowCustomConfigModal(false)
          }}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </div>
  );
}

export default RunParametersForm;