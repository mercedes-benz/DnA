import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './request-workspace.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import { fabricApi } from '../../apis/fabric.api';
import RoleCard from '../roleCard/RoleCard';

const WorkspaceItem = ({ workspace, onRequest }) => {
  return (
    <div className={Styles.workspaceItemContainer}>
      <div className={Styles.workspaceIcon}>
        <i className="icon mbc-icon tools-mini"></i>
      </div>
      <div className={Styles.workspaceContent}>
        <h3>{workspace?.name}</h3>
        <div className={Styles.workspaceMetadata}>
          <p>Created by: {workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName} | {workspace?.createdOn && <>Created on: {regionalDateAndTimeConversionSolution(workspace?.createdOn)}</>}</p>
        </div>
      </div>
      <div className={Styles.workspaceAction}>
        <button className="btn btn-primary" onClick={() => onRequest(workspace)}>Request Workspace</button>
      </div>
    </div>
  )
}

const RequestWorkspace = ({ onRefresh }) => {
  const [workspaces, setWorkspaces] = useState([]);
  const [filteredWorkspaces, setFilteredWorkspaces] = useState([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState({});
  const [roleList, setRoleList] = useState([]);
  const [reason, setReason] = useState('');
  const [reasonError, setReasonError] = useState(true);
  const [currentStep, setCurrentStep] = useState('workspace-selection');

  useEffect(() => {
    ProgressIndicator.show();
      fabricApi
        .getFabricWorkspaceLov()
        .then((res) => {
          if(res.status !== 204) {
            setWorkspaces(res?.data?.records);
          } else {
            setWorkspaces([]);
          }
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching fabric workspaces failed!',
            'alert',
          );
        });
  }, []);

  const handleWorkspaceSearch = (e) => {
    const search = e.target.value;
    const filteredWorkspacesTemp = workspaces.filter(workspace =>
      workspace.name.toLowerCase().includes(search.toLowerCase())
    );
    setFilteredWorkspaces(filteredWorkspacesTemp);
  }

  useEffect(() => {
    reason.length > 20 ? setReasonError(false) : setReasonError(true); 
  }, [reason]);

  const handleAddRole = (role) => {
    const roleListTemp = roleList.filter(roleTemp => roleTemp.roleID !== role.roleID);
    setRoleList([...roleListTemp, {...role}]);
  }

  const handleWorkspaceRequest = (workspace) => {
    setSelectedWorkspace(workspace);
    setCurrentStep('role-selection');
  }

  const handleSubmit = () => {
    const data = {
      roleList,
      reason
    }

    fabricApi.requestRoles(selectedWorkspace?.id, data).then(() => {
      ProgressIndicator.hide();
      onRefresh();
      Notification.show('Fabric Workspace successfully requested');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while requesting fabric workspace',
        'alert',
      );
    });
  }

  return (
    <div className={classNames(Styles.form)}>
      <div className={Styles.formHeader}>
        <h3>Request Fabric Workspace</h3>
        <p>Search a workspace and request</p>
      </div>
      {/* Top navigation */}
      <div>
        <div className={Styles.stepsContainer}>
          <div className={classNames(Styles.step, (currentStep === 'workspace-selection' || currentStep === 'role-selection' || currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={Styles.stepIcon}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Workspace Selection</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'role-selection' || currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={Styles.stepIcon}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Role Selection</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={Styles.stepIcon}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Reason</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'summary') && Styles.complete)}>
            <div className={Styles.stepIcon}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Summary</div>
          </div>
        </div>
      </div>
      {/* Search and request workspace */}
      {currentStep === 'workspace-selection' && 
        <div className={Styles.searchContainer}>
          <h3>Select Workspace</h3>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group', Styles.searchBox)}>
                {/* <label className={'input-label'}>
                  Search Workspaces
                </label> */}
                <input
                  type="text"
                  className={'input-field'}
                  id="workspaceName"
                  placeholder="Search for Workspace Name"
                  autoComplete="off"
                  maxLength={256}
                  onChange={handleWorkspaceSearch}
                />
                <button className={classNames('btn', Styles.searchBtn)} onClick={() => console.log('search')}><i className="icon mbc-icon search"></i></button>
              </div>
            </div>
          </div>
          {/* No workspaces */}
          {filteredWorkspaces.length === 0 &&
            <div className={Styles.noWorkspaces}>
              <i className="icon mbc-icon search"></i> <span>Search for workspaces</span>
            </div>
          }
          <div className={Styles.workspaceList}>
            {filteredWorkspaces.map(workspace => <WorkspaceItem key={workspace?.id} workspace={workspace} onRequest={handleWorkspaceRequest} />)}
          </div>
        </div>
      }
      {/* Select Role */}
      {currentStep === 'role-selection' &&
        <div className={Styles.rolesContainer}>
          <h3>Select Role(s)</h3>
          <div className={Styles.flex}>
            {selectedWorkspace?.status?.roles?.map(role => 
              <RoleCard key={role.id} role={role} onAdd={handleAddRole} />
            )}
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={() => setCurrentStep('reason')}
            >
              Next
            </button>
          </div>
        </div>
      }
      
      {/* Fill reason */}
      {currentStep === 'reason' &&
        <div className={Styles.rolesForm}>
          <h3>Reason</h3>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              {/* <div className={classNames('input-field-group include-error area', errors.reason ? 'error' : '')}> */}
              <div className={classNames('input-field-group include-error area')}>
                <label id="reason" className="input-label" htmlFor="reason">
                  Reason <sup>*</sup>
                </label>
                <textarea
                  id="reason"
                  className={'input-field-area'}
                  type="text"
                  rows={50}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                />
                {reasonError && <span className={'error-message'}>Min 20 characters</span>}
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={() => setCurrentStep('summary')}
            >
              Next
            </button>
          </div>
        </div>
      }
      {/* Summary */}
      {currentStep === 'summary' &&
        <div className={Styles.summary}>
          <h3>Apply Role for</h3>
          <div className={Styles.workspaceSummaryContent}>
            <div className={Styles.workspaceIcon}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.workspaceContent}>
              <h3>{selectedWorkspace?.name}</h3>
              <div className={Styles.workspaceMetadata}>
                <p>Created by: {selectedWorkspace?.createdBy?.firstName} {selectedWorkspace?.createdBy?.lastName} | {selectedWorkspace?.createdOn && <>Created on: {regionalDateAndTimeConversionSolution(selectedWorkspace?.createdOn)}</>}</p>
              </div>
            </div>
          </div>
          <h3>Selected Roles</h3>
          <div className={Styles.flex}>
            {roleList?.map(role => 
              <RoleCard type={'display'} key={role.id} role={role} />
            )}
          </div>
          <h3>Additional Information</h3>
          <p>Reason</p>
          <div className={Styles.reasonContainer}>
            {reason}
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit}
            >
              Submit
            </button>
          </div>
        </div>
      }
    </div>
  );
}

export default RequestWorkspace;