import { useRef, useCallback } from 'react';
import { CodeSpaceApiClient } from '../apis/codespace.api';

export const useDeploymentStatus = () => {
  const eventSourceRef = useRef(null);

  const startListening = useCallback((projectName, environment, onStatusUpdate, onComplete, onError) => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const envMap = {
      'staging': 'int',
      'int': 'int',
      'production': 'prod',
      'prod': 'prod'
    };
    const mappedEnvironment = envMap[environment.toLowerCase()] || environment;

    // Start new SSE connection
    eventSourceRef.current = CodeSpaceApiClient.subscribeToDeploymentStatus(
      projectName,
      mappedEnvironment,
      (data) => {
        console.log('Deployment status update:', data);
        onStatusUpdate && onStatusUpdate(data);
      },
      (data) => {
        console.log('Deployment complete:', data);
        onComplete && onComplete(data);
        // Connection will be closed by the API client
        eventSourceRef.current = null;
      },
      (error) => {
        console.error('Deployment SSE error:', error);
        onError && onError(error);
        eventSourceRef.current = null;
      }
    );
  }, []);

  /**
   * Stop listening to deployment status updates
   */
  const stopListening = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
  }, []);

  return {
    startListening,
    stopListening
  };
};
