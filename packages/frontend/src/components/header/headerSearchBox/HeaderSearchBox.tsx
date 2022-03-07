import cn from 'classnames';
// import { debounce } from 'lodash';
import * as React from 'react';
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

export interface IHeaderSearchBoxState {
  show: boolean;
  hideSuggestion: boolean;
  searchTerm: string;
  results: ISearchSolutionResultItem[];
  maxItemsPerPage: number;
  totalNumberOfPages: number;
  currentPageNumber: number;
  currentPageOffset: number;
  cursor: number;
}

export class HeaderSearchBox extends React.Component<IHeaderSearchBoxProps, IHeaderSearchBoxState> {
  protected searchInput: HTMLInputElement;
  protected suggestionContainer: HTMLUListElement;

  protected KEY_CODE = {
    backspace: 8,
    tab: 9,
    enter: 13,
    upArrow: 38,
    downArrow: 40,
  };

  public constructor(props: IHeaderSearchBoxProps, context?: any) {
    super(props, context);

    this.state = {
      show: false,
      hideSuggestion: false,
      searchTerm: '',
      results: [],
      maxItemsPerPage: 10,
      totalNumberOfPages: 0,
      currentPageNumber: 1,
      currentPageOffset: 0,
      cursor: -1,
    };
  }

  public componentDidMount() {
    const params = getParams();
    let query = params ? params.query : null;
    if (query) {
      query = query.split('?page=')[0]; // For getting only search query from param without page info
      this.setState({ searchTerm: decodeURI(query), show: true });
    }
  }

  public render() {
    const suggestions = this.state.results.map((item: ISearchSolutionResultItem, index: number) => {
      const cursor = this.state.cursor;
      return (
        <li
          key={item.id}
          // tslint:disable-next-line: jsx-no-lambda
          onClick={() => this.onSuggestionItemClick(item.id)}
          className={cursor === index ? Styles.active + ' active' : null}
        >
          {item.productName}
        </li>
      );
    });

    const canShow = this.state.show;
    const hideSuggestion = this.state.hideSuggestion;

    return (
      <div id='searchPanel' className={Styles.searchPanel}>
        <input
          type="text"
          className={classNames(Styles.searchInputField, canShow ? '' : Styles.hide)}
          ref={(searchInput) => {
            this.searchInput = searchInput;
          }}
          placeholder="Solution title / tag"
          onChange={this.onSearchInputChange}
          onKeyDown={this.onSearchInputKeyDown}
          maxLength={200}
          value={this.state.searchTerm}
        />
        <button onClick={this.onSearchIconButtonClick}>
          <i className={classNames('icon mbc-icon search', canShow ? Styles.active : '')} />
        </button>
        {canShow && !hideSuggestion ? (
          <ul
            ref={(suggestionContainer) => {
              this.suggestionContainer = suggestionContainer;
            }}
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

  protected onSearchIconButtonClick = () => {
    const query = this.searchInput.value;
    const initiateSearch = this.state.show && query.length;
    if (initiateSearch) {
      this.setState(
        {
          hideSuggestion: true,
        },
        () => {
          history.push('/search/' + encodeURI(query));
        },
      );
    } else {
      this.setState(
        {
          show: !this.state.show,
        },
        () => {
          this.searchInput.focus();
        },
      );
    }
  };

  protected onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    this.setState(
      {
        searchTerm: event.currentTarget.value,
      },
      () => {
        const searchTerm = this.state.searchTerm;
        if (searchTerm && searchTerm.length > 1) {
          this.getSolutionsInfoBySearchTerm();
        } else {
          this.setState({ results: [] });
        }
      },
    );
  };

  protected onSearchInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const keyPressed = event.which;
    const query = this.searchInput.value;
    const { cursor, results, hideSuggestion } = this.state;
    if (keyPressed === this.KEY_CODE.enter && query.length) {
      this.setState({ hideSuggestion: true }, () => {
        if (cursor !== -1 && results.length && !hideSuggestion) {
          this.gotoSolution(results[cursor].id);
        } else {
          history.push('/search/' + query);
        }
      });
    } else if (keyPressed === this.KEY_CODE.upArrow && cursor > 0) {
      this.setState((prevState) => ({
        cursor: prevState.cursor - 1,
      }));
    } else if (keyPressed === this.KEY_CODE.downArrow && cursor < results.length - 1) {
      this.setState((prevState) => ({
        cursor: prevState.cursor + 1,
      }));
    }

    if (!hideSuggestion && (keyPressed === this.KEY_CODE.upArrow || keyPressed === this.KEY_CODE.downArrow)) {
      const containerHeight = this.suggestionContainer.getBoundingClientRect().height;
      const containerScrollTop = this.suggestionContainer.scrollTop;
      const activeElem = this.suggestionContainer.querySelector('li.active') as HTMLLIElement;
      if (activeElem) {
        const activeElemHeight = activeElem.getBoundingClientRect().height;

        if (containerHeight < activeElem.offsetTop) {
          this.suggestionContainer.scrollTop = containerScrollTop + activeElemHeight * 2;
        } else if (containerScrollTop) {
          this.suggestionContainer.scrollTop = containerScrollTop - activeElemHeight * 2;
        }
      }
    }
  };

  protected onSuggestionItemClick = (solutionId: string) => {
    this.setState({ hideSuggestion: true }, () => {
      this.gotoSolution(solutionId);
    });
  };

  protected getSolutionsInfoBySearchTerm = () => {
    ApiClient.getSolutionsBySearchTerm(this.state.searchTerm, 10, this.state.currentPageOffset)
      .then((response) => {
        if (response) {
          const searchSolutionResponse = response.data.solutions as ISearchSolutionResponse;
          this.setState({
            results: searchSolutionResponse.totalCount ? searchSolutionResponse.records : [],
            hideSuggestion: searchSolutionResponse.totalCount === 0,
            cursor: -1,
            totalNumberOfPages: Math.ceil(searchSolutionResponse.totalCount / this.state.maxItemsPerPage),
          });
        }
      })
      .catch((err: Error) => {
        Notification.show('Error getting search suggestions.', 'alert');
      });
  };

  protected gotoSolution = (solutionId: string) => {
    history.push('/summary/' + solutionId);
  };
}
