import React, { useState } from 'react';
import ConfirmModal from 'dna-container/ConfirmModal';
import { Envs } from '../../Utility/envs';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';

const IntMigrationModal = ({ show, codeSpaceData, onDismiss }) => {
  const [copied, setCopied] = useState(false);

  const projectDetails = codeSpaceData?.projectDetails;
  const projectName = projectDetails?.projectName?.toLowerCase();
  const clusterEnv = Envs.CODE_SERVER_GIT_ENVREF || '';
  const intDeploymentDetails = projectDetails?.intDeploymentDetails;
  const deployedAppUrl = intDeploymentDetails?.deploymentUrl || '';
  const currentHost = projectName + '-int.' + (Envs.CODESERVER_APP_NAMESPACE || '');
  const newHost = projectName + '-' + clusterEnv + '-int.prod-dna-cs-apps';
  const lastDeployedOn = regionalDateAndTimeConversionSolution(intDeploymentDetails?.lastDeployedOn);

  const onCopy = () => {
    const text = 'Project Name: ' + projectName + '\n' +
      'Deployed App URL: ' + deployedAppUrl + '\n' +
      'Current Host: ' + currentHost + '\n' +
      'New Host: ' + newHost;
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const handleDismiss = () => {
    if (projectName) {
      localStorage.setItem('intMigrationDismissed_' + projectName, 'true');
    }
    setCopied(false);
    onDismiss();
  };

  return (
    <ConfirmModal
      title={'Staging Environment Migration Notice'}
      acceptButtonTitle="OK, I understand"
      showAcceptButton={true}
      showCancelButton={false}
      modalWidth="50%"
      show={show}
      content={
        <div>
          <p>
            Your workspace <strong>{projectName}</strong> has an existing staging(int) deployment
            (last deployed on <strong>{lastDeployedOn}</strong>) since the last staging deployed
            app (<a href={deployedAppUrl} target="_blank" rel="noopener noreferrer">{deployedAppUrl}</a>) needs
            to be migrated to a new namespace and we need your support.
          </p>
          <p>
            Please send the below information to the{' '}
            <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">DnA Team</a>{' '}
            before proceeding with the Staging Deployment.
          </p>
          <div style={{ background: '#1e1e1e', padding: '12px 16px', borderRadius: '4px', position: 'relative', marginBottom: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <strong>Namespace Migration Details</strong>
              <button
                className="btn btn-secondary"
                style={{ padding: '4px 12px', fontSize: '12px' }}
                onClick={onCopy}
              >
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
            <div style={{ fontFamily: 'monospace', fontSize: '13px', lineHeight: '1.6' }}>
              <div>Project Name: {projectName}</div>
              <div>Deployed App URL: {deployedAppUrl}</div>
              <div>Current Host: {currentHost}</div>
              <div>New Host: {newHost}</div>
            </div>
          </div>
          <p>
            <strong>Note:</strong> This notification will only be displayed once.
            Please save the details and contact link below for future reference.
          </p>
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
