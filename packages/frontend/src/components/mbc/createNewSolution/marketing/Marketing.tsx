import cn from 'classnames';
import * as React from 'react';
import { IMarketing, IMarketingCustomerJourney, IMarketingCommunicationChannel, IMarketingRole } from 'globals/types';
import Styles from './Marketing.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import TextArea from 'components/mbc/shared/textArea/TextArea';
import PersonaSelect from './../../shared/personaSelect/PersonaSelect';
// import PersonaAvatar from '../../../../assets/images/team-internal-avatar.jpg';
import LiAvatar from '../../../../assets/images/li.png';
import HenryAvatar from '../../../../assets/images/henry.png';
import VictoriaAvatar from '../../../../assets/images/victoria.png';
import TomAvatar from '../../../../assets/images/tom.png';
import RoleSelect from 'components/mbc/shared/roleSelect/RoleSelect';
// import { ApiClient } from '../../../../services/ApiClient';

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
  marketingRolesLOV: IMarketingRole[];
  onSaveDraft: (tabToBeSaved: string) => void;
  marketing: IMarketing;
  modifyMarketing: (analytics: IMarketing) => void;
  marketingCommunicationChannelsLOV: IMarketingCommunicationChannel[];
  customerJourneyPhasesLOV: IMarketingCustomerJourney[];
}

export interface IMarketingState {
  marketing: IMarketing;
  showDescription: boolean;
  descriptionError: string;
  neededRoleMaster: any;
}

export default class Marketing extends React.Component<IMarketingProps, IMarketingState> {
  public static getDerivedStateFromProps(props: IMarketingProps, state: IMarketingState) {
    return {
        marketing: props.marketing,
    };
  }
  constructor(props: IMarketingProps) {
    super(props);
    this.state = {
        marketing: this.props.marketing
        ? this.props.marketing
        : {
            customerJourneyPhases: [],
            marketingCommunicationChannels: [],
            personalization: {isChecked: false,description: ''},
            personas: [],
            marketingRoles: []
          },
        showDescription: false,
        descriptionError: null,
        neededRoleMaster: []
    };
  }

  // public componentDidMount() {
    
  // }
  componentDidMount() {
    SelectBox.defaultSetup();

    // ApiClient.getSkills().then((response: any) => {
    //   if (response) {
    //     this.setState(
    //       {
    //         neededRoleMaster: response,
    //       },
    //       () => {
    //         SelectBox.defaultSetup();
    //       },
    //     );
    //   }
    // });

    // this.setState(
    //   {
    //     neededRoleMaster: this.props.marketingRolesLOV,
    //   },
    //   () => {
    //     SelectBox.defaultSetup();
    //   },
    // );
  }

  public render() {
    const descriptionError = this.state.descriptionError || '';
    return (
      <React.Fragment>
        <div className={classNames(Styles.marketingWrapper)}>
            <div className={classNames(Styles.firstPanel)}>
                <h3>Marketing</h3>
                <div className={classNames(Styles.formWrapper)}>
                  <div className={Styles.flexLayout}>   
                      
                    <div id="customerJourneyPhaseContainer" className={classNames('input-field-group')}>
                        <label id="customerJourneyPhaselabel" className="input-label" htmlFor="customerJourneyPhaseSelect">
                        Use Case, Core Needs and Customer Journey Phase
                        </label>
                        <div id="customerJourneyPhase" className="custom-select">
                            <select 
                            id="customerJourneyPhaseSelect" 
                            multiple={true}
                            onChange={this.onCustomerJourneyChange} 
                            value={this.state.marketing.customerJourneyPhases.map(item => item.name)}>
                            {this.props?.customerJourneyPhasesLOV?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                                </option>
                            ))}
                            </select>
                        </div>
                    </div>

                    <div id="marketingCommunicationChannelContainer" className={classNames('input-field-group')}>
                        <label id="marketingCommunicationChannellabel" className="input-label" htmlFor="marketingCommunicationChannelSelect">
                        Marketing Communication Channel
                        </label>
                        <div id="marketingCommunicationChannel" className="custom-select">
                            <select id="marketingCommunicationChannelSelect" 
                            multiple={true}
                            onChange={this.onMarketingCommunicationChannelChange} 
                            value={this.state.marketing.marketingCommunicationChannels.map(item => item.name)}>
                            {this.props?.marketingCommunicationChannelsLOV?.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                                </option>
                            ))}
                            </select>
                        </div>
                    </div>
                                           
                  </div>
                </div>
            </div>
        </div>
        <div className={classNames(Styles.personalizedExperienceWrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Personalization</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={Styles.checkboxWrapper}>
                  <label className="checkbox">
                      <span className="wrapper">
                      <input
                          type="checkbox"
                          className="ff-only"
                          checked={this.state.marketing?.personalization?.isChecked ? this.state.marketing?.personalization?.isChecked : false}
                          onChange={this.onPersonalizationCheckBoxChange}
                      />
                      </span>
                      <span className={classNames("label")}>Personalized customer experience</span>
                  </label>
              </div>
              {
                this.state.marketing.personalization.isChecked ? 
                <div className={Styles.description}>
                    <TextArea
                        controlId={'description'}
                        containerId={'descriptionContainer'}
                        labelId={'descriptionLabel'}
                        label={'Please describe the type of personalization being activated with your use case and the segmentation used to identify the target audience for the marketing activitiy.'}
                        rows={50}
                        value={this.state.marketing.personalization.description}
                        errorText={descriptionError}
                        required={true}
                        onChange={this.onDescriptionChange}
                    />
                </div>
                : ''
              }
            </div>  
          </div>              
        </div> 
        <div className={classNames(Styles.personaWrapper)}>
            <PersonaSelect 
            personas={personas}
            selectedPersonasList={this.props.marketing.personas}
            isSummary={false}
            onChangePersonas={this.onPersonaChange}></PersonaSelect>
        </div>
        <div className={classNames(Styles.personaWrapper)}>
          <RoleSelect 
          neededRoleMaster={this.props.marketingRolesLOV} 
          onRoleChange={this.onRoleChange}
          neededRoles={this.state.marketing.marketingRoles}
          ></RoleSelect>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onMarketingSubmit}>
            Save & Next
          </button>
        </div>
      </React.Fragment>
    );
  }

  protected onRoleChange = (roles: any) => {
    const {marketing} = this.state;
    marketing.marketingRoles = roles;
    this.setState({marketing});
  }
  public resetChanges = () => {
    if (this.props?.marketing) {
      const marketing = this.state?.marketing;
      marketing.customerJourneyPhases = this.props?.marketing?.customerJourneyPhases;
      marketing.marketingCommunicationChannels = this.props?.marketing?.marketingCommunicationChannels;
      marketing.personalization = this.props?.marketing?.personalization;
      marketing.personas = this.props?.marketing?.personas;
    }
  };
  protected validateMarketingForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if(this.state.marketing.personalization.isChecked){
      if(!this.state.marketing.personalization.description || this.state.marketing.personalization.description === ''){
        this.setState({ descriptionError: errorMissingEntry });
        formValid = false;
      }       
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };
  protected onMarketingSubmit = () => {
    if (this.validateMarketingForm()) {
      this.props.modifyMarketing(this.state.marketing);
      this.props.onSaveDraft('marketing');
    }
  };

  protected onCustomerJourneyChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const customerJourneyPhases: IMarketingCustomerJourney[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const customerJourneyPhase: IMarketingCustomerJourney = { id: null, name: null };
        customerJourneyPhase.id = option.value;
        customerJourneyPhase.name = option.label;
        customerJourneyPhases.push(customerJourneyPhase);
      });
    }
    const marketing = this.state.marketing;
    marketing.customerJourneyPhases = customerJourneyPhases;
    this.setState({ marketing });
  };
  

  protected onMarketingCommunicationChannelChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const marketingCommunicationChannels: IMarketingCommunicationChannel[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const marketingCommunicationChannel: IMarketingCommunicationChannel = { id: null, name: null };
        marketingCommunicationChannel.id = option.value;
        marketingCommunicationChannel.name = option.label;
        marketingCommunicationChannels.push(marketingCommunicationChannel);
      });
    }
    const marketing = this.state.marketing;
    marketing.marketingCommunicationChannels = marketingCommunicationChannels;
    this.setState({ marketing });
  };

  protected onDescriptionChange = (e: React.FormEvent<HTMLTextAreaElement>) => {
    const desc = e.currentTarget.value;
    const marketing = this.props.marketing;
    marketing.personalization.description = desc;
    if (desc === '' || desc === null) {
      this.setState({ descriptionError: '*Missing Entry' });
    } else {
      this.setState({ descriptionError: '' });
    }
    this.setState({
        marketing
    });
  };

  protected onPersonaChange = (personas: string[]) => {
    const marketing = this.props.marketing;
    marketing.personas = personas;
    this.setState({
        marketing,
    });  
  }

  protected onPersonalizationCheckBoxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const checkboxValue = e.target.checked;
    const marketing = this.props.marketing;
    if(checkboxValue){
        marketing.personalization.isChecked = true;
    } else {
        marketing.personalization.isChecked = false;
        marketing.personalization.description = '';
    }
    this.setState({marketing})
  }
}
