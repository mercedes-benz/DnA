var fs = require('fs');
var cssjson = require('cssjson');

var cssCustomProps = {};
var cssPaths = ['../../src/globals/css/css-custom-properties.css'];

for (var i = 0, l = cssPaths.length, cssPath, cssFilename, cssFile, cssObj; i < l; i++) {
  cssPath = cssPaths[i];
  try {
    cssFilename = require.resolve(cssPath);
    cssFile = fs.readFileSync(cssFilename, 'utf8');
    cssObj = cssjson.toJSON(cssFile);

    if (cssObj.hasOwnProperty('children')) {
      for (var rootChild in cssObj.children) {
        if (cssObj.children.hasOwnProperty(rootChild)) {
          if (rootChild === ':root') {
            if (cssObj.children[':root'].hasOwnProperty('attributes')) {
              var attributes = cssObj.children[':root'].attributes;
              for (var property in attributes) {
                if (attributes.hasOwnProperty(property)) {
                  var iStart = property.indexOf('--');
                  if (iStart >= 0) {
                    var propertyName = property.substr(iStart + 2);
                    cssCustomProps[propertyName] = attributes[property];
                  }
                }
              }
            }
          }
        }
      }
    }
  } catch (e) {
    throw new Error(e);
  }
}

module.exports = cssCustomProps;
