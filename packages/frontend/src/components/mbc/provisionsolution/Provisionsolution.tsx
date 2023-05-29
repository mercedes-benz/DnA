import React from 'react';
import Styles from './Provisionsolution.scss';
import Description from '../createNewSolution/description/Description';
import { ApiClient } from '../../../services/ApiClient';

import SelectBox from 'components/formElements/SelectBox/SelectBox';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';

// import { history } from '../../../router/History';

// @ts-ignore
import * as _ from 'lodash';
import { ComputeFixedTag, ProvisionSource } from 'globals/Enums';

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
  IMaturityLevel,
  IMilestonesList,
  INeededRoleObject,
  IPhase,
  IPortfolio,
  IProjectStatus,
  IRelatedProduct,
  IResult,
  ISharing,
  IStrategicRelevance,
  ISubDivision,
  ITag,
  ITeams,
  IUserInfo,
} from 'globals/types';

export interface IProvisionSolutionState {
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
  solution: IProvisionSolutionData;
  response?: ICreateNewSolutionResult;
  request?: ICreateNewSolutionRequest;
  showAlertChangesModal: boolean;
  publishFlag: boolean;
  businessGoalsList: IBusinessGoal[];
  maturityLevelsList: IMaturityLevel[];
  benefitRelevancesList: IBenefitRelevance[];
  strategicRelevancesList: IStrategicRelevance[];
  isProvision: boolean;
  departmentTags: IDepartment[];
}

export interface IProvisionSolutionProps {
  provisionFrom: string;
  provisionStatus?: (status: boolean) => void;
  provisionedSolutionId: (dnaNotebookId: string) => void;
  projectToBeProvisioned?: string;
  projectToBeProvisionedObject?: any;
}

export interface IProvisionSolutionData {
  description: IDescriptionRequest;
  team?: ITeams[];
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
  neededRoles?: INeededRoleObject[];
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

export default class Provisionsolution extends React.Component<IProvisionSolutionProps, IProvisionSolutionState> {
  constructor(props: IProvisionSolutionProps) {
    super(props);
    this.state = {
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
          department: ''
        },
        openSegments: [],
        team: [],
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
          dnaNotebookId: null,
          dnaDataikuProjectId: null,
          dnaSubscriptionAppId: null,
          solutionOnCloud: false,
          usesExistingInternalPlatforms: false,
          platforms: [],
        },
        publish: false,
        neededRoles: [],
      },
      // stateChanged: false,
      showAlertChangesModal: false,
      publishFlag: false,
      businessGoalsList: [],
      maturityLevelsList: [],
      benefitRelevancesList: [],
      strategicRelevancesList: [],
      isProvision: true,
      departmentTags: []
    };
  }
  public render() {
    return (
      <React.Fragment>
        <div className={Styles.sandboxpanel}>
          <h3> Provision Solution </h3>
          <p>
            Once you provision, your solution will be saved as draft and not visible to others. Please publish the
            solution to make it visible to everyone.
          </p>
          <div className={Styles.scrollContent}>
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
        </div>
      </React.Fragment>
    );
  }

  public componentDidMount() {
    if (this.props.provisionFrom === ProvisionSource.NOTEBOOK) {
      ApiClient.getNotebooksDetails().then((res) => {
        const solution = this.state.solution;
        solution.description.productName = 'Solution for - ' + res.name;
        solution.description.description = 'Solution for - ' + res.description;
        const portfolio: IPortfolio = {
          dnaNotebookId: res.id,
          dnaDataikuProjectId: null,
          dnaSubscriptionAppId: null,
          solutionOnCloud: false,
          usesExistingInternalPlatforms: true,
          platforms: [
            {
              id: null,
              name: ComputeFixedTag.NOTEBOOK,
            },
          ],
        };

        solution.portfolio = portfolio;
        this.setState({
          solution,
        });
      });
    }
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
          },
          () => {
            SelectBox.defaultSetup();
            ProgressIndicator.hide();
          },
        );
      }
    });
  }

  static getDerivedStateFromProps(nextProps: any, prevState: any) {
    if (nextProps.projectToBeProvisioned && nextProps.provisionFrom === ProvisionSource.MALWARESCANSERVICE) {
      const solution = prevState.solution;
      solution.description.productName = nextProps.projectToBeProvisionedObject.appName;
      solution.description.description = nextProps.projectToBeProvisionedObject.description
        ? nextProps.projectToBeProvisionedObject.description
        : nextProps.projectToBeProvisionedObject.appName;
      const portfolio: IPortfolio = {
        dnaSubscriptionAppId: nextProps.projectToBeProvisioned,
        dnaDataikuProjectId: null,
        dnaNotebookId: null,
        solutionOnCloud: false,
        usesExistingInternalPlatforms: true,
        platforms: [
          {
            id: null,
            name: ComputeFixedTag.MALWARESCANSERVICE,
          },
        ],
      };

      solution.portfolio = portfolio;
      return { solution: solution };
    }
    return null;
  }

  /****************** Following method is introduced temperorily here,                ***********************
   ****************** once project details will start coming there will be no need it ***********************/

  public callDAtaikuProjectDetails(projectKey: string, cloudProfile: string) {
    if (this.props.provisionFrom === ProvisionSource.DATAIKU) {
      const tempVar = projectKey;
      ProgressIndicator.show();
      ApiClient.getDataikuProjectDetailsByProjectkey(tempVar, cloudProfile).then(
        (res) => {
          const solution = this.state.solution;
          solution.description.productName = res?.data?.projectName;
          solution.description.description = res?.data?.description ? res?.data?.description : res?.data?.projectName;
          const portfolio: IPortfolio = {
            dnaDataikuProjectId: res?.data?.projectName,
            dnaNotebookId: null,
            dnaSubscriptionAppId: null,
            solutionOnCloud: false,
            usesExistingInternalPlatforms: true,
            platforms: [
              {
                id: null,
                name: ComputeFixedTag.DATAIKU,
              },
            ],
          };

          solution.portfolio = portfolio;
          this.setState({
            solution,
          });
        },
        (err) => {
          this.showErrorNotification('Something went wrong');
        },
      );
    }
  }

  protected modifySolutionDescription = (description: IDescriptionRequest) => {
    const currentSolutionObject = this.state.solution;
    currentSolutionObject.description = description;
    this.setState({
      solution: currentSolutionObject,
    });
  };

  protected showNotification(isPublished: boolean) {
    ProgressIndicator.hide();
    Notification.show('Solution Provisioned Successfully!');
  }

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected saveDescription = () => {
    this.state.solution.openSegments.push('Description', 'Platform');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.solution.publish, 'platform');
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
        team: solution.team,
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
        department: solution.description.department
      },
    };
    ProgressIndicator.show();

    if (this.state.response && this.state.response.data && this.state.response.data.id) {
      data.data.id = this.state.response.data.id;
      ApiClient.updateSolution(data)
        .then((response) => {
          if (response) {
            this.setState(
              {
                response,
              },
              () => {
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
            this.setState(
              {
                response,
              },
              () => {
                this.showNotification(isPublished);
                if (this.props.provisionFrom === ProvisionSource.NOTEBOOK) {
                  this.props.provisionStatus(false);
                  ApiClient.getNotebooksDetails().then((res) => {
                    this.props.provisionedSolutionId(res.solutionId);
                  });
                } else if (this.props.provisionFrom === ProvisionSource.DATAIKU) {
                  this.props.provisionedSolutionId(response.data.id);
                } else if (this.props.provisionFrom === ProvisionSource.MALWARESCANSERVICE) {
                  this.props.provisionedSolutionId(response.data.id);
                }
                // else if (this.props.provisionFromHome) {
                //   document.querySelector('body').style.overflow = 'auto'; //  Quick fix for making overflow since model makes body inline
                //   history.push('/summary/' + response.data.id);
                // }
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

  protected onSaveDraft = (tabToBeSaved: string) => {
    const currentTab = tabToBeSaved;
    this.setState({ saveActionType: 'btn' });
    if (currentTab === 'description') {
      this.saveDescription();
    }
  };
}
