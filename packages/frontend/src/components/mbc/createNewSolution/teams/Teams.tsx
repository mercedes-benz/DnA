import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { IconAvatarNew } from '../../../../components/icons/IconAvatarNew';
import { ITeams } from '../../../../globals/types';
import AddTeamMemberModal from './addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from './teamMemberListItem/TeamMemberListItem';
import Styles from './Teams.scss';

const classNames = cn.bind(Styles);

export interface ITeamProps {
  team: ITeams[];
  modifyTeam: (team: ITeams[]) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
}

export interface ITeamsState {
  showAddTeamMemberModal: boolean;
  editTeamMember: boolean;
  editTeamMemberIndex: number;
  teamMembers: ITeams[];
  teamMemberObj: ITeams;
  teamMemberError: string;
}

export default class Teams extends React.Component<ITeamProps, ITeamsState> {
  public static getDerivedStateFromProps(props: ITeamProps, state: ITeamsState) {
    if (props && props.team && props.team.length !== 0) {
      return {
        teamMembers: props.team,
      };
    }
    // Need to reset teamMembers based on the parent prop.
    // Need to confirm the validitiy of the above condition
    if (props && props.team && props.team.length === 0) {
      return {
        teamMembers: props.team,
      };
    }
    return null;
  }

  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();

  constructor(props: any) {
    super(props);
    this.state = {
      showAddTeamMemberModal: false,
      editTeamMember: false,
      editTeamMemberIndex: -1,
      teamMembers: this.props.team,
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
      teamMemberError: null,
    };
  }

  public render() {
    const teamMembersList = this.state.teamMembers.map((member: ITeams, index: number) => {
      return (
        <TeamMemberListItem
          key={index}
          itemIndex={index}
          teamMember={member}
          showMoveUp={index !== 0}
          showMoveDown={index + 1 !== this.state.teamMembers.length}
          onMoveUp={this.onTeamMemberMoveUp}
          onMoveDown={this.onTeamMemberMoveDown}
          onEdit={this.onTeamMemberEdit}
          onDelete={this.onTeamMemberDelete}
        />
      );
    });

    const teamMemberError = this.state.teamMemberError || '';

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Solution-team and sponsors</h3>
            <div className={Styles.teamListWrapper}>
              <div className={Styles.addTeamMemberWrapper}>
                <IconAvatarNew className={Styles.avatarIcon} />
                <button id="AddTeamMemberBtn" onClick={this.showAddTeamMemberModalView}>
                  <i className="icon mbc-icon plus" />
                  <span>Add team member</span>
                </button>
                <div className={classNames(Styles.teamsErrorMessage, teamMemberError.length ? '' : 'hide')}>
                  <span className="error-message">{teamMemberError}</span>
                </div>
              </div>
              {teamMembersList}
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onTeamsSubmit}>
            Save & Next
          </button>
        </div>
        {this.state.showAddTeamMemberModal && (
          <AddTeamMemberModal
            ref={this.addTeamMemberModalRef}
            editMode={this.state.editTeamMember}
            showAddTeamMemberModal={this.state.showAddTeamMemberModal}
            teamMember={this.state.teamMemberObj}
            onUpdateTeamMemberList={this.updateTeamMemberList}
            onAddTeamMemberModalCancel={this.onAddTeamMemberModalCancel}
          />
        )}
      </React.Fragment>
    );
  }

  protected onTeamMemberEdit = (index: number) => {
    const teamMemberObj = this.state.teamMembers[index];
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

  protected onTeamMemberDelete = (index: number) => {
    const teamMembers = this.state.teamMembers;
    teamMembers.splice(index, 1);
    this.setState({ teamMembers });
    this.props.modifyTeam(this.state.teamMembers);
  };

  protected onTeamMemberMoveUp = (index: number) => {
    const teamMembers = this.state.teamMembers;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index - 1, 0, teamMember);
    this.setState({ teamMembers });
    this.props.modifyTeam(this.state.teamMembers);
  };

  protected onTeamMemberMoveDown = (index: number) => {
    const teamMembers = this.state.teamMembers;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index + 1, 0, teamMember);
    this.setState({ teamMembers });
    this.props.modifyTeam(this.state.teamMembers);
  };

  protected resetTeamsState() {
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

  protected showAddTeamMemberModalView = () => {
    this.resetTeamsState();
    this.setState({ showAddTeamMemberModal: true }, () => {
      this.addTeamMemberModalRef.current.setTeamMemberData(this.state.teamMemberObj, false);
    });
  };

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetTeamsState();
    });
  };

  protected updateTeamMemberList = (teamMemberObj: ITeams) => {
    const { editTeamMember, editTeamMemberIndex, teamMembers } = this.state;
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    this.setState(
      {
        teamMembers,
        showAddTeamMemberModal: false,
      },
      () => {
        this.setState({ teamMemberError: null });
        this.resetTeamsState();
        this.props.modifyTeam(this.state.teamMembers);
      },
    );
  };

  protected onTeamsSubmit = () => {
    if (this.validateTeamsTab()) {
      this.props.modifyTeam(this.state.teamMembers);
      this.props.onSaveDraft('teams');
    }
  };

  protected validateTeamsTab = () => {
    let allowSave = true;
    let teamMemberError = null;
    if (this.state.teamMembers.length === 0) {
      teamMemberError = '*Please add minimum one team member.';
      allowSave = false;
    }

    this.setState({ teamMemberError });

    return allowSave;
  };
}
