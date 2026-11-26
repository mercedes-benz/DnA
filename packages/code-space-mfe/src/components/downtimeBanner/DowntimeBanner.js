import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Modal from 'dna-container/Modal';
import Styles from './DowntimeBanner.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const POLL_INTERVAL_MS = 60000;
const BANNER_MESSAGE =
    'Codespaces may be temporarily disrupted due to ongoing service issues. We apologize for the inconvenience. Please avoid triggering or initiating new deployments during this period.';

const HELP_URL = '';

const ARGOCD_COMPONENT_PATTERN = /\b(argocd-[a-z-]+)\b/;

const getDisplayName = (d) => {
    const component = d.serviceName === 'ArgoCD / Cluster' ? d.message?.match(ARGOCD_COMPONENT_PATTERN)?.[1] : null;
    return component ? `${d.serviceName} (${component})` : d.serviceName;
};

const AFFECTED_FEATURES = {
    'GitHub Runner':
        'Creating new codespaces, pushing code, and running build and deploy pipelines may fail or be delayed.',
    'MB Artifactory':
        'Builds may fail because dependencies, packages and base images cannot be downloaded.',
    'ALICE Auth':
        'Permission checks may fail. Managing roles, entitlements and collaborators may not work.',
    SSO: 'Logging in may fail. Users who are already logged in may be signed out when their session expires.',
    Vault: 'Saving, reading or updating secrets and environment variables may fail. Builds and deployments that need secrets may also fail.',
    'ArgoCD / Cluster':
        'Deploying codespaces to staging or production may fail or be delayed, and the deployment status may not update.',
    'Harbor Registry':
        'Building and deploying codespaces may fail because container images cannot be pushed to or pulled from the registry.',
};

const toDate = (value) => {
    const date = value ? new Date(value) : null;
    return date && !isNaN(date.getTime()) ? date : null;
};

const formatDateTime = (value) => {
    const date = toDate(value);
    return date
        ? date.toLocaleString('en-GB', {
              day: '2-digit',
              month: 'short',
              year: 'numeric',
              hour: 'numeric',
              minute: '2-digit',
              hour12: true,
          })
        : 'N/A';
};

const formatTime = (value) => {
    const date = toDate(value);
    return date ? date.toLocaleTimeString('en-GB', { hour: 'numeric', minute: '2-digit', hour12: true }) : 'N/A';
};

const formatDownFor = (value) => {
    const date = toDate(value);
    if (!date) return null;
    const minutes = Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000));
    if (minutes < 1) return 'less than 1 min';
    if (minutes < 60) return `${minutes} min`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ${minutes % 60}m`;
    return `${Math.floor(hours / 24)}d ${hours % 24}h`;
};

const DowntimeBanner = () => {
    const [downtimes, setDowntimes] = useState([]);
    const [showDetails, setShowDetails] = useState(false);

    const fetchDowntimes = () => {
        if (document.hidden) return;
        CodeSpaceApiClient.getActiveDowntimes()
            .then((res) => setDowntimes(Array.isArray(res?.data) ? res.data : []))
            .catch(() => setDowntimes([]));
    };

    useEffect(() => {
        fetchDowntimes();
        const interval = setInterval(fetchDowntimes, POLL_INTERVAL_MS);
        document.addEventListener('visibilitychange', fetchDowntimes);
        return () => {
            clearInterval(interval);
            document.removeEventListener('visibilitychange', fetchDowntimes);
        };
    }, []);

    useEffect(() => {
        if (!downtimes.length) setShowDetails(false);
    }, [downtimes]);

    if (!downtimes.length) return null;

    const detailsContent = (
        <div className={classNames(Styles.detailsModal)}>
            {downtimes.map((d) => {
                const downFor = formatDownFor(d.detectedAt);
                const affected = AFFECTED_FEATURES[d.serviceName];
                return (
                    <div key={d.id} className={classNames(Styles.detailsItem)}>
                        <div className={classNames(Styles.itemHeader)}>
                            <h5>{getDisplayName(d)}</h5>
                            {downFor && <span className={classNames(Styles.downFor)}>Down for {downFor}</span>}
                        </div>
                        <p>{d.message}</p>
                        <div className={classNames(Styles.meta)}>
                            Down since: {formatDateTime(d.detectedAt)} &bull; Last checked: {formatTime(d.lastChecked)}
                        </div>
                        {affected && (
                            <div className={classNames(Styles.affected)}>
                                <strong>Affected:</strong> {affected}
                            </div>
                        )}
                    </div>
                );
            })}
            <div className={classNames(Styles.footer)}>
                <p className={classNames(Styles.refreshNote)}>
                    Status is checked every minute. This banner disappears automatically when the service recovers.
                </p>                {HELP_URL && (
                    <p>
                        Need help?{' '}
                        <a href={HELP_URL} target="_blank" rel="noopener noreferrer">
                            Contact the CodeSpaces team
                        </a>
                    </p>
                )}
            </div>
        </div>
    );

    return (
        <>
            <div className={classNames(Styles.banner)}>
                <i className="icon mbc-icon alert circle" />
                <p>
                    <strong>Service Downtime ({downtimes.map(getDisplayName).join(', ')}):</strong> {BANNER_MESSAGE}                </p>
                <button type="button" className={classNames(Styles.detailsLink)} onClick={() => setShowDetails(true)}>
                    More details
                </button>
            </div>
            {showDetails && (
                <Modal
                    title={'Service Downtime Details'}
                    hiddenTitle={false}
                    showAcceptButton={false}
                    showCancelButton={false}
                    modalWidth="640px"
                    show={showDetails}
                    content={detailsContent}
                    scrollableContent={true}
                    onCancel={() => setShowDetails(false)}
                />
            )}
        </>
    );
};

export default DowntimeBanner;
