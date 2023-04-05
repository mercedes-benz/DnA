import cn from 'classnames';
import * as React from 'react';
import { getParams } from '../../../router/RouterUtils';
import { history } from '../../../router/History';
// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import InputFields from '../../../assets/modules/uilab/js/src/input-fields';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';
import { ApiClient } from '../../../services/ApiClient';
import ConfirmModal from '../../formElements/modal/confirmModal/ConfirmModal';
import { trackEvent } from '../../../services/utils';
import { parseMessage } from '../../../utils/ParseMissingField';

// @ts-ignore
import * as _ from 'lodash';
import {
  ICreateNewReportRequest,
  ICreateNewReportResult,
  ITag,
  ITeams,
  IProductPhase,
  IProductStatus,
  IDesignGuide,
  IFrontEndTech,
  IDepartment,
  IHierarchies,
  IIntegratedPortal,
  IKpiNames,
  IReportingCauses,
  IRessort,
  IDescriptionRequest,
  ICustomers,
  ICreateNewReport,
  IKpis,
  IDataAndFunctions,
  IUserInfo,
  IRole,
  IART,
  IDataSourceMaster,
  IConnectionType,
  IDataWarehouse,
  // ISingleDataSources,
  IDivision,
  ISubDivision,
  IDataClassification,
} from 'globals/types';
import Styles from './CreateNewReport.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

import Customer from './customer/Customer';
import Description from './description/Description';
import Kpi from './kpi/Kpi';
import DataFunction from './dataFunction/DataFunction';
import Members from './members/Members';
import { ReportsApiClient } from '../../../services/ReportsApiClient';
import { serializeReportRequestBody } from './utility/Utility';
import { USER_ROLE } from 'globals/constants';
import { TeamMemberType } from 'globals/Enums';
import Caption from '../shared/caption/Caption';

const classNames = cn.bind(Styles);
export interface ICreateNewReportState {
  divisions: IDivision[];
  subDivisions: ISubDivision[];
  dataSources: IDataSourceMaster[];
  departments: IDepartment[];
  frontEndTechnologies: IFrontEndTech[];
  hierarchies: IHierarchies[];
  arts: IART[];
  integratedPortals: IIntegratedPortal[];
  kpiNames: IKpiNames[];
  productPhases: IProductPhase[];
  reportingCauses: IReportingCauses[];
  ressort: IRessort[];
  statuses: IProductStatus[];
  designGuideImplemented: IDesignGuide[];
  connectionTypes: IConnectionType[];
  dataClassifications: IDataClassification[];
  dataWarehouses: IDataWarehouse[];
  // commonFunctions: ICommonFunctions[];
  editMode: boolean;
  currentTab: string;
  nextTab: string;
  clickedTab: string;
  report: ICreateNewReport;
  response?: ICreateNewReportResult;
  request?: ICreateNewReportRequest;
  saveActionType: string;
  tabClassNames: Map<string, string>;
  currentState: ICreateNewReport;
  showAlertChangesModal: boolean;
  publishFlag: boolean;
  tags: ITag[];
  departmentTags: IDepartment[];
  fieldsMissing: boolean;
}

export interface ICreateNewReportProps {
  user: IUserInfo;
  location?: any;
}
export default class CreateNewReport extends React.Component<ICreateNewReportProps, ICreateNewReportState> {
  private customerComponent = React.createRef<Customer>();
  private kpiComponent = React.createRef<Kpi>();
  private dataFunctionComponent = React.createRef<DataFunction>();
  private membersComponent = React.createRef<Members>();
  constructor(props: ICreateNewReportProps) {
    super(props);
    this.customerComponent = React.createRef<Customer>();
    this.kpiComponent = React.createRef<Kpi>();
    this.dataFunctionComponent = React.createRef<DataFunction>();
    this.membersComponent = React.createRef<Members>();
    this.state = {
      divisions: [],
      subDivisions: [],
      dataSources: [],
      departments: [],
      frontEndTechnologies: [],
      hierarchies: [],
      arts: [],
      integratedPortals: [],
      kpiNames: [],
      productPhases: [],
      reportingCauses: [],
      ressort: [],
      statuses: [],
      designGuideImplemented: [],
      tags: [],
      connectionTypes: [],
      dataClassifications: [
        { id: 'Confidential', name: 'Confidential' },
        { id: 'Internal', name: 'Internal' },
        { id: 'Public', name: 'Public' },
      ],
      dataWarehouses: [],
      // commonFunctions: [],
      departmentTags: [],
      editMode: false,
      currentTab: 'description',
      nextTab: 'customer',
      clickedTab: '',
      response: {},
      saveActionType: '',
      tabClassNames: new Map<string, string>(),
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
          reportType: null,
          piiData: '',
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
          reportOwners: [],
          reportAdmins: [],
        },
        publish: false,
        openSegments: [],
        usingQuickPath: true,
        reportId: null,
      },
      currentState: null,
      showAlertChangesModal: false,
      publishFlag: false,
      fieldsMissing: false,
    };
  }
  // public componentWillReceiveProps(nextProps: any) {
  //   if (nextProps.location.pathname.indexOf('createnewreport')) {
  //     window.location.reload();
  //   }
  // }
  public componentDidMount() {
    // Tabs.defaultSetup();
    InputFields.defaultSetup();
    ProgressIndicator.show();

    ReportsApiClient.getCreateNewReportData().then((response) => {
      if (response) {
        const departments = response[0].data;
        const frontEndTechnologies = response[1].data;
        const hierarchies = response[2].data;
        const integratedPortals = response[3].data;
        const kpiNames = response[4].data;
        const reportingCauses = response[5].data;
        const ressort = response[6].data;
        const statuses = response[7].data;
        const arts = response[8].data;
        const tags: ITag[] = response[9].data;
        const connectionTypes: IConnectionType[] = response[10].data;
        const dataWarehouses: IDataWarehouse[] = response[11].data;
        const divisions: IDivision[] = response[12];
        const departmentTags: IDepartment[] = response[13].data;
        const dataClassifications: IDataClassification[] = response[14].data;
        const creatorInfo = this.props.user;
        const teamMemberObj: ITeams = {
          department: creatorInfo.department,
          email: creatorInfo.eMail,
          firstName: creatorInfo.firstName,
          shortId: creatorInfo.id,
          lastName: creatorInfo.lastName,
          userType: TeamMemberType.INTERNAL,
          teamMemberPosition: '',
        };

        if (creatorInfo.mobileNumber !== '') {
          teamMemberObj.mobileNumber = creatorInfo.mobileNumber;
        }
        this.setState(
          (prevState) => ({
            // dataSources,
            departments,
            frontEndTechnologies,
            hierarchies,
            integratedPortals,
            kpiNames,
            reportingCauses,
            ressort,
            statuses,
            arts,
            tags,
            departmentTags,
            dataWarehouses,
            divisions,
            connectionTypes,
            dataClassifications,
            // commonFunctions,
            report: {
              ...prevState.report,
              members: {
                ...prevState.report.members,
                reportAdmins: [teamMemberObj],
              },
            },
          }),
          () => {
            ApiClient.getMasterDataSources().then((response) => {
              if (response) {
                const dataSourcesTags: ITag[] = response;
                this.setState(
                  {
                    dataSources: dataSourcesTags,
                  },
                  () => {
                    Button.defaultSetup();
                    SelectBox.defaultSetup();
                    ProgressIndicator.hide();
                    this.getReportById(() => {});
                  },
                );
              }
            });
          },
        );
      } else {
        ProgressIndicator.hide();
      }
    });

    // ApiClient.getCreateNewSolutionData().then((response) => {
    //   if (response) {
    //     const dataSources: ITag[] = response[9];
    //     this.setState({
    //       dataSources
    //     });
    //   }
    // });
  }

  protected setupEditReportData(
    subDivisions: ISubDivision[],
    response: ICreateNewReportResult,
    report: ICreateNewReport,
    resetChildComponents: () => void | null,
  ) {
    this.setState(
      {
        subDivisions,
        response,
        report,
      },
      () => {
        this.setOpenTabs(report.openSegments);
        SelectBox.defaultSetup();
        Tabs.defaultSetup();
        ProgressIndicator.hide();
        resetChildComponents();
        this.setState({
          currentState: JSON.parse(JSON.stringify(report)),
        });
      },
    );
  }

  public async getReportById(resetChildComponents?: () => void | null) {
    let { id } = getParams();
    if ((id == null || id === '') && this.state.response.data != null) {
      id = this.state.response.data.id;
    }
    if (id) {
      this.setState({ editMode: true });
      ProgressIndicator.show();

      ReportsApiClient.getReportById(id)
        .then((res) => {
          if (res) {
            const user = this.props.user;
            const isSuperAdmin = user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN);
            const isReportAdmin = user.roles.find((role: IRole) => role.id === USER_ROLE.REPORTADMIN);
            const isProductOwner = res.members.reportOwners?.find(
              (teamMember: ITeams) => teamMember.shortId === user.id,
            )?.shortId;

            if (
              isSuperAdmin !== undefined ||
              isReportAdmin !== undefined ||
              isProductOwner !== undefined ||
              // user.id === (res.createdBy ? res.createdBy.id : '')
              res.members.reportAdmins.find((teamMember) => teamMember.shortId === user.id) !== undefined ||
              (user?.divisionAdmins && user?.divisionAdmins.includes(res?.description?.division?.name))
            ) {
              const response = this.state.response;
              const {
                productPhases,
                statuses,
                // frontEndTechnologies,
                designGuideImplemented,
                // integratedPortals,
                // arts,
                // dataSources,
                // connectionTypes,
                // dataClassifications,
              } = this.state;
              response.data = res;
              const report = this.state.report;
              report.description.productName = res.productName;
              report.description.productDescription = res.description.productDescription;
              report.description.productPhase = productPhases?.filter(
                (item: any) => item.name === res.description.productPhase,
              );
              report.description.status = statuses?.filter((item: any) => item.name === res.description.status);
              report.description.frontendTechnologies = res.description.frontendTechnologies;
              report.description.designGuideImplemented = designGuideImplemented?.filter(
                (item: any) => item.name === res.description.designGuideImplemented,
              );
              report.description.agileReleaseTrain = res.description.agileReleaseTrain;
              report.description.integratedPortal = res.description.integratedPortal;
              report.description.tags = res.description.tags;
              report.description.division = res.description.division;
              report.description.department = (res.description.department as any)?.split(' ') || null;
              report.description.reportLink = res.description.reportLink;
              report.description.reportType = res.description?.reportType;
              report.description.piiData = res.description?.piiData;
              report.customer.internalCustomers = res.customer?.internalCustomers || [];
              report.customer.externalCustomers = res.customer?.externalCustomers || [];
              // report.customer.processOwners = res.customer?.processOwners || [];
              report.kpis = res.kpis || [];
              report.dataAndFunctions.dataWarehouseInUse = res.dataAndFunctions?.dataWarehouseInUse || [];
              report.dataAndFunctions.singleDataSources = res.dataAndFunctions?.singleDataSources || [];
              // res.dataAndFunctions?.singleDataSources?.map((item: ISingleDataSources) => {
              //   item.dataSources =
              //     dataSources?.filter((subItem: any) => item.dataSources.indexOf(subItem.name) > -1) || [];
              //   item.connectionTypes =
              //     connectionTypes?.filter((subItem: any) => item.connectionTypes.indexOf(subItem.name) > -1) || [];
              //   item.dataClassification
              //   return item;
              // }) || [];
              // report.members.developers = res.members.developers || [];
              report.members.reportOwners = res.members.reportOwners || [];
              report.members.reportAdmins = res.members.reportAdmins || [];
              report.publish = res.publish;
              report.openSegments = res.openSegments || [];
              report.reportId = res.reportId;
              let subDivisions: ISubDivision[] = [{ id: '0', name: 'None' }];
              const divisionId = res.description.division?.id;
              if (divisionId) {
                ApiClient.getSubDivisions(res.description.division.id)
                  .then((subDivResponse) => {
                    subDivisions = subDivResponse;
                  })
                  .finally(() => {
                    this.setupEditReportData(subDivisions, response, report, resetChildComponents);
                  });
              } else {
                this.setupEditReportData(subDivisions, response, report, resetChildComponents);
              }
            } else {
              ProgressIndicator.hide();
              history.replace('/unauthorised');
            }
          }
        })
        .catch((error) => {
          this.setState(
            {
              response: {},
            },
            () => {
              this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            },
          );
        });
    }
  }

  public changeQuickPath = () => {
    const report = { ...this.state.report };
    // report.usingQuickPath = !value;
    report.usingQuickPath = false;

    this.setState({ report }, () => {
      Tabs.defaultSetup();
      if (!this.state.report.usingQuickPath) {
        document.getElementById('description').click();
      }
    });
  };

  public render() {
    const currentTab = this.state.currentTab;
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel)}>
          <div className={Styles.flexLayout}>
            <div>
              <Caption title={this.state.report.reportId ? 'Edit Report' : 'Create Report'} />
            </div>
            {!this.state.report.reportId && currentTab === 'description' ? (
              <div className={Styles.switchButton}>
                {/* <label className="switch">
                  <span className="label" style={{ marginRight: '5px' }}>
                    {this.state.report.usingQuickPath ? 'Disable Quick View' : 'Enable Quick View'}
                  </span>
                  <span className="wrapper">
                    <input
                      type="checkbox"
                      className="ff-only"
                      onChange={() => this.changeQuickPath(this.state.report.usingQuickPath)}
                      checked={this.state.report.usingQuickPath}
                    />
                  </span>
                </label> */}
              </div>
            ) : (
              ''
            )}
          </div>
          <h3 className={classNames(Styles.title, this.state.currentTab !== 'description' ? '' : 'hidden')}>
            {this.state.report.description.productName}{' '}
            {this.state.report.reportId ? '(' + this.state.report.reportId + ')' : ''}
          </h3>
          {this.state.report.usingQuickPath && !this.state.report.reportId ? (
            <Description
              divisions={this.state.divisions}
              subDivisions={this.state.subDivisions}
              productPhases={this.state.productPhases}
              statuses={this.state.statuses}
              arts={this.state.arts}
              integratedPortals={this.state.integratedPortals}
              designGuideImplemented={this.state.designGuideImplemented}
              frontEndTechnologies={this.state.frontEndTechnologies}
              description={this.state.report.description}
              modifyReportDescription={this.modifyReportDescription}
              onSaveDraft={this.onSaveDraft}
              tags={this.state.tags}
              departmentTags={this.state.departmentTags}
              setSubDivisions={(subDivisions: ISubDivision[]) =>
                this.setState({ subDivisions }, () => SelectBox.defaultSetup())
              }
              enableQuickPath={this.state.report.usingQuickPath}
              refineReport={this.changeQuickPath}
            />
          ) : (
            <div id="create-report-tabs" className="tabs-panel">
              <div className="tabs-wrapper">
                <nav>
                  <ul className="tabs">
                    <li
                      className={
                        this.state.tabClassNames.has('Description')
                          ? this.state.tabClassNames.get('Description')
                          : 'tab active'
                      }
                    >
                      <a href="#tab-content-1" id="description" onClick={this.setCurrentTab}>
                        Description
                      </a>
                    </li>
                    <li
                      className={
                        this.state.tabClassNames.has('Customer')
                          ? this.state.tabClassNames.get('Customer')
                          : 'tab disabled'
                      }
                    >
                      <a href="#tab-content-2" id="customer" onClick={this.setCurrentTab}>
                        Customer
                      </a>
                    </li>
                    <li
                      className={
                        this.state.tabClassNames.has('Kpis') ? this.state.tabClassNames.get('Kpis') : 'tab disabled'
                      }
                    >
                      <a href="#tab-content-3" id="kpi" onClick={this.setCurrentTab}>
                        Content &amp; Functions
                      </a>
                    </li>
                    <li
                      className={
                        this.state.tabClassNames.has('DataAndFunctions')
                          ? this.state.tabClassNames.get('DataAndFunctions')
                          : 'tab disabled'
                      }
                    >
                      <a href="#tab-content-4" id="datafunction" onClick={this.setCurrentTab}>
                        Data
                      </a>
                    </li>
                    <li
                      className={
                        this.state.tabClassNames.has('Members')
                          ? this.state.tabClassNames.get('Members')
                          : 'tab disabled'
                      }
                    >
                      <a href="#tab-content-5" id="members" onClick={this.setCurrentTab}>
                        Members
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
              <div className="tabs-content-wrapper">
                <div id="tab-content-1" className="tab-content">
                  {currentTab === 'description' && (
                    <Description
                      divisions={this.state.divisions}
                      subDivisions={this.state.subDivisions}
                      productPhases={this.state.productPhases}
                      statuses={this.state.statuses}
                      arts={this.state.arts}
                      integratedPortals={this.state.integratedPortals}
                      designGuideImplemented={this.state.designGuideImplemented}
                      frontEndTechnologies={this.state.frontEndTechnologies}
                      description={this.state.report.description}
                      modifyReportDescription={this.modifyReportDescription}
                      onSaveDraft={this.onSaveDraft}
                      tags={this.state.tags}
                      departmentTags={this.state.departmentTags}
                      setSubDivisions={(subDivisions: ISubDivision[]) =>
                        this.setState({ subDivisions }, () => SelectBox.defaultSetup())
                      }
                      enableQuickPath={false}
                    />
                  )}
                </div>
                <div id="tab-content-2" className="tab-content">
                  {currentTab === 'customer' && (
                    <Customer
                      customer={this.state.report.customer}
                      hierarchies={this.state.hierarchies}
                      departments={this.state.departments}
                      ressort={this.state.ressort}
                      divisions={this.state.divisions}
                      modifyCustomer={this.modifyCustomer}
                      onSaveDraft={this.onSaveDraft}
                      ref={this.customerComponent}
                    />
                  )}
                </div>
                <div id="tab-content-3" className="tab-content">
                  {currentTab === 'kpi' && (
                    <Kpi
                      kpis={this.state.report.kpis}
                      kpiNames={this.state.kpiNames}
                      reportingCause={this.state.reportingCauses}
                      modifyKpi={this.modifyKpi}
                      onSaveDraft={this.onSaveDraft}
                      ref={this.kpiComponent}
                    />
                  )}
                </div>
                <div id="tab-content-4" className="tab-content">
                  {currentTab === 'datafunction' && (
                    <DataFunction
                      dataAndFunctions={this.state.report.dataAndFunctions}
                      dataSources={this.state.dataSources}
                      connectionTypes={this.state.connectionTypes}
                      dataClassifications={this.state.dataClassifications}
                      dataWarehouses={this.state.dataWarehouses}
                      // commonFunctions={this.state.commonFunctions}
                      modifyDataFunction={this.modifyDataFunction}
                      onSaveDraft={this.onSaveDraft}
                      ref={this.dataFunctionComponent}
                    />
                  )}
                </div>
                <div id="tab-content-5" className="tab-content">
                  {currentTab === 'members' && (
                    <Members
                      members={this.state.report.members}
                      modifyMember={this.modifyMember}
                      onSaveDraft={this.onSaveDraft}
                      onPublish={this.onPublish}
                      ref={this.membersComponent}
                    />
                  )}
                </div>
              </div>
            </div>
          )}

          <ConfirmModal
            title="Save Changes?"
            acceptButtonTitle="Close"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.showAlertChangesModal}
            content={
              <div id="contentparentdiv">
                Press &#187;Close&#171; to save your changes or press
                <br />
                &#187;Cancel&#171; to discard changes.
              </div>
            }
            onCancel={this.onCancellingUpdateChanges}
            onAccept={this.onAcceptUpdateChanges}
          />
        </div>
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </React.Fragment>
    );
  }

  public setOpenTabs = (openSegments: string[]) => {
    if (!this.state.report.publish) {
      if (openSegments != null && openSegments.length > 0) {
        const tabClasses = new Map<string, string>();
        openSegments.forEach((openSegment) => {
          tabClasses.set(openSegment, 'tab valid');
        });
        this.setState({ tabClassNames: tabClasses });
      }
    } else {
      const tabClasses = new Map<string, string>();
      tabClasses.set('Description', 'tab valid');
      tabClasses.set('Customer', 'tab valid');
      tabClasses.set('Kpis', 'tab valid');
      tabClasses.set('DataAndFunctions', 'tab valid');
      tabClasses.set('Members', 'tab valid');
      this.setState({ tabClassNames: tabClasses });
    }
  };
  public onAcceptUpdateChanges = () => {
    document.getElementById(this.state.currentTab).click();
    this.setState({
      showAlertChangesModal: false,
    });
  };

  public onCancellingUpdateChanges = () => {
    this.getReportById(() => {
      this.setState(
        {
          showAlertChangesModal: false,
        },
        () => {
          const currentTab = this.state.currentTab;
          currentTab === 'customer' && this.customerComponent.current.resetChanges();
          currentTab === 'kpi' && this.kpiComponent.current.resetChanges();
          currentTab === 'datafunction' && this.dataFunctionComponent.current.resetChanges();
          currentTab === 'members' && this.membersComponent.current.resetChanges();
          document.getElementById(this.state.clickedTab).click();
        },
      );
    });
  };

  protected onSaveDraft = (tabToBeSaved: string) => {
    const currentTab = tabToBeSaved;
    this.setState({ saveActionType: 'btn' });
    if (currentTab === 'description') {
      this.saveDescription();
    } else if (currentTab === 'customer') {
      this.saveCustomer();
    } else if (currentTab === 'kpi') {
      this.saveKpi();
    } else if (currentTab === 'datafunction') {
      this.saveDataFunction();
    } else if (currentTab === 'members') {
      this.saveMembers();
    } else if (currentTab === 'quickpath') {
      this.saveDescriptionWithQuickPath();
    } else {
      // If multiple clicks on save happens then the currenttab doesnt get updated in that case
      // just save not moving to another tab.
      this.callApiToSave(false, null);
    }
  };

  protected onPublish = () => {
    this.state.report.openSegments.push('Members');
    this.setState({ publishFlag: true }, () => {
      this.callApiToSave(true, null);
    });
  };

  protected setTabsAndClick = (nextTab: string) => {
    const nextTabToClick = nextTab;
    this.setState({
      currentTab: nextTab,
      nextTab: nextTabToClick,
    });
    if (nextTabToClick) {
      document.getElementById(nextTabToClick).click();
    }
  };

  protected setCurrentTab = (event: React.MouseEvent) => {
    const target = event.target as HTMLLinkElement;
    const newState = this.state.report;
    const saveActionType = this.state.saveActionType;
    const currentState = this.state.currentState;

    if (!currentState || saveActionType === 'btn' || _.isEqual(newState, currentState) || this.state.fieldsMissing) {
      this.setState({ currentTab: target.id, saveActionType: '' });
    } else {
      if (!this.state.showAlertChangesModal) {
        this.setState({ showAlertChangesModal: true, clickedTab: target.id });
      }
    }
  };
  protected saveDescriptionWithQuickPath = () => {
    // this.state.report.openSegments.push('Description');
    this.setState({ publishFlag: true });
    this.callApiToSave(true, null);
  };
  protected saveDescription = () => {
    this.state.report.openSegments.push('Description');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.report.publish, 'customer');
  };
  protected saveCustomer = () => {
    this.state.report.openSegments.push('Customer');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.report.publish, 'kpi');
  };
  protected saveKpi = () => {
    this.state.report.openSegments.push('Kpis');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.report.publish, 'datafunction');
  };
  protected saveDataFunction = () => {
    this.state.report.openSegments.push('DataAndFunctions');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.report.publish, 'members');
  };
  protected saveMembers = () => {
    this.state.report.openSegments.push('Members');
    this.setState({ publishFlag: false });
    this.callApiToSave(this.state.report.publish, null);
  };
  protected callApiToSave = (isPublished: boolean, nextTab: string) => {
    const report = this.state.report;
    const distinct = (value: any, index: any, self: any) => {
      return self.indexOf(value) === index;
    };

    this.state.report.openSegments = report.openSegments.filter(distinct);
    if (report.description.division.subdivision.id === '0') {
      report.description.division.subdivision.id = null;
      report.description.division.subdivision.name = null;
    }
    const data: ICreateNewReportRequest = {
      data: {
        productName: report.description.productName,
        description: _.omit(report.description, ['productName', 'subdivision']),
        customer: report.customer,
        kpis: report.kpis,
        dataAndFunctions: report.dataAndFunctions,
        members: report.members,
        publish: isPublished,
        openSegments: report.openSegments,
        usingQuickPath: report.usingQuickPath,
        reportId: report.reportId,
      },
    };
    // create deep copy of an object (won't alter original object)
    // serialize an object to match request body
    const requestBody = JSON.parse(JSON.stringify(data));
    ProgressIndicator.show();

    if (this.state.response && this.state.response.data && this.state.response.data.id) {
      requestBody.data.id = this.state.response.data.id;
      serializeReportRequestBody(requestBody);
      ReportsApiClient.updateReport(requestBody)
        .then((response) => {
          if (response) {
            this.trackReportEvent(`Update Report ${isPublished ? 'Publish' : 'Save as Draft'} action on tab panel`);
            this.setState(
              {
                response,
              },
              () => {
                if (isPublished) {
                  this.state.report.publish = true;
                }
                if (this.state.report.description.division.subdivision.id === null) {
                  this.state.report.description.division.subdivision.id = '0';
                  this.state.report.description.division.subdivision.name = 'Choose';
                }
                this.setState({
                  // currentStateHash: btoa(unescape(encodeURIComponent(JSON.stringify(this.state.report)))),
                  // currentStateHash: JSON.stringify(this.state.report),
                  currentState: JSON.parse(JSON.stringify(this.state.report)),
                });
                this.setOpenTabs(report.openSegments);
                nextTab && this.setTabsAndClick(nextTab);
                this.showNotification(isPublished);
              },
            );
          }
        })
        .catch((error) => {
          const fieldsMissing = /(data.description|data.customer|data.kpis|data.dataAndFunctions)/gi.test(
            error.message,
          );
          if (fieldsMissing) {
            const tempArr = error.message.split('data.');
            tempArr.splice(0, 1);
            tempArr.forEach((element: string) => {
              this.showErrorNotification(parseMessage(element));
            });

            this.setState({
              fieldsMissing,
            });
          } else {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          }
        });
    } else {
      serializeReportRequestBody(requestBody);
      ReportsApiClient.createNewReport(requestBody)
        .then((response) => {
          if (response) {
            this.trackReportEvent('New Report Save as ' + (isPublished ? 'Publish' : 'Draft') +' action on tab panel');
            this.setState(
              {
                response,
              },
              () => {
                this.showNotification(isPublished);
                if (report.usingQuickPath) {
                  history.push('/allreports');
                } else {
                  this.setState({
                    // currentStateHash: btoa(unescape(encodeURIComponent(JSON.stringify(this.state.report)))),
                    // currentStateHash: JSON.stringify(this.state.report),
                    currentState: JSON.parse(JSON.stringify(this.state.report)),
                  });
                  this.setOpenTabs(report.openSegments);
                  this.setTabsAndClick(nextTab);
                }
              },
            );
          }
        })
        .catch((error) => {
          this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
        });
    }
    this.setState({
      request: data,
    });
  };

  protected trackReportEvent(message: string) {
    const from = document.getElementById(this.state.currentTab)?.innerText;
    trackEvent(`${this.state.editMode ? 'Edit' : 'Create'} Report`, message, from ? from : '');
  }

  protected showNotification(isPublished: boolean) {
    ProgressIndicator.hide();
    Notification.show((isPublished ? 'Report saved and published' : 'Draft saved') + ' successfully.');
  }

  protected showErrorNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  }

  protected modifyReportDescription = (description: IDescriptionRequest) => {
    const currentReportObject = this.state.report;
    currentReportObject.description = description;
    this.setState({
      report: currentReportObject,
    });
  };

  protected modifyCustomer = (customer: ICustomers) => {
    const currentReportObject = this.state.report;
    currentReportObject.customer = customer;
    this.setState({
      report: currentReportObject,
    });
  };
  protected modifyKpi = (kpi: IKpis[]) => {
    const currentReportObject = this.state.report;
    currentReportObject.kpis = kpi;
    this.setState({
      report: currentReportObject,
    });
  };

  protected modifyDataFunction = (dataFunction: IDataAndFunctions) => {
    const currentReportObject = this.state.report;
    currentReportObject.dataAndFunctions = dataFunction;
    this.setState({
      report: currentReportObject,
    });
  };
  protected modifyMember = (productOwners: ITeams[], reportAdmins: ITeams[]) => {
    const currentReportObject = this.state.report;
    currentReportObject.members.reportOwners = productOwners;
    currentReportObject.members.reportAdmins = reportAdmins;
    this.setState({
      report: currentReportObject,
    });
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
