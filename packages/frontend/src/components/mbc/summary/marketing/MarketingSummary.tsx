import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import { IMarketing } from 'globals/types';
import Styles from './MarketingSummary.scss';

const classNames = cn.bind(Styles);

export interface IMarketingProps {
  marketing: IMarketing;
}

export default class MarketingSummary extends React.Component<IMarketingProps, any> {
  constructor(props: IMarketingProps) {
    super(props);
  }

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.flexLayout, Styles.mainPanel, 'mainPanelSection')}>
          <div id="teamMembersWrapper" className={Styles.wrapper}>
            <div>
              <h3>Marketing</h3>
            </div>
            <div id="marketing">
                <div className={Styles.firstPanel}>
                    <div className={Styles.formWrapper}>
                        <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                            <div id="customerJourneyPhase">
                                <label className="input-label summary">Customer Journey Phases</label>
                                <br />
                                {this.props.marketing.customerJourneyPhases.length > 0 ? this.props.marketing.customerJourneyPhases.map(item=>item.name).join(', ') : 'N/A'}
                            </div>
                            <div id="marketingCommunicationChannel">
                                <label className="input-label summary">Marketing Communication Channels</label>
                                <br />
                                {this.props.marketing.marketingCommunicationChannels.length > 0 ? this.props.marketing.marketingCommunicationChannels.map(item=>item.name).join(', ') : 'N/A'}
                            </div>
                            <div id="personas">
                                <label className="input-label summary">Personas</label>
                                <br />
                                {this.props.marketing.personas.length > 0 ? this.props.marketing.personas.join(', ') : 'N/A'}
                            </div>                            
                        </div>
                        <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                            <div id="personalization">
                                <label className="input-label summary">Personalization Description</label>
                                <br />                    
                                <div className={Styles.solutionDescription}>
                                    <pre className={Styles.solutionPre}>
                                        {this.props.marketing.personalization.description}
                                    </pre>
                                </div>
                            </div>
                        </div>                        
                    </div>
                </div>              
            </div>
          </div>
        </div>{' '}
      </React.Fragment>
    );
  }
}
