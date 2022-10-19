import cn from 'classnames';
import React, { useState } from 'react';
import { IconImage } from 'components/icons/IconImage';
import { SOLUTION_LOGO_IMAGE_TYPES } from 'globals/constants';
import { ILogoDetails } from 'globals/types';
import AddImageModal from './addImageModal/AddImageModal';
import LogoImage from './LogoImage/LogoImage';
import Styles from './LogoManager.scss';

const classNames = cn.bind(Styles);

export interface ILogoManagerProps {
  logoDetails: ILogoDetails;
  modifyLogoDetails: (logoDetails: ILogoDetails) => void;
}

const LogoManager = (props: ILogoManagerProps) => {
  const [showAddImageModal, setshowAddImageModal] = useState<boolean>(false);

  const onAddImageBtnClick = () => {
    setshowAddImageModal(true);
  };

  const onAddImageModalCancel = () => {
    setshowAddImageModal(false);
  };

  const onLogoDetailsReceived = (logoDetails: ILogoDetails) => {
    props.modifyLogoDetails(logoDetails);
    setshowAddImageModal(false);
  };

  return (
    <React.Fragment>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <h3>Select Logo Image</h3>
          <div className={classNames(Styles.logoManagerWrapper)}>
            <div className={Styles.flexLayout}>
              <div>
                <p className="info-message">To easily find your solution please select one of our preset images.</p>
                <div className="addButtonWrapper">
                  <IconImage className="buttonIcon" />
                  <button onClick={onAddImageBtnClick}>
                    <i className="icon mbc-icon plus" />
                    <span>Select from preset images</span>
                  </button>
                </div>
              </div>
              <div className={Styles.previewWrapper}>
                <h6>Preview ({props.logoDetails ? props.logoDetails.fileName : 'Default'})</h6>
                <p className="info-message small">Banner- and Tile-Image (Safe Area / 1440x480)</p>
                <div className={Styles.previewBannerTileWrapper}>
                  <LogoImage
                    width="280"
                    height="105"
                    displayType={SOLUTION_LOGO_IMAGE_TYPES.TILE}
                    logoDetails={props.logoDetails}
                  />
                  <i className="icon mbc-icon check circle" />
                  <span />
                  <div />
                </div>
                <p className="info-message small">List-Image (300px x 300px).</p>
                <div className={Styles.previewListImageWrapper}>
                  <LogoImage
                    width="80"
                    height="80"
                    displayType={SOLUTION_LOGO_IMAGE_TYPES.THUMBNAIL}
                    logoDetails={props.logoDetails}
                  />
                  <i className="icon mbc-icon check circle" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      {showAddImageModal && (
        <AddImageModal
          logoDetails={props.logoDetails}
          showModal={showAddImageModal}
          onImageSelect={onLogoDetailsReceived}
          onModalCancel={onAddImageModalCancel}
        />
      )}
    </React.Fragment>
  );
};

export default LogoManager;
