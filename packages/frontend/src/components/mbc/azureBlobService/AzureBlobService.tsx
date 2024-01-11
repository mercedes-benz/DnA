import React from 'react';
import Styles from './AzureBlobService.scss';
import Caption from '../shared/caption/Caption';
import { Envs } from 'globals/Envs';
const AzureBlobService = () => {
  const src = Envs.APP_URL + '/azure-blob-scan/src.yaml';
  const dest = Envs.APP_URL + '/azure-blob-scan/dest.yaml';
  const pvc = Envs.APP_URL + '/azure-blob-scan/pvc.yaml';

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <Caption title="Malware Azure Blob Scanner Service" />
        <div className={Styles.content}>
          <h3>Clamav Service to scan Azure blob</h3>
          <div className={Styles.info}>
            <p>
              <b>Prerequisites:</b>
            </p>
            <ul>
              <li>Kubernetes Cluster with root permission to run container</li>
              <li>Azure Storage Account</li>
            </ul>
            <p>
              <b>Installation:</b>
            </p>
            <p>
              <b>Docker</b>
            </p>
            <ol>
              <li>
                Create secret for source and destination container.
                <ol type="i">
                  <li>
                    Source container configuration: Goto the file{' '}
                    <a target="_blank" href={src} rel="noreferrer" download="src.yaml">
                      src.yaml
                    </a>{' '}
                    and replace <em>namespace-name, account-name,
                    account-key, container-name</em> with your source account
                    configuration.
                  </li>
                  <li>
                    Destination container configuration: Goto the file{' '}
                    <a target="_blank" href={dest} rel="noreferrer" download="dest.yaml">
                      dest.yaml
                    </a>{' '}
                    and replace <em>namespace-name, account-name,
                      account-key, container-name</em> with your
                    destination account configuration.
                  </li>
                </ol>
              </li>
              <li>
                Run docker by mounting the src.yaml and dest.yaml
                <pre>
                  <code>
                    <span>docker run -v src.yaml:/az-secret/src.yaml \</span>
                    <br />
                    <span>    -v dest.yaml:/az-secret/dest.yaml \</span>
                    <br />
                    <span>    -p 8080:8080 \</span>
                    <br />
                    <span>    --cap-add SYS_ADMIN --device /dev/fuse \</span>
                    <br/>
                    <span>    {Envs.CLAMAV_IMAGE_URL}</span>
                  </code>
                </pre>
              </li>
            </ol>
            <p>Kubernetes</p>
            <ol>
              <li>
                Create a storage. Goto the file{' '}
                <a target="_blank" href={pvc} rel="noreferrer" download="pvc.yaml">
                  pvc.yaml
                </a>{' '}
                and replace <em>namespace-name</em> with your namespace and create it
                <br />
                <code>
                  <span>kubectl apply -f pvc.yaml</span>
                </code>
              </li>
              <li>
                Create secret for source and destination container
                <ol type="i">
                  <li>
                    Source container configuration: Goto the file{' '}
                    <a target="_blank" href={src} rel="noreferrer" download="src.yaml">
                      src.yaml
                    </a>{' '}
                    and replace <em>namespace-name, account-name, 
                      account-key, container-name </em> with your source account
                    configuration.
                  </li>
                  <li>
                    Destination container configuration: Goto the file{' '}
                    <a target="_blank" href={dest} rel="noreferrer" download="dest.yaml">
                      dest.yaml
                    </a>{' '}
                    and replace <em>namespace-name, 
                    account-name, account-key, container-name </em>
                    with your destination account configuration. Create the secrets:
                    <code>
                      <span>kubectl create secret generic az-secret</span>
                      <br />
                      <span>--from-file=src.yaml</span>
                      <br />
                      <span>--from-file=dest.yaml</span>
                    </code>
                  </li>
                </ol>
              </li>
              <li>
                To deploy the clamav service. Goto the file
                <a target="_blank" href={dest} rel="noreferrer" download="dest.yaml">
                  {' '}
                  dest.yaml
                </a>{' '}
                and replace <em>namespace-name</em> with your namespace and create it
                <br />
                <code>
                  <span>kubectl apply -f deployment.yaml</span>
                </code>
              </li>
            </ol>
            <p>
              <b>Usage:</b>
            </p>
            <ul>
              <li>
                Scan a azure blob
                <br />
                <code>
                  <span>
                    &ensp;curl --location 'http://localhost:8080/scan?filePath=/test-folder&filename=kubernetes.png'
                    --header 'apiToken: <em>api token key</em>'
                  </span>
                </code>
                <br />
                Modify the filepath, filename according to your needs and use valid apiToken. For the apiToken contact
                the team.
              </li>
              <li>
                Update Virus Database
                <br />
                <code>
                  <span>
                    &ensp;curl --location 'http://localhost:8080/updateVirusDatabase' --header 'apiToken:
                    <em>api token key</em>'
                  </span>
                </code>
                <br />
                Use valid apiToken. For the apiToken contact the team.
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AzureBlobService;
