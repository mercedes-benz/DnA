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
    { label: 'Name', key: 'name' },
    { label: 'Phase', key: 'productPhase' },
    { label: 'Description', key: 'description' },
    { label: 'Tags', key: 'tags' },
    { label: 'Division', key: 'division' },
    { label: 'Subdivision', key: 'subdivision' },
    { label: 'Department', key: 'department' },
    { label: 'Status', key: 'status' },
    { label: 'Integrated In Portal', key: 'integratedPortal' },
    { label: 'Agile Release Trains', key: 'agileReleaseTrains' },
    { label: 'Design Guide Implemented', key: 'designGuideImplemented' },
    { label: 'Frontend Technologies', key: 'frontendTechnologies' },
    { label: 'Customers', key: 'customers' },
    { label: 'Process Owner', key: 'processOwners' },
    { label: 'KPIs', key: 'kpis' },
    { label: 'Data Warehouse', key: 'datawarehouses' },
    { label: 'Single Datasource', key: 'singledatasources' },
    { label: 'Product Owner', key: 'productOwners' },
    { label: 'Developers', key: 'developers' },
    { label: 'Admin', key: 'admin' },
    { label: 'IsPublished', key: 'publish' },
    { label: 'CreatedDate', key: 'createdDate' },
    { label: 'LastModifiedDate', key: 'lastModifiedDate' },
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
      const reportsCSV = resCSV.data.reports as IAllReportsResultCSV;
      if (reportsCSV.records) {
        reportsCSV.records.forEach((report) => {
          reportsCSVData.push({
            name: report.productName ? sanitize(report.productName) : 'NA',
            productPhase: report.description.productPhase ? report.description.productPhase : 'NA',
            description: report.description.productDescription ? sanitize(report.description.productDescription) : 'NA',
            tags:
              report.description.tags && report.description.tags.length > 0
                ? sanitize(report.description.tags.join(', '))
                : 'NA',
            division: report.description.division?.name ? report.description.division.name : 'NA',
            subdivision: report.description.division?.subdivision ? report.description.division.subdivision.name : 'NA',
            department: report.description.department ? report.description.department : 'NA',
            status: report.description.status ? report.description.status : 'NA',
            integratedPortal: report.description.integratedPortal?.length
              ? report.description.integratedPortal?.join(', ')
              : 'NA',
            agileReleaseTrains: report.description.agileReleaseTrains?.length
              ? report.description.agileReleaseTrains?.join(', ')
              : 'NA',
            designGuideImplemented: report.description.designGuideImplemented || 'NA',
            frontendTechnologies: report.description.frontendTechnologies?.length
              ? report.description.frontendTechnologies?.join(', ')
              : 'NA',
            customers: report.customer?.customerDetails?.length
              ? report.customer?.customerDetails?.map((customer) => Object.values(customer))?.join(' | ')
              : 'NA',
            processOwners: report.customer?.processOwners?.length
              ? report.customer.processOwners?.map((member) => member.shortId)?.join(', ')
              : 'NA',
            kpis: report.kpis?.length ? report.kpis?.map((kpi) => Object.values(kpi))?.join(' | ') : 'NA',
            datawarehouses: report.dataAndFunctions?.dataWarehouseInUse?.length
              ? report.dataAndFunctions.dataWarehouseInUse
                  ?.map((datawarehouse) => Object.values(datawarehouse))
                  ?.join(' | ')
              : 'NA',
            singledatasources: report.dataAndFunctions?.singleDataSources?.length
              ? report.dataAndFunctions.singleDataSources
                  ?.map((singledatasource) => Object.values(singledatasource))
                  ?.join(' | ')
              : 'NA',
            productOwners: report.members.productOwners?.length
              ? report.members.productOwners?.map((member) => member.shortId)?.join(', ')
              : 'NA',
            developers: report.members.developers?.length
              ? report.members.developers?.map((member) => member.shortId)?.join(', ')
              : 'NA',
            admin: report.members.admin?.length
              ? report.members.admin?.map((member) => member.shortId)?.join(', ')
              : 'NA',
            publish: report.publish ? 'Yes' : 'No',
            createdDate: report?.createdDate ? regionalDateAndTimeConversionSolution(report.createdDate) : 'NA',
            lastModifiedDate: report?.lastModifiedDate ? regionalDateAndTimeConversionSolution(report.lastModifiedDate) : 'NA',
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
