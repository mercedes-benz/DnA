import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, withRouter } from 'react-router-dom';
import { dataProductsApi } from '../../apis/dataproducts.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import { hostServer } from '../../server/api';
import { deserializeFormData, serializeDivisionSubDivision } from '../../Utility/formData';
import { setDataProduct, setDivisionList } from '../redux/dataProductSlice';

import Styles from './styles.scss';

const Summary = ({ history }) => {
  const { id: dataProductId } = useParams();
  const { selectedDataProduct: data, divisionList } = useSelector((state) => state.provideDataProducts);

  const division = serializeDivisionSubDivision(divisionList, {
    division: data.division,
    subDivision: data.subDivision,
  });
  const dispatch = useDispatch();

  const [currentTab, setCurrentTab] = useState('provider');
  const showDataDescription = data?.openSegments?.includes('ClassificationAndConfidentiality');
  const showPersonalData = data?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const showTransNationalData = data?.openSegments?.includes('IdentifiyingTransnationalDataTransfer');
  const showDeletionRequirements = data?.openSegments?.includes('SpecifyDeletionRequirements');

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
      dispatch(setDataProduct({}));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getDataProductById = () => {
    dataProductsApi.getDataProductById(dataProductId).then((res) => {
      const data = deserializeFormData(res.data);
      dispatch(setDataProduct(data));
      Tabs.defaultSetup();
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
                    <a href="#tab-content-2" id="consumer" onClick={setTab} className={'hidden'}>
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
                {showDataDescription ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Data Description & Classification</h5>
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
                ) : null}
                {showTransNationalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Trans-national Data</h5>
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
                            <label className="input-label summary">Is one of these countries not within the EU?</label>
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
                          <h5>Deletion Requirements & Other</h5>
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
                          {data.otherRelevantInfo}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
              <div id="tab-content-2" className="tab-content"></div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default withRouter(Summary);
