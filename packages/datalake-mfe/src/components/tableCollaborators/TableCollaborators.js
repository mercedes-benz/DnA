import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './tableCollaborators.scss';
import TeamSearch from 'dna-container/TeamSearch';
import { setTables } from '../../redux/graphSlice';
import Notification from '../../common/modules/uilab/js/src/notification';

const TableCollaborators = ({ table, onSave, user ,onProjCollabAdd ,isProjectLevelCollab}) => {
  const { project } = useSelector(state => state.graph);
  const dispatch = useDispatch();

  const [searchTerm, setSearchTerm] = useState('');
  const [showUserDetails, setShowUserDetails] = useState(false);
  const [showUserAlreadyExistsError, setShowUserAlreadyExistsError] = useState(false);
  const [editMode] = useState(false);
  const [teamMember] = useState();
  const [collabs, setCollabs] = useState(table?.length > 0 ? table : []);

  const addMemberFromTeamSearch = (member) => {
    const isMemberExists = collabs.filter(item => item.id === member.shortId);
    if(user.id === member.shortId) {
      Notification.show(`Owner can't be added as a collaborator`, 'alert');
    } else if (isMemberExists.length > 0) {
      setShowUserAlreadyExistsError(true);
    } else {
      const memberObj = {
        collaborator: {
          ...member,
          id: member.shortId
        },
        hasWritePermission: false
      }
      setCollabs([...collabs, memberObj]);
    }
    setShowUserAlreadyExistsError(false);
  }

  const resetUserAlreadyExists = () => {
    console.log('reset user already exists');
  }

  useEffect(()=>{
    if(isProjectLevelCollab){
      onProjCollabAdd(collabs);
    }
  },[collabs])

  
  const onPermissionChange = (collab) => {
    const collabItem = collabs.map((item) => {
      if (item.collaborator.id === collab.collaborator.id) {
        return {
          ...item,
          hasWritePermission: !item.hasWritePermission,
        };
      }
      return item;
    });

    setCollabs(collabItem);
    if(isProjectLevelCollab){
      onProjCollabAdd(collabItem);
    }
  };

  const onCollabaratorDelete = (id) => {
    const tempCollabs = collabs.filter(item => item.collaborator.id !== id);
    setCollabs(tempCollabs);
  };

  useEffect(() => {
    const projectTemp = {...project};
    const tableIndex = projectTemp.tables.findIndex(item => item.tableName === table.tableName);
    let newTables = [...projectTemp.tables];
    newTables[tableIndex] = {...newTables[tableIndex], collabs: [...collabs]};
    dispatch(setTables(newTables));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [collabs]);

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
                              <label className={Styles.checkBoxlabel}>Read</label>
                            </label>
                          </div>
                          &nbsp;&nbsp;&nbsp;
                          <div
                            className={classNames(
                              'input-field-group include-error ' + Styles.inputGrp,
                            )}
                          >
                            <label className= {classNames('checkbox ' , Styles.writeAccess , item.collaborator.id === user.id ? Styles.checkBoxDisable : '')}>
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
                              <label className={Styles.checkBoxlabel}>Write</label>
                            </label>
                          </div>
                        </div>
                        <div className={Styles.collUserTitleCol}>
                          <div
                            className= {classNames(Styles.deleteEntry , item.collaborator.id === user.id ? Styles.disableDelete : '' )}
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
              <div className={classNames(Styles.btnRight , isProjectLevelCollab ? 'hidden' : '') }>
                <button className={'btn btn-tertiary'} onClick={onSave}>Ok</button>
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