import {
  IART,
  IConnectionType,
  ICreateNewReportRequest,
  IDataSourceMaster,
  IDatawarehouseInItem,
  IDesignGuide,
  IFrontEndTech,
  IIntegratedPortal,
  IProductPhase,
  IProductStatus,
  ISingleDataSources,
  ISubSystems,
} from 'globals/types';

export const serializeReportRequestBody = (requestBody: ICreateNewReportRequest) => {
  requestBody.data.description.department = requestBody.data.description.department?.toString() as any;
  requestBody.data.description.frontendTechnologies =
    requestBody.data.description.frontendTechnologies?.length == 1 &&
    requestBody.data.description.frontendTechnologies[0].name == 'Choose'
      ? null
      : (requestBody.data.description.frontendTechnologies
          ?.filter((item: IFrontEndTech) => item.name !== 'Choose')
          ?.map((item: IFrontEndTech) => item.name) as any[]);
  requestBody.data.description.agileReleaseTrains =
    requestBody.data.description.agileReleaseTrains?.length > 0
      ? (requestBody.data.description.agileReleaseTrains?.map((item: IART) => item.name) as any[])
      : null;
  requestBody.data.description.integratedPortal =
    requestBody.data.description.integratedPortal?.length > 0
      ? (requestBody.data.description.integratedPortal?.map((item: IIntegratedPortal) => item.name) as any[])
      : null;
  requestBody.data.description.designGuideImplemented =
    requestBody.data.description.designGuideImplemented?.length == 1 &&
    requestBody.data.description.designGuideImplemented.includes('Choose')
      ? null
      : (requestBody.data.description.designGuideImplemented
          ?.filter((item: IDesignGuide) => item.name !== 'Choose')
          ?.map((item: IDesignGuide) => item.name)
          ?.toString() as any);
  requestBody.data.description.productPhase =
    requestBody.data.description.productPhase?.length == 1 &&
    requestBody.data.description.productPhase.includes('Choose')
      ? null
      : (requestBody.data.description.productPhase
          ?.filter((item: IProductPhase) => item.name !== 'Choose')
          ?.map((item: IProductPhase) => item.name)
          ?.toString() as any);
  requestBody.data.description.status = requestBody.data.description.status
    ?.filter((item: IProductStatus) => item.name !== 'Choose')
    ?.map((item: IProductStatus) => item.name)
    ?.toString() as any;
  requestBody.data.dataAndFunctions.dataWarehouseInUse = requestBody.data.dataAndFunctions.dataWarehouseInUse?.map(
    (item: IDatawarehouseInItem) => {
      item.dataWarehouse = item.dataWarehouse || null;
      item.commonFunctions = item.commonFunctions?.length ? item.commonFunctions : null;
      item.specificFunctions = item.specificFunctions?.length ? item.specificFunctions : null;
      item.queries = item.queries?.length ? item.queries : null;
      item.commonFunctions = item.commonFunctions?.length ? item.commonFunctions : null;
      item.dataSources = item.dataSources?.length ? item.dataSources : null;
      item.connectionTypes = item.connectionTypes?.length ? item.connectionTypes : null;
      return item;
    },
  );
  requestBody.data.dataAndFunctions.singleDataSources = requestBody.data.dataAndFunctions.singleDataSources?.map(
    (item: ISingleDataSources) => {
      item.dataSources = item.dataSources?.length
        ? (item.dataSources?.map((item: IDataSourceMaster) => item.name) as any[])
        : null;
      item.subsystems = item.subsystems?.length
        ? (item.subsystems?.map((item: ISubSystems) => item.name) as any[])
        : null;
      item.connectionTypes = item.connectionTypes?.length
        ? (item.connectionTypes?.map((item: IConnectionType) => item.name) as any[])
        : null;
      return item;
    },
  );
  return requestBody;
};
