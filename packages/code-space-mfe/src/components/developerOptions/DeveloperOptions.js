import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';

import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import { Notification, ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import Styles from './DeveloperOptions.scss';

const PAT_PERMISSIONS = [
  { value: 'workspace:read', label: 'Read' },
  { value: 'workspace:deploy:staging', label: 'Deploy staging' },
  { value: 'workspace:deploy:production', label: 'Deploy production' },
];

const DeveloperOptions = () => {
  const history = useHistory();
  const [tokens, setTokens] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showRevealModal, setShowRevealModal] = useState(false);
  const [showRevokeModal, setShowRevokeModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [permissions, setPermissions] = useState([]);
  const [comment, setComment] = useState('');
  const [validationError, setValidationError] = useState('');
  const [pending, setPending] = useState(false);
  const [revokeVersion, setRevokeVersion] = useState(null);
  const [deleteVersion, setDeleteVersion] = useState(null);
  const [revealedToken, setRevealedToken] = useState('');

  const loadTokens = () => {
    setLoading(true);
    setLoadError(false);
    ProgressIndicator.show();
    CodeSpaceApiClient.getPersonalAccessTokens()
      .then((response) => {
        setTokens(Array.isArray(response?.data?.data) ? response.data.data : []);
      })
      .catch((error) => {
        setTokens([]);
        setLoadError(true);
        Notification.show(error?.response?.data?.errors?.[0]?.message || error?.message || 'Unable to load PATs.', 'alert');
      })
      .finally(() => {
        setLoading(false);
        ProgressIndicator.hide();
      });
  };

  useEffect(() => {
    loadTokens();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const togglePermission = (permission) => {
    setPermissions((selected) => selected.includes(permission)
      ? selected.filter((item) => item !== permission)
      : [...selected, permission]);
    setValidationError('');
  };

  const closeCreateModal = () => {
    if (pending) {
      return;
    }
    setShowCreateModal(false);
    setPermissions([]);
    setComment('');
    setValidationError('');
  };

  const createToken = () => {
    if (permissions.length === 0) {
      setValidationError('Select at least one permission.');
      return;
    }
    setPending(true);
    ProgressIndicator.show();
    CodeSpaceApiClient.createPersonalAccessToken({ permissions, comment })
      .then((response) => {
        const createdToken = response?.data?.token;
        if (!createdToken) {
          throw new Error('Unable to create PAT.');
        }
        setRevealedToken(createdToken);
        setShowCreateModal(false);
        setShowRevealModal(true);
        setPermissions([]);
        setComment('');
        setValidationError('');
        setTokens((currentTokens) => [{
          version: response.data.version,
          displaySuffix: response.data.displaySuffix,
          status: 'ACTIVE',
          permissions: response.data.permissions,
          comment: response.data.comment,
          createdAt: response.data.createdAt,
          codeSpaceProjectCount: response.data.codeSpaceProjectCount,
        }, ...currentTokens]);
      })
      .catch((error) => {
        Notification.show(error?.response?.data?.errors?.[0]?.message || error?.message || 'Unable to create PAT.', 'alert');
      })
      .finally(() => {
        setPending(false);
        ProgressIndicator.hide();
      });
  };

  const closeRevealModal = () => {
    setShowRevealModal(false);
    setRevealedToken('');
  };

  const copyToken = async () => {
    try {
      await navigator.clipboard.writeText(revealedToken);
      Notification.show('Copied to clipboard.');
    } catch (error) {
      Notification.show('Unable to copy token.', 'alert');
    }
  };

  const revokeToken = () => {
    setPending(true);
    ProgressIndicator.show();
    CodeSpaceApiClient.revokePersonalAccessToken(revokeVersion)
      .then(() => {
        setShowRevokeModal(false);
        setRevokeVersion(null);
        setTokens((currentTokens) => currentTokens.map((token) => (
          token.version === revokeVersion ? { ...token, status: 'REVOKED' } : token
        )));
        Notification.show('PAT revoked.');
      })
      .catch((error) => {
        Notification.show(error?.response?.data?.errors?.[0]?.message || error?.message || 'Unable to revoke PAT.', 'alert');
      })
      .finally(() => {
        setPending(false);
        ProgressIndicator.hide();
      });
  };

  const deleteTokenVault = () => {
    setPending(true);
    ProgressIndicator.show();
    CodeSpaceApiClient.deletePersonalAccessTokenVault(deleteVersion)
      .then(() => {
        setShowDeleteModal(false);
        setTokens((currentTokens) => currentTokens.filter((token) => token.version !== deleteVersion));
        setDeleteVersion(null);
        Notification.show('PAT deleted.');
      })
      .catch((error) => {
        Notification.show(error?.response?.data?.errors?.[0]?.message || error?.message || 'Unable to delete PAT.', 'alert');
      })
      .finally(() => {
        setPending(false);
        ProgressIndicator.hide();
      });
  };

  const createModalContent = (
    <div className={Styles.modalContent}>
      <header className={Styles.modalHeader}>
        <button className="modal-close-button" type="button" onClick={closeCreateModal} aria-label="Close">
          <i className="icon mbc-icon close thin" />
        </button>
      </header>
      <p><strong>Never expires.</strong> This token remains active until you revoke it.</p>
      <p>Keep this token secret. It will be shown only once.</p>
      <p>Scope: <strong>All codespaces I have access to</strong></p>
      <fieldset className={Styles.permissions}>
        <legend>Permissions</legend>
        {PAT_PERMISSIONS.map((permission) => (
          <label key={permission.value} className="checkbox">
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                checked={permissions.includes(permission.value)}
                onChange={() => togglePermission(permission.value)}
              />
            </span>
            <span className="label">{permission.label}</span>
          </label>
        ))}
      </fieldset>
      {validationError && <p className={Styles.validationError} role="alert">{validationError}</p>}
      <div className="input-field-group area">
        <label className="input-label" htmlFor="pat-comment">Comment (optional), Max length : 200</label>
        <textarea
          id="pat-comment"
          className="input-field-area"
          value={comment}
          maxLength={200}
          onChange={(event) => setComment(event.target.value)}
          rows="3"
        />
      </div>
      <div className={Styles.modalActions}>
        <button className="btn btn-primary" type="button" onClick={closeCreateModal} disabled={pending}>Cancel</button>
        <button className="btn btn-tertiary" type="button" onClick={createToken} disabled={pending}>
          Create PAT
        </button>
      </div>
    </div>
  );

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <Caption title="Developers options" onBackClick={() => history.push('/')}>
          <div className={Styles.listHeader}>
            <button className={classNames('btn btn-primary', Styles.refreshButton)} type="button" tooltip-data="Refresh" onClick={loadTokens}>
              <i className="icon mbc-icon refresh" />
            </button>
            {tokens.length > 0 && (
              <button className="btn btn-primary" type="button" onClick={() => setShowCreateModal(true)}>
                Create new PAT
              </button>
            )}
          </div>
        </Caption>
        <div className={Styles.content}>
          {!loading && loadError && (
            <div className={Styles.emptyState}>
              <p>Unable to load personal access tokens.</p>
              <button className="btn btn-primary" type="button" onClick={loadTokens}>Try again</button>
            </div>
          )}
          {!loading && !loadError && tokens.length === 0 && (
            <div className={Styles.emptyState}>
              <h5>Create a new PAT token</h5>
              <button className="btn btn-tertiary" type="button" onClick={() => setShowCreateModal(true)}>
                Create new PAT
              </button>
            </div>
          )}
          {!loading && !loadError && tokens.length > 0 && (
            <div className={Styles.tableWrapper}>
              <table className="ul-table">
                <thead>
                  <tr className="header-row">
                    <th>S.No.</th>
                    <th>Token</th>
                    <th>Codespaces</th>
                    <th>Permissions</th>
                    <th>Status</th>
                    <th>Created date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {tokens.map((token, index) => (
                    <React.Fragment key={token.version}>
                      <tr>
                        <td>{index + 1}</td>
                        <td><code>cs_pat_…****{token.displaySuffix}</code><small className={Styles.version}>v{token.version}</small></td>
                        <td>All accessible codespaces</td>
                        <td>
                          <div className={Styles.permissionChips}>
                            {(token.permissions || []).map((permission) => (
                              <span className={Styles.permissionChip} key={permission}>
                                {PAT_PERMISSIONS.find((item) => item.value === permission)?.label || permission}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td>{token.status === 'REVOKED' ? 'Revoked' : 'Active'}</td>
                        <td>{token.createdAt ? regionalDateAndTimeConversionSolution(token.createdAt) : '—'}</td>
                        <td>
                          <button
                            className="btn btn-secondary"
                            type="button"
                            disabled={pending || token.status === 'REVOKED'}
                            onClick={() => {
                              setRevokeVersion(token.version);
                              setShowRevokeModal(true);
                            }}
                          >
                            {token.status === 'REVOKED' ? 'Revoked' : 'Revoke'}
                          </button>
                          {token.status === 'REVOKED' && (
                            <button
                              className={Styles.actionBtn + ' btn btn-primary'}
                              type="button"
                              disabled={pending}
                              tooltip-data="Delete"
                              onClick={() => {
                                setDeleteVersion(token.version);
                                setShowDeleteModal(true);
                              }}
                            >
                              <i className="icon mbc-icon trash-outline" />
                            </button>
                          )}
                        </td>
                      </tr>
                      {token.comment && (
                        <tr className={Styles.commentRow}>
                          <td colSpan="7">Comment: {token.comment}</td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
      {showCreateModal && (
        <Modal
          title="Create new PAT"
          show={showCreateModal}
          showAcceptButton={false}
          showCancelButton={false}
          hideCloseButton
          content={createModalContent}
        />
      )}
      {showRevealModal && (
        <Modal
          title="Personal access token"
          show={showRevealModal}
          showAcceptButton={false}
          showCancelButton={false}
          hideCloseButton
          content={(
            <div className={Styles.modalContent}>
              <header className={Styles.modalHeader}>
                <button className="modal-close-button" type="button" onClick={closeRevealModal} aria-label="Close">
                  <i className="icon mbc-icon close thin" />
                </button>
              </header>
              <p>Copy this token now. It will not be shown again.</p>
              <input className={Styles.tokenInput} type="text" readOnly value={revealedToken} onFocus={(event) => event.target.select()} />
              <div className={Styles.modalActions}>
                <button className="btn btn-primary" type="button" onClick={closeRevealModal}>Close</button>
                <button className="btn btn-tertiary" type="button" onClick={copyToken}>Copy</button>
              </div>
            </div>
          )}
        />
      )}
      {showRevokeModal && (
        <Modal
          title="Revoke PAT"
          show={showRevokeModal}
          showAcceptButton={false}
          showCancelButton={false}
          hideCloseButton
          content={(
            <div className={Styles.modalContent}>
              <header className={Styles.modalHeader}>
                <button className="modal-close-button" type="button" onClick={() => setShowRevokeModal(false)} aria-label="Close">
                  <i className="icon mbc-icon close thin" />
                </button>
              </header>
              <p>Are you sure you want to revoke this personal access token? This action cannot be undone.</p>
              <div className={Styles.modalActions}>
                <button className="btn btn-primary" type="button" disabled={pending} onClick={() => setShowRevokeModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" disabled={pending} onClick={revokeToken}>Revoke</button>
              </div>
            </div>
          )}
        />
      )}
      {showDeleteModal && (
        <Modal
          title="Delete PAT"
          show={showDeleteModal}
          showAcceptButton={false}
          showCancelButton={false}
          hideCloseButton
          content={(
            <div className={Styles.modalContent}>
              <header className={Styles.modalHeader}>
                <button className="modal-close-button" type="button" onClick={() => setShowDeleteModal(false)} aria-label="Close">
                  <i className="icon mbc-icon close thin" />
                </button>
              </header>
              <p>Are you sure you want to delete this personal access token? This action cannot be undone.</p>
              <div className={Styles.modalActions}>
                <button className="btn btn-primary" type="button" disabled={pending} onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" disabled={pending} onClick={deleteTokenVault}>Delete</button>
              </div>
            </div>
          )}
        />
      )}
    </div>
  );
};

export default DeveloperOptions;
