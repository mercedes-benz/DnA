import classNames from 'classnames';
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
// styles
import Styles from './data-entry-users.scss';
import AddUser from 'dna-container/AddUser';
import DatePicker from 'dna-container/DatePicker';

const DataEntryUsers = ({ user }) => {
  const { id } = useParams();
  const [dataEntryUsers, setDataEntryUsers] = useState([]);
  const [createdBy] = useState();
  const [dueDate, setDueDate] = useState();
  const [instructions, setInstructions] = useState('');
  const getCollabarators = (collaborators) => {
    const collabarationData = {
      firstName: collaborators.firstName,
      lastName: collaborators.lastName,
      accesskey: collaborators.shortId,
      department: collaborators.department,
      email: collaborators.email,
      mobileNumber: collaborators.mobileNumber,
    };

    let duplicateMember = false;
    duplicateMember = dataEntryUsers.filter((member) => member.accesskey === collaborators.shortId)?.length
      ? true
      : false;
    const isCreator = id ? createdBy.id === collaborators.shortId : user.id === collaborators.shortId;

    if (duplicateMember) {
      Notification.show('User Already Exist.', 'warning');
    } else if (isCreator) {
      Notification.show(
        `${collaborators.firstName} ${collaborators.lastName} is a creator. Creator can't be added as user.`,
        'warning',
      );
    } else {
      dataEntryUsers.push(collabarationData);
      setDataEntryUsers([...dataEntryUsers]);
    }
  };
  const removeDeUser = (index) => {
    const temp = dataEntryUsers.splice(index, 1);
    setDataEntryUsers(temp);
  };
  return (
    <>
      <div className={classNames(Styles.container)}>
        <div className={Styles.header}>
          <h3>Search, Add and Send to Data Entry Users</h3>
          <p>Enter the information to start creating!</p>
        </div>
        <div className={Styles.flex}>
          <div className={Styles.col}>
            <AddUser
              title={'User'} 
              getCollabarators={getCollabarators} 
              dagId={''} 
              isRequired={false} 
              isUserprivilegeSearch={false} 
            />
            <div className={Styles.deUserList}>
              {dataEntryUsers.length > 0 ? 
                <table>
                  <thead>
                    <tr>
                      <th className={Styles.id}>ID</th>
                      <th className={Styles.name}>Name</th>
                      <th className={Styles.action}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dataEntryUsers.map((dataEntryUser, index) => {
                      <tr>
                        <td>{dataEntryUser?.shortId}</td>
                        <td>{dataEntryUser?.firstName} {dataEntryUser?.lastName}</td>
                        <td>
                          <button className={classNames('btn', Styles.btnAction)} onClick={() => removeDeUser(index)}>
                            <i className="icon mbc-icon close thin"></i> Remove
                          </button>
                        </td>
                      </tr>
                    })}
                  </tbody>
                </table>
                : 
                <div className={Styles.noUsers}>
                  <p>No Data Entry users added</p>
                </div>
              }
            </div>
          </div>
          <div className={Styles.col}>
            <div className={classNames('input-field-group area')}>
              <label className="input-label" htmlFor="instructions">
                Filling Instructions
              </label>
              <textarea
                id="instructions"
                className={'input-field-area'}
                type="text"
                rows={50}
                value={instructions}
                onChange={(e) => setInstructions(e.target.value)}
              />
            </div>
          </div>
          <div className={Styles.col2}>
            <div className={'input-field-group'}>
              <label htmlFor="dueDate" className="input-label">
                Due Date
              </label>
              <DatePicker
                label="Due Date"
                name={'Due Date'}
                value={dueDate}
                // minDate={minDate}
                onChange={(value) => {
                  setDueDate(value);
                }}
              />
            </div>
          </div>
        </div>
        <div className={Styles.footer}>
          <button
            className="btn btn-tertiary"
            onClick={() => console.log('clicked')}
          >
            Send to Data Entry Users
          </button>
        </div>
      </div>
    </>
  );
}

export default DataEntryUsers;