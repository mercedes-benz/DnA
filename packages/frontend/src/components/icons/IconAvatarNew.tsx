import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconAvatarNewProps {
  className?: string;
}

const IconAvatarNew = (props: IIconAvatarNewProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="55">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Group" strokeWidth="2">
          <circle cx="27.5" cy="27.5" r="26.5" stroke="#99A5B3" strokeWidth="2" />
          <g id="Group-2" transform="translate(16.000000, 14.000000)" stroke="#00ADEF">
            <path
              d="M0,24.9981932 C0.204344866,23.2170101 0.810288082,21.5755169 1.92615856,20.1181701 C3.33021608,18.284539 5.18318519,17.1791333 7.3126771,16.5986616 C10.4232433,15.7505998 13.5373134,15.7702013 16.5885947,16.8720431 C19.6931344,17.9931895 21.9020933,20.1309408 22.7627758,23.5741223 C22.8540921,23.9394555 22.9331668,24.4147481 23,25"
              id="Path"
            />
            <path
              d="M6.01758899,5.46778898 C6.25275358,2.21908105 9.24627736,-0.271766559 12.524286,0.0238304145 C15.7428244,0.31413866 18.2238466,3.05184119 17.9839644,6.44663235 C17.7369343,9.94233814 14.7165346,12.1524544 11.7097158,11.9917914 C8.48488731,11.8538557 5.75426183,9.10557572 6.01758899,5.46778898"
              id="Path"
            />
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};

export default IconAvatarNew;
