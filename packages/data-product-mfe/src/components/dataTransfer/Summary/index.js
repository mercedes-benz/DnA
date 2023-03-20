import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, withRouter } from 'react-router-dom';
import { dataTransferApi } from '../../../apis/datatransfers.api';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../../common/modules/uilab/js/src/tabs';
import { hostServer } from '../../../server/api';
import { deserializeFormData, serializeDivisionSubDivision } from '../../../Utility/formData';
import { setSelectedDataTransfer, setDivisionList } from '../redux/dataTransferSlice';

import Styles from './styles.scss';

import ShowTeamMemberList from 'dna-container/ShowTeamMemberList';
import { regionalDateFormat } from '../../../Utility/utils';

const Summary = ({ history }) => {
  const { id: dataTransferId } = useParams();
  const { selectedDataTransfer: data, divisionList } = useSelector((state) => state.provideDataTransfers);

  const division = serializeDivisionSubDivision(divisionList, {
    division: data.division,
    subDivision: data.subDivision,
  });
  const consumerDivision = data.consumer
    ? serializeDivisionSubDivision(divisionList, {
        division: data.consumer?.division,
        subDivision: data.consumer?.subDivision,
      })
    : {};

  const dispatch = useDispatch();

  const [currentTab, setCurrentTab] = useState('provider');
  const showDataDescription = data?.openSegments?.includes('ClassificationAndConfidentiality');
  const showPersonalData = data?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const showTransNationalData = data?.openSegments?.includes('IdentifiyingTransnationalDataTransfer');
  const showDeletionRequirements = data?.openSegments?.includes('SpecifyDeletionRequirements');
  const showReceivingSideMembers = data?.users?.length;

  const showConsumerPersonalData = data.consumer?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const receivingMembers = data?.users?.map((member, index) => {
    return <ShowTeamMemberList key={index} itemIndex={index} teamMember={member} />;
  });

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      dispatch(setDivisionList(res.data));
      ProgressIndicator.hide();
    });
  }, [dispatch]);

  useEffect(() => {
    getDataProductById();

    return () => {
      dispatch(setSelectedDataTransfer({}));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getDataProductById = () => {
    dataTransferApi.getDataTransferById(dataTransferId).then((res) => {
      if (res.status === 204) {
        return history.push('/NotFound');
      } else {
        const data = deserializeFormData({ item: res.data });
        dispatch(setSelectedDataTransfer(data));
        Tabs.defaultSetup();
      }
    });
  };

  const setTab = (e) => {
    // e.preventDefault();
    setCurrentTab(e.target.id);
  };

  return (
    <>
      <div className={Styles.mainPanel}>
        <div>
          <button className="btn btn-text back arrow" type="submit" onClick={() => history.goBack()}>
            Back
          </button>
          <div className={Styles.summaryBannerTitle}>
            <h2>{data.productName}</h2>
          </div>
          <div id="data-product-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={currentTab === 'provider' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="provider" onClick={setTab}>
                      Provider Summary
                    </a>
                  </li>
                  <li className={currentTab === 'consumer' ? 'tab active ' : 'tab '}>
                    <a href="#tab-content-2" id="consumer" onClick={setTab}>
                      Consumer Summary
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary1" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary2" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary3" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary4" className={'hidden'}>
                      `
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <div className={Styles.sectionWrapper}>
                  <div className={Styles.firstPanel}>
                    <div className={Styles.flexLayout}>
                      <div>
                        <h5>Contact Information</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">
                          Data Product Name / Short description of data transfer
                        </label>
                        <br />
                        {data.productName}
                      </div>
                      <div>
                        <label className="input-label summary">Date of Data Transfer</label>
                        <br />
                        {regionalDateFormat(data.dateOfDataTransfer)}
                      </div>
                      <div>
                        <label className="input-label summary">Point of contact for data transfer</label>
                        <br />
                        {data.name?.firstName} {data.name?.lastName}
                      </div>
                      <div>
                        <label className="input-label summary">Data responsible IO and/or Business Owner for application</label>
                        <br />
                        {data.informationOwner?.firstName} {data.informationOwner?.lastName}
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
                        {data.department}
                      </div>
                      <div>
                        <label className="input-label summary">PlanningIT App-ID</label>
                        <br />
                        {data.planningIT || '-'}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Corresponding Compliance Contact, i.e. Local Compliance Officer/ Responsible or Multiplier </label>
                        <br />
                        {data.complianceOfficer}
                      </div>
                    </div>
                  </div>
                </div>
                {showDataDescription ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Data Description &amp; Classification</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">Description of transferred data</label>
                          <br />
                          {data.classificationOfTransferedData}
                        </div>
                        <div>
                          <label className="input-label summary">Confidentiality classification of transferred data (based on Information classification)</label>
                          <br />
                          {data.confidentiality}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showPersonalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Personal Related Data</h5>
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
                            <label className="input-label summary">Description of personal related data</label>
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
                ) : null}
                {showTransNationalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Transnational Data</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Is data being transferred from one country to another?
                          </label>
                          <br />
                          {data.transnationalDataTransfer}
                        </div>
                        {data.transnationalDataTransfer == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Is one of these countries outside the EU?</label>
                            <br />
                            {data.transnationalDataTransferNotWithinEU || 'No'}
                          </div>
                        ) : null}
                        {data.transnationalDataTransfer == 'Yes' &&
                        data.transnationalDataTransferNotWithinEU == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Has LCO/LCR approved this data transfer?</label>
                            <br />
                            {data.LCOApprovedDataTransfer}
                          </div>
                        ) : null}
                        <div>
                          <label className="input-label summary">Does data product contain (potential) insider information?</label>
                          <br />
                          {data.insiderInformation}
                        </div>
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
                ) : null}
                {showDeletionRequirements ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Other Data</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Are there specific deletion requirements for this data?
                          </label>
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
                          {data.otherRelevantInfo || '-'}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showReceivingSideMembers ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Point of contact (data receiving side)</h5>
                        </div>
                      </div>
                      {receivingMembers}
                    </div>
                  </div>
                ) : null}
              </div>
              <div id="tab-content-2" className="tab-content">
                {data.consumer ? (
                  <>
                    <div className={Styles.sectionWrapper}>
                      <div className={Styles.firstPanel}>
                        <div className={Styles.flexLayout}>
                          <div>
                            <h5>Contact Information</h5>
                          </div>
                        </div>
                        <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                          <div>
                            <label className="input-label summary">Responsible Manager (E3 +) </label>
                            <br />
                            {data?.consumer?.businessOwnerName?.firstName} {data?.consumer?.businessOwnerName?.lastName}
                          </div>
                          <div>
                            <label className="input-label summary">Date of Agreement</label>
                            <br />
                            {regionalDateFormat(data?.consumer?.dateOfAgreement)}
                          </div>
                          <div>
                            <label className="input-label summary">Division</label>
                            <br />
                            {consumerDivision?.name}
                          </div>
                          <div>
                            <label className="input-label summary">Sub Division</label>
                            <br />
                            {consumerDivision?.subdivision?.name || '-'}
                          </div>
                        </div>
                        <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                          <div>
                            <label className="input-label summary">Department</label>
                            <br />
                            {data?.consumer?.department}
                          </div>
                          <div>
                            <label className="input-label summary">
                              LCO/LCR needed to be involved / has to check legal basis of usage of personal data?{' '}
                            </label>
                            <br />
                            {data?.consumer?.lcoNeeded}
                          </div>
                          <div>
                            <label className="input-label summary">PlanningIT App-ID</label>
                            <br />
                            {data?.consumer?.planningIT || '-'}
                          </div>
                          <div>
                            <label className="input-label summary">Corresponding Compliance Contact, i.e. Local Compliance Officer/ Responsible or Multiplier </label>
                            <br />
                            {data?.consumer?.complianceOfficer || '-'}
                          </div>
                        </div>
                      </div>
                    </div>
                    {showConsumerPersonalData ? (
                      <div className={Styles.sectionWrapper}>
                        <div className={Styles.firstPanel}>
                          <div className={Styles.flexLayout}>
                            <div>
                              <h5>Personal Related Data</h5>
                            </div>
                          </div>
                          <div className={Styles.flexLayout}>
                            <div>
                              <label className="input-label summary">
                                Is personal related data transferred and actually processed at application level?
                              </label>
                              <br />
                              {data?.consumer?.personalRelatedData}
                            </div>
                          </div>
                          {data?.consumer?.personalRelatedData === 'Yes' ? (
                            <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                              <div>
                                <label className="input-label summary">
                                  (Business) purpose of processing this personal related data
                                </label>
                                <br />
                                {data?.consumer?.personalRelatedDataPurpose}
                              </div>
                              <div>
                                <label className="input-label summary">
                                  Legal basis for processing this personal related data?
                                </label>
                                <br />
                                {data?.consumer?.personalRelatedDataLegalBasis}
                              </div>
                              <div>
                                <label className="input-label summary">
                                  Has corresponding compliance contact checked overall personal data processing
                                </label>
                                <br />
                                {data?.consumer?.LCOCheckedLegalBasis}
                              </div>
                              <div>
                                <label className="input-label summary">
                                  LCO/LCR comments to data usage of personal data
                                </label>
                                <br />
                                {data?.consumer?.LCOComments}
                              </div>
                            </div>
                          ) : null}
                        </div>
                      </div>
                    ) : null}
                  </>
                ) : (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.flexLayout} style={{ color: '#c0c8d0', justifyContent: 'center' }}>
                      Receiving side not yet started to finalise the data transfer.
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default withRouter(Summary);
