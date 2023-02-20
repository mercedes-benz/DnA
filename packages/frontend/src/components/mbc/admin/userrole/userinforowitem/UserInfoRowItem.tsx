import cn from 'classnames';
import * as React from 'react';

import { IRole, IUserInfo } from 'globals/types';
import Styles from './UserInfoRowItem.scss';

const classNames = cn.bind(Styles);

export interface IUserInfoRowItemProps {
  user: IUserInfo;
  roles: IRole[];
  key: string;
  showEditModal: (user: IUserInfo) => void;
}

export default class UserInfoRowItem extends React.Component<IUserInfoRowItemProps, any> {
  protected isTouch = false;
  protected listRowElement: HTMLTableRowElement;

  constructor(props: any) {
    super(props);
  }

  public onEditUser = () => {
    this.props.showEditModal(this.props.user);
  };

  public render() {
    const user = this.props.user;
    const roleValue = user.roles.map((userRole: IRole) => {
      return userRole.name;
    });
    return (
      <React.Fragment>
        <tr id={user.id} key={user.id} className="data-row" ref={this.listRow}>
          <td className="wrap-text">
            {user.firstName},&nbsp;{user.lastName}
          </td>
          <td className="wrap-text">{user.id}</td>
          <td>{roleValue.join(', ')}</td>
          <td className={'wrap-text ' + classNames(Styles.actionLinksTD)} onClick={this.onEditUser}>
            Edit
          </td>
        </tr>
      </React.Fragment>
    );
  }

  protected listRow = (element: HTMLTableRowElement) => {
    this.listRowElement = element;
  };
}
