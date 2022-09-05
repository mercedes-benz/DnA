// import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import { IUserDetails } from '../../../../../globals/types';
// @ts-ignore
import InputFieldsUtils from '../../../formElements/InputFields/InputFieldsUtils';

import TeamSearch from '../../../../mbc/teamSearch/TeamSearch';
import { Envs } from '../../../../../globals/Envs';

export interface IAddUserProps {
  getCollabarators: (teamMemberObj: IUserDetails, dagId: string) => void;
  dagId: string;
}

export interface IAddUserState {
  searchTerm: string;
  showUserDetails: boolean;
}
export default class AddUser extends React.Component<IAddUserProps, IAddUserState> {
  constructor(props: any) {
    super(props);
    this.state = {
      searchTerm: '',
      showUserDetails: false,
    };
  }

  public componentDidMount() {
    Button.defaultSetup();
  }

  public render() {
    return (
      <TeamSearch
        label={
          <>
            Find Collaborator<sup>*</sup>{' '}
            <span dangerouslySetInnerHTML={{ __html: Envs.INTERNAL_USER_TEAMS_INFO }}></span>
          </>
        }
        onAddTeamMember={this.addMemberFromTeamSearch}
        btnText="Add User"
        searchTerm={this.state.searchTerm}
        setSearchTerm={(value) => this.setState({ searchTerm: value })}
        showUserDetails={this.state.showUserDetails}
        setShowUserDetails={(value) => this.setState({ showUserDetails: value })}
      />
    );
  }
  protected addMemberFromTeamSearch = (teamMember: any) => {
    const teamMemberObj: IUserDetails = {
      department: teamMember.department,
      email: teamMember.email,
      firstName: teamMember.firstName,
      shortId: teamMember.shortId,
      lastName: teamMember.lastName,
      mobileNumber: teamMember.mobileNumber,
    };
    this.props.getCollabarators(teamMemberObj, this.props.dagId);
  };
}
