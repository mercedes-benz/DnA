import * as React from 'react';
import { Modal } from '../Modal';
import Styles from './InfoModal.scss';

export interface IInfoModalProps {
  title: string;
  modalWidth?: string;
  content: string | React.ReactNode;
  show: boolean;
  moreInfoLink?: string;
  onCancel?: () => void;
}

export const InfoModal = (props: IInfoModalProps) => {
  const content: React.ReactNode = (
    <div className={Styles.contentWrapper}>
      {props.content}
      {props.moreInfoLink ? (
        <div className={Styles.moreInfoLinkWrapper}>
          <a className="btn btn-text arrow" href={props.moreInfoLink} target="_blank" rel="noreferrer">
            More information
          </a>
        </div>
      ) : null}
    </div>
  );
  return (
    <Modal
      title={props.title}
      modalWidth={props.modalWidth}
      showAcceptButton={false}
      showCancelButton={false}
      buttonAlignment="right"
      show={props.show}
      content={content}
      scrollableContent={true}
      onCancel={props.onCancel}
    />
  );
};
