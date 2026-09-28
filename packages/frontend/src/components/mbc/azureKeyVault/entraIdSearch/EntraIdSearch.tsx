import cn from 'classnames';
import React, { useCallback, useEffect, useRef, useState } from 'react';
import { debounce } from 'lodash';
import { IKeyVaultPrincipal, IKeyVaultPrincipalKind } from 'globals/types';
import { ApiClient } from '../../../../services/ApiClient';
import Styles from '../../teamSearch/TeamSearch.scss';

const classNames = cn.bind(Styles);

const KEY_CODE = {
  enter: 13,
  upArrow: 38,
  downArrow: 40,
};

const MIN_SEARCH_LENGTH = 3;
const SEARCH_DEBOUNCE_MS = 500;

export interface EntraIdSearchProps {
  label: React.ReactNode;
  principalType: IKeyVaultPrincipalKind;
  onAddPrincipal: (principal: IKeyVaultPrincipal) => void;
}

/**
 * Entra ID (Microsoft Graph) principal search, modelled on the AddUser/TeamSearch
 * component: debounced type ahead with a suggestion list and keyboard navigation.
 */
const EntraIdSearch = (props: EntraIdSearchProps) => {
  const { principalType, onAddPrincipal } = props;

  const searchInput = useRef(null);
  const suggestionContainer = useRef(null);
  const principalTypeRef = useRef(principalType);
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState<IKeyVaultPrincipal[]>([]);
  const [cursor, setCursor] = useState(-1);
  const [hideSuggestion, setHideSuggestion] = useState(true);
  const [showNoResultsError, setShowNoResultsError] = useState(false);
  const [showSearchError, setShowSearchError] = useState(false);

  const searchPrincipals = (term: string) => {
    ApiClient.searchKeyVaultPrincipals(term, principalTypeRef.current)
      .then((response: IKeyVaultPrincipal[]) => {
        const records = response || [];
        setResults(records);
        setShowSearchError(false);
        setShowNoResultsError(records.length === 0);
        setHideSuggestion(records.length === 0);
        setCursor(-1);
      })
      .catch(() => {
        setResults([]);
        setShowNoResultsError(false);
        setShowSearchError(true);
        setHideSuggestion(true);
        setCursor(-1);
      });
  };

  const debouncedSearch = useCallback(
    debounce((term: string) => {
      searchPrincipals(term);
    }, SEARCH_DEBOUNCE_MS),
    [],
  );

  useEffect(() => {
    principalTypeRef.current = principalType;
    setResults([]);
    setSearchTerm('');
    setHideSuggestion(true);
    setShowNoResultsError(false);
    setShowSearchError(false);
    setCursor(-1);
  }, [principalType]);

  useEffect(() => {
    if (searchTerm.trim().length >= MIN_SEARCH_LENGTH) {
      debouncedSearch(searchTerm.trim());
    } else {
      setResults([]);
      setHideSuggestion(true);
      setShowNoResultsError(false);
      setShowSearchError(false);
    }
  }, [searchTerm]);

  const selectPrincipal = (principal: IKeyVaultPrincipal) => {
    setHideSuggestion(true);
    setShowNoResultsError(false);
    setCursor(-1);
    setSearchTerm('');
    onAddPrincipal(principal);
    searchInput.current?.focus();
  };

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
  };

  const scrollToActiveSuggestion = () => {
    const container = suggestionContainer.current;
    if (!container) {
      return;
    }
    const activeElem = container.querySelector('li.active') as HTMLLIElement;
    if (activeElem) {
      const containerHeight = container.getBoundingClientRect().height;
      const activeElemHeight = activeElem.getBoundingClientRect().height;
      container.scrollTop = 0;
      if (containerHeight < activeElem.offsetTop) {
        container.scrollTop = activeElem.offsetTop - activeElemHeight * 2;
      }
    }
  };

  const onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    if (keyPressed === KEY_CODE.enter) {
      if (cursor !== -1 && results.length && !hideSuggestion) {
        selectPrincipal(results[cursor]);
      }
    } else if (keyPressed === KEY_CODE.upArrow && cursor > 0) {
      setCursor((prevState) => prevState - 1);
      scrollToActiveSuggestion();
    } else if (keyPressed === KEY_CODE.downArrow && cursor < results.length - 1) {
      setCursor((prevState) => prevState + 1);
      scrollToActiveSuggestion();
    }
  };

  const suggestions = results.map((principal, index) => {
    const identifier = principal.identifier || principal.mail || principal.appId || '';
    return (
      <li
        key={principal.id || identifier}
        onClick={() => selectPrincipal(principal)}
        className={cursor === index ? Styles.active + ' active' : null}
      >
        {principal.displayName || identifier}
        {identifier && principal.displayName ? ` - ${identifier}` : ''}
      </li>
    );
  });

  return (
    <div className={Styles.teamSearchContainer}>
      <div className={classNames(Styles.searchWrapper)}>
        <div className={'input-field-group'}>
          <label htmlFor="entraIdSearchField" className="input-label">
            {props.label}
          </label>
          <div id="searchPanel" className={Styles.searchPanel}>
            <input
              type="text"
              className={Styles.searchInputField}
              ref={searchInput}
              id="entraIdSearchField"
              value={searchTerm}
              placeholder="Enter minimum 3 characters of the display name"
              onChange={onSearchInputChange}
              onKeyDown={onSearchInputKeyDown}
              maxLength={200}
              autoComplete="off"
            />
            {showNoResultsError && <p className={Styles.searchError}>No results found in Entra ID.</p>}
            {showSearchError && <p className={Styles.searchError}>Unable to search Entra ID.</p>}
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
  );
};

export default EntraIdSearch;
