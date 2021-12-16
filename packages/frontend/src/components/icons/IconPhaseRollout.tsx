/**
 * @author Philipp Laube <philipp.laube@etecture.de>
 */

import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPhaseRolloutProps {
  className?: string;
}

export const IconPhaseRollout = (props: IIconPhaseRolloutProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{}}>
        <path d="M17.3602266,17.8436261 C16.7473088,17.3428895 16.074221,16.9349575 15.3583003,16.6249292 C15.9824363,15.3032295 16.3363173,13.8234561 16.374051,12.2743343 L19.9291785,12.2743343 C19.8526912,14.4761473 18.8787535,16.4484986 17.3602266,17.8436261 L17.3602266,17.8436261 Z M4.07184136,12.2743343 L7.62492918,12.2743343 C7.66266289,13.8234561 8.01654391,15.3032295 8.64067989,16.6249292 C7.92475921,16.9349575 7.25269122,17.3428895 6.63875354,17.8436261 C5.12022663,16.4484986 4.14730878,14.4761473 4.07184136,12.2743343 L4.07184136,12.2743343 Z M6.52045326,6.26447592 C7.14152975,6.79376771 7.82889518,7.22311615 8.56113314,7.55048159 C7.98594901,8.82628895 7.66062323,10.2448725 7.62492918,11.7266856 L4.07184136,11.7266856 C4.14526912,9.581983 5.0682153,7.65246459 6.52045326,6.26447592 L6.52045326,6.26447592 Z M12.2743343,11.7266856 L12.2743343,8.27966006 C13.1891218,8.25008499 14.0865722,8.07569405 14.9330312,7.7615864 C15.4806799,8.97212465 15.7907082,10.3193201 15.8264023,11.7266856 L12.2743343,11.7266856 Z M12.2743343,15.9314448 L12.2743343,12.2743343 L15.8264023,12.2743343 C15.7886686,13.752068 15.4490652,15.1635127 14.8504249,16.4199433 C14.0264023,16.1241926 13.1575071,15.96 12.2743343,15.9314448 L12.2743343,15.9314448 Z M15.1104816,17.1144476 C15.7631728,17.393881 16.3771105,17.76 16.9390368,18.2087252 C15.7947875,19.1214731 14.3955807,19.7262323 12.8658357,19.8934844 C13.7785836,19.1214731 14.5352975,18.1781303 15.1104816,17.1144476 L15.1104816,17.1144476 Z M12.2743343,19.6732011 L12.2743343,16.4790935 C13.0698017,16.5066289 13.854051,16.6534844 14.5975071,16.9125212 C14.0080453,17.9853824 13.2248159,18.9266856 12.2743343,19.6732011 L12.2743343,19.6732011 Z M11.1351841,19.8934844 C9.60441926,19.7262323 8.20419263,19.1214731 7.05994334,18.2087252 C7.62186969,17.76 8.2368272,17.393881 8.88849858,17.1144476 C9.46368272,18.1781303 10.2203966,19.1214731 11.1351841,19.8934844 L11.1351841,19.8934844 Z M11.7266856,16.4790935 L11.7266856,19.6732011 C10.7741643,18.9266856 9.99093484,17.9853824 9.40147309,16.9125212 C10.1469688,16.6534844 10.9291785,16.5066289 11.7266856,16.4790935 L11.7266856,16.4790935 Z M11.7266856,12.2743343 L11.7266856,15.9314448 C10.8414731,15.96 9.9725779,16.1241926 9.14855524,16.4199433 C8.54991501,15.1635127 8.21031161,13.752068 8.1725779,12.2743343 L11.7266856,12.2743343 Z M11.7266856,8.27966006 L11.7266856,11.7266856 L8.1725779,11.7266856 C8.20827195,10.3193201 8.51932011,8.97212465 9.06492918,7.7615864 C9.91342776,8.07569405 10.8098584,8.25008499 11.7266856,8.27966006 L11.7266856,8.27966006 Z M8.79773371,7.05586402 C8.12872521,6.75909348 7.50152975,6.37053824 6.92838527,5.89325779 C8.09711048,4.9223796 9.54628895,4.28090652 11.1351841,4.10753541 C10.1724646,4.92033994 9.38209632,5.92283286 8.79773371,7.05586402 L8.79773371,7.05586402 Z M11.7266856,4.3278187 L11.7266856,7.73201133 C10.8934844,7.70345609 10.0796601,7.54538244 9.30764873,7.26492918 C9.90526912,6.11966006 10.722153,5.11512748 11.7266856,4.3278187 L11.7266856,4.3278187 Z M12.8658357,4.10753541 C14.4526912,4.28090652 15.9028895,4.9223796 17.0705949,5.89325779 C16.4984703,6.37053824 15.8712748,6.75909348 15.2022663,7.05586402 C14.6179037,5.92283286 13.8275354,4.92033994 12.8658357,4.10753541 L12.8658357,4.10753541 Z M12.2743343,4.3278187 C13.277847,5.11512748 14.0947309,6.11966006 14.6933711,7.26492918 C13.9203399,7.54538244 13.1044759,7.70345609 12.2743343,7.73201133 L12.2743343,4.3278187 Z M19.9291785,11.7266856 L16.374051,11.7266856 C16.3383569,10.2448725 16.014051,8.82628895 15.4388669,7.55048159 C16.1711048,7.22311615 16.8574504,6.79376771 17.4785269,6.26447592 C18.9317847,7.65246459 19.8547309,9.581983 19.9291785,11.7266856 L19.9291785,11.7266856 Z M12,3 C7.03675676,3 3,7.03783784 3,12.0010811 C3,16.9632432 7.03675676,21 12,21 C16.9621622,21 21,16.9632432 21,12.0010811 C21,7.03783784 16.9621622,3 12,3 L12,3 Z" />
      </g>
    </SvgIcon>
  );
};
