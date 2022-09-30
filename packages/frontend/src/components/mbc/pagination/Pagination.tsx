import * as React from 'react';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import Styles from './Pagination.scss';

export interface IPaginationProps {
  onPreviousClick: () => void;
  onNextClick: () => void;
  onViewByNumbers?: (pageNo: number) => void;
  pageNumber: number;
  totalPages: number;
  displayByPage?: boolean;
}

export interface IPaginationState {
  selectedPageNumber: number;
}

export default class Pagination extends React.PureComponent<IPaginationProps, IPaginationState> {
  constructor(props: IPaginationProps) {
    super(props);
    this.state = {
      selectedPageNumber:
        parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
    };
  }
  /* tslint:disable:jsx-no-lambda */
  public render(): JSX.Element {
    return (
      <div id="pagination-container" className={Styles.container}>
        <button
          className="btn btn-text back arrow"
          disabled={this.props.pageNumber === 1}
          type="button"
          id="dropdownMenuButton"
          onClick={this.props.onPreviousClick}
        >
          Previous
        </button>
        {this.props.displayByPage ? (
          <span>
            <span className={Styles.pageNumebr}>{`${this.props.pageNumber}/${this.props.totalPages}`}</span>
            <h2 className={Styles.spaceLine} />
            {'show '}
            <a
              className={
                this.state.selectedPageNumber === 15 ? Styles.pageNumLevel + ' ' + Styles.active : Styles.pageNumLevel
              }
              target="_blank"
              onClick={this.fetchData(15)}
            >
              {'15 '}
            </a>
            <a
              className={
                this.state.selectedPageNumber === 30 ? Styles.pageNumLevel + ' ' + Styles.active : Styles.pageNumLevel
              }
              target="_blank"
              onClick={this.fetchData(30)}
            >
              {'30 '}
            </a>
            <a
              className={
                this.state.selectedPageNumber === 60 ? Styles.pageNumLevel + ' ' + Styles.active : Styles.pageNumLevel
              }
              target="_blank"
              onClick={this.fetchData(60)}
            >
              {'60'}
            </a>
          </span>
        ) : (
          <span>{`${this.props.pageNumber}/${this.props.totalPages}`}</span>
        )}
        <button
          className="btn btn-text next arrow"
          disabled={this.props.pageNumber === this.props.totalPages}
          type="button"
          id="dropdownMenuButton"
          onClick={this.props.onNextClick}
        >
          Next
        </button>
      </div>
    );
  }
  protected fetchData = (val: number) => () => {
    sessionStorage.setItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE, val.toString());
    this.props.onViewByNumbers(val);
    this.setState({
      selectedPageNumber: val,
    });
  };
}
