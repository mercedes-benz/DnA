import cn from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './TypeAheadBox.scss';

const classNames = cn.bind(Styles);

interface IList {
  name: string;
  [key: string]: any;
}

export interface IRowItemProps {
  controlId: string;
  label: string | React.ReactNode;
  placeholder: string;
  required: boolean;
  defaultValue: string;
  list: IList[];
  setSelected: (item: any) => void;
  render?: (item: any) => React.ReactNode;
  showError: boolean;
  onInputChange?: (value: any, showSpinner: (val: boolean) => void) => void;
  disabled?: boolean;
}

const TypeAheadBox: React.FC<IRowItemProps> = (props: IRowItemProps) => {
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
  const [selectedItem, setSelectedItem] = useState('');
  const [cursor, setCursor] = useState(-1);
  const [filteredList, setFilteredList] = useState([]);
  const [hideSuggestion, setHideSuggestion] = useState(true);
  const [showNoResultsError, setShowNoResultsError] = useState(false);
  const [errorText, setErrorText] = useState(null);
  const [showSpinner, setShowSpinner] = useState(false);

  useEffect(() => {
    setSelectedItem(props.defaultValue || '');
  }, [props.defaultValue]);

  useEffect(() => {
    if (props.showError) {
      setErrorText('*Missing Entry');
    }
  }, [props.showError]);

  useEffect(() => {
    if (props.onInputChange) {
      setFilteredList(props.list);
      setShowNoResultsError(props.list?.length === 0 ? true : false);
      // setTimeout(() => {
      //   setShowNoResultsError(false);
      // }, 3000);
      setHideSuggestion(props.list?.length === 0);
      setCursor(-1);
    } else {
      setFilteredList(props.list);
    }
  }, [props.list]);

  useEffect(() => {
    // clear errors
    setShowNoResultsError(false);
  }, []);

  const suggestionList = filteredList?.map((item: any, index: number) => {
    return (
      <li
        key={item.id}
        onClick={() => onSuggestionItemClick(index)}
        className={cursor === index ? Styles.active + ' active' : null}
      >
        {props.render ? props.render(item) : item.name}
      </li>
    );
  });

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
    props.onInputChange && props.onInputChange(event.currentTarget.value, (val: boolean) => setShowSpinner(val));
    setShowNoResultsError(false);
    if (event.currentTarget.value.length > 0) {
      setErrorText('');
      setSelectedItem('');
    } else {
      setErrorText('*Missing Entry');
    }
  };

  useEffect(() => {
    if (!props.onInputChange) {
      if (searchTerm.length > 1) {
        const filteredResults = props.list?.filter(
          (item: any) => item.name.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1,
        );
        setFilteredList(filteredResults);
        setShowNoResultsError(filteredResults.length === 0 ? true : false);
        setTimeout(() => {
          setShowNoResultsError(false);
        }, 3000);
        setHideSuggestion(filteredResults.length === 0);
        setCursor(-1);
      } else {
        setFilteredList([]);
      }
    }
  }, [searchTerm]);

  const onSuggestionItemClick = (id: number) => {
    setSelectedItem(filteredList[id].name);
    setSearchTerm('');
    setHideSuggestion(true);
    props.setSelected(filteredList[id]);
  };

  const onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const query = searchTerm;

    if (keyPressed === KEY_CODE.enter && query.length) {
      if (cursor === -1) {
        console.log('');
      } else {
        setHideSuggestion(true);

        if (cursor !== -1 && filteredList.length && !hideSuggestion) {
          setShowNoResultsError(false);
          setCursor(-1);
          setSelectedItem(filteredList[cursor].name);
          setSearchTerm('');
          props.setSelected(filteredList[cursor]);
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
    } else if (keyPressed === KEY_CODE.downArrow && cursor < filteredList.length - 1) {
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
    <div className={classNames(Styles.searchWrapper)}>
      <div
        className={classNames(
          'input-field-group include-error',
          !props.disabled && props.showError && errorText ? 'error' : '',
          props.disabled ? 'disabled' : '',
        )}
      >
        <label htmlFor={props.controlId} className="input-label">
          {props.label}
          {props.required && <span> *</span>}
        </label>

        <div id="searchPanel" className={Styles.searchPanel}>
          <span className={Styles.selectedItem}>{selectedItem}</span>
          <input
            type="text"
            className={'input-field'}
            ref={searchInput}
            id={props.controlId}
            required={props.required}
            value={searchTerm}
            placeholder={selectedItem?.length > 0 ? undefined : props.placeholder}
            onChange={onSearchInputChange}
            onKeyDown={onSearchInputKeyDown}
            maxLength={200}
            autoComplete="off"
            disabled={props.disabled || false}
          />
          {showSpinner && <div className={classNames('progress infinite', Styles.spinner)} />}
          {selectedItem?.length > 0 ? (
            <button
              onClick={() => {
                setSelectedItem('');
                props.setSelected('');
                searchInput.current.focus();
              }}
            >
              <i className={classNames('icon mbc-icon close circle')} />
            </button>
          ) : null}
          {showNoResultsError && <p className={Styles.searchError}>No results found.</p>}
          {!hideSuggestion && searchTerm.length > 0 && (
            <ul ref={suggestionContainer} className={cn(Styles.suggestionList, 'mbc-scroll')}>
              {suggestionList}
            </ul>
          )}
        </div>
        {!props.disabled && props.showError && errorText && <span className="error-message">{errorText}</span>}
      </div>
    </div>
  );
};

export default TypeAheadBox;
