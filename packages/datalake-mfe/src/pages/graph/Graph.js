import classNames from 'classnames';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './graph.scss';
// dna-container
import FullScreenModeIcon from 'dna-container/FullScreenModeIcon';
import Modal from 'dna-container/Modal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import GraphTable from '../../components/GraphTable';
import TableForm from '../../components/tableForm/TableForm';
import SlidingModal from '../../components/slidingModal/SlidingModal';
import { setBox, setTables } from '../../redux/graphSlice';
import { getProjectDetails } from '../../redux/graph.services';
import TableCollaborators from '../../components/tableCollaborators/TableCollaborators';
import { datalakeApi } from '../../apis/datalake.api';

const Graph = () => {
    const { id } = useParams();
    const dispatch = useDispatch();
    const {
        box,
        version,
        project,
    } = useSelector(state => state.graph);

    const methods = useForm();
    const {
      register,
      handleSubmit,
      formState: { errors },
    } = methods;

    useEffect(() => {
        dispatch(getProjectDetails(id));
    }, [id, dispatch]);

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
    const [showInferenceModal, setShowInferenceModal] = useState(false);
    const [showTechnicalUserModal, setShowTechnicalUserModal] = useState(false);

    const inferenceContent = <>
        <p>Access data using any endpoints</p>
        <div className={classNames('input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
                Inference by
            </label>
            <div className={Styles.flexLayout}>
                <label className="checkbox">
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">REST</span>
                </label>
                <label className="checkbox">
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">GRAPHQL</span>
                </label>
                <label className="checkbox">
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">ODATA</span>
                </label>
                <label className="checkbox">
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">SQL</span>
                </label>
                <label className={classNames("checkbox", Styles.disabled)}>
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">DDX (Coming Soon)</span>
                </label>
                <label className={classNames("checkbox", Styles.disabled)}>
                    <span className="wrapper">
                        <input type="checkbox" className="ff-only" />
                    </span>
                    <span className="label">CDC (Coming Soon)</span>
                </label>
            </div>
            <button
                className={classNames('btn btn-primary')}
                type="button"
                onClick={() => { setShowInferenceModal(false) }}
            >
                Create Inference
            </button>
        </div>
    </>;   

    const handleEditTechnicalUser = (values) => {
      const data = {
        clientId: values.clientId,
        clientSecret: values.clientSecret
      }
      ProgressIndicator.show();
      datalakeApi.updateTechnicalUser(data, project?.data?.id).then(() => {
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
            // defaultValue={clientId}
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
            type="text"
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

  const handlePublish = () => {
    const data = {...project}
    ProgressIndicator.show();
    datalakeApi.updateDatalakeProject(project?.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Table(s) published successfully');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while publishing table(s)',
        'alert',
      );
    });
  }
  
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              {/* <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} /> */}
              <div className={Styles.nbtitle}>
                <button tooltip-data="Go Back" className="btn btn-text back arrow" onClick={() => { history.back() }}></button>
                <h2>Data Lakehouse Project</h2>
              </div>
            </div>
            <div className={Styles.navigation}>
                <div className={Styles.headerright}>
                    <div>
                        <button
                            className={classNames('btn btn-primary', Styles.btnOutline)}
                            type="button"
                            onClick={() => { setShowTechnicalUserModal(true) }}
                        >
                            <i className="icon mbc-icon plus" />
                            <span>Create Technical User</span>
                        </button>
                    </div>
                    <div>
                        <button
                            className={classNames('btn btn-primary', Styles.btnOutline)}
                            type="button"
                            onClick={() => { setShowInferenceModal(true) }}
                        >
                            <i className="icon mbc-icon plus" />
                            <span>Add Inference</span>
                        </button>
                    </div>
                    <div>
                        <button
                            className={classNames('btn btn-primary', Styles.btnOutline)}
                            type="button"
                            onClick={() => { setToggleModal(!toggleModal)}}
                        >
                            <i className="icon mbc-icon plus" />
                            <span>Add Table</span>
                        </button>
                    </div>
                  <div tooltip-data="Open New Tab" className={Styles.OpenNewTab}>
                    <i className="icon mbc-icon arrow small right" />
                    <span> &nbsp; </span>
                  </div>
                  <div>
                    <FullScreenModeIcon fsNeed={false} />
                  </div>
                </div>
            </div>
        </div>
        <div className={classNames(Styles.content)}>
          <div className="graph">
            <svg
              className="main"
              viewBox={`${box.x} ${box.y} ${box.w} ${box.h}`}
              onMouseDown={mouseDownHandler}
              onMouseUp={mouseUpHandler}
              onMouseMove={mouseMoveHandler}
              // onWheel={wheelHandler}
              ref={svg}
            >
              {project?.tables?.length > 0 && project.tables.map(table => {
                return (
                    <>
                        <GraphTable
                            key={table.id}
                            table={table}
                            onTableMouseDown={tableMouseDownHandler}
                            tableSelectedId={tableSelectedId}
                            setTableSelectId={setTableSelectId}
                            onCollabClick={handleCollab}
                            onDeleteTable={handleDeleteTable}
                        />
                    </>
                );
              })}
            </svg>
          </div>
        </div>
        
        <div style={{textAlign: 'right', marginTop: '20px'}}>
                <button className={classNames('btn btn-tertiary')} onClick={handlePublish}>Publish</button>
            </div>
      </div>
      
      { toggleModal && 
        <SlidingModal
            title={'Add Table'}
            toggle={toggleModal}
            setToggle={() => setToggleModal(!toggleModal)}
            content={<TableForm setToggle={() => setToggleModal(!toggleModal)}  />}
        />
    }

    { showInferenceModal &&
        <Modal
            title={'Add Inference'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'35%'}
            buttonAlignment="right"
            show={showInferenceModal}
            content={inferenceContent}
            scrollableContent={false}
            onCancel={() => {
                setShowInferenceModal(false);
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
        content={<TableCollaborators edit={false} table={table} onSave={() => setShowCollabModal(false)} />}
        scrollableContent={false}
        onCancel={() => setShowCollabModal(false)}
        modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
        }}
      />
    }
    </>
  );
}

export default Graph;