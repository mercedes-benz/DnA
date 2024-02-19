import classNames from 'classnames';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './graph.scss';
// dna-container
import FullScreenModeIcon from 'dna-container/FullScreenModeIcon';
import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import SelectBox from '../../common/modules/uilab/js/src/select';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Notification from '../../common/modules/uilab/js/src/notification';
import GraphTable from '../../components/GraphTable';
import TableForm from '../../components/tableForm/TableForm';
import SlidingModal from '../../components/slidingModal/SlidingModal';
import Spinner from '../../components/spinner/Spinner';
import { setBox, setTables } from '../../redux/graphSlice';
import { getProjectDetails } from '../../redux/graph.services';
import TableCollaborators from '../../components/tableCollaborators/TableCollaborators';
import { datalakeApi } from '../../apis/datalake.api';
import ColumnForm from '../../components/columnForm/ColumnForm';
import EditTableForm from '../../components/editTableForm/EditTableForm';
import DataProductForm from '../../components/dataProductForm/DataProductForm';
import { ConnectionModal } from '../../components/connectionInfo/ConnectionModal';

const Graph = ({user}) => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const {
    box,
    version,
    project,
    isLoading,
  } = useSelector(state => state.graph);

  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const isOwner = user.id === project?.createdBy?.id;

  useEffect(() => {
    dispatch(getProjectDetails(id));
  }, [id, dispatch]);

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const [loading, setLoading] = useState(true);
  const [connectionInfo, setConnectionInfo] = useState();
  const [hasTable, setHasTable] = useState(project.tables.length > 0);
  useEffect(() => {
    setHasTable(project.tables.length > 0);
  }, [project.tables])

  useEffect(() => {
    ProgressIndicator.show();
    datalakeApi.getConnectionInfo(id)
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
  }, [id]);

  /* A callback function that is used to update the viewbox of the svg. */
  const resizeHandler = useCallback(() => {
    dispatch(setBox({
      x: box.x,
      y: box.y,
      w:
        box.w && box.clientW
          ? box.w * (window.innerWidth / box.clientW)
          : window.innerWidth,
      h:
        box.h && box.clientH
          ? box.h * (window.innerHeight / box.clientH)
          : window.innerHeight,
      clientW: window.innerWidth,
      clientH: window.innerHeight,
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    window.addEventListener('resize', resizeHandler);

    return () => {
      window.removeEventListener('resize', resizeHandler);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    resizeHandler();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);


  // const tableList = useMemo(() => Object.values(tableDict), [tableDict]);
  const svg = useRef();

  // ''|dragging|moving|linking
  const [mode, setMode] = useState('');
  // offset of svg origin
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const [movingTable, setMovingTable] = useState();

  // const [editingLink, setEditingLink] = useState(null);
  const [tableSelectedId, setTableSelectId] = useState(null);

  /**
   * It sets the offset to the mouse position relative to the box, and sets the mode to 'draging'
   */
  const mouseDownHandler = e => {
    if (e.target.tagName === 'svg' && e.button !== 2) {
      setOffset({
        x: box.x + (e.clientX * box.w) / global.innerWidth,
        y: box.y + (e.clientY * box.h) / global.innerHeight,
      });
      setMode('draging');
    }
  };

  /**
   * When the user releases the mouse button, if the user was in linking mode, and the user is not
   * linking the same table to itself, then add a new link to the link dictionary
   */
  const mouseUpHandler = () => {
    setMode('');
    setMovingTable(null);
  };

  /**
   * It sets the moving table to the table that was clicked on, and sets the mode to moving
   * @param e - the event object
   * @param table - the table object that was clicked on
   */
  const tableMouseDownHandler = (e, table) => {
    if (e.button === 2 || version !== 'currentVersion') return;
    const { x: cursorX, y: cursorY } = getSVGCursor(e);

    setMovingTable({
      id: table.tableName,
      offsetX: cursorX - table.xcoOrdinate,
      offsetY: cursorY - table.ycoOrdinate,
    });

    setMode('moving');
    e.preventDefault();
    // e.stopPropagation();
  };

  /**
   * It takes a mouse event and returns the cursor position in SVG coordinates
   * @returns The cursor position in the SVG coordinate system.
   */
  const getSVGCursor = ({ clientX, clientY }) => {
    let point = svg.current.createSVGPoint();
    point.x = clientX;
    point.y = clientY;

    return point.matrixTransform(svg.current.getScreenCTM().inverse());
  };

  /**
   * > When the mouse is moving, if the mode is 'draging', then update the box state with the new x
   * and y values. If the mode is 'moving', then update the tableDict state with the new x and y
   * values. If the mode is 'linking', then update the linkStat state with the new endX and endY
   * values
   */
  const mouseMoveHandler = e => {
    if (!mode) return;
    if (mode === 'draging') {
      dispatch(setBox({
        w: box.w,
        h: box.h,
        x: offset.x - e.clientX * (box.w / global.innerWidth),
        y: offset.y - e.clientY * (box.h / global.innerHeight),
        clientH: box.clientH,
        clientW: box.clientW,
      }));
    }

    if (mode === 'moving') {
      const { x: cursorX, y: cursorY } = getSVGCursor(e);
      const projectTables = project.tables.map(table =>
        table.tableName === movingTable.id ? {...table, xcoOrdinate: cursorX - movingTable.offsetX, ycoOrdinate: cursorY - movingTable.offsetY} : table
      );
      dispatch(setTables([...projectTables]));
    }
  };

  const [toggleModal, setToggleModal] = useState(false);
  const [showTechnicalUserModal, setShowTechnicalUserModal] = useState(false);

  const [graphState] = useState('unchanged');

  useEffect(() => {
    const handler = event => {
      event.preventDefault();
      event.returnValue = '';
    };
    if (graphState !== 'unchanged') {
      window.addEventListener('beforeunload', handler);
      // clean it up, if the dirty state changes
      return () => {
        window.removeEventListener('beforeunload', handler);
      };
    }
    // since this is not dirty, don't do anything
    return () => {};
  }, [graphState]);

  const handleEditTechnicalUser = (values) => {
    const data = {
      clientId: values.clientId,
      clientSecret: values.clientSecret
    }
    ProgressIndicator.show();
    datalakeApi.updateTechnicalUser(project?.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Techical user details updated successfully');
      setShowTechnicalUserModal(false);
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating technical user',
        'alert',
      );
    });
  }

  const technicalUserContent = <>
    <FormProvider {...methods}>
      <p>Create a Technical User to connect your publised tables to Trino REST and in Dataiku.</p>
      <div className={classNames('input-field-group include-error', errors?.clientId ? 'error' : '')}>
        <label className={classNames(Styles.inputLabel, 'input-label')}>
          Client ID <sup>*</sup>
        </label>
        <div>
          <input
            type="text"
            className={classNames('input-field')}
            id="clientId"
            placeholder="Type here"
            autoComplete="off"
            maxLength={55}
            defaultValue={!loading && (connectionInfo?.howToConnect?.trino?.techUserVO?.accesskey ? connectionInfo?.howToConnect?.trino?.techUserVO?.accesskey : '')}
            {...register('clientId', { required: '*Missing entry'})}
          />
          <span className={classNames('error-message')}>{errors?.clientId?.message}</span>
        </div>
      </div>
      <div className={classNames('input-field-group include-error', errors?.clientSecret ? 'error' : '')}>
        <label className={classNames(Styles.inputLabel, 'input-label')}>
          Client Secret <sup>*</sup>
        </label>
        <div>
          <input
            type="password"
            className={classNames('input-field')}
            id="clientSecret"
            placeholder="Type here"
            autoComplete="off"
            maxLength={55}
            // defaultValue={clientId}
            {...register('clientSecret', { required: '*Missing entry'})}
          />
          <span className={classNames('error-message')}>{errors?.clientSecret?.message}</span>
        </div>
      </div>
      <p>Keep your Client Secret safe. We do not store this information.</p>
      <div className={Styles.btnContainer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={handleSubmit((values) => {
            handleEditTechnicalUser(values)
          })}
        >
          Save
        </button>
      </div>
    </FormProvider>
  </>;

  const [showDataProductModal, setShowDataProductModal] = useState(false);

  const handleCreateDataProduct = () => {
    setShowDataProductModal(false);
  }

  const [showCollabModal, setShowCollabModal] = useState(false);
  const [table, setTable] = useState([]);

  const handleCollab = (table) => {
    setShowCollabModal(true);
    setTable({...table});
  }

  const handleDeleteTable = (tableName) => {
    const projectTemp = {...project};
    const tempTables = projectTemp.tables.filter(item => item.tableName !== tableName);
    projectTemp.tables = [...tempTables];
    dispatch(setTables(projectTemp.tables));
  }

  const [connectors,setConnectors] = useState([]);
  const [formats, setFormats] = useState([]);
  const [dataTypes, setDataTypes] = useState([]);

  useEffect(() => {
    if(project.connectorType === 'Iceberg') {
      const connector = connectors.filter(item => item.name === 'Iceberg');
      setFormats(connector[0] !== undefined ? [...connector[0].formats] : []);
      setDataTypes(connector[0] !== undefined ? [...connector[0].dataTypes] : []);
    }
    if(project.connectorType === 'Delta Lake') {
      const connector = connectors.filter(item => item.name === 'Delta Lake');
      setFormats(connector[0] !== undefined ? [...connector[0].formats] : []);
      setDataTypes(connector[0] !== undefined ? [...connector[0].dataTypes] : []);
    }
  }, [connectors, project]);

  useEffect(() => {
    ProgressIndicator.show();
    datalakeApi.getConnectors()
      .then((res) => {
        setConnectors(res.data.connectors);
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error while fetching connectors',
          'alert',
        );
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
      });
  }, []);

  const [showColumnModal, setShowColumnModal] = useState(false);
  const [showTableModal, setShowTableModal] = useState(false);
  const [selectedTable, setSelectedTable] = useState();
  const [selectedColumn, setSelectedColumn] = useState();
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const [columnEdit, setColumnEdit] = useState(false);

  const handleAddColumn = (table) => {
    setShowColumnModal(true);
    setSelectedTable({...table});
  }

  const handleEditColumn = (column, table, index) => {
    setShowColumnModal(true);
    setColumnEdit(true);
    setSelectedTable({...table});
    setSelectedColumn({...column});
    setSelectedIndex(index);
  }

  const handleEditTable = (table) => {
    setShowTableModal(true);
    setSelectedTable({...table});
  }

  const addColumn = (values) => {
    const tempTable = {...selectedTable};
    const columnData = {
      columnName: values.columnName,
      comment: values.comment,
      dataType: values.dataType,
      notNullConstraintEnabled: values.notNullConstraintEnabled,
    };

    const projectTemp = {...project};
    const tableIndex = projectTemp.tables.findIndex(item => item.tableName === tempTable.tableName);
    let newTables = [...projectTemp.tables];
    newTables[tableIndex] = {...newTables[tableIndex], columns: [...newTables[tableIndex].columns, columnData]};
    dispatch(setTables(newTables));
    setShowColumnModal(false);
    Tooltip.defaultSetup();
  }

  const editColumn = (values) => {
    const tempTable = {...selectedTable};
    const columnData = {
      columnName: values.columnName,
      comment: values.comment,
      dataType: values.dataType,
      notNullConstraintEnabled: values.notNullConstraintEnabled,
    };

    const projectTemp = {...project};
    const tableIndex = projectTemp.tables.findIndex(item => item.tableName === tempTable.tableName);
    let newTables = [...projectTemp.tables];
    let newColumns = [...newTables[tableIndex].columns];
    newColumns[selectedIndex] = {...columnData};
    newTables[tableIndex] = {...newTables[tableIndex], columns: [...newColumns]};
    dispatch(setTables(newTables));
    setShowColumnModal(false);
    setColumnEdit(false);
    Tooltip.defaultSetup();
  }

  const editTable = (values) => {
    const tempTable = {...selectedTable};
    const projectTemp = {...project};
    const tableIndex = projectTemp.tables.findIndex(item => item.tableName === tempTable.tableName);
    let newTables = [...projectTemp.tables];
    newTables[tableIndex] = {
      ...newTables[tableIndex],
      tableName: values.tableName,
      description: values.description,
      dataFormat: values.dataFormat
    };
    dispatch(setTables(newTables));
    setShowTableModal(false);
    Tooltip.defaultSetup();
  }

  const handlePublish = () => {
    const data = {...project}
    ProgressIndicator.show();
    datalakeApi.updateDatalakeProject(project?.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Table(s) published successfully');
      setHasTable(true);
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while publishing table(s)',
        'alert',
      );
    });
  }

  const [fullScreenMode, setFullScreenMode] = useState(false);

  const toggleFullScreenMode = () => {
    setFullScreenMode(!fullScreenMode);
  };

  const [showConnectionModal, setShowConnectionModal] = useState(false);

  const isValidFile = (file) => ['csv', 'parquet', 'json'].includes(file?.name?.split('.')[1]);
  const [uploadFile, setUploadFile] = useState({});
  const [showUploadModal, setShowUploadModal] = useState(false);
  const uploadContent = <>
    <div>
      <p>File to Upload: {uploadFile.length > 0 && uploadFile[0]?.name}</p>
      <div className={Styles.btnContainer}>
        <button className="btn" disabled={true}>Publish</button>
      </div>
    </div>
  </>

  return (
    !isLoading ?
      <div className={fullScreenMode ? Styles.datalakeWrapperFSmode : '' + ' ' + Styles.datalakeWrapper}>
        <div className={classNames(Styles.mainPanel)}>
          <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              {/* <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} /> */}
              <div className={Styles.nbtitle}>
                <button tooltip-data="Go Back" className="btn btn-text back arrow" onClick={() => { history.back() }}></button>
                <h2>{project?.projectName} <span>({project?.connectorType})</span></h2>
              </div>
            </div>
            <div className={Styles.navigation}>
              <div className={Styles.headerright}>
                {hasTable && (
                  <div>
                    <button
                      className={classNames('btn btn-primary', Styles.btnOutline, !isOwner && Styles.btnDisabled)}
                      type="button"
                      onClick={() => { setShowDataProductModal(true) }}
                    >
                      <i className="icon mbc-icon dataproductoverview" />
                      <span>Provision as a Data Product</span>
                    </button>
                  </div>)}
                {hasTable && (
                  <div>
                    <button
                      className={classNames('btn btn-primary', Styles.btnOutline)}
                      type="button"
                      onClick={() => { setShowConnectionModal(true) }}
                    >
                      <i className="icon mbc-icon comparison" />
                      <span>How to Connect</span>
                    </button>
                  </div>
                )}
                {hasTable && (
                  <div className={Styles.uploadFile}>
                    <input
                      type="file"
                      id="uploadFile"
                      name="uploadFile"
                      className={Styles.fileInput}
                      onChange={(e) => {
                        const isValid = isValidFile(e.target.files[0]);
                        if (!isValid) {
                          Notification.show('File is not valid. Only .csv, .parquet or .json files allowed.', 'alert');
                        } else {
                          setUploadFile(e.target.files);
                          setShowUploadModal(true);
                        }
                      }}
                      onClick={(e) => { e.target.value = '' }}
                      accept=".csv, .parquet, .json"
                    />
                    <button
                      className={classNames('btn btn-primary', Styles.btnOutline, !isOwner && Styles.btnDisabled)}
                      type="button"
                    >
                      <i className="icon mbc-icon upload" />
                      <label htmlFor="uploadFile" tooltip-data="Only .csv, .parquet, and .json files are allowed">Upload File</label>
                    </button>
                  </div>
                )}
                {hasTable && (
                  <div>
                    <button
                      className={classNames('btn btn-primary', Styles.btnOutline, !isOwner && Styles.btnDisabled)}
                      type="button"
                      onClick={() => { setShowTechnicalUserModal(true) }}
                    >
                      <i className="icon mbc-icon plus" />
                      <span>Create Technical User</span>
                    </button>
                  </div>
                )}
                <div>
                  <button
                    className={classNames('btn btn-primary', Styles.btnOutline, !isOwner && Styles.btnDisabled)}
                    type="button"
                    onClick={() => { setToggleModal(!toggleModal)}}
                  >
                    <i className="icon mbc-icon plus" />
                    <span>Add Table</span>
                  </button>
                </div>
                <div onClick={toggleFullScreenMode}>
                  <FullScreenModeIcon fsNeed={fullScreenMode} />
                </div>
              </div>
            </div>
          </div>
          <div className={classNames(Styles.content)}>
            <div className={classNames('graph', fullScreenMode ? Styles.fullscreen : '')}>
              <svg
                className="main"
                viewBox={`${box.x} ${box.y} ${box.w} ${box.h}`}
                onMouseDown={mouseDownHandler}
                onMouseUp={mouseUpHandler}
                onMouseMove={mouseMoveHandler}
                // onWheel={wheelHandler}
                ref={svg}
              >
                {project?.tables?.length > 0 && project.tables.map((table, index) => {
                  return (
                    <>
                      <GraphTable
                        key={table.tableName + index}
                        table={table}
                        onTableMouseDown={tableMouseDownHandler}
                        tableSelectedId={tableSelectedId}
                        setTableSelectId={setTableSelectId}
                        onCollabClick={handleCollab}
                        onDeleteTable={handleDeleteTable}
                        onAddColumn={handleAddColumn}
                        onEditColumn={handleEditColumn}
                        onEditTable={handleEditTable}
                        isOwner={isOwner}
                      />
                    </>
                  );
                })}
              </svg>
            </div>
          </div>

          <div style={{textAlign: 'right', marginTop: '20px'}}>
            <button className={classNames('btn btn-tertiary')} onClick={handlePublish}>Save & Publish</button>
          </div>
        </div>

        { toggleModal &&
          <SlidingModal
            title={'Add Table'}
            toggle={toggleModal}
            setToggle={() => setToggleModal(!toggleModal)}
            content={<TableForm setToggle={() => setToggleModal(!toggleModal)} formats={formats} dataTypes={dataTypes} />}
          />
        }

        { showUploadModal &&
          <Modal
            title={'Publish File'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'35%'}
            buttonAlignment="right"
            show={showUploadModal}
            content={uploadContent}
            scrollableContent={false}
            onCancel={() => {
              setShowUploadModal(false);
              setUploadFile({});
            }}
          />
        }

        { showDataProductModal &&
          <Modal
            title={'Provision as a Data Product'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'35%'}
            buttonAlignment="right"
            show={showDataProductModal}
            content={<DataProductForm project={{...project}} onCreate={handleCreateDataProduct} />}
            scrollableContent={false}
            onCancel={() => {
              setShowDataProductModal(false);
            }}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '35%',
            }}
          />
        }

        { showTechnicalUserModal &&
          <Modal
            title={'Edit Technical User'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'35%'}
            buttonAlignment="right"
            show={showTechnicalUserModal}
            content={technicalUserContent}
            scrollableContent={false}
            onCancel={() => {
              setShowTechnicalUserModal(false);
            }}
          />
        }

        {
          showCollabModal &&
          <Modal
            title={'Table Collaborators'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'60%'}
            buttonAlignment="right"
            show={showCollabModal}
            content={<TableCollaborators edit={false} table={table} onSave={() => setShowCollabModal(false)} user={user} />}
            scrollableContent={false}
            onCancel={() => setShowCollabModal(false)}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '60%',
            }}
          />
        }

        { showColumnModal &&
          <Modal
            title={columnEdit ? 'Edit Column' : 'Add Column'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'60%'}
            buttonAlignment="right"
            show={showColumnModal}
            content={<ColumnForm setToggle={() => setToggleModal(!toggleModal)} dataTypes={dataTypes} edit={columnEdit} column={selectedColumn} onAddColumn={addColumn} onEditColumn={editColumn} />}
            scrollableContent={false}
            onCancel={() => {
              setShowColumnModal(false);
              setSelectedColumn();
              setColumnEdit(false);
            }}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '60%',
              maxWidth: '50vw'
            }}
          />
        }

        { showTableModal &&
          <Modal
            title={'Edit Table'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'60%'}
            buttonAlignment="right"
            show={showTableModal}
            content={<EditTableForm setToggle={() => setToggleModal(!toggleModal)} formats={formats} table={selectedTable} onEditTable={editTable} />}
            scrollableContent={false}
            onCancel={() => {
              setShowTableModal(false);
              setSelectedTable();
            }}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '60%',
              maxWidth: '50vw'
            }}
          />
        }

        {showConnectionModal &&
          <InfoModal
            title={'Connect'}
            modalCSS={Styles.header}
            show={showConnectionModal}
            content={<ConnectionModal projectId={id} onOkClick={() => setShowConnectionModal(false)} />}
            hiddenTitle={true}
            onCancel={() => setShowConnectionModal(false)}
          />

        }
      </div> : <Spinner />
  );
}

export default Graph;