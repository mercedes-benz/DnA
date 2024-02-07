import classNames from 'classnames';
import React from 'react';
import Styles from './DoraMetrics.scss';

const Tile = (props:any) => {
  const item = {...props.item};
  return (
    <div className={classNames(Styles.portTile)}>
      <div className={classNames(Styles.portTileVal)}>
        <div className={Styles.serviceIcon}>
          <i className={`icon mbc-icon ${item.icon}`} />
        </div>
        <div className={Styles.serviceName}>
          <span>
            {item.name}
          </span>
          <h5>{item.count}</h5>
        </div>
      </div>
    </div>
  )
}

const DoraMetrics = () => {
  const doraMetrics = [
    { 
      id: '1', 
      name: 'Lead Time to Change', 
      count: '1 day - 1 week',
      icon: 'document',
    },
    { 
      id: '2', 
      name: 'Deployment Frequency', 
      count: 'Weekly',
      icon: 'refresh',
    },
    { 
      id: '3', 
      name: 'Change Failure Rate', 
      count: '16-30%',
      icon: 'chronos',
    },
    { 
      id: '4', 
      name: 'Mean Time to Restore', 
      count: '< 1 hour',
      icon: 'provision',
    },
    { 
      id: '5', 
      name: 'Reliability Rate', 
      count: 'Frequently',
      icon: 'workspace',
    },
    { 
      id: '6', 
      name: 'Net Promoter Score', 
      count: 'Excellent (71-100)',
      icon: 'review-outline',
    },
  ];

  return (
    <div className={Styles.content}>
      <div className={classNames(Styles.portContentsection)}>
        <div className={classNames(Styles.portHeader)}>
          {doraMetrics.map(item =>
            <Tile item={item} key={item.id} />
          )}
        </div>
      </div>
    </div>
  );
}

export default DoraMetrics;