import cn from 'classnames';
import * as React from 'react';
import InfoModal from '../../../../components/formElements/modal/infoModal/InfoModal';
// @ts-ignore
import ExpansionPanel from '../../../../assets/modules/uilab/js/src/expansion-panel';
// @ts-ignore
import InputFields from '../../../../assets/modules/uilab/js/src/input-fields';
// @ts-ignore
import ImgCostDriver from '../../../../assets/images/cost-driver-info.png';
// @ts-ignore
import ImgMaturityLevel from '../../../../assets/images/maturity-level-info.png';
// @ts-ignore
import ImgRiskAssesment from '../../../../assets/images/risk-assesment-info.png';
// @ts-ignore
import ImgValueDriver from '../../../../assets/images/value-driver-info.png';
import { IconAvatarNew } from '../../../../components/icons/IconAvatarNew';
import {
  IAssessment,
  IAttachment,
  IBenefitRelevance,
  ICostFactor,
  ICostRampUp,
  IDigitalValue,
  IMaturityLevel,
  IStrategicRelevance,
  ITeams,
  IValueFactor,
  IValueRampUp,
} from '../../../../globals/types';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';
import AttachmentUploader from '../AttachmentUploader/AttachmentUploader';
import AddTeamMemberModal from '../../addTeamMember/addTeamMemberModal/AddTeamMemberModal';
import TeamMemberListItem from '../../addTeamMember/teamMemberListItem/TeamMemberListItem';
import AddOrEditFactorModal from './addOrEditFactorModal/AddOrEditFactorModal';
import Styles from './DigitalValue.scss';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import {IntlProvider, FormattedNumber} from 'react-intl';

const classNames = cn.bind(Styles);

export interface IDigitalValueProps {
  digitalValue: IDigitalValue;
  maturityLevelsList: IMaturityLevel[];
  benefitRelevancesList: IBenefitRelevance[];
  strategicRelevancesList: IStrategicRelevance[];
  onSaveDraft: (tabToBeSaved: string) => void;
  onPublish: () => void;
  modifyDigitalValue: (digitalValue: IDigitalValue) => void;
}

export interface IDigitalValueState {
  digitalValue: string;
  digitalValueError: string;
  effortValue: string;
  effortValueError: string;
  digitalValueComment: string;
  effortValueComment: string;
  currentFieldValue: string;
  showCommentArea: boolean;
  commentValueError: string;
  commentValue: string;
  showAddTeamMemberModal: boolean;
  editTeamMember: boolean;
  editTeamMemberIndex: number;
  contollerTeamMembers: ITeams[];
  isControllerMember: boolean;
  sharingTeamMembers: ITeams[];
  teamMemberObj: ITeams;
  addTeamMemberInController: boolean;
  attachments: IAttachment[];
  showStrategyCommentArea: boolean;
  currentStrategyFieldValue: string;
  strategyCommentValueError: string;
  strategyCommentValue: string;
  strategicRelevance: string;
  strategicRelevanceComment: string;
  benefitRealizationRisk: string;
  benefitRelevanceComment: string;
  showMaturityLevelInfoModal: boolean;
  showCostDriverInfoModal: boolean;
  showValueDriverInfoModal: boolean;
  showRiskAssesmentInfoModal: boolean;
  maturityLevel: string;
  costDrivers: ICostFactor[];
  valueDrivers: IValueFactor[];
  currentSelectedFactor: string;
  currentSelectedFactorDescription: string;
  editFactor: boolean;
  showAddOrEditFactorModal: boolean;
  factorItem: ICostFactor | IValueFactor;
  indexToEdit: null | number;
  showDeleteModal: boolean;
  indexToDelete: null | number;
  assessment: IAssessment;
}

export interface IAttachmentResponse {
  error?: any;
  fileDetails: IAttachment;
}

export default class DigitalValue extends React.Component<IDigitalValueProps, IDigitalValueState> {
  public commentAreaInput: HTMLTextAreaElement;
  public strategyCommentAreaInput: HTMLTextAreaElement;
  private addTeamMemberModalRef = React.createRef<AddTeamMemberModal>();
  private addOrEditFactorModalRef = React.createRef<AddOrEditFactorModal>();

  constructor(props: IDigitalValueProps) {
    super(props);
    this.state = {
      digitalValue: '',
      digitalValueError: '',
      effortValue: '',
      effortValueError: '',
      digitalValueComment: '',
      effortValueComment: '',
      currentFieldValue: null,
      showCommentArea: false,
      commentValueError: '',
      commentValue: '',
      showAddTeamMemberModal: false,
      isControllerMember: false,
      editTeamMember: false,
      editTeamMemberIndex: -1,
      contollerTeamMembers: [],
      sharingTeamMembers: [],
      teamMemberObj: {
        shortId: '',
        company: '',
        department: '',
        email: '',
        firstName: '',
        lastName: '',
        userType: '',
        teamMemberPosition: '',
      },
      addTeamMemberInController: true,
      attachments: [],
      showStrategyCommentArea: false,
      currentStrategyFieldValue: '',
      strategyCommentValueError: '',
      strategyCommentValue: '',
      strategicRelevance: 'Very Low',
      strategicRelevanceComment: '',
      benefitRealizationRisk: 'Very High',
      benefitRelevanceComment: '',
      showCostDriverInfoModal: false,
      showValueDriverInfoModal: false,
      showMaturityLevelInfoModal: false,
      showRiskAssesmentInfoModal: false,
      maturityLevel: '',
      costDrivers: [],
      valueDrivers: [],
      currentSelectedFactor: 'Cost',
      editFactor: false,
      showAddOrEditFactorModal: false,
      factorItem: {
        description: '',
        category: '',
        source: '',
        value: '',
        rampUp: [],
      },
      indexToEdit: null,
      showDeleteModal: false,
      indexToDelete: null,
      currentSelectedFactorDescription: '',
      assessment: {
        strategicRelevance: 'Very Low',
        commentOnStrategicRelevance: '',
        benefitRealizationRisk: 'Very High',
        commentOnBenefitRealizationRisk: '',
      },
    };
  }

  public componentDidMount() {
    if (this.props.digitalValue) {
      this.updateComponentValues(this.props.digitalValue);
    } else {
      SelectBox.defaultSetup();
      ExpansionPanel.defaultSetup();
    }
  }

  public render() {
    // const currentFieldValue = this.state.currentFieldValue;
    // const effortValueComment = this.state.effortValueComment;
    // const digitalValueComment = this.state.digitalValueComment;
    // const commentValueError = this.state.commentValueError || '';
    // const digitalValue = this.state.digitalValue;
    // const digitalValueError = this.state.digitalValueError || '';
    // const effortValue = this.state.effortValue;
    // const effortValueError = this.state.effortValueError || '';
    const { maturityLevel, costDrivers, valueDrivers, currentStrategyFieldValue, assessment } = this.state;
    const strategyCommentValueError = this.state.strategyCommentValueError || '';

    // const requiredError = '*Missing entry';

    const contollerTeamMembersList = this.state.contollerTeamMembers
      ? this.state.contollerTeamMembers.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.contollerTeamMembers.length}
              onMoveUp={this.onTeamMemberMoveUp}
              onMoveDown={this.onTeamMemberMoveDown}
              onEdit={this.onControllerTeamMemberEdit}
              onDelete={this.onControllerTeamMemberDelete}
            />
          );
        })
      : [];

    const sharingTeamMembersList = this.state.sharingTeamMembers
      ? this.state.sharingTeamMembers.map((member: ITeams, index: number) => {
          return (
            <TeamMemberListItem
              key={index}
              itemIndex={index}
              teamMember={member}
              showMoveUp={index !== 0}
              showMoveDown={index + 1 !== this.state.sharingTeamMembers.length}
              onMoveUp={this.onTeamMemberMoveUp}
              onMoveDown={this.onTeamMemberMoveDown}
              onEdit={this.onSharingTeamMemberEdit}
              onDelete={this.onSharingTeamMemberDelete}
            />
          );
        })
      : [];

    return (
      <React.Fragment>
        {/*         
        <div className={classNames(Styles.wrapper)}>          
          <div className={classNames(Styles.firstPanel)}>
            <h3>Digital Value</h3>
            <div className={Styles.digitalValueWrapper}>
              <div className={Styles.flexLayout}>
                <div>
                  <div
                    className={classNames(
                      'input-field-group',
                      Styles.digitalValueInputField,
                      digitalValueError.length ? 'error' : '',
                    )}
                  >
                    <label id="ValueLabel" htmlFor="valueInput" className="input-label">
                      Value (in m/€)
                    </label>
                    <input
                      type="text"
                      className="input-field numeric"
                      required={digitalValueError.length > 0}
                      required-error={requiredError}
                      id="valueInput"
                      maxLength={10}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={this.onDigitalValueOnChange}
                      value={digitalValue}
                    />
                    <span className={classNames('error-message-local', digitalValueError.length ? '' : 'hide')}>
                      {digitalValueError}
                    </span>
                  </div>
                  <div className={Styles.addButtonWrapper}>
                    <button id="addCommentBtn_Value" value="Value" onClick={this.onAddOrEditCommentClick}>
                      {this.getCommentButtonContent(digitalValueComment)}
                    </button>
                  </div>
                  <p className={digitalValueComment !== '' ? '' : 'hide'}>{digitalValueComment}</p>
                </div>
                <div>
                  <div
                    className={classNames(
                      'input-field-group',
                      Styles.digitalValueInputField,
                      effortValueError.length ? 'error' : '',
                    )}
                  >
                    <label id="effortLabel" htmlFor="effortInput" className="input-label">
                      Effort (in m/€)
                    </label>
                    <input
                      type="text"
                      className="input-field numeric"
                      required={effortValueError.length > 0}
                      required-error={requiredError}
                      id="effortInput"
                      maxLength={10}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={this.onEffortValueOnChange}
                      value={effortValue}
                    />
                    <span className={classNames('error-message-local', effortValueError.length ? '' : 'hide')}>
                      {effortValueError}
                    </span>
                  </div>
                  <div className={Styles.addButtonWrapper}>
                    <button id="addCommentBtn_Effort" value="Effort" onClick={this.onAddOrEditCommentClick}>
                      {this.getCommentButtonContent(effortValueComment)}
                    </button>
                  </div>
                  <p className={effortValueComment !== '' ? '' : 'hide'}>{effortValueComment}</p>
                </div>
              </div>
              
              <div className={classNames(Styles.commentWapper, this.state.showCommentArea ? '' : 'hide')}>
                <div
                  className={classNames(
                    'input-field-group include-error area',
                    commentValueError.length ? 'error-local' : '',
                  )}
                >
                  <label className="input-label" htmlFor="comment">
                    Comment for {currentFieldValue ? currentFieldValue : ''} (optional, max. 200 characters)
                  </label>
                  <textarea
                    value={this.state.commentValue}
                    className={classNames('input-field-area', Styles.commentTextarea)}
                    onChange={this.onCommentChange}
                    ref={commentAreaInput => {
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
                    onClick={this.addDigitalValueComment}
                    type="button"
                    disabled={commentValueError !== ''}
                  >
                    Save
                  </button>
                </div>
              </div>
            </div>
          </div>        
        </div>
         */}
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Maturity Level</h3>
            <div className={Styles.infoIcon}>
              <i className="icon mbc-icon info" onClick={this.showMaturityLevelInfoModal} />
            </div>
            <div className={Styles.digitalValueWrapper}>
              <div className={Styles.flexLayout}>
                <div>
                  <div className={Styles.setMaxWidth}>
                    <p className="hide">
                      Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt.
                    </p>
                    <div className={classNames('input-field-group')}>
                      <label id="maturityLevelSelectLabel" htmlFor="maturityLevelSelect" className="input-label">
                        Maturity Level
                      </label>
                      <div id="maturityLevel" className="custom-select">
                        <select id="maturityLevelSelect" onChange={this.onMaturityLevelChange} value={maturityLevel}>
                          {this.props.maturityLevelsList.map((obj) => (
                            <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                              {obj.name}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <p className="hide">
                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt.
                  </p>
                  <div className={Styles.addIconButtonWrapper}>
                    <IconAvatarNew className={Styles.buttonIcon} />
                    <button id="AddControllerBtn" onClick={() => { this.setState({ isControllerMember: true }); this.addControllerMember(); }}>
                      <i className="icon mbc-icon plus" />
                      <span>Add controller (optional)</span>
                    </button>
                  </div>
                </div>
              </div>
              {contollerTeamMembersList.length ? (
                <div className={Styles.memberListWrapper}>
                  <h4>Controllers</h4>
                  {contollerTeamMembersList}
                </div>
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Cost Driver</h3>
            <div className={Styles.infoIcon}>
              <i className="icon mbc-icon info" onClick={this.showCostDriverInfoModal} />
            </div>
            <div className={Styles.digitalValueWrapper}>
              <div id="costDriversWrapper" className={Styles.expansionListWrapper}>
                {costDrivers ? (
                  costDrivers.length ? (
                    <div className="expansion-panel-group">
                      {costDrivers.map((item: ICostFactor, index: number) => {
                        const expansionPanelId = 'constFactorExpPanel' + index;
                        return (
                          <div id={'costDriverPanel_' + index} key={index} className="expansion-panel">
                            <span className="animation-wrapper" />
                            <input type="checkbox" id={expansionPanelId} />
                            <label className="expansion-panel-label" htmlFor={expansionPanelId}>
                              {`Cost Factor ${index + 1} ${item.description}`}
                              <i className="icon down-up-flip" />
                            </label>
                            <div className="expansion-panel-content">
                              <div className={Styles.expansionnPanelContent}>
                                <div className={classNames(Styles.flexLayout, Styles.factorInfo)}>
                                  <div>
                                    <label>Description</label>
                                    <div>{item.description}</div>
                                  </div>
                                  <div>
                                    <label>Category</label>
                                    <div>{item.category}</div>
                                  </div>
                                  <div>
                                    <label>Value</label>
                                    <div>
                                      <IntlProvider locale={navigator.language} defaultLocale="en">
                                        {item.value ? <FormattedNumber value={Number(item.value)} /> : ''}
                                      </IntlProvider>
                                      &euro;
                                    </div>
                                  </div>
                                  <div>
                                    <label>Source</label>
                                    <div>{item.source}</div>
                                  </div>
                                </div>
                                <div>
                                  <label>Ramp-up</label>
                                  <div className={Styles.rampUpScrollableWrapper}>
                                    {/* <div className={Styles.scrollerLeft}>
                                    <span>
                                      <i className="icon mbc-icon arrow small left" />
                                    </span>
                                  </div> */}
                                    <div className={Styles.rampUpContainer}>
                                      {item.rampUp.map((cost: ICostRampUp, indexVal: number) => {
                                        return (
                                          <div className={Styles.rampUpItem} key={indexVal}>
                                            <strong>{cost.year}</strong>
                                            <div>
                                              <IntlProvider locale={navigator.language} defaultLocale="en">
                                                {cost.value ? <FormattedNumber value={Number(cost.value)} /> : ''}
                                              </IntlProvider>
                                              &euro;
                                            </div>
                                          </div>
                                        );
                                      })}
                                    </div>
                                    {/* <div className={Styles.scrollerRight}>
                                    <span>
                                      <i className="icon mbc-icon arrow small right" />
                                    </span>
                                  </div> */}
                                  </div>
                                  <div className={Styles.factorItemActionWrapper}>
                                    <div className={Styles.addButtonWrapper}>
                                      <button
                                        id={'editCostFactorBtn' + index}
                                        onClick={(e: any) => this.onEditCostFactorClick(e, index)}
                                      >
                                        <i className="icon edit medium-grey" />
                                        <span>Edit Cost Factor</span>
                                      </button>
                                    </div>
                                    <div className={Styles.addButtonWrapper}>
                                      <button
                                        id={'deleteCostFactorBtn' + index}
                                        onClick={this.showDeleteFactorModal(index, item.description)}
                                      >
                                        <i className="icon delete" />
                                        <span>Delete Cost Factor</span>
                                      </button>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    ''
                  )
                ) : (
                  ''
                )}
              </div>
              <div>
                <div
                  className={classNames(
                    Styles.addButtonWrapper,
                    costDrivers ? (costDrivers.length ? '' : Styles.setBorder) : Styles.setBorder,
                  )}
                >
                  <button id="addCostFactorBtn" onClick={this.onAddCostFactorClick}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Cost Factor</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Value Driver</h3>
            <div className={Styles.infoIcon}>
              <i className="icon mbc-icon info" onClick={this.showValueDriverInfoModal} />
            </div>
            <div className={Styles.digitalValueWrapper}>
              <div id="valueDriversWrapper" className={Styles.expansionListWrapper}>
                {valueDrivers ? (
                  valueDrivers.length ? (
                    <div className="expansion-panel-group">
                      {valueDrivers.map((item: IValueFactor, index: number) => {
                        const expansionPanelId = 'valueFactorExpPanel' + index;
                        return (
                          <div id={'valueDriverPanel_' + index} key={index} className="expansion-panel">
                            <span className="animation-wrapper" />
                            <input type="checkbox" id={expansionPanelId} />
                            <label className="expansion-panel-label" htmlFor={expansionPanelId}>
                              {`Value Factor ${index + 1} ${item.description}`}
                              <i className="icon down-up-flip" />
                            </label>
                            <div className="expansion-panel-content">
                              <div className={Styles.expansionnPanelContent}>
                                <div className={classNames(Styles.flexLayout, Styles.factorInfo)}>
                                  <div>
                                    <label>Description</label>
                                    <div>{item.description}</div>
                                  </div>
                                  <div>
                                    <label>Category</label>
                                    <div>{item.category}</div>
                                  </div>
                                  <div>
                                    <label>Value</label>
                                    <div>
                                      <IntlProvider locale={navigator.language} defaultLocale="en">
                                        {item.value ? <FormattedNumber value={Number(item.value)} /> : ''}
                                      </IntlProvider>
                                      &euro;
                                    </div>
                                  </div>
                                  <div>
                                    <label>Source</label>
                                    <div>{item.source}</div>
                                  </div>
                                </div>
                                <div>
                                  <label>Ramp-up</label>
                                  <div className={Styles.rampUpScrollableWrapper}>
                                    {/* <div className={Styles.scrollerLeft}>
                                <span>
                                    <i className="icon mbc-icon arrow small left" />
                                </span>
                                </div> */}
                                    <div className={Styles.rampUpContainer}>
                                      {item.rampUp.map((valueDriver: IValueRampUp, indexVal: number) => {
                                        return (
                                          <div className={Styles.rampUpItem} key={indexVal}>
                                            <strong>{valueDriver.year}</strong>
                                            <div>
                                              <IntlProvider locale={navigator.language} defaultLocale="en">
                                                {valueDriver.percent ? <FormattedNumber value={Number(valueDriver.percent)} /> : ''}
                                              </IntlProvider>
                                              %
                                            </div>
                                            <div>
                                              <IntlProvider locale={navigator.language} defaultLocale="en">
                                                {valueDriver.value ? <FormattedNumber value={Number(valueDriver.value)} /> : ''}
                                              </IntlProvider>
                                              &euro;
                                            </div>
                                          </div>
                                        );
                                      })}
                                    </div>
                                    {/* <div className={Styles.scrollerRight}>
                                <span>
                                    <i className="icon mbc-icon arrow small right" />
                                </span>
                                </div> */}
                                  </div>
                                  <div className={Styles.factorItemActionWrapper}>
                                    <div className={Styles.addButtonWrapper}>
                                      <button
                                        id={'editCostFactorBtn' + index}
                                        onClick={(e: any) => this.onEditValueFactorClick(e, index)}
                                      >
                                        <i className="icon edit medium-grey" />
                                        <span>Edit Value Factor</span>
                                      </button>
                                    </div>
                                    <div className={Styles.addButtonWrapper}>
                                      <button
                                        id={'deleteCostFactorBtn' + index}
                                        onClick={this.showDeleteValueFactorModal(index, item.description)}
                                      >
                                        <i className="icon delete" />
                                        <span>Delete Value Factor</span>
                                      </button>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    ''
                  )
                ) : (
                  ''
                )}
              </div>
              <div>
                <div
                  className={classNames(
                    Styles.addButtonWrapper,
                    valueDrivers ? (valueDrivers.length ? '' : Styles.setBorder) : Styles.setBorder,
                  )}
                >
                  <button id="addValueFactorBtn" onClick={this.onAddValueFactorClick}>
                    <i className="icon mbc-icon plus" />
                    <span>Add Value Factor</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <AttachmentUploader
          attachments={this.state.attachments}
          modifyAttachments={this.modifyAttachments}
          containerId="DigitalValue"
        />
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Strategy &amp; Risk Assessment</h3>
            <div className={Styles.infoIcon}>
              <i className="icon mbc-icon info" onClick={this.showRiskAssesmentInfoModal} />
            </div>
            <div className={Styles.digitalValueWrapper}>
              <div className={Styles.flexLayout}>
                <div>
                  <div className={classNames('input-field-group')}>
                    <label
                      id="strategicRelevanceSelectLabel"
                      htmlFor="strategicRelevanceSelect"
                      className="input-label"
                    >
                      Strategic Relevance<sup>*</sup>
                    </label>
                    <div id="strategicRelevance" className="custom-select">
                      <select
                        id="strategicRelevanceSelect"
                        onChange={this.onStrategicRelevanceChange}
                        value={this.state.assessment ? this.state.assessment.strategicRelevance : 'Very Low'}
                      >
                        {this.props.strategicRelevancesList.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className={Styles.addButtonWrapper}>
                    <button
                      id="addCommentBtn_StrategicRelevance"
                      value="StrategicRelevance"
                      onClick={this.onAddOrEditStrategyCommentClick}
                    >
                      {this.getCommentButtonContent(assessment ? assessment.commentOnStrategicRelevance : '')}
                    </button>
                  </div>
                  <p className={assessment ? (assessment.commentOnStrategicRelevance !== '' ? '' : 'hide') : 'hide'}>
                    {assessment ? assessment.commentOnStrategicRelevance : ''}
                  </p>
                </div>
                <div>
                  <div className={classNames('input-field-group')}>
                    <label id="benefitRelevanceSelectLabel" htmlFor="benefitRelevanceSelect" className="input-label">
                      Benefit Realization Risk<sup>*</sup>
                    </label>
                    <div id="benefitRelevance" className="custom-select">
                      <select
                        id="benefitRelevanceSelect"
                        onChange={this.onBenefitRelevanceChange}
                        value={this.state.assessment ? this.state.assessment.benefitRealizationRisk : 'Very High'}
                      >
                        {this.props.benefitRelevancesList.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className={Styles.addButtonWrapper}>
                    <button
                      id="addCommentBtn_BenefitRelevance"
                      value="enefitRelevance"
                      onClick={this.onAddOrEditStrategyCommentClick}
                    >
                      {this.getCommentButtonContent(assessment ? assessment.commentOnBenefitRealizationRisk : '')}
                    </button>
                  </div>
                  <p
                    className={assessment ? (assessment.commentOnBenefitRealizationRisk !== '' ? '' : 'hide') : 'hide'}
                  >
                    {assessment ? assessment.commentOnBenefitRealizationRisk : ''}
                  </p>
                </div>
              </div>
              <div className={classNames(Styles.commentWapper, this.state.showStrategyCommentArea ? '' : 'hide')}>
                <div
                  className={classNames(
                    'input-field-group include-error area',
                    strategyCommentValueError.length ? 'error-local' : '',
                  )}
                >
                  <label className="input-label" htmlFor="comment">
                    Comment for{' '}
                    {currentStrategyFieldValue === 'addCommentBtn_StrategicRelevance'
                      ? 'Strategic Relevance'
                      : 'Benefit Relevance'}{' '}
                    (optional, max. 200 characters)
                  </label>
                  <textarea
                    value={this.state.strategyCommentValue}
                    className={classNames('input-field-area', Styles.commentTextarea)}
                    onChange={this.onStrategyCommentChange}
                    ref={(strategyCommentAreaInput) => {
                      this.strategyCommentAreaInput = strategyCommentAreaInput;
                    }}
                    id="strategyComment"
                  />
                  <span className={classNames('error-message-local', strategyCommentValueError.length ? '' : 'hide')}>
                    {strategyCommentValueError}
                  </span>
                </div>
                <div className={Styles.actionWrapper}>
                  <button
                    id="saveCommentbtn"
                    className="btn btn-primary"
                    onClick={this.addDigitalValueStrategyComment}
                    type="button"
                    disabled={strategyCommentValueError !== ''}
                  >
                    Save
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Share / Permission</h3>
            <div className={Styles.digitalValueWrapper}>
              {/* <div className={Styles.flexLayout}> */}
              {/* <div>
                  <p>
                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt.
                  </p>
                </div> */}
              <div>
                <div className={Styles.addIconButtonWrapper}>
                  <IconAvatarNew className={Styles.buttonIcon} />
                  <button onClick={() => { this.setState({ isControllerMember: false }); this.addTeamMember(); }}>
                    <i className="icon mbc-icon plus" />
                    <span>Add share / permission to team member</span>
                  </button>
                </div>
              </div>
              {/* </div> */}
              {sharingTeamMembersList.length ? (
                <div className={Styles.memberListWrapper}>
                  <h4>Authorised viewers</h4>
                  {sharingTeamMembersList}
                </div>
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <div className="btn-set">
            <button className="btn btn-primary" type="button" onClick={this.onDigitalValueSubmit}>
              Save
            </button>
            <button
              className={'btn btn-tertiary ' + classNames(Styles.publishBtn)}
              type="button"
              onClick={this.onSolutionPublish}
            >
              Publish
            </button>
          </div>
        </div>
        {this.state.showAddTeamMemberModal && (
          <AddTeamMemberModal
            ref={this.addTeamMemberModalRef}
            modalTitleText={this.state.addTeamMemberInController ? 'controller' : 'team member'}
            showOnlyInteral={true}
            editMode={this.state.editTeamMember}
            showAddTeamMemberModal={this.state.showAddTeamMemberModal}
            teamMember={this.state.teamMemberObj}
            onUpdateTeamMemberList={this.updateTeamMemberList}
            onAddTeamMemberModalCancel={this.onAddTeamMemberModalCancel}
            validateMemebersList={this.validateMembersList}
          />
        )}
        {this.state.showAddOrEditFactorModal && (
          <AddOrEditFactorModal
            ref={this.addOrEditFactorModalRef}
            factorId={this.state.currentSelectedFactor}
            editMode={this.state.editFactor}
            showAddOrEditFactorModal={this.state.showAddOrEditFactorModal}
            factorItem={this.state.factorItem}
            onAddOrEditFactorItem={this.onAddOrEditFactorItem}
            onAddOrEditFactorModalCancel={this.onAddOrEditFactorModalCancel}
          />
        )}
        {this.state.showMaturityLevelInfoModal && (
          <InfoModal
            title={'Maturity Level'}
            show={this.state.showMaturityLevelInfoModal}
            content={<img width="562" src={ImgMaturityLevel} />}
            onCancel={this.onMaturityLevelInfoModalCancel}
          />
        )}
        {this.state.showCostDriverInfoModal && (
          <InfoModal
            title={'Cost Driver'}
            show={this.state.showCostDriverInfoModal}
            content={<img width="633" src={ImgCostDriver} />}
            onCancel={this.onCostDriverInfoModalCancel}
          />
        )}
        {this.state.showValueDriverInfoModal && (
          <InfoModal
            title={'Value Driver'}
            show={this.state.showValueDriverInfoModal}
            content={<img width="633" src={ImgValueDriver} />}
            onCancel={this.onValueDriverInfoModalCancel}
          />
        )}
        {this.state.showRiskAssesmentInfoModal && (
          <InfoModal
            title={'Rating Logic'}
            show={this.state.showRiskAssesmentInfoModal}
            content={<img width="651" src={ImgRiskAssesment} />}
            onCancel={this.onRiskAssesmentInfoModalCancel}
          />
        )}
        {this.state.showDeleteModal && (
          <ConfirmModal
            title={''}
            showAcceptButton={true}
            showCancelButton={false}
            acceptButtonTitle={'Confirm'}
            show={this.state.showDeleteModal}
            removalConfirmation={true}
            content={
              <div className={Styles.removeFactorPopupContent}>
                <div>Remove {this.state.currentSelectedFactor} Driver</div>
                <div>{this.state.currentSelectedFactorDescription}</div>
              </div>
            }
            onCancel={this.onRemoveFactorModalCancel}
            onAccept={this.onAccept}
          />
        )}
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.digitalValue) {
      this.updateComponentValues(this.props.digitalValue, true);
    }
  };

  public updateComponentValues = (digitalValueObj: IDigitalValue, fromReset?: boolean) => {
    const tempAssessment = fromReset
      ? null
      : {
          strategicRelevance: 'Very Low',
          commentOnStrategicRelevance: '',
          benefitRealizationRisk: 'Very High',
          commentOnBenefitRealizationRisk: '',
        };
    digitalValueObj.assessment = digitalValueObj.assessment ? digitalValueObj.assessment : tempAssessment;
    const digitalValue = digitalValueObj.digitalValue ? digitalValueObj.digitalValue.toString() : '';
    const digitalValueComment = digitalValueObj.digitalValueComment;
    const effortValue = digitalValueObj.digitalEffort ? digitalValueObj.digitalEffort.toString() : '';
    const effortValueComment = digitalValueObj.digitalEffortComment;
    const costDrivers = digitalValueObj.costDrivers;
    const valueDrivers = digitalValueObj.valueDrivers;
    const maturityLevel = digitalValueObj.maturityLevel;
    const assessment = digitalValueObj.assessment;
    const projectControllers = digitalValueObj.projectControllers;
    const attachments = digitalValueObj.attachments;
    const permissions = digitalValueObj.permissions;
    this.setState(
      {
        digitalValue,
        digitalValueComment,
        effortValue,
        effortValueComment,
        contollerTeamMembers: projectControllers ? projectControllers : [],
        costDrivers,
        valueDrivers,
        maturityLevel,
        assessment,
        attachments,
        sharingTeamMembers: permissions ? permissions : [],
      },
      () => {
        SelectBox.defaultSetup();
      },
    );
  };

  protected modifyAttachments = (attachments: IAttachment[]) => {
    // console.log(attachments);
    this.setState({ attachments });
  };

  protected addControllerMember = () => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.showAddTeamMemberModalView();
    });
  };

  protected addTeamMember = () => {
    this.setState({ addTeamMemberInController: false }, () => {
      this.showAddTeamMemberModalView();
    });
  };

  protected validateMembersList = (teamMemberObj: ITeams) => {
    if(this.state.isControllerMember) {
      let duplicateMember = false;
      duplicateMember = this.state.contollerTeamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
      return duplicateMember;
    } else {
      let duplicateMember = false;
      duplicateMember = this.state.sharingTeamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
      return duplicateMember;
    }
  };

  protected getCommentButtonContent(commentValue: string) {
    return !commentValue || commentValue === '' ? (
      <React.Fragment>
        <i className="icon mbc-icon plus" />
        <span>Add comment</span>
      </React.Fragment>
    ) : (
      <React.Fragment>
        <i className="icon mbc-icon edit" />
        <span>Edit comment</span>
      </React.Fragment>
    );
  }

  protected onDigitalValueOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const digitalValue = e.currentTarget.value;
    this.setState(
      {
        digitalValue,
      },
      () => {
        this.props.modifyDigitalValue(this.getDigitalValueObj());
      },
    );

    if (digitalValue === '') {
      this.setState({
        effortValueError: '',
      });
    }
  };

  protected onEffortValueOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const effortValue = e.currentTarget.value;

    this.setState(
      {
        effortValue,
      },
      () => {
        this.props.modifyDigitalValue(this.getDigitalValueObj());
      },
    );

    if (effortValue === '') {
      this.setState({
        digitalValueError: '',
      });
    }
  };

  protected onAddOrEditCommentClick = (e: React.FormEvent<HTMLButtonElement>) => {
    const currentFieldValue = e.currentTarget.value;
    this.setState(
      {
        currentFieldValue,
        showCommentArea: true,
        commentValue: currentFieldValue === 'Value' ? this.state.digitalValueComment : this.state.effortValueComment,
      },
      () => {
        this.commentAreaInput.focus();
      },
    );
  };

  protected onMaturityLevelChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const maturityLevel = e.currentTarget.selectedOptions[0].value;
    this.setState({ maturityLevel }, () => {
      // this.props.modifyDigitalValue(this.getDigitalMaturityLevelObj());
    });
  };

  protected onStrategicRelevanceChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const strategicRelevanceValue = e.currentTarget.selectedOptions[0].value;
    const { benefitRealizationRisk, commentOnStrategicRelevance, commentOnBenefitRealizationRisk } =
      this.state.assessment;
    this.setState(
      {
        assessment: {
          strategicRelevance: strategicRelevanceValue,
          benefitRealizationRisk,
          commentOnStrategicRelevance,
          commentOnBenefitRealizationRisk,
        },
      },
      () => {
        // this.props.modifyDigitalValue(this.getDigitalValueAssesmentObj());
      },
    );
  };

  protected onBenefitRelevanceChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const benefitRealizationRiskValue = e.currentTarget.selectedOptions[0].value;
    const { strategicRelevance, commentOnStrategicRelevance, commentOnBenefitRealizationRisk } = this.state.assessment;
    this.setState(
      {
        assessment: {
          benefitRealizationRisk: benefitRealizationRiskValue,
          strategicRelevance,
          commentOnStrategicRelevance,
          commentOnBenefitRealizationRisk,
        },
      },
      () => {
        // this.props.modifyDigitalValue(this.getDigitalValueAssesmentObj());
      },
    );
  };

  protected onAddOrEditStrategyCommentClick = (e: React.FormEvent<HTMLButtonElement>) => {
    const currentStrategyFieldValue = e.currentTarget.id;
    const currentRelevanceType = e.currentTarget.id;
    this.setState(
      {
        currentStrategyFieldValue,
        showStrategyCommentArea: true,
        strategyCommentValue:
          currentRelevanceType === 'addCommentBtn_StrategicRelevance'
            ? this.state.assessment.commentOnStrategicRelevance
            : this.state.assessment.commentOnBenefitRealizationRisk,
      },
      () => {
        this.strategyCommentAreaInput.focus();
      },
    );
  };

  protected onCommentChange = (e: React.FormEvent<HTMLTextAreaElement>) => {
    const maxCharReachedError = '*Reached maximum character limit';
    const commentValue = e.currentTarget.value;
    if (commentValue.length <= 200) {
      this.setState({ commentValue, commentValueError: null });
    } else {
      this.setState({ commentValueError: maxCharReachedError });
    }
  };

  protected onStrategyCommentChange = (e: React.FormEvent<HTMLTextAreaElement>) => {
    const maxCharReachedError = '*Reached maximum character limit';
    const strategyCommentValue = e.currentTarget.value;
    if (strategyCommentValue.length <= 200) {
      this.setState({ strategyCommentValue, strategyCommentValueError: null });
    } else {
      this.setState({ strategyCommentValueError: maxCharReachedError });
    }
  };

  protected addDigitalValueComment = () => {
    const currentFieldValue = this.state.currentFieldValue;
    if (currentFieldValue === 'Effort') {
      this.setState({
        effortValueComment: this.state.commentValue,
      });
    } else {
      this.setState({
        digitalValueComment: this.state.commentValue,
      });
    }
    this.setState(
      {
        showCommentArea: false,
        commentValue: '',
      },
      () => {
        this.props.modifyDigitalValue(this.getDigitalValueObj());
      },
    );
  };

  protected addDigitalValueStrategyComment = () => {
    const currentStrategyFieldValue = this.state.currentStrategyFieldValue;
    if (currentStrategyFieldValue === 'addCommentBtn_StrategicRelevance') {
      this.setState({
        assessment: {
          commentOnStrategicRelevance: this.state.strategyCommentValue,
          benefitRealizationRisk: this.state.assessment.benefitRealizationRisk,
          strategicRelevance: this.state.assessment.strategicRelevance,
          commentOnBenefitRealizationRisk: this.state.assessment.commentOnBenefitRealizationRisk,
        },
      });
    } else {
      this.setState({
        assessment: {
          commentOnBenefitRealizationRisk: this.state.strategyCommentValue,
          benefitRealizationRisk: this.state.assessment.benefitRealizationRisk,
          strategicRelevance: this.state.assessment.strategicRelevance,
          commentOnStrategicRelevance: this.state.assessment.commentOnStrategicRelevance,
        },
      });
    }
    this.setState(
      {
        showStrategyCommentArea: false,
        strategyCommentValue: '',
      },
      () => {
        // this.props.modifyDigitalValue(this.getDigitalValueAssesmentObj());
      },
    );
  };

  protected getDigitalValueObj() {
    return {
      digitalValue: this.state.digitalValue ? parseFloat(this.state.digitalValue) : null,
      digitalValueComment: this.state.digitalValueComment,
      digitalEffort: this.state.effortValue ? parseFloat(this.state.effortValue) : null,
      digitalEffortComment: this.state.effortValueComment,
      costDrivers: this.state.costDrivers,
      valueDrivers: this.state.valueDrivers,
      maturityLevel: this.state.maturityLevel,
      projectControllers: this.state.contollerTeamMembers,
      attachments: this.state.attachments,
      permissions: this.state.sharingTeamMembers,
      assessment: {
        strategicRelevance: this.state.assessment
          ? this.state.assessment.strategicRelevance
            ? this.state.assessment.strategicRelevance
            : 'Very Low'
          : 'Very Low',
        commentOnStrategicRelevance: this.state.assessment ? this.state.assessment.commentOnStrategicRelevance : '',
        benefitRealizationRisk: this.state.assessment
          ? this.state.assessment.benefitRealizationRisk
            ? this.state.assessment.benefitRealizationRisk
            : 'Very High'
          : 'Very High',
        commentOnBenefitRealizationRisk: this.state.assessment
          ? this.state.assessment.commentOnBenefitRealizationRisk
          : '',
      },
    };
  }

  protected getDigitalValueAssesmentObj() {
    return {
      strategicRelevance: this.state.strategicRelevance,
      commentOnStrategicRelevance: this.state.strategicRelevanceComment,
      benefitRealizationRisk: this.state.benefitRealizationRisk,
      commentOnBenefitRealizationRisk: this.state.benefitRelevanceComment,
    };
  }

  protected validateDigitalValueInputs() {
    let isValid = true;
    const requiredError = '*Missing entry';
    if (this.state.digitalValue === '' && this.state.effortValue !== '') {
      this.setState({
        digitalValueError: requiredError,
      });
      isValid = false;
    } else if (this.state.digitalValue !== '' && this.state.effortValue === '') {
      this.setState({
        effortValueError: requiredError,
      });
      isValid = false;
    } else {
      this.setState({
        digitalValueError: '',
        effortValueError: '',
      });
    }
    return isValid;
  }

  protected showAddTeamMemberModalView = () => {
    this.resetAddTeamMemberState();
    this.setState({ showAddTeamMemberModal: true }, () => {
      this.addTeamMemberModalRef.current.setTeamMemberData(this.state.teamMemberObj, false);
    });
  };

  protected resetAddTeamMemberState() {
    this.setState({
      editTeamMemberIndex: -1,
      teamMemberObj: {
        shortId: '',
        department: '',
        email: '',
        firstName: '',
        lastName: '',
        userType: '',
        mobileNumber: '',
        teamMemberPosition: '',
      },
      editTeamMember: false,
    });
  }

  protected onControllerTeamMemberEdit = (index: number) => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onSharingTeamMemberEdit = (index: number) => {
    this.setState({ addTeamMemberInController: false }, () => {
      this.onTeamMemberEdit(index);
    });
  };

  protected onControllerTeamMemberDelete = (index: number) => {
    this.setState({ addTeamMemberInController: true }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onSharingTeamMemberDelete = (index: number) => {
    this.setState({ addTeamMemberInController: false }, () => {
      this.onTeamMemberDelete(index);
    });
  };

  protected onTeamMemberEdit = (index: number) => {
    const { contollerTeamMembers, sharingTeamMembers, addTeamMemberInController } = this.state;
    const teamMemberObj = addTeamMemberInController ? contollerTeamMembers[index] : sharingTeamMembers[index];
    this.setState(
      {
        teamMemberObj,
        showAddTeamMemberModal: true,
        editTeamMember: true,
        editTeamMemberIndex: index,
      },
      () => {
        this.addTeamMemberModalRef.current.setTeamMemberData(teamMemberObj, true);
      },
    );
  };

  protected updateTeamMemberList = (teamMemberObj: ITeams) => {
    const { editTeamMember, editTeamMemberIndex, contollerTeamMembers, sharingTeamMembers, addTeamMemberInController } =
      this.state;
    let teamMembers = sharingTeamMembers;
    if (addTeamMemberInController) {
      teamMembers = contollerTeamMembers;
    }
    if (editTeamMember) {
      teamMembers.splice(editTeamMemberIndex, 1);
      teamMembers.splice(editTeamMemberIndex, 0, teamMemberObj);
    } else {
      teamMembers.push(teamMemberObj);
    }

    const stateUpdateObj = {
      showAddTeamMemberModal: false,
      contollerTeamMembers: this.state.contollerTeamMembers,
      sharingTeamMembers: this.state.sharingTeamMembers,
    };

    if (addTeamMemberInController) {
      stateUpdateObj.contollerTeamMembers = teamMembers;
    } else {
      stateUpdateObj.sharingTeamMembers = teamMembers;
    }

    this.setState(stateUpdateObj, () => {
      this.resetAddTeamMemberState();
      // this.props.modifyTeam(this.state.teamMembers);
    });
  };

  protected onAddTeamMemberModalCancel = () => {
    this.setState({ showAddTeamMemberModal: false }, () => {
      this.resetAddTeamMemberState();
    });
  };

  protected onTeamMemberDelete = (index: number) => {
    const { contollerTeamMembers, sharingTeamMembers, addTeamMemberInController } = this.state;
    const teamMembers = addTeamMemberInController ? contollerTeamMembers : sharingTeamMembers;
    teamMembers.splice(index, 1);
    if (addTeamMemberInController) {
      this.setState({ contollerTeamMembers: teamMembers });
    } else {
      this.setState({ sharingTeamMembers: teamMembers });
    }
    // this.props.modifyTeam(this.state.contollerTeamMembers);
  };

  protected onTeamMemberMoveUp = (index: number) => {
    const contollerTeamMembers = this.state.contollerTeamMembers;
    const teamMember = contollerTeamMembers.splice(index, 1)[0];
    contollerTeamMembers.splice(index - 1, 0, teamMember);
    this.setState({ contollerTeamMembers });
    // this.props.modifyTeam(this.state.teamMembers);
  };

  protected onTeamMemberMoveDown = (index: number) => {
    const contollerTeamMembers = this.state.contollerTeamMembers;
    const teamMember = contollerTeamMembers.splice(index, 1)[0];
    contollerTeamMembers.splice(index + 1, 0, teamMember);
    this.setState({ contollerTeamMembers });
    // this.props.modifyTeam(this.state.teamMembers);
  };

  protected onDigitalValueSubmit = () => {
    // if (this.validateDigitalValueInputs()) {
    this.props.modifyDigitalValue(this.getDigitalValueObj());
    this.props.onSaveDraft('digitalvalue');
    // }
  };

  protected onSolutionPublish = () => {
    // if (this.validateDigitalValueInputs()) {
    this.props.modifyDigitalValue(this.getDigitalValueObj());
    this.props.onPublish();
    // }
  };

  protected onEditCostFactorClick = (e: React.FormEvent<HTMLButtonElement>, index: number) => {
    this.setState(
      {
        currentSelectedFactor: 'Cost',
        factorItem: this.state.costDrivers[index],
      },
      () => {
        this.showddOrEditFactorModalView('EDIT', index);
      },
    );
  };

  protected onEditValueFactorClick = (e: React.FormEvent<HTMLButtonElement>, index: number) => {
    this.setState(
      {
        currentSelectedFactor: 'Value',
        factorItem: this.state.valueDrivers[index],
      },
      () => {
        this.showddOrEditFactorModalView('EDIT', index);
      },
    );
  };

  protected onAddCostFactorClick = (e: React.FormEvent<HTMLButtonElement>) => {
    this.setState(
      {
        currentSelectedFactor: 'Cost',
      },
      () => {
        this.showddOrEditFactorModalView('ADD');
      },
    );
  };

  protected onAddValueFactorClick = (e: React.FormEvent<HTMLButtonElement>) => {
    this.setState(
      {
        currentSelectedFactor: 'Value',
      },
      () => {
        this.showddOrEditFactorModalView('ADD');
      },
    );
  };

  protected deleteCostFactor = (index: number) => {
    const { costDrivers, valueDrivers } = this.state;
    if (this.state.currentSelectedFactor === 'Cost') {
      costDrivers.splice(index, 1);
      this.setState({ costDrivers, showDeleteModal: false, indexToDelete: null });
    }

    if (this.state.currentSelectedFactor === 'Value') {
      valueDrivers.splice(index, 1);
      this.setState({ valueDrivers, showDeleteModal: false, indexToDelete: null });
    }
  };

  protected showddOrEditFactorModalView = (mode: string, indexToEdit?: number) => {
    // this.resetAddOrEditFactorState();
    const isEditMode = mode === 'ADD' ? false : true;
    if (mode !== 'ADD') {
      this.setState({ indexToEdit });
    }
    this.setState({ showAddOrEditFactorModal: true }, () => {
      this.addOrEditFactorModalRef.current.setFactorData(this.state.factorItem, isEditMode);
      InputFields.defaultSetup();
    });
  };

  protected onAddOrEditFactorItem = (factorItem: any, isEdit?: boolean) => {
    this.setState({ showAddOrEditFactorModal: false });
    let { costDrivers, valueDrivers } = this.state;

    if (!costDrivers) {
      costDrivers = [];
    }
    if (!valueDrivers) {
      valueDrivers = [];
    }
    if (this.state.currentSelectedFactor === 'Cost') {
      if (!isEdit) {
        costDrivers.push(factorItem as ICostFactor);
      } else {
        costDrivers[this.state.indexToEdit] = factorItem;
      }
      this.setState({ costDrivers });
    } else {
      if (!isEdit) {
        valueDrivers.push(factorItem as IValueFactor);
      } else {
        valueDrivers[this.state.indexToEdit] = factorItem;
      }
      this.setState({ valueDrivers });
    }
  };

  protected showDeleteFactorModal = (indexToDelete: number, factorDescription: string) => {
    return () => {
      this.setState({
        showDeleteModal: true,
        indexToDelete,
        currentSelectedFactor: 'Cost',
        currentSelectedFactorDescription: factorDescription,
      });
    };
  };

  protected showDeleteValueFactorModal = (indexToDelete: number, factorDescription: string) => {
    return () => {
      this.setState({
        showDeleteModal: true,
        indexToDelete,
        currentSelectedFactor: 'Value',
        currentSelectedFactorDescription: factorDescription,
      });
    };
  };

  protected onAccept = () => {
    this.deleteCostFactor(this.state.indexToDelete);
  };

  protected onRemoveFactorModalCancel = () => {
    this.setState({ showDeleteModal: false, indexToDelete: null });
  };

  protected onAddOrEditFactorModalCancel = (factorItem?: any) => {
    this.setState({ showAddOrEditFactorModal: false }, () => {
      // this.resetAddOrEditFactorState();
      this.setState({ showAddOrEditFactorModal: false });
      if (factorItem.length > 0) {
        let { costDrivers, valueDrivers } = this.state;

        if (!costDrivers) {
          costDrivers = [];
        }
        if (!valueDrivers) {
          valueDrivers = [];
        }
        if (this.state.currentSelectedFactor === 'Cost') {
          costDrivers[this.state.indexToEdit].rampUp = factorItem;
          this.setState({ costDrivers });
        } else {
          valueDrivers[this.state.indexToEdit].rampUp = factorItem;
          this.setState({ valueDrivers });
        }
      }
    });
  };

  protected showMaturityLevelInfoModal = () => {
    this.setState({ showMaturityLevelInfoModal: true });
  };

  protected onMaturityLevelInfoModalCancel = () => {
    this.setState({ showMaturityLevelInfoModal: false });
  };

  protected showCostDriverInfoModal = () => {
    this.setState({ showCostDriverInfoModal: true });
  };

  protected onCostDriverInfoModalCancel = () => {
    this.setState({ showCostDriverInfoModal: false });
  };

  protected showValueDriverInfoModal = () => {
    this.setState({ showValueDriverInfoModal: true });
  };

  protected onValueDriverInfoModalCancel = () => {
    this.setState({ showValueDriverInfoModal: false });
  };

  protected showRiskAssesmentInfoModal = () => {
    this.setState({ showRiskAssesmentInfoModal: true });
  };

  protected onRiskAssesmentInfoModalCancel = () => {
    this.setState({ showRiskAssesmentInfoModal: false });
  };
}
