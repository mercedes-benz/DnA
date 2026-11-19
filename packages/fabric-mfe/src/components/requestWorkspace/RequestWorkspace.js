import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './request-workspace.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import { fabricApi } from '../../apis/fabric.api';
import RoleCard from '../roleCard/RoleCard';
import TypeAheadBox from 'dna-container/TypeAheadBox';

const RequestWorkspace = ({ onRefresh }) => {
  const [workspaces, setWorkspaces] = useState([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState({});
  const [roleList, setRoleList] = useState([]);
  const [reason, setReason] = useState('');
  const [reasonError, setReasonError] = useState(false);
  const [currentStep, setCurrentStep] = useState('workspace-selection');
  const [searchedWorkspace, setSearchedWorkspace] = useState(null);

  const handleWorkspaceSearch = (searchTerm, showSpinner) => {
    if (searchTerm.length > 2) {
      showSpinner(true);
      fabricApi
        .searchFabricWorkspaceLov(0, 0, searchTerm)
        .then((res) => {
          if (res.status !== 204) {
            setWorkspaces(res?.data?.records || []);
          } else {
            setWorkspaces([]);
          }
          showSpinner(false);
        })
        .catch((e) => {
          showSpinner(false);
          Notification.show(
            e.response?.data?.errors?.[0]?.message || 'Error while fetching workspaces.',
            'alert',
          );
        });
    } else {
      setWorkspaces([]); 
    }
  };
 
  useEffect(() => {
    reason.length > 20 && setReasonError(false);   
  }, [reason]);

  const handleAddRole = (role) => {
    const roleListTemp = roleList.filter(roleTemp => roleTemp.roleID !== role.roleID);
    setRoleList([...roleListTemp, {...role}]);
  }

  const handleWorkspaceRequest = (workspace) => {
    setRoleList([]);
    setReason('');
    setReasonError(false);
    setSelectedWorkspace(workspace);
    setCurrentStep('role-selection');
  }

  const handleRoleSelectionNext = () => {
    if (roleList.length === 0) {
      Notification.show('Please select a role and proceed', 'alert');
      return;
    }
  
    const invalidRoles = roleList.filter(role =>
      !role.validFrom || !role.validTo || new Date(role.validTo) <= new Date(role.validFrom)
    );
  
    if (invalidRoles.length > 0) {
      Notification.show(
        'One or more roles have invalid date ranges. Please ensure Valid Until is after Valid From.',
        'alert'
      );
      return;
    }
  
    setCurrentStep('reason');
  };
  

  const handleReasonNext = () => {
    if (reason.length >= 20) {
      setCurrentStep('summary');
    } else {
      setReasonError(true);
    }
  };
  
  useEffect(() => {
    if (reason.length >= 20) {
      setReasonError(false);
    }
  }, [reason]);
  

  const handleSubmit = () => {
    const data = {
      roleList,
      reason
    }

    fabricApi.requestRoles(selectedWorkspace?.id, data).then(() => {
      ProgressIndicator.hide();
      onRefresh();
      Notification.show('Fabric Workspace Access successfully requested');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while requesting fabric workspace access',
        'alert',
      );
    });
  }

  return (
    <div className={classNames(Styles.form)}>
      <div className={Styles.formHeader}>
        <h3>Request Fabric Workspace Access</h3>
        <p>Search a workspace and request access</p>
      </div>
      {/* Top navigation */}
      <div>
        <div className={Styles.stepsContainer}>
          <div className={classNames(Styles.step, (currentStep === 'workspace-selection' || currentStep === 'role-selection' || currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={classNames(Styles.stepIcon, currentStep === 'workspace-selection' && Styles.activeIcon)}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Workspace Selection</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'role-selection' || currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={classNames(Styles.stepIcon, currentStep === 'role-selection' && Styles.activeIcon)}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Role Selection</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'reason' || currentStep === 'summary') && Styles.complete)}>
            <div className={classNames(Styles.stepIcon, currentStep === 'reason' && Styles.activeIcon)}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Reason</div>
          </div>
          <div className={classNames(Styles.step, (currentStep === 'summary') && Styles.complete)}>
            <div className={classNames(Styles.stepIcon, currentStep === 'summary' && Styles.activeIcon)}>
              <i className="icon mbc-icon tools-mini"></i>
            </div>
            <div className={Styles.stepLabel}>Summary</div>
          </div>
        </div>
      </div>
      {/* Search and request workspace */}
      {currentStep === 'workspace-selection' && 
        <div className={Styles.searchContainer}>
          <h3 className={Styles.subTitle}>Select Workspace</h3>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <TypeAheadBox
                // label={'Search Workspace'}
                placeholder={'Search for Workspace Name (Enter minimum 3 characters)'}
                defaultValue={searchedWorkspace?.name}
                list={workspaces}
                setSelected={(selectedWorkspace) => {
                  setSearchedWorkspace(selectedWorkspace || null);
                  if (selectedWorkspace) {
                    handleWorkspaceRequest(selectedWorkspace);
                  }
                }}
                onInputChange={handleWorkspaceSearch}
                required={false}
                render={(item) => (
                  <div className={Styles.optionContainer}>
                    <div className={Styles.optionHeader}>
                      <span className={Styles.optionText}>
                        <i className="icon mbc-icon tools-mini"></i> <strong>{item?.name}</strong>
                      </span>
                    </div>
                    <div className={Styles.optionSubText}>
                      <strong>Created by:</strong> {item?.createdBy?.firstName} {item?.createdBy?.lastName}
                    </div>
                    {item?.createdOn && (
                      <div className={Styles.optionSubText}>
                        <strong>Created on:</strong> {regionalDateAndTimeConversionSolution(item?.createdOn)}
                      </div>
                    )}
                  </div>
                )}
              />
            </div>
          </div>
        </div>
      }
      {/* Select Role */}
      {currentStep === 'role-selection' &&
        <div className={Styles.rolesContainer}>
          <h3 className={Styles.subTitle}>Select Role(s)</h3>
          {roleList.length === 0 ? (
  <p style={{ color: 'var(--color-orange)' }}>
    <i className="icon mbc-icon alert circle"></i>
    &nbsp;Note: Please fill the validity and select the role.
  </p>
) : (
  <p style={{ color: 'var(--color-orange)' }}>
    <i className="icon mbc-icon alert circle"></i>
    &nbsp;Note: Check your selected roles for the access.
  </p>
)}



          <div className={Styles.flex}>
            {/* No roles */}
            {(selectedWorkspace?.status?.roles === null || selectedWorkspace?.status?.roles?.length === 0) &&
              <div className={classNames(Styles.col, Styles.noWorkspaces)}>
                <i className="icon mbc-icon info"></i> <span>No roles found for this workspace. Please try another.</span>
              </div>
            }
            {selectedWorkspace?.status?.roles?.map(role => {
              const matchingRole = roleList.find(r => r.roleID === role.id);
              const updatedRole = matchingRole ? { ...role, validFrom: matchingRole.validFrom, validTo: matchingRole.validTo, isSelected: true } : role;
              return <RoleCard key={role.id} role={updatedRole} onAdd={handleAddRole} />
            }
            )}
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => setCurrentStep('workspace-selection')}
            >
              Prev
            </button>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleRoleSelectionNext}
            >
              Next
            </button>
          </div>
        </div>
      }

      {/* Fill reason */}
      {currentStep === 'reason' &&
        <div className={Styles.rolesForm}>
          <h3 className={Styles.subTitle}>Reason</h3>
          <div className={Styles.flex}>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error area', reasonError && 'error')}>
                <label id="reason" className="input-label" htmlFor="reason">
                  Reason <sup>*</sup>
                </label>
                <textarea
                  id="reason"
                  className="input-field-area"
                  rows={5}
                  maxLength={100}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="Please describe your reason for access..."
                />
                <div className={Styles.charCount}>
                  {reason.length} / 100
                </div>
                {reasonError && <span className="error-message">Minimum 20 characters required.</span>}
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
          <button
              className="btn btn-primary"
              type="button"
              onClick={() => { setCurrentStep('role-selection') }}
            >
              Prev
            </button>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleReasonNext}
            >
              Next
            </button>
          </div>
        </div>
      }
      {/* Summary */}
      {currentStep === 'summary' &&
        <div className={Styles.summary}>
          <h3 className={Styles.subTitle}>Apply Role for</h3>
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
          <div className={Styles.selectedRolesContainer}>
            <h3 className={Styles.subTitle}>Selected Roles</h3>
            <div className={Styles.flex}>
              {roleList?.map(role => 
                <RoleCard type={'display'} key={role.id} role={role} />
              )}
            </div>
          </div>
          <h3>Additional Information</h3>
          <p>Reason</p>
          <div className={Styles.reasonContainer}>
            {reason}
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => setCurrentStep('reason')}
            >
              Prev
            </button>
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