import cn from 'classnames';
import * as React from 'react';
import { TEAMS_PROFILE_LINK_URL_PREFIX } from '../../../../globals/constants';
import { TeamMemberType } from '../../../../globals/Enums';
import { ITeams } from '../../../../globals/types';
import { IconAvatar } from '../../../icons/IconAvatar';
// import { Modal } from '../../../formElements/modal/Modal';
import Styles from './TeamMemberListItem.scss';
import { Envs } from '../../../../globals/Envs';

const classNames = cn.bind(Styles);

export interface ITeamMemberListItemProps {
  teamMember: ITeams;
  itemIndex: number;
  showMoveUp: boolean;
  showMoveDown: boolean;
  onMoveUp: (index: number) => void;
  onMoveDown: (index: number) => void;
  onEdit: (index: number) => void;
  onDelete: (index: number) => void;
  hidePosition?: boolean;
}

export interface ITeamMemberListItemState {
  showContextMenu: boolean;
}

export default class TeamMemberListItem extends React.Component<ITeamMemberListItemProps, ITeamMemberListItemState> {
  constructor(props: any) {
    super(props);
    this.state = {
      showContextMenu: false,
    };

    // this.toggleContextMenu = this.toggleContextMenu.bind(this);
    // this.onEdit = this.onEdit.bind(this);
    // this.onDelete = this.onDelete.bind(this);
    // this.onMoveUp = this.onMoveUp.bind(this);
    // this.onMoveDown = this.onMoveDown.bind(this);
  }

  public toggleContextMenu = () => {
    this.setState({
      showContextMenu: !this.state.showContextMenu,
    });
  };

  public onEdit = () => {
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onEdit(this.props.itemIndex);
      },
    );
  };

  public onDelete = () => {
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onDelete(this.props.itemIndex);
      },
    );
  };

  public onMoveUp = () => {
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onMoveUp(this.props.itemIndex);
      },
    );
  };

  public onMoveDown = () => {
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onMoveDown(this.props.itemIndex);
      },
    );
  };

  public render() {
    const teamMember = this.props.teamMember;

    return (
      <React.Fragment>
        {teamMember?.userType === TeamMemberType.INTERNAL ? (
          <div className={Styles.memberListWrapper}>
            <div className={Styles.avatar}>
              <IconAvatar className={Styles.avatarIcon} />
            </div>
            <div className={Styles.details}>
              {
                this.props?.hidePosition ? null : <h6>{teamMember?.teamMemberPosition}</h6>
              }
              <div className={Styles.memberDetails}>
                <div>
                  {teamMember?.firstName} {teamMember?.lastName} <br />
                  {teamMember?.department}
                </div>
                <div>
                  <a href={`mailto:${teamMember.email}`}>{teamMember.email}</a>
                </div>
                <div>{teamMember.mobileNumber}</div>
                {Envs.ENABLE_INTERNAL_USER_INFO ? (
                  <div>
                    <a href={TEAMS_PROFILE_LINK_URL_PREFIX + teamMember.shortId}>Teams Profile</a>
                  </div>
                ) : (
                  ''
                )}
              </div>
            </div>
            <span className={Styles.contextMenu} onClick={this.toggleContextMenu}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}>
              <ul>
                <li>
                  <span onClick={this.onEdit}>Edit team member</span>
                </li>
                <li>
                  <span onClick={this.onDelete}>Delete selected entry</span>
                </li>
              </ul>
            </div>
            <span onClick={this.onMoveUp} className={classNames(Styles.orderUp, this.props.showMoveUp ? '' : 'hide')}>
              <i className="icon mbc-icon arrow small up" />
            </span>
            <span
              onClick={this.onMoveDown}
              className={classNames(Styles.orderDown, this.props.showMoveDown ? '' : 'hide')}
            >
              <i className="icon mbc-icon arrow small down" />
            </span>
          </div>
        ) : (
          <div className={Styles.memberListWrapper}>
            <div className={Styles.avatar}>
              <IconAvatar className={Styles.avatarIcon} fill="#697582" />
            </div>
            <div className={Styles.details}>
              <h6>
                {teamMember?.teamMemberPosition} ({teamMember?.userType})
              </h6>
              <div className={Styles.memberDetails}>
                <div>{teamMember?.company}</div>
                <div>{''}</div>
                <div>{''}</div>
                {/* <div>
                  <a href={`http://${teamMember.company}`}>{teamMember.company}.com</a>
                </div> */}
              </div>
            </div>
            <span className={Styles.contextMenu} onClick={this.toggleContextMenu}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}>
              <ul>
                <li>
                  <span onClick={this.onEdit}>Edit team member</span>
                </li>
                <li>
                  <span onClick={this.onDelete}>Delete selected entry</span>
                </li>
              </ul>
            </div>
            <span onClick={this.onMoveUp} className={classNames(Styles.orderUp, this.props.showMoveUp ? '' : 'hide')}>
              <i className="icon mbc-icon arrow small up" />
            </span>
            <span
              onClick={this.onMoveDown}
              className={classNames(Styles.orderDown, this.props.showMoveDown ? '' : 'hide')}
            >
              <i className="icon mbc-icon arrow small down" />
            </span>
          </div>
        )}
      </React.Fragment>
    );
  }
}
