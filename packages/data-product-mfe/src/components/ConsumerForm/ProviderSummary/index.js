import classNames from 'classnames';
import React from 'react';
import { useSelector } from 'react-redux';
import { serializeDivisionSubDivision } from '../../../Utility/formData';
import Styles from './styles.scss';

const ProviderSummary = ({ onSave }) => {
  const { selectedDataProduct: data, divisionList } = useSelector((state) => state.provideDataProducts);

  const division = serializeDivisionSubDivision(divisionList, {
    division: data.division,
    subDivision: data.subDivision,
  });

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
                <label className="input-label summary">Data Product Name</label>
                <br />
                {data.productName}
              </div>
              <div>
                <label className="input-label summary">Date of Data Transfer</label>
                <br />
                {data.dateOfDataTransfer}
              </div>
              <div>
                <label className="input-label summary">Name</label>
                <br />
                {data.name}
              </div>
              <div>
                <label className="input-label summary">Division</label>
                <br />
                {division?.name}
              </div>
            </div>
            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Sub Division</label>
                <br />
                {division?.subdivision?.name}
              </div>
              <div>
                <label className="input-label summary">Department</label>
                <br />
                {data.department}
              </div>
              <div>
                <label className="input-label summary">PlanningIT App-ID</label>
                <br />
                {data.planningIT}
              </div>
              <div>
                <label className="input-label summary">Complaince Officer / Responsible (LCO/LCR) </label>
                <br />
                {data.complianceOfficer}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.sectionWrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Data Description & Classification</label>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
            <div>
              <label className="input-label summary">Description & Classification of transfered data</label>
              <br />
              {data.classificationOfTransferedData}
            </div>
            <div>
              <label className="input-label summary">Confidentiality</label>
              <br />
              {data.confidentiality}
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.sectionWrapper}>
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
              {data.personalRelatedData}
            </div>
          </div>
          {data.personalRelatedData === 'Yes' ? (
            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
              <div>
                <label className="input-label summary">Description</label>
                <br />
                {data.personalRelatedDataDescription}
              </div>
              <div>
                <label className="input-label summary">
                  Original (business) purpose of processing this personal related data
                </label>
                <br />
                {data.personalRelatedDataPurpose}
              </div>
              <div>
                <label className="input-label summary">
                  Original legal basis for processing this personal related data
                </label>
                <br />
                {data.personalRelatedDataLegalBasis}
              </div>
              <div></div>
            </div>
          ) : null}
        </div>
      </div>
      <div className={Styles.sectionWrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Trans-national Data</label>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
            <div>
              <label className="input-label summary">Is data being transferred from one country to another?</label>
              <br />
              {data.transnationalDataTransfer}
            </div>
            {data.transnationalDataTransfer == 'Yes' ? (
              <div>
                <label className="input-label summary">Is one of these countries not within the EU?</label>
                <br />
                {data.transnationalDataTransferNotWithinEU || 'No'}
              </div>
            ) : null}
            {data.transnationalDataTransfer == 'Yes' && data.transnationalDataTransferNotWithinEU == 'Yes' ? (
              <div>
                <label className="input-label summary">Has LCO/LCR approved this data transfer?</label>
                <br />
                {data.LCOApprovedDataTransfer}
              </div>
            ) : null}
          </div>
          <div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Is data from China included?</label>
              <br />
              {data.dataOriginatedFromChina}
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.sectionWrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Deletion Requirements & Other</label>
            </div>
          </div>
          <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
            <div>
              <label className="input-label summary">Are there specific deletion requirements for this data?</label>
              <br />
              {data.deletionRequirement}
            </div>
            {data.deletionRequirement === 'Yes' ? (
              <div>
                <label className="input-label summary">Describe deletion requirements</label>
                <br />
                {data.deletionRequirementDescription}
              </div>
            ) : null}
            <div></div>
          </div>
          <div className={Styles.flexLayout}>
            <div>
              <label className="input-label summary">Other relevant information </label>
              <br />
              {data.otherRelevantInfo}
            </div>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <button className="btn btn-primary" type="submit" onClick={onSave}>
          Save & Next
        </button>
      </div>
    </>
  );
};

export default ProviderSummary;
