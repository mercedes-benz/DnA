import cn from 'classnames';

import * as React from 'react';

import { ISubDivision, ITagResult } from '../../../../../globals/types';

import Styles from './TagRowItem.scss';

const classNames = cn.bind(Styles);

export interface ITagRowItemProps {
  tagItem: ITagResult;
  key: string;
  showDeleteConfirmModal: (tagItem: ITagResult) => void;
  showUpdateConfirmModal: (tagItem: ITagResult) => void;
}
export class TagRowItem extends React.Component<ITagRowItemProps, any> {
  constructor(props: any) {
    super(props);
  }

  public render() {
    const tagItem = this.props.tagItem;
    const subdivisions = tagItem.subdivisions;
    return (
      <React.Fragment>
        <tr id={tagItem.id + ''} key={tagItem.id} className="data-row">
          <td className="wrap-text">
            {tagItem.name}
            {subdivisions && (
              <React.Fragment>
                <br />
                Sub Divisions - {subdivisions.map((item: ISubDivision) => item.name).join(', ')}
                {!subdivisions.length && 'N/A'}
              </React.Fragment>
            )}
          </td>
          <td className="wrap-text">{tagItem.category.name}</td>
          <td className={'wrap-text ' + classNames(Styles.actionLinksTD)}>
            <div className={Styles.actionCol}>
              {subdivisions && (
                <div onClick={this.onTagUpdate}>
                  <i className="icon mbc-icon edit small " tooltip-data={'Update Details'} />
                  Update
                </div>
              )}
              <div onClick={this.onTagDelete}>
                <i className="icon mbc-icon trash-outline  small " tooltip-data={'Edit Details'} />
                Delete
              </div>
            </div>
          </td>
        </tr>
      </React.Fragment>
    );
  }
  protected onTagUpdate = () => {
    this.props.showUpdateConfirmModal(this.props.tagItem);
  };
  protected onTagDelete = () => {
    this.props.showDeleteConfirmModal(this.props.tagItem);
  };
}
