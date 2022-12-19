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
const PROGRESS_RING_RADIUS = 22.5;
const PROGRESS_RING_X_Y = 25;
const PROGRESS_BLOCK_WRAPPER_CLASS = 'progress-block-wrapper';
const PROGRESS_CLASS = 'progress';
const PROGRESS_INFINITE_CLASS = 'infinite';
const PROGRESS_DETERMINITE_CLASS = 'determinite';

class ProgressIndicator {
  constructor(element) {
    this._element = element;
    this.setupProgressRing();
  }

  setupProgressRing() {
    const svgCircle = `<circle class="progress-ring-circle" r="${PROGRESS_RING_RADIUS}" cx="${PROGRESS_RING_X_Y}" cy="${PROGRESS_RING_X_Y}" />`;
    this._element.innerHTML = `<svg class="progress-ring">${svgCircle}</svg>`;
  }

  setProgress(percent) {
    const circle = this._element.querySelector('circle');
    const radius = circle.r.baseVal.value;
    const circumference = radius * 2 * Math.PI;
    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    const offset = circumference - (percent / 100) * circumference;
    circle.style.strokeDashoffset = offset;
  }

  static show(progress) {
    console.log(document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS));
    const progressBlockWrapper =
      document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS) || document.createElement('DIV');
    progressBlockWrapper.innerHTML = '';
    const progressElem = document.createElement('DIV');
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
  }

  static setProgress(progress) {
    if (this.determiniteProgress) {
      this.determiniteProgress.setProgress(progress);
    }
  }

  static hide() {
    const progressBlockWrapper = this._element
      ? this._element.parentNode
      : document.body.querySelector('.' + PROGRESS_BLOCK_WRAPPER_CLASS);
    if (progressBlockWrapper && progressBlockWrapper.classList.contains(PROGRESS_BLOCK_WRAPPER_CLASS)) {
      progressBlockWrapper.parentNode?.removeChild(progressBlockWrapper);
    }
  }
}

export default ProgressIndicator;
