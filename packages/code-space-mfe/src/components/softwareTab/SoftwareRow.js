import cn from 'classnames';
import React from 'react';
import Styles from './software-row.scss';

const SoftwareRow = ({ software, onDeleteSoftware, onSelectSoftware, onEditSoftware }) => {
  const classNames = cn.bind(Styles);

  const handleEditSoftware = (e) => {
    e.stopPropagation();
    onEditSoftware(software);
  }

  const handleDeleteSoftware = (e) => {
    e.stopPropagation();
    onDeleteSoftware(software);
  }

  return (
    <React.Fragment>
      <tr 
        className={classNames('data-row', Styles.dataRow)}
        onClick={() => onSelectSoftware(software)}>
        <td className={'wrap-text'}>
          <div>{software?.id}</div>
        </td>
        <td className={'wrap-text'}>
          <span>
            {software?.softwareName}
          </span>
        </td>
        <td className={'wrap-text'}>
          <span>{software?.createdBy}</span>
        </td>
        <td className={classNames('wrap-text', Styles.actionColumn)}>
          <div>
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleEditSoftware}>
              <i className='icon edit'></i>
            </button>
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleDeleteSoftware}>
              <i className='icon delete'></i>
            </button>
          </div>
        </td>
      </tr>
    </React.Fragment>
  );
};
export default SoftwareRow;
