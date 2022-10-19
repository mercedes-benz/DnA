import { Data } from 'react-csv/components/CommonPropTypes';
import { IAllSolutionsResultCSV, IDataSources, IFilterParams, IPhasesItem, IRolloutDetail } from '../globals/types';
import { ApiClient } from './ApiClient';
import { Envs } from '../globals/Envs';
import { getDivisionsQueryValue, 
  regionalDateAndTimeConversionSolution,
  regionalForMonthAndYear 
} from './utils';

export const getDataForCSV = (
  queryParams: IFilterParams,
  numberOfSelectedLocations: number,
  numberOfSelectedPhases: number,
  numberOfSelectedStatuses: number,
  numberOfSelectedProjectTypes: number,
  sortByField: string,
  sortType: string,
  enablePortfolioSolutionsView: boolean,
  onDataSuccess: (csvData: Data, csvHeader: Data) => void,
  searchKey?: string,
) => {
  const solutionsCSVData: string | Data = [];

  const csvHeaders: string | Data = [
    { label: 'Name', key: 'name' },
    { label: 'Phase', key: 'phase' },
    { label: 'Description', key: 'description' },
    { label: 'Tags', key: 'tags' },
    { label: 'Division', key: 'division' },
    { label: 'Subdivision', key: 'subdivision' },
    { label: 'RelatedProducts', key: 'relatedProducts' },
    { label: 'BusinessGoals', key: 'businessGoal' },
    { label: 'Status', key: 'status' },
    { label: 'ReasonForHoldOrClose', key: 'reasonForHoldOrClose' },
    { label: 'AttachedFiles', key: 'attachedFiles' },
    { label: 'Bookmarked', key: 'bookmarked' },
    { label: 'Location', key: 'location' },
    { label: 'Expected Benefits', key: 'expectedBenefits' },
    { label: 'Business Need', key: 'businessNeed' },
    { label: 'Register support of additional resources', key: 'additionalResource' },
    { label: 'Needed Roles/Skills(with FTE count)', key: 'neededRoles' },
    { label: 'Data Strategy Domain', key: 'dataStrategyDomain' },
    { label: 'Team', key: 'team' },
    { label: 'Milestones-Kick-off', key: 'kickoff' },
    { label: 'Milestones-Ideation', key: 'ideation' },
    { label: 'Milestones-Concept Development / Proof of concept', key: 'conceptDevelopment' },
    { label: 'Milestones-Pilot', key: 'pilot' },
    { label: 'Milestones-Professionalization', key: 'professionalization' },
    { label: 'Rollout Locations', key: 'rolloutLocations' },
    { label: 'DataSources', key: 'dataSources' },
    { label: 'TotalDataVolume', key: 'totalDataVolume' },
    { label: 'SolutionOnCloud ', key: 'solutionOnCloud' },
    { label: 'UsageOf' + Envs.DNA_COMPANY_NAME, key: 'usageOfInternal' },
    { label: 'Platform', key: 'platform' },
    { label: 'Languages', key: 'languages' },
    { label: 'ModelsAlgorithms', key: 'modelsAlgorithms' },
    { label: 'Visualization', key: 'visualization' },
    { label: 'GitRepository', key: 'gitRepository' },
    { label: 'Results', key: 'results' },
    { label: 'Comment', key: 'comment' },
    { label: 'IsPublished', key: 'publish' },
    { label: 'DataComplianceAddedLinks', key: 'dataComplianceAddedLinks' },
    { label: 'DataComplianceAttachedFiles', key: 'dataComplianceAttachedFiles' },
    { label: 'DataComplianceLocalComplianceOfficers', key: 'dataComplianceLocalComplianceOfficers' },
    { label: 'DataComplianceQuickCheck', key: 'dataComplianceQuickCheck' },
    { label: 'DataComplianceUseCaseDescAndEval', key: 'dataComplianceUseCaseDescAndEval' },
    { label: 'DataComplianceExpertGuidelineNeeded', key: 'dataComplianceExpertGuidelineNeeded' },
    { label: 'DataComplianceReadyForImplementation', key: 'dataComplianceReadyForImplementation' },
    { label: 'MaturityLevel', key: 'maturityLevel' },
    { label: 'Controller', key: 'controller' },
    { label: 'CalculatedDigitalValue', key: 'calculatedDigitalValue' },
    { label: 'CostFactorSummary', key: 'costFactorSummary' },
    { label: 'ValueFactorSummary', key: 'valueFactorSummary' },
    { label: 'BreakEvenPoint', key: 'breakEvenPoint' },
    { label: 'BenefitRealizationRisk', key: 'benefitRealizationRisk' },
    { label: 'CommentOnBenefitRealizationRisk', key: 'commentOnBenefitRealizationRisk' },
    { label: 'CommentOnStrategicRelevance', key: 'commentOnStrategicRelevance' },
    { label: 'StrategicRelevance', key: 'strategicRelevance' },
    { label: 'DigitalValueAttachedFiles', key: 'digitalValueAttachedFiles' },
    { label: 'DigitalValuePermissions', key: 'digitalValuePermissions' },
    { label: 'CreatedBy', key: 'createdBy' },
    { label: 'CreatedDate', key: 'createdDate' },
    { label: 'LastModifiedDate', key: 'lastModifiedDate' },
  ];

  const csvHeadersForFoss: string | Data = [
    { label: 'Name', key: 'name' },
    { label: 'Phase', key: 'phase' },
    { label: 'Description', key: 'description' },
    { label: 'Tags', key: 'tags' },
    { label: 'Division', key: 'division' },
    { label: 'Subdivision', key: 'subdivision' },
    { label: 'RelatedProducts', key: 'relatedProducts' },
    { label: 'BusinessGoals', key: 'businessGoal' },
    { label: 'Status', key: 'status' },
    { label: 'ReasonForHoldOrClose', key: 'reasonForHoldOrClose' },
    { label: 'AttachedFiles', key: 'attachedFiles' },
    { label: 'Bookmarked', key: 'bookmarked' },
    { label: 'Location', key: 'location' },
    { label: 'Expected Benefits', key: 'expectedBenefits' },
    { label: 'Business Need', key: 'businessNeed' },
    { label: 'Register support of additional resources', key: 'additionalResource' },
    { label: 'Needed Roles/Skills(with FTE count)', key: 'neededRoles' },
    { label: 'Data Strategy Domain', key: 'dataStrategyDomain' },
    { label: 'Team', key: 'team' },
    { label: 'Milestones-Kick-off', key: 'kickoff' },
    { label: 'Milestones-Ideation', key: 'ideation' },
    { label: 'Milestones-Concept Development / Proof of concept', key: 'conceptDevelopment' },
    { label: 'Milestones-Pilot', key: 'pilot' },
    { label: 'Milestones-Professionalization', key: 'professionalization' },
    { label: 'Rollout Locations', key: 'rolloutLocations' },
    { label: 'DataSources', key: 'dataSources' },
    { label: 'TotalDataVolume', key: 'totalDataVolume' },
    { label: 'SolutionOnCloud ', key: 'solutionOnCloud' },
    { label: 'UsageOf' + Envs.DNA_COMPANY_NAME, key: 'usageOfInternal' },
    { label: 'Platform', key: 'platform' },
    { label: 'Languages', key: 'languages' },
    { label: 'ModelsAlgorithms', key: 'modelsAlgorithms' },
    { label: 'Visualization', key: 'visualization' },
    { label: 'GitRepository', key: 'gitRepository' },
    { label: 'Results', key: 'results' },
    { label: 'Comment', key: 'comment' },
    { label: 'IsPublished', key: 'publish' },
    { label: 'MaturityLevel', key: 'maturityLevel' },
    { label: 'Controller', key: 'controller' },
    { label: 'CalculatedDigitalValue', key: 'calculatedDigitalValue' },
    { label: 'CostFactorSummary', key: 'costFactorSummary' },
    { label: 'ValueFactorSummary', key: 'valueFactorSummary' },
    { label: 'BreakEvenPoint', key: 'breakEvenPoint' },
    { label: 'BenefitRealizationRisk', key: 'benefitRealizationRisk' },
    { label: 'CommentOnBenefitRealizationRisk', key: 'commentOnBenefitRealizationRisk' },
    { label: 'CommentOnStrategicRelevance', key: 'commentOnStrategicRelevance' },
    { label: 'StrategicRelevance', key: 'strategicRelevance' },
    { label: 'DigitalValueAttachedFiles', key: 'digitalValueAttachedFiles' },
    { label: 'DigitalValuePermissions', key: 'digitalValuePermissions' },
    { label: 'CreatedBy', key: 'createdBy' },
    { label: 'CreatedDate', key: 'createdDate' },
    { label: 'LastModifiedDate', key: 'lastModifiedDate' },
  ];

  let locationIds = queryParams.location.join(',');
  let phaseIds = queryParams.phase.join(',');
  const divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);
  let status = queryParams.status.join(',');
  let useCaseType = queryParams.useCaseType.join(',');
  const dataVolumes = enablePortfolioSolutionsView ? queryParams.dataVolume ? queryParams.dataVolume.join(',') : '' : '';
  const tags = queryParams.tag.join(',');
  if (queryParams.location.length === numberOfSelectedLocations) {
    locationIds = '';
  }
  if (queryParams.phase.length === numberOfSelectedPhases) {
    phaseIds = '';
  }
  if (queryParams.status.length === numberOfSelectedStatuses) {
    status = '';
  }

  if (queryParams.useCaseType.length === numberOfSelectedProjectTypes) {
    useCaseType = '';
  }

  let dataSourcesList:any = [];

  ApiClient.getDataSources()
    .then((res) => {
      dataSourcesList = [...res];
    })
    .catch((error) => {
      console.log(error.message);
    });

  const isDigitalValueContributionEnabled = window.location.href.indexOf('digitalvaluecontribution') !== -1;
  const isNotificationEnabled = window.location.href.indexOf('notebook') !== -1;
  ApiClient.exportDatatoCSV(
    locationIds,
    phaseIds,
    divisionIds,
    status,
    useCaseType,
    dataVolumes,
    tags,
    sortByField,
    sortType,
    enablePortfolioSolutionsView,
    searchKey,
    isDigitalValueContributionEnabled,
    isNotificationEnabled
  ).then((resCSV) => {
    if (resCSV) {
      const solutionsCSV = resCSV.data
        ? (resCSV.data.solutions as IAllSolutionsResultCSV)
        : { records: [], totalCount: 0 };
      if (solutionsCSV.records) {
        solutionsCSV.records.forEach((solution) => {
          const dataComplianceOBJ = Envs.ENABLE_DATA_COMPLIANCE ? {
            dataComplianceAddedLinks:
              solution.dataCompliance && solution.dataCompliance.links && solution.dataCompliance.links.length > 0
                ? sanitize(solution.dataCompliance.links.map((link) => link.link).join('|'))
                : 'NA',
            dataComplianceAttachedFiles:
              solution.dataCompliance &&
              solution.dataCompliance.attachments &&
              solution.dataCompliance.attachments.length > 0
                ? solution.dataCompliance.attachments.map((attachment) => attachment.fileName).join('|')
                : 'NA',
            dataComplianceLocalComplianceOfficers:
              solution.dataCompliance &&
              solution.dataCompliance.complianceOfficers &&
              solution.dataCompliance.complianceOfficers.length > 0
                ? solution.dataCompliance.complianceOfficers.map((team) => team.shortId).join('|')
                : 'NA',
            dataComplianceQuickCheck: solution.dataCompliance
              ? solution.dataCompliance.quickCheck
                ? 'Yes'
                : 'No'
              : 'NA',
            dataComplianceUseCaseDescAndEval: solution.dataCompliance
              ? solution.dataCompliance.useCaseDescAndEval
                ? 'Yes'
                : 'No'
              : 'NA',
            dataComplianceExpertGuidelineNeeded: solution.dataCompliance
              ? solution.dataCompliance.expertGuidelineNeeded
                ? 'Yes'
                : 'No'
              : 'NA',
            dataComplianceReadyForImplementation: solution.dataCompliance
              ? solution.dataCompliance.readyForImplementation
                ? 'Yes'
                : 'No'
              : 'NA',
          } : {};

          solutionsCSVData.push({
            name: solution.productName ? sanitize(solution.productName) : 'NA',
            phase: solution.currentPhase.name ? solution.currentPhase.name : 'NA',
            description: solution.description ? sanitize(solution.description) : 'NA',
            tags: solution.tags && solution.tags.length > 0 ? sanitize(solution.tags.join('|')) : 'NA',
            division: solution.division?.name ? solution.division.name : 'NA',
            subdivision: solution.division?.subdivision ? solution.division.subdivision.name : 'NA',
            relatedProducts:
              solution.relatedProducts && solution.relatedProducts.length > 0
                ? sanitize(solution.relatedProducts.join('|'))
                : 'NA',
            businessGoal:
              solution.businessGoals && solution.businessGoals.length > 0
                ? sanitize(solution.businessGoals.join('|'))
                : 'NA',
            status: solution.projectStatus.name ? solution.projectStatus.name : 'NA',
            attachedFiles:
              solution.attachments && solution.attachments.length > 0
                ? solution.attachments.map((attachment) => attachment.fileName).join('|')
                : 'NA',
            bookmarked: solution.bookmarked ? 'Yes' : 'No',
            location:
              solution.locations && solution.locations.length > 0
                ? solution.locations.map((location) => location.name).join('|')
                : 'NA',
            expectedBenefits: solution.expectedBenefits ? sanitize(solution.expectedBenefits) : 'NA',
            businessNeed: solution.businessNeed ? sanitize(solution.businessNeed) : 'NA',
            additionalResource: solution.additionalResource ? solution.additionalResource : 'NA',
            neededRoles:
              solution.skills && solution.skills.length > 0
                ? solution.skills
                    .map((item) =>{
                      if(Number(item.requestedFTECount) > 0)
                      return item.neededSkill + '(' + item.requestedFTECount + ')';
                    })
                    .join('|')
                : 'NA',
            dataStrategyDomain: solution.dataStrategyDomain ? sanitize(solution.dataStrategyDomain) : 'NA',
            team:
              solution.team && solution.team.length > 0
                ? solution.team.map((member) => member.shortId).join('|')
                : 'NA',
            kickoff: 
              solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0 
                ? setMilestonesPhases(solution.milestones.phases, 1)
                : 'NA',
            ideation: 
              solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0 
                ? setMilestonesPhases(solution.milestones.phases, 2)
                : 'NA',   
            conceptDevelopment:
              solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0 
                ? setMilestonesPhases(solution.milestones.phases, 3)
                : 'NA',   
            pilot:
              solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0 
                ? setMilestonesPhases(solution.milestones.phases, 4)
                : 'NA',  
            professionalization:
              solution.milestones && solution.milestones.phases && solution.milestones.phases.length > 0 
                ? setMilestonesPhases(solution.milestones.phases, 5)
                : 'NA',        
            rolloutLocations: 
              solution.milestones && solution.milestones.rollouts && solution.milestones.rollouts.details.length > 0 
                ? setRolloutLocations(solution.milestones.rollouts.details)
                : 'NA',       
            dataSources:
              solution.dataSources && solution.dataSources.dataSources && solution.dataSources.dataSources.length > 0
                ? setDataSources(solution.dataSources.dataSources, dataSourcesList)
                : 'NA',
            totalDataVolume:
              solution.dataSources && solution.dataSources.dataVolume ? solution.dataSources.dataVolume.name : 'NA',
            solutionOnCloud: solution.portfolio ? (solution.portfolio.solutionOnCloud ? 'Yes' : 'No') : 'NA',
            usageOfInternal: solution.portfolio
              ? solution.portfolio.usesExistingInternalPlatforms
                ? 'Yes'
                : 'No'
              : 'NA',
            platform:
              solution.portfolio && solution.portfolio.platforms && solution.portfolio.platforms.length > 0
                ? sanitize(solution.portfolio.platforms.map((platform) => platform.name).join('|'))
                : 'NA',
            languages:
              solution.analytics && solution.analytics.languages && solution.analytics.languages.length > 0
                ? sanitize(solution.analytics.languages.map((languages) => languages.name).join('|'))
                : 'NA',
            modelsAlgorithms:
              solution.analytics && solution.analytics.algorithms && solution.analytics.algorithms.length > 0
                ? sanitize(solution.analytics.algorithms.map((algorithm) => algorithm.name).join('|'))
                : 'NA',
            visualization:
              solution.analytics && solution.analytics.visualizations && solution.analytics.visualizations.length > 0
                ? sanitize(solution.analytics.visualizations.map((visualization) => visualization.name).join('|'))
                : 'NA',
            gitRepository: solution.sharing ? solution.sharing.gitUrl : 'NA',
            results: solution.sharing && solution.sharing.result ? sanitize(solution.sharing.result.name) : 'NA',
            comment: solution.sharing ? sanitize(solution.sharing.resultUrl) : 'NA',
            publish: solution.publish ? 'Yes' : 'No',
            reasonForHoldOrClose: solution.reasonForHoldOrClose ? sanitize(solution.reasonForHoldOrClose) : 'NA',
            ...dataComplianceOBJ,
            maturityLevel: solution.digitalValue ? solution.digitalValue.maturityLevel : 'NA',
            controller:
              solution.digitalValue &&
              solution.digitalValue.projectControllers &&
              solution.digitalValue.projectControllers.length > 0
                ? solution.digitalValue.projectControllers
                    .map((projectController) => projectController.shortId)
                    .join('|')
                : 'NA',
            calculatedDigitalValue:
              solution.digitalValue &&
              solution.digitalValue.valueCalculator &&
              solution.digitalValue.valueCalculator.calculatedDigitalValue
                ? 'valueAt: ' +
                  solution.digitalValue.valueCalculator.calculatedDigitalValue.valueAt +
                  '|year: ' +
                  solution.digitalValue.valueCalculator.calculatedDigitalValue.year +
                  '|value: ' +
                  solution.digitalValue.valueCalculator.calculatedDigitalValue.value +
                  '€'
                : 'NA',
            costFactorSummary:
              solution.digitalValue &&
              solution.digitalValue.valueCalculator &&
              solution.digitalValue.valueCalculator.costFactorSummary
                ? 'year: ' +
                  solution.digitalValue.valueCalculator.costFactorSummary.year +
                  '|value: ' +
                  solution.digitalValue.valueCalculator.costFactorSummary.value +
                  '€'
                : 'NA',
            valueFactorSummary:
              solution.digitalValue &&
              solution.digitalValue.valueCalculator &&
              solution.digitalValue.valueCalculator.valueFactorSummary
                ? 'year: ' +
                  solution.digitalValue.valueCalculator.valueFactorSummary.year +
                  '|value: ' +
                  solution.digitalValue.valueCalculator.valueFactorSummary.value +
                  '€'
                : 'NA',
            breakEvenPoint:
              solution.digitalValue &&
              solution.digitalValue.valueCalculator &&
              solution.digitalValue.valueCalculator.breakEvenPoint
                ? solution.digitalValue.valueCalculator.breakEvenPoint
                : 'NA',
            benefitRealizationRisk:
              solution.digitalValue && solution.digitalValue.assessment
                ? solution.digitalValue.assessment.benefitRealizationRisk
                : 'NA',
            commentOnBenefitRealizationRisk:
              solution.digitalValue && solution.digitalValue.assessment
                ? solution.digitalValue.assessment.commentOnBenefitRealizationRisk !== null
                  ? sanitize(solution.digitalValue.assessment.commentOnBenefitRealizationRisk)
                  : 'NA'
                : 'NA',
            commentOnStrategicRelevance:
              solution.digitalValue && solution.digitalValue.assessment
                ? solution.digitalValue.assessment.commentOnStrategicRelevance !== null
                  ? sanitize(solution.digitalValue.assessment.commentOnStrategicRelevance)
                  : 'NA'
                : 'NA',
            strategicRelevance:
              solution.digitalValue && solution.digitalValue.assessment
                ? solution.digitalValue.assessment.strategicRelevance
                : 'NA',
            digitalValueAttachedFiles:
              solution.digitalValue &&
              solution.digitalValue.attachments &&
              solution.digitalValue.attachments.length > 0
                ? solution.digitalValue.attachments.map((attachment) => attachment.fileName).join('|')
                : 'NA',
            digitalValuePermissions:
              solution.digitalValue &&
              solution.digitalValue.permissions &&
              solution.digitalValue.permissions.length > 0
                ? solution.digitalValue.permissions.map((team) => team.shortId).join('|')
                : 'NA',
            createdBy: solution.createdBy ? solution.createdBy.id : 'NA',
            createdDate: solution.createdDate ? regionalDateAndTimeConversionSolution(solution.createdDate) : 'NA',
            lastModifiedDate: solution.lastModifiedDate ? regionalDateAndTimeConversionSolution(solution.lastModifiedDate) : 'NA',
            // createdDate: solution.createdDate ? solution.createdDate : 'NA',
            // lastModifiedDate: solution.lastModifiedDate ? solution.lastModifiedDate : 'NA',
          });
        });
      }
      onDataSuccess(solutionsCSVData, Envs.ENABLE_DATA_COMPLIANCE ? csvHeaders : csvHeadersForFoss);
    }
  });
};

export const sanitize = (text: string) => {
  return text.replace(/"/g, '""');
};

export const setDataSources = (dataSources: IDataSources[], dsList: any) => {
  const stringValsArr = dataSources.map((item: any) => { 
    let dsBadge:any = Envs.DNA_APPNAME_HEADER;
    if(dsList.length > 0) {
      const dataSource = dsList.filter((ds:any) => ds.name === item.dataSource);
      if(dataSource.length === 1) {
        if(dataSource[0].source !== null && dataSource[0].dataType !== null) {
          if(dataSource[0].dataType !== undefined && dataSource[0].source !== undefined) {
            if(dataSource[0].dataType === "Not set") {
              dsBadge = dataSource[0].source;
            } else {
              dsBadge = dataSource[0].source + '-' + dataSource[0].dataType.charAt(0).toUpperCase() + dataSource[0].dataType.slice(1);
            }
          }
        }
      }
      return (item.dataSource + ' (' + dsBadge + (item.weightage !== 0 ? (' - ' + item.weightage + '%') : '') + ')');
    }
    return (item.dataSource + (item.weightage !== 0 ? ' (' + item.weightage + '%)' : ''));
  });
  const dataValues = stringValsArr.join('|');
  return dataValues;
};

export const setMilestonesPhases = (phases: IPhasesItem[], phaseId: number) => {
  const temVar = phases.filter((phase: IPhasesItem) => Number(phase.phase.id) === phaseId);
  const dataValues = temVar[0] ? (temVar[0].month > 0 && temVar[0].year > 0) || temVar[0].description.length > 0? sanitize( (temVar[0].month > 0 && temVar[0].year > 0 ? regionalForMonthAndYear(temVar[0].month+'/'+'01'+'/'+temVar[0].year):'') + '|' + temVar[0].description) : '' : '';
  return dataValues;
};

export const setRolloutLocations = (locations: IRolloutDetail[]) => {
  const stringValsArr = locations.map((location: IRolloutDetail) => location.location.name + '(' + (location.month > 0 && location.year > 0 ? regionalForMonthAndYear(location.month+'/'+'01'+'/'+location.year):'') + ')');
  const dataValues = stringValsArr.join('|');
  return dataValues;
};
