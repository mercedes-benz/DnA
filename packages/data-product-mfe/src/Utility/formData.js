export const serializeFormData = ({ values, division, type = 'provider', isDataProduct = false, dropdowns, currentTab }) => {
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
            additionalInformation: values.additionalInformation,
            tags: values.tags,
            // access: {
            //   accessType: values.accessType,
            //   confidentiality: values.confidentiality,
            //   deletionRequirements: values.deletionRequirements === 'Yes' ? true : false,
            //   kafka: values.kafka,
            //   minimumInformationCheck: values.minimumInformationCheck,
            //   oneApi: values.oneApi,
            //   personalRelatedData: values.personalRelatedDataInDescription === 'Yes' ? true : false,
            //   restrictDataAccess: values.restrictDataAccess === 'Yes' ? true : false
            // },
            access: {
              accessType: values.accessType,
              confidentiality: currentTab == 'description' ?
                values.confidentialityInDescription 
              :
                values.confidentiality,
              deletionRequirements: currentTab == 'description' ?
                values.deletionRequirementInDescription === 'Yes' ? true : false
                :
                values.deletionRequirement === 'Yes' ? true : false,
              kafka: values.kafka,
              minimumInformationCheck: values.minimumInformationCheck,
              oneApi: values.oneApi,
              personalRelatedData: currentTab == 'description' ?
                values.personalRelatedDataInDescription === 'Yes' ? true : false
                : 
                values.personalRelatedData === 'Yes' ? true : false,                     
              restrictDataAccess: values.restrictDataAccess === 'Yes' ? true : false
            },
            howToAccessText: values.howToAccessText,
            howToAccessTemplate: values.howToAccessTemplate,
            useTemplate: values.accessType,
            accessTypeTab: values.accessTypeTab,
            productOwner: values.productOwner,
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
                  contactAwareTransfer: values.personalRelatedDataContactAwareTransfer === 'Yes' ? true : false, //boolean
                  objectionsToTransfer: values.personalRelatedDataObjectionsTransfer === 'Yes' ? true : false, //boolean
                  transferringNonetheless: values.personalRelatedDataTransferingNonetheless,
                  objections: values.personalRelatedDataTransferingObjections
                },
                transnationalDataTransfer: {
                  dataTransferred: values.transnationalDataTransfer === 'Yes' ? true : false, //boolean
                  notWithinEU: values.transnationalDataTransferNotWithinEU === 'Yes' ? true : false, //boolean
                  contactAwareTransfer: values.transnationalDataContactAwareTransfer === 'Yes' ? true : false, //boolean
                  objectionsToTransfer: values.transnationalDataObjectionsTransfer === 'Yes' ? true : false, //boolean
                  transferringNonetheless: values.transnationalDataTransferingNonetheless,
                  objections: values.transnationalDataTransferingObjections
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
                  access: {
                    accessType: values.accessType,
                    confidentiality: currentTab == 'description' ?
                      values.confidentialityInDescription 
                    :
                      values.confidentiality,
                    deletionRequirements: currentTab == 'description' ?
                      values.deletionRequirementInDescription === 'Yes' ? true : false
                      :
                      values.deletionRequirement === 'Yes' ? true : false,
                    kafka: values.kafka,
                    minimumInformationCheck: values.minimumInformationCheck,
                    oneApi: values.oneApi,
                    personalRelatedData: currentTab == 'description' ?
                      values.personalRelatedDataInDescription === 'Yes' ? true : false
                      : 
                      values.personalRelatedData === 'Yes' ? true : false,                     
                    restrictDataAccess: values.restrictDataAccess === 'Yes' ? true : false
                  },
                  howToAccessText: values.howToAccessText,
                  howToAccessTemplate: values.howToAccessTemplate,
                  useTemplate: values.accessType,
                  accessTypeTab: values.accessTypeTab,
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
                  tags: values.tags,
                  ddx: values.ddx,
                  kafka: values.kafka,
                  oneApi: values.oneApi,
                  productOwner: values.productOwner,
                  contactInformation: {
                    appId: values.planningIT,
                    department: values.department === '' ? undefined : values.department?.toString(),
                    division,
                    informationOwner: values.informationOwner,
                    localComplianceOfficer: values.complianceOfficer?.toString(),
                    name: values.name,
                  },
                  ...(values?.openSegments?.includes('ClassificationAndConfidentiality') && {
                    classificationConfidentiality: {
                      confidentiality: currentTab == 'classification-confidentiality' ? 
                      values.confidentiality
                      : 
                      values.confidentialityInDescription,
                      description: values.classificationOfTransferedData || '',
                    },
                  }),
                  ...(values?.openSegments?.includes('IdentifyingPersonalRelatedData') && {
                    personalRelatedData: {
                      personalRelatedData: currentTab == 'personal-data' ?
                        values.personalRelatedData === 'Yes' ? true : false
                      :
                        values.personalRelatedDataInDescription === 'Yes' ? true : false,
                      ...(values.personalRelatedData === 'Yes' && {
                        description: values.personalRelatedDataDescription,
                        legalBasis: values.personalRelatedDataLegalBasis,
                        purpose: values.personalRelatedDataPurpose,
                        contactAwareTransfer: values.personalRelatedDataContactAwareTransfer === 'Yes' ? true : false, //boolean
                        objectionsToTransfer: values.personalRelatedDataObjectionsTransfer === 'Yes' ? true : false, //boolean
                        transferringNonetheless: values.personalRelatedDataTransferingNonetheless,
                        objections: values.personalRelatedDataTransferingObjections
                      }),
                    },
                  }),
                  ...(values?.openSegments?.includes('IdentifiyingTransnationalDataTransfer') && {
                    transnationalDataTransfer: {
                      dataTransferred: values.transnationalDataTransfer === 'Yes' ? true : false,
                      notWithinEU: values.transnationalDataTransferNotWithinEU === 'Yes' ? true : false,
                      contactAwareTransfer: values.transnationalDataContactAwareTransfer === 'Yes' ? true : false, //boolean
                      objectionsToTransfer: values.transnationalDataObjectionsTransfer === 'Yes' ? true : false, //boolean
                      transferringNonetheless: values.transnationalDataTransferingNonetheless,
                      objections: values.transnationalDataTransferingObjections
                    },
                  }),
                  ...(values?.openSegments?.includes('SpecifyDeletionRequirements') && {
                    deletionRequirement: {
                      deletionRequirements: currentTab == 'deletion-requirements' ?
                        values.deletionRequirement === 'Yes' ? true : false
                      :
                        values.deletionRequirementInDescription === 'Yes' ? true : false,
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
          department: item.providerInformation?.contactInformation?.department === '' ? undefined : item.providerInformation?.contactInformation?.department?.split(),
          division: item.providerInformation?.contactInformation?.division?.id,
          subDivision: item.providerInformation?.contactInformation?.division?.subdivision?.id || '0',
          complianceOfficer: item.providerInformation?.contactInformation?.localComplianceOfficer?.split(),
          confidentiality: item.providerInformation?.classificationConfidentiality?.confidentiality || 'Internal',
          classificationOfTransferedData: item.providerInformation?.classificationConfidentiality?.description,
          deletionRequirement: item.providerInformation?.deletionRequirement?.deletionRequirements ? 'Yes' : 'No',
          deletionRequirementDescription: item.providerInformation?.deletionRequirement?.description,
          otherRelevantInfo: item.providerInformation?.deletionRequirement?.otherRelevantInformation,
          openSegments: item.providerInformation?.openSegments,
          personalRelatedDataDescription: item.providerInformation?.personalRelatedData?.description,
          personalRelatedDataLegalBasis: item.providerInformation?.personalRelatedData?.legalBasis,
          personalRelatedData: item.providerInformation?.personalRelatedData?.personalRelatedData ? 'Yes' : 'No',
          personalRelatedDataContactAwareTransfer: item?.providerInformation?.personalRelatedData?.contactAwareTransfer  ? 'Yes' : item.providerInformation?.personalRelatedData?.personalRelatedData ? 'No' : '',
          personalRelatedDataObjectionsTransfer: item?.providerInformation?.personalRelatedData?.objectionsToTransfer  ? 'Yes' : item?.providerInformation?.personalRelatedData?.contactAwareTransfer ? 'No' : '',
          personalRelatedDataTransferingNonetheless: item?.providerInformation?.personalRelatedData?.transferringNonetheless,
          personalRelatedDataTransferingObjections: item?.providerInformation?.personalRelatedData?.objections,
          personalRelatedDataPurpose: item.providerInformation?.personalRelatedData?.purpose,
          transnationalDataTransfer: item.providerInformation?.transnationalDataTransfer?.dataTransferred
            ? 'Yes'
            : 'No',
          transnationalDataTransferNotWithinEU: item.providerInformation?.transnationalDataTransfer?.notWithinEU
            ? 'Yes'
            : item.providerInformation?.transnationalDataTransfer?.dataTransferred
            ? 'No'
            : '',
          transnationalDataContactAwareTransfer: item.providerInformation?.transnationalDataTransfer?.contactAwareTransfer
            ? 'Yes'
            : item.providerInformation?.transnationalDataTransfer?.notWithinEU ? 'No' : '',
          transnationalDataObjectionsTransfer: item.providerInformation?.transnationalDataTransfer?.objectionsToTransfer
            ? 'Yes'
            : item.providerInformation?.transnationalDataTransfer?.contactAwareTransfer ? 'No' : '',
          transnationalDataTransferingNonetheless: item.providerInformation?.transnationalDataTransfer?.transferringNonetheless,
          transnationalDataTransferingObjections: item.providerInformation?.transnationalDataTransfer?.objections,
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
              dateOfDataTransfer: item.consumerInformation?.contactInformation.dataTransferDate || '',
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

              personalRelatedDataContactAwareTransfer: item?.consumerInformation?.personalRelatedData?.contactAwareTransfer ? 'Yes' : item?.consumerInformation?.personalRelatedData?.personalRelatedData ? 'No' : '',
              personalRelatedDataObjectionsTransfer: item?.consumerInformation?.personalRelatedData?.objectionsToTransfer  ? 'Yes' : item?.consumerInformation?.personalRelatedData?.contactAwareTransfer ? 'No': '',
              personalRelatedDataTransferingNonetheless: item?.consumerInformation?.personalRelatedData?.transferringNonetheless,
              personalRelatedDataTransferingObjections: item?.consumerInformation?.personalRelatedData?.objections,

              notifyUsers: item?.notifyUsers,
              publish: item.publish,
            },
          }),
        }
      : {
          description: item.description,
          additionalInformation: item.additionalInformation,
          tags: item.tags,
          ART: item?.agileReleaseTrain?.name || '',
          carLAFunction: item?.carLaFunction?.name || '',
          corporateDataCatalog: item?.corporateDataCatalog?.name || '',
          platform: item?.platform?.map((item) => item.name) || [],
          frontEndTools: item?.frontEndTools?.map((item) => item.name) || [],
          ddx: item?.ddx,
          // kafka: item?.kafka,
          // oneApi: item?.oneApi,
          productOwner: item?.productOwner,

          dataProductId: item?.dataProductId,
          productName: item?.dataProductName,
          id: item?.id,
          
            accessType: item?.access?.accessType,
            confidentialityInDescription: item?.access?.confidentiality || 'Internal',
            kafka: item?.access?.kafka,
            minimumInformationCheck: item?.access?.minimumInformationCheck,
            oneApi: item?.access?.oneApi,
            // personalRelatedData: item?.personalRelatedData,
            personalRelatedDataInDescription: item?.access?.personalRelatedData ? 'Yes' : 'No',
            // deletionRequirements: item?.deletionRequirements,
            // restrictDataAccess: item?.restrictDataAccess,
            deletionRequirementInDescription: item?.access?.deletionRequirements ? 'Yes' : 'No',
            restrictDataAccess: item?.access?.restrictDataAccess ? 'Yes' : 'No',
          
          howToAccessText: item?.howToAccessText,
          howToAccessTemplate: item?.howToAccessTemplate,
          kafkaArray: item?.howToAccessTemplate?.accessDetailsCollectionVO[0]?.stepCollectionVO ? item?.howToAccessTemplate?.accessDetailsCollectionVO[0]?.stepCollectionVO : [],
          liveAccessArray: item?.howToAccessTemplate?.accessDetailsCollectionVO[1]?.stepCollectionVO ? item?.howToAccessTemplate?.accessDetailsCollectionVO[1]?.stepCollectionVO : [],
          apiArray: item?.howToAccessTemplate?.accessDetailsCollectionVO[2]?.stepCollectionVO ? item?.howToAccessTemplate?.accessDetailsCollectionVO[2]?.stepCollectionVO : [],
          // useTemplate: item?.howToAccessTemplate?.useTemplate,
          useTemplate: item?.access?.accessType,
          accessTypeTab: item?.accessTypeTab,
          deletionRequirements: item?.deletionRequirements ? 'Yes' : 'No',
          // restrictDataAccess: item?.restrictDataAccess ? 'Yes' : 'No',

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

          personalRelatedDataContactAwareTransfer: item?.personalRelatedData?.contactAwareTransfer ? 'Yes' : item?.personalRelatedData?.personalRelatedData ? 'No' : '',
          personalRelatedDataObjectionsTransfer: item?.personalRelatedData?.objectionsToTransfer  ? 'Yes' : item?.personalRelatedData?.contactAwareTransfer ? 'No': '',
          personalRelatedDataTransferingNonetheless: item?.personalRelatedData?.transferringNonetheless,
          personalRelatedDataTransferingObjections: item?.personalRelatedData?.objections,

          transnationalDataTransfer: item?.transnationalDataTransfer?.dataTransferred ? 'Yes' :  'No',
          transnationalDataTransferNotWithinEU: item?.transnationalDataTransfer?.notWithinEU ? 'Yes' : item?.transnationalDataTransfer?.dataTransferred ? 'No' : '',

          transnationalDataContactAwareTransfer: item?.transnationalDataTransfer?.contactAwareTransfer ? 'Yes' : item?.transnationalDataTransfer?.notWithinEU ? 'No' : '',
          transnationalDataObjectionsTransfer: item?.transnationalDataTransfer?.objectionsToTransfer ? 'Yes' : item?.transnationalDataTransfer?.contactAwareTransfer ? 'No' : '',
          transnationalDataTransferingNonetheless: item?.transnationalDataTransfer?.transferringNonetheless,
          transnationalDataTransferingObjections: item?.transnationalDataTransfer?.objections,

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
              dateOfDataTransfer: item.consumerFormValues?.consumerInformation?.contactInformation.dataTransferDate || '',
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

              personalRelatedDataContactAwareTransfer: item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer
                ? 'Yes'
                : item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer?.personalRelatedData ? 'No' : '',
              personalRelatedDataObjectionsTransfer: item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer?.objectionsToTransfer
                ? 'Yes'
                : item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer?.contactAwareTransfer ? 'No': '',
              personalRelatedDataTransferingNonetheless:
                item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer?.transferringNonetheless,
              personalRelatedDataTransferingObjections:
                item?.consumerFormValues?.consumerInformation?.personalRelatedData?.contactAwareTransfer?.objections,

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
