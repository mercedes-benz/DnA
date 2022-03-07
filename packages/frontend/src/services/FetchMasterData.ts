import { IDataVolume, IFilterParams, IPhase, ISubDivisionSolution, ITag } from '../globals/types';
import { ApiClient } from './ApiClient';
import { SESSION_STORAGE_KEYS } from '../globals/constants';
import { getTranslatedLabel } from '../globals/i18n/TranslationsProvider';

export function getDropDownData() {
  return new Promise((resolve, reject) => {
    ApiClient.getFiltersMasterData()
      .then((response) => {
        let portfolioFilterValues: IFilterParams;
        if (response) {
          portfolioFilterValues = JSON.parse(
            sessionStorage.getItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES),
          ) as IFilterParams;
          const locations = response[0];
          const divisions = response[1];
          const divisionsToPass =
            portfolioFilterValues && portfolioFilterValues.division.length > 0
              ? divisions.filter((element: any) => portfolioFilterValues.division.includes(element.id))
              : divisions;
          ApiClient.getSubDivisionsData(divisionsToPass)
            .then((subDivisionsList) => {
              const projectStatuses = response[2];
              const phases: IPhase[] = response[3];
              const tagValues: ITag[] = response[5];
              const subDivisions: ISubDivisionSolution[] = [].concat(...subDivisionsList);
              phases.forEach((phase) => {
                phase.name = getTranslatedLabel(phase.name);
              });

              const dataVolumes: IDataVolume[] = response[4];

              // let newQueryParams = queryParams;
              // if (portfolioFilterValues) {
              //   newQueryParams = portfolioFilterValues;
              //   if (newQueryParams.status.includes('0')) {
              //     newQueryParams.status = [];
              //   }
              //   if (newQueryParams.useCaseType.includes('0')) {
              //     newQueryParams.useCaseType = [];
              //   }
              //   // Solved the issue on tag if user has already values in session
              //   if (!newQueryParams.tag) {
              //     newQueryParams.tag = [];
              //   }
              //   const selectedValues: ITag[] = [];
              //   newQueryParams.tag.forEach((a: any) => {
              //     const tag: ITag = { id: null, name: null };
              //     tag.id = a;
              //     tag.name = a;
              //     selectedValues.push(tag);
              //   });
              //   // this.setState({tags: newQueryParams.tag});
              // } else {
              //   newQueryParams.phase = phases.map((phase: IPhase) => {
              //     return phase.id;
              //   });
              //   newQueryParams.division = divisions.map((division: IDivision) => {
              //     return division.id;
              //   });
              //   newQueryParams.subDivision = subDivisions.map((subDivision: ISubDivisionSolution) => {
              //     return subDivision.id;
              //   });
              //   newQueryParams.location = locations.map((location: ILocation) => {
              //     return location.id;
              //   });
              //   newQueryParams.status = [];
              //   newQueryParams.useCaseType = [];
              //   newQueryParams.tag = [];
              // }

              // this.setState({locations, divisions, subDivisions, projectStatuses, phases, dataVolumes, tagValues});
              resolve({ locations, divisions, subDivisions, projectStatuses, phases, dataVolumes, tagValues });
            })
            .catch((error: Error) => {
              // this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
              reject(error.message ? error.message : 'Some Error Occured');
            });
        }
      })
      .catch((error: Error) => {
        // this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        reject(error.message ? error.message : 'Some Error Occured');
      });
  });
}
