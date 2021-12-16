import * as React from 'react';

export interface IIconAttentionProps {
  className?: string;
}

export const IconAttention = (props: IIconAttentionProps): JSX.Element => {
  return (
    <svg width="67px" height="67px" viewBox="0 0 67 67" version="1.1">
      <title>Icon_doc</title>
      <g id="Specifiactions" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="MacBookPro" transform="translate(-2751.000000, -228.000000)">
          <g id="Modal_dialog" transform="translate(2465.000000, 144.000000)">
            <g id="Icon_doc" transform="translate(287.000000, 85.000000)">
              <circle id="Oval-2" stroke="#C0C8D0" strokeWidth="2" cx="32.5" cy="32.5" r="32.5" />
              <g
                id="+"
                transform="translate(28.000000, 14.000000)"
                fill="#00ADEF"
                fontFamily="var(--font-family)"
                fontSize="30"
                fontWeight="normal"
              >
                <text id="!">
                  <tspan x="0.764648438" y="27">
                    !
                  </tspan>
                </text>
              </g>
            </g>
          </g>
        </g>
      </g>
    </svg>
  );
};
