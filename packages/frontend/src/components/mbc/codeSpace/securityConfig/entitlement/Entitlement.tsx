import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';

import Styles from './Entitlement.scss';
// @ts-ignore
import Tooltip from '../../../../../assets/modules/uilab/js/src/tooltip';
import Notification from '../../../../../assets/modules/uilab/js/src/notification';
import Modal from '../../../../../components/formElements/modal/Modal';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import EntitlementSubList from './EntitlementSubList';
import Pagination from 'components/mbc/pagination/Pagination';
import { CODE_SPACE_DISABLE_DNA_PROTECT, CODE_SPACE_STATUS, SESSION_STORAGE_KEYS } from 'globals/constants';
import EditOrCreateEntitlement from './EditOrCreateEntitlement';

const classNames = cn.bind(Styles);

export interface IEntitlementProps {
  onSaveDraft: (tabToBeSaved: string, config: any) => void;
  id: string;
  config: any;
  readOnlyMode: boolean;
  isCodeSpaceAdminPage: boolean;
}

export interface IEntitlementState {
  showContextMenu: boolean;
  entitlementName: string;
  entitlemenPath: string;
  httpMethod: string;
  entitelmentList: any;
  contextMenuOffsetTop: number;
  contextMenuOffsetLeft: number;
  isCreateOrEditEntitlementModal: boolean;
  editEntitlementModal: boolean;
  entitlementNameErrorMessage: string;
  entitlementPathErrorMessage: string;
  entitlementHttpMethodErrorMessage: string;
  isProtectedByDna: boolean;
  isDnAProtectModal: boolean;
  showDeleteModal: boolean;
  deleteEntitlementName: string;
  totalNumberOfPages: number;
  currentPageNumber: number;
  maxItemsPerPage: number;
  entitelmentListResponse: any;
  config: any;
}

export default class Entitlement extends React.Component<IEntitlementProps, IEntitlementState> {
  constructor(props: IEntitlementProps) {
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
      isProtectedByDna: false,
      isDnAProtectModal: false,
      showDeleteModal: false,
      deleteEntitlementName: '',
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE)) || 10,
      entitelmentListResponse: [],
      config: {},
    };

    // Bind the method to the class instance in the constructor
    this.handleEntitementEdit = this.handleEntitementEdit.bind(this);
  }

  public componentDidUpdate(prevProps: IEntitlementProps, prevState: IEntitlementState) {
    // Check if the parent component has updated
    if (this.props.config !== prevProps.config) {
      if (this.props.config?.entitlements?.length > 0) {
        const records = this.props.config.entitlements;
        const prjIdSortVal = records?.sort(function (item1: any, item2: any) {
          return item1.name.localeCompare(item2.name);
        });
        const totalNumberOfPages = Math.ceil(records?.length / this.state.maxItemsPerPage);
        const modifiedData = prjIdSortVal.slice(0, this.state.maxItemsPerPage);
        this.setState({
          // entitelmentListResponse: this.props.config.entitlements,
          // entitelmentList: this.props.config.entitlements,
          entitelmentListResponse: prjIdSortVal,
          entitelmentList: modifiedData,
          totalNumberOfPages: totalNumberOfPages,
          config: this.props.config,
          isProtectedByDna: this.props.config.isProtectedByDna,
        });
      } else {
        this.setState({
          config: this.props.config,
          isProtectedByDna: this.props.config.isProtectedByDna,
        });
      }
    }
  }

  public componentDidMount() {
    Tooltip.defaultSetup();
  }

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected editCreateEditEntitlementModal = () => {
    this.setState({ editEntitlementModal: false, isCreateOrEditEntitlementModal: false });
  };

  protected onChangeHttp = (e: any) => {
    this.setState({ httpMethod: e.target.value, entitlementHttpMethodErrorMessage: '' });
  };

  protected onPersonalizationCheckBoxChange = (type: string) => {
    if (type === 'accept') {
      this.setState({ isProtectedByDna: false });
    } else if (type === 'cancel') {
      this.setState({ isProtectedByDna: true });
    }
    this.setState({ isDnAProtectModal: false });
  };

  protected validateEntitementForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (this.state.entitlementName?.trim()?.length === 0) {
      this.setState({ entitlementNameErrorMessage: errorMissingEntry });
      formValid = false;
    }

    if (this.state.isProtectedByDna && this.state.entitlemenPath?.trim()?.length === 0) {
      this.setState({ entitlementPathErrorMessage: errorMissingEntry });
      formValid = false;
    }

    if (this.state.isProtectedByDna && (this.state.httpMethod === '0' || this.state.httpMethod?.trim()?.length === 0)) {
      this.setState({ entitlementHttpMethodErrorMessage: errorMissingEntry });
      formValid = false;
    }

    return formValid;
  };

  protected handleDeleteEntitlement = () => {
    const updatedList = this.state.entitelmentList.filter(
      (item: any) => item.entitlementName !== this.state.deleteEntitlementName,
    );
    this.setState({
      entitelmentList: updatedList,
      showDeleteModal: false,
      deleteEntitlementName: '',
    });
  };

  protected handleEntitementAdd = (type: string) => {
    if (this.validateEntitementForm()) {
      const { entitelmentList, entitlementName, entitlemenPath, httpMethod, isProtectedByDna } = this.state;

      // Check if the item already exists in the list
      const existingItem = entitelmentList.find((item: any) => item.entitlementName === entitlementName);

      if (type === 'Update' && existingItem) {
        // Update the existing item
        const updatedList = entitelmentList.map((item: any) =>
          item.entitlementName === entitlementName ? { ...item, entitlementName, entitlemenPath, httpMethod } : item,
        );

        this.setState({
          entitelmentList: updatedList,
          isCreateOrEditEntitlementModal: false,
          entitlementName: '',
          entitlemenPath: '',
          httpMethod: '',
          isProtectedByDna: false,
          editEntitlementModal: false, // Set edit mode to false
        });
      } else {
        // Add a new item
        const updatedList = entitelmentList.concat({
          entitlementName,
          entitlemenPath,
          httpMethod,
          isProtectedByDna,
        });

        this.setState({
          entitelmentList: updatedList,
          isCreateOrEditEntitlementModal: false,
          entitlementName: '',
          entitlemenPath: '',
          httpMethod: '',
          isProtectedByDna: false,
        });
      }
    }
  };

  protected handleEntitementEdit = (editEntitlement: any) => {
    this.setState({
      editEntitlementModal: true,
      isCreateOrEditEntitlementModal: true,
      entitlementName: editEntitlement.entitlementName,
      entitlemenPath: editEntitlement.entitlemenPath,
      httpMethod: editEntitlement.httpMethod,
      isProtectedByDna: editEntitlement.isProtectedByDna,
    });
  };

  protected getRefreshedDagPermission = (name: string, dagIndex: number) => {};

  protected getProjectSorted = (entitel: any) => {
    this.setState({
      entitelmentList: entitel,
    });
  };

  protected updatedFinalEntitlementList = (entitelmentList: any) => {
    this.setState({
      entitelmentListResponse: entitelmentList,
    });
  };

  protected onPaginationPreviousClick = () => {
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

  protected onPaginationNextClick = () => {
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

  protected onViewByPageNum = (pageNum: number) => {
    const totalNumberOfPages = Math.ceil(this.state.entitelmentListResponse.length / pageNum);
    const modifiedData = this.state.entitelmentListResponse.slice(0, pageNum);
    this.setState({
      entitelmentList: modifiedData,
      totalNumberOfPages: totalNumberOfPages,
      currentPageNumber: 1,
      maxItemsPerPage: pageNum,
    });
  };

  protected updateEntitlement = (entitlementData: any) => {
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

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.provisionStyles)}>
          <div className={classNames(Styles.wrapper)}>
            <div className={classNames('decriptionSection', 'mbc-scroll')}>
              <h3 className={classNames(Styles.title)}>Entitlements for your application authorization</h3>
              <div className={Styles.parentEntitlement}>
                <div className={Styles.checkboxWrapper}>
                  <label
                    className={classNames(
                      'checkbox',
                      !CODE_SPACE_DISABLE_DNA_PROTECT.includes(this.state.config?.status) ? '' : 'hide',
                    )}
                  >
                    <span className="wrapper">
                      <input
                        type="checkbox"
                        className="ff-only"
                        checked={this.state.isProtectedByDna ? this.state.isProtectedByDna : false}
                        onChange={(e) => {
                          const checkboxValue = e.target.checked;
                          if (!e.target.checked) {
                            this.setState({ isDnAProtectModal: true });
                          }
                          this.setState({ isProtectedByDna: checkboxValue });
                        }}
                        disabled={
                          this.props.readOnlyMode || CODE_SPACE_DISABLE_DNA_PROTECT.includes(this.state.config?.status)
                        }
                      />
                    </span>
                    <span className={classNames('label')}>Do you want to DnA platform to protect your API's</span>
                  </label>
                  {!CODE_SPACE_STATUS.includes(this.state.config?.status) && !this.props.isCodeSpaceAdminPage && (
                    <p
                      style={{ color: 'var(--color-orange)' }}
                      className={classNames(this.props.readOnlyMode ? ' hidden' : '')}
                    >
                      <i className="icon mbc-icon alert circle"></i> Once the config is in published state, Can Add /
                      Edit Entitlement
                    </p>
                  )}
                </div>
                {!this.props.readOnlyMode ? (
                  <div className={classNames(Styles.createEntitlementButton)}>
                    <button
                      className={classNames('btn add-dataiku-container btn-primary', Styles.createButton)}
                      type="button"
                      onClick={() => {
                        this.setState({
                          isCreateOrEditEntitlementModal: true,
                        });
                      }}
                      title={
                        !CODE_SPACE_STATUS.includes(this.state.config?.status)
                          ? 'Once the config is in published state, can add New Entitlement.'
                          : ''
                      }
                      disabled={!CODE_SPACE_STATUS.includes(this.state.config?.status)}
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
                      isProtectedByDna={this.state?.isProtectedByDna}
                      listOfProject={this.state.entitelmentList}
                      status={this.state.config?.status}
                      getRefreshedDagPermission={this.getRefreshedDagPermission}
                      updatedFinalEntitlementList={this.updatedFinalEntitlementList}
                      getProjectSorted={this.getProjectSorted}
                    />
                  </div>
                  <Pagination
                    totalPages={this.state.totalNumberOfPages}
                    pageNumber={this.state.currentPageNumber}
                    onPreviousClick={this.onPaginationPreviousClick}
                    onNextClick={this.onPaginationNextClick}
                    onViewByNumbers={this.onViewByPageNum}
                    displayByPage={true}
                  />
                </>
              ) : (
                <div className={classNames('no-data', Styles.noData)}> No Entitlements found</div>
              )}
            </div>
          </div>
        </div>

        {this.state.isDnAProtectModal && (
          <ConfirmModal
            title={'ss'}
            acceptButtonTitle="Yes"
            cancelButtonTitle="No"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.isDnAProtectModal}
            content={
              <div className={Styles.deleteForecastResult}>
                <div className={Styles.closeIcon}>
                  <i className={classNames('icon mbc-icon close thin')} />
                </div>
                <div>
                  All API Path/Pattern and HTTP Method will be removed from the entitlement.
                  <br />
                  Are you sure you want to proceed?
                </div>
              </div>
            }
            onCancel={() => this.onPersonalizationCheckBoxChange('cancel')}
            onAccept={() => this.onPersonalizationCheckBoxChange('accept')}
          />
        )}

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
                isProtectedByDna={this.state.isProtectedByDna}
                submitEntitlement={(entitlementData: any) => this.updateEntitlement(entitlementData)}
                editEntitlementModal={this.state.editEntitlementModal}
                entitlementNameErrorMessage={this.state.entitlementNameErrorMessage}
              />
            }
            scrollableContent={true}
            onCancel={this.editCreateEditEntitlementModal}
          />
        )}
        {
          <div className="btnConatiner">
            <button className="btn btn-primary" type="button" onClick={this.onEntitlementSubmit}>
              {!CODE_SPACE_STATUS.includes(this.state.config?.status) || this.props.readOnlyMode
                ? 'Next'
                : 'Save & Next'}
            </button>
          </div>
        }
      </React.Fragment>
    );
  }

  protected onEntitlementSubmit = () => {
    let formValid = true;
    let isMissingApiList = false;
    if (this.state.isProtectedByDna) {
      this.state.entitelmentListResponse.map((item: any) => {
        if (item?.apiList?.length === 0 || item?.apiList?.length === undefined) {
          formValid = false;
          isMissingApiList = true;
        }
      });
      if (isMissingApiList) {
        this.showErrorNotification('API Path/Pattern and HTTP Method for Entitlements is Missing');
      }
    }
    if (formValid) {
      this.setState(
        {
          config: {
            ...this.state.config,
            entitlements: this.state.entitelmentListResponse,
            isProtectedByDna: this.state.isProtectedByDna,
          },
        },
        () => {
          this.props.onSaveDraft('entitlement', this.state.config);
        },
      );
    }
  };
}
