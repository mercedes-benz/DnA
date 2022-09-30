import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import ImgNotCompletedGuideline from '../../../../assets/images/Not-Completed-Guidelines.png';
// @ts-ignore
import ImgQuickCheckReady from '../../../../assets/images/Quick-Check-Ready.png';
// @ts-ignore
import ImgQuickCheck from '../../../../assets/images/Quick-Check.png';
// @ts-ignore
import ImgUseCaseCheckReady from '../../../../assets/images/UseCsae-Check-Ready.png';
// @ts-ignore
import ImgUseCaseCheck from '../../../../assets/images/UseCsae-Check.png';
import { IDataCompliance, ITeams } from 'globals/types';
import TeamMemberListItem from '../team/teamMemberListItem/TeamMemberListItem';
import AttachmentsListItem from './attachments/AttachmentsListItems';
import Styles from './DataComplianceSummary.scss';
import LinksListItems from './links/LinksListItems';

const classNames = cn.bind(Styles);

export interface ITeamProps {
  dataCompliance: IDataCompliance;
}

export default class DataComplianceSummary extends React.Component<ITeamProps, any> {
  constructor(props: ITeamProps) {
    super(props);
  }

  public render() {
    const teamMembersList = this.props.dataCompliance.complianceOfficers.length
      ? this.props.dataCompliance.complianceOfficers.map((member: ITeams, index: number) => {
          return <TeamMemberListItem key={index} itemIndex={index} teamMember={member} useFullWidth={true} />;
        })
      : 'NA';

    const attachmentsList = <AttachmentsListItem attachments={this.props.dataCompliance.attachments} />;
    const linksList = <LinksListItems links={this.props.dataCompliance.links} />;

    const image =
      this.props.dataCompliance.quickCheck &&
      !this.props.dataCompliance.expertGuidelineNeeded &&
      !this.props.dataCompliance.useCaseDescAndEval &&
      !this.props.dataCompliance.readyForImplementation ? (
        <img width="100%" src={ImgQuickCheck} />
      ) : this.props.dataCompliance.quickCheck &&
        this.props.dataCompliance.expertGuidelineNeeded &&
        !this.props.dataCompliance.useCaseDescAndEval &&
        !this.props.dataCompliance.readyForImplementation ? (
        <img width="100%" src={ImgQuickCheckReady} />
      ) : this.props.dataCompliance.quickCheck &&
        !this.props.dataCompliance.expertGuidelineNeeded &&
        this.props.dataCompliance.useCaseDescAndEval &&
        !this.props.dataCompliance.readyForImplementation ? (
        <img width="100%" src={ImgUseCaseCheck} />
      ) : this.props.dataCompliance.quickCheck &&
        !this.props.dataCompliance.expertGuidelineNeeded &&
        this.props.dataCompliance.useCaseDescAndEval &&
        this.props.dataCompliance.readyForImplementation ? (
        <img width="100%" src={ImgUseCaseCheckReady} />
      ) : (
        <img width="100%" src={ImgNotCompletedGuideline} />
      );

    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div id="dataComplianceWrapper" className={Styles.wrapper}>
            <h3>Compliance Framework / Process Flow</h3>
            <div className={Styles.imageSection}>{image}</div>
            <div id="complianceLinks">
              <h3>Added Links</h3>
              {linksList}
              <br />
              <br />
            </div>
            <div id="complianceAttachmets" className={Styles.attachmentSection}>
              <h3>Attached Files</h3>
              {attachmentsList}
            </div>
            <div id="complianceOfficers" className={Styles.officerSection}>
              <h3>Local Compliance Officers</h3>
              {teamMembersList}
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }
}
