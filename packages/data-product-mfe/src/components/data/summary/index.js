import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { withRouter } from 'react-router-dom';
import { dataProductApi } from '../../../apis/data.api';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../../common/modules/uilab/js/src/tabs';
import { hostServer } from '../../../server/api';
import DataTranferCardLayout from '../../dataTransfer/Layout/CardView/DataProductCardItem';

import { setSelectedData, setDivisionList } from '../redux/dataSlice';

import Styles from './styles.scss';

import { regionalDateFormat } from '../../../Utility/utils';
import mockData from '../data.json';

const Summary = ({ history, user }) => {
  const { selectedData: data, data: dataList } = useSelector((state) => state.data);

  const dispatch = useDispatch();

  const [currentTab, setCurrentTab] = useState('provider');

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
      dispatch(setSelectedData({}));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dataList]);

  const getDataProductById = () => {
    dataProductApi.getDataById(dataList, '0059').then((res) => {
      // const data = deserializeFormData(res.data);
      if (res) {
        dispatch(setSelectedData(res));
      } else {
        return history.push('/NotFound');
      }
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
            <h2>{data?.productName}</h2>
          </div>
          <div id="data-product-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={currentTab === 'provider' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="provider" onClick={setTab}>
                      Data Product Summary
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
                  <li className={'tab disabled'}>
                    <a id="summary5" className={'hidden'}>
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
                        <h5>Description</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                      <div>
                        <label className="input-label summary">Data Product Name</label>
                        <br />
                        {data.productName}
                      </div>
                      <div>
                        <label className="input-label summary">Data Product Description</label>
                        <br />
                        {data.productDescription}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">ART</label>
                        <br />
                        {data.ART}
                      </div>
                      <div>
                        <label className="input-label summary">carLA</label>
                        <br />
                        {data.carLA}
                      </div>
                      <div>
                        <label className="input-label summary">Corporate Data Catalog</label>
                        <br />
                        {data.dataCatalog}
                      </div>
                      <div>
                        <label className="input-label summary">How to access data catalog</label>
                        <br />
                        <span dangerouslySetInnerHTML={{ __html: data.howToAccess }}></span>
                      </div>
                    </div>
                  </div>
                </div>
                <div className={Styles.sectionWrapper}>
                  <div className={Styles.firstPanel}>
                    <div className={Styles.flexLayout}>
                      <div>
                        <h5>Contact Information</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Information Owner</label>
                        <br />
                        {data.informationOwner?.firstName} {data.informationOwner?.lastName}
                      </div>
                      <div>
                        <label className="input-label summary">Publish Date of Data Product</label>
                        <br />
                        {regionalDateFormat(data.dateOfDataProduct)}
                      </div>
                      <div>
                        <label className="input-label summary">Name</label>
                        <br />
                        {data?.providerInformation?.contactInformation?.name?.firstName}{' '}
                        {data?.providerInformation?.contactInformation?.name?.lastName}
                      </div>
                      <div>
                        <label className="input-label summary">Division</label>
                        <br />
                        {data?.division?.name}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Sub Division</label>
                        <br />
                        {data?.division?.subdivision?.name || '-'}
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
                      <div>
                        <label className="input-label summary">Compliance Officer / Responsible (LCO/LCR) </label>
                        <br />
                        {data.complianceOfficer}
                      </div>
                    </div>
                  </div>
                </div>
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
                        {data?.providerInformation?.classificationConfidentiality?.confidentiality}
                      </div>
                    </div>
                  </div>
                </div>
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
                      {data.transnationalDataTransfer == 'Yes' && data.transnationalDataTransferNotWithinEU == 'Yes' ? (
                        <div>
                          <label className="input-label summary">Has LCO/LCR approved this data transfer?</label>
                          <br />
                          {data.LCOApprovedDataTransfer}
                        </div>
                      ) : null}
                      <div>
                        <label className="input-label summary">Does product contain insider information?</label>
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
                        {data.otherRelevantInfo || '-'}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <hr className={Styles.line} />
          <div className={Styles.dataTransferSection}>
            <h3>{`Data Transfer ${mockData?.result?.length ? `(${mockData?.result?.length})` : null}`}</h3>
            <div>
              <Link to={'/datasharing'} target="_blank" rel="noreferrer noopener">
                Show in Data Sharing
                <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} />
              </Link>
            </div>
          </div>
          <div className={classNames(Styles.allDataproductCardviewContent)}>
            {mockData.result?.map((product, index) => {
              return <DataTranferCardLayout key={index} product={product} user={user} isDataProduct={true} />;
            })}
          </div>
          <div className={Styles.actionBtns}>
            <div className="btn-set">
              <button className="btn btn-primary" type="button">
                Edit
              </button>
              <button className="btn btn-primary" type="button">
                Delete
              </button>
            </div>
            <div style={{ alignSelf: 'flex-end' }}>
              <button className="btn btn-tertiary" type="button">
                How to access
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default withRouter(Summary);
