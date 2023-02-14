import React, { useState, useEffect } from 'react';
import { debounce } from 'lodash';
import cn from 'classnames';
import Styles from './DataComplianceNetworkList.scss';

// components from container app
import Pagination from 'dna-container/Pagination';
import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';
import TextBox from 'dna-container/TextBox';
import Spinner from 'dna-container/Spinner';
import TypeAheadBox from 'dna-container/TypeAheadBox';
import Tags from 'dna-container/Tags';
import Caption from 'dna-container/Caption';

import RowItem from './rowItem/RowItem';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import { SESSION_STORAGE_KEYS, USER_ROLE } from '../../Utility/constants';

import { dataComplianceNetworkListApi } from '../../apis/datacompliancenetworklist.api';
import { regionalDateAndTimeConversionSolution, validateEmail } from '../../Utility/utils';

const classNames = cn.bind(Styles);

const DataComplianceNetworkList = (props) => {
  const isAdmin =
    props.user.roles.find((role) => role.id === USER_ROLE.ADMIN || role.id === USER_ROLE.DATACOMPLIANCEADMIN) !==
    undefined;

  const [loading, setLoading] = useState(false);

  const [sortBy, setSortBy] = useState({
    name: 'entityId',
    currentSortType: 'desc',
    nextSortType: 'asc',
  });

  /* Pagination */
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );

  const [searchTerm, setSearchTerm] = useState('');

  const [dataComplianceNetworkList, setDataComplianceNetworkList] = useState([]);

  const [showDeleteEntityConfirmModal, setShowDeleteEntityConfirmModal] = useState(false);
  const [updateConfirmModalOverlay, setUpdateConfirmModalOverlay] = useState(false);

  const [updateMode, setUpdateMode] = useState(false);
  const [showEntityFormModal, setShowEntityFormModal] = useState(false);

  const [entityList, setEntityList] = useState([]);

  const [showChangeLog, setShowChangeLog] = useState(false);
  const [changeLogs, setChangeLogs] = useState([]);

  const [entity, setEntity] = useState({
    id: '',
    entityId: '',
    entityName: '',
    entityCountry: '',
    localComplianceOfficer: [],
    localComplianceResponsible: [],
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

  const dummyTags = [];

  /* error states */
  const [entityIdError, setEntityIdError] = useState('');
  const [entityNameError, setEntityNameError] = useState('');
  const [entityCountryError, setEntityCountryError] = useState('');
  const [entityError, setEntityError] = useState(false);
  const [localComplianceOfficerError, setLocalComplianceOfficerError] = useState(false);
  const [localComplianceResponsibleError, setLocalComplianceResponsibleError] = useState(false);
  const [localComplianceSpecialistError, setLocalComplianceSpecialistError] = useState(false);

  const [entitySearch, setEntitySearch] = useState(true);

  /* Email Validation */
  const setLocalComplianceOfficer = (arr) => {
    setLocalComplianceOfficerError(false);
    arr.map((item) => {
      if (validateEmail(item)) {
        return item;
      } else {
        setLocalComplianceOfficerError(true);
      }
    });
    setEntity({ ...entity, localComplianceOfficer: arr });
  };
  const setLocalComplianceResponsible = (arr) => {
    setLocalComplianceResponsibleError(false);
    arr.map((item) => {
      if (validateEmail(item)) {
        return item;
      } else {
        setLocalComplianceResponsibleError(true);
      }
    });
    setEntity({ ...entity, localComplianceResponsible: arr });
  };
  const setLocalComplianceSpecialist = (arr) => {
    setLocalComplianceSpecialistError(false);
    arr.map((item) => {
      if (validateEmail(item)) {
        return item;
      } else {
        setLocalComplianceSpecialistError(true);
      }
    });
    setEntity({ ...entity, localComplianceSpecialist: arr });
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    setLoading(true);
    setData();
    dataComplianceNetworkListApi
      .getEntityList(0, 0, sortBy.name, 'asc')
      .then((res) => {
        res.data?.records?.map((item) => {
          item['name'] = item.entityId.toString() + ' - ' + item.entityName.toString();
          return item;
        });
        setEntityList(res.data);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const setData = async () => {
    await getResults('');
  };

  /* getResults */
  const getResults = async (action) => {
    const showProgressIndicator = ['add', 'update', 'delete'].includes(action);
    const showContentLoader = ['reset', 'categoryChange', 'search', 'pagination'].includes(action);

    showProgressIndicator && ProgressIndicator.show();
    showContentLoader && setLoading(true);

    let results = [];

    await dataComplianceNetworkListApi
      .getDataComplianceNetworkList(0, 0, sortBy.name, 'asc')
      .then((res) => {
        if (res.data.records) {
          results = [...res.data.records];
        }
      })
      .catch((err) => {
        Notification.show(err.message, 'alert');
        setDataComplianceNetworkList([]);
      });

    if (searchTerm) {
      results = results.filter((result) => {
        const localComplianceOfficers = result.localComplianceOfficer.toString();
        const localComplianceResponsibles = result.localComplianceResponsible.toString();
        const localComplianceSpecialists = result.localComplianceSpecialist.toString();
        return (
          result.entityName.toLowerCase().includes(searchTerm) ||
          result.entityCountry.toLowerCase().includes(searchTerm) ||
          result.entityId.toLowerCase().includes(searchTerm) ||
          localComplianceOfficers.includes(searchTerm) ||
          localComplianceResponsibles.includes(searchTerm) ||
          localComplianceSpecialists.includes(searchTerm)
        );
      });
    }

    if (sortBy) {
      if (sortBy.name === 'entityId') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.entityId.toLowerCase() === b.entityId.toLowerCase() ? 0 : -1;
          } else {
            return a.entityId.toLowerCase() === b.entityId.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'entityName') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.entityName.toLowerCase() === b.entityName.toLowerCase() ? 0 : -1;
          } else {
            return a.entityName.toLowerCase() === b.entityName.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'entityCountry') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.entityCountry.toLowerCase() === b.entityCountry.toLowerCase() ? 0 : -1;
          } else {
            return a.entityCountry.toLowerCase() === b.entityCountry.toLowerCase() ? -1 : 0;
          }
        });
      }
    }

    setDataComplianceNetworkList(
      results.slice(
        currentPageOffset > results.length ? 0 : currentPageOffset,
        currentPageOffset + maxItemsPerPage < results.length ? currentPageOffset + maxItemsPerPage : results.length,
      ),
    );
    setTotalNumberOfPages(Math.ceil(results.length / maxItemsPerPage));
    setCurrentPageNumber(
      currentPageNumber > Math.ceil(results.length / maxItemsPerPage)
        ? Math.ceil(results.length / maxItemsPerPage) > 0
          ? Math.ceil(results.length / maxItemsPerPage)
          : 1
        : currentPageNumber,
    );
    showProgressIndicator && ProgressIndicator.hide();
    showContentLoader && setLoading(false);
  };

  /* Sort */
  const sortEntities = (propName, sortOrder) => {
    const tempSortBy = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: sortBy.currentSortType,
    };
    setSortBy(tempSortBy);
  };
  useEffect(() => {
    getResults('sort');
  }, [sortBy]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    getResults('pagination');
  }, [maxItemsPerPage, currentPageOffset, currentPageNumber]); // eslint-disable-line react-hooks/exhaustive-deps

  /* Search */
  useEffect(() => {
    getResults('search');
  }, [searchTerm]); // eslint-disable-line react-hooks/exhaustive-deps
  const onSearchTextChange = debounce((e) => {
    const input = e.target;
    const searchText = input.value.toLowerCase();
    setSearchTerm(searchText);
  }, 500);

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
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  /* Modal handling */
  const showDeleteConfirmModal = (entity) => {
    setEntity({
      ...entity,
      id: entity.id,
      entityName: entity.entityName,
    });
    setShowDeleteEntityConfirmModal(true);
  };
  const showUpdateConfirmModal = (entity) => {
    setShowEntityFormModal(true);
    setUpdateMode(true);
    setEntitySearch(true);
    setEntity({
      ...entity,
      id: entity.id,
      entityId: entity.entityId,
      entityName: entity.entityName,
      entityCountry: entity.entityCountry,
      localComplianceOfficer: entity.localComplianceOfficer,
      localComplianceResponsible: entity.localComplianceResponsible,
      localComplianceSpecialist: entity.localComplianceSpecialist,
      createdDate: entity.createdDate,
      createdBy: entity.createdBy,
    });

    setEntityIdError(null);
    setEntityNameError(null);
    setEntityCountryError(null);
  };
  const onCancelDeleteChanges = () => {
    setShowDeleteEntityConfirmModal(false);
  };
  const onAcceptDeleteChanges = () => {
    setShowDeleteEntityConfirmModal(false);
    ProgressIndicator.show();
    const newArr = dataComplianceNetworkList.filter((object) => {
      return object.id !== entity.id;
    });
    dataComplianceNetworkListApi
      .deleteDataComplianceNetworkList(entity.id)
      .then((response) => {
        console.log(response);
        setDataComplianceNetworkList(newArr);
        ProgressIndicator.hide();
        Notification.show('Legal entity deleted successfully.');
      })
      .catch((error) => {
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

    if (entitySearch) {
      if (!entity.entityId && entity.entityId.length === 0 && entity.entityName.length === 0) {
        errorMessage = 'Please select entity';
        formValid = false;
        setEntityError(true);
      }
    } else {
      if (entity.entityId.length > 0) {
        setEntityIdError(null);
      } else {
        setEntityIdError('*Missing entry');
        formValid = false;
      }
      if (entity.entityName.length > 0) {
        setEntityNameError(null);
      } else {
        setEntityNameError('*Missing entry');
        formValid = false;
      }
    }
    if (entity.entityCountry.length > 0) {
      setEntityCountryError(null);
    } else {
      setEntityCountryError('*Missing entry');
      formValid = false;
    }
    if (
      localComplianceOfficerError ||
      localComplianceResponsibleError ||
      localComplianceSpecialistError
    ) {
      formValid = false;
      errorMessage = 'Please enter emails in correct format';
    }
    if (formValid) {
      setUpdateConfirmModalOverlay(true);
    } else {
      Notification.show(errorMessage, 'alert');
    }
  };
  const onEntitySelect = (entity) => {
    setEntity((prev) => ({ ...prev, entityId: entity.entityId, entityName: entity.entityName }));
  };
  const resetEntity = () => {
    setEntity({
      ...entity,
      entityId: '',
      entityName: '',
      localComplianceOfficer: [],
      localComplianceResponsible: [],
      localComplianceSpecialist: [],
    });

    setEntityError(false);
    setEntityIdError(null);
    setEntityNameError(null);
    setEntityCountryError(null);
    
    setEntitySearch(true);
  };
  const onEntityAdd = () => {
    let formValid = true;
    let errorMessage = 'Please fill Entity ID and Entity Name';
    if (entitySearch) {
      if (!entity.entityId && entity.entityId.length === 0 && entity.entityName.length === 0) {
        errorMessage = 'Please select entity';
        formValid = false;
        setEntityError(true);
      }
    } else {
      if (entity.entityId.length > 0) {
        setEntityIdError(null);
      } else {
        setEntityIdError('*Missing entry');
        formValid = false;
      }
      if (entity.entityName.length > 0) {
        setEntityNameError(null);
      } else {
        setEntityNameError('*Missing entry');
        formValid = false;
      }
    }
    if (entity.entityCountry.length > 0) {
      setEntityCountryError(null);
    } else {
      setEntityCountryError('*Missing entry');
      formValid = false;
    }
    if (
      localComplianceOfficerError ||
      localComplianceResponsibleError ||
      localComplianceSpecialistError
    ) {
      formValid = false;
      errorMessage = 'Please enter emails in correct format';
    }
    if (formValid) {
      ProgressIndicator.show();
      setUpdateConfirmModalOverlay(false);
      const data = {
        entityId: entity.entityId,
        entityName: entity.entityName,
        entityCountry: entity.entityCountry,
        localComplianceOfficer: entity.localComplianceOfficer,
        localComplianceResponsible: entity.localComplianceResponsible,
        localComplianceSpecialist: entity.localComplianceSpecialist,
      };
      dataComplianceNetworkListApi
        .saveDataComplianceNetworkList(data)
        .then(() => {
          getResults('add');
          ProgressIndicator.hide();
          Notification.show('Legal entity saved successfully.');
          resetEntity();
          setShowEntityFormModal(false);
        })
        .catch((error) => {
          ProgressIndicator.hide();
          error.response.status === 409
            ? Notification.show(error.response.data.errors[0].message, 'alert')
            : Notification.show(error.message, 'alert');
        });
    } else {
      Notification.show(errorMessage, 'alert');
      ProgressIndicator.hide();
    }
  };
  const onEntityUpdate = () => {
    setUpdateConfirmModalOverlay(false);
    ProgressIndicator.show();
    const newState = dataComplianceNetworkList.map((obj) => {
      if (obj.id === entity.id) {
        return {
          ...obj,
          id: entity.id,
          entityId: entity.entityId,
          entityName: entity.entityName,
          entityCountry: entity.entityCountry,
          localComplianceOfficer: entity.localComplianceOfficer,
          localComplianceResponsible: entity.localComplianceResponsible,
          localComplianceSpecialist: entity.localComplianceSpecialist,
        };
      }
      return obj;
    });
    const data = {
      id: entity.id,
      createdDate: entity.createdDate,
      createdBy: entity.createdBy,
      entityId: entity.entityId,
      entityName: entity.entityName,
      entityCountry: entity.entityCountry,
      localComplianceOfficer: entity.localComplianceOfficer,
      localComplianceResponsible: entity.localComplianceResponsible,
      localComplianceSpecialist: entity.localComplianceSpecialist,
    };
    dataComplianceNetworkListApi
      .updateDataComplianceNetworkList(data)
      .then((response) => {
        console.log(response);
        Notification.show('Legal entity updated successfully.');
        setDataComplianceNetworkList(newState);
        ProgressIndicator.hide();
        resetEntity();
        setShowEntityFormModal(false);
      })
      .catch((error) => {
        ProgressIndicator.hide();
        Notification.show(error.message, 'alert');
      });
  };
  const onChangeEntityId = (e) => {
    setEntity({ ...entity, entityId: e.currentTarget.value });
    if (e.currentTarget.value.length > 0) {
      setEntityIdError(null);
    } else {
      setEntityIdError('*Missing entry');
    }
  };
  const onChangeEntityName = (e) => {
    setEntity({ ...entity, entityName: e.currentTarget.value });
    if (e.currentTarget.value.length > 0) {
      setEntityNameError(null);
    } else {
      setEntityNameError('*Missing entry');
    }
  };

  const onChangeEntityCountry = (e) => {
    setEntity({ ...entity, entityCountry: e.currentTarget.value });
    if (e.currentTarget.value.length > 0) {
      setEntityCountryError(null);
    } else {
      setEntityCountryError('*Missing entry');
    }
  };

  /* jsx */
  const deleteModalContent = (
    <div id="contentparentdiv" className={Styles.modalContentWrapper}>
      <div className={Styles.modalTitle}>Delete Entity</div>
      <div className={Styles.modalContent}>
        <p>The entity &laquo;{entity.entityName ? entity.entityName : ''}&raquo; will be removed permanently.</p>
      </div>
    </div>
  );
  const entityFormModalContent = (
    <div id="addOrUpdateFormWrapper" className={Styles.infoPopup}>
      <div className={classNames(Styles.modalContent, Styles.formWrapperMain)}>
        <button className={Styles.btnSwitch} onClick={() => setEntitySearch(!entitySearch)}>
          {entitySearch ? 'Add New Entity' : 'Back to Search'}
        </button>
        {entitySearch ? (
          <>
            <TypeAheadBox
              controlId={'entity'}
              label={'Entity'}
              placeholder={'Search Entity ID or Entity Name'}
              required={true}
              defaultValue={
                updateMode && entity?.entityId?.length > 0 && entity?.entityName?.length > 0
                  ? entity?.entityId + ' - ' + entity?.entityName
                  : ''
              }
              list={entityList.records}
              setSelected={onEntitySelect}
              showError={entityError}
            />
          </>
        ) : (
          <>
            <TextBox
              type="text"
              controlId={'entity-id'}
              label={'Entity ID'}
              placeholder={'Type here'}
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
              placeholder={'Type here'}
              value={entity.entityName}
              errorText={entityNameError}
              required={true}
              maxLength={200}
              onChange={onChangeEntityName}
            />
          </>
        )}
        <TextBox
          type="text"
          controlId={'entity-country'}
          label={'Country'}
          placeholder={'Type here'}
          value={entity.entityCountry}
          errorText={entityCountryError}
          required={true}
          maxLength={200}
          onChange={onChangeEntityCountry}
        />
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Officer (LCO)'}
            max={100}
            chips={entity.localComplianceOfficer}
            setTags={setLocalComplianceOfficer}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          {localComplianceOfficerError && (
            <span className={classNames('error-message', Styles.tagError)}>Please enter valid email address.</span>
          )}
        </div>
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Responsible (LCR)'}
            max={100}
            chips={entity.localComplianceResponsible}
            setTags={setLocalComplianceResponsible}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          {localComplianceResponsibleError && (
            <span className={classNames('error-message', Styles.tagError)}>Please enter valid email address.</span>
          )}
        </div>
        <div className={Styles.tagControl}>
          <Tags
            title={'Local Compliance Support / Specialist (LCS)'}
            max={100}
            chips={entity.localComplianceSpecialist}
            setTags={setLocalComplianceSpecialist}
            tags={dummyTags}
            isMandatory={false}
            showMissingEntryError={false}
          />
          {localComplianceSpecialistError && (
            <span className={classNames('error-message', Styles.tagError)}>Please enter valid email address.</span>
          )}
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
          <p>Do you want to proceed with updating &lt;&lt;{entity.entityName ? entity.entityName : ''}&gt;&gt;?</p>
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

  const changeLogContent = (
    <table className="ul-table solutions">
      <tbody>
        {changeLogs?.length
          ? changeLogs.map((data, index) => {
              return (
                <tr key={index} className="data-row">
                  <td className="wrap-text">{regionalDateAndTimeConversionSolution(data.createdOn)}</td>
                  <td className="wrap-text">
                    {data.createdBy.firstName}&nbsp;{data.createdBy.lastName}
                  </td>
                  <td>
                    <span className="hidden">`</span>
                  </td>
                  <td>
                    <span className="hidden">`</span>
                  </td>
                  <td>
                    <span className="hidden">`</span>
                  </td>
                  <td>
                    <span className="hidden">`</span>
                  </td>
                  <td>
                    <span className="hidden">`</span>
                  </td>
                  <td className="wrap-text">{data.message}</td>
                </tr>
              );
            })
          : null}
      </tbody>
    </table>
  );

  const handleChangeLogModal = () => {
    ProgressIndicator.show();
    dataComplianceNetworkListApi
      .getDataComplianceChangeLogs()
      .then((res) => {
        console.log(res);
        setShowChangeLog(true);
        setChangeLogs(res.data.records);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(
          e?.response?.data?.errors[0]?.message || 'Erorr while fetching data compliance network list change logs',
          'alert',
        );
      });
  };

  const handleChangeLogModalClose = () => {
    setChangeLogs([]);
    setShowChangeLog(false);
  };

  return (
    <div className={Styles.mainPanel}>
      <Caption title="Data Compliance Network List" />
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
            />
          </div>
          {isAdmin && (
            <>
              <div className={classNames(Styles.addItemButton, loading ? Styles.disabledAddItemBtn : '')}>
                <button onClick={onEntityFormModalOpen}>
                  <i className="icon mbc-icon plus" />
                  <span>Add New Entity</span>
                </button>
              </div>
              <div className={Styles.addItemButton}>
                <button onClick={handleChangeLogModal}>
                  <i className="icon mbc-icon link" />
                  <span>{showChangeLog ? 'Hide Change Log' : 'See Change Log'}</span>
                </button>
              </div>
            </>
          )}
        </div>
        {loading && <Spinner />}
        {!loading && dataComplianceNetworkList.length === 0 && (
          <div className={Styles.tagIsEmpty}>There is no list available</div>
        )}
        {!loading && dataComplianceNetworkList.length > 0 && (
          <div className={Styles.tablePanel}>
            <div className={Styles.tableResponsive}>
              <table className="ul-table">
                <thead>
                  <tr className="header-row">
                    <th onClick={() => sortEntities('entityId', sortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' + (sortBy.name === 'entityId' ? sortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Entity ID
                      </label>
                    </th>
                    <th onClick={() => sortEntities('entityName', sortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' + (sortBy.name === 'entityName' ? sortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Entity Name
                      </label>
                    </th>
                    <th onClick={() => sortEntities('entityCountry', sortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' + (sortBy.name === 'entityCountry' ? sortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Country
                      </label>
                    </th>
                    <th>
                      <label>
                        <i
                          className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                          tooltip-data="- First point of contact for data compliance question
                        - Reporting line to IL/C"
                        />
                        Local Compliance Officer (LCO)
                      </label>
                    </th>
                    <th>
                      <label>
                        <i
                          className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                          tooltip-data="- First point of contact for data compliance question
                          - Employee of the respective Group entity"
                        />
                        Local Compliance Responsible (LCR)
                      </label>
                    </th>
                    {/* <th>
                      <label>
                        <i
                          className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                          tooltip-data="(old role, sucessive to be 
                            replaced by LCO/LCR)"
                        />
                        Data Protection Coordinator (DPC)
                      </label>
                    </th> */}
                    <th>
                      <label>
                        <i
                          className={classNames('icon mbc-icon info iconsmd', Styles.infoIcon)}
                          tooltip-data="- In case of reporting line to LCO, Local Compliance Specialist.
                          - In all other cases Local Compliance Support"
                        />
                        Local Compliance Support / Specialist (LCS)
                      </label>
                    </th>
                    {isAdmin && (
                      <th className={Styles.actionLinksTD}>
                        <label>Action</label>
                      </th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {dataComplianceNetworkList.map((result) => {
                    return (
                      <RowItem
                        item={result}
                        key={result.id}
                        isAdmin={isAdmin}
                        showDeleteConfirmModal={showDeleteConfirmModal}
                        showUpdateConfirmModal={showUpdateConfirmModal}
                      />
                    );
                  })}
                </tbody>
              </table>
            </div>
            {dataComplianceNetworkList.length && (
              <Pagination
                totalPages={totalNumberOfPages}
                pageNumber={currentPageNumber}
                onPreviousClick={onPaginationPreviousClick}
                onNextClick={onPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
              />
            )}
          </div>
        )}
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
        {showEntityFormModal && (
          <InfoModal
            title={updateMode ? 'Update Entity' : 'Add New Entity'}
            modalWidth={'35vw'}
            show={showEntityFormModal}
            content={entityFormModalContent}
            onCancel={onShowEntityFormModalCancel}
          />
        )}
        <InfoModal
          title={'Change Logs'}
          show={showChangeLog}
          content={changeLogs?.length ? changeLogContent : ''}
          onCancel={handleChangeLogModalClose}
        />
      </div>
    </div>
  );
};

export default DataComplianceNetworkList;
