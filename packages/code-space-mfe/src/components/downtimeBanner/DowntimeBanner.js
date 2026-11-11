import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DowntimeBanner.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const POLL_INTERVAL_MS = 60000;

const DowntimeBanner = () => {
    const [downtimes, setDowntimes] = useState([]);

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

    if (!downtimes.length) return null;

    return (
        <div className={classNames(Styles.banner)}>
            <div className={classNames(Styles.content)}>
                <div className={classNames(Styles.placeholder)}>
                    <i className="icon mbc-icon alert circle" />
                    <h5>Service Downtime:</h5>
                </div>
                <div className={classNames(Styles.messages)}>
                    {downtimes.map((d) => (
                        <p key={d.id}>
                            <strong>{d.serviceName}:</strong> {d.message}
                        </p>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default DowntimeBanner;
