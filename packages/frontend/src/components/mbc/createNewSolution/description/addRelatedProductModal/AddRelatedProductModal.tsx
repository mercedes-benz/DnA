import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import { IRelatedProduct } from 'globals/types';
// import { ApiClient } from '../../../../../services/ApiClient';
// @ts-ignore
import InputFieldsUtils from 'components/formElements/InputFields/InputFieldsUtils';
import Modal from 'components/formElements/modal/Modal';
import Tags from 'components/formElements/tags/Tags';
import Styles from './AddRelatedProductModal.scss';
const classNames = cn.bind(Styles);

export interface IAddRelatedProductModalProps {
  modalTitleText?: string;
  fieldTitleText?: string;
  max: number;
  chips: string[];
  showAddRelatedProductModal: boolean;
  relatedProduct: IRelatedProduct[];
  showOnlyInteral?: boolean;
  onAddRelatedProductModalCancel: () => void;
  onRelatedProductChangeUpdate: (arr: string[]) => void;
}

export interface IAddRelatedProductModalState {
  relatedProductObj: IRelatedProduct[];
  name: string;
  id: string;
  KEY?: any;
  filteredRelatedProducts: IRelatedProduct[];
  chips: string[];
  tags: string[];
  max: number;
  userInput?: string;
  activeSuggestionIndex: number;
  isFocused: boolean;
}

export default class AddRelatedProductModal extends React.Component<
  IAddRelatedProductModalProps,
  IAddRelatedProductModalState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      name: '',
      id: '',
      relatedProductObj: this.props.relatedProduct,
      tags: [],
      chips: [],
      KEY: {
        backspace: 8,
        tab: 9,
        enter: 13,
        upArrow: 38,
        downArrow: 40,
      },
      max: this.props.max,
      filteredRelatedProducts: [],
      userInput: '',
      activeSuggestionIndex: -1,
      isFocused: false,
    };
  }

  /*public componentDidMount() {
      this.setChips(this.props.chips);
    } */

  public render() {
    const addRelatedProductModalContent: React.ReactNode = (
      <div id="relatedProductModalDiv" className={classNames(Styles.firstPanel, Styles.addRelatedProductModal)}>
        <div className={Styles.formWrapper}>
          <div>
            <div className={classNames(Styles.relatedProductModalParentdiv, Styles.searchWrapper)}>
              <Tags
                title={this.props.fieldTitleText ? this.props.fieldTitleText : 'Related Products'}
                max={100}
                chips={this.state.chips}
                setTags={this.setTags}
                // setTags1={this.setTags1}
                isMandatory={false}
                showMissingEntryError={false}
                tags={[]}
              />
            </div>
          </div>

          {this.state.chips.length > 0 && (
            <div className={classNames(Styles.flexLayout, Styles.actionWrapper)}>
              <button className={'btn btn-primary'} onClick={this.addRelatedProduct} type="button">
                Confirm
              </button>
            </div>
          )}
        </div>
      </div>
    );

    return (
      <Modal
        title={this.props.modalTitleText ? this.props.modalTitleText : 'Add related products'}
        showAcceptButton={false}
        showCancelButton={false}
        buttonAlignment="right"
        show={this.props.showAddRelatedProductModal}
        content={addRelatedProductModalContent}
        onCancel={this.onModalCancel}
      />
    );
  }
  protected setTags = (arr: string[]) => {
    this.setState({
      chips: arr,
    });
  };

  protected addRelatedProduct = () => {
    const chips = this.state.chips;
    this.setState(
      {
        chips: [],
      },
      () => {
        this.props.onAddRelatedProductModalCancel();
        this.props.onRelatedProductChangeUpdate(chips);
      },
    );
  };
  protected onModalCancel = () => {
    this.setState(
      {
        chips: [],
      },
      () => {
        this.props.onAddRelatedProductModalCancel();
      },
    );
  };
  // protected onRelatedProductFieldFocus = () => {
  //   this.setState({ isFocused: true });
  // };

  // protected onRelatedProductFieldBlur = () => {
  //   this.setState({ isFocused: false });
  // };

  /*
    protected setChips = (chips: string[]) => {
      if (chips && chips.length) {
        this.setState({ chips });
      }
    };
    */
  /*
  protected onSuggestionClick = (event: React.ChangeEvent) => {
    const target = event.currentTarget as HTMLElement;
    const userInput = target.innerText;
    if (target.id && target.id !== '0') {
      this.setState({
        userInput,
      });
      this.updateChips(userInput);
    }
  }
  */
  /*
  protected onTextInputChange = (event: React.ChangeEvent) => {
    const target = event.target as HTMLInputElement;
    const userInput = target.value;
    const relatedProduct = this.props.relatedProduct;

    if (userInput) {
      let filteredRelatedProducts = relatedProduct.filter(
        relatedProduct => relatedProduct.name.toLowerCase().indexOf(userInput.toLowerCase()) > -1,
      );
      if (filteredRelatedProducts.length === 0) {
        filteredRelatedProducts = [{ id: '0', name: 'No suggestions available' }];
      }
      this.setState({
        filteredRelatedProducts,
        userInput,
      });
    } else {
      this.setState({
        filteredRelatedProducts: [],
        userInput,
        activeSuggestionIndex: -1,
      });
    }
  };
  */
  /*
  
    protected updateChips = (value: string) => {
      if (!this.props.max || this.state.chips.length < this.props.max) {
        // const value = this.state.userInput;
  
        if (!value) {
          return;
        }
  
        const chip = value.trim();
  
        if (chip && this.state.chips.indexOf(chip) < 0) {
          const chips = this.state.chips;
          chips.push(chip);
          this.props.setRelatedProducts(chips);
          this.setState({
            chips,
          });
        }
      }
      // target.value = '';
  
      this.setState({
        userInput: '',
        filteredRelatedProducts: [],
        activeSuggestionIndex: -1,
      });
    }; */
}
