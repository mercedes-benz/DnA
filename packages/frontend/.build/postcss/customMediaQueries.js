const breakpoints = {
  '--xxxxxs': '(max-width: 320px)',
  '--xxxxs': '(max-width: 375px)',
  '--xxxxm': '(max-width: 414px)',
  '--xxxs': '(max-width: 480px)',
  '--xxs': '(max-width: 575.98px)',
  '--xs': '(max-width: 767.98px)',
  '--small': '(max-width: 991.98px)',
  '--sm': '(max-width: 1024px)',
  '--medium': '(max-width: 1199.98px)',
  '--mmmms': '(max-width: 1280px)',
  '--mmms': '(max-width: 1440px)',
  '--mms': '(max-width: 1600px)',
  '--large': '(max-width: 1920px)',
  '--xl': '(max-width: 2560px)',

  '--only-xxxxs': '(0px <= width < 320px)',
  '--only-xxxs': '(320px <= width < 480px)',
  '--only-xxs': '(480px <= width < 576px)',
  '--only-xs': '(576px <= width < 768px)',
  '--only-small': '(768px <= width < 992px)',
  '--only-medium': '(992px <= width < 1200px)',
  '--only-large': '(1200px <= width < 1920px)',
  '--only-xl': '(1920px <= width < 2560px)',

  '--greater-xxxxs': '(min-width: 320px)',
  '--greater-xxxs': '(min-width: 480px)',
  '--greater-xxs': '(min-width: 576px)',
  '--greater-xs': '(min-width: 768px)',
  '--greater-small': '(min-width: 992px)',
  '--greater-medium': '(min-width: 1200px)',
  '--greater-large': '(min-width: 1920px)',
  '--greater-xl': '(min-width: 2560px)',

  '--retina': '(-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi), (min-resolution: 2dppx)',
};

module.exports = breakpoints;
