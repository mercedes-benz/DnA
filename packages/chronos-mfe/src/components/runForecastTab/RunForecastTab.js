import React, { useEffect, useState } from 'react';
import { useForm, FormProvider } from "react-hook-form";
import { useParams } from 'react-router-dom';
// Container components
import SelectBox from 'dna-container/SelectBox';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { chronosApi } from '../../apis/chronos.api';
import RunParametersForm from './runParametersForm/RunParametersForm';
import InputFileArea from './inputFileArea/InputFileArea';

const RunForecastTab = ({ onRunClick }) => {
  const { id: projectId } = useParams();
  const methods = useForm();
  const [inputFile, setInputFile] = useState();

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);
  
  const onSubmit = (data) => {
    console.log('formData');
    console.log(data);
    const formData = new FormData();
    if(data.savedInputPath !== undefined) {
      formData.append('file', '');
    } else {
      if(data.file.length === 1) {
        formData.append('file', data.file[0]);
      } else {
        formData.append('file', data.droppedFile[0]);
      }
    }
    formData.append('runName', data.runName);
    formData.append('configurationFile', data.configurationFile);
    formData.append('frequency', data.frequency);
    formData.append('forecastHorizon', data.forecastHorizon);
    formData.append('hierarchy', data.hierarchy === undefined ? '' : data.hierarchy);
    formData.append('runOnPowerfulMachines', data.runOnPowerfulMachines === undefined ? false : data.runOnPowerfulMachines);
    formData.append('comment', data.comment);
    formData.append('saveRequestPart', data.saveRequestPart === undefined ? 'false' : data.saveRequestPart + '');
    formData.append('savedInputPath', data.savedInputPath === undefined ? null : data.savedInputPath);

    ProgressIndicator.show();
    chronosApi.createForecastRun(formData, projectId).then(() => {
        Notification.show('Run created successfully');
        document.body.scrollTop = 0;
        document.documentElement.scrollTop = 0;
        onRunClick();
        ProgressIndicator.hide();
        methods.reset();
      }).catch(error => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating run',
          'alert',
        );
      });

    methods.reset();
    SelectBox.defaultSetup();
    setInputFile();
  }

  return (
    <FormProvider {...methods} >
      <form onSubmit={methods.handleSubmit(onSubmit)}>
        <InputFileArea
          projectId={projectId}
          inputFile={inputFile}
          setInputFile={setInputFile}
        />
        <RunParametersForm />
        <div className="btnContainer">
          <button className="btn btn-tertiary" type="submit">Run Forecast</button>
        </div>
      </form>
    </FormProvider>
  );
};

export default RunForecastTab;
