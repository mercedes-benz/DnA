import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './AdminNotifications.scss';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { NotificationApiClient } from '../../../../services/NotificationApiClient';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { Envs } from 'globals/Envs';
import Modal from 'components/formElements/modal/Modal';

interface IAdminNotificationProps {
  userId: string;
}

const SectionBreak = () => <div className={Styles.sectionBreak}></div>;

export const AdminNotifications = ({ userId }: IAdminNotificationProps) => {
  const [notifyTOU, setNotifyTOU] = useState(false);
  const [notificationMsg, setNotificationMsg] = useState('');
  const [showCustomNotification, setCustomNotification] = useState(false);

  const [notificationError, setNotificationError] = useState('');
  const [showInfoModal, setInfoModal] = useState(false);

  const StorageTOU = Envs.STORAGE_TOU_HTML;
  const termsOfUseUpdateMsg = `Storage terms of use has been updated to newer version. Please view [here](${
    StorageTOU.match(/href="([^"]*)"/)?.[1]
  })`;

  const validation = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (notificationMsg === '') {
      setNotificationError(errorMissingEntry);
      formValid = false;
    }
    return formValid;
  };

  const onCustomMessageSubmit = () => {
    if (validation()) {
      setCustomNotification(true);
    }
  };

  const onCustomNotificationAccept = () => {
    NotificationApiClient.createNotification(notificationMsg, userId)
      .then((res) => {
        Notification.show('Notification published sucessfully.');
        setNotificationMsg('');
        setCustomNotification(false);
      })
      .catch(() => {
        Notification.show('Error while publishing notification.', 'alert');
        setCustomNotification(false);
      });
  };

  const onTOUNotificationAccept = () => {
    NotificationApiClient.createNotification(termsOfUseUpdateMsg, userId)
      .then((res) => {
        Notification.show('Notification published sucessfully.');
        setNotifyTOU(false);
      })
      .catch(() => {
        Notification.show('Error while publishing notification.', 'alert');
        setNotifyTOU(false);
      });
  };

  const infoModalContent = (
    <div>
      <table className={Styles.markdownTable}>
        <thead>
          <tr>
            <th>Markdown</th>
            <th>HTML</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <code>This is a **Bold Text**</code>{' '}
            </td>
            <td>
              <code>This is a &lt;strong&gt;Bold Text&lt;/strong&gt;</code>
            </td>
          </tr>
          <tr>
            <td>
              <code>This is a __Bold Text__</code>{' '}
            </td>
            <td>
              <code>This is a &lt;strong&gt;Bold Text&lt;/strong&gt;</code>
            </td>
          </tr>
          <tr>
            <td>
              <code>This is a *Italics Text*</code>{' '}
            </td>
            <td>
              <code>This is a &lt;em&gt;Italics Text&lt;/em&gt;</code>
            </td>
          </tr>
          <tr>
            <td>
              <code>This is a _Italics Text_</code>{' '}
            </td>
            <td>
              <code>This is a &lt;em&gt;Italics Text&lt;/em&gt;</code>
            </td>
          </tr>
          <tr>
            <td>
              <code>Please [Click here](https://www.example.com)</code>{' '}
            </td>
            <td>
              <code>
                Please &lt;a href="https://www.example.com" target="_blank" rel="noopener noreferrer"&gt; Click here
                &lt;/a&gt;
              </code>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );

  return (
    <>
      <div className={Styles.mainPanel}>
        <h3>Notification to all users</h3>
        <div className={Styles.textAreaContainer}>
          <div
            className={classNames('input-field-group include-error', notificationError?.length ? 'error' : '')}
            style={{ minHeight: '160px' }}
          >
            <div className={Styles.infoIcon}>
              <i className="icon mbc-icon info" onClick={() => setInfoModal(true)} />
            </div>
            <textarea
              className="input-field-area"
              placeholder="Type here... eg: Please click [here](https://www.example.com)"
              value={notificationMsg}
              onChange={(e) => {
                setNotificationMsg(e.target.value);
                !e.target.value ? setNotificationError('*Missing entry') : setNotificationError('');
              }}
              required={true}
            />
            <span className={classNames('error-message', notificationError?.length ? '' : 'hide')}>
              {notificationError}
            </span>
          </div>
        </div>
        <div className={Styles.submitBtn}>
          <button className={'btn btn-tertiary '} type="button" onClick={onCustomMessageSubmit}>
            Submit
          </button>
        </div>
      </div>
      <SectionBreak />
      <div className={classNames(Styles.mainPanel, Styles.TOUSection)}>
        <h3>Notification to storage users</h3>
        <div className={Styles.BtnContainer}>
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
          onAccept={onTOUNotificationAccept}
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
          onAccept={onCustomNotificationAccept}
          onCancel={() => setCustomNotification(false)}
        />
      )}
      {showInfoModal && (
        <Modal
          title="Markdown formatter"
          show={showInfoModal}
          showAcceptButton={false}
          showCancelButton={false}
          content={infoModalContent}
          buttonAlignment="center"
          onCancel={() => setInfoModal(false)}
        />
      )}
    </>
  );
};
