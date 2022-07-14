import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { TeamMemberType } from '../../../../globals/Enums';
import { ITeams } from '../../../../globals/types';
import { ApiClient } from '../../../../services/ApiClient';
// @ts-ignore
import InputFieldsUtils from '../../../formElements/InputFields/InputFieldsUtils';
import Modal from '../../../formElements/modal/Modal';
import Styles from './AddTeamMemberModal.scss';
import { Envs } from '../../../../globals/Envs';
import * as Validation from '../../../../utils/Validation';
import TeamSearch from '../../teamSearch/TeamSearch';
import TextBox from '../../shared/textBox/TextBox';

const classNames = cn.bind(Styles);

export interface IAddTeamMemberModalProps {
  modalTitleText?: string;
  editMode: boolean;
  showAddTeamMemberModal: boolean;
  teamMember: ITeams;
  showOnlyInteral?: boolean;
  hideTeamPosition?: boolean;
  teamPositionNotRequired?: boolean;
  onAddTeamMemberModalCancel: () => void;
  onUpdateTeamMemberList: (teamMemberObj: ITeams) => void;
  validateMemebersList?: (teamMemberObj: ITeams) => boolean;
}

export interface IAddTeamMemberModalState {
  belongingInternal: boolean;
  teamPositionInternal: string;
  teamPositionInternalError: string;
  userIdInternal: string;
  userIdInternalError: string;
  companyExternal: string;
  companyExternalError: string;
  teamPositionExternal: string;
  teamPositionExternalError: string;
  teamMemberObj: ITeams;
  showNotFoundError: boolean;
  teamMemberError: string;
  firstName: string;
  firstNameError: string;
  lastName: string;
  lastNameError: string;
  mobileNumber: string;
  mobileNumberError: string;
  email: string;
  emailError: string;
  company: string;
  companyError: string;
  department: string;
  departmentError: string;
  shortID: string;
  shortIDError: string;
  teamPosition: string;
  teamPositionError: string;
  isEmailValid: boolean;
  showUserAlreadyExistsError: boolean;
}

export default class AddTeamMemberModal extends React.Component<IAddTeamMemberModalProps, IAddTeamMemberModalState> {
  constructor(props: IAddTeamMemberModalProps) {
    super(props);
    this.state = {
      belongingInternal: true,
      teamPositionInternal: '',
      teamPositionInternalError: null,
      userIdInternal: '',
      userIdInternalError: null,
      companyExternal: '',
      companyExternalError: null,
      teamPositionExternal: '',
      teamPositionExternalError: null,
      teamMemberObj: this.props.teamMember,
      showNotFoundError: false,
      teamMemberError: null,
      firstName: '',
      firstNameError: null,
      lastName: '',
      lastNameError: null,
      mobileNumber: '',
      mobileNumberError: null,
      email: '',
      emailError: null,
      company: '',
      companyError: null,
      department: '',
      departmentError: null,
      shortID: '',
      shortIDError: null,
      teamPosition: '',
      teamPositionError: null,
      isEmailValid: true,
      showUserAlreadyExistsError: false,
    };
    this.validateMobile = this.validateMobile.bind(this);
    this.validateEmailID = this.validateEmailID.bind(this);
  }

  public render() {
    // const requiredError = '*Missing entry';
    const teamPositionInternalError = this.state.teamPositionInternalError || '';
    // const userIdInternalError = this.state.userIdInternalError || '';
    const companyExternalError = this.state.companyExternalError || '';
    const teamPositionExternalError = this.state.teamPositionExternalError || '';

    const firstNameError = this.state.firstNameError || '';
    const lastNameError = this.state.lastNameError || '';
    const mobileNumberError = this.state.mobileNumberError || '';
    const emailError = this.state.emailError || '';
    const companyError = this.state.companyError || '';
    const departmentError = this.state.departmentError || '';
    const shortIDError = this.state.shortIDError || '';
    const teamPositionError = this.state.teamPositionError || '';

    const {
      belongingInternal,
      // userIdInternal,
      teamPositionInternal,
      teamPositionExternal,
      companyExternal,
      // teamMemberObj,
      firstName,
      lastName,
      mobileNumber,
      email,
      company,
      department,
      shortID,
      teamPosition,
      showUserAlreadyExistsError,
    } = this.state;

    const addTeamMemberModalContent: React.ReactNode = (
      <div id="teamsModalDiv" className={classNames(Styles.firstPanel, Styles.addTeamMemberModal, 'mbc-scroll')}>
        <div className={Styles.formWrapper}>
          {this.props.showOnlyInteral ? (
            ''
          ) : (
            <div className="input-field-group">
              <label htmlFor="usr" className="input-label">
                Belonging<sup>*</sup>
              </label>
              <div>
                <label className={classNames('radio', this.props.editMode ? 'disabled' : '')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="Internal"
                      name="belonging"
                      onChange={this.onBelongingChange}
                      checked={belongingInternal}
                      disabled={this.props.editMode}
                    />
                  </span>
                  <span className="label">Internal ({Envs.DNA_COMPANY_NAME} Login)</span>
                </label>
                <label className={classNames('radio', this.props.editMode ? 'disabled' : '')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="External"
                      name="belonging"
                      onChange={this.onBelongingChange}
                      checked={!belongingInternal}
                      disabled={this.props.editMode}
                    />
                  </span>
                  <span className="label">External</span>
                </label>
              </div>
            </div>
          )}
          <div className={belongingInternal ? Styles.internalWrapper : 'hide'}>
            {!this.props.hideTeamPosition ? (
              <TextBox
                type="text"
                controlId={'teamPositionInternal'}
                label={'Team Position (e.g. IT)'}
                placeholder={"Type here"}
                value={teamPositionInternal}
                errorText={teamPositionInternalError}
                required={!this.props.teamPositionNotRequired ? true : false}
                maxLength={200}
                onChange={this.textInputOnChange}
              />
            ) : (
              ''
            )}
            <TeamSearch
              label={
                <>
                  Find User<sup>*</sup>{' '}
                  <span dangerouslySetInnerHTML={{__html: Envs.INTERNAL_USER_TEAMS_INFO}}></span>
                </>
              }
              editMode={this.props.editMode}
              teamMemberObj={this.props.teamMember}
              onAddTeamMember={this.addMemberFromTeamSearch}
              userAlreadyExists={showUserAlreadyExistsError}
              resetUserAlreadyExists={this.resetUserAlreadyExists}
              btnText="Save"
            />
          </div>
          <div className={!belongingInternal ? Styles.externalWrapper : 'hide'}>
            <div className={Styles.flexLayout}>
              <TextBox
                type="text"
                controlId={'companyExternal'}
                label={'Company'}
                placeholder={"Type here"}
                value={companyExternal}
                errorText={companyExternalError}
                required={true}
                maxLength={200}
                onChange={this.textInputOnChange}
              />
              
              <TextBox
                type="text"
                controlId={'teamPositionExternal'}
                label={'Team Position (e.g. IT)'}
                placeholder={"Type here"}
                value={teamPositionExternal}
                errorText={teamPositionExternalError}
                required={true}
                maxLength={200}
                onChange={this.textInputOnChange}
              />
            </div>
            <div className={Styles.actionWrapper}>
              <button className="btn btn-primary" onClick={this.addTeamMember} type="button">
                Save
              </button>
            </div>
          </div>
        </div>
      </div>
    );

    const addTeamMemberModalContentForFoss: React.ReactNode = (
      <div id="teamsModalDiv" className={classNames(Styles.firstPanel, Styles.addTeamMemberModal)}>
        <div className={Styles.formWrapper}>
          { !(Envs.OIDC_PROVIDER === 'INTERNAL') &&
            <TextBox
              type="email"
              controlId={'shortID'}
              label={'User Name / Email'}
              placeholder={"example@example.com"}
              value={shortID}
              errorText={shortIDError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
              onBlur={this.validateEmailID}
            />
          }
          <div className={classNames(Styles.flexLayout, Envs.OIDC_PROVIDER !== 'INTERNAL' ? 'hide' : '')}>
            <TextBox
              type="text"
              controlId={'shortID'}
              label={'User ID'}
              placeholder={"Type here"}
              value={shortID}
              errorText={shortIDError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
            />

            <TextBox
              type="email"
              controlId={'email'}
              label={'Email'}
              placeholder={"example@example.com"}
              value={email}
              errorText={emailError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
              onBlur={this.validateEmailID}
            />
          </div>
          <div className={Styles.flexLayout}>
            <TextBox
              type="text"
              controlId={'firstName'}
              label={'First Name'}
              placeholder={"Type here"}
              value={firstName}
              errorText={firstNameError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
            />

            <TextBox
              type="text"
              controlId={'lastName'}
              label={'Last Name'}
              placeholder={"Type here"}
              value={lastName}
              errorText={lastNameError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
            />
          </div>

          <div className={Styles.flexLayout}>
            <TextBox
              type="text"
              controlId={'company'}
              label={'Company'}
              placeholder={"Type here"}
              value={company}
              errorText={companyError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
            />

            <TextBox
              type="text"
              controlId={'department'}
              label={'Department'}
              placeholder={"Type here"}
              value={department}
              errorText={departmentError}
              required={true}
              maxLength={200}
              onChange={this.textInputOnChange}
            />
          </div>

          <div className={Styles.flexLayout}>
            {!this.props.hideTeamPosition ? (
              <TextBox
                type="text"
                controlId={'teamPosition'}
                label={'Team Position (e.g. IT)'}
                placeholder={"Type here"}
                value={teamPosition}
                errorText={teamPositionError}
                required={!this.props.teamPositionNotRequired ? true : false}
                maxLength={200}
                onChange={this.textInputOnChange}
              />
            ) : (
              ''
            )}
            <TextBox
              type="text"
              controlId={'mobileNumber'}
              label={'Mobile No.'}
              placeholder="+49123456"
              value={mobileNumber}
              errorText={mobileNumberError}
              required={true}
              maxLength={15}
              onChange={this.validateMobile}
            />
          </div>

          <div className={classNames(Styles.flexLayout, Styles.actionWrapper, Styles.saveButton)}>
            <div>
              <button className="btn btn-primary" onClick={this.addTeamMemberForFoss} type="button">
                Save
              </button>
            </div>
          </div>
          {/* </div> */}
        </div>
      </div>
    );

    return (
      <Modal
        title={
          (this.props.editMode ? 'Edit' : 'Add') +
          ' ' +
          (this.props.modalTitleText ? this.props.modalTitleText : 'team member')
        }
        showAcceptButton={false}
        showCancelButton={false}
        buttonAlignment="right"
        show={this.props.showAddTeamMemberModal}
        content={Envs.ENABLE_INTERNAL_USER_INFO ? addTeamMemberModalContent : addTeamMemberModalContentForFoss}
        onCancel={this.onModalCancel}
      />
    );
  }

  public setTeamMemberData = (teamMemberObj: ITeams, editMode: boolean) => {
    if (editMode) {
      if (teamMemberObj.userType === TeamMemberType.INTERNAL) {
        this.setState({
          belongingInternal: true,
          teamPositionInternal: teamMemberObj.teamMemberPosition,
          userIdInternal: teamMemberObj.shortId,
          teamMemberObj,
          firstName: teamMemberObj.firstName,
          lastName: teamMemberObj.lastName,
          company: teamMemberObj.company,
          department: teamMemberObj.department,
          mobileNumber: teamMemberObj.mobileNumber,
          email: teamMemberObj.email,
          shortID: teamMemberObj.shortId,
          teamPosition: teamMemberObj.teamMemberPosition,
        });
      } else {
        this.setState({
          belongingInternal: false,
          teamPositionExternal: teamMemberObj.teamMemberPosition,
          companyExternal: teamMemberObj.company,
          teamMemberObj,
          firstName: teamMemberObj.firstName,
          lastName: teamMemberObj.lastName,
          company: teamMemberObj.company,
          department: teamMemberObj.department,
          mobileNumber: teamMemberObj.mobileNumber,
          email: teamMemberObj.email,
          shortID: teamMemberObj.shortId,
          teamPosition: teamMemberObj.teamMemberPosition,
        });
      }
    } else {
      this.clearModalFields();
    }
  };

  protected onModalCancel = () => {
    this.clearModalFields();
    this.props.onAddTeamMemberModalCancel();
  };

  protected onSearchClearIconClick = () => {
    this.setState({ userIdInternal: '' });
  };

  protected onSearchClick = () => {
    const internalMemberPosition = this.state.teamPositionInternal;
    const seachUserId = this.state.userIdInternal;

    ProgressIndicator.show();

    ApiClient.getDRDUserInfo(seachUserId)
      .then((data: any) => {
        const teamMemberObj: ITeams = {
          department: data.department,
          email: data.email,
          firstName: data.firstName,
          shortId: data.id,
          lastName: data.lastName,
          userType: TeamMemberType.INTERNAL,
          teamMemberPosition: internalMemberPosition,
        };

        if (data.mobileNumber !== '') {
          teamMemberObj.mobileNumber = data.mobileNumber;
        }

        this.setState(
          {
            teamMemberObj,
            showNotFoundError: false,
          },
          () => {
            ProgressIndicator.hide();
          },
        );
      })
      .catch(() => {
        ProgressIndicator.hide();
        this.setState({
          showNotFoundError: true,
        });
      });
  };

  protected textInputOnChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const name: string = e.currentTarget.name;
    const value: string = e.currentTarget.value;
    this.setState(
      (prevState) => ({
        ...prevState,
        [name]: value,
      }),
      () => {
        this.formFieldsErrorValidation(name);
        if (name === 'email') {
          this.validateEmailID(e);
        } else {
          name === 'shortID' && e.target.type === 'email' && this.validateEmailID(e);
        }
      },
    );
  };

  protected addTeamMember = () => {
    let teamMember: ITeams = this.state.teamMemberObj;
    const belongingInternal = teamMember.userType === TeamMemberType.INTERNAL;
    let proceedSave = false;
    if (belongingInternal && this.validateInternalTeamMemberForm()) {
      teamMember.teamMemberPosition = this.state.teamPositionInternal;
      proceedSave = true;
    } else if (this.validateExternalTeamMemberForm()) {
      teamMember = {
        company: this.state.companyExternal,
        teamMemberPosition: this.state.teamPositionExternal,
        userType: TeamMemberType.EXTERNAL,
      };
      proceedSave = true;
    }

    if (proceedSave) {
      this.props.onUpdateTeamMemberList(teamMember);
    }
  };

  protected addMemberFromTeamSearch = (teamMemberObj: ITeams) => {
    const teamMember: ITeams = teamMemberObj;
    const belongingInternal = teamMember.userType === TeamMemberType.INTERNAL;
    if (belongingInternal && this.validateInternalTeamMemberFormTeamSearch(teamMemberObj)) {
      teamMember.teamMemberPosition = this.state.teamPositionInternal;
      this.props.onUpdateTeamMemberList(teamMember);
    }
  };

  protected validateInternalTeamMemberFormTeamSearch = (teamMemberObj: ITeams) => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';

    if (this.state.teamPositionInternal === '' && !this.props.hideTeamPosition && !this.props.teamPositionNotRequired) {
      this.setState({ teamPositionInternalError: errorMissingEntry });
      formValid = false;
    }

    if (teamMemberObj.shortId === '') {
      this.setState({ userIdInternalError: errorMissingEntry });
      formValid = false;
    }

    if (!this.props.editMode && typeof this.props.validateMemebersList === 'function') {
      const isDuplicate = this.props.validateMemebersList(teamMemberObj);
      if (isDuplicate) {
        this.setState({ showUserAlreadyExistsError: true });
        formValid = false;
      }
    }

    return formValid;
  };

  protected resetUserAlreadyExists = () => {
    this.setState({ showUserAlreadyExistsError: false });
  };

  protected addTeamMemberForFoss = () => {
    const teamMember: ITeams = this.state.teamMemberObj;
    let proceedSave = false;
    if (this.validateInternalTeamMemberFormForFoss()) {
      teamMember.company = this.state.company;
      teamMember.department = this.state.department;
      teamMember.email = Envs.OIDC_PROVIDER === 'INTERNAL' ? this.state.email : this.state.shortID;
      teamMember.firstName = this.state.firstName;
      teamMember.lastName = this.state.lastName;
      teamMember.mobileNumber = this.state.mobileNumber;
      teamMember.shortId = this.state.shortID;
      teamMember.userType = TeamMemberType.INTERNAL;
      teamMember.teamMemberPosition = this.state.teamPosition;
      proceedSave = true;
    }
    if (proceedSave) {
      this.props.onUpdateTeamMemberList(teamMember);
    }
  };

  protected clearModalFields() {
    this.setState({
      belongingInternal: true,
      teamPositionInternal: '',
      teamPositionInternalError: null,
      userIdInternal: '',
      userIdInternalError: null,
      companyExternal: '',
      companyExternalError: '',
      teamPositionExternal: '',
      teamPositionExternalError: '',
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
      showNotFoundError: false,
    });

    InputFieldsUtils.resetErrors('#teamsModalDiv');
  }

  protected onBelongingChange = (e: React.FormEvent<HTMLInputElement>) => {
    this.setState({ belongingInternal: e.currentTarget.value === 'Internal' });
  };

  protected onSearchKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    if (keyPressed === 13) {
      this.onSearchClick();
    }
  };

  protected validateInternalTeamMemberForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';

    if (this.state.teamPositionInternal === '' && !this.props.hideTeamPosition && !this.props.teamPositionNotRequired) {
      this.setState({ teamPositionInternalError: errorMissingEntry });
      formValid = false;
    }

    if (this.state.teamMemberObj.shortId === '') {
      this.setState({ userIdInternalError: errorMissingEntry });
      formValid = false;
    }

    if (!this.props.editMode && typeof this.props.validateMemebersList === 'function') {
      const isDuplicate = this.props.validateMemebersList(this.state.teamMemberObj);
      formValid = !isDuplicate;
    }

    return formValid;
  };

  protected validateExternalTeamMemberForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';

    if (this.state.companyExternal === '') {
      this.setState({ companyExternalError: errorMissingEntry });
      formValid = false;
    }

    if (this.state.teamPositionExternal === '') {
      this.setState({ teamPositionExternalError: errorMissingEntry });
      formValid = false;
    }

    return formValid;
  };

  protected validateInternalTeamMemberFormForFoss = () => {
    let formValid = true;
    if (Envs.OIDC_PROVIDER === 'INTERNAL') {
      if(this.props.hideTeamPosition) {
        if (
          this.state.shortID === '' ||
          this.state.shortID === null ||
          this.state.company === '' ||
          this.state.company === null ||
          this.state.department === '' ||
          this.state.department === null ||
          this.state.firstName === '' ||
          this.state.firstName === null ||
          this.state.lastName === '' ||
          this.state.lastName === null ||
          this.state.email === '' ||
          this.state.email === null ||
          this.state.mobileNumber === '' ||
          this.state.mobileNumber === null ||
          !this.state.isEmailValid
        ) {
          formValid = false;
        }
      } else {
        if (
          this.state.teamPosition === '' ||
          this.state.teamPosition === null ||
          this.state.shortID === '' ||
          this.state.shortID === null ||
          this.state.company === '' ||
          this.state.company === null ||
          this.state.department === '' ||
          this.state.department === null ||
          this.state.firstName === '' ||
          this.state.firstName === null ||
          this.state.lastName === '' ||
          this.state.lastName === null ||
          this.state.email === '' ||
          this.state.email === null ||
          this.state.mobileNumber === '' ||
          this.state.mobileNumber === null ||
          !this.state.isEmailValid
        ) {
          formValid = false;
        }
      }
      
      ['shortID', 'email', 'firstName', 'lastName', 'company', 'department', 'teamPosition', 'mobileNumber'].map(
        (field) => this.formFieldsErrorValidation(field),
      );
    } else {
      if(this.props.hideTeamPosition) {
        if (
          this.state.company === '' ||
          this.state.company === null ||
          this.state.department === '' ||
          this.state.department === null ||
          this.state.firstName === '' ||
          this.state.firstName === null ||
          this.state.lastName === '' ||
          this.state.lastName === null ||
          this.state.shortID === '' ||
          this.state.shortID === null ||
          this.state.mobileNumber === '' ||
          this.state.mobileNumber === null ||
          !this.state.isEmailValid
        ) {
          formValid = false;
        }
      } else {
        if (
          this.state.teamPosition === '' ||
          this.state.teamPosition === null ||
          this.state.company === '' ||
          this.state.company === null ||
          this.state.department === '' ||
          this.state.department === null ||
          this.state.firstName === '' ||
          this.state.firstName === null ||
          this.state.lastName === '' ||
          this.state.lastName === null ||
          this.state.shortID === '' ||
          this.state.shortID === null ||
          this.state.mobileNumber === '' ||
          this.state.mobileNumber === null ||
          !this.state.isEmailValid
        ) {
          formValid = false;
        }
      }
      
      ['shortID', 'teamPosition', 'company', 'department', 'firstName', 'lastName', 'mobileNumber'].map((field) =>
        this.formFieldsErrorValidation(field),
      );
    }
    return formValid;
  };

  protected formFieldsErrorValidation = (name: string) => {
    const errorMissingEntry = '*Missing entry';
    switch (name) {
      case 'shortID':
        {
          if (this.state.shortID === '' || this.state.shortID === null) {
            this.setState({ shortIDError: errorMissingEntry });
          } else {
            if (Envs.OIDC_PROVIDER === 'INTERNAL') {
              this.setState({ shortIDError: '' });
            } else {
              this.state.isEmailValid && this.setState({ shortIDError: '' });
            }
          }
        }
        break;
      case 'email':
        {
          if (this.state.email === '' || this.state.email === null) {
            this.setState({ emailError: errorMissingEntry });
          } else {
            if (this.state.isEmailValid) {
              this.setState({ emailError: '' });
            }
          }
        }
        break;
      case 'firstName':
        {
          if (this.state.firstName === '' || this.state.firstName === null) {
            this.setState({ firstNameError: errorMissingEntry });
          } else {
            this.setState({ firstNameError: '' });
          }
        }
        break;
      case 'lastName':
        {
          if (this.state.lastName === '' || this.state.lastName === null) {
            this.setState({ lastNameError: errorMissingEntry });
          } else {
            this.setState({ lastNameError: '' });
          }
        }
        break;
      case 'company':
        {
          if (this.state.company === '' || this.state.company === null) {
            this.setState({ companyError: errorMissingEntry });
          } else {
            this.setState({ companyError: '' });
          }
        }
        break;
      case 'department':
        {
          if (this.state.department === '' || this.state.department === null) {
            this.setState({ departmentError: errorMissingEntry });
          } else {
            this.setState({ departmentError: '' });
          }
        }
        break;
      case 'teamPosition':
        {
          if ((this.state.teamPosition === '' || this.state.teamPosition === null) && !this.state.teamPosition) {
            this.setState({ teamPositionError: errorMissingEntry });
          } else {
            this.setState({ teamPositionError: '' });
          }
        }
        break;
      case 'mobileNumber':
        {
          if (this.state.mobileNumber === '' || this.state.mobileNumber === null) {
            this.setState({ mobileNumberError: errorMissingEntry });
          } else {
            this.setState({ mobileNumberError: '' });
          }
        }
        break;
      default:
        null;
    }
  };

  protected validateMobile(el: React.FormEvent<HTMLInputElement>) {
    const numberVal = el.currentTarget.value;
    if (Validation.validateMobileNumber(numberVal)) {
      this.setState({ mobileNumber: numberVal }, () => {
        this.formFieldsErrorValidation('mobileNumber');
      });
    }
  }

  protected validateEmailID(el: React.ChangeEvent<HTMLInputElement>) {
    const emailVal = el.target.value;
    if (Envs.OIDC_PROVIDER === 'INTERNAL') {
      if (!Validation.validateEmail(emailVal)) {
        this.setState({ emailError: 'Invalid Email', isEmailValid: false });
      } else {
        this.setState({ emailError: '', isEmailValid: true });
      }
    } else {
      if (!Validation.validateEmail(emailVal)) {
        this.setState({ shortIDError: 'Invalid User Name / Email', isEmailValid: false });
      } else {
        this.setState({ shortIDError: '', isEmailValid: true });
      }
    }
  }
}
