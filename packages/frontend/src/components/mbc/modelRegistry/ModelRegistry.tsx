import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Styles from './ModelRegistry.scss';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

const classNames = cn.bind(Styles);

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import InfoModal from 'components/formElements/modal/infoModal/InfoModal';
// import Pagination from '../pagination/Pagination';
import ModelRegistryList from './modelRegistryList/ModelRegistryList';
import { ModelRegistryApiClient } from '../../../services/ModelRegistryApiClient';
import { Envs } from 'globals/Envs';
import Caption from '../shared/caption/Caption';

const MyModelRegistry = (props: any) => {
  const [modelsList, setModelsList] = useState([]);
  const [info, setInfo] = useState(false);

  useEffect(() => {
    getModelsList();
  }, []);

  const getModelsList = () => {
    ProgressIndicator.show();
    ModelRegistryApiClient.getModels()
      .then((res) => {
        setModelsList(res.data);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
      });
  };

  const onInfoModalShow = () => {
    setInfo(true);
  };
  const onInfoModalCancel = () => {
    setInfo(false);
  };

  const contentForInfo = (
    <div className={Styles.infoPopup}>
      <div className={Styles.modalContent}>
        <p>
          Model Registry displays your ML Pipeline Models and provides External API URL to consume ML models through
          Secured API on your applications.
        </p>
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.wrapper}>
          <Caption title="My Model Registry" />
        </div>
        {modelsList === null || modelsList === undefined || modelsList.length === 0 ? null : (
          <React.Fragment>
            <div className={Styles.infoIcon}>
              <i
                tooltip-data="Info"
                className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                onClick={onInfoModalShow}
              />
            </div>
          </React.Fragment>
        )}
        <div className={Styles.content}>
          <div className={Styles.NoModels}>
            <div className={Styles.header}>
              {modelsList === null || modelsList === undefined || modelsList.length === 0 ? (
                <React.Fragment>
                  <p>
                    Model Registry displays your ML Pipeline Models and provides External API URL to consume ML models
                    through Secured API on your applications.
                  </p>
                  <i
                    tooltip-data="Info"
                    className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                    onClick={onInfoModalShow}
                  />
                </React.Fragment>
              ) : (
                ''
              )}
            </div>
            <div className={Styles.modelsContent}>
              {modelsList === null || modelsList === undefined || modelsList.length === 0 ? (
                <div className={Styles.modelsListEmpty}>
                  <React.Fragment>
                    <p className={Styles.textCenter}>You don't have any ML Pipeline Models to get External API URL.</p>
                    <a
                      className={'btn btn-tertiary ' + Styles.addNewBtn}
                      href={`${Envs.ML_PIPELINE_URL}/_/models/?ns=kubeflow-${props.user.id}`}
                      target="_blank"
                      rel="noreferrer noopener"
                    >
                      Manage Models on ML Pipeline
                    </a>
                  </React.Fragment>
                </div>
              ) : (
                <React.Fragment>
                  <div className={Styles.modelsList}>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          <th>
                            <label className="sortable-column-header">Model Name</label>
                          </th>
                          {/* <th>
                            <label className="sortable-column-header">Model ID</label>
                          </th>
                          <th>
                            <label className="sortable-column-header">API Key</label>
                          </th> */}
                          <th>
                            <label className="sortable-column-header">Action</label>
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {modelsList?.map((item: any, index: number) => {
                          return <ModelRegistryList item={item} key={index} />;
                        })}
                      </tbody>
                    </table>
                  </div>
                  {/* {malwareScanApiKeysList?.length ? (
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : (
                    ''
                  )} */}
                </React.Fragment>
              )}
            </div>
          </div>
        </div>
      </div>
      {info && (
        <InfoModal
          title={'About Model Registry'}
          modalWidth={'35vw'}
          show={info}
          content={contentForInfo}
          onCancel={onInfoModalCancel}
        />
      )}
    </React.Fragment>
  );
};

export default MyModelRegistry;
