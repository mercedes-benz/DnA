import classnames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './comparison-row.scss';
// import from DNA Container
import CircularProgressBar from '../../circularProgressBar/CircularProgressBar';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';

const classNames = classnames.bind(Styles);

const ComparisonRow = (props) => {
  const item = props.item;

  const [isChecked, setIsChecked] = useState(false);

  const onChangeCheck = (e) => {
    if (!e.currentTarget.checked) {
      props.deselectComparison(e.currentTarget.id);
    } else {
      props.selectComparison(e.currentTarget.id);
    }
    setIsChecked(e.currentTarget.checked);
    e.nativeEvent.stopImmediatePropagation();
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const onRowClick = () => {
    props.openDetails(item);
  };

  // const onItemDelete = () => {
  //   props.showDeleteConfirmModal(props.item);
  // };

  const handleStatusClick = (e, item) => {
    props.onOpenErrorModal(item);
  }

  const stopPropagation = (e) => {
    e.stopPropagation();
  };

  return (
    <React.Fragment>
      <tr
        key={item.id}
        onClick={onRowClick}
        className={classNames('data-row', Styles.dataRow)}
      >
        <td>
          <label
            className={classNames('checkbox', Styles.checkboxItem)}
            onClick={(e) => stopPropagation(e)}
          >
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                id={'checkbox-' + item.comparisonId}
                checked={isChecked}
                onChange={(e) => {
                  e.stopPropagation();
                  onChangeCheck(e, item.comparisonId);
                }}
                disabled={item.state.lifeCycleState === 'CREATED'}
              />
            </span>{' '}
          </label>
        </td>
        <td>
          <span>{item.comparisonName}</span>
        </td>
        <td>
          {item.state.lifeCycleState === 'SUCCESS' && <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} tooltip-data={item.state.result_state} />}
          {item.state.lifeCycleState === 'FAILED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.lifeCycleState === 'CREATED' && <div tooltip-data={'IN PROGRESS'} ><CircularProgressBar /></div>}
        </td>
        <td>
          {regionalDateAndTimeConversionSolution(item.triggeredOn)}
        </td>
        <td>
          {item.triggeredBy}
        </td>
        <td>
          {item.actualsFile?.length === 0 ? 'NA' : item.actualsFile.split("/")[3]}
        </td>
      </tr>
    </React.Fragment>
  );
};

export default ComparisonRow;
