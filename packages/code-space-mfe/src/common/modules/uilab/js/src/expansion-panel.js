/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): expansion-panel.js
 * --------------------------------------------------------------------------
 */

import { makeArray, getSelectionParent, setRippleAnimation } from './util/index';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class ExpansionPanel {
  static defaultSetup() {
    // Setup Animation for Expansion Controls
    makeArray(document.querySelectorAll('.expansion-panel input')).forEach((elem) => {
      setRippleAnimation(elem.parentNode);
      if (elem.type === 'checkbox') {
        elem.addEventListener('change', (e) => {
          const elemWrap = getSelectionParent(e.target);
          const tooltipElement = document.querySelector('.tooltip');
          const isExpandCollapseLabel = /(expand|collapse)/gi.test(tooltipElement?.textContent);
          if (e.target.checked) {
            elemWrap.classList.add('open');
            if (isExpandCollapseLabel) tooltipElement.innerText = 'Collapse';
          } else {
            elemWrap.classList.remove('open');
            if (isExpandCollapseLabel) tooltipElement.innerText = 'Expand';
          }
        });
      }
    });
  }
}

ExpansionPanel.defaultSetup();

export default ExpansionPanel;
