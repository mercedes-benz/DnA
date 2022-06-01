import * as React from 'react';
// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { IUserDetails } from '../../../globals/types';
// @ts-ignore
import InputFieldsUtils from '../../formElements/InputFields/InputFieldsUtils';
import TeamSearch from '../teamSearch/TeamSearch';

export interface IAddUserProps {
  /** function used to set collaborators information */
  getCollabarators: (teamMemberObj: IUserDetails, dagId: string) => void;
  /** string to be passed as a parameter to getCollaborator function */
  dagId: string;
  /** displays (*) next to label */
  isRequired: boolean;
}

/**
 * Component to be used to add Users/Collaborators
 * @visibleName Add User
 */
export default class AddUser extends React.Component<IAddUserProps> {
  constructor(props: any) {
    super(props);
  }

  public componentDidMount() {
    Button.defaultSetup();
  }

  public render() {
    return (
      <TeamSearch
        label={<>Find Collaborator {this.props.isRequired ? <sup>*</sup> : null}</>}
        onAddTeamMember={this.addMemberFromTeamSearch}
        btnText="Add User"
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
  static defaultProps = { isRequired: true };
}
