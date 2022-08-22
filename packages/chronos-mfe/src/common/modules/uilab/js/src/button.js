/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): button.js
 * --------------------------------------------------------------------------
 */

import { makeArray, setRippleAnimation } from './util/index';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class Button {
  static defaultSetup() {
    // Setup Button click animation
    makeArray(document.querySelectorAll('.btn:not(.btn-text)')).forEach((btnElem) => {
      if (btnElem.classList.contains('btn-secondary')) {
        setRippleAnimation(btnElem, false, 'rgba(192, 200, 208, 1)');
      } else if (btnElem.classList.contains('btn-icon-circle')) {
        setRippleAnimation(btnElem);
      } else {
        setRippleAnimation(btnElem, false);
      }
    });
  }
}

Button.defaultSetup();

export default Button;
