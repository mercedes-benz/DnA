import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
// import Button from '../../../common/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

import Styles from './Entitlement.scss';
// @ts-ignore
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import EntitlementSubList from './EntitlementSubList';
// import Pagination from 'components/mbc/pagination/Pagination';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import EditOrCreateEntitlement from './EditOrCreateEntitlement';
import SelectBox from 'dna-container/SelectBox';

const classNames = cn.bind(Styles);

// export interface IEntitlementProps {
//   onSaveDraft: (tabToBeSaved: string, config: any) => void;
//   onPublish: (config: any, env: string) => void;
//   env: string;
//   id: string;
//   config: any;
//   readOnlyMode: boolean;
//   projectName?: string;
// }

// export interface IEntitlementState {
//   showContextMenu: boolean;
//   entitlementName: string;
//   entitlemenPath: string;
//   httpMethod: string;
//   entitelmentList: any;
//   contextMenuOffsetTop: number;
//   contextMenuOffsetLeft: number;
//   isCreateOrEditEntitlementModal: boolean;
//   editEntitlementModal: boolean;
//   entitlementNameErrorMessage: string;
//   entitlementPathErrorMessage: string;
//   entitlementHttpMethodErrorMessage: string;
//   isDnAProtectModal: boolean;
//   showDeleteModal: boolean;
//   deleteEntitlementName: string;
//   totalNumberOfPages: number;
//   currentPageNumber: number;
//   maxItemsPerPage: number;
//   entitelmentListResponse: any;
//   config: any;
//   appId: string;
//   appIdErrorMessage: string;
// }

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
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE)) || 10,
      entitelmentListResponse: [],
      config: {},
      appId: '',
      appIdErrorMessage: '',
    };

    // Bind the method to the class instance in the constructor
    this.handleEntitementEdit = this.handleEntitementEdit.bind(this);
  }

  componentDidUpdate(prevProps) {
    // Check if the parent component has updated
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
  }

  componentDidMount() {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
  }

  showErrorNotification(message) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
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
    const errorMissingEntry = '*Missing entry';
    if (this.state.entitlementName?.trim()?.length === 0) {
      this.setState({ entitlementNameErrorMessage: errorMissingEntry });
      formValid = false;
    }

    if (this.state.entitlemenPath?.trim()?.length === 0) {
      this.setState({ entitlementPathErrorMessage: errorMissingEntry });
      formValid = false;
    }

    if (this.state.httpMethod === '0' || this.state.httpMethod?.trim()?.length === 0) {
      this.setState({ entitlementHttpMethodErrorMessage: errorMissingEntry });
      formValid = false;
    }

    return formValid;
  };

  handleDeleteEntitlement = () => {
    const updatedList = this.state.entitelmentList.filter(
      (item) => item.entitlementName !== this.state.deleteEntitlementName,
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

      // Check if the item already exists in the list
      const existingItem = entitelmentList.find((item) => item.entitlementName === entitlementName);

      if (type === 'Update' && existingItem) {
        // Update the existing item
        const updatedList = entitelmentList.map((item) =>
          item.entitlementName === entitlementName ? { ...item, entitlementName, entitlemenPath, httpMethod } : item,
        );

        this.setState({
          entitelmentList: updatedList,
          isCreateOrEditEntitlementModal: false,
          entitlementName: '',
          entitlemenPath: '',
          httpMethod: '',
          editEntitlementModal: false, // Set edit mode to false
        });
      } else {
        // Add a new item
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

  handleEntitementEdit = (editEntitlement) => {
    this.setState({
      editEntitlementModal: true,
      isCreateOrEditEntitlementModal: true,
      entitlementName: editEntitlement.entitlementName,
      entitlemenPath: editEntitlement.entitlemenPath,
      httpMethod: editEntitlement.httpMethod,
    });
  };

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
      this.state.maxItemsPerPage * currentPageNumberTemp,
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
      this.state.maxItemsPerPage * currentPageNumberTemp,
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
    // Flag to indicate if the entitlement already exists
    let entitlementExists = false;

    // Iterate over each object in entitelmentListResponse
    for (const responseItem of this.state.entitelmentListResponse) {
      // Check if the entitlement name exists in the current responseItem
      if (responseItem.name === entitlementData.name) {
        entitlementExists = true;
        break;
      }
    }

    if (entitlementExists) {
      // If the entitlement exists, set an error message
      this.setState({
        entitlementNameErrorMessage: 'An entitlement with this name already exists.',
      });
    } else {
      // If the entitlement is new, add it to the list
      const updatedEntitlementList = [...this.state.entitelmentList, entitlementData];

      // Decide how to add the new entitlement to entitelmentListResponse
      const updatedEntitelmentListListResponse = [
        ...this.state.entitelmentListResponse,
        {
          ...entitlementData,
        },
      ];

      // Update the state with the new entitlement list and close the modal.
      this.setState({
        entitelmentList: updatedEntitlementList,
        entitelmentListResponse: updatedEntitelmentListListResponse,
        isCreateOrEditEntitlementModal: false,
        editEntitlementModal: false,
        entitlementNameErrorMessage: '', // Clear any previous error message
      });
    }
  };

  render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.provisionStyles)}>
          <div className={classNames(Styles.wrapper)}>
            <div className={classNames('decriptionSection', 'mbc-scroll')}>
              <h3 className={classNames(Styles.title)}>
                {this.props.env === 'int'
                  ? 'Entitlements for your Staging application authorization'
                  : 'Entitlements for your Production application authorization'}
              </h3>
              <div className={Styles.parentEntitlement}>
                <div className={Styles.checkboxWrapper}>
                  <div
                    className={classNames(
                      ' input-field-group include-error ',
                      this.state?.appIdErrorMessage?.length ? 'error' : '',
                    )}
                  >
                    <label id="PrjName" htmlFor="PrjId" className="input-label">
                      {this.props.env === 'int' ? 'Staging ' : 'Production '} Application Id<sup>*</sup>
                    </label>
                    <input
                      type="text"
                      className="input-field"
                      required={this.props.readOnlyMode ? false : true}
                      id="AppId"
                      maxLength={50}
                      placeholder="Application id registered in Alice"
                      autoComplete="off"
                      onChange={(e) => {
                        e.target.value.length !== 0
                          ? this.setState({ appId: e.target.value, appIdErrorMessage: '' })
                          : this.setState({ appId: e.target.value, appIdErrorMessage: '*Missing entry' });
                      }}
                      value={this.state.appId}
                      readOnly={this.props.readOnlyMode}
                    />
                    <p
                    style={{ color: 'var(--color-orange)' }}
                    className={classNames(this.state?.appId === this.props?.config?.appId || !this.props?.config?.appId ? ' hide' : '')}
                  >
                    <i className="icon mbc-icon alert circle"></i> Please redeploy with the new client id and client secret for the application id changes to be reflected. Note that the old credentials will be used until then.
                  </p>
                    <span className={classNames('error-message', this.state?.appIdErrorMessage?.length ? '' : 'hide')}>
                      {this.state?.appIdErrorMessage}
                    </span>
                  </div>
                </div>
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
              {this.state.entitelmentListResponse?.length > 0 ? (
                <>
                  <div className={classNames(Styles.subList)}>
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
                  {/* <Pagination
                    totalPages={this.state.totalNumberOfPages}
                    pageNumber={this.state.currentPageNumber}
                    onPreviousClick={this.onPaginationPreviousClick}
                    onNextClick={this.onPaginationNextClick}
                    onViewByNumbers={this.onViewByPageNum}
                    displayByPage={true}
                  /> */}
                </>
              ) : (
                <div className={classNames('no-data', Styles.noData)}> No Entitlements found</div>
              )}
            </div>
          </div>
        </div>

        {this.state.showDeleteModal && (
          <ConfirmModal
            title={'Delete'}
            showAcceptButton={false}
            showCancelButton={false}
            show={this.state.showDeleteModal}
            removalConfirmation={true}
            showIcon={false}
            showCloseIcon={true}
            content={
              <div className={Styles.deleteForecastResult}>
                <div className={Styles.closeIcon}>
                  <i className={classNames('icon mbc-icon close thin')} />
                </div>
                <div>
                  You are going to delete the Entitlement which will be removed from roles and role-mappings as well.
                  <br />
                  Are you sure you want to proceed?
                </div>
                <br />
                <div className={Styles.deleteBtn}>
                  <button className={'btn btn-secondary'} type="button" onClick={this.handleDeleteEntitlement}>
                    Delete
                  </button>
                </div>
              </div>
            }
          />
        )}

        {this.state.isCreateOrEditEntitlementModal && (
          <Modal
            title={'Create a new Entitlement'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'80%'}
            buttonAlignment="right"
            show={this.state.isCreateOrEditEntitlementModal}
            content={
              <EditOrCreateEntitlement
                submitEntitlement={(entitlementData) => this.updateEntitlement(entitlementData)}
                editEntitlementModal={this.state.editEntitlementModal}
                entitlementNameErrorMessage={this.state.entitlementNameErrorMessage}
                projectName={this.props.projectName}
                env={this.props.env}
              />
            }
            scrollableContent={true}
            onCancel={this.editCreateEditEntitlementModal}
          />
        )}
        {!this.props.readOnlyMode && (
          <div className="btnConatiner">
            <div className="btn-set">
              <button className="btn btn-primary" type="button" onClick={this.onEntitlementSubmit}>
                {this.props.env === 'int' ? 'Save Staging' : 'Save Production'}
              </button>
              <button
                className={'btn btn-primary ' + classNames(Styles.publishBtn)}
                type="button"
                disabled={this.state.entitelmentListResponse.length === 0 || this.state.appId.length === 0 }
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

  onEntitlementSubmit = () => {
    let formValid = true;
    const isMissingApiList = false;
    let isMissingAppId = false;
    if (isMissingApiList) {
      this.showErrorNotification('API Path/Pattern and HTTP Method for Entitlements is Missing');
    }
    if (this.state.appId.length === 0) {
      formValid = false;
      isMissingAppId = true;
    }
    if (isMissingAppId) {
      this.showErrorNotification('Application Id is Missing');
    }
    if (formValid) {
      this.setState(
        {
          config: {
            ...this.state.config,
            entitlements: this.state.entitelmentListResponse,
            appId: this.state.appId,
          },
        },
        () => {
          this.props.onSaveDraft(
            this.props.env === 'int' ? 'stagingEntitlement' : 'productionEntitlement',
            this.state.config,
          );
        },
      );
    }
  };
  onEntitlementPublish = () => {
    let formValid = true;
    const isMissingApiList = false;
    let isMissingAppId = false;
    if (isMissingApiList) {
      this.showErrorNotification('API Path/Pattern and HTTP Method for Entitlements is Missing');
    }
    if (this.state.appId.length === 0) {
      formValid = false;
      isMissingAppId = true;
    }
    if (isMissingAppId) {
      this.showErrorNotification('Application Id is Missing');
    }
    if (formValid) {
      this.setState(
        {
          config: {
            ...this.state.config,
            entitlements: this.state.entitelmentListResponse,
            appId: this.state.appId,
          },
        },
        () => {
          this.props.onPublish(this.state.config, this.props.env);
        },
      );
    }
  };
}
