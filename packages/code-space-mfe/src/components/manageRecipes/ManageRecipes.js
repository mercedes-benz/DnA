import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './ManageRecipes.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import RecipeList from './RecipeList';
import Pagination from 'dna-container/Pagination';
import Modal from 'dna-container/Modal';
import { IconGear } from 'dna-container/IconGear';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import { getQueryParameterByName } from 'dna-container/Query';
import Caption from 'dna-container/Caption';
import { Notification } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { Tooltip } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import ViewRecipe from '../codeSpaceRecipe/ViewRecipe';
import RecipeCard from '../recipeCard/RecipeCard';
import { USER_ROLE } from '../../Utility/constants';

const ManageRecipes = ({ user }) => {
  const history = useHistory();

  const isAdmin = user.roles.find((role) => role.id === USER_ROLE.CODESPACEADMIN) !== undefined;

  const [loading, setLoading] = useState(true);
  const [recipes, setRecipes] = useState([]);

  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState({});

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  // pagination
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  );
  const [sortBy, setSortBy] = useState({ name: 'requestedDate', currentSortType: 'desc', nextSortType: 'asc' });

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberInit = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetInit = pageNumberOnQuery ? (currentPageNumber - 1) * maxItemsPerPage : 0;
    setCurrentPageNumber(currentPageNumberInit);
    setCurrentPageOffset(currentPageOffsetInit);
  }, [currentPageNumber, maxItemsPerPage]);

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetInner = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetInner);
  };

  const onPaginationNextClick = async () => {
    let currentPageNum = currentPageNumber;
    const currentPageOffsetInner = currentPageNum * maxItemsPerPage;
    setCurrentPageOffset(currentPageOffsetInner);
    currentPageNum = currentPageNum + 1;
    setCurrentPageNumber(currentPageNum);
  };

  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    getCodespaceRecipes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPageOffset, maxItemsPerPage]);

  const [additionalServices, setAdditionalServices] = useState([]);

  useEffect(() => {
    CodeSpaceApiClient.getAdditionalServicesLov()
      .then((response) => {
        setAdditionalServices(response.data.data);
      })
      .catch((err) => {
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);

  const getCodespaceRecipes = () => {
    ProgressIndicator.show();
    setLoading(true);
    CodeSpaceApiClient.getCodeSpaceRecipes()
      .then((res) => {
        setLoading(false);
        ProgressIndicator.hide();
        if(Array.isArray(res?.data?.data)) {
          const totalNumberOfPagesInner = Math.ceil(res?.data?.count / maxItemsPerPage);
          setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
          setTotalNumberOfPages(totalNumberOfPagesInner);
          setRecipes(res?.data?.data);
        } else {
          setRecipes([]);
        }
      })
      .catch((err) => {
        setLoading(false);
        ProgressIndicator.hide();
        Notification.show(err?.message || 'Something went wrong.', 'alert');
      });
  };

  const sortByColumn = (propName, sortOrder) => {
    if (!propName && !sortOrder) {
      propName = 'requestedDate';
      sortOrder = 'desc';
    }
    const newSortType = sortOrder === 'asc' ? 'desc' : 'asc';
    const newSortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: newSortType,
    };
    const convertToDateObj = (date) => {
      const parts = date.split(', ');
      const dateParts = parts[0].split('/');
      const timeParts = parts[1].split(':');
      return new Date(dateParts[2], dateParts[1] - 1, dateParts[0], timeParts[0], timeParts[1], timeParts[2]);
    };

    let data = recipes;
    if (propName === 'recipeName') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.recipeName.toLowerCase().localeCompare(b.recipeName.toLowerCase());
        } else {
          return b.recipeName.toLowerCase().localeCompare(a.recipeName.toLowerCase());
        }
      });
    } else if (propName === 'createdBy') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.createdBy.firstName.toLowerCase().localeCompare(b.createdBy.firstName.toLowerCase());
        } else {
          return b.createdBy.firstName.toLowerCase().localeCompare(a.createdBy.firstName.toLowerCase());
        }
      });
    } else if (propName === 'projectStatus') {
      data = data.sort((a, b) => {
        if (sortOrder === 'asc') {
          return a.status.toLowerCase().localeCompare(b.status.toLowerCase());
        } else {
          return b.status.toLowerCase().localeCompare(a.status.toLowerCase());
        }
      });
    } else if (propName === 'createdOn') {
      data = data.sort((a, b) => {
        const dateA = convertToDateObj(regionalDateAndTimeConversionSolution(a.createdOn)).getTime();
        const dateB = convertToDateObj(regionalDateAndTimeConversionSolution(b.createdOn)).getTime();
        if (sortOrder === 'asc') {
          return dateA - dateB;
        } else {
          return dateB - dateA;
        }
      });
    }
    setRecipes(data);

    setSortBy(newSortField);
  };

  const handleRecipeDelete = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpaceRecipe(selectedRecipe?.id)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show("Recipe Deleted Successfully");
        setShowDeleteModal(false);
        getCodespaceRecipes();
      }).catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
      });
  }

  return (
    <>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <Caption title="Manage Recipes" onBackClick={() => history.push('/')}>
            <div className={classNames(Styles.listHeader)}>
              {isAdmin && 
                <div className={Styles.actionBtns}>
                  <button
                    className={classNames('btn btn-primary', Styles.btnOutline)}
                    type="button"
                    onClick={() => history.push('/administration')}
                  >
                    <IconGear size={'14'} />
                    <span>Administration</span>
                  </button>
                </div>
              }
              <div>
                <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={getCodespaceRecipes}>
                  <i className="icon mbc-icon refresh"></i>
                </button>
              </div>
              <span className={Styles.dividerLine}> &nbsp; </span>
              <div tooltip-data="Card View">
                <span
                  className={cardViewMode ? Styles.iconActive : Styles.iconInactive}
                  onClick={() => {
                    setCardViewMode(true);
                    setListViewMode(false);
                    sessionStorage.removeItem('storageListViewModeEnable');
                  }}
                >
                  <i className="icon mbc-icon widgets" />
                </span>
              </div>
              <span className={Styles.dividerLine}> &nbsp; </span>
              <div tooltip-data="List View">
                <span
                  className={listViewMode ? Styles.iconActive : Styles.iconInactive}
                  onClick={() => {
                    setCardViewMode(false);
                    setListViewMode(true);
                    sessionStorage.setItem('storageListViewModeEnable', true);
                  }}
                >
                  <i className="icon mbc-icon listview big" />
                </span>
              </div>
            </div>
          </Caption>
          <div className="tabs-content-wrapper">
            <div id="tab-content-2" className="tab-content">
              <div className={Styles.content}>
                {!loading && recipes?.length === 0 &&
                  <div className={Styles.noRequests}>
                    <h5>You don&apos;t have any Code Space Recipe at this time.</h5>
                    <p>Please add a new one.</p>
                    <button
                      className={classNames('btn btn-tertiary')}
                      type="button"
                      onClick={() => history.push('/codespaceRecipes')}
                    >
                      <span>Add New Recipe</span>
                    </button>
                  </div>
                }
                {!loading && cardViewMode && recipes?.length > 0 &&
                  <>
                    <div className={classNames(Styles.projectsContainer)}>
                      <div className={Styles.createNewCard} onClick={() => history.push('/codespaceRecipes/manageRecipe')}>
                        <div className={Styles.addicon}> &nbsp; </div>
                        <label className={Styles.addlabel}>Add New Recipe</label>
                      </div>
                      {recipes.map((recipe) => 
                        <RecipeCard
                          key={recipe.id}
                          recipe={recipe}
                          onSelectRecipe={(recipe) => { 
                            setSelectedRecipe(recipe);
                            setShowDetailsModal(true);}
                          }
                          onDeleteRecipe={(recipe) => {
                            setSelectedRecipe(recipe);
                            setShowDeleteModal(true);
                          }}
                          additionalServices={additionalServices?.filter(service => recipe?.additionalServices?.includes(service?.serviceName))}
                        />
                      )}
                    </div>
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  </>
                }
                {listViewMode && (
                  <>
                    {recipes && recipes?.length ? (
                      <div className={Styles.createNewArea}>
                        <button className={'btn btn-secondary'} type="button" onClick={() => history.push('/codespaceRecipes/manageRecipe')}>
                          <span className={Styles.addCircle}>
                            <i className="icon mbc-icon plus" />
                          </span>
                          <span>Add New Recipe</span>
                        </button>
                      </div>
                    ) : null}
                  </>
                )}
                {!loading && listViewMode && recipes?.length > 0 &&
                  <div className={Styles.allCodeSpace}>
                    <div className={Styles.allcodeSpaceListviewContent}>
                      <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                        <thead>
                          <tr className={classNames('header-row', Styles.tableTitle)}>
                            <th className={Styles.softwareColumn} onClick={() => sortByColumn('', sortBy.nextSortType)}>
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (sortBy.name === 'recipeName' ? sortBy.currentSortType : '')
                                }
                              >
                                <i className="icon sort" />
                                Recipe Name
                              </label>
                            </th>
                            <th>
                              <label>
                                Hardware Configuration
                              </label>
                            </th>
                            <th className={Styles.softwareColumn} >
                              <label>
                                Software Configuration
                              </label>
                            </th>
                            <th className={Styles.softwareColumn} >
                              <label>
                                Additional Services
                              </label>
                            </th>
                            <th className={Styles.ciColumn}>
                              <label>                            
                                CI/CD Management
                              </label>
                            </th>
                            <th className={Styles.actionColumn}>
                              <label>Action</label>
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {recipes.map((recipe) =>
                            <RecipeList
                              key={recipe.recipeName}
                              recipe={recipe}
                              additionalServices={additionalServices?.filter(service => recipe?.additionalServices?.includes(service.serviceName))}
                              onRefresh={getCodespaceRecipes}
                              onSelectRecipe={(recipe) => { 
                                setSelectedRecipe(recipe);
                                setShowDetailsModal(true);}
                              }
                              onDeleteRecipe={(recipe) => {
                                setSelectedRecipe(recipe);
                                setShowDeleteModal(true);
                              }}
                            />
                          )}
                        </tbody>
                      </table>
                    </div>
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  </div>
                }
              </div>
            </div>
          </div>
        </div>
      </div>
      {showDetailsModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="60vw"
          show={showDetailsModal}
          scrollableContent={true}
          content={<ViewRecipe recipe={selectedRecipe} additionalServices={additionalServices?.filter(service => selectedRecipe?.additionalServices?.includes(service.serviceName))} />}
          onCancel={() => {
            setShowDetailsModal(false);
          }}
        />
      )}
      {showDeleteModal && 
        <Modal
          title="Delete Recipe"
          show={showDeleteModal}
          showAcceptButton={false}
          showCancelButton={false}
          scrollableContent={false}
          hideCloseButton={true}
          content={
            <div>
              <header>
                <button className="modal-close-button" onClick={() => setShowDeleteModal(false)}><i className="icon mbc-icon close thin"></i></button>
              </header>
              <div>
                <p>The Recipe will be deleted permanently. Are you sure you want to delete it?</p>
              </div>
              <div className="btn-set footerRight">
                <button className="btn btn-primary" type="button" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" onClick={handleRecipeDelete}>Confirm</button>
              </div>
            </div>
          } 
        />
      }
    </>
  );
};
export default ManageRecipes;