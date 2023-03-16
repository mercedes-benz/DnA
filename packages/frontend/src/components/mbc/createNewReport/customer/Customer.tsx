import cn from 'classnames';
import * as React from 'react';
import Styles from './Customer.scss';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
// import IconAvatarNew from 'components/icons/IconAvatarNew';
import { ITeams, ICustomers, IDepartment, IHierarchies, IRessort, IInternalCustomerDetails, 
  IExternalCustomerDetails, IDivision, IDivisionAndSubDivision } from 'globals/types';
import AddTeamMemberModal from 'components/mbc/addTeamMember/addTeamMemberModal/AddTeamMemberModal';
// import TeamMemberListItem from 'components/mbc/addTeamMember/teamMemberListItem/TeamMemberListItem';
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import { ErrorMsg } from 'globals/Enums';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import TextArea from 'components/mbc/shared/textArea/TextArea';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import TeamSearch from '../../teamSearch/TeamSearch';
// import { IconAvatar } from 'components/icons/IconAvatar';
import TeamMemberListItem from 'components/mbc/addTeamMember/teamMemberListItem/TeamMemberListItem';
import IconAvatarNew from 'components/icons/IconAvatarNew';

const classNames = cn.bind(Styles);

export interface ICustomerProps {
  customer: ICustomers;
  hierarchies: IHierarchies[];
  departments: IDepartment[];
  ressort: IRessort[];
  divisions: IDivision[];
  // customerDetails: ICustomerDetails;
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyCustomer: (modifyCustomer: ICustomers) => void;
}
export interface ICustomerState {
  customer: ICustomers;
  internalCustomerInfo: IInternalCustomerDetails;
  externalCustomerInfo: IExternalCustomerDetails;
  comment: string;
  internalCustomerErrors: IInternalCustomerDetails;
  externalCustomerErrors: IExternalCustomerDetails;
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
  searchTerm: string;
  searchTermForName: string;
  // nameToDisplay: string;
  processOwnerToDisplay: string;
  customerType: string;
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
        internalCustomers: [],
        externalCustomers: [],
      },
      internalCustomerInfo: {
        // name: {
        //   company: '',
        //   department: '',
        //   email: '',
        //   firstName: '',
        //   shortId: '',
        //   lastName: '',
        //   mobileNumber: '',
        //   teamMemberPosition: '',
        //   userType: ''
        // },
        customerRelation: 'Internal',
        comment: '',
        department: '',
        level: '',
        legalEntity: '',
        division: {id: '', name: '', subdivision: {id: '', name: ''}},
        accessToSensibleData: 'false',
        processOwner: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        }
      },
      externalCustomerInfo:{
        name: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        },
        companyName: '',
        customerRelation: 'External',
        comment: ''
      },
      internalCustomerErrors: {
        // name: {
        //   company: '',
        //   department: '',
        //   email: '',
        //   firstName: '',
        //   shortId: '',
        //   lastName: '',
        //   mobileNumber: '',
        //   teamMemberPosition: '',
        //   userType: ''
        // },
        customerRelation: '',
        comment: '',
        department: '',
        level: '',
        legalEntity: '',
        division: '',
        accessToSensibleData: 'false',
        processOwner: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        }
      },
      externalCustomerErrors:{
        name: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        },
        companyName: '',
        customerRelation: '',
        comment: ''
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
      searchTerm: '',
      searchTermForName: '',
      // nameToDisplay: '',
      processOwnerToDisplay: '',
      customerType: 'Internal',
    };
    this.onUsRiskChange = this.onUsRiskChange.bind(this);
  }
  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();

  public componentDidMount() {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
    this.setState({
      // nameToDisplay: this.state.internalCustomerInfo.name.firstName ? 
      // (this.state.internalCustomerInfo.name.firstName +' '+ this.state.internalCustomerInfo.name.lastName) : '',
      processOwnerToDisplay: this.state.internalCustomerInfo.processOwner.firstName ? this.state.internalCustomerInfo.processOwner.firstName +' '+ this.state.internalCustomerInfo.processOwner.lastName : ''
    });
  }

  // public onCustomerNameChange = (e: React.FormEvent<HTMLInputElement>) => {
  //   const fieldValue = e.currentTarget.value;
    
  //   this.setState((prevState) => ({
  //     // nameToDisplay: fieldValue,
  //     externalCustomerInfo: {
  //       ...prevState.externalCustomerInfo,
  //       name:{
  //         ...prevState.externalCustomerInfo.name,
  //       ['firstName']: fieldValue.split(' ')[0],
  //       ['lastName']: fieldValue.split(' ')[1],
  //       }
  //     },
  //   }));
  // };

  public onUsRiskChange (e: React.FormEvent<HTMLInputElement>) {
    const name = e.currentTarget.name;
    const usRiskVal = e.currentTarget.value;
    this.setState((prevState) => ({
      internalCustomerInfo: {
        ...prevState.internalCustomerInfo,
        [name]: usRiskVal,
      },
    }));
  }

  public render() {
    const requiredError = '*Missing entry';
    const addCustomerModelContent = (
      <div className={Styles.addCustomerModelContent}>
        <br />
        <div>
          {this.state.customerType === 'Internal' && (
            <>
              <div className={Styles.flexLayout}>
                {/* <div>
                { this.state.customerType === 'Internal'?
                  <div className={classNames('input-field-group include-error')}>
                    <TeamSearch
                      label={
                        <>
                          Name
                        </>
                      }
                      fieldMode={true}
                      fieldValue={this.state.nameToDisplay}
                      setFieldValue={(val) => this.setState({nameToDisplay: val})}
                      onAddTeamMember={(value) => this.addNameFromTeamSearch(value)}
                      btnText="Save"
                      searchTerm={this.state.searchTermForName}
                      setSearchTerm={(value) => this.setState({searchTermForName: value})}
                      showUserDetails={false}
                      setShowUserDetails={() => {}}
                    />    
                  </div>
                  : 
                  <div>
                    <TextBox
                      type="text"
                      name={'name'}
                      controlId={'customerNameInput'}
                      labelId={'customerNameLabel'}
                      label={'Name'}
                      placeholder={'Type here'}
                      value={this.state.nameToDisplay}
                      // errorText={this.state.errors.name}
                      required={false}
                      maxLength={200}
                      onChange={this.onCustomerNameChange}
                    />
                  </div>
                  }
                </div> */}

                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                    )}
                  >
                    <label id="customerTypeLabel" htmlFor="customerTypeField" className="input-label">
                      Customer Relation
                    </label>
                    <div className="custom-select">
                      <select
                        id="customerTypeField"
                        name="customerRelation"
                        multiple={false}
                        value={this.state.customerType}
                        onChange={this.internalHandleSelectChange}
                      >
                        <option id={'Internal'} key={'Internal'} value={'Internal'}>
                          Internal
                        </option>
                        <option id={'External'} key={'External'} value={'External'}>
                          External
                        </option>
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      this.state.internalCustomerErrors.level ? 'error' : '',
                    )}
                  >
                    <label id="hirarchyLabel" htmlFor="hierarchyField" className="input-label">
                      Level<sup>*</sup>
                    </label>
                    <div className="custom-select">
                      <select
                        id="hierarchyField"
                        name="level"
                        multiple={false}
                        required-error={requiredError}
                        required={true}
                        value={this.state.internalCustomerInfo.level}
                        onChange={this.internalHandleSelectChange}
                      >
                        <option value={''}>Choose</option>
                        {this.props.hierarchies?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span
                      className={classNames('error-message', this.state.internalCustomerErrors.level ? '' : 'hide')}
                    >
                      {this.state.internalCustomerErrors.level}
                    </span>
                  </div>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      this.state.internalCustomerErrors.department ? 'error' : '',
                    )}
                  >
                    <label id="departmentLabel" htmlFor="departmentField" className="input-label">
                      Customer E2-Department<sup>*</sup>
                    </label>
                    <div className="custom-select">
                      <select
                        id="departmentField"
                        name="department"
                        multiple={false}
                        required-error={requiredError}
                        required={true}
                        value={this.state.internalCustomerInfo.department || ''}
                        onChange={this.internalHandleSelectChange}
                      >
                        <option value={''}>Choose</option>
                        {this.props.departments?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span
                      className={classNames(
                        'error-message',
                        this.state.internalCustomerErrors.department?.length ? '' : 'hide',
                      )}
                    >
                      {this.state.internalCustomerErrors.department}
                    </span>
                  </div>
                </div>
                <div>
                  <div className={Styles.divisionContainer}>
                    <div
                      className={classNames(
                        'input-field-group include-error',
                        // this.state.internalCustomerInfo.division.id === '0' ? 'error' : '',
                      )}
                    >
                      <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                        Customer Division
                      </label>
                      <div className="custom-select">
                        <select
                          id="divisionField"
                          name="division"
                          // required={true}
                          // required-error={requiredError}
                          onChange={this.onDivisionChange}
                          value={this.state.internalCustomerInfo?.division?.id}
                        >
                          <option id="divisionOption" value={''}>
                            Choose
                          </option>
                          {this.props.divisions?.map((obj) => (
                            <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                              {obj.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      {/* <span className={classNames('error-message', this.state.internalCustomerInfo.division.id === '0' ? '' : 'hide')}>
                      *Missing entry
                    </span> */}
                    </div>
                  </div>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      this.state.internalCustomerErrors.legalEntity ? 'error' : '',
                    )}
                  >
                    <label id="ressortLabel" htmlFor="ressortField" className="input-label">
                      MB Legal Entity<sup>*</sup>
                    </label>
                    <div className="custom-select">
                      <select
                        id="ressortField"
                        name="legalEntity"
                        multiple={false}
                        required-error={requiredError}
                        required={true}
                        value={this.state.internalCustomerInfo.legalEntity || ''}
                        onChange={this.internalHandleSelectChange}
                      >
                        <option value={''}>Choose</option>
                        {this.props.ressort?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span
                      className={classNames(
                        'error-message',
                        this.state.internalCustomerErrors.legalEntity?.length ? '' : 'hide',
                      )}
                    >
                      {this.state.internalCustomerErrors.legalEntity}
                    </span>
                  </div>
                </div>
                <div>
                  <div className={classNames('input-field-group include-error')}>
                    <label id="usRiskLabel" className={classNames('input-label', Styles.usRiskLabel)}>
                      US-Risk<sup>*</sup> (US-Access to sensible data)
                    </label>
                    <div>
                      <label className="radio">
                        <span className="wrapper">
                          <input
                            type="radio"
                            name="accessToSensibleData"
                            value={'false'}
                            checked={this.state.internalCustomerInfo.accessToSensibleData?.toString() == 'false'}
                            onChange={this.onUsRiskChange}
                          />
                        </span>
                        <span className="label">No</span>
                      </label>

                      <label className="radio">
                        <span className="wrapper">
                          <input
                            type="radio"
                            name="accessToSensibleData"
                            value={'true'}
                            checked={this.state.internalCustomerInfo.accessToSensibleData?.toString() == 'true'}
                            onChange={this.onUsRiskChange}
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}

          {this.state.customerType === 'External' && (
            <div className={Styles.flexLayout}>
              <div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                  )}
                >
                  <label id="customerTypeLabel" htmlFor="customerTypeField" className="input-label">
                    Customer Relation
                  </label>
                  <div className="custom-select">
                    <select
                      id="customerTypeField"
                      name="customerRelation"
                      multiple={false}
                      value={this.state.customerType}
                      onChange={this.externalHandleSelectChange}
                    >
                      {/* <option value={''}>Choose</option> */}
                      <option id={'Internal'} key={'Internal'} value={'Internal'}>
                        Internal
                      </option>
                      <option id={'External'} key={'External'} value={'External'}>
                        External
                      </option>
                    </select>
                  </div>
                </div>
              </div>
              <div>
                {/* <div
                className={classNames('input-field-group include-error', this.state.errors.companyName ? 'error' : '')}
              >
                <label id="companyNameLabel" htmlFor="companyNameField" className="input-label">
                  Company Name<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="companyNameField"
                    name="companyName"
                    multiple={false}
                    required-error={requiredError}
                    required={true}
                    value={this.state.customerInfo.companyName || ''}
                    onChange={this.handleSelectChange}
                  >
                    <option value={''}>Choose</option>
                    {this.props.departments?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', this.state.errors.companyName ? '' : 'hide')}>
                  {this.state.errors.companyName}
                </span>                
              </div> */}
                <div>
                  <TextBox
                    type="text"
                    name="companyName"
                    controlId={'companyNameInput'}
                    labelId={'companyNameLabel'}
                    label={'Company Name'}
                    placeholder={'Type here'}
                    value={this.state.externalCustomerInfo.companyName}
                    errorText={this.state.externalCustomerErrors.companyName}
                    required={true}
                    maxLength={200}
                    onChange={this.externalHandleChangeInputField}
                  />
                </div>
              </div>
            </div>
          )}

          <div>
            {this.state.customerType === 'Internal' ? (
              <TextArea
                controlId={'customerComment'}
                containerId={'customerComment'}
                name={'comment'}
                labelId={'customerCommentLabel'}
                label={'Comment'}
                rows={50}
                value={this.state.internalCustomerInfo.comment}
                required={false}
                onChange={this.internalHandleChange}
                // onBlur={this.validateCustomerModal}
              />
            ) : (
              <TextArea
                controlId={'customerComment'}
                containerId={'customerComment'}
                name={'comment'}
                labelId={'customerCommentLabel'}
                label={'Comment'}
                rows={50}
                value={this.state.externalCustomerInfo.comment}
                required={false}
                onChange={this.externalHandleChange}
                // onBlur={this.validateCustomerModal}
              />
            )}
            {this.state.customerType === 'Internal' ? (
              <div className={Styles.flexLayout}>
                {/* <div>
                <TextBox
                  type="text"
                  controlId={'processOwnerInput'}
                  labelId={'processOwnerLabel'}
                  label={'Process Owner'}
                  placeholder={'Type here'}
                  value={this.state.customerInfo.processOwner}
                  // errorText={this.state.errors.processOwner}
                  required={false}
                  maxLength={200}
                  onChange={this.onTextOnChange}
                />
              </div> */}
                <div className={classNames('input-field-group include-error')}>
                  <TeamSearch
                    label={<>Process Owner</>}
                    fieldMode={true}
                    fieldValue={this.state.processOwnerToDisplay}
                    setFieldValue={(val) => this.setState({ processOwnerToDisplay: val })}
                    onAddTeamMember={(value) => this.addMemberFromTeamSearch(value)}
                    btnText="Save"
                    searchTerm={this.state.searchTerm}
                    setSearchTerm={(value) => this.setState({ searchTerm: value })}
                    showUserDetails={false}
                    setShowUserDetails={() => {}}
                  />
                  {/* <span className={classNames('error-message')}>{this.state.internalCustomerErrors.processOwner}</span> */}
                </div>
                <div></div>
              </div>
            ) : (
              ''
            )}

            {/* {this.state.duplicateCustomerAdded ? <span className={'error-message'}>Customer already exist</span> : ''} */}
            <div className="btnConatiner">
              {this.state.customerType === 'Internal' ? (
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={this.state.addCustomer ? this.onAddInternalCustomer : this.onEditInternalCustomer}
                >
                  {this.state.addCustomer ? 'Add' : this.state.editCustomer && 'Save'}
                </button>
              ) : (
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={this.state.addCustomer ? this.onAddExternalCustomer : this.onEditExternalCustomer}
                >
                  {this.state.addCustomer ? 'Add' : this.state.editCustomer && 'Save'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    );

    // const developerTeamMembersList = this.state.customer.processOwners
    //   ? this.state.customer.processOwners?.map((member: ITeams, index: number) => {
    //       return (
    //         <TeamMemberListItem
    //           key={index}
    //           itemIndex={index}
    //           teamMember={member}
    //           showMoveUp={index !== 0}
    //           showMoveDown={index + 1 !== this.state.customer.processOwners?.length}
    //           onMoveUp={this.onTeamMemberMoveUp}
    //           onMoveDown={this.onTeamMemberMoveDown}
    //           onEdit={this.onSharingTeamMemberEdit}
    //           onDelete={this.onSharingTeamMemberDelete}
    //         />
    //       );
    //     })
    //   : [];

    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Customer</div>
        <div className={Styles.modalContent}>This customer will be deleted permanently.</div>
      </div>
    );

    

    const internalCustomersList = this.state.customer.internalCustomers
      ? this.state.customer.internalCustomers?.map((member: any, index: number) => {
          console.log(member);
          return (
            <TeamMemberListItem
              key={'internal'+index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={false}
              showMoveDown={false}
              division={member?.division?.name}
              department={member.department}
              relation={member.customerRelation}
              editOptionText={'Edit Customer'}
              deleteOptionText={'Delete Customer'}
              onMoveUp={() => {}}
              onMoveDown={() => {}}
              onEdit={() => this.onInternalEditCustomerOpen(member)}
              onDelete={() => this.onDeleteInternalCustomer(member)}
            />
          );
        })
      : [];

    const externalCustomersList = this.state.customer.externalCustomers
      ? this.state.customer.externalCustomers?.map((member: any, index: number) => {
          return (
            <TeamMemberListItem
              key={'external-'+index}
              itemIndex={index}
              teamMember={member.name}
              showMoveUp={false}
              showMoveDown={false}
              relation={member.customerRelation}
              companyName={member.companyName}
              editOptionText={'Edit Customer'}
              deleteOptionText={'Delete Customer'}
              onMoveUp={() => {}}
              onMoveDown={() => {}}
              onEdit={() => this.onExternalEditCustomerOpen(member)}
              onDelete={() => this.onDeleteExternalCustomer(member)}
            />
          );
        })
      : [];  

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Please add or edit the report's customers</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={classNames('expanstion-table', Styles.listOfCustomerTable)}>
                <div className={Styles.customerGrp}>
                  <div className={Styles.customerGrpList}>
                    <div className={Styles.customerGrpListItem}>

                      {(this.state.customer.internalCustomers?.length < 1 && this.state.customer.externalCustomers?.length < 1) && (
                        <div className={Styles.customerWrapper}>
                          <div className={Styles.customerWrapperNoList}>
                            <div className={Styles.addTeamMemberWrapper}>
                              <IconAvatarNew className={Styles.avatarIcon} />
                              <button id="AddTeamMemberBtn" onClick={this.addCustomerModel}>
                                <i className="icon mbc-icon plus" />
                                <span>Add Customer</span>
                              </button>
                              {(!this.state.customer.internalCustomers?.length && !this.state.customer.externalCustomers?.length) && (
                                <div className={classNames(this.state.customerTabError ? '' : 'hide')}>
                                  <span className="error-message">{this.state.customerTabError}</span>
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      )}
                      <br />
                      {(this.state.customer.internalCustomers?.length > 0 || this.state.customer.externalCustomers?.length > 0) && (
                        <div className={Styles.addTeamMemberWrapper}>
                          <IconAvatarNew className={Styles.avatarIcon} />
                          <button id="AddTeamMemberBtn" onClick={this.addCustomerModel}>
                            <i className="icon mbc-icon plus" />
                            <span>Add Customer</span>
                          </button>
                        </div>                     
                      )}
                      {internalCustomersList.length ? internalCustomersList : ''}

                      {externalCustomersList.length ? externalCustomersList : ''}
                      
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onCustomer}>
            Save & Next
          </button>
        </div>
        {(this.state.addCustomer || this.state.editCustomer) &&(
          <Modal
            title={this.state.addCustomer ? 'Add Customer' : this.state.editCustomer && 'Edit Customer'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'60%'}
            buttonAlignment="right"
            show={this.state.addCustomer || this.state.editCustomer}
            content={addCustomerModelContent}
            scrollableContent={true}
            onCancel={this.addCustomerModelClose}
          />
        )}
        {/* {this.state.showAddTeamMemberModal && (
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
        )} */}
        <ConfirmModal
          title="Delete Customer"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={this.state.showDeleteModal}
          content={deleteModalContent}
          onCancel={this.onCancellingDeleteChanges}
          onAccept={this.onAcceptDeleteChangesInternal}
        />
      </React.Fragment>
    );
  }

  public resetChanges = () => {
    if (this.props.customer) {
      const customer = this.props.customer;
      customer.internalCustomers = this.props.customer.internalCustomers;
      customer.externalCustomers = this.props.customer.externalCustomers;
    }
  };
  protected onCustomer = () => {
    if (this.validateCustomerTab()) {
      this.props.modifyCustomer(this.state.customer);
      this.props.onSaveDraft('customer');
    }
  };

  protected validateCustomerTab = () => {
    const { externalCustomers, internalCustomers } = this.state.customer;
    (!internalCustomers?.length && !externalCustomers?.length) &&
      this.setState({
        customerTabError: ErrorMsg.CUSTOMER_TAB,
      });  
    return (internalCustomers?.length || externalCustomers?.length);
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

  protected internalHandleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement | HTMLInputElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    this.setState((prevState) => ({
      internalCustomerInfo: {
        ...prevState.internalCustomerInfo,
        [name]: value,
      },
    }));
  };

  protected externalHandleChange = (e: React.ChangeEvent<HTMLSelectElement | HTMLTextAreaElement | HTMLInputElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    this.setState((prevState) => ({
      externalCustomerInfo: {
        ...prevState.externalCustomerInfo,
        [name]: value,
      },
    }));
  };

  // protected internalHandleChangeInputField = (e: React.ChangeEvent<HTMLInputElement>) => {
  //   const name = e.target.name;
  //   const value = e.target.value;
  //   this.setState((prevState) => ({
  //     internalCustomerInfo: {
  //       ...prevState.internalCustomerInfo,
  //       [name]: value,
  //     },
  //   }));
  // };

  protected externalHandleChangeInputField = (e: React.ChangeEvent<HTMLInputElement>) => {
    const name = e.target.name;
    const value = e.target.value;
    this.setState((prevState) => ({
      externalCustomerInfo: {
        ...prevState.externalCustomerInfo,
        [name]: value,
      },
    }));
  };

  protected onDivisionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    const division: IDivisionAndSubDivision = { id: '0', name: null, subdivision: { id: null, name: null } };
    const selectedOptions = e.currentTarget.selectedOptions;
    if (selectedOptions.length) {
      division.id = selectedOptions[0].value;
      division.name = selectedOptions[0].label;

      if (division.id !== '0') {
        this.setState((prevState) => ({
          internalCustomerInfo: {
            ...prevState.internalCustomerInfo,
            [name]: division,
          },
        }));
      }
    }
  };

  protected internalHandleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    let selectedValue = '';
    const selectedOptions = e.currentTarget.selectedOptions;
      if (selectedOptions.length) {
        Array.from(selectedOptions).forEach((option) => {
          selectedValue = option.value;
        });
        this.setState((prevState) => ({
          internalCustomerInfo: {
            ...prevState.internalCustomerInfo,
            [name]: selectedValue,
          },
        }),
        ()=>{
          if(name === 'customerRelation'){
            this.setState((prevState) =>(
            {
              customerType: selectedValue,
              // internalCustomerInfo: {
              //   ...prevState.internalCustomerInfo,
              //   level: '',
              //   department: '',
              //   legalEntity: '',
              //   comment: '',
              //   name: {
              //     company: '',
              //     department: '',
              //     email: '',
              //     firstName: '',
              //     shortId: '',
              //     lastName: '',
              //     mobileNumber: '',
              //     teamMemberPosition: '',
              //     userType: ''
              //   },
              //   division: '',
              //   accessToSensibleData: false,
              //   processOwner: {
              //     company: '',
              //     department: '',
              //     email: '',
              //     firstName: '',
              //     shortId: '',
              //     lastName: '',
              //     mobileNumber: '',
              //     teamMemberPosition: '',
              //     userType: ''
              //   }
              // },
              internalCustomerErrors: {
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                customerRelation: '',
                comment: '',
                department: '',
                level: '',
                legalEntity: '',
                division: '',
                accessToSensibleData: 'false',
                processOwner: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                }
              },
              externalCustomerInfo: {
                ...prevState.externalCustomerInfo,
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                companyName: '',
                customerRelation: 'External',
                comment: ''
              },
              externalCustomerErrors:{
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                companyName: '',
                customerRelation: '',
                comment: ''
              },
            }
            ),()=>{SelectBox.defaultSetup(true);});
          }
          
        },);        
      }     
  }

  protected externalHandleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const name = e.target.name;
    let selectedValue = '';
    const selectedOptions = e.currentTarget.selectedOptions;
      if (selectedOptions.length) {
        Array.from(selectedOptions).forEach((option) => {
          selectedValue = option.value;
        });
        this.setState((prevState) => ({
          externalCustomerInfo: {
            ...prevState.externalCustomerInfo,
            [name]: selectedValue,
          },
        }),
        ()=>{
          if(name === 'customerRelation'){
            this.setState((prevState) =>(
            {
              customerType: selectedValue,
              // externalCustomerInfo: {
              //   ...prevState.externalCustomerInfo,
              //   name: {
              //     company: '',
              //     department: '',
              //     email: '',
              //     firstName: '',
              //     shortId: '',
              //     lastName: '',
              //     mobileNumber: '',
              //     teamMemberPosition: '',
              //     userType: ''
              //   },
              //   companyName: '',
              //   customerRelation: '',
              //   comment: ''
              // },
              externalCustomerErrors:{
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                companyName: '',
                customerRelation: '',
                comment: ''
              },
              internalCustomerInfo: {
                ...prevState.internalCustomerInfo,
                level: '',
                department: '',
                legalEntity: '',
                comment: '',
                customerRelation: 'Internal',
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                division: '',
                accessToSensibleData: false,
                processOwner: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                }
              },
              internalCustomerErrors: {
                name: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                },
                customerRelation: 'Internal',
                comment: '',
                department: '',
                level: '',
                legalEntity: '',
                division: '',
                accessToSensibleData: 'false',
                processOwner: {
                  company: '',
                  department: '',
                  email: '',
                  firstName: '',
                  shortId: '',
                  lastName: '',
                  mobileNumber: '',
                  teamMemberPosition: '',
                  userType: ''
                }
              }
            }
            ),()=>{SelectBox.defaultSetup(true);});
          }
          
        },);        
      }     
  }

  protected addMemberFromTeamSearch = (value: ITeams) => {
  this.setState((prevState) => ({
      processOwnerToDisplay: value?.firstName ? value?.firstName+' '+value?.lastName : '',
      internalCustomerInfo: {
        ...prevState.internalCustomerInfo,
        processOwner: value
      },
    }));
  };

  // protected addNameFromTeamSearch = (value: ITeams) => {
  //   this.setState((prevState) => ({
  //     nameToDisplay: value?.firstName ? value?.firstName+' '+value?.lastName : '',
  //     internalCustomerInfo: {
  //       ...prevState.internalCustomerInfo,
  //       name: value
  //     },
  //   }));
  // };

  protected addCustomerModelClose = () => {
    this.setState({
      addCustomer: false,
      editCustomer: false,
      duplicateCustomerAdded: false,
      // nameToDisplay: '',
      processOwnerToDisplay: '',
      internalCustomerInfo: {
        // name: {
        //   company: '',
        //   department: '',
        //   email: '',
        //   firstName: '',
        //   shortId: '',
        //   lastName: '',
        //   mobileNumber: '',
        //   teamMemberPosition: '',
        //   userType: ''
        // },
        customerRelation: 'Internal',
        comment: '',
        department: '',
        level: '',
        legalEntity: '',
        division: '',
        accessToSensibleData: 'false',
        processOwner: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        }
      },
      externalCustomerInfo:{
        name: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        },
        companyName: '',
        customerRelation: '',
        comment: ''
      },
      internalCustomerErrors: {
        // name: {
        //   company: '',
        //   department: '',
        //   email: '',
        //   firstName: '',
        //   shortId: '',
        //   lastName: '',
        //   mobileNumber: '',
        //   teamMemberPosition: '',
        //   userType: ''
        // },
        customerRelation: 'Internal',
        comment: '',
        department: '',
        level: '',
        legalEntity: '',
        division: '',
        accessToSensibleData: 'false',
        processOwner: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        }
      },
      externalCustomerErrors:{
        name: {
          company: '',
          department: '',
          email: '',
          firstName: '',
          shortId: '',
          lastName: '',
          mobileNumber: '',
          teamMemberPosition: '',
          userType: ''
        },
        companyName: '',
        customerRelation: '',
        comment: ''
      },
    });
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteModal: false });
  };

  protected onAcceptDeleteChangesInternal = () => {
    const customerList = [...this.state.customer.internalCustomers];
    customerList.splice(this.state.editCustomerIndex, 1);
    const customerProps = this.props.customer;
    customerProps.internalCustomers = customerList;
    this.setState((prevState) => ({
      customer: {
        ...prevState.customer,
        customerDetails: customerList,
      },
      showDeleteModal: false,
    }));
  };

  // protected onAcceptDeleteChangesExternal = () => {
  //   const customerList = [...this.state.customer.externalCustomers];
  //   customerList.splice(this.state.editCustomerIndex, 1);
  //   const customerProps = this.props.customer;
  //   customerProps.externalCustomers = customerList;
  //   this.setState((prevState) => ({
  //     customer: {
  //       ...prevState.customer,
  //       customerDetails: customerList,
  //     },
  //     showDeleteModal: false,
  //   }));
  // };

  protected isCustomerExist = (customerList: IInternalCustomerDetails[]) => {
    const { level, legalEntity, department } = this.state.internalCustomerInfo;
    let customerExists = false;
    for (let ind = 0; ind < customerList?.length; ind++) {
      if (
        customerList[ind].level === level &&
        customerList[ind].department === department &&
        customerList[ind].legalEntity === legalEntity
      ) {
        customerExists = true;
        break;
      } else {
        customerExists = false;
      }
    }
    return customerExists;
  };

  protected isExternalCustomerExist = (customerList: IExternalCustomerDetails[]) => {
    const { name, comment, companyName } = this.state.externalCustomerInfo;
    let customerExists = false;
    for (let ind = 0; ind < customerList?.length; ind++) {
      if (
        customerList[ind].name === name &&
        customerList[ind].comment === comment &&
        customerList[ind].companyName === companyName
      ) {
        customerExists = true;
        break;
      } else {
        customerExists = false;
      }
    }
    return customerExists;
  };

  protected onAddInternalCustomer = () => {
    const { 
      level, 
      legalEntity, 
      department, 
      comment, 
      // name,
      customerRelation,
      division,
      accessToSensibleData,
      processOwner
    } = this.state.internalCustomerInfo;
    const { internalCustomers: addedCustomerList } = this.state.customer;
    const selectedValues: IInternalCustomerDetails[] = [];
    selectedValues.push({
      level,
      legalEntity,
      department,
      comment,
      // name,
      customerRelation,
      division : division.id ? division : null,
      accessToSensibleData,
      processOwner
    });

    // const customerExists = this.isCustomerExist(addedCustomerList);

    // if (!customerExists && this.validateCustomerModal()) {
    if (this.validateInternalCustomerModal()) {
      const customer = this.props.customer;
      customer.internalCustomers = [...customer.internalCustomers, ...selectedValues];
      this.setState(
        (prevState: any) => ({
          addCustomer: false,
          duplicateCustomerAdded: false,
          // nameToDisplay: '',
          processOwnerToDisplay: '',
          customer: {
            ...prevState.customer,
            internalCustomers: [...prevState.customer.internalCustomers, ...selectedValues],
          },
          internalCustomerInfo: {
            // name: {
            //   company: '',
            //   department: '',
            //   email: '',
            //   firstName: '',
            //   shortId: '',
            //   lastName: '',
            //   mobileNumber: '',
            //   teamMemberPosition: '',
            //   userType: ''
            // },
            customerRelation: 'Internal',
            comment: '',
            department: '',
            level: '',
            legalEntity: '',
            division: '',
            accessToSensibleData: 'false',
            processOwner: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            }
          },
          internalCustomerErrors: {
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            customerRelation: 'Internal',
            comment: '',
            department: '',
            level: '',
            legalEntity: '',
            division: '',
            accessToSensibleData: 'false',
            processOwner: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            }
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

  protected onAddExternalCustomer = () => {
    const {  
      name,
      comment, 
      customerRelation,
      companyName } = this.state.externalCustomerInfo;
    const { externalCustomers: addedCustomerList } = this.state.customer;
    const selectedValues: IExternalCustomerDetails[] = [];
    selectedValues.push({
      name,
      comment,
      customerRelation,
      companyName
    });

    const customerExists = this.isExternalCustomerExist(addedCustomerList);

    if (!customerExists && this.validateExternalCustomerModal()) {
      const customer = this.props.customer;
      customer.externalCustomers = [...customer.externalCustomers, ...selectedValues];
      this.setState(
        () => ({
          addCustomer: false,
          duplicateCustomerAdded: false,
          // nameToDisplay: '',
          externalCustomerInfo:{
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            companyName: '',
            customerRelation: '',
            comment: ''
          },
          externalCustomerErrors:{
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            companyName: '',
            customerRelation: '',
            comment: ''
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

  protected onInternalEditCustomerOpen = (customer: IInternalCustomerDetails) => {
    const { level, department, legalEntity, comment,
      // name,
      customerRelation,
      division,
      accessToSensibleData,
      processOwner,
     } = customer;
    const { internalCustomers } = this.state.customer;
    const editCustomerIndex = internalCustomers.findIndex(
      (item) => item.level === level && item.department === department && item.legalEntity === legalEntity,
    );
    this.setState((prevState) => (
      {
        addCustomer: false,
        editCustomer: true,
        editCustomerIndex,
        customerType: 'Internal',
        internalCustomerInfo: {
          ...prevState.internalCustomerInfo,
          level,
          department,
          legalEntity,
          comment,
          name,
          customerRelation,
          division,
          accessToSensibleData,
          processOwner
        },
      }),
      () => {
        this.setState({
          // nameToDisplay: name?.firstName ? name?.firstName +' '+ name?.lastName : '',
          processOwnerToDisplay: processOwner?.firstName ? processOwner?.firstName +' '+ processOwner?.lastName : ''
        });
        SelectBox.defaultSetup();
      },
    );
  };

  protected onExternalEditCustomerOpen = (customer: IExternalCustomerDetails) => {
    const {
      name, 
      comment,
      customerRelation,
      companyName } = customer;
    const { externalCustomers } = this.state.customer;
    const editCustomerIndex = externalCustomers.findIndex(
      (item) => item.companyName === companyName && item.name === name && item.customerRelation === customerRelation,
    );
    this.setState(
      {
        addCustomer: false,
        editCustomer: true,
        editCustomerIndex,
        customerType: 'External',
        externalCustomerInfo: {
          name, 
          comment,
          customerRelation,
          companyName
        },
      },
      () => {
        // this.setState({
        //   nameToDisplay: (name.firstName ? name.firstName : '') +' '+ (name.lastName ? name.lastName : '')
        // });
        SelectBox.defaultSetup();
      },
    );
  };

  protected onDeleteInternalCustomer = (customer: IInternalCustomerDetails) => {
    const { level, department, legalEntity } = customer;
    const { internalCustomers } = this.state.customer;
    const deleteCustomerIndex = internalCustomers.findIndex(
      (item) => item.level === level && item.department === department && item.legalEntity === legalEntity,
    );
    this.setState({
      showDeleteModal: true,
      editCustomerIndex: deleteCustomerIndex,
    });
  };

  protected onDeleteExternalCustomer = (customer: IExternalCustomerDetails) => {
    const { companyName, customerRelation, name } = customer;
    const { externalCustomers } = this.state.customer;
    const deleteCustomerIndex = externalCustomers.findIndex(
      (item) => item.companyName === companyName && item.customerRelation === customerRelation && item.name === name,
    );
    this.setState({
      showDeleteModal: true,
      editCustomerIndex: deleteCustomerIndex,
    });
  };

  protected onEditInternalCustomer = () => {
    const { editCustomerIndex } = this.state;
    const { 
      level, 
      department, 
      legalEntity, 
      comment,
      // name,
      customerRelation,
      division,
      accessToSensibleData,
      processOwner 
    } = this.state.internalCustomerInfo;
    const { internalCustomers: addedCustomerList } = this.state.customer;
    const customerExists = this.isCustomerExist(addedCustomerList);
    const newIndex = addedCustomerList.findIndex(
      (item) => item.level === level && item.department === department && item.legalEntity === legalEntity,
    );
    if (this.validateInternalCustomerModal()) {
      if ((customerExists && editCustomerIndex === newIndex) || !customerExists) {
        const customerList = [...addedCustomerList]; // create copy of original array
        customerList[editCustomerIndex] = { 
          level, 
          department, 
          legalEntity, 
          comment,
          // name,
          customerRelation,
          division: division.id ? division : null,
          accessToSensibleData,
          processOwner,
        }; // modify copied array
        const customer = this.props.customer;
        customer.internalCustomers = customerList;
        this.setState((prevState) => ({
          editCustomer: false,
          duplicateCustomerAdded: false,
          customer: {
            ...prevState.customer,
            internalCustomers: customerList,
          },
          internalCustomerInfo: {
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            customerRelation: 'Internal',
            comment: '',
            department: '',
            level: '',
            legalEntity: '',
            division: '',
            accessToSensibleData: 'false',
            processOwner: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            }
          },
          internalCustomerErrors: {
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            customerRelation: 'Internal',
            comment: '',
            department: '',
            level: '',
            legalEntity: '',
            division: '',
            accessToSensibleData: 'false',
            processOwner: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            }
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

  protected onEditExternalCustomer = () => {
    const { editCustomerIndex } = this.state;
    const { 
      name, 
      comment,
      customerRelation,
      companyName } = this.state.externalCustomerInfo;
    const { externalCustomers: addedCustomerList } = this.state.customer;
    const customerExists = this.isExternalCustomerExist(addedCustomerList);
    const newIndex = addedCustomerList.findIndex(
      (item) => item.name === name && item.companyName === companyName && item.comment === comment,
    );
    if (this.validateExternalCustomerModal()) {
      if ((customerExists && editCustomerIndex === newIndex) || !customerExists) {
        const customerList = [...addedCustomerList]; // create copy of original array
        customerList[editCustomerIndex] = { 
          name, 
          comment,
          customerRelation,
          companyName }; // modify copied array
        const customer = this.props.customer;
        customer.externalCustomers = customerList;
        this.setState((prevState) => ({
          editCustomer: false,
          duplicateCustomerAdded: false,
          customer: {
            ...prevState.customer,
            externalCustomers: customerList,
          },
          externalCustomerInfo:{
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            companyName: '',
            customerRelation: '',
            comment: ''
          },
          externalCustomerErrors:{
            name: {
              company: '',
              department: '',
              email: '',
              firstName: '',
              shortId: '',
              lastName: '',
              mobileNumber: '',
              teamMemberPosition: '',
              userType: ''
            },
            companyName: '',
            customerRelation: '',
            comment: ''
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

  protected validateInternalCustomerModal = () => {
    let formValid = true;
    const errors = this.state.internalCustomerErrors;
    const errorMissingEntry = '*Missing entry';

    if (!this.state.internalCustomerInfo.level) {
      errors.level = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.internalCustomerInfo.department) {
      errors.department = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.internalCustomerInfo.legalEntity) {
      errors.legalEntity = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.internalCustomerInfo.customerRelation) {
      errors.customerRelation = errorMissingEntry;
      formValid = false;
    }
    // if (this.state.internalCustomerInfo.division.id === '0') {
    //   errors.division = errorMissingEntry;
    //   formValid = false;
    // }
    // if (!this.state.customerInfo.usRisk) {
    //   errors.usRisk = errorMissingEntry;
    //   formValid = false;
    // }
    // if (!this.state.customerInfo.companyName) {
    //   errors.companyName = errorMissingEntry;
    //   formValid = false;
    // }
    else {
      errors.comment = '';
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ internalCustomerErrors: errors });
    return formValid;
  };

  protected validateExternalCustomerModal = () => {
    let formValid = true;
    const errors = this.state.externalCustomerErrors;
    const errorMissingEntry = '*Missing entry';

    if (!this.state.externalCustomerInfo.companyName) {
      errors.companyName = errorMissingEntry;
      formValid = false;
    }
    if (!this.state.externalCustomerInfo.customerRelation) {
      errors.customerRelation = errorMissingEntry;
      formValid = false;
    }
    
    else {
      errors.comment = '';
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    this.setState({ externalCustomerErrors: errors });
    return formValid;
  };

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetAddTeamMemberState();
    });
  };

  // protected updateTeamMemberList = (teamMemberObj: ITeams) => {
  //   const { editTeamMember, editTeamMemberIndex, customer, addProductOwnerInController } = this.state;
  //   const teamMembers = customer.processOwners;
  //   if (editTeamMember) {
  //     teamMembers.splice(editTeamMemberIndex, 1);
  //     teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
  //   } else {
  //     teamMembers.push(teamMemberObj);
  //   }
  //   const stateUpdateObj = {
  //     showAddTeamMemberModal: false,
  //     customer: {
  //       customerDetails: this.state.customer.customerDetails,
  //       processOwners: this.state.customer.processOwners,
  //     },
  //   };

  //   if (addProductOwnerInController) {
  //     stateUpdateObj.customer.processOwners = teamMembers;
  //   } else {
  //     stateUpdateObj.customer.processOwners = teamMembers;
  //   }
  //   this.setState(stateUpdateObj, () => {
  //     this.resetAddTeamMemberState();
  //   });
  // };
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

  // protected onTeamMemberMoveUp = (index: number) => {
  //   const processOwner = this.state.customer.processOwners;
  //   const teamMember = processOwner.splice(index, 1)[0];
  //   processOwner.splice(index - 1, 0, teamMember);
  //   this.setState((prevState) => ({
  //     ...prevState,
  //     customer: {
  //       ...prevState.customer,
  //       processOwner,
  //     },
  //   }));
  // };

  // protected onTeamMemberMoveDown = (index: number) => {
  //   const processOwner = this.state.customer.processOwners;
  //   const teamMember = processOwner.splice(index, 1)[0];
  //   processOwner.splice(index + 1, 0, teamMember);
  //   this.setState((prevState) => ({
  //     ...prevState,
  //     customer: {
  //       ...prevState.customer,
  //       processOwner,
  //     },
  //   }));
  // };

  // protected onSharingTeamMemberEdit = (index: number) => {
  //   this.setState({ addProductOwnerInController: false }, () => {
  //     this.onTeamMemberEdit(index);
  //   });
  // };
  // protected onSharingTeamMemberDelete = (index: number) => {
  //   this.setState({ addProductOwnerInController: false }, () => {
  //     this.onTeamMemberDelete(index);
  //   });
  // };

  // protected onTeamMemberDelete = (index: number) => {
  //   const {
  //     customer: { processOwners },
  //     addProductOwnerInController,
  //   } = this.state;
  //   const teamMembers = addProductOwnerInController ? processOwners : processOwners;
  //   teamMembers.splice(index, 1);
  //   if (addProductOwnerInController) {
  //     this.setState((prevState) => ({
  //       ...prevState,
  //       customer: {
  //         ...prevState.customer,
  //         processOwner: teamMembers,
  //       },
  //     }));
  //   } else {
  //     this.setState((prevState) => ({
  //       ...prevState,
  //       customer: {
  //         ...prevState.customer,
  //         processOwner: teamMembers,
  //       },
  //     }));
  //   }
  // };

  // protected onTeamMemberEdit = (index: number) => {
  //   const {
  //     customer: { processOwners },
  //     addProductOwnerInController,
  //   } = this.state;
  //   const teamMemberObj = addProductOwnerInController ? processOwners[index] : processOwners[index];
  //   this.setState(
  //     {
  //       teamMemberObj,
  //       showAddTeamMemberModal: true,
  //       editTeamMember: true,
  //       editTeamMemberIndex: index,
  //     },
  //     () => {
  //       this.addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, true);
  //     },
  //   );
  // };
  sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: IInternalCustomerDetails[] = [];

      if (columnName === columnName) {
        sortedArray = this.state.customer.internalCustomers?.sort((a, b) => {
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
