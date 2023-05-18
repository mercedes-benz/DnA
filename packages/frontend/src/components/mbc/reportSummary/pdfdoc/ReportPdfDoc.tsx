import { Document, Font, Page, StyleSheet, Text, View, Image, Link } from '@react-pdf/renderer';
import * as React from 'react';
import { PropsWithChildren } from 'react';
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
import {
  ICreateNewReport,
  ICustomers,
  IDataAndFunctions,
  IDescriptionRequest,
  IKpis,
  IMembers,
  ITeams,
  IUserInfo,
} from 'globals/types';
import { TEAMS_PROFILE_LINK_URL_PREFIX } from 'globals/constants';

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

interface IReportPDFProps {
  report: ICreateNewReport;
  canShowDataFunction: boolean;
  canShowCustomer: boolean;
  canShowMember: boolean;
  canShowKpi: boolean;
  user: IUserInfo;
}

interface ICustomerProps {
  customer: ICustomers;
  showCustomer: boolean;
}

interface IKPIProps {
  kpis: IKpis[];
  showKPI: boolean;
}

interface IDataAndFunctionsProps {
  dataAndFunctions: IDataAndFunctions;
  showDataAndFunction: boolean;
  showMembers: boolean;
}

interface IMembersProps {
  members: IMembers;
  showMembers: boolean;
}

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
    // borderTopColor: '#697582',
    // borderTopWidth: 0.5,
    width: '100%',
    paddingTop: 5,
  },
  pageNumber: {
    position: 'absolute',
    right: 0,
    bottom: 0,
  },
});

const teamMembersList = (members: ITeams[]) => {
  return members?.map((member: ITeams, index: number) => {
    const isInternalMember = member?.userType === TeamMemberType.INTERNAL;
    return (
      <View key={index} style={{ display: 'flex', flexDirection: 'row', width: '50%', marginBottom: 15 }}>
        <View style={{ width: 45 }}>
          {isInternalMember ? (
            <Image style={{ width: 30 }} src={ImgTeamInternalAvatar} />
          ) : (
            <Image style={{ width: 30 }} src={ImgTeamExternalAvatar} />
          )}
        </View>
        <View>
          {member?.teamMemberPosition && (
          <Text style={[styles.sectionTitle, { marginBottom: 5 }]}>{`${member?.teamMemberPosition}${
            !isInternalMember ? ' (' + member?.userType + ')' : ''
          }`}</Text>
          )}
          {isInternalMember ? (
            <View>
              <Text>
                <Link src={TEAMS_PROFILE_LINK_URL_PREFIX + member?.shortId}>
                  <Text>
                    {member?.firstName} {member?.lastName}
                  </Text>
                </Link>
              </Text>
              <Text>{member?.department}</Text>
            </View>
          ) : (
            <View>
              <Text>{member?.company}</Text>
              <Text>&nbsp;</Text>
            </View>
          )}
        </View>
      </View>
    );
  });
};

const pageNumberRender = (pageInfo: any) => {
  return `${pageInfo.pageNumber} - ${pageInfo.totalPages}`;
};

const Description = (description: IDescriptionRequest) => (
  <>
    <View style={styles.flexLayout} wrap={false}>
      <View style={[styles.flexCol2, styles.firstCol, { marginRight: 20 }]}>
        <Text style={styles.sectionTitle}>Description</Text>
        <Text>{description.productDescription}</Text>
      </View>
      <View style={[styles.flexCol2]}>
        <Text style={styles.sectionTitle}>Tags</Text>
        {description.tags?.length ? <Text>{description.tags?.join(', ')}</Text> : <Text>NA</Text>}
      </View>
      <View style={[styles.flexCol2]}>
        <Text style={styles.sectionTitle}>Report Link</Text>
        <Text>
          <Link src={description.reportLink}>
            <Text>{description.reportLink}</Text>
          </Link>
        </Text>
      </View>
    </View>
    <View style={styles.seperatorLine} />
    <View style={styles.flexLayout} wrap={false}>
      <View style={[styles.flexCol2, styles.firstCol]}>
        <Text style={styles.sectionTitle}>Division</Text>
        <Text>
          {description.division?.name || description.division?.name === 'Choose' ? 'NA' : description.division?.name}
        </Text>
      </View>
      <View style={styles.flexCol2}>
        <Text style={styles.sectionTitle}>Sub Division</Text>
        <Text>{description.division?.subdivision?.name || 'NA'}</Text>
      </View>
      <View style={styles.flexCol2}>
        <Text style={styles.sectionTitle}>E2-Department</Text>
        <Text>{description.department || 'NA'}</Text>
      </View>
    </View>
    <View style={styles.flexLayout} wrap={false}>
      <View style={styles.firstCol}>
        <Text style={styles.sectionTitle}>Report Type</Text>
        <Text>{description.reportType && description?.reportType != '0' ? description.reportType : 'NA'}</Text>
      </View>
      <View style={styles.flexCol2}>
        <Text style={styles.sectionTitle}>Status</Text>
        <Text>{description.status || 'NA'}</Text>
      </View>
      <View style={styles.flexCol2}>
        <Text style={styles.sectionTitle}>Integrated In Portal</Text>
        {description.integratedPortal && description?.integratedPortal != '0' ? (
          <Text>{description.integratedPortal}</Text>
        ) : (
          <Text>NA</Text>
        )}
      </View>
    </View>
    <View style={styles.flexLayout} wrap={false}>
      <View style={styles.firstCol}>
        <Text style={styles.sectionTitle}>Agile Release Train</Text>
        {description.agileReleaseTrain && description.agileReleaseTrain != '0' ? (
          <Text>{description.agileReleaseTrain}</Text>
        ) : (
          <Text>NA</Text>
        )}
      </View>
      <View style={styles.flexCol2}>
        <Text style={styles.sectionTitle}>Frontend Technologies</Text>
        {description.frontendTechnologies?.length ? (
          <Text>{description.frontendTechnologies?.join(', ')}</Text>
        ) : (
          <Text>NA</Text>
        )}
      </View>
      <View style={styles.flexCol2}>
        <Text>{' '}</Text>
      </View>
    </View>
    <View style={styles.seperatorLine} />
  </>
);

const Customer = ({ customer, showCustomer }: ICustomerProps) => {
  return (
    <>
      {showCustomer &&
        customer.internalCustomers?.length &&
        <Text style={[styles.subTitle, styles.setMarginTop]}>Customer</Text>
      }
      {showCustomer &&
        customer.internalCustomers?.length &&
        customer.internalCustomers?.map((data: any, index: number) => (
          <React.Fragment key={index}>
            <View style={[styles.flexLayout, { marginBottom: 0 }]} wrap={false}>
              <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>{`Customer ${index + 1}`}</Text>
              </View>
            </View>
            <View style={[styles.flexLayout, { marginVertical: 15 }]} wrap={false}>  
              <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>Level</Text>
                <Text>{data.level || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Department</Text>
                <Text>{data.department || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>US-Risk (US-Access to sensible data)</Text>
                <Text>{data.accessToSensibleData || 'NA'}</Text>
              </View>
            </View>
            <View style={[styles.flexLayout, { marginVertical: 15 }]} wrap={false}>
              <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>MB Legal Entity</Text>
                <Text>{data.legalEntity || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Customer Relation</Text>
                <Text>{data.customerRelation || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Customer Division</Text>
                <Text>{data.division?.name || 'NA'}</Text>
              </View>
              
            </View>
            <View style={[styles.flexLayout, { marginVertical: 15 }]} wrap={false}>
            <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>Process Owner</Text>
                <View style={styles.flexLayout}>{teamMembersList([data?.processOwner])}</View>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Comment</Text>
                <Text>{data.comment || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}></View>
            </View>
            {customer.internalCustomers?.length > 0 ? (
              <View style={styles.seperatorLine} />
            ) : null}
          </React.Fragment>
        ))}

      {showCustomer &&
        customer.externalCustomers?.length &&
        customer.externalCustomers?.map((data: any, index: number) => (
          <React.Fragment key={index}>
            <View style={[styles.flexLayout, { marginBottom: 0 }]} wrap={false}>
              <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>{`External Customer ${index + 1}`}</Text>
              </View>
            </View>
            <View style={[styles.flexLayout, { marginVertical: 5 }]} wrap={false}>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Company Name</Text>
                <Text>{data.companyName || 'NA'}</Text>
              </View>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Customer Relation</Text>
                <Text>{data.customerRelation || 'NA'}</Text>
              </View>
              
            </View>
            <View style={[styles.flexLayout, { marginVertical: 15 }]} wrap={false}>
              <View style={styles.flexCol2}>
                <Text style={styles.sectionTitle}>Comment</Text>
                <Text>{data.comment || 'NA'}</Text>
              </View>
            </View>
            {customer.internalCustomers?.length > 0 ? (
              <View style={styles.seperatorLine} />
            ) : null}
          </React.Fragment>
        ))}  
    </>
  );
};

const KPI = ({ kpis, showKPI }: IKPIProps) => {
  return (
    <>
      {showKPI &&
        kpis?.length && 
        <Text style={[styles.subTitle, styles.setMarginTop]}>Content & Functions</Text>
      }
      {showKPI &&
        kpis?.length &&
        kpis?.map((kpi: any, index: number) => (
          <React.Fragment key={index}>
            <View wrap={false}>
              <View style={[styles.flexLayout, { marginBottom: 0 }]} wrap={false}>
                <View style={styles.firstCol}>
                  <Text style={styles.sectionTitle}>{`KPI ${index + 1}`}</Text>
                </View>
              </View>
              <View style={[styles.flexLayout, { marginVertical: 5 }]} wrap={false}>
                <View style={styles.firstCol}>
                  <Text style={styles.sectionTitle}>KPI Name</Text>
                  <Text>{kpi?.name?.kpiName || 'NA'}</Text>
                </View>
                <View style={styles.flexCol2}>
                  <Text style={styles.sectionTitle}>KPI Classification</Text>
                  <Text>{kpi?.name?.kpiClassification || 'NA'}</Text>
                </View>
                <View style={styles.flexCol2}>
                  <Text style={styles.sectionTitle}>Reporting Cause</Text>
                  <Text>{kpi.reportingCause ? kpi.reportingCause?.join(', ') : 'NA'}</Text>
                </View>
              </View>
              <View style={[styles.flexLayout, { marginVertical: 15 }]} wrap={false}>
                <View style={styles.firstCol}>
                  <Text style={styles.sectionTitle}>Link to KPI Information</Text>
                  {kpi.kpiLink ? <Link src={kpi.kpiLink}>{kpi.kpiLink}</Link> : <Text>NA</Text>}
                </View>
                <View style={styles.flexCol2}>
                  <Text style={styles.sectionTitle}>KPI Description</Text>
                  <Text>{kpi.comment || 'NA'}</Text>
                </View>
              </View>
              <View style={styles.seperatorLine} />
            </View>
          </React.Fragment>
        ))}
    </>
  );
};

const DataAndFunction = ({ dataAndFunctions, showDataAndFunction, showMembers }: IDataAndFunctionsProps) => {
  return (
    <>
      {showDataAndFunction && (
        <View>
          <Text style={[styles.subTitle, styles.setMarginTop]}>Data</Text>
          {dataAndFunctions.dataWarehouseInUse?.length
            ? dataAndFunctions.dataWarehouseInUse?.map((data: any, index: number) => (
                <React.Fragment key={index}>
                  <View style={[styles.flexLayout, { marginBottom: 10 }]} wrap={false}>
                    <View style={styles.firstCol}>
                      <Text style={styles.sectionTitle}>Data Warehouse</Text>
                      <Text>{data.dataWarehouse || 'NA'}</Text>
                    </View>
                  </View>
                  <View style={[styles.flexLayout, { marginVertical: 5 }]} wrap={false}>
                    <View style={styles.firstCol}>
                      <Text style={styles.sectionTitle}>Data Classification</Text>
                      {data.dataClassification ? <Text>{data.dataClassification}</Text> : <Text>NA</Text>}
                    </View>
                    <View style={styles.flexCol2}>
                      <Text style={styles.sectionTitle}>Connection Type</Text>
                      {data.connectionType ? <Text>{data.connectionType}</Text> : <Text>NA</Text>}
                    </View>
                  </View>
                  {(dataAndFunctions.dataWarehouseInUse?.length > 1 ||
                    dataAndFunctions?.singleDataSources?.length > 0) && <View style={styles.seperatorLine} />}
                </React.Fragment>
              ))
            : null}
          {dataAndFunctions.singleDataSources?.length
            ? dataAndFunctions.singleDataSources?.map((data: any, index: number) => (
                <React.Fragment key={index}>
                  <View style={[styles.flexLayout, { marginBottom: 0 }]} wrap={false}>
                    <View style={styles.firstCol}>
                      <Text style={styles.sectionTitle}>{`Single Data Source ${index + 1}`}</Text>
                    </View>
                  </View>
                  <View style={styles.flexLayout} wrap={false}>
                    <View style={styles.firstCol}>
                      <Text style={styles.sectionTitle}>Data Sources</Text>
                      {data.dataSources ? <Text>{data.dataSources?.map((item:any) => item.dataSource).join(' / ')}</Text> : <Text>NA</Text>}
                    </View>
                    <View style={styles.flexCol2}>
                      <Text style={styles.sectionTitle}>Data Classification</Text>
                      {data.dataClassification ? <Text>{data.dataClassification}</Text> : <Text>NA</Text>}
                    </View>
                    <View style={styles.flexCol2}>
                      <Text style={styles.sectionTitle}>Connection Type</Text>
                      {data.connectionType ? <Text>{data.connectionType}</Text> : <Text>NA</Text>}
                    </View>
                  </View>
                  {dataAndFunctions.singleDataSources?.length > 1 ? <View style={styles.seperatorLine} /> : null}
                </React.Fragment>
              ))
            : null}
          {showMembers ? <View style={styles.seperatorLine} /> : null}
        </View>
      )}
    </>
  );
};

const Members = ({ showMembers, members }: IMembersProps) => {
  return (
    <>
      {showMembers && (
        <View wrap={false}>
          <Text style={[styles.subTitle, styles.setMarginTop, { marginBottom: 25 }]}>Members</Text>
          {members.reportAdmins?.length ? (
            <View>
              <View style={styles.firstCol}>
                <Text style={styles.sectionTitle}>Report Administrator(s)</Text>
              </View>
              <View style={styles.flexLayout}>{teamMembersList(members.reportAdmins)}</View>
              <View style={styles.seperatorLine} />
            </View>
          ) : null}
        </View>
      )}
    </>
  );
};

type Props = PropsWithChildren<IReportPDFProps | any>;

export const ReportPdfDoc = (props: Props) => (
  <Document {...props}>
    <Page style={styles.page} wrap={true} {...props}>
      <View style={styles.view}>
        <Text style={styles.title}>
          {props.report.productName} ({props.report.reportId})
        </Text>
        <Text style={styles.subTitle}>Report Summary</Text>
        <Description {...props.report.description} />
        <Customer customer={props.report.customer} showCustomer={props.canShowCustomer} />
        <KPI kpis={props.report.kpis} showKPI={props.canShowKpi} />
        <DataAndFunction
          dataAndFunctions={props.report.dataAndFunctions}
          showDataAndFunction={props.canShowDataFunction}
          showMembers={props.canShowMember}
        />
        <Members members={props.report.members} showMembers={props.canShowMember} />
        <View fixed={true} style={{ marginTop: 25 }}>
          <Text fixed={true} style={styles.pageNumber} render={pageNumberRender} />
          <Text style={styles.footer}>{props.report.productName} - Summary</Text>
        </View>
      </View>
    </Page>
  </Document>
);
