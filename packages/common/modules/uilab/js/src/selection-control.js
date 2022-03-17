/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): selection-control.js
 * --------------------------------------------------------------------------
 */

import { makeArray, getSelectionParent, setRippleAnimation } from './util/index';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class SelectionControl {
  static defaultSetup() {
    // Setup Animation for Selection Controls
    makeArray(document.querySelectorAll('.checkbox input, .radio input, .legend input')).forEach((elem) => {
      setRippleAnimation(elem.parentNode);

      if (elem.type === 'checkbox') {
        elem.addEventListener('change', (e) => {
          const elemWrap = getSelectionParent(e.target);
          if (e.target.checked) {
            elemWrap.classList.add('checked');
          } else {
            elemWrap.classList.remove('checked');
          }
        });
      }
    });
    // Setup Selection Control switch
    makeArray(document.querySelectorAll('.switch input')).forEach((switchElem) => {
      setRippleAnimation(switchElem.parentNode);
      switchElem.addEventListener('change', (e) => {
        const elemWrap = getSelectionParent(e.target);
        if (e.target.checked) {
          elemWrap.classList.add('on');
        } else {
          elemWrap.classList.remove('on');
        }
      });
    });
    // Setup Selection Control legend
    makeArray(document.querySelectorAll('.legend input')).forEach((legendElem) => {
      legendElem.addEventListener('change', (e) => {
        const elemWrap = getSelectionParent(e.target);

        makeArray(document.querySelectorAll('.legend input[name=' + e.target.name + ']')).forEach((elem) => {
          if (!elem.checked) {
            getSelectionParent(elem).classList.remove('active');
          }
        });

        elemWrap.classList.add('active');
      });
    });
  }
}

SelectionControl.defaultSetup();

export default SelectionControl;
