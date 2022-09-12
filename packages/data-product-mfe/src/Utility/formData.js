import { regionalDateAndTimeConversionSolution } from './utils';

export const serializeFormData = (values, division) => {
  if (values.openSegments.length === 1 && values.openSegments.includes('ContactInformation')) {
    return {
      contactInformation: {
        appId: values.planningIT,
        dataTransferDate: new Date(),
        department: values.department,
        division,
        localComplianceOfficer: values.complianceOfficer?.toString(),
        name: values.name,
      },
      id: values.id,
      dataProductName: values.productName,
      dataFromChina: values.dataOriginatedFromChina === 'Yes' ? true : false,
      openSegments: values.openSegments,
      publish: false,
    };
  } else {
    return {
      classificationConfidentiality: {
        confidentiality: values.confidentiality,
        description: values.classificationOfTransferedData || '',
      },
      contactInformation: {
        appId: values.planningIT,
        dataTransferDate: new Date(),
        department: values.department,
        division,
        localComplianceOfficer: values.complianceOfficer?.toString(),
        name: values.name,
      },
      dataFromChina: values.dataOriginatedFromChina === 'Yes' ? true : false,
      dataProductName: values.productName,
      deletionRequirement: {
        deletionRequirements: values.deletionRequirement === 'Yes' ? true : false,
        description: values.deletionRequirementDescription,
      },
      id: values.id,
      openSegments: values.openSegments,
      otherInformation: values.otherRelevantInfo,
      personalRelatedData: {
        description: values.personalRelatedDataDescription,
        legalBasis: values.personalRelatedDataLegalBasis,
        personalRelatedData: values.personalRelatedData === 'Yes' ? true : false, //boolean
        purpose: values.personalRelatedDataPurpose,
      },
      publish: values.publish || false,
      transnationalDataTransfer: {
        approved: values.LCOApprovedDataTransfer,
        dataTransferred: values.transnationalDataTransfer === 'Yes' ? true : false, //boolean
        notWithinEU: values.transnationalDataTransferNotWithinEU === 'Yes' ? true : false, //boolean
      },
    };
  }
};

export const deserializeFormData = (item) => {
  return {
    id: item.id,
    name: item.contactInformation.name,
    planningIT: item.contactInformation.appId,
    dateOfDataTransfer: regionalDateAndTimeConversionSolution(item.contactInformation.dataTransferDate),
    department: item.contactInformation.department,
    division: item.contactInformation.division.id,
    subDivision: item.contactInformation.division.subdivision.id || '0',
    complianceOfficer: item.contactInformation.localComplianceOfficer?.split(),
    confidentiality: item.classificationConfidentiality?.confidentiality || 'Public',
    classificationOfTransferedData: item.classificationConfidentiality?.description,
    productName: item.dataProductName,
    dataOriginatedFromChina: item.dataFromChina ? 'Yes' : 'No',
    deletionRequirement: item.deletionRequirement?.deletionRequirements ? 'Yes' : 'No',
    deletionRequirementDescription: item.deletionRequirement?.description,
    openSegments: item.openSegments,
    otherRelevantInfo: item.otherInformation,
    personalRelatedDataDescription: item.personalRelatedData?.description,
    personalRelatedDataLegalBasis: item.personalRelatedData?.legalBasis,
    personalRelatedData: item.personalRelatedData?.personalRelatedData ? 'Yes' : 'No',
    personalRelatedDataPurpose: item.personalRelatedData?.purpose,
    publish: item.publish,
    LCOApprovedDataTransfer: item.transnationalDataTransfer?.approved,
    transnationalDataTransfer: item.transnationalDataTransfer?.dataTransferred ? 'Yes' : 'No',
    transnationalDataTransferNotWithinEU: item.transnationalDataTransfer?.notWithinEU ? 'Yes' : '',
  };
};

export const serializeDivisionSubDivision = (divisions, values) => {
  return divisions.reduce((acc, curr) => {
    if (curr.id === values.division) {
      acc['id'] = curr.id;
      acc['name'] = curr.name;
      acc['subdivision'] =
        values.subDivision !== '0'
          ? curr.subdivisions.find((sub) => sub.id === values.subDivision)
          : { id: null, name: null };
    }
    return acc;
  }, {});
};

export const mapOpenSegments = {
  'contact-info': 'ContactInformation',
  'classification-confidentiality': 'ClassificationAndConfidentiality',
  'personal-data': 'IdentifyingPersonalRelatedData',
  'trans-national-data-transfer': 'IdentifiyingTransnationalDataTransfer',
  'deletion-requirements': 'SpecifyDeletionRequirements',
};

export const consumerOpenSegments = {
  'provider-summary': 'Provider Summary',
  'consumer-contact-info': 'ConsumerContactInformation',
  'consumer-personal-data': 'ConsumerIdentifyingPersonalRelatedData',
};
