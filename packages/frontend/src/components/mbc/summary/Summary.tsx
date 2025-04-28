import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { getParams } from '../../../router/RouterUtils';

import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import Modal from 'components/formElements/modal/Modal';
import { USER_ROLE, SOLUTION_LOGO_IMAGE_TYPES, SOLUTION_VALUE_CALCULATION_TYPES } from 'globals/constants';
import { IBookMarks, ICreateNewSolutionResult, IPhase, IRole, IUserInfo, INotebookInfo, IDataiku, IDepartment, ITeams } from 'globals/types';
import { history } from '../../../router/History';
import { ApiClient } from '../../../services/ApiClient';
import { ICreateNewSolutionData } from '../createNewSolution/CreateNewSolution';
import DataComplianceSummary from './datacompliance/DataComplianceSummary';
import DataSourcesSummary from './datasources/DataSourcesSummary';
import AnalyticsSummary from './analytics/AnalyticsSummary';
import SharingSummary from './sharing/SharingSummary';
import DescriptionSummary from './description/DescriptionSummary';
import DigitalValueSummary from './digitalvalue/DigitalValueSummary';
import MilestonesSummary from './milestones/MilestonesSummary';
import { SummaryPdfDoc } from './pdfdoc/SummaryPdfDoc';
import Styles from './Summary.scss';
import TeamSummary from './team/TeamSummary';
import Platform from './platform/PlatformSummary';

import LogoImage from '../createNewSolution/description/logoManager/LogoImage/LogoImage';
import MarketingSummary from './marketing/MarketingSummary';
import AddUser from '../addUser/AddUser';
import { isSolutionFixedTagIncludedInArray } from '../../../services/utils';


const classNames = cn.bind(Styles);

export interface ISummaryState {
  isGenAI: boolean;
  response?: ICreateNewSolutionResult;
  solution: ICreateNewSolutionData;
  phases: IPhase[];
  canShowDataSources: boolean;
  canShowAnalytics: boolean;
  canShowSharing: boolean;
  canShowTeams: boolean;
  canShowPlatform: boolean;
  canShowMilestones: boolean;
  canShowDigitalValue: boolean;
  showDeleteSolutionModal: boolean;
  solutionToBeDeleted: string;
  solutionToBeTransfered: string;
  tabClassNames: Map<string, string>;
  currentTab: string;
  noteBookInfo: INotebookInfo;
  dataIkuInfo: IDataiku;
  dnaNotebookEnabled: boolean;
  dnaDataIkuProjectEnabled: boolean;
  notebookAndDataIkuNotEnabled: boolean;
  dataSources: any;
  departmentTags: IDepartment[];
  showTransferOwnershipConsentModal: boolean;
  showTransferOwnershipModal: boolean;
  solutionCollaborators: ITeams[];
}
export interface IAllSolutionsListItem {
  id?: string;
  productName: string;
}
export interface IAllSolutionsResult {
  records?: IAllSolutionsListItem[];
  totalCount?: number;
}

export default class Summary extends React.Component<{ user: IUserInfo }, ISummaryState> {

  static digitalValueTypeKeyValue = Object.keys(SOLUTION_VALUE_CALCULATION_TYPES)[0];
  static dataValueTypeKeyValue = Object.keys(SOLUTION_VALUE_CALCULATION_TYPES)[1];

  constructor(props: any) {
    super(props);
    this.state = {
      isGenAI: false,
      response: {},
      phases: [],
      solution: {
        description: {
          productName: '',
          location: [
            {
              id: '',
              name: '',
            },
          ],
          division: {
            id: '',
            name: '',
            subdivision: {
              id: '',
              name: '',
            },
          },

          status: {
            id: '',
            name: '',
          },

          relatedProducts: [],

          reasonForHoldOrClose: '',
          expectedBenefits: '',
          description: '',
          tags: [],
          businessNeeds: '',
          businessGoal: [],
          attachments: [],
          businessGoalsList: [],
          logoDetails: null,
          dataStrategyDomain: '',
          requestedFTECount: 0,
          additionalResource: '',
          department: '',
          leanIXDetails: {},
          appId: '',
        },
        openSegments: [],
        team: { team: [] },
        currentPhase: null,
        milestones: { phases: [], rollouts: { details: [], description: '' } },
        analytics: { languages: [], algorithms: [], visualizations: [], analyticsSolution: [] },
        dataSources: {
          dataSources: [],
          dataVolume: {},
        },
        sharing: {
          gitUrl: '',
          result: {
            id: null,
            name: '',
          },
          resultUrl: '',
        },
        marketing: {
          customerJourneyPhases: [],
          marketingCommunicationChannels: [],
          personalization: {
            isChecked: false,
            description: ''
          },
          personas: [],
          marketingRoles: []
        },
        datacompliance: {
          quickCheck: false,
          expertGuidelineNeeded: false,
          useCaseDescAndEval: false,
          readyForImplementation: false,
          attachments: [],
          links: [],
          complianceOfficers: [],
          aiRiskAssessmentType: '',
          workersCouncilApproval: false
        },
        digitalValue: {
          typeOfCalculation: Summary.digitalValueTypeKeyValue,
          maturityLevel: '',
          projectControllers: [],
          attachments: [],
          permissions: [],
          assessment: {
            strategicRelevance: '',
            commentOnStrategicRelevance: '',
            benefitRealizationRisk: '',
            commentOnBenefitRealizationRisk: '',
          },
          digitalValue: 0,
          digitalValueComment: '',
          digitalEffort: 0,
          digitalEffortComment: '',
          costDrivers: [],
          valueDrivers: [],
          valueCalculator: {
            calculatedValueRampUpYears: [],
            costFactorSummary: {
              year: '',
              value: '',
            },
            valueFactorSummary: {
              year: '',
              value: '',
            },
            calculatedDigitalValue: {
              valueAt: '',
              year: '',
              value: '',
            },
            breakEvenPoint: '',
          },
        },
        portfolio: {
          solutionOnCloud: false,
          usesExistingInternalPlatforms: false,
          platforms: [],
          dnaNotebookId: null,
          dnaDataikuProjectId: null,
          dnaDataikuProjectInstance: null,
          dnaSubscriptionAppId: null,
        },
        publish: false,
        bookmarked: false,
        neededRoles: [],
        createdDate: '',
        lastModifiedDate: '',
      },
      canShowDataSources: false,
      canShowAnalytics: false,
      canShowSharing: false,
      canShowDigitalValue: false,
      canShowTeams: false,
      canShowPlatform: false,
      canShowMilestones: false,
      showDeleteSolutionModal: false,
      solutionToBeDeleted: null,
      solutionToBeTransfered: null,
      tabClassNames: new Map<string, string>(),
      currentTab: 'description',
      noteBookInfo: null,
      dataIkuInfo: null,
      dnaNotebookEnabled: false,
      dnaDataIkuProjectEnabled: false,
      notebookAndDataIkuNotEnabled: true,
      dataSources: '',
      departmentTags: [],
      showTransferOwnershipConsentModal: false,
      showTransferOwnershipModal: false,
      solutionCollaborators: []
    };
  }

  public render() {
    const header = document.getElementById('header');
    window.addEventListener('scroll', () => {
      if (window.pageYOffset > 47) {
        header?.classList.remove('nav-header-bg-trans');
      } else {
        header?.classList.add('nav-header-bg-trans');
      }
    });
    const userInfo = this.props.user;
    const isAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
    const solutionName = this.state.solution.description.productName;
    const logoDetails = this.state.solution.description.logoDetails;

    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Solution</div>
        <div className={Styles.modalContent}>This solution will be deleted permanently.</div>
      </div>
    );
    const transferOwnershipConsentContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Transfer Ownership</div>
        <div className={Styles.modalContent}>Are sure you want to transfer ownership? After that you will not be able to make any changes in solution.</div>
      </div>
    );
    const {
      canShowTeams,
      solution: {
        datacompliance: { quickCheck, useCaseDescAndEval, attachments, links, complianceOfficers, aiRiskAssessmentType },
      },
    } = this.state;
    const canShowComplianceSummary =
      canShowTeams && 
      (quickCheck ||
        useCaseDescAndEval ||
        (attachments && attachments.length) ||
        (links && links.length) ||
        (complianceOfficers && complianceOfficers.length) ||
        (aiRiskAssessmentType && aiRiskAssessmentType.length));

    const canShowDescription = this.state.solution.description.productName !== '';
    const canShowDigitalValue =
      canShowDescription &&
      (isAdmin !== undefined || (userInfo.id === this.checkUserCanViewDigitalValue(userInfo)))
      &&
      (this.state.solution?.digitalValue?.maturityLevel && this.state.solution?.digitalValue?.maturityLevel != null);

    const canShowMarketing = this.state.solution?.marketing?.customerJourneyPhases?.length > 0 ||
      this.state.solution?.marketing?.marketingCommunicationChannels?.length > 0 ||
      this.state.solution?.marketing?.personas?.length > 0 ||
      this.state.solution?.marketing?.personalization?.isChecked ||
      this.state.solution?.marketing?.marketingRoles.length > 0;

    const pdfContent = canShowDescription ? (
      <SummaryPdfDoc
        solution={this.state.solution}
        dataSources={this.state.dataSources}
        lastModifiedDate={this.state.solution.lastModifiedDate}
        createdDate={this.state.solution.createdDate}
        canShowTeams={this.state.canShowTeams}
        canShowPlatform={this.state.canShowPlatform}
        canShowMilestones={this.state.canShowMilestones}
        canShowDataSources={this.state.canShowDataSources}
        canShowDigitalValue={canShowDigitalValue}
        canShowComplianceSummary={canShowComplianceSummary}
        isGenAi={this.state.isGenAI}
        user={this.props.user}
        noteBookInfo={this.state.noteBookInfo}
        dataIkuInfo={this.state.dataIkuInfo}
        dnaNotebookEnabled={this.state.dnaNotebookEnabled}
        dnaDataIkuProjectEnabled={this.state.dnaDataIkuProjectEnabled}
        notebookAndDataIkuNotEnabled={this.state.notebookAndDataIkuNotEnabled}
      />
    ) : null;

    const getCollabarators = (collaborators: any) => {
      const collabarationData = {
        firstName: collaborators.firstName,
        lastName: collaborators.lastName,
        shortId: collaborators.shortId,
        id: collaborators.shortId,
        department: collaborators.department,
        email: collaborators.email,
        mobileNumber: collaborators.mobileNumber,
        userType: 'internal'
      };

      let duplicateMember = false;
      duplicateMember = this.state?.solutionCollaborators?.filter((member) => member.shortId === collaborators.shortId)?.length
        ? true
        : false;
      const isCreator = collaborators.shortId === this.state?.solution?.createdBy?.id;

      if (duplicateMember) {
        Notification.show('Collaborator Already Exist.', 'warning');
      } else if (isCreator) {
        Notification.show(
          `${collaborators.firstName} ${collaborators.lastName} is a creator. Creator can't be added as collaborator.`,
          'warning',
        );
      } else {
        // bucketCollaborators.push(collabarationData);
        // setBucketCollaborators([...bucketCollaborators]);
        const { solutionCollaborators } = this.state;
        solutionCollaborators.push(collabarationData);
        this.setState({ solutionCollaborators })
      }
    };

    const transferOwnershipContent = (
      <div className={classNames('input-field-group include-error')}>
        <div className={Styles.bucketColContent}>
          <div className={Styles.bucketColContentList}>
            <div className={Styles.bucketColContentListAdd}>
              <AddUser getCollabarators={getCollabarators} dagId={''} isRequired={false} isUserprivilegeSearch={false} />
            </div>
            <div className={Styles.bucketColUsersList}>
              {this.state.solutionCollaborators?.length > 0 ? (
                <React.Fragment>
                  <div className={Styles.collUserTitle}>
                    <div className={Styles.collUserTitleCol}>User ID</div>
                    <div className={Styles.collUserTitleCol}>Name</div>
                    <div className={Styles.collUserTitleCol}></div>
                  </div>
                  <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                    {this.state.solutionCollaborators
                      // ?.filter((item) => item.accesskey !== user.id && item.accesskey !== createdBy.id)
                      ?.map((item: any, collIndex: any) => {
                        return (
                          <div key={'team-member-' + collIndex} className={Styles.collUserContentRow}>
                            <div className={Styles.collUserTitleCol}>{item.shortId}</div>
                            <div className={Styles.collUserTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                            <div className={Styles.collUserTitleCol}>
                              <div className={Styles.deleteEntry} onClick={() => this.onTransferOwnership(item)}>
                                <i className="icon mbc-icon comparison" />
                                Transfer Ownership
                              </div>
                            </div>
                          </div>
                        );
                      })}
                  </div>
                </React.Fragment>
              ) : (
                <div className={Styles.bucketColContentEmpty}>
                  <h6> Collaborators Not Exist!</h6>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );

    return (
      <div className={Styles.summaryWrapper}>
        {canShowDescription ? (
          <LogoImage
            displayType={SOLUTION_LOGO_IMAGE_TYPES.BANNER}
            logoDetails={logoDetails}
            className={Styles.summaryBannerimg}
          />
        ) : null}
        <div className={Styles.summaryContentWrapper}>
          <div className={Styles.backButtonWapper}>
            <button className="btn btn-text back arrow" type="submit" onClick={this.goback}>
              Back
            </button>
            <div className={Styles.summeryBannerTitle}>
              <h2>{solutionName}</h2>
            </div>
          </div>
          <div id="solution-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={this.state.currentTab === 'description' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="description" onClick={this.setCurrentTab}>
                      Solution Summary
                    </a>
                  </li>
                  <li
                    className={
                      (this.state.currentTab === 'digitalValue' ? 'tab active ' : 'tab ') +
                      (canShowDigitalValue ? '' : 'disabled')
                    }
                  >
                    <a
                      href="#tab-content-2"
                      id="digitalValue"
                      onClick={this.setCurrentTab}
                      className={canShowDigitalValue ? '' : 'hidden'}
                    >
                      Value Calculation Summary
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="digitalValue2" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="digitalValue3" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="digitalValue4" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="digitalValue5" className={'hidden'}>
                      `
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                {canShowDescription ? (
                  <React.Fragment>
                    <DescriptionSummary
                      description={this.state.solution.description}
                      canEdit={isAdmin !== undefined || userInfo.id === this.checkUserCanEditSolution(userInfo)}
                      solutionId={this.state.response.data ? this.state.response.data.id : ''}
                      bookmarked={this.state.solution.bookmarked}
                      lastModifiedDate={this.state.solution.lastModifiedDate}
                      createdDate={this.state.solution.createdDate}
                      onEdit={this.onEditSolution}
                      onDelete={this.onDeleteSolution}
                      updateBookmark={this.updateBookmark}
                      onExportToPDFDocument={pdfContent}
                      isPublished={this.state.solution.publish}
                      canTransferOwnerShip={userInfo?.id === this.state.solution?.createdBy?.id}
                      onTransferOwnershipSolutionConsent={this.onTransferOwnershipSolutionConsent}
                    />

                    {this.state.canShowPlatform && !this.state.isGenAI && (
                      <Platform
                        portfolio={this.state.solution.portfolio}
                        noteBookInfo={this.state.noteBookInfo}
                        dataIkuInfo={this.state.dataIkuInfo}
                        dnaNotebookEnabled={this.state.dnaNotebookEnabled}
                        dnaDataIkuProjectEnabled={this.state.dnaDataIkuProjectEnabled}
                        notebookAndDataIkuNotEnabled={this.state.notebookAndDataIkuNotEnabled}
                      />
                    )}
                    {this.state.canShowTeams && (
                      <TeamSummary team={this.state.solution.team} neededRoles={this.state.solution.neededRoles} />
                    )}

                    {this.state.canShowMilestones && (
                      <MilestonesSummary milestones={this.state.solution.milestones} phases={this.state.phases} currentPhase={this.state.solution.currentPhase} />
                    )}
                    
                    {this.state.canShowDataSources && !this.state.isGenAI && (
                      <DataSourcesSummary
                        datasources={this.state.solution.dataSources}
                        portfolio={this.state.solution.portfolio}
                        analytics={this.state.solution.analytics}
                        sharing={this.state.solution.sharing}
                        dsList={this.state.dataSources}
                      />
                    )}
                    {this.state.canShowAnalytics && <AnalyticsSummary
                      isGenAI={this.state.isGenAI}
                      analytics={this.state.solution.analytics}
                    />
                    }
                    {this.state.canShowPlatform && this.state.isGenAI && (
                      <Platform
                        portfolio={this.state.solution.portfolio}
                        noteBookInfo={this.state.noteBookInfo}
                        dataIkuInfo={this.state.dataIkuInfo}
                        dnaNotebookEnabled={this.state.dnaNotebookEnabled}
                        dnaDataIkuProjectEnabled={this.state.dnaDataIkuProjectEnabled}
                        notebookAndDataIkuNotEnabled={this.state.notebookAndDataIkuNotEnabled}
                      />
                    )}
                    {this.state.canShowDataSources && this.state.isGenAI && (
                      <DataSourcesSummary
                        datasources={this.state.solution.dataSources}
                        portfolio={this.state.solution.portfolio}
                        analytics={this.state.solution.analytics}
                        sharing={this.state.solution.sharing}
                        dsList={this.state.dataSources}
                      />
                    )}
                    {this.state.canShowSharing && <SharingSummary
                      sharing={this.state.solution.sharing}
                    />}
                    {canShowComplianceSummary ? (
                      <DataComplianceSummary dataCompliance={this.state.solution.datacompliance} />
                    ) : null}

                    {canShowMarketing && (
                      <MarketingSummary marketing={this.state.solution.marketing} />
                    )}

                  </React.Fragment>
                ) : (
                  ''
                )}
              </div>
              <div id="tab-content-2" className="tab-content">
                {canShowDigitalValue ? (
                  <DigitalValueSummary
                    digitalValue={this.state.solution.digitalValue}
                    solutionName={this.state.solution.description.productName}
                    canEdit={isAdmin !== undefined || (userInfo.id === this.checkUserCanEditSolution(userInfo))}
                    solutionId={this.state.response.data ? this.state.response.data.id : ''}
                    bookmarked={this.state.solution.bookmarked}
                    onEdit={this.onEditSolution}
                    onDelete={this.onDeleteSolution}
                    updateBookmark={this.updateBookmark}
                    onExportToPDFDocument={pdfContent}
                    canTransferOwnerShip={userInfo?.id === this.state.solution?.createdBy?.id}
                    onTransferOwnershipSolutionConsent={this.onTransferOwnershipSolutionConsent}
                  />
                ) : (
                  ''
                )}
              </div>
              <ConfirmModal
                title="Delete Solution"
                acceptButtonTitle="Delete"
                cancelButtonTitle="Cancel"
                showAcceptButton={true}
                showCancelButton={true}
                show={this.state.showDeleteSolutionModal}
                content={deleteModalContent}
                onCancel={this.onCancellingDeleteChanges}
                onAccept={this.onAcceptDeleteChanges}
              />
              <ConfirmModal
                title="Transfer Ownership"
                acceptButtonTitle="Proceed"
                cancelButtonTitle="Cancel"
                showAcceptButton={true}
                showCancelButton={true}
                show={this.state.showTransferOwnershipConsentModal}
                content={transferOwnershipConsentContent}
                onCancel={this.onCancellingTransferOwnershipConsentModal}
                onAccept={this.onAcceptingTransferOwnershipConsentModal}
              />
              <Modal
                title={"Transfer Ownership"}
                showAcceptButton={false}
                showCancelButton={false}
                buttonAlignment="right"
                show={this.state.showTransferOwnershipModal}
                content={transferOwnershipContent}
                scrollableContent={true}
                onCancel={this.onCancellingTransferOwnershipModal}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
  public async componentWillReceiveProps() {
    this.setup();
  }
  public async componentDidMount() {
    ApiClient.getDataSources()
      .then((res) => {
        this.setState({ dataSources: res }, () => { });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    this.setup();
    Tabs.defaultSetup();
    document.getElementById('header').classList.add('nav-header-bg-trans');
    document.getElementById('mainContainer').style.padding = '0px';
  }
  public async setup() {
    let { id } = getParams();
    if ((id == null || id === '') && this.state.response.data != null) {
      id = this.state.response.data.id;
    }
    await this.getSolutionById(id);

    ApiClient.getBookmarkedSolutions()
      .then((res) => {
        const solutions = res.data.solutions as IAllSolutionsResult;
        const sol = this.state.solution;
        if (solutions.totalCount > 0) {
          solutions.records.filter((tmpSol) => {
            return tmpSol.id === id;
          }).length > 0
            ? (sol.bookmarked = true)
            : (sol.bookmarked = false);
        }
        this.setState({ solution: sol }, () => { });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  }
  public async getSolutionById(id: string) {
    // let { id } = getParams();
    // if ((id == null || id === '') && this.state.response.data != null) {
    //   id = this.state.response.data.id;
    // }
    if (id) {
      ProgressIndicator.show();

      const userInfo = this.props.user;
      const isAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);

      await ApiClient.getSolutionById(id)
        .then((res) => {
          if (res) {
            const response = this.state.response;
            response.data = res;
            const solution = this.state.solution;
            solution.description.productName = res.productName;
            solution.description.businessNeeds = res.businessNeed;
            solution.description.description = res.description;
            solution.description.division = res.division;
            solution.description.expectedBenefits = res.expectedBenefits;
            solution.description.location = res.locations;
            solution.description.status = res.projectStatus;
            solution.description.relatedProducts = res.relatedProducts;
            solution.description.businessGoal = res.businessGoals;
            solution.description.tags = res.tags;
            if (res.tags && isSolutionFixedTagIncludedInArray(res.tags)) {
              this.setState({ isGenAI: true });
            }
            solution.description.logoDetails = res.logoDetails;
            solution.description.attachments = res.attachments;
            solution.description.reasonForHoldOrClose = res.reasonForHoldOrClose;
            solution.description.dataStrategyDomain = res.dataStrategyDomain;
            solution.description.additionalResource = res.additionalResource;
            solution.description.requestedFTECount = res.requestedFTECount;
            solution.description.department = res.department;
            solution.description.createdBy = res.createdBy;
            solution.description.appId = res.appId;
            solution.description.leanIXDetails = res.leanIXDetails;
            solution.milestones = res.milestones;
            solution.currentPhase = res.currentPhase;
            solution.team.team = res.team;
            solution.neededRoles = res.skills;
            solution.dataSources = res.dataSources;
            solution.digitalValue = res.digitalValue;
            if (res.digitalValue) {
              solution.digitalValue.typeOfCalculation = res.digitalValue?.typeOfCalculation ? res.digitalValue.typeOfCalculation : Summary.digitalValueTypeKeyValue;
            }
            solution.datacompliance = res.dataCompliance;
            // this.digitalValueComponent.current.updateComponentValues(res.digitalValue);
            solution.analytics = res.analytics;
            solution.portfolio = res.portfolio;
            solution.sharing = res.sharing;
            solution.marketing = res.marketing;
            solution.openSegments = res.openSegments;
            solution.publish = res.publish;
            solution.createdBy = res.createdBy;
            solution.createdDate = res.createdDate;
            solution.lastModifiedDate = res.lastModifiedDate;
            const tempCollab = res.team.filter((item: any) => {
              item.id = item.shortId
              return item.id !== userInfo.id && item.userType === 'internal';
            }
            )
            this.setState(
              {
                response,
                solution,
                solutionCollaborators: JSON.parse(JSON.stringify(tempCollab)),
                canShowDataSources:
                  (solution.dataSources &&
                    solution.dataSources.dataSources &&
                    solution.dataSources.dataSources.length > 0) ||
                  (solution.dataSources &&
                    solution.dataSources.dataVolume &&
                    solution.dataSources.dataVolume.name &&
                    solution.dataSources.dataVolume.name !== 'Choose') ||
                  (solution.analytics && solution.analytics.algorithms && solution.analytics.algorithms.length > 0) ||
                  (solution.analytics && solution.analytics.languages && solution.analytics.languages.length > 0) ||
                  (solution.analytics &&
                    solution.analytics.visualizations &&
                    solution.analytics.visualizations.length > 0) ||
                  (solution.sharing &&
                    ((solution.sharing.gitUrl && solution.sharing.gitUrl !== '') ||
                      (solution.sharing.result &&
                        solution.sharing.result.name &&
                        solution.sharing.result.name !== 'Choose') ||
                      (solution.sharing.resultUrl && solution.sharing.resultUrl !== ''))),
                canShowAnalytics:
                  (solution.analytics && solution.analytics.algorithms && solution.analytics.algorithms.length > 0) ||
                  (solution.analytics && solution.analytics.languages && solution.analytics.languages.length > 0) ||
                  (solution.analytics && solution.analytics.analyticsSolution && solution.analytics.analyticsSolution.length > 0) ||
                  (solution.analytics && solution.analytics.visualizations && solution.analytics.visualizations.length > 0),
                canShowSharing: solution.sharing &&
                  ((solution.sharing.gitUrl && solution.sharing.gitUrl !== '') ||
                    (solution.sharing.result &&
                      solution.sharing.result.name &&
                      solution.sharing.result.name !== 'Choose') ||
                    (solution.sharing.resultUrl && solution.sharing.resultUrl !== '')),
                canShowTeams: solution.team && solution.team.team.length > 0,
                canShowDigitalValue:
                  solution.digitalValue &&
                    (isAdmin !== undefined || (userInfo.id === this.checkUserCanViewDigitalValue(userInfo)))
                    ? true
                    : false,
                canShowMilestones:
                  solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0,
                currentTab: 'description',
                canShowPlatform:
                  (solution.portfolio && solution.portfolio.solutionOnCloud) ||
                  (solution.portfolio && solution.portfolio.usesExistingInternalPlatforms),
              },
              () => {
                if (res.portfolio.dnaNotebookId !== null) {
                  ApiClient.getNotebooksDetails(res.portfolio.dnaNotebookId).then((res) => {
                    this.setState({
                      noteBookInfo: res,
                      dnaNotebookEnabled: false,
                      notebookAndDataIkuNotEnabled: false,
                    });
                  });
                } else if (res.portfolio.dnaDataikuProjectId !== null) {
                  ApiClient.getDataikuProjectDetailsByProjectkey(res.portfolio.dnaDataikuProjectId, res.portfolio.dnaDataikuProjectInstance).then((dataikuRes) => {
                    this.setState({
                      dataIkuInfo: dataikuRes.data,
                      notebookAndDataIkuNotEnabled: false,
                      dnaDataIkuProjectEnabled: true,
                    });
                  }).catch(() => {
                    const dataikuDetails = {
                      name: res.portfolio.dnaDataikuProjectId,
                      projectKey: res.portfolio.dnaDataikuProjectId,
                      cloudProfile: res.portfolio.dnaDataikuProjectInstance,
                    };
                    this.setState({
                      dataIkuInfo: { ...this.state.dataIkuInfo, ...dataikuDetails },
                      notebookAndDataIkuNotEnabled: false,
                      dnaDataIkuProjectEnabled: true,
                    })
                  });
                }

                ProgressIndicator.hide();
                this.ResetTabs();
              },
            );
          }
        })
        .catch((error) => {
          this.setState(
            {
              response: {},
            },
            () => {
              const msg = error.message
                ? error.message.startsWith('No value')
                  ? 'No solution found.'
                  : error.message
                : 'Some Error Occured';
              this.showErrorNotification(msg);
            },
          );
        });
    }
  }
  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected onEditSolution = (solutionId: string) => {
    history.push('/editSolution/' + solutionId + '/true');
  };

  protected checkUserCanEditSolution(userInfo: IUserInfo) {
    let userId = '';
    if (this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id)) {
      userId = this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
    } else if (this.state.solution?.createdBy?.id === userInfo.id) {
      userId = this.state.solution.createdBy.id;
    } else if (
      userInfo?.divisionAdmins &&
      userInfo?.divisionAdmins.includes(this.state.solution?.description?.division?.name)
    ) {
      userId = userInfo.id;
    } else {
      userId = '';
    }
    return userId;
  }

  protected checkUserCanViewDigitalValue(userInfo: IUserInfo) {
    let userId = '';
    if (this.state.solution.digitalValue) {
      if (this.state.solution.digitalValue.permissions) {
        if (this.state.solution.digitalValue.permissions.find((teamMember) => teamMember.shortId === userInfo.id)) {
          userId = this.state.solution.digitalValue.permissions.find(
            (teamMember) => teamMember.shortId === userInfo.id,
          ).shortId;
        } else if (this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id)) {
          userId = this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
        } else if (
          userInfo?.divisionAdmins &&
          userInfo?.divisionAdmins.includes(this.state.solution?.description?.division?.name)
        ) {
          userId = userInfo.id;
        }
        else if (this.state.solution.createdBy) {
          userId = this.state.solution.createdBy.id;
        }
      } else if (this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id)) {
        userId = this.state.solution.team.team.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
      } else if (
        userInfo?.divisionAdmins &&
        userInfo?.divisionAdmins.includes(this.state.solution?.description?.division?.name)
      ) {
        userId = userInfo.id;
      } else if (this.state.solution.createdBy) {
        userId = this.state.solution.createdBy.id;
      }
    } else {
      userId = '';
    }
    return userId;
  }

  protected onDeleteSolution = (solutionId: string) => {
    this.setState({ showDeleteSolutionModal: true, solutionToBeDeleted: solutionId });
  };

  protected onTransferOwnershipSolutionConsent = (solutionId: string) => {
    this.setState({ showTransferOwnershipConsentModal: true, solutionToBeTransfered: solutionId });
  };

  protected updateBookmark = (solutionId: string, isRemove: boolean) => {
    const data: IBookMarks = {
      favoriteUsecases: [solutionId],
      id: this.props.user ? this.props.user.id : null,
      deleteBookmark: isRemove,
    };
    ProgressIndicator.show();
    ApiClient.bookMarkSolutions(data)
      .then((res) => {
        if (res) {
          const solution = this.state.solution;
          solution.bookmarked = !isRemove;
          this.setState(
            {
              solution,
            },
            () => {
              ProgressIndicator.hide();
              Notification.show(isRemove ? 'Removed from My Bookmarks' : 'Added to My Bookmarks');
            },
          );
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteSolutionModal: false, solutionToBeDeleted: null });
  };

  protected onCancellingTransferOwnershipConsentModal = () => {
    this.setState({ showTransferOwnershipConsentModal: false, solutionToBeTransfered: null });
  };

  protected onAcceptingTransferOwnershipConsentModal = () => {
    this.setState({ showTransferOwnershipModal: true, showTransferOwnershipConsentModal: false });
  };

  protected onCancellingTransferOwnershipModal = () => {
    this.setState({ showTransferOwnershipModal: false, solutionToBeTransfered: null });
  };

  protected onTransferOwnership = (userObj: ITeams) => {
    // call api to change ownership

    ProgressIndicator.show();
    ApiClient.transferSolutionOwner(userObj, this.state.solutionToBeTransfered)
      .then((res) => {
        if (res) {
          Notification.show('Owner transferred successfully');
          ProgressIndicator.hide();
          this.setState({ showTransferOwnershipModal: false }, () => {
            history.goBack();
          });
        }
      })
      .catch((error) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ showTransferOwnershipModal: false });
      });
  };

  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    ApiClient.deleteSolution(this.state.solutionToBeDeleted)
      .then((res) => {
        if (res) {
          Notification.show('Solution deleted successfully');
          ProgressIndicator.hide();
          this.setState({ showDeleteSolutionModal: false }, () => {
            history.goBack();
          });
        }
      })
      .catch((error) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ showDeleteSolutionModal: false });
      });
  };

  protected exportToPDF = () => { };

  protected setCurrentTab = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    this.setState({ currentTab: target.id });
  };
  protected goback = () => {
    history.goBack();
  };

  protected ResetTabs = () => {
    const tabActiveIndicator = document.querySelector('.active-indicator') as HTMLSpanElement;
    const firstTabContent = document.querySelector('.tab-content');
    const activeTabContent = document.querySelector('.tab-content.active');

    if (tabActiveIndicator && firstTabContent && activeTabContent) {
      tabActiveIndicator.style.left = '0px';
      if (!firstTabContent.classList.contains('active')) {
        firstTabContent.classList.add('active');
        activeTabContent.classList.remove('active');
      }
    }
  };


}
