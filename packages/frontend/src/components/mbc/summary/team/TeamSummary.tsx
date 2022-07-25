import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import { ITeams } from '../../../../globals/types';
import TeamMemberListItem from './teamMemberListItem/TeamMemberListItem';
import Styles from './TeamSummary.scss';
import {IntlProvider, FormattedNumber} from 'react-intl'

const classNames = cn.bind(Styles);

export interface ITeamProps {
  team: ITeamRequest;
  neededRoles: INeededRoleObject[];
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

export default class TeamSummary extends React.Component<ITeamProps, any> {
  constructor(props: ITeamProps) {
    super(props);
  }

  public render() {
    const teamMembersList = this.props.team.team.map((member: ITeams, index: number) => {
      return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    });
    return (
      <React.Fragment>
        <div className={classNames(Styles.flexLayout, Styles.mainPanel, 'mainPanelSection')}>
          <div id="teamMembersWrapper" className={Styles.wrapper}>
            <div>
              <h3>Members</h3>
              <div>{teamMembersList}</div>
            </div>
            <div id="neededRoles">
              <h3>Needed Roles/Skills</h3>
              <br />
              <div className={Styles.rolesList}>
                {this.props.neededRoles
                  ? this.props.neededRoles.length > 0
                    ? this.props.neededRoles.map((item, index) => {
                        return (
                          <div key={item.neededSkill + index} id={item.neededSkill + index}>
                            {item.neededSkill}:{' '}
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              {item.requestedFTECount ? <FormattedNumber value={Number(item.requestedFTECount)} /> : 'N/A'}
                            </IntlProvider>
                          </div>
                        );
                      })
                    : 'N/A'
                  : 'N/A'}
              </div>
            </div>
          </div>
        </div>{' '}
      </React.Fragment>
    );
  }
}
