import classNames from 'classnames';
import React from 'react';
import Styles from './selected-file-view.scss';

const SelectedFile = ({ selectedFile, setSelected, setIsExistingInputFile }) => {
  return (
    <>
      <div className={Styles.selectedFile}>
        <div>
          <span>Input File</span>
          <span>{selectedFile?.name}</span>
        </div>
        <div className={Styles.msgContainer}>
          <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />
          <span>File is ready to use.</span>
        </div>
        {/* <div className={Styles.msgContainer}>
          <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} />
          <span>Something&#8217;s wrong with the Input File. Please select correct data set. <a href="#">Here</a> you can find further information on how to set up data correctly.</span>
        </div>
        <div className={Styles.msgContainer}>
          <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)} />
          <span>Index not sorted. Either delete Input File and select another one or try to run anyway.</span>
        </div> */}
        <div>
          <i onClick={() => { setSelected(); setIsExistingInputFile(false); }} className={classNames('icon delete', Styles.deleteIcon)} />
        </div>
      </div>
    </>
  );
};

export default SelectedFile;