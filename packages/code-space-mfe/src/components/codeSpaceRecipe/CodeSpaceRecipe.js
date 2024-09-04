import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './CodeSpaceRecipe.scss';
import SelectBox from 'dna-container/SelectBox';
import TextBox from 'dna-container/TextBox';
import Caption from 'dna-container/Caption';
import { Envs } from '../../Utility/envs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { isValidGitUrl } from '../../Utility/utils';
import { useHistory } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import Tags from 'dna-container/Tags';
import Modal from 'dna-container/Modal';
import ServiceCard from '../serviceCard/ServiceCard';
const classNames = cn.bind(Styles);

const CodeSpaceRecipe = (props) => {
  const { id: recipeId } = useParams();

  const edit = recipeId ? true : false;
  const requiredError = '*Missing entry';
  const repeatedError = '*Recipe name already exists';
  const history = useHistory();

  const [notificationMsg, setNotificationMsg] = useState(false);
  const [softwares, setSoftwares] = useState([]);
  const [enableCreate, setEnableCreate] = useState(false);

  const [recipeName, setRecipeName] = useState('');
  const [isPublic, setIsPublic] = useState(false);
  const [gitUrl, setGitUrl] = useState('');
  const [hardware, setHardware] = useState('large');
  const [software, setSoftware] = useState([]);
  const [gitPath] = useState('');
  const [gitRepoLoc, setGitRepoLoc] = useState('');
  const [deployPath, setDeployPath] = useState('');
  
  const [diskSpace, setDiskSpace] = useState('');
  const [minCpu, setMinCpu] = useState('1');
  const [maxCpu, setMaxCpu] = useState('');
  const [minRam, setMinRam] = useState('1000');
  const [maxRam, setMaxRam] = useState('');
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

  const [additionalServices, setAdditionalServices] = useState([]);
  const [selectedAdditionalServices, setSelectedAdditionalServices] = useState([]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);
  
  useEffect(() => {
    if(recipeId !== undefined) {
      ProgressIndicator.show();
      CodeSpaceApiClient.getCodeSpaceRecipe(recipeId)
        .then((res) => {
          const recipe = res.data.data;
          setDiskSpace(recipe?.diskSpace);
          setMaxCpu(recipe?.maxCpu);
          setMinCpu(recipe?.minCpu);
          setMinRam(recipe?.minRam);
          setMaxRam(recipe?.maxRam);
          setRecipeName(recipe?.recipeName);
          setGitUrl(recipe?.repodetails);
          setSoftware(recipe?.software);
          setIsPublic(recipe?.isPublic);
          setGitRepoLoc(recipe?.gitRepoLoc);
          setDeployPath(recipe?.deployPath);
          setSelectedAdditionalServices(recipe?.additionalServices);
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        })
        .catch((err) => {
          ProgressIndicator.hide();
          if (err?.response?.data?.errors?.length > 0) {
            err?.response?.data?.errors.forEach((err) => {
              Notification.show(err?.message || 'Something went wrong.', 'alert');
            });
          } else {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          }
        });
    }
  }, [recipeId]);

  useEffect(() => {
    CodeSpaceApiClient.getSoftwareLov()
      .then((response) => {
        ProgressIndicator.hide();
        const res = response.data.data;
        let softwares = [];
        softwares = res.map((item) => {
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
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getAdditionalServicesLov()
      .then((response) => {
        ProgressIndicator.hide();
        setAdditionalServices(response.data.data);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);

  const onRecipeNameChange = (e) => {
    const value = e.currentTarget.value;
    setRecipeName(value);
    setErrorObj((prevState) => ({
      ...prevState,
      recipeName: '',
    }));
  };

  const onGitUrlChange = (e) => {
    const githubUrlVal = e.currentTarget.value.trim();
    setGitUrl(githubUrlVal);
    setEnableCreate(false);
    const errorText = githubUrlVal.length
      ? (isValidGitUrl(githubUrlVal) ? '' : `Provide valid https://github.com/ or ${Envs.CODE_SPACE_GIT_PAT_APP_URL} git url.`)
      : requiredError;
    setErrorObj((prevState) => ({
      ...prevState,
      gitUrl: errorText,
    }));
  };

  const onSoftwareChange = (selectedTags) => {
    setSoftware(selectedTags);
    setSoftwareMissing(false);
    setErrorObj((prevState) => ({
      ...prevState,
      software: '',
    }));
  };

  const onAdditionalServicesChange = (e) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        let temp = '';
        temp = option.value;
        selectedValues.push(temp);
      });
    }
    const selectedAdditionalServices = additionalServices?.filter(service => selectedValues.includes(service.serviceName));
    setSelectedAdditionalServices(selectedAdditionalServices);
  };

  // const onGitPathChange = (e) => {
  //   const gitPath = e.currentTarget.value.trim();
  //   setGitPath(gitPath);
  //   setErrorObj((prevState) => ({
  //     ...prevState,
  //     gitPath: '',
  //   }));
  // }

  const onGitRepoLocChange = (e) => {
    const gitRepoLoc = e.currentTarget.value.trim();
    setGitRepoLoc(gitRepoLoc);
    setErrorObj((prevState) => ({
      ...prevState,
      gitRepoLoc: '',
    }));
  }
  const onDeployPathChange = (e) => {
    const deployPath = e.currentTarget.value.trim();
    setDeployPath(deployPath);
    setErrorObj((prevState) => ({
      ...prevState,
      deployPath: '',
    }));
  }

  const onHardwareChange = (e) => {
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

  const onIsPublicChange = (e) => {
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

  };

  const convertRam = ()  => {
    const ramValue = parseInt(maxRam)*1000;
    return ramValue.toString();
  };
  
  const verifyRequest = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.verifyGitUser(gitHubUrl)
      .then((response) => {
        ProgressIndicator.hide();
        if (response?.data.success === 'SUCCESS') {
          setEnableCreate(true);
        } else {
          setEnableCreate(false);
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
        if (err.message === 'Value or Item already exist!') {
          setErrorObj((prevState) => ({
            ...prevState,
            recipeName: repeatedError,
          }));
        }
      });
  };

  const onCreateRecipe = () => {
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
        maxRam: convertRam(),
        oSName: 'Debian-OS-11',
        osname: 'Debian-OS-11',
        plugins: ['string'],
        recipeName: recipeName,
        recipeId: recipeName?.replace(/\s+/g, ''),
        recipeType: isPublic ? 'public' : 'private',
        repodetails: gitUrl,
        software: software,
        isPublic: isPublic,
        gitPath: gitPath,
        gitRepoLoc: gitRepoLoc,
        deployPath: deployPath,
        additionalServices: selectedAdditionalServices.map(service => service?.serviceName),
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpaceRecipe(CreateNewRecipe)
        .then(() => {
          ProgressIndicator.hide();
          history.push('/manageRecipes');
          Notification.show('New Recipe Created successfully');
        })
        .catch((err) => {
          ProgressIndicator.hide();
          if(err?.response?.status === 409) {
            Notification.show(err?.response?.data?.data, 'alert');
          }
          if(err?.response?.status === 400) {
            Notification.show(`Your git repo main branch is protected with repo lock. Kindly uncheck 'Do not allow bypassing the above settings' in branch protection rule settings and try again.`, 'warning');
          }
          if(err?.response?.status !== 409 && err?.response?.status !== 400) {
            Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
          }
          if (err.message === 'Value or Item already exist!') {
            setErrorObj((prevState) => ({
              ...prevState,
              recipeName: repeatedError,
            }));
          }
        });
    }
  };

  const onUpdateRecipe = () => {
    if (validateForm()) {
      const data = {
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
        maxRam: convertRam(),
        oSName: 'Debian-OS-11',
        osname: 'Debian-OS-11',
        plugins: ['string'],
        recipeName: recipeName,
        recipeId: recipeName?.replace(/\s+/g, ''),
        recipeType: isPublic ? 'public' : 'private',
        repodetails: gitUrl,
        software: software,
        isPublic: isPublic,
        gitPath: gitPath,
        gitRepoLoc: gitRepoLoc,
        deployPath: deployPath,
        additionalServices: selectedAdditionalServices.map(service => service?.serviceName),
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.updateCodeSpaceRecipe(recipeId, data)
        .then(() => {
          ProgressIndicator.hide();
          history.push('/manageRecipes');
          Notification.show('Recipe Updated successfully');
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
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
    // if (isPublic) {
    //   if (gitRepoLoc === '') {
    //     isValid = false;
    //     setErrorObj((prevState) => ({
    //       ...prevState,
    //       gitRepoLoc: requiredError,
    //     }));
    //   }
    //   if (gitPath === '') {
    //     isValid = false;
    //     setErrorObj((prevState) => ({
    //       ...prevState,
    //       gitPath: requiredError,
    //     }));
    //   }
    //   if (deployPath === '') {
    //     isValid = false;
    //     setErrorObj((prevState) => ({
    //       ...prevState,
    //       deployPath: requiredError,
    //     }));

    //   }
    // }
    return isValid;
  };

  return (
    <div>
      <div>
        <div className={classNames(Styles.mainPanel)}>
          <div>
            <Caption title={edit ? 'Update Recipe' : 'Create New Recipe'}>
              <p className={Styles.warning}><i className="icon mbc-icon alert circle" /> <span>Recipe creation cannot be done from personal repo. For eg. <pre>USERID/repo_name</pre></span></p>
            </Caption>
            <div className={classNames(Styles.wrapper)}>
              <div className={classNames(Styles.firstPanel, 'addRecipe')}>
                <div className={classNames(Styles.formWrapper)}>
                  <div className={classNames(Styles.flex)}>
                    <div className={classNames(Styles.col2, recipeId && Styles.disabledSection)}>
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
                          <button className={classNames('btn btn-tertiary', Styles.verifyButton, errorObj?.gitUrl?.length > 0 && Styles.giturlerror)} type="button" onClick={verifyRequest}>
                            <i className="icon mbc-icon alert circle"></i>Kindly add the PID6C39 as admin contributor in your gitHub repo and click here to verify.
                          </button>
                        )}
                      </div>
                      <p className={classNames(Styles.giturlsuccess, enableCreate ? '' : ' hide')}>
                        <i className="icon mbc-icon alert circle"></i>PID6C39 onboarded successfully.
                      </p>
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
                  <div className={Styles.disabledSection}>
                    <h4 className={classNames(Styles.sectionHeader)}>CI/CD (Coming Soon)</h4>
                    <div className={classNames(Styles.formWrapper, Styles.mT)}>
                      <div className={classNames(Styles.flex)}>
                        {/* <div className={classNames(Styles.col2)}>
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
                        </div> */}
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

                <div>
                  <h4 className={classNames(Styles.sectionHeader)}>Additional Services</h4>
                  <div className={classNames(Styles.formWrapper, Styles.mT)}>
                    <div className={classNames(Styles.flex)}>
                      <div className={classNames(Styles.col2)}>
                        <div className={classNames('input-field-group')}>
                          {/* <label className="input-label" htmlFor="additionalServicesSelect">
                            Select Additional Services
                          </label> */}
                          <div id="additionalServices" className="custom-select">
                            <select
                              id="additionalServicesSelect"
                              multiple={true}
                              required={false}
                              onChange={onAdditionalServicesChange}
                              value={selectedAdditionalServices?.map(service => service?.serviceName)}
                            >
                              {additionalServices?.map(service => 
                                <option key={service?.serviceName} value={service?.serviceName}>{service?.serviceName} {service?.version}</option>
                              )}
                            </select>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className={classNames(Styles.formWrapper, Styles.mT)}>
                  <div className={classNames(Styles.flex)}>
                    {selectedAdditionalServices?.map(service => 
                      <div key={service?.serviceName} className={classNames(Styles.col3)}>
                        <ServiceCard service={service} />
                      </div>
                    )}
                  </div>
                </div>

                <div className={Styles.btnConatiner}>
                  <button className={classNames(enableCreate ? 'btn-tertiary' : Styles.disableVerifyButton, 'btn')} type="button" disabled={!enableCreate} onClick={edit ? onUpdateRecipe : onCreateRecipe}>
                    {edit ? 'Update Recipe' : 'Create Recipe'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        {notificationMsg && 
          <Modal
            title="Public Visibility"
            hiddenTitle={true}
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
                <div className={Styles.publicWarning}>
                  <h4>Recipe Visibility</h4>
                  <p>The Recipe will be visible to all users. Are you sure to make it Public?</p>
                  <div>
                    <p><i className="icon mbc-icon alert circle"></i> Please make sure to check <pre>&apos;Template repository&apos;</pre> setting. You can follow below steps:</p>
                    <ol>
                      <li>Open your recipe repository</li>
                      <li>Once you&apos;re in the repository, look for the <pre>&apos;Settings&apos;</pre> tab at the top of the page and click on it.</li>
                      <li>In the <pre>&apos;Settings&apos;</pre> menu, click on <pre>&apos;General&apos;</pre> section.</li>
                      <li>In the <pre>&apos;General&apos;</pre> settings, you&apos;ll find an option labeled <pre>&apos;Template repository&apos;</pre>. Check the box next to it to enable this repository as a template.</li>
                    </ol>
                    <p>These steps will help you smoothly convert your repository into a template repository on GitHub and make your recipe template public.</p>
                  </div>
                </div>
                <div className="btn-set footerRight">
                  <button className="btn btn-primary" type="button" onClick={onNotificationMsgCancel}>Cancel</button>
                  <button className="btn btn-tertiary" type="button" onClick={onNotificationMsgAccept}>Confirm</button>
                </div>
              </div>
            }
          />
        }
      </div>
    </div>
  );
};

export default CodeSpaceRecipe;
