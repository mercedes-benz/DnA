import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './usageStatistics.scss';
import Caption from '../shared/caption/Caption';
import { ApiClient } from '../../../services/ApiClient';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { DataProductFilterApiClient } from '../../../services/DataProductFilterApiClient';
import { ReportsApiClient } from '../../../services/ReportsApiClient';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
import { ChronosApiClient } from '../../../services/ChronosApiClient';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

const UsageTile = (props:any) => {
  const item = {...props.item};
  return (
    <div className={classNames(Styles.portTile)}>
      <div className={classNames(Styles.portTileVal)}>
        <div className={Styles.serviceIcon}>
          <i className={`icon mbc-icon ${item.icon}`} />
        </div>
        <div className={Styles.serviceName}>
          <span>
            {new Intl.NumberFormat(navigator.language).format(Number(item.count))}
          </span>
          <h5>{item.name}</h5>
        </div>
      </div>
    </div>
  )
}
const UsageStatistics = () => {
  const [usageStatistics, setUsageStatistics] = useState([]);
  const [notebookTransparency, setNotebookTransparency] = useState(0);
  const [solutionTransparency, setSolutionTransparency] = useState(0);
  const [userTransparency, setUserTransparency] = useState(0);
  const [dataProductTransparency, setDataProductTransparency] = useState(0);
  const [dataTransferTransparency, setDataTransferTransparency] = useState(0);
  const [reportTransparency, setReportTransparency] = useState(0);
  const [chronosTransparency, setChronosTransparency] = useState({ projectCount: 0, userCount: 0});
  const [codeSpaceTransparency, setCodeSpaceTransparency] = useState(0);

  useEffect(() => {
    ProgressIndicator.show();
    ApiClient.getNotebooksTransparency()
      .then((res) => {
        setNotebookTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting notebooks transparency', 'alert');
      });
    
    ApiClient.getSolutionsTransparency()
      .then((res) => {
        setSolutionTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting solutions transparency', 'alert');
      });

    ApiClient.getUsersTransparency()
      .then((res) => {
        setUserTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting users transparency', 'alert');
      });

    DataProductFilterApiClient.getDataProductsTransparency()
      .then((res) => {
        setDataProductTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting data products transparency', 'alert');
      });

    DataProductFilterApiClient.getDataTransfersTransparency()
      .then((res) => {
        setDataTransferTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting data transfers transparency', 'alert');
      });

    ReportsApiClient.getReportsTransparency()
      .then((res) => {
        setReportTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting reports transparency', 'alert');
      });

    CodeSpaceApiClient.getWorkSpacesTransparency()
      .then((res) => {
        setCodeSpaceTransparency(res.count);
      })
      .catch(() => {
        Notification.show('Error while getting code spaces transparency', 'alert');
      });

    ChronosApiClient.getChronosTransparency()
      .then((res) => {
        setChronosTransparency(res);
      })
      .catch(() => {
        Notification.show('Error while getting chronos transparency', 'alert');
      });
    ProgressIndicator.hide();
  }, []);

  useEffect(() => {
    const usageStatisticsInitial = [
      {
        category: 'Users',
        data: [
          { 
            id: '1', 
            name: 'DnA Users', 
            count: userTransparency,
            icon: 'profile',
          },
          { 
            id: '3', 
            name: 'Chronos Users', 
            count: chronosTransparency.userCount,
            icon: 'chronos',
            path: '/chronos'
          },
        ]
      },
      {
        category: 'Transparency',
        data: [
          { 
            id: '7', 
            name: 'Solutions Count', 
            count: solutionTransparency,
            icon: 'solutionoverview',
            path: '/allsolutions'
          },
          { 
            id: '8', 
            name: 'Reports Count', 
            count: reportTransparency,
            icon: 'reports',
            path: '/allreports'
          },
        ]
      },
      {
        category: 'Data',
        data: [
          { 
            id: '4', 
            name: 'Published Data Products', 
            count: dataProductTransparency,
            icon: 'dataproductoverview',
            path: '/data'
          },
          { 
            id: '5', 
            name: 'Minimum Information / Sharings', 
            count: dataTransferTransparency,
            icon: 'datasharing',
            path: '/data'
          }
        ]
      },
      {
        category: 'Tools',
        data: [
          { 
            id: '2', 
            name: 'Chronos Projects', 
            count: chronosTransparency?.projectCount,
            icon: 'chronos',
            path: '/chronos'
          },
          { 
            id: '6', 
            name: 'Jupyter Notebooks', 
            count: notebookTransparency,
            icon: 'jupyter',
            path: '/notebook'
          },
          { 
            id: '9',
            name: 'CodeSpaces', 
            count: codeSpaceTransparency,
            icon: 'codespace',
            path: '/codespaces'
          },
        ]
      },
    ];
    setUsageStatistics([...usageStatisticsInitial]);
  }, [userTransparency, chronosTransparency, dataProductTransparency, dataTransferTransparency, notebookTransparency, solutionTransparency, reportTransparency, codeSpaceTransparency]);

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={Styles.wrapper}>
        <Caption title="Usage Statistics" />
      </div>
      <div className={Styles.content}>
        <div className={classNames(Styles.portContentsection)}>
          
            {usageStatistics.map(item =>
              <div className={Styles.serviceSeparator} key={item.category}>
                <h5>{item.category}</h5>
                <div className={classNames(Styles.portHeader)}>
                  {item.data.map((service:any) => <UsageTile item={service} key={service.id} />)}
                </div>
              </div>
            )}
        </div>
      </div>
    </div>
  );
};

export default UsageStatistics;
