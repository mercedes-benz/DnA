import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import { ITeams } from '../../../../globals/types';
import TeamMemberListItem from './teamMemberListItem/TeamMemberListItem';
import Styles from './TeamSummary.scss';

const classNames = cn.bind(Styles);

export interface ITeamProps {
  team: ITeams[];
}

export default class TeamSummary extends React.Component<ITeamProps, any> {
  constructor(props: ITeamProps) {
    super(props);
  }

  public render() {
    const teamMembersList = this.props.team.map((member: ITeams, index: number) => {
      return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    });
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div id="teamMembersWrapper" className={Styles.wrapper}>
            <h3>Team</h3>
            <div>{teamMembersList}</div>
          </div>
        </div>{' '}
      </React.Fragment>
    );
  }
}
