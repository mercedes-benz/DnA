import classNames from 'classnames';
import React, { useState, useEffect, useCallback} from 'react';
import Styles from './CdcPush.scss';
import SelectBox from 'dna-container/SelectBox';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import {DIVISIONS, DATA_ORIGINS } from '../../utilities/constants';
import { fabricApi } from '../../apis/fabric.api';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';
import { useForm } from 'react-hook-form';
import { Envs } from '../../utilities/envs';

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
  dataOrigin,
  isDataLakeAvailability,
  isDocumentationUpdated,
  isDataAsset,
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
      dataOrigin: dataOrigin || "",
      isDataLakeAvailability: isDataLakeAvailability ? "Yes" : "No",
      leanIXId: workspaceMetadata?.appId || "",
      isDocumentationUpdated: isDocumentationUpdated ? "Yes" : "No",
      isDataAsset: isDataAsset ? "Yes" : "No",
      dataConfidentiality: workspaceMetadata?.dataClassification.toLowerCase() || ""
    },
    owners: [workspaceCreator]
  };
};


const ViewTablesModalContent = ({ workspaceId, lakehouseId, lakehouseName }) => {
  const [tables, setTables] = useState([]);
  const [columnsByTable, setColumnsByTable] = useState({});
  const [selectedTables, setSelectedTables] = useState({});
  const [selectedColumns, setSelectedColumns] = useState({});
  const [selectAll, setSelectAll] = useState(false);
  const [division, setDivision] = useState("0");
  const [dataOrigin, setDataOrigin] = useState("0");
  const [isDocumentationUpdated, setIsDocumentationUpdated] = useState(false);
  const [isDataAsset, setIsDataAsset] = useState(false);
  const [isDataLakeAvailability, setIsDataLakeAvailability] = useState(true);
  const [workspaceMetadata, setWorkspaceMetadata] = useState(null);
  const [workspaceCreator, setWorkspaceCreator] = useState(null);
  const [description, setDescription] = useState(null);
  const [showCdcLogin, setShowCdcLogin] = useState(false);


  const methods = useForm();
  const { 
    setValue, 
    handleSubmit, 
    register, 
    formState: { errors }
  } = methods;


  const [dataOriginError, setDataOriginError] = useState('');
  const [divisionError, setDivisionError] = useState('');

  useEffect(() => {
    // setDivisions(DIVISIONS);
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
    fabricApi.getLakehouseTables(workspaceId, lakehouseId)
      .then(res => {
        setTables(res?.data?.data?.tables || []);
        ProgressIndicator.hide();
      })
      .catch((e) => {
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
    // console.log("workspaceId for metadata fetch:", workspaceId);
    if (!workspaceId) return;

    fabricApi.getFabricWorkspace(workspaceId)
      .then(res => {
        const data = res?.data; 
        setWorkspaceMetadata(data);
        setWorkspaceCreator(data?.createdBy);
      })
      .catch((e) => {
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Failed to fetch workspace metadata',
          'alert'
        );
      });
  }, [workspaceId]);

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


  const handlePush = useCallback(() => {
    const leanIXId = workspaceMetadata?.appId;
    if (!leanIXId) {
      Notification.show("Cannot push to CDC. LeanIX ID is missing.", 'alert');
      return;
    }

    let hasError = false;

    if (division === "0" || !division) {
      setDivisionError("*Missing entry");
      hasError = true;
    }
    if (dataOrigin === "0" || !dataOrigin) {
      setDataOriginError("*Missing entry");
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
      dataOrigin: (dataOrigin || '').toLowerCase(),
      divisions: division || [],
      isDataAsset,
      isDocumentationUpdated,
      isDataLakeAvailability,
      description,
      workspaceCreator
    });

    ProgressIndicator.show();
    fabricApi.pushSelectedTables(workspaceId, payload)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show("Push to CDC successful!", "success");
      })
      .catch((e) => {
        ProgressIndicator.hide();

        if (e?.response?.status === 403) {
          Notification.show("Forbidden: User Info not found in CDC", "alert");
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
    dataOrigin,
    lakehouseId,
    lakehouseName,
    columnsByTable,
    tables,
    selectedTables,
    selectedColumns,
    isDataAsset,
    isDocumentationUpdated,
    isDataLakeAvailability,
    description,
    workspaceCreator,
  ]);

  const onPush = handleSubmit(handlePush);


    return (
    <div className={Styles.modalFAQContentWrapper}>

        <div className={Styles.flex}>
          <div className={Styles.col3}>
            <div
              className={classNames(
                'input-field-group include-error',
                dataOriginError.length ? 'error' : '',
              )}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Data Origin <sup>*</sup>
              </label>
              <div className={classNames('custom-select')}>
                <select
                  id="dataOriginField"
                  defaultValue={dataOrigin}
                  onChange={(e) => {
                    setDataOrigin(e.target.value);
                    if (dataOriginError) setDataOriginError(""); 
                  }}
                >
                  <option value={0}>Choose</option>
                  {DATA_ORIGINS?.map((origin, index) => (
                    <option key={index} value={origin}>
                      {origin}
                    </option>
                  ))}
                </select>
              </div>
              <span
                className={classNames(
                  'error-message',
                  dataOriginError.length ? '' : 'hide'
                )}
              >
                {dataOriginError}
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
              <label className="input-label">Data Asset <sup>*</sup></label>
              <div className={Styles.boolean}>
                <label className={classNames('radio')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="true"
                      name="dataAsset"
                      checked={isDataAsset === true}
                      onClick={() => {
                        setIsDataAsset(true);
                        setValue('dataAsset', 'true');
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
                      name="dataAsset"
                      checked={isDataAsset === false}
                      onClick={() => {
                        setIsDataAsset(false);
                        setValue('dataAsset', 'false');
                      }}
                    />
                  </span>
                  <span className="label">No</span>
                </label>
              </div>
            </div>
          </div>

          <div className={Styles.col3}>
            <div className={classNames('input-field-group include-error')}>
              <label className="input-label">DataLake Available <sup>*</sup></label>
              <div className={Styles.boolean}>
                <label className={classNames('radio')}>
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      value="true"
                      name="dataLakeAvailability"
                      checked={isDataLakeAvailability === true}
                      onClick={() => {
                        setIsDataLakeAvailability(true);
                        setValue('dataLakeAvailability', 'true');
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
                      name="dataLakeAvailability"
                      checked={isDataLakeAvailability === false}
                      onClick={() => {
                        setIsDataLakeAvailability(false);
                        setValue('dataLakeAvailability', 'false');
                      }}
                    />
                  </span>
                  <span className="label">No</span>
                </label>
              </div>
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
                            const colSelections = cols.reduce((acc, col) => {
                              acc[col.columnName] = true;
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

      <div className={Styles.pushButtonContainer}>
        <button className={(Object.keys(selectedTables).length === 0 || Object.keys(selectedColumns).length === 0) ? classNames("btn btn-primary") : classNames("btn btn-tertiary")} type="button" disabled={Object.keys(selectedTables).length === 0 || Object.keys(selectedColumns).length === 0} onClick={onPush}>
          Push
        </button>
      </div>

        {showCdcLogin && (
          <div className={Styles.loginCDC}>
            <div className={Styles.loginCDCtext}>
              <i className="icon mbc-icon alert circle" />
              Looks like you have not logged in to CDC application before and because of which
              you are not allowed to make your push. Hence {" "} {" "}
              <a
                href={Envs.CDC_URL}
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

