import cn from 'classnames/bind';
import * as React from 'react';
// @ts-ignore
import Button from '../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Notification from '../../assets/modules/uilab/js/src/notification';
// @ts-ignore

import { CSVLink } from 'react-csv';
import { Data } from 'react-csv/components/CommonPropTypes';
import { getDataForCSV } from '../../services/SolutionsCSV';
import { csvSeparator } from '../../services/utils';
import ProgressIndicator from '../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../assets/modules/uilab/js/src/tooltip';

import countriesdata from 'globals/maps/countries.json';
import {
  IBarChartDataItem,
  IFilterParams,
  ILocationsMapChartDataItem,
  IPhase,
  ISolutionDigitalValue,
  IStackedBarChartDataItem,
  ITag,
  IUserInfo,
  IWidgetsResponse,
} from 'globals/types';
import { history } from '../../router/History';
import { ApiClient } from '../../services/ApiClient';
import { attachEllipsis, DataFormater, trackEvent } from '../../services/utils';
import Styles from './Portfolio.scss';
import BarChartWidget from './widgets/BarChartWidget/BarChartWidget';
// import ScatterChartWidget from './widgets/ScatterChartWidget/ScatterChartWidget';
import StackedBarChartWidget from './widgets/StackedBarChartWidget/StackedBarChartWidget';
import WorldMapBubbleWidget from './widgets/WorldMapBubbleWidget/WorldMapBubbleWidget';
// import { SESSION_STORAGE_KEYS } from '../../globals/constants';

import SolutionsFilter from './filters/SolutionsFilter';
import filterStyle from './filters/Filter.scss';
// import {getDropDownData} from '../../services/FetchMasterData';

const classNames = cn.bind(Styles);

export interface IFilterType {
  [key: string]: string;
}

export interface IPortfolioState {
  openWidgetPanel: boolean;
  openFilterPanel: boolean;
  widgets: IWidgetsResponse[];
  queryParams: IFilterParams;
  totalSolutionCounts: number;
  solutionsDataKPI: string;
  digitalValueDataKPI: string;
  dataValueDataSavingsKPI: string;
  dataValueDataRevenueKPI: string;
  dnaNotebooksDataKPI: string;
  solutionsDataKPILoader: boolean;
  digitalValueDataKPILoader: boolean;
  dataValueDataKPILoader: boolean;
  dnaNotebooksDataKPILoader: boolean;
  // digitalValueChartData: IScatterChartDataItem[];
  newDigitalValueChartData: IStackedBarChartDataItem[];
  newDataValueChartData: IStackedBarChartDataItem[];
  milestonesChartData: IBarChartDataItem[];
  dataSourcesChartData: IBarChartDataItem[];
  locationsChartData: ILocationsMapChartDataItem[];
  portfolioFirstTimeDataLoaded: boolean;
  portfolioDataFilterApplied: boolean;
  showDigitalValueLoader: boolean;
  showDataValueLoader: boolean;
  showMilestoneLoader: boolean;
  showDataSourceLoader: boolean;
  showLocationLoader: boolean;
  openFilters: boolean;
  tagValues: ITag[];
  tagFilterValues: ITag[];
  selectedTags: string[];
  selectedTagsToPass: string[];
  csvData: any[];
  csvHeader: any[];
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
const StackedBarChartTooltipDataValue = ({ active, payload }: any) => {
  if (active) {
    const itemObj = payload[0].payload;
    const savings = DataFormater(
      parseFloat((Math.round((itemObj['firstBarValue'] + Number.EPSILON) * 100) / 100).toString()),
    );
    const revenue = DataFormater(
      parseFloat((Math.round((itemObj['secondBarValue'] + Number.EPSILON) * 100) / 100).toString()),
    );
    return (
      <div className="chart-tooltip">
        <span className="label">
          <i className="icon legend" style={{ backgroundColor: itemObj['firstBarFillColor'] }} />
          Savings: {savings}
        </span>
        <br />
        <span className="label">
          <i className="icon legend" style={{ backgroundColor: itemObj['secondBarFillColor'] }} />
          Revenue: {revenue}
        </span>
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
        <span className="value">{new Intl.NumberFormat(navigator.language).format(Number(payload[0].value))}</span>
      </div>
    );
  }

  return null;
};

export default class Portfolio extends React.Component<IPortfolioProps, IPortfolioState> {
  protected tagInput: HTMLInputElement;
  protected csvLink: any = React.createRef();
  constructor(props: IPortfolioProps) {
    super(props);
    this.state = {
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
      totalSolutionCounts: 0,
      solutionsDataKPI: '-',
      digitalValueDataKPI: '-',
      dataValueDataSavingsKPI: '-',
      dataValueDataRevenueKPI: '-',
      dnaNotebooksDataKPI: '-',
      solutionsDataKPILoader: false,
      digitalValueDataKPILoader: false,
      dataValueDataKPILoader: false,
      dnaNotebooksDataKPILoader: false,
      // digitalValueChartData: [],
      newDigitalValueChartData: [],
      newDataValueChartData: [],
      milestonesChartData: [],
      dataSourcesChartData: [],
      locationsChartData: [],
      portfolioFirstTimeDataLoaded: false,
      portfolioDataFilterApplied: false,
      showDigitalValueLoader: true,
      showDataValueLoader: true,
      showMilestoneLoader: true,
      showDataSourceLoader: true,
      showLocationLoader: true,
      openFilters: false,
      tagValues: [],
      tagFilterValues: [],
      selectedTags: [],
      selectedTagsToPass: [],
      csvData: [],
      csvHeader: [],
    };
  }

  public getWidgetData(
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    tagSearch: string,
  ) {
    ProgressIndicator.hide();
    this.setState({
      solutionsDataKPI: '-',
      digitalValueDataKPI: '-',
      dataValueDataSavingsKPI: '-',
      dataValueDataRevenueKPI: '-',
      dnaNotebooksDataKPI: '-',
      solutionsDataKPILoader: true,
      digitalValueDataKPILoader: true,
      dataValueDataKPILoader: true,
      dnaNotebooksDataKPILoader: true,
      dataSourcesChartData: [],
      showDataSourceLoader: true,
      milestonesChartData: [],
      showMilestoneLoader: true,
      newDigitalValueChartData: [],
      newDataValueChartData: [],
      showDigitalValueLoader: true,
      showDataValueLoader: true,
      locationsChartData: [],
      showLocationLoader: true,
    });

    ApiClient.getDashboardData('datasources', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const dataSourcesChartData: IBarChartDataItem[] = [];
        res.dataSources.forEach((item: any) => {
          if (item.dataVolume.name !== 'Choose' && item.dataVolume.name !== null) {
            const dataVolume = item.dataVolume;
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
              barValue: item.solutionCount,
              barFillColor,
            });
          }
        });
        this.setState({ dataSourcesChartData, showDataSourceLoader: false });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ dataSourcesChartData: [], showDataSourceLoader: false });
      });

    ApiClient.getDashboardData('locations', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const locationsChartData: ILocationsMapChartDataItem[] = [];
        res.locations.forEach((item: any) => {
          const location = item.location;
          locationsChartData.push({
            id: location.id,
            countryName: location.name,
            coordinates: this.getCoordinatesByCountry(location.name) as [number, number],
            countValue: item.solutionCount,
          });
        });
        this.setState({ locationsChartData, showLocationLoader: false });
      })
      .catch((error: any) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ locationsChartData: [], showLocationLoader: false });
      });
    ApiClient.getDashboardData('milestones', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const milestonesChartData: IBarChartDataItem[] = [];
        res.milestones.forEach((item: any) => {
          const phase: IPhase = item.phase;
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
            barValue: item.solutionCount,
            barFillColor,
          });
        });
        this.setState({ milestonesChartData, showMilestoneLoader: false });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ milestonesChartData: [], showMilestoneLoader: false });
      });
    ApiClient.getDashboardData('digitalvalue', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const totalDigitalValue = res.totalDigitalValue;
        this.setState({
          digitalValueDataKPI: (Math.round((totalDigitalValue + Number.EPSILON) * 100) / 100).toString(),
        });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    ApiClient.getDashboardData('solutioncount', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const totalSolutionCount = res.totalCount;
        this.setState({ solutionsDataKPI: totalSolutionCount.toString(), totalSolutionCounts: totalSolutionCount });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    ApiClient.getDashboardData('notebook/solutioncount', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const totalSolutionCount = res.totalCount;
        this.setState({ dnaNotebooksDataKPI: totalSolutionCount.toString() });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    ApiClient.getDashboardData('digitalvaluesummary', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        if (res) {
          const totalDigitalValue = res.solDigitalValuesummary;
          const newDigitalValueChartData: IStackedBarChartDataItem[] = [];
          const currentYear = new Date().getFullYear();
          let startYear = currentYear;
          const tenthYearFromNow = currentYear + 10;
          do {
            const tempObj: any = {};

            tempObj.id = startYear.toString();
            tempObj.labelValue = startYear.toString();

            tempObj.firstBarValue = 0;
            tempObj.firstBarFillColor = '#9DE1FC';
            tempObj.secondBarValue = 0;
            tempObj.secondBarFillColor = '#29CBDF';
            tempObj.thirdBarValue = 0;
            tempObj.thirdBarFillColor = '#186BB8';
            tempObj.fourthBarValue = 0;
            tempObj.fourthBarFillColor = '#2BBBF1';
            tempObj.fifthBarValue = 0;
            tempObj.fifthBarFillColor = '#3F84C4';

            const filteredObjectWithYear = totalDigitalValue.filter((item: any) => {
              if (item.year == startYear.toString()) {
                return item;
              }
            })[0];

            if (filteredObjectWithYear) {
              const sortedSolutions = filteredObjectWithYear.digitalValueVO;

              if (sortedSolutions.length >= 1) {
                const firstSolution = sortedSolutions[0];
                tempObj.firstBarValue = firstSolution.digitalValue;
                tempObj.firstSolution = firstSolution;
              }
              if (sortedSolutions.length >= 2) {
                const firstSolution = sortedSolutions[0];
                tempObj.firstBarValue = firstSolution.digitalValue;
                tempObj.firstSolution = firstSolution;

                const secondSolution = sortedSolutions[1];
                tempObj.secondBarValue = secondSolution.digitalValue;
                tempObj.secondSolution = secondSolution;
              }
              if (sortedSolutions.length >= 3) {
                const firstSolution = sortedSolutions[0];
                tempObj.firstBarValue = firstSolution.digitalValue;
                tempObj.firstSolution = firstSolution;

                const secondSolution = sortedSolutions[1];
                tempObj.secondBarValue = secondSolution.digitalValue;
                tempObj.secondSolution = secondSolution;

                const thirdSolution = sortedSolutions[2];
                tempObj.thirdBarValue = thirdSolution.digitalValue;
                tempObj.thirdSolution = thirdSolution;
              }
              if (sortedSolutions.length >= 4) {
                const firstSolution = sortedSolutions[0];
                tempObj.firstBarValue = firstSolution.digitalValue;
                tempObj.firstSolution = firstSolution;

                const secondSolution = sortedSolutions[1];
                tempObj.secondBarValue = secondSolution.digitalValue;
                tempObj.secondSolution = secondSolution;

                const thirdSolution = sortedSolutions[2];
                tempObj.thirdBarValue = thirdSolution.digitalValue;
                tempObj.thirdSolution = thirdSolution;

                const fourthSolution = sortedSolutions[3];
                tempObj.fourthBarValue = fourthSolution.digitalValue;
                tempObj.fourthSolution = fourthSolution;
              }
              if (sortedSolutions.length >= 5) {
                const firstSolution = sortedSolutions[0];
                tempObj.firstBarValue = firstSolution.digitalValue;
                tempObj.firstSolution = firstSolution;

                const secondSolution = sortedSolutions[1];
                tempObj.secondBarValue = secondSolution.digitalValue;
                tempObj.secondSolution = secondSolution;

                const thirdSolution = sortedSolutions[2];
                tempObj.thirdBarValue = thirdSolution.digitalValue;
                tempObj.thirdSolution = thirdSolution;

                const fourthSolution = sortedSolutions[3];
                tempObj.fourthBarValue = fourthSolution.digitalValue;
                tempObj.fourthSolution = fourthSolution;

                const fifthSolution = sortedSolutions[4];
                tempObj.fifthBarValue = fifthSolution.digitalValue;
                tempObj.fifthSolution = fifthSolution;
              }
            }

            newDigitalValueChartData.push(tempObj);
            startYear++;
          } while (startYear <= tenthYearFromNow);
          this.setState({ newDigitalValueChartData, showDigitalValueLoader: false });
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ newDigitalValueChartData: [], showDigitalValueLoader: false });
      });
    ApiClient.getDashboardData('datavalue', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        const totalSavings = res.totalDataValueSavings;
        const totalRevenue = res.totalDataValueRevenue;
        this.setState({
          dataValueDataSavingsKPI: (Math.round((totalSavings + Number.EPSILON) * 100) / 100).toString(),
          dataValueDataRevenueKPI: (Math.round((totalRevenue + Number.EPSILON) * 100) / 100).toString(),
        });
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
    ApiClient.getDashboardData('datavaluesummary', locations, phases, divisions, status, useCaseType, tagSearch)
      .then((res: any) => {
        if (res) {
          const totalDataValue = res.solDataValueSummary;
          const newDataValueChartData: IStackedBarChartDataItem[] = [];
          const currentYear = new Date().getFullYear();
          let startYear = currentYear;
          const tenthYearFromNow = currentYear + 10;
          do {
            const tempObj: any = {};
            tempObj.id = startYear.toString();

            tempObj.labelValue = startYear.toString();
            tempObj.firstBarValue = 0;
            tempObj.firstBarFillColor = '#9DE1FC';
            tempObj.secondBarValue = 0;
            tempObj.secondBarFillColor = '#186BB8';

            const filteredObjectWithYear = totalDataValue.filter((item: any) => {
              if (item.year == startYear.toString()) {
                return item;
              }
            })[0];
            if (filteredObjectWithYear) {
              const sortedSolutions = filteredObjectWithYear.dataValueVO;
              let savings = 0;
              let revenue = 0;
              sortedSolutions.forEach((solution: any) => {
                savings = savings + solution.savings;
                revenue = revenue + solution.revenue;
              });
              tempObj.firstBarValue = savings;
              tempObj.secondBarValue = revenue;
            }
            newDataValueChartData.push(tempObj);
            startYear++;
          } while (startYear <= tenthYearFromNow);
          this.setState({ newDataValueChartData, showDataValueLoader: false });
          this.setState({ showDataValueLoader: false });
        }
      })
      .catch((error) => {
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ newDataValueChartData: [], showDataValueLoader: false });
      });
  }

  protected openCloseFilter = () => {
    this.setState(
      {
        openFilters: !this.state.openFilters,
      },
      () => {
        // trackEvent(
        //   this.getPageTitle(this.state.enablePortfolioSolutionsView, true),
        //   'Moved solution list view to',
        //   'List View',
        // );
        // sessionStorage.setItem(SESSION_STORAGE_KEYS.LISTVIEW_MODE_ENABLE, 'true');
      },
    );
  };

  public render() {
    const {
      solutionsDataKPI,
      digitalValueDataKPI,
      dataValueDataSavingsKPI,
      dataValueDataRevenueKPI,
      // dnaNotebooksDataKPI,
      newDigitalValueChartData,
      newDataValueChartData,
      milestonesChartData,
      dataSourcesChartData,
      showDigitalValueLoader,
      showDataValueLoader,
      showDataSourceLoader,
      showLocationLoader,
      showMilestoneLoader,
      solutionsDataKPILoader,
      digitalValueDataKPILoader,
      dataValueDataKPILoader,
      // dnaNotebooksDataKPILoader,
    } = this.state;

    const exportCSVIcon = () => {
      const element = (
        <span className={classNames(filterStyle.iconTrigger)} onClick={this.triggerDownloadCSVData} >
          <i tooltip-data="Export to CSV" className="icon download" />
        </span>
      )
      return element;
    };

    return (
      <div className={Styles.pageWrapper}>
        <div className={classNames(Styles.mainPanel)}>
          <button
            className={classNames('btn btn-text back arrow', Styles.backBtn)}
            type="submit"
            onClick={() => history.goBack()}
          >
            Back
          </button>
          <div className={Styles.caption}>
            <h3>My Portfolio</h3>
            <div className={Styles.allSolExport}>
              <CSVLink
                data={this.state.csvData}
                headers={this.state.csvHeader}
                ref={(r: any) => (this.csvLink = r)}
                filename={`Portfolio-Export.csv`}
                target="_blank"
                separator={csvSeparator(navigator.language)}
              />
              <div tooltip-data="Filters">
                {exportCSVIcon()}
                <span className={this.state.openFilters ? Styles.activeFilters : ''} onClick={this.openCloseFilter}>
                  <span className={Styles.dividerLine}> &nbsp; </span>
                  {this.state.portfolioDataFilterApplied && <i className="active-status" />}
                  <i className="icon mbc-icon filter big" />
                </span>
              </div>
            </div>
          </div>
          <SolutionsFilter
            userId={this.props.user.id}
            getSolutions={(
              locations: string,
              phases: string,
              divisions: string,
              status: string,
              useCaseType: string,
              tags: string,
            ) => this.getSolutions(locations, phases, divisions, status, useCaseType, tags)}
            showSolutionsFilter={true}
            solutionsDataLoaded={this.state.portfolioFirstTimeDataLoaded}
            setSolutionsDataLoaded={(value: boolean) => this.setState({ portfolioFirstTimeDataLoaded: value })}
            setSolutionsFilterApplied={(value: boolean) => {
              console.log(value);
              this.setState({ portfolioDataFilterApplied: value });
            }}
            openFilters={this.state.openFilters}
            getAllTags={(tags: any) => {
              this.setState({ tagValues: tags });
            }}
            getValuesFromFilter={(value: any) => {
              this.setState({
                tagFilterValues: value.tagFilterValues ? value.tagFilterValues : [],
                selectedTags: value.tagFilterValues ? value.tagFilterValues.map((item: any) => item.name) : [],
              });
            }}
            setSelectedTags={this.state.selectedTagsToPass}
          />
          <div className={classNames(Styles.portContentsection)}>
            <div className={classNames(Styles.portHeader)}>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileVal)}>
                  <h5> Digital Value (€) </h5>
                  <span>
                    {digitalValueDataKPI !== '-' ? (
                      DataFormater(parseFloat(digitalValueDataKPI))
                    ) : (
                      <div>
                        {digitalValueDataKPILoader ? (
                          <div className={classNames(Styles.KPILoader)}>
                            <div className="progress infinite"></div>
                          </div>
                        ) : (
                          <div className={Styles.noDataMessage}>-</div>
                        )}
                      </div>
                    )}
                  </span>
                </div>
                <div className={classNames(Styles.portNavMore)}>
                  <label className="hidden">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label
                    className={classNames(Styles.portNav)}
                    onClick={this.onSummaryDigitalValueContributionBtnClick}
                  >
                    <i className="icon mbc-icon arrow small right" />
                  </label>
                </div>
              </div>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileDataVal)}>
                  <h5>Data Value (€)</h5>
                  <div>
                    {dataValueDataSavingsKPI !== '-' || dataValueDataRevenueKPI !== '-' ? (
                      <div>
                        <span>
                          <span className={classNames(Styles.dataValFieldName)}>Savings:&nbsp;</span>
                          {dataValueDataSavingsKPI !== '-' ? DataFormater(parseFloat(dataValueDataSavingsKPI)) : null}
                        </span>
                        <span>
                          <span className={classNames(Styles.dataValFieldName)}>Revenue:&nbsp;</span>
                          {dataValueDataRevenueKPI !== '-' ? DataFormater(parseFloat(dataValueDataRevenueKPI)) : null}
                        </span>
                      </div>
                    ) : (
                      <div>
                        {dataValueDataKPILoader ? (
                          <div className={classNames(Styles.KPILoader)}>
                            <div className="progress infinite"></div>
                          </div>
                        ) : (
                          <div className={Styles.noDataMessage}>-</div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
                {/* <div className={classNames(Styles.portNavMore)}>
                  <label className="hidden">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label className={classNames(Styles.portNav)}>
                    <i className="icon mbc-icon arrow small right" />
                  </label>
                </div> */}
              </div>
              <div className={classNames(Styles.portTile)}>
                <div className={classNames(Styles.portTileVal)}>
                  <h5> Solutions Count </h5>
                  <span>
                    {solutionsDataKPI !== '-' ? (
                      new Intl.NumberFormat(navigator.language).format(Number(solutionsDataKPI))
                    ) : (
                      <div>
                        {solutionsDataKPILoader ? (
                          <div className={Styles.KPILoader}>
                            <div className="progress infinite"></div>
                          </div>
                        ) : (
                          <div className={Styles.noDataMessage}>-</div>
                        )}
                      </div>
                    )}
                  </span>
                </div>
                <div className={classNames(solutionsDataKPI !== '-' ? Styles.portNavMore : 'hide')}>
                  <label className="hidden">
                    <i className="icon mbc-icon listItem context" />
                  </label>
                  <label className={classNames(Styles.portNav)} onClick={this.onSummaryKpiBtnClick}>
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
                        <div>
                          {showDigitalValueLoader ? (
                            <div className={Styles.widgetLoader}>
                              <div className="progress infinite"></div>
                            </div>
                          ) : (
                            <div className={Styles.noDataMessage}>Data Not Available...</div>
                          )}
                        </div>
                      )}
                    </section>
                  </div>
                  <div id="dataValueWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Data Value</h1>
                      <span className="sub-title-text">in €</span>
                      <section>
                        {newDataValueChartData.length ? (
                          <StackedBarChartWidget
                            data={newDataValueChartData}
                            xAxisLabel="Year"
                            yAxisLabel="Value"
                            tooltipContentComponent={StackedBarChartTooltipDataValue}
                          />
                        ) : (
                          <div>
                            {showDataValueLoader ? (
                              <div className={Styles.widgetLoader}>
                                <div className="progress infinite"></div>
                              </div>
                            ) : (
                              <div className={Styles.noDataMessage}>Data Not Available...</div>
                            )}
                          </div>
                        )}
                      </section>
                    </header>
                  </div>
                </div>
                <div className={classNames(Styles.widgetsConatinaer, Styles.divider)}>
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
                        <div>
                          {showMilestoneLoader ? (
                            <div className={Styles.widgetLoader}>
                              <div className="progress infinite"></div>
                            </div>
                          ) : (
                            <div className={Styles.noDataMessage}>Data Not Available...</div>
                          )}
                        </div>
                      )}
                    </section>
                  </div>
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
                        <div>
                          {showDataSourceLoader ? (
                            <div className={Styles.widgetLoader}>
                              <div className="progress infinite"></div>
                            </div>
                          ) : (
                            <div className={Styles.noDataMessage}>Data Not Available...</div>
                          )}
                        </div>
                      )}
                    </section>
                  </div>
                </div>
                <div className={classNames(Styles.widgetsConatinaer, Styles.divider)}>
                  <div id="locationsWidget" className={Styles.widgetWrapper}>
                    <header>
                      <h1 className={Styles.widgetTitle}>Locations</h1>
                      <span className="sub-title-text">based on development location.</span>
                    </header>
                    <section>
                      {this.state.locationsChartData.length ? (
                        <WorldMapBubbleWidget
                          data={this.state.locationsChartData}
                          onLocationMarkerClick={this.onLocationChartMarkerClick}
                        />
                      ) : (
                        <div>
                          {showLocationLoader ? (
                            <div className={Styles.widgetLoader}>
                              <div className="progress infinite"></div>
                            </div>
                          ) : (
                            <div className={Styles.noDataMessage}>Data Not Available...</div>
                          )}
                        </div>
                      )}
                    </section>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
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

  protected getSolutions = (
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    tags: string,
  ) => {
    ProgressIndicator.show();
    this.setState(
      {
        portfolioFirstTimeDataLoaded: true,
      },
      () => {
        this.getWidgetData(locations, phases, divisions, status, useCaseType, tags);
      },
    );
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
    if (this.state.totalSolutionCounts > 0) {
      trackEvent('Portfolio', 'View Solutions', 'From Solution count KPI');
      history.push('/viewsolutions/digitalvalue');
    } else {
      Notification.show('No solutions available to view.', 'alert');
    }
  };

  protected onSummaryDigitalValueContributionBtnClick = () => {
    if (parseFloat(this.state.digitalValueDataKPI) > 0) {
      trackEvent('Portfolio', 'View Solutions', 'From Digital Value KPI');
      history.push('/viewsolutions/digitalvaluecontribution');
    } else {
      Notification.show('No solutions available to view.', 'alert');
    }
  };

  protected onSummaryNotebookBtnClick = () => {
    if (Number(this.state.dnaNotebooksDataKPI) > 0) {
      trackEvent('Portfolio', 'View Solutions', 'From Notebook KPI');
      history.push('/viewsolutions/notebook');
    } else {
      Notification.show('No solutions available to view.', 'alert');
    }
  };

  // protected onDigitalValueChartBubbleClick = (item: any) => {
  //   history.push('/summary/' + item.id);
  // };

  protected onDigitalValueChartBarClick = (item: any) => {
    // Any change in payload datakeys need an update in below code
    const solutionId = item.payload[item.tooltipPayload[0].dataKey.replace('BarValue', 'Solution')].solutionId;
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
    history.push(`/viewsolutions/location/${marker.id}`);
  };

  protected goToViewSolutionsFor(kpiId: string, item: any) {
    if (item) {
      const payload = item.activePayload[0] ? item.activePayload[0].payload : null;
      if (payload && payload.barValue) {
        const payloadId = payload.id;
        let eventFrom = null;
        let value = null;
        switch (kpiId) {
          case 'phase':
            eventFrom = 'From Milestones Chart Bar';
            value = payload.labelValue;
            break;
          case 'datavolume':
            eventFrom = 'From Data Sources Chart Bar';
            value = payload.labelValue;
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

  protected triggerDownloadCSVData = () => {
    const pageTitle = 'Portfolio Solutions';
    this.setState({ csvData: [], csvHeader: [] });
    ProgressIndicator.show();
    getDataForCSV(
      this.state.queryParams,
      -1,
      -1,
      -1,
      -1,
      'Phases',
      'asc',
      true,
      (csvData: Data, csvHeader: Data) => {
        this.setState(
          {
            csvData,
            csvHeader,
          },
          () => {
            if (this.csvLink) {
              setTimeout(() => {
                trackEvent(
                  pageTitle,
                  'Download CSV',
                  'Downloaded solutions list data as .csv exported file',
                );
                this.csvLink.link.click();
              }, 0);
            }
            ProgressIndicator.hide();
          },
        );
      },
    );
  }
}
