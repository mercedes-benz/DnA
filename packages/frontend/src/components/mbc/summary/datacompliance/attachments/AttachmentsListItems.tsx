import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Notification from '../../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import { IAttachment } from 'globals/types';
import { ApiClient } from '../../../../../services/ApiClient';
// import { Modal } from '../../../formElements/modal/Modal';
import Styles from './AttachmentsListItems.scss';

const classNames = cn.bind(Styles);

export interface IAttachmentsListItemProps {
  attachments: IAttachment[];
  leftMarginZero?: boolean;
}

export default class AttachmentsListItem extends React.Component<IAttachmentsListItemProps, any> {
  constructor(props: any) {
    super(props);
  }

  public render() {
    const attachments = this.props.attachments;

    return (
      <React.Fragment>
        <div
          className={
            this.props.leftMarginZero ? Styles.attachmentContainerWithNoLeftMargin : Styles.attachmentContainer
          }
        >
          {attachments
            ? attachments.map((attachment: IAttachment, index: number) => {
                return (
                  <div key={attachment.id} className={Styles.attachmentWrapper}>
                    <i className={classNames(Styles.attachmentIcon, 'icon document')} />
                    <span className={Styles.fileNameText}>{attachment.fileName}</span>
                    <i
                      onClick={this.downloadAttachment(attachment)}
                      className={classNames(Styles.downloadIcon, 'icon download')}
                    />
                    <br />
                    <span className={Styles.fileSizeText}>{attachment.fileSize}</span>
                  </div>
                );
              })
            : 'N/A'}
        </div>
      </React.Fragment>
    );
  }

  protected downloadAttachment = (attachment: IAttachment) => {
    return () => {
      ProgressIndicator.show();
      ApiClient.downloadAttachment(attachment)
        .then((res) => {
          if (res.ok) {
            res.blob().then((blob: any) => {
              const link = document.createElement('a');
              const url = URL.createObjectURL(blob);
              link.href = url;
              link.download = attachment.fileName;
              link.click();
              ProgressIndicator.hide();
            });
          } else {
            ProgressIndicator.hide();
            Notification.show('Error downloading attachment. Please try again later.', 'alert');
          }
        })
        .catch((err: Error) => {
          ProgressIndicator.hide();
          Notification.show('Error downloading attachment. Error - ' + err.message, 'alert');
        });
    };
  };
}
