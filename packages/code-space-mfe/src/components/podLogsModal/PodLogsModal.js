import React, { useState, useEffect, useRef } from 'react';
import Styles from './PodLogsModal.scss';
import Modal from 'dna-container/Modal';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const PodLogsModal = (props) => {
  const { projectName, environment, show, onClose, envLabel } = props;
  const [pods, setPods] = useState([]);
  const [logLines, setLogLines] = useState([]);
  const [status, setStatus] = useState('connecting'); // connecting, live, completed, error
  const [errorMessage, setErrorMessage] = useState('');
  const [logCount, setLogCount] = useState(0);
  const sseRef = useRef(null);
  const logsEndRef = useRef(null);

  useEffect(() => {
    if (show && projectName && environment) {
      setStatus('connecting');
      setLogLines([]);
      setPods([]);
      setErrorMessage('');
      setLogCount(0);

      sseRef.current = CodeSpaceApiClient.subscribeToPodLogs(
        projectName,
        environment,
        (podInfoData) => {
          setPods(podInfoData.pods || []);
          setStatus('live');
        },
        (podLogData) => {
          if (podLogData.logs && podLogData.logs.length > 0) {
            setLogLines((prev) => {
              const updated = [...prev, ...podLogData.logs];
              setLogCount(updated.length);
              return updated;
            });
          }
        },
        () => {
          setStatus('completed');
        },
        (errorData) => {
          setErrorMessage(errorData?.message || 'Failed to fetch pod logs');
          setStatus('error');
        },
      );
    }

    return () => {
      if (sseRef.current) {
        sseRef.current.close();
        sseRef.current = null;
      }
    };
  }, [show, projectName, environment]);

  useEffect(() => {
    if (logsEndRef.current) {
      logsEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [logLines]);

  const getStatusLabel = () => {
    switch (status) {
      case 'connecting':
        return 'Connecting...';
      case 'live':
        return 'Live';
      case 'completed':
        return 'Stream ended';
      case 'error':
        return 'Error';
      default:
        return '';
    }
  };

  const getDotClass = () => {
    switch (status) {
      case 'live':
        return Styles.live;
      case 'error':
        return Styles.error;
      default:
        return Styles.stopped;
    }
  };

  return (
    <Modal
      title={`Live Deployment Logs - ${envLabel || environment}`}
      hiddenTitle={false}
      showAcceptButton={false}
      showCancelButton={false}
      modalWidth={'70%'}
      modalStyle={{ minHeight: '60%' }}
      buttonAlignment="center"
      show={show}
      content={
        <div className={Styles.podLogsModal}>
          {pods.length > 0 && (
            <div className={Styles.podInfo}>
              {pods.map((pod, idx) => (
                <span key={idx}>
                  <span className={Styles.podName}>{pod.name}</span>
                  <span className={Styles.podStatus}>({pod.status})</span>
                  {idx < pods.length - 1 && ', '}
                </span>
              ))}
            </div>
          )}
          <div className={Styles.logsContainer}>
            {status === 'connecting' && (
              <div className={Styles.connecting}>Connecting to pod logs...</div>
            )}
            {status === 'error' && logLines.length === 0 && (
              <div className={Styles.errorMsg}>{errorMessage}</div>
            )}
            {logLines.length > 0 &&
              logLines.map((line, idx) => (
                <div key={idx} className={Styles.logLine}>
                  {line}
                </div>
              ))}
            {status === 'live' && logLines.length === 0 && (
              <div className={Styles.noLogs}>Waiting for logs...</div>
            )}
            <div ref={logsEndRef} />
          </div>
          <div className={Styles.statusBar}>
            <div className={Styles.statusIndicator}>
              <span className={`${Styles.dot} ${getDotClass()}`}></span>
              <span>{getStatusLabel()}</span>
            </div>
            <span>{logCount} log lines</span>
          </div>
        </div>
      }
      scrollableContent={false}
      onCancel={onClose}
    />
  );
};

export default PodLogsModal;
