import { useRef, useCallback } from 'react';
import { CodeSpaceApiClient } from '../apis/codespace.api';
// @ts-ignore
import Notification from '../common/modules/uilab/js/src/notification';

const ENV_MAP = { staging: 'int', int: 'int', production: 'prod', prod: 'prod' };

export const useDeploymentStatus = () => {
  const eventSourceRef = useRef(null);

  const startListening = useCallback((projectName, environment, onStatusUpdate, onComplete, onError) => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }
    const mappedEnv = ENV_MAP[environment.toLowerCase()] || environment;
    eventSourceRef.current = CodeSpaceApiClient.subscribeToDeploymentStatus(
      projectName,
      mappedEnv,
      (data) => { onStatusUpdate && onStatusUpdate(data); },
      (data) => { onComplete && onComplete(data); eventSourceRef.current = null; },
      (error) => { onError && onError(error); eventSourceRef.current = null; }
    );
  }, []);

  const stopListening = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
  }, []);

  return { startListening, stopListening };
};

/**
 * Reusable SSE handler factory for deploy flows.
 * Refreshes card data and shows notifications on status changes.
 */
export const createDeployHandlers = (codeSpaceId, refreshCallback) => {
  const refreshCard = () => {
    CodeSpaceApiClient.getWorkspaceById(codeSpaceId)
      .then((res) => { if (res.data && refreshCallback) refreshCallback(codeSpaceId, res.data); })
      .catch(() => {});
  };

  return {
    onStatusUpdate: () => refreshCard(),
    onComplete: (data) => {
      const status = data?.currentStatus || data?.status;
      const name = data?.projectName || 'Workspace';
      const isFailed = status === 'FAILED' || status === 'ERROR' || status === 'DEPLOYMENT_FAILED';
      Notification.show(
        isFailed ? `Deployment failed for ${name}` : `Deployment completed successfully for ${name}`,
        isFailed ? 'alert' : undefined
      );
      refreshCard();
    },
    onError: () => {
      Notification.show('Real-time deployment updates disconnected. Please refresh manually.', 'alert');
    }
  };
};
