/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): tooltip.js
 * --------------------------------------------------------------------------
 */

import { makeArray, getSelectionParent } from './util/index';
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
      elem.addEventListener('mouseover', (e) => {
        const message = elem.getAttribute('tooltip-data');
        let tooltipElement;
        if (!document.querySelector('.tooltip')) {
          tooltipElement = document.createElement('DIV');
          tooltipElement.classList.add('tooltip');
          tooltipElement.innerText = message;
          // for expansion panel
          const expansionLabel = getSelectionParent(e.target);
          const expansionPanel = getSelectionParent(expansionLabel);
          if (expansionPanel.classList.contains('expansion-panel') && expansionPanel.classList.contains('open')) {
            if (message === 'Expand') {
              if (tooltipElement) tooltipElement.innerText = 'Collapse';
            }
          }
          document.body.append(tooltipElement);
          setTimeout(() => {
            tooltipElement?.classList.add('show');
          }, 20);
          popper = new Popper(elem, tooltipElement, { placement: 'top' });
        }
      });

      elem.addEventListener('mouseout', () => {
        const elem = document.querySelector('.tooltip');
        if (elem) elem.parentNode?.removeChild(elem);
        if (popper) popper.destroy();
      });
      elem.addEventListener('click', () => {
        const elem = document.querySelector('.tooltip');
        if (elem) elem.parentNode?.removeChild(elem);
        if (popper) popper.destroy();
      });
    });
  }

  static clear() {
    document.querySelectorAll('.tooltip.show').forEach((elem) => elem.remove());
  }
}

Tooltip.defaultSetup();

export default Tooltip;
