// @ts-ignore
import Select from '../../../assets/modules/uilab/js/src/select';
// @ts-ignore
import SelectionControl from '../../../assets/modules/uilab/js/src/selection-control';

class SelectBox {
  public static defaultSetup(disableEventDispatch?: boolean): void {
    this.refresh();
    Select.defaultSetup();
    SelectionControl.defaultSetup();
    this.setupSelectBoxes(disableEventDispatch);
  }

  public static refresh(selectBoxId?: string): void {
    if (selectBoxId) {
      const selectElement: HTMLSelectElement = document.querySelector(`#${selectBoxId}`);
      if(selectElement) {
        const selectOptions = selectElement?.options;
        const selectElementDIV = selectElement?.nextSibling;
        
        if(selectElementDIV) {
          const selectElementItemsDIV = selectElementDIV?.nextSibling as HTMLDivElement;
          const selectOptionsDiv = selectElementItemsDIV?.querySelectorAll('div');
          if (selectOptions?.length !== selectOptionsDiv?.length) {
            selectElementDIV?.nextSibling && selectElementDIV?.nextSibling.remove();
            selectElementDIV && selectElementDIV.remove();
            Select.defaultSetup([selectElement.parentNode]);
            SelectionControl.defaultSetup();
            this.setupSelectBoxes(true, [selectElement.parentNode]);
          }
        }
      } else {
        console.log('Select Box not found for id - ' + selectBoxId);
      }
    } else {
      const selectElements: NodeListOf<Element> = document.querySelectorAll('.select-selected');
      let elementIndex = 0;
      while (elementIndex < selectElements.length) {
        const element = selectElements[elementIndex];
        element.nextSibling && element.nextSibling.remove();
        element.remove();
        elementIndex++;
      }
    }
  }

  protected static setupSelectBoxes(disableEventDispatch?: boolean, customSelects?: any) {
    /* Code for solving select box and select control issues in firefox */
    if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1) {
      const fixingElements: NodeListOf<Element> = document.querySelectorAll(
        'input[type="checkbox"],input[type="radio"]',
      );

      Array.from(fixingElements).forEach((fixingElement: HTMLInputElement) => {
        fixingElement.className = 'ff-only';
      });
    }

    const selectBoxes: NodeListOf<Element> = customSelects || document.querySelectorAll('.custom-select');
    Array.from(selectBoxes).forEach((selectBox) => {
      // const selectItemVisible: Element = selectBox.querySelector('.select-selected');
      selectBox.setAttribute('tabIndex', '0');
      selectBox.removeEventListener('focus', this.focusSelectBox, true);
      selectBox.addEventListener('focus', this.focusSelectBox, true);
      selectBox.removeEventListener('blur', this.blurSelectBox, true);
      selectBox.addEventListener('blur', this.blurSelectBox, true);
      selectBox.removeEventListener('mouseup', this.mousedownSelectBox);
      selectBox.addEventListener('mouseup', this.mousedownSelectBox);
      const selectItemElements: NodeListOf<Element> = selectBox.querySelectorAll('.select-items');
      Array.from(selectItemElements).forEach((item) => {
        const selectElement: HTMLSelectElement = item.parentNode.querySelector('select');
        item.addEventListener('click', () => {
          this.dispatchReactOnChangeEvent(selectElement);
        });

        if (!disableEventDispatch) {
          this.dispatchReactOnChangeEvent(selectElement);
        }

        if (selectElement.multiple) {
          const allWrapperDiv = item.previousSibling;
          const allCheckBoxWrapperDiv = allWrapperDiv.parentElement.querySelector('.select-selected');

          const selectedOptions = selectElement.selectedOptions;
          const options = selectElement.options;
          const allCheckBox: HTMLInputElement = allCheckBoxWrapperDiv.querySelector('input[type="checkbox"]');

          if (selectedOptions.length && selectedOptions.length === options.length) {
            allCheckBox.checked = true;
          }

          allCheckBox.tabIndex = -1;

          // allCheckBox.removeEventListener('focus', this.focusSelectBox);
          // allCheckBox.addEventListener('focus', this.focusSelectBox);

          // allCheckBox.removeEventListener('blur', this.blurSelectBox);
          // allCheckBox.addEventListener('blur', this.blurSelectBox);

          const labelValues: string[] = [];

          Array.from(options).forEach((option, index) => {
            const optionCheckBox: HTMLInputElement = item
              .querySelector(`div:nth-child(${index + 1})`)
              .querySelector('input[type="checkbox"]');
            if (option.selected) {
              optionCheckBox.checked = true;
              optionCheckBox.parentElement.parentElement.classList.add('checked');
              labelValues.push(option.innerHTML);
            }

            optionCheckBox.tabIndex = -1;

            // optionCheckBox.removeEventListener('focus', this.focusSelectBox);
            // optionCheckBox.addEventListener('focus', this.focusSelectBox);

            // optionCheckBox.removeEventListener('blur', this.blurSelectBox);
            // optionCheckBox.addEventListener('blur', this.blurSelectBox);
          });

          if (labelValues.length) {
            const valuesSpan: HTMLElement = allCheckBoxWrapperDiv.querySelector('.values');
            const allSpan: HTMLElement = allCheckBoxWrapperDiv.querySelector('.checkbox>.label');
            allSpan.style.display = 'none';
            valuesSpan.innerHTML = labelValues.join(', ');
          }

          // Detecting All check box change event
          allWrapperDiv.addEventListener('change', (e) => {
            this.dispatchReactOnChangeEvent(selectElement);
          });
          // Detecting item check box change event
          item.addEventListener('change', (e) => {
            this.dispatchReactOnChangeEvent(selectElement);
          });

          // Code for adding item search input for filttering
          const inputElem = document.createElement('INPUT');
          inputElem.classList.add('input-field', 'ddl-search');
          inputElem.setAttribute('placeholder', 'Search...');

          inputElem.addEventListener('click', (e: Event) => {
            e.stopPropagation();
          });

          inputElem.addEventListener('mousedown', (e: Event) => {
            e.stopPropagation();
          });

          inputElem.addEventListener('change', (e: Event) => {
            e.stopPropagation();
          });

          inputElem.addEventListener('blur', (e: Event) => {
            const searchElem: HTMLInputElement = e.currentTarget as HTMLInputElement;
            this.setClearDDLSearchTimeout(searchElem.parentElement.parentElement);
          });

          inputElem.addEventListener('keyup', (e: Event) => {
            const searchElem: HTMLInputElement = e.currentTarget as HTMLInputElement;
            const searchVal = searchElem.value.toLowerCase();
            const cbWrapperElems = item.querySelectorAll('.checkbox');
            Array.from(cbWrapperElems).forEach((cbWrapperElem) => {
              const labelText = cbWrapperElem.textContent.toLowerCase();
              const hasSearchVal = labelText.includes(searchVal.toLowerCase());
              const cbWrapperElemParent = cbWrapperElem.parentElement;
              cbWrapperElemParent.setAttribute('class', hasSearchVal ? '' : 'hide');
            });
          });
          item.prepend(inputElem);
        } else {
          let selectedOption = item.querySelector(`div.same-as-selected`);
          if (selectedOption) {
            selectedOption.classList.remove('same-as-selected');
          }
          selectedOption = item.querySelector(`div:nth-child(${selectElement.selectedIndex + 1}`);
          if (selectedOption) {
            const selectedLabel = item.parentNode.querySelector('div.select-selected span').textContent;
            Array.from(item.parentNode.querySelector(`div.select-items`)?.childNodes)?.forEach((item:Element) => {
              if (item.textContent === selectedLabel) {
                item.classList.add('same-as-selected')
              }
            })
            // selectedOption.classList.add('same-as-selected');
          }
          // console.log(item.querySelector(`div:nth-child(${selectElement.selectedIndex + 1}`));
        }
      });
    });
  }

  protected static dispatchReactOnChangeEvent(selectElement: HTMLSelectElement) {
    if (!selectElement.multiple) {
      const nativeSelectValueSetter = Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype, 'value').set;
      nativeSelectValueSetter.call(selectElement, selectElement.value);
    }

    const selectEvent = new Event('change', { bubbles: true });
    selectElement.dispatchEvent(selectEvent);
  }

  protected static focusSelectBox(e: any) {
    const fieldGroupDiv = e.target.closest('.input-field-group');
    fieldGroupDiv.classList.add('focused');
  }

  protected static blurSelectBox(e: any) {
    const fieldGroupDiv = e.target.closest('.input-field-group');
    fieldGroupDiv.classList.remove('focused');
    const selectElem = fieldGroupDiv.querySelector('select');
    let errorMsgElem = fieldGroupDiv.querySelector('.error-message');

    let showError = selectElem.selectedIndex === 0;
    if (selectElem.multiple) {
      if (e.currentTarget.tagName === 'INPUT' || e.currentTarget.classList.contains('custom-select')) {
        showError = selectElem.selectedIndex === -1;
      } else if (fieldGroupDiv.classList.contains('error')) {
        showError = true;
      } else if (showError) {
        showError = false;
      }
      SelectBox.setClearDDLSearchTimeout(fieldGroupDiv);
    }
    if (selectElem.hasAttribute('required') && showError) {
      fieldGroupDiv.classList.add('error');

      if (selectElem.hasAttribute('required-error')) {
        const errorMessage = selectElem.getAttribute('required-error');
        if (errorMsgElem) {
          errorMsgElem.innerHTML = errorMessage;
        } else {
          errorMsgElem = document.createElement('SPAN');
          errorMsgElem.classList.add('error-message');
          errorMsgElem.innerHTML = errorMessage;
          fieldGroupDiv.append(errorMsgElem);
        }
      }
    } else {
      fieldGroupDiv.classList.remove('error');
      if (errorMsgElem) {
        fieldGroupDiv.removeChild(errorMsgElem);
      }
    }
  }

  protected static mousedownSelectBox(e: any) {
    setTimeout(() => {
      const fieldGroupDiv = e.target.closest('.input-field-group');
      const selectElem = fieldGroupDiv.querySelector('select');
      const canClearError = selectElem.multiple ? selectElem.selectedIndex >= 0 :  selectElem.selectedIndex > 0;
      if (canClearError) {
        const errorMsgElem = fieldGroupDiv.querySelector('.error-message');
        fieldGroupDiv.classList.remove('error');
        if (errorMsgElem) {
          fieldGroupDiv.removeChild(errorMsgElem);
        }
      }
    }, 10);
  }

  protected static setClearDDLSearchTimeout(wrapperElem: any) {
    setTimeout(() => {
      if (wrapperElem.querySelector('.select-hide')) {
        // Resetting search items on blur
        const inputElem = wrapperElem.querySelector('.ddl-search');
        if (inputElem && inputElem.value) {
          inputElem.value = '';
          const cbWrapperElems = inputElem.parentElement.querySelectorAll('.checkbox') as HTMLDivElement[];
          Array.from(cbWrapperElems).forEach((cbWrapperElem) => {
            cbWrapperElem.parentElement.removeAttribute('class');
          });
        }
      }
    }, 300);
  }
}

export default SelectBox;
