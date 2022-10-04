import cn from 'classnames';
// import Upload from 'rc-upload';
// import { RcFile } from 'rc-upload/lib/interface';
import React, { useEffect, useState } from 'react';
import Modal from 'components/formElements/modal/Modal';
import { IconImage } from 'components/icons/IconImage';
import { PredefinedSolutionLogoImagesInfo, SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import { ILogoDetails } from 'globals/types';
import LogoImage from '../LogoImage/LogoImage';
import Styles from './AddImageModal.scss';

const classNames = cn.bind(Styles);

export interface IAddImageModalProps {
  logoDetails: ILogoDetails;
  showModal: boolean;
  onModalCancel: () => void;
  onImageSelect: (logoDetails: ILogoDetails) => void;
}

const AddImageModal = (props: IAddImageModalProps) => {
  const [selectedImageInfo, setSelectedImageInfo] = useState<any>();

  const setStateOfSelectedImageId = () => {
    const logoDetails = props.logoDetails;
    const predefinedImageInfo = logoDetails
      ? { id: logoDetails.id, name: logoDetails.fileName }
      : PredefinedSolutionLogoImagesInfo.images[0];
    setSelectedImageInfo(predefinedImageInfo);
  };

  useEffect(() => {
    setStateOfSelectedImageId();
  }, [props.logoDetails]);

  // const uploaderProps = {
  //   accept: 'image/*',
  //   beforeUpload(file: RcFile, fileList: RcFile[]) {
  //     console.log('beforeUpload', file.type);
  //     console.log('beforeUpload', fileList[0].name);
  //     return false;
  //   },
  // };

  const onPredefinnedImageItemClick = (imageInfo: any) => {
    return () => setSelectedImageInfo(imageInfo);
  };

  const onSaveBtnClick = () => {
    const logoDetail: ILogoDetails = {
      id: selectedImageInfo.id,
      fileName: selectedImageInfo.name,
      fileSize: '',
      isPredefined: true,
    };
    props.onImageSelect(logoDetail);
  };

  const addImageModalContent: React.ReactNode = (
    <div id="addImageModalDiv" className={classNames(Styles.firstPanel, Styles.addImageModalWrapper)}>
      <h4>
        <IconImage className={Styles.titleIcon} />
        Preset Images
      </h4>
      <div className={classNames(Styles.predefinedImageGridWrapper, 'mbc-scroll')}>
        <div className={Styles.predefinedImageGrid}>
          {PredefinedSolutionLogoImagesInfo.images.map((item: any) => {
            const isSelectedItem = item.id === (selectedImageInfo ? selectedImageInfo.id : 'default');
            const logoDetails: ILogoDetails = {
              id: item.id,
              isPredefined: true,
            };
            return (
              <div key={item.id} className={classNames(Styles.imageWrapper, isSelectedItem ? Styles.selected : null)}>
                {/* {item.image} */}
                <LogoImage key={item.id} displayType={SOLUTION_LOGO_IMAGE_TYPES.TILE} logoDetails={logoDetails} />
                <i className="icon mbc-icon check-mark " />
                <span>{item.name}</span>
                <button className="btn" onClick={onPredefinnedImageItemClick(item)} />
              </div>
            );
          })}
        </div>
      </div>
      <div className={Styles.actionWrapper}>
        <button className="btn btn-tertiary" onClick={onSaveBtnClick}>
          Select
        </button>
      </div>
      {/* <Upload {...uploaderProps}>Choose an image file logo</Upload> */}
    </div>
  );

  const onModalCancel = () => {
    props.onModalCancel();
    setStateOfSelectedImageId();
  };

  return (
    <Modal
      title=""
      showAcceptButton={false}
      showCancelButton={false}
      buttonAlignment="right"
      show={props.showModal}
      content={addImageModalContent}
      onCancel={onModalCancel}
    />
  );
};

export default AddImageModal;
