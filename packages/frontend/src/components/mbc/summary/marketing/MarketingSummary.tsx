import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import { IMarketing } from 'globals/types';
import Styles from './MarketingSummary.scss';
import PersonaSelect from './../../shared/personaSelect/PersonaSelect';
// import PersonaAvatar from '../../../../assets/images/team-internal-avatar.jpg';
import LiAvatar from '../../../../assets/images/li.png';
import HenryAvatar from '../../../../assets/images/henry.png';
import VictoriaAvatar from '../../../../assets/images/victoria.png';
import TomAvatar from '../../../../assets/images/tom.png';

const classNames = cn.bind(Styles);

const personas = [
  {
    id: 1,
    avatar: LiAvatar,
    name: 'Li',
    description: 'The digital Mercedes-Benz enthusiast',
    value: 'enthusiast'
  },
  {
    id: 2,
    avatar: HenryAvatar,
    name: 'Henry',
    description: 'The demanding quality-seeker',
    value: 'quality-seeker'
  },
  {
    id: 3,
    avatar: VictoriaAvatar,
    name: 'Victoria',
    description: 'The ambitious self-optimizer',
    value: 'self-optimizer'
  },
  {
    id: 4,
    avatar: TomAvatar,
    name: 'Tom',
    description: 'The price-conscious tech-enthusiast',
    value: 'tech-enthusiast'
  },
];

export interface IMarketingProps {
  marketing: IMarketing;
}

export default class MarketingSummary extends React.Component<IMarketingProps, any> {
  constructor(props: IMarketingProps) {
    super(props);
  }

  protected onPersonaChange = (personas: string[]) => {
    const marketing = this.props.marketing;
    marketing.personas = personas;
    this.setState({
        marketing,
    });  
  }

  protected formattedTitleOfCustomerPhase = (text: string) => {
    const tempText = text.split('<')[0];
    const strRegExp = new RegExp(tempText.trim(), 'g');
    return text.replace(strRegExp, `<b>${tempText}</b>`);
  }

  public render() {
    const personasToShow = personas.filter(item=> this.props.marketing?.personas?.includes(item.value));
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
                                <label className="input-label summary">Use Case, Core Needs and Customer Journey Phase</label>
                                <br />
                                {this.props?.marketing?.customerJourneyPhases?.length > 0 ? this.props?.marketing?.customerJourneyPhases?.map((item, index)=>
                                  {return (<div key={index} dangerouslySetInnerHTML={{__html: this.formattedTitleOfCustomerPhase(item.name)}}></div>)}) : 'N/A'}
                            </div>
                            <div id="marketingCommunicationChannel">
                                <label className="input-label summary">Marketing Communication Channels</label>
                                <br />
                                {this.props?.marketing?.marketingCommunicationChannels?.length > 0 ? this.props?.marketing?.marketingCommunicationChannels?.map(item=>item.name).join(', ') : 'N/A'}
                            </div>
                            <div id="personalization">
                              <label className="input-label summary">Personalization Description</label>
                              <br />                    
                              <div className={Styles.solutionDescription}>
                                  <pre className={Styles.solutionPre}>
                                      {this.props?.marketing?.personalization?.description ? this.props?.marketing?.personalization?.description : 'N/A'}
                                  </pre>
                              </div>
                            </div>                           
                        </div>
                        <div className={classNames(Styles.personaWrapper)}>
                            <PersonaSelect 
                            personas={personasToShow}
                            selectedPersonasList={this.props.marketing.personas}
                            isSummary={true}
                            onChangePersonas={this.onPersonaChange}></PersonaSelect>
                        </div>              
                    </div>
                </div>              
            </div>
            <div>
              <h3>Marketing Roles</h3>
              <div id="marketingRoles" className={Styles.firstPanel}>
                <div className={classNames(Styles.marketingRoleWrapper)}>
                {this.props?.marketing?.marketingRoles?.length > 0 ? this.props?.marketing?.marketingRoles?.map((item, index)=>
                      {return (<div key={index}>{item.role}</div>)}) : 'N/A'}
                </div>
              </div>
            </div>
          </div>
        </div>{' '}
      </React.Fragment>
    );
  }
}
