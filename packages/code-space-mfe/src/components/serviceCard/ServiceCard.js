import classNames from 'classnames';
import React, { useState } from 'react';
import Modal from 'dna-container/Modal';
import Styles from './ServiceCard.scss';
import Notification from '../../common/modules/uilab/js/src/notification';

const ServiceCard = ({service}) => {
  const [showMoreInfo, setShowMoreInfo] = useState(false);  

  const handleCopyJson = (json) => {
    navigator.clipboard.writeText(JSON.stringify(json, null, 2)).then(() => {
      Notification.show('Copied to Clipboard');
    });
  }

  const moreInfoContent = (
    <div className={Styles.modal}>
      <div className={Styles.header}>
        <h3>{service?.serviceName} <span>v{service?.version}</span></h3>
        <div className={Styles.copyJSON} onClick={() => handleCopyJson(service?.additionalProperties)}><i className="icon mbc-icon copy"></i> Copy JSON</div>
      </div>
      <div className={Styles.content}>
        {service?.additionalProperties?.env &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Env(s)</p>
            {service?.additionalProperties?.env?.map(item => 
              <div key={item.name} className={classNames(Styles.item)}>
                <p className={classNames(Styles.name)}>
                  {item.name}
                </p>
                <p className={classNames(Styles.value)}>
                  {item.value}
                </p>
              </div>
            )}
          </div>
        }

        {service?.additionalProperties?.ports &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Port(s)</p>
            {service?.additionalProperties?.ports?.map((item, index) => {
              return ( 
                <div key={index} className={classNames(Styles.item)}>
                  <p className={classNames(Styles.name)}>
                    Protocol: {item.protocol}
                  </p>
                  <p className={classNames(Styles.value)}>
                    Container Port: {item.containerPort}
                  </p>
                </div>
              )
            })}
          </div>
        }
      </div>
      <div className={Styles.footer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={() => setShowMoreInfo(false)}
        >
          Okay
        </button>
      </div>
    </div>
  );

  return (
    <div className={classNames(Styles.serviceCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div className={classNames(Styles.cardHeadTitle)}>
            <i className="icon mbc-icon tools-mini"></i> {service?.serviceName} {service?.version}
            <span onClick={() => handleCopyJson(service?.additionalProperties)}><i className="icon mbc-icon copy"></i> Copy JSON</span>
          </div>
        </div>
      </div>
      <div className={Styles.cardBodySection}>
        {service?.additionalProperties?.env &&
          <div className={classNames(Styles.itemContainer)}>
            {service?.additionalProperties?.name && 
              <div className={classNames(Styles.item)}>
                <p className={classNames(Styles.name)}>
                  Name
                </p>
                <p className={classNames(Styles.value)}>
                  {service?.additionalProperties?.name}
                </p>
              </div>
            }
          </div>
        }

        {service?.additionalProperties?.env &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Env(s)</p>
            {service?.additionalProperties?.env?.slice(0, 4)?.map(item => 
              <div key={item.name} className={classNames(Styles.item)}>
                <p className={classNames(Styles.name)}>
                  {item.name}
                </p>
                <p className={classNames(Styles.value)}>
                  {item.value}
                </p>
              </div>
            )}
          </div>
        }

        {service?.additionalProperties?.ports &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Port(s)</p>
            {service?.additionalProperties?.ports?.map((item, index) => {
              return ( 
                <div key={index} className={classNames(Styles.item)}>
                  <p className={classNames(Styles.name)}>
                    Protocol: {item.protocol}
                  </p>
                  <p className={classNames(Styles.value)}>
                    Container Port: {item.containerPort}
                  </p>
                </div>
              )
            })}
          </div>
        }
      </div>
      <div className={Styles.cardFooter}>
        <button className={classNames('btn btn-primary', Styles.btnMore)} onClick={() => setShowMoreInfo(true)}>
          <i className="icon mbc-icon info"></i> More Info
        </button>
      </div>
      {showMoreInfo && 
        <Modal
          title={'Software Details'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showMoreInfo}
          content={moreInfoContent}
          scrollableContent={true}
          onCancel={() => setShowMoreInfo(false)}
        />
      }
    </div>
  );
};
export default ServiceCard;