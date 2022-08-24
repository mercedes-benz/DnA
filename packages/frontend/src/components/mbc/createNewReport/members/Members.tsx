import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
// @ts-ignore
import ImgCostDriver from '../../../../assets/images/cost-driver-info.png';
// @ts-ignore
import ImgMaturityLevel from '../../../../assets/images/maturity-level-info.png';
// @ts-ignore
import ImgRiskAssesment from '../../../../assets/images/risk-assesment-info.png';
// @ts-ignore
import ImgValueDriver from '../../../../assets/images/value-driver-info.png';
import { IconAvatarNew } from '../../../../components/icons/IconAvatarNew';
import { IAttachment, IMembers, ITeams } from '../../../../globals/types';
import AddTeamMemberModal from '../../addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from '../../addTeamMember/teamMemberListItem/TeamMemberListItem';
import Styles from './Members.scss';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import { ErrorMsg } from '../../../../globals/Enums';

const classNames = cn.bind(Styles);

export interface IMembersProps {
  members: IMembers;
  onPublish: () => void;
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyMember: (developers: ITeams[], productOwners: ITeams[], admin: ITeams[]) => void;
}

export interface IMembersState {
  members: IMembers;
  showAddTeamMemberModal: boolean;
  editTeamMember: boolean;
  editTeamMemberIndex: number;
  teamMemberObj: ITeams;
  addTeamMemberInController: boolean;
  addAdminInController: boolean;
  membersTabError: {
    developers: string;
    productOwner: string;
    admin: string;
  };
  hideTeamPosition: boolean;
  teamPositionNotRequired: boolean;
}

export interface IAttachmentResponse {
  error?: any;
  fileDetails: IAttachment;
}

export default class Members extends React.Component<IMembersProps, IMembersState> {
  public commentAreaInput: HTMLTextAreaElement;
  public strategyCommentAreaInput: HTMLTextAreaElement;
  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();
  // private addProductOwnerMemberModalRef = React.createRef<AddOrEditFactorModal>();

  public static getDerivedStateFromProps(props: IMembersProps, state: IMembersState) {
    return {
      members: props.members,
    };
  }

  constructor(props: IMembersProps) {
    super(props);
    this.state = {
      members: {
        developers: [],
        productOwners: [],
        admin: [],
      },
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
      addTeamMemberInController: true,
      addAdminInController: true,
      membersTabError: {
        developers: '',
        productOwner: '',
        admin: '',
      },
      hideTeamPosition: true,
      teamPositionNotRequired: true
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    const { developers: devsError, productOwner: POError, admin: adminError } = this.state.membersTabError;

    const developerTeamMembersList = this.state.members.developers
      ? this.state.members.developers?.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.members.developers.length}
              onMoveUp={this.onTeamMemberMoveUp}
              onMoveDown={this.onTeamMemberMoveDown}
              onEdit={this.onSharingTeamMemberEdit}
              onDelete={this.onSharingTeamMemberDelete}
            />
          );
        })
      : [];
    const productOwnerTeamMembersList = this.state.members.productOwners
      ? this.state.members.productOwners?.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.members.productOwners.length}
              onMoveUp={(index) => this.onTeamMemberMoveUp(index, 'productOwners')}
              onMoveDown={(index) => this.onTeamMemberMoveDown(index, 'productOwners')}
              onEdit={this.onControllerTeamMemberEdit}
              onDelete={this.onControllerTeamMemberDelete}
            />
          );
        })
      : [];

    const adminTeamMembersList = this.state.members.admin
      ? this.state.members.admin?.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.members.admin.length}
              onMoveUp={(index) => this.onTeamMemberMoveUp(index, 'admin')}
              onMoveDown={(index) => this.onTeamMemberMoveDown(index, 'admin')}
              onEdit={this.onAdminEdit}
              onDelete={this.onAdminDelete}
            />
          );
        })
      : [];

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Report Members</h3>
            <div className={classNames(Styles.teamListWrapper, POError ? 'error' : '')}>
              <h5>
                {' '}
                Product / Report Owner <sup>*</sup> (can edit/delete){' '}
              </h5>
              {productOwnerTeamMembersList.length ? productOwnerTeamMembersList : ''}
              {!this.state.members.productOwners.length && (
                <div className={Styles.addTeamMemberWrapper}>
                  <IconAvatarNew className={Styles.avatarIcon} />
                  <button id="AddTeamMemberBtn" onClick={this.addProductOwnerMember}>
                    <i className="icon mbc-icon plus" />
                    <span>Add member</span>
                  </button>
                  <div className={classNames(POError ? '' : 'hide')}>
                    <span className="error-message">{POError}</span>
                  </div>
                </div>
              )}
            </div>
          </div>
          <div className={classNames(Styles.firstPanel)}>
            <div className={classNames(Styles.teamListWrapper, devsError ? 'error' : '')}>
              <h5>
                {' '}
                Developer <sup>*</sup>{' '}
              </h5>
              {developerTeamMembersList.length ? developerTeamMembersList : ''}
              <div className={Styles.addTeamMemberWrapper}>
                {this.state.members.developers.length < 1 && <IconAvatarNew className={Styles.avatarIcon} />}
                <button id="AddTeamMemberBtn" onClick={this.addDeveloperMember}>
                  <i className="icon mbc-icon plus" />
                  <span>Add Developer</span>
                </button>
                {!this.state.members.developers.length && (
                  <div className={classNames(devsError ? '' : 'hide')}>
                    <span className="error-message">{devsError}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className={classNames(Styles.adminWrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Administration</h3>
            <div className={classNames(Styles.teamListWrapper, adminError ? 'error' : '')}>
              <h5>
                {' '}
                Report Admin <sup>*</sup> (can edit/delete){' '}
              </h5>
              {adminTeamMembersList.length ? adminTeamMembersList : ''}
              <div className={Styles.addTeamMemberWrapper}>
                {this.state.members.admin?.length < 1 && <IconAvatarNew className={Styles.avatarIcon} />}
                <button id="AddTeamMemberBtn" onClick={this.addAdminMember}>
                  <i className="icon mbc-icon plus" />
                  <span>Add Report Admin</span>
                </button>
                {!this.state.members.admin.length && (
                  <div className={classNames(adminError ? '' : 'hide')}>
                    <span className="error-message">{adminError}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <div className="btn-set">
            <button className="btn btn-primary" type="button" onClick={this.onSaveMembers}>
              Save
            </button>
            <button
              className={'btn btn-tertiary ' + classNames(Styles.publishBtn)}
              type="button"
              onClick={this.onMembersValueSubmit}
            >
              Publish
            </button>
          </div>
        </div>
        {this.state.showAddTeamMemberModal && (
          <AddTeamMemberModal
            ref={this.addTeamMemberModalRef}
            modalTitleText={'team member'}
            showOnlyInteral={true}
            editMode={this.state.editTeamMember}
            showAddTeamMemberModal={this.state.showAddTeamMemberModal}
            teamMember={this.state.teamMemberObj}
            hideTeamPosition={this.state.hideTeamPosition}
            teamPositionNotRequired={this.state.teamPositionNotRequired}
            onUpdateTeamMemberList={this.updateTeamMemberList}
            onAddTeamMemberModalCancel={this.onAddTeamMemberModalCancel}
            validateMemebersList={this.validateMembersList}
          />
        )}
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.members) {
      const members = this.props.members;
      members.developers = this.props.members.developers;
      members.productOwners = this.props.members.productOwners;
    }
  };
  protected addDeveloperMember = () => {
    this.setState({ addTeamMemberInController: false, addAdminInController: false, hideTeamPosition: false }, () => {
      this.showAddTeamMemberModalView();
    });
  };
  protected addProductOwnerMember = () => {
    this.setState({ addTeamMemberInController: true, hideTeamPosition: false }, () => {
      this.showAddTeamMemberModalView();
    });
  };
  protected addAdminMember = () => {
    this.setState({ addAdminInController: true, addTeamMemberInController: false, hideTeamPosition: true }, () => {
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

  protected onControllerTeamMemberEdit = (index: number) => {
    this.setState({ addTeamMemberInController: true, addAdminInController: false, hideTeamPosition: false }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onAdminEdit = (index: number) => {
    this.setState({ addAdminInController: true, addTeamMemberInController: false, hideTeamPosition: true }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onSharingTeamMemberEdit = (index: number) => {
    this.setState({ addTeamMemberInController: false, addAdminInController: false, hideTeamPosition: false }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onControllerTeamMemberDelete = (index: number) => {
    this.setState({ addTeamMemberInController: true, addAdminInController: false }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onAdminDelete = (index: number) => {
    this.setState({ addAdminInController: true, addTeamMemberInController: false }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onSharingTeamMemberDelete = (index: number) => {
    this.setState({ addTeamMemberInController: false, addAdminInController: false }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onTeamMemberEdit = (index: number) => {
    const {
      members: { developers, productOwners, admin },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    let teamMemberObj: ITeams;
    if (addTeamMemberInController) {
      teamMemberObj = productOwners[index];
    } else if (addAdminInController) {
      teamMemberObj = admin[index];
    } else {
      teamMemberObj = developers[index];
    }

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

  protected updateTeamMemberList = (teamMemberObj: ITeams) => {
    const {
      editTeamMember,
      editTeamMemberIndex,
      members: { developers, productOwners, admin },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    let teamMembers = developers;
    if (addTeamMemberInController) {
      teamMembers = productOwners;
    } else if (addAdminInController) {
      teamMembers = admin;
    }
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    const stateUpdateObj = {
      showAddTeamMemberModal: false,
      members: {
        productOwners: this.state.members.productOwners,
        developers: this.state.members.developers,
        admin: this.state.members.admin,
      },
    };

    if (addTeamMemberInController) {
      stateUpdateObj.members.productOwners = teamMembers;
    } else if (addAdminInController) {
      stateUpdateObj.members.admin = teamMembers;
    } else {
      stateUpdateObj.members.developers = teamMembers;
    }

    this.setState(stateUpdateObj, () => {
      this.resetAddTeamMemberState();
      // this.props.modifyTeam(this.state.teamMembers);
    });
  };

  protected validateMembersList = (teamMemberObj: ITeams) => {
    const { admin, developers } = this.state.members;
    const { addAdminInController, addTeamMemberInController } = this.state;
    let duplicateMember = false;
    if (addAdminInController) {
      duplicateMember = admin?.filter((admin) => admin.shortId === teamMemberObj.shortId)?.length ? true : false;
    } else if (!addTeamMemberInController) {
      duplicateMember = developers?.filter((admin) => admin.shortId === teamMemberObj.shortId)?.length ? true : false;
    }
    return duplicateMember;
  };

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetAddTeamMemberState();
    });
  };

  protected onTeamMemberDelete = (index: number) => {
    const {
      members: { productOwners, developers, admin },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    const teamMembers = addTeamMemberInController ? productOwners : addAdminInController ? admin : developers;
    teamMembers.splice(index, 1);
    if (addTeamMemberInController) {
      this.setState((prevState) => ({
        members: {
          ...prevState.members,
          productOwners: teamMembers,
        },
      }));
    } else if (addAdminInController) {
      this.setState((prevState) => ({
        members: {
          ...prevState.members,
          admin: teamMembers,
        },
      }));
    } else {
      this.setState((prevState) => ({
        members: {
          ...prevState.members,
          developers: teamMembers,
        },
      }));
    }
  };
  protected onTeamMemberMoveUp = (index: number, teamMembers = 'developers') => {
    const members = teamMembers === 'developers' ? this.state.members.developers : this.state.members.productOwners;
    const teamMember = members.splice(index, 1)[0];
    members.splice(index - 1, 0, teamMember);
    this.setState((prevState) => ({
      members: {
        ...prevState.members,
        [teamMembers]: members,
      },
    }));
  };
  protected onTeamMemberMoveDown = (index: number, teamMembers = 'developers') => {
    const members = teamMembers === 'developers' ? this.state.members.developers : this.state.members.productOwners;
    const teamMember = members.splice(index, 1)[0];
    members.splice(index + 1, 0, teamMember);
    this.setState((prevState) => ({
      members: {
        ...prevState.members,
        [teamMembers]: members,
      },
    }));
  };

  protected onSaveMembers = () => {
    if (this.validateMembersTab()) {
      this.props.modifyMember(
        this.state.members.developers,
        this.state.members.productOwners,
        this.state.members.admin,
      );
      this.props.onSaveDraft('members');
    }
  };

  protected onMembersValueSubmit = () => {
    if (this.validateMembersTab()) {
      this.props.modifyMember(
        this.state.members.developers,
        this.state.members.productOwners,
        this.state.members.admin,
      );
      this.props.onPublish();
    }
  };

  protected validateMembersTab = () => {
    const { developers: devList, productOwners: POList, admin: adminList } = this.state.members;
    let developers = '';
    let productOwner = '';
    let admin = '';
    if (!devList.length) developers = ErrorMsg.MEMBERS_TAB_DEVELOPERS;
    if (!POList.length) productOwner = ErrorMsg.MEMBERS_TAB_PRODUCT_OWNER;
    if (!adminList.length) admin = ErrorMsg.MEMBERS_TAB_ADMIN;
    this.setState({
      membersTabError: {
        developers,
        productOwner,
        admin,
      },
    });
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return devList.length && POList.length && adminList.length;
  };
}
