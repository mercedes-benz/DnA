/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): InputFields.js
 * --------------------------------------------------------------------------
 */

import { makeArray, getSelectionStart } from './util/index';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class InputFields {
  static defaultSetup() {
    function validateFloatKeyPress(el, evt) {
      const charCode = evt.which ? evt.which : evt.keyCode;
      const number = el.value.split('.');
      if (charCode !== 46 && charCode > 31 && (charCode < 48 || charCode > 57)) {
        return false;
      }

      // just one dot (thanks ddlab)
      if (number.length > 1 && charCode === 46) {
        return false;
      }

      // get the carat position
      const caratPos = getSelectionStart(el);
      const dotPos = el.value.indexOf('.');
      if (caratPos > dotPos && dotPos > -1 && number[1].length > 1) {
        return false;
      }

      return true;
    }

    function updateNumericValue(inputElem, decrease) {
      const stepValueText = inputElem.getAttribute('step-value');
      const stepValue = parseFloat(stepValueText);
      inputElem.focus();
      let numericValue = parseFloat(inputElem.value || 0);
      if (decrease) {
        numericValue -= stepValue;
      } else {
        numericValue += stepValue;
      }

      let decimalFixing = 0;
      if (stepValueText.indexOf('.') !== -1) {
        decimalFixing = stepValueText.split('.')[1].length;
      }

      inputElem.value = numericValue.toFixed(decimalFixing);
    }

    // Setup InputFields click animation
    makeArray(document.querySelectorAll('.input-field, .input-field-area')).forEach((inputElem) => {
      inputElem.addEventListener('focus', (evt) => {
        evt.target.parentNode.classList.add('focused');
      });

      inputElem.addEventListener('blur', (evt) => {
        const elem = evt.target;
        const elemWrapper = elem.parentNode;
        elemWrapper.classList.remove('focused');
        let errorMsgElem = elemWrapper.querySelector('.error-message');

        if (elem.hasAttribute('required') && elem.value.trim() === '') {
          elemWrapper.classList.add('error');

          if (elem.hasAttribute('required-error')) {
            const errorMessage = elem.getAttribute('required-error');
            if (errorMsgElem) {
              errorMsgElem.innerHTML = errorMessage;
            } else {
              errorMsgElem = document.createElement('SPAN');
              errorMsgElem.classList.add('error-message');
              errorMsgElem.innerHTML = errorMessage;
              elemWrapper.append(errorMsgElem);
            }
          }
        } else {
          elemWrapper.classList.remove('error');
          if (errorMsgElem) {
            elemWrapper.removeChild(errorMsgElem);
          }
        }
      });

      if (inputElem.classList.contains('numeric')) {
        const elemWrapper = inputElem.parentNode;
        const stepValue = inputElem.hasAttribute('step-value');

        if (stepValue) {
          inputElem.classList.add('has-step-value');
          const upArrow = document.createElement('I');
          const downArrow = document.createElement('I');
          upArrow.classList.add('icon', 'arrow', 'up');
          downArrow.classList.add('icon', 'arrow', 'down');
          const triggerWrapper = document.createElement('DIV');
          triggerWrapper.classList.add('trigger-wrapper');
          triggerWrapper.style.top = inputElem.offsetTop + 'px';
          triggerWrapper.append(upArrow);
          triggerWrapper.append(downArrow);
          elemWrapper.append(triggerWrapper);
          const intervalTimeout = 300;
          let intervalId;
          let pressedTime;

          downArrow.addEventListener('mousedown', (evt) => {
            evt.preventDefault();
            pressedTime = new Date();
            intervalId = setInterval(() => {
              updateNumericValue(inputElem, true);
            }, intervalTimeout);
          });

          upArrow.addEventListener('mousedown', (evt) => {
            evt.preventDefault();
            pressedTime = new Date();
            intervalId = setInterval(() => {
              updateNumericValue(inputElem);
            }, intervalTimeout);
          });

          upArrow.addEventListener('mouseup', () => {
            clearInterval(intervalId);
          });

          downArrow.addEventListener('mouseup', () => {
            clearInterval(intervalId);
          });

          window.addEventListener('mouseup', () => {
            clearInterval(intervalId);
          });

          downArrow.addEventListener('click', () => {
            if (new Date().getTime() - pressedTime.getTime() < intervalTimeout) {
              updateNumericValue(inputElem, true);
            }
          });

          upArrow.addEventListener('click', () => {
            if (new Date().getTime() - pressedTime.getTime() < intervalTimeout) {
              updateNumericValue(inputElem);
            }
          });
        }

        inputElem.addEventListener('keypress', (evt) => {
          if (!validateFloatKeyPress(evt.target, evt)) {
            evt.preventDefault();
          }
        });
      }
    });
  }

  static refresh() {
    makeArray(document.querySelectorAll('.has-step-value')).forEach((inputStepElem) => {
      inputStepElem.parentNode.querySelector('.trigger-wrapper').style.top = inputStepElem.offsetTop + 'px';
    });
  }
}

InputFields.defaultSetup();

export default InputFields;
