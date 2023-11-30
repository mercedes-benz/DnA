import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from "react-hook-form";
import { useParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';
// Container Components
import Modal from 'dna-container/Modal';
// Container components
import SelectBox from 'dna-container/SelectBox';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { chronosApi } from '../../apis/chronos.api';
import RunParametersForm from './runParametersForm/RunParametersForm';
import InputFileArea from './inputFileArea/InputFileArea';
import { setInputFile } from '../../redux/chronosFormSlice';
import { getProjectDetails } from '../../redux/projectDetails.services';
import Tutorial from '../../components/tutorial/Tutorial';

const RunForecastTab = ({ onRunClick }) => {
  const { id: projectId } = useParams();
  const methods = useForm();
  const dispatch = useDispatch();

  const [showTutorial, setShowTutorial] = useState((localStorage.getItem('showTutorial') === null || localStorage.getItem('showTutorial') === 'true') ? true : false);

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);
  
  const onSubmit = (data) => {
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
    if(data.configurationFile.includes('OPTIMISATION_CONFIG')) {
      formData.append('runOnPowerfulMachines', true);
    } else {
      formData.append('runOnPowerfulMachines', data.runOnPowerfulMachines === undefined ? false : data.runOnPowerfulMachines);
    }
    formData.append('chronosVersion', data.chronosVersion === undefined ? '' : data.chronosVersion);
    formData.append('comment', data.comment);
    formData.append('saveRequestPart', data.saveRequestPart === undefined ? 'false' : data.saveRequestPart + '');
    formData.append('savedInputPath', data.savedInputPath === undefined ? null : data.savedInputPath);

    ProgressIndicator.show();
    chronosApi.createForecastRun(formData, projectId).then(() => {
        Notification.show('Forecast running...\n\nThis usually takes about 10 minutes.');
        document.body.scrollTop = 0;
        document.documentElement.scrollTop = 0;
        onRunClick();
        ProgressIndicator.hide();
        methods.reset();
        SelectBox.defaultSetup();
        dispatch(setInputFile({}));
        dispatch(getProjectDetails(projectId));
      }).catch(error => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating run',
          'alert',
        );
      });
  }

  return (
    <>
      <FormProvider {...methods} >
        <form onSubmit={methods.handleSubmit(onSubmit)}>
          <InputFileArea showTutorial={setShowTutorial} />
          <RunParametersForm />
          <div className="btnContainer">
            <button className="btn btn-tertiary" type="submit">Run Forecast</button>
          </div>
        </form>
      </FormProvider>
      
      { showTutorial &&
        <Modal
          title={'Chronos Tutorial'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={showTutorial}
          content={<Tutorial sliderWidth="100%" sliderHeight="auto" onOk={() => setShowTutorial(false)} />}
          scrollableContent={false}
          onCancel={() => {
            setShowTutorial(false)
          }}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </>
  );
};

export default RunForecastTab;
