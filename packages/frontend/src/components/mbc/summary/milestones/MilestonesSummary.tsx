import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { IconConceptDevelopment } from 'components/icons/IconConceptDevelopment';
import { IconIdeation } from 'components/icons/IconIdeation';
import { IconKickOff } from 'components/icons/IconKickOff';
import { IconOperations } from 'components/icons/IconOperations';
import { IconPilot } from 'components/icons/IconPilot';
import { IconProfessionalization } from 'components/icons/IconProfessionalization';
import { IMilestonesList, IPhase, IPhasesItem } from 'globals/types';
import { regionalForMonthAndYear } from '../../../../services/utils';

import Styles from './MilestonesSummary.scss';
const classNames = cn.bind(Styles);

export interface IMileStonesState {
  showMilestonesModal?: boolean;
  milestones?: IMilestonesList;
  kickoff?: IPhasesItem;
  ideation?: IPhasesItem;
  poc?: IPhasesItem;
  pilot?: IPhasesItem;
  prof?: IPhasesItem;
  ops?: IPhasesItem;
}
export interface IMilestonesProps {
  phases: IPhase[];
  // currentPhase: IPhase;
  milestones?: IMilestonesList;
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
export default class MilestonesSummary extends React.Component<IMilestonesProps, IMileStonesState> {
  public static getDerivedStateFromProps(props: IMilestonesProps, state: IMileStonesState) {
    if (props && props.milestones && props.milestones.rollouts.details.length > 0) {
      const milestones: IMilestonesList = state.milestones;
      milestones.rollouts.details = props.milestones.rollouts.details;
    }
    if (props && props.milestones && props.milestones.phases.length > 0) {
      const milestones: IMilestonesList = state.milestones;
      milestones.phases.forEach((milestone) => {
        if (milestone) {
          props.milestones.phases.forEach((propMileStone) => {
            if (milestone.phase.id === propMileStone.phase.id) {
              milestone.description = propMileStone.description;
              milestone.month = propMileStone.month;
              milestone.phase = propMileStone.phase;
              milestone.year = propMileStone.year;
            }
          });
        }
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
      return {
        milestones,
        kickoff: defaultKickOff,
        ideation: defaultIdeation,
        poc: defaultPoc,
        pilot: defaultPilot,
        prof: defaultProf,
        ops: defaultOps,
      };
    }
    return null;
  }

  public mileStoneIcons = [
    <IconKickOff key={1} />,
    <IconIdeation key={2} />,
    <IconConceptDevelopment key={3} />,
    <IconPilot key={4} />,
    <IconProfessionalization key={5} className={Styles.proffIcon} />,
    <IconOperations key={6} />,
  ];
  constructor(props: any) {
    super(props);
    this.state = {
      milestones: { phases: [], rollouts: { details: [], description: '' } },
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
    };
    const milestones = this.state.milestones.phases;
    milestones.push(this.state.kickoff);
    milestones.push(this.state.ideation);
    milestones.push(this.state.poc);
    milestones.push(this.state.pilot);
    milestones.push(this.state.prof);
    milestones.push(this.state.ops);
  }
  // public componentDidMount() {
  //   const milestones = this.state.milestones.phases;
  //   milestones.push(this.state.kickoff);
  //   milestones.push(this.state.ideation);
  //   milestones.push(this.state.poc);
  //   milestones.push(this.state.pilot);
  //   milestones.push(this.state.prof);
  //   milestones.push(this.state.ops);
  // }
  public canShowPhasesPlaceHolder(): boolean {
    let showPhasesPlaceHolder = false;
    this.state.milestones.phases.forEach((milestone) => {
      if (milestone.month && milestone.year) {
        showPhasesPlaceHolder = true;
      }
    });

    return showPhasesPlaceHolder;
  }

  public render() {
    const canShowPhasesPlaceHolder = this.canShowPhasesPlaceHolder();
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          <div id="milestonesViewWrapper" className={Styles.wrapper}>
            <h3>Milestones</h3>
            <div className={classNames(Styles.flexLayout, Styles.milestonesWrapper, 'active')}>
              {this.state.milestones.phases.map((milestone, index) => {
                if (milestone) {
                  const showPhase = milestone.month && milestone.year;
                  return (
                    <div key={milestone.phase.id} className={showPhase ? Styles.active : ''}>
                      <div
                        className={classNames(
                          Styles.info,
                          showPhase ? '' : canShowPhasesPlaceHolder ? 'hidden' : 'hide',
                        )}
                      >
                        <div>{this.mileStoneIcons[index]}</div>
                        <div className={classNames(Styles.phase, '')}>{milestone.phase.name}</div>
                        <div className={Styles.monthYear}>
                          {/* {milestone.month >= 10 ? milestone.month : '0' + milestone.month}/{milestone.year} */}
                          {milestone.month > 0 && milestone.year > 0
                            ? regionalForMonthAndYear(milestone.month + '/' + '01' + '/' + milestone.year)
                            : ''}
                        </div>
                      </div>

                      <div className={Styles.divider}>
                        <span className={classNames(Styles.dot, Styles.left)} />
                        <span className={Styles.line} />
                        <span className={classNames(Styles.dot, Styles.right)} />
                      </div>
                      {milestone.description && (
                        <div className={Styles.comment}>
                          <p>{milestone.description}</p>
                        </div>
                      )}
                    </div>
                  );
                }
              })}
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
        </div>
      </React.Fragment>
    );
  }
}
