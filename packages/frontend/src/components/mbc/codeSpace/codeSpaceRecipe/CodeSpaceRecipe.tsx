import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './CodeSpaceRecipe.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import { Envs } from 'globals/Envs';
import { ICodeCollaborator, ITag, IUserInfo } from 'globals/types';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { Notification } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import { isValidGitUrl } from '../../../../services/utils';
import { useHistory } from 'react-router-dom'; const classNames = cn.bind(Styles);
import Tags from 'components/formElements/tags/Tags';
import Modal from 'components/formElements/modal/Modal';
export interface ICreateRecipe {
  createdBy: IUserInfo;
  recipeName: string;
  recipeType: string;
  gitUrl: string;
  diskSpace: string;
  minCpu: string;
  maxCpu: string;
  minRam: string;
  maxRam: string;
  isPublic: boolean;
  users: ICodeCollaborator[];
  software: string[];
  plugins: string[];

}
export interface Isoftware {
  name: string;
  version: string;
}

export interface IUserInfoProps {
  user: IUserInfo;
}

const CodeSpaceRecipe = (props: IUserInfoProps) => {
  const requiredError = '*Missing entry';
  const repeatedError = '*Recipe name already exists';
  const history = useHistory();

  const [notificationMsg, setNotificationMsg] = useState(false);
  const [softwares, setSoftwares] = useState<ITag[]>([]);
  const [enableCreate, setEnableCreate] = useState(false);
  const [recipeName, setRecipeName] = useState('');
  const recipeType = 'private';
  const [gitUrl, setGitUrl] = useState('');

  const [gitPath, setGitPath] = useState('');
  const [gitRepoLoc, setGitRepoLoc] = useState('');
  const [deployPath, setDeployPath] = useState('');
  const [diskSpace, setDiskSpace] = useState('');
  const minCpu = 0;
  const [maxCpu, setMaxCpu] = useState('');
  const minRam = 0;
  const [maxRam, setMaxRam] = useState('');
  const [hardware, setHardware] = useState('large');
  const [software, setSoftware] = useState([]);
  const [isPublic, setIsPublic] = useState(false);
  const [isSoftwareMissing, setSoftwareMissing] = useState(false);
  const [errorObj, setErrorObj] = useState({
    recipeName: '',
    hardware: '',
    recipeType: '',
    gitUrl: '',
    gitPath: '',
    software: '',
    gitRepoLoc: '',
    deployPath: '',
    discSpace: '',
    minCpu: '',
    maxCpu: '',
    minRam: '',
    maxRam: '',
  });

  const gitHubUrl = {
    gitHubUrl: gitUrl
  };
  useEffect(() => {

    CodeSpaceApiClient.getSoftwareLov()
      .then((response) => {
        ProgressIndicator.hide();
        const res = response.data;
        let softwares: any = [];
        softwares = res.map((item: any) => {
          return { id: item.softwareName, name: item.softwareName };
        }
        );
        setSoftwares(softwares);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err: any) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);


  const onRecipeNameChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = e.currentTarget.value;
    setRecipeName(value);
    setErrorObj((prevState) => ({
      ...prevState,
      recipeName: '',
    }));
  };

  const onGitUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const githubUrlVal = e.currentTarget.value.trim();
    setEnableCreate(false);
    setGitUrl(githubUrlVal);
    console.log("AJAY: "+githubUrlVal);
    const errorText = githubUrlVal.length
      ? isValidGitUrl(githubUrlVal, isPublic)
        ? ''
        : `provide valid ${isPublic ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL} git url.`
      : requiredError;
    setErrorObj((prevState) => ({
      ...prevState,
      gitUrl: errorText,
    }));
  };

  const onSoftwareChange = (selectedTags: React.SetStateAction<any[]>) => {
    setSoftware(selectedTags);
    setSoftwareMissing(false);
    setErrorObj((prevState) => ({
      ...prevState,
      software: '',
    }));
  };

  const onGitPathChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const gitPath = e.currentTarget.value.trim();
    setGitPath(gitPath);
    setErrorObj((prevState) => ({
      ...prevState,
      gitPath: '',
    }));
  }

  const onGitRepoLocChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const gitRepoLoc = e.currentTarget.value.trim();
    setGitRepoLoc(gitRepoLoc);
    setErrorObj((prevState) => ({
      ...prevState,
      gitRepoLoc: '',
    }));
  }
  const onDeployPathChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const deployPath = e.currentTarget.value.trim();
    setDeployPath(deployPath);
    setErrorObj((prevState) => ({
      ...prevState,
      deployPath: '',
    }));
  }

  const onHardwareChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setHardware(value);
    if (value === 'small') {
      setMaxCpu('0.5');
      setMaxRam('2');
      setDiskSpace('5');
    } else if (value === 'medium') {
      setMaxCpu('1');
      setMaxRam('3');
      setDiskSpace('10');
    } else {
      setMaxCpu('1.5');
      setMaxRam('4');
      setDiskSpace('15');
    }
  };

  const onIsPublicChange = (e: React.FormEvent<HTMLInputElement>) => {
    const currentValue = e.currentTarget.value;
    if (currentValue === 'true') {
      setIsPublic(true);
      setNotificationMsg(true);
    } else {
      setIsPublic(false);
      setNotificationMsg(false);
    }
  };

  const onNotificationMsgAccept = () => {
    setIsPublic(true);
    setNotificationMsg(false);
  };
  const onNotificationMsgCancel = () => {
    setIsPublic(false);

    setNotificationMsg(false);

  }
  const verifyRequest = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.verifyGitUser(gitHubUrl)
      .then((response) => {
        ProgressIndicator.hide();
        if (response?.success === 'SUCCESS') {
          setEnableCreate(true);
        } else {
          setEnableCreate(false);
        }
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show(err.message, 'alert');
        if (err.message === 'Value or Item already exist!') {
          setErrorObj((prevState) => ({
            ...prevState,
            recipeName: repeatedError,
          }));
        }
      });
  };
  const onRequest = () => {
    if (validateForm()) {
      const CreateNewRecipe = {
        createdBy: {
          department: props.user.department,
          email: props.user.email,
          firstName: props.user.firstName,
          gitUserName: props.user.id,
          id: props.user.id,
          lastName: props.user.lastName,
          mobileNumber: props.user.mobileNumber,
        },
        diskSpace: diskSpace,
        maxCpu: maxCpu,
        minCpu: minCpu,
        minRam: minRam,
        maxRam: maxRam,
        oSName: 'Debian-OS-11',
        osname: 'Debian-OS-11',
        plugins: ['string'],
        recipeName: recipeName,
        recipeType: recipeType,
        repodetails: gitUrl,
        software: software,
        isPublic: isPublic,
        gitPath: gitPath,
        gitRepoLoc: gitRepoLoc,
        deployPath: deployPath,
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpaceRecipe(CreateNewRecipe)
        .then((res) => {
          ProgressIndicator.hide();
          history.push('/codespaces');
          Notification.show('New Recipe Created successfully');
        })
        .catch((err: Error) => {
          ProgressIndicator.hide();
          Notification.show(err.message, 'alert');
          if (err.message === 'Value or Item already exist!') {
            setErrorObj((prevState) => ({
              ...prevState,
              recipeName: repeatedError,
            }));
          }
        });
    }
  };

  const validateForm = () => {
    let isValid = true;
    if (recipeName === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        recipeName: requiredError,
      }));
    }
    if (gitUrl === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        gitUrl: requiredError,
      }));
    }
    if (diskSpace === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        discSpace: requiredError,
      }));
    }
    if (maxCpu === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        maxCpu: requiredError,
      }));
    }
    if (maxRam === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        maxRam: requiredError,
      }));
    }
    if (software.length === 0) {
      isValid = false;
      setSoftwareMissing(true)
      setErrorObj((prevState) => ({
        ...prevState,
        software: requiredError,
      }));
    }
    if (isPublic) {
      if (gitRepoLoc === '') {
        isValid = false;
        setErrorObj((prevState) => ({
          ...prevState,
          gitRepoLoc: requiredError,
        }));
      }
      if (gitPath === '') {
        isValid = false;
        setErrorObj((prevState) => ({
          ...prevState,
          gitPath: requiredError,
        }));
      }
      if (deployPath === '') {
        isValid = false;
        setErrorObj((prevState) => ({
          ...prevState,
          deployPath: requiredError,
        }));

      }
    }
    return isValid;
  };

  return (
    <div>
      <div>
        <div className={classNames(Styles.mainPanel)}>
          <div>
            <h3>Recipe Management</h3>
            <div className={classNames(Styles.wrapper)}>
              <div className={classNames(Styles.firstPanel, 'addRecipe')}>
                <h3>Recipe Details</h3>
                <div className={classNames(Styles.formWrapper)}>
                  <div className={classNames(Styles.flex)}>
                    <div className={(Styles.col2)}>
                      <TextBox
                        type="text"
                        controlId={'recipeNameInput'}
                        labelId={'recipeNameLabel'}
                        label={'Recipe Name'}
                        placeholder={'Type here'}
                        value={recipeName}
                        errorText={errorObj.recipeName}
                        required={true}
                        maxLength={200}
                        onChange={onRecipeNameChange}
                      />
                    </div>
                    <div className={(Styles.col2)}>
                      <div className={classNames('input-field-group include-error')}>
                        <label className={classNames(Styles.inputLabel, Styles.m5, 'input-label')}>
                          Recipe Visibility <sup>*</sup>
                        </label>
                        <div className={Styles.pIIField}>
                          <label className={classNames('radio')}>
                            <span className="wrapper">
                              <input
                                type="radio"
                                className="ff-only"
                                value="true"
                                name="isPublic"
                                checked={isPublic === true}
                                onChange={onIsPublicChange}
                              />
                            </span>
                            <span className="label">Public</span>
                          </label>
                          <label className={classNames('radio')}>
                            <span className="wrapper">
                              <input
                                type="radio"
                                className="ff-only"
                                value="false"
                                name="isPublic"
                                checked={isPublic === false}
                                onChange={onIsPublicChange}
                              />
                            </span>
                            <span className="label">Private</span>
                          </label>
                        </div>
                      </div>
                    </div>
                    <div>
                      <Modal
                        title="Public Visibility"
                        show={notificationMsg}
                        showAcceptButton={false}
                        showCancelButton={false}
                        acceptButtonTitle="Confirm"
                        cancelButtonTitle="Cancel"
                        buttonAlignment='right'
                        scrollableContent={false}
                        hideCloseButton={true}
                        content={
                          <div>
                            <header>
                              <button className="modal-close-button" onClick={onNotificationMsgCancel}><i className="icon mbc-icon close thin"></i></button>
                            </header>
                            <div>
                              <p>The Recipe will be visible to all users. Are you sure to make it Public?</p>
                            </div>
                            <div className="btn-set footerRight">
                              <button className="btn btn-primary" type="button" onClick={onNotificationMsgCancel}>Cancel</button>
                              <button className="btn btn-tertiary" type="button" onClick={onNotificationMsgAccept}>Confirm</button>
                            </div>
                          </div>
                        }
                      />
                    </div>
                    <div className={classNames(Styles.col2)}>
                      <div className={classNames(Styles.inputLabel, 'input-label')}>
                        <TextBox
                          type="text"
                          controlId={'gitUrlInput'}
                          labelId={'gitUrlLabel'}
                          label={'GitHub Url'}
                          placeholder={'Type here'}
                          value={gitUrl}
                          errorText={errorObj.gitUrl}
                          required={true}
                          maxLength={200}
                          onChange={onGitUrlChange}
                        />
                        {(!enableCreate &&
                          <button className={classNames('btn btn-tertiary', Styles.verifyButton)} type="button" onClick={verifyRequest}>
                            <i className="icon mbc-icon alert circle"></i>Kindly add the PID6C39 as admin contributor in your gitHub repo and click here to verify.
                          </button>
                        )}
                        <p
                          style={{ color: 'var(--color-green)'}}
                          className={classNames(enableCreate ? '' : ' hide')}
                        >
                          <i className="icon mbc-icon alert circle"></i>PID6C39 onboarded successfully.
                        </p>
                      </div>
                    </div>
                    <div className={classNames(Styles.col2)}>
                      <div className={classNames('input-field-group include-error')}>
                        <label className={classNames(Styles.inputLabel, 'input-label')}>
                          Hardware Configuration <sup>*</sup>
                        </label>
                        <div id="HardwareBox" className='custom-select'>
                          <select
                            id="HardewareField"
                            required={true}
                            required-error={errorObj.hardware}
                            value={hardware}
                            onChange={onHardwareChange}>
                            <option value={'small'}>DiskSpace - 5GB CPU - 0.5 RAM - 2GB</option>
                            <option value={'medium'}>DiskSpace - 10GB CPU - 1 RAM - 3GB</option>
                            <option value={'large'}>DiskSpace - 15GB CPU - 1.5 RAM - 4GB</option>
                          </select>
                          <span className={classNames('error-message', errorObj.discSpace.length ? '' : 'hide')}>
                            {errorObj.discSpace}
                          </span>
                        </div>
                      </div>
                      <div className={classNames(Styles.request)} >
                        <a href={Envs.CODESPACE_HARDWARE_REQUEST_TEMPLATE} target="_blank" rel="noopener noreferrer">
                          Request new Hardware
                        </a>
                      </div>
                    </div>
                    <div className={classNames(Styles.col2)}>
                      <Tags
                        title={'Software Configuration'}
                        max={100}
                        chips={software}
                        placeholder={'Type here...e.g Java11'}
                        tags={softwares}
                        setTags={(selectedTags) => {
                          onSoftwareChange(selectedTags);
                        }}
                        isMandatory={true}
                        showMissingEntryError={isSoftwareMissing}
                      />
                      <div className={classNames(Styles.request)} >
                        <a href={Envs.CODESPACE_SOFTWARE_REQUEST_TEMPLATE} target="_blank" rel="noopener noreferrer">
                          Request new Software
                        </a>
                      </div>
                    </div>

                  </div>
                </div>

                {isPublic && (
                  <div>
                    <h4 className={classNames(Styles.sectionHeader)}>CI/CD</h4>
                    <div className={classNames(Styles.formWrapper, Styles.mT)}>
                      <div className={classNames(Styles.flex)}>
                        <div className={classNames(Styles.col2)}>
                          <div className={classNames('input-field-group include-error')}>
                            <div>
                              <TextBox
                                type="text"
                                controlId={'gitPath'}
                                label={'Git Repository'}
                                labelId={'gitPathLabel'}
                                placeholder={'Type here '}
                                value={gitPath}
                                errorText={errorObj.gitPath}
                                required={true}
                                maxLength={200}
                                onChange={onGitPathChange}
                              />
                            </div>
                          </div>
                        </div>
                        <div className={classNames(Styles.col2)}>
                          <div className={classNames('input-field-group include-error')}>
                            <div>
                              <TextBox
                                type="text"
                                controlId={'deployPath'}
                                label={'Helm File Path'}
                                labelId={'deployPathLabel'}
                                placeholder={'e.g /deploy/helm'}
                                value={deployPath}
                                errorText={errorObj.deployPath}
                                required={true}
                                maxLength={200}
                                onChange={onDeployPathChange}
                              />
                            </div>
                          </div>
                        </div>
                        <div className={classNames(Styles.col2)}>
                          <div className={classNames('input-field-group include-error')}>
                            <div>
                              <TextBox
                                type="text"
                                controlId={'gitRepoLoc'}
                                label={'Docker File Path'}
                                labelId={'gitRepoLocLabel'}
                                placeholder={'e.g /build/docker'}
                                value={gitRepoLoc}
                                errorText={errorObj.gitRepoLoc}
                                required={true}
                                maxLength={200}
                                onChange={onGitRepoLocChange}
                              />
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
                <div className={Styles.btnConatiner}>
                  <button className={classNames(enableCreate ? 'btn-tertiary' : Styles.disableVerifyButton, 'btn')} type="button" disabled={!enableCreate} onClick={onRequest}>
                    Create Recipe
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CodeSpaceRecipe;
