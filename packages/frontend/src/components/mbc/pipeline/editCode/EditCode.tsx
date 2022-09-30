import cn from 'classnames';
import React, { useState, useEffect } from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// import { IUserInfo } from '../../../globals/types';
import Styles from './EditCode.scss';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
const classNames = cn.bind(Styles);
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
import { history } from '../../../../router/History';
import AceEditor from 'react-ace';
import { PipelineApiClient } from '../../../../services/PipelineApiClient';
import { IPipelineProjectDag, IError, IPipelineProjectDetail } from 'globals/types';
import { getParams } from '../../../../router/RouterUtils';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import FullScreenModeIcon from 'components/icons/fullScreenMode/FullScreenModeIcon';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/ext-language_tools';
import { Envs } from 'globals/Envs';

const EditCode = () => {
  const [dagNameFieldInput, setDagNameFieldInput] = useState<string>('');
  const [info, setInfo] = useState(false);
  const [dagEditorContent, setDagEditorContent] = useState<string>('');
  const [dagEditorContentError, setDagEditorContentError] = useState<string>('');
  const [dagNameNotExisteDagContent, setDagNameNotExisteDagContent] = useState<boolean>(false);
  const [isCodeEditable, setIsCodeEditable] = useState<boolean>(false);
  const [isFsEnable, setIsFsEnable] = useState<boolean>(false);
  const [updateProjectError, setUpdateProjectError] = useState<string>('');

  const onInfoModalCancel = () => {
    setInfo(false);
  };
  const goback = () => {
    history.goBack();
  };
  const fsActive = () => {
    setIsFsEnable(!isFsEnable);
  };

  const validateAddNewDag = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (dagEditorContent === '') {
      setDagEditorContentError(errorMissingEntry);
      formValid = false;
    }

    const dagIdMatched =
      dagEditorContent.includes(`'${dagNameFieldInput}'`) || dagEditorContent.includes(`"${dagNameFieldInput}"`);
    if (!dagIdMatched) {
      setDagNameNotExisteDagContent(true);
      formValid = false;
    } else {
      setDagNameNotExisteDagContent(false);
    }
    return formValid;
  };

  const onDagCodeChange = (newValue: any, e: any) => {
    setDagEditorContent(newValue);
  };
  const updateDag = () => {
    if (validateAddNewDag()) {
      const data = {
        data: {
          dagName: dagNameFieldInput,
          dagContent: dagEditorContent,
        },
      };
      ProgressIndicator.show();
      PipelineApiClient.putDag(data)
        .then((response: IPipelineProjectDag) => {
          ProgressIndicator.hide();
          Notification.show('DAG update successfully!');
          setIsFsEnable(false);
          setUpdateProjectError('');
        })
        .catch((err: IError) => {
          setUpdateProjectError(err.message);
          ProgressIndicator.hide();
        });
    }
  };
  useEffect(() => {
    const id = getParams().id;
    const dagName = id;
    ProgressIndicator.show();
    Tooltip.defaultSetup();
    PipelineApiClient.getSpecificDagDetails(dagName)
      .then((response: IPipelineProjectDag) => {
        setDagNameFieldInput(response.dagName);
        setDagEditorContent(response.dagContent);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });

    PipelineApiClient.getPipelineProjectList()
      .then((response) => {
        const records = response.data;
        records.find((item: IPipelineProjectDetail) => {
          item.dags.find((item: IPipelineProjectDag) => {
            if (item.dagName === id && item.permissions.includes('can_dag_edit')) {
              setIsCodeEditable(true);
            }
          });
        });
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  }, []);
  const contentForInfo = (
    <div className={Styles.infoPopup}>
      <div className={Styles.modalContent}>
        Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's
        standard dummy text ever since the 1500s, when an unknown pri
      </div>
    </div>
  );

  const contentForCodeEditor = (
    <div className={Styles.contentForCodeEditor}>
      <div className={isFsEnable ? Styles.codeEditorFs : ' '}>
        <div className={Styles.modalContent}>
          <div className={Styles.dagCodeEditorTile}>
            {dagNameFieldInput}.py
            <div className={Styles.logo}>
              <img src={Envs.DNA_BRAND_LOGO_URL} />
            </div>
            <div className={Styles.fullScr} onClick={fsActive}>
              <FullScreenModeIcon fsNeed={isFsEnable} />
            </div>
          </div>
        </div>
        <div className={Styles.dagCodeEditorContent}>
          <AceEditor
            width="100%"
            height={isFsEnable ? '65vh' : '360px'}
            placeholder="Type here"
            mode="python"
            theme="solarized_dark"
            name="dagEditor"
            fontSize={16}
            showPrintMargin={false}
            showGutter={true}
            highlightActiveLine={true}
            value={dagEditorContent}
            onChange={onDagCodeChange}
            readOnly={isCodeEditable ? false : true}
            setOptions={{
              enableBasicAutocompletion: true,
              enableLiveAutocompletion: false,
              enableSnippets: false,
              showLineNumbers: true,
              tabSize: 2,
            }}
          />
          <span className={classNames('error-message', dagEditorContentError.length ? '' : 'hide')}>
            {dagEditorContentError}
          </span>
          {dagNameNotExisteDagContent && (
            <div className={Styles.dagNameNotExisteDagContent}>
              {' '}
              Your DAG Code is invalid. DAG Name({dagNameFieldInput}) is not found in code as dag_id!
            </div>
          )}
        </div>
        <p className={Styles.DagCodeError}> {updateProjectError !== '' && updateProjectError} </p>
        <div className={isCodeEditable ? 'active' : Styles.disable}>
          <div className={Styles.addBtn}>
            <button onClick={updateDag} className={Styles.actionBtn + ' btn btn-tertiary'} type="button">
              <span>Update DAG</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <button className="btn btn-text back arrow" type="submit" onClick={goback}>
            Back
          </button>
          <div className={Styles.title}>
            <h2>DAG Code Editor</h2>
          </div>
        </div>
        <div className={Styles.content}>{contentForCodeEditor}</div>
      </div>
      {info && (
        <InfoModal
          title={'About DAG Code Editor'}
          modalWidth={'35vw'}
          show={info}
          content={contentForInfo}
          onCancel={onInfoModalCancel}
        />
      )}
    </React.Fragment>
  );
};

export default EditCode;
