import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconAttachmentProps {
  className?: string;
}

export const IconAttachment = (props: IIconAttachmentProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="55">
      <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g>
          <g>
            <g transform="translate(16.500000, 13.750000)">
              <path
                fill="#00ADEF"
                fillRule="nonzero"
                d="M14.5554564,1.375 L1.375,1.375 L1.375,26.125 L20.625,26.125 L20.625,7.84727182 L20.625,8.25 L13.75,8.25 L13.75,1.375 L14.5554564,1.375 Z M15.125,1.94454365 L15.125,6.875 L20.0554564,6.875 L15.125,1.94454365 Z M0,0 L15.125,0 L22,6.875 L22,27.5 L0,27.5 L0,0 Z M4.125,13.75 L17.875,13.75 L17.875,16.5 L4.125,16.5 L4.125,13.75 Z M4.125,19.25 L17.875,19.25 L17.875,22 L4.125,22 L4.125,19.25 Z"
              />
            </g>
          </g>
          <path
            d="M53.099369,26.5879838 C52.733707,11.7521706 40.5059076,0.309739277 25.833525,0.975951045 C11.3701478,1.63278505 0.325671252,13.8731075 0.975541679,28.1761467 C1.62150546,42.3957629 13.5688078,53.7559434 28.2331818,53.0742974 C42.5473251,52.4088671 53.3441204,40.4645302 53.099369,26.5879838"
            stroke="#99A5B3"
            strokeWidth="2"
          />
        </g>
      </g>
    </SvgIcon>
  );
};
