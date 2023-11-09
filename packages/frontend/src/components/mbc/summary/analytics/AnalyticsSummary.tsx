import cn from 'classnames';
import * as React from 'react';
import { IAnalytics } from 'globals/types';
import Styles from './AnalyticsSummary.scss';
const classNames = cn.bind(Styles);

export interface ITeamProps {
  analytics: IAnalytics;
  isGenAI: boolean;
}

export default function AnalyticsSummary(props: ITeamProps) {
  const languageChips =
    props.analytics.languages && props.analytics.languages.length > 0
      ? props.analytics.languages.map((chip: any, index: any) => {
        const lastIndex: boolean = index === props.analytics.languages.length - 1;
        return (
          <React.Fragment key={index}>
            {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
          </React.Fragment>
        );
      })
      : 'NA';
  const algoChips =
    props.analytics.algorithms && props.analytics.algorithms.length > 0
      ? props.analytics.algorithms.map((chip: any, index: any) => {
        const lastIndex: boolean = index === props.analytics.algorithms.length - 1;
        return (
          <React.Fragment key={index}>
            {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
          </React.Fragment>
        );
      })
      : 'NA';
  const visualizationChips =
    props.analytics.visualizations && props.analytics.visualizations.length > 0
      ? props.analytics.visualizations.map((chip: any, index: any) => {
        const lastIndex: boolean = index === props.analytics.visualizations.length - 1;
        return (
          <React.Fragment key={index}>
            {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
          </React.Fragment>
        );
      })
      : 'NA';
  const solutionsChips =
    props.analytics.analyticsSolution && props.analytics.analyticsSolution.length > 0
      ? props.analytics.analyticsSolution.map((chip: any, index: any) => {
        const lastIndex: boolean = index === props.analytics.analyticsSolution.length - 1;
        return (
          <React.Fragment key={index}>
            {chip.name}&nbsp;{!lastIndex && `\u002F\xa0`}
          </React.Fragment>
        );
      })
      : 'NA';
  return (
    <React.Fragment>
      <div className={classNames(Styles.flexLayout, Styles.mainPanel, 'mainPanelSection')}>
        <div id="analyticsSummary" className={Styles.wrapper}>
          <div>
            <h3>{props.isGenAI ? 'Technology' : 'Analytics'}</h3>
          </div>
          <div className={Styles.firstPanel}>
            <div className={classNames(Styles.flexLayout)}>
              {!props.isGenAI && <div id="languageTags" className={classNames(Styles.solutionSection)}>
                <label className="input-label summary">Languages</label>
                <br />
                <div className={classNames(Styles.row)}>{languageChips}</div>
              </div>}
              <div id="algorithmTags" className={classNames(Styles.solutionSection)}>
                <label className="input-label summary">{props.isGenAI ? 'GenAI Models' : 'Models/Algorithms'}</label>
                <br />
                <div className={classNames(Styles.row)}>{algoChips}</div>
              </div>
              {!props.isGenAI && <div id="visualizationTags" className={classNames(Styles.solutionSection)}>
                <label className="input-label summary">Visualization</label>
                <br />
                <div className={classNames(Styles.row)}>{visualizationChips}</div>
              </div>}
              {props.isGenAI && <div id="solutionsTags" className={classNames(Styles.solutionSection)}>
                <label className="input-label summary">Solutions</label>
                <br />
                <div className={classNames(Styles.row)}>{solutionsChips}</div>
              </div>}
              {props.isGenAI && <div className={classNames(Styles.solutionSection)}>
                <label className="input-label summary">&nbsp;</label>
              </div>}
            </div>
          </div>
        </div>
      </div>{' '}
    </React.Fragment>
  );
}
