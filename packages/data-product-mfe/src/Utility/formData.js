import { regionalDateAndTimeConversionSolution } from './utils';

export const serializeFormData = (values, division, type = 'provider') => {
  const isProviderForm = type === 'provider';
  if (isProviderForm && values.openSegments?.length === 1 && values.openSegments?.includes('ContactInformation')) {
    return {
      providerInformation: {
        contactInformation: {
          appId: values.planningIT,
          dataTransferDate: new Date(),
          department: values.department,
          division,
          localComplianceOfficer: values.complianceOfficer?.toString(),
          name: values.name,
        },
        openSegments: values.openSegments,
      },
      id: values.id,
      dataProductName: values.productName,
      notifyUsers: false,
      providerFormSubmitted: false,
      publish: false,
    };
  } else {
    return {
      providerInformation: {
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
        deletionRequirement: {
          deletionRequirements: values.deletionRequirement === 'Yes' ? true : false,
          description: values.deletionRequirementDescription,
          otherRelevantInformation: values.otherRelevantInfo,
        },
        personalRelatedData: {
          description: values.personalRelatedDataDescription,
          legalBasis: values.personalRelatedDataLegalBasis,
          personalRelatedData: values.personalRelatedData === 'Yes' ? true : false, //boolean
          purpose: values.personalRelatedDataPurpose,
        },
        transnationalDataTransfer: {
          approved: values.LCOApprovedDataTransfer,
          dataTransferred: values.transnationalDataTransfer === 'Yes' ? true : false, //boolean
          notWithinEU: values.transnationalDataTransferNotWithinEU === 'Yes' ? true : false, //boolean
          dataFromChina: values.dataOriginatedFromChina === 'Yes' ? true : false,
        },
        openSegments: values.openSegments,
      },
      ...(!isProviderForm && values.formValues),
      notifyUsers: values.notifyUsers || false,
      users: values.users || [],
      dataProductName: values.productName,
      id: values.id,
      providerFormSubmitted: values.providerFormSubmitted || false,
      publish: values.publish || false,
    };
  }
};

export const deserializeFormData = (item, type = 'provider') => {
  const isProvider = type === 'provider';
  return {
    id: item.id,
    name: item.providerInformation.contactInformation.name,
    planningIT: item.providerInformation.contactInformation.appId,
    dateOfDataTransfer: regionalDateAndTimeConversionSolution(
      item.providerInformation.contactInformation.dataTransferDate,
    ),
    department: item.providerInformation.contactInformation.department,
    division: item.providerInformation.contactInformation.division.id,
    subDivision: item.providerInformation.contactInformation.division.subdivision.id || '0',
    complianceOfficer: item.providerInformation.contactInformation.localComplianceOfficer?.split(),
    confidentiality: item.providerInformation.classificationConfidentiality?.confidentiality || 'Public',
    classificationOfTransferedData: item.providerInformation.classificationConfidentiality?.description,
    productName: item.dataProductName,
    dataOriginatedFromChina: item.dataFromChina ? 'Yes' : 'No',
    deletionRequirement: item.providerInformation.deletionRequirement?.deletionRequirements ? 'Yes' : 'No',
    deletionRequirementDescription: item.providerInformation.deletionRequirement?.description,
    otherRelevantInfo: item.providerInformation.deletionRequirement?.otherRelevantInformation,
    openSegments: item.providerInformation.openSegments,
    personalRelatedDataDescription: item.providerInformation.personalRelatedData?.description,
    personalRelatedDataLegalBasis: item.providerInformation.personalRelatedData?.legalBasis,
    personalRelatedData: item.providerInformation.personalRelatedData?.personalRelatedData ? 'Yes' : 'No',
    personalRelatedDataPurpose: item.providerInformation.personalRelatedData?.purpose,
    publish: item.publish,
    LCOApprovedDataTransfer: item.providerInformation.transnationalDataTransfer?.approved,
    transnationalDataTransfer: item.providerInformation.transnationalDataTransfer?.dataTransferred ? 'Yes' : 'No',
    transnationalDataTransferNotWithinEU: item.providerInformation.transnationalDataTransfer?.notWithinEU ? 'Yes' : '',
    notifyUsers: item.notifyUsers,
    users: item.users,
    providerFormSubmitted: item.providerFormSubmitted,
    ...(!isProvider && {
      consumer: {
        planningIT: item.consumerInformation?.contactInformation?.appId || 'APP-',
        department: item.consumerInformation?.contactInformation?.department,
        division: item.consumerInformation?.contactInformation?.division.id,
        subDivision: item.consumerInformation?.contactInformation.division.subdivision.id || '0',
        lcoNeeded: item.consumerInformation?.contactInformation.lcoNeeded
          ? item.consumerInformation?.contactInformation.lcoNeeded
            ? 'Yes'
            : 'No'
          : 'Yes',
        complianceOfficer: item.consumerInformation?.contactInformation.localComplianceOfficer?.split(),
        businessOwnerName: item.consumerInformation?.contactInformation.ownerName,
        openSegments: item.consumerInformation?.openSegments,
        LCOComments: item.consumerInformation?.personalRelatedData.comment,
        LCOCheckedLegalBasis: item.consumerInformation?.personalRelatedData.lcoChecked,
        personalRelatedDataLegalBasis: item.consumerInformation?.personalRelatedData.legalBasis,
        personalRelatedData: item.consumerInformation?.personalRelatedData.personalRelatedData ? 'Yes' : 'No',
        personalRelatedDataPurpose: item.consumerInformation?.personalRelatedData.purpose,
        notifyUsers: item.notifyUsers,
        publish: item.publish,
      },
    }),
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
  'consumer-contact-info': 'ContactInformation',
  'consumer-personal-data': 'IdentifyingPersonalRelatedData',
};
