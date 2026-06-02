import React, { useState, useEffect } from 'react';
import ConfirmModal from 'dna-container/ConfirmModal';
import { Envs } from '../../Utility/envs';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import Styles from './IntMigrationModal.scss';

const IntMigrationModal = ({ show, codeSpaceData, onDismiss }) => {
  const [copied, setCopied] = useState(false);
  useEffect(() => {
  }, [show]);

  const projectDetails = codeSpaceData?.projectDetails;
  const projectName = projectDetails?.projectName?.toLowerCase();
  const intDeploymentDetails = projectDetails?.intDeploymentDetails;
  const deployedAppUrl = intDeploymentDetails?.deploymentUrl || '';
  const currentHost = projectName + '-int.' + (Envs.CODESERVER_APP_NAMESPACE || '');
  // eslint-disable-next-line no-unused-vars
  const lastDeployedOn = regionalDateAndTimeConversionSolution(intDeploymentDetails?.lastDeployedOn);

  const onCopy = () => {
    const text = 'Project Name: ' + projectName + '\n' +
      'Deployed App URL: ' + deployedAppUrl + '\n' +
      'Current Host: ' + currentHost;
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const handleDismiss = () => {
    setCopied(false);
    onDismiss();
  };

  return (
    <ConfirmModal
      title={''}
      acceptButtonTitle="OK"
      showAcceptButton={true}
      showCancelButton={false}
      showIcon={false}
      modalWidth="35%"
      show={show}
      content={
        <div className={Styles.intMigrationWarning}>
          <div style={{ textAlign: 'center', marginBottom: '16px' }}>
            <svg width="67px" height="67px" viewBox="0 0 67 67" version="1.1">
              <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
                <circle stroke="red" strokeWidth="2" cx="32.5" cy="32.5" r="32.5" />
                <text fill="red" fontFamily="var(--font-family)" fontSize="30" fontWeight="normal">
                  <tspan x="28.76" y="41">!</tspan>
                </text>
              </g>
            </svg>
          </div>
          <p>
            <span style={{ color: 'red', fontSize: '18px', marginRight: '6px' }}>⚠</span>
            As part of standardization and easier management of application deployments in DnA, we have initiated a minor change to separate int applications into different namespaces (isolation). This requires a small adjustment on the DnA Team&apos;s side while you are deploying in the staging environment only.
          </p>
          <p style={{ color: 'red', fontWeight: 'bold' }}>
            Note: Production does not require any changes. This notification will only be displayed once.
            Please save the details and contact link below for future reference.
          </p>
          <p>
            Please send below information to the{' '}
            <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">DnA Team</a>{' '}
            before proceeding with the Staging Deployment.
          </p>
          <div className={Styles.copySection}>
            <div className={Styles.copySectionHeader}>
              <strong>Namespace Migration Details</strong>
              <button
                className="btn btn-secondary"
                style={{ padding: '4px 12px', fontSize: '12px' }}
                onClick={onCopy}
              >
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
            <div className={Styles.copySectionDetails}>
              <div>Project Name: {projectName}</div>
              <div>Deployed App URL: {deployedAppUrl}</div>
              <div>Current Host: {currentHost}</div>
            </div>
          </div>
          <p>
            <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">Contact Codespace Team</a>
          </p>
        </div>
      }
      onCancel={handleDismiss}
      onAccept={handleDismiss}
    />
  );
};

export const needsIntMigration = (codeSpaceData) => {
  const intDeploymentDetails = codeSpaceData?.projectDetails?.intDeploymentDetails;
  const lastDeployedOn = intDeploymentDetails?.lastDeployedOn;
  const migrationCutoff = Envs.CODESPACE_INT_MIGRATION_CUTOFF;
  const projectName = codeSpaceData?.projectDetails?.projectName;

  if (!migrationCutoff || !lastDeployedOn || !projectName) return false;

  const cutoffDate = new Date(migrationCutoff);
  const deployedDate = new Date(lastDeployedOn);
  const storageKey = 'intMigrationDismissed_' + projectName;

  return deployedDate < cutoffDate && !localStorage.getItem(storageKey);
};

export default IntMigrationModal;
