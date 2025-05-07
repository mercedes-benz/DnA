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
import { Envs } from '../../../Utility/envs';

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
                        <label className="input-label summary">LeanIX App-ID</label>
                        <br />
                        {data?.leanIX?.leanIXDetails?.appReferenceStr !== null ? <a href={`${Envs.LEANIX_BASEURL}/${data?.leanIX?.leanIXDetails?.appReferenceStr}`} target="_blank" rel="noopener noreferrer">{data?.leanIX?.appId}</a> : data?.leanIX?.appId || '-'}
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
                          <pre className={classNames(Styles.formattedText)}>{data.classificationOfTransferedData}</pre>
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
                  <div className={classNames(Styles.sectionWrapper, data.personalRelatedData === 'Yes' && Styles.yellowBorder)}>
                    { data.personalRelatedData === 'Yes' && <i className={classNames('icon mbc-icon alert circle', Styles.warningIcon)} /> }
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
                        <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                          <div>
                            <label className="input-label summary">Description of personal related data</label>
                            <br />
                            <pre className={classNames(Styles.formattedText)}>{data.personalRelatedDataTransferingNonetheless}</pre>
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original (business) purpose of processing this personal related data
                            </label>
                            <br />
                            <pre className={classNames(Styles.formattedText)}>{data.personalRelatedDataPurpose}</pre>
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original legal basis for processing this personal related data
                            </label>
                            <br />
                            {data.personalRelatedDataLegalBasis}
                          </div>
                        </div>
                      ) : null}
                    </div>
                    {data.personalRelatedData === 'Yes' ? (<div className={Styles.flexLayout}>
                      <div>
                        <label className="input-label summary">Is corresponding Compliance contact aware of this transfer?</label>
                        <br />
                        {data.personalRelatedDataContactAwareTransfer}
                      </div>
                    </div>) : null}
                    {data.personalRelatedData === 'Yes' && data.personalRelatedDataContactAwareTransfer === 'Yes'
                      ? (<div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">Has s/he any objections to this transfer?</label>
                          <br />
                          {data.personalRelatedDataObjectionsTransfer}
                        </div>
                        {data.personalRelatedDataObjectionsTransfer === 'Yes' && <>
                          <div>
                            <label className="input-label summary">Please state your reasoning for transfering nonetheless</label>
                            <br />
                            <pre className={classNames(Styles.formattedText)}>{data.personalRelatedDataTransferingNonetheless}</pre>
                          </div>
                          <div>
                            <label className="input-label summary">Please state your objections</label>
                            <br />
                            <pre className={classNames(Styles.formattedText)}>{data.personalRelatedDataTransferingObjections}</pre>
                          </div>
                        </>}
                      </div>) : null}
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
                      <div className={classNames(Styles.flexLayout)}>
                        <div>
                          <label className="input-label summary">
                            Is data being transferred from one country to another?
                          </label>
                          <br />
                          {data.transnationalDataTransfer}
                        </div>
                        {data.transnationalDataTransfer === 'Yes' ? (
                          <div>
                            <label className="input-label summary">Is one of these countries outside the EU?</label>
                            <br />
                            {data.transnationalDataTransferNotWithinEU || 'No'}
                          </div>
                        ) : null}
                      </div>
                      {data?.transnationalDataTransfer === 'Yes' &&
                        data?.transnationalDataTransferNotWithinEU === 'Yes' ? (<div className={Styles.flexLayout}>
                          <div>
                            <label className="input-label summary">Is corresponding Compliance contact aware of this transfer?</label>
                            <br />
                            {data?.transnationalDataContactAwareTransfer}
                          </div>
                        </div>) : null}
                      {data?.transnationalDataTransferNotWithinEU === 'Yes' && data?.transnationalDataContactAwareTransfer === 'Yes'
                        ? (<div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                          <div>
                            <label className="input-label summary">Has s/he any objections to this transfer?</label>
                            <br />
                            {data?.transnationalDataObjectionsTransfer}
                          </div>
                          {data?.transnationalDataObjectionsTransfer === 'Yes' && <>
                            <div>
                              <label className="input-label summary">Please state your reasoning for transfering nonetheless</label>
                              <br />
                              <pre className={classNames(Styles.formattedText)}>{data?.transnationalDataTransferingNonetheless}</pre>
                            </div>
                            <div>
                              <label className="input-label summary">Please state your objections</label>
                              <br />
                              <pre className={classNames(Styles.formattedText)}>{data?.transnationalDataTransferingObjections}</pre>
                            </div>
                          </>}
                        </div>) : null}
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
                          <label className="input-label summary">Does data product contain (potential) insider information?</label>
                          <br />
                          {data.insiderInformation}
                        </div>
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
                            <pre className={classNames(Styles.formattedText)}>{data.deletionRequirementDescription}</pre>
                          </div>
                        ) : null}
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.oneColumn)}>
                        <div>
                          <label className="input-label summary">Other relevant information </label>
                          <br />
                          <pre className={classNames(Styles.formattedText)}>{data.otherRelevantInfo || '-'}</pre>
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
                          <span
                            className={Styles.editicon}
                            onClick={() => {
                              history.push(`/datasharing/consume/${dataTransferId}`);
                            }}
                              style={{ cursor: "pointer" }}>
                              <div>
                                <i className="icon mbc-icon edit small" />
                              </div>
                          </span>
                        </div>
                        <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                          <div>
                            <label className="input-label summary">Responsible Manager (E3 +) </label>
                            <br />
                            {data?.consumer?.businessOwnerName?.firstName} {data?.consumer?.businessOwnerName?.lastName}
                          </div>
                          <div>
                            <label className="input-label summary">Date of Data Transfer</label>
                            <br />
                            {regionalDateFormat(data?.consumer?.dateOfDataTransfer)}
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
                            <label className="input-label summary">LeanIX App-ID</label>
                            <br />
                            {data?.consumer?.leanIX?.leanIXDetails.appReferenceStr !== null ?<a href={`${Envs.LEANIX_BASEURL}/${data.consumer?.leanIX?.leanIXDetails?.appReferenceStr}`} target="_blank" rel="noopener noreferrer">{data?.consumer?.leanIX?.appId}</a>: data?.consumer?.leanIX?.appId || '-'}
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
                      <div className={classNames(Styles.sectionWrapper, data?.consumer?.personalRelatedData === 'Yes' && Styles.yellowBorder)}>
                        { data?.consumer?.personalRelatedData === 'Yes' && <i className={classNames('icon mbc-icon alert circle', Styles.warningIcon)} /> }
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
                                <pre className={classNames(Styles.formattedText)}>{data?.consumer?.personalRelatedDataPurpose}</pre>
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
                                <pre className={classNames(Styles.formattedText)}>{data?.consumer?.LCOComments}</pre>
                              </div>
                            </div>
                          ) : null}
                          {data?.consumer?.personalRelatedData === 'Yes' ? (<div className={Styles.flexLayout}>
                            <div>
                              <label className="input-label summary">Is corresponding Compliance contact aware of this transfer?</label>
                              <br />
                              {data?.consumer?.personalRelatedDataContactAwareTransfer}
                            </div>
                          </div>) : null}
                          {data?.consumer?.personalRelatedData === 'Yes' && data?.consumer?.personalRelatedDataContactAwareTransfer === 'Yes'
                            ? (<div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                              <div>
                                <label className="input-label summary">Has s/he any objections to this transfer?</label>
                                <br />
                                {data?.consumer?.personalRelatedDataObjectionsTransfer}
                              </div>
                              {data?.consumer?.personalRelatedDataObjectionsTransfer === 'Yes' && <>
                                <div>
                                  <label className="input-label summary">Please state your reasoning for transfering nonetheless</label>
                                  <br />
                                  <pre className={classNames(Styles.formattedText)}>{data?.consumer?.personalRelatedDataTransferingNonetheless}</pre>
                                </div>
                                <div>
                                  <label className="input-label summary">Please state your objections</label>
                                  <br />
                                  <pre className={classNames(Styles.formattedText)}>{data?.consumer?.personalRelatedDataTransferingObjections}</pre>
                                </div>
                              </>}
                            </div>) : null}
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
