import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';

import { IconConceptDevelopment } from 'components/icons/IconConceptDevelopment';
import { IconIdeation } from 'components/icons/IconIdeation';
import { IconKickOff } from 'components/icons/IconKickOff';
import { IconOperations } from 'components/icons/IconOperations';
import { IconPilot } from 'components/icons/IconPilot';
import { IconProfessionalization } from 'components/icons/IconProfessionalization';
import { ILocation, IMilestonesList, IMonth, IPhase, IPhasesItem, IRollout, IRolloutDetail } from 'globals/types';
import { regionalForMonthAndYear } from '../../../../services/utils';
// import { ApiClient } from '../../../../services/ApiClient';
import Modal from 'components/formElements/modal/Modal';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import Styles from './Milestones.scss';
const classNames = cn.bind(Styles);

export interface IMilestonesProps {
  phases: IPhase[];
  locations: ILocation[];
  currentPhase: IPhase;
  modifyMileStones: (milestonesList: IMilestonesList, currentPhase: IPhase) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
  milestones?: IMilestonesList;
}

export interface IPhaseError {
  phaseId: string;
  monthValid: boolean;
  yearValid: boolean;
}

export interface IRolloutError {
  locationId: string;
  monthValid: boolean;
  yearValid: boolean;
}

export interface IMileStonesState {
  milestonesList: IMilestonesList;
  years?: number[];
  months?: IMonth[];
  showMilestonesModal?: boolean;
  milestones?: IMilestonesList;
  milestonesErrorMessage: string;
  kickoff?: IPhasesItem;
  ideation?: IPhasesItem;
  poc?: IPhasesItem;
  pilot?: IPhasesItem;
  prof?: IPhasesItem;
  ops?: IPhasesItem;
  rollout?: IRollout;
  rolloutDetails: IRolloutDetail[];
  locationValue: ILocation[];
  showCommentArea: boolean;
  currentMilestoneIndex: number;
  cancelChanges: boolean;
  commentValue: string;
  commentValueError: string;
  noMilestoneSelectedError: string;
  currentPhase: IPhase;
  phaseErrors: IPhaseError[];
  rolloutLocationValid: boolean;
  rolloutLocationErrors: IRolloutError[];
}
const defaultKickOff: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '1', name: 'Kick-off' },
};

const defaultIdeation: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '2', name: 'Ideation' },
};

const defaultPoc: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '3', name: 'Concept Development / Proof of concept' },
};

const defaultPilot: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '4', name: 'Pilot' },
};
const defaultProf: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '5', name: 'Professionalization' },
};
const defaultOps: IPhasesItem = {
  description: null,
  month: 0,
  year: 0,
  phase: { id: '6', name: 'Operations / Rollout' },
};
// const defaultRollout: IRollout = {
//   description: null,
//   details: [],
// };
export default class Milestones extends React.Component<IMilestonesProps, IMileStonesState> {
  public static getDerivedStateFromProps(props: IMilestonesProps, state: IMileStonesState) {
    if (props && props.milestones && props.milestones.phases.length !== 0 && state.currentMilestoneIndex === -1) {
      const phases = state.milestones.phases;
      phases.forEach((milestone) => {
        props.milestones.phases.forEach((propMileStone) => {
          if (milestone.phase.id === propMileStone.phase.id) {
            milestone.description = propMileStone.description;
            milestone.month = propMileStone.month;
            milestone.phase = propMileStone.phase;
            milestone.year = propMileStone.year;
          }
        });
      });
      props.milestones.phases.forEach((milestone) => {
        if (milestone.phase.id === '1') {
          defaultKickOff.description = milestone.description;
          defaultKickOff.month = milestone.month;
          defaultKickOff.year = milestone.year;
          defaultKickOff.phase = milestone.phase;
        }
        if (milestone.phase.id === '2') {
          defaultIdeation.description = milestone.description;
          defaultIdeation.month = milestone.month;
          defaultIdeation.year = milestone.year;
          defaultIdeation.phase = milestone.phase;
        }
        if (milestone.phase.id === '3') {
          defaultPoc.description = milestone.description;
          defaultPoc.month = milestone.month;
          defaultPoc.year = milestone.year;
          defaultPoc.phase = milestone.phase;
        }
        if (milestone.phase.id === '4') {
          defaultPilot.description = milestone.description;
          defaultPilot.month = milestone.month;
          defaultPilot.year = milestone.year;
          defaultPilot.phase = milestone.phase;
        }
        if (milestone.phase.id === '5') {
          defaultProf.description = milestone.description;
          defaultProf.month = milestone.month;
          defaultProf.year = milestone.year;
          defaultProf.phase = milestone.phase;
        }
        if (milestone.phase.id === '6') {
          defaultOps.description = milestone.description;
          defaultOps.month = milestone.month;
          defaultOps.year = milestone.year;
          defaultOps.phase = milestone.phase;
        }
      });

      const milestones = {
        phases,
        rollouts: props.milestones.rollouts,
      };

      const locationValue: ILocation[] = [];
      if (props.milestones.rollouts && props.milestones.rollouts.details && props.milestones.rollouts.details.length) {
        props.milestones.rollouts.details.forEach((rolloutDetail: IRolloutDetail) => {
          locationValue.push(rolloutDetail.location);
        });
      }

      return {
        milestones,
        kickoff: defaultKickOff,
        ideation: defaultIdeation,
        poc: defaultPoc,
        pilot: defaultPilot,
        prof: defaultProf,
        ops: defaultOps,
        currentMilestoneIndex: 0,
        currentPhase: props.currentPhase,
        commentValue: '',
        rolloutDetails: props.milestones.rollouts ? props.milestones.rollouts.details : [],
        locationValue,
      };
    }
    return null;
  }

  public commentAreaInput: HTMLTextAreaElement;

  public mileStoneIcons = [
    <IconKickOff key={1} />,
    <IconIdeation key={2} />,
    <IconConceptDevelopment key={3} />,
    <IconPilot key={4} />,
    <IconProfessionalization key={5} className={Styles.proffIcon} />,
    <IconOperations key={6} />,
  ];

  constructor(props: IMilestonesProps) {
    super(props);
    this.state = {
      milestonesList: JSON.parse(JSON.stringify(this.props.milestones)),
      showMilestonesModal: false,
      cancelChanges: false,
      years: [
        2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029, 2030, 2031, 2032,
        2033, 2034, 2035, 2036, 2037, 2038, 2039, 2040, 2041, 2042, 2043, 2044, 2045, 2046, 2047, 2048, 2049, 2050,
        2051, 2052, 2053, 2054, 2055, 2056, 2057, 2058, 2059, 2060, 2061, 2062, 2063, 2064, 2065, 2066, 2067, 2068,
        2069, 2070, 2071, 2072, 2073, 2074, 2075, 2076, 2077, 2078, 2079, 2080, 2081, 2082, 2083, 2084, 2085, 2086,
        2087, 2088, 2089, 2090, 2091, 2092, 2093, 2094, 2095, 2096, 2097, 2098, 2099,
      ],
      milestones: { phases: [], rollouts: null },
      milestonesErrorMessage: null,
      kickoff: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '1',
          name: 'Kick-off',
        },
      },
      ideation: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '2',
          name: 'Ideation',
        },
      },
      poc: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '3',
          name: 'Concept Development / Proof of concept',
        },
      },
      pilot: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '4',
          name: 'Pilot',
        },
      },
      prof: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '5',
          name: 'Professionalization',
        },
      },
      ops: {
        description: '',
        month: 0,
        year: 0,
        phase: {
          id: '6',
          name: 'Operations / Rollout',
        },
      },
      rollout: {
        details: [],
        description: '',
      },
      rolloutDetails: [],
      months: [
        {
          id: 1,
          name: 'January',
        },
        {
          id: 2,
          name: 'February',
        },
        {
          id: 3,
          name: 'March',
        },
        {
          id: 4,
          name: 'April',
        },
        {
          id: 5,
          name: 'May',
        },
        {
          id: 6,
          name: 'June',
        },
        {
          id: 7,
          name: 'July',
        },
        {
          id: 8,
          name: 'August',
        },
        {
          id: 9,
          name: 'September',
        },
        {
          id: 10,
          name: 'October',
        },
        {
          id: 11,
          name: 'November',
        },
        {
          id: 12,
          name: 'December',
        },
      ],
      locationValue: [],
      showCommentArea: false,
      currentMilestoneIndex: -1,
      commentValue: '',
      commentValueError: null,
      noMilestoneSelectedError: null,
      currentPhase: {
        id: '1',
        name: 'Kick-off',
      },
      phaseErrors: [],
      rolloutLocationValid: true,
      rolloutLocationErrors: [],
    };

    // this.onAddOrEditCommentClick = this.onAddOrEditCommentClick.bind(this);
    // this.onCommentChange = this.onCommentChange.bind(this);
    // this.addMilestoneComment = this.addMilestoneComment.bind(this);
  }

  public resetChanges = () => {
    const milestones: IMilestonesList = this.state.milestones;
    if (this.props.milestones && this.props.milestones.phases.length === 0) {
      milestones.phases.forEach((milestone) => {
        milestone.description = '';
        milestone.month = 0;
        milestone.year = 0;
        if (milestone.phase.id === '1') {
          milestone.phase = { id: '1', name: 'Kick-off' };
        } else if (milestone.phase.id === '2') {
          milestone.phase = { id: '2', name: 'Ideation' };
        } else if (milestone.phase.id === '3') {
          milestone.phase = { id: '', name: 'Concept Development / Proof of concept' };
        } else if (milestone.phase.id === '4') {
          milestone.phase = { id: '4', name: 'Pilot' };
        } else if (milestone.phase.id === '5') {
          milestone.phase = { id: '5', name: 'Professionalization' };
        } else if (milestone.phase.id === '6') {
          milestone.phase = { id: '6', name: 'Operations / Rollout' };
        }
      });
      this.setState({
        kickoff: defaultKickOff,
        ideation: defaultIdeation,
        poc: defaultPoc,
        pilot: defaultPilot,
        prof: defaultProf,
        ops: defaultOps,
      });
    } else if (this.props.milestones && this.props.milestones.phases.length > 0) {
      this.props.milestones.phases.forEach((propMileStone) => {
        milestones.phases.forEach((milestone) => {
          if (milestone.phase.id === propMileStone.phase.id) {
            milestone.description = propMileStone.description;
            milestone.month = propMileStone.month;
            milestone.phase = propMileStone.phase;
            milestone.year = propMileStone.year;
          }
          // may need to check this else condition
        });
      });
    }
    this.setState({ milestones, currentPhase: this.props.currentPhase });
    this.resetMilestonesModel();
  };

  public updateComponentValues = (milestonesList: IMilestonesList) => {
    this.setState({ milestonesList });
  };

  public canShowPhasesPlaceHolder(): boolean {
    const milestones = this.state.milestones;
    let showPhasesPlaceHolder = false;
    milestones.phases.forEach((milestone) => {
      if (milestone.month && milestone.year) {
        showPhasesPlaceHolder = true;
      }
    });

    if (
      !showPhasesPlaceHolder &&
      milestones.rollouts &&
      milestones.rollouts.details &&
      milestones.rollouts.details.length
    ) {
      milestones.rollouts.details.forEach((rollout: IRolloutDetail) => {
        if (rollout.month && rollout.year) {
          showPhasesPlaceHolder = true;
        }
      });
    }

    return showPhasesPlaceHolder;
  }

  public onAddOrEditCommentClick = (e: React.FormEvent<HTMLButtonElement>) => {
    const currentMilestoneIndex = parseInt(e.currentTarget.value, 10);
    const currentMilestone = this.state.milestones.phases[currentMilestoneIndex];

    this.setState(
      {
        showCommentArea: true,
        currentMilestoneIndex,
        commentValue: currentMilestone.description,
        commentValueError: null,
      },
      () => {
        this.commentAreaInput.focus();
      },
    );
    this.props.modifyMileStones(this.state.milestones, this.state.currentPhase);
  };

  public onCommentChange = (e: React.FormEvent<HTMLTextAreaElement>) => {
    const maxCharReachedError = '*Reached maximum character limit';
    const commentValue = e.currentTarget.value;
    if (commentValue.length <= 200) {
      this.setState({ commentValue, commentValueError: null });
    } else {
      this.setState({ commentValueError: maxCharReachedError });
    }
    this.props.modifyMileStones(this.state.milestones, this.state.currentPhase);
  };

  public addMilestoneComment = () => {
    const milestones = this.state.milestones;
    const currentMilestone = this.state.milestones.phases[this.state.currentMilestoneIndex];
    if (currentMilestone.phase.id === '6') {
      milestones.rollouts.description = this.state.commentValue;
    } else {
      currentMilestone.description = this.state.commentValue;
      milestones[this.state.currentMilestoneIndex] = currentMilestone;
    }
    this.setState({
      showCommentArea: false,
      milestones,
      commentValue: '',
    });
    this.props.modifyMileStones(this.state.milestones, this.state.currentPhase);
  };

  public componentDidMount() {
    const phases = this.state.milestones.phases;
    phases.push(this.state.kickoff);
    phases.push(this.state.ideation);
    phases.push(this.state.poc);
    phases.push(this.state.pilot);
    phases.push(this.state.prof);
    phases.push(this.state.ops);
    const phaseErrors: IPhaseError[] = [];
    phases.forEach((phasesItem: IPhasesItem) => {
      const initialPhaseError = {
        phaseId: phasesItem.phase.id,
        monthValid: true,
        yearValid: true,
      };
      phaseErrors.push(initialPhaseError);
    });

    this.setState({ phaseErrors });
  }
  public render() {
    const noMilestoneSelectedError = this.state.noMilestoneSelectedError || '';
    const missingEntryError = '*Missing Entry';

    const phases = this.props.phases.map((obj, index) => {
      const phaseError = this.state.phaseErrors[index] || {
        phaseId: obj.id,
        monthValid: true,
        yearValid: true,
      };
      const monthValid = phaseError.monthValid;
      const yearValid = phaseError.yearValid;
      const isCurrentPhase = obj.id === (this.state.currentPhase ? this.state.currentPhase.id : '');

      const milestone = this.state.milestones.phases.find((phasesItem: IPhasesItem) => {
        return phasesItem.phase.id === obj.id;
      });

      const locationValues = this.state.locationValue.map((location: ILocation) => {
        return location.id;
      });

      const rolloutDetails = this.state.rolloutDetails;

      return (
        <React.Fragment key={obj.id}>
          {obj.id !== '6' ? (
            <div id={obj.name + 'flexContainer'} className={classNames(Styles.flexLayout)}>
              <div id={obj.name + 'columnDisplay'} className={classNames(Styles.columnDisplay, Styles.labelCol)}>
                <label id={obj.name + 'label'} className="input-label">
                  {obj.name}
                </label>
              </div>
              <div className={classNames(Styles.columnDisplay, Styles.midColumn)}>
                <div id={obj.name + 'monthColDisplay'}>
                  <div
                    id={obj.name + 'monthsContainer'}
                    className={classNames(
                      'input-field-group include-error no-label',
                      Styles.monthAndYearContainer,
                      monthValid ? '' : 'error-local',
                    )}
                  >
                    <div id={obj.name + 'months'} className=" custom-select">
                      <select
                        id={obj.name + 'monthSelect'}
                        value={milestone ? milestone.month : ''}
                        onChange={this.onMonthChange(obj.id)}
                      >
                        <option id={obj.name + 'defaultMonth'} value={0}>
                          Select month
                        </option>
                        {this.state.months.map((obj0) => (
                          <option id={obj.name + obj0.id + obj0.name} key={obj0.id} value={obj0.id}>
                            {obj0.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span className={classNames('error-message-local', monthValid ? 'hide' : '')}>
                      {missingEntryError}
                    </span>
                  </div>
                </div>
                <div id={obj.name + 'yearColDisplay'}>
                  <div
                    id={obj.name + 'yearContainer'}
                    className={classNames(
                      'input-field-group include-error no-label',
                      Styles.monthAndYearContainer,
                      yearValid ? '' : 'error-local',
                    )}
                  >
                    <div id={obj.name + 'years'} className=" custom-select">
                      <select
                        id={obj.name + 'yearSelect'}
                        value={milestone ? milestone.year : ''}
                        onChange={this.onYearChange(obj.id)}
                      >
                        <option id={obj.name + 'defaultYear'} value={0}>
                          Select year
                        </option>
                        {this.state.years.map((obj1) => (
                          <option id={obj1 + obj.name} key={obj1} value={obj1}>
                            {obj1}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span className={classNames('error-message-local', yearValid ? 'hide' : '')}>
                      {missingEntryError}
                    </span>
                  </div>
                </div>
              </div>
              <div
                id={obj.name + 'currentPhaseColDisplay'}
                className={classNames(Styles.columnDisplay, Styles.currentPhaseColumn)}
              >
                <label className="radio">
                  <span className="wrapper">
                    <input
                      name="currentPhase"
                      type="radio"
                      className="ff-only"
                      value={obj.id}
                      checked={isCurrentPhase}
                      onChange={this.onCurrentPhaseRadioChange}
                    />
                  </span>
                </label>
              </div>
            </div>
          ) : (
            <div id={obj.name + 'flexContainer'} className={classNames(Styles.flexLayout)}>
              <div id={obj.name + 'columnDisplay'} className={classNames(Styles.columnDisplay, Styles.rolloutLabelCol)}>
                <label id={obj.name + 'label'} className="input-label">
                  {obj.name}
                </label>
              </div>
              <div className={Styles.columnDisplay}>
                <div className={Styles.rolloutLocationSelect}>
                  <div
                    id="locationContainer"
                    className={classNames(
                      'input-field-group include-error',
                      this.state.rolloutLocationValid ? '' : 'error-local',
                    )}
                  >
                    <label id="locationLabel" className="input-label" htmlFor="rollotLocationSelect">
                      Rollout Locations<sup>*</sup>
                    </label>
                    <div id="rollotLocation" className="custom-select">
                      <select
                        id="rollotLocationSelect"
                        multiple={true}
                        onChange={this.onLocationChange}
                        value={locationValues}
                      >
                        {this.props.locations.map((loc) => (
                          <option id={loc.name + loc.id} key={loc.id} value={loc.id}>
                            {loc.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span className={classNames('error-message-local', this.state.rolloutLocationValid ? 'hide' : '')}>
                      {missingEntryError}
                    </span>
                  </div>
                </div>
                {this.state.locationValue.map((loc: ILocation, locIndex: number) => {
                  const rolloutDetailsByLocation = rolloutDetails.find((detail: IRolloutDetail) => {
                    return detail.location.id === loc.id;
                  });

                  let locMonthValid = true;
                  let locYearValid = true;

                  if (monthValid && yearValid) {
                    const rolloutLocationErrors = this.state.rolloutLocationErrors;
                    if (rolloutLocationErrors.length) {
                      const rolloutLocationError = rolloutLocationErrors[locIndex];
                      if (rolloutLocationError) {
                        locMonthValid = rolloutLocationError.monthValid;
                        locYearValid = rolloutLocationError.yearValid;
                      }
                    }
                  } else {
                    locMonthValid = false;
                    locYearValid = false;
                  }

                  return (
                    <div key={locIndex}>
                      <div className={classNames('input-field-group', Styles.rolloutLocationLabel)}>
                        <label className="input-label">{loc.name}</label>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div id={loc.name + 'monthColDisplay'}>
                          <div
                            id={loc.name + 'monthsContainer'}
                            className={classNames(
                              'input-field-group no-label',
                              Styles.monthAndYearContainer,
                              locMonthValid ? '' : 'error-local',
                            )}
                          >
                            <div id={loc.name + 'months'} className=" custom-select">
                              <select
                                id={loc.name + 'monthSelect'}
                                value={rolloutDetailsByLocation ? rolloutDetailsByLocation.month : ''}
                                onChange={this.onMonthChange(loc.id, true, loc)}
                              >
                                <option id={loc.name + 'defaultMonth'} value={0}>
                                  Select month
                                </option>
                                {this.state.months.map((obj0) => (
                                  <option id={obj.name + obj0.id + obj0.name} key={obj0.id} value={obj0.id}>
                                    {obj0.name}
                                  </option>
                                ))}
                              </select>
                            </div>
                            <span className={classNames('error-message-local', locMonthValid ? 'hide' : '')}>
                              {missingEntryError}
                            </span>
                          </div>
                        </div>
                        <div id={loc.name + 'yearColDisplay'}>
                          <div
                            id={loc.name + 'yearContainer'}
                            className={classNames(
                              'input-field-group no-label',
                              Styles.monthAndYearContainer,
                              locYearValid ? '' : 'error-local',
                            )}
                          >
                            <div id={loc.name + 'years'} className=" custom-select">
                              <select
                                id={loc.name + 'yearSelect'}
                                value={rolloutDetailsByLocation ? rolloutDetailsByLocation.year : ''}
                                onChange={this.onYearChange(loc.id, true, loc)}
                              >
                                <option id={loc.name + 'defaultYear'} value={0}>
                                  Select year
                                </option>
                                {this.state.years.map((obj1) => (
                                  <option id={obj1 + obj.name} key={obj1} value={obj1}>
                                    {obj1}
                                  </option>
                                ))}
                              </select>
                            </div>
                            <span className={classNames('error-message-local', locYearValid ? 'hide' : '')}>
                              {missingEntryError}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
              <div
                id={obj.name + 'currentPhaseColDisplay'}
                className={classNames(Styles.columnDisplay, Styles.currentPhaseColumn, Styles.rolloutCurPhaseCol)}
              >
                <label className="radio">
                  <span className="wrapper">
                    <input
                      name="currentPhase"
                      type="radio"
                      className="ff-only"
                      value={obj.id}
                      checked={isCurrentPhase}
                      onChange={this.onCurrentPhaseRadioChange}
                    />
                  </span>
                </label>
              </div>
            </div>
          )}
        </React.Fragment>
      );
    });

    const milestonesModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={classNames(Styles.firstPanel, Styles.addMilestonesModal)}>
        <div className={Styles.formWrapper}>
          <div id="contentFlex" className={classNames(Styles.flexLayout)}>
            <div id="phaseHeader">
              <label id="phaseLabel" className={classNames(Styles.headingText)}>
                Phase<sup>*</sup>
              </label>
            </div>
            <div className={Styles.midColumn}>
              <div id="monthHeader">
                <label id="monthLabel" className={classNames(Styles.headingText)}>
                  Month<sup>*</sup>
                </label>
              </div>
              <div id="yearHeader">
                <label id="yearLabel" className={classNames(Styles.headingText)}>
                  Year<sup>*</sup>
                </label>
              </div>
            </div>
            <div id="currnetPhaseHeader" className={Styles.currentPhaseColumn}>
              <label id="currnetPhaseLabel" className={classNames(Styles.headingText)}>
                Current<sup>*</sup>
              </label>
            </div>
          </div>
          {phases}
          <div className={classNames(noMilestoneSelectedError.length ? '' : 'hide')}>
            <span className="error-message">{noMilestoneSelectedError}</span>
          </div>
          <div id="buttonContainer" className={classNames(Styles.actionWrapper)}>
            <button id="saveMilestonebtn" className="btn btn-primary" onClick={this.addMileStones} type="button">
              Save
            </button>
          </div>
        </div>
      </div>
    );

    const canShowPhasesPlaceHolder = this.canShowPhasesPlaceHolder();
    let canShowAddMileStoneBtn = false;
    const currentMilestone = this.state.milestones.phases[this.state.currentMilestoneIndex];
    const commentValueError = this.state.commentValueError || '';
    const milestonesErrorMessage = this.state.milestonesErrorMessage || '';

    return (
      <React.Fragment>
        <div id="mainWrapper" className={classNames(Styles.wrapper)}>
          <div id="mainPanel" className={classNames(Styles.firstPanel)}>
            <h3 id="pageHeader">
              Milestones and solution progress
              {canShowPhasesPlaceHolder ? (
                <button className={Styles.editMilestonesBtn} onClick={this.onMileStonesAdd}>
                  <i className="icon mbc-icon edit" />
                  <span>Edit milestones</span>
                </button>
              ) : (
                ''
              )}
            </h3>
            <div
              className={classNames(
                Styles.flexLayout,
                Styles.milestonesWrapper,
                canShowPhasesPlaceHolder ? Styles.active : '',
              )}
            >
              {this.state.milestones.phases.map((milestone, index) => {
                const rollouts = this.state.milestones.rollouts;
                if (rollouts) {
                  if (milestone.phase.id === '6' && rollouts.details && rollouts.details.length) {
                    const rolloutMilestone = rollouts.details[0];
                    milestone.description = rollouts.description;
                    milestone.month = rolloutMilestone.month;
                    milestone.year = rolloutMilestone.year;
                  }
                }

                const showPhase = milestone.month && milestone.year;
                let showAddMileStoneBtn = false;
                const showAddComment = !milestone.description || milestone.description === '';

                if (!canShowAddMileStoneBtn && !showPhase) {
                  showAddMileStoneBtn = true;
                  canShowAddMileStoneBtn = true;
                }

                return (
                  <div key={milestone.phase.id} className={showPhase ? Styles.active : ''}>
                    <div
                      className={classNames(
                        Styles.info,
                        showPhase ? '' : canShowPhasesPlaceHolder && !showAddMileStoneBtn ? 'hidden' : 'hide',
                      )}
                    >
                      <div>{this.mileStoneIcons[index]}</div>
                      <div className={classNames(Styles.phase, showPhase ? '' : 'hide')}>{milestone.phase.name}</div>
                      <div className={Styles.monthYear}>
                        {/* {milestone.month >= 10 ? milestone.month : '0' + milestone.month}/{milestone.year} */}
                        {milestone.month > 0 && milestone.year > 0
                          ? regionalForMonthAndYear(milestone.month + '/' + '01' + '/' + milestone.year)
                          : ''}
                      </div>
                    </div>
                    <div
                      className={
                        showAddMileStoneBtn ? (showPhase ? 'hide' : '') : canShowPhasesPlaceHolder ? 'hide' : 'hidden'
                      }
                    >
                      <div id="addWrapper" className={Styles.addMilestonesWrapper}>
                        <button id="addBtn" onClick={this.onMileStonesAdd}>
                          <i id="addIcon" className="icon mbc-icon plus" />
                          <span id="addSpan">Add milestone</span>
                        </button>
                      </div>
                    </div>
                    <div className={Styles.divider}>
                      <span className={classNames(Styles.dot, Styles.left)} />
                      <span className={Styles.line} />
                      <span className={classNames(Styles.dot, Styles.right)} />
                    </div>
                    <div className={Styles.comment}>
                      <div
                        className={classNames(
                          Styles.addCommentWrapper,
                          showPhase ? '' : 'hide',
                          !showAddComment ? Styles.edit : '',
                        )}
                      >
                        <button id={'addCommentBtn_' + { index }} value={index} onClick={this.onAddOrEditCommentClick}>
                          {showAddComment ? (
                            <React.Fragment>
                              <i className="icon mbc-icon plus" />
                              <span>Add comment</span>
                            </React.Fragment>
                          ) : (
                            <React.Fragment>
                              <i className="icon mbc-icon edit" />
                              <span>Edit comment</span>
                            </React.Fragment>
                          )}
                        </button>
                      </div>
                      <p className={showPhase && milestone.description !== '' ? '' : 'hide'}>{milestone.description}</p>
                    </div>
                  </div>
                );
              })}
            </div>
            <div className={classNames(Styles.commentWapper, this.state.showCommentArea ? '' : 'hide')}>
              <div
                className={classNames(
                  'input-field-group include-error area',
                  commentValueError.length ? 'error-local' : '',
                )}
              >
                <label className="input-label" htmlFor="comment">
                  {currentMilestone ? currentMilestone.phase.name : ''} (optional, max. 200 characters)
                </label>
                <textarea
                  value={this.state.commentValue}
                  className={classNames('input-field-area', Styles.commentTextarea)}
                  onChange={this.onCommentChange}
                  ref={(commentAreaInput) => {
                    this.commentAreaInput = commentAreaInput;
                  }}
                  id="comment"
                />
                <span className={classNames('error-message-local', commentValueError.length ? '' : 'hide')}>
                  {commentValueError}
                </span>
              </div>
              <div className={Styles.actionWrapper}>
                <button
                  id="saveCommentbtn"
                  className="btn btn-primary"
                  onClick={this.addMilestoneComment}
                  type="button"
                  disabled={commentValueError !== ''}
                >
                  Save
                </button>
              </div>
            </div>
            {this.state.milestones?.rollouts?.details && this.state.milestones?.rollouts?.details.length > 0 ? (
              <div>
                <h3>Rollout Locations</h3>
                <br />
                <div className={classNames(Styles.rolloutLocationsList)}>
                  {this.state.milestones?.rollouts?.details.map((rollout, index) => {
                    return (
                      <span key={index}>
                        {rollout.location.name}(
                        {rollout.month > 0 && rollout.year > 0
                          ? regionalForMonthAndYear(rollout.month + '/' + '01' + '/' + rollout.year)
                          : ''}
                        ){index <= this.state.milestones.rollouts.details.length - 2 ? ', ' : ''}
                      </span>
                    );
                  })}
                </div>
              </div>
            ) : (
              ''
            )}
          </div>
          <div className={classNames(milestonesErrorMessage.length ? '' : 'hide')}>
            <span className="error-message">{milestonesErrorMessage}</span>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onMilestonesSubmit}>
            Save & Next
          </button>
        </div>
        <Modal
          title="Add or Edit milestones"
          acceptButtonTitle="Save"
          buttonAlignment="right"
          showAcceptButton={false}
          showCancelButton={false}
          show={this.state.showMilestonesModal}
          content={milestonesModalContent}
          scrollableContent={true}
          onCancel={this.onAddorEditMilestonesModalCancel}
        />
      </React.Fragment>
    );
  }

  public onAddorEditMilestonesModalCancel = () => {
    this.resetMilestonesModel();
    this.setState({ showMilestonesModal: false });
  };

  public clearModalFields() {
    this.setState({
      showMilestonesModal: false,
    });
  }

  public onMileStonesAdd = () => {
    this.setState(
      {
        showMilestonesModal: true,
        noMilestoneSelectedError: '',
      },
      () => {
        SelectBox.defaultSetup(false);
      },
    );
  };

  public addMileStones = () => {
    const phases = [];
    phases.push(this.state.kickoff);
    phases.push(this.state.ideation);
    phases.push(this.state.poc);
    phases.push(this.state.pilot);
    phases.push(this.state.prof);
    phases.push(this.state.ops);

    const rollouts: IRollout = this.state.rollout;

    rollouts.details = this.state.rolloutDetails;

    const milestones: IMilestonesList = this.state.milestones;
    milestones.rollouts = rollouts;

    if (this.validateMilestones(milestones)) {
      this.setState({
        showMilestonesModal: false,
        milestones,
        currentMilestoneIndex: -1,
        milestonesList: JSON.parse(JSON.stringify(milestones)),
      });
      this.props.modifyMileStones(milestones, this.state.currentPhase);
    }
  };

  public onMonthChange =
    (phaseType: string, isRollout?: boolean, location?: ILocation) => (event: React.ChangeEvent<HTMLSelectElement>) => {
      const target = event.target as HTMLSelectElement;
      const monthValue = parseInt(target.value, 10);
      if (isRollout) {
        this.updateRolloutDetails(location, monthValue, 0, true);
      } else {
        switch (phaseType) {
          case '1':
            const kickoff: IPhasesItem = {
              month: this.state.kickoff.month,
              year: this.state.kickoff.year,
              phase: { id: this.state.kickoff.phase.id, name: this.state.kickoff.phase.name },
              description: this.state.kickoff.description,
            };
            kickoff.month = monthValue;
            this.setState({
              kickoff,
            });
            break;
          case '2':
            const ideation: IPhasesItem = {
              month: this.state.ideation.month,
              year: this.state.ideation.year,
              phase: { id: this.state.ideation.phase.id, name: this.state.ideation.phase.name },
              description: this.state.ideation.description,
            };
            ideation.month = monthValue;
            this.setState({
              ideation,
            });
            break;
          case '3':
            const poc: IPhasesItem = {
              month: this.state.poc.month,
              year: this.state.poc.year,
              phase: { id: this.state.poc.phase.id, name: this.state.poc.phase.name },
              description: this.state.poc.description,
            };
            poc.month = monthValue;
            this.setState({
              poc,
            });
            break;
          case '4':
            const pilot: IPhasesItem = {
              month: this.state.pilot.month,
              year: this.state.pilot.year,
              phase: { id: this.state.pilot.phase.id, name: this.state.pilot.phase.name },
              description: this.state.pilot.description,
            };
            pilot.month = monthValue;
            this.setState({
              pilot,
            });
            break;
          case '5':
            const prof: IPhasesItem = {
              month: this.state.prof.month,
              year: this.state.prof.year,
              phase: { id: this.state.prof.phase.id, name: this.state.prof.phase.name },
              description: this.state.prof.description,
            };
            prof.month = monthValue;
            this.setState({
              prof,
            });
            break;
          case '6':
            const ops: IPhasesItem = {
              month: this.state.ops.month,
              year: this.state.ops.year,
              phase: { id: this.state.ops.phase.id, name: this.state.ops.phase.name },
              description: this.state.ops.description,
            };
            ops.month = monthValue;
            this.setState({
              ops,
            });
            break;
          default:
            break;
        }

        const milestones = this.state.milestones;
        let phaseIndex = -1;
        const phaseItem = milestones.phases.find((phase: IPhasesItem, itemIndex: number) => {
          phaseIndex = itemIndex;
          return phase.phase.id === phaseType;
        });

        phaseItem.month = monthValue;

        milestones.phases.splice(phaseIndex, 1);
        milestones.phases.splice(phaseIndex, 0, phaseItem);

        this.setState({
          milestones,
        });
      }
    };

  public onYearChange =
    (phaseType: string, isRollout?: boolean, location?: ILocation) => (event: React.ChangeEvent<HTMLSelectElement>) => {
      const target = event.target as HTMLSelectElement;
      const yearValue = parseInt(target.value, 10);
      if (isRollout) {
        this.updateRolloutDetails(location, 0, yearValue, false);
      } else {
        switch (phaseType) {
          case '1':
            const kickoff: IPhasesItem = {
              month: this.state.kickoff.month,
              year: this.state.kickoff.year,
              phase: { id: this.state.kickoff.phase.id, name: this.state.kickoff.phase.name },
              description: this.state.kickoff.description,
            };
            kickoff.year = yearValue;
            this.setState({
              kickoff,
            });
            break;
          case '2':
            const ideation: IPhasesItem = {
              month: this.state.ideation.month,
              year: this.state.ideation.year,
              phase: { id: this.state.ideation.phase.id, name: this.state.ideation.phase.name },
              description: this.state.ideation.description,
            };
            ideation.year = yearValue;
            this.setState({
              ideation,
            });
            break;
          case '3':
            const poc: IPhasesItem = {
              month: this.state.poc.month,
              year: this.state.poc.year,
              phase: { id: this.state.poc.phase.id, name: this.state.poc.phase.name },
              description: this.state.poc.description,
            };
            poc.year = yearValue;
            this.setState({
              poc,
            });
            break;
          case '4':
            const pilot: IPhasesItem = {
              month: this.state.pilot.month,
              year: this.state.pilot.year,
              phase: { id: this.state.pilot.phase.id, name: this.state.pilot.phase.name },
              description: this.state.pilot.description,
            };
            pilot.year = yearValue;
            this.setState({
              pilot,
            });
            break;
          case '5':
            const prof: IPhasesItem = {
              month: this.state.prof.month,
              year: this.state.prof.year,
              phase: { id: this.state.prof.phase.id, name: this.state.prof.phase.name },
              description: this.state.prof.description,
            };
            prof.year = yearValue;
            this.setState({
              prof,
            });
            break;
          case '6':
            const ops: IPhasesItem = {
              month: this.state.ops.month,
              year: this.state.ops.year,
              phase: { id: this.state.ops.phase.id, name: this.state.ops.phase.name },
              description: this.state.ops.description,
            };
            ops.year = yearValue;
            this.setState({
              ops,
            });
            break;
          default:
            break;
        }

        const milestones = this.state.milestones;
        let phaseIndex = -1;
        const phaseItem = milestones.phases.find((phase: IPhasesItem, itemIndex: number) => {
          phaseIndex = itemIndex;
          return phase.phase.id === phaseType;
        });

        phaseItem.year = yearValue;

        milestones.phases.splice(phaseIndex, 1);
        milestones.phases.splice(phaseIndex, 0, phaseItem);

        this.setState({
          milestones,
        });
      }
    };

  protected resetMilestonesModel = () => {
    const orgMilestones = JSON.parse(JSON.stringify(this.state.milestonesList));
    if (orgMilestones.phases.length) {
      if (orgMilestones.rollouts.details && orgMilestones.rollouts.details.length) {
        const locationValue: ILocation[] = [];
        orgMilestones.rollouts.details.forEach((rolloutDetail: IRolloutDetail) => {
          locationValue.push(rolloutDetail.location);
        });
        this.setState({ rolloutDetails: orgMilestones.rollouts.details, locationValue });
      } else {
        this.setState({ locationValue: [], rolloutDetails: [] });
      }
      const milestones = this.state.milestones;
      // milestones.phases = orgMilestones.phases;
      const milestonePhases = milestones.phases;
      orgMilestones.phases.forEach((phase: IPhasesItem) => {
        const phaseItem = milestonePhases.find((phaseItemState: IPhasesItem) => {
          return phaseItemState.phase.id === phase.phase.id;
        });

        if (phaseItem) {
          phaseItem.month = phase.month;
          phaseItem.year = phase.year;
        }
      });
      milestones.rollouts.details = orgMilestones.rollouts.details;
      milestones.rollouts.description = orgMilestones.rollouts.description;
      this.setState({ milestones }, () => {
        SelectBox.defaultSetup(false);
      });
    } else {
      const milestones = this.state.milestones;
      if (milestones) {
        milestones.phases.forEach((phase: IPhasesItem) => {
          phase.month = 0;
          phase.year = 0;
        });
        if (milestones.rollouts) {
          milestones.rollouts.details = [];
        }
      }
      this.setState({ milestones, locationValue: [], rolloutDetails: [] }, () => {
        SelectBox.defaultSetup(false);
      });
    }
  };

  protected updateRolloutDetails = (location: ILocation, month: number, year: number, updateMonth: boolean) => {
    const rolloutDetails = this.state.rolloutDetails;
    let rolloutDetailIndex = -1;
    const exitingRolloutDetail = rolloutDetails.find((detail: IRolloutDetail, index: number) => {
      rolloutDetailIndex = index;
      return detail.location.id === location.id;
    });
    if (exitingRolloutDetail) {
      if (updateMonth) {
        exitingRolloutDetail.month = month;
      } else {
        exitingRolloutDetail.year = year;
      }
      rolloutDetails.splice(rolloutDetailIndex, 1);
      rolloutDetails.splice(rolloutDetailIndex, 0, exitingRolloutDetail);

      this.setState({
        rolloutDetails,
      });
    } else {
      const rolloutDetail: IRolloutDetail = {
        location,
        month: updateMonth ? month : 0,
        year: !updateMonth ? year : 0,
      };
      rolloutDetails.push(rolloutDetail);
    }

    this.setState({ rolloutDetails });
  };

  protected onCurrentPhaseRadioChange = (e: React.FormEvent<HTMLInputElement>) => {
    const phaseId = e.currentTarget.value;
    const selectedPhase = this.props.phases.find((phase: IPhase) => {
      return phase.id === phaseId;
    });
    this.setState({ currentPhase: selectedPhase });
  };

  protected onLocationChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: ILocation[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const location: ILocation = { id: null, name: null, is_row: false };
        location.id = option.value;
        location.name = option.label;
        selectedValues.push(location);
      });
    }
    const rolloutDetails = this.state.rolloutDetails;
    if (rolloutDetails.length) {
      const removableRolloutsIndexs: number[] = [];
      rolloutDetails.forEach((rollout: IRolloutDetail, index: number) => {
        const locationItem = selectedValues.find((location: ILocation) => {
          return location.id === rollout.location.id;
        });

        if (!locationItem) {
          removableRolloutsIndexs.push(index);
        }
      });

      removableRolloutsIndexs.forEach((rolloutIndex) => {
        rolloutDetails.splice(rolloutIndex, 1);
      });
      if (removableRolloutsIndexs.length) {
        this.setState({ rolloutDetails });
      }
    }
    this.setState({ locationValue: selectedValues }, () => {
      if (this.state.showMilestonesModal) {
        SelectBox.defaultSetup(true);
      }
    });
  };

  protected validateMilestones = (milestones: IMilestonesList) => {
    let milestonesFormValid = true;
    const phaseErrors = this.state.phaseErrors;
    const currentPhaseId = this.state.currentPhase.id;

    if (currentPhaseId === '6' && !this.state.locationValue.length) {
      this.setState({
        rolloutLocationValid: false,
        noMilestoneSelectedError: '*Please provide atleast one location for Operations / Rollout phase information.',
      });
      return false;
    } else {
      this.setState({
        rolloutLocationValid: true,
        noMilestoneSelectedError: '',
        rolloutLocationErrors: [],
      });
    }

    const filledPhases = milestones.phases.filter((phasesItem: IPhasesItem, index: number) => {
      phaseErrors[index].monthValid = phaseErrors[index].yearValid = true;
      if (parseInt(currentPhaseId, 10) >= parseInt(phasesItem.phase.id, 10)) {
        phaseErrors[index].monthValid = phasesItem.month > 0;
        phaseErrors[index].yearValid = phasesItem.year > 0;
      }

      return phasesItem.month > 0 && phasesItem.year > 0 && phasesItem.phase.id !== '6'; // Except Rollout
    });

    // Check for rollout also
    if (filledPhases.length <= 5 && milestones.rollouts && milestones.rollouts.details.length) {
      phaseErrors[5].monthValid = phaseErrors[5].yearValid = true;
      const rollout: IRolloutDetail = milestones.rollouts.details[0];
      if (parseInt(currentPhaseId, 10) >= 6) {
        phaseErrors[5].monthValid = rollout.month > 0;
        phaseErrors[5].yearValid = rollout.year > 0;
      }
    }

    if (!filledPhases.length) {
      this.setState({ noMilestoneSelectedError: '*Please provide atleast one milestone phase information.' });
      milestonesFormValid = false;
    } else {
      this.setState({ noMilestoneSelectedError: '' });
    }

    const inValidPhases = phaseErrors.filter((phaseError: IPhaseError) => {
      return !phaseError.monthValid || !phaseError.yearValid;
    });

    if (milestonesFormValid && inValidPhases.length) {
      this.setState({ noMilestoneSelectedError: '*Please provide milestone phase information till current phase.' });
      milestonesFormValid = false;
    }

    if (milestonesFormValid && this.state.locationValue.length && milestones.rollouts) {
      const rolloutLocationErrors: IRolloutError[] = [];
      this.state.locationValue.forEach((loc: ILocation) => {
        const rolloutError = { locationId: loc.id, monthValid: true, yearValid: true };
        if (!milestones.rollouts.details.length) {
          rolloutError.monthValid = false;
          rolloutError.yearValid = false;
        } else {
          const rollout = milestones.rollouts.details.find((rolloutDetail: IRolloutDetail) => {
            return rolloutDetail.location.id === loc.id;
          });

          if (rollout) {
            rolloutError.monthValid = rollout.month > 0;
            rolloutError.yearValid = rollout.year > 0;
          } else {
            rolloutError.monthValid = false;
            rolloutError.yearValid = false;
          }
        }

        if (!rolloutError.monthValid || !rolloutError.yearValid) {
          milestonesFormValid = false;
        }
        rolloutLocationErrors.push(rolloutError);
      });

      this.setState({
        rolloutLocationErrors,
        noMilestoneSelectedError: '*Please provide valid date information for selected rollout locations.',
      });
    }

    if (
      milestonesFormValid &&
      milestones.rollouts &&
      milestones.rollouts.details.length &&
      !this.IsRolloutLocationsDateValuesValid(milestones.rollouts.details)
    ) {
      this.setState({
        noMilestoneSelectedError: '*Please provide valid date information for selected rollout locations.',
      });
      milestonesFormValid = false;
    }

    return milestonesFormValid;
  };

  protected IsRolloutLocationsDateValuesValid = (rolloutDetails: IRolloutDetail[]) => {
    let hasValidRollouts = true;
    const rolloutLocationErrors: IRolloutError[] = [];
    rolloutDetails.forEach((rollout: IRolloutDetail) => {
      const rolloutError = { locationId: rollout.location.id, monthValid: true, yearValid: true };
      rolloutError.monthValid = rollout.month > 0;
      rolloutError.yearValid = rollout.year > 0;
      rolloutLocationErrors.push(rolloutError);
      if (!rolloutError.monthValid || !rolloutError.yearValid) {
        hasValidRollouts = false;
      }
    });

    this.setState({ rolloutLocationErrors });

    return hasValidRollouts;
  };

  protected onMilestonesSubmit = () => {
    const milestones = this.state.milestones;
    const currentPhase = this.state.currentPhase;
    if (this.validateMilestones(milestones)) {
      this.setState({ milestonesErrorMessage: null });
      this.props.modifyMileStones(milestones, currentPhase);
      this.props.onSaveDraft('milestones');
    } else {
      this.setState({ milestonesErrorMessage: '*Please add minimum one milestone.' });
    }
  };
}
