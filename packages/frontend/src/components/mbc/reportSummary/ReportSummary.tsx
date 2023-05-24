// import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { getParams } from '../../../router/RouterUtils';

import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { USER_ROLE } from 'globals/constants';
import { ICreateNewReportResult, IRole, IUserInfo, ILogoDetails, ICreateNewReport } from 'globals/types';
import { history } from '../../../router/History';
import { ReportPdfDoc } from './pdfdoc/ReportPdfDoc';
import Styles from './ReportSummary.scss';
import DescriptionSummary from './description/DescriptionSummary';
import CustomerSummary from './customer/CustomerSummary';
import KpiSummary from './kpi/KpiSummary';
import DataFunctionSummary from './dataFunction/DataFunctionSummary';
import MembersSummary from './members/MembersSummary';

import { ReportsApiClient } from '../../../services/ReportsApiClient';

export interface IReportState {
  response?: ICreateNewReportResult;
  report: ICreateNewReport;
  publish: boolean;
  showDeleteReportModal: boolean;
  reportToBeDeleted: string;
  currentTab: string;
  tabClassNames: Map<string, string>;
  canShowCustomer: boolean;
  canShowKpi: boolean;
  canShowDataFunction: boolean;
  canShowMembers: boolean;
}
export interface ICreateNewReportData {
  description: IDescriptionReportRequest;
  customer: ICustomerDetails[];
  bookmarked: boolean;
}
export interface ICustomerDetails {
  hierarchy: string[];
  ressort: string[];
  department: string[];
}
export interface IDescriptionReportRequest {
  reportName: string;
  description: string;
  statusDetails: string;
  integratedInPortals: string;
  designGuids: string;
  frontEndTechs: string;
  logoDetails: ILogoDetails;
}
export default class ReportSummary extends React.Component<{ user: IUserInfo }, IReportState> {
  constructor(props: any) {
    super(props);
    this.state = {
      response: {},
      report: {
        productName: '',
        description: {
          productName: '',
          productDescription: '',
          division: {
            id: '',
            name: '',
            subdivision: {
              id: '',
              name: '',
            },
          },
          subdivision: '',
          department: [],
          productPhase: null,
          status: null,
          agileReleaseTrain: '',
          integratedPortal: '',
          designGuideImplemented: null,
          frontendTechnologies: [],
          tags: [],
          reportLink: '',
          reportType: '',
          piiData: ''
        },
        kpis: [],
        customer: {
          internalCustomers: [],
          externalCustomers: [],
        },
        dataAndFunctions: {
          dataWarehouseInUse: [],
          singleDataSources: [],
        },
        members: {
          // reportOwners: [],
          reportAdmins: [],
        },
        publish: false,
        openSegments: [],
        usingQuickPath: false,
        reportId: null,
      },
      publish: false,
      showDeleteReportModal: false,
      tabClassNames: new Map<string, string>(),
      currentTab: 'description',
      reportToBeDeleted: null,
      canShowCustomer: false,
      canShowKpi: false,
      canShowDataFunction: false,
      canShowMembers: false,
    };
  }

  public render() {
    const header = document.getElementById('header');
    window.addEventListener('scroll', () => {
      if (window.pageYOffset > 47) {
        header?.classList.remove('nav-header-bg-trans');
      } else {
        header?.classList.add('nav-header-bg-trans');
      }
    });
    const userInfo = this.props.user;
    const isSuperAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
    const isReportAdmin = userInfo.roles.find((role: IRole) => role.id === USER_ROLE.REPORTADMIN);
    // const isProductOwner = this.state.report.members.reportOwners?.find(
    //   (teamMember: ITeams) => teamMember.shortId === userInfo.id,
    // )?.shortId;
    const reportName = this.state.report.productName;
    const reportId = this.state.report.reportId;
    // const logoDetails = this.state.report.description.logoDetails;

    const deleteModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Report</div>
        <div className={Styles.modalContent}>This report will be deleted permanently.</div>
      </div>
    );
    const canShowDescription = this.state.report.productName !== '';

    const pdfContent = canShowDescription ? (
      <ReportPdfDoc
        report={this.state.report}
        canShowDataFunction={this.state.canShowDataFunction}
        canShowCustomer={this.state.canShowCustomer}
        canShowMember={this.state.canShowMembers}
        canShowKpi={this.state.canShowKpi}
        user={this.props.user}
      />
    ) : null;

    return (
      <div className={Styles.summaryWrapper}>
        {canShowDescription ? (
          <img className={Styles.summaryBannerimg} src="./images/reportLogoImages/banners/default.jpg" />
        ) : null}
        <div className={Styles.summaryContentWrapper}>
          <div className={Styles.backButtonWapper}>
            <button className="btn btn-text back arrow" type="submit" onClick={this.goback}>
              Back
            </button>
            <div className={Styles.summeryBannerTitle}>
              <h2>
                {reportName} ({reportId})
              </h2>
            </div>
          </div>
          <div id="report-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={this.state.currentTab === 'description' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="description" onClick={this.setCurrentTab}>
                      Report Summary
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="" className={'hidden'}>
                      `
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                {canShowDescription ? (
                  <React.Fragment>
                    <DescriptionSummary
                      productName={this.state.report.productName}
                      description={this.state.report.description}
                      canEdit={
                        isReportAdmin !== undefined ||
                        isSuperAdmin !== undefined ||
                        // isProductOwner !== undefined ||
                        userInfo.id === this.checkUserCanEditReport(userInfo)
                      }
                      reportId={this.state.response.data ? this.state.response.data.reportId : ''}
                      onEdit={()=>this.onEditReport(this.state.response.data.reportId)}
                      onDelete={()=>this.onDeleteReport(this.state.response.data.id)}
                      onExportToPDFDocument={pdfContent}
                      reportLink={this.state.response.data ? this.state.response.data.description.reportLink : ''}
                    />
                  </React.Fragment>
                ) : (
                  ''
                )}
                {this.state.canShowCustomer && (
                  <React.Fragment>
                    <CustomerSummary customers={this.state.report.customer} />
                  </React.Fragment>
                )}
                {this.state.canShowKpi && (
                  <React.Fragment>
                    <KpiSummary kpis={this.state.report.kpis} />
                  </React.Fragment>
                )}
                {this.state.canShowDataFunction && (
                  <React.Fragment>
                    <DataFunctionSummary dataAndFunctions={this.state.report.dataAndFunctions} />
                  </React.Fragment>
                )}
                {this.state.canShowMembers && (
                  <React.Fragment>
                    <MembersSummary members={this.state.report.members} />
                  </React.Fragment>
                )}
              </div>
              <ConfirmModal
                title="Delete Report"
                acceptButtonTitle="Delete"
                cancelButtonTitle="Cancel"
                showAcceptButton={true}
                showCancelButton={true}
                show={this.state.showDeleteReportModal}
                content={deleteModalContent}
                onCancel={this.onCancellingDeleteChanges}
                onAccept={this.onAcceptDeleteChanges}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
  public async componentWillReceiveProps() {
    this.setup();
  }
  public async componentDidMount() {
    this.setup();
    Tabs.defaultSetup();
    document.getElementById('header').classList.add('nav-header-bg-trans');
    document.getElementById('mainContainer').style.padding = '0px';
  }
  public async setup() {
    let { id } = getParams();
    if ((id == null || id === '') && this.state.response.data != null) {
      id = this.state.response.data.id;
    }
    await this.getReportById(id);
  }
  public async getReportById(id: string) {
    if (id) {
      ProgressIndicator.show();

      await ReportsApiClient.getReportById(id)
        .then((res) => {
          if (res) {
            const response = this.state.response;
            response.data = res;
            const report = this.state.report;
            report.productName = res.productName;
            report.description = res.description;
            report.customer = res.customer;
            report.kpis = res.kpis || [];
            report.dataAndFunctions.dataWarehouseInUse = res.dataAndFunctions.dataWarehouseInUse || [];
            report.dataAndFunctions.singleDataSources = res.dataAndFunctions.singleDataSources || [];
            // report.members.reportOwners = res.members.reportOwners || [];
            report.members.reportAdmins = res.members.reportAdmins || [];
            report.publish = res.publish;
            report.openSegments = res.openSegments || [];
            report.reportId = res.reportId;
            this.setState(
              {
                response,
                report,
                canShowCustomer: res.customer.internalCustomers?.length > 0 || res.customer.externalCustomers?.length > 0,
                canShowKpi: res.kpis?.length > 0,
                canShowDataFunction:
                  res.dataAndFunctions.dataWarehouseInUse?.length > 0 ||
                  res.dataAndFunctions.singleDataSources?.length > 0,
                canShowMembers:
                  // res.members.reportOwners?.length > 0 ||
                  // res.members.developers?.length > 0 ||
                  res.members.reportAdmins?.length > 0,
              },
              () => {
                ProgressIndicator.hide();
                this.ResetTabs();
              },
            );
          }
        })
        .catch((error) => {
          this.setState(
            {
              response: {},
            },
            () => {
              const msg = error.message
                ? error.message.startsWith('No value')
                  ? 'No report found.'
                  : error.message
                : 'Some Error Occured';
              this.showErrorNotification(msg);
            },
          );
        });
    }
  }
  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected onEditReport = (reportId: string) => {
    history.push('/editreport/' + reportId + '/true');
  };

  protected checkUserCanEditReport(userInfo: IUserInfo) {
    let userId = '';
    if (this.state.report.members.reportAdmins.find((teamMember) => teamMember.shortId === userInfo.id)) {
      userId = this.state.report.members.reportAdmins.find((teamMember) => teamMember.shortId === userInfo.id).shortId;
    } else if (
      userInfo?.divisionAdmins &&
      userInfo?.divisionAdmins.includes(this.state.report?.description?.division?.name)
    ) {
      userId = userInfo.id;
    }
    // else if (this.state.report.createdBy) {
    //   userId = this.state.report.createdBy.id;
    // }
    else {
      userId = '';
    }
    return userId;
  }

  protected onDeleteReport = (reportId: string) => {
    this.setState({ showDeleteReportModal: true, reportToBeDeleted: reportId });
  };

  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteReportModal: false, reportToBeDeleted: null });
  };

  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    ReportsApiClient.deleteReportById(this.state.reportToBeDeleted)
      .then((res) => {
        if (res) {
          Notification.show('Report deleted successfully');
          ProgressIndicator.hide();
          this.setState({ showDeleteReportModal: false }, () => {
            history.goBack();
          });
        }
      })
      .catch((error) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        this.setState({ showDeleteReportModal: false });
      });
  };

  protected exportToPDF = () => {};

  protected setCurrentTab = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    this.setState({ currentTab: target.id });
  };
  protected goback = () => {
    history.goBack();
  };

  protected ResetTabs = () => {
    const tabActiveIndicator = document.querySelector('.active-indicator') as HTMLSpanElement;
    const firstTabContent = document.querySelector('.tab-content');
    const activeTabContent = document.querySelector('.tab-content.active');

    if (tabActiveIndicator && firstTabContent && activeTabContent) {
      tabActiveIndicator.style.left = '0px';
      if (!firstTabContent.classList.contains('active')) {
        firstTabContent.classList.add('active');
        activeTabContent.classList.remove('active');
      }
    }
  };
}
