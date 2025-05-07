import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-solarized_dark';
import Styles from './Entitlement.scss';
// @ts-ignore
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import EntitlementSubList from './EntitlementSubList';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import EditOrCreateEntitlement from './EditOrCreateEntitlement';
import SelectBox from 'dna-container/SelectBox';

const classNames = cn.bind(Styles);

export default class Entitlement extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showContextMenu: false,
      entitlementName: '',
      entitlemenPath: '',
      httpMethod: '',
      entitelmentList: [],
      contextMenuOffsetTop: 0,
      contextMenuOffsetLeft: 0,
      isCreateOrEditEntitlementModal: false,
      editEntitlementModal: false,
      entitlementNameErrorMessage: '',
      entitlementPathErrorMessage: '',
      entitlementHttpMethodErrorMessage: '',
      isDnAProtectModal: false,
      showDeleteModal: false,
      deleteEntitlementName: '',
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      maxItemsPerPage:
        parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE)) || 10,
      entitelmentListResponse: [],
      config: {},
      appId: '',
      appIdErrorMessage: '',
      showDiscardModal: false,

      showJson: false,
      publishedData: '',
      jsonData: JSON.stringify(
        {
          appId: (props.config && props.config.appId) || '',
          entitlements: (props.config && props.config.entitlements) || [],
        },
        null,
        2
      ),
      originalJsonData: JSON.stringify(
        {
          appId: (props.config && props.config.appId) || '',
          entitlements: (props.config && props.config.entitlements) || [],
        },
        null,
        2
      ),
      jsonError: '',
      isJsonTouched: false,
      toggleError: ''
    };

    this.handleEntitementEdit = this.handleEntitementEdit.bind(this);
    this.handleToggle = this.handleToggle.bind(this);
    this.handleJsonChange = this.handleJsonChange.bind(this);
    this.onSave = this.onSave.bind(this);
    this.onDiscard = this.onDiscard.bind(this);
    this.confirmDiscard = this.confirmDiscard.bind(this);
  }

  componentDidMount() {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.props.config !== prevProps.config) {
      if (this.props.config?.entitlements?.length > 0) {
        const records = this.props.config.entitlements;
        const totalNumberOfPages = Math.ceil(records?.length / this.state.maxItemsPerPage);
        this.setState({
          entitelmentListResponse: this.props.config.entitlements,
          entitelmentList: this.props.config.entitlements,
          totalNumberOfPages: totalNumberOfPages,
          config: this.props.config,
          appId: this.props.config.appId,
        });
      } else {
        this.setState({
          config: this.props.config,
          appId: this.props.config.appId,
        });
      }
    }

    if (
      !this.state.isJsonTouched &&
      (prevState.appId !== this.state.appId ||
        prevState.entitelmentListResponse !== this.state.entitelmentListResponse)
    ) {
      const jsonData = JSON.stringify(
        {
          appId: this.state.appId,
          entitlements: this.state.entitelmentListResponse,
        },
        null,
        2
      );
      if (jsonData !== this.state.jsonData) {
        this.setState({ jsonData, originalJsonData: jsonData });
      }
    }
  }
  showErrorNotification(message) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }


  handleToggle() {
    const envKey =
      this.props.env === 'int' ? 'publishedData_staging' : 'publishedData_production';
    const storedPublishedData = localStorage.getItem(envKey);

    const showJsonNext = !this.state.showJson;

    if (!showJsonNext && this.state.isJsonTouched) {
      try {
        const parsedData = JSON.parse(this.state.jsonData);
        this.setState({
          showJson: showJsonNext,
          appId: parsedData.appId || '',
          entitelmentList: parsedData.entitlements || [],
          entitelmentListResponse: parsedData.entitlements || [],
          jsonError: '',
          appIdErrorMessage: parsedData.appId?.trim()?.length ? '' : '*Missing entry',
          isJsonTouched: false,
        });
      } catch (e) {
        this.setState({
          showJson: showJsonNext,
          jsonError: ['Invalid JSON format: ' + e.message],
        });
      }
    } else {
      this.setState({
        showJson: showJsonNext,
        jsonData: this.props.readOnlyMode
          ? storedPublishedData || '// No Published Data Available'
          : this.state.jsonData,
        publishedData: storedPublishedData || '',
        isJsonTouched: false,
        originalJsonData: this.state.jsonData,
      });
    }
  }


  handleJsonChange(newValue) {
    try {
      this.setState({ isJsonTouched: true, jsonData: newValue });

      let parsedData;
      try {
        parsedData = JSON.parse(newValue);
      } catch (e) {
        this.setState({ jsonError: ['Invalid JSON format: ' + e.message] });
        return;
      }

      let errors = [];
      if (parsedData.entitlements) {
        parsedData.entitlements.forEach((entitlement, index) => {
          const { apiPattern, httpMethod } = entitlement;

          if (apiPattern && !apiPattern.startsWith('/api/')) {
            errors.push(`Error in entitlement ${index + 1}: API Path should start with '/api/'`);
          }

          if (
            httpMethod &&
            !['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS', 'TRACE', 'CONNECT'].includes(httpMethod)
          ) {
            errors.push(`Error in entitlement ${index + 1}: Invalid HTTP Method`);
          }
        });

        if (errors.length === 0) {
          this.setState({
            entitelmentList: [...parsedData.entitlements],
            entitelmentListResponse: [...parsedData.entitlements],
            jsonError: [],
            appId: parsedData.appId
          });
        } else {
          this.setState({ jsonError: errors });
        }
      }
    } catch (error) {
      console.error('Error during JSON change:', error);
      this.setState({ jsonError: [error.message] });
    }
  }


  onSave() {
    if (this.state.jsonError?.length === 0) {
      try {
        const parsedData = JSON.parse(this.state.jsonData);
        const newAppId = parsedData.appId;
        const newEntitlements = parsedData.entitlements;

        this.setState({
          appId: newAppId,
          entitelmentListResponse: newEntitlements,
          entitelmentList: newEntitlements,
          isJsonTouched: false,
          toggleError: '',
        });

        Notification.show('JSON changes saved successfully');

        if (this.props.onSaveDraft) {
          this.props.onSaveDraft(this.props.env, {
            ...this.state.config,
            entitlements: newEntitlements,
            appId: newAppId,
          });
        }
      } catch (e) {
        Notification.show('Invalid JSON. Please correct the errors.', 'alert');
      }
    } else {
      Notification.show('Please fix the JSON errors before saving.', 'alert');
    }
  }

  onDiscard() {
    this.setState({ showDiscardModal: true });
  }

  confirmDiscard() {
    try {
      const parsedOriginal = JSON.parse(this.state.originalJsonData);

      this.setState({
        jsonData: this.state.originalJsonData,
        isJsonTouched: false,
        jsonError: '',
        toggleError: '',
        showDiscardModal: false,
        appId: parsedOriginal.appId || '',
        entitelmentList: parsedOriginal.entitlements || [],
        entitelmentListResponse: parsedOriginal.entitlements || [],
      });

      Notification.show('Changes discarded successfully');
    } catch (e) {
      this.setState({
        jsonError: ['Error while discarding changes: ' + e.message],
        showDiscardModal: false,
      });
    }
  }

  editCreateEditEntitlementModal = () => {
    this.setState({
      editEntitlementModal: false,
      isCreateOrEditEntitlementModal: false,
      entitlementNameErrorMessage: '',
    });
  };

  onChangeHttp = (e) => {
    this.setState({ httpMethod: e.target.value, entitlementHttpMethodErrorMessage: '' });
  };

  validateEntitementForm = () => {
    let formValid = true;
    const errorMissing = '*Missing entry';

    if (this.state.entitlementName?.trim()?.length === 0) {
      this.setState({ entitlementNameErrorMessage: errorMissing });
      formValid = false;
    }

    if (this.state.entitlemenPath?.trim()?.length === 0) {
      this.setState({ entitlementPathErrorMessage: errorMissing });
      formValid = false;
    }

    if (this.state.httpMethod === '0' || this.state.httpMethod?.trim()?.length === 0) {
      this.setState({ entitlementHttpMethodErrorMessage: errorMissing });
      formValid = false;
    }

    return formValid;
  };

  handleDeleteEntitlement = () => {

    const updatedList = this.state.entitelmentList.filter(
      (item) => item.entitlementName !== this.state.deleteEntitlementName
    );
    this.setState({
      entitelmentList: updatedList,
      showDeleteModal: false,
      deleteEntitlementName: '',
    });
  };

  handleEntitementAdd = (type) => {
    if (this.validateEntitementForm()) {
      const { entitelmentList, entitlementName, entitlemenPath, httpMethod } = this.state;
      const existingItem = entitelmentList.find((item) => item.entitlementName === entitlementName);

      if (type === 'Update' && existingItem) {

        const updatedList = entitelmentList.map((item) =>
          item.entitlementName === entitlementName
            ? { ...item, entitlementName, entitlemenPath, httpMethod }
            : item
        );
        this.setState({
          entitelmentList: updatedList,
          isCreateOrEditEntitlementModal: false,
          entitlementName: '',
          entitlemenPath: '',
          httpMethod: '',
          editEntitlementModal: false,
        });
      } else {

        const updatedList = entitelmentList.concat({
          entitlementName,
          entitlemenPath,
          httpMethod,
        });
        this.setState({
          entitelmentList: updatedList,
          isCreateOrEditEntitlementModal: false,
          entitlementName: '',
          entitlemenPath: '',
          httpMethod: '',
        });
      }
    }
  };

  handleEntitementEdit(editEntitlement) {
    this.setState({
      editEntitlementModal: true,
      isCreateOrEditEntitlementModal: true,
      entitlementName: editEntitlement.entitlementName,
      entitlemenPath: editEntitlement.entitlemenPath,
      httpMethod: editEntitlement.httpMethod,
    });
  }

  getRefreshedDagPermission = () => { };
  getProjectSorted = (entitel) => {
    this.setState({
      entitelmentList: entitel,
    });
  };

  updatedFinalEntitlementList = (entitelmentList) => {
    this.setState({
      entitelmentListResponse: entitelmentList,
    });
  };


  onPaginationPreviousClick = () => {
    const currentPageNumberTemp = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * this.state.maxItemsPerPage;
    const modifiedData = this.state.entitelmentListResponse.slice(
      currentPageOffset,
      this.state.maxItemsPerPage * currentPageNumberTemp
    );
    this.setState({
      entitelmentList: modifiedData,
      currentPageNumber: currentPageNumberTemp,
    });
  };

  onPaginationNextClick = () => {
    let currentPageNumberTemp = this.state.currentPageNumber;
    const currentPageOffset = this.state.currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumberTemp = this.state.currentPageNumber + 1;
    const modifiedData = this.state.entitelmentListResponse.slice(
      currentPageOffset,
      this.state.maxItemsPerPage * currentPageNumberTemp
    );
    this.setState({
      entitelmentList: modifiedData,
      currentPageNumber: currentPageNumberTemp,
    });
  };

  onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(this.state.entitelmentListResponse.length / pageNum);
    const modifiedData = this.state.entitelmentListResponse.slice(0, pageNum);
    this.setState({
      entitelmentList: modifiedData,
      totalNumberOfPages: totalNumberOfPages,
      currentPageNumber: 1,
      maxItemsPerPage: pageNum,
    });
  };

  updateEntitlement = (entitlementData) => {
    let entitlementExists = false;
    for (const responseItem of this.state.entitelmentListResponse) {
      if (responseItem.name === entitlementData.name) {
        entitlementExists = true;
        break;
      }
    }

    if (entitlementExists) {
      this.setState({
        entitlementNameErrorMessage: 'An entitlement with this name already exists.',
      });
    } else {
      const updatedEntitlementList = [...this.state.entitelmentList, entitlementData];
      const updatedEntitelmentListListResponse = [
        ...this.state.entitelmentListResponse,
        { ...entitlementData },
      ];
      this.setState({
        entitelmentList: updatedEntitlementList,
        entitelmentListResponse: updatedEntitelmentListListResponse,
        isCreateOrEditEntitlementModal: false,
        editEntitlementModal: false,
        entitlementNameErrorMessage: '',
      });
    }
  };


  onEntitlementSubmit = () => {
    let formValid = true;
    if (this.state.appId.trim().length === 0) {
      formValid = false;
      this.setState({ appIdErrorMessage: '*Missing entry' });
      this.showErrorNotification('Application Id is Missing');
    } else {
      this.setState({ appIdErrorMessage: '' });
    }


    if (formValid) {
      let newAppId = this.state.appId;
      let newEntitlements = this.state.entitelmentListResponse;


      if (this.state.showJson) {
        try {
          const parsedData = JSON.parse(this.state.jsonData);
          newAppId = parsedData.appId || newAppId;
          newEntitlements = parsedData.entitlements || newEntitlements;

          this.setState({
            appId: newAppId,
            entitelmentListResponse: newEntitlements,
            entitelmentList: newEntitlements,
            isJsonTouched: false,
          });
        } catch (e) {
          this.showErrorNotification('Invalid JSON. Please correct the errors before saving.');
          return;
        }
      }
      this.setState(
        {
          config: {
            ...this.state.config,
            entitlements: newEntitlements,
            appId: newAppId,
          },
        },
        () => {
          this.props.onSaveDraft(
            this.props.env === 'int' ? 'stagingEntitlement' : 'productionEntitlement',
            this.state.config
          );
          Notification.show(
            this.props.env === 'int'
              ? 'Staging Saved Successfully'
              : 'Production Saved Successfully'
          );
        }
      );
    }
  };


  onEntitlementPublish = () => {
    let formValid = true;
    if (!this.state.appId) {
      formValid = false;
      this.showErrorNotification('Application Id is Missing');
    }

    if (formValid) {

      const newPublishData = {
        appId: this.state.appId,
        entitlements: this.state.entitelmentListResponse,
      };


      const envKey = this.props.env === 'int'
        ? 'publishedData_staging'
        : 'publishedData_production';

      const publishedJson = JSON.stringify(newPublishData, null, 2);
      localStorage.setItem(envKey, publishedJson);


      this.setState(
        {
          config: {
            ...this.state.config,
            entitlements: newPublishData.entitlements,
            appId: this.state.appId,
          },
          publishedData: publishedJson,
          jsonData: publishedJson,
          isJsonTouched: false,
        },
        () => {

          this.props.onPublish(this.state.config, this.props.env);
          Notification.show(
            this.props.env === 'int'
              ? 'Staging Published Successfully'
              : 'Production Published Successfully'
          );
        }
      );
    }
  };
  render() {
    return (
      <React.Fragment>

        <div className={classNames(Styles.toggleSwitch)}>
          <label className={classNames('switch', this.state.showJson ? 'on' : '')}>
            <span className="label" style={{ marginRight: '5px' }}>Show JSON</span>
            <span className="wrapper">
              <input
                type="checkbox"
                onChange={this.handleToggle}
                checked={this.state.showJson}
              />
            </span>
          </label>
        </div>


        {!this.state.showJson ? (
          <div className={classNames(Styles.provisionStyles)}>
            <div className={classNames(Styles.wrapper)}>
              <div className={classNames('decriptionSection', 'mbc-scroll')}>

                <div>
                  <h3 className={classNames(Styles.title)} >
                    {this.props.env === 'int'
                      ? 'Entitlements for your Staging application authorization'
                      : 'Entitlements for your Production application authorization'}
                  </h3>

                  {!this.props.readOnlyMode && this?.state?.appId?.length ? (
                    <div className={classNames(Styles.createEntitlementButton)}>
                      <button
                        className={classNames('btn add-dataiku-container btn-primary', Styles.createButton)}
                        type="button"
                        onClick={() => {
                          this.setState({
                            isCreateOrEditEntitlementModal: true,
                          });
                        }}
                      >
                        <i className="icon mbc-icon plus" />
                        <span>Create New Entitlement</span>
                      </button>
                    </div>
                  ) : (
                    ''
                  )}
                </div>

                <div className={Styles.parentEntitlement}>
                  <div className={Styles.checkboxWrapper}>
                    <div
                      className={classNames(
                        'input-field-group include-error',
                        this.state?.appIdErrorMessage?.length ? 'error' : ''
                      )}
                    >
                      <label htmlFor="AppId" className="input-label">
                        {this.props.env === 'int'
                          ? 'Staging '
                          : 'Production '}
                        Application Id<sup>*</sup>
                      </label>
                      <input
                        type="text"
                        className="input-field"
                        required={!this.props.readOnlyMode}
                        id="AppId"
                        maxLength={50}
                        placeholder="Application id registered in Alice"
                        autoComplete="off"
                        onChange={(e) => {
                          const val = e.target.value.trim();
                          this.setState({
                            appId: e.target.value,
                            appIdErrorMessage: val.length === 0 ? '*Missing entry' : '',
                          });
                        }}

                        value={this.state.appId}
                        readOnly={this.props.readOnlyMode}
                      />
                      <span
                        className={classNames(
                          'error-message',
                          this.state?.appIdErrorMessage?.length ? '' : 'hide'
                        )}
                      >
                        {this.state?.appIdErrorMessage}
                      </span>
                    </div>
                  </div>
                </div>

                {this.state.appId &&
                  this.props.config?.appId &&
                  this.state.appId !== this.props.config.appId && (
                    <p className={classNames(Styles.alertMessage)}>
                      <i className="icon mbc-icon alert circle"></i> Please redeploy with the new
                      client id and client secret for the application id changes to be reflected.
                      Note that the old credentials will be used until then.
                    </p>
                  )}

                {this.state.entitelmentListResponse?.length > 0 ? (
                  <div className={classNames(Styles.subList)} style={{ padding: '25px' }}>
                    <EntitlementSubList
                      readOnlyMode={this.props.readOnlyMode}
                      entitelmentListResponse={this.state.entitelmentListResponse}
                      listOfProject={this.state.entitelmentList}
                      getRefreshedDagPermission={this.getRefreshedDagPermission}
                      updatedFinalEntitlementList={this.updatedFinalEntitlementList}
                      getProjectSorted={this.getProjectSorted}
                      projectName={this.props.projectName}
                      env={this.props.env}
                    />
                  </div>

                ) : (
                  <div className={classNames('no-data', Styles.noData)}>
                    No Entitlements found
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (

          <div className={classNames(Styles.jsonView)} >
            <div className={classNames(Styles.wrapper)} >

              <div
                className={classNames(Styles.titleWrapper)}
              >
                <div>
                  <h3 className={classNames(Styles.title)} >
                    {this.props.env === 'int'
                      ? 'Entitlements for your Staging application authorization'
                      : 'Entitlements for your Production application authorization'}
                  </h3>
                </div>

                <button
                  onClick={() => {
                    navigator.clipboard.writeText(this.state.jsonData).then(() => {
                      Notification.show('Copied to Clipboard');
                    });
                  }}
                  className={classNames('btn-primary', Styles.actionBtn)}
                  type="button"
                  title="Copy JSON"
                >
                  <i className={classNames('icon mbc-icon copy', Styles.copyIcon)} />
                </button>

              </div>
              <AceEditor
                mode="json"
                theme="solarized_dark"
                readOnly={this.props.readOnlyMode}
                onChange={this.props.readOnlyMode ? undefined : this.handleJsonChange}
                value={
                  this.props.readOnlyMode
                    ? this.state.publishedData || '// No Published Data Available'
                    : this.state.jsonData
                }
                fontSize={15}
                name="json_editor"
                editorProps={{ $blockScrolling: true }}
                width="100%"
                height="400px"
                showPrintMargin={false}
                setOptions={{
                  useWorker: false,
                  showLineNumbers: true,
                  tabSize: 2,
                  indentUnit: 2,
                  wrap: true,
                  softTabs: true,
                  lineHeight: 1.5,
                }}
                className={classNames(Styles.editor)}
              />
              {this.state.jsonError && this.state.jsonError.length > 0 && (
                <div className={classNames(Styles.jsonError)}>
                  {this.state.jsonError.map((error, idx) => (
                    <div key={idx}>{error}</div>
                  ))}
                </div>
              )}
              <div className={classNames(Styles.instructionContainer)}>
                <ul className={classNames(Styles.instructionList)}>
                  <li>
                    Ensure the <strong>appId</strong> field is unique and identifies the application.
                  </li>
                  <li>
                    Always start the <strong>apiPattern</strong> with <code>/api/</code>.
                  </li>
                  <li>
                    Use only valid HTTP methods: {' '}
                    <strong>GET</strong>, <strong>POST</strong>, <strong>PUT</strong>, <strong>DELETE</strong>,{' '}
                    <strong>PATCH</strong>, <strong>HEAD</strong>, <strong>OPTIONS</strong>, <strong>TRACE</strong>,{' '}
                    <strong>CONNECT</strong>.
                  </li>
                  <li>
                    Each entitlement name should be an <strong>array of strings</strong>.
                  </li>
                </ul>
              </div>

              {!this.props.readOnlyMode && this.state.isJsonTouched && (
                <div className={classNames(Styles.discardBtnContainer)}>
                  <button
                    onClick={() => this.setState({ showDiscardModal: true })}
                    className={classNames('btn', Styles.discardBtn)}
                  >
                    Discard Changes
                  </button>
                </div>
              )}
              {this.state.showDiscardModal && (
                <ConfirmModal
                  title="Discard Changes"
                  showAcceptButton={false}
                  showCancelButton={false}
                  show={this.state.showDiscardModal}
                  showIcon={false}
                  showCloseIcon={true}
                  content={
                    <div className={classNames(Styles.confirmModalContent)}>
                      <p>Are you sure you want to discard changes?</p>
                      <div className={classNames(Styles.confirmModalButtons)}>
                        <button
                          className="btn btn-secondary"
                          type="button"
                          onClick={() => this.setState({ showDiscardModal: false })}
                        >
                          No
                        </button>
                        <button
                          className="btn btn-primary"
                          type="button"
                          onClick={this.confirmDiscard}
                        >
                          Yes
                        </button>
                      </div>
                    </div>
                  }
                />
              )}
            </div> </div>
        )}
        {this.state.isCreateOrEditEntitlementModal && (
          <Modal
            title={this.state.editEntitlementModal ? 'Edit Entitlement' : 'Create New Entitlement'}
            showAcceptButton={false}
            showCancelButton={false}
            show={this.state.isCreateOrEditEntitlementModal}
            content={
              <EditOrCreateEntitlement
                submitEntitlement={this.updateEntitlement}
                editEntitlementModal={this.state.editEntitlementModal}
                entitlementNameErrorMessage={this.state.entitlementNameErrorMessage}
                projectName={this.props.projectName}
                env={this.props.env}
              />
            }
            scrollableContent={true}
            onCancel={() => this.setState({ isCreateOrEditEntitlementModal: false })}
          />
        )}
        {!this.props.readOnlyMode && (
          <div className="btnConatiner">
            <div className="btn-set">
              <button className="btn btn-primary" type="button" onClick={this.onEntitlementSubmit}>
                {this.props.env === 'int' ? 'Save Staging' : 'Save Production'}
              </button>
              <button
                className={'btn btn-tertiary ' + classNames(Styles.publishBtn)}
                type="button"
                onClick={this.onEntitlementPublish}
              >
                {this.props.env === 'int' ? 'Publish Staging' : 'Publish Production'}
              </button>
            </div>
          </div>
        )}
      </React.Fragment>
    );
  }
}


