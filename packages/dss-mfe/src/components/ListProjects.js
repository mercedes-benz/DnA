import cn from 'classnames';
import * as React from 'react';
import { CSVLink } from 'react-csv';

import { history } from '../store';

import Styles from './ListProjects.scss';

import Notification from '../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../common/modules/uilab/js/src/tabs';
import Tooltip from '../common/modules/uilab/js/src/tooltip';

import { Envs } from '../Utility/envs';
import { ProvisionSource } from '../Utility/constants';
import { getDateFromTimestamp, convertTextToLink } from '../Utility/utils';

import ListRowItem from './ListRowItem/ListRowItem';

import { getDataForCSV } from './DataikuCSV';

import DataNotExist from './DataNotExist';

// import component from container app
import Caption from 'dna-container/Caption';
import Pagination from 'dna-container/Pagination';
import Modal from 'dna-container/Modal';
import InfoModal from 'dna-container/InfoModal';
import ProvisionSolution from 'dna-container/ProvisionSolution';

import { dataikuApi } from '../apis/dataiku.api';
import CreateOrEditProject from './CreateOrEditProject/index';

const classNames = cn.bind(Styles);

export default class ListProjects extends React.Component {
  static child = React.createRef();
  static prodSearchInput;
  static trainingSearchInput;
  static dnaSearchInput;
  static csvLink = React.createRef();

  constructor(props) {
    super(props);
    this.child = React.createRef();
    this.state = {
      isUserCanCreateDataiku: false,
      isCreateEditDataikuModal: false,
      isEditDataikuProject: false,
      productionList: {},
      productionListToDisplayAfterSearch: [],
      trainingList: {},
      trainingListToDisplayAfterSearch: [],
      dnaList: {},
      dnaListToDisplayAfterSearch: [],
      tabClassNames: new Map(),
      currentTab: 'production',
      showProvisionModal: false,
      editDataikuProjectModal: false,
      projectTobeProvisioned: null,
      showDetailsModal: false,
      projectData: {
        name: '',
        shortDesc: '',
        projectKey: '',
        tags: [],
        ownerDisplayName: '',
        role: '',
        projectStatus: '',
        contributors: [],
        projectType: '',
        checklists: {
          checklists: [],
        },
        creationTag: {
          versionNumber: 0,
          lastModifiedBy: {
            login: '',
          },
          lastModifiedOn: '',
        },
        solutionId: null,

        ownerLogin: '',
        projectAppType: '',
        tutorialProject: true,
        disableAutomaticTriggers: true,
        commitMode: '',
        isProjectAdmin: true,
        canReadProjectContent: true,
        canWriteProjectContent: true,
        canModerateDashboards: true,
        canWriteDashboards: true,
        canManageDashboardAuthorizations: true,
        canManageExposedElements: true,
        canManageAdditionalDashboardUsers: true,
        canExportDatasetsData: true,
        canReadDashboards: true,
        canRunScenarios: true,
        canExecuteApp: true,
        objectImgHash: {},
        isProjectImg: true,
        defaultImgColor: '',
        imgPattern: 0,
        metrics: {},
        metricsChecks: {},
        sparkPipelinesEnabled: true,
        sqlPipelinesEnabled: true,
        versionTag: {},
        customFields: {},
      },
      showSearchBar: true,
      searchTerm: '',
      totalNumberOfPages: 2,
      currentPageNumber: 0,
      totalNumberOfRecords: 0,
      maxItemsPerPage: 15,
      currentPageOffset: 0,
      paginatedRecords: [],
      currentColumnToSort: 'name',
      nextSortOrder: 'desc',
      currentSortOrder: 'asc',
      openFilterPanel: false,
      hideFilterView: true,
      csvData: [],
      csvHeader: [],
      showInfo: false,
    };
  }

  render() {
    const productionList = this.state.productionList?.data?.length > 0 && this.state.paginatedRecords.length > 0 ? this.state.paginatedRecords : [];
    const trainingList = this.state.trainingList?.data?.length > 0 && this.state.paginatedRecords.length > 0 ? this.state.paginatedRecords : [];
    const projectsData = trainingList.map((project) => {
      return (
        <ListRowItem
          key={project.id}
          project={project}
          projectId={project.projectKey}
          editDataikuProjectDetail={this.editDataikuProjectDetails}
          deleteDataikuProject={this.deleteDataikuProject}
          isProduction={false}
          isDnaProject={false}
          openDetailsModal={this.openDetailsModal}
          user={this.props.user}
          hostHistory={this.props.hostHistory}
        />
      );
    });

    const productionsData = productionList.map((project) => {
      return (
        <ListRowItem
          key={project.id}
          project={project}
          projectId={project.projectKey}
          isProduction={true}
          isDnaProject={false}
          editDataikuProjectDetail={this.editDataikuProjectDetails}
          deleteDataikuProject={this.deleteDataikuProject}
          openProvisionModal={this.openProvisionModal}
          openDetailsModal={this.openDetailsModal}
          user={this.props.user}
          hostHistory={this.props.hostHistory}
        />
      );
    });

    const contentForDetails = this.state.showDetailsModal && (
      <React.Fragment>
        <h4>{this.state.projectData.name}</h4>
        <p className={Styles.projectDescription}>{this.state.projectData.shortDesc}</p>
        <div className={classNames(Styles.modalFlexLayout, Styles.threeColumn, 'mbc-scroll')}>
          <div>
            <div>
              <label id="createdOn" className="input-label summary">
                Created On
              </label>
              <br />
              {this.state.projectData?.creationTag?.lastModifiedOn
                ? getDateFromTimestamp(this.state.projectData.creationTag.lastModifiedOn)
                : '-NA-'}
            </div>
            <br />
            <br />
            <div>
              <label id="projectStatus" className="input-label summary">
                Status
              </label>
              <br />
              {this.state.projectData?.status ? this.state.projectData.status : '-NA-'}
            </div>
            <br />
            <br />
            <div>
              <label id="tags" className="input-label summary">
                Tags
              </label>
              <br />
              {this.state.projectData?.tags?.length > 0 ? this.state.projectData.tags.join(', ') : '-NA-'}
            </div>
          </div>
          <div>
            {this.state.currentTab === 'production' ? (
              <React.Fragment>
                <div>
                  <label id="cotributors" className="input-label summary">
                    Collaborators
                  </label>
                  <br />
                  {this.state.projectData?.collaborators?.length > 0 ?
                    this.state.projectData?.collaborators.map((user) =>
                      <div key={user?.userId}>
                        <div>{user?.givenName} {user?.surName} (<span className={classNames(Styles.permission)}>{user?.permission}</span>)</div>
                      </div>)
                    : '-NA-'}
                </div>
                <br />
                <br />
              </React.Fragment>
            ) : (
              ''
            )}
            <div>
              <label id="role" className="input-label summary">
                Role
              </label>
              <br />
              {this.state.projectData?.role ? this.state.projectData.role : '-NA-'}
            </div>
          </div>
          <div>
            {this.state.projectData?.checklists?.checklists.map((checklist, index) => {
              let totalDone = 0;
              const totalChecklistCount = checklist.items.length;
              totalDone = checklist.items.filter((item) => item.done).length;
              return (
                <div key={index} className={classNames(Styles.checklistWrapper, 'mbc-scroll sub')}>
                  <label className="input-label summary">{checklist.title}</label>
                  <p className={Styles.doneChecklistCount}>
                    ({totalDone}/{totalChecklistCount} done)
                  </p>
                  <div>
                    <ul className="list-item-group divider">
                      {checklist.items.map((item, itemIndex) => {
                        return (
                          <li className="list-item" key={itemIndex}>
                            {item.done ? (
                              <i
                                tooltip-data={'Done'}
                                className={'icon mbc-icon check circle ' + Styles.checkWithCircle}
                              />
                            ) : (
                              <i tooltip-data={'Not Done'} className={'icon mbc-icon check circle'} />
                            )}
                            {/* <label className="item-text">{item.text}</label> */}
                            <label
                              className="item-text"
                              dangerouslySetInnerHTML={{ __html: convertTextToLink(item.text, this.state.currentTab) }}
                            ></label>
                          </li>
                        );
                      })}
                    </ul>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </React.Fragment>
    );
    const liveProjectEmpty = (
      <React.Fragment>
        <div>
          There is no Live project available.&nbsp;
          {!this.state.isUserCanCreateDataiku &&
            <span>Currently, you don`t own any licences.&nbsp;
              <span className={Styles.clickHereText}
                onClick={() => onFerretInfoModalShow()}>
                Click here
              </span>&nbsp;for licence acquisition procedure.</span>}
        </div>
      </React.Fragment>
    );
    const liveProjectSearchEmpty = (
      <React.Fragment>
        Searched result could not be found in Live projects.
      </React.Fragment>
    );
    const trainingProjectEmpty = (
      <React.Fragment>
        There is no Training project available, in order to create a new Dataiku Training project you will have to raise
        a Ferret request &nbsp;
        <a href={Envs.DATAIKU_FERRET_URL} target="_blank" rel="noreferrer">
          here.
        </a>
      </React.Fragment>
    );
    const trainingProjectSearchEmpty = (
      <React.Fragment>
        Searched result could not be found in Training projects.
      </React.Fragment>
    );
    const canShowSearch = this.state.showSearchBar;
    const { openFilterPanel, hideFilterView } = this.state;
    const csvFileName =
      this.state.currentTab === 'production' ? 'Dataiku_Projects_Live.csv' : 'Dataiku_Projects_Training.csv';

    const onFerretInfoModalShow = () => {
      this.setState({ showInfo: true });
    };
    const onFerretInfoModalCancel = () => {
      this.setState({ showInfo: false });
    };
    const contentForInfo =
      this.state.currentTab === 'production' ? (
        <div className={Styles.infoPopup}>
          <div className={Styles.modalContent}>
            <div dangerouslySetInnerHTML={{ __html: Envs.DATAIKU_LICENSE_CREATION_CONTENT }}></div>
          </div>
        </div>
      ) : (
        <div className={Styles.infoPopup}>
          <div className={Styles.modalContent}>
            <p>
              In order to create a new Dataiku Training project you will have to raise a Ferret request &nbsp;
              <a href={Envs.DATAIKU_FERRET_URL} target="_blank" rel="noreferrer">
                here.
              </a>
            </p>
          </div>
        </div>
      );

    return (
      <React.Fragment>
        <div className={Styles.mainPanel}>
          <div className={Styles.wrapper}>
            <Caption title="Dataiku Projects" />
          </div>
          <div className={Styles.content}>
            <div className={classNames(Styles.listHeader)}>
              <React.Fragment>
                <div className={classNames(Styles.listHeaderContent)}>
                  <React.Fragment>
                    {this.state.isUserCanCreateDataiku && this.state.currentTab === 'production' &&
                      <button className={'btn add-dataiku-container btn-primary'} type="button"
                        disabled={!this.state.isUserCanCreateDataiku}
                        onClick={(() => {
                          this.setState({
                            isCreateEditDataikuModal: true,
                          });
                        })}>
                        <i className="icon mbc-icon plus" />
                        <span>Create New Dataiku project</span>
                      </button>}
                  </React.Fragment>
                </div>
              </React.Fragment>
            </div>
            <div id="dataiku-project-tabs" className="tabs-panel">
              <div className="tabs-wrapper">
                <nav>
                  <ul className="tabs">
                    <li className={this.state.currentTab === 'production' ? 'tab active' : 'tab'}>
                      <a href="#tab-project" id="production" onClick={this.setCurrentTab}>
                        Live Projects
                      </a>
                    </li>
                    <li className={this.state.currentTab === 'training' ? 'tab active ' : 'tab '}>
                      <a href="#tab-training" id="training" onClick={this.setCurrentTab}>
                        Training Projects
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="training2" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="training3" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="training4" className={'hidden'}>
                        `
                      </a>
                    </li>
                    <li className={'tab disabled'}>
                      <a id="training5" className={'hidden'}>
                        `
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
              <div className="tabs-content-wrapper">
                <div id="tab-project" className={Styles.tabContentStyle + ' tab-content'}>
                  <React.Fragment>
                    {!this.state.isUserCanCreateDataiku && <i
                      tooltip-data="Info"
                      className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                      onClick={onFerretInfoModalShow}
                    />}
                    {this.state.productionList.data ? (
                      this.state.productionList.data.length > 0 ? (
                        <div className={Styles.searchPanel}>
                          <input
                            type="text"
                            className={classNames(Styles.searchInputField, canShowSearch ? '' : Styles.hide)}
                            ref={(searchInput) => {
                              this.prodSearchInput = searchInput;
                            }}
                            placeholder="Search Project"
                            onChange={this.onSearchInputChange}
                            maxLength={200}
                            value={this.state.searchTerm}
                          />
                          {/* <button>
                        <i className="icon mbc-icon close thin" />
                      </button> */}
                          <button className="default-cursor" onClick={this.onProdSearchIconButtonClick}>
                            <i className={classNames('icon mbc-icon search', canShowSearch ? Styles.active : '')} />
                          </button>
                        </div>
                      ) : (
                        ''
                      )
                    ) : (
                      ''
                    )}

                    {productionsData.length > 0 ? (
                      <table className={'ul-table solutions ' + Styles.table}>
                        <thead>
                          <tr className="header-row">
                            <th
                              onClick={() => {
                                this.sortByColumn('name', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'name' ? this.state.currentSortOrder : '')
                                }
                              >
                                <i className="icon sort" />
                                Name
                              </label>
                            </th>
                            <th
                              onClick={() => {
                                this.sortByColumn('shortDesc', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'shortDesc' ? this.state.currentSortOrder : '')
                                }
                              >
                                <i className="icon sort" />
                                Description
                              </label>
                            </th>
                            <th
                              onClick={() => {
                                this.sortByColumn('role', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'role' ? this.state.currentSortOrder : '')
                                }
                              >
                                <i className="icon sort" />
                                User Profile
                              </label>
                            </th>
                            <th
                              onClick={() => {
                                this.sortByColumn('cloudProfile', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'cloudProfile'
                                    ? this.state.currentSortOrder
                                    : '')
                                }
                              >
                                <i className="icon sort" />
                                Instance
                              </label>
                            </th>
                            <th className="actionColumn">&nbsp;</th>
                            <th>
                              <label className="sortable-column-header">Action</label>
                            </th>
                          </tr>
                        </thead>
                        <tbody>{productionsData}</tbody>
                      </table>
                    ) : this.state.productionList.data ? (
                      this.state.productionList.data.length > 0 ? (
                        <div className={Styles.datasNotExisted}>
                          <DataNotExist message={liveProjectSearchEmpty} height={'150px'} />
                        </div>
                      ) : (
                        <div className={Styles.datasNotExisted}>
                          <DataNotExist message={liveProjectEmpty} height={'150px'} />
                        </div>
                      )
                    ) : (
                      <div className={Styles.datasNotExisted}>
                        <DataNotExist message={liveProjectEmpty} height={'150px'} />
                      </div>
                    )}
                    {this.state.totalNumberOfRecords ? (
                      <Pagination
                        totalPages={this.state.totalNumberOfPages}
                        pageNumber={this.state.currentPageNumber}
                        onPreviousClick={this.onPaginationPreviousClick}
                        onNextClick={this.onPaginationNextClick}
                        onViewByNumbers={this.onViewByPageNum}
                        displayByPage={true}
                      />
                    ) : (
                      ''
                    )}
                  </React.Fragment>
                </div>
                <div id="tab-training" className={Styles.tabContentStyle + ' tab-content'}>
                  <React.Fragment>
                    <i
                      tooltip-data="Info"
                      className={Styles.iconsmd + ' icon mbc-icon info iconsmd'}
                      onClick={onFerretInfoModalShow}
                    />
                    {this.state.trainingList.data ? (
                      this.state.trainingList.data.length > 0 ? (
                        <div className={Styles.searchPanel}>
                          <input
                            type="text"
                            className={classNames(Styles.searchInputField, canShowSearch ? '' : Styles.hide)}
                            ref={(searchInput) => {
                              this.trainingSearchInput = searchInput;
                            }}
                            placeholder="Search Project"
                            onChange={this.onSearchInputChange}
                            maxLength={200}
                            value={this.state.searchTerm}
                          />
                          <button className="default-cursor" onClick={this.onTrainingSearchIconButtonClick}>
                            <i className={classNames('icon mbc-icon search', canShowSearch ? Styles.active : '')} />
                          </button>
                        </div>
                      ) : (
                        ''
                      )
                    ) : (
                      ''
                    )}
                    {projectsData.length > 0 ? (
                      <table className={'ul-table solutions ' + Styles.table}>
                        <thead>
                          <tr className="header-row">
                            <th
                              onClick={() => {
                                this.sortByColumn('name', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'name' ? this.state.currentSortOrder : 'asc')
                                }
                              >
                                <i className="icon sort" />
                                Name
                              </label>
                            </th>
                            <th
                              onClick={() => {
                                this.sortByColumn('shortDesc', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'shortDesc' ? this.state.currentSortOrder : '')
                                }
                              >
                                <i className="icon sort" />
                                Description
                              </label>
                            </th>
                            <th
                              onClick={() => {
                                this.sortByColumn('lastModifiedOn', this.state.nextSortOrder);
                              }}
                            >
                              <label
                                className={
                                  'sortable-column-header ' +
                                  (this.state.currentColumnToSort == 'lastModifiedOn'
                                    ? this.state.currentSortOrder
                                    : '')
                                }
                              >
                                <i className="icon sort" />
                                Last Used
                              </label>
                            </th>
                            <th className="actionColumn">&nbsp; </th>
                          </tr>
                        </thead>
                        <tbody>{projectsData}</tbody>
                      </table>
                    ) : this.state.trainingList.data ? (
                      this.state.trainingList.data.length > 0 ? (
                        <div className={Styles.datasNotExisted}>
                          <DataNotExist message={trainingProjectSearchEmpty} height={'150px'} />
                        </div>
                      ) : (
                        <div className={Styles.datasNotExisted}>
                          <DataNotExist message={trainingProjectEmpty} height={'150px'} />
                        </div>
                      )
                    ) : (
                      <div className={Styles.datasNotExisted}>
                        <DataNotExist message={trainingProjectEmpty} height={'150px'} />
                      </div>
                    )}
                    {this.state.totalNumberOfRecords ? (
                      <Pagination
                        totalPages={this.state.totalNumberOfPages}
                        pageNumber={this.state.currentPageNumber}
                        onPreviousClick={this.onPaginationPreviousClick}
                        onNextClick={this.onPaginationNextClick}
                        onViewByNumbers={this.onViewByPageNum}
                        displayByPage={true}
                      />
                    ) : (
                      ''
                    )}
                  </React.Fragment>
                </div>
                <br />
              </div>
            </div>
          </div>
        </div>
        <div className={classNames(Styles.rightPanel)}>
          <div className={Styles.triggerWrapper}>
            <span
              className={classNames(
                Styles.iconTrigger,
                openFilterPanel ? Styles.active : '',
                hideFilterView ? 'hide' : '',
              )}
              onClick={this.onFilterIconClick}
            ></span>
            <span className={classNames(Styles.iconTrigger)} onClick={this.triggerDownloadCSVData}>
              <i tooltip-data="Export to CSV" className="icon download" />
            </span>
          </div>
        </div>
        <CSVLink
          data={this.state.csvData}
          headers={this.state.csvHeader}
          ref={(r) => (this.csvLink = r)}
          filename={csvFileName}
          target="_blank"
        />
        {this.state.isCreateEditDataikuModal && (
          <Modal
            title={this.state.editDataikuProjectModal ? 'Edit Dataiku Project' : 'Create a new Dataiku Project'}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'80%'}
            buttonAlignment="right"
            show={this.state.isCreateEditDataikuModal}
            content={
              <CreateOrEditProject
                editDataikuProjectDetail={this.state.projectData}
                isEdit={this.state.editDataikuProjectModal}
                isUserCanCreateDataiku={this.state.isUserCanCreateDataiku}
                user={this.props.user}
                callDnaDataList={this.callDnaDataListApi} />
            }
            scrollableContent={true}
            onCancel={this.editCreateEditDataikuModal}
          />
        )}
        {this.state.showProvisionModal && (
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'80%'}
            buttonAlignment="right"
            show={this.state.showProvisionModal}
            content={
              <ProvisionSolution
                ref={this.child}
                projectToBeProvisioned={this.state.projectData.projectKey}
                provisionedSolutionId={this.provisionedSolutionId}
                provisionFrom={ProvisionSource.DATAIKU}
              />
            }
            scrollableContent={false}
            onCancel={this.onProvisionModalCancel}
          />
        )}

        {this.state.showDetailsModal && (
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'80%'}
            buttonAlignment="right"
            show={this.state.showDetailsModal}
            content={contentForDetails}
            scrollableContent={false}
            onCancel={this.onInfoModalCancel}
          />
        )}

        {this.state.showInfo && (
          <InfoModal
            title={this.state.currentTab === 'production' ? 'Details to get license' : 'Project Creation Details'}
            modalWidth={'35vw'}
            show={this.state.showInfo}
            content={contentForInfo}
            onCancel={onFerretInfoModalCancel}
          />
        )}
      </React.Fragment>
    );
  }

  componentDidMount = () => {
    Tabs.defaultSetup();
    Tooltip.defaultSetup();
    this.validateUser();
    this.onViewByPageNum(15);
  };

  // TO check if user is able to create a dataiku project or not.
  validateUser = () => {
    ProgressIndicator.show();
    dataikuApi
      .validateUserPrivilage(this.props?.user?.id)
      .then((response) => {
        if (response?.data && response?.data?.canCreate) {
          this.setState({ isUserCanCreateDataiku: response.data.canCreate });
        }
      })
      .catch((err) => {
        err;
        if (err?.response?.data?.response?.errors?.length > 0) {
          err?.response?.data?.response?.errors.forEach((err) => {
            this.showErrorNotification(err?.message || 'Something went wrong.');
          });
        } else {
          this.showErrorNotification('Something went wrong.');
        }
      }).finally(() => {
        // TO get all Live Projects.
        this.getLiveProjects();
      });
  }

  provisionedSolutionId = () => {
    this.setState({
      showProvisionModal: false,
    });
    this.getLiveProjects();
    /*******************************************************************
     *******************************************************************
            Following lines will be deleted after resolving of issue
     *******************************************************************
    ********************************************************************/
    // const targetVal = document.getElementById('provision'+this.state.projectData.projectKey);
    // targetVal.removeChild(targetVal.childNodes[0]);
    // targetVal.appendChild(this.createSummaryLink(solutionId));
  };

  openProvisionModal = (project) => {
    this.setState({ showProvisionModal: true, projectData: project }, () => {
      this.child.current.callDAtaikuProjectDetails(project.projectKey, project.cloudProfile);
    });
  };

  callDnaDataListApi = () => {
    this.getLiveProjects();
    this.editCreateEditDataikuModal()
  };

  deleteDataikuProject = (projectKey, cloudProfile) => {
    ProgressIndicator.show();
    dataikuApi
      .deleteDnaProjectByProjectByName(projectKey, cloudProfile)
      .then((response) => {
        const data = response.data;
        if (data.success === "SUCCESS") {
          ProgressIndicator.hide();
          this.getLiveProjects();
          Notification.show('Dataiku project deleted successfully');
        } else {
          ProgressIndicator.hide();
          this.showErrorNotification('Failed to delete Dataiku project.');
        }
      })
      .catch((err) => {
        err;
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            this.showErrorNotification(err?.message || 'Something went wrong.');
          });
        } else {
          this.showErrorNotification('Something went wrong.');
        }
      });
  }

  editDataikuProjectDetails = (project) => {
    this.setState({ editDataikuProjectModal: true, projectData: project, isCreateEditDataikuModal: true });
  };

  createSummaryLink = (provisionedSolutionId) => {
    const span = document.createElement('i');
    const goToSolution = (solutionId) => {
      history.push('/summary/' + solutionId);
    };
    span.onclick = () => {
      goToSolution(provisionedSolutionId);
    };
    span.className = 'icon mbc-icon solutions solutionLink';
    span['tooltip-data'] = 'Go to Solution';
    return span;
  };

  onProvisionModalCancel = () => {
    this.setState({ showProvisionModal: false });
  };

  editCreateEditDataikuModal = () => {
    this.setState({ editDataikuProjectModal: false, isCreateEditDataikuModal: false });
  };

  openDetailsModal = (project) => {
    // ApiClient.getDataikuProjectDetails(project.projectKey, isProduction).then((res) => {
    //   this.setState({showDetailsModal: true, projectData: project});
    // });
    this.setState({ showDetailsModal: true, projectData: project });
  };

  onInfoModalCancel = () => {
    this.setState({ showDetailsModal: false });
  };

  getLiveProjects = () => {
    ProgressIndicator.show();
    dataikuApi
      .getDataikuProjectsList(true)
      .then((response) => {
        const data = response.data;
        this.setState(
          {
            productionList: data,
            totalNumberOfRecords: data.totalCount,
            totalNumberOfPages: Math.ceil(data.totalCount / this.state.maxItemsPerPage),
            currentPageNumber: 1,
            currentPageOffset: 0,
            productionListToDisplayAfterSearch: [],
            searchTerm: '',
          },
          () => {
            this.getPaginatedProjects();
          },
        );
        ProgressIndicator.hide();
      })
      .catch((err) => {
        err;
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            this.showErrorNotification(err?.message || 'Something went wrong.');
          });
        } else {
          this.showErrorNotification('Something went wrong.');
        }
      });

    /****************************************
     * Following line is temporary
     * *************************************/
    //  this.setState({
    //    productionList: {data: this.temporaryData},
    //    productionListToDisplayAfterSearch: this.temporaryData,
    //    searchTerm: ''});
  };

  getTrainingProjects = () => {
    ProgressIndicator.show();
    dataikuApi
      .getDataikuProjectsList(false)
      .then((response) => {
        const data = response.data;
        this.setState(
          {
            trainingList: data,
            totalNumberOfRecords: data.totalCount,
            currentPageNumber: 1,
            currentPageOffset: 0,
            totalNumberOfPages: Math.ceil(data.totalCount / this.state.maxItemsPerPage),
            trainingListToDisplayAfterSearch: [],
            searchTerm: '',
          },
          () => {
            this.getPaginatedProjects();
          },
        );
        ProgressIndicator.hide();
      })
      .catch((err) => {
        err;
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            this.showErrorNotification(err?.message || 'Something went wrong.');
          });
        } else {
          this.showErrorNotification('Something went wrong.');
        }
      });

    /****************************************
     * Following line is temporary
     * *************************************/
    // this.setState({trainingList: {data: this.temporaryData},
    //   trainingListToDisplayAfterSearch: this.temporaryData,
    //   searchTerm: ''});
  };

  getDnaProjects = () => {
    ProgressIndicator.show();
    dataikuApi
      .getDnaProjectList()
      .then((response) => {
        const data = response.data;
        this.setState(
          {
            dnaList: data,
            totalNumberOfRecords: data.totalCount,
            currentPageNumber: 1,
            currentPageOffset: 0,
            totalNumberOfPages: Math.ceil(data.totalCount / this.state.maxItemsPerPage),
            dnaListToDisplayAfterSearch: [],
            searchTerm: '',
          },
          () => {
            this.getPaginatedProjects();
          },
        );
        ProgressIndicator.hide();
      })
      .catch((err) => {
        err;
        this.getPaginatedProjects();
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            this.showErrorNotification(err?.message || 'Something went wrong.');
          });
        } else {
          this.showErrorNotification('Something went wrong.');
        }
      });

    /****************************************
     * Following line is temporary
     * *************************************/
    // this.setState({dnaList: {data: this.temporaryData},
    //   dnaListToDisplayAfterSearch: this.temporaryData,
    //   searchTerm: ''});
  };

  showErrorNotification = (message) => {
    ProgressIndicator.hide();
    Notification.show(message, 'alert');
  };

  setCurrentTab = (event) => {
    const target = event.target;
    this.setState({
      currentTab: target.id,
      currentColumnToSort: 'name',
      nextSortOrder: 'desc',
      currentSortOrder: 'asc',
    });
    if (target.id === 'training') {
      this.setState({
        productionList: {},
        paginatedRecords: []
      }, ()=> {
        this.getTrainingProjects();
      });
    } else if (target.id === 'production') {
      this.setState({
        trainingList: {},
        paginatedRecords: [],
      }, ()=> {
        this.getLiveProjects();
      });
    } else if (target.id === 'createOrEdit') {
      this.getDnaProjects();
    }
  };

  goback = () => {
    history.goBack();
  };

  ResetTabs = () => {
    const tabActiveIndicator = document.querySelector('.active-indicator');
    const firstTabContent = document.querySelector('.tab-content');
    const activeTabContent = document.querySelector('.tab-content.active');

    if (tabActiveIndicator && firstTabContent && activeTabContent) {
      tabActiveIndicator.style.left = '0px';
      if (!firstTabContent.classList.contains('active')) {
        firstTabContent.classList.add('active');
        activeTabContent.classList.remove('active');
      }
    }
  };

  onSearchInputChange = (event) => {
    this.setState(
      {
        searchTerm: event.currentTarget.value,
      },
      () => {
        const searchTerm = this.state.searchTerm;

        if (searchTerm && searchTerm.length > 1) {
          this.getSolutionsInfoBySearchTerm();
        } else {
          if (searchTerm.length == 0) {
            if (this.state.currentTab === 'production') {
              const tempList = this.state.productionList.data.length > 0 ? this.state.productionList.data : [];
              const tillRecord = 0 + this.state.maxItemsPerPage;
              const records = tempList.slice(0, tillRecord);
              this.setState({
                totalNumberOfRecords: tempList.length,
                totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
                paginatedRecords: records,
                productionListToDisplayAfterSearch: [],
                currentPageNumber: 1,
                currentPageOffset: 0,
              });
            } else if (this.state.currentTab === 'createOrEdit') {
              const tempList = this.state.dnaList.data.length > 0 ? this.state.dnaList.data : [];
              const tillRecord = 0 + this.state.maxItemsPerPage;
              const records = tempList.slice(0, tillRecord);
              this.setState({
                totalNumberOfRecords: tempList.length,
                totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
                paginatedRecords: records,
                dnaListToDisplayAfterSearch: [],
                currentPageNumber: 1,
                currentPageOffset: 0,
              });
            } else {
              const tempList = this.state.trainingList.data.length > 0 ? this.state.trainingList.data : [];
              const tillRecord = 0 + this.state.maxItemsPerPage;
              const records = tempList.slice(0, tillRecord);
              this.setState({
                totalNumberOfRecords: tempList.length,
                totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
                paginatedRecords: records,
                trainingListToDisplayAfterSearch: [],
                currentPageNumber: 1,
                currentPageOffset: 0,
              });
            }
          }
        }
      },
    );
  };

  getSolutionsInfoBySearchTerm = () => {
    if (this.state.currentTab === 'production') {
      const tempList =
        this.state.productionList.data.length > 0
          ? this.state.productionList.data.filter((item) => {
            if (item.name.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
              return item;
            }
            if (item.shortDesc) {
              if (item.shortDesc.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
                return item;
              }
            }
          })
          : [];
      const tillRecord = 0 + this.state.maxItemsPerPage;
      const records = tempList.slice(0, tillRecord);
      this.setState({
        totalNumberOfRecords: tempList.length,
        totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
        paginatedRecords: records,
        productionListToDisplayAfterSearch: tempList,
        currentPageNumber: 1,
        currentPageOffset: 0,
      });
    } else if (this.state.currentTab === 'createOrEdit') {
      const tempList =
        this.state.dnaList.data.length > 0
          ? this.state.dnaList.data.filter((item) => {
            if (item.projectName.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
              return item;
            }
            if (item.description) {
              if (item.description.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
                return item;
              }
            }
          })
          : [];
      const tillRecord = 0 + this.state.maxItemsPerPage;
      const records = tempList.slice(0, tillRecord);
      this.setState({
        totalNumberOfRecords: tempList.length,
        totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
        paginatedRecords: records,
        dnaListToDisplayAfterSearch: tempList,
        currentPageNumber: 1,
        currentPageOffset: 0,
      });
    } else {
      const tempList =
        this.state.trainingList.data.length > 0
          ? this.state.trainingList.data.filter((item) => {
            if (item.name.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
              return item;
            }
            if (item.shortDesc) {
              if (item.shortDesc.toLowerCase().includes(this.state.searchTerm.toLowerCase())) {
                return item;
              }
            }
          })
          : [];
      const tillRecord = 0 + this.state.maxItemsPerPage;
      const records = tempList.slice(0, tillRecord);
      this.setState({
        totalNumberOfRecords: tempList.length,
        totalNumberOfPages: Math.ceil(tempList.length / this.state.maxItemsPerPage),
        paginatedRecords: records,
        trainingListToDisplayAfterSearch: tempList,
        currentPageNumber: 1,
        currentPageOffset: 0,
      });
    }
  };

  onProdSearchIconButtonClick = () => {
    this.prodSearchInput.focus();
  };

  onTrainingSearchIconButtonClick = () => {
    this.trainingSearchInput.focus();
  };

  onDnaSearchIconButtonClick = () => {
    this.dnaSearchInput.focus();
  };

  onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getPaginatedProjects();
    });
  };

  onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getPaginatedProjects();
    });
  };

  onViewByPageNum = (pageNum) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      if (this.state.currentTab === 'production') {
        this.setState({
          totalNumberOfPages: Math.ceil(
            (this.state.productionListToDisplayAfterSearch.length > 0
              ? this.state.productionListToDisplayAfterSearch.length
              : this.state.productionList.totalCount) / this.state.maxItemsPerPage,
          ),
        });
      } else if (this.state.currentTab === 'createOrEdit') {
        this.setState({
          totalNumberOfPages: Math.ceil(
            (this.state.dnaListToDisplayAfterSearch.length > 0
              ? this.state.dnaListToDisplayAfterSearch.length
              : this.state.dnaList.totalCount) / this.state.maxItemsPerPage,
          ),
        });
      } else {
        this.setState({
          totalNumberOfPages: Math.ceil(
            (this.state.trainingListToDisplayAfterSearch.length > 0
              ? this.state.trainingListToDisplayAfterSearch.length
              : this.state.trainingList.totalCount) / this.state.maxItemsPerPage,
          ),
        });
      }
      this.getPaginatedProjects();
    });
  };

  getPaginatedProjects = () => {
    const tillRecord = this.state.currentPageOffset + this.state.maxItemsPerPage;
    let sortedProductionList = [];
    let sortedTrainingList = [];
    let sortedDnaList = [];
    const productionList =
      this.state.productionListToDisplayAfterSearch.length > 0
        ? this.state.productionListToDisplayAfterSearch
        : this.state.productionList.data
          ? this.state.productionList.data
          : [];
    const trainingList =
      this.state.trainingListToDisplayAfterSearch.length > 0
        ? this.state.trainingListToDisplayAfterSearch
        : this.state.trainingList.data
          ? this.state.trainingList.data
          : [];
    if (this.state.currentTab === 'production' && productionList.length) {
      sortedProductionList = productionList.sort((a, b) => {
        const nameA = a.name.toUpperCase(); // ignore upper and lowercase
        const nameB = b.name.toUpperCase(); // ignore upper and lowercase
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        // names must be equal
        return 0;
      });
    } else if (trainingList.length) {
      sortedTrainingList = trainingList.sort((a, b) => {
        const nameA = a.name.toUpperCase(); // ignore upper and lowercase
        const nameB = b.name.toUpperCase(); // ignore upper and lowercase
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }

        // names must be equal
        return 0;
      });
    }
    const records =
      this.state.currentTab === 'production'
        ? sortedProductionList.slice(this.state.currentPageOffset, tillRecord)
        : this.state.currentTab === 'createOrEdit'
          ? sortedDnaList.slice(this.state.currentPageOffset, tillRecord)
          : sortedTrainingList.slice(this.state.currentPageOffset, tillRecord);
    this.setState(
      {
        paginatedRecords: records,
      },
      () => {
        Tooltip.defaultSetup();
      },
    );
  };

  sortByColumn = (columnName, sortOrder) => {
    if (columnName !== this.state.currentColumnToSort) {
      sortOrder = 'asc';
    }
    const arrayToSort =
      this.state.currentTab === 'production' ? this.state.productionList.data :
        this.state.currentTab === 'createOrEdit' ? this.state.dnaList.data : this.state.trainingList.data;
    const sortedArray = arrayToSort.sort((a, b) => {
      let nameA;
      let nameB;
      if (columnName == 'lastModifiedOn') {
        nameA = a['versionTag'][columnName] ? a['versionTag'][columnName] : ''; // ignore upper and lowercase
        nameB = b['versionTag'][columnName] ? b['versionTag'][columnName] : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder == 'asc' ? 1 : -1;
        }
        if (nameA > nameB) {
          return sortOrder == 'asc' ? -1 : 1;
        }
      } else {
        nameA = a[columnName] ? a[columnName].toUpperCase() : ''; // ignore upper and lowercase
        nameB = b[columnName] ? b[columnName].toUpperCase() : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder == 'asc' ? -1 : 1;
        }
        if (nameA > nameB) {
          return sortOrder == 'asc' ? 1 : -1;
        }
      }
      // names must be equal
      return 0;
    });

    const tillRecord = 0 + this.state.maxItemsPerPage;
    const records = sortedArray.slice(0, tillRecord);
    if (this.state.currentTab === 'production') {
      this.setState({ productionListToDisplayAfterSearch: sortedArray });
    } else if (this.state.currentTab === 'createOrEdit') {
      this.setState({ dnaListToDisplayAfterSearch: sortedArray });
    } else {
      this.setState({ trainingListToDisplayAfterSearch: sortedArray });
    }
    this.setState({
      totalNumberOfRecords: sortedArray.length,
      totalNumberOfPages: Math.ceil(sortedArray.length / this.state.maxItemsPerPage),
      paginatedRecords: records,
      currentPageNumber: 1,
      currentPageOffset: 0,
      currentColumnToSort: columnName,
      nextSortOrder: sortOrder === 'asc' ? 'desc' : 'asc',
      currentSortOrder: sortOrder,
    });
  };

  sortResult = (columnName, sortOrder) => {
    const arrayToSort =
      this.state.currentTab === 'production' ? this.state.productionList.data :
        this.state.currentTab === 'createOrEdit' ? this.state.dnaList.data : this.state.trainingList.data;
    const sortedArray = arrayToSort.sort((a, b) => {
      let nameA;
      let nameB;
      if (columnName == 'lastModifiedOn') {
        nameA = a['versionTag'][columnName] ? a['versionTag'][columnName] : ''; // ignore upper and lowercase
        nameB = b['versionTag'][columnName] ? b['versionTag'][columnName] : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder == 'asc' ? 1 : -1;
        }
        if (nameA > nameB) {
          return sortOrder == 'asc' ? -1 : 1;
        }
      } else {
        nameA = a[columnName] ? a[columnName].toUpperCase() : ''; // ignore upper and lowercase
        nameB = b[columnName] ? b[columnName].toUpperCase() : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder == 'asc' ? -1 : 1;
        }
        if (nameA > nameB) {
          return sortOrder == 'asc' ? 1 : -1;
        }
      }
    });
    return sortedArray;
  };

  onFilterIconClick = () => {
    this.setState({ openFilterPanel: !this.state.openFilterPanel });
  };

  triggerDownloadCSVData = () => {
    this.setState({ csvData: [], csvHeader: [] });
    // ProgressIndicator.show();
    const records =
      this.state.currentTab === 'production'
        ? this.state.productionListToDisplayAfterSearch
          ? this.state.productionListToDisplayAfterSearch.length > 0
            ? this.state.productionListToDisplayAfterSearch
            : this.state.productionList.data
              ? this.sortResult('name', 'asc')
              : []
          : []
        : this.state.trainingListToDisplayAfterSearch
          ? this.state.trainingListToDisplayAfterSearch.length > 0
            ? this.state.trainingListToDisplayAfterSearch
            : this.state.trainingList.data
              ? this.sortResult('name', 'asc')
              : []
          : [];

    if (records.length > 0) {
      getDataForCSV(records, (csvData, csvHeader) => {
        this.setState(
          {
            csvData,
            csvHeader,
          },
          () => {
            if (this.csvLink) {
              setTimeout(() => {
                this.csvLink.link.click();
              }, 0);
            }
            // ProgressIndicator.hide();
          },
        );
      });
    } else {
      ProgressIndicator.show();
      this.showErrorNotification('Records not available');
      // ProgressIndicator.hide();
    }
  };
}
