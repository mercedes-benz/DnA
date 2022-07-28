import React, { useState, useEffect } from 'react';
import cn from 'classnames';
import Pagination from '../pagination/Pagination';
import Styles from './DataComplianceNetworkList.scss';
import { ISortField } from '../allSolutions/AllSolutions';
import { IDataComplianceRequest, ITag } from '../../../globals/types';
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

  const [isResultLoading, setIsResultLoading] = useState(false);
  const [asortBy, setAsortBy] = useState<ISortField>({
    name: 'entityId',
    currentSortType: 'asc',
    nextSortType: 'desc',
  });

  /* Pagination */
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);
  
  const [results, setResults] = useState([]);

  const [entityToBeUpdated, setEntityToBeUpdated] = useState('');
  const [entityToBeDeleted, setEntityToBeDeleted] = useState('');
  const [entityToBeDeletedName, setEntityToBeDeletedName] = useState('');
  const [entityToBeUpdatedName, setEntityToBeUpdatedName] = useState('');

  const [showDeleteTagConfirmModal, setShowDeleteTagConfirmModal] = useState(false);
  const [updateConfirmModelOverlay, setUpdateConfirmModelOverlay] = useState(false);

  const [updateItemMode, setUpdateItemMode] = useState(true);
  const [addNewItem, setAddNewItem] = useState(false);

  const [entityList, setEntityList] = useState<IEntityItem[]>([]);
  const [entityIdError, setEntityIdError] = useState('');
  const [entityNameError, setEntityNameError] = useState('');
  const [entityId, setEntityId] = useState('');
  const [entityName, setEntityName] = useState('');
  const [lcos, setLcos] = useState<string[]>([]);
  const [lcrs, setLcrs] = useState<string[]>([]);
  const [dpcs, setDpcs] = useState<string[]>([]);
  const [lcss, setLcss] = useState<string[]>([]);
  const dummyTags:ITag[] = [];
  const [lcoError, setLcoError] = useState(false);
  const [lcrError, setLcrError] = useState(false);
  const [dpcError, setDpcError] = useState(false);
  const [lcsError, setLcsError] = useState(false);

  const [addNew, setAddNew] = useState(false);

  const [createdDate, setCreatedDate] = useState('');
  const [selectedEntity, setSelectedEntity] = useState('');

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
    setLcos(arr);
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
    setLcrs(arr);
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
    setDpcs(arr);
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
    setLcss(arr);
  };

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  /* Sort */
  const sortTags = (propName: string, sortOrder: string) => {
    const asortBy2: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: asortBy.currentSortType,
    };
    setAsortBy(asortBy2);
  };
  useEffect(() => {
    ProgressIndicator.show();
    DataComplianceApiClient.getDataComplianceNetworkList(currentPageOffset, maxItemsPerPage, asortBy.name, asortBy.currentSortType)
      .then((res:any) => {
        setResults(res.records);
        ProgressIndicator.hide();
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        ProgressIndicator.hide();
      });
  }, [asortBy, maxItemsPerPage, currentPageOffset]);
  useEffect(() => {
    ProgressIndicator.show();
    DataComplianceApiClient.getDataComplianceNetworkList(0, 0, asortBy.name, asortBy.currentSortType)
      .then((res:any) => {
        const totalPages = Math.ceil(res.records.length / maxItemsPerPage);
        const currentPage =
          currentPageNumber > Math.ceil(res.records.length / maxItemsPerPage)
            ? Math.ceil(res.records.length / maxItemsPerPage) > 0
              ? Math.ceil(res.records.length / maxItemsPerPage)
              : 1
            : currentPageNumber;
        setTotalNumberOfPages(totalPages);
        setCurrentPageNumber(currentPage);
        ProgressIndicator.hide();
      })
      .catch((err:any) => {
        Notification.show(err.message, 'alert');
        ProgressIndicator.hide();
      });
  }, [maxItemsPerPage, currentPageOffset]);
  
  /* Search */
  const onSearchInput = (e: React.FormEvent<HTMLInputElement>) => {
    const filteredResults = results.filter((result) => {
      return result.entityName.toLowerCase().match(e.currentTarget.value.toLowerCase()) || result.entityId.toLowerCase().match(e.currentTarget.value.toLowerCase());
    });
    setResults(filteredResults);
    if(e.currentTarget.value.length === 0) {
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
  const showDeleteConfirmModal = (tagItem: any) => {
    setEntityToBeDeleted(tagItem.id);
    setEntityToBeDeletedName(tagItem.entityName);
    setShowDeleteTagConfirmModal(true);
  };
  const showUpdateConfirmModal = (tagItem: any) => {
    setAddNewItem(true);
    setUpdateItemMode(true);
    setEntityId(tagItem.entityId);
    setEntityName(tagItem.entityName);
    setLcos(tagItem.localComplianceOfficer);
    setLcrs(tagItem.localComplianceResponsible);
    setDpcs(tagItem.dataProtectionCoordinator);
    setLcss(tagItem.localComplianceSpecialist);
    setEntityToBeUpdated(tagItem.id);
    setEntityToBeUpdatedName(tagItem.entityName);
    setEntityIdError(null);
    setEntityNameError(null);
    setCreatedDate(tagItem.createdDate);
  };
  const onCancellingDeleteChanges = () => {
    setShowDeleteTagConfirmModal(false);
  };
  const onAcceptDeleteChanges = () => {
    setShowDeleteTagConfirmModal(false);
    ProgressIndicator.show();
    const newArr = results.filter(object => {
      return object.id !== entityToBeDeleted;
    });
    DataComplianceApiClient.deleteDataComplianceNetworkList(entityToBeDeleted)
      .then((response:any) => {
        setResults(newArr);
        ProgressIndicator.hide();
        Notification.show('Legal entity deleted successfully.');
      })
      .catch((error:any) => {
        ProgressIndicator.hide();
        Notification.show('Legal entity not deleted.', 'alert');
      });
    setAddNewItem(false);
  };
  const onAddItemModalCancel = () => {
    setAddNewItem(false);
  };
  const onAddItemModalOpen = () => {
    resetEntity();
    setAddNewItem(true);
    setUpdateItemMode(false);
    DataComplianceApiClient.getEntityList(0, 0, 'entityId', 'asc')
      .then((res:any) => {
        setEntityList(res.records);
      })
      .catch((err:any) => {
        console.log(err);
      });
  };
  const updateConfirmModelOverlayCancel = () => {
    setUpdateConfirmModelOverlay(false);
  };
  const updateConfirmModelOverlayUpdate = () => {
    let formValid = true;
    let errorMessage = 'Please fill Entity ID and Entity Name';
    if(entityId.length > 0) {
      setEntityIdError(null);
    } else {
      setEntityIdError('*Missing entry');
      formValid = false;
    }
    if(entityName.length > 0) {
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
      setUpdateConfirmModelOverlay(true); 
    } else {
      Notification.show(errorMessage, 'alert');
    }
  };
  const onEntitySelect = (entity:any) => {
    setSelectedEntity(entity.entityID + ' - ' + entity.entityName);
    setEntityId(entity.entityID);
    setEntityName(entity.entityName);
  }
  const resetEntity = () => {
    setEntityId('');
    setEntityName('');
    setLcos([]);
    setLcrs([]);
    setDpcs([]);
    setLcss([]);
    setEntityIdError(null);
    setEntityNameError(null);
    setAddNew(false);
  }
  const onTagAddItem = () => {
    let formValid = true;
    let errorMessage = 'Please fill Entity ID and Entity Name';
    if(!addNew) {
      if(selectedEntity.length > 0) {
        errorMessage = 'Please select entity';
      } else {
        formValid = false;
      }
    } else {
      if(entityId.length > 0) {
        setEntityIdError(null);
      } else {
        setEntityIdError('*Missing entry');
        formValid = false;
      }
      if(entityName.length > 0) {
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
      setUpdateConfirmModelOverlay(false);
      const entity = {
        dataProtectionCoordinator: dpcs,
        entityId: entityId,
        entityName: entityName,
        localComplianceOfficer: lcos,
        localComplianceResponsible: lcrs,
        localComplianceSpecialist: lcss
      }
      const newResults = [...results, entity];
      const data: IDataComplianceRequest = {
        data: {
          dataProtectionCoordinator: dpcs,
          entityId: entityId,
          entityName: entityName,
          localComplianceOfficer: lcos,
          localComplianceResponsible: lcrs,
          localComplianceSpecialist: lcss
        }
      }
      DataComplianceApiClient.saveDataComplianceNetworkList(data)
        .then((response:any) => {
          setResults(newResults);
          ProgressIndicator.hide();
          Notification.show('Legal entity saved successfully.');
          resetEntity();
          setAddNewItem(false);
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
  const onTagUpdateItem = () => {
    setUpdateConfirmModelOverlay(false);
    ProgressIndicator.show();
    const newState = results.map(obj => {
      if (obj.id === entityToBeUpdated) {
        return {
          ...obj, 
          id: entityToBeUpdated,
          entityId: entityId, 
          entityName: entityName,
          localComplianceOfficer: lcos,
          localComplianceResponsible: lcrs,
          dataProtectionCoordinator: dpcs,
          localComplianceSpecialist: lcss 
        };
      }
      return obj;
    });
    const data: IDataComplianceRequest = {
      data: {
        id: entityToBeUpdated,
        dataProtectionCoordinator: dpcs,
        createdDate: createdDate,
        entityId: entityId,
        entityName: entityName,
        localComplianceOfficer: lcos,
        localComplianceResponsible: lcrs,
        localComplianceSpecialist: lcss
      }
    }
    DataComplianceApiClient.updateDataComplianceNetworkList(data)
      .then((response:any) => {
        Notification.show('Legal entity updated successfully.');
        setResults(newState);
        ProgressIndicator.hide();
        resetEntity();
        setAddNewItem(false);
      })
      .catch((error:any) => {
        ProgressIndicator.hide();
        Notification.show(error.message, 'alert');
      });
  };
  const onChangeEntityId = (e: React.FormEvent<HTMLInputElement>) => {
    setEntityId(e.currentTarget.value);
    if(e.currentTarget.value.length > 0) {
      setEntityIdError(null);
    } else {
      setEntityIdError('*Missing entry');
    }
  };
  const onChangeEntityName = (e: React.FormEvent<HTMLInputElement>) => {
    setEntityName(e.currentTarget.value);
    if(e.currentTarget.value.length > 0) {
      setEntityNameError(null);
    } else {
      setEntityNameError('*Missing entry');
    }
  };

  /* jsx */
  const resultData = results.map((result) => {
    return (
      <RowItem
        item={result}
        key={result.id}
        showDeleteConfirmModal={showDeleteConfirmModal}
        showUpdateConfirmModal={showUpdateConfirmModal}
      />
    );
  });
  const modalContent: React.ReactNode = (
    <div id="contentparentdiv" className={Styles.modalContentWrapper}>
      <div className={Styles.modalTitle}>Delete Tag</div>
      <div className={Styles.modalContent}>
        The tag &laquo;{entityToBeDeletedName ? entityToBeDeletedName : ''}&raquo; will be removed
        permanently.
      </div>
    </div>
  );
  const contentForAddNewItem = (
    <div id="addOrUpdateFormWrapper" className={Styles.infoPopup}>
      <div className={classNames(Styles.modalContent, Styles.formWrapperMain)}>
        <button className={Styles.btnSwitch} onClick={() => setAddNew(!addNew)}>{ addNew ? 'Back to Search' : 'Add New Entity' }</button>
        { addNew ? 
          <>
            <TextBox
              type="text"
              controlId={'entity-id'}
              label={'Entity ID'}
              placeholder={"Type here"}
              value={entityId}
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
              value={entityName}
              errorText={entityNameError}
              required={true}
              maxLength={200}
              onChange={onChangeEntityName}
            />
          </>   : 
          <>
          {
            entityList.length > 0 &&
              <TypeAheadBox
                label={'Legal Entity'}
                placeholder={"Search Entity ID or Entity Name"}
                list={entityList}
                defaultValue={'001'}
                onItemSelect={onEntitySelect}
                required={true}
              />
          }
          </>
        }
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Officer (LCO)'}
            max={100}
            chips={lcos}
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
            chips={lcrs}
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
            chips={dpcs}
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
            chips={lcss}
            setTags={setLcssIds}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          { lcsError && <span className={classNames("error-message", Styles.tagError)}>Please enter valid email address.</span> }
        </div>
        <div className={Styles.addBtn}>
          <button
            onClick={updateItemMode ? updateConfirmModelOverlayUpdate : onTagAddItem}
            className={Styles.actionBtn + ' btn btn-tertiary'}
            type="button"
          >
            {updateItemMode ? <span>Update</span> : <span>Add</span>}
          </button>
        </div>
      </div>
      {updateConfirmModelOverlay && (
        <div className={Styles.updateModelOverlayContent}>
          <p>
            Updating &lt;&lt;{entityToBeUpdatedName ? entityToBeUpdatedName : ''}&gt;&gt; would also update all the associated
            solutions. <br /> Do you want to proceed?
          </p>
          <div>
            <button
              className={Styles.actionBtn + ' btn btn-default'}
              type="button"
              onClick={updateConfirmModelOverlayCancel}
            >
              Cancel
            </button>{' '}
            &nbsp;
            <button className={Styles.actionBtn + ' btn btn-tertiary'} type="button" onClick={onTagUpdateItem}>
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
          <div className={`input-field-group search-field ${isResultLoading ? 'disabled' : ''}`}>
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
              onChange={onSearchInput}
              disabled={isResultLoading}
            />
          </div>
          <div
            className={classNames(
              Styles.addItemButton,
              isResultLoading ? Styles.disabledAddItemBtn : '',
            )}
          >
            <button onClick={onAddItemModalOpen}>
              <i className="icon mbc-icon plus" />
              <span>Add New Item</span>
            </button>
          </div>
          <div className={Styles.addItemButton}>
            <button>
              <i className="icon mbc-icon link" />
              <span>See Change Log</span>
            </button>
          </div>
        </div>
        {!isResultLoading ? (
          resultData.length === 0 ? (
            <div className={Styles.tagIsEmpty}>There is no list available</div>
          ) : (
            <div className={Styles.tablePanel}>
              <table className="ul-table">
                <thead>
                  <tr className="header-row">
                    <th onClick={() => sortTags('entityId', asortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (asortBy.name === 'entityId' ? asortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Entity ID
                      </label>
                    </th>
                    <th onClick={() => sortTags('entityName', asortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (asortBy.name === 'entityName' ? asortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Entity Name
                      </label>
                    </th>
                    <th>
                      <label>
                          <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="- first point of contact for data compliance question
                        - reporting line to IL/C" />
                          Local Compliance Officer (LCO)
                      </label>
                    </th>
                    <th>
                      <label>
                          <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="- first point of contact for data compliance question
                          - employee of the respective Group entity" />
                          Local Compliance Responsible (LCR)
                      </label>
                    </th>
                    <th>
                      <label>
                          <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="(old role, sucessive to be replaced by LCO/LCR)" />
                          Data Protection Coordinator (DPC)
                      </label>
                    </th>
                    <th>
                      <label>
                          <i className={classNames("icon mbc-icon info iconsmd", Styles.infoIcon)} tooltip-data="in case of reporting line to LCO, Local Compliance Specialist.
                          In all other cases Local Compliance Support" />
                          Local Compliance Support / Specialist (LCS)
                      </label>
                    </th>
                    <th>
                      <label>Action</label>
                    </th>
                  </tr>
                </thead>
                <tbody>{resultData}</tbody>
              </table>
              {results.length &&
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
          )
        ) : (
          <Spinner />
        )}
        <ConfirmModal
          title="Delete Tag"
          acceptButtonTitle="Delete"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={showDeleteTagConfirmModal}
          content={modalContent}
          onCancel={onCancellingDeleteChanges}
          onAccept={onAcceptDeleteChanges}
        />
        <InfoModal
          title={updateItemMode ? 'Update Item' : 'Add New Item'}
          modalWidth={'35vw'}
          show={addNewItem}
          content={contentForAddNewItem}
          onCancel={onAddItemModalCancel}
        />
      </div>
    </div>
  );
}

export default DataComplianceNetworkList;
