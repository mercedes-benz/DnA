import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, withRouter } from 'react-router-dom';
import { dataProductsApi } from '../../../apis/dataproducts.api';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../../common/modules/uilab/js/src/tabs';
import { hostServer } from '../../../server/api';

import { setSelectedData, setDivisionList } from '../redux/dataSlice';

import Styles from './styles.scss';
import dummyData from '../data.json';

import { regionalDateFormat } from '../../../Utility/utils';

const Summary = ({ history }) => {
  const { id: dataProductId } = useParams();
  const { selectedData: data } = useSelector((state) => state.data);

  // const division = serializeDivisionSubDivision(divisionList, {
  //   division: data.division,
  //   subDivision: data.subDivision,
  // });

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
  }, []);

  const getDataProductById = () => {
    dataProductsApi.getDataProductById(dataProductId).then(() => {
      // const data = deserializeFormData(res.data);
      dispatch(setSelectedData(dummyData));
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
                        <h5>Form 1</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Data Product Name</label>
                        <br />
                        {data.dataProductName}
                      </div>
                      <div>
                        <label className="input-label summary">Date of Data Transfer</label>
                        <br />
                        {regionalDateFormat(data.createdDate)}
                      </div>
                      <div>
                        <label className="input-label summary">Name</label>
                        <br />
                        {data.createdBy?.firstName} {data.createdBy?.lastName}
                      </div>
                      <div>
                        <label className="input-label summary">Division</label>
                        <br />
                        {data.division?.name}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Sub Division</label>
                        <br />
                        {data.division?.subdivision?.name || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">Department</label>
                        <br />
                        {data.department}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default withRouter(Summary);
