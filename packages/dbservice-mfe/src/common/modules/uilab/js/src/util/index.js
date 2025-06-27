/**
 * --------------------------------------------------------------------------
 * UiLab (v1.0.0): util/index.js
 * --------------------------------------------------------------------------
 */

const makeArray = (nodeList) => {
  if (!nodeList) {
    return [];
  }

  return [].slice.call(nodeList);
};

const getRGB = (rgbColorString) => {
  const match = rgbColorString.match(/rgba?\((\d{1,3}), ?(\d{1,3}), ?(\d{1,3})\)?(?:, ?(\d(?:\.\d?))\))?/);
  return match ? { red: match[1], green: match[2], blue: match[3] } : {};
};

const getSelectionParent = (inputElem) => {
  let elemWrap = inputElem.parentNode;
  if (elemWrap.classList.contains('wrapper')) {
    elemWrap = elemWrap.parentNode;
  }

  return elemWrap;
};

const setRippleAnimation = (elem, center = true, rbgaColor) => {
  let mouseInPressedState = false;

  elem.addEventListener('mousedown', (e) => {
    const { nodeName } = e.target;
    if (nodeName !== 'INPUT' && nodeName !== 'I' && nodeName !== 'BUTTON' && nodeName !== 'A') {
      return;
    }

    e.stopPropagation();
    mouseInPressedState = true;
    let el = elem;
    const animationWrapper = elem.querySelector('.animation-wrapper');
    if (animationWrapper) {
      el = animationWrapper;
    }

    const clientRect = el.getBoundingClientRect();
    let x = clientRect.width / 2;
    let y = clientRect.height / 2;
    if (!center) {
      x = e.pageX - (clientRect.x + window.pageXOffset);
      y = e.pageY - (clientRect.y + window.pageYOffset);
    }

    const duration = 750;
    const offsetDuration = 300;
    const animationStartOpacity = 0.3;
    const opacityLevel = 1.5;
    const stopLevel = 2.5;
    let animationFrame;
    let animationStart;

    const rgb = getRGB(rbgaColor || window.getComputedStyle(el).color);

    const animationStep = (timestamp) => {
      if (!animationStart) {
        animationStart = timestamp;
      }

      const frame = timestamp - animationStart;
      const circle = 'circle at ' + x + 'px ' + y + 'px';
      let color = 'rgba(' + rgb.red + ', ' + rgb.green + ', ' + rgb.blue + ',' + animationStartOpacity + ')';
      let stop = '100%';
      if (frame < duration - offsetDuration) {
        const easing = (frame / duration) * (stopLevel - frame / duration);
        const opacityEasing = mouseInPressedState
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

  const removeAnimation = () => {
    let el = elem;
    const animationWrapper = elem.querySelector('.animation-wrapper');
    if (animationWrapper) {
      el = animationWrapper;
    }

    el.style.backgroundImage = 'none';
    mouseInPressedState = false;
  };

  elem.addEventListener('mouseup', removeAnimation);
  elem.addEventListener('mouseleave', removeAnimation);
};

const getSelectionStart = (elem) => {
  if (elem.createTextRange) {
    const r = document.selection.createRange().duplicate();
    r.moveEnd('character', elem.value.length);
    if (r.text === '') {
      return elem.value.length;
    }

    return elem.value.lastIndexOf(r.text);
  }

  return elem.selectionStart;
};

export { makeArray, getRGB, getSelectionParent, setRippleAnimation, getSelectionStart };
