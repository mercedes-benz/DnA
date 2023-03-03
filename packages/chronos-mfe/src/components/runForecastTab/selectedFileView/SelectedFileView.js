import classNames from 'classnames';
import React from 'react';
import styles from './selected-file-view.scss';

const SelectedFile = ({ selectedFile, setSelected, setIsExistingInputFile }) => {
  return (
    <>
      <div className={styles.selectedFile}>
        <div>
          <span>Input File</span>
          <span>{selectedFile?.name}</span>
        </div>
        <div className={styles.msgContainer}>
          <i className={classNames('icon mbc-icon check circle', styles.checkCircle)} />
          <span>File is ready to use.</span>
        </div>
        {/* <div className={styles.msgContainer}>
          <i className={classNames('icon mbc-icon close circle', styles.closeCircle)} />
          <span>Something&#8217;s wrong with the Input File. Please select correct data set. <a href="#">Here</a> you can find further information on how to set up data correctly.</span>
        </div>
        <div className={styles.msgContainer}>
          <i className={classNames('icon mbc-icon alert circle', styles.alertCircle)} />
          <span>Index not sorted. Either delete Input File and select another one or try to run anyway.</span>
        </div> */}
        <div>
          <i onClick={() => { setSelected(); setIsExistingInputFile(false); }} className={classNames('icon delete', styles.deleteIcon)} />
        </div>
      </div>
    </>
  );
};

export default SelectedFile;