import classNames from 'classnames';
import React from 'react';
import Styles from './de-users-information.scss';

const DeUsersInformation = ({ project }) => {
  return (
    <div className={Styles.container}>
      <div className={Styles.header}>
        <h3>{project?.name} Data Entry Users</h3>
        <p>Know the filling status</p>
      </div>
      <div className={Styles.flex}>
        <div className={Styles.col}>
          <div className={Styles.deUserList}>
            <table>
              <thead>
                <tr>
                  <th className={Styles.id}>ID</th>
                  <th className={Styles.name}>Name</th>
                  <th className={Styles.status}>Status</th>
                </tr>
              </thead>
              <tbody>
                {project?.dataEntryUsers?.length > 0 && project?.dataEntryUsers?.map((dataEntryUser) => {
                  <tr>
                    <td>{dataEntryUser?.shortId}</td>
                    <td>{dataEntryUser?.firstName} {dataEntryUser?.lastName}</td>
                    <td>
                      <span className={classNames(Styles.notFilled)}>{dataEntryUser?.fillingStatus}</span>
                    </td>
                  </tr>
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DeUsersInformation;