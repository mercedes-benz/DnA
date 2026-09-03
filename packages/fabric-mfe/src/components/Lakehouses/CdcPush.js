import classNames from 'classnames';
import React, { useState, useEffect, useCallback, useRef } from 'react';
import Styles from './CdcPush.scss';
import SelectBox from 'dna-container/SelectBox';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import {DIVISIONS, DATA_TIER, DATA_TIER_MAP } from '../../utilities/constants';
import { fabricApi } from '../../apis/fabric.api';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';
import { useForm } from 'react-hook-form';
import { Envs } from '../../utilities/envs';

const TIER_REVERSE_MAP = Object.fromEntries(
  Object.entries(DATA_TIER_MAP).map(([label, num]) => [num, label])
);

export const buildCdcPayload = ({
  workspaceId,
  workspaceMetadata,
  lakehouseId,
  lakehouseName, 
  selectedColumns,
  selectedTables,
  columnsByTable,
  tables,
  divisions,
  dataTier,
  isDocumentationUpdated,
  description,
  workspaceCreator
}) => {
  const dbName = lakehouseName;

  const schemaMap = {};

  tables.forEach((table) => {
  const { tableName } = table;
  const schemaName = table.schemaName || "dbo";

    const isTableSelected = selectedTables?.[tableName];
    const selectedColsMap = selectedColumns?.[tableName];
    const allColumns = columnsByTable?.[tableName] || [];

    if (!isTableSelected && !selectedColsMap) return;

    const finalCols = allColumns
      .filter((col) => {
        if (selectedColsMap) return selectedColsMap[col.columnName];
        return true; 
      })
      .map((col) => ({
        columnName: col.columnName,
        colType: col.colType,
        colConstraint: col.colConstraint
      }));

    if (!finalCols || finalCols.length === 0) return;

    const tableObj = {
      tableName,
      tableId: null,
      columns: finalCols
    };

    if (!schemaMap[schemaName]) {
      schemaMap[schemaName] = {
        schemaName,
        schemaId: null,
        tables: []
      };
    }

    schemaMap[schemaName].tables.push(tableObj);
  });

  const schemas = Object.values(schemaMap);

  return {
    metadata: {
      serviceName: workspaceMetadata.name,
      serviceId: null,
      description: workspaceMetadata.description,
      databases: [
        {
          dbName,
          dbId: lakehouseId,
          schemas,
          description: description || ""
        }
      ]
    },
    workspaceId,
    mandatoryFields: {
      divisions: divisions || [],
      department: workspaceMetadata?.department || "",
      tier: dataTier || "",
      leanIXId: workspaceMetadata?.appId || "",
      isDocumentationUpdated: isDocumentationUpdated ? "Yes" : "No",
      dataLakeName: "OneFabric",
      dataConfidentiality: workspaceMetadata?.dataClassification.toLowerCase() || ""
    },
    owners: [workspaceCreator]
  };
};

const MISMATCH_TYPE_CONFIG = {
  NEW_TABLE: { label: 'New Table', colorClass: 'badgeNew' },
  DELETED_TABLE: { label: 'Deleted Table', colorClass: 'badgeDeleted' },
  COLUMNS_ADDED: { label: 'New Column', colorClass: 'badgeNew' },
  COLUMNS_REMOVED: { label: 'Deleted Column', colorClass: 'badgeDeleted' },
  COLUMN_TYPE_CHANGED: { label: 'Datatype Changed', colorClass: 'badgeModified' },
};

const isLakehouseAlreadyPublished = (publishedDetails, lakehouseId, lakehouseName) => {
  if (!publishedDetails) return false;

  const publishedNames = publishedDetails.publishedLakeHouseNames || [];
  const publishedIds = publishedDetails.publishedLakeHouseIds || [];
  const publishedEntries = publishedDetails.publishedLakeHouseDetails || [];

  return (
    publishedNames.includes(lakehouseName) ||
    publishedNames.includes(lakehouseId) ||
    publishedIds.includes(lakehouseId) ||
    publishedEntries.some((entry) =>
      entry?.lakeHouseId === lakehouseId ||
      entry?.lakeHouseName === lakehouseName
    )
  );
};

const ViewTablesModalContent = ({ workspaceId, lakehouseId, lakehouseName, onRefreshWorkspace, mismatches: mismatchesProp = [] }) => {
  const [tables, setTables] = useState([]);
  const [columnsByTable, setColumnsByTable] = useState({});
  const [selectedTables, setSelectedTables] = useState({});
  const [selectedColumns, setSelectedColumns] = useState({});
  const [selectAll, setSelectAll] = useState(false);
  const [division, setDivision] = useState("0");
  const [dataTier, setDataTier] = useState("0");
  const [isDocumentationUpdated, setIsDocumentationUpdated] = useState(false);
  const [workspaceMetadata, setWorkspaceMetadata] = useState(null);
  const [workspaceCreator, setWorkspaceCreator] = useState(null);
  const [description, setDescription] = useState(null);
  const [showCdcLogin, setShowCdcLogin] = useState(false);
  const [isAlreadyPublished, setIsAlreadyPublished] = useState(false);
  const [schemaEnabled, setSchemaEnabled] = useState(true);
  const [schemaCheckLoading, setSchemaCheckLoading] = useState(true);
  const [localMismatches, setLocalMismatches] = useState(mismatchesProp);
  const [previouslyPublishedTables, setPreviouslyPublishedTables] = useState([]);
  const previouslyEnabledColumnsRef = useRef({});
  const isAutoPopulatingRef = useRef(false);

  const methods = useForm();
  const { 
    setValue, 
    handleSubmit, 
    register, 
    formState: { errors }
  } = methods;

  const [dataTierError, setDataTierError] = useState('');
  const [divisionError, setDivisionError] = useState('');

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    if (!isAutoPopulatingRef.current) return;
    const tierEl = document.getElementById('dataTierField');
    if (tierEl && dataTier !== '0') {
      tierEl.value = dataTier;
      SelectBox.defaultSetup(true);
    }
  }, [dataTier]);

  useEffect(() => {
    if (!isAutoPopulatingRef.current) return;
    if (Array.isArray(division) && division.length > 0) {
      const divEl = document.getElementById('divisionField');
      if (divEl) {
        Array.from(divEl.options).forEach(opt => {
          opt.selected = division.includes(opt.value);
        });
        setTimeout(() => {
          SelectBox.defaultSetup(true);
        }, 0);
      }
    }
  }, [division]);

  useEffect(() => {
    if (mismatchesProp.length > 0) {
      setLocalMismatches(mismatchesProp);
      return;
    }
    if (!workspaceId || !lakehouseId) return;
    fabricApi.checkTableMismatch(workspaceId, lakehouseId)
      .then((res) => {
        const data = res?.data;
        if (data?.hasMismatch && data.mismatches?.length > 0) {
          setLocalMismatches(data.mismatches);
        }
      })
      .catch(() => {});
  }, [workspaceId, lakehouseId, mismatchesProp]);

  useEffect(() => {
    ProgressIndicator.show();
    fabricApi.getLakehouseTables(workspaceId, lakehouseId)
      .then(res => {
        const fetchedTables = res?.data?.data?.tables || [];
        // Component state and payloads use tableName keys, while upstream may return schema variants.
        const tablesByName = new Map();
        fetchedTables.forEach(table => {
          const existingTable = tablesByName.get(table.tableName);
          const hasSchemaName = table.schemaName && table.schemaName.trim() !== '';
          const existingHasSchemaName = existingTable?.schemaName && existingTable.schemaName.trim() !== '';
          if (!existingTable || (!existingHasSchemaName && hasSchemaName)) {
            tablesByName.set(table.tableName, table);
          }
        });
        const uniqueTables = Array.from(tablesByName.values());
        setTables(uniqueTables);
        // Judge schema support from raw rows because schema-less lakehouses also expose a dbo SQL-endpoint copy.
        const hasSchemaName = fetchedTables.length > 0
          && fetchedTables.every(t => t.schemaName && t.schemaName.trim() !== '');
        setSchemaEnabled(hasSchemaName);
        setSchemaCheckLoading(false);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        setSchemaCheckLoading(false);
        ProgressIndicator.hide();
        if (e?.response?.status === 403) {
          Notification.show('Unauthorized to view this page or not found', 'alert');
          history.push(`/`);
        } else {
          Notification.show(e?.response?.data?.errors?.[0]?.message || 'Fetching tables failed!', 'alert');
        }
      });
  }, [workspaceId, lakehouseId]);

  useEffect(() => {

    if (!workspaceId) return;

    fabricApi.getFabricWorkspace(workspaceId)
      .then(res => {
        const data = res?.data; 
        setWorkspaceMetadata(data);
        setWorkspaceCreator(data?.createdBy);

        const isPublished = isLakehouseAlreadyPublished(
          data?.cdcPublishedLakeHouseDetails,
          lakehouseId,
          lakehouseName
        );
        console.log('[CdcPush] isPublished:', isPublished, 'lakehouseId:', lakehouseId, 'lakehouseName:', lakehouseName, 'publishedNames:', data?.cdcPublishedLakeHouseDetails?.publishedLakeHouseNames);

        if (isPublished) {
          setHasPushedOnce(true);
        }

        if (isPublished && data?.name) {
          console.log('[CdcPush] Fetching catalog metadata for serviceName:', data.name);
          fabricApi.getCatalogMetadata(workspaceId, data.name)
            .then((metaRes) => {
              console.log('[CdcPush] getCatalogMetadata response:', JSON.stringify(metaRes?.data).substring(0, 500));
              const cdcCatalogs = metaRes?.data?.data?.publishedCDCCatalogs || [];
              console.log('[CdcPush] cdcCatalogs count:', cdcCatalogs.length, 'looking for lakeHouseId:', lakehouseId);
              const lakehouseEntry = cdcCatalogs.find(c => c.lakeHouseId === lakehouseId);
              if (!lakehouseEntry) {
                console.warn('[CdcPush] No matching lakehouse entry found. Available lakeHouseIds:', cdcCatalogs.map(c => c.lakeHouseId));
                return;
              }

              console.log('[CdcPush] Found lakehouse entry, mandatoryFields:', JSON.stringify(lakehouseEntry.mandatoryFields));
              isAutoPopulatingRef.current = true;
              const mf = lakehouseEntry.mandatoryFields;
              if (mf) {
                if (mf.tier) {
                  const tierLabel = TIER_REVERSE_MAP[mf.tier];
                  console.log('[CdcPush] Setting tier:', mf.tier, '->', tierLabel);
                  if (tierLabel) setDataTier(tierLabel);
                }
                if (mf.divisions?.length) {
                  console.log('[CdcPush] Raw divisions from API:', mf.divisions);
                  // Normalize enum names (e.g. MERCEDES_BENZ_CARS) to display names
                  const normalized = mf.divisions.map(d => {
                    if (DIVISIONS.includes(d)) return d;
                    const match = DIVISIONS.find(name =>
                      name.toUpperCase().replace(/[^A-Z0-9]/g, '_') === d
                    );
                    return match || d;
                  });
                  console.log('[CdcPush] Normalized divisions:', normalized);
                  setDivision(normalized);
                }
                if (mf.isDocumentationUpdated) {
                  console.log('[CdcPush] Setting isDocumentationUpdated:', mf.isDocumentationUpdated);
                  setIsDocumentationUpdated(mf.isDocumentationUpdated === 'Yes');
                }
              }

              const databases = metaRes?.data?.data?.metadata?.databases || [];
              const db = databases.find(d => d.dbName === lakehouseName) || databases.find(d => d.dbId === lakehouseId) || databases[0];
              if (db?.description) {
                console.log('[CdcPush] Setting description:', db.description);
                setDescription(db.description);
                setValue('description', db.description, { shouldValidate: true, shouldDirty: true });
              }

              const tableDetails = lakehouseEntry.publishedLakehouseTableDetails || [];
              console.log('[CdcPush] Raw publishedLakehouseTableDetails:', JSON.stringify(tableDetails.map(t => ({ name: t.tableName, enabled: t.enabled, cols: (t.columns || []).length }))));
              const enabledTableNames = tableDetails
                .filter(t => t.enabled === true)
                .map(t => t.tableName);
              console.log('[CdcPush] Enabled tables to pre-select:', enabledTableNames, 'from', tableDetails.length, 'total');

              const enabledColsByTable = {};
              tableDetails.filter(t => t.enabled === true).forEach(t => {
                if (t.columns && t.columns.length > 0) {
                  enabledColsByTable[t.tableName] = t.columns
                    .filter(c => c.enabled === true)
                    .map(c => c.columnName);
                }
              });
              previouslyEnabledColumnsRef.current = enabledColsByTable;
              console.log('[CdcPush] Enabled columns by table:', JSON.stringify(enabledColsByTable));

              if (enabledTableNames.length > 0) {
                setPreviouslyPublishedTables(enabledTableNames);
              }
              setTimeout(() => { isAutoPopulatingRef.current = false; }, 200);
            })
            .catch((err) => {
              console.error('[CdcPush] Failed to fetch catalog metadata for auto-populate:', err?.response?.status, err?.message);
            });
        }
      })
      .catch((e) => {
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Failed to fetch workspace metadata',
          'alert'
        );
      });
  }, [workspaceId, lakehouseName, lakehouseId, setValue]);

  // Auto-select previously published tables once both tables list and publish history are loaded
  useEffect(() => {
    if (previouslyPublishedTables.length > 0 && tables.length > 0) {
      const autoSelectedTables = {};
      tables.forEach((t) => {
        if (previouslyPublishedTables.includes(t.tableName)) {
          autoSelectedTables[t.tableName] = true;
        }
      });
      if (Object.keys(autoSelectedTables).length > 0) {
        setSelectedTables(autoSelectedTables);
        setSelectAll(Object.keys(autoSelectedTables).length === tables.length);

        // Fetch columns for each pre-selected table and restore column selections
        Object.keys(autoSelectedTables).forEach((tableName) => {
          const tableObj = tables.find(t => t.tableName === tableName);
          const schemaName = tableObj?.schemaName || 'dbo';
          if (!columnsByTable[tableName]) {
            fabricApi.getTableSchema(workspaceId, lakehouseId, tableName, schemaName)
              .then((res) => {
                const fetchedColumns = res?.data?.data?.columns || [];
                setColumnsByTable(prev => ({ ...prev, [tableName]: fetchedColumns }));
                // Restore column selections from stored enabled flags
                const storedCols = previouslyEnabledColumnsRef.current[tableName] || [];
                const colSelections = fetchedColumns.reduce((acc, col) => {
                  acc[col.columnName] = storedCols.length > 0
                    ? storedCols.includes(col.columnName)
                    : true;
                  return acc;
                }, {});
                setSelectedColumns(prev => ({ ...prev, [tableName]: colSelections }));
                 const allColsSelected = fetchedColumns.every(col => colSelections[col.columnName]);
                setSelectedTables(prev => ({ ...prev, [tableName]: allColsSelected }));
                setSelectedTables(prev => {
                  const allTablesFullySelected = tables.every(t => prev[t.tableName]);
                  setSelectAll(allTablesFullySelected);
                  return prev;
                });
              })
              .catch(() => {});
          }
        });
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [previouslyPublishedTables, tables, workspaceId, lakehouseId]);

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, [tables]);

  const toggleTableSelect = async (tableName) => {
    const isSelected = !!selectedTables[tableName];
    const newSelected = { ...selectedTables, [tableName]: !isSelected };
    setSelectedTables(newSelected);

    const tableObj = tables.find(t => t.tableName === tableName);
    const schemaName = tableObj?.schemaName;

    if (!isSelected && !columnsByTable[tableName]) {
      try {
        const res = await fabricApi.getTableSchema(workspaceId, lakehouseId, tableName, schemaName);
        const fetchedColumns = res?.data?.data?.columns || [];

        setColumnsByTable(prev => ({
          ...prev,
          [tableName]: fetchedColumns
        }));

        const colSelections = fetchedColumns.reduce((acc, col) => {
          acc[col.columnName] = true;
          return acc;
        }, {});
        setSelectedColumns(prev => ({ ...prev, [tableName]: colSelections }));

      } catch (err) {
        Notification.show(`Failed to load columns for ${tableName}`, 'alert');
      }
    }

    if (!isSelected && columnsByTable[tableName]) {
      const colSelections = columnsByTable[tableName].reduce((acc, col) => {
        acc[col.columnName] = true;
        return acc;
      }, {});
      setSelectedColumns(prev => ({ ...prev, [tableName]: colSelections }));
    }

    if (isSelected) {
      const newCols = { ...selectedColumns };
      delete newCols[tableName];
      setSelectedColumns(newCols);
    }
  };

  const toggleColumnSelect = (tableName, columnName) => {
    setSelectedColumns(prev => {
      const prevTableColumns = prev[tableName] || {};
      const updatedTableColumns = {
        ...prevTableColumns,
        [columnName]: !prevTableColumns[columnName],
      };

      const allColumnNames = columnsByTable[tableName]?.map(col => col.columnName) || [];
      const allSelected = allColumnNames.every(name => updatedTableColumns[name]);

      setSelectedTables(prevTables => ({
        ...prevTables,
        [tableName]: allSelected,
      }));

      return {
        ...prev,
        [tableName]: updatedTableColumns,
      };
    });
  };

  const toggleSelectAllTables = async () => {
    const newValue = !selectAll;
    setSelectAll(newValue);

    const allSelectedTables = {};
    const allSelectedColumns = {};
    const updatedColumnsByTable = { ...columnsByTable };

    for (const t of tables) {
      const tableName = t.tableName;
      const schemaName = t.schemaName || 'dbo';
      allSelectedTables[tableName] = newValue;

      if (newValue && !columnsByTable[tableName]) {
        try {
          const res = await fabricApi.getTableSchema(workspaceId, lakehouseId, tableName, schemaName);
          const fetchedColumns = res?.data?.data?.columns || [];
          updatedColumnsByTable[tableName] = fetchedColumns;

          const allCols = fetchedColumns.reduce((acc, col) => {
            acc[col.columnName] = true;
            return acc;
          }, {});
          allSelectedColumns[tableName] = allCols;
        } catch (err) {
          Notification.show(`Failed to load columns for ${tableName}`, 'alert');
        }
      } else if (newValue && columnsByTable[tableName]) {
        const allCols = columnsByTable[tableName].reduce((acc, col) => {
          acc[col.columnName] = true;
          return acc;
        }, {});
        allSelectedColumns[tableName] = allCols;
      }
    }

    setColumnsByTable(updatedColumnsByTable);
    setSelectedTables(allSelectedTables);
    setSelectedColumns(allSelectedColumns);
  };


  const handlePush = useCallback(async () => {
    const leanIXId = workspaceMetadata?.appId;
    if (!leanIXId) {
      Notification.show("Cannot push to CDC. LeanIX ID is missing.", 'alert');
      return;
    }

    let hasError = false;
    setDivisionError('');
    setDataTierError('');

    if (division === "0" || !division || (Array.isArray(division) && division.length === 0)) {
      setDivisionError("*Missing entry");
      hasError = true;
    }
    if (dataTier === "0" || !dataTier) {
      setDataTierError("*Missing entry");
      hasError = true;
    }

    if (hasError) {
      return;
    }

    const payload = buildCdcPayload({
      workspaceId,
      workspaceMetadata,
      lakehouseId,
      lakehouseName,
      selectedTables,
      selectedColumns,
      columnsByTable,
      tables,
      dataTier: DATA_TIER_MAP[dataTier] || null,
      divisions: division || [],
      isDocumentationUpdated,
      description,
      workspaceCreator
    });

    ProgressIndicator.show();
    const pushApi = isAlreadyPublished
      ? fabricApi.updatePublishedTables(workspaceId, payload)
      : fabricApi.pushSelectedTables(workspaceId, payload);
    pushApi
      .then(() => {
        const publishedSnapshot = {
          workspaceId,
          lakehouseId,
          lakehouseName,
          publishedAt: new Date().toISOString(),
          tables: tables.filter(t => selectedTables[t.tableName]).map(table => ({
            tableName: table.tableName,
            schemaName: table.schemaName,
            columns: columnsByTable[table.tableName]?.map(col => ({
              columnName: col.columnName,
              colType: col.colType
            })) || []
          }))
        };

        fabricApi.saveLakehouseSnapshot(workspaceId, lakehouseId, publishedSnapshot)
          .catch(err => {
            console.error('Failed to save snapshot:', err);
          });

        ProgressIndicator.hide();
        Notification.show("Push to CDC successful!", "success");

        if (onRefreshWorkspace) {
          onRefreshWorkspace();
        }
      })
      .catch((e) => {
        ProgressIndicator.hide();

        if (e?.response?.status === 400) {
          Notification.show("Failed to publish fabric workspace catalog: User didn't log in to CDC", "alert");
          setShowCdcLogin(true);
          return;
        }

        const backendMessage =
          e?.response?.data?.responses?.errors?.[0]?.message ||
          e?.response?.data?.errors?.[0]?.message ||
          '';

        Notification.show(backendMessage, 'alert');
      });


  }, [
    workspaceId,
    workspaceMetadata,
    division,
    dataTier,
    lakehouseId,
    lakehouseName,
    columnsByTable,
    tables,
    selectedTables,
    selectedColumns,
    isDocumentationUpdated,
    description,
    workspaceCreator,
    onRefreshWorkspace,
    isAlreadyPublished,
  ]);

  const onPush = handleSubmit(handlePush);
  const isLakehousePublished = isLakehouseAlreadyPublished(
    workspaceMetadata?.cdcPublishedLakeHouseDetails,
    lakehouseId,
    lakehouseName
  );

  const isPushDisabled =
  !workspaceMetadata || 
  !schemaEnabled ||
  schemaCheckLoading ||
  Object.keys(selectedTables).length === 0 ||
  Object.keys(selectedColumns).length === 0;

    return (
    <div className={Styles.modalFAQContentWrapper}>
      {localMismatches.length > 0 && (
        <div className={Styles.schemaChangesPanel}>
          <div className={Styles.schemaChangesPanelHeader}>
            <i className="icon mbc-icon alert circle" />
            <span>Schema Changes Detected</span>
            <span className={Styles.schemaChangesBadgeCount}>{localMismatches.length} change{localMismatches.length !== 1 ? 's' : ''}</span>
          </div>
          <div className={Styles.schemaChangesList}>
            {localMismatches.map((mismatch, idx) => {
              const config = MISMATCH_TYPE_CONFIG[mismatch.mismatchType] || { label: mismatch.mismatchType?.replace(/_/g, ' '), colorClass: 'badgeModified' };
              return (
                <div key={idx} className={Styles.schemaChangeItem}>
                  <div className={Styles.schemaChangeItemHeader}>
                    <span className={Styles.schemaChangeTableName}>{mismatch.tableName}</span>
                    <span className={classNames(Styles.schemaChangeBadge, Styles[config.colorClass])}>
                      {config.label}
                    </span>
                  </div>
                  {mismatch.details && (
                    <p className={Styles.schemaChangeDetails}>{mismatch.details}</p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

        {!schemaCheckLoading && !schemaEnabled && (
          <div className={Styles.schemaWarningBanner}>
            <i className="icon mbc-icon alert circle"></i>
            <span>CDC Push requires Lakehouse Schemas to be enabled. Please recreate the lakehouse with the Lakehouse Schemas option checked.</span>
          </div>
        )}

        <div className={Styles.flex}>
          <div className={Styles.col3}>
            <div
              className={classNames(
                'input-field-group include-error',
                dataTierError.length ? 'error' : '',
              )}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Tier <sup>*</sup>
              </label>
              <div className={classNames('custom-select')}>
                <select
                  id="dataTierField"
                  defaultValue={dataTier}
                  onChange={(e) => {
                    setDataTier(e.target.value);
                    if (dataTierError) setDataTierError(""); 
                  }}
                >
                  <option value={0}>Choose</option>
                  {DATA_TIER?.map((tier, index) => (
                    <option key={index} value={tier}>
                      {tier}
                    </option>
                  ))}
                </select>
              </div>
              <span
                className={classNames(
                  'error-message',
                  dataTierError.length ? '' : 'hide'
                )}
              >
                {dataTierError}
              </span>
            </div>
          </div>

          <div className={Styles.col3}>
            <div
              className={classNames('input-field-group include-error',
                divisionError.length ? 'error' : '',
              )}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Division <sup>*</sup>
              </label>
              <div className={classNames('custom-select')}>
                <select
                  id="divisionField"
                  multiple={true}
                  // defaultValue={division}
                  onChange={(e) => {
                    const values = Array.from(e.target.selectedOptions, opt => opt.value);
                    setDivision(values);
                    if (divisionError) setDivisionError(""); 
                  }}
                >
                  {/* <option id="divisionOption" value={0}>
                    Choose
                  </option> */}
                  {DIVISIONS.map((name, index) => (
                    <option key={index} value={name}>
                      {name}
                    </option>
                  ))}
                </select>
              </div>
              <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                {divisionError}
              </span>
            </div>
          </div>

          <div className={Styles.col3}>
            <div className={classNames('input-field-group include-error')}>
              <label className="input-label">Documentation Updated <sup>*</sup></label>
              <div className={Styles.boolean}>
                <label className={classNames('radio')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="true"
                      name="documentationUpdated"
                      checked={isDocumentationUpdated === true}
                      onClick={() => {
                        setIsDocumentationUpdated(true);
                        setValue('documentationUpdated', 'true');
                      }}
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
                <label className={classNames('radio')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="false"
                      name="documentationUpdated"
                      checked={isDocumentationUpdated === false}
                      onClick={() => {
                        setIsDocumentationUpdated(false);
                        setValue('documentationUpdated', 'false');
                      }}
                    />
                  </span>
                  <span className="label">No</span>
                </label>
              </div>
            </div>
          </div>
          <div className={Styles.col3}></div>
        </div>

        <div className={Styles.col}>
          <div className={classNames('input-field-group include-error area', errors.description ? 'error' : '')}>
            <label id="description" className="input-label" htmlFor="description">
              Description <sup>*</sup>
            </label>
            <textarea
              id="description"
              className={'input-field-area'}
              type="text"
              defaultValue={description}
              rows={50}
              {...register('description', { required: '*Missing entry', pattern: /^(?!\s+$)(\s*\S+\s*)+$/, onChange: (e) => { setDescription(e.target.value) } })}
            />
            <span className={'error-message'}>{errors?.description?.message}{errors.description?.type === 'pattern' && `Spaces (and special characters) not allowed as field value.`}</span>
          </div>
        </div>  

      <div className={Styles.tableRow}>
        <div className={Styles.checkboxWrapper}>
          <label className="checkbox" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                checked={selectAll}
                onClick={toggleSelectAllTables}
              />
            </span>
            <div className={Styles.selectAllLabel}> All </div>
          </label>
        </div>

      </div>

      <div className="expansion-panel-group">
        {tables.map((table, idx) => {
          const panelId = `table-${idx}`;
          const tableName = table.tableName;

          return (
            <div key={tableName} className="expansion-panel">
              <span className="animation-wrapper" />
              <input type="checkbox" className="ff-only" id={panelId} />
              <div className={Styles.tableRow}>
                <div className={Styles.checkboxWrapper}>
                  <label className="checkbox" >
                    <span className="wrapper">
                      <input
                        type="checkbox"
                        className="ff-only"
                        checked={!!selectedTables[tableName]}
                        onClick={() => toggleTableSelect(tableName)}
                        style={{ opacity: 1, position: 'static', zIndex: 9999 }}
                        
                      />
                    </span>
                  </label>
                </div>
                <label
                  htmlFor={panelId}
                  className={classNames('expansion-panel-label', Styles.tableHeaderLabel)}
                  onClick={() => {
                    if (!columnsByTable[tableName]) {
                      ProgressIndicator.show();
                      fabricApi.getTableSchema(workspaceId, lakehouseId, tableName, table.schemaName)
                        .then(res => {
                          const cols = res.data.data.columns || [];
                          setColumnsByTable(prev => ({ ...prev, [tableName]: cols }));
                          if (selectedTables[tableName]) {
                            const storedCols = previouslyEnabledColumnsRef.current[tableName];
                            const colSelections = cols.reduce((acc, col) => {
                              acc[col.columnName] = storedCols !== undefined
                                ? storedCols.includes(col.columnName)
                                : true;
                              return acc;
                            }, {});
                            setSelectedColumns(prev => ({ ...prev, [tableName]: colSelections }));
                          }
                          ProgressIndicator.hide();
                        })
                        .catch((e) => {
                          ProgressIndicator.hide();
                          Notification.show(e?.response?.data?.errors?.[0]?.message || 'Fetching Columns failed!', 'alert');
                        });
                    }
                  }}
                >
                  <span className={Styles.tableName}>{tableName}</span>
                  <i tooltip-data="Expand" className="icon down-up-flip" />
                </label>
              </div>

              <div className="expansion-panel-content">
                {columnsByTable[tableName]?.length > 0 ? (
                  <div className={Styles.tableExpandedContent}>
                    <div className={Styles.tableColumnsHeader}>
                      <div></div>
                      <div>Column Name</div>
                      <div>Column Type</div>
                    </div>
                    {columnsByTable[tableName].map((col, colIdx) => (
                      <div key={colIdx} className={Styles.tableColumnRow}>
                        <div className={Styles.checkboxWrapper}>
                          <label className="checkbox">
                            <span className="wrapper">
                              <input
                                type="checkbox"
                                className="ff-only"
                                checked={!!selectedColumns?.[tableName]?.[col.columnName]}
                                onClick={() => toggleColumnSelect(tableName, col.columnName)}
                                style={{ opacity: 1, position: 'static', zIndex: 9999 }}
                              />
                            </span>
                          </label>
                        </div>
                        <div className={Styles.columnName}>{col.columnName}</div>
                        <div className={Styles.columnType}>{col.colType}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className={Styles.tableRow}>
                    <span>No columns found...</span>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className={Styles.disclaimer}>
        <p>
          ** Please be advised that your onboarding process at the CDC is conducted virtually.
          As a result, you are responsible for maintaining and updating any changes in the
          database that pertain to your profile or assigned data. This includes ensuring the
          accuracy and timeliness of all relevant information. Should you require assistance
          or clarification, please do not hesitate to reach out to the support team. 
          Email:&nbsp;
          <a href={`mailto:${Envs.DNA_MAIL}`} target="_blank" rel="noreferrer">{Envs.DNA_MAIL}</a> **
        </p>
      </div>

      <div className={Styles.pushButtonContainer}>
        <button className={isPushDisabled ? classNames("btn btn-primary") : classNames("btn btn-tertiary")} type="button" disabled={isPushDisabled} onClick={onPush}>
          Push
        </button>
      </div>

        {showCdcLogin && (
          <div className={Styles.loginCDC}>
            <div className={Styles.loginCDCtext}>
              <i className="icon mbc-icon alert circle" />
              Looks like you have not logged in to CDC application before, because of which
              you are not allowed to make your push. Hence {" "} {" "}
              <a
                href={Envs.CDC_SIGNIN_URL}
                target="_blank"
                rel="noopener noreferrer"
                className={Styles.loginCDCLink}
              >
                 Login To CDC <i className={`icon mbc-icon new-tab ${Styles.loginIcon}`} />
              </a>
              {" "}first and then come back to push your data.
            </div>
          </div>
        )}
    </div>
  );
}
export default ViewTablesModalContent;