import cn from 'classnames';
// import { debounce } from 'lodash';
import React, {useState, useEffect, useRef} from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { history } from '../../../router/History';
import { getParams } from '../../../router/RouterUtils';
import { ApiClient } from '../../../services/ApiClient';
import Styles from './HeaderSearchBox.scss';

const classNames = cn.bind(Styles);

export interface ISearchSolutionResultItem {
  id: string;
  productName: string;
}

export interface ISearchSolutionResponse {
  records: ISearchSolutionResultItem[];
  totalCount: number;
}

export interface IHeaderSearchBoxProps {
  show?: boolean;
  onClose?: () => void;
}

const HeaderSearchBox:React.FC<IHeaderSearchBoxProps> = (props) => {
  // const searchInput: HTMLInputElement = useRef();
  // const suggestionContainer: HTMLUListElement = useRef();
  const searchInput:any = useRef(null);
  const suggestionContainer:any = useRef(null);

  const KEY_CODE = {
    backspace: 8,
    tab: 9,
    enter: 13,
    upArrow: 38,
    downArrow: 40,
  };

  const [show, setShow] = useState<boolean>(false);
  const [hideSuggestion, setHideSuggestion] = useState<boolean>(false);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [results, setResults] = useState<ISearchSolutionResultItem[]>([]);
  const [maxItemsPerPage] = useState<number>(10);
  const [totalNumberOfPages, setTotalNumberOfPages] = useState<number>(0);
  // const [currentPageNumber, setCurrentPageNumber] = useState<number>(1);
  const [currentPageOffset] = useState<number>(0);
  const [cursor, setCursor] = useState<number>(-1);

  useEffect(() => {
    const params = getParams();
    let query = params ? params.query : null;
    if (query) {
      query = query.split('?page=')[0]; // For getting only search query from param without page info
      setSearchTerm(decodeURI(query));
      setShow(true);
    }
  }, []);

  const suggestions = results.map((item: ISearchSolutionResultItem, index: number) => {
    const cursorTemp = cursor;
    return (
      <li
        key={item.id}
        // tslint:disable-next-line: jsx-no-lambda
        onClick={() => onSuggestionItemClick(item.id)}
        className={cursorTemp === index ? Styles.active + ' active' : null}
      >
        {item.productName}
      </li>
    );
  });

  const onSearchIconButtonClick = () => {
    const query = searchTerm;
    const initiateSearch = show && query.length;
    if (initiateSearch) {
      setHideSuggestion(true);
      history.push('/search/' + encodeURI(query));

    } else {
      setShow(!show);
      // searchInput.focus();
    }
  };

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
  };

  useEffect(() => {
    if (searchTerm && searchTerm.length > 1) {
      getSolutionsInfoBySearchTerm();
    } else {
      setResults([]);
    }
  }, [searchTerm]);

  const onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const query = searchTerm;
    if (keyPressed === KEY_CODE.enter && query.length) {
      setHideSuggestion(true);
      if (cursor !== -1 && results.length && !hideSuggestion) {
        gotoSolution(results[cursor].id);
      } else {
        history.push('/search/' + query);
      }
    } else if (keyPressed === KEY_CODE.upArrow && cursor > 0) {
      setCursor(cursor - 1);
    } else if (keyPressed === KEY_CODE.downArrow && cursor < results.length - 1) {
      setCursor(cursor + 1);
    }

    if (!hideSuggestion && (keyPressed === KEY_CODE.upArrow || keyPressed === KEY_CODE.downArrow)) {
      const containerHeight = suggestionContainer.getBoundingClientRect().height;
      const containerScrollTop = suggestionContainer.scrollTop;
      const activeElem = suggestionContainer.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;

        if (containerHeight < activeElem.offsetTop) {
          suggestionContainer.scrollTop = containerScrollTop + activeElemHeight * 2;
        } else if (containerScrollTop) {
          suggestionContainer.scrollTop = containerScrollTop - activeElemHeight * 2;
        }
      }
    }
  };

  const onSuggestionItemClick = (solutionId: string) => {
    setHideSuggestion(true);
    gotoSolution(solutionId);
  };
  
  const getSolutionsInfoBySearchTerm = () => {
    ApiClient.getSolutionsBySearchTerm(searchTerm, 10, currentPageOffset)
      .then((response) => {
        if (response) {
          const searchSolutionResponse = response.data.solutions as ISearchSolutionResponse;
          setResults(searchSolutionResponse.totalCount ? searchSolutionResponse.records : []);
          setHideSuggestion(searchSolutionResponse.totalCount === 0);
          setCursor(-1);
          setTotalNumberOfPages(Math.ceil(searchSolutionResponse.totalCount / maxItemsPerPage));
          console.log(totalNumberOfPages);
        }
      })
      .catch((err: Error) => {
        Notification.show('Error getting search suggestions.', 'alert');
      });
  };

  const gotoSolution = (solutionId: string) => {
    history.push('/summary/' + solutionId);
  };

  return (
    <div id='searchPanel' className={Styles.searchPanel}>
      <input
        type="text"
        className={classNames(Styles.searchInputField)}
        ref={searchInput}
        // placeholder="Solution title / tag"
        placeholder="What are you looking for today?"
        onChange={onSearchInputChange}
        onKeyDown={onSearchInputKeyDown}
        maxLength={200}
        value={searchTerm}
      />
      <button onClick={onSearchIconButtonClick}>
        <i className={classNames('icon mbc-icon search', show ? Styles.active : '')} />
      </button>
      {!hideSuggestion && searchTerm.length > 0 ? (
        <ul
          ref={suggestionContainer}
          className={Styles.suggestionList}
        >
          {suggestions}
        </ul>
      ) : (
        ''
      )}
    </div>
  );
}

export default HeaderSearchBox;
