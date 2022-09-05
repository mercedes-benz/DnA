import cn from 'classnames';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { debounce } from 'lodash';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { ITeams } from '../../../globals/types';
import { TeamMemberType } from '../../../globals/Enums';
import { ApiClient } from '../../../services/ApiClient';
import Styles from './TeamSearch.scss';

const classNames = cn.bind(Styles);

export interface TeamSearchProps {
  label: any;
  editMode?: boolean;
  userId?: string;
  teamMemberObj?: ITeams;
  onAddTeamMember: (teamMemberObj: ITeams) => void;
  userAlreadyExists?: boolean;
  resetUserAlreadyExists?: () => void;
  btnText: string;
  searchTerm: string;
  setSearchTerm: (val: string) => void;
  showUserDetails: boolean;
  setShowUserDetails: (val: boolean) => void;
}

const TeamSearch = (props: TeamSearchProps) => {
  const KEY_CODE = {
    backspace: 8,
    tab: 9,
    enter: 13,
    upArrow: 38,
    downArrow: 40,
  };

  const { searchTerm, setSearchTerm, showUserDetails, setShowUserDetails } = props;

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
        key={item.id}
        onClick={() => onSuggestionItemClick(index)}
        className={cursor === index ? Styles.active + ' active' : null}
      >
        {item.lastName}, {item.firstName} - {item.email}
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
    if (searchTerm.length > 1) {
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
    ApiClient.getUsersBySearchTerm(searchTerm)
      .then((response) => {
        if (response) {
          if (response.records !== undefined) {
            setResults(response.records);
            setShowNoResultsError(response.records.length === 0 ? true : false);
            setHideSuggestion(response.records.length === 0);
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
    const teamMemberObj: ITeams = {
      department: results[shortId].department,
      email: results[shortId].email,
      firstName: results[shortId].firstName,
      shortId: results[shortId].id,
      lastName: results[shortId].lastName,
      mobileNumber: results[shortId].mobileNumber !== '' ? results[shortId].mobileNumber : '',
      userType: TeamMemberType.INTERNAL,
    };
    setTeamMemberObj(teamMemberObj);
    setShowNoResultsError(false);
    setShowUserDetails(true);
    setSearchTerm('');
    searchInput.current.focus();
  };

  const dRDUserSearch = () => {
    ProgressIndicator.show();

    ApiClient.getDRDUserInfo(searchTerm)
      .then((data: any) => {
        const teamMemberObj: ITeams = {
          department: data.department,
          email: data.email,
          firstName: data.firstName,
          shortId: data.id,
          lastName: data.lastName,
          mobileNumber: data.mobileNumber !== '' ? data.mobileNumber : '',
          userType: TeamMemberType.INTERNAL,
        };

        setTeamMemberObj(teamMemberObj);
        setShowUserDetails(true);
        setHideSuggestion(true);
        setShowNoResultsError(false);
        setShowNoUserFoundError(false);

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
          const teamMemberObj: ITeams = {
            department: results[cursor].department,
            email: results[cursor].email,
            firstName: results[cursor].firstName,
            shortId: results[cursor].id,
            lastName: results[cursor].lastName,
            mobileNumber: results[cursor].mobileNumber !== '' ? results[cursor].mobileNumber : '',
            userType: TeamMemberType.INTERNAL,
          };

          setTeamMemberObj(teamMemberObj);
          setShowUserDetails(true);
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
                className={classNames(Styles.searchInputField)}
                ref={searchInput}
                id="userId"
                value={searchTerm}
                placeholder="Short ID, first name, last name, email"
                onChange={onSearchInputChange}
                onKeyDown={onSearchInputKeyDown}
                maxLength={200}
                autoComplete="off"
              />
              <button disabled={searchTerm.length ? false : true} onClick={onSearchIconButtonClick}>
                <i className={classNames('icon mbc-icon search')} />
              </button>
              {showNoResultsError && <p className={Styles.searchError}>No results found. Please try with Short ID.</p>}
              {showNoUserFoundError && (
                <p className={Styles.searchError}>User details not found. Please provide valid User-ID.</p>
              )}
              {props.userAlreadyExists && <p className={Styles.searchError}>User already exists.</p>}
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
                {teamMemberObj.firstName} {teamMemberObj.lastName} <br />
                AD ID: {teamMemberObj.shortId} | Email: {teamMemberObj.email} <br />
                Department:{' '}
                {teamMemberObj.department === '' || teamMemberObj.department === null
                  ? 'NA'
                  : teamMemberObj.department}{' '}
                | Mobile Number:{' '}
                {teamMemberObj.mobileNumber === '' || teamMemberObj.mobileNumber === null
                  ? 'NA'
                  : teamMemberObj.mobileNumber}
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

export default TeamSearch;
