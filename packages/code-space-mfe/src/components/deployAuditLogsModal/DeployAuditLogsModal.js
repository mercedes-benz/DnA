import classNames from 'classnames';
import React from 'react';
import Styles from './DeployAuditLogsModal.scss';
import Modal from 'dna-container/Modal';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';

// interface DeployAuditLogsModalProps {
//   deployedEnvInfo: string;
//   show: boolean;
//   setShowAuditLogsModal: (show: boolean) => void;
//   logsList: Array<any>;
// }

const DeployAuditLogsModal = (props) => {
  return (
    <Modal
      title={'Deployment Audit Logs - ' + props.deployedEnvInfo}
      hiddenTitle={false}
      showAcceptButton={false}
      showCancelButton={false}
      modalWidth={'70%'}
      modalStyle={{ minHeight: '86%' }}
      buttonAlignment="center"
      show={props.show}
      content={
        <>
          <div className={classNames(Styles.allCodeSpace)}>
            <div className={classNames(Styles.allcodeSpaceListviewContent)}>
              <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                <thead>
                  <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                    <th>
                      <label>Branch</label>
                    </th>
                    <th>
                      <label>Deployed On</label>
                    </th>
                    <th>
                      <label>Triggered By</label>
                    </th>
                    <th>
                      <label>Triggered On</label>
                    </th>
                    <th>
                      <label>Deployment Status</label>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {props.logsList &&
                    props.logsList.map((item, index) => (
                      <tr className={classNames('data-row')} key={index}>
                        <td>{item.branch}</td>
                        <td>{item.deployedOn && regionalDateAndTimeConversionSolution(item.deployedOn)}</td>
                        <td>{item.triggeredBy}</td>
                        <td>{regionalDateAndTimeConversionSolution(item.triggeredOn)}</td>
                        <td>{item.deploymentStatus}</td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      }
      scrollableContent={true}
      onCancel={() => props.setShowAuditLogsModal(false)}
    />
  );
};

export default DeployAuditLogsModal;
