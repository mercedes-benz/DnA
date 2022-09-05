import cn from 'classnames';
import * as React from 'react';
import { DATA_COMPLIANCE_INFO_LINKS } from '../../../../globals/constants';
import ConfirmModal from '../../../../components/formElements/modal/confirmModal/ConfirmModal';
import InfoModal from '../../../../components/formElements/modal/infoModal/InfoModal';
import IconAvatarNew from '../../../../components/icons/IconAvatarNew';
import { IAttachment, IDataCompliance, ILink, ITeams } from '../../../../globals/types';
import AttachmentUploader from '../AttachmentUploader/AttachmentUploader';
import ExternalLink from '../externalLink/ExternalLink';
import AddTeamMemberModal from '../../addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from '../../addTeamMember/teamMemberListItem/TeamMemberListItem';
import Styles from './DataCompliance.scss';

const classNames = cn.bind(Styles);

export interface IDataComplianceProps {
  datacompliance: IDataCompliance;
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyDataCompliance: (datacompliance: IDataCompliance) => void;
}

export interface IDataComplianceState {
  datacompliance: IDataCompliance;
}

export default class DataCompliance extends React.Component<IDataComplianceProps, any> {
  public static getDerivedStateFromProps(props: IDataComplianceProps, state: IDataComplianceState) {
    if (props.datacompliance !== state.datacompliance) {
      const datacompliance: IDataCompliance = {
        quickCheck: false,
        expertGuidelineNeeded: false,
        useCaseDescAndEval: false,
        readyForImplementation: false,
        attachments: [],
        links: [],
        complianceOfficers: [],
      };
      const dataComplianceVal = props.datacompliance;
      datacompliance.quickCheck = dataComplianceVal.quickCheck ? true : false;
      datacompliance.expertGuidelineNeeded =
        dataComplianceVal.quickCheck && dataComplianceVal.expertGuidelineNeeded ? true : false;
      datacompliance.useCaseDescAndEval =
        dataComplianceVal.quickCheck && dataComplianceVal.useCaseDescAndEval ? true : false;
      datacompliance.readyForImplementation =
        dataComplianceVal.quickCheck && dataComplianceVal.useCaseDescAndEval && dataComplianceVal.readyForImplementation
          ? true
          : false;
      datacompliance.attachments = dataComplianceVal.attachments;
      datacompliance.links = dataComplianceVal.links;
      datacompliance.complianceOfficers = dataComplianceVal.complianceOfficers;
      return { datacompliance };
    }
    return null;
  }
  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();
  constructor(props: IDataComplianceProps) {
    super(props);
    this.state = {
      datacompliance: {
        quickCheck: false,
        expertGuidelineNeeded: false,
        useCaseDescAndEval: false,
        readyForImplementation: false,
        attachments: [],
        links: [],
        complianceOfficers: [],
      },
      addTeamMemberInController: true,
      showAddTeamMemberModal: false,
      editTeamMember: false,
      sharingTeamMembers: [],
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
      contollerTeamMembers: [],
      showInfoModal: false,
      showExpertGuidelineModal: false,
      tempexpertGuidelineNeededValue: false,
    };
  }

  public resetChanges = () => {
    if (this.props.datacompliance) {
      const datacompliance = this.state.datacompliance;
      datacompliance.quickCheck = this.props.datacompliance.quickCheck;
      datacompliance.expertGuidelineNeeded = this.props.datacompliance.expertGuidelineNeeded;
      datacompliance.useCaseDescAndEval = this.props.datacompliance.useCaseDescAndEval;
      datacompliance.readyForImplementation = this.props.datacompliance.readyForImplementation;
      datacompliance.attachments = this.props.datacompliance.attachments;
      datacompliance.links = this.props.datacompliance.links;
      datacompliance.complianceOfficers = this.props.datacompliance.complianceOfficers;
    }
  };

  public render() {
    const complianceOfficersList = this.state.datacompliance.complianceOfficers
      ? this.state.datacompliance.complianceOfficers.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.datacompliance.complianceOfficers.length}
              onMoveUp={this.onTeamMemberMoveUp}
              onMoveDown={this.onTeamMemberMoveDown}
              onEdit={this.onControllerTeamMemberEdit}
              onDelete={this.onControllerTeamMemberDelete}
            />
          );
        })
      : [];

    const contactString = `Please add your local Data Compliance Contact.`;

    const contentForExpertGuidelineModal = (
      <div className={classNames(Styles.formWrapper, Styles.expertGuidelineSection)}>
        <p>
          You're about to continue without
          <br />
          Expert Guidelines
        </p>

        <div>
          <label className="checkbox">
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                checked={this.state.datacompliance.expertGuidelineNeeded}
                onChange={this.onChangeOfExpertGuidelines}
              />
            </span>
            <span className="label">Expert Guidelines are not needed for this solution</span>
          </label>
        </div>

        <div>
          <button className="btn btn-tertiary" type="button" onClick={this.onConfirmExpertGuidelines}>
            Confirm
          </button>
        </div>
      </div>
    );

    const canDisabledUsecase = this.state.datacompliance
      ? this.state.datacompliance.quickCheck && !this.state.datacompliance.expertGuidelineNeeded
        ? false
        : true
      : true;
    const canDisabledReadyForImplementation = this.state.datacompliance
      ? !canDisabledUsecase && this.state.datacompliance.useCaseDescAndEval
        ? false
        : true
      : true;

    const guidelinesLink = DATA_COMPLIANCE_INFO_LINKS.GUIDELINES;

    const contentForInfoModal = (
      <div className={Styles.processFlowWrapper}>
        <div className={Styles.step1Container}>
          <label>Step 1</label>
          <p>
            You start with the{' '}
            <a href={guidelinesLink} target="_blank" rel="noreferrer">
              Quick Check of the Initial Guidelines
            </a>{' '}
            to assess the legal criticality of your Use Case and to find out whether the application of the Expert
            Guidelines is necessary.
          </p>
        </div>
        <div className={Styles.quickReady}>
          <label>Ready for Implementation</label>
          <p>If no you're done.</p>
        </div>
        <div className={Styles.step2Container}>
          <label>Step 2</label>
          <p>
            If yes, you fill in the{' '}
            <a href={guidelinesLink} target="_blank" rel="noreferrer">
              Use Case Description
            </a>{' '}
            as part of the Expert Guidelines. From a comprehensive set of questions only those will be displayed in
            DataQ which are relevant for your Use Case. The purpose of the Use Case Description is to gather all
            relevant information from the Data Scientist/Use Case Sponsor as basis for the legal evaluation.
          </p>
        </div>
        <div className={Styles.step3Container}>
          <label>Step 3</label>
          <p>
            Your Use Case Description forms the basis for the{' '}
            <a href={guidelinesLink} target="_blank" rel="noreferrer">
              Use Case Evaluation
            </a>{' '}
            as part of the Expert Guidelines. DataQ will automatically display the relevant rules and measures to
            mitigate the legal risks of your intended Use Case. You will get concrete guidance on how to modify your Use
            Case in order to implement it in a compliant way while at the same time preserving the intended outcome of
            your Data Analytics Use Case as best possible.
          </p>
        </div>
        <div className={Styles.step4Container}>
          <label>Step 4</label>
          <p>
            For critical Use Cases you can seek consultation from your{' '}
            <a href={DATA_COMPLIANCE_INFO_LINKS.LOCAL_OFFICER} target="_blank" rel="noreferrer">
              Local Compliance Officer
            </a>{' '}
            (1st level support).
            <br />
            <br />
            You can find your local contact person on Data Compliance Contact-List.
          </p>
        </div>
        <div className={Styles.expertReady}>
          <label>Ready for Implementation</label>
        </div>
      </div>
    );

    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <div className={Styles.flexLayout}>
              <h3>Data Compliance</h3>
              <div className={Styles.infoIcon}>
                <i className="icon mbc-icon info" onClick={this.showInfoModal} />
              </div>
            </div>

            <div className={classNames(Styles.formWrapper)}>
              <div>
                <label className="checkbox">
                  <span className="wrapper">
                    <input
                      type="checkbox"
                      className="ff-only"
                      checked={this.state.datacompliance.quickCheck}
                      onChange={this.onChangeOfQuickCheck}
                    />
                  </span>
                  <span className="label">
                    You’ve completed the <strong>Quick Check of the Initial Guidelines</strong>
                  </span>
                </label>
              </div>
              <p className={Styles.complianceInfo}>
                The Initial Guidelines give you a comprehensive overview OR summary overview on the legal restrictions
                which apply to Data Analytics. They provide a Quick Check for a first orientation on the legal
                criticality of your Use Case and recommendations on how to proceed in a compliant way.
              </p>
              <hr />
              <div>
                <label className={classNames('checkbox', canDisabledUsecase ? Styles.greyOut : '')}>
                  <span className="wrapper">
                    <input
                      disabled={canDisabledUsecase}
                      type="checkbox"
                      className="ff-only"
                      checked={this.state.datacompliance.useCaseDescAndEval}
                      onChange={this.onChangeOfUseCaseDescAndEval}
                    />
                  </span>
                  <span className="label">
                    You’ve filled out the <strong>Use Case Description</strong> and took measures according to the{' '}
                    <strong>Use Case Evaluation</strong>
                  </span>
                </label>
              </div>
              <div>
                <label className={classNames('checkbox', canDisabledReadyForImplementation ? Styles.greyOut : '')}>
                  <span className="wrapper">
                    <input
                      disabled={canDisabledReadyForImplementation}
                      type="checkbox"
                      className="ff-only"
                      checked={this.state.datacompliance.readyForImplementation}
                      onChange={this.onChangeReadyForImplementationCheck}
                    />
                  </span>
                  <span className="label">Ready for implementation</span>
                </label>
              </div>
              <p className={Styles.complianceInfo}>
                The Expert Guidelines consist of two integral parts, on the one hand the corresponding Evaluation Use
                Case Description and on the other hand the corresponding Use Case Evaluation. The purpose of the Use
                Case Description is to gather all relevant information on a specific Use Case from the business
                department. With regard to these information, the corresponding Evaluation provide recommendations and
                measures to mitigate the risks of your planned Use Case in order to have it implemented more easily
                while at the same time preserving the intended outcome of your Data Analytics Use Case as best possible.
              </p>
            </div>
          </div>
        </div>
        <AttachmentUploader
          infoText="Please add the Compliance Framework for Data Analytics attachments of your Use Case (Evaluation Result or a Link to the documents)."
          attachments={this.state?.datacompliance?.attachments}
          modifyAttachments={this.modifyAttachments}
          containerId="DataCompliance"
        />
        <ExternalLink links={this.state.datacompliance.links} modifyLinks={this.modifyLinks} />
        <div className={classNames(Styles.contactWrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Add Contact</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div>
                {!complianceOfficersList.length ? (
                  <div>
                    <p>{contactString}</p>
                  </div>
                ) : null}
                <div>
                  <div className={Styles.addButtonWrapper}>
                    <IconAvatarNew className={Styles.buttonIcon} />
                    <button id="addComplianceOfficerBtn" onClick={this.addControllerMember}>
                      <i className="icon mbc-icon plus" />
                      <span>Add Responsible Local Compliance Officer</span>
                    </button>
                  </div>
                </div>
              </div>
              {complianceOfficersList.length ? (
                <div className={Styles.memberListWrapper}>
                  <h4>Contacts</h4>
                  {complianceOfficersList}
                </div>
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onDataComplianceSubmit}>
            Save & Next
          </button>
        </div>
        {this.state.showAddTeamMemberModal && (
          <AddTeamMemberModal
            ref={this.addTeamMemberModalRef}
            modalTitleText={'Contact'}
            showOnlyInteral={true}
            editMode={this.state.editTeamMember}
            showAddTeamMemberModal={this.state.showAddTeamMemberModal}
            teamMember={this.state.teamMemberObj}
            onUpdateTeamMemberList={this.updateTeamMemberList}
            onAddTeamMemberModalCancel={this.onAddTeamMemberModalCancel}
            validateMemebersList={this.validateMembersList}
          />
        )}
        {this.state.showInfoModal && (
          <InfoModal
            title={'Compliance Framework / Process Flow'}
            modalWidth={'35vw'}
            show={this.state.showInfoModal}
            content={contentForInfoModal}
            moreInfoLink={DATA_COMPLIANCE_INFO_LINKS.MORE_INFO}
            onCancel={this.onInfoModalCancel}
          />
        )}
        {this.state.showExpertGuidelineModal && (
          <ConfirmModal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            show={this.state.showExpertGuidelineModal}
            removalConfirmation={true}
            showIcon={false}
            content={contentForExpertGuidelineModal}
            onCancel={this.onExpertGuidelineModalCancel}
          />
        )}
      </React.Fragment>
    );
  }

  protected onChangeOfQuickCheck = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { datacompliance } = this.state;
    datacompliance.quickCheck = e.target.checked;
    if (datacompliance.quickCheck === true) {
      this.setState({
        showExpertGuidelineModal: true,
      });
    } else {
      datacompliance.expertGuidelineNeeded = false;
      datacompliance.useCaseDescAndEval = false;
      datacompliance.readyForImplementation = false;
      this.setState({
        tempexpertGuidelineNeededValue: false,
      });
    }
    this.setState({
      datacompliance,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected onChangeOfExpertGuidelines = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { datacompliance } = this.state;
    datacompliance.expertGuidelineNeeded = e.target.checked;
    this.setState({
      tempexpertGuidelineNeededValue: e.target.checked,
      datacompliance,
    });
  };

  protected onChangeOfUseCaseDescAndEval = (e: React.ChangeEvent<HTMLInputElement>) => {
    const datacompliance = this.state.datacompliance;
    datacompliance.useCaseDescAndEval = e.target.checked;
    if (e.target.checked === false) {
      datacompliance.readyForImplementation = false;
    }
    this.setState({
      datacompliance,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected onChangeReadyForImplementationCheck = (e: React.ChangeEvent<HTMLInputElement>) => {
    const datacompliance = this.state.datacompliance;
    datacompliance.readyForImplementation = e.target.checked;
    this.setState({
      datacompliance,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected onConfirmExpertGuidelines = () => {
    const datacompliance = this.state.datacompliance;
    datacompliance.expertGuidelineNeeded = this.state.tempexpertGuidelineNeededValue;
    this.setState({
      datacompliance,
      showExpertGuidelineModal: false,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected modifyAttachments = (attachments: IAttachment[]) => {
    const datacompliance = this.state.datacompliance;
    datacompliance.attachments = attachments;
    this.setState({
      datacompliance,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected modifyLinks = (links: ILink[]) => {
    const datacompliance = this.state.datacompliance;
    datacompliance.links = links;
    this.setState({
      datacompliance,
    });
    this.props.modifyDataCompliance(datacompliance);
  };

  protected onDataComplianceSubmit = () => {
    this.state.datacompliance.complianceOfficers = this.state.datacompliance.complianceOfficers;
    this.props.modifyDataCompliance(this.state.datacompliance);
    this.props.onSaveDraft('datacompliance');
  };

  protected addControllerMember = () => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.showAddTeamMemberModalView();
    });
  };

  protected showAddTeamMemberModalView = () => {
    this.resetAddTeamMemberState();
    this.setState({ showAddTeamMemberModal: true }, () => {
      this.addTeamMemberModalRef.current.setTeamMemberData(this.state.teamMemberObj, false);
    });
  };

  protected resetAddTeamMemberState() {
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

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetAddTeamMemberState();
    });
  };

  protected updateTeamMemberList = (teamMemberObj: ITeams) => {
    const { editTeamMember, editTeamMemberIndex, sharingTeamMembers, addTeamMemberInController } = this.state;
    let teamMembers = sharingTeamMembers;
    if (addTeamMemberInController) {
      teamMembers = this.state.datacompliance.complianceOfficers;
    }
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    const stateUpdateObj = {
      showAddTeamMemberModal: false,
      contollerTeamMembers: this.state.contollerTeamMembers,
      sharingTeamMembers: this.state.sharingTeamMembers,
      datacompliance: { complianceOfficers: this.state.datacompliance.complianceOfficers },
    };

    if (addTeamMemberInController) {
      stateUpdateObj.contollerTeamMembers = teamMembers;
    } else {
      stateUpdateObj.sharingTeamMembers = teamMembers;
    }

    this.setState(stateUpdateObj, () => {
      this.resetAddTeamMemberState();
      // this.props.modifyTeam(this.state.teamMembers);
    });
  };

  protected validateMembersList = (teamMemberObj: ITeams) => {
    let duplicateMember = false;
    duplicateMember = this.state.datacompliance.complianceOfficers?.filter(
      (member: any) => member.shortId === teamMemberObj.shortId,
    )?.length
      ? true
      : false;
    return duplicateMember;
  };

  protected onTeamMemberMoveUp = (index: number) => {
    const contollerTeamMembers = this.state.contollerTeamMembers;
    const teamMember = contollerTeamMembers.splice(index, 1)[0];
    contollerTeamMembers.splice(index - 1, 0, teamMember);
    this.setState({ contollerTeamMembers });
    // this.props.modifyTeam(this.state.teamMembers);
  };

  protected onTeamMemberMoveDown = (index: number) => {
    const contollerTeamMembers = this.state.contollerTeamMembers;
    const teamMember = contollerTeamMembers.splice(index, 1)[0];
    contollerTeamMembers.splice(index + 1, 0, teamMember);
    this.setState({ contollerTeamMembers });
    // this.props.modifyTeam(this.state.teamMembers);
  };

  protected onControllerTeamMemberEdit = (index: number) => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onTeamMemberEdit = (index: number) => {
    const { sharingTeamMembers, addTeamMemberInController } = this.state;
    const teamMemberObj = addTeamMemberInController
      ? this.state.datacompliance.complianceOfficers[index]
      : sharingTeamMembers[index];
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

  protected onControllerTeamMemberDelete = (index: number) => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onTeamMemberDelete = (index: number) => {
    const { sharingTeamMembers, addTeamMemberInController } = this.state;
    const teamMembers = addTeamMemberInController ? this.state.datacompliance.complianceOfficers : sharingTeamMembers;
    teamMembers.splice(index, 1);
    if (addTeamMemberInController) {
      this.setState({ contollerTeamMembers: teamMembers, datacompliance: { complianceOfficers: teamMembers } });
    } else {
      this.setState({ sharingTeamMembers: teamMembers });
    }
    // this.props.modifyTeam(this.state.contollerTeamMembers);
  };

  protected showInfoModal = () => {
    this.setState({ showInfoModal: true });
  };

  protected onInfoModalCancel = () => {
    this.setState({ showInfoModal: false });
  };

  protected onExpertGuidelineModalCancel = () => {
    const datacompliance = this.state.datacompliance;
    datacompliance.quickCheck = false;
    datacompliance.expertGuidelineNeeded = false;
    this.setState({ showExpertGuidelineModal: false, datacompliance, tempexpertGuidelineNeededValue: false });
  };
}
