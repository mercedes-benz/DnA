//  import cn from 'classnames';
import * as React from 'react';

// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';

import { IRole, ISubDivision, IUserInfo, IUserRequestVO } from 'globals/types';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';

import Pagination from 'components/mbc/pagination/Pagination';
import UserInfoRowItem from './userinforowitem/UserInfoRowItem';
import Styles from './UserRoleManagement.scss';

import { ApiClient } from '../../../../services/ApiClient';
import { ISortField } from 'components/mbc/allSolutions/AllSolutions';

import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { SESSION_STORAGE_KEYS, USER_ROLE } from 'globals/constants';
import Modal from 'components/formElements/modal/Modal';
import classNames from 'classnames';
import { debounce } from 'lodash';
// const classNames = cn.bind(Styles);

export interface IUserRoleManagementState {
  totalNumberOfPages: number;
  roles: IRole[];
  updatedRole: IRole;
  moduleRoles: IRole[];
  currentUserRole: string;
  sortBy: ISortField;
  users: IUserInfo[];
  searchText: string;
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  currentPageOffset: number;
  showEditUsersModal: boolean;
  currentUserToEdit: IUserInfo;
  currentRoleCategory: IRole;
  isLoading: boolean;
  divisionList: any[];
  selectedDivisions: any[];
  divisionError: string;
}

export class UserRoleManagement extends React.Component<any, IUserRoleManagementState> {
  constructor(props: any) {
    super(props);
    this.state = {
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
      totalNumberOfRecords: 0,
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      currentPageOffset: 0,
      users: [],
      roles: [],
      moduleRoles: [],
      searchText: null,
      currentUserToEdit: null,
      currentUserRole: '',
      currentRoleCategory: null,
      updatedRole: null,
      sortBy: {
        name: 'firstName',
        currentSortType: 'asc',
        nextSortType: 'desc',
      },
      showEditUsersModal: false,
      isLoading: false,
      divisionList: [],
      selectedDivisions: [],
      divisionError: null,
    };
  }
  public getUsers() {
    let offset = this.state.currentPageOffset ? this.state.currentPageOffset : 0;
    let limit = this.state.maxItemsPerPage ? this.state.maxItemsPerPage : 10;
    const filterByRole = this.state.currentRoleCategory && this.state.currentRoleCategory.id !== '0';
    const filterByName = this.state.searchText && this.state.searchText !== '';
    if (filterByRole || filterByName) {
      offset = 0;
      limit = 9999999; // set a max number
    }
    this.setState({ isLoading: true });
    ApiClient.getUsers(offset, limit, this.state.sortBy.name, this.state.sortBy.currentSortType)
      .then((res) => {
        if (res) {
          let users = res.records;
          let totalFilteredRecords = users.length;
          if (filterByRole || filterByName) {
            if (filterByRole) {
              users = res.records.filter((user) => {
                return user.roles && user.roles[0].id === this.state.currentRoleCategory.id;
              });
            }
            if (filterByName) {
              users = users.filter((user) => {
                return (
                  user.firstName.toLowerCase().match(this.state.searchText.toLowerCase()) ||
                  user.lastName.toLowerCase().match(this.state.searchText) ||
                  user.id.toLowerCase().match(this.state.searchText)
                );
              });
            }
            totalFilteredRecords = users.length;
            const revisedOffset = this.state.currentPageNumber > 1 ? this.state.currentPageOffset : offset;
            users = users.splice(
              revisedOffset,
              revisedOffset + this.state.maxItemsPerPage < users.length
                ? revisedOffset + this.state.maxItemsPerPage
                : users.length,
            );
          }
          this.setState(
            {
              users,
              totalNumberOfPages: Math.ceil(
                (filterByRole || filterByName ? totalFilteredRecords : res.totalCount) / this.state.maxItemsPerPage,
              ),
            },
            () => {
              //  ProgressIndicator.hide();
              this.setState({ isLoading: false });
            },
          );
        }
      })
      .catch((error) => {
        this.setState(
          {
            users: [],
          },
          () => {
            // ProgressIgitndicator.hide();
            this.setState({ isLoading: false });
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  }
  public sortUsers = (propName: string, sortOrder: string) => {
    const sortBy: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: this.state.sortBy.currentSortType,
    };
    this.setState(
      {
        sortBy,
      },
      () => {
        this.getUsers();
      },
    );
  };
  public componentDidMount() {
    this.setState({ isLoading: true });
    ProgressIndicator.show();
    ApiClient.getUserRoles()
      .then((res) => {
        if (res) {
          this.setState({ roles: res }, () => {
            SelectBox.defaultSetup();
          });
        }
        this.setState({ isLoading: false });
      })
      .catch((error) => {
        this.setState(
          {
            roles: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
            this.setState({ isLoading: false });
            SelectBox.defaultSetup();
          },
        );
      });

    this.getUsers();
    this.getDivisions();
    ProgressIndicator.hide();
  }

  public onSearchInput = debounce((e: React.FormEvent<HTMLInputElement>) => {
    const input = e.target as HTMLInputElement;
    const searchText = input.value.toLowerCase();
    this.setState({ searchText }, () => {
      this.getUsers();
    });
  }, 500);

  public onRoleFilterChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        // tslint:disable-next-line: radix
        this.setState({ currentRoleCategory: { id: option.value, name: option.label } }, () => {
          this.getUsers();
        });
      });
    }
  };
  public getDivisions = () => {
    return ApiClient.getDivisions()
      .then((res1) => {
        if (res1) {
          const tempDivisionList: any = [];
          res1.forEach((divsion) => {
            tempDivisionList.push({
              id: divsion.id + '',
              name: divsion.name,
              subdivisions: divsion.subdivisions.filter((item: ISubDivision) => item.id !== 'EMPTY'),
            });
          });
          this.setState({ divisionList: tempDivisionList });
        }
      })
      .catch((error) => {
        this.setState(
          {
            divisionList: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public render() {
    const divisionError = this.state.divisionError || '';
    const userData = this.state.users.map((user) => {
      return <UserInfoRowItem key={user.id} user={user} roles={this.state.roles} showEditModal={this.showEditModal} />;
    });
    const hasUserRole = this.state.updatedRole?.id === USER_ROLE.USER;
    const hasAdminRole = this.state.updatedRole?.id === USER_ROLE.ADMIN;
    const hasPrivilegedRole = !hasUserRole && !hasAdminRole;
    const moduleAdminRoles = this.state.roles.filter((role: IRole) => role.id !== USER_ROLE.USER && role.id !== USER_ROLE.ADMIN);
    const moduleRoleValues = this.state.moduleRoles.map((role: IRole) => {
      return role.id;
    });
    const editModalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContent}>
        Select a User Role for {this.state.currentUserToEdit ? this.state.currentUserToEdit.firstName : ''}{' '}
        {this.state.currentUserToEdit ? this.state.currentUserToEdit.lastName : ''} and Press &laquo;Save&raquo; to
        confirm.
        <div className={Styles.roleContent}>
          <div>
            <div id="roleSelectContainer" className="input-field-group include-error">
              <label className="radio">
                <span className="wrapper">
                  <input
                    type="radio"
                    className="ff-only"
                    name="role"
                    value={USER_ROLE.USER}
                    checked={hasUserRole}
                    onChange={this.onMainRoleChange}
                  />
                </span>
                <span className="label">User(Defualt)</span>
              </label>
              <label className="radio">
                <span className="wrapper">
                  <input
                    type="radio"
                    className="ff-only"
                    name="role"
                    value={USER_ROLE.ADMIN}
                    checked={hasAdminRole}
                    onChange={this.onMainRoleChange}
                  />
                </span>
                <span className="label">Admin(Complete Access to Management)</span>
              </label>
              <br />
              <label className="radio">
                <span className="wrapper">
                  <input
                    type="radio"
                    className="ff-only"
                    name="role"
                    value="-1"
                    checked={hasPrivilegedRole}
                    onChange={this.onMainRoleChange}
                  />
                </span>
                <span className="label">Privileged Roles(Access based on Modules)</span>
              </label>
            </div>  
            <div className={classNames(Styles.flexLayout, hasPrivilegedRole ? '' : 'hide')}>
              <div id="roleContainer" className="input-field-group include-error">
                <label id="roleLabel" className="input-label" htmlFor="roleSelect">
                  Select Module Admin Roles
                </label>
                <div id="role" className="custom-select">
                  <select
                    id="roleSelect"
                    multiple={true}
                    onChange={this.onModuleRoleChange}
                    value={moduleRoleValues}
                  >
                    {moduleAdminRoles.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                {/* <span className={classNames('error-message', roleError.length ? '' : 'hide')}>{roleError}</span> */}
              </div>
              {this.state.moduleRoles.some((role: IRole) => role.id === USER_ROLE.DIVISIONADMIN) ? (
                <div
                  id="divisionContainer"
                  className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}
                >
                  <label id="divisionLabel" className="input-label" htmlFor="divisionSelect">
                    Division<sup>*</sup>
                  </label>
                  <div id="division" className="custom-select">
                    <select
                      id="divisionSelect"
                      required={true}
                      multiple={true}
                      onChange={this.onDivisionChange}
                      value={this.state.selectedDivisions}
                    >
                      {this.state.divisionList.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>{divisionError}</span>
                </div>
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
      </div>
    );
    return (
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.searchPanel}>
            <div>
              <div className={`input-field-group search-field ${this.state.isLoading ? 'disabled' : ''}`}>
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
                  onChange={this.onSearchInput}
                  disabled={!this.state.searchText && this.state.isLoading}
                />
              </div>
            </div>
            <div>
              <div id="statusContainer" className={`input-field-group ${this.state.isLoading ? 'disabled' : ''}`}>
                <label id="statusLabel" className="input-label" htmlFor="statusSelect">
                  Filter by
                </label>
                <div className={Styles.customContainer}>
                  <div className={`custom-select ${this.state.isLoading ? 'disabled' : ''}`}>
                    <select id="filterBy" onChange={this.onRoleFilterChange} defaultValue="0">
                      <option key="0" id="0" value="0">
                        All
                      </option>
                      {/* {this.state.categories && */}
                      {this.state.roles.map((role: IRole) => (
                        <option key={role.id} id={'' + role.id} value={role.id}>
                          {role.name}
                        </option>
                      ))}
                      {/* }  */}
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          {userData.length === 0 ? (
            <div className={Styles.userIsEmpty}>There is no user available</div>
          ) : (
            <div className={Styles.tablePanel}>
              {!this.state.isLoading ? (
                <table className="ul-table users">
                  <thead>
                    <tr className="header-row">
                      <th onClick={this.sortUsers.bind(null, 'firstName', this.state.sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' +
                            (this.state.sortBy.name === 'firstName' ? this.state.sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Name
                        </label>
                      </th>
                      <th onClick={this.sortUsers.bind(null, 'id', this.state.sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' +
                            (this.state.sortBy.name === 'id' ? this.state.sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          User-ID
                        </label>
                      </th>
                      <th onClick={this.sortUsers.bind(null, 'roles', this.state.sortBy.nextSortType)}>
                        <label
                          className={
                            'sortable-column-header ' +
                            (this.state.sortBy.name === 'roles' ? this.state.sortBy.currentSortType : '')
                          }
                        >
                          <i className="icon sort" />
                          Role
                        </label>
                      </th>
                      <th className="actionColumn">
                        <label>Action</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>{userData}</tbody>
                </table>
              ) : (
                <div className={classNames('text-center', Styles.spinner)}>
                  <div className="progress infinite" />
                </div>
              )}

              {this.state.users.length ? (
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
            </div>
          )}
        </div>
        <Modal
          title={
            this.state.currentUserToEdit
              ? 'Edit ' + this.state.currentUserToEdit.lastName + ', ' + this.state.currentUserToEdit.firstName
              : ''
          }
          buttonAlignment="center"
          acceptButtonTitle="Save"
          showAcceptButton={true}
          showCancelButton={false}
          show={this.state.showEditUsersModal}
          content={editModalContent}
          onCancel={this.onCancelRoleChanges}
          onAccept={this.onAcceptRoleChanges}
        />
      </div>
    );
  }
  public showEditModal = (user: IUserInfo) => {
    const roleValue = user.roles.map((userRole: IRole) => {
      return userRole;
    });
    const hasPrivilegedRole = roleValue.some((role:IRole) => role.id !== USER_ROLE.USER && role.id !== USER_ROLE.ADMIN);
    this.setState(
      {
        showEditUsersModal: true,
        currentUserToEdit: user,
        updatedRole: hasPrivilegedRole ? undefined : roleValue[0],
        moduleRoles: hasPrivilegedRole ? roleValue : [],
        selectedDivisions: user.divisionAdmins ? user.divisionAdmins : [],
      },
      () => {
        SelectBox.defaultSetup(true);
      },
    );
  };

  protected onMainRoleChange = (event: React.FormEvent<HTMLInputElement>) => {
    // this.setState({ dnaComputeMode: event.currentTarget.value });
    const selectedValue = event.currentTarget.value;
    this.setState(
      {
        updatedRole: this.state.roles.find((item: IRole) => item.id === selectedValue),
        moduleRoles: [],
        selectedDivisions: [],
      },
      () => {
        SelectBox.defaultSetup(true);
      },
    );
  };

  public onModuleRoleChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: IRole[] = [];
    Array.from(selectedOptions).forEach((option) => {
      // let label = option.label;
      // if (label === 'ADMIN') {
      //   label = 'Admin';
      // } else if (label === 'EXTENDED') {
      //   label = 'Extended';
      // } else if (label === 'USER') {
      //   label = 'User';
      // }

      selectedValues.push(this.state.roles.find((role: IRole) => role.id === option.value));
    });

    this.setState({ moduleRoles: selectedValues }, () => {
      SelectBox.defaultSetup(true);
    });
  };

  public onDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: any[] = [];
    Array.from(selectedOptions).forEach((option) => {
      // const location: any = { id: null, name: null };
      // location.id = option.value;
      // location.name = option.label;
      selectedValues.push(option.label);
    });
    this.setState({ selectedDivisions: selectedValues });
  };

  protected onAcceptRoleChanges = () => {
    if (this.state.currentUserToEdit && (this.state.updatedRole || this.state.moduleRoles.length)) {
      const roleChanged = this.state.updatedRole ? this.state.currentUserToEdit.roles.filter((userCurrentRole: IRole) => {
        return userCurrentRole.id !== this.state.updatedRole.id;
      }) : this.state.moduleRoles;
      const isDivisionChanged =
        JSON.stringify(this.state.currentUserToEdit.divisionAdmins) !== JSON.stringify(this.state.selectedDivisions);

      if (this.state.moduleRoles.some((role:IRole) => role.id === USER_ROLE.DIVISIONADMIN)) {
        if (this.validateUserRoleForm()) {
          this.callApiToUpdateUser();
        }
      } else {
        if ((roleChanged && roleChanged.length > 0) || isDivisionChanged) {
          this.callApiToUpdateUser();
        } else {
          this.setState({ showEditUsersModal: false }, () => {
            this.showErrorNotification('User is not updated!');
          });
        }
      }
    }
  };

  protected callApiToUpdateUser = () => {
    const userToEdit = this.state.currentUserToEdit;
    const putData: IUserRequestVO = {
      data: {
        id: userToEdit.id,
        firstName: userToEdit.firstName,
        lastName: userToEdit.lastName,
        department: userToEdit.department,
        eMail: userToEdit.eMail,
        mobileNumber: userToEdit.mobileNumber,
        favoriteUsecases: userToEdit.favoriteUsecases,
        roles: this.state.updatedRole ? [this.state.updatedRole] : this.state.moduleRoles,
        divisionAdmins: this.state.moduleRoles.some((role:IRole) => role.id === USER_ROLE.DIVISIONADMIN) ? this.state.selectedDivisions : [],
      },
    };
    ProgressIndicator.show();
    ApiClient.updateUser(putData)
      .then((response) => {
        if (response) {
          const users = this.state.users.map((user) => {
            if (user.id === userToEdit.id) {
              user.roles = this.state.updatedRole ? [this.state.updatedRole] : this.state.moduleRoles;
              user.divisionAdmins = response.divisionAdmins;
            }
            return user;
          });
          this.setState({ currentUserToEdit: response, showEditUsersModal: false, users }, () => {
            this.showNotification('Role updated successfully!');
          });
        }
      })
      .catch((error) => {
        ProgressIndicator.hide();
        this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
      });
  };

  protected onCancelRoleChanges = () => {
    this.setState({ showEditUsersModal: false });
  };
  protected showErrorNotification(message: string) {
    Notification.show(message, 'alert');
  }

  protected showNotification(message: string) {
    ProgressIndicator.hide();
    Notification.show(message);
  }

  protected onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getUsers();
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getUsers();
    });
  };
  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      this.getUsers();
    });
  };

  protected validateUserRoleForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (!this.state.selectedDivisions || !this.state.selectedDivisions.length) {
      this.setState({ divisionError: errorMissingEntry });
      formValid = false;
    }
    return formValid;
  };
}
