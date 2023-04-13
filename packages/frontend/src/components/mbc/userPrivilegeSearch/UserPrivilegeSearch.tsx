import cn from 'classnames';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { debounce } from 'lodash';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { IUserPrivilege } from 'globals/types';
import { ApiClient } from '../../../services/ApiClient';
import Styles from './UserPrivilegeSearch.scss';

const classNames = cn.bind(Styles);

export interface UserPrivilegeSearchProps {
  label: any;
  editMode?: boolean;
  userId?: string;
  teamMemberObj?: IUserPrivilege;
  onAddTeamMember: (teamMemberObj: IUserPrivilege) => void;
  userAlreadyExists?: boolean;
  resetUserAlreadyExists?: () => void;
  btnText: string;
  searchTerm: string;
  setSearchTerm: (val: string) => void;
  showUserDetails: boolean;
  setShowUserDetails: (val: boolean) => void;
  customUserErrorMsg?: string;
  fieldMode?: boolean;
  fieldValue?: string;
  setFieldValue?: (val: string) => void;
}

const UserprivilegeSearch = (props: UserPrivilegeSearchProps) => {
  const KEY_CODE = {
    backspace: 8,
    tab: 9,
    enter: 13,
    upArrow: 38,
    downArrow: 40,
  };

  const { searchTerm, setSearchTerm, showUserDetails, setShowUserDetails, fieldMode, fieldValue, setFieldValue } =
    props;

  if (fieldMode && typeof fieldValue === 'undefined') {
    throw Error(`If "fieldMode" is ${fieldMode}, fieldValue and setFieldValue props are required.`);
  }

  const searchInput = useRef(null);
  const suggestionContainer = useRef(null);
  const [teamMemberObj, setTeamMemberObj] = useState(props.teamMemberObj);
  const [results, setResults] = useState([]);
  const [cursor, setCursor] = useState(-1);
  const [hideSuggestion, setHideSuggestion] = useState(true);
  const [showNoResultsError, setShowNoResultsError] = useState(false);
  const [showNoUserFoundError, setShowNoUserFoundError] = useState(false);

  useEffect(() => {
    if (props.editMode) {
      setTeamMemberObj(props.teamMemberObj);
      setShowUserDetails(true);
    }
  }, [props.editMode]);

  const suggestions = results.map((item: any, index: number) => {
    return (
      <li
        key={item.id || item.shortId}
        onClick={() => onSuggestionItemClick(index)}
        className={cursor === index ? Styles.active + ' active' : null}
      >
        {item.userId} - {item.profile}
      </li>
    );
  });

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
    setShowNoUserFoundError(false);
    setShowNoResultsError(false);
    setShowUserDetails(false);
  };

  useEffect(() => {
    if (searchTerm?.length > 1) {
      debouncedFetchUser(searchTerm);
    } else {
      setResults([]);
    }
  }, [searchTerm]);

  const debouncedFetchUser = useCallback(
    debounce((searchTerm) => {
      getTeamMembersInfoBySearchTerm(searchTerm);
    }, 500),
    [],
  );

  const getTeamMembersInfoBySearchTerm = (searchTerm: string) => {
    ApiClient.getUserprivilegeSearchTerm(searchTerm)
      .then((response) => {
        if (response) {
          if (response.data !== undefined) {
            setResults(response.data);
            setShowNoResultsError(response.data.length === 0 ? true : false);
            setHideSuggestion(response.data.length === 0);
            setCursor(-1);
          } else {
            setResults([]);
            setShowNoResultsError(true);
            setHideSuggestion(true);
            setCursor(-1);
          }
        }
      })
      .catch((err: Error) => {
        setShowNoResultsError(true);
        setHideSuggestion(true);
        setCursor(-1);
      });
  };

  const onSuggestionItemClick = (shortId: number) => {
    setHideSuggestion(true);
    const teamMemberObj: IUserPrivilege = {
      id: results[shortId].id,
      userId: results[shortId].userId,
      profile: results[shortId].profile,
      givenName: results[shortId].givenName,
      surName: results[shortId].surName,
    };
    setTeamMemberObj(teamMemberObj);
    setShowNoResultsError(false);
    if (fieldMode) {
      props.onAddTeamMember(teamMemberObj);
    } else {
      setShowUserDetails(true);
    }
    setSearchTerm('');
    searchInput.current.focus();
  };

  const dRDUserSearch = () => {
    ProgressIndicator.show();

    ApiClient.getUserprivilegeSearchTerm(searchTerm)
      .then((data: any) => {
        if (data?.data?.length > 0) {
          const teamMemberObj: IUserPrivilege = {
            id: data.data[0].id,
            userId: data.data[0].userId,
            profile: data.data[0].profile,
            givenName: data.data[0].givenName,
            surName: data.data[0].surName,
          };

          setTeamMemberObj(teamMemberObj);
          if (fieldMode) {
            setHideSuggestion(false);
            setResults([teamMemberObj]);
            props.onAddTeamMember(teamMemberObj);
          } else {
            setShowUserDetails(true);
            setHideSuggestion(true);
          }
          setShowNoResultsError(false);
          setShowNoUserFoundError(false);
        }
        ProgressIndicator.hide();
      })
      .catch(() => {
        ProgressIndicator.hide();
        setShowUserDetails(false);
        setHideSuggestion(true);
        setShowNoResultsError(false);
        setShowNoUserFoundError(true);
      });
  };

  const onSearchIconButtonClick = () => {
    dRDUserSearch();
  };

  const onCloseButtonClick = () => {
    setFieldValue('');
    setSearchTerm('');
    props.onAddTeamMember(null);
    searchInput.current.focus();
  };

  const onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const query = searchTerm;

    if (keyPressed === KEY_CODE.enter && query.length) {
      if (cursor === -1) {
        dRDUserSearch();
      } else {
        setHideSuggestion(true);

        if (cursor !== -1 && results.length && !hideSuggestion) {
          // this.gotoSolution(results[cursor].id);
          const teamMemberObj: IUserPrivilege = {
            id: results[cursor].id,
            userId: results[cursor].userId,
            profile: results[cursor].profile,
            givenName: results[cursor].givenName,
            surName: results[cursor].surName,
          };

          setTeamMemberObj(teamMemberObj);
          !fieldMode && setShowUserDetails(true);
          setShowNoResultsError(false);
          setShowNoUserFoundError(false);
          setCursor(-1);
          setSearchTerm('');
          searchInput.current.focus();
        }
      }
    } else if (keyPressed === KEY_CODE.upArrow && cursor > 0) {
      setCursor((prevState) => prevState - 1);
      const containerHeight = suggestionContainer.current.getBoundingClientRect().height;
      const activeElem = suggestionContainer.current.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;
        suggestionContainer.current.scrollTop = 0;
        if (containerHeight < activeElem.offsetTop) {
          suggestionContainer.current.scrollTop = activeElem.offsetTop - activeElemHeight * 2;
        }
      }
    } else if (keyPressed === KEY_CODE.downArrow && cursor < results.length - 1) {
      setCursor((prevState) => prevState + 1);
      const containerHeight = suggestionContainer.current.getBoundingClientRect().height;
      const activeElem = suggestionContainer.current.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;
        suggestionContainer.current.scrollTop = 0;
        if (containerHeight - 80 < activeElem.offsetTop) {
          suggestionContainer.current.scrollTop = activeElem.offsetTop - activeElemHeight * 1;
        }
      }
    }
  };

  return (
    <div className={Styles.teamSearchContainer}>
      <div>
        <div className={classNames(Styles.searchWrapper)}>
          <div className={'input-field-group'}>
            <label htmlFor="userId" className="input-label">
              {props.label}
            </label>

            <div id="searchPanel" className={Styles.searchPanel}>
              <input
                type="text"
                className={classNames(Styles.searchInputField, fieldMode ? `input-field ${Styles.addBorder}` : '')}
                ref={searchInput}
                id="userId"
                value={searchTerm || fieldValue || ''}
                placeholder="Short ID, first name, last name"
                onChange={onSearchInputChange}
                onKeyDown={onSearchInputKeyDown}
                maxLength={200}
                autoComplete="off"
              />
              {fieldMode ? (
                fieldValue?.length ? (
                  <button
                    className={classNames(Styles.fieldModeBtn, fieldValue ? '' : 'hide')}
                    onClick={onCloseButtonClick}
                  >
                    <i className={classNames('icon mbc-icon close circle')} />
                  </button>
                ) : (
                  <button
                    className={Styles.fieldModeBtn}
                    disabled={searchTerm.length ? false : true}
                    onClick={onSearchIconButtonClick}
                  >
                    <i className={classNames('icon mbc-icon search')} />
                  </button>
                )
              ) : (
                <button
                  className="search"
                  disabled={searchTerm.length ? false : true}
                  onClick={onSearchIconButtonClick}
                >
                  <i className={classNames('icon mbc-icon search')} />
                </button>
              )}
              {showNoResultsError && <p className={Styles.searchError}>No results found. Please try with Short ID.</p>}
              {showNoUserFoundError && (
                <p className={Styles.searchError}>User details not found. Please provide valid User-ID.</p>
              )}
              {props.userAlreadyExists && (
                <p className={Styles.searchError}>{props.customUserErrorMsg || 'User already exists.'}</p>
              )}
              {!hideSuggestion && searchTerm.length > 0 ? (
                <ul ref={suggestionContainer} className={Styles.suggestionList}>
                  {suggestions}
                </ul>
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
      </div>
      <div
        className={classNames(
          Styles.actionWrapper,
          showUserDetails ? '' : 'hide',
          props.userAlreadyExists && Styles.space100,
        )}
      >
        <div className={classNames(Styles.userInfoWrapper)}>
          <div>
            <i className="icon mbc-icon check circle inline" />
          </div>
          <div>
            {teamMemberObj && (
              <p className={classNames(Styles.userDetails)}>
                {teamMemberObj.givenName} {teamMemberObj.surName} <br />
                User ID: {teamMemberObj.userId} <br />
                profile:{' '}
                {teamMemberObj.profile}
              </p>
            )}
          </div>
        </div>
        <div>
          <button
            className="btn btn-primary"
            onClick={() => {
              props.btnText === 'Add User' && setShowUserDetails(false);
              props.onAddTeamMember(teamMemberObj);
            }}
            type="button"
          >
            {props.btnText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserprivilegeSearch;
