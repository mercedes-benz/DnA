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
      showDiscardModal: false, // Controls discard confirmation modal

      // JSON view state:
      showJson: false,
      publishedData: '', // The environment-specific published data
      jsonData: JSON.stringify(
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
        this.setState({ jsonData });
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

    
    this.setState((prevState) => ({
      showJson: !prevState.showJson,
      jsonData: this.props.readOnlyMode
        ? storedPublishedData || '// No Published Data Available'
        : this.state.jsonData,
      publishedData: storedPublishedData || '',
      isJsonTouched: false,
    }));
  }

  handleJsonChange(newValue) {
    try {
      this.setState({ isJsonTouched: true, jsonData: newValue });
  
      let parsedData;
      try {
        parsedData = JSON.parse(newValue);
      } catch (e) {
        throw new Error('Invalid JSON format: ' + e.message);
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
            !['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS', 'TRACE', 'CONNECT'].includes(
              httpMethod
            )
          ) {
            errors.push(`Error in entitlement ${index + 1}: Invalid HTTP Method`);
          }
        });
  
        if (errors.length === 0) {
          
          this.setState({
            entitelmentList: [...parsedData.entitlements], 
            entitelmentListResponse: [...parsedData.entitlements], 
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
    
    const jsonData = JSON.stringify(
      {
        appId: this.state.appId,
        entitlements: this.state.entitelmentListResponse,
      },
      null,
      2
    );

    this.setState({
      jsonData,
      isJsonTouched: false,
      jsonError: '',
      toggleError: '',
      showDiscardModal: false,
    });

    Notification.show('Changes discarded successfully');
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
        // Update the existing item
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

  getRefreshedDagPermission = () => {};
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
      this.showErrorNotification('Application Id is Missing');
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
                
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '10px',
                  }}
                >
                 <h3 className={classNames(Styles.title)} style={{ marginRight: 'auto', padding: '25px' }}>
  {this.props.env === 'int'
    ? 'Entitlements for your Staging application authorization'
    : 'Entitlements for your Production application authorization'}
</h3>

                  {!this.props.readOnlyMode && !this.state.showJson && (
                    <button
                      className="btn btn-primary"
                      type="button"
                      onClick={() =>
                        this.setState({
                          isCreateOrEditEntitlementModal: true,
                          editEntitlementModal: false,
                        })
                      }
                      style={{
                        backgroundColor: 'transparent',
                        color: 'white',
                        padding: '16px 20px',
                        fontSize: '16px',
                        borderRadius: '5px',
                        border: 'none',
                        cursor: 'pointer',
                        marginLeft: 'auto',
                      }}
                    >
                      + Create New Entitlement
                    </button>
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
                          e.target.value.length !== 0
                            ? this.setState({ appId: e.target.value, appIdErrorMessage: '' })
                            : this.setState({
                                appId: e.target.value,
                                appIdErrorMessage: '*Missing entry',
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

<p style={{ color: 'var(--color-orange)', marginTop: '10px', paddingLeft: '25px' }}>
  <i className="icon mbc-icon alert circle"></i> Please redeploy with the new
  client id and client secret for the application id changes to be reflected.
  Note that the old credentials will be used until then.
</p>


               
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
         
          <div className={classNames(Styles.jsonView)} style={{ display: 'flex', justifyContent: 'center' }}>
            <div className={classNames(Styles.wrapper)} style={{ maxWidth: '100%', width: '100%' }}>
              
              <div
                className={classNames(Styles.titleWrapper)}
                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    width: '100%',
                    marginBottom: '10px',
                  }}
                >
                  <h3 className={classNames(Styles.title)} style={{ margin: 0 }}>
                    {this.props.env === 'int'
                      ? 'Entitlements for your Staging application authorization'
                      : 'Entitlements for your Production application authorization'}
                  </h3>
                  {!this.props.readOnlyMode && !this.state.showJson && (
                    <button
                      className="btn btn-primary"
                      type="button"
                      onClick={() =>
                        this.setState({
                          isCreateOrEditEntitlementModal: true,
                          editEntitlementModal: false,
                        })
                      }
                      style={{
                        backgroundColor: '#007bff',
                        color: 'white',
                        padding: '16px 20px',
                        fontSize: '14px',
                        borderRadius: '5px',
                        border: 'none',
                        cursor: 'pointer',
                        marginLeft: 'auto',
                      }}
                    >
                      + Create Entitlement
                    </button>
                  )}
                </div>

                <button
                  onClick={() => {
                    navigator.clipboard.writeText(this.state.jsonData).then(() => {
                      Notification.show('Copied to Clipboard');
                    });
                  }}
                  className={classNames('btn btn-primary', Styles.actionBtn)}
                  type="button"
                  title="Copy JSON"
                  style={{ backgroundColor: 'transparent', border: 'none', marginLeft: '10px' }}
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
                style={{ marginTop: '5px', textAlign: 'left' }}
              />

              
              {this.state.jsonError && this.state.jsonError.length > 0 && (
                <div style={{ color: 'red', fontWeight: 'bold', marginTop: '10px', fontSize: '16px' }}>
                  {this.state.jsonError.map((error, idx) => (
                    <div key={idx}>{error}</div>
                  ))}
                </div>
              )}

            
              <div style={{ marginTop: '15px', fontSize: '14px', color: '#A9A9A9' }}>
                <ul style={{ listStyleType: 'disc' }}>
                  <li>
                    Ensure the <strong>appId</strong> field is unique and identifies the application.
                  </li>
                  <li>
                    Always start the <strong>apiPattern</strong> with <code>/api/</code>.
                  </li>
                  <li>
                    Use only valid HTTP methods:{' '}
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
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'flex-end',
                    alignItems: 'center',
                    marginTop: '15px',
                    marginBottom: '20px',
                    marginRight: '20px',
                  }}
                >
                  <button
                    onClick={() => this.setState({ showDiscardModal: true })}
                    className={classNames('btn', Styles.discardBtn)}
                    style={{
                      backgroundColor: 'transparent',
                      color: '#1E90FF',
                      border: '2px solid #1E90FF',
                      padding: '6px 12px',
                      fontSize: '14px',
                    }}
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
                    <div>
                      <p>Are you sure you want to discard changes?</p>
                      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '10px' }}>
                        <button
                          className="btn btn-secondary"
                          type="button"
                          onClick={() => this.setState({ showDiscardModal: false })}
                          style={{ marginRight: '10px' }}
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
            </div>
          </div>
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
          <div className="btn-set" style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
            <div className="btnConatiner">
              <button
                className="btn btn-primary"
                type="button"
                onClick={this.onEntitlementSubmit}
                style={{ marginLeft: '10px' }}
              >
                {this.props.env === 'int' ? 'Save Staging' : 'Save Production'}
              </button>
              <button
                className={'btn btn-primary ' + classNames(Styles.publishBtn)}
                type="button"
                onClick={this.onEntitlementPublish}
                style={{ marginLeft: '10px' }}
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
