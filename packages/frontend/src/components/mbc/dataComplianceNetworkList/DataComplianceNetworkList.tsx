import React, { useState, useEffect } from 'react';
import cn from 'classnames';
import Pagination from '../pagination/Pagination';
import Styles from './DataComplianceNetworkList.scss';
import { ISortField } from '../allSolutions/AllSolutions';
import { IDataComplianceRequest, IEntity, ITag } from '../../../globals/types';
import RowItem from './rowItem/RowItem';
import ConfirmModal from '../../../components/formElements/modal/confirmModal/ConfirmModal';
import InfoModal from '../../../components/formElements/modal/infoModal/InfoModal';
import TextBox from '../shared/textBox/TextBox';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import Spinner from '../shared/spinner/Spinner';
import { SESSION_STORAGE_KEYS } from '../../../globals/constants';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import TypeAheadBox from '../shared/typeAheadBox/TypeAheadBox';
import Tags from '../../../components/formElements/tags/Tags';
import { DataComplianceApiClient } from '../../../services/DataComplianceApiClient';
import { validateEmail } from '../../../utils/Validation';

export interface IEntityItem {
  id?: string;
  entityId: string;
  entityName: string;
}

const classNames = cn.bind(Styles);

const DataComplianceNetworkList:React.FC = () => {

  const [loading, setLoading] = useState(false);

  const [sortBy, setSortBy] = useState<ISortField>({
    name: 'entityId',
    currentSortType: 'asc',
    nextSortType: 'desc',
  });

  /* Pagination */
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);
  
  const [dataComplianceNetworkList, setDataComplianceNetworkList] = useState([]);

  const [showDeleteEntityConfirmModal, setShowDeleteEntityConfirmModal] = useState(false);
  const [updateConfirmModalOverlay, setUpdateConfirmModalOverlay] = useState(false);

  const [updateMode, setUpdateMode] = useState(false);
  const [showEntityFormModal, setShowEntityFormModal] = useState(false);

  const [entityList, setEntityList] = useState<IEntityItem[]>([]);
  const [selectedEntityError, setSelectedEntityError] = useState(false);
  
  const [entity, setEntity] = useState<IEntity>({
    id: '',
    entityId: '',
    entityName: '',
    localComplianceOfficer: [],
    localComplianceResponsible: [],
    dataProtectionCoordinator: [],
    localComplianceSpecialist: [],
    createdDate: '',
    createdBy: {
      id: '',
      firstName: '',
      lastName: '',
      eMail: '',
      mobileNumber: '',
      department: '',
    },
    lastModifiedDate: '',
    modifiedBy: {
      id: '',
      firstName: '',
      lastName: '',
      eMail: '',
      mobileNumber: '',
      department: '',
    },
  });
  
  const dummyTags:ITag[] = [];

  /* error states */
  const [entityIdError, setEntityIdError] = useState('');
  const [entityNameError, setEntityNameError] = useState('');
  const [lcoError, setLcoError] = useState(false);
  const [lcrError, setLcrError] = useState(false);
  const [dpcError, setDpcError] = useState(false);
  const [lcsError, setLcsError] = useState(false);

  const [entitySearch, setEntitySearch] = useState(true);

  /* Email Validation */
  const setLcoIds = (arr: string[]) => {
    setLcoError(false);
    arr.map(item => {
      if(validateEmail(item)) {
        return item;
      } else {
        setLcoError(true);
      }
    });
    setEntity({...entity, localComplianceOfficer: arr});
  };
  const setLcrIds = (arr: string[]) => {
    setLcrError(false);
    arr.map(item => {
      if(validateEmail(item)) {
        return item;
      } else {
        setLcrError(true);
      }
    });
    setEntity({...entity, localComplianceResponsible: arr});
  };
  const setDpcsIds = (arr: string[]) => {
    setDpcError(false);
    arr.map(item => {
      if(validateEmail(item)) {
        return item;
      } else {
        setDpcError(true);
      }
    });
    setEntity({...entity, dataProtectionCoordinator: arr});
  };
  const setLcssIds = (arr: string[]) => {
    setLcsError(false);
    arr.map(item => {
      if(validateEmail(item)) {
        return item;
      } else {
        setLcsError(true);
      }
    });
    setEntity({...entity, localComplianceSpecialist: arr});
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    setLoading(true)
    DataComplianceApiClient.getDataComplianceNetworkList(0, 0, 'entityId', 'asc')
      .then((res:any) => {
        if(res.records) {
          setDataComplianceNetworkList(res.records);
        } else {
          setDataComplianceNetworkList([]);
        }
        setLoading(false);
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        setLoading(false);
      });

    DataComplianceApiClient.getEntityList(0, 0, 'entityId', 'asc')
    .then((res:any) => {
      setEntityList(res.records);
    })
    .catch((err:any) => {
      console.log(err);
    });
  }, []);

  const getDataComplianceNetworkList = () => {
    DataComplianceApiClient.getDataComplianceNetworkList(0, 0, 'entityId', 'asc')
      .then((res:any) => {
        if(res.records) {
          setDataComplianceNetworkList(res.records);
        } else {
          setDataComplianceNetworkList([]);
        }
        setLoading(false);
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        setLoading(false);
      });
  }

  /* Sort */
  const sortEntities = (propName: string, sortOrder: string) => {
    const tempSortBy: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: sortBy.currentSortType,
    };
    setSortBy(tempSortBy);
  };
  useEffect(() => {
    setLoading(true);
    DataComplianceApiClient.getDataComplianceNetworkList(currentPageOffset, maxItemsPerPage, sortBy.name, sortBy.currentSortType)
      .then((res:any) => {
        if(res.records) {
          setDataComplianceNetworkList(res.records);
        } else {
          setDataComplianceNetworkList([]);
        }
        setLoading(false);
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        setLoading(false);
      });
  }, [sortBy, maxItemsPerPage, currentPageOffset]);
  useEffect(() => {
    setLoading(true);
    DataComplianceApiClient.getDataComplianceNetworkList(0, 0, sortBy.name, sortBy.currentSortType)
      .then((res:any) => {
        if(res.records) {
          const totalPages = Math.ceil(res.records.length / maxItemsPerPage);
          const currentPage =
            currentPageNumber > Math.ceil(res.records.length / maxItemsPerPage)
              ? Math.ceil(res.records.length / maxItemsPerPage) > 0
                ? Math.ceil(res.records.length / maxItemsPerPage)
                : 1
              : currentPageNumber;
          setTotalNumberOfPages(totalPages);
          setCurrentPageNumber(currentPage);
          setLoading(false);
        } else {
          setDataComplianceNetworkList([]);
        }
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        setLoading(false);
      });
  }, [maxItemsPerPage, currentPageOffset]);
  
  /* Search */
  const onSearchTextChange = (e: React.FormEvent<HTMLInputElement>) => {
    const searchText = e.currentTarget.value.toLowerCase();
    const filteredResults = dataComplianceNetworkList.filter((result) => {
      const localComplianceOfficers = result.localComplianceOfficer.toString();
      const localComplianceResponsibles = result.localComplianceResponsible.toString();
      const dataProtectionCoordinators = result.dataProtectionCoordinator.toString();
      const localComplianceSpecialists = result.localComplianceSpecialist.toString();
      return result.entityName.toLowerCase().includes(searchText) || result.entityId.toLowerCase().includes(searchText) ||
              localComplianceOfficers.includes(searchText) || localComplianceResponsibles.includes(searchText) || dataProtectionCoordinators.includes(searchText)
              || localComplianceSpecialists.includes(searchText);
    });
    if(searchText.length > 0) {
      setDataComplianceNetworkList(filteredResults);
    } else {
      getDataComplianceNetworkList();
      setCurrentPageOffset(0);
    }
  }

  /* Pagination */
  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffset = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffset);
  };
  const onPaginationNextClick = () => {
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffset);
  };
  const onViewByPageNum = (pageNum: number) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  /* Modal handling */
  const showDeleteConfirmModal = (entity: any) => {
    setEntity({
      ...entity,
      id: entity.id,
      entityName: entity.entityName,
    });
    setShowDeleteEntityConfirmModal(true);
  };
  const showUpdateConfirmModal = (entity: any) => {
    setShowEntityFormModal(true);
    setUpdateMode(true);
    setEntitySearch(true);
    setEntity({
      ...entity,
      id: entity.id,
      entityId: entity.entityId,
      entityName: entity.entityName,
      localComplianceOfficer: entity.localComplianceOfficer,
      localComplianceResponsible: entity.localComplianceResponsible,
      dataProtectionCoordinator: entity.dataProtectionCoordinator,
      localComplianceSpecialist: entity.localComplianceSpecialist,
      createdDate: entity.createdDate,
      createdBy: entity.createdBy
    });

    setEntityIdError(null);
    setEntityNameError(null);
  };
  const onCancelDeleteChanges = () => {
    setShowDeleteEntityConfirmModal(false);
  };
  const onAcceptDeleteChanges = () => {
    setShowDeleteEntityConfirmModal(false);
    ProgressIndicator.show();
    const newArr = dataComplianceNetworkList.filter(object => {
      return object.id !== entity.id;
    });
    DataComplianceApiClient.deleteDataComplianceNetworkList(entity.id)
      .then((response:any) => {
        setDataComplianceNetworkList(newArr);
        ProgressIndicator.hide();
        Notification.show('Legal entity deleted successfully.');
      })
      .catch((error:any) => {
        ProgressIndicator.hide();
        Notification.show(error.message, 'alert');
      });
    setShowEntityFormModal(false);
  };
  const onShowEntityFormModalCancel = () => {
    setShowEntityFormModal(false);
  };
  const onEntityFormModalOpen = () => {
    resetEntity();
    setShowEntityFormModal(true);
    setUpdateMode(false);
  };
  const updateConfirmModalOverlayCancel = () => {
    setUpdateConfirmModalOverlay(false);
  };
  const updateConfirmModalOverlayUpdate = () => {
    let formValid = true;
    let errorMessage = 'Please fill Entity ID and Entity Name';
    
    if(entity.entityId.length > 0) {
      setEntityIdError(null);
    } else {
      setEntityIdError('*Missing entry');
      formValid = false;
    }
    if(entity.entityName.length > 0) {
      setEntityNameError(null);
    } else {
      setEntityNameError('*Missing entry');
      formValid = false;
    }
    if(lcoError || lcrError || dpcError || lcsError) {
      formValid = false;
      errorMessage = 'Please enter emails in correct format';
    }
    if(formValid) {
      setUpdateConfirmModalOverlay(true); 
    } else {
      Notification.show(errorMessage, 'alert');
    }
  };
  const onEntitySelect = (entity:any) => {
    setEntity((prev) => ({...prev, entityId: entity.entityId, entityName: entity.entityName}));
  }
  const onEntitySelectError = (error: boolean) => {
    setSelectedEntityError(error);
    console.log('error:', error);
  }
  const resetEntity = () => {
    setEntity({
      ...entity,
      entityId: '',
      entityName: '',
      localComplianceOfficer: [] as string[],
      localComplianceResponsible: [] as string[],
      dataProtectionCoordinator: [] as string[],
      localComplianceSpecialist: [] as string[],
    });

    setSelectedEntityError(false);
    setEntityIdError(null);
    setEntityNameError(null);

    setEntitySearch(true);
  }
  const onEntityAdd = () => {
    let formValid = true;
    let errorMessage = 'Please fill Entity ID and Entity Name';
    if(entitySearch) {
      console.log('entitySearch');
      console.log('selectedError: ', selectedEntityError);
      if(selectedEntityError) {
        errorMessage = 'Please select entity';
        formValid = false;
      } else {
        formValid = true;
      }
    } else {
      console.log('entity manual');
      if(entity.entityId.length > 0) {
        setEntityIdError(null);
      } else {
        setEntityIdError('*Missing entry');
        formValid = false;
      }
      if(entity.entityName.length > 0) {
        setEntityNameError(null);
      } else {
        setEntityNameError('*Missing entry');
        formValid = false;
      }
    }
    if(lcoError || lcrError || dpcError || lcsError) {
      formValid = false;
      errorMessage = 'Please enter emails in correct format';
    }
    if(formValid) {
      ProgressIndicator.show();
      setUpdateConfirmModalOverlay(false);
      const data: IDataComplianceRequest = {
        data: {
          dataProtectionCoordinator: entity.dataProtectionCoordinator,
          entityId: entity.entityId,
          entityName: entity.entityName,
          localComplianceOfficer: entity.localComplianceOfficer,
          localComplianceResponsible: entity.localComplianceResponsible,
          localComplianceSpecialist: entity.localComplianceSpecialist
        }
      }
      DataComplianceApiClient.saveDataComplianceNetworkList(data)
        .then((response:any) => {
          getDataComplianceNetworkList();
          ProgressIndicator.hide();
          Notification.show('Legal entity saved successfully.');
          resetEntity();
          setShowEntityFormModal(false);
        })
        .catch((error:any) => {
          ProgressIndicator.hide();
          Notification.show(error.message, 'alert');
        });
    } else {
      Notification.show(errorMessage, 'alert');
      ProgressIndicator.hide();
    }
  };
  const onEntityUpdate = () => {
    setUpdateConfirmModalOverlay(false);
    ProgressIndicator.show();
    const newState = dataComplianceNetworkList.map(obj => {
      if (obj.id === entity.id) {
        return {
          ...obj, 
          id: entity.id,
          entityId: entity.entityId, 
          entityName: entity.entityName,
          localComplianceOfficer: entity.localComplianceOfficer,
          localComplianceResponsible: entity.localComplianceResponsible,
          dataProtectionCoordinator: entity.dataProtectionCoordinator,
          localComplianceSpecialist: entity.localComplianceSpecialist 
        };
      }
      return obj;
    });
    const data: IDataComplianceRequest = {
      data: {
        id: entity.id,
        dataProtectionCoordinator: entity.dataProtectionCoordinator,
        createdDate: entity.createdDate,
        createdBy: entity.createdBy,
        entityId: entity.entityId,
        entityName: entity.entityName,
        localComplianceOfficer: entity.localComplianceOfficer,
        localComplianceResponsible: entity.localComplianceResponsible,
        localComplianceSpecialist: entity.localComplianceSpecialist
      }
    }
    DataComplianceApiClient.updateDataComplianceNetworkList(data)
      .then((response:any) => {
        Notification.show('Legal entity updated successfully.');
        setDataComplianceNetworkList(newState);
        ProgressIndicator.hide();
        resetEntity();
        setShowEntityFormModal(false);
      })
      .catch((error:any) => {
        ProgressIndicator.hide();
        Notification.show(error.message, 'alert');
      });
  };
  const onChangeEntityId = (e: React.FormEvent<HTMLInputElement>) => {
    setEntity({...entity, entityId: e.currentTarget.value});
    if(e.currentTarget.value.length > 0) {
      setEntityIdError(null);
    } else {
      setEntityIdError('*Missing entry');
    }
  };
  const onChangeEntityName = (e: React.FormEvent<HTMLInputElement>) => {
    setEntity({...entity, entityName: e.currentTarget.value});
    if(e.currentTarget.value.length > 0) {
      setEntityNameError(null);
    } else {
      setEntityNameError('*Missing entry');
    }
  };

  /* jsx */
  const deleteModalContent: React.ReactNode = (
    <div id="contentparentdiv" className={Styles.modalContentWrapper}>
      <div className={Styles.modalTitle}>Delete Entity</div>
      <div className={Styles.modalContent}>
        <p>The entity &laquo;{entity.entityName ? entity.entityName : ''}&raquo; will be removed
        permanently.</p>
      </div>
    </div>
  );
  const entityFormModalContent = (
    <div id="addOrUpdateFormWrapper" className={Styles.infoPopup}>
      <div className={classNames(Styles.modalContent, Styles.formWrapperMain)}>
        <button className={Styles.btnSwitch} onClick={() => setEntitySearch(!entitySearch)}>{ entitySearch ? 'Add New Entity' : 'Back to Search' }</button>
        { entitySearch ? 
            <>
                <TypeAheadBox
                  label={'Entity'}
                  placeholder={"Search Entity ID or Entity Name"}
                  list={entityList}
                  defaultValue={updateMode ? entity.entityId + ' - ' + entity.entityName : ''}
                  onItemSelect={onEntitySelect}
                  required={true}
                  onError={onEntitySelectError}
                />
            </> : 
            <>
             <TextBox
               type="text"
               controlId={'entity-id'}
               label={'Entity ID'}
               placeholder={"Type here"}
               value={entity.entityId}
               errorText={entityIdError}
               required={true}
               maxLength={200}
               onChange={onChangeEntityId}
             />
             <TextBox
               type="text"
               controlId={'entity-name'}
               label={'Entity Name'}
               placeholder={"Type here"}
               value={entity.entityName}
               errorText={entityNameError}
               required={true}
               maxLength={200}
               onChange={onChangeEntityName}
             />
           </>
        }
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Officer (LCO)'}
            max={100}
            chips={entity.localComplianceOfficer}
            setTags={setLcoIds}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          { lcoError && <span className={classNames("error-message", Styles.tagError)}>Please enter valid email address.</span> }
        </div>
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Responsible (LCR)'}
            max={100}
            chips={entity.localComplianceResponsible}
            setTags={setLcrIds}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          { lcrError && <span className={classNames("error-message", Styles.tagError)}>Please enter valid email address.</span> }
        </div>
        <div className={Styles.tagControl}>
          <Tags
            title={'Data Protection Coordinator (DPC)'}
            max={100}
            chips={entity.dataProtectionCoordinator}
            setTags={setDpcsIds}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          { dpcError && <span className={classNames("error-message", Styles.tagError)}>Please enter valid email address.</span> }
        </div>
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Support / Specialist (LCS)'}
            max={100}
            chips={entity.localComplianceSpecialist}
            setTags={setLcssIds}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          { lcsError && <span className={classNames("error-message", Styles.tagError)}>Please enter valid email address.</span> }
        </div>
        <div className={Styles.addBtn}>
          <button
            onClick={updateMode ? updateConfirmModalOverlayUpdate : onEntityAdd}
            className={Styles.actionBtn + ' btn btn-tertiary'}
            type="button"
          >
            {updateMode ? <span>Update</span> : <span>Add</span>}
          </button>
        </div>
      </div>
      {updateConfirmModalOverlay && (
        <div className={Styles.updateModelOverlayContent}>
          <p>
            Updating &lt;&lt;{entity.entityName ? entity.entityName : ''}&gt;&gt; would also update all the associated
            solutions. <br /> Do you want to proceed?
          </p>
          <div>
            <button
              className={Styles.actionBtn + ' btn btn-default'}
              type="button"
              onClick={updateConfirmModalOverlayCancel}
            >
              Cancel
            </button>{' '}
            &nbsp;
            <button className={Styles.actionBtn + ' btn btn-tertiary'} type="button" onClick={onEntityUpdate}>
              Update
            </button>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.caption}>
        <h3>Data Compliance Network List</h3>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.searchPanel}>
          <div className={`input-field-group search-field ${loading ? 'disabled' : ''}`}>
            <label id="searchLabel" className="input-label" htmlFor="searchInput">
              Search Entries
            </label>
            <input
              type="text"
              className="input-field search"
              required={false}
              id="searchInput"
              maxLength={200}
              placeholder="Type here"
              autoComplete="off"
              onChange={onSearchTextChange}
              disabled={loading}
            />
          </div>
          <div
            className={classNames(
              Styles.addItemButton,
              loading ? Styles.disabledAddItemBtn : '',
            )}
          >
            <button onClick={onEntityFormModalOpen}>
              <i className="icon mbc-icon plus" />
              <span>Add New Entity</span>
            </button>
          </div>
          <div className={Styles.addItemButton}>
            <button>
              <i className="icon mbc-icon link" />
              <span>See Change Log</span>
            </button>
          </div>
        </div>
        { loading && <Spinner /> }
        { !loading && dataComplianceNetworkList.length === 0 && <div className={Styles.tagIsEmpty}>There is no list available</div> }
        { !loading && dataComplianceNetworkList.length > 0 && 
          <div className={Styles.tablePanel}>
            <table className="ul-table">
              <thead>
                <tr className="header-row">
                  <th onClick={() => sortEntities('entityId', sortBy.nextSortType)}>
                    <label
                      className={
                        'sortable-column-header ' +
                        (sortBy.name === 'entityId' ? sortBy.currentSortType : '')
                      }
                    >
                      <i className="icon sort" />
                      Entity ID
                    </label>
                  </th>
                  <th onClick={() => sortEntities('entityName', sortBy.nextSortType)}>
                    <label
                      className={
                        'sortable-column-header ' +
                        (sortBy.name === 'entityName' ? sortBy.currentSortType : '')
                      }
                    >
                      <i className="icon sort" />
                      Entity Name
                    </label>
                  </th>
                  <th>
                    <label>
                        <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="- First point of contact for data compliance question
                      - Reporting line to IL/C" />
                        Local Compliance Officer (LCO)
                    </label>
                  </th>
                  <th>
                    <label>
                        <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="- First point of contact for data compliance question
                        - Employee of the respective Group entity" />
                        Local Compliance Responsible (LCR)
                    </label>
                  </th>
                  <th>
                    <label>
                        <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="(old role, sucessive to be 
                          replaced by LCO/LCR)" />
                        Data Protection Coordinator (DPC)
                    </label>
                  </th>
                  <th>
                    <label>
                        <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="- In case of reporting line to LCO, Local Compliance Specialist.
                        - In all other cases Local Compliance Support" />
                        Local Compliance Support / Specialist (LCS)
                    </label>
                  </th>
                  <th>
                    <label>Action</label>
                  </th>
                </tr>
              </thead>
              <tbody>
                {
                  dataComplianceNetworkList.map((result) => {
                    return (
                      <RowItem
                        item={result}
                        key={result.id}
                        showDeleteConfirmModal={showDeleteConfirmModal}
                        showUpdateConfirmModal={showUpdateConfirmModal}
                      />
                    );
                  })
                }
              </tbody>
            </table>
            {dataComplianceNetworkList.length &&
              <Pagination
                totalPages={totalNumberOfPages}
                pageNumber={currentPageNumber}
                onPreviousClick={onPaginationPreviousClick}
                onNextClick={onPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
              />
            }
          </div>
        }
        <ConfirmModal
          title="Delete Entity"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={showDeleteEntityConfirmModal}
          content={deleteModalContent}
          onCancel={onCancelDeleteChanges}
          onAccept={onAcceptDeleteChanges}
        />
        <InfoModal
          title={updateMode ? 'Update Entity' : 'Add New Entity'}
          modalWidth={'35vw'}
          show={showEntityFormModal}
          content={entityFormModalContent}
          onCancel={onShowEntityFormModalCancel}
        />
      </div>
    </div>
  );
}

export default DataComplianceNetworkList;
