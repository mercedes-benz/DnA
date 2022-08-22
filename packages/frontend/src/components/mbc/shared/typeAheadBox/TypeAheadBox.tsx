import cn from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './TypeAheadBox.scss';

const classNames = cn.bind(Styles);

export interface IRowItemProps {
  label: string;
  placeholder: string;
  list: any;
  defaultValue: string;
  onItemSelect: (entity:any) => void;
  onError?: (error:boolean) => void;
  required: boolean;
  entityError?: boolean;
}

const TypeAheadBox:React.FC<IRowItemProps> = (props: IRowItemProps) => {
  const KEY_CODE = {
    backspace: 8,
    tab: 9,
    enter: 13,
    upArrow: 38,
    downArrow: 40,
  };

  const searchInput = useRef(null);
  const suggestionContainer = useRef(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [cursor, setCursor] = useState(-1);
  const [suggestions, setSuggestions] = useState([]);
  const [hideSuggestion, setHideSuggestion] = useState(true);
  const [showNoResultsError, setShowNoResultsError] = useState(false);
  const [errorText, setErrorText] = useState(null);
  const [isSelected, setIsSelected] = useState(false);

  useEffect(() => {
    setIsSelected(true);
    if(props.defaultValue.length > 0) {
      setSearchTerm(props.defaultValue);
    } else {
      setSearchTerm('');
    }
  }, [props.defaultValue]);

  useEffect(() => {
    if(props.entityError) {
      setErrorText('*Missing Entry');
    }
  }, [props.entityError]);
  
  useEffect(() => {
    setSuggestions(props.list);
  }, [props.list]);
  
  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
    setShowNoResultsError(false);
    setIsSelected(false);
    if(event.currentTarget.value.length > 0) {
      setErrorText('');
    } else {
      setErrorText('*Missing Entry');
      const entity = {
        entityId: '',
        entityName: ''
      }
      props.onItemSelect(entity);
    }
  };

  useEffect(() => {
    if(searchTerm.length > 1) {
      const filteredResults = suggestions.filter((item:any) => {
        const currentEntity = item.entityId + ' - ' + item.entityName;
        return currentEntity.toLowerCase().includes(searchTerm.toLowerCase());
      });
      setSuggestions(filteredResults);
      setShowNoResultsError(filteredResults.length === 0 ? true : false);
      setTimeout(() => {
        setShowNoResultsError(false);
      }, 3000);
      setHideSuggestion(filteredResults.length === 0);
      setCursor(-1);
    } else {
      setSuggestions(props.list);
    }
  }, [searchTerm]);

  const onSuggestionItemClick = (id: number) => {
    setSearchTerm(suggestions[id].entityId + ' - ' + suggestions[id].entityName);
    setHideSuggestion(true);
    setIsSelected(true);
    const entity = {
      entityId: suggestions[id].entityId,
      entityName: suggestions[id].entityName
    }
    props.onItemSelect(entity);
  };

  const onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const query = searchTerm;

    if (keyPressed === KEY_CODE.enter && query.length) {
      
      if(cursor === -1) {
        console.log('');
      } else {
        setHideSuggestion(true);

        if (cursor !== -1 && suggestions.length && !hideSuggestion) {
          setShowNoResultsError(false);
          setCursor(-1);
          setSearchTerm(suggestions[cursor].entityId + ' - ' + suggestions[cursor].entityName);
        }
      }
    } else if (keyPressed === KEY_CODE.upArrow && cursor > 0) {
      setCursor((prevState) => (prevState - 1));
      const containerHeight = suggestionContainer.current.getBoundingClientRect().height;
      const activeElem = suggestionContainer.current.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;
        suggestionContainer.current.scrollTop = 0;
        if(containerHeight < activeElem.offsetTop) {
          suggestionContainer.current.scrollTop = activeElem.offsetTop - activeElemHeight * 2;
        }
      }
    } else if (keyPressed === KEY_CODE.downArrow && cursor < suggestions.length - 1) {
      setCursor((prevState) => (prevState + 1));
      const containerHeight = suggestionContainer.current.getBoundingClientRect().height;
      const activeElem = suggestionContainer.current.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;
        suggestionContainer.current.scrollTop = 0;
        if(containerHeight - 80 < activeElem.offsetTop) {
          suggestionContainer.current.scrollTop = activeElem.offsetTop - activeElemHeight * 1;
        } 
      }
    }
  };
  
  return (
    <div className={classNames(Styles.searchWrapper)}>
      <div className={classNames(
        'input-field-group include-error',
        errorText && 'error',
      )}>
        <label htmlFor="userId" className="input-label">
          {props.label}{props.required && <span>*</span>}
        </label>

        <div id='searchPanel' className={Styles.searchPanel}>
          <input
            type="text"
            className={"input-field"}
            ref={searchInput}
            id="userId"
            required={true}
            value={searchTerm}
            placeholder={props.placeholder}
            onChange={onSearchInputChange}
            onKeyDown={onSearchInputKeyDown}
            maxLength={200}
            autoComplete="off"
          />
          {showNoResultsError && 
            <p className={Styles.searchError}>No results found.</p>
          }
          {!hideSuggestion && searchTerm.length > 0 && !isSelected ? (
            <ul
              ref={suggestionContainer}
              className={Styles.suggestionList}
            >
              {
                suggestions.map((item: any, index: number) => {
                  return (
                    <li
                      key={item.id}
                      onClick={() => onSuggestionItemClick(index)}
                      className={cursor === index ? Styles.active + ' active' : null}
                    >
                      {item.entityId} - {item.entityName}
                    </li>
                  );
                })
              }
            </ul>
          ) : ''}
        </div>
        {
          errorText &&
            <span className="error-message">
              {errorText}
            </span>
        }
      </div>
    </div>
  );
}

export default TypeAheadBox;
