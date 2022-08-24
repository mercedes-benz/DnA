import classNames from 'classnames';
import * as React from 'react';
import Styles from './Customer.scss';
import Modal from '../../../formElements/modal/Modal';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import { IconAvatarNew } from '../../../../components/icons/IconAvatarNew';
import { ITeams, ICustomers, IDepartment, IHierarchies, IRessort, ICustomerDetails } from '../../../../globals/types';
import AddTeamMemberModal from '../../addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from '../../addTeamMember/teamMemberListItem/TeamMemberListItem';
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { ErrorMsg } from '../../../../globals/Enums';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';
import TextArea from '../../shared/textArea/TextArea';

export interface ICustomerProps {
  customer: ICustomers;
  hierarchies: IHierarchies[];
  departments: IDepartment[];
  ressort: IRessort[];
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyCustomer: (modifyCustomer: ICustomers) => void;
}
export interface ICustomerState {
  customer: ICustomers;
  customerInfo: ICustomerDetails;
  comment: string;
  errors: ICustomerDetails;
  addCustomer: boolean;
  editCustomer: boolean;
  editCustomerIndex: number;
  showAddTeamMemberModal: boolean;
  editTeamMember: boolean;
  editTeamMemberIndex: number;
  teamMemberObj: ITeams;
  addProductOwnerInController: boolean;
  currentColumnToSort: string;
  currentSortOrder: string;
  nextSortOrder: string;
  duplicateCustomerAdded: boolean;
  customerTabError: string;
  showDeleteModal: boolean;
}
export default class Customer extends React.Component<ICustomerProps, ICustomerState> {
  public static getDerivedStateFromProps(props: ICustomerProps, state: ICustomerState) {
    return {
      customer: props.customer,
    };
  }
  constructor(props: ICustomerProps) {
    super(props);
    this.state = {
      customer: {
        customerDetails: [],
        processOwners: [],
      },
      customerInfo: {
        hierarchy: '',
        department: '',
        ressort: '',
        comment: '',
      },
      errors: {
        hierarchy: '',
        department: '',
        ressort: '',
        comment: '',
      },
      comment: null,
      addCustomer: false,
      editCustomer: false,
      editCustomerIndex: -1,
      showAddTeamMemberModal: false,
      editTeamMember: false,
      editTeamMemberIndex: -1,
      teamMemberObj: {
        shortId: '',
        company: '',
        department: '',
        email: '',
        firstName: '',
        lastName: '',
        userType: '',
        teamMemberPosition: '',
      },
      addProductOwnerInController: true,
      currentColumnToSort: 'customerName',
      currentSortOrder: 'desc',
      nextSortOrder: 'asc',
      duplicateCustomerAdded: false,
      customerTabError: '',
      showDeleteModal: false,
    };
  }
  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();

  public componentDidMount() {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }

  public render() {
    const requiredError = '*Missing entry';
    const addCustomerModelContent = (
      <div className={Styles.addCustomerModelContent}>
        <br />
        <div>
          <div className={Styles.flexLayout}>
            <div>
              <div
                className={classNames('input-field-group include-error', this.state.errors.hierarchy ? 'error' : '')}
              >
                <label id="hirarchyLabel" htmlFor="hierarchyField" className="input-label">
                  Hierarchy<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="hierarchyField"
                    name="hierarchy"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    value={this.state.customerInfo.hierarchy || ''}
                    onChange={this.handleChange}
                  >
                    <option value={''}>Choose</option>
                    {this.props.hierarchies?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.hierarchy ? '' : 'hide')}>
                  {this.state.errors.hierarchy}
                </span>
              </div>
            </div>
            <div>
              <div
                className={classNames('input-field-group include-error', this.state.errors.department ? 'error' : '')}
              >
                <label id="departmentLabel" htmlFor="departmentField" className="input-label">
                  Department<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="departmentField"
                    name="department"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    value={this.state.customerInfo.department || ''}
                    onChange={this.handleChange}
                  >
                    <option value={''}>Choose</option>
                    {this.props.departments?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.department?.length ? '' : 'hide')}>
                  {this.state.errors.department}
                </span>
              </div>
            </div>
          </div>
          <div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  this.state.errors.ressort?.length ? 'error' : '',
                )}
              >
                <label id="ressortLabel" htmlFor="ressortField" className="input-label">
                  Ressort<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="ressortField"
                    name="ressort"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    value={this.state.customerInfo.ressort || ''}
                    onChange={this.handleChange}
                  >
                    <option value={''}>Choose</option>
                    {this.props.ressort?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.ressort?.length ? '' : 'hide')}>
                  {this.state.errors.ressort}
                </span>
              </div>
            </div>
          </div>
          <div>
            <TextArea
              controlId={'customerComment'}
              containerId={'customerComment'}
              name={'comment'}
              labelId={'customerCommentLabel'}
              label={'Comment'}
              rows={50}
              value={this.state.customerInfo.comment}
              required={false}
              onChange={this.handleChange}
              onBlur={this.validateCustomerModal}
            />
            {this.state.duplicateCustomerAdded ? <span className={'error-message'}>Customer already exist</span> : ''}
            <div className="btnConatiner">
              <button
                className="btn btn-primary"
                type="button"
                onClick={this.state.addCustomer ? this.onAddCustomer : this.onEditCustomer}
              >
                {this.state.addCustomer ? 'Add' : this.state.editCustomer && 'Save'}
              </button>
            </div>
          </div>
        </div>
      </div>
    );

    const developerTeamMembersList = this.state.customer.processOwners
      ? this.state.customer.processOwners?.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.customer.processOwners?.length}
              onMoveUp={this.onTeamMemberMoveUp}
              onMoveDown={this.onTeamMemberMoveDown}
              onEdit={this.onSharingTeamMemberEdit}
              onDelete={this.onSharingTeamMemberDelete}
            />
          );
        })
      : [];

    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Customer</div>
        <div className={Styles.modalContent}>This customer will be deleted permanently.</div>
      </div>
    );

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Customer</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={classNames('expanstion-table', Styles.listOfCustomerTable)}>
                <div className={Styles.customerGrp}>
                  <div className={Styles.customerGrpList}>
                    <div className={Styles.customerGrpListItem}>
                      {this.state.customer.customerDetails?.length ? (
                        <div className={Styles.customerCaption}>
                          <div className={Styles.customerTile}>
                            <div className={Styles.customerTitleCol}>
                              <label>Customer No.</label>
                            </div>
                            <div className={Styles.customerTitleCol}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort === 'hierarchy' ? this.state.currentSortOrder : '')
                                }
                                onClick={this.sortByColumn('hierarchy', this.state.nextSortOrder)}
                              >
                                <i className="icon sort" />
                                Hierarchy
                              </label>
                            </div>
                            <div className={Styles.customerTitleCol}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort === 'ressort' ? this.state.currentSortOrder : '')
                                }
                                onClick={this.sortByColumn('ressort', this.state.nextSortOrder)}
                              >
                                <i className="icon sort" />
                                Ressort
                              </label>
                            </div>
                            <div className={Styles.customerTitleCol}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort === 'department' ? this.state.currentSortOrder : '')
                                }
                                onClick={this.sortByColumn('department', this.state.nextSortOrder)}
                              >
                                <i className="icon sort" />
                                Department
                              </label>
                            </div>
                            <div className={Styles.customerTitleCol}>Action</div>
                          </div>
                        </div>
                      ) : (
                        ''
                      )}
                      {this.state.customer.customerDetails?.map((customer, index) => {
                        return (
                          <div
                            key={index}
                            className={'expansion-panel-group airflowexpansionPanel ' + Styles.customerGrpListItemPanel}
                          >
                            <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                              <span className="animation-wrapper"></span>
                              <input type="checkbox" id={index + '1'} defaultChecked={index === 0} />
                              <label
                                className={Styles.expansionLabel + ' expansion-panel-label '}
                                htmlFor={index + '1'}
                              >
                                <div className={Styles.customerTile}>
                                  <div className={Styles.customerTitleCol}>{`Customer ${index + 1}`}</div>
                                  <div className={Styles.customerTitleCol}>{customer.hierarchy || '-'}</div>
                                  <div className={Styles.customerTitleCol}>{customer.ressort || '-'}</div>
                                  <div className={Styles.customerTitleCol}>{customer.department || '-'}</div>
                                  <div className={Styles.customerTitleCol}></div>
                                </div>
                                <i tooltip-data="Expand" className="icon down-up-flip"></i>
                              </label>
                              <div className="expansion-panel-content">
                                <div className={Styles.customerCollContent}>
                                  <div className={Styles.customerDesc}>
                                    <pre className={Styles.commentPre}>
                                      {customer.comment}
                                    </pre>
                                  </div>
                                  <div className={Styles.customerBtnGrp}>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => this.onEditCustomerOpen(customer)}
                                    >
                                      <i className="icon mbc-icon edit"></i>
                                      <span>Edit Customer </span>
                                    </button>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => this.onDeleteCustomer(customer)}
                                    >
                                      <i className="icon delete"></i>
                                      <span>Delete Customer </span>
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>
              <div className={Styles.addTeamMemberWrapper}>
                <button id="AddTeamMemberBtn" onClick={this.addCustomerModel}>
                  <i className="icon mbc-icon plus" />
                  <span>Add Customer</span>
                </button>
                {!this.state.customer.customerDetails?.length && (
                  <div className={classNames(this.state.customerTabError ? '' : 'hide')}>
                    <span className="error-message">{this.state.customerTabError}</span>
                  </div>
                )}
              </div>
              <br />
              <div className={Styles.commentHeading}>
                Process Owner {this.state.customer.processOwners?.length == 1 ? '' : ' (if applicable)'}
              </div>
              <div className={Styles.productOwnerList}>
                {developerTeamMembersList?.length ? developerTeamMembersList : ''}
              </div>
              {this.state.customer.processOwners?.length === 1 ? (
                ''
              ) : (
                <div className={Styles.addTeamMemberWrapper}>
                  <IconAvatarNew className={Styles.avatarIcon} />
                  <button id="AddTeamMemberBtn" onClick={this.addDeveloperMember}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Process Owner</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onCustomer}>
            Save & Next
          </button>
        </div>
        <Modal
          title={this.state.addCustomer ? 'Add Customer' : this.state.editCustomer && 'Edit Customer'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={this.state.addCustomer || this.state.editCustomer}
          content={addCustomerModelContent}
          scrollableContent={false}
          onCancel={this.addCustomerModelClose}
        />
        { this.state.showAddTeamMemberModal && (
          <AddTeamMemberModal
            ref={this.addTeamMemberModalRef}
            modalTitleText={'Process Owner'}
            showOnlyInteral={true}
            editMode={this.state.editTeamMember}
            showAddTeamMemberModal={this.state.showAddTeamMemberModal}
            teamMember={this.state.teamMemberObj}
            onUpdateTeamMemberList={this.updateTeamMemberList}
            onAddTeamMemberModalCancel={this.onAddTeamMemberModalCancel}
          />
        )}
        <ConfirmModal
          title="Delete Customer"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={this.state.showDeleteModal}
          content={deleteModalContent}
          onCancel={this.onCancellingDeleteChanges}
          onAccept={this.onAcceptDeleteChanges}
        />
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.customer) {
      const customer = this.props.customer;
      customer.customerDetails = this.props.customer.customerDetails;
      customer.processOwners = this.props.customer.processOwners;
    }
  };
  protected onCustomer = () => {
    if (this.validateCustomerTab()) {
      this.props.modifyCustomer(this.state.customer);
      this.props.onSaveDraft('customer');
    }
  };

  protected validateCustomerTab = () => {
    const { customerDetails } = this.state.customer;
    !customerDetails?.length &&
      this.setState({
        customerTabError: ErrorMsg.CUSTOMER_TAB,
      });
    return customerDetails?.length;
  };

  protected addCustomerModel = () => {
    this.setState(
      {
        addCustomer: true,
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected handleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    this.setState((prevState) => ({
      customerInfo: {
        ...prevState.customerInfo,
        [name]: value,
      },
    }));
  };

  protected addCustomerModelClose = () => {
    this.setState({
      addCustomer: false,
      editCustomer: false,
      duplicateCustomerAdded: false,
      customerInfo: {
        hierarchy: '',
        department: '',
        ressort: '',
        comment: '',
      },
      errors: {
        hierarchy: '',
        ressort: '',
        department: '',
        comment: '',
      },
    });
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteModal: false });
  };

  protected onAcceptDeleteChanges = () => {
    const customerList = [...this.state.customer.customerDetails];
    customerList.splice(this.state.editCustomerIndex, 1);
    const customerProps = this.props.customer;
    customerProps.customerDetails = customerList;
    this.setState((prevState) => ({
      customer: {
        ...prevState.customer,
        customerDetails: customerList,
      },
      showDeleteModal: false,
    }));
  };

  protected isCustomerExist = (customerList: ICustomerDetails[]) => {
    const { hierarchy, ressort, department } = this.state.customerInfo;
    let customerExists = false;
    for (let ind = 0; ind < customerList?.length; ind++) {
      if (
        customerList[ind].hierarchy === hierarchy &&
        customerList[ind].department === department &&
        customerList[ind].ressort === ressort
      ) {
        customerExists = true;
        break;
      } else {
        customerExists = false;
      }
    }
    return customerExists;
  };

  protected onAddCustomer = () => {
    const { hierarchy, ressort, department, comment } = this.state.customerInfo;
    const { customerDetails: addedCustomerList } = this.state.customer;
    const selectedValues: ICustomerDetails[] = [];
    selectedValues.push({
      hierarchy,
      ressort,
      department,
      comment,
    });

    const customerExists = this.isCustomerExist(addedCustomerList);

    if (!customerExists && this.validateCustomerModal()) {
      const customer = this.props.customer;
      customer.customerDetails = [...customer.customerDetails, ...selectedValues];
      this.setState(
        (prevState: any) => ({
          addCustomer: false,
          duplicateCustomerAdded: false,
          customer: {
            ...prevState.customer,
            customerDetails: [...prevState.customer.customerDetails, ...selectedValues],
          },
          customerInfo: {
            hierarchy: '',
            department: '',
            ressort: '',
            comment: '',
          },
          errors: {
            hierarchy: '',
            department: '',
            ressort: '',
            comment: '',
          },
        }),
        () => {
          ExpansionPanel.defaultSetup();
          Tooltip.defaultSetup();
        },
      );
    } else {
      addedCustomerList?.length &&
        this.setState({
          duplicateCustomerAdded: true,
        });
    }
  };

  protected onEditCustomerOpen = (customer: ICustomerDetails) => {
    const { hierarchy, department, ressort, comment } = customer;
    const { customerDetails } = this.state.customer;
    const editCustomerIndex = customerDetails.findIndex(
      (item) => item.hierarchy === hierarchy && item.department === department && item.ressort === ressort,
    );
    this.setState(
      {
        addCustomer: false,
        editCustomer: true,
        editCustomerIndex,
        customerInfo: {
          hierarchy,
          department,
          ressort,
          comment,
        },
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected onDeleteCustomer = (customer: ICustomerDetails) => {
    const { hierarchy, department, ressort } = customer;
    const { customerDetails } = this.state.customer;
    const deleteCustomerIndex = customerDetails.findIndex(
      (item) => item.hierarchy === hierarchy && item.department === department && item.ressort === ressort,
    );
    this.setState({
      showDeleteModal: true,
      editCustomerIndex: deleteCustomerIndex,
    });
  };

  protected onEditCustomer = () => {
    const { editCustomerIndex } = this.state;
    const { hierarchy, department, ressort, comment } = this.state.customerInfo;
    const { customerDetails: addedCustomerList } = this.state.customer;
    const customerExists = this.isCustomerExist(addedCustomerList);
    const newIndex = addedCustomerList.findIndex(
      (item) => item.hierarchy === hierarchy && item.department === department && item.ressort === ressort,
    );
    if (this.validateCustomerModal()) {
      if ((customerExists && editCustomerIndex === newIndex) || !customerExists) {
        const customerList = [...addedCustomerList]; // create copy of original array
        customerList[editCustomerIndex] = { hierarchy, department, ressort, comment }; // modify copied array
        const customer = this.props.customer;
        customer.customerDetails = customerList;
        this.setState((prevState) => ({
          editCustomer: false,
          duplicateCustomerAdded: false,
          customer: {
            ...prevState.customer,
            customerDetails: customerList,
          },
          errors: {
            hierarchy: '',
            department: '',
            ressort: '',
            comment: '',
          },
          customerInfo: {
            hierarchy: '',
            department: '',
            ressort: '',
            comment: '',
          },
        }));
      } else if (customerExists && this.state.editCustomerIndex !== newIndex) {
        // Do not edit as user already exists
        this.setState({
          duplicateCustomerAdded: true,
        });
      }
    }
  };

  protected validateCustomerModal = () => {
    let formValid = true;
    const errors = this.state.errors;
    const errorMissingEntry = '*Missing entry';

    if (!this.state.customerInfo.hierarchy) {
      errors.hierarchy = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.customerInfo.department) {
      errors.department = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.customerInfo.ressort) {
      errors.ressort = errorMissingEntry;
      formValid = false;
    }
    // if (!this.state.customerInfo.comment) {
    //   errors.comment = errorMissingEntry;
    //   formValid = false;
    // } 
    else {
      errors.comment = '';
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ errors });
    return formValid;
  };

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetAddTeamMemberState();
    });
  };

  protected updateTeamMemberList = (teamMemberObj: ITeams) => {
    const { editTeamMember, editTeamMemberIndex, customer, addProductOwnerInController } = this.state;
    const teamMembers = customer.processOwners;
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }
    const stateUpdateObj = {
      showAddTeamMemberModal: false,
      customer: {
        customerDetails: this.state.customer.customerDetails,
        processOwners: this.state.customer.processOwners,
      },
    };

    if (addProductOwnerInController) {
      stateUpdateObj.customer.processOwners = teamMembers;
    } else {
      stateUpdateObj.customer.processOwners = teamMembers;
    }
    this.setState(stateUpdateObj, () => {
      this.resetAddTeamMemberState();
    });
  };
  protected addDeveloperMember = () => {
    this.setState({ addProductOwnerInController: true }, () => {
      this.showAddTeamMemberModalView();
    });
  };
  protected showAddTeamMemberModalView = () => {
    this.resetAddTeamMemberState();
    this.setState({ showAddTeamMemberModal: true }, () => {
      this.addTeamMemberModalRef.current.setTeamMemberData(this.state.teamMemberObj, false);
    });
  };
  protected resetAddTeamMemberState() {
    this.setState({
      editTeamMemberIndex: -1,
      teamMemberObj: {
        shortId: '',
        department: '',
        email: '',
        firstName: '',
        lastName: '',
        userType: '',
        mobileNumber: '',
        teamMemberPosition: '',
      },
      editTeamMember: false,
    });
  }

  protected onTeamMemberMoveUp = (index: number) => {
    const processOwner = this.state.customer.processOwners;
    const teamMember = processOwner.splice(index, 1)[0];
    processOwner.splice(index - 1, 0, teamMember);
    this.setState((prevState) => ({
      ...prevState,
      customer: {
        ...prevState.customer,
        processOwner,
      },
    }));
  };

  protected onTeamMemberMoveDown = (index: number) => {
    const processOwner = this.state.customer.processOwners;
    const teamMember = processOwner.splice(index, 1)[0];
    processOwner.splice(index + 1, 0, teamMember);
    this.setState((prevState) => ({
      ...prevState,
      customer: {
        ...prevState.customer,
        processOwner,
      },
    }));
  };

  protected onSharingTeamMemberEdit = (index: number) => {
    this.setState({ addProductOwnerInController: false }, () => {
      this.onTeamMemberEdit(index);
    });
  };
  protected onSharingTeamMemberDelete = (index: number) => {
    this.setState({ addProductOwnerInController: false }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onTeamMemberDelete = (index: number) => {
    const {
      customer: { processOwners },
      addProductOwnerInController,
    } = this.state;
    const teamMembers = addProductOwnerInController ? processOwners : processOwners;
    teamMembers.splice(index, 1);
    if (addProductOwnerInController) {
      this.setState((prevState) => ({
        ...prevState,
        customer: {
          ...prevState.customer,
          processOwner: teamMembers,
        },
      }));
    } else {
      this.setState((prevState) => ({
        ...prevState,
        customer: {
          ...prevState.customer,
          processOwner: teamMembers,
        },
      }));
    }
  };

  protected onTeamMemberEdit = (index: number) => {
    const {
      customer: { processOwners },
      addProductOwnerInController,
    } = this.state;
    const teamMemberObj = addProductOwnerInController ? processOwners[index] : processOwners[index];
    this.setState(
      {
        teamMemberObj,
        showAddTeamMemberModal: true,
        editTeamMember: true,
        editTeamMemberIndex: index,
      },
      () => {
        this.addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, true);
      },
    );
  };
  sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: ICustomerDetails[] = [];

      if (columnName === columnName) {
        sortedArray = this.state.customer.customerDetails?.sort((a, b) => {
          const nameA = a[columnName]?.toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          const nameB = b[columnName]?.toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          if (nameA < nameB) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA > nameB) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      }
      this.setState({
        nextSortOrder: sortOrder == 'asc' ? 'desc' : 'asc',
      });
      this.setState({
        currentSortOrder: sortOrder,
      });
      this.setState({
        currentColumnToSort: columnName,
      });
      this.setState((prevState) => ({
        customer: {
          ...prevState.customer,
          customerDetails: sortedArray,
        },
      }));
    };
  };
}
