/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): tooltip.js
 * --------------------------------------------------------------------------
 */

import { makeArray } from './util/index';
import Popper from 'popper.js';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class Tooltip {
  static defaultSetup() {
    makeArray(document.querySelectorAll('[tooltip-data]')).forEach((elem) => {
      let popper = null;
      elem.addEventListener('mouseover', () => {
        const message = elem.getAttribute('tooltip-data');
        const tooltipElement = document.createElement('DIV');
        tooltipElement.classList.add('tooltip');
        tooltipElement.innerText = message;
        document.body.append(tooltipElement);
        setTimeout(() => {
          tooltipElement.classList.add('show');
        }, 20);
        popper = new Popper(elem, tooltipElement, { placement: 'top' });
      });

      elem.addEventListener('mouseout', () => {
        const elem = document.querySelector('.tooltip');
        elem.parentNode.removeChild(elem);
        popper.destroy();
      });
    });
  }
}

Tooltip.defaultSetup();

export default Tooltip;
