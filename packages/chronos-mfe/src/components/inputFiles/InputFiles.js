import classNames from 'classnames';
import React from 'react';
import Styles from './input-files.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const InputFiles = ({inputFiles, showModal}) => {
  return (
    <>
    { inputFiles.length > 0 ? 
    <table className={classNames('ul-table', Styles.firstPanel)}>
      <thead>
        <tr className="header-row">
          <th><label>Name</label></th>
          <th><label>Uploaded By</label></th>
          <th><label>Uploaded On</label></th>
          <th>&nbsp;</th>
        </tr>
      </thead>
      <tbody>
        { inputFiles.map(inputFile =>
            <tr className={classNames('data-row', Styles.dataRow)} key={inputFile?.id}>
              <td>{inputFile?.name}</td>
              <td>{inputFile?.createdBy}</td>
              <td>{regionalDateAndTimeConversionSolution(inputFile?.createdOn)}</td>
              <td><i onClick={() => showModal(inputFile?.id)} className={classNames('icon delete', Styles.deleteIcon)} /></td>
            </tr>
          )
        }
      </tbody>
    </table> :
    <div className={Styles.firstPanel}>
      <p>No input files present</p>
    </div>
    }
    </>
  );
}

export default InputFiles;