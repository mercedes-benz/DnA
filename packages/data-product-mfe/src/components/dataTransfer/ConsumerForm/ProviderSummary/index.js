import classNames from 'classnames';
import React, { useEffect, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import { serializeDivisionSubDivision } from '../../../../Utility/formData';
import Styles from './styles.scss';

const ProviderSummary = ({ onSave, providerFormIsDraft }) => {
  const { selectedDataTransfer: data, divisionList } = useSelector((state) => state.provideDataTransfers);

  const isMounted = useRef(false);
  const [providerInformation, setProviderInformation] = useState({});

  const division = serializeDivisionSubDivision(divisionList, {
    division: providerInformation.division,
    subDivision: providerInformation.subDivision,
  });

  const showDataDescription = providerInformation?.openSegments?.includes('ClassificationAndConfidentiality');
  const showPersonalData = providerInformation?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const showTransNationalData = providerInformation?.openSegments?.includes('IdentifiyingTransnationalDataTransfer');
  const showDeletionRequirements = providerInformation?.openSegments?.includes('SpecifyDeletionRequirements');

  useEffect(() => {
    if (data.id && !isMounted.current) {
      isMounted.current = true;
      let copyData = { ...data };
      delete copyData['consumer'];
      setProviderInformation(copyData);
    }
  }, [data]);

  return (
    <>
      <div className={Styles.wrapper}>
        <span className={Styles.description}>Summary of provider information</span>
        <div className={Styles.firstPanel}>
          <div className={Styles.formWrapper}>
            <hr className="divider1" />
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Contact Information</label>
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Data Product Name / Short description of data transfer</label>
                <br />
                {providerInformation.productName}
              </div>
              <div>
                <label className="input-label summary">Point of contact for data transfer</label>
                <br />
                {providerInformation.name?.firstName} {providerInformation.name?.lastName}
              </div>
              <div>
                <label className="input-label summary">Data responsible IO and/or Business Owner for application</label>
                <br />
                {providerInformation.informationOwner?.firstName} {providerInformation.informationOwner?.lastName}
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Division</label>
                <br />
                {division?.name}
              </div>
              <div>
                <label className="input-label summary">Sub Division</label>
                <br />
                {division?.subdivision?.name || '-'}
              </div>
              <div>
                <label className="input-label summary">Department</label>
                <br />
                {providerInformation.department}
              </div>
              <div>
                <label className="input-label summary">PlanningIT App-ID</label>
                <br />
                {providerInformation.planningIT || '-'}
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Corresponding Compliance Contact, i.e. Local Compliance Officer/ Responsible or Multiplier </label>
                <br />
                {providerInformation.complianceOfficer}
              </div>
            </div>
          </div>
        </div>
      </div>
      {showDataDescription ? (
        <div className={Styles.sectionWrapper}>
          <div className={Styles.firstPanel}>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Data Description &amp; Classification</label>
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
              <div>
                <label className="input-label summary">Description of transfered data</label>
                <br />
                {providerInformation.classificationOfTransferedData}
              </div>
              <div>
                <label className="input-label summary">Confidentiality</label>
                <br />
                {providerInformation.confidentiality}
              </div>
            </div>
          </div>
        </div>
      ) : null}
      {showPersonalData ? (
        <div className={classNames(Styles.sectionWrapper, providerInformation?.personalRelatedData === 'Yes' && Styles.yellowBorder)}>
          { providerInformation?.personalRelatedData === 'Yes' && <i className={classNames('icon mbc-icon alert circle', Styles.warningIcon)} /> }
          <div className={Styles.firstPanel}>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Personal Related Data</label>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Is data personal related</label>
                <br />
                {providerInformation.personalRelatedData}
              </div>
            </div>
            {providerInformation.personalRelatedData === 'Yes' ? (
              <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                <div>
                  <label className="input-label summary">Description of personal related data</label>
                  <br />
                  {providerInformation.personalRelatedDataDescription}
                </div>
                <div>
                  <label className="input-label summary">
                    Original (business) purpose of processing this personal related data
                  </label>
                  <br />
                  {providerInformation.personalRelatedDataPurpose}
                </div>
                <div>
                  <label className="input-label summary">
                    Original legal basis for processing this personal related data
                  </label>
                  <br />
                  {providerInformation.personalRelatedDataLegalBasis}
                </div>
                <div></div>
              </div>
            ) : null}
          </div>
          {providerInformation.personalRelatedData === 'Yes' ? (<div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Is corresponding Compliance contact aware of this transfer?</label>
              <br />
              {providerInformation.personalRelatedDataContactAwareTransfer}
            </div>
          </div>) : null}
          {providerInformation.personalRelatedData === 'Yes' && providerInformation.personalRelatedDataContactAwareTransfer === 'Yes'
            ? (<div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Has s/he any objections to this transfer?</label>
                <br />
                {providerInformation.personalRelatedDataObjectionsTransfer}
              </div>
              {providerInformation.personalRelatedDataObjectionsTransfer === 'Yes' && <>
                <div>
                  <label className="input-label summary">Please state your reasoning for transfering nonetheless</label>
                  <br />
                  {providerInformation.personalRelatedDataTransferingNonetheless}
                </div>
                <div>
                  <label className="input-label summary">Please state your objections</label>
                  <br />
                  {providerInformation.personalRelatedDataTransferingObjections}
                </div>
              </>}
              <div></div>
            </div>) : null}
        </div>
      ) : null}
      {showTransNationalData ? (
        <div className={Styles.sectionWrapper}>
          <div className={Styles.firstPanel}>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Transnational Data</label>
              </div>
            </div>
            <div className={classNames(Styles.flexLayout)}>
              <div>
                <label className="input-label summary">Is data being transferred from one country to another?</label>
                <br />
                {providerInformation.transnationalDataTransfer}
              </div>
              {providerInformation.transnationalDataTransfer === 'Yes' ? (
                <div>
                  <label className="input-label summary">Is one of these countries outside the EU?</label>
                  <br />
                  {providerInformation.transnationalDataTransferNotWithinEU || 'No'}
                </div>
              ) : null}
            </div>
            {providerInformation?.transnationalDataTransfer === 'Yes' &&
              providerInformation?.transnationalDataTransferNotWithinEU === 'Yes' ? (<div className={Styles.flexLayout}>
                <div>
                  <label className="input-label summary">Is corresponding Compliance contact aware of this transfer?</label>
                  <br />
                  {providerInformation?.transnationalDataContactAwareTransfer}
                </div>
              </div>) : null}
            {providerInformation?.transnationalDataTransferNotWithinEU === 'Yes' && providerInformation?.transnationalDataContactAwareTransfer === 'Yes'
              ? (<div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                <div>
                  <label className="input-label summary">Has s/he any objections to this transfer?</label>
                  <br />
                  {providerInformation?.transnationalDataObjectionsTransfer}
                </div>
                {providerInformation?.transnationalDataObjectionsTransfer === 'Yes' && <>
                  <div>
                    <label className="input-label summary">Please state your reasoning for transfering nonetheless</label>
                    <br />
                    {providerInformation?.transnationalDataTransferingNonetheless}
                  </div>
                  <div>
                    <label className="input-label summary">Please state your objections</label>
                    <br />
                    {providerInformation?.transnationalDataTransferingObjections}
                  </div></>}
                <div></div>
              </div>) : null}
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Is data from China included?</label>
                <br />
                {providerInformation.dataOriginatedFromChina}
              </div>
            </div>
          </div>
        </div>
      ) : null}
      {showDeletionRequirements ? (
        <div className={Styles.sectionWrapper}>
          <div className={Styles.firstPanel}>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Other Data</label>
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
              <div>
                <label className="input-label summary">Does data product contain (potential) insider information?</label>
                <br />
                {providerInformation.insiderInformation}
              </div>
              <div>
                <label className="input-label summary">Are there specific deletion requirements for this data?</label>
                <br />
                {providerInformation.deletionRequirement}
              </div>
              {providerInformation.deletionRequirement === 'Yes' ? (
                <div>
                  <label className="input-label summary">Describe deletion requirements</label>
                  <br />
                  {providerInformation.deletionRequirementDescription}
                </div>
              ) : null}
              <div></div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label className="input-label summary">Other relevant information </label>
                <br />
                {providerInformation.otherRelevantInfo || '-'}
              </div>
            </div>
          </div>
        </div>
      ) : null}
      <div className="btnContainer">
        <button disabled={providerFormIsDraft} className="btn btn-primary" type="submit" onClick={onSave}>
          Save & Next
        </button>
      </div>
    </>
  );
};

export default ProviderSummary;
