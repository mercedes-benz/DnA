// src/components/manageCapacity/ManageCapacity.js
import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './ManageCapacity.scss';
import { fabricApi } from '../../apis/fabric.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import SelectBox from 'dna-container/SelectBox';
import {SKU_OPTIONS, STATE_OPTIONS } from '../../utilities/constants';

const ManageCapacity = ({ onClose }) => {
  const [capacities, setCapacities] = useState([]);
  const [regionOptions, setRegionOptions] = useState([]);
  const [listError] = useState('');
  const [expandedId, setExpandedId] = useState(undefined);
  const [editValues, setEditValues] = useState({});
  const [editingRows, setEditingRows] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newCapacity, setNewCapacity] = useState({ id: '', name: '', sku: '', region: '', state: 'Active' });
  const [addErrors, setAddErrors] = useState({});
  const [addStatus, setAddStatus] = useState(null);
  const [addLoading, setAddLoading] = useState(false);
  const [addFetching, setAddFetching] = useState(false);
  const [updateErrors, setUpdateErrors] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const addFormRef = useRef(null);
  const scrollPanelRef = useRef(null);

  useEffect(() => {
    fetchCapacities();
    fabricApi
      .getRegions()
      .then((res) => {
        setRegionOptions(res?.data || []);
      })
      .catch(() => {
        Notification.error('Failed to fetch regions.');
      });
  }, []);

  const fetchCapacities = () => {
    ProgressIndicator.show();
    setCapacities([]);
    fabricApi
      .getCapacities()
      .then((res) => {
        setCapacities(res?.data || []);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.error(e?.response?.data?.errors?.[0]?.message || 'Failed to fetch capacities.');
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
    if (showAddForm) {
      setTimeout(() => SelectBox.defaultSetup(), 50);
    }
  }, [capacities, showAddForm]);

  useEffect(() => {
    if (expandedId !== undefined) {
      setTimeout(() => SelectBox.defaultSetup(), 50);
    }
  }, [expandedId]);

  const handleSelect = (capacity, rowId) => {
    if (expandedId === rowId) {
      setExpandedId(undefined);
      setEditingRows((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
      return;
    }
    setExpandedId(rowId);
    setEditingRows((prev) => ({ ...prev, [rowId]: false }));
    if (!editValues[rowId]) {
      const initial = {
        id: capacity.id || '',
        name: capacity.name || '',
        sku: capacity.sku || '',
        region: capacity.region || '',
        state: capacity.state || '',
      };
      setEditValues((prev) => ({ ...prev, [rowId]: initial }));
    }
  };

  const handleEditChange = (id, field, value) => {
    setEditValues((prev) => ({
      ...prev,
      [id]: { ...prev[id], [field]: value },
    }));
  };

  const handleCapacityIdChange = (rowId, value) => {
    handleEditChange(rowId, 'id', value);
    setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], id: '' } }));
  };

  const handleUpdate = (rowId) => {
    const vals = editValues[rowId] || {};
    const errors = {};
    if (!vals.id?.trim()) errors.id = '*Required';
    if (!vals.name?.trim()) errors.name = '*Required';
    if (!vals.sku?.trim()) errors.sku = '*Required';
    if (!vals.state?.trim()) errors.state = '*Required';
    if (Object.keys(errors).length > 0) {
      setUpdateErrors((prev) => ({ ...prev, [rowId]: errors }));
      return;
    }
    setUpdateErrors((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
    fabricApi
      .updateCapacity(vals.id, editValues[rowId])
      .then(() => {
        setExpandedId(undefined);
        setEditValues((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        setEditingRows((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        setUpdateErrors((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        Notification.show('Capacity updated successfully!');
        fetchCapacities();
      })
      .catch((e) => {
        Notification.error(e?.response?.data?.errors?.[0]?.message || 'Update failed.');
      });
  };

  const handleDelete = (rowId, capacityRegion) => {
    fabricApi
      .deleteCapacity(capacityRegion)
      .then(() => {
        setDeleteConfirmId(null);
        if (expandedId === rowId) setExpandedId(undefined);
        setEditValues((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        setEditingRows((prev) => { const u = { ...prev }; delete u[rowId]; return u; });
        fetchCapacities();
        Notification.show('Capacity deleted successfully!');
      })
      .catch((e) => {
        setDeleteConfirmId(null);
        Notification.error(e?.response?.data?.errors?.[0]?.message || 'Delete failed.');
      });
  };

  const handleFetchForNew = () => {
    const trimmed = newCapacity.id?.trim();
    if (!trimmed || trimmed.length < 25) {
      setAddErrors((prev) => ({ ...prev, id: 'Please enter a valid Capacity ID (min 25 characters)' }));
      return;
    }
    setAddFetching(true);
    fabricApi
      .getCapacityById(trimmed)
      .then((res) => {
        const data = res?.data;
        if (data && (data.id || data.name)) {
          setNewCapacity((prev) => ({
            ...prev,
            id: data.id || trimmed,
            name: data.name || '',
            sku: data.sku || '',
            region: data.region || '',
            state: data.state || '',
          }));
          setAddErrors({});
          setTimeout(() => SelectBox.defaultSetup(), 50);
        } else {
          Notification.show('No capacity found with this ID.', 'alert');
        }
      })
      .catch(() => {
        Notification.show('No capacity found with this ID.', 'alert');
      })
      .finally(() => {
        setAddFetching(false);
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
        setNewCapacity({ id: '', name: '', sku: '', region: '', state: 'Active' });
        setAddErrors({});
        Notification.show('Capacity added successfully!');
        fetchCapacities();
        setTimeout(() => setAddStatus(null), 3000);
      })
      .catch((e) => {
        setAddLoading(false);
        Notification.error(e?.response?.data?.errors?.[0]?.message || 'Failed to add capacity.');
      });
  };

  const coveredRegions = new Set(capacities.map((c) => c.region));
  const availableRegionsForAdd = regionOptions.filter((r) => !coveredRegions.has(r));
  const allRegionsCovered = availableRegionsForAdd.length === 0;

  const allRegionRows = capacities
    .filter((capacity) => {
      if (!searchTerm.trim()) return true;
      const term = searchTerm.trim().toLowerCase();
      return (
        (capacity.region || '').toLowerCase().includes(term) ||
        (capacity.name || '').toLowerCase().includes(term)
      );
    })
    .sort((a, b) => (a.region || '').localeCompare(b.region || ''));

  return (
    <div className={Styles.wrapper}>
      <div className={Styles.header}>
        <h3 className={Styles.title}>Manage Region Capacity</h3>
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
        {listError && (
          <div className={classNames(Styles.inlineMsg, Styles.error)}>
            <i className="icon mbc-icon alert circle" /> {listError}
          </div>
        )}

        {allRegionRows.map((capacity) => {
            const rowId = capacity.id != null ? capacity.id : `row-${capacity.region}`;
            const isEditing = !!editingRows[rowId];
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
                      <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.id ? 'error' : '')}>
                        <label className="input-label">Capacity ID <sup>*</sup></label>
                        <input
                          type="text"
                          className={classNames('input-field', !isEditing && Styles.readOnlyInput)}
                          value={editValues[rowId].id}
                          readOnly={!isEditing}
                          onChange={isEditing ? (e) => handleCapacityIdChange(rowId, e.target.value) : undefined}
                          placeholder="Enter Capacity ID"
                        />
                        <span className="error-message">{updateErrors[rowId]?.id}</span>
                      </div>
                      <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.name ? 'error' : '')}>
                        <label className="input-label">Region Capacity Name <sup>*</sup></label>
                        <input
                          type="text"
                          className={classNames('input-field', !isEditing && Styles.readOnlyInput)}
                          value={editValues[rowId].name}
                          readOnly={!isEditing}
                          onChange={isEditing ? (e) => { handleEditChange(rowId, 'name', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], name: '' } })); } : undefined}
                        />
                        <span className="error-message">{updateErrors[rowId]?.name}</span>
                      </div>
                      <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.sku ? 'error' : '')}>
                        <label className="input-label">SKU <sup>*</sup></label>
                        <div className={classNames('custom-select', !isEditing && Styles.readOnlySelect)}>
                          <select
                            value={editValues[rowId].sku}
                            disabled={!isEditing}
                            onChange={isEditing ? (e) => { handleEditChange(rowId, 'sku', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], sku: '' } })); } : undefined}
                          >
                            <option value="">Choose</option>
                            {SKU_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                            {editValues[rowId].sku && !SKU_OPTIONS.includes(editValues[rowId].sku) && (
                              <option key={editValues[rowId].sku} value={editValues[rowId].sku}>{editValues[rowId].sku}</option>
                            )}
                          </select>
                        </div>
                        <span className="error-message">{updateErrors[rowId]?.sku}</span>
                      </div>
                    </div>
                    <div className={Styles.formRow}>
                      <div className={classNames('input-field-group', Styles.formGroup)}>
                        <label className="input-label">Region</label>
                        <div className={classNames('custom-select', Styles.readOnlySelect)}>
                          <select value={editValues[rowId].region} disabled>
                            <option value="">Choose</option>
                            {regionOptions.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                            {editValues[rowId].region && !regionOptions.includes(editValues[rowId].region) && (
                              <option key={editValues[rowId].region} value={editValues[rowId].region}>{editValues[rowId].region}</option>
                            )}
                          </select>
                        </div>
                      </div>
                      <div className={classNames('input-field-group include-error', Styles.formGroup, updateErrors[rowId]?.state ? 'error' : '')}>
                        <label className="input-label">State <sup>*</sup></label>
                        <div className={classNames('custom-select', !isEditing && Styles.readOnlySelect)}>
                          <select
                            value={editValues[rowId].state}
                            disabled={!isEditing}
                            onChange={isEditing ? (e) => { handleEditChange(rowId, 'state', e.target.value); setUpdateErrors((prev) => ({ ...prev, [rowId]: { ...prev[rowId], state: '' } })); } : undefined}
                          >
                            <option value="">Choose</option>
                            {STATE_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                            {editValues[rowId].state && !STATE_OPTIONS.includes(editValues[rowId].state) && (
                              <option key={editValues[rowId].state} value={editValues[rowId].state}>{editValues[rowId].state}</option>
                            )}
                          </select>
                        </div>
                        <span className="error-message">{updateErrors[rowId]?.state}</span>
                      </div>
                      <div className={Styles.formGroup} />
                    </div>
                    <div className={Styles.actionRow}>
                      {!isEditing ? (
                        <button
                          className="btn btn-secondary"
                          onClick={() => setEditingRows((prev) => ({ ...prev, [rowId]: true }))}
                        >
                          Edit
                        </button>
                      ) : (
                        <button className="btn btn-primary" onClick={() => handleUpdate(rowId, capacity.id)}>
                          Update
                        </button>
                      )}
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
                <label className="input-label">Region ID <sup>*</sup></label>
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
                <label className="input-label">Region Capacity Name <sup>*</sup></label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="Enter name"
                  value={newCapacity.name}
                  onChange={(e) => { setNewCapacity((prev) => ({ ...prev, name: e.target.value })); setAddErrors((prev) => ({ ...prev, name: '' })); }}
                />
                <span className="error-message">{addErrors.name}</span>
              </div>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.sku ? 'error' : '')}>
                <label className="input-label">SKU <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.sku}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, sku: e.target.value })); setAddErrors((prev) => ({ ...prev, sku: '' })); }}
                  >
                    <option value="">Choose</option>
                    {SKU_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                    {newCapacity.sku && !SKU_OPTIONS.includes(newCapacity.sku) && (
                      <option key={newCapacity.sku} value={newCapacity.sku}>{newCapacity.sku}</option>
                    )}
                  </select>
                </div>
                <span className="error-message">{addErrors.sku}</span>
              </div>
            </div>
            <div className={Styles.formRow}>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.region ? 'error' : '')}>
                <label className="input-label">Region <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.region}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, region: e.target.value })); setAddErrors((prev) => ({ ...prev, region: '' })); }}
                  >
                    <option value="">Choose</option>
                    {availableRegionsForAdd.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                    {newCapacity.region && !regionOptions.includes(newCapacity.region) && (
                      <option key={newCapacity.region} value={newCapacity.region}>{newCapacity.region}</option>
                    )}
                    {newCapacity.region && regionOptions.includes(newCapacity.region) && !availableRegionsForAdd.includes(newCapacity.region) && (
                      <option key={newCapacity.region} value={newCapacity.region}>{newCapacity.region}</option>
                    )}
                  </select>
                </div>
                <span className="error-message">{addErrors.region}</span>
              </div>
              <div className={classNames('input-field-group include-error', Styles.formGroup, addErrors.state ? 'error' : '')}>
                <label className="input-label">State <sup>*</sup></label>
                <div className="custom-select">
                  <select
                    value={newCapacity.state}
                    onChange={(e) => { setNewCapacity((prev) => ({ ...prev, state: e.target.value })); setAddErrors((prev) => ({ ...prev, state: '' })); }}
                  >
                    <option value="">Choose</option>
                    {STATE_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
                    {newCapacity.state && !STATE_OPTIONS.includes(newCapacity.state) && (
                      <option key={newCapacity.state} value={newCapacity.state}>{newCapacity.state}</option>
                    )}
                  </select>
                </div>
                <span className="error-message">{addErrors.state}</span>
              </div>
              <div className={Styles.formGroup} />
            </div>
            <div className={Styles.actionRow}>
              <button
                type="button"
                className="btn btn-secondary"
                disabled={addFetching}
                onClick={handleFetchForNew}
              >
                {addFetching ? 'Fetching...' : 'Fetch Capacity'}
              </button>
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
        <div>
          {!allRegionsCovered && (
            <button className="btn btn-secondary" onClick={() => {
              if (showAddForm) {
                setShowAddForm(false);
                setTimeout(() => setShowAddForm(true), 0);
              } else {
                setShowAddForm(true);
              }
            }}>
              <i className="icon mbc-icon plus" />
              <span>&nbsp;Add Capacity</span>
            </button>
          )}
        </div>
        <button className="btn btn-secondary" onClick={onClose}>
          Close
        </button>
      </div>
    </div>
  );
};

export default ManageCapacity;
