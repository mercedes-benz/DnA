// @ts-nocheck
import { Document, Font, Image, Link, Page, StyleSheet, Text, View } from '@react-pdf/renderer';
import * as React from 'react';
// @ts-ignore
import ImgAttachment from '../../../../assets/images/attachment.jpg';
// @ts-ignore
import ImgLink from '../../../../assets/images/link.jpg';
// @ts-ignore
import ImgNotCompletedGuideline from '../../../../assets/images/Not-Completed-Guidelines.png';
// @ts-ignore
import ImgQuickCheckReady from '../../../../assets/images/Quick-Check-Ready.png';
// @ts-ignore
import ImgQuickCheck from '../../../../assets/images/Quick-Check.png';
// @ts-ignore
import ImgTeamExternalAvatar from '../../../../assets/images/team-external-avatar.jpg';
// @ts-ignore
import ImgTeamInternalAvatar from '../../../../assets/images/team-internal-avatar.jpg';
// @ts-ignore
import ImgTick from '../../../../assets/images/tick.jpg';
// @ts-ignore
import ImgUseCaseCheckReady from '../../../../assets/images/UseCsae-Check-Ready.png';
// @ts-ignore
import ImgUseCaseCheck from '../../../../assets/images/UseCsae-Check.png';
import { TeamMemberType } from 'globals/Enums';
import { getDataikuInstanceTag, getDateFromTimestamp } from '../../../../services/utils';

// @ts-ignore
import ImgIdeation from '../../../../assets/images/ideation.jpg';
// @ts-ignore
import ImgKickOff from '../../../../assets/images/kick_off.jpg';
// @ts-ignore
import ImgPilot from '../../../../assets/images/pilot.jpg';
// @ts-ignore
import ImgPOC from '../../../../assets/images/POC.jpg';
// @ts-ignore
import ImgProfessionalization from '../../../../assets/images/professionalization.jpg';
// @ts-ignore
import ImgRollout from '../../../../assets/images/rollout.jpg';
import jupeterImg from '../../../../assets/images/jupyter-icon.jpg';
import dataIkuimg from '../../../../assets/images/dataiku-icon.jpg';

// import PersonaAvatar from '../../../../assets/images/team-internal-avatar.jpg';
import LiAvatar from '../../../../assets/images/li.png';
import HenryAvatar from '../../../../assets/images/henry.png';
import VictoriaAvatar from '../../../../assets/images/victoria.png';
import TomAvatar from '../../../../assets/images/tom.png';
import { AI_RISK_ASSESSMENT_TYPES } from 'globals/constants';

import {
  IAttachment,
  ICostFactor,
  IDataCompliance,
  ILink,
  IMilestonesList,
  IPhase,
  IPhasesItem,
  ITeams,
  IValueFactor,
  IValueRampUp,
  INeededRoleObject,
  IUserInfo,
  INotebookInfo,
  IDataiku,
  IDataValueRampUp,
} from 'globals/types';
import { SOLUTION_DATA_VALUE_CATEGORY_TYPES, SOLUTION_VALUE_CALCULATION_TYPES, TEAMS_PROFILE_LINK_URL_PREFIX, TOTAL_LOCATIONS_COUNT } from 'globals/constants';
import { Envs } from 'globals/Envs';
import { getDateTimeFromTimestamp, regionalForMonthAndYear } from '../../../../services/utils';
import { ICreateNewSolutionData } from '../../createNewSolution/CreateNewSolution';
import { IntlProvider, FormattedNumber } from 'react-intl';

Font.register({
  family: 'Roboto-Regular',
  format: 'truetype',
  fontStyle: 'normal',
  fontWeight: 'normal',
  fonts: [{ src: 'fonts/Roboto-Regular.ttf' }],
});
Font.register({
  family: 'Roboto-Medium',
  format: 'truetype',
  fontStyle: 'normal',
  fontWeight: 'normal',
  fonts: [{ src: 'fonts/Roboto-Medium.ttf' }],
});

const styles = StyleSheet.create({
  page: {
    paddingHorizontal: 50,
    paddingBottom: 20,
    paddingTop: 40,
  },
  view: {
    fontFamily: 'Roboto-Regular',
    fontSize: 10,
    color: '#1C2026',
    paddingBottom: 20,
  },
  title: {
    fontSize: 16,
    marginBottom: 10,
  },
  subTitle: {
    fontSize: 12,
  },
  sectionTitle: {
    fontSize: 9,
    fontWeight: 'bold',
    fontFamily: 'Roboto-Medium',
    marginBottom: 12,
  },
  imageSection: {
    textAlign: 'center',
    marginBottom: 100,
    marginTop: 80,
  },
  attachmentIcon: {
    position: 'absolute',
    left: 0,
    fontSize: 24,
    marginTop: 4,
    color: '#697582',
  },
  linkIcon: {
    position: 'absolute',
    left: 0,
    fontSize: 24,
    top: 4,
    color: '#697582',
  },
  flexLayout: {
    display: 'flex',
    flexWrap: 'wrap',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 25,
  },
  rampUpContainer: {
    border: '1 solid #697582',
    marginTop: -20,
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    // flexGrow: 1,
    // flexBasis: 120,
    // flexShrink: 0,
  },
  flexCol1: {
    flex: 1,
  },
  flexCol2: {
    flex: 1,
    marginLeft: 10,
  },
  flexCol4: {
    flex: 2,
    marginLeft: 10,
  },
  firstCol: {
    flex: 1,
    marginLeft: 0,
  },
  wideCol: {
    flex: 3,
  },
  spaceCol: {
    flex: 1,
  },
  chip: {
    paddingVertical: 5,
    paddingHorizontal: 10,
    backgroundColor: '#bbb',
    marginRight: 5,
    marginTop: 5,
    flexGrow: 1,
    flexShrink: 1,
  },
  milestoneCol: {
    alignItems: 'center',
    width: '16.66%',
    display: 'flex',
    flexDirection: 'column',
  },
  milestoneValueView: {
    width: '100%',
    textAlign: 'center',
    height: 75,
  },
  milestoneTimelineView: {
    width: '100%',
    height: 5,
    position: 'relative',
  },
  milestoneTimelineDot: {
    height: 5,
    width: 5,
    borderRadius: 2.5,
    backgroundColor: '#697582',
    position: 'absolute',
  },
  milestoneTimelineLine: {
    height: 2.5,
    width: 67.5,
    backgroundColor: '#697582',
    position: 'absolute',
    left: 7.5,
    top: 1.25,
  },
  milestoneCommentView: {
    marginTop: 20,
    paddingHorizontal: 6,
  },
  seperatorLine: {
    borderBottomColor: '#697582',
    borderBottomWidth: 0.5,
  },
  seperatorLineLight: {
    borderBottomColor: '#C0C8D0',
    borderBottomWidth: 0.5,
  },
  setMarginTop: {
    marginTop: 25,
  },
  setMarginBottom: {
    marginTop: 25,
  },
  setNegativeMarginBottom: {
    marginBottom: -80,
  },
  setNegativeMarginTop: {
    marginTop: -100,
  },
  setMarginTop15: {
    marginTop: 15,
  },
  setMarginTop3: {
    marginTop: 3,
  },
  noMarginTop: {
    marginTop: 0,
  },
  noMarginBottom: {
    marginBottom: 0,
  },
  bigFont: {
    fontSize: 25,
  },
  alignCenter: {
    textAlign: 'justify',
  },
  footer: {
    position: 'absolute',
    left: 0,
    bottom: 0,
    fontSize: 8,
    borderTopColor: '#697582',
    borderTopWidth: 0.5,
    width: '100%',
    paddingTop: 5,
  },
  costFactorSeperator: {
    borderTopColor: '#C0C8D0',
    borderTopWidth: 0.5,
    width: '100%',
    marginBottom: 20,
  },
  pageNumber: {
    position: 'absolute',
    right: 0,
    bottom: 0,
  },
  jupeterCard: {
    display: 'flex',
    flexWrap: 'wrap',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 25,
  },
  jupeterIcon: {
    width: '80px',
  },
  jupeterCardContent: {
    width: '280px',
  },
  JuperterCardDesc: {
    width: '100%',
  },
  smallText: {
    fontSize: 7,
  },
});

const digitalValueTypeKeyValue  = Object.keys(SOLUTION_VALUE_CALCULATION_TYPES)[0];
const dataValueTypeKeyValue  = Object.keys(SOLUTION_VALUE_CALCULATION_TYPES)[1];
const dataValueSavingsKeyValue = Object.keys(SOLUTION_DATA_VALUE_CATEGORY_TYPES)[0];
const dataValueRevenueKeyValue = Object.keys(SOLUTION_DATA_VALUE_CATEGORY_TYPES)[1];

const processDataValues = (values: any[]) => {
  const dataValues = values.join(' / ');
  return <Text>{dataValues}</Text>;
};


const processDataSourceValues = (values: any[], dsList: any) => {
  const stringValsArr = values.map((item: any) => {
    let dsBadge: any = Envs.DNA_APPNAME_HEADER;
    if (dsList.length > 0) {
      const dataSource = dsList.filter((ds: any) => ds.name === item.dataSource);
      if (dataSource.length === 1) {
        if (dataSource[0].source !== null && dataSource[0].dataType !== null) {
          if (dataSource[0].dataType !== undefined && dataSource[0].source !== undefined) {
            if (dataSource[0].dataType === 'Not set') {
              dsBadge = dataSource[0].source;
            } else {
              dsBadge =
                dataSource[0].source +
                '-' +
                dataSource[0].dataType.charAt(0).toUpperCase() +
                dataSource[0].dataType.slice(1);
            }
          }
        }
      }
      return item.dataSource + ' (' + dsBadge + (item.weightage !== 0 ? ' - ' + item.weightage + '%' : '') + ')';
    }
    return item.dataSource + (item.weightage !== 0 ? ' (' + item.weightage + '%)' : '');
  });
  return processDataValues(stringValsArr);
};

const processDataValuesFromObj = (values: any[]) => {
  const stringValsArr = values.map((item: any) => item.name);
  return processDataValues(stringValsArr);
};

const formatEmptyText = (displayVal: string) => {
  return displayVal && displayVal !== '' && displayVal.toLocaleLowerCase() !== 'choose' ? displayVal : 'NA';
};

const teamMembersList = (members: ITeams[]) => {
  return members.map((member: ITeams, index: number) => {
    const isInternalMember = member.userType === TeamMemberType.INTERNAL;
    return (
      <View key={index} style={{ display: 'flex', flexDirection: 'row', width: '50%', marginBottom: 15 }}>
        <View style={{ width: 30, height: 30, marginRight: 10 }}>
          {isInternalMember ? (
            <Image style={{ width: 'auto', height: 'auto' }} src={ImgTeamInternalAvatar} />
          ) : (
            <Image style={{ width: 'auto', height: 'auto' }} src={ImgTeamExternalAvatar} />
          )}
        </View>
        <View>
          <Text style={[styles.sectionTitle, { marginBottom: 5 }]}>{`${member.teamMemberPosition}${!isInternalMember ? ' (' + member.userType + ')' : ''
            }`}</Text>
          {isInternalMember ? (
            <View>
              <Text style={{ marginBottom: 15, marginTop: 5 }}>
                <Link src={TEAMS_PROFILE_LINK_URL_PREFIX + member.shortId}>
                  <Text>
                    {member.firstName} {member.lastName}
                  </Text>
                </Link>
              </Text>
              <Text>{member.department}</Text>
              <Text>{member?.isUseCaseOwner ? 'Use Case Owner' : ''}</Text>
            </View>
          ) : (
            <View>
              <Text>{member.company}</Text>
              <Text>&nbsp;</Text>
            </View>
          )}
        </View>
      </View>
    );
  });
};

const linkList = (links: ILink[]) => {
  return links.map((link: ILink, index: number) => {
    return (
      <View key={index} style={{ display: 'flex', flexDirection: 'row', width: '50%', marginBottom: 15 }}>
        <View style={{ width: 10, height: 10, marginRight: 10 }}>
          <Image style={{ width: 'auto', height: 'auto' }} src={ImgLink} />
        </View>
        <Text>
          <Link src={link.link}>
            <Text>{link.link}</Text>
          </Link>
        </Text>
      </View>
    );
  });
};

const attachmentList = (attachments: IAttachment[]) => {
  return attachments.map((attachment: IAttachment, index: number) => {
    return (
      <View key={index} style={{ display: 'flex', flexDirection: 'row', width: '50%', marginBottom: 15 }}>
        <View style={{ width: 10, height: 10, marginRight: 10 }}>
          <Image style={{ width: 'auto', height: 'auto' }} src={ImgAttachment} />
        </View>
        <Text>{attachment.fileName}</Text>
      </View>
    );
  });
};

const costDrivers = (costFactors: ICostFactor[]) => {
  return costFactors.map((costFactor: ICostFactor, index: number) => {
    return (
      <View key={index} wrap={false}>
        <Text style={styles.sectionTitle}>{`Cost Factor ${index + 1} ${costFactor.description}`}</Text>
        <View style={[styles.flexLayout, { marginTop: 10 }]}>
          <View style={[styles.flexCol2, styles.firstCol]}>
            <Text style={styles.sectionTitle}>Description</Text>
            <Text>{costFactor.description}</Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Category</Text>
            <Text>{costFactor.category}</Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Value</Text>
            <Text>
              {costFactor.value !== '' ? (
                <IntlProvider locale={navigator.language} defaultLocale="en">
                  <FormattedNumber value={Number(costFactor.value)} />
                </IntlProvider>
              ) : (
                ''
              )}
              &euro;
            </Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Source</Text>
            <Text>{costFactor.source}</Text>
          </View>
        </View>
        <Text style={styles.sectionTitle}>Ramp-up</Text>
        <View style={styles.flexLayout}>
          {costFactor.rampUp.map((item: any, rampIndex: number) => (
            <View key={rampIndex} style={[styles.rampUpContainer, styles.smallText]}>
              <Text>{item.year}</Text>
              <Text>
                {item.value !== '' ? (
                  <IntlProvider locale={navigator.language} defaultLocale="en">
                    <FormattedNumber value={Number(item.value)} />
                  </IntlProvider>
                ) : (
                  ''
                )}
                &euro;
              </Text>
            </View>
          ))}
        </View>
        <View style={styles.costFactorSeperator} />
      </View>
    );
  });
};

const valueDrivers = (valueFactors: IValueFactor[], hidePercent: boolean) => {
  let totalSavings = 0;
  let totalRevenue = 0;
  const valueDriversViews = valueFactors.map((valueFactor: IValueFactor, index: number) => {
    totalSavings += valueFactor.category === dataValueSavingsKeyValue ? parseFloat(valueFactor.value) : 0;
    totalRevenue += valueFactor.category === dataValueRevenueKeyValue ? parseFloat(valueFactor.value) : 0;
    return (
      <View key={index} wrap={false}>
        <Text style={styles.sectionTitle}>{`Value Factor ${index + 1} ${valueFactor.description}`}</Text>
        <View style={[styles.flexLayout, { marginTop: 10 }]}>
          <View style={[styles.flexCol2, styles.firstCol]}>
            <Text style={styles.sectionTitle}>Description</Text>
            <Text>{valueFactor.description}</Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Category</Text>
            <Text>
              {!hidePercent ? valueFactor.category : SOLUTION_DATA_VALUE_CATEGORY_TYPES[valueFactor.category]}
            </Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Value</Text>
            <Text>
              {valueFactor.value !== '' ? (
                <IntlProvider locale={navigator.language} defaultLocale="en">
                  <FormattedNumber value={Number(valueFactor.value)} />
                </IntlProvider>
              ) : (
                ''
              )}
              &euro;
            </Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Source</Text>
            <Text>{valueFactor.source}</Text>
          </View>
        </View>
        <Text style={styles.sectionTitle}>Ramp-up</Text>
        <View style={styles.flexLayout}>
          {valueFactor.rampUp.map((item: any, rampIndex: number) => (
            <View key={rampIndex} style={[styles.rampUpContainer, styles.smallText]}>
              <Text>{item.year}</Text>
              {!hidePercent && (
                <Text>
                  {item.percent !== '' ? (
                    <IntlProvider locale={navigator.language} defaultLocale="en">
                      <FormattedNumber value={Number(item.percent)} />
                    </IntlProvider>
                  ) : (
                    ''
                  )}
                  %
                </Text>
              )}
              <Text>
                {item.value !== '' ? (
                  <IntlProvider locale={navigator.language} defaultLocale="en">
                    <FormattedNumber value={Number(item.value)} />
                  </IntlProvider>
                ) : (
                  ''
                )}
                &euro;
              </Text>
            </View>
          ))}
        </View>
        <View style={styles.costFactorSeperator} />
      </View>
    );
  });

  return <>{valueDriversViews}{hidePercent && (
    <View style={styles.sectionTitle}>
      <Text>
        Total {SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueSavingsKeyValue]} -{' '}
        <IntlProvider locale={navigator.language} defaultLocale="en">
          <FormattedNumber value={Number(totalSavings)} />
        </IntlProvider>
        &euro;
      </Text>
      <Text>
        Total {SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueRevenueKeyValue]} -{' '}
        <IntlProvider locale={navigator.language} defaultLocale="en">
          <FormattedNumber value={Number(totalRevenue)} />
        </IntlProvider>
        &euro;
      </Text>
    </View>
  )}</>;
};

const digitalValue = (items: IValueRampUp[]) => {
  return items.map((item: IValueRampUp, index: number) => {
    return (
      <View key={index} style={[styles.rampUpContainer, styles.smallText]}>
        <Text>{item.year}</Text>
        <Text>
          {item.percent !== '' ? (
            <IntlProvider locale={navigator.language} defaultLocale="en">
              <FormattedNumber value={Number(item.percent)} />
            </IntlProvider>
          ) : (
            ''
          )}
          %
        </Text>
        <Text>
          {item.value !== '' ? (
            <IntlProvider locale={navigator.language} defaultLocale="en">
              <FormattedNumber value={Number(item.value)} />
            </IntlProvider>
          ) : (
            ''
          )}
          &euro;
        </Text>
      </View>
    );
  });
};

const dataValue = (items: IDataValueRampUp[]) => {
  return items.map((item: IDataValueRampUp, index: number) => {
    return (
      <View key={index} style={[styles.rampUpContainer, styles.smallText]}>
        <Text>{item.year}</Text>
        <Text>
          <IntlProvider locale={navigator.language} defaultLocale="en">
            <FormattedNumber value={Number(item.value)} />
          </IntlProvider>
          &euro;
        </Text>
      </View>
    );
  });
};

const dataComplianceProcessFlow = (dataCompliance: IDataCompliance) => {
  const image =
    dataCompliance.quickCheck &&
      !dataCompliance.expertGuidelineNeeded &&
      !dataCompliance.useCaseDescAndEval &&
      !dataCompliance.readyForImplementation ? (
      <Image style={{ width: 450, height: 150 }} src={ImgQuickCheck} />
    ) : dataCompliance.quickCheck &&
      dataCompliance.expertGuidelineNeeded &&
      !dataCompliance.useCaseDescAndEval &&
      !dataCompliance.readyForImplementation ? (
      <Image style={{ width: 450, height: 150 }} src={ImgQuickCheckReady} />
    ) : dataCompliance.quickCheck &&
      !dataCompliance.expertGuidelineNeeded &&
      dataCompliance.useCaseDescAndEval &&
      !dataCompliance.readyForImplementation ? (
      <Image style={{ width: 450, height: 150 }} src={ImgUseCaseCheck} />
    ) : dataCompliance.quickCheck &&
      !dataCompliance.expertGuidelineNeeded &&
      dataCompliance.useCaseDescAndEval &&
      dataCompliance.readyForImplementation ? (
      <Image style={{ width: 450, height: 150 }} src={ImgUseCaseCheckReady} />
    ) : (
      <Image style={{ width: 450, height: 150 }} src={ImgNotCompletedGuideline} />
    );
  return <View style={styles.imageSection}>{image}</View>;
};

const getPhaseItemView = (phaseItem: IPhasesItem, phaseImageFileName: string, firstItem?: boolean) => {
  const canShowPhase = phaseItem ? phaseItem.month && phaseItem.year : false;
  const activeState = { backgroundColor: canShowPhase ? '#697582' : '#C0C8D0' };
  if (!phaseItem) {
    // Creating dummy phaseItem place holder value if phaseItem is null since we use 'opacity' to hide phase view
    const phase: IPhase = { id: '1', name: 'Kick-off' };
    phaseItem = { month: 1, year: 1971, description: '', phase };
  }
  return (
    <View style={styles.milestoneCol}>
      <Image style={{ width: 25, opacity: canShowPhase ? 1 : 0 }} src={phaseImageFileName} />
      <View style={[styles.milestoneValueView, { opacity: canShowPhase ? 1 : 0 }]}>
        <Text style={[styles.sectionTitle, styles.setMarginTop15, styles.noMarginBottom]}>{phaseItem.phase.name}</Text>
        <Text>
          {/* {phaseItem.month >= 10 ? phaseItem.month : '0' + phaseItem.month}/{phaseItem.year} */}
          {phaseItem.month > 0 && phaseItem.year > 0
            ? regionalForMonthAndYear(phaseItem.month + '/' + '01' + '/' + phaseItem.year)
            : ''}
        </Text>
      </View>
      {firstItem ? (
        <View style={styles.milestoneTimelineView}>
          <View style={[styles.milestoneTimelineDot, activeState]} />
          <View style={[styles.milestoneTimelineLine, activeState]} />
          <View style={[styles.milestoneTimelineDot, { left: 77.5 }, activeState]} />
        </View>
      ) : (
        <View style={styles.milestoneTimelineView}>
          <View style={[styles.milestoneTimelineLine, { left: 2.5, width: 72.5 }, activeState]} />
          <View style={[styles.milestoneTimelineDot, { left: 77.5 }, activeState]} />
        </View>
      )}
      <View style={[styles.milestoneCommentView, { opacity: canShowPhase ? 1 : 0 }]}>
        <Text>{phaseItem.description}</Text>
      </View>
    </View>
  );
};

const milestonesView = (milestones: IMilestonesList) => {
  const phases = milestones.phases;
  return (
    <View style={{ display: 'flex', flexDirection: 'row', marginBottom: 25 }}>
      {getPhaseItemView(phases[0], ImgKickOff, true)}
      {getPhaseItemView(phases[1], ImgIdeation)}
      {getPhaseItemView(phases[2], ImgPOC)}
      {getPhaseItemView(phases[3], ImgPilot)}
      {getPhaseItemView(phases[4], ImgProfessionalization)}
      {getPhaseItemView(phases[5], ImgRollout)}
    </View>
  );
};

const pageNumberRender = (pageInfo: any) => {
  return `${pageInfo.pageNumber} - ${pageInfo.totalPages}`;
};

const neededRoles = (neededRoles: INeededRoleObject[]) => {
  return neededRoles.map((neededRole: INeededRoleObject, index: number) => {
    return (
      <View key={index} style={[styles.rampUpContainer, styles.smallText]}>
        <Text>{neededRole.neededSkill}</Text>
        <Text>
          <IntlProvider locale={navigator.language} defaultLocale="en">
            {neededRole.requestedFTECount ? <FormattedNumber value={Number(neededRole.requestedFTECount)} /> : 'N/A'}
          </IntlProvider>
        </Text>
      </View>
    );
  });
};

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

const personasList = (selectedPersonas: any[]) => {
  const selectedPersonasToShow = personas.filter(item => selectedPersonas.includes(item.value))
  return selectedPersonasToShow.map((persona: any, index: number) => {
    return (
      <View key={index} style={{ display: 'flex', flexDirection: 'row', width: '50%', marginBottom: 15 }}>
        <View style={{ width: 30, height: 30, marginRight: 10 }}>
          <Image style={{ width: 'auto', height: 'auto' }} src={persona.avatar} />
        </View>
        <View>
          <Text style={[styles.sectionTitle, { marginBottom: 5 }]}>
            {persona.name}
          </Text>
          <View>
            <Text>{persona.description}</Text>
            <Text>&nbsp;</Text>
          </View>

        </View>
      </View>
    );
  });
};

interface SummaryPdfDocProps {
  solution: ICreateNewSolutionData;
  dataSources?: any;
  lastModifiedDate: string;
  createdDate: string;
  canShowTeams: boolean;
  canShowPlatform: boolean;
  canShowMilestones: boolean;
  canShowDataSources: boolean;
  canShowDigitalValue: boolean;
  canShowComplianceSummary: number | boolean;
  isGenAi: boolean;
  user: IUserInfo;
  noteBookInfo: INotebookInfo;
  dataIkuInfo: IDataiku;
  dnaNotebookEnabled: boolean;
  dnaDataIkuProjectEnabled: boolean;
  notebookAndDataIkuNotEnabled: boolean;
  children?: any;
}
export const SummaryPdfDoc = (props: SummaryPdfDocProps) => (
  // @ts-ignore
  <Document>
    {/* @ts-ignore */}
    <Page style={styles.page} wrap={true}>
      <View style={styles.view}>
        <Text style={styles.title}>{props.solution.description.productName}</Text>
        <Text style={styles.subTitle}>Solution Summary</Text>
        <View style={styles.flexLayout} wrap={false}>
          <View style={[styles.flexCol2, styles.firstCol, { marginRight: 20 }]}>
            <Text style={styles.sectionTitle}>Description</Text>
            <Text>{props.solution.description.description}</Text>
          </View>
          <View style={[styles.flexCol2]}>
            <Text style={styles.sectionTitle}>Tags</Text>
            {props.solution.description.tags && props.solution.description.tags.length ? (
              <Text>{props.solution.description.tags.join(', ')}</Text>
            ) : (
              <Text>NA</Text>
            )}
          </View>
        </View>
        <View style={styles.seperatorLine} />
        <View style={styles.flexLayout} wrap={false}>
          <View style={[styles.flexCol2, styles.firstCol]}>
            <Text style={styles.sectionTitle}>Division</Text>
            <Text>{props.solution.description.division?.name || 'NA'}</Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Sub Division</Text>
            <Text>{props.solution.description.division?.subdivision?.name || 'NA'}</Text>
          </View>
          <View style={styles.flexCol2}>
            <Text style={styles.sectionTitle}>Status</Text>
            <Text>{props.solution.description.status.name}</Text>
          </View>
          <View style={[styles.flexCol2, styles.wideCol]}>
            <Text style={styles.sectionTitle}>Location</Text>
            <Text>
              {props.solution.description.location
                ? props.solution.description.location.length > 0
                  ? props.solution.description.location.length === TOTAL_LOCATIONS_COUNT
                    ? 'All'
                    : props.solution.description.location.map((item: any) => item.name).join(', ')
                  : 'NA'
                : 'NA'}
            </Text>
          </View>
        </View>
        <View style={styles.flexLayout} wrap={false}>
          <View style={[styles.flexCol2, styles.firstCol]}>
            <Text style={styles.sectionTitle}>Data Strategy Domain</Text>
            <Text>{props.solution.description.dataStrategyDomain === 'Choose' ? 'NA' : props.solution.description.dataStrategyDomain}</Text>
          </View>
          <View style={[styles.flexCol2, styles.wideCol]}>
            <Text style={styles.sectionTitle}>Register support of additional resources</Text>
            <Text>
              {props.solution.description.additionalResource ? props.solution.description.additionalResource : 'N/A'}
            </Text>
          </View>
        </View>
        <View style={styles.flexLayout} wrap={false}>
          <View style={[styles.flexCol2, styles.firstCol]}>
            <Text style={styles.sectionTitle}>Created On</Text>
            <Text>{props.createdDate ? getDateTimeFromTimestamp(props.createdDate) : '-'}</Text>
          </View>
          <View style={[styles.flexCol2]}>
            <Text style={styles.sectionTitle}>Department</Text>
            <Text>
              {props.solution.description?.department ? props.solution.description?.department : 'NA'}
            </Text>
          </View>
          <View style={[styles.flexCol2, styles.wideCol]}>
            <Text style={styles.sectionTitle}>Last Modified On</Text>
            <Text>{props.lastModifiedDate ? getDateTimeFromTimestamp(props.lastModifiedDate) : '-'}</Text>
          </View>
        </View>
        <View style={styles.seperatorLine} />
        <View style={styles.flexLayout} wrap={false}>
          <View style={[styles.flexCol2, styles.firstCol, { marginRight: 20 }]}>
            <Text style={styles.sectionTitle}>Related Products</Text>
            {props.solution.description.relatedProducts && props.solution.description.relatedProducts.length ? (
              <Text>{props.solution.description.relatedProducts.join(', ')}</Text>
            ) : (
              <Text>NA</Text>
            )}
          </View>
          <View style={[styles.flexCol2, styles.firstCol, { marginRight: 20 }]}>
            <Text style={styles.sectionTitle}>LeanIX App ID</Text>
            {props?.solution?.description?.leanIXDetails?.appReferenceStr ? (
              <Link 
              to="#"
              src={`${Envs.LEANIX_BASEURL}/${props?.solution?.description?.leanIXDetails?.appReferenceStr}`}
              style={{ color: 'blue', textDecoration: 'underline' }} // Optional: style the link
            >
              {props?.solution?.description?.appId}
            </Link>
            ) : (
              <Text>{props?.solution?.description?.appId ? `${props?.solution?.description?.appId}` : 'NA'}</Text>
            )}
          </View>
          <View style={[styles.flexCol2]}>
            <Text style={styles.sectionTitle}>Business Goals</Text>
            {props.solution.description.businessGoal ? (
              <Text>{props.solution.description.businessGoal.join(', ')}</Text>
            ) : (
              <Text>NA</Text>
            )}
          </View>
        </View>
        <View style={styles.seperatorLine} />
        <View style={styles.flexLayout}>
          <View style={styles.flexCol1}>
            <Text style={styles.sectionTitle}>Expected Benefits</Text>
            <Text>{props.solution.description.expectedBenefits}</Text>
          </View>
        </View>
        <View style={[styles.flexLayout]}>
          <View style={styles.flexCol1}>
            <Text style={styles.sectionTitle}>Business Need</Text>
            <Text>{props.solution.description.businessNeeds}</Text>
          </View>
        </View>
        <View wrap={false}>
          <Text style={[styles.subTitle, styles.setMarginTop]}>Attached Files</Text>
          <View style={styles.flexLayout}>
            {props.solution.description.attachments ? (
              attachmentList(props.solution.description.attachments)
            ) : (
              <Text>NA</Text>
            )}
          </View>
        </View>
        {props.solution.description.status.id === '4' || props.solution.description.status.id === '5' ? (
          <View style={[styles.flexLayout, styles.noMarginTop]} wrap={false}>
            <View style={styles.flexCol1}>
              <Text style={styles.sectionTitle}>Reason of "On hold" / "Closed"</Text>
              <Text>{props.solution.description.reasonForHoldOrClose}</Text>
            </View>
          </View>
        ) : (
          <View />
        )}
        <View style={styles.seperatorLine} />

        {props.canShowPlatform && (
          <View wrap={false}>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Compute</Text>
            </View>
            <View style={styles.flexLayout}>
              <View style={styles.flexCol4}>
                <View>
                  <Text style={styles.sectionTitle}>Solution On Cloud</Text>
                  {props.solution.portfolio.solutionOnCloud ? (
                    <Text style={{ paddingLeft: 13 }}>
                      <Image src={ImgTick} style={{ width: 15 }} />
                    </Text>
                  ) : (
                    <Text>NA</Text>
                  )}
                </View>
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Usage Of {Envs.DNA_COMPANY_NAME} Platforms</Text>
                {props.solution.portfolio.usesExistingInternalPlatforms ? (
                  <Text style={{ paddingLeft: 13 }}>
                    <Image src={ImgTick} style={{ width: 15 }} />
                  </Text>
                ) : (
                  <Text>NA</Text>
                )}
                {(props.dnaNotebookEnabled || props.dnaDataIkuProjectEnabled) && (
                  <View>
                    <View style={{ marginBottom: 5 }}>
                      <View style={{ display: 'flex', flexDirection: 'row', width: '70%', marginTop: 5 }}>
                        <View style={{ width: 36 }}>
                          {props.dnaNotebookEnabled && <Image src={jupeterImg} style={{ width: 21 }} />}
                          {props.dnaDataIkuProjectEnabled && <Image src={dataIkuimg} style={{ width: 21 }} />}
                        </View>
                        <View>
                          <Text style={[styles.sectionTitle, { marginBottom: 2 }]}>
                            {(props.dnaNotebookEnabled && props.noteBookInfo.name) ||
                              (props.dnaDataIkuProjectEnabled && (
                                <Link
                                  src={
                                    props.dataIkuInfo?.cloudProfile?.toLowerCase().includes('extollo')
                                      ? Envs.DATAIKU_LIVE_APP_URL + '/projects/' + props.dataIkuInfo.projectKey + '/'
                                      : Envs.DATAIKU_LIVE_ON_PREMISE_APP_URL +
                                        '/projects/' +
                                        props.dataIkuInfo.projectKey +
                                        '/'
                                  }
                                >
                                  <Text>{props.dataIkuInfo.name}</Text>
                                </Link>
                              ))}
                            {props.dnaDataIkuProjectEnabled && (
                              <> ({getDataikuInstanceTag(props?.dataIkuInfo?.cloudProfile)})</>
                            )}
                          </Text>
                          <View>
                            <Text>
                              {(props?.dnaNotebookEnabled && props?.noteBookInfo?.createdOn) ||
                              (props?.dnaDataIkuProjectEnabled && props?.dataIkuInfo?.creationTag?.lastModifiedOn)
                                ? `Created on ${getDateFromTimestamp(
                                    (props.dnaNotebookEnabled && props.noteBookInfo.createdOn) ||
                                      (props.dnaDataIkuProjectEnabled && props.dataIkuInfo.creationTag?.lastModifiedOn),
                                    '.',
                                  )}`
                                : ''}{' '}
                              {(props.dnaNotebookEnabled &&
                                props.noteBookInfo.createdBy.firstName &&
                                'by ' + props.dataIkuInfo?.ownerDisplayName) ||
                                props.dataIkuInfo?.ownerLogin ||
                                ''}
                            </Text>
                            <Text>
                              {(props.dnaNotebookEnabled && props.noteBookInfo.description) ||
                                (props.dnaDataIkuProjectEnabled && props.dataIkuInfo.shortDesc)}
                            </Text>
                          </View>
                        </View>
                      </View>
                    </View>
                  </View>
                )}
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Platform</Text>
                {props.solution.portfolio.platforms && props.solution.portfolio.platforms.length > 0 ? (
                  processDataValuesFromObj(props.solution.portfolio.platforms)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
            </View>
            <View style={styles.seperatorLineLight} />
          </View>
        )}
        <View style={styles.seperatorLine} />
        {props.canShowTeams ? (
          <View>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Team</Text>
              <View style={styles.flexLayout}>{teamMembersList(props.solution.team.team)}</View>
              <View style={styles.seperatorLine} />
            </View>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop, { marginBottom: 10 }]}>Needed Roles/Skills</Text>
              <View style={styles.flexLayout}>
                {props.solution.neededRoles ? (
                  props.solution.neededRoles && props.solution.neededRoles.length ? (
                    neededRoles(props.solution.neededRoles)
                  ) : (
                    <Text>NA</Text>
                  )
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
          </View>
        ) : (
          <View />
        )}
        {props.canShowMilestones ? (
          <View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Milestones</Text>
              <View style={styles.setMarginTop}>{milestonesView(props.solution.milestones)}</View>
            </View>
            {props.solution.milestones?.rollouts?.details && props.solution.milestones?.rollouts?.details.length > 0 ? (
              <View>
                <Text style={[styles.subTitle, styles.setMarginTop]}>Rollout Locations</Text>
                <View style={{ display: 'flex', flexDirection: 'row', marginBottom: 25, marginTop: 25 }}>
                  {props.solution.milestones?.rollouts?.details.map((rollout, index) => {
                    return (
                      <Text key={index}>
                        {rollout.location.name}(
                        {rollout.month > 0 && rollout.year > 0
                          ? regionalForMonthAndYear(rollout.month + '/' + '01' + '/' + rollout.year)
                          : ''}
                        ){index <= props.solution.milestones.rollouts.details.length - 2 ? ', ' : ''}
                      </Text>
                    );
                  })}
                </View>
                <View style={styles.seperatorLine} />
              </View>
            ) : (
              <Text></Text>
            )}
          </View>
        ) : (
          <View />
        )}
        {props.canShowDataSources ? (
          <View>
            {(props.solution.dataSources &&
              props.solution.dataSources.dataSources &&
              props.solution.dataSources.dataSources.length > 0) ||
            (props.solution.dataSources &&
              props.solution.dataSources.dataVolume &&
              props.solution.dataSources.dataVolume.name &&
              props.solution.dataSources.dataVolume.name !== 'Choose') ? (
              <View wrap={false}>
                <View style={styles.flexLayout}>
                  <View style={[styles.flexCol4, styles.firstCol]}>
                    <Text style={styles.subTitle}>Data Sources</Text>
                  </View>
                  <View style={styles.flexCol4}>
                    <Text style={styles.sectionTitle}>Data Sources</Text>
                    {props.solution.dataSources.dataSources && props.solution.dataSources.dataSources.length > 0 ? (
                      processDataSourceValues(props.solution.dataSources.dataSources, props.dataSources)
                    ) : (
                      <Text>NA</Text>
                    )}
                  </View>
                  <View style={styles.flexCol4}>
                    <Text style={styles.sectionTitle}>Total Data Volume</Text>
                    <Text>{formatEmptyText(props.solution.dataSources.dataVolume.name)}</Text>
                  </View>
                  <View style={styles.flexCol4}>
                    <Text>&nbsp;</Text>
                  </View>
                </View>
                <View style={styles.seperatorLineLight} />
              </View>
            ) : (
              <View />
            )}
          </View>
        ) : (
          <View />
        )}

        {(props.solution.analytics &&
          props.solution.analytics.algorithms &&
          props.solution.analytics.algorithms.length > 0) ||
        (props.solution.analytics &&
          props.solution.analytics.languages &&
          props.solution.analytics.languages.length > 0) ||
        (props.solution.analytics &&
          props.solution.analytics.visualizations &&
          props.solution.analytics.visualizations.length > 0) ? (
          <View wrap={false}>
            <View style={styles.flexLayout}>
              <View style={[styles.flexCol4, styles.firstCol]}>
                <Text style={styles.subTitle}>{props.isGenAi ?"Technology":"Analytics"}</Text>
              </View>
              {!props.isGenAi ?(<View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Languages</Text>
                {props.solution.analytics.languages && props.solution.analytics.languages.length > 0 ? (
                  processDataValuesFromObj(props.solution.analytics.languages)
                ) : (
                  <Text>NA</Text>
                )}
              </View>): ''}
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>{props.isGenAi?"GenAI Models ":"Models/Algorithms"}</Text>
                {props.solution.analytics.algorithms && props.solution.analytics.algorithms.length > 0 ? (
                  processDataValuesFromObj(props.solution.analytics.algorithms)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>{props.isGenAi ? "solutions" : "Visualization"}</Text>
                  {!props.isGenAi ? (
                    props.solution.analytics.visualizations && props.solution.analytics.visualizations.length > 0 ? (
                      processDataValuesFromObj(props.solution.analytics.visualizations)
                    ) : (
                      <Text>NA</Text>
                    )) : (
                    props.solution.analytics.analyticsSolution && props.solution.analytics.analyticsSolution.length > 0 ? (
                      processDataValuesFromObj(props.solution.analytics.analyticsSolution)
                    ) : (
                      <Text>NA</Text>
                    )
                  )
                  }
              </View>
              </View>
            <View style={styles.seperatorLineLight} />
          </View>
        ) : (
          <View />
        )}
        {props.solution.sharing &&
        ((props.solution.sharing.gitUrl && props.solution.sharing.gitUrl !== '') ||
          (props.solution.sharing.result &&
            props.solution.sharing.result.name &&
            props.solution.sharing.result.name !== 'Choose') ||
          (props.solution.sharing.resultUrl && props.solution.sharing.resultUrl !== '')) ? (
          <View wrap={false}>
            <View style={styles.flexLayout}>
              <View style={[styles.flexCol4, styles.firstCol]}>
                <Text style={styles.subTitle}>Sharing</Text>
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Git Repository</Text>
                {props.solution.sharing.gitUrl && props.solution.sharing.gitUrl !== '' ? (
                  <Link src={props.solution.sharing.gitUrl}>
                    <Text>{props.solution.sharing.gitUrl}</Text>
                  </Link>
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Results</Text>
                <Text>{formatEmptyText(props.solution.sharing.result.name)}</Text>
              </View>
              <View style={styles.flexCol4}>
                <Text style={styles.sectionTitle}>Comment</Text>
                {props.solution.sharing.resultUrl && props.solution.sharing.resultUrl !== '' ? (
                  <Link src={props.solution.sharing.resultUrl}>
                    <Text>{props.solution.sharing.resultUrl}</Text>
                  </Link>
                ) : (
                  <Text>NA</Text>
                )}
              </View>
            </View>
            <View style={styles.seperatorLineLight} />
          </View>
        ) : (
          <View />
        )}
        <View style={styles.seperatorLine} />
        {props.solution?.marketing &&
        (props.solution?.marketing?.customerJourneyPhases?.length > 0 ||
          props.solution?.marketing?.marketingCommunicationChannels?.length > 0 ||
          props.solution?.marketing?.personas?.length > 0 ||
          props.solution?.marketing?.personalization?.isChecked ||
          props.solution?.marketing?.marketingRoles?.length > 0) ? (
          <View>
            <Text style={[styles.subTitle, styles.setMarginTop]}>Marketing</Text>
            <View style={styles.flexLayout}>
            <View style={[styles.flexCol4, styles.firstCol]}>
              <Text style={styles.sectionTitle}>Use Case, Core Needs and Customer Journey Phase</Text>
              {props.solution?.marketing?.customerJourneyPhases?.length > 0 ? (
                <View>
                  {props.solution?.marketing?.customerJourneyPhases?.map((item, index) => {
                    return <Text key={index}>{item.name}</Text>;
                  })}
                </View>
              ) : (
                <Text>NA</Text>
                )}
              </View>
            </View>
            <View style={styles.flexLayout}>
              <View style={[styles.flexCol4, styles.firstCol]}>
                <Text style={styles.sectionTitle}>Marketing Communication Channels</Text>
                {props.solution?.marketing?.marketingCommunicationChannels?.length > 0 ? (
                  <Text>
                    {props.solution?.marketing?.marketingCommunicationChannels?.map((item) => item.name).join(', ')}
                  </Text>
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={[styles.flexCol4, styles.firstCol]}>
                <Text style={styles.sectionTitle}>Personalisation</Text>
                {props.solution?.marketing?.personalization?.isChecked ? (
                  <Text>{props.solution?.marketing?.personalization?.description}</Text>
                ) : (
                  <Text>NA</Text>
                )}
              </View>
            </View>
            <View>
              <Text style={[styles.sectionTitle, styles.setMarginTop]}>Personas</Text>
              {personasList(props.solution?.marketing?.personas).length ? (
                <View style={styles.flexLayout}>{personasList(props.solution?.marketing?.personas)}</View>
              ) : (
                <Text>NA</Text>
              )}
            </View>
            <View>
              <Text style={[styles.sectionTitle, styles.setMarginTop]}>Marketing Roles</Text>
              {props.solution?.marketing?.marketingRoles?.length > 0 ? (
                <View>
                  {props.solution?.marketing?.marketingRoles?.map((item, index) => {
                    return <Text key={index}>{item.role}</Text>;
                  })}
                </View>
              ) : (
                <Text>NA</Text>
              )}
            </View>
            <View style={styles.seperatorLine} />
          </View>
        ) : (
          <View />
        )}

        {props.canShowComplianceSummary ? (
          <View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop, styles.setNegativeMarginBottom]}>
                Compliance Framework / Process Flow
              </Text>
              <View style={styles.flexLayout}>{dataComplianceProcessFlow(props.solution.datacompliance)}</View>
              <View style={[styles.seperatorLine, styles.setNegativeMarginTop]} />
            </View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Results of the AI Risk Self-Assessment</Text>
              <View style={styles.flexLayout}>
                <Text>{AI_RISK_ASSESSMENT_TYPES[props.solution.datacompliance.aiRiskAssessmentType]}</Text>
              </View>
              <View style={styles.seperatorLine} />
            </View>
             <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Workers Council Approval</Text>
              <View style={styles.flexLayout}>
                <Text>
                  {!props.solution.datacompliance.workersCouncilApproval ? 'No' : 'Yes'}
                </Text>
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Added Links</Text>
              <View style={styles.flexLayout}>
                {props.solution.datacompliance.links && props.solution.datacompliance.links.length ? (
                  linkList(props.solution.datacompliance.links)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Attached Files</Text>
              <View style={styles.flexLayout}>
                {props.solution.datacompliance.attachments && props.solution.datacompliance.attachments.length ? (
                  attachmentList(props.solution.datacompliance.attachments)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Local Compliance Officers</Text>
              <View style={styles.flexLayout}>
                {props.solution.datacompliance.complianceOfficers &&
                props.solution.datacompliance.complianceOfficers.length ? (
                  teamMembersList(props.solution.datacompliance.complianceOfficers)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
          </View>
        ) : (
          <View />
        )}
        {props.solution.digitalValue && props.canShowDigitalValue ? (
          <View>
            <View wrap={false}>
              <Text style={[styles.title, { marginTop: 10 }]}>
                {SOLUTION_VALUE_CALCULATION_TYPES[props.solution.digitalValue.typeOfCalculation]} Summary
              </Text>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Maturity Level</Text>
              <View style={styles.flexLayout}>
                {props.solution.digitalValue.maturityLevel ? (
                  <Text>{props.solution.digitalValue.maturityLevel}</Text>
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Controllers</Text>
              <View style={styles.flexLayout}>
                {props.solution.digitalValue.projectControllers &&
                props.solution.digitalValue.projectControllers.length ? (
                  teamMembersList(props.solution.digitalValue.projectControllers)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop, { marginBottom: 10 }]}>Value Driver</Text>
              <View>
                {props.solution.digitalValue ? (
                  props.solution.digitalValue.valueDrivers && props.solution.digitalValue.valueDrivers.length ? (
                    valueDrivers(
                      props.solution.digitalValue.valueDrivers,
                      props.solution.digitalValue.typeOfCalculation === dataValueTypeKeyValue,
                    )
                  ) : (
                    <Text>NA</Text>
                  )
                ) : (
                  <Text>NA</Text>
                )}
              </View>
            </View>
            <View>
              <Text style={[styles.subTitle, styles.setMarginTop, { marginBottom: 10 }]}>Cost Driver</Text>
              <View>
                {props.solution.digitalValue ? (
                  props.solution.digitalValue.costDrivers && props.solution.digitalValue.costDrivers.length ? (
                    costDrivers(props.solution.digitalValue.costDrivers)
                  ) : (
                    <Text>NA</Text>
                  )
                ) : (
                  <Text>NA</Text>
                )}
              </View>
            </View>
            <View>
              <Text style={[styles.subTitle, { marginBottom: 10 }]}>
                {SOLUTION_VALUE_CALCULATION_TYPES[props.solution.digitalValue?.typeOfCalculation]}
              </Text>
              {props.solution.digitalValue?.typeOfCalculation === digitalValueTypeKeyValue && (
                <View style={[styles.flexLayout]}>
                  {props.solution.digitalValue ? (
                    props.solution.digitalValue.valueCalculator &&
                    props.solution.digitalValue.valueCalculator.calculatedValueRampUpYears &&
                    props.solution.digitalValue.valueCalculator.calculatedValueRampUpYears.length > 0 ? (
                      digitalValue(props.solution.digitalValue.valueCalculator.calculatedValueRampUpYears)
                    ) : (
                      <Text>NA</Text>
                    )
                  ) : (
                    <Text>NA</Text>
                  )}
                </View>
              )}
              {props.solution.digitalValue?.typeOfCalculation === dataValueTypeKeyValue && (
                <>
                  <Text>{SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueSavingsKeyValue]} Value Ramup</Text>
                  <View style={[styles.flexLayout]}>
                    {props.solution?.digitalValue ? (
                      props.solution.digitalValue?.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator?.calculatedValueRampUpYearsVO &&
                      props.solution.digitalValue.dataValueCalculator.calculatedValueRampUpYearsVO?.savings?.length >
                        0 ? (
                          dataValue(
                            props.solution.digitalValue.dataValueCalculator.calculatedValueRampUpYearsVO.savings
                          )
                      ) : (
                        <Text>NA</Text>
                      )
                    ) : (
                      <Text>NA</Text>
                    )}
                  </View>
                  <Text>{SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueRevenueKeyValue]} Value Ramup</Text>
                  <View style={[styles.flexLayout]}>
                    {props.solution.digitalValue ? (
                      props.solution.digitalValue.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator.calculatedValueRampUpYearsVO &&
                      props.solution.digitalValue.dataValueCalculator.calculatedValueRampUpYearsVO.revenue?.length >
                        0 ? (
                          dataValue(
                            props.solution.digitalValue.dataValueCalculator.calculatedValueRampUpYearsVO.revenue
                          )
                      ) : (
                        <Text>NA</Text>
                      )
                    ) : (
                      <Text>NA</Text>
                    )}
                  </View>
                </>
              )}
            </View>
            {props.solution.digitalValue?.typeOfCalculation === digitalValueTypeKeyValue && (
              <View>
                <View style={[styles.flexLayout]}>
                  <View style={[styles.flexCol2, styles.firstCol]}>
                    <Text style={styles.sectionTitle}>
                      {' '}
                      Digital Value at{' '}
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.calculatedDigitalValue ? (
                        <Text>{props.solution.digitalValue.valueCalculator.calculatedDigitalValue.valueAt}%</Text>
                      ) : (
                        <Text>%</Text>
                      )}
                    </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.calculatedDigitalValue ? (
                        props.solution.digitalValue.valueCalculator.calculatedDigitalValue.year +
                        ' (' +
                        props.solution.digitalValue.valueCalculator.calculatedDigitalValue.value ? (
                          <IntlProvider locale={navigator.language} defaultLocale="en">
                            <FormattedNumber
                              value={Number(props.solution.digitalValue.valueCalculator.calculatedDigitalValue.value)}
                            />
                          </IntlProvider>
                        ) : (
                          '' + '€' + ')'
                        )
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                  <View style={styles.flexCol2}>
                    <Text style={styles.sectionTitle}>
                      {' '}
                      Value Drivers (
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.valueFactorSummary ? (
                        props.solution.digitalValue.valueCalculator.valueFactorSummary.year
                      ) : (
                        <View />
                      )}
                      )
                    </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.valueFactorSummary &&
                      props.solution.digitalValue.valueCalculator.valueFactorSummary.value ? (
                        <Text>
                          {props.solution.digitalValue.valueCalculator.valueFactorSummary.value ? (
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              <FormattedNumber
                                value={Number(props.solution.digitalValue.valueCalculator.valueFactorSummary.value)}
                              />
                            </IntlProvider>
                          ) : (
                            ''
                          )}
                          &euro;
                        </Text>
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                  <View style={styles.flexCol2}>
                    <Text style={styles.sectionTitle}>
                      {' '}
                      Cost Drivers (
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.costFactorSummary ? (
                        props.solution.digitalValue.valueCalculator.costFactorSummary.year
                      ) : (
                        <View />
                      )}
                      )
                    </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.costFactorSummary &&
                      props.solution.digitalValue.valueCalculator.costFactorSummary.value ? (
                        <Text>
                          {' '}
                          {props.solution.digitalValue.valueCalculator.costFactorSummary.value ? (
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              <FormattedNumber
                                value={Number(props.solution.digitalValue.valueCalculator.costFactorSummary.value)}
                              />
                            </IntlProvider>
                          ) : (
                            ''
                          )}
                          &euro;
                        </Text>
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                  <View style={styles.flexCol2}>
                    <Text style={styles.sectionTitle}> Break Even Point </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.valueCalculator &&
                      props.solution.digitalValue.valueCalculator.breakEvenPoint ? (
                        props.solution.digitalValue.valueCalculator.breakEvenPoint
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                </View>
              </View>
            )}

            {props.solution.digitalValue?.typeOfCalculation === dataValueTypeKeyValue && (
              <View>
                <View style={[styles.flexLayout]}>
                  <View style={styles.flexCol2}>
                    <Text style={styles.sectionTitle}>
                      {SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueSavingsKeyValue]} Value Summary (
                      {props.solution.digitalValue.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO ? (
                        props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO.year
                      ) : (
                        <View />
                      )}
                      )
                    </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO &&
                      props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO.value ? (
                        <Text>
                          {props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO.value ? (
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              <FormattedNumber
                                value={Number(props.solution.digitalValue.dataValueCalculator.savingsValueFactorSummaryVO.value)}
                              />
                            </IntlProvider>
                          ) : (
                            ''
                          )}
                          &euro;
                        </Text>
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                  <View style={styles.flexCol2}>
                    <Text style={styles.sectionTitle}>
                      {SOLUTION_DATA_VALUE_CATEGORY_TYPES[dataValueRevenueKeyValue]} Value Summary (
                      {props.solution.digitalValue.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO ? (
                        props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO.year
                      ) : (
                        <View />
                      )}
                      )
                    </Text>
                    <Text style={styles.sectionTitle}>
                      {props.solution.digitalValue.dataValueCalculator &&
                      props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO &&
                      props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO.value ? (
                        <Text>
                          {props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO.value ? (
                            <IntlProvider locale={navigator.language} defaultLocale="en">
                              <FormattedNumber
                                value={Number(props.solution.digitalValue.dataValueCalculator.revenueValueFactorSummaryVO.value)}
                              />
                            </IntlProvider>
                          ) : (
                            ''
                          )}
                          &euro;
                        </Text>
                      ) : (
                        <Text>NA</Text>
                      )}
                    </Text>
                  </View>
                </View>
              </View>
            )}

            <View wrap={false}>
              <Text style={[styles.subTitle]}>Attached Files</Text>
              <View style={styles.flexLayout}>
                {props.solution.digitalValue.attachments && props.solution.digitalValue.attachments.length ? (
                  attachmentList(props.solution.digitalValue.attachments)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View style={styles.seperatorLine} />
            </View>
            <View style={styles.flexLayout} wrap={false}>
              <View style={styles.flexCol1}>
                <Text style={[styles.subTitle, { marginBottom: 15 }]}>Strategy & Risk Assessment</Text>
                <Text style={styles.sectionTitle}>Strategic Relevance</Text>
                <Text style={{ marginBottom: 12 }}>
                  {props.solution.digitalValue.assessment &&
                  props.solution.digitalValue.assessment.strategicRelevance ? (
                    props.solution.digitalValue.assessment.strategicRelevance
                  ) : (
                    <Text>NA</Text>
                  )}
                </Text>
                <Text>
                  {props.solution.digitalValue.assessment &&
                  props.solution.digitalValue.assessment.commentOnStrategicRelevance ? (
                    props.solution.digitalValue.assessment.commentOnStrategicRelevance
                  ) : (
                    <View />
                  )}
                </Text>
              </View>
            </View>
            <View style={[styles.flexLayout, styles.noMarginTop]} wrap={false}>
              <View style={styles.flexCol1}>
                <Text style={styles.sectionTitle}>Benefit Realization Risk</Text>
                <Text style={{ marginBottom: 12 }}>
                  {props.solution.digitalValue.assessment &&
                  props.solution.digitalValue.assessment.benefitRealizationRisk ? (
                    props.solution.digitalValue.assessment.benefitRealizationRisk
                  ) : (
                    <Text>NA</Text>
                  )}
                </Text>
                <Text>
                  {props.solution.digitalValue.assessment &&
                  props.solution.digitalValue.assessment.commentOnBenefitRealizationRisk ? (
                    props.solution.digitalValue.assessment.commentOnBenefitRealizationRisk
                  ) : (
                    <View />
                  )}
                </Text>
              </View>
            </View>
            <View style={styles.seperatorLine} />
            <View wrap={false}>
              <Text style={[styles.subTitle, styles.setMarginTop]}>Share / Permission</Text>
              <View style={styles.flexLayout}>
                {props.solution.digitalValue.permissions && props.solution.digitalValue.permissions.length ? (
                  teamMembersList(props.solution.digitalValue.permissions)
                ) : (
                  <Text>NA</Text>
                )}
              </View>
              <View fixed={true} style={styles.footer}>
                <Text>{props.solution.description.productName} - Summary</Text>
              </View>
            </View>
          </View>
        ) : (
          <View />
        )}
        <View fixed={true} style={{bottom: 0}}>
          <Text fixed={true} style={styles.pageNumber} render={pageNumberRender} />
        </View>
      </View>
    </Page>
  </Document>
);
