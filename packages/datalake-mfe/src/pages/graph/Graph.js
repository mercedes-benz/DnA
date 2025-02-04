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
import ConfirmModal from 'dna-container/ConfirmModal'
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
import { Envs } from '../../utilities/envs';
import  TableNav from '../../components/tableNav/tableNav';

const Graph = ({user, hostHistory}) => {
    const { id } = useParams();
    const dispatch = useDispatch();
    const {
        box,
        version,
        project,
        isLoading,
    } = useSelector(state => state.graph);
    const [hasWritePermission, setHasWritePermission] = useState(true);
    
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
    const [hasTable, setHasTable] = useState(project.tables.length > 0 );
    const [showInvaildDpModal, setShowInvalidDpModal] = useState(false);
    const [showUnLinkModal, setShowUnLinkModal] = useState(false);
    const [showRefreshModel, setShowRefreshModel] = useState(false);
    const [isSaved, setIsSaved] = useState(true);
    const [showSaveModel, setShowSaveModel] = useState(false)
    const [showDelWarningModel, setShowDelWarningModel] = useState(false);
    const [delWarningMsg , setDelWarningMsg] = useState('');
    const [delTableName, setDelTableName] = useState('')
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
    useEffect (()=>{
      setHasTable(project.tables.length > 0 );
      if(!isOwner && project?.collabs?.length > 0){
        const hasPermission = project?.collabs?.some(collab => collab.collaborator.id === user.id && collab.hasWritePermission);
        setHasWritePermission(hasPermission);
      }
    },[project]);
    
    const wheelHandler = e => {
      let { deltaY, deltaX } = e;
      const cursor = getSVGCursor(e);
      let state = JSON.parse(JSON.stringify(box));
      if (!e.ctrlKey) {
        if(!(state.w > 4000 && deltaY > 0) && !(state.w < 600 && deltaY < 0)){
          state.x= state.x + deltaX;
          state.y= state.y + deltaY;
        }
        
      } else {
            if(!(state.w > 4000 && deltaY > 0) && !(state.w < 600 && deltaY < 0)){   
              deltaY = deltaY * 2;
              deltaX = deltaY * (state.w / state.h);
              const deltaLimit = 600; 
              if (deltaY > deltaLimit) {
                deltaY = deltaY > deltaLimit ? deltaLimit : deltaY;
                deltaX = deltaY * (state.w / state.h);
            } else if (deltaY < -deltaLimit) {
                deltaY = deltaY < -deltaLimit ? -deltaLimit : deltaY;
                deltaX = deltaY * (state.w / state.h);
            }
              state.x= state.x - ((cursor.x - state.x) / state.w) * deltaX;
              state.y= state.y - ((cursor.y - state.y) / state.h) * deltaY;
              state.w= state.w + deltaX;
              state.h = state.h + deltaY;

            }
      }
      dispatch(setBox(state));

      e.preventDefault();
  };  
  const handlerTableSelected = table => {
    let state = JSON.parse(JSON.stringify(box));
    state.x = table.xcoOrdinate +  268 - (table.xcoOrdinate > -16 ? 264 : -table.xcoOrdinate / 2);
    state.y = table.ycoOrdinate;
    dispatch(setBox(state));
    setTableSelectId(table.tableName);
};

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
        setIsSaved(false);
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
          error?.response?.data?.errors[0]?.message || error?.response?.data?.warnings[0]?.message || 'Error while updating technical user',
          'alert',
        );
        console.log(error)
      });
    }
    
  const onAddTableClick = () => {
    dispatch(setBox({ 
      x: 0, 
      y: 0,
      w: box.w,
      h: box.h,
      clientH: box.clientH,
      clientW: box.clientW 
    }));
    if (isSaved) {
      setToggleModal(!toggleModal)
    } else {
      setShowSaveModel(true)
    }
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

  const handleCreateDataProduct = (values,isNewDataProduct) => {
    ProgressIndicator.show();
    if (isNewDataProduct) {
      ProgressIndicator.show();
      const dataProductData = {
        dataProductName: values?.name,
        description: values?.description,
        isPublish: false,
        createdBy: project?.createdBy,
        access: {
          personalRelatedData: project?.hasPii,
          confidentiality: project?.classificationType,
        }
      };
      datalakeApi.createDataProduct(dataProductData).then((res) => {
        const dataProductDetails = {
          id: res?.data?.data.id,
          dataProductId: res.data.data.dataProductId,
          dataProductName: res.data.data.dataProductName,
        }
        linkDataProduct(dataProductDetails,isNewDataProduct);
      }).catch((error) => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors[0]?.message || 'Error while creating dataProduct',
          'alert',
        );
      });
    } else{
      const dataProductDetails = {
        dataProductId: values.dataProductId,
        dataProductName: values.dataProductName,
        id: values.id,
      }
      linkDataProduct(dataProductDetails,isNewDataProduct);
    }
    setShowDataProductModal(false);
  }

  const onDataProductClick = () => {
    if (project?.dataProductDetails?.dataProductId?.length > 0) {
      hostHistory.push(`/data/dataproduct/summary/${project?.dataProductDetails.dataProductId}`);
    } else {
      setShowDataProductModal(true);
    }
  }

  const linkDataProduct = (dataProductDetails,isNewDataProduct) =>{
    datalakeApi.linkDataProduct(project?.id, dataProductDetails).then(() => {
      ProgressIndicator.hide();
        dispatch(getProjectDetails(id));
        Notification.show('Data Product Created and linked successfully.');
      }).catch(() => {
      ProgressIndicator.hide();    
        Notification.show(isNewDataProduct ? 'DataProduct created But Failed to link to DataLake Project' : 'Failed to link DataProduct', 'alert');
    });

  }

  const unLinkDataProduct = () => {
    const dataProductDetails = {
      dataProductId: null,
      dataProductName: null,
      id: null,
    }
      ProgressIndicator.show();
      datalakeApi.linkDataProduct(project?.id, dataProductDetails).then(() => {
        ProgressIndicator.hide();
        dispatch(getProjectDetails(id));
        Notification.show('Data Product unlinked successfully');
        setShowUnLinkModal(false);
        }).catch(() => {
        ProgressIndicator.hide();
          Notification.show('failed to unlink data product');
          setShowUnLinkModal(false);
      });   
  }

  const [showCollabModal, setShowCollabModal] = useState(false);
  const [table, setTable] = useState([]);

  const handleCollab = (table) => {
    setShowCollabModal(true);
    setTable({...table});
  }

  const handleDeleteTable = (tableName) => {
    if (project.catalogName === 'iceberg') {
      setDelWarningMsg('This action will also delete the data inside the table . Are you sure you want to delete ?');
    } else {
      setDelWarningMsg(' Are you sure you want to delete ?')
    }
    setDelTableName(tableName);
    setShowDelWarningModel(true)

  }

  const proccedToDelTable = (tableName) => {
    ProgressIndicator.show();
    const projectTemp = {...project};
    const tempTables = projectTemp.tables.filter(item => item.tableName !== tableName);
    projectTemp.tables = [...tempTables];
    dispatch(setTables(projectTemp.tables));
    datalakeApi.updateDatalakeProject(project?.id, projectTemp).then(() => {
      ProgressIndicator.hide();
      Notification.show('Table Deleted successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while publishing table(s)',
        'alert',
      );
    });
  }

  const [connectors,setConnectors] = useState([]);
  const [formats, setFormats] = useState([]);
  const [dataTypes, setDataTypes] = useState([]);
  const [keyWords, setKeyWords] = useState([]);

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
        setKeyWords(res.data.reserveWords);
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
      Notification.show('Data saved successfully');
      setIsSaved(true);
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
  
  
  // const isValidFile = (file) => ['csv', 'parquet', 'json'].includes(file?.name?.split('.')[1]);
  // const [uploadFile, setUploadFile] = useState({});
  // const [showUploadModal, setShowUploadModal] = useState(false);
  // const uploadContent = <>
  //   <div>
  //     <p>File to Upload: {uploadFile.length > 0 && uploadFile[0]?.name}</p>
  //     <div className={Styles.btnContainer}>
  //       <button className="btn" disabled={true}>Publish</button>
  //     </div>
  //   </div>
  // </>
     
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
                <button
                className={classNames(Styles.refreshBtn)}
                type='button'
                onClick={() => { setShowRefreshModel(true) }}
              >
                <i className={classNames("icon mbc-icon refresh")} tooltip-data={"refresh"} />
              </button>
              </div>
            </div>
            <div className={Styles.navigation}>
                <div className={Styles.headerright}>
                {(hasTable || project?.dataProductDetails?.dataProductId?.length) && (
                  <div className={classNames(Styles.dpWrapper)}>
                    <button
                      className={classNames('btn btn-primary', Styles.btnOutline, Styles.dataProduct,(!isOwner && !project?.dataProductDetails?.dataProductId?.length) && Styles.btnDisabled, Envs.ENABLE_PROVISION_AND_UPLOAD ? '' : 'hide')}
                      type="button"
                      onClick={() => onDataProductClick()}
                    >
                      <i className="icon mbc-icon dataproductoverview" />
                      <span>
                        {!project?.dataProductDetails?.dataProductId?.length ? "Provision as a Data Product" : "View Data Product"}
                      </span>
                    </button>
                    {(project?.dataProductDetails?.dataProductId?.length > 0 && isOwner) && (
                    <button className={classNames('btn btn-primary', Styles.btnOutline, Styles.unLinkbtn)} onClick={() => setShowUnLinkModal(true)}>
                      <i className="icon mbc-icon link" tooltip-data={"Unlink Data Product"} />
                    </button>)}
                    {(project?.dataProductDetails?.invalidState && isOwner) && (
                      <button className={classNames('btn btn-primary', Styles.btnOutline, Styles.warningBtn)} onClick={() => { setShowInvalidDpModal(true) }}>
                        <i className={classNames("icon mbc-icon alert circle", Styles.warningIcon)} />
                      </button>
                    )}
                  </div>)}       
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
                    {/* {hasTable && ( 
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
                            className={classNames('btn btn-primary', Styles.btnOutline, !isOwner && Styles.btnDisabled,Envs.ENABLE_PROVISION_AND_UPLOAD ? '' : 'hide')}
                            type="button"
                            >
                            <i className="icon mbc-icon upload" />
                            <label htmlFor="uploadFile" tooltip-data="Only .csv, .parquet, and .json files are allowed">Upload File</label>
                        </button>
                    </div>
                    )} */}
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
                            className={classNames('btn btn-primary', Styles.btnOutline, (!hasWritePermission) && Styles.btnDisabled)}
                            type="button"
                            onClick={onAddTableClick}
                        >
                            <i className="icon mbc-icon plus" />
                            <span>Add Table</span>
                        </button>
                    </div>
                    {hasTable && (
                      <div style={{textAlign: 'right', padding: '5px'}}>
                        <button className={classNames('btn btn-tertiary', isSaved ? Styles.btnDisabled : '' )} onClick={handlePublish}>Save</button>
                      </div>
                    )}
                  <div onClick={toggleFullScreenMode}>
                    <FullScreenModeIcon fsNeed={fullScreenMode} />
                  </div>
                </div>
            </div>
        </div>
          <div className={classNames(Styles.content)}>
            <div className={classNames('graph', Styles.graphBox, fullScreenMode ? Styles.fullscreen : '')}>
              {project?.tables?.length > 0 && <div className={classNames(Styles.tableNav)} >
                <TableNav tables={project?.tables} onTableSelected={handlerTableSelected} />

              </div>}

              <svg
                className={classNames('main', Styles.graphSvg)}
                viewBox={`${box.x} ${box.y} ${box.w} ${box.h}`}
                onMouseDown={mouseDownHandler}
                onMouseUp={mouseUpHandler}
                onMouseMove={mouseMoveHandler}
                onWheel={wheelHandler}
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
                            hasWritePermission={hasWritePermission}
                            hasDataProduct = {project?.dataProductDetails?.dataProductId?.length > 0}
                        />
                    </>
                );
              })}
            </svg>
          </div>
        </div>
        
      </div>
      
      { toggleModal && 
        <SlidingModal
            title={'Add Table'}
            toggle={toggleModal}
            setToggle={() => setToggleModal(!toggleModal)}
            content={<TableForm focusTable={handlerTableSelected} setToggle={() => setToggleModal(!toggleModal)} formats={formats} dataTypes={dataTypes} keyWords={keyWords}isSaved ={(val)=>{setIsSaved(val)}} />}
        />
    }

    {/* { showUploadModal &&
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
    } */}

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
        content={<TableCollaborators edit={false} table={table} isProjectLevelCollab ={false} onSave={() => setShowCollabModal(false)} user={user} />}
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
    {showInvaildDpModal &&
        <ConfirmModal
        acceptButtonTitle="unlink"
        cancelButtonTitle="close"
        showAcceptButton={true}
        showCancelButton={true}
        show={showInvaildDpModal}
        content={
          <div id="contentparentdiv">
            The schema of the data, which is already linked to the data product, has been changed.<br></br> This modification may affect the data flow for the data product subscribers.<br></br> Please validate and unlink data product if needed.
          </div>
        }
        onCancel={() => {
          setShowInvalidDpModal(false);
        }}
        onAccept={() => {
          unLinkDataProduct();
        }}
      />
    }

    {showRefreshModel && 
          <ConfirmModal
          acceptButtonTitle="refresh"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={showRefreshModel}
          content={
            <div id="contentparentdiv">
              Unsaved data will be lost on refresh. Are you sure you want to refresh?
            </div>
          }
          onCancel={() => {
            setShowRefreshModel(false);
          }}
          onAccept={() => {
            dispatch(setBox({ 
              x: 0, 
              y: 0,
              w: box.w,
              h: box.h,
              clientH: box.clientH,
              clientW: box.clientW 
            }));
            dispatch(getProjectDetails(id));
            setShowRefreshModel(false);
          }}
        />
     }
     {showDelWarningModel && 
          <ConfirmModal
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={showDelWarningModel}
          content={
            <div id="contentparentdiv">
             {delWarningMsg}
            </div>
          }
          onCancel={() => {
            setShowDelWarningModel(false);
          }}
          onAccept={() => {
            proccedToDelTable(delTableName);
            setShowDelWarningModel(false);
          }}
        />
     }
    {showSaveModel && 
          <ConfirmModal
          acceptButtonTitle="Yes"
          cancelButtonTitle="No"
          showAcceptButton={true}
          showCancelButton={true}
          show={showSaveModel}
          content={
            <div id="contentparentdiv">
             Do you wish to save the alignment changes?
            </div>
          }
          onCancel={() => {
            setShowSaveModel(false);
            setToggleModal(!toggleModal);
          }}
          onAccept={() => {
            setShowSaveModel(false);
            handlePublish();
            setToggleModal(!toggleModal);
          }}
        />
     }
    {showUnLinkModal &&
      <ConfirmModal
        acceptButtonTitle="Unlink"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showUnLinkModal}
        content={
          <div id="contentparentdiv">
            Are you sure you want to unlink data product?
          </div>
          }
        onCancel={() => {
          setShowUnLinkModal(false);
        }}
        onAccept={() => {
          unLinkDataProduct();
        }}
      />
      }
    </div> : <Spinner />
  );
}

export default Graph;