import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './tableCollaborators.scss';
import TeamSearch from 'dna-container/TeamSearch';

const TableCollaborators = ({ collaborators }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showUserDetails, setShowUserDetails] = useState(false);
  const [showUserAlreadyExistsError] = useState(false);
  const [editMode] = useState(false);
  const [teamMember] = useState();
  
  const addMemberFromTeamSearch = () => {
    console.log('add team member from search');
  }

  const resetUserAlreadyExists = () => {
    console.log('reset user already exists');
  }

  const onPermissionEdit = (index) => {
    return () => {
      if (collaborators[index].permissions.includes('can_edit')) {
        collaborators[index].permissions.splice(
          collaborators[index].permissions.indexOf('can_edit'),
          1,
        );
      } else {
        collaborators[index].permissions.push('can_edit');
      }
    };
  };

  const onCollabaratorDelete = (index) => {
    collaborators?.splice(index, 1);
  };

  return (
    <div className={Styles.dagCollContent}>
      <div className={Styles.dagCollContentList}>
        <div className={Styles.dagCollContentListAdd}>
          <TeamSearch
            label={'Find User'}
            editMode={editMode}
            teamMemberObj={teamMember}
            onAddTeamMember={addMemberFromTeamSearch}
            userAlreadyExists={showUserAlreadyExistsError}
            resetUserAlreadyExists={resetUserAlreadyExists}
            btnText="Add User"
            searchTerm={searchTerm}
            setSearchTerm={(val) => setSearchTerm(val)}
            showUserDetails={showUserDetails}
            setShowUserDetails={(val) => setShowUserDetails(val)}
          />
        </div>
        <div className={Styles.dagCollUsersList}>
          {collaborators?.length > 0 ? (
            <React.Fragment>
              <div className={Styles.collUserTitle}>
                <div className={Styles.collUserTitleCol}>User ID</div>
                <div className={Styles.collUserTitleCol}>Name</div>
                <div className={Styles.collUserTitleCol}>Permission</div>
                <div className={Styles.collUserTitleCol}></div>
              </div>
              <div className={Styles.collUserContent}>
                {collaborators.map(
                  (item, index) => {
                    return (
                      <div
                        key={index}
                        className={Styles.collUserContentRow}
                      >
                        <div className={Styles.collUserTitleCol}>{item.username}</div>
                        <div className={Styles.collUserTitleCol}>
                          {item.firstName + ' ' + item.lastName}
                        </div>
                        <div className={Styles.collUserTitleCol}>
                          <div
                            className={classNames(
                              'input-field-group include-error ' + Styles.inputGrp,
                            )}
                          >
                            <label className={'checkbox ' + Styles.checkBoxDisable}>
                              <span className="wrapper">
                                <input
                                  type="checkbox"
                                  className="ff-only"
                                  value="can_read"
                                  checked={true}
                                />
                              </span>
                              <span className="label">Read</span>
                            </label>
                          </div>
                          &nbsp;&nbsp;&nbsp;
                          <div
                            className={classNames(
                              'input-field-group include-error ' + Styles.inputGrp,
                            )}
                          >
                            <label className={'checkbox ' + Styles.writeAccess}>
                              <span className="wrapper">
                                <input
                                  type="checkbox"
                                  className="ff-only"
                                  value="can_edit"
                                  checked={
                                    item.permissions !== null
                                      ? item.permissions.includes('can_edit')
                                      : false
                                  }
                                  onClick={() => onPermissionEdit(index)}
                                />
                              </span>
                              <span className="label">Write</span>
                            </label>
                          </div>
                        </div>
                        <div className={Styles.collUserTitleCol}>
                          <div
                            className={Styles.deleteEntry}
                            onClick={() => onCollabaratorDelete(index)}
                          >
                            <i className="icon mbc-icon trash-outline" />
                            Delete Entry
                          </div>
                        </div>
                      </div>
                    );
                  },
                )}
              </div>
            </React.Fragment>
          ) : (
            <div className={Styles.dagCollContentEmpoty}>
              <h6>Collaborators Not Exist!</h6>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default TableCollaborators;