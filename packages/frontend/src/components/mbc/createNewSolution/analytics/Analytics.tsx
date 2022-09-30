import cn from 'classnames';
import * as React from 'react';
import { IAnalytics, ITag } from 'globals/types';
import Tags from 'components/formElements/tags/Tags';
import Styles from './Analytics.scss';
const classNames = cn.bind(Styles);

export interface IAnalyticsProps {
  analytics: IAnalytics;
  onSaveDraft: (tabToBeSaved: string) => void;
  modifyAnalytics: (analytics: IAnalytics) => void;
  languages: ITag[];
  visualizations: ITag[];
  algorithms: ITag[];
}

export interface IAnalyticsState {
  analytics: IAnalytics;
}

export default class Analytics extends React.Component<IAnalyticsProps, IAnalyticsState> {
  public static getDerivedStateFromProps(props: IAnalyticsProps, state: IAnalyticsState) {
    if (props.analytics) {
      const analytics = state.analytics;
      if (props.analytics.languages && props.analytics.languages.length > 0) {
        analytics.languages = props.analytics.languages;
      }
      if (props.analytics.algorithms && props.analytics.algorithms.length > 0) {
        analytics.algorithms = props.analytics.algorithms;
      }
      if (props.analytics.visualizations && props.analytics.visualizations.length > 0) {
        analytics.visualizations = props.analytics.visualizations;
      }
      return {
        analytics,
      };
    }
    return null;
  }
  constructor(props: IAnalyticsProps) {
    super(props);
    this.state = {
      analytics: {
        languages: [],
        algorithms: [],
        visualizations: [],
      },
    };
  }

  public render() {
    const languageChips: string[] = [];
    if (this.props.analytics) {
      this.props.analytics.languages.forEach((lang) => {
        languageChips.push(lang.name);
      });
    }

    const algoChips: string[] = [];
    if (this.props.analytics) {
      this.props.analytics.algorithms.forEach((lang) => {
        algoChips.push(lang.name);
      });
    }

    const visualChips: string[] = [];
    if (this.props.analytics) {
      this.props.analytics.visualizations.forEach((lang) => {
        visualChips.push(lang.name);
      });
    }
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Analytics</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={Styles.flexLayout}>
                <div>
                  <div>
                    <Tags
                      title={'Languages'}
                      max={100}
                      chips={languageChips}
                      setTags={this.setLanguages}
                      tags={this.props.languages}
                      showMissingEntryError={false}
                      {...this.props}
                    />
                  </div>
                  <div>
                    <Tags
                      title={'Visualization'}
                      max={100}
                      chips={visualChips}
                      setTags={this.setVisuals}
                      tags={this.props.visualizations}
                      showMissingEntryError={false}
                      {...this.props}
                    />
                  </div>
                </div>
                <div>
                  <Tags
                    title={'Models/Algorithms'}
                    max={100}
                    chips={algoChips}
                    setTags={this.setAlgos}
                    tags={this.props.algorithms}
                    showMissingEntryError={false}
                    {...this.props}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onAnalyticsSubmit}>
            Save & Next
          </button>
        </div>
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.analytics) {
      const analytics = this.state.analytics;
      // let languages = analytics.languages;
      // if (this.props.analytics.languages && this.props.analytics.languages.length > 0) {
      //   languages = this.props.analytics.languages;
      // } else if (
      //   this.props.analytics.languages &&
      //   this.props.analytics.languages.length === 0 &&
      //   languages &&
      //   languages.length > 0
      // ) {
      //   languages.forEach(lang => languages.pop());
      // }
      // let algorithms = analytics.algorithms;
      // if (this.props.analytics.algorithms && this.props.analytics.algorithms.length > 0) {
      //   algorithms = this.props.analytics.algorithms;
      // } else if (
      //   this.props.analytics.algorithms &&
      //   this.props.analytics.algorithms.length === 0 &&
      //   algorithms &&
      //   algorithms.length > 0
      // ) {
      //   algorithms.forEach(algo => algorithms.pop());
      // }
      // let visualizations = this.props.analytics.visualizations;
      // if (this.props.analytics.visualizations && this.props.analytics.visualizations.length > 0) {
      //   visualizations = this.props.analytics.visualizations;
      // } else if (
      //   this.props.analytics.visualizations &&
      //   this.props.analytics.visualizations.length === 0 &&
      //   visualizations &&
      //   visualizations.length > 0
      // ) {
      //   visualizations.forEach(visu => visualizations.pop());
      // }
      // analytics.languages = languages;
      // analytics.algorithms = algorithms;
      // analytics.visualizations = visualizations;
      analytics.languages = this.props.analytics.languages;
      analytics.algorithms = this.props.analytics.algorithms;
      analytics.visualizations = this.props.analytics.visualizations;
      this.setState({ analytics });
    }
  };
  protected onAnalyticsSubmit = () => {
    this.props.modifyAnalytics(this.state.analytics);
    this.props.onSaveDraft('analytics');
  };

  protected setLanguages = (arr: string[]) => {
    const temp: string[] = [];
    const analytics = this.state.analytics;
    const mainLanList = this.props.languages;
    const langs = mainLanList.filter((l1) => {
      if (arr.includes(l1.name)) {
        temp.push(l1.name);
        return true;
      } else {
        return false;
      }
    });
    arr.forEach((a) => {
      if (!temp.includes(a)) {
        langs.push({ id: null, name: a });
      }
    });
    analytics.languages = langs;
    this.props.modifyAnalytics(analytics);
  };

  protected setAlgos = (arr: string[]) => {
    const temp: string[] = [];
    const analytics = this.state.analytics;
    const algorithms = this.props.algorithms;
    const algos = algorithms.filter((l1) => {
      if (arr.includes(l1.name)) {
        temp.push(l1.name);
        return true;
      } else {
        return false;
      }
    });
    arr.forEach((a) => {
      if (!temp.includes(a)) {
        algos.push({ id: null, name: a });
      }
    });
    analytics.algorithms = algos;
    this.props.modifyAnalytics(analytics);
  };

  protected setVisuals = (arr: string[]) => {
    const temp: string[] = [];

    const analytics = this.state.analytics;
    const visualizations = this.props.visualizations;
    const visuals = visualizations.filter((l1) => {
      if (arr.includes(l1.name)) {
        temp.push(l1.name);
        return true;
      } else {
        return false;
      }
    });
    arr.forEach((a) => {
      if (!temp.includes(a)) {
        visuals.push({ id: null, name: a });
      }
    });
    analytics.visualizations = visuals;
    this.props.modifyAnalytics(analytics);
  };
}
