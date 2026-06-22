// src/components/manageCapacity/ManageCapacity.js
import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './ManageCapacity.scss';
import { fabricApi } from '../../apis/fabric.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import SelectBox from 'dna-container/SelectBox';
import {SKU_OPTIONS, REGION_OPTIONS, STATE_OPTIONS } from '../../utilities/constants';

const ManageCapacity = ({ onClose }) => {
  const [capacities, setCapacities] = useState([]);
  const [listError, setListError] = useState('');
  const [expandedId, setExpandedId] = useState(undefined);
  const [editValues, setEditValues] = useState({});
  const [updateStatus, setUpdateStatus] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const [deleteStatus, setDeleteStatus] = useState({});
  const [showAddForm, setShowAddForm] = useState(false);
  const [newCapacity, setNewCapacity] = useState({ id: '', name: '', sku: '', region: '', state: '' });
  const [addErrors, setAddErrors] = useState({});
  const [addStatus, setAddStatus] = useState(null);
  const [addLoading, setAddLoading] = useState(false);
  const [updateErrors, setUpdateErrors] = useState({});
  const [originalValues, setOriginalValues] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const addFormRef = useRef(null);
  const scrollPanelRef = useRef(null);

  useEffect(() => {
    fetchCapacities();
  }, []);

  const fetchCapacities = () => {
    ProgressIndicator.show();
    setListError('');
    setCapacities([]);
    fabricApi
      .getCapacities()
      .then((res) => {
        setCapacities(res?.data || []);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        setListError(
          e?.response?.data?.errors?.[0]?.message || 'Failed to fetch capacities.'
        );
      });
  };

  useEffect(() => {
    if (showAddForm && scrollPanelRef.current) {
      scrollPanelRef.current.scrollTop = scrollPanelRef.current.scrollHeight;
    }
    if (showAddForm) {
      setTimeout(() => SelectBox.defaultSetup(), 50);
    }
  }, [showAddForm]);

  useEffect(() => {
    if (expandedId !== undefined) {
      setTimeout(() => SelectBox.defaultSetup(), 50);
    }
  }, [expandedId]);

  const handleSelect = (capacity, rowId) => {
    if (expandedId === rowId) {
      setExpandedId(undefined);
      return;
    }
    setExpandedId(rowId);
    if (!editValues[rowId]) {
      const initial = {
        id: capacity.id || '',
        name: capacity.name || '',
        sku: capacity.sku || '',
        region: capacity.region || '',
        state: capacity.state || '',
      };
      setEditValues((prev) => ({ ...prev, [rowId]: initial }));
      setOriginalValues((prev) => ({ ...prev, [rowId]: initial }));
    }
  };

  const handleEditChange = (id, field, value) => {
    setEditValues((prev) => ({
      ...prev,
      [id]: { ...prev[id], [field]: value },
    }));
  };

  const handleUpdate = (rowId, capacityId) => {
    const vals = editValues[rowId] || {};
    const errors = {};
    if (!vals.name?.trim()) errors.name = '*Required';
    if (!vals.sku?.trim()) errors.sku = '*Required';
    if (!vals.region?.trim()) errors.region = '*Required';
    if (!vals.state?.trim()) errors.state = '*Required';
    if (!vals.id?.trim()) errors.id = '*Required';
    if (Object.keys(errors).length > 0) {
      setUpdateErrors((prev) => ({ ...prev, [rowId]: errors }));
      return;
    }
    setUpdateErrors((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
    fabricApi
      .updateCapacity(capacityId, editValues[rowId])
      .then(() => {
        setUpdateStatus((prev) => ({
          ...prev,
          [rowId]: { type: 'success', text: 'Updated successfully!' },
        }));
        setEditValues((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        setExpandedId(undefined);
        fetchCapacities();
        setTimeout(() => {
          setUpdateStatus((prev) => {
            const updated = { ...prev };
            delete updated[rowId];
            return updated;
          });
        }, 5000);
      })
      .catch((e) => {
        setUpdateStatus((prev) => ({
          ...prev,
          [rowId]: {
            type: 'error',
            text: e?.response?.data?.errors?.[0]?.message || 'Update failed.',
          },
        }));
      });
  };

  const handleDelete = (rowId, capacityRegion) => {
    fabricApi
      .deleteCapacity(capacityRegion)
      .then(() => {
        setDeleteConfirmId(null);
        if (expandedId === rowId) setExpandedId(undefined);
        setEditValues((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        fetchCapacities();
      })
      .catch((e) => {
        setDeleteConfirmId(null);
        setDeleteStatus((prev) => ({
          ...prev,
          [rowId]: {
            type: 'error',
            text: e?.response?.data?.errors?.[0]?.message || 'Delete failed.',
          },
        }));
      });
  };

  const handleAdd = () => {
    const errors = {};
    if (!newCapacity.id?.trim()) errors.id = '*Required';
    if (!newCapacity.name?.trim()) errors.name = '*Required';
    if (!newCapacity.sku?.trim()) errors.sku = '*Required';
    if (!newCapacity.region?.trim()) errors.region = '*Required';
    if (!newCapacity.state?.trim()) errors.state = '*Required';
    if (Object.keys(errors).length > 0) {
      setAddErrors(errors);
      return;
    }
    setAddErrors({});
    setAddLoading(true);
    fabricApi
      .addCapacity(newCapacity)
      .then(() => {
        setAddLoading(false);
        setShowAddForm(false);
        setNewCapacity({ id: '', name: '', sku: '', region: '', state: '' });
        setAddErrors({});
        setAddStatus({ type: 'success', text: 'Capacity added successfully!' });
        fetchCapacities();
        setTimeout(() => setAddStatus(null), 3000);
      })
      .catch((e) => {
        setAddLoading(false);
        setAddStatus({
          type: 'error',
          text: e?.response?.data?.errors?.[0]?.message || 'Failed to add capacity.',
        });
      });
  };

  const isModified = (rowId, field) =>
    !!(originalValues[rowId] && editValues[rowId] &&
      editValues[rowId][field] !== originalValues[rowId][field]);

  const filteredCapacities = capacities
    .filter((c) => {
      if (!searchTerm.trim()) return true;
      const term = searchTerm.trim().toLowerCase();
      return (
        (c.name || '').toLowerCase().includes(term) ||
        (c.region || '').toLowerCase().includes(term)
      );
    })
    .sort((a, b) => (a.region || '').localeCompare(b.region || ''));

  return (
    <div className={Styles.wrapper}>
      <div className={Styles.header}>
        <h3 className={Styles.title}>Manage Capacity</h3>
      </div>

      <div className={Styles.searchBar}>
        <div className="input-field-group search-field">
          <input
            type="text"
            className="input-field search"
            placeholder="Search by name or region..."
            maxLength={100}
            autoComplete="off"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className={Styles.scrollPanel} ref={scrollPanelRef}>
        {!showAddForm && addStatus?.type === 'success' && (
          <div className={classNames(Styles.inlineMsg, Styles.success, Styles.addSuccessBanner)}>
            <i className="icon mbc-icon check circle" /> {addStatus.text}
          </div>
        )}
        {listError && (
          <div className={classNames(Styles.inlineMsg, Styles.error)}>
            <i className="icon mbc-icon alert circle" /> {listError}
          </div>
        )}

        {!listError && capacities.length === 0 && (
          <div className={Styles.emptyState}>
            <p>No capacities found. Use &quot;Add Capacity&quot; to add a new one.</p>
          </div>
        )}

        {!listError && capacities.length > 0 && filteredCapacities.length === 0 && (
          <div className={Styles.emptyState}>
            <p>No capacities match your search.</p>
          </div>
        )}

        {filteredCapacities.map((capacity, idx) => {
          const rowId = capacity.id != null ? capacity.id : `row-${idx}`;
          return (
          <div key={rowId} className={Styles.capacityItem}>
            <div
              className={classNames(Styles.capacityHeader, expandedId === rowId && Styles.active)}
              onClick={() => handleSelect(capacity, rowId)}
            >
              <span className={Styles.capacityName}>{capacity.name}</span>
              <span className={Styles.capacityRegion}>{capacity.region}</span>
              <i className={classNames('icon', expandedId === rowId ? 'mbc-icon arrow-up' : 'mbc-icon arrow-down')} />
            </div>

            {expandedId === rowId && editValues[rowId] && (
              <div className={Styles.capacityDetails}>
                <div className={Styles.formRow}>
                  <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.id ? 'error' : '', isModified(rowId, 'id') ? Styles.fieldModified : '')}>
                    <label className="input-label">ID <sup>*</sup></label>
                    <input
                      type="text"
                      className="input-field"
                      value={editValues[rowId].id}
                      onChange={(e) => { handleEditChange(rowId, 'id', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], id: '' } })); }}
                    />
                    <span className="error-message">{updateErrors[rowId]?.id}</span>
                  </div>
                  <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.name ? 'error' : '', isModified(rowId, 'name') ? Styles.fieldModified : '')}>
                    <label className="input-label">Name <sup>*</sup></label>
                    <input
                      type="text"
                      className="input-field"
                      value={editValues[rowId].name}
                      onChange={(e) => { handleEditChange(rowId, 'name', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], name: '' } })); }}
                    />
                    <span className="error-message">{updateErrors[rowId]?.name}</span>
                  </div>
                </div>
                <div className={Styles.formRow}>
                  <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.sku ? 'error' : '', isModified(rowId, 'sku') ? Styles.fieldModified : '')}>
                    <label className="input-label">SKU <sup>*</sup></label>
                    <div className="custom-select">
                      <select
                        value={editValues[rowId].sku}
                        onChange={(e) => { handleEditChange(rowId, 'sku', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], sku: '' } })); }}
                      >
                        <option value="">Choose</option>
                        {SKU_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                      </select>
                    </div>
                    <span className="error-message">{updateErrors[rowId]?.sku}</span>
                  </div>
                  <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.region ? 'error' : '', isModified(rowId, 'region') ? Styles.fieldModified : '')}>
                    <label className="input-label">Region <sup>*</sup></label>
                    <div className="custom-select">
                      <select
                        value={editValues[rowId].region}
                        onChange={(e) => { handleEditChange(rowId, 'region', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], region: '' } })); }}
                      >
                        <option value="">Choose</option>
                        {REGION_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                      </select>
                    </div>
                    <span className="error-message">{updateErrors[rowId]?.region}</span>
                  </div>
                </div>
                <div className={Styles.formRow}>
                  <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.state ? 'error' : '', isModified(rowId, 'state') ? Styles.fieldModified : '')}>
                    <label className="input-label">State <sup>*</sup></label>
                    <div className="custom-select">
                      <select
                        value={editValues[rowId].state}
                        onChange={(e) => { handleEditChange(rowId, 'state', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], state: '' } })); }}
                      >
                        <option value="">Choose</option>
                        {STATE_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                      </select>
                    </div>
                    <span className="error-message">{updateErrors[rowId]?.state}</span>
                  </div>
                  <div className={Styles.formGroup} />
                </div>
                <div className={Styles.actionRow}>
                  <button className="btn btn-primary" onClick={() => handleUpdate(rowId, capacity.id)}>
                    Update
                  </button>
                  {deleteConfirmId === rowId ? (
                    <>
                      <span className={Styles.deleteConfirmText}>Are you sure?</span>
                      <button className={classNames('btn btn-primary', Styles.deleteBtnConfirm)} onClick={() => handleDelete(rowId, capacity.region)}>Yes, Delete</button>
                      <button className="btn btn-secondary" onClick={() => setDeleteConfirmId(null)}>Cancel</button>
                    </>
                  ) : (
                    <button className={classNames('btn', Styles.deleteBtn)} onClick={(e) => { e.stopPropagation(); setDeleteConfirmId(rowId); }}>
                      <i className="icon delete" />&nbsp;Delete
                    </button>
                  )}
                  {updateStatus[rowId] && (
                    <span
                      className={classNames(
                        Styles.inlineMsg,
                        updateStatus[rowId].type === 'success' ? Styles.success : Styles.error
                      )}
                    >
                      {updateStatus[rowId].text}
                    </span>
                  )}
                  {deleteStatus[rowId] && (
                    <span className={classNames(Styles.inlineMsg, Styles.error)}>
                      {deleteStatus[rowId].text}
                    </span>
                  )}
                </div>
              </div>
            )}
          </div>
          );
        })}

        {showAddForm && (
          <div className={Styles.addForm} ref={addFormRef}>
            <div className={Styles.addFormHeader}>
              <h5 className={Styles.addFormTitle}>New Capacity</h5>
              <button
                className={classNames('btn', Styles.addFormClose)}
                onClick={() => { setShowAddForm(false); setNewCapacity({ id: '', name: '', sku: '', region: '', state: '' }); setAddErrors({}); setAddStatus(null); }}
              >
                <i className="icon mbc-icon close thin" />
              </button>
            </div>
            <div className={Styles.formRow}>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.id ? 'error' : '')}>
                <label className="input-label">ID <sup>*</sup></label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Enter ID"
                  value={newCapacity.id}
                  onChange={(e) => { setNewCapacity((prev) => ({ ...prev, id: e.target.value })); setAddErrors((prev) => ({ ...prev, id: '' })); }}
                />
                <span className="error-message">{addErrors.id}</span>
              </div>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.name ? 'error' : '')}>
                <label className="input-label">Name <sup>*</sup></label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Enter name"
                  value={newCapacity.name}
                  onChange={(e) => { setNewCapacity((prev) => ({ ...prev, name: e.target.value })); setAddErrors((prev) => ({ ...prev, name: '' })); }}
                />
                <span className="error-message">{addErrors.name}</span>
              </div>
            </div>
            <div className={Styles.formRow}>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.sku ? 'error' : '')}>
                <label className="input-label">SKU <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.sku}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, sku: e.target.value })); setAddErrors((prev) => ({ ...prev, sku: '' })); }}
                  >
                    <option value="">Choose</option>
                    {SKU_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                  </select>
                </div>
                <span className="error-message">{addErrors.sku}</span>
              </div>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.region ? 'error' : '')}>
                <label className="input-label">Region <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.region}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, region: e.target.value })); setAddErrors((prev) => ({ ...prev, region: '' })); }}
                  >
                    <option value="">Choose</option>
                    {REGION_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                  </select>
                </div>
                <span className="error-message">{addErrors.region}</span>
              </div>
            </div>
            <div className={Styles.formRow}>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.state ? 'error' : '')}>
                <label className="input-label">State <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.state}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, state: e.target.value })); setAddErrors((prev) => ({ ...prev, state: '' })); }}
                  >
                    <option value="">Choose</option>
                    {STATE_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                  </select>
                </div>
                <span className="error-message">{addErrors.state}</span>
              </div>
              <div className={Styles.formGroup} />
            </div>
            <div className={Styles.actionRow}>
              <button
                className="btn btn-tertiary"
                disabled={addLoading}
                onClick={handleAdd}
              >
                Add
              </button>
              {addStatus?.type === 'error' && (
                <span className={classNames(Styles.inlineMsg, Styles.error)}>
                  {addStatus.text}
                </span>
              )}
            </div>
          </div>
        )}
      </div>

      <div className={Styles.footer}>
        <button className="btn btn-secondary" onClick={() => {
          if (showAddForm) {
            setShowAddForm(false);
            setTimeout(() => {
              setShowAddForm(true);
            }, 0);
          } else {
            setShowAddForm(true);
          }
        }}>
          <i className="icon mbc-icon plus" />
          <span>&nbsp;Add Capacity</span>
        </button>
        <button className="btn btn-secondary" onClick={onClose}>
          Close
        </button>
      </div>
    </div>
  );
};

export default ManageCapacity;