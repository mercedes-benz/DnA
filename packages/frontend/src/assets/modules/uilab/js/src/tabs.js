import { setRippleAnimation, makeArray } from './util/index';
import InputFields from './input-fields';

class Tabs {
  constructor(element) {
    this._element = element;
  }

  static defaultSetup() {
    const tabs = document.querySelectorAll('.tabs');

    makeArray(tabs).forEach((tabsElem) => {
      const tabItems = tabsElem.querySelectorAll('.tab');

      const activeIndicator = document.createElement('SPAN');
      activeIndicator.setAttribute('class', 'active-indicator');

      makeArray(tabItems).forEach((tabElem) => {
        tabElem.addEventListener('click', setActiveClass);

        if (tabElem.classList.contains('active')) {
          moveActiveIndicator(tabsElem, tabElem, activeIndicator);
          showActiveTabContent(tabElem, tabsElem.parentNode.parentNode.parentNode);
        }

        setRippleAnimation(tabElem.firstChild, false, 'rgba(192, 200, 208, 1)');
      });

      if (tabsElem.scrollWidth > tabsElem.clientWidth) {
        const tabPanel = tabsElem.parentNode.parentNode;
        const arrowLeftElem = document.createElement('BUTTON');
        arrowLeftElem.setAttribute('class', 'previous');
        arrowLeftElem.innerHTML = '<i class="icon arrow left"></i>';
        tabPanel.prepend(arrowLeftElem);
        const arrowRightElem = document.createElement('BUTTON');
        arrowRightElem.setAttribute('class', 'next');
        arrowRightElem.innerHTML = '<i class="icon arrow right"></i>';
        tabPanel.append(arrowRightElem);
        tabPanel.classList.add('scrollable');
        setRippleAnimation(arrowLeftElem, true, 'rgba(0, 173, 239, 1)');
        setRippleAnimation(arrowRightElem, true, 'rgba(0, 173, 239, 1)');
        arrowLeftElem.addEventListener('click', scrollTabs);
        arrowRightElem.addEventListener('click', scrollTabs);
        let timerId = 0;
        tabsElem.addEventListener('scroll', (e) => {
          clearInterval(timerId);
          timerId = setTimeout(() => {
            manageScrollTriggers(e.target, arrowLeftElem, arrowRightElem);
          }, 100);
        });

        manageScrollTriggers(tabsElem, arrowLeftElem, arrowRightElem);
      }

      tabsElem.appendChild(activeIndicator);
    });

    function setActiveClass(evt) {
      evt.preventDefault();
      const tabElem = evt.currentTarget;
      const tabsElem = tabElem.parentNode;
      const curTabs = tabsElem.querySelectorAll('.tab');
      makeArray(curTabs).forEach((tab) => {
        tab.classList.remove('active');
      });

      moveActiveIndicator(tabsElem, tabElem);

      tabElem.classList.add('active');

      showActiveTabContent(tabElem, tabsElem.parentNode.parentNode.parentNode);
    }

    function scrollTabs(evt) {
      const arrowElem = evt.currentTarget;
      const tabNavElem = arrowElem.parentNode;
      const tabsElem = tabNavElem.querySelector('.tabs');
      let leftScrollVal = 0;
      if (arrowElem.classList.contains('next')) {
        leftScrollVal = tabsElem.scrollLeft + tabsElem.clientWidth;
      } else {
        leftScrollVal = tabsElem.scrollLeft - tabsElem.clientWidth;
      }

      tabsElem.scroll({
        left: leftScrollVal,
        behavior: 'smooth',
      });
    }

    function manageScrollTriggers(tabsElem, arrowLeftElem, arrowRightElem) {
      arrowLeftElem.classList.remove('hide');
      arrowRightElem.classList.remove('hide');
      if (!tabsElem.scrollLeft) {
        arrowLeftElem.classList.add('hide');
      } else if (tabsElem.scrollWidth - tabsElem.scrollLeft === tabsElem.clientWidth) {
        arrowRightElem.classList.add('hide');
      }
    }

    function moveActiveIndicator(tabsElem, tabElem, activeIndicator) {
      let timeOut = 0;
      if (activeIndicator) {
        timeOut = 500;
      } else {
        activeIndicator = tabsElem.querySelector('.active-indicator');
      }

      const tabsClientRect = tabsElem.getBoundingClientRect();
      const tabClientRect = tabElem.getBoundingClientRect();
      const leftScrollVal = tabClientRect.x - tabsClientRect.x + tabsElem.scrollLeft;
      activeIndicator.style.width = tabClientRect.width + 'px';
      activeIndicator.style.left = leftScrollVal + 'px';

      let leftScrollCorrection = 0;
      let correctedTabsElemLeftScrollVal = 0;
      if (tabClientRect.left < tabsClientRect.left) {
        leftScrollCorrection = parseInt(tabsClientRect.left - tabClientRect.left, 10);
        correctedTabsElemLeftScrollVal = tabsElem.scrollLeft - leftScrollCorrection;
      } else if (tabClientRect.left + tabClientRect.width > tabsClientRect.left + tabsClientRect.width) {
        leftScrollCorrection = parseInt(
          tabClientRect.left + tabClientRect.width - (tabsClientRect.left + tabsClientRect.width),
          10,
        );
        correctedTabsElemLeftScrollVal = leftScrollCorrection ? tabsElem.scrollLeft + leftScrollCorrection : 0;
      }

      if (leftScrollCorrection) {
        setTimeout(() => {
          tabsElem.scroll({
            left: correctedTabsElemLeftScrollVal,
            behavior: 'smooth',
          });

          if (timeOut) {
            moveActiveIndicator(tabsElem, tabElem);
          }
        }, timeOut);
      }
    }

    function showActiveTabContent(activeTab, tabPanel) {
      const tabContents = tabPanel.querySelectorAll('.tab-content');
      makeArray(tabContents).forEach((tabContent) => {
        tabContent.classList.remove('active');
      });
      const tabId = activeTab.querySelector('a').getAttribute('href');
      if (tabId) {
        tabPanel.querySelector(tabId).classList.add('active');
      }

      InputFields.refresh();
    }
  }
}
Tabs.defaultSetup();

export default Tabs;
