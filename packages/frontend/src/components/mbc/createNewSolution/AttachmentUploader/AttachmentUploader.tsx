import cn from 'classnames';
import Upload from 'rc-upload';
import { RcFile, UploadRequestError } from 'rc-upload/lib/interface';
import * as React from 'react';
import { ATTACH_FILES_TO_ACCEPT } from 'globals/constants';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { IconAttachment } from 'components/icons/IconAttachment';
import { IAttachment, IAttachmentError } from 'globals/types';
import { ApiClient } from '../../../../services/ApiClient';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import Styles from './AttachmentUploader.scss';
import { refreshToken } from '../../../../utils/RefreshToken';

const classNames = cn.bind(Styles);

export interface IAttachmentUploaderProps {
  infoText?: string;
  attachments: IAttachment[];
  modifyAttachments: (attachments: IAttachment[]) => void;
  containerId?: string;
}

export interface IAttachmentUploaderState {
  attachments: IAttachment[];
  indexToDelete: null | number;
  showDeleteModal: boolean;
  attachmentToDelete: null | IAttachment;
  errorsInUpload: IAttachmentError[];
  jwt: string | void;
}

export interface IAttachmentResponse {
  errors?: IAttachmentError[];
  fileDetails: IAttachment;
}

export default class AttachmentUploader extends React.Component<IAttachmentUploaderProps, IAttachmentUploaderState> {
  public static getDerivedStateFromProps(props: IAttachmentUploaderProps, state: IAttachmentUploaderState) {
    if (props && props.attachments) {
      return {
        attachments: props.attachments,
      };
    }
    return null;
  }
  constructor(props: IAttachmentUploaderProps) {
    super(props);
    this.state = {
      attachments: [],
      indexToDelete: null,
      showDeleteModal: false,
      attachmentToDelete: null,
      errorsInUpload: [],
      jwt: ApiClient.readJwt(),
    };
    this.beforeUpload = this.beforeUpload.bind(this);
  }

  beforeUpload = (file: RcFile, fileList: RcFile[]) => {
    return new Promise<void>((resolve, reject) => {
      if (process.env.NODE_ENV === 'production') {
        const jwt = ApiClient.readJwt();
        refreshToken(jwt)
          .then((newJwt: any) => {
            this.setState({ jwt: newJwt });
            // continue as usual
            resolve();
          })
          .catch((err: any) => {
            // prevent upload
            reject(err);
          });
      } else {
        // continue as usual if not in production
        resolve();
      }
    });
  };

  public render() {
    const onSuccess = (attachmentResponse: IAttachmentResponse) => {
      this.updateAttachments(attachmentResponse.fileDetails);
      this.props.modifyAttachments(this.state.attachments);
      ProgressIndicator.hide();
    };
    const onError = (err: UploadRequestError, errResponse: IAttachmentResponse) => {
      if (errResponse.errors) {
        if (errResponse.errors.length > 0) {
          this.setState({ errorsInUpload: errResponse.errors });
          ProgressIndicator.hide();
        }
      }
    };
    const uploaderProps = {
      accept: ATTACH_FILES_TO_ACCEPT,
      headers: {
        Authorization: this.state.jwt || '',
      },
      action: ApiClient.getAttachmentBaseURL(),
      beforeUpload: this.beforeUpload,
      onStart: (file: RcFile) => {
        this.setState({ errorsInUpload: [] });
        ProgressIndicator.show(1);
      },
      onSuccess,
      onProgress(step: { percent: number }) {
        ProgressIndicator.show(Math.round(step.percent));
      },
      onError,
    };
    const infoText = this.props.infoText;
    return (
      <div className={classNames(Styles.wrapper)}>
        <div className={classNames(Styles.firstPanel)}>
          <h3>Add Attachments</h3>
          <p className="info-message">Maximum file size - 5MB</p>
          <div className={Styles.attachmentsWrapper}>
            <div className={Styles.flexLayout}>
              <div>
                {infoText ? <p className="info-message">{infoText}</p> : ''}
                <div className="addButtonWrapper">
                  <IconAttachment className="buttonIcon" />
                  <button
                    id={
                      'AddAttachment' + (this.props.containerId ? this.props.containerId.replace(' ', '_') : '') + 'Btn'
                    }
                  >
                    <i className="icon mbc-icon plus" />
                    <Upload {...uploaderProps}>Add attachment</Upload>
                  </button>
                </div>
                <ul className={Styles.errorsListWrapper}>
                  {this.state.errorsInUpload.map((item: any, index: number) => {
                    return (
                      <li key={'error-' + index} className="error-message">
                        {item.message}
                      </li>
                    );
                  })}
                </ul>
              </div>
              <div className={Styles.attachmentContainer}>
                {this.state.attachments.map((attachment: IAttachment, index: number) => {
                  return (
                    <div key={attachment.id} className={Styles.attachmentWrapper}>
                      <i className={classNames(Styles.attachmentIcon, 'icon document')} />
                      <span className={Styles.fileNameText} onClick={this.downloadAttachment(attachment)}>
                        {attachment.fileName}
                      </span>
                      <i
                        onClick={this.openDeleteModal(attachment, index)}
                        className={classNames(Styles.deleteIcon, 'icon delete')}
                      />
                      <br />
                      <span className={Styles.fileSizeText}>{attachment.fileSize}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
        <ConfirmModal
          title={''}
          showAcceptButton={true}
          showCancelButton={false}
          acceptButtonTitle={'Confirm'}
          show={this.state.showDeleteModal}
          removalConfirmation={true}
          content={
            <div style={{ margin: '35px 0', textAlign: 'center' }}>
              <div>Remove Attachment</div>
              <div className={classNames(Styles.removeConfirmationContent, 'hide')}>
                Are you sure to remove the attachment?
              </div>
            </div>
          }
          onCancel={this.onInfoModalCancel}
          onAccept={this.onAccept}
        />
      </div>
    );
  }

  protected updateAttachments = (attachment: IAttachment) => {
    const attachments = this.state.attachments;
    attachments.push(attachment);
    this.setState({ attachments });
  };

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

  protected deleteAttachment = (attachment: IAttachment, index: number) => {
    // ProgressIndicator.show();
    // ApiClient.deleteAttachment(attachment.id)
    //   .then((res) => {
    const attachments = this.state.attachments;
    attachments.splice(index, 1);
    this.setState({ attachments, showDeleteModal: false });
    // this.setState({ attachments, showDeleteModal: false }, () => {
    //   ProgressIndicator.hide();
    // });
    // })
    // .catch((err: Error) => {
    //   ProgressIndicator.hide();
    //   Notification.show('Error deleting attachment. Error - ' + err.message, 'alert');
    // });
  };

  protected openDeleteModal = (attachment: IAttachment, index: number) => {
    return () => {
      this.setState({ showDeleteModal: true, attachmentToDelete: attachment, indexToDelete: index });
    };
  };

  protected onAccept = () => {
    this.deleteAttachment(this.state.attachmentToDelete, this.state.indexToDelete);
  };

  protected onInfoModalCancel = () => {
    this.setState({ showDeleteModal: false });
  };
}
