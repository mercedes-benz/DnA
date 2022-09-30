import * as React from 'react';
import { IInfoItem } from 'globals/types';

export interface IInfoListProps {
  list: IInfoItem[];
}

export const InfoList = (props: IInfoListProps) => {
  return (
    <div>
      {props.list.map((info: IInfoItem, index: number) => {
        return (
          <React.Fragment key={index}>
            <strong>{info.title}:</strong>
            <p>{info.description}</p>
          </React.Fragment>
        );
      })}
    </div>
  );
};
