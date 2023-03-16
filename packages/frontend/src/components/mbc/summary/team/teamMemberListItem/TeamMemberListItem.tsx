import cn from 'classnames';
import * as React from 'react';
import { TEAMS_PROFILE_LINK_URL_PREFIX } from 'globals/constants';
import { TeamMemberType } from 'globals/Enums';
import { ITeams } from 'globals/types';
import { IconAvatar } from 'components/icons/IconAvatar';
// import { Modal } from '../../../formElements/modal/Modal';
import Styles from './TeamMemberListItem.scss';

const classNames = cn.bind(Styles);

export interface ITeamMemberListItemProps {
  teamMember: ITeams | any;
  itemIndex: number;
  useFullWidth?: boolean;
}

export default class TeamMemberListItem extends React.Component<ITeamMemberListItemProps, any> {
  constructor(props: any) {
    super(props);
  }

  public render() {
    const teamMember = this.props.teamMember;

    return (
      <React.Fragment>
        {teamMember?.userType === TeamMemberType.INTERNAL ? (
          <div className={classNames(Styles.memberListWrapper, this.props.useFullWidth ? Styles.useFullWidth : null)}>
            <div className={Styles.avatar}>
              <IconAvatar className={Styles.avatarIcon} />
            </div>
            <div className={Styles.details}>
              <h6>{teamMember?.teamMemberPosition}</h6>
              <div className={Styles.memberDetails}>
                <div>
                  <a href={TEAMS_PROFILE_LINK_URL_PREFIX + teamMember?.shortId}>
                    {teamMember?.firstName} {teamMember?.lastName}
                  </a>{' '}
                  <br />
                  {teamMember?.department}
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className={Styles.memberListWrapper}>
            <div className={Styles.avatar}>
              <IconAvatar className={Styles.avatarIcon} fill="#697582" />
            </div>
            <div className={Styles.details}>
              <h6>
                {teamMember?.teamMemberPosition} {teamMember?.userType ? `(${teamMember?.userType})` : null}
              </h6>
              <div className={Styles.memberDetails}>
                {teamMember?.company ? (
                  <div>{teamMember?.company}</div>
                ) : (
                  <div>
                    {teamMember?.level && <>{teamMember?.level} | {teamMember?.division && teamMember?.division.name} | {teamMember?.department && teamMember?.department}</>}
                    {teamMember?.shortId ? (
                      <a href={TEAMS_PROFILE_LINK_URL_PREFIX + teamMember?.shortId}>
                        {teamMember?.firstName} {teamMember?.lastName}
                      </a>
                    ) : (
                      <>
                        {teamMember?.firstName} {teamMember?.lastName} {teamMember?.lastName && <br />}
                        {teamMember?.email}
                      </>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </React.Fragment>
    );
  }
}
