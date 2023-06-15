import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './MembersSummary.scss';
import { IMembers, ITeams } from 'globals/types';
import TeamMemberListItem from 'components/mbc/summary/team/teamMemberListItem/TeamMemberListItem';

const classNames = cn.bind(Styles);

interface IMembersSummaryProps {
  members: IMembers;
}

export default class MembersSummary extends React.Component<IMembersSummaryProps> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
  }

  public render() {
    // const productOwner = this.props.members.reportOwners?.map((member: ITeams, index: number) => {
    //   return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    // });

    // const developerList = this.props.members.developers?.map((member: ITeams, index: number) => {
    //   return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    // });

    const adminList = this.props.members.reportAdmins?.map((member: ITeams, index: number) => {
      return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} />;
    });

    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div className={Styles.wrapper}>
            <div className={Styles.firstPanel}>
              <h3 className={Styles.description}>Members</h3>
              {/* {this.props.members.reportOwners?.length ? (
                <div className={Styles.membersListView}>
                  <label className={'input-label summary ' + Styles.cardSubTitle}>Report Member(s)</label>
                  <div>{productOwner}</div>
                </div>
              ) : null} */}
              {/* {this.props.members.developers?.length ? (
                <div className={Styles.membersListView}>
                  <label className={'input-label summary ' + Styles.cardSubTitle}>Developer(s)</label>
                  <div>{developerList}</div>
                </div>
              ) : null} */}
              <div className={Styles.membersListView}>
                <label className={'input-label summary ' + Styles.cardSubTitle}>Report Administrator(s)</label>
                <div>{adminList}</div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
