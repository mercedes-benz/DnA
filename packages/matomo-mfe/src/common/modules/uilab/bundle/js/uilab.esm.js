/*!
 * UiLab v1.0.0
 * Copyright 2011-2020 Daimler
 */
import Popper from 'popper.js';

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): util/index.js
 * --------------------------------------------------------------------------
 */
var makeArray = function makeArray(nodeList) {
  if (!nodeList) {
    return [];
  }

  return [].slice.call(nodeList);
};

var getRGB = function getRGB(rgbColorString) {
  var match = rgbColorString.match(/rgba?\((\d{1,3}), ?(\d{1,3}), ?(\d{1,3})\)?(?:, ?(\d(?:\.\d?))\))?/);
  return match
    ? {
        red: match[1],
        green: match[2],
        blue: match[3],
      }
    : {};
};

var getSelectionParent = function getSelectionParent(inputElem) {
  var elemWrap = inputElem.parentNode;

  if (elemWrap.classList.contains('wrapper')) {
    elemWrap = elemWrap.parentNode;
  }

  return elemWrap;
};

var setRippleAnimation = function setRippleAnimation(elem, center, rbgaColor) {
  if (center === void 0) {
    center = true;
  }

  var mouseInPressedState = false;
  elem.addEventListener('mousedown', function (e) {
    var nodeName = e.target.nodeName;

    if (nodeName !== 'INPUT' && nodeName !== 'I' && nodeName !== 'BUTTON' && nodeName !== 'A') {
      return;
    }

    e.stopPropagation();
    mouseInPressedState = true;
    var el = elem;
    var animationWrapper = elem.querySelector('.animation-wrapper');

    if (animationWrapper) {
      el = animationWrapper;
    }

    var clientRect = el.getBoundingClientRect();
    var x = clientRect.width / 2;
    var y = clientRect.height / 2;

    if (!center) {
      x = e.pageX - (clientRect.x + window.pageXOffset);
      y = e.pageY - (clientRect.y + window.pageYOffset);
    }

    var duration = 750;
    var offsetDuration = 300;
    var animationStartOpacity = 0.3;
    var opacityLevel = 1.5;
    var stopLevel = 2.5;
    var animationFrame;
    var animationStart;
    var rgb = getRGB(rbgaColor || window.getComputedStyle(el).color);

    var animationStep = function animationStep(timestamp) {
      if (!animationStart) {
        animationStart = timestamp;
      }

      var frame = timestamp - animationStart;
      var circle = 'circle at ' + x + 'px ' + y + 'px';
      var color = 'rgba(' + rgb.red + ', ' + rgb.green + ', ' + rgb.blue + ',' + animationStartOpacity + ')';
      var stop = '100%';

      if (frame < duration - offsetDuration) {
        var easing = (frame / duration) * (stopLevel - frame / duration);
        var opacityEasing = mouseInPressedState
          ? animationStartOpacity
          : animationStartOpacity * (opacityLevel - easing);
        color = 'rgba(' + rgb.red + ', ' + rgb.green + ', ' + rgb.blue + ', ' + opacityEasing + ')';
        stop = 90 * easing + '%';
        el.style.backgroundImage =
          'radial-gradient(' + circle + ', ' + color + ' ' + stop + ', transparent ' + stop + ')';
        animationFrame = window.requestAnimationFrame(animationStep);
      } else {
        if (mouseInPressedState) {
          el.style.backgroundImage =
            'radial-gradient(' + circle + ', ' + color + ' ' + stop + ', transparent ' + stop + ')';
        } else {
          el.style.backgroundImage = 'none';
        }

        window.cancelAnimationFrame(animationFrame);
      }
    };

    animationFrame = window.requestAnimationFrame(animationStep);
  });

  var removeAnimation = function removeAnimation() {
    var el = elem;
    var animationWrapper = elem.querySelector('.animation-wrapper');

    if (animationWrapper) {
      el = animationWrapper;
    }

    el.style.backgroundImage = 'none';
    mouseInPressedState = false;
  };

  elem.addEventListener('mouseup', removeAnimation);
  elem.addEventListener('mouseleave', removeAnimation);
};

var getSelectionStart = function getSelectionStart(elem) {
  if (elem.createTextRange) {
    var r = document.selection.createRange().duplicate();
    r.moveEnd('character', elem.value.length);

    if (r.text === '') {
      return elem.value.length;
    }

    return elem.value.lastIndexOf(r.text);
  }

  return elem.selectionStart;
};

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): navigation.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var Navigation = /*#__PURE__*/ (function () {
  function Navigation(element) {
    this._element = element;
    this.selectActiveLink();

    this._addEventListeners();
  }

  var _proto = Navigation.prototype;

  _proto.selectActiveLink = function selectActiveLink() {
    makeArray(this._element.querySelectorAll('a[href="' + document.location.pathname + '"]')).forEach(function (
      activeLink,
    ) {
      var subNavItem = activeLink.parentNode.parentNode.parentNode;

      if (subNavItem && subNavItem.classList.contains('has-sub-nav')) {
        subNavItem.classList.add('opened');
      }
    });
  };

  _proto.collapseAll = function collapseAll() {
    makeArray(this._element.querySelectorAll('.has-sub-nav')).forEach(function (subnavWrapper) {
      subnavWrapper.classList.remove('opened');
    });
  };

  _proto.removeActive = function removeActive() {
    makeArray(this._element.querySelectorAll('.active')).forEach(function (activeItem) {
      activeItem.classList.remove('active');
    });
  };

  _proto._addEventListeners = function _addEventListeners() {
    var _this = this;

    makeArray(this._element.querySelectorAll('.has-sub-nav>.nav-link')).forEach(function (subnavtoggle) {
      subnavtoggle.addEventListener('click', function (e) {
        var subNavItem = e.target.parentNode;

        if (subNavItem.classList.contains('opened')) {
          subNavItem.style.height = null;
        } else {
          subNavItem.style.height =
            subNavItem.querySelectorAll('.nav-link').length * (subNavItem.clientHeight + 2) - 1 + 'px';
        }

        subNavItem.classList.toggle('opened');
      });
    });
    makeArray(this._element.querySelectorAll('.nav-item:not(.has-sub-nav)')).forEach(function (navItem) {
      navItem.addEventListener('click', function (e) {
        _this.removeActive();

        e.target.parentNode.classList.add('active');
      });
    });
  };

  return Navigation;
})();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): button.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var Button = /*#__PURE__*/ (function () {
  function Button() {}

  Button.defaultSetup = function defaultSetup() {
    // Setup Button click animation
    makeArray(document.querySelectorAll('.btn:not(.btn-text)')).forEach(function (btnElem) {
      if (btnElem.classList.contains('btn-secondary')) {
        setRippleAnimation(btnElem, false, 'rgba(192, 200, 208, 1)');
      } else if (btnElem.classList.contains('btn-icon-circle')) {
        setRippleAnimation(btnElem);
      } else {
        setRippleAnimation(btnElem, false);
      }
    });
  };

  return Button;
})();

Button.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): select.js
 * --------------------------------------------------------------------------
 */

var Select = /*#__PURE__*/ (function () {
  function Select() {}

  Select.defaultSetup = function defaultSetup() {
    var i;
    var j;
    var selElmnt;
    var a;
    var b;
    var c;
    /* Look for any elements with the class 'custom-select': */

    var x = document.getElementsByClassName('custom-select');

    for (i = 0; i < x.length; i++) {
      selElmnt = x[i].getElementsByTagName('select')[0];
      /* For each element, create a new DIV that will act as the selected item: */

      a = document.createElement('DIV');
      var labelWrapper = document.createElement('SPAN');
      labelWrapper.setAttribute('class', 'label');
      var isMultiSelect = selElmnt.hasAttribute('multiple');
      /* For each element, create a new DIV that will contain the option list: */

      b = document.createElement('DIV');

      if (isMultiSelect) {
        var valuesSpan = document.createElement('SPAN');
        valuesSpan.setAttribute('class', 'values');
        a.appendChild(valuesSpan);
        a.setAttribute('class', 'select-selected multiple');
        b.setAttribute('class', 'select-items multiple select-hide');
        labelWrapper.innerHTML =
          '<label class="checkbox"><span class="wrapper"><input type="checkbox" value="label"/></span><span class="label">All</span></label>';
      } else {
        a.setAttribute('class', 'select-selected');
        b.setAttribute('class', 'select-items select-hide');
        labelWrapper.innerHTML = selElmnt.options[selElmnt.selectedIndex].innerHTML;
      }

      a.appendChild(labelWrapper);
      var animationWrapper = document.createElement('SPAN');
      animationWrapper.setAttribute('class', 'animation-wrapper');
      a.appendChild(animationWrapper);
      var iconElem = document.createElement('I');
      iconElem.setAttribute('class', 'icon down-up-flip');
      a.appendChild(iconElem);
      x[i].appendChild(a);
      setRippleAnimation(a.parentNode);

      for (j = 0; j < selElmnt.length; j++) {
        /* For each option in the original select element,
        create a new DIV that will act as an option item: */
        c = document.createElement('DIV');
        var optionLabel = selElmnt.options[j].innerHTML;
        var optionValue = selElmnt.options[j].value;

        if (isMultiSelect) {
          c.innerHTML =
            '<label class="checkbox"><span class="wrapper"><input type="checkbox" data-index="' +
            j +
            '" value="' +
            optionValue +
            '"/></span><span class="label">' +
            optionLabel +
            '</span></label>';
          c.addEventListener('click', function (e) {
            e.stopPropagation();
            var optionsWrapper = this.parentNode;
            var allCheckBoxElem = optionsWrapper.previousSibling.querySelector('.checkbox');

            if (e.target.nodeName === 'INPUT') {
              var allOptions = optionsWrapper.querySelectorAll('input[type="checkbox"]').length;
              var checkedOptions = optionsWrapper.querySelectorAll('input[type="checkbox"]:checked').length;
              var allCB = allCheckBoxElem.querySelector('input[type="checkbox"]');
              var selectElem = optionsWrapper.parentNode.querySelector('select');
              allCB.checked = allOptions === checkedOptions;

              if (allCB.checked) {
                allCheckBoxElem.classList.add('checked');
              } else {
                allCheckBoxElem.classList.remove('checked');
              }

              selectElem.options[e.target.getAttribute('data-index')].selected = e.target.checked;
            }
          });
        } else {
          if (j === 0) {
            c.setAttribute('class', 'same-as-selected');
          }

          c.innerHTML = optionLabel;
          c.addEventListener('click', function () {
            /* When an item is clicked, update the original select box,
            and the selected item: */
            var y;
            var i;
            var k;
            var s = this.parentNode.parentNode.getElementsByTagName('select')[0];
            var h = this.parentNode.previousSibling.childNodes[0];

            for (i = 0; i < s.length; i++) {
              if (s.options[i].innerHTML === this.innerHTML) {
                s.selectedIndex = i;
                h.innerHTML = this.innerHTML;
                y = this.parentNode.getElementsByClassName('same-as-selected');

                for (k = 0; k < y.length; k++) {
                  y[k].removeAttribute('class');
                }

                this.setAttribute('class', 'same-as-selected');
                break;
              }
            }

            h.click();
          });
        }

        b.appendChild(c);
      }

      x[i].appendChild(b);
      a.addEventListener('click', function (e) {
        /* When the select box is clicked, close any other select boxes,
        and open/close the current select box: */
        var selectWrapper = e.target.parentNode;
        var optionsWrapper = this.nextSibling;

        if (selectWrapper.nodeName === 'DIV') {
          e.stopPropagation();
          closeAllSelect(this);
          this.nextSibling.classList.toggle('select-hide');
          this.classList.toggle('open');
        } else {
          e.stopPropagation();

          if (e.target.nodeName === 'INPUT') {
            var selectElem = optionsWrapper.parentNode.querySelector('select');
            makeArray(optionsWrapper.querySelectorAll('.checkbox')).forEach(function (checkboxElem) {
              var cb = checkboxElem.querySelector('input[type="checkbox"]');

              if (e.target.checked) {
                checkboxElem.classList.add('checked');
              } else {
                checkboxElem.classList.remove('checked');
              }

              cb.checked = e.target.checked;
              selectElem.options[cb.getAttribute('data-index')].selected = e.target.checked;
            });
          }
        } // Code for mutiple select

        if (this.classList.contains('multiple')) {
          manageMultiSelectValues(this);
        }
      });
    }

    function manageMultiSelectValues(selectElem) {
      var checkedOptions = selectElem.nextSibling.querySelectorAll('.checkbox.checked');
      var valuesSpan = selectElem.querySelector('.values');
      var allSpan = selectElem.querySelector('.checkbox>.label');
      var optionsTextArr = [];
      makeArray(checkedOptions).forEach(function (checkboxElem) {
        optionsTextArr.push(checkboxElem.querySelector('.label').innerHTML);
      });

      if (optionsTextArr.length && !selectElem.classList.contains('open')) {
        valuesSpan.innerHTML = optionsTextArr.join(', ');
        allSpan.style.display = 'none';
      } else {
        valuesSpan.innerHTML = '';
        allSpan.style.display = '';
      }
    }

    function closeAllSelect(elmnt) {
      /* A function that will close all select boxes in the document,
      except the current select box: */
      var i;
      var arrNo = [];
      var x = document.getElementsByClassName('select-items');
      var y = document.getElementsByClassName('select-selected');

      for (i = 0; i < y.length; i++) {
        var el = y[i];

        if (elmnt === el) {
          arrNo.push(i);
        } else {
          el.classList.remove('open');
        } // Code for mutiple select

        if (el.classList.contains('multiple')) {
          manageMultiSelectValues(el);
        }
      }

      for (i = 0; i < x.length; i++) {
        if (arrNo.indexOf(i)) {
          x[i].classList.add('select-hide');
        }
      }
    }
    /* If the user clicks anywhere outside the select box,
    then close all select boxes: */

    document.addEventListener('click', closeAllSelect);
  };

  return Select;
})();

Select.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): selection-control.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var SelectionControl = /*#__PURE__*/ (function () {
  function SelectionControl() {}

  SelectionControl.defaultSetup = function defaultSetup() {
    // Setup Animation for Selection Controls
    makeArray(document.querySelectorAll('.checkbox input, .radio input, .legend input')).forEach(function (elem) {
      setRippleAnimation(elem.parentNode);

      if (elem.type === 'checkbox') {
        elem.addEventListener('change', function (e) {
          var elemWrap = getSelectionParent(e.target);

          if (e.target.checked) {
            elemWrap.classList.add('checked');
          } else {
            elemWrap.classList.remove('checked');
          }
        });
      }
    }); // Setup Selection Control switch

    makeArray(document.querySelectorAll('.switch input')).forEach(function (switchElem) {
      setRippleAnimation(switchElem.parentNode);
      switchElem.addEventListener('change', function (e) {
        var elemWrap = getSelectionParent(e.target);

        if (e.target.checked) {
          elemWrap.classList.add('on');
        } else {
          elemWrap.classList.remove('on');
        }
      });
    }); // Setup Selection Control legend

    makeArray(document.querySelectorAll('.legend input')).forEach(function (legendElem) {
      legendElem.addEventListener('change', function (e) {
        var elemWrap = getSelectionParent(e.target);
        makeArray(document.querySelectorAll('.legend input[name=' + e.target.name + ']')).forEach(function (elem) {
          if (!elem.checked) {
            getSelectionParent(elem).classList.remove('active');
          }
        });
        elemWrap.classList.add('active');
      });
    });
  };

  return SelectionControl;
})();

SelectionControl.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): InputFields.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var InputFields = /*#__PURE__*/ (function () {
  function InputFields() {}

  InputFields.defaultSetup = function defaultSetup() {
    function validateFloatKeyPress(el, evt) {
      var charCode = evt.which ? evt.which : evt.keyCode;
      var number = el.value.split('.');

      if (charCode !== 46 && charCode > 31 && (charCode < 48 || charCode > 57)) {
        return false;
      } // just one dot (thanks ddlab)

      if (number.length > 1 && charCode === 46) {
        return false;
      } // get the carat position

      var caratPos = getSelectionStart(el);
      var dotPos = el.value.indexOf('.');

      if (caratPos > dotPos && dotPos > -1 && number[1].length > 1) {
        return false;
      }

      return true;
    }

    function updateNumericValue(inputElem, decrease) {
      var stepValueText = inputElem.getAttribute('step-value');
      var stepValue = parseFloat(stepValueText);
      inputElem.focus();
      var numericValue = parseFloat(inputElem.value || 0);

      if (decrease) {
        numericValue -= stepValue;
      } else {
        numericValue += stepValue;
      }

      var decimalFixing = 0;

      if (stepValueText.indexOf('.') !== -1) {
        decimalFixing = stepValueText.split('.')[1].length;
      }

      inputElem.value = numericValue.toFixed(decimalFixing);
    } // Setup InputFields click animation

    makeArray(document.querySelectorAll('.input-field, .input-field-area')).forEach(function (inputElem) {
      inputElem.addEventListener('focus', function (evt) {
        evt.target.parentNode.classList.add('focused');
      });
      inputElem.addEventListener('blur', function (evt) {
        var elem = evt.target;
        var elemWrapper = elem.parentNode;
        elemWrapper.classList.remove('focused');
        var errorMsgElem = elemWrapper.querySelector('.error-message');

        if (elem.hasAttribute('required') && elem.value.trim() === '') {
          elemWrapper.classList.add('error');

          if (elem.hasAttribute('required-error')) {
            var errorMessage = elem.getAttribute('required-error');

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
        var elemWrapper = inputElem.parentNode;
        var stepValue = inputElem.hasAttribute('step-value');

        if (stepValue) {
          inputElem.classList.add('has-step-value');
          var upArrow = document.createElement('I');
          var downArrow = document.createElement('I');
          upArrow.classList.add('icon', 'arrow', 'up');
          downArrow.classList.add('icon', 'arrow', 'down');
          var triggerWrapper = document.createElement('DIV');
          triggerWrapper.classList.add('trigger-wrapper');
          triggerWrapper.style.top = inputElem.offsetTop + 'px';
          triggerWrapper.append(upArrow);
          triggerWrapper.append(downArrow);
          elemWrapper.append(triggerWrapper);
          var intervalTimeout = 300;
          var intervalId;
          var pressedTime;
          downArrow.addEventListener('mousedown', function (evt) {
            evt.preventDefault();
            pressedTime = new Date();
            intervalId = setInterval(function () {
              updateNumericValue(inputElem, true);
            }, intervalTimeout);
          });
          upArrow.addEventListener('mousedown', function (evt) {
            evt.preventDefault();
            pressedTime = new Date();
            intervalId = setInterval(function () {
              updateNumericValue(inputElem);
            }, intervalTimeout);
          });
          upArrow.addEventListener('mouseup', function () {
            clearInterval(intervalId);
          });
          downArrow.addEventListener('mouseup', function () {
            clearInterval(intervalId);
          });
          window.addEventListener('mouseup', function () {
            clearInterval(intervalId);
          });
          downArrow.addEventListener('click', function () {
            if (new Date().getTime() - pressedTime.getTime() < intervalTimeout) {
              updateNumericValue(inputElem, true);
            }
          });
          upArrow.addEventListener('click', function () {
            if (new Date().getTime() - pressedTime.getTime() < intervalTimeout) {
              updateNumericValue(inputElem);
            }
          });
        }

        inputElem.addEventListener('keypress', function (evt) {
          if (!validateFloatKeyPress(evt.target, evt)) {
            evt.preventDefault();
          }
        });
      }
    });
  };

  InputFields.refresh = function refresh() {
    makeArray(document.querySelectorAll('.has-step-value')).forEach(function (inputStepElem) {
      inputStepElem.parentNode.querySelector('.trigger-wrapper').style.top = inputStepElem.offsetTop + 'px';
    });
  };

  return InputFields;
})();

InputFields.defaultSetup();

var Tabs = /*#__PURE__*/ (function () {
  function Tabs(element) {
    this._element = element;
  }

  Tabs.defaultSetup = function defaultSetup() {
    var tabs = document.querySelectorAll('.tabs');
    makeArray(tabs).forEach(function (tabsElem) {
      var tabItems = tabsElem.getElementsByClassName('tab');
      var activeIndicator = document.createElement('SPAN');
      activeIndicator.setAttribute('class', 'active-indicator');
      makeArray(tabItems).forEach(function (tabElem) {
        tabElem.addEventListener('click', setActiveClass);

        if (tabElem.classList.contains('active')) {
          moveActiveIndicator(tabsElem, tabElem, activeIndicator);
          showActiveTabContent(tabElem, tabsElem.parentNode.parentNode.parentNode);
        }

        setRippleAnimation(tabElem.firstChild, false, 'rgba(192, 200, 208, 1)');
      });

      if (tabsElem.scrollWidth > tabsElem.clientWidth) {
        var tabPanel = tabsElem.parentNode.parentNode;
        var arrowLeftElem = document.createElement('BUTTON');
        arrowLeftElem.setAttribute('class', 'previous');
        arrowLeftElem.innerHTML = '<i class="icon arrow left"></i>';
        tabPanel.prepend(arrowLeftElem);
        var arrowRightElem = document.createElement('BUTTON');
        arrowRightElem.setAttribute('class', 'next');
        arrowRightElem.innerHTML = '<i class="icon arrow right"></i>';
        tabPanel.append(arrowRightElem);
        tabPanel.classList.add('scrollable');
        setRippleAnimation(arrowLeftElem, true, 'rgba(0, 173, 239, 1)');
        setRippleAnimation(arrowRightElem, true, 'rgba(0, 173, 239, 1)');
        arrowLeftElem.addEventListener('click', scrollTabs);
        arrowRightElem.addEventListener('click', scrollTabs);
        var timerId = 0;
        tabsElem.addEventListener('scroll', function (e) {
          clearInterval(timerId);
          timerId = setTimeout(function () {
            manageScrollTriggers(e.target, arrowLeftElem, arrowRightElem);
          }, 100);
        });
        manageScrollTriggers(tabsElem, arrowLeftElem, arrowRightElem);
      }

      tabsElem.appendChild(activeIndicator);
    });

    function setActiveClass(evt) {
      evt.preventDefault();
      var tabElem = evt.currentTarget;
      var tabsElem = tabElem.parentNode;
      var curTabs = tabsElem.querySelectorAll('.tab');
      makeArray(curTabs).forEach(function (tab) {
        tab.classList.remove('active');
      });
      moveActiveIndicator(tabsElem, tabElem);
      tabElem.classList.add('active');
      showActiveTabContent(tabElem, tabsElem.parentNode.parentNode.parentNode);
    }

    function scrollTabs(evt) {
      var arrowElem = evt.currentTarget;
      var tabNavElem = arrowElem.parentNode;
      var tabsElem = tabNavElem.querySelector('.tabs');
      var leftScrollVal = 0;

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
      var timeOut = 0;

      if (activeIndicator) {
        timeOut = 500;
      } else {
        activeIndicator = tabsElem.querySelector('.active-indicator');
      }

      var tabsClientRect = tabsElem.getBoundingClientRect();
      var tabClientRect = tabElem.getBoundingClientRect();
      var leftScrollVal = tabClientRect.x - tabsClientRect.x + tabsElem.scrollLeft;
      activeIndicator.style.width = tabClientRect.width + 'px';
      activeIndicator.style.left = leftScrollVal + 'px';
      var leftScrollCorrection = 0;
      var correctedTabsElemLeftScrollVal = 0;

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
        setTimeout(function () {
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
      var tabContents = tabPanel.querySelectorAll('.tab-content');
      makeArray(tabContents).forEach(function (tabContent) {
        tabContent.classList.remove('active');
      });
      var tabId = activeTab.querySelector('a').getAttribute('href');

      if (tabId) {
        tabPanel.querySelector(tabId).classList.add('active');
      }

      InputFields.refresh();
    }
  };

  return Tabs;
})();

Tabs.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): expansion-panel.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var ExpansionPanel = /*#__PURE__*/ (function () {
  function ExpansionPanel() {}

  ExpansionPanel.defaultSetup = function defaultSetup() {
    // Setup Animation for Expansion Controls
    makeArray(document.querySelectorAll('.expansion-panel input')).forEach(function (elem) {
      setRippleAnimation(elem.parentNode);

      if (elem.type === 'checkbox') {
        elem.addEventListener('change', function (e) {
          var elemWrap = getSelectionParent(e.target);

          if (e.target.checked) {
            elemWrap.classList.add('open');
          } else {
            elemWrap.classList.remove('open');
          }
        });
      }
    });
  };

  return ExpansionPanel;
})();

ExpansionPanel.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): notification.js
 * --------------------------------------------------------------------------
 */

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */
var Notification = /*#__PURE__*/ (function () {
  function Notification() {}

  Notification.show = function show(message, type, hideDelay) {
    if (type === void 0) {
      type = 'success';
    }

    if (hideDelay === void 0) {
      hideDelay = 6000;
    }

    var notificationElement = document.createElement('DIV');
    var messageElement = document.createElement('P');
    var closeElement = document.createElement('I');
    closeElement.classList.add('icon', 'close');
    notificationElement.classList.add('notification', 'toast');

    if (type) {
      notificationElement.classList.add(type);
    }

    messageElement.innerText = message;
    notificationElement.append(messageElement);
    notificationElement.append(closeElement);
    closeElement.addEventListener('click', function (evt) {
      evt.target.parentNode.classList.add('hide');
    });
    notificationElement.addEventListener('transitionend', function (evt) {
      var notificationElement = evt.target;
      var notificationWrapper = notificationElement.parentNode;

      if (notificationWrapper && notificationElement.classList.contains('hide')) {
        notificationWrapper.removeChild(notificationElement);

        if (!notificationWrapper.childNodes.length) {
          notificationWrapper.parentNode.removeChild(notificationWrapper);
        }

        if (notificationWrapper.childNodes.length > 3) {
          notificationWrapper.firstChild.classList.remove('show');
          notificationWrapper.firstChild.classList.add('hide');
        }
      }
    });
    var wrapper = document.getElementById('notification-wrapper');

    if (wrapper) {
      wrapper.append(notificationElement);

      if (wrapper.childNodes.length > 3) {
        wrapper.firstChild.classList.add('hide');
      }
    } else {
      var notificationWrapper = document.createElement('DIV');
      notificationWrapper.setAttribute('id', 'notification-wrapper');
      notificationWrapper.classList.add('notification-wrapper');
      notificationWrapper.append(notificationElement);
      document.body.append(notificationWrapper);
    }

    setTimeout(function () {
      notificationElement.classList.add('show');

      if (wrapper && wrapper.childNodes.length > 3) {
        notificationElement.classList.add('delay');
      }
    }, 20);

    if (wrapper && wrapper.childNodes.length > 3) {
      setTimeout(function () {
        notificationElement.classList.remove('delay');
      }, 100);
    }

    setTimeout(function () {
      notificationElement.classList.add('hide');
    }, hideDelay);
  };

  return Notification;
})();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): tooltip.js
 * --------------------------------------------------------------------------
 */
/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */

var Tooltip = /*#__PURE__*/ (function () {
  function Tooltip() {}

  Tooltip.defaultSetup = function defaultSetup() {
    makeArray(document.querySelectorAll('[tooltip-data]')).forEach(function (elem) {
      var popper = null;
      elem.addEventListener('mouseover', function () {
        var message = elem.getAttribute('tooltip-data');
        var tooltipElement = document.createElement('DIV');
        tooltipElement.classList.add('tooltip');
        tooltipElement.innerText = message;
        document.body.append(tooltipElement);
        setTimeout(function () {
          tooltipElement.classList.add('show');
        }, 20);
        popper = new Popper(elem, tooltipElement, {
          placement: 'top',
        });
      });
      elem.addEventListener('mouseout', function () {
        var elem = document.querySelector('.tooltip');
        elem.parentNode.removeChild(elem);
        popper.destroy();
      });
    });
  };

  return Tooltip;
})();

Tooltip.defaultSetup();

/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): progress-indicator.js
 * --------------------------------------------------------------------------
 */

/**
 * ------------------------------------------------------------------------
 * Constants
 * ------------------------------------------------------------------------
 */
var PROGRESS_RING_RADIUS = 22.5;
var PROGRESS_RING_X_Y = 25;
var PROGRESS_BLOCK_WRAPPER_CLASS = 'progress-block-wrapper';
var PROGRESS_CLASS = 'progress';
var PROGRESS_INFINITE_CLASS = 'infinite';
var PROGRESS_DETERMINITE_CLASS = 'determinite';

var ProgressIndicator = /*#__PURE__*/ (function () {
  function ProgressIndicator(element) {
    this._element = element;
    this.setupProgressRing();
  }

  var _proto = ProgressIndicator.prototype;

  _proto.setupProgressRing = function setupProgressRing() {
    var svgCircle =
      '<circle class="progress-ring-circle" r="' +
      PROGRESS_RING_RADIUS +
      '" cx="' +
      PROGRESS_RING_X_Y +
      '" cy="' +
      PROGRESS_RING_X_Y +
      '" />';
    this._element.innerHTML = '<svg class="progress-ring">' + svgCircle + '</svg>';
  };

  _proto.setProgress = function setProgress(percent) {
    var circle = this._element.querySelector('circle');

    var radius = circle.r.baseVal.value;
    var circumference = radius * 2 * Math.PI;
    circle.style.strokeDasharray = circumference + ' ' + circumference;
    var offset = circumference - (percent / 100) * circumference;
    circle.style.strokeDashoffset = offset;
  };

  ProgressIndicator.show = function show(progress) {
    console.log(document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS));
    var progressBlockWrapper =
      document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS) || document.createElement('DIV');
    progressBlockWrapper.innerHTML = '';
    var progressElem = document.createElement('DIV');
    progressBlockWrapper.classList.add(PROGRESS_BLOCK_WRAPPER_CLASS);
    progressElem.classList.add(PROGRESS_CLASS);
    this._element = progressElem;

    if (progress) {
      progressElem.classList.add(PROGRESS_DETERMINITE_CLASS);
      this.determiniteProgress = new this(progressElem);
      this.determiniteProgress.setProgress(progress);
    } else {
      progressElem.classList.add(PROGRESS_INFINITE_CLASS);
    }

    progressBlockWrapper.append(progressElem);
    document.body.append(progressBlockWrapper);
  };

  ProgressIndicator.setProgress = function setProgress(progress) {
    if (this.determiniteProgress) {
      this.determiniteProgress.setProgress(progress);
    }
  };

  ProgressIndicator.hide = function hide() {
    var progressBlockWrapper = this._element
      ? this._element.parentNode
      : document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS);

    if (progressBlockWrapper && progressBlockWrapper.classList.contains(PROGRESS_BLOCK_WRAPPER_CLASS)) {
      progressBlockWrapper.parentNode.removeChild(progressBlockWrapper);
    }
  };

  return ProgressIndicator;
})();

export {
  Button,
  ExpansionPanel,
  InputFields,
  Navigation,
  Notification,
  ProgressIndicator,
  Select,
  SelectionControl,
  Tabs,
  Tooltip,
};
//# sourceMappingURL=uilab.esm.js.map
