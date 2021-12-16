import cn from 'classnames/bind';
import * as React from 'react';
// @ts-ignore
import Button from '../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Notification from '../../assets/modules/uilab/js/src/notification';
// @ts-ignore

import ProgressIndicator from '../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../assets/modules/uilab/js/src/tooltip';

import countriesdata from '../../globals/maps/countries.json';
import {
  IBarChartDataItem,
  IBreakEvenYearSolutions,
  IDataVolume,
  IDivision,
  // IDivisionFilterPreference,
  IFilterParams,
  // IFilterPreferences,
  ILocation,
  ILocationsMapChartDataItem,
  IPhase,
  IPorfolioSolutionItem,
  IPortfolioSolutionsData,
  IProjectStatus,
  // IScatterChartDataItem,
  IProjectType,
  ISolutionDigitalValue,
  IStackedBarChartDataItem,
  ISubDivisionSolution,
  ITag,
  IUserInfo,
  // IUserPreference,
  // IUserPreferenceRequest,
  IWidgetsResponse,
} from '../../globals/types';
import { history } from '../../router/History';
import { ApiClient } from '../../services/ApiClient';
import { attachEllipsis, DataFormater, trackEvent } from '../../services/utils';
import Styles from './Portfolio.scss';
import BarChartWidget from './widgets/BarChartWidget/BarChartWidget';
// import ScatterChartWidget from './widgets/ScatterChartWidget/ScatterChartWidget';
import StackedBarChartWidget from './widgets/StackedBarChartWidget/StackedBarChartWidget';
import WorldMapBubbleWidget from './widgets/WorldMapBubbleWidget/WorldMapBubbleWidget';

import SolutionsFilter from '../solutionsFilter/SolutionsFilter';

const classNames = cn.bind(Styles);

export interface IFilterType {
  [key: string]: string;
}

export interface IPortfolioState {
  phases: IPhase[];
  divisions: IDivision[];
  subDivisions: ISubDivisionSolution[];
  locations: ILocation[];
  projectStatuses: IProjectStatus[];
  projectTypes: IProjectType[];
  phaseFilterValues: IPhase[];
  divisionFilterValues: IDivision[];
  subDivisionFilterValues: ISubDivisionSolution[];
  locationFilterValues: ILocation[];
  statusFilterValue: IProjectStatus;
  typeFilterValue: IProjectType;
  tagFilterValues: ITag[];
  tagValues: ITag[];
  dataVolumes: IDataVolume[];
  openWidgetPanel: boolean;
  openFilterPanel: boolean;
  widgets: IWidgetsResponse[];
  queryParams: IFilterParams;
  solutions: IPorfolioSolutionItem[];
  solutionsDataKPI: string;
  digitalValueDataKPI: string;
  dnaNotebooksDataKPI: string;
  // digitalValueChartData: IScatterChartDataItem[];
  newDigitalValueChartData: IStackedBarChartDataItem[];
  milestonesChartData: IBarChartDataItem[];
  dataSourcesChartData: IBarChartDataItem[];
  locationsChartData: ILocationsMapChartDataItem[];
  portfolioFirstTimeDataLoaded: boolean;
  portfolioDataFilterApplied: boolean;
  userPreferenceDataId: string;
  tags: string[];
  flagForSubDivision: number;
  getValuesFromFilter: any;
}

export interface IPortfolioProps {
  user: IUserInfo;
}

// const ScatterChartTooltip = ({ active, payload }: any) => {
//   if (active) {
//     const itemObj = payload[0].payload;
//     const commentary =
//       itemObj.descriptionValue.length <= 240
//         ? itemObj.descriptionValue
//         : itemObj.descriptionValue.substring(0, 240) + '...';
//     return (
//       <div className="chart-tooltip">
//         <span className="label">{itemObj.labelValue}</span>
//         <br />
//         <div className="flex-layout">
//           <div>
//             <b>Value</b>
//             <br />
//             <span>{itemObj.yAxisValue} m/€</span>
//           </div>
//           <div>
//             <b>Effort</b>
//             <br />
//             <span>{itemObj.xAxisValue} m/€</span>
//           </div>
//           <div>
//             <b>FTE</b>
//             <br />
//             <span>{itemObj.zAxisValue}</span>
//           </div>
//         </div>
//         <div className="flex-layout">
//           <div>
//             <b>Commentary</b>
//             <p className="tooltip-desc">{commentary}</p>
//           </div>
//         </div>
//       </div>
//     );
//   }

//   return null;
// };

const StackedBarChartTooltip = ({ active, payload }: any) => {
  if (active) {
    const itemObj = payload[0].payload;
    // tslint:disable-next-line: no-string-literal
    const firstSolution = itemObj['firstSolution'] as ISolutionDigitalValue;
    // tslint:disable-next-line: no-string-literal
    const firstBarFillColor = itemObj['firstBarFillColor'] as string;
    // tslint:disable-next-line: no-string-literal
    const secondSolution = itemObj['secondSolution'] as ISolutionDigitalValue;
    // tslint:disable-next-line: no-string-literal
    const secondBarFillColor = itemObj['secondBarFillColor'] as string;
    // tslint:disable-next-line: no-string-literal
    const thirdSolution = itemObj['thirdSolution'] as ISolutionDigitalValue;
    // tslint:disable-next-line: no-string-literal
    const thirdBarFillColor = itemObj['thirdBarFillColor'] as string;
    // tslint:disable-next-line: no-string-literal
    const fourthSolution = itemObj['fourthSolution'] as ISolutionDigitalValue;
    // tslint:disable-next-line: no-string-literal
    const fourthBarFillColor = itemObj['fourthBarFillColor'] as string;
    // tslint:disable-next-line: no-string-literal
    const fifthSolution = itemObj['fifthSolution'] as ISolutionDigitalValue;
    // tslint:disable-next-line: no-string-literal
    const fifthBarFillColor = itemObj['fifthBarFillColor'] as string;
    const colorsArray = [
      firstBarFillColor,
      secondBarFillColor,
      thirdBarFillColor,
      fourthBarFillColor,
      fifthBarFillColor,
    ];
    const solutionLabelLength = 50;

    const solutionsList = [firstSolution, secondSolution, thirdSolution, fourthSolution, fifthSolution].map(
      (item: ISolutionDigitalValue, index: number) => {
        return item ? (
          <span className="value pre">
            <i className="icon legend" style={{ backgroundColor: colorsArray[index] }} />
            {index + 1}. {attachEllipsis(item.productName, solutionLabelLength)} - {DataFormater(item.digitalValue)} €
          </span>
        ) : null;
      },
    );

    return (
      <div className="chart-tooltip">
        <span className="label">{itemObj.labelValue}</span>
        <br />
        {solutionsList.every((element) => element === null) ? 'No Soutions at 100%.' : solutionsList}
      </div>
    );
  }

  return null;
};

const BarChartTooltip = ({ active, payload, label }: any) => {
  if (active) {
    return (
      <div className="chart-tooltip">
        <span className="label">{label}</span>
        <br />
        <span className="value">{payload[0].value}</span>
      </div>
    );
  }

  return null;
};

export default class Portfolio extends React.Component<IPortfolioProps, IPortfolioState> {
  protected tagInput: HTMLInputElement;
  constructor(props: IPortfolioProps) {
    super(props);
    this.state = {
      phases: [],
      divisions: [],
      subDivisions: [],
      locations: [],
      projectStatuses: [],
      projectTypes: [
        { id: '1', name: 'My Bookmarks', routePath: 'bookmarks' },
        { id: '2', name: 'My Solutions', routePath: 'mysolutions' },
      ],
      phaseFilterValues: [],
      divisionFilterValues: [],
      subDivisionFilterValues: [],
      locationFilterValues: [],
      statusFilterValue: null,
      typeFilterValue: null,
      tagValues: [],
      tagFilterValues: [],
      tags: [],
      dataVolumes: [],
      openWidgetPanel: false,
      openFilterPanel: false,
      widgets: [],
      queryParams: {
        phase: [],
        location: [],
        division: [],
        subDivision: [],
        status: [],
        useCaseType: [],
        tag: [],
      },
      solutions: [],
      solutionsDataKPI: '-',
      digitalValueDataKPI: '-',
      dnaNotebooksDataKPI: '-',
      // digitalValueChartData: [],
      newDigitalValueChartData: [],
      milestonesChartData: [],
      dataSourcesChartData: [],
      locationsChartData: [],
      portfolioFirstTimeDataLoaded: false,
      portfolioDataFilterApplied: false,
      userPreferenceDataId: null,
      flagForSubDivision: 0,
      getValuesFromFilter: {},
    };
  }

  public render() {
    const {
      solutionsDataKPI,
      digitalValueDataKPI,
      dnaNotebooksDataKPI,
      newDigitalValueChartData,
      milestonesChartData,
      dataSourcesChartData,
    } = this.state;

    return (
      <div className={Styles.pageWrapper}>
        <div className={classNames(Styles.mainPanel)}>
          <h3>My Portfolio</h3>
          <div className={classNames(Styles.portContentsection)}>
            <div className={classNames(Styles.portHeader)}>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileVal)}>
                  <h5> Digital Value (€) </h5>
                  <span>{digitalValueDataKPI !== '-' ? DataFormater(parseFloat(digitalValueDataKPI)) : '-'}</span>
                </div>
                <div className={classNames(Styles.portNavMore)}>
                  <label className="hide">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label className={classNames(Styles.portNav, 'hide')}>
                    <i className="icon mbc-icon arrow small right" />
                  </label>
                </div>
              </div>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileVal)}>
                  <h5> Solutions Count </h5>
                  <span>{solutionsDataKPI ? solutionsDataKPI : '-'}</span>
                </div>
                <div className={classNames(Styles.portNavMore)}>
                  <label className="hidden">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label className={classNames(Styles.portNav)} onClick={this.onSummaryKpiBtnClick}>
                    <i className="icon mbc-icon arrow small right" />
                  </label>
                </div>
              </div>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileVal)}>
                  <h5>Solutions using DnA Notebook</h5>
                  <span>{dnaNotebooksDataKPI ? dnaNotebooksDataKPI : '-'}</span>
                </div>
                <div className={classNames(Styles.portNavMore)}>
                  <label className="hide">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label className={classNames(Styles.portNav, 'hide')}>
                    <i className="icon mbc-icon arrow small right" />
                  </label>
                </div>
              </div>
            </div>
            <div className={classNames(Styles.portGraphView)}>
              <div className={Styles.portfolioContainer}>
                <div className={Styles.widgetsConatinaer}>
                  <div id="digitalValueWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Digital Value</h1>
                      <span className="sub-title-text">at 100% in €</span>
                    </header>
                    <section>
                      {newDigitalValueChartData.length ? (
                        // <ScatterChartWidget
                        //   data={digitalValueChartData}
                        //   xAxisLabel="Effort"
                        //   xAxisSubLabel="one time costs until run rate m/€"
                        //   yAxisLabel="Value"
                        //   yAxisSubLabel="yearly cash generated run rate in m/€"
                        //   tooltipContentComponent={ScatterChartTooltip}
                        //   onScatterChartBubbleClick={this.onDigitalValueChartBubbleClick}
                        // />
                        <StackedBarChartWidget
                          data={newDigitalValueChartData}
                          xAxisLabel="Year"
                          yAxisLabel="Value"
                          tooltipContentComponent={StackedBarChartTooltip}
                          onChartBarClick={this.onDigitalValueChartBarClick}
                        />
                      ) : (
                        <div className={Styles.noDataMessage}>Data Not Available...</div>
                      )}
                    </section>
                  </div>
                  <div id="milestonesWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Milestones</h1>
                      <span className="sub-title-text">based on End-2-End Process</span>
                    </header>
                    <section>
                      {milestonesChartData.length ? (
                        <BarChartWidget
                          data={milestonesChartData}
                          xAxisLabel="Phase"
                          yAxisLabel="Number"
                          tooltipContentComponent={BarChartTooltip}
                          onChartBarClick={this.onMilestonesChartBarClick}
                        />
                      ) : (
                        <div className={Styles.noDataMessage}>Data Not Available...</div>
                      )}
                    </section>
                  </div>
                </div>
                <div className={classNames(Styles.widgetsConatinaer, Styles.divider)}>
                  <div id="dataSourcesWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Data Sources</h1>
                      <span className="sub-title-text">based on amount of data used.</span>
                    </header>
                    <section>
                      {dataSourcesChartData.length ? (
                        <BarChartWidget
                          data={dataSourcesChartData}
                          xAxisLabel="Data Volume"
                          yAxisLabel="Solutions"
                          tooltipContentComponent={BarChartTooltip}
                          onChartBarClick={this.onDataSourcesChartBarClick}
                        />
                      ) : (
                        <div className={Styles.noDataMessage}>Data Not Available...</div>
                      )}
                    </section>
                  </div>
                  <div id="locationsWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Locations</h1>
                      <span className="sub-title-text">based on development location.</span>
                    </header>
                    <section>
                      <WorldMapBubbleWidget
                        data={this.state.locationsChartData}
                        onLocationMarkerClick={this.onLocationChartMarkerClick}
                      />
                    </section>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <SolutionsFilter
          userId={this.props.user.id}
          getSolutions={(queryParams: IFilterParams) => this.getFilteredSolutions(queryParams)}
          solutionsDataLoaded={this.state.portfolioFirstTimeDataLoaded}
          setSolutionsDataLoaded={(value: boolean) => this.setState({ portfolioFirstTimeDataLoaded: value })}
          getValuesFromFilter={(value: any) => {
            this.setState({ locations: value.locations ? value.locations : [] });
            this.setState({ phases: value.phases ? value.phases : [] });
            this.setState({ projectStatuses: value.projectStatuses ? value.projectStatuses : [] });
            this.setState({ projectTypes: value.projectTypes ? value.projectTypes : [] });
            this.setState((prev) => {
              return {
                getValuesFromFilter: {
                  ...prev.getValuesFromFilter,
                  ...value,
                },
              };
            });
          }}
        />
      </div>
    );
  }

  protected onWidgetIconClick = () => {
    this.setState({ openWidgetPanel: !this.state.openWidgetPanel, openFilterPanel: false });
  };

  protected onFilterIconClick = () => {
    this.setState({ openWidgetPanel: false, openFilterPanel: !this.state.openFilterPanel });
  };

  protected getAllWidgets() {
    ApiClient.getAllWidgets()
      .then((res) => {
        if (res) {
          this.setState(
            {
              widgets: res,
            },
            () => {
              ProgressIndicator.hide();
            },
          );
        }
      })
      .catch((error) => {
        this.setState(
          {
            widgets: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  }

  protected getFilteredSolutions = (queryParams: IFilterParams) => {
    ProgressIndicator.show();
    this.setState(
      {
        queryParams,
        // portfolioDataFilterApplied: true,
      },
      () => {
        this.getSolutions();
        // this.storeFilterValuesInSession();
      },
    );
  };

  protected getSolutions = () => {
    const queryParams = this.state.queryParams;
    let locationIds = queryParams.location.join(',');
    let phaseIds = queryParams.phase.join(',');
    let divisionIds = queryParams.division.join(',');
    // let subDivisionIds = queryParams.subDivision.join(',');
    let status = queryParams.status.join(',');
    let useCaseType = queryParams.useCaseType.join(',');
    const tags = queryParams.tag.join(',');

    if (queryParams.division.length > 0) {
      const distinctSelectedDivisions = queryParams.division;
      const tempArr: any[] = [];
      distinctSelectedDivisions.forEach((item) => {
        const tempString = '{' + item + ',[]}';
        tempArr.push(tempString);
      });
      divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
    }

    if (queryParams.subDivision.length > 0) {
      const distinctSelectedDivisions = queryParams.division;
      const tempArr: any[] = [];
      distinctSelectedDivisions.forEach((item) => {
        const tempSubdiv = queryParams.subDivision.map((value) => {
          const tempArray = value.split('-');
          if (item === tempArray[1]) {
            return tempArray[0];
          }
        });
        let tempString = '';
        // if (tempSubdiv.length === 0) {
        //   tempString += '{' + item + ',[0,null]}';
        // }
        // if (this.state.subDivisions.length === queryParams.subDivision.length && tempSubdiv.length > 0) {
        //   tempString += '{' + item + ',[0,' + tempSubdiv.filter(div => div) + ',null]}';
        // } else {
        //   tempString += '{' + item + ',[' + tempSubdiv.filter(div => div) + ']}';
        // }

        if (tempSubdiv.length === 0) {
          tempString += '{' + item + ',[]}';
        } else {
          tempString += '{' + item + ',[' + tempSubdiv.filter((div) => div) + ']}';
        }
        tempArr.push(tempString);
      });
      divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
    }

    if (queryParams.division.length === 0) {
      divisionIds = '';
    }

    if (queryParams.location.length === this.state.locations.length) {
      locationIds = '';
    }

    if (queryParams.phase.length === this.state.phases.length) {
      phaseIds = '';
    }

    if (queryParams.status.length === this.state.projectStatuses.length) {
      status = '';
    }

    if (queryParams.useCaseType.length === this.state.projectTypes.length) {
      useCaseType = '';
    }

    ApiClient.getSolutionsByGraphQLForPortfolio(locationIds, phaseIds, divisionIds, status, useCaseType, tags)
      .then((res) => {
        if (res) {
          const solutionsData = res.data.solutions as IPortfolioSolutionsData;
          this.setState(
            {
              solutions: solutionsData.totalCount ? solutionsData.records : [],
              solutionsDataKPI: '-',
              digitalValueDataKPI: '-',
              // digitalValueChartData: [],
              newDigitalValueChartData: [],
              milestonesChartData: [],
              dataSourcesChartData: [],
              locationsChartData: [],
              portfolioFirstTimeDataLoaded: true,
              tags: queryParams.tag,
              flagForSubDivision: 0,
            },
            () => {
              if (solutionsData.records && solutionsData.records.length) {
                this.createAndSetPortfolioStateData(solutionsData.records);
              }
              ProgressIndicator.show();
              ProgressIndicator.hide();
            },
          );
        }
      })
      .catch((error) => {
        this.setState(
          {
            solutions: [],
            solutionsDataKPI: '-',
            digitalValueDataKPI: '-',
            // digitalValueChartData: [],
            newDigitalValueChartData: [],
            milestonesChartData: [],
            dataSourcesChartData: [],
            locationsChartData: [],
            portfolioFirstTimeDataLoaded: true,
            flagForSubDivision: 0,
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected getValuesInArray(arrayValue: any) {
    const values: string[] = [];
    Array.from(arrayValue).forEach((item: any) => {
      values.push(item.name);
    });
    return values;
  }
  protected createAndSetPortfolioStateData = (solutions: IPorfolioSolutionItem[]) => {
    const solutionsCount = solutions.length;
    // const digitalValueChartData: IScatterChartDataItem[] = [];
    const newDigitalValueChartData: IStackedBarChartDataItem[] = [];
    const milestonesChartData: IBarChartDataItem[] = [];
    const dataSourcesChartData: IBarChartDataItem[] = [];
    const locationsChartData: ILocationsMapChartDataItem[] = [];
    let totalDigitalValue = 0;
    let dnaNotebooksDataKPI = 0;
    const { phases, locationFilterValues, dataVolumes } = this.state.getValuesFromFilter;
    const currentYear = new Date().getFullYear();
    let startYear = currentYear;
    const tenthYearFromNow = currentYear + 10;
    do {
      const year = startYear.toString();
      newDigitalValueChartData.push({
        id: year,
        labelValue: year,
        firstBarValue: 0,
        firstBarFillColor: '#9DE1FC',
        secondBarValue: 0,
        secondBarFillColor: '#29CBDF',
        thirdBarValue: 0,
        thirdBarFillColor: '#186BB8',
        fourthBarValue: 0,
        fourthBarFillColor: '#2BBBF1',
        fifthBarValue: 0,
        fifthBarFillColor: '#3F84C4',
      });
      startYear++;
    } while (startYear <= tenthYearFromNow);

    const tempBreakEvenYearDigitalValue: IBreakEvenYearSolutions[] = [];

    phases.forEach((phase: IPhase) => {
      let name = phase.name;
      let barFillColor = '#57C9F5';
      switch (phase.id) {
        case '2':
          barFillColor = '#3F84C4';
          break;
        case '3':
          name = 'POC';
          barFillColor = '#2BBBF1';
          break;
        case '4':
          barFillColor = '#186BB8';
          break;
        case '5':
          name = 'Professionaliz.';
          barFillColor = '#29CBDF';
          break;
        case '6':
          name = 'Rollout';
          barFillColor = '#00ADEF';
          break;
        default:
          break;
      }
      milestonesChartData.push({
        id: phase.id,
        labelValue: name,
        barValue: 0,
        barFillColor,
      });
    });

    dataVolumes.forEach((dataVolume: IDataVolume) => {
      const name = dataVolume.name;
      let barFillColor = '#57C9F5';
      switch (dataVolume.id) {
        case '2':
          barFillColor = '#3F84C4';
          break;
        case '3':
          barFillColor = '#2BBBF1';
          break;
        case '4':
          barFillColor = '#186BB8';
          break;
      }
      dataSourcesChartData.push({
        id: dataVolume.id,
        labelValue: name,
        barValue: 0,
        barFillColor,
      });
    });

    solutions.forEach((solution) => {
      // For dnaNotebooksDataKPI
      if (solution.portfolio?.dnaNotebookId) {
        dnaNotebooksDataKPI += 1;
      }

      const digitalValue = solution.digitalValue;
      if (digitalValue) {
        if (digitalValue.digitalEffort === null) {
          digitalValue.digitalEffort = 0;
        }
        if (digitalValue.digitalValue === null) {
          digitalValue.digitalValue = 0;
        }
        totalDigitalValue += digitalValue.digitalValue;
      }

      // if (digitalValue && digitalValue.digitalEffort && digitalValue.digitalValue) {
      //   const valueData = digitalValue.digitalValue;
      //   digitalValueChartData.push({
      //     id: solution.id,
      //     labelValue: solution.productName,
      //     descriptionValue: solution.description,
      //     yAxisValue: valueData,
      //     xAxisValue: digitalValue.digitalEffort,
      //     zAxisValue: solution.team.length,
      //   });
      // }

      if (
        digitalValue &&
        digitalValue.digitalValue &&
        digitalValue.digitalValue > 0 &&
        digitalValue.valueCalculator &&
        digitalValue.valueCalculator.calculatedDigitalValue &&
        digitalValue.valueCalculator.calculatedDigitalValue.year
      ) {
        const breakEvenYear = parseInt(digitalValue.valueCalculator.calculatedDigitalValue.year, 10);
        if (breakEvenYear >= currentYear && breakEvenYear <= tenthYearFromNow) {
          const existedBreakEvenYear = tempBreakEvenYearDigitalValue.find(
            (item) => item.breakEvenYear === breakEvenYear,
          );
          const breakEvenYearSolutions = existedBreakEvenYear || {
            breakEvenYear,
            solutions: [],
          };

          breakEvenYearSolutions.solutions.push({
            id: solution.id,
            productName: solution.productName,
            description: solution.description,
            digitalValue: solution.digitalValue.digitalValue,
          });

          if (!existedBreakEvenYear) {
            tempBreakEvenYearDigitalValue.push(breakEvenYearSolutions);
          }
        }
      }

      const currentPhase = solution.currentPhase;
      if (currentPhase && currentPhase.id) {
        if (milestonesChartData[parseInt(currentPhase.id, 10) - 1]) {
          milestonesChartData[parseInt(currentPhase.id, 10) - 1].barValue += 1;
        }
      }
      const dataSources = solution.dataSources;
      if (dataSources && dataSources.dataVolume && dataSources.dataVolume.id && dataSources.dataVolume.id !== '0') {
        if (dataSourcesChartData[parseInt(dataSources.dataVolume.id, 10) - 1]) {
          dataSourcesChartData[parseInt(dataSources.dataVolume.id, 10) - 1].barValue += 1;
        }
      }

      const locations = solution.locations;
      locations.forEach((location: ILocation) => {
        const hasFilterSelected = locationFilterValues.find((item: ILocation) => {
          return item.id === location.id;
        });
        if (hasFilterSelected) {
          let locationDataItemIndex = -1;
          const locationDataItem = locationsChartData.find((item: ILocationsMapChartDataItem, index: number) => {
            locationDataItemIndex = index;
            return item.countryName === location.name;
          });

          locationDataItemIndex = locationDataItem ? locationDataItemIndex : -1;

          const locationsChartDataItem = locationDataItem
            ? locationDataItem
            : {
                id: location.id,
                countryName: location.name,
                coordinates: this.getCoordinatesByCountry(location.name) as [number, number],
                countValue: 0,
              };

          locationsChartDataItem.countValue += 1;
          if (locationDataItemIndex !== -1) {
            locationsChartData.splice(locationDataItemIndex, 1);
            locationsChartData.splice(locationDataItemIndex, 0, locationsChartDataItem);
          } else {
            locationsChartData.push(locationsChartDataItem);
          }
        }
      });
    });

    tempBreakEvenYearDigitalValue.forEach((item: IBreakEvenYearSolutions) => {
      const breakEvenYearData = newDigitalValueChartData.find(
        (data) => data.labelValue === item.breakEvenYear.toString(),
      );
      const sortedSolutions = item.solutions.sort((a: ISolutionDigitalValue, b: ISolutionDigitalValue) => {
        const key = 'digitalValue' as keyof ISolutionDigitalValue;
        const aValue = a[key] as number;
        const bValue = b[key] as number;
        return bValue - aValue; // Sort by decending
      });
      if (sortedSolutions.length >= 1) {
        const firstSolution = sortedSolutions[0];
        breakEvenYearData.firstBarValue = firstSolution.digitalValue;
        breakEvenYearData.firstSolution = firstSolution;
      }
      if (sortedSolutions.length >= 2) {
        const secondSolution = sortedSolutions[1];
        breakEvenYearData.secondBarValue = secondSolution.digitalValue;
        breakEvenYearData.secondSolution = secondSolution;
      }
      if (sortedSolutions.length >= 3) {
        const thirdSolution = sortedSolutions[2];
        breakEvenYearData.thirdBarValue = thirdSolution.digitalValue;
        breakEvenYearData.thirdSolution = thirdSolution;
      }
      if (sortedSolutions.length >= 4) {
        const fourthSolution = sortedSolutions[3];
        breakEvenYearData.fourthBarValue = fourthSolution.digitalValue;
        breakEvenYearData.fourthSolution = fourthSolution;
      }
      if (sortedSolutions.length >= 5) {
        const fifthSolution = sortedSolutions[4];
        breakEvenYearData.fifthBarValue = fifthSolution.digitalValue;
        breakEvenYearData.fifthSolution = fifthSolution;
      }
    });

    this.setState({
      digitalValueDataKPI: (Math.round((totalDigitalValue + Number.EPSILON) * 100) / 100).toString(),
      solutionsDataKPI: solutionsCount.toString(),
      dnaNotebooksDataKPI: dnaNotebooksDataKPI.toString(),
      // digitalValueChartData,
      newDigitalValueChartData,
      milestonesChartData,
      dataSourcesChartData,
      locationsChartData,
    });
  };

  protected getCoordinatesByCountry = (name: string) => {
    const countryData = countriesdata?.find((countryItem: any) => {
      return countryItem.name.toLowerCase() === name.toLowerCase();
    });

    if (countryData) {
      const coordinates = countryData.latlng;
      return [coordinates[1], coordinates[0]];
    } else {
      return [50, 50];
    }
  };

  protected onSummaryKpiBtnClick = () => {
    if (this.state.solutions.length) {
      trackEvent('Portfolio', 'View Solutions', 'From Solution count KPI');
      sessionStorage.setItem('subDivisionCount', JSON.stringify(this.state.subDivisions.length));
      history.push('/viewsolutions/digitalvalue');
    } else {
      Notification.show('No solutions available to view.', 'alert');
    }
  };

  // protected onDigitalValueChartBubbleClick = (item: any) => {
  //   history.push('/summary/' + item.id);
  // };

  protected onDigitalValueChartBarClick = (item: any) => {
    // Any change in payload datakeys need an update in below code
    const solutionId = item.payload[item.tooltipPayload[0].dataKey.replace('BarValue', 'Solution')].id;
    trackEvent('Portfolio', 'View Solution Summary', 'From Digital Value Chart Bar - ' + solutionId);
    history.push('/summary/' + solutionId);
  };

  protected onMilestonesChartBarClick = (item: any) => {
    this.goToViewSolutionsFor('phase', item);
  };

  protected onDataSourcesChartBarClick = (item: any) => {
    this.goToViewSolutionsFor('datavolume', item);
  };

  protected onLocationChartMarkerClick = (marker: ILocationsMapChartDataItem) => {
    trackEvent('Portfolio', 'View Solutions', 'From Location Chart Map Marker - ' + marker.countryName);
    history.push({
      pathname: `/viewsolutions/location/${marker.id}`,
      state: this.state.getValuesFromFilter,
    });
  };

  protected goToViewSolutionsFor(kpiId: string, item: any) {
    const { phases, dataVolumes } = this.state.getValuesFromFilter;
    if (item) {
      const payload = item.activePayload[0] ? item.activePayload[0].payload : null;
      if (payload && payload.barValue) {
        const payloadId = payload.id;
        let eventFrom = null;
        let value = null;
        switch (kpiId) {
          case 'phase':
            eventFrom = 'From Milestones Chart Bar';
            const phase = phases.find((phase: IPhase) => phase.id === payloadId);
            value = phase.name;
            break;
          case 'datavolume':
            eventFrom = 'From Data Sources Chart Bar';
            const volume = dataVolumes.find((volume: IDataVolume) => volume.id === payloadId);
            value = volume.name;
            break;
        }
        if (eventFrom && value) {
          trackEvent('Portfolio', 'View Solutions', eventFrom + ' - ' + value);
        }
        history.push(`/viewsolutions/${kpiId}/${payloadId}`);
      } else {
        Notification.show('No solutions available to view.', 'alert');
      }
    }
  }
}
