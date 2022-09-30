import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import { ILink } from 'globals/types';
import Styles from './LinksListItems.scss';

const classNames = cn.bind(Styles);

export interface ILinksListItemsProps {
  links: ILink[];
}

export default class LinksListItems extends React.Component<ILinksListItemsProps, any> {
  constructor(props: any) {
    super(props);
  }

  public render() {
    const links = this.props.links;

    return (
      <React.Fragment>
        {links ? (
          <div className={Styles.linkContainer}>
            {links.map((link: ILink, index: number) => {
              return (
                <div key={index} className={Styles.linkWrapper}>
                  <i className={classNames(Styles.linkIcon, 'icon mbc-icon link')} />
                  <a className={Styles.fileNameText} target="_blank" href={link.link} rel="noreferrer">
                    {link.link}
                  </a>
                </div>
              );
            })}
          </div>
        ) : (
          'NA'
        )}
      </React.Fragment>
    );
  }
}
