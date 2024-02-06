import React from "react";
import Styles from '../connectionInfo/ConnectionModel.scss';
import { useState, useEffect } from "react";
import classNames from "classnames";
//import {useNavigate} from 'react-router-dom';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from "../../common/modules/uilab/js/src/progress-indicator";
import { datalakeApi } from "../../apis/datalake.api";

export const ConnectionModal = ({ projectId, onOkClick }) => {
    const [showSecretKey, setShowSecretKey] = useState(false);
    const [connectionInfo, setConnectionInfo] = useState();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Tabs.defaultSetup();
    }, []);

    useEffect(() => {
        ProgressIndicator.show();
        datalakeApi.getConnectionInfo(projectId)
        .then((res) => {
            setConnectionInfo(res.data.data);
            ProgressIndicator.hide();
            setLoading(false);
        })
        .catch((err) => {
            Notification.show(
            err?.response?.data?.errors?.[0]?.message || 'Error while fetching connection information',
            'alert',
            );
            ProgressIndicator.hide();
            setLoading(false);
        });
    }, [projectId]);
    
    const copyToClipboard = (content) => {
        navigator.clipboard.writeText('');
        navigator.clipboard.writeText(content).then(() => Notification.show('Copied to Clipboard'));
    };

    const connectUsingRESTAPI = (
        <>
            <div className={Styles.emptyDataikuProjectsList}>Coming Soon</div>
        </>
    );

    const connectToOData = (
        <>
            <div className={Styles.emptyDataikuProjectsList}>Coming Soon</div>
        </>
    );

    const connectToGraphQl = (
        <>
            <div className={Styles.emptyDataikuProjectsList}>Coming Soon</div>
        </>
    );

    const connectToTrino = (!loading && 
        <>
            <p className={classNames(Styles.dbHighlight)}>From DBeaver: Select the trino driver and enter the below details.</p> 
            <div className={classNames(Styles.trinoConnectDiv)}>
                <div>
                    <p><strong>For OIDC User:</strong></p>
                    <ul>
                        <li>Host: {connectionInfo.howToConnect.trino.userVO.hostName}</li>
                        <li>Port: {connectionInfo.howToConnect.trino.userVO.port}</li>
                        <li>
                            Username: {connectionInfo.howToConnect.trino.userVO.accesskey} 
                            <span onClick={() => copyToClipboard(connectionInfo.howToConnect.trino.userVO.accesskey)}>
                                <i className="icon mbc-icon copy" />
                            </span>
                        </li>
                        <li>
                            Password: 
                            {showSecretKey
                                ? connectionInfo.howToConnect.trino.userVO.secretKey
                                : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}

                            {showSecretKey ? (
                                <i
                                    className={classNames('icon mbc-icon visibility-hide ', Styles.showIcon)}
                                    onClick={() => setShowSecretKey(false)}
                                    tooltip-data="Hide"
                                />
                            ) : (
                                <i
                                    className={classNames('icon mbc-icon visibility-show ', Styles.showIcon)}
                                    onClick={() => setShowSecretKey(true)}
                                    tooltip-data="Show"
                                />
                            )}
                            <span  onClick={() => copyToClipboard(connectionInfo.howToConnect.trino.userVO.secretKey)}>
                                <i className="icon mbc-icon copy" />
                            </span>
                        </li>
                        <li>External Authentication: {connectionInfo.howToConnect.trino.userVO.externalAuthentication ? 'Yes' : 'No'}</li>
                    </ul>
                </div>
                <div>
                    <p><strong>For Technical User:</strong></p> 
                    <ul>
                        <li>Host: {connectionInfo.howToConnect.trino.techUserVO.hostName}</li>
                        <li>Port: {connectionInfo.howToConnect.trino.techUserVO.port}</li>
                        <li>
                            Access Key: {connectionInfo.howToConnect.trino.techUserVO.accesskey} 
                            <span onClick={() => copyToClipboard(connectionInfo.howToConnect.trino.techUserVO.accesskey)}>
                                <i className="icon mbc-icon copy" />
                            </span>
                        </li>
                        <li>
                            Client Secret: 
                            {showSecretKey
                                ? connectionInfo.howToConnect.trino.techUserVO.secretKey
                                : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}

                            {showSecretKey ? (
                                <i
                                    className={classNames('icon mbc-icon visibility-hide ', Styles.showIcon)}
                                    onClick={() => setShowSecretKey(false)}
                                    tooltip-data="Hide"
                                />
                            ) : (
                                <i
                                    className={classNames('icon mbc-icon visibility-show ', Styles.showIcon)}
                                    onClick={() => setShowSecretKey(true)}
                                    tooltip-data="Show"
                                />
                            )}
                            <span  onClick={() => copyToClipboard(connectionInfo.howToConnect.trino.techUserVO.secretKey)}>
                                <i className="icon mbc-icon copy" />
                            </span>
                        </li>
                    </ul>
                </div>
            </div>
            <hr />
            <p>By using trino <a href="https://trino.io/docs/current/develop/client-protocol.html" target="_blank" rel="noreferrer noopener">REST API Client</a></p>
        </>
    );

    const connectToParquet = (
        <>
            <div className={Styles.emptyDataikuProjectsList}>Coming Soon</div>
        </>
    );

    const connectToJupyterNotebook = (!loading &&
        <>
            <p>
                <strong>For OIDC User:</strong>
                <span
                    className={Styles.copyCodeIcon}
                    onClick={() => {
                        const content = document.getElementById('jupyterusercode')?.innerText;
                        copyToClipboard(content);
                    }}
                >
                    <i className="icon mbc-icon copy" />
                </span>
            </p>
            <code>
<pre id="jupyterusercode">
{`from trino.dbapi import connect
from trino.auth import OAuth2Authentication
conn = connect(
    user="${connectionInfo.howToConnect.trino.userVO.accesskey}",
    catalog="${connectionInfo.project.catalogName}",
    auth=OAuth2Authentication(),
    http_scheme="https",
    verify=False,
    host="${connectionInfo.howToConnect.trino.userVO.hostName}",
    port=${connectionInfo.howToConnect.trino.userVO.port},
    schema="${connectionInfo.project.schemaName}"
)
cur = conn.cursor()
cur.execute("YOUR_STATEMENT_TO_EXECUTE")
rows = cur.fetchall()
print(rows)`}
</pre>
            </code>
            <br/>
            <p>
                <strong>For Technical User:</strong>
                <span
                    className={Styles.copyCodeIcon}
                    onClick={() => {
                        const content = document.getElementById('jupytertechcode')?.innerText;
                        copyToClipboard(content);
                    }}
                >
                    <i className="icon mbc-icon copy" />
                </span>
            </p>
            <code>
<pre id="jupytertechcode">
{`from trino.dbapi import connect
from trino.auth import BasicAuthentication
conn = connect(
    host="${connectionInfo.howToConnect.trino.techUserVO.hostName}",
    auth=BasicAuthentication("${connectionInfo.howToConnect.trino.techUserVO.accesskey}", "XXXXXXX"),
    port=${connectionInfo.howToConnect.trino.techUserVO.port},
    user="${connectionInfo.howToConnect.trino.techUserVO.accesskey}",
    catalog="${connectionInfo.project.catalogName}",
    schema="${connectionInfo.project.schemaName}",
    verify=False
)
cur = conn.cursor()
cur.execute('SELECT * FROM YOUR_CATALOG.YOUR_PROJECT_SCHEMA.YOUR_TABLE')
rows = cur.fetchall()
print(rows)`}
</pre>
            </code>
        </>
    );

    return (
        <div>
            <div className={Styles.accessDetails}>
                <>
                    <h5>Access Details for Data Lakehouse</h5>
                </>
                {/* <table>
                    <tbody>
                        <tr>
                            <td>
                                <strong>Access Key :</strong>
                            </td>
                            <td id="accessKey" className={Styles.keys}>
                                {bucketInfo?.accessInfo?.accesskey}
                            </td>
                            <td>
                                <span className={Styles.copyIcon} onClick={() => copyToClipboard(bucketInfo?.accessInfo?.accesskey)}>
                                    <i className="icon mbc-icon copy" />
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <strong>Secret Key :</strong>
                            </td>
                            <td id="secretKey" className={Styles.keys}>
                                {showSecretKey
                                    ? bucketInfo?.accessInfo?.secretKey
                                    : Array.from({ length: 30 }, (_, i) => <React.Fragment key={i}>&bull;</React.Fragment>)}
                            </td>
                            <td>
                                {showSecretKey ? (
                                    <React.Fragment>
                                        <i
                                            className={classNames('icon mbc-icon visibility-hide ', Styles.visibilityIcon)}
                                            onClick={() => setShowSecretKey(false)}
                                            tooltip-data="Hide"
                                        />
                                    </React.Fragment>
                                ) : (
                                    <React.Fragment>
                                        <i
                                            className={classNames('icon mbc-icon visibility-show ', Styles.visibilityIcon)}
                                            onClick={() => setShowSecretKey(true)}
                                            tooltip-data="Show"
                                        />
                                    </React.Fragment>
                                )}
                                <span className={Styles.copyIcon} onClick={() => copyToClipboard(bucketInfo?.accessInfo?.secretKey)}>
                                    <i className="icon mbc-icon copy" />
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table> */}
                <div className={'tabs-panel'}>
                    <div className="tabs-wrapper">
                        <nav>
                            <ul className="tabs">
                                <li className={'tab active'}>
                                    <a href="#tab-content-1" id="restApi">
                                        <strong>Connect using Trino</strong>
                                    </a>
                                </li>
                                <li className={'tab'}>
                                    <a href="#tab-content-2" id="jupyterNotebook">
                                        <strong>Connect using Jupyter Notebook</strong>
                                    </a>
                                </li>
                                <li className={'tab'}>
                                    <a href="#tab-content-3" id="jupyterNotebook">
                                        <strong>Connect using REST</strong>
                                    </a>
                                </li>
                                <li className={'tab'}>
                                    <a href="#tab-content-4" id="jupyterNotebook">
                                        <strong>Connect using OData</strong>
                                    </a>
                                </li>
                                <li className={'tab'}>
                                    <a href="#tab-content-5" id="jupyterNotebook">
                                        <strong>Connect using GraphQL</strong>
                                    </a>
                                </li>
                                <li className={'tab'}>
                                    <a href="#tab-content-6" id="jupyterNotebook">
                                        <strong>Connect using Parquet</strong>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                    <div className={classNames('tabs-content-wrapper', Styles.tabsContentWrapper)}>
                        <div id="tab-content-1" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            <span
                                className={Styles.copyIcon}
                                onClick={() => {
                                    const content = document.getElementById('tab-content-1')?.innerText;
                                    copyToClipboard(content);
                                }}
                            >
                                <i className="icon mbc-icon copy" />
                            </span>
                            <div className={Styles.connectionCode}>{connectToTrino}</div>
                        </div>
                        <div id="tab-content-2" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            <div className={Styles.connectionCode}>{connectToJupyterNotebook}</div>
                        </div>
                        <div id="tab-content-3" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            <span
                                className={Styles.copyIcon}
                                onClick={() => {
                                    const content = document.getElementById('tab-content-3')?.innerText;
                                    copyToClipboard(content);
                                }}
                            >
                                <i className="icon mbc-icon copy" />
                            </span>
                            <div className={classNames(Styles.connectionCode, Styles.restAPIContent)}>{connectUsingRESTAPI}</div>
                        </div>
                        <div id="tab-content-4" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            {/* <span
                                className={Styles.copyIcon}
                                onClick={() => {
                                    const content = document.getElementById('tab-content-4')?.innerText;
                                    copyToClipboard(content);
                                }}
                            >
                                <i className="icon mbc-icon copy" />
                            </span> */}
                            <div className={Styles.connectionCode}>{connectToOData}</div>
                        </div>
                        <div id="tab-content-5" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            {/* <span
                                className={Styles.copyIcon}
                                onClick={() => {
                                    const content = document.getElementById('tab-content-5')?.innerText;
                                    copyToClipboard(content);
                                }}
                            >
                                <i className="icon mbc-icon copy" />
                            </span> */}
                            <div className={Styles.connectionCode}>{connectToGraphQl}</div>
                        </div>
                        <div id="tab-content-6" className={classNames('tab-content mbc-scroll', Styles.tabContentContainer)}>
                            <div className={Styles.connectionCode}>{connectToParquet}</div>
                        </div>
                    </div>
                </div>
                <button className={classNames('btn btn-primary', Styles.OkBtn)} onClick={onOkClick}>
                    OK
                </button>
            </div>
        </div>
    )
}