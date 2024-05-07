import { hostServer, reportsServer, server } from '../server/api';

const getAllDataProductList = (sortBy, sortOrder, queryParams) => {
  if(queryParams) {
    return server.get(`/dataproducts?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}${queryParams}`, {
      data: {},
    });
  } else {
    return server.get(`/dataproducts?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
      data: {},
    });
  }
};

const getDataProductById = (id) => {
  return server.get(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/dataproducts', {
    data,
  });
};

const getDataProductsByGraphQL = (data) =>{ 
  let reqQuery = `art:"${data.arts}",platform:"${data.platforms}",frontendTool:"${data.frontendTools}",offset:${data.offset},limit:${data.limit},division:"${data.divisions}",department:"${data.departments}",productOwner:"${data.productOwners}",informationOwner:"${data.informationOwners}",dataSteward:"${data.dataStewards}"`;
  const resQuery = `totalCount
  records {
      id,
      dataProductId,
      dataProductName,
      description,
      carLaFunction {
          id,
          name
      }
      ddx,
      productOwner {
          shortId,
          userType,
          firstName,
          lastName,
          department,
          email,
          mobileNumber,
          company,
          teamMemberPosition,
          addedByProvider,
      }
      tags,
      platform {
          id,
          name
      },
      frontEndTools {
          id,
          name,
      }
      access {
          accessType,
          kafka,
          oneApi,
          confidentiality,
          personalRelatedData,
          deletionRequirement,
          restrictDataAccess,
          minimumInformationCheck,
      },
      howToAccessTemplate {
          useTemplate,
          accessDetailsCollectionVO {
              accessType,
              stepCollectionVO {
                  stepNumber,
                  stepIconType,
                  stepText,
              }
          }
      },
      agileReleaseTrain {
          id,
          name
      },
      corporateDataCatalog {
          id,
          name
      },
      recordStatus,
      isPublish,
      notifyUsers,
      createdDate,
      lastModifiedDate,
      additionalInformation,
      createdBy {
          id,
          name,
          description,
          firstName,
          lastName,
          department,
          email,
          mobileNumber,
      },
      modifiedBy {
          id,
          name,
          description,
          firstName,
          lastName,
          department,
          email,
          mobileNumber,
      },
      contactInformation {
          department
          localComplianceOfficer
          appId
          informationOwner {
              shortId,
              userType,
              firstName,
              lastName,
              department,
              email,
              mobileNumber,
              company,
              teamMemberPosition,
              addedByProvider,
          },
          name {
              shortId,
              userType,
              firstName,
              lastName,
              department,
              email,
              mobileNumber,
              company,
              teamMemberPosition,
              addedByProvider,
          },
          division {
              id,
              name,
              subdivision {
                  id,
                  name
              }
          }
      }
      classificationConfidentiality {
          description,
          confidentiality
      },
      personalRelatedData {
          personalRelatedData,
          description,
          purpose,
          legalBasis,
          contactAwareTransfer,
          objectionsToTransfer,
          transferringNonetheless,
          objections
      },
      transnationalDataTransfer {
          dataTransferred,
          notWithinEU,
          contactAwareTransfer,
          objectionsToTransfer,
          transferringNonetheless,
          objections
      },
      deletionRequirement {
          deletionRequirements,
          description,
          otherRelevantInformation,
          insiderInformation
      },
      datatransfersAssociated {
          id,
          datatransferId,
          datatrandferName
      },
      openSegments
  } `;
  const apiQuery = {
    query: `query Dataproducts{
      dataproducts(${reqQuery}){
        ${resQuery}
      }
    }`,
  };
  return server.post('minified', apiQuery);

}
const updateDataProduct = (data) => {
  return server.put('/dataproducts', {
    data,
  });
};

const deleteDataProduct = (id) => {
  return server.delete(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataTransfer = (id, data) => {
  return server.post(`/dataproducts/${id}/datatransfer`, {
    data,
  });
};

const getAllDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getMyDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&isCreator=true&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getAllAgileReleaseTrains = () => {
  return reportsServer.get('/lov/agilereleasetrains', { data: {} });
};

const getAllCarlaFunctions = () => {
  return server.get('/carlafunctions?limit=0&offset=0', { data: {} });
};

const getAllCorporateDataCatalogs = () => {
  return hostServer.get('/datasources?source=CDC', { data: {} });
};

const getAllPlatforms = () => {
  return server.get('/platforms?limit=0&offset=0', { data: {} });
};

const getAllFrontEndTools = () => {
  return reportsServer.get('/lov/frontendtechnologies', { data: {} });
};

const getAllTags = () => {
  return server.get('/tags', { data: {} });
};

const getAllAccessTypes = () => {
  return server.get('/accesstypes', { data: {} });
};

export const dataProductApi = {
  getAllDataProductList,
  getDataProductById,
  createDataProduct,
  updateDataProduct,
  deleteDataProduct,
  createDataTransfer,
  getAllDataTransfers,
  getMyDataTransfers,
  getAllAgileReleaseTrains,
  getAllCarlaFunctions,
  getAllCorporateDataCatalogs,
  getAllPlatforms,
  getAllFrontEndTools,
  getAllTags,
  getAllAccessTypes,
  getDataProductsByGraphQL
};
