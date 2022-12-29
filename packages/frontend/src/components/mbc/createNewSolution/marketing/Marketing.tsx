import cn from 'classnames';
import * as React from 'react';
import { IResult, IMarketing, IMarketingCustomerJourney } from 'globals/types';
import Styles from './Marketing.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import TextArea from 'components/mbc/shared/textArea/TextArea';
// import TextBox from 'components/mbc/shared/textBox/TextBox';
const classNames = cn.bind(Styles);

export interface IMarketingProps {
  onSaveDraft: (tabToBeSaved: string) => void;
  marketing: IMarketing;
  modifyMarketing: (analytics: IMarketing) => void;
  results: IResult[];
}

export interface IMarketingState {
  marketing: IMarketing;
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
            personas: []
          },
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Marketing</h3>
            <div className={classNames(Styles.formWrapper)}>
                <div className={Styles.flexLayout}>   
                    <div>
                        <div id="customerJourneyPhaseContainer" className={classNames('input-field-group')}>
                            <label id="customerJourneyPhaselabel" className="input-label" htmlFor="customerJourneyPhaseSelect">
                            Customer Journey Phase
                            </label>
                            <div id="customerJourneyPhase" className="custom-select">
                                <select 
                                id="customerJourneyPhaseSelect" 
                                multiple={true}
                                onChange={this.onCustomerJourneyChange} 
                                value={this.state.marketing.customerJourneyPhases.map(item => item.name)}>
                                {this.props.results.map((obj) => (
                                    <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
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
                                {this.props.results.map((obj) => (
                                    <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                    {obj.name}
                                    </option>
                                ))}
                                </select>
                            </div>
                        </div>
                    </div>   
                    <div>                
                        <div>
                            <label className="checkbox">
                                <span className="wrapper">
                                <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.expertGuidelineNeeded}
                                    // onChange={this.onChangeOfExpertGuidelines}
                                />
                                </span>
                                <span className={classNames("label")}>Personalized customer experience</span>
                            </label>
                        </div>
                        <div className={Styles.description}>
                            <TextArea
                                controlId={'description'}
                                containerId={'descriptionContainer'}
                                labelId={'descriptionLabel'}
                                label={'Please describe the type of personalization being activated with your use case and the segmentation used to identify the target audience for the marketing activitiy.'}
                                rows={50}
                                value={''}
                                required={true}
                                onChange={()=>this.onDescriptionChange}
                            />
                        </div>
                    </div>                      
                </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onMarketingSubmit}>
            Save & Next
          </button>
        </div>
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.marketing) {
      const marketing = this.state.marketing;
      marketing.customerJourneyPhases = this.props.marketing.customerJourneyPhases;
      marketing.marketingCommunicationChannels = this.props.marketing.marketingCommunicationChannels;
      marketing.personalization = this.props.marketing.personalization;
      marketing.personas = this.props.marketing.personas;
    }
  };
  protected onMarketingSubmit = () => {
    this.props.modifyMarketing(this.state.marketing);
    this.props.onSaveDraft('marketing');
  };

  protected onCustomerJourneyChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const customerJourneyPhase: IMarketingCustomerJourney = { id: null, name: null };
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        customerJourneyPhase.id = option.value;
        customerJourneyPhase.name = option.label;
      });
    }
    const marketing = this.state.marketing;
    marketing.customerJourneyPhases.push(customerJourneyPhase);
    this.setState({ marketing });
    this.props.modifyMarketing(marketing);
  };
  

  protected onMarketingCommunicationChannelChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const customerJourneyPhase: IMarketingCustomerJourney = { id: null, name: null };
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        customerJourneyPhase.id = option.value;
        customerJourneyPhase.name = option.label;
      });
    }
    const marketing = this.state.marketing;
    marketing.customerJourneyPhases.push(customerJourneyPhase);
    this.setState({ marketing });
    this.props.modifyMarketing(marketing);
  };

  protected onDescriptionChange = (e: React.FormEvent<HTMLInputElement>) => {
    const resultUrl = e.currentTarget.value;
    const marketing = this.state.marketing;
    marketing.personalization.description = resultUrl;
    this.setState({
        marketing,
    });
    this.props.modifyMarketing(marketing);
  };
}
