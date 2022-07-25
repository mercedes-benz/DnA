import cn from 'classnames';
import * as React from 'react';
import InputFieldsUtils from '../../../formElements/InputFields/InputFieldsUtils';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { IconAvatarNew } from '../../../../components/icons/IconAvatarNew';
import AddTeamMemberModal from '../../addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from '../../addTeamMember/teamMemberListItem/TeamMemberListItem';
import { IRelatedProduct, ITeams } from '../../../../globals/types';
import AddRelatedProductModal from '../description/addRelatedProductModal/AddRelatedProductModal';
import Styles from './Teams.scss';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import { ApiClient } from '../../../../services/ApiClient';
import InfoModal from '../../../formElements/modal/infoModal/InfoModal';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';
import { InputFields } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import { InfoList } from '../../../formElements/modal/infoModal/InfoList';
import { RolesInfoList } from '../../../../globals/constants';
import NumberFormat from 'react-number-format';

const classNames = cn.bind(Styles);

export interface ITeamProps {
  team: ITeamRequest;
  neededRoles: INeededRoleObject[];
  modifyTeam: (team: ITeamRequest, neededRoles: INeededRoleObject[]) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
}

export interface ITeamsState {
  showAddTeamMemberModal: boolean;
  editTeamMember: boolean;
  editTeamMemberIndex: number;
  teamMembers: ITeams[];
  teamMemberObj: ITeams;
  teamMemberError: string;
  neededRoleMaster: IRelatedProduct[];
  neededRoleValue: string[];
  showNeededRoleInfo: boolean;
  showAddNeededRoleModal: boolean;
  relatedProductObj: IRelatedProduct[];
  chips: string[];
  roleCountFieldList: INeededRoleObject[];
  showDeleteModal: boolean;
  roleToDelete: INeededRoleObject;
}

export interface ITeamRequest {
  team?: ITeams[];
}

export interface INeededRoleObject {
  fromDate: string;
  neededSkill: string;
  requestedFTECount: string;
  toDate: string;
}

export default class Teams extends React.Component<ITeamProps, ITeamsState> {
  public static getDerivedStateFromProps(props: ITeamProps, state: ITeamsState) {
    if (props && props.team && props.team.team.length !== 0) {
      return {
        teamMembers: props.team.team,
      };
    }
    // Need to reset teamMembers based on the parent prop.
    // Need to confirm the validitiy of the above condition
    if (props && props.team && props.team.team.length === 0) {
      return {
        teamMembers: props.team.team,
      };
    }
    // if (props.neededRoles !== state.roleCountFieldList) {
    //   return { neededRoleValue: props.neededRoles.map((item) => item.neededSkill) };
    // }
    return null;
  }

  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();
  private addNeededRoleModalRef = React.createRef<AddRelatedProductModal>();

  constructor(props: any) {
    super(props);
    this.state = {
      showAddTeamMemberModal: false,
      editTeamMember: false,
      editTeamMemberIndex: -1,
      teamMembers: this.props.team.team,
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
      neededRoleMaster: [],
      neededRoleValue: [],
      showNeededRoleInfo: false,
      showAddNeededRoleModal: false,
      relatedProductObj: [],
      chips: [],
      roleCountFieldList: this.props.neededRoles,
      showDeleteModal: false,
      roleToDelete: {
        fromDate: '',
        neededSkill: '',
        requestedFTECount: '',
        toDate: '',
      },
    };
  }

  componentDidMount() {
    ApiClient.getSkills().then((response) => {
      if (response) {
        this.setState(
          {
            neededRoleMaster: response,
            roleCountFieldList: this.props.neededRoles,
            neededRoleValue: this.props.neededRoles ? this.props.neededRoles.map((item) => item.neededSkill) : [],
          },
          () => {
            SelectBox.defaultSetup();
            InputFields.defaultSetup();
          },
        );
      }
    });
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

    const neededRoleValues = this.state.neededRoleValue
      ? this.state.neededRoleValue.map((neededRole: string) => {
          return neededRole;
        })
      : [];

    const { roleCountFieldList } = this.state;

    const contentForNeededRoleInfoModal = <InfoList list={RolesInfoList} />;

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

        <div className={classNames(Styles.wrapperNeededRole)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Needed Roles/Skills</h3>
            <i className={classNames('icon mbc-icon info', Styles.roleInfo)} onClick={this.showNeededRoleInfoModal} />
            <div id="roleWrapper" className={Styles.roleWrapperContent}>
              <div id="roleContainer" className={classNames('input-field-group')}>
                <label id="roleLabel" className={classNames('input-label', Styles.roleLabel)} htmlFor="roleSelect">
                  Select Roles/Skills{' '}
                </label>
                <div id="role" className="custom-select">
                  <select
                    id="roleSelect"
                    multiple={true}
                    required={false}
                    onChange={this.onNeededRoleChange}
                    value={neededRoleValues}
                  >
                    {this.state.neededRoleMaster
                      ? this.state.neededRoleMaster.map((obj) => (
                          <option id={obj.name} key={obj.name} value={obj.name}>
                            {obj.name}
                          </option>
                        ))
                      : ''}
                  </select>
                </div>
                <div>
                  <button className={classNames(Styles.roleAddButton)} onClick={this.showAddNeededRoleModalView}>
                    <i className="icon mbc-icon plus" />
                    &nbsp;
                    <span>Add new needed roles/skills</span>
                  </button>
                </div>

                <div className={Styles.roleCountFieldList}>
                  {roleCountFieldList ? (
                    roleCountFieldList.length > 0 ? (
                      <div>
                        <table className="ul-table users">
                          <thead>
                            <tr className="header-row">
                              <th>
                                <label>Roles/Skills</label>
                              </th>

                              <th>
                                <label>Number of FTE</label>
                              </th>

                              <th className="text-center">
                                <label>Action</label>
                              </th>
                            </tr>
                          </thead>
                          <tbody>
                            {roleCountFieldList &&
                              roleCountFieldList.map((item: INeededRoleObject, index: number) => {
                                return (
                                  <tr className="data-row" key={item.neededSkill + '-' + index}>
                                    <td>{item.neededSkill}</td>
                                    <td>
                                      <div>
                                        {/* @ts-ignore */}
                                        <NumberFormat
                                            className={classNames('input-field', Styles.fteField)}
                                            id={'numberOfRequestedFTE-' + index}
                                            name="requestedFTECount"
                                            value={                                              
                                              new Intl.NumberFormat(navigator.language).format(Number(item.requestedFTECount))
                                            }
                                            thousandSeparator={false}
                                            decimalScale={2}
                                            decimalSeparator={navigator.language == 'de-DE' || navigator.language == 'de' || navigator.language == 'DE' ? "," : "."}
                                            onValueChange={(values, sourceInfo) => this.handleChange(values,sourceInfo)}
                                        />
                                      </div>
                                    </td>
                                    <td className="text-center">
                                      <i
                                        onClick={this.openDeleteModal(item)}
                                        className={classNames(Styles.deleteIcon, 'icon delete')}
                                      />
                                    </td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      ''
                    )
                  ) : (
                    ''
                  )}
                </div>
              </div>
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
            validateMemebersList={this.validateMembersList}
          />
        )}
        {this.state.showAddNeededRoleModal && (
          <AddRelatedProductModal
            modalTitleText={'Add needed roles/skills'}
            fieldTitleText={'Needed Roles/Skills'}
            ref={this.addNeededRoleModalRef}
            showAddRelatedProductModal={this.state.showAddNeededRoleModal}
            relatedProduct={this.state.relatedProductObj}
            onAddRelatedProductModalCancel={this.onAddNeededRoleModalCancel}
            max={100}
            chips={this.state.chips}
            onRelatedProductChangeUpdate={this.onNeededRoleChangeUpdate}
          />
        )}
        {this.state.showNeededRoleInfo && (
          <InfoModal
            title={'Needed Role/Skills Information'}
            modalWidth={'35vw'}
            show={this.state.showNeededRoleInfo}
            content={contentForNeededRoleInfoModal}
            onCancel={this.onNeededRoleInfoModalCancel}
          />
        )}
        {this.state.showDeleteModal && (
          <ConfirmModal
            title={''}
            showAcceptButton={true}
            showCancelButton={true}
            acceptButtonTitle={'Confirm'}
            cancelButtonTitle={'Cancel'}
            show={this.state.showDeleteModal}
            removalConfirmation={true}
            content={
              <div style={{ margin: '35px 0', textAlign: 'center' }}>
                <div>Remove Role and FTE Value</div>
                <div className={classNames(Styles.removeConfirmationContent, 'hide')}>
                  Are you sure to remove the Role and FTE Value?
                </div>
              </div>
            }
            onCancel={this.onInfoModalCancel}
            onAccept={this.onAccept}
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
    const tempTeamsObj = { team: teamMembers };
    this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
  };

  protected onTeamMemberMoveUp = (index: number) => {
    const teamMembers = this.state.teamMembers;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index - 1, 0, teamMember);
    this.setState({ teamMembers });
    const tempTeamsObj = { team: teamMembers };
    this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
  };

  protected onTeamMemberMoveDown = (index: number) => {
    const teamMembers = this.state.teamMembers;
    const teamMember = teamMembers.splice(index, 1)[0];
    teamMembers.splice(index + 1, 0, teamMember);
    this.setState({ teamMembers });
    const tempTeamsObj = { team: teamMembers };
    this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
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
        const tempTeamsObj = { team: this.state.teamMembers };
        this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
      },
    );
  };

  protected validateMembersList = (teamMemberObj: ITeams) => {
    let duplicateMember = false;
    duplicateMember = this.state.teamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    return duplicateMember;
  };

  protected onTeamsSubmit = () => {
    if (this.validateTeamsTab()) {
      const tempTeamsObj = { team: this.state.teamMembers };
      this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
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

  protected onNeededRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.label);
      });
    }

    this.changeInRoleCount(selectedValues);
  };

  protected showNeededRoleInfoModal = () => {
    this.setState({ showNeededRoleInfo: true });
  };

  protected onNeededRoleInfoModalCancel = () => {
    this.setState({ showNeededRoleInfo: false });
  };

  protected showAddNeededRoleModalView = () => {
    this.setState({ showAddNeededRoleModal: true });
  };

  protected onAddNeededRoleModalCancel = () => {
    this.setState({ showAddNeededRoleModal: false }, () => {
      // this.resetTeamsState();
    });
  };

  protected onNeededRoleChangeUpdate = (data: string[]) => {
    const neededRoles: IRelatedProduct[] = data
      .filter(
        (o1) => !this.state.neededRoleMaster.some((o2: { name: string }) => o1.toLowerCase() === o2.name.toLowerCase()),
      )
      .map((str) => {
        return {
          id: str,
          name: str,
        };
      });
    this.state.neededRoleMaster.push(...neededRoles);
    const selectedNeededRoles: string[] = [];
    data.forEach((o1) => {
      this.state.neededRoleMaster.forEach((o2) => {
        if (o1.toLowerCase() === o2.name.toLowerCase()) {
          selectedNeededRoles.push(o2.name);
        }
      });
    });

    const selectedValues: string[] = selectedNeededRoles;
    const { neededRoleValue } = this.state;
    neededRoleValue.push(...selectedValues);
    this.setState({ neededRoleValue });
    InputFieldsUtils.resetErrors('#roleWrapper'); // reseting the parent filed //

    this.setState(
      {
        neededRoleMaster: this.state.neededRoleMaster,
      },
      () => {
        SelectBox.defaultSetup(false);
        this.changeInRoleCount(neededRoleValue);
      },
    );
  };

  protected changeInRoleCount(selectedValues: string[]) {
    const { roleCountFieldList } = this.state;
    const tempRoleCountFieldList: INeededRoleObject[] = [];
    if (roleCountFieldList) {
      roleCountFieldList.forEach((item, index) => {
        if (!selectedValues.includes(item.neededSkill)) {
          roleCountFieldList.splice(index, 1);
        }
      });
    }

    const tempArr = roleCountFieldList ? roleCountFieldList.map((item) => item.neededSkill) : [];
    const missing = selectedValues.filter((item) => tempArr.indexOf(item) < 0);

    this.state.neededRoleMaster.forEach((item: IRelatedProduct) => {
      if (selectedValues.includes(item.name) && missing.length !== 0) {
        const tempObj = {
          fromDate: '',
          neededSkill: item.name,
          requestedFTECount: '0',
          toDate: '',
        };
        tempRoleCountFieldList.push(tempObj);
      }
    });

    const a = roleCountFieldList ? roleCountFieldList : [];
    const b = tempRoleCountFieldList ? tempRoleCountFieldList : [];

    if (a.length > 0 && b.length > 0) {
      // A comparer used to determine if two entries are equal.
      const isSameUser = (a: INeededRoleObject, b: INeededRoleObject) => a.neededSkill == b.neededSkill;

      // Get items that only occur in the left array,
      // using the compareFunction to determine equality.
      const onlyInLeft = (left: any, right: any, compareFunction: any) =>
        left.filter((leftValue: any) => !right.some((rightValue: any) => compareFunction(leftValue, rightValue)));

      const onlyInA = onlyInLeft(a, b, isSameUser);
      const onlyInB = onlyInLeft(b, a, isSameUser);

      const result = [...onlyInA, ...onlyInB];
      roleCountFieldList.push(...result);
      this.setState({ roleCountFieldList, neededRoleValue: selectedValues },()=>{
        const tempTeamsObj = {team: this.state.teamMembers}
        this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
      });
    } else {
      /************** Setting default value when loads while edit ***************/
      this.setState(
        {
          roleCountFieldList: tempRoleCountFieldList.length > 0 ? tempRoleCountFieldList : roleCountFieldList,
          neededRoleValue: selectedValues,
        },
        () => {
          InputFields.defaultSetup();
          const tempTeamsObj = {team: this.state.teamMembers}
          if(tempRoleCountFieldList.length > 0)
          this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
        },
      );
    }
  }

  // protected onFTEChange(e: React.FormEvent<HTMLInputElement>) {
  //   const name: string = e.currentTarget.name;
  //   const regex = /^[0-9]\d*(((,\d{3}){1})?(\.\d{0,2})?)$/;
  //   const valRecieved: string = e.currentTarget.value;
  //   const tmepVal = valRecieved.toString().replace(',', '.');
  //   if (regex.test(tmepVal) || tmepVal === '') {
  //     const value: any = tmepVal !== null && tmepVal !== '' ? tmepVal : 0;
  //     const index = Number(e.currentTarget.id.split('-')[1]);
  //     const { roleCountFieldList } = this.state;
  //     roleCountFieldList.forEach((item, itemIndex) => {
  //       if (index === itemIndex) {
  //         item[name] = value;
  //       }
  //       return item;
  //     });
  //     this.setState({ roleCountFieldList },()=>{
  //       const tempTeamsObj = {team: this.state.teamMembers}
  //       this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
  //     });
  //   }
  // }

  protected handleChange = (values: any, sourceInfo: any) => {
    const { value } = values;
    const name: string = sourceInfo?.event?.target?.name;
    const tempVal: any = value !== null && value !== '' ? value : 0;
    const index = Number(sourceInfo?.event?.target?.id.split('-')[1]);
    const { roleCountFieldList } = this.state;
    roleCountFieldList.forEach((item, itemIndex) => {
      if (index === itemIndex) {
        item[name] = tempVal;
      }
      return item;
    });
    this.setState({ roleCountFieldList },()=>{
      const tempTeamsObj = {team: this.state.teamMembers}
      this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
    });
  };

  protected changeDotWithComma = (text: any) => {
    return text.replace('.', ',');
  };

  protected openDeleteModal = (role: INeededRoleObject) => {
    return () => {
      this.setState({ showDeleteModal: true, roleToDelete: role });
    };
  };

  protected onAccept = () => {
    this.deleteRole(this.state.roleToDelete);
    this.setState({ showDeleteModal: false });
  };

  protected onInfoModalCancel = () => {
    this.setState({ showDeleteModal: false });
  };

  protected deleteRole = (selectedRole: INeededRoleObject) => {
    const { neededRoleValue } = this.state;
    const tempArr = neededRoleValue.filter((item, index) => {
      if (selectedRole.neededSkill != item) {
        return item;
      }
    });
    this.setState({ neededRoleValue: tempArr }, () => {
      SelectBox.defaultSetup(false);
      this.changeInRoleCount(tempArr);
    });
  };
}
