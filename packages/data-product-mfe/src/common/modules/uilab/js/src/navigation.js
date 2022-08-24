/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): navigation.js
 * --------------------------------------------------------------------------
 */

import { makeArray } from './util/index';

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

class Navigation {
  constructor(element) {
    this._element = element;
    this.selectActiveLink();
    this._addEventListeners();
  }

  selectActiveLink() {
    makeArray(this._element.querySelectorAll('a[href="' + document.location.pathname + '"]')).forEach((activeLink) => {
      const subNavItem = activeLink.parentNode.parentNode.parentNode;
      if (subNavItem && subNavItem.classList.contains('has-sub-nav')) {
        subNavItem.classList.add('opened');
      }
    });
  }

  collapseAll() {
    makeArray(this._element.querySelectorAll('.has-sub-nav')).forEach((subnavWrapper) => {
      subnavWrapper.classList.remove('opened');
    });
  }

  removeActive() {
    makeArray(this._element.querySelectorAll('.active')).forEach((activeItem) => {
      activeItem.classList.remove('active');
    });
  }

  _addEventListeners() {
    makeArray(this._element.querySelectorAll('.has-sub-nav>.nav-link')).forEach((subnavtoggle) => {
      subnavtoggle.addEventListener('click', (e) => {
        const subNavItem = e.target.parentNode;

        if (subNavItem.classList.contains('opened')) {
          subNavItem.style.height = null;
        } else {
          subNavItem.style.height =
            subNavItem.querySelectorAll('.nav-link').length * (subNavItem.clientHeight + 2) - 1 + 'px';
        }

        subNavItem.classList.toggle('opened');
      });
    });
    makeArray(this._element.querySelectorAll('.nav-item:not(.has-sub-nav)')).forEach((navItem) => {
      navItem.addEventListener('click', (e) => {
        this.removeActive();
        e.target.parentNode.classList.add('active');
      });
    });
  }
}

export default Navigation;
