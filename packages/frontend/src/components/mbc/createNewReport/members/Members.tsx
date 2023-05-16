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
import IconAvatarNew from 'components/icons/IconAvatarNew';
import { IAttachment, IMembers, ITeams } from 'globals/types';
import AddTeamMemberModal from 'components/mbc/addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from 'components/mbc/addTeamMember/teamMemberListItem/TeamMemberListItem';
import Styles from './Members.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { ErrorMsg } from 'globals/Enums';

const classNames = cn.bind(Styles);

export interface IMembersProps {
  members: IMembers;
  onPublish: () => void;
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyMember: (productOwners: ITeams[], admin: ITeams[]) => void;
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
        reportOwners: [],
        reportAdmins: [],
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
        productOwner: '',
        admin: '',
      },
      hideTeamPosition: true,
      teamPositionNotRequired: true,
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    const { 
      // productOwner: POError, 
      admin: adminError } = this.state.membersTabError;

    // const developerTeamMembersList = this.state.members.developers
    //   ? this.state.members.developers?.map((member: ITeams, index: number) => {
    //       return (
    //         <TeamMemberListItem
    //           key={index}
    //           itemIndex={index}
    //           teamMember={member}
    //           showMoveUp={index !== 0}
    //           showMoveDown={index + 1 !== this.state.members.developers.length}
    //           onMoveUp={this.onTeamMemberMoveUp}
    //           onMoveDown={this.onTeamMemberMoveDown}
    //           onEdit={this.onSharingTeamMemberEdit}
    //           onDelete={this.onSharingTeamMemberDelete}
    //         />
    //       );
    //     })
    //   : [];
    // const productOwnerTeamMembersList = this.state.members.reportOwners
    //   ? this.state.members.reportOwners?.map((member: ITeams, index: number) => {
    //       return (
    //         <TeamMemberListItem
    //           key={index}
    //           itemIndex={index}
    //           teamMember={member}
    //           showMoveUp={index !== 0}
    //           showMoveDown={index + 1 !== this.state.members.reportOwners.length}
    //           onMoveUp={(index) => this.onTeamMemberMoveUp(index, 'productOwners')}
    //           onMoveDown={(index) => this.onTeamMemberMoveDown(index, 'productOwners')}
    //           onEdit={this.onControllerTeamMemberEdit}
    //           onDelete={this.onControllerTeamMemberDelete}
    //         />
    //       );
    //     })
    //   : [];

    const adminTeamMembersList = this.state.members.reportAdmins
      ? this.state.members.reportAdmins?.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.members.reportAdmins.length}
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
        {/* <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Please add report members</h3>
            <div className={classNames(Styles.teamListWrapper, productOwnerTeamMembersList.length < 1 ? 'error' : '')}>
              <div className={Styles.addTeamMemberWrapper}>
                <IconAvatarNew className={Styles.avatarIcon} />
                <button id="AddTeamMemberBtn" onClick={this.addProductOwnerMember}>
                  <i className="icon mbc-icon plus" />
                  <span>Add Report Member</span>
                </button>
                <div className={classNames(productOwnerTeamMembersList.length < 1 ? '' : 'hide')}>
                  <span className="error-message">{POError}</span>
                </div>
              </div>
              {productOwnerTeamMembersList.length ? productOwnerTeamMembersList : ''}
            </div>
          </div>
        </div> */}
        <div className={classNames(Styles.adminWrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Please add report administrators</h3>
            <div className={classNames(Styles.teamListWrapper, adminTeamMembersList.length < 1 ? 'error' : '')}>
              {/* <h5>
                {' '}
                Report Admin <sup>*</sup> (can edit/delete){' '}
              </h5> */}
              <div className={Styles.addTeamMemberWrapper}>
                <IconAvatarNew className={Styles.avatarIcon} />
                <button id="AddTeamMemberBtn" onClick={this.addAdminMember}>
                  <i className="icon mbc-icon plus" />
                  <span>Add Report Administrator</span>
                </button>
                {!this.state.members.reportAdmins.length && (
                  <div className={classNames(adminTeamMembersList.length < 1  ? '' : 'hide')}>
                    <span className="error-message">{adminError}</span>
                  </div>
                )}
              </div>
              {adminTeamMembersList.length ? adminTeamMembersList : ''}
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
      // members.developers = this.props.members.developers;
      members.reportOwners = this.props.members.reportOwners;
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
      members: { reportOwners, reportAdmins },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    let teamMemberObj: ITeams;
    if (addTeamMemberInController) {
      teamMemberObj = reportOwners[index];
    } else if (addAdminInController) {
      teamMemberObj = reportAdmins[index];
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
      members: { reportOwners, reportAdmins },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    let teamMembers = null;
    if (addTeamMemberInController) {
      teamMembers = reportOwners;
    } else if (addAdminInController) {
      teamMembers = reportAdmins;
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
        reportOwners: this.state.members.reportOwners,
        reportAdmins: this.state.members.reportAdmins,
      },
    };

    if (addTeamMemberInController) {
      stateUpdateObj.members.reportOwners = teamMembers;
    } else if (addAdminInController) {
      stateUpdateObj.members.reportAdmins = teamMembers;
    }

    this.setState(stateUpdateObj, () => {
      this.resetAddTeamMemberState();
      // this.props.modifyTeam(this.state.teamMembers);
    });
  };

  protected validateMembersList = (teamMemberObj: ITeams) => {
    console.log(teamMemberObj);
    const { reportAdmins, reportOwners } = this.state.members;
    const { addAdminInController, addTeamMemberInController } = this.state;
    let duplicateMember = false;
    if (addTeamMemberInController) {
      duplicateMember = reportOwners?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    } else if (addAdminInController) {
      duplicateMember = reportAdmins?.filter((admin) => admin.shortId === teamMemberObj.shortId)?.length ? true : false;
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
      members: { reportOwners, reportAdmins },
      addTeamMemberInController,
      addAdminInController,
    } = this.state;
    const teamMembers = addTeamMemberInController ? reportOwners : reportAdmins;
    teamMembers.splice(index, 1);
    if (addTeamMemberInController) {
      this.setState((prevState) => ({
        members: {
          ...prevState.members,
          reportOwners: teamMembers,
        },
      }));
    } else if (addAdminInController) {
      this.setState((prevState) => ({
        members: {
          ...prevState.members,
          reportAdmins: teamMembers,
        },
      }));
    }
  };
  protected onTeamMemberMoveUp = (index: number, teamMembers = 'developers') => {
    const members = this.state.members.reportOwners;
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
    const members = this.state.members.reportOwners;
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
        this.state.members.reportOwners,
        this.state.members.reportAdmins,
      );
      this.props.onSaveDraft('members');
    }
  };

  protected onMembersValueSubmit = () => {
    if (this.validateMembersTab()) {
      this.props.modifyMember(
        this.state.members.reportOwners,
        this.state.members.reportAdmins,
      );
      this.props.onPublish();
    }
  };

  protected validateMembersTab = () => {
    const { reportOwners: POList, reportAdmins: adminList } = this.state.members;
    let productOwner = '';
    let admin = '';
    if (!POList.length) productOwner = ErrorMsg.MEMBERS_TAB_PRODUCT_OWNER;
    if (!adminList.length) admin = ErrorMsg.MEMBERS_TAB_ADMIN;
    this.setState({
      membersTabError: {
        productOwner,
        admin,
      },
    });
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return POList.length && adminList.length;
  };
}
