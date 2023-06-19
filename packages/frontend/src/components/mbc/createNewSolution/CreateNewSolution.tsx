import cn from 'classnames';
import * as React from 'react';
import { history } from '../../../router/History';
import { getParams } from '../../../router/RouterUtils';

// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import InputFields from '../../../assets/modules/uilab/js/src/input-fields';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { USER_ROLE } from 'globals/constants';
import { Envs } from 'globals/Envs';
import { ApiClient } from '../../../services/ApiClient';
// import { getQueryParameterByName } from '../../../services/Query';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

// @ts-ignore
import * as _ from 'lodash';
import {
  IAnalytics,
  IAttachment,
  IBenefitRelevance,
  IBusinessGoal,
  ICreateNewSolutionRequest,
  ICreateNewSolutionResult,
  IDataCompliance,
  IDataSource,
  IDataVolume,
  IDepartment,
  // ILanguage,
  IDigitalValue,
  IDivision,
  IDivisionAndSubDivision,
  ILocation,
  ILogoDetails,
  IMarketing,
  IMarketingCommunicationChannel,
  IMarketingCustomerJourney,
  IMarketingRole,
  IMaturityLevel,
  IMilestonesList,
  IPhase,
  IPortfolio,
  IProjectStatus,
  IRelatedProduct,
  IResult,
  IRole,
  ISharing,
  IStrategicRelevance,
  ISubDivision,
  ITag,
  ITeams,
  IUserInfo,
} from 'globals/types';
import SelectBox from '../../formElements/SelectBox/SelectBox';
import Analytics from './analytics/Analytics';
import Styles from './CreateNewSolution.scss';
import DataCompliance from './datacompliance/DataCompliance';
import DataSources from './datasources/DataSources';
import Description from './description/Description';
import DigitalValue from './digitalvalue/DigitalValue';
import Milestones from './milestones/Milestones';
import Platform from './platform/Platform';
import Sharing from './sharing/Sharing';
import Teams from './teams/Teams';
import { trackEvent } from '../../../services/utils';
import Marketing from './marketing/Marketing';
import Caption from '../shared/caption/Caption';

const classNames = cn.bind(Styles);
export interface ICreateNewSolutionState {
  editMode: boolean;
  locations: ILocation[];
  divisions: IDivision[];
  subDivisions: ISubDivision[];
  projectStatuses: IProjectStatus[];
  phases: IPhase[];
  tags: ITag[];
  relatedProducts: string[];
  businessGoal: string;
  relatedProductsMaster: IRelatedProduct[];
  languages: ITag[];
  algorithms: ITag[];
  visualizations: ITag[];
  results: IResult[];
  dataSourcesTags: ITag[];
  dataSources: IDataSource;
  dataVolumes: IDataVolume[];
  platforms: ITag[];
  currentTab: string;
  nextTab: string;
  clickedTab: string;
  saveActionType: string;
  tabClassNames: Map<string, string>;
  solution: ICreateNewSolutionData;
  response?: ICreateNewSolutionResult;
  request?: ICreateNewSolutionRequest;
  currentState: ICreateNewSolutionData;
  showAlertChangesModal: boolean;
  publishFlag: boolean;
  businessGoalsList: IBusinessGoal[];
  maturityLevelsList: IMaturityLevel[];
  benefitRelevancesList: IBenefitRelevance[];
  strategicRelevancesList: IStrategicRelevance[];
  isProvision: boolean;
  departmentTags: IDepartment[];
  customerJourneyPhasesLOV: IMarketingCustomerJourney[];
  marketingCommunicationChannelsLOV: IMarketingCommunicationChannel[];
  marketingRolesLOV: IMarketingRole[];
}

export interface ICreateNewSolutionProps {
  user: IUserInfo;
  location?: any;
}

export interface ICreateNewSolutionData {
  description: IDescriptionRequest;
  team?: ITeamRequest;
  currentPhase?: IPhase;
  milestones?: IMilestonesList;
  dataSources: IDataSource;
  openSegments: string[];
  analytics: IAnalytics;
  sharing: ISharing;
  marketing: IMarketing;
  datacompliance: IDataCompliance;
  digitalValue: IDigitalValue;
  portfolio: IPortfolio;
  publish: boolean;
  createdBy?: IUserInfo;
  bookmarked?: boolean;
  neededRoles: INeededRoleObject[];
  createdDate: string;
  lastModifiedDate: string;
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
export interface IDescriptionRequest {
  productName: string;
  location: ILocation[];
  division: IDivisionAndSubDivision;
  status: IProjectStatus;
  relatedProducts: string[];
  expectedBenefits: string;
  description: string;
  businessNeeds: string;
  reasonForHoldOrClose: string;
  tags: string[];
  logoDetails: ILogoDetails;
  attachments: IAttachment[];
  businessGoal: string[];
  businessGoalsList: IBusinessGoal[];
  dataStrategyDomain: string;
  requestedFTECount: number;
  additionalResource: string;
  department: string;
}

export default class CreateNewSolution extends React.Component<ICreateNewSolutionProps, ICreateNewSolutionState> {
  private milestoneComponent = React.createRef<Milestones>();
  private dataSourceComponent = React.createRef<DataSources>();
  private analyticComponent = React.createRef<Analytics>();
  private sharingComponent = React.createRef<Sharing>();
  private digitalValueComponent = React.createRef<DigitalValue>();
  private platformComponent = React.createRef<Platform>();
  private dataComplianceComponent = React.createRef<DataCompliance>();
  private MarketingComponent = React.createRef<Marketing>();
  constructor(props: ICreateNewSolutionProps) {
    super(props);
    this.milestoneComponent = React.createRef<Milestones>();
    this.dataSourceComponent = React.createRef<DataSources>();
    this.analyticComponent = React.createRef<Analytics>();
    this.sharingComponent = React.createRef<Sharing>();
    this.digitalValueComponent = React.createRef<DigitalValue>();
    this.platformComponent = React.createRef<Platform>();
    this.state = {
      editMode: false,
      locations: [],
      divisions: [],
      subDivisions: [],
      projectStatuses: [],
      tags: [],
      relatedProducts: [],
      relatedProductsMaster: [],
      phases: [],
      languages: [],
      visualizations: [],
      algorithms: [],
      results: [],
      platforms: [],
      businessGoal: '',
      dataSourcesTags: [],
      dataSources: {
        dataSources: [],
        dataVolume: {},
      },
      dataVolumes: [],
      currentTab: 'description',
      nextTab: 'teams',
      clickedTab: '',
      response: {},
      saveActionType: '',
      tabClassNames: new Map<string, string>(),
      solution: {
        description: {
          productName: '',
          location: [
            {
              id: '',
              name: '',
              is_row: false,
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
          logoDetails: null,
          attachments: [],
          businessGoalsList: [],
          dataStrategyDomain: '',
          requestedFTECount: 0,
          additionalResource: '',
          department: '',
        },
        openSegments: [],
        team: { team: [] },
        currentPhase: null,
        milestones: { phases: [], rollouts: { details: [], description: '' } },
        analytics: { languages: [], algorithms: [], visualizations: [] },
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
            description: '',
          },
          personas: [],
          marketingRoles: [],
        },
        datacompliance: {
          quickCheck: false,
          expertGuidelineNeeded: false,
          useCaseDescAndEval: false,
          readyForImplementation: false,
          attachments: [],
          links: [],
          complianceOfficers: [],
        },
        digitalValue: {
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
        },
        portfolio: {
          solutionOnCloud: false,
          usesExistingInternalPlatforms: false,
          platforms: [],
          dnaNotebookId: null,
          dnaDataikuProjectId: null,
          dnaSubscriptionAppId: null,
        },
        publish: false,
        neededRoles: [],
        createdDate: '',
        lastModifiedDate: '',
      },
      // stateChanged: false,
      currentState: null,
      showAlertChangesModal: false,
      publishFlag: false,
      businessGoalsList: [],
      maturityLevelsList: [],
      benefitRelevancesList: [],
      strategicRelevancesList: [],
      isProvision: false,
      departmentTags: [],
      customerJourneyPhasesLOV: [],
      marketingCommunicationChannelsLOV: [],
      marketingRolesLOV: [],
    };
  }
  // public componentWillReceiveProps(nextProps: any) {
  //   if (nextProps.location.pathname.indexOf('createnewsolution')) {
  //     window.location.reload();
  //   }
  // }
  public componentDidMount() {
    Tabs.defaultSetup();
    InputFields.defaultSetup();
    ProgressIndicator.show();

    ApiClient.getCreateNewSolutionData().then((response) => {
      if (response) {
        const locations = response[0];
        const divisions = response[1];
        const projectStatuses = response[2];
        const phases: IPhase[] = response[3];
        const tags: ITag[] = response[4];
        const languages: ITag[] = response[5];
        const results: IResult[] = response[6];
        const algorithms: ITag[] = response[7];
        const visualizations: ITag[] = response[8];
        const dataSourcesTags: ITag[] = response[9];
        const dataVolumes: IDataVolume[] = response[10];
        const platforms: ITag[] = response[11];
        const relatedProductsMaster: IRelatedProduct[] = response[12];
        const businessGoal = response[13];
        const businessGoalsList = response[14].data;
        const maturityLevelsList = response[15].data;
        const benefitRelevancesList = response[16].data;
        const strategicRelevancesList = response[17].data;
        const customerJourneyPhasesLOV = response[18];
        const marketingCommunicationChannelsLOV = response[19];
        const departmentTags = response[20].data;
        const marketingRolesLOV = response[21];
        phases.forEach((phase) => {
          switch (phase.id) {
            case '1':
              phase.name = 'Kick-off';
              break;
            case '2':
              phase.name = 'Ideation';
              break;
            case '3':
              phase.name = 'Concept Development / Proof of concept';
              break;
            case '4':
              phase.name = 'Pilot';
              break;
            case '5':
              phase.name = 'Professionalization';
              break;
            case '6':
              phase.name = 'Operations / Rollout';
              break;
            default:
              break;
          }
        });
        this.setState(
          {
            locations,
            divisions,
            projectStatuses,
            phases,
            tags,
            relatedProductsMaster,
            languages,
            results,
            algorithms,
            visualizations,
            dataSourcesTags,
            dataVolumes,
            platforms,
            businessGoal,
            businessGoalsList,
            maturityLevelsList,
            benefitRelevancesList,
            strategicRelevancesList,
            customerJourneyPhasesLOV,
            marketingCommunicationChannelsLOV,
            departmentTags,
            marketingRolesLOV,
          },
          () => {
            Button.defaultSetup();
            SelectBox.defaultSetup();
            ProgressIndicator.hide();
            this.getSolutionById(() => {});
          },
        );
      }
    });
  }

  protected setupEditSolutionData(
    subDivisions: ISubDivision[],
    response: ICreateNewSolutionResult,
    solution: ICreateNewSolutionData,
    resetChildComponents: () => void | null,
  ) {
    this.setState(
      {
        subDivisions,
        response,
        solution,
      },
      () => {
        this.setOpenTabs(solution.openSegments);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
        resetChildComponents();
        this.setState({
          // currentStateHash: btoa(unescape(encodeURIComponent(JSON.stringify(this.state.solution)))),
          // currentStateHash: JSON.stringify(this.state.solution),
          currentState: JSON.parse(JSON.stringify(solution)),
        });
      },
    );
  }

  public async getSolutionById(resetChildComponents?: () => void | null) {
    let { id } = getParams();
    if ((id == null || id === '') && this.state.response.data != null) {
      id = this.state.response.data.id;
    }
    if (id) {
      this.setState({ editMode: true });
      ProgressIndicator.show();

      ApiClient.getSolutionById(id)
        .then((res) => {
          if (res) {
            const user = this.props.user;
            const isAdmin = user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
            if (
              isAdmin !== undefined ||
              user.id === (res.createdBy ? res.createdBy.id : '') ||
              res.team.find((teamMember) => teamMember.shortId === user.id) !== undefined ||
              (user?.divisionAdmins && user?.divisionAdmins.includes(res?.division?.name))
            ) {
              const response = this.state.response;
              response.data = res;
              const solution = this.state.solution;
              solution.description.productName = res.productName;
              solution.description.businessNeeds = res.businessNeed;
              solution.description.businessGoal = res.businessGoals;
              solution.description.description = res.description;
              solution.description.division = res.division;
              solution.description.expectedBenefits = res.expectedBenefits;
              solution.description.location = res.locations;
              solution.description.status = res.projectStatus;
              solution.description.relatedProducts = res.relatedProducts;
              solution.description.tags = res.tags;
              solution.description.logoDetails = res.logoDetails;
              solution.description.attachments = res.attachments;
              solution.description.reasonForHoldOrClose = res.reasonForHoldOrClose;
              solution.description.dataStrategyDomain = res.dataStrategyDomain;
              solution.description.additionalResource = res.additionalResource;
              solution.description.department = res.department;
              // solution.description.neededRoles = res.skills;
              solution.description.requestedFTECount = res.requestedFTECount;
              solution.milestones = res.milestones;
              // this.milestoneComponent.current.updateComponentValues(res.milestones);
              solution.currentPhase = res.currentPhase;
              solution.team.team = res.team;
              solution.neededRoles = res.skills;
              solution.dataSources = res.dataSources;
              // solution.digitalValue = this.translateToMillions(res.digitalValue);
              // this.digitalValueComponent.current.updateComponentValues(this.translateToMillions(res.digitalValue));
              solution.digitalValue = res.digitalValue;
              // this.digitalValueComponent.current.updateComponentValues(res.digitalValue);
              solution.analytics = res.analytics;
              solution.portfolio = res.portfolio;
              solution.sharing = res.sharing;
              solution.marketing = res?.marketing;
              solution.datacompliance = res.dataCompliance;
              solution.openSegments = res.openSegments;
              solution.publish = res.publish;
              let subDivisions: ISubDivision[] = [{ id: '0', name: 'None' }];
              const divisionId = res.division?.id;
              if (divisionId) {
                ApiClient.getSubDivisions(divisionId)
                  .then((subDivResponse) => {
                    subDivisions = subDivResponse;
                  })
                  .finally(() => {
                    this.setupEditSolutionData(subDivisions, response, solution, resetChildComponents);
                  });
              } else {
                this.setupEditSolutionData(subDivisions, response, solution, resetChildComponents);
              }
            } else {
              ProgressIndicator.hide();
              history.replace('/unauthorised');
            }
          }
        })
        .catch((error) => {
          this.setState(
            {
              response: {},
            },
            () => {
              this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            },
          );
        });
    }
  }

  public render() {
    const currentTab = this.state.currentTab;
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <Caption title={this.state.solution.description.productName || 'Create Solution'} />
          <div id="create-solution-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li
                    className={
                      this.state.tabClassNames.has('Description')
                        ? this.state.tabClassNames.get('Description')
                        : 'tab active'
                    }
                  >
                    <a href="#tab-content-1" id="description" onClick={this.setCurrentTab}>
                      Description
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('Platform')
                        ? this.state.tabClassNames.get('Platform')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-2" id="platform" onClick={this.setCurrentTab}>
                      Compute
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('Teams') ? this.state.tabClassNames.get('Teams') : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-3" id="teams" onClick={this.setCurrentTab}>
                      Members
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('Milestones')
                        ? this.state.tabClassNames.get('Milestones')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-4" id="milestones" onClick={this.setCurrentTab}>
                      Milestones
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('DataSources')
                        ? this.state.tabClassNames.get('DataSources')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-5" id="datasources" onClick={this.setCurrentTab}>
                      Data Sources
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('Analytics')
                        ? this.state.tabClassNames.get('Analytics')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-6" id="analytics" onClick={this.setCurrentTab}>
                      Analytics
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('Sharing') ? this.state.tabClassNames.get('Sharing') : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-7" id="sharing" onClick={this.setCurrentTab}>
                      Sharing
                    </a>
                  </li>
                  {Envs.ENABLE_DATA_COMPLIANCE ? (
                    <li
                      className={
                        this.state.tabClassNames.has('DataCompliance')
                          ? this.state.tabClassNames.get('DataCompliance')
                          : 'tab disabled'
                      }
                    >
                      <a href="#tab-content-8" id="datacompliance" onClick={this.setCurrentTab}>
                        Data Compliance
                      </a>
                    </li>
                  ) : (
                    ''
                  )}
                  <li
                    className={
                      this.state.tabClassNames.has('Marketing')
                        ? this.state.tabClassNames.get('Marketing')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-9" id="marketing" onClick={this.setCurrentTab}>
                      Marketing
                    </a>
                  </li>
                  <li
                    className={
                      this.state.tabClassNames.has('DigitalValue')
                        ? this.state.tabClassNames.get('DigitalValue')
                        : 'tab disabled'
                    }
                  >
                    <a href="#tab-content-10" id="digitalvalue" onClick={this.setCurrentTab}>
                      Digital Value
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <Description
                  locations={this.state.locations}
                  divisions={this.state.divisions}
                  subDivisions={this.state.subDivisions}
                  tags={this.state.tags}
                  logoDetails={this.state.solution.description.logoDetails}
                  attachments={this.state.solution.description.attachments}
                  projectStatuses={this.state.projectStatuses}
                  relatedProductsMaster={this.state.relatedProductsMaster}
                  businessGoalsList={this.state.businessGoalsList}
                  departmentTags={this.state.departmentTags}
                  description={this.state.solution.description}
                  modfifyDescription={this.modifySolutionDescription}
                  onSaveDraft={this.onSaveDraft}
                  isProvision={this.state.isProvision}
                />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'platform' && (
                  <Platform
                    solutionId={this.state.response?.data?.id}
                    user={this.props.user}
                    portfolio={this.state.solution.portfolio}
                    platforms={this.state.platforms}
                    modifyPortfolio={this.modifyPortfolio}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.platformComponent}
                  />
                )}
              </div>
              <div id="tab-content-3" className="tab-content">
                {currentTab === 'teams' && (
                  <Teams
                    team={this.state.solution.team}
                    neededRoles={this.state.solution.neededRoles}
                    modifyTeam={this.modifySolutionTeam}
                    onSaveDraft={this.onSaveDraft}
                  />
                )}
              </div>
              <div id="tab-content-4" className="tab-content">
                {currentTab === 'milestones' && (
                  <Milestones
                    milestones={this.state.solution.milestones}
                    phases={this.state.phases}
                    locations={this.state.locations}
                    currentPhase={this.state.solution.currentPhase}
                    modifyMileStones={this.modifyMileStones}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.milestoneComponent}
                  />
                )}
              </div>
              <div id="tab-content-5" className="tab-content">
                {currentTab === 'datasources' && (
                  <DataSources
                    dataSource={this.state.solution.dataSources}
                    dataSourcesTags={this.state.dataSourcesTags}
                    dataVolumes={this.state.dataVolumes}
                    modifyDataSources={this.modifyDataSources}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.dataSourceComponent}
                  />
                )}
              </div>
              <div id="tab-content-6" className="tab-content">
                {currentTab === 'analytics' && (
                  <Analytics
                    analytics={this.state.solution.analytics}
                    languages={this.state.languages}
                    algorithms={this.state.algorithms}
                    visualizations={this.state.visualizations}
                    modifyAnalytics={this.modifyAnalytics}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.analyticComponent}
                  />
                )}
              </div>
              <div id="tab-content-7" className="tab-content">
                {currentTab === 'sharing' && (
                  <Sharing
                    sharing={this.state.solution.sharing}
                    modifySharing={this.modifySharing}
                    results={this.state.results}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.sharingComponent}
                  />
                )}
              </div>
              {Envs.ENABLE_DATA_COMPLIANCE ? (
                <div id="tab-content-8" className="tab-content">
                  {currentTab === 'datacompliance' && (
                    <DataCompliance
                      datacompliance={this.state.solution.datacompliance}
                      onSaveDraft={this.onSaveDraft}
                      modifyDataCompliance={this.modifyDataCompliance}
                      ref={this.dataComplianceComponent}
                    />
                  )}
                </div>
              ) : (
                ''
              )}
              <div id="tab-content-9" className="tab-content">
                {currentTab === 'marketing' && (
                  <Marketing
                    marketing={this.state.solution.marketing}
                    modifyMarketing={this.modifyMarketing}
                    marketingCommunicationChannelsLOV={this.state.marketingCommunicationChannelsLOV}
                    customerJourneyPhasesLOV={this.state.customerJourneyPhasesLOV}
                    marketingRolesLOV={this.state.marketingRolesLOV}
                    onSaveDraft={this.onSaveDraft}
                    ref={this.MarketingComponent}
                  />
                )}
              </div>
              <div id="tab-content-10" className="tab-content">
                {currentTab === 'digitalvalue' && (
                  <DigitalValue
                    digitalValue={this.state.solution.digitalValue}
                    maturityLevelsList={this.state.maturityLevelsList}
                    benefitRelevancesList={this.state.benefitRelevancesList}
                    strategicRelevancesList={this.state.strategicRelevancesList}
                    onSaveDraft={this.onSaveDraft}
                    onPublish={this.onPublish}
                    modifyDigitalValue={this.modifyDigitalValue}
                    ref={this.digitalValueComponent}
                  />
                )}
              </div>
            </div>
          </div>
          <ConfirmModal
            title="Save Changes?"
            acceptButtonTitle="Close"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.showAlertChangesModal}
            content={
              <div id="contentparentdiv">
                Press &#187;Close&#171; to save your changes or press
                <br />
                &#187;Cancel&#171; to discard changes.
              </div>
            }
            onCancel={this.onCancellingUpdateChanges}
            onAccept={this.onAcceptUpdateChanges}
          />
        </div>
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </React.Fragment>
    );
  }

  public setOpenTabs = (openSegments: string[]) => {
    if (!this.state.solution.publish) {
      if (openSegments != null && openSegments.length > 0) {
        const tabClasses = new Map<string, string>();
        openSegments.forEach((openSegment) => {
          tabClasses.set(openSegment, 'tab valid');
        });
        this.setState({ tabClassNames: tabClasses });
      }
    } else {
      const tabClasses = new Map<string, string>();
      tabClasses.set('Description', 'tab valid');
      tabClasses.set('Teams', 'tab valid');
      tabClasses.set('Milestones', 'tab valid');
      tabClasses.set('Sharing', 'tab valid');
      tabClasses.set('DataSources', 'tab valid');
      tabClasses.set('Platform', 'tab valid');
      tabClasses.set('DataCompliance', 'tab valid');
      tabClasses.set('Marketing', 'tab valid');
      tabClasses.set('DigitalValue', 'tab valid');
      tabClasses.set('Analytics', 'tab valid');
      this.setState({ tabClassNames: tabClasses });
    }
  };
  public onCancellingUpdateChanges = () => {
    this.getSolutionById(() => {
      // Newlly added related products has id and name same
      const relatedProductsMasterFilter: IRelatedProduct[] = this.state.relatedProductsMaster.filter((obj) => {
        return obj.id !== obj.name;
      });
      this.setState(
        {
          showAlertChangesModal: false,
          relatedProductsMaster: relatedProductsMasterFilter,
        },
        () => {
          const currentTab = this.state.currentTab;
          currentTab === 'platform' && this.platformComponent.current.resetChanges();
          currentTab === 'milestones' && this.milestoneComponent.current.resetChanges();
          currentTab === 'datasources' && this.dataSourceComponent.current.resetChanges();
          currentTab === 'analytics' && this.analyticComponent.current.resetChanges();
          currentTab === 'sharing' && this.sharingComponent.current.resetChanges();
          currentTab === 'datacompliance' && this.dataComplianceComponent.current.resetChanges();
          currentTab === 'digitalvalue' && this.digitalValueComponent.current.resetChanges();
          document.getElementById(this.state.clickedTab).click();
        },
      );
    });
  };
  public onAcceptUpdateChanges = () => {
    document.getElementById(this.state.currentTab).click();
    this.setState({
      showAlertChangesModal: false,
    });
  };
  protected onSaveDraft = (tabToBeSaved: string) => {
    // const currentTab = this.state.currentTab;
    const currentTab = tabToBeSaved;
    this.setState({ saveActionType: 'btn' });
    if (currentTab === 'description') {
      this.saveDescription();
    } else if (currentTab === 'platform') {
      this.savePlatform();
    } else if (currentTab === 'teams') {
      this.saveTeam();
    } else if (currentTab === 'milestones') {
      this.saveMilestones();
    } else if (currentTab === 'datasources') {
      this.saveDataSources();
    } else if (currentTab === 'analytics') {
      this.saveAnalytics();
    } else if (currentTab === 'sharing') {
      this.saveSharing();
    } else if (currentTab === 'marketing') {
      this.saveMarketing();
    } else if (currentTab === 'datacompliance') {
      this.saveDataCompliance();
    } else if (currentTab === 'digitalvalue') {
      this.saveDigitalValue();
    } else {
      // If multiple clicks on save happens then the currenttab doesnt get updated in that case
      // just save not moving to another tab.
      this.callApiToSave(false, null);
    }
  };

  protected onPublish = () => {
    this.setState({ publishFlag: true }, () => {
      this.callApiToSave(true, null);
    });
  };

  protected setTabsAndClick = (nextTab: string) => {
    const nextTabToClick = nextTab;
    this.setState({
      currentTab: nextTab,
      nextTab: nextTabToClick,
    });
    if (nextTabToClick) {
      document.getElementById(nextTabToClick).click();
    }
  };

  protected setCurrentTab = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    const newState = this.state.solution;
    const saveActionType = this.state.saveActionType;
    const currentState = this.state.currentState;

    if (!currentState || saveActionType === 'btn' || _.isEqual(newState, currentState)) {
      this.setState({ currentTab: target.id, saveActionType: '' });
    } else {
      if (!this.state.showAlertChangesModal) {
        this.setState({ showAlertChangesModal: true, clickedTab: target.id });
      }
    }
  };

  protected saveDescription = () => {
    this.state.solution.openSegments.push('Description');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'platform');
  };
  protected savePlatform = () => {
    this.state.solution.openSegments.push('Platform');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'teams');
  };
  protected saveTeam = () => {
    this.state.solution.openSegments.push('Teams');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'milestones');
  };
  protected saveMilestones = () => {
    this.state.solution.openSegments.push('Milestones');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'datasources');
  };
  protected saveDataSources = () => {
    this.state.solution.openSegments.push('DataSources');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'analytics');
  };

  protected saveAnalytics = () => {
    this.state.solution.openSegments.push('Analytics');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'sharing');
  };
  protected saveSharing = () => {
    this.state.solution.openSegments.push('Sharing');
    this.setState({ publishFlag: false });
    const nextTab = Envs.ENABLE_DATA_COMPLIANCE ? 'datacompliance' : 'marketing';
    this.callApiToSave(this.state.solution.publish, nextTab);
  };
  protected saveDataCompliance = () => {
    this.state.solution.openSegments.push('DataCompliance');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'marketing');
  };
  protected saveMarketing = () => {
    this.state.solution.openSegments.push('Marketing');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'digitalvalue');
  };
  protected saveDigitalValue = () => {
    this.state.solution.openSegments.push('DigitalValue');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, null);
  };
  protected callApiToSave = (isPublished: boolean, nextTab: string) => {
    const solution = this.state.solution;
    const distinct = (value: any, index: any, self: any) => {
      return self.indexOf(value) === index;
    };

    this.state.solution.openSegments = solution.openSegments.filter(distinct);
    if (solution.description.division.subdivision.id === '0') {
      solution.description.division.subdivision.id = null;
      solution.description.division.subdivision.name = null;
    }
    if (solution.marketing.personalization.isChecked === null) {
      solution.marketing.personalization.isChecked = false;
    }
    const data: ICreateNewSolutionRequest = {
      data: {
        productName: solution.description.productName,
        reasonForHoldOrClose: solution.description.reasonForHoldOrClose,
        businessNeed: solution.description.businessNeeds,
        businessGoals: solution.description.businessGoal,
        description: solution.description.description,
        division: solution.description.division,
        expectedBenefits: solution.description.expectedBenefits,
        locations: solution.description.location,
        relatedProducts: solution.description.relatedProducts,
        projectStatus: solution.description.status,
        openSegments: solution.openSegments,
        tags: solution.description.tags,
        logoDetails: solution.description.logoDetails,
        attachments: solution.description.attachments,
        department: solution.description.department,
        team: solution.team.team,
        currentPhase: solution.currentPhase,
        milestones: solution.milestones,
        dataSources: solution.dataSources,
        // digitalValue: this.translateToUnits(solution.digitalValue),
        dataCompliance: solution.datacompliance,
        digitalValue: solution.digitalValue,
        portfolio: solution.portfolio,
        publish: isPublished,
        analytics: solution.analytics,
        sharing: solution.sharing,
        marketing: solution.marketing,
        dataStrategyDomain: solution.description.dataStrategyDomain,
        requestedFTECount: solution.description.requestedFTECount,
        skills: solution.neededRoles,
        additionalResource: solution.description.additionalResource,
      },
    };
    ProgressIndicator.show();

    if (this.state.response && this.state.response.data && this.state.response.data.id) {
      data.data.id = this.state.response.data.id;
      ApiClient.updateSolution(data)
        .then((response) => {
          if (response) {
            this.trackSolutionEvent(`Update Solution ${isPublished ? 'Publish' : 'Save as Draft'} action on tab panel`);
            this.setState(
              {
                response,
              },
              () => {
                this.setState({
                  // currentStateHash: btoa(unescape(encodeURIComponent(JSON.stringify(this.state.solution)))),
                  // currentStateHash: JSON.stringify(this.state.solution),
                  currentState: JSON.parse(JSON.stringify(this.state.solution)),
                });
                this.setOpenTabs(solution.openSegments);
                nextTab && this.setTabsAndClick(nextTab);
                this.showNotification(isPublished);
              },
            );
          }
        })
        .catch((error) => {
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    } else {
      ApiClient.createNewSolution(data)
        .then((response) => {
          if (response) {
            this.trackSolutionEvent('New Solution Save as Draft action on tab panel');
            this.setState(
              {
                response,
              },
              () => {
                this.setState({
                  // currentStateHash: btoa(unescape(encodeURIComponent(JSON.stringify(this.state.solution)))),
                  // currentStateHash: JSON.stringify(this.state.solution),
                  currentState: JSON.parse(JSON.stringify(this.state.solution)),
                });
                this.setOpenTabs(solution.openSegments);
                this.setTabsAndClick(nextTab);
                this.showNotification(isPublished);
              },
            );
          }
        })
        .catch((error) => {
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    }
    this.setState({
      request: data,
    });
  };

  protected trackSolutionEvent(message: string) {
    const from = document.getElementById(this.state.currentTab)?.innerText;
    trackEvent(`${this.state.editMode ? 'Edit' : 'Create'} Solution`, message, from ? from : '');
  }

  protected showNotification(isPublished: boolean) {
    ProgressIndicator.hide();
    Notification.show((this.state.publishFlag ? 'Solution saved and published' : 'Draft saved') + ' successfully.');
  }

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected modifySolutionDescription = (description: IDescriptionRequest) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.description = description;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifySolutionTeam = (team: ITeamRequest, neededRoles: INeededRoleObject[]) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.team = team;
    currentSolutionObject.neededRoles = neededRoles;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyMileStones = (milestones: IMilestonesList, currentPhase: IPhase) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.milestones = milestones;
    currentSolutionObject.currentPhase = currentPhase;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyDataSources = (dataSources: IDataSource) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.dataSources = dataSources;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyAnalytics = (analytics: IAnalytics) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.analytics = analytics;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifySharing = (sharing: ISharing) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.sharing = sharing;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyMarketing = (marketing: IMarketing) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.marketing = marketing;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyDataCompliance = (datacompliance: IDataCompliance) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.datacompliance = datacompliance;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyDigitalValue = (digitalValue: IDigitalValue) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.digitalValue = digitalValue;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected modifyPortfolio = (portfolio: IPortfolio) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.portfolio = portfolio;
    this.setState({
      solution: currentSolutionObject,
    });
  };
}
