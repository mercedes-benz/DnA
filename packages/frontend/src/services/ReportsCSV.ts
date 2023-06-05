import { Data } from 'react-csv/components/CommonPropTypes';
import { IAllReportsResultCSV, IReportFilterParams } from '../globals/types';
import { ReportsApiClient } from './ReportsApiClient';
import { getDivisionsQueryValue, regionalDateAndTimeConversionSolution } from './utils';

export const getDataForCSV = (
  queryParams: IReportFilterParams,
  numberOfSelectedArts: number,
  numberOfSelectedDepartments: number,
  numberOfSelectedProcessOwners: number,
  numberOfSelectedProductOwners: number,
  sortByField: string,
  sortType: string,
  onDataSuccess: (csvData: Data, csvHeader: Data) => void,
) => {
  const reportsCSVData: string | Data = [];

  const csvHeaders: string | Data = [
    { label: 'Report ID', key: 'reportId' },
    { label: 'Name', key: 'name' },
    { label: 'Report Type', key: 'reportType' },
    { label: 'Description', key: 'description' },
    { label: 'Report Link', key: 'reportLink' },
    { label: 'Tags', key: 'tags' },
    { label: 'Division', key: 'division' },
    { label: 'Subdivision', key: 'subdivision' },
    { label: 'E2-Department', key: 'department' },
    { label: 'Status', key: 'status' },
    { label: 'Integrated In Portal', key: 'integratedPortal' },
    { label: 'Agile Release Train', key: 'agileReleaseTrains' },
    { label: 'Frontend Technologies', key: 'frontendTechnologies' },
    { label: 'Internal Customers', key: 'internalCustomers' },
    { label: 'External Customers', key: 'externalCustomers' },
    { label: 'KPIs', key: 'kpis' },
    { label: 'Data Warehouse', key: 'datawarehouses' },
    { label: 'Single Datasource', key: 'singledatasources' },
    { label: 'Report Admin', key: 'reportAdmins' },
    { label: 'IsPublished', key: 'publish' },
    { label: 'CreatedDate', key: 'createdDate' },
    { label: 'LastModifiedDate', key: 'lastModifiedDate' },
    { label: 'Procedure ID', key: 'procedureId' },
  ];

  let agileReleaseTrains = queryParams.agileReleaseTrains?.join(',');
  let processOwners = queryParams.processOwners?.join(',');
  let productOwners = queryParams.productOwners?.join(',');
  let departments = queryParams.departments?.join(',');
  const divisionIds = getDivisionsQueryValue(queryParams.division, queryParams.subDivision);

  if (queryParams.agileReleaseTrains.length === numberOfSelectedArts) {
    agileReleaseTrains = '';
  }
  if (queryParams.departments.length === numberOfSelectedDepartments) {
    departments = '';
  }
  if (queryParams.processOwners.length === numberOfSelectedProcessOwners) {
    processOwners = '';
  }

  if (queryParams.productOwners.length === numberOfSelectedProductOwners) {
    productOwners = '';
  }
  ReportsApiClient.exportDatatoCSV(
    divisionIds,
    agileReleaseTrains,
    departments,
    processOwners,
    productOwners,
    sortByField,
    sortType,
  ).then((resCSV) => {
    if (resCSV) {
      const reportsCSV = resCSV?.data?.reports as IAllReportsResultCSV;
      if (reportsCSV?.records) {
        reportsCSV?.records?.forEach((report) => {
          reportsCSVData.push({
            reportId: report.reportId ? sanitize(report.reportId) : 'NA', 
            name: report.productName ? sanitize(report.productName) : 'NA',
            reportType: report.description.reportType && report.description?.reportType != '0' ? sanitize(report.description.reportType) : 'NA',
            description: report.description.productDescription ? sanitize(report.description.productDescription) : 'NA',
            reportLink: report.description.reportLink ? sanitize(report.description.reportLink) : 'NA',
            tags:
              report.description.tags && report.description.tags.length > 0
                ? sanitize(report.description.tags.join(', '))
                : 'NA',
            division: report.description.division?.name ? report.description.division.name : 'NA',
            subdivision: report.description.division?.subdivision ? report.description.division.subdivision.name : 'NA',
            department: report.description.department ? report.description.department : 'NA',
            status: report.description.status ? report.description.status : 'NA',
            integratedPortal: report.description.integratedPortal && report.description?.integratedPortal != '0'
              ? report.description.integratedPortal
              : 'NA',
            agileReleaseTrains: report.description.agileReleaseTrain && report?.description.agileReleaseTrain != '0'
              ? report.description.agileReleaseTrain
              : 'NA',
            frontendTechnologies: report.description.frontendTechnologies?.length
              ? report.description.frontendTechnologies?.join(', ')
              : 'NA',
            internalCustomers: (report.customer?.internalCustomers?.length
              ? report.customer?.internalCustomers?.map((customer) => 
              'customerRelation: ' + customer?.customerRelation
              + '|' + 'level: ' + customer?.level
              + '|' + 'customerDivision: ' + customer?.division?.name
              + '|' + 'e2-department: ' + customer?.department
              + '|' + 'mbLegalEntity: ' + customer?.legalEntity
              + '|' + 'usRisk: ' + customer?.accessToSensibleData
              + '|' + 'comment: ' + customer?.comment
              + '|' + 'processOwner: ' + JSON.parse(JSON.stringify(customer?.processOwner))?.firstName +' '+ JSON.parse(JSON.stringify(customer?.processOwner))?.lastName)
              : 'NA'),
            externalCustomers: report.customer?.externalCustomers?.length
              ? report.customer.externalCustomers?.map((customer) =>               
              'customerRelation: ' + customer?.customerRelation
              + '|' + 'companyName: ' + customer?.companyName
              + '|' + 'comment: ' + customer?.comment
              )
              : 'NA',
            kpis: report.kpis?.length ? report.kpis?.map((kpi) => 
              'kpiName: ' + (kpi?.name?.kpiName ? kpi?.name?.kpiName : 'NA')
              + '|' + 'kpiClassification: ' + (kpi?.name?.kpiClassification ? kpi?.name?.kpiClassification : 'NA')
              + '|' + 'reportingCause: ' + (kpi?.reportingCause ? kpi?.reportingCause : 'NA')
              + '|' + 'kpiLink: ' + (kpi?.kpiLink ? kpi?.kpiLink : 'NA')
              + '|' + 'description: ' + (kpi?.description ? kpi?.description : 'NA')
              ) : 'NA',
            datawarehouses: report.dataAndFunctions?.dataWarehouseInUse?.length
              ? report.dataAndFunctions.dataWarehouseInUse
                  ?.map((datawarehouse) => 
                  'datawarehouse: ' + (datawarehouse?.dataWarehouse)
                  + '|' + 'connectionType: ' + datawarehouse?.connectionType
                  + '|' + 'dataClassification: ' + datawarehouse?.dataClassification
                  )   
              : 'NA',
            singledatasources: report.dataAndFunctions?.singleDataSources?.length
              ? report.dataAndFunctions.singleDataSources
                  ?.map((singledatasource) => 
                  'dataSource: ' + (singledatasource?.dataSources.map(item => item.dataSource ))
                  + '|' + 'connectionType: ' + singledatasource?.connectionType
                  + '|' + 'dataClassification: ' + singledatasource?.dataClassification
                  )
              : 'NA',
            admin: report.members.reportAdmins?.length
              ? report.members.reportAdmins?.map((member) => member.shortId)?.join(', ')
              : 'NA',
            publish: report.publish ? 'Yes' : 'No',
            createdDate: report?.createdDate ? regionalDateAndTimeConversionSolution(report.createdDate) : 'NA',
            lastModifiedDate: report?.lastModifiedDate ? regionalDateAndTimeConversionSolution(report.lastModifiedDate) : 'NA',
            procedureId: report?.description?.procedureId ? sanitize(report?.description?.procedureId) : 'NA',
          });
        });
      }
      onDataSuccess(reportsCSVData, csvHeaders);
    }
  });
};

export const sanitize = (text: string) => {
  return text.replace(/"/g, '""');
};
