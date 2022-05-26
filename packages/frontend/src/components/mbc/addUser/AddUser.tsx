import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { IUserDetails } from '../../../globals/types';
import { ApiClient } from '../../../services/ApiClient';
// @ts-ignore
import InputFieldsUtils from '../../formElements/InputFields/InputFieldsUtils';
import Styles from './AddUser.scss';

const classNames = cn.bind(Styles);

export interface IAddUserProps {
  /** function used to set collaborators information */
  getCollabarators: (teamMemberObj: IUserDetails, dagId: string) => void;
  /** string to be passed as a parameter to getCollaborator function */
  dagId: string;
}
export interface IAddUserState {
  belongingInternal: boolean;
  teamPositionInternal: string;
  teamPositionInternalError: string;
  userIdInternal: string;
  userIdInternalError: string;
  companyExternal: string;
  companyExternalError: string;
  teamPositionExternal: string;
  teamPositionExternalError: string;
  showNotFoundError: boolean;
  teamMemberError: string;
}

/**
 * Component to be used to add Users/Collaborators
 * @visibleName Add User
 */
export default class AddUser extends React.Component<IAddUserProps, IAddUserState> {
  constructor(props: any) {
    super(props);
    this.state = {
      belongingInternal: true,
      teamPositionInternal: '',
      teamPositionInternalError: null,
      userIdInternal: '',
      userIdInternalError: null,
      companyExternal: '',
      companyExternalError: null,
      teamPositionExternal: '',
      teamPositionExternalError: null,
      showNotFoundError: false,
      teamMemberError: null,
    };
  }

  public componentDidMount() {
    Button.defaultSetup();
  }

  public render() {
    const {
      userIdInternal,
      // userIdInternalError,
    } = this.state;

    return (
      <div id="teamsModalDiv" className={classNames(Styles.firstPanel, Styles.addCollabarionUserAdd)}>
        <div className={Styles.formWrapper}>
          <div>
            <div>
              <div className={classNames(Styles.flexLayout, Styles.searchWrapper)}>
                <div className={classNames('input-field-group')}>
                  <label htmlFor="userIdInternal" className="input-label">
                    Add Collaborator
                  </label>
                  <input
                    type="text"
                    className="input-field"
                    id="userIdInternal"
                    name="userIdInternal"
                    placeholder="Please Enter EMEA ID / Short ID"
                    autoComplete="off"
                    value={userIdInternal}
                    maxLength={100}
                    onKeyDown={this.onSearchKeyDown}
                    onChange={this.textInputOnChange}
                  />
                  <span className={Styles.clearSearch} onClick={this.onSearchClearIconClick}>
                    <i className={classNames('icon mbc-icon close circle', userIdInternal.length ? '' : 'hide')} />
                  </span>
                  {/* <span className={classNames('error-message', userIdInternalError.length ? '' : 'hide')}>
                    {userIdInternalError}
                  </span> */}
                </div>
                <div>
                  <button className="btn btn-primary" type="button" onClick={this.onSearchClick}>
                    Add User
                  </button>
                </div>
              </div>
              <span className={classNames('error-message', this.state.showNotFoundError ? '' : 'hide')}>
                User details not found. Please provide valid User-ID.
              </span>
            </div>
          </div>
        </div>
      </div>
    );
  }
  protected onSearchClearIconClick = () => {
    this.setState({ userIdInternal: '' });
  };
  protected onSearchClick = () => {
    const seachUserId = this.state.userIdInternal;
    ProgressIndicator.show();
    ApiClient.getDRDUserInfo(seachUserId)
      .then((data: any) => {
        const teamMemberObj: IUserDetails = {
          department: data.department,
          email: data.email,
          firstName: data.firstName,
          shortId: data.id,
          lastName: data.lastName,
          mobileNumber: data.mobileNumber,
        };
        this.setState({
          showNotFoundError: false,
        });
        this.props.getCollabarators(teamMemberObj, this.props.dagId);
        this.setState({ userIdInternal: '' });
        ProgressIndicator.hide();
      })
      .catch(() => {
        ProgressIndicator.hide();
        this.setState({
          showNotFoundError: true,
        });
      });
  };
  protected onSearchKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    if (keyPressed === 13) {
      this.onSearchClick();
    }
  };

  protected textInputOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const name: string = e.currentTarget.name;
    const value: string = e.currentTarget.value;
    this.setState((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };
}
