export const serializeFormData = ({ values, division, type = 'provider', isDataProduct = false, dropdowns }) => {
  const isProviderForm = type === 'provider';
  const isConsumerForm = type === 'consumer';
  if (
    (isProviderForm && values.openSegments?.length === 1 && values.openSegments?.includes('ContactInformation')) ||
    (isDataProduct && values.openSegments?.length === 1 && values.openSegments?.includes('Description'))
  ) {
    return {
      ...(!isDataProduct
        ? {
            providerInformation: {
              contactInformation: {
                appId: values.planningIT,
                dataTransferDate: values.dateOfDataTransfer === null ? undefined : new Date(values.dateOfDataTransfer),
                department: values.department === '' ? undefined : values.department?.toString(),
                division,
                localComplianceOfficer: values.complianceOfficer?.toString(),
                name: values.name,
                informationOwner: values.informationOwner,
              },
              openSegments: values.openSegments,
            },
            id: values.id,
            dataTransferName: values.productName,
            notifyUsers: false,
            publish: false,
          }
        : {
            id: values.id,
            dataProductName: values.productName,
            description: values.description,
            howToAccessText: values.howToAccessText,
            isPublish: values.publish || false,
            notifyUsers: values.notifyUsers || false,
            openSegments: values.openSegments,
            agileReleaseTrain: dropdowns.agileReleaseTrains?.find((item) => item.name === values.ART),
            carLaFunction: dropdowns.carLAFunctions?.find((item) => item.name === values.carLAFunction),
            corporateDataCatalog: dropdowns.corporateDataCatalogs?.find(
              (item) => item.name === values.corporateDataCatalog,
            ),
            platform: values.platform,
            frontEndTools: values.frontEndTools,
            ddx: values.ddx,
            kafka: values.kafka,
            oneApi: values.oneApi,
          }),
    };
  } else {
    return {
      ...(!isDataProduct
        ? {
            notifyUsers: values.notifyUsers || false,
            dataTransferName: values.productName,
            id: values.id,
            publish: values.publish || false,
            ...(isProviderForm && {
              providerInformation: {
                classificationConfidentiality: {
                  confidentiality: values.confidentiality,
                  description: values.classificationOfTransferedData || '',
                },
                contactInformation: {
                  appId: values.planningIT,
                  dataTransferDate: values.dateOfDataTransfer === null ? undefined : new Date(values.dateOfDataTransfer),
                  department: values.department === '' ? undefined : values.department?.toString(),
                  division,
                  localComplianceOfficer: values.complianceOfficer?.toString(),
                  name: values.name,
                  informationOwner: values.informationOwner,
                },
                deletionRequirement: {
                  deletionRequirements: values.deletionRequirement === 'Yes' ? true : false,
                  description: values.deletionRequirementDescription,
                  otherRelevantInformation: values.otherRelevantInfo,
                  insiderInformation: values.insiderInformation,
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
                providerFormSubmitted: values.providerFormSubmitted || false,
                users: values.users || [],
              },
            }),
            ...(!isProviderForm && values.consumerFormValues),
          }
        : {
            ...(isConsumerForm
              ? {
                  ...values.consumerFormValues,
                  ...{
                    notifyUsers: values.notifyUsers || false,
                    dataTransferName: values.dataTransferName,
                    isPublish: values.publish || false,
                  },
                }
              : {
                  dataProductName: values.productName,
                  description: values.description,
                  howToAccessText: values.howToAccessText,
                  id: values.id,
                  isPublish: values.publish || false,
                  notifyUsers: values.notifyUsers || false,
                  openSegments: values.openSegments,
                  agileReleaseTrain: dropdowns?.agileReleaseTrains?.find((item) => item.name === values.ART),
                  carLaFunction: dropdowns?.carLAFunctions?.find((item) => item.name === values.carLAFunction),
                  corporateDataCatalog: dropdowns?.corporateDataCatalogs?.find(
                    (item) => item.name === values.corporateDataCatalog,
                  ),
                  platform: values.platform,
                  frontEndTools: values.frontEndTools,
                  ddx: values.ddx,
                  kafka: values.kafka,
                  oneApi: values.oneApi,
                  contactInformation: {
                    appId: values.planningIT,
                    dataProductDate: values.dateOfDataProduct === null ? undefined : new Date(values.dateOfDataProduct),
                    department: values.department === '' ? undefined : values.department?.toString(),
                    division,
                    informationOwner: values.informationOwner,
                    localComplianceOfficer: values.complianceOfficer?.toString(),
                    name: values.name,
                  },
                  ...(values?.openSegments?.includes('ClassificationAndConfidentiality') && {
                    classificationConfidentiality: {
                      confidentiality: values.confidentiality,
                      description: values.classificationOfTransferedData || '',
                    },
                  }),
                  ...(values?.openSegments?.includes('IdentifyingPersonalRelatedData') && {
                    personalRelatedData: {
                      personalRelatedData: values.personalRelatedData === 'Yes' ? true : false,
                      ...(values.personalRelatedData === 'Yes' && {
                        description: values.personalRelatedDataDescription,
                        legalBasis: values.personalRelatedDataLegalBasis,
                        purpose: values.personalRelatedDataPurpose,
                      }),
                    },
                  }),
                  ...(values?.openSegments?.includes('IdentifiyingTransnationalDataTransfer') && {
                    transnationalDataTransfer: {
                      approved: values.LCOApprovedDataTransfer,
                      dataFromChina: values.dataOriginatedFromChina === 'Yes' ? true : false,
                      dataTransferred: values.transnationalDataTransfer === 'Yes' ? true : false,
                      notWithinEU: values.transnationalDataTransferNotWithinEU === 'Yes' ? true : false,
                    },
                  }),
                  ...(values?.openSegments?.includes('SpecifyDeletionRequirements') && {
                    deletionRequirement: {
                      deletionRequirements: values.deletionRequirement === 'Yes' ? true : false,
                      description: values.deletionRequirementDescription,
                      otherRelevantInformation: values.otherRelevantInfo,
                      insiderInformation: values.insiderInformation,
                    },
                  }),
                }),
          }),
    };
  }
};

export const deserializeFormData = ({ item, type = 'provider', isDataProduct = false }) => {
  const isProvider = type === 'provider';
  const isConsumerForm = type === 'consumer';
  return {
    ...(!isDataProduct
      ? {
          id: item.id,
          productName: item.dataTransferName,
          publish: item.publish,
          name: item.providerInformation?.contactInformation?.name,
          informationOwner: item.providerInformation?.contactInformation?.informationOwner,
          planningIT: item.providerInformation?.contactInformation?.appId,
          dateOfDataTransfer: item.providerInformation?.contactInformation?.dataTransferDate === null ? undefined : item.providerInformation?.contactInformation?.dataTransferDate,
          department: item.providerInformation?.contactInformation?.department === '' ? undefined : item.providerInformation?.contactInformation?.department?.split(),
          division: item.providerInformation?.contactInformation?.division?.id,
          subDivision: item.providerInformation?.contactInformation?.division?.subdivision?.id || '0',
          complianceOfficer: item.providerInformation?.contactInformation?.localComplianceOfficer?.split(),
          confidentiality: item.providerInformation?.classificationConfidentiality?.confidentiality || 'Internal',
          classificationOfTransferedData: item.providerInformation?.classificationConfidentiality?.description,
          dataOriginatedFromChina: item.providerInformation?.transnationalDataTransfer?.dataFromChina ? 'Yes' : 'No',
          deletionRequirement: item.providerInformation?.deletionRequirement?.deletionRequirements ? 'Yes' : 'No',
          deletionRequirementDescription: item.providerInformation?.deletionRequirement?.description,
          otherRelevantInfo: item.providerInformation?.deletionRequirement?.otherRelevantInformation,
          openSegments: item.providerInformation?.openSegments,
          personalRelatedDataDescription: item.providerInformation?.personalRelatedData?.description,
          personalRelatedDataLegalBasis: item.providerInformation?.personalRelatedData?.legalBasis,
          personalRelatedData: item.providerInformation?.personalRelatedData?.personalRelatedData ? 'Yes' : 'No',
          personalRelatedDataPurpose: item.providerInformation?.personalRelatedData?.purpose,
          LCOApprovedDataTransfer: item.providerInformation?.transnationalDataTransfer?.approved,
          transnationalDataTransfer: item.providerInformation?.transnationalDataTransfer?.dataTransferred
            ? 'Yes'
            : 'No',
          transnationalDataTransferNotWithinEU: item.providerInformation?.transnationalDataTransfer?.notWithinEU
            ? 'Yes'
            : '',
          insiderInformation: item.providerInformation?.deletionRequirement?.insiderInformation || 'No',
          notifyUsers: item?.notifyUsers,
          users: item.providerInformation?.users,
          providerFormSubmitted: item.providerInformation?.providerFormSubmitted,
          createdBy: item.providerInformation?.createdBy,
          modifiedBy: item.providerInformation?.modifiedBy,
          ...((!isProvider || item.consumerInformation) && {
            consumer: {
              planningIT: item.consumerInformation?.contactInformation?.appId,
              department: item.consumerInformation?.contactInformation?.department === '' ? undefined : item.consumerInformation?.contactInformation?.department?.split(),
              division: item.consumerInformation?.contactInformation?.division.id,
              subDivision: item.consumerInformation?.contactInformation.division.subdivision.id || '0',
              dateOfAgreement: item.consumerInformation?.contactInformation.agreementDate || '',
              lcoNeeded: item.consumerInformation?.contactInformation.lcoNeeded ? 'Yes' : 'No',
              complianceOfficer: item.consumerInformation?.contactInformation.localComplianceOfficer
                ?.split()
                .filter(Boolean),
              businessOwnerName: item.consumerInformation?.contactInformation.ownerName,
              openSegments: item.consumerInformation?.openSegments,
              LCOComments: item.consumerInformation?.personalRelatedData.comment,
              LCOCheckedLegalBasis: item.consumerInformation?.personalRelatedData.lcoChecked,
              personalRelatedDataLegalBasis: item.consumerInformation?.personalRelatedData.legalBasis,
              personalRelatedData: item.consumerInformation?.personalRelatedData.personalRelatedData ? 'Yes' : 'No',
              personalRelatedDataPurpose: item.consumerInformation?.personalRelatedData.purpose,
              notifyUsers: item?.notifyUsers,
              publish: item.publish,
            },
          }),
        }
      : {
          description: item.description,
          ART: item?.agileReleaseTrain?.name || '',
          carLAFunction: item?.carLaFunction?.name || '',
          corporateDataCatalog: item?.corporateDataCatalog?.name || '',
          platform: item?.platform?.map((item) => item.name) || [],
          frontEndTools: item?.frontEndTools?.map((item) => item.name) || [],
          ddx: item?.ddx,
          kafka: item?.kafka,
          oneApi: item?.oneApi,

          dataProductId: item?.dataProductId,
          productName: item?.dataProductName,
          id: item?.id,
          howToAccessText: item?.howToAccessText,

          isPublish: item.isPublish,
          notifyUsers: item.notifyUsers,
          openSegments: item?.openSegments,
          informationOwner: item?.contactInformation?.informationOwner,
          dateOfDataProduct: item?.contactInformation?.dataProductDate === null ? undefined : item?.contactInformation?.dataProductDate,
          department: item?.contactInformation?.department === '' ? undefined : item?.contactInformation?.department?.split(),
          name: item?.contactInformation?.name,
          division: item?.contactInformation?.division?.id || '0',
          subDivision: item?.contactInformation?.division?.subdivision?.id || '0',
          complianceOfficer: item?.contactInformation?.localComplianceOfficer?.split(),
          planningIT: item?.contactInformation?.appId,

          confidentiality: item?.classificationConfidentiality?.confidentiality || 'Internal',
          classificationOfTransferedData: item?.classificationConfidentiality?.description,

          personalRelatedDataDescription: item?.personalRelatedData?.description,
          personalRelatedDataLegalBasis: item?.personalRelatedData?.legalBasis,
          personalRelatedData: item?.personalRelatedData?.personalRelatedData ? 'Yes' : 'No',
          personalRelatedDataPurpose: item?.personalRelatedData?.purpose,

          LCOApprovedDataTransfer: item?.transnationalDataTransfer?.approved,
          transnationalDataTransfer: item?.transnationalDataTransfer?.dataTransferred ? 'Yes' : 'No',
          transnationalDataTransferNotWithinEU: item?.transnationalDataTransfer?.notWithinEU ? 'Yes' : '',
          dataOriginatedFromChina: item?.transnationalDataTransfer?.dataFromChina ? 'Yes' : 'No',

          insiderInformation: item?.deletionRequirement?.insiderInformation || 'No',
          deletionRequirement: item?.deletionRequirement?.deletionRequirements ? 'Yes' : 'No',
          deletionRequirementDescription: item?.deletionRequirement?.description,
          otherRelevantInfo: item?.deletionRequirement?.otherRelevantInformation,

          datatransfersAssociated: item?.datatransfersAssociated,
          dataTransferName: item?.dataTransferName,

          createdBy: item?.createdBy,
          modifiedBy: item?.modifiedBy,

          ...(isConsumerForm && {
            consumer: {
              planningIT: item.consumerFormValues?.consumerInformation?.contactInformation?.appId,
              department: item.consumerFormValues?.consumerInformation?.contactInformation?.department === '' ? undefined : item.consumerFormValues?.consumerInformation?.contactInformation?.department?.split(),
              division: item.consumerFormValues?.consumerInformation?.contactInformation?.division.id,
              subDivision:
                item.consumerFormValues?.consumerInformation?.contactInformation.division.subdivision.id || '0',
              dateOfAgreement: item.consumerFormValues?.consumerInformation?.contactInformation.agreementDate || '',
              lcoNeeded: item.consumerFormValues?.consumerInformation?.contactInformation.lcoNeeded ? 'Yes' : 'No',
              complianceOfficer: item.consumerFormValues?.consumerInformation?.contactInformation.localComplianceOfficer
                ?.split()
                .filter(Boolean),
              businessOwnerName: item.consumerFormValues?.consumerInformation?.contactInformation.ownerName,
              openSegments: item.consumerFormValues?.consumerInformation?.openSegments,
              LCOComments: item.consumerFormValues?.consumerInformation?.personalRelatedData.comment,
              LCOCheckedLegalBasis: item.consumerFormValues?.consumerInformation?.personalRelatedData.lcoChecked,
              personalRelatedDataLegalBasis:
                item.consumerFormValues?.consumerInformation?.personalRelatedData.legalBasis,
              personalRelatedData: item.consumerFormValues?.consumerInformation?.personalRelatedData.personalRelatedData
                ? 'Yes'
                : 'No',
              personalRelatedDataPurpose: item.consumerFormValues?.consumerInformation?.personalRelatedData.purpose,
              dataTransferName: item?.dataTransferName,
            },
          }),
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

export const stringArrayToObjectArray = (values, dataList) => {
  const result = [];
  values?.map((value) => {
    dataList?.forEach((data) => {
      if (data?.name === value) {
        return result.push(data);
      }
    });
  });
  return result;
};

export const mapOpenSegments = {
  description: 'Description',
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
