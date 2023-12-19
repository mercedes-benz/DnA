import classNames from 'classnames';
import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './tableCollaborators.scss';
import TeamSearch from 'dna-container/TeamSearch';
import { setTables } from '../../redux/graphSlice';
import Notification from '../../common/modules/uilab/js/src/notification';

const TableCollaborators = ({ table, onSave }) => {
  const { project } = useSelector(state => state.graph);
  const dispatch = useDispatch();

  const [searchTerm, setSearchTerm] = useState('');
  const [showUserDetails, setShowUserDetails] = useState(false);
  const [showUserAlreadyExistsError] = useState(false);
  const [editMode] = useState(false);
  const [teamMember] = useState();
  const [collabs, setCollabs] = useState(table?.collabs?.length > 0 ? [...table.collabs] : []);

  const addMemberFromTeamSearch = (member) => {
    const memberObj = {
      collaborator: {
        ...member,
        id: member.shortId
      },
      hasWritePermission: false
    }
    setCollabs([...collabs, memberObj]);
  }

  const resetUserAlreadyExists = () => {
    console.log('reset user already exists');
  }

  const onPermissionChange = (collab) => {
    let collabIndex = -1;
    const collabItem = collabs.find((item, itemIndex) => {
      collabIndex = itemIndex;
      return item.collaborator.id === collab.collaborator.id;
    });
    if (collabItem.hasWritePermission) {
      collabItem.hasWritePermission = false;
    } else {
      collabItem.hasWritePermission = true;
    }
    collabs[collabIndex] = collabItem;
    setCollabs([...collabs]);
  };

  const onCollabaratorDelete = (id) => {
    const tempCollabs = collabs.filter(item => item.collaborator.id !== id);
    setCollabs(tempCollabs);
  };

  const handleSaveCollabs = () => {
    const projectTemp = {...project};
    const tempTables = projectTemp.tables.filter(item => item.tableName !== table.tableName);
    const currentTable = {...table, collabs: [...collabs]};
    const fullTables = [...tempTables, currentTable];
    projectTemp.tables = [...fullTables];
    dispatch(setTables(projectTemp.tables));
    onSave();
    Notification.show('Collaborator(s) added successfully to the table');
  }

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
          {collabs?.length > 0 ? (
            <React.Fragment>
              <div className={Styles.collUserTitle}>
                <div className={Styles.collUserTitleCol}>User ID</div>
                <div className={Styles.collUserTitleCol}>Name</div>
                <div className={Styles.collUserTitleCol}>Permission</div>
                <div className={Styles.collUserTitleCol}></div>
              </div>
              <div className={Styles.collUserContent}>
                {collabs.map(
                  (item, index) => {
                    return (
                      <div
                        key={index}
                        className={Styles.collUserContentRow}
                      >
                        <div className={Styles.collUserTitleCol}>{item.collaborator.id}</div>
                        <div className={Styles.collUserTitleCol}>
                          {item.collaborator.firstName + ' ' + item.collaborator.lastName}
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
                                  defaultChecked={
                                    item.hasWritePermission !== null
                                      ? item.hasWritePermission
                                      : false
                                  }
                                  onChange={() => onPermissionChange(item)}
                                />
                              </span>
                              <span className="label">Write</span>
                            </label>
                          </div>
                        </div>
                        <div className={Styles.collUserTitleCol}>
                          <div
                            className={Styles.deleteEntry}
                            onClick={() => onCollabaratorDelete(item.collaborator.id)}
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
              <button className={'btn btn-tertiary'} onClick={handleSaveCollabs}>Save</button>
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