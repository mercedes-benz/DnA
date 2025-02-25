import cn from 'classnames';
import React from 'react';
import Styles from './additional-service-row.scss';

const AdditionalServiceRow = ({ service, onDeleteAdditionalService, onSelectAdditionalService, onEditAdditionalService }) => {
  const classNames = cn.bind(Styles);

  const handleEditAdditionalService = (e) => {
    e.stopPropagation();
    onEditAdditionalService(service);
  }

  const handleDeleteAdditionalService = (e) => {
    e.stopPropagation();
    onDeleteAdditionalService(service);
  }

  return (
    <React.Fragment>
      <tr 
        className={classNames('data-row', Styles.dataRow)}
        onClick={() => onSelectAdditionalService(service)}>
        <td className={'wrap-text'}>
          <div>{service?.id}</div>
        </td>
        <td className={'wrap-text'}>
          <span>{service?.serviceName}-{service?.version}</span>
        </td>
        {/* <td className={'wrap-text'}>
          {service?.configuration}
        </td> */}
        <td className={classNames('wrap-text', Styles.actionColumn)}>
          <div>
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleEditAdditionalService}>
              <i className='icon edit'></i>
            </button>
            <button className={'btn btn-primary ' + Styles.actionBtn} type="button" onClick={handleDeleteAdditionalService}>
              <i className='icon delete'></i>
            </button>
          </div>
        </td>
      </tr>
    </React.Fragment>
  );
};
export default AdditionalServiceRow;
