/**
 * @author Jens Ihrig <jens.ihrig@etecture.de>
 */

import classNames from 'classnames';
import * as React from 'react';
import Styles from './SvgIcon.scss';

/**
 * scaled svg icon
 * @see https://css-tricks.com/scale-svg/
 */
export default class SvgIcon extends React.Component {
  render() {
    const size = this.props.size ? this.props.size : '24';
    const viewBox = `0 0 ${size} ${size}`;
    const iconClass = classNames(Styles.icon, this.props.className);

    return (
      <i className={iconClass}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width={size}
          height={size}
          viewBox={this.props.viewbox || viewBox}
          enableBackground={`new ${viewBox}`}
          id={this.props.id}
          preserveAspectRatio="xMidYMin slice"
          className={Styles.svg}
        >
          {this.props.children}
        </svg>
      </i>
    );
  }
}
