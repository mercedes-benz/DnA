/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): select.js
 * --------------------------------------------------------------------------
 */

import { setRippleAnimation, makeArray } from './util/index';

class Select {
  static defaultSetup() {
    let i;
    let j;
    let selElmnt;
    let a;
    let b;
    let c;
    /* Look for any elements with the class 'custom-select': */
    const x = document.querySelectorAll('.custom-select');
    for (i = 0; i < x.length; i++) {
      selElmnt = x[i].querySelectorAll('select')[0];
      /* For each element, create a new DIV that will act as the selected item: */
      a = document.createElement('DIV');
      const labelWrapper = document.createElement('SPAN');
      labelWrapper.setAttribute('class', 'label');
      const isMultiSelect = selElmnt.hasAttribute('multiple');

      /* For each element, create a new DIV that will contain the option list: */
      b = document.createElement('DIV');

      if (isMultiSelect) {
        const valuesSpan = document.createElement('SPAN');
        valuesSpan.setAttribute('class', 'values');
        a.appendChild(valuesSpan);
        a.setAttribute('class', 'select-selected multiple');
        b.setAttribute('class', 'select-items multiple select-hide');
        labelWrapper.innerHTML =
          '<label class="checkbox"><span class="wrapper"><input type="checkbox" value="label"/></span><span class="label">All</span></label>';
      } else {
        a.setAttribute('class', 'select-selected');
        b.setAttribute('class', 'select-items select-hide');
        labelWrapper.innerHTML = selElmnt.options[selElmnt.selectedIndex]?.innerHTML;
      }

      a.appendChild(labelWrapper);
      const animationWrapper = document.createElement('SPAN');
      animationWrapper.setAttribute('class', 'animation-wrapper');
      a.appendChild(animationWrapper);
      const iconElem = document.createElement('I');
      iconElem.setAttribute('class', 'icon down-up-flip');
      a.appendChild(iconElem);
      x[i].appendChild(a);
      setRippleAnimation(a.parentNode);

      for (j = 0; j < selElmnt.length; j++) {
        /* For each option in the original select element,
      create a new DIV that will act as an option item: */
        c = document.createElement('DIV');

        const optionLabel = selElmnt.options[j].innerHTML;
        const optionValue = selElmnt.options[j].value;
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
            const optionsWrapper = this.parentNode;
            const allCheckBoxElem = optionsWrapper.previousSibling.querySelector('.checkbox');
            if (e.target.nodeName === 'INPUT') {
              const allOptions = optionsWrapper.querySelectorAll('input[type="checkbox"]').length;
              const checkedOptions = optionsWrapper.querySelectorAll('input[type="checkbox"]:checked').length;
              const allCB = allCheckBoxElem.querySelector('input[type="checkbox"]');
              const selectElem = optionsWrapper.parentNode.querySelector('select');
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
            let y;
            let i;
            let k;
            const s = this.parentNode.parentNode.querySelectorAll('select')[0];
            const h = this.parentNode.previousSibling.childNodes[0];
            for (i = 0; i < s.length; i++) {
              if (s.options[i].innerHTML === this.innerHTML) {
                s.selectedIndex = i;
                h.innerHTML = this.innerHTML;
                y = this.parentNode.querySelectorAll('.same-as-selected');
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
        const selectWrapper = e.target.parentNode;
        const optionsWrapper = this.nextSibling;
        if (selectWrapper.nodeName === 'DIV') {
          e.stopPropagation();
          closeAllSelect(this);
          this.nextSibling.classList.toggle('select-hide');
          this.classList.toggle('open');
        } else {
          e.stopPropagation();

          if (e.target.nodeName === 'INPUT') {
            const selectElem = optionsWrapper.parentNode.querySelector('select');
            makeArray(optionsWrapper.querySelectorAll('.checkbox')).forEach((checkboxElem) => {
              const cb = checkboxElem.querySelector('input[type="checkbox"]');
              if (e.target.checked) {
                checkboxElem.classList.add('checked');
              } else {
                checkboxElem.classList.remove('checked');
              }

              cb.checked = e.target.checked;
              selectElem.options[cb.getAttribute('data-index')].selected = e.target.checked;
            });
          }
        }

        // Code for mutiple select
        if (this.classList.contains('multiple')) {
          manageMultiSelectValues(this);
        }
      });
    }

    function manageMultiSelectValues(selectElem) {
      const checkedOptions = selectElem.nextSibling.querySelectorAll('.checkbox.checked');
      const valuesSpan = selectElem.querySelector('.values');
      const allSpan = selectElem.querySelector('.checkbox>.label');
      const optionsTextArr = [];
      makeArray(checkedOptions).forEach((checkboxElem) => {
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
      let i;
      const arrNo = [];
      const x = document.querySelectorAll('.select-items');
      const y = document.querySelectorAll('.select-selected');
      for (i = 0; i < y.length; i++) {
        const el = y[i];
        if (elmnt === el) {
          arrNo.push(i);
        } else {
          el.classList.remove('open');
        }

        // Code for mutiple select
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
  }
}
Select.defaultSetup();

export default Select;
