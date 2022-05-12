import React, { useState } from 'react';
import Styles from './AdminNotifications.scss';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';

export const AdminNotifications = () => {
  const [notifyTOU, setNotifyTOU] = useState(false);
  const [showCustomNotification, setCustomNotification] = useState(false);

  return (
    <>
      <div className={Styles.mainPanel}>
        <h3>Notification to all users</h3>
        <div style={{ marginTop: '30px', background: '#252a33' }}>
          <div className={'input-field-group'}>
            <textarea className="input-field-area" required={true} />
            <button
              className={'btn btn-tertiary '}
              type="button"
              onClick={() => setCustomNotification(true)}
              style={{ float: 'right', margin: '10px 0px' }}
            >
              Submit
            </button>
          </div>
        </div>
      </div>
      <div style={{ background: '#1c2026', height: 15 }}></div>
      <div className={Styles.mainPanel} style={{ marginTop: '25px', background: '#252a33' }}>
        <h3>Notification to storage users</h3>
        <div style={{ marginTop: '30px' }}>
          <button className={'btn btn-tertiary'} onClick={() => setNotifyTOU(true)}>
            Send notification to all users about change in storage buckets terms of use
          </button>
        </div>
      </div>
      {notifyTOU && (
        <ConfirmModal
          title="Notification"
          acceptButtonTitle="Confirm"
          cancelButtonTitle="Cancel"
          show={notifyTOU}
          showAcceptButton={true}
          showCancelButton={true}
          content={
            <div>
              Are you sure you want to send notification to all users about update in storage{' '}
              <strong>terms of use</strong>{' '}
            </div>
          }
          onAccept={() => setNotifyTOU(false)}
          onCancel={() => setNotifyTOU(false)}
        />
      )}
      {showCustomNotification && (
        <ConfirmModal
          title="Notification"
          acceptButtonTitle="Confirm"
          cancelButtonTitle="Cancel"
          show={showCustomNotification}
          showAcceptButton={true}
          showCancelButton={true}
          content={<div>Are you sure you want to send notification to all users</div>}
          onAccept={() => setCustomNotification(false)}
          onCancel={() => setCustomNotification(false)}
        />
      )}
    </>
  );
};
