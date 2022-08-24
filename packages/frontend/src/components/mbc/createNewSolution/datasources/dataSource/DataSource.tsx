import React, { useState, useEffect } from 'react';
// @ts-ignore
import ReactSlider from 'react-slider';
import NumberFormat from 'react-number-format';
import Styles from './DataSource.scss';
import { ITag } from '../../../../../globals/types';
import { Envs } from '../../../../../globals/Envs';

export interface IDataSourceProps {
  name: string;
  weightage: number;
  list?: ITag[];
  onWeightageChange: (index: number) => void;
}

const DataSource = (props: IDataSourceProps) => {

  const [sliderValue, setSliderValue] = useState(props.weightage);
  const [badge, setBadge] = useState<any>(Envs.DNA_APPNAME_HEADER);

  const handleSliderValueChange = (val: any) => {
    setSliderValue(val);
  }

  const handleInputChange = (e: any) => {
    if(parseInt(e.target.value) > 100) {
      setSliderValue(100);
    } else {
      setSliderValue(parseInt(e.target.value));
    }
  }

  const handleKeyDown = (e: any) => {
    if (e.key === "." || e.key === "," || e.key === "-") {
      e.preventDefault();
    }
  };

  useEffect(() => {
    if(props.list) {
      const dataSource = props.list.filter(ds => ds.name === props.name);
      if(dataSource.length === 1) {
        if(dataSource[0].source !== null && dataSource[0].dataType !== null) {
          if(dataSource[0].dataType !== undefined && dataSource[0].source !== undefined) {
            if(dataSource[0].dataType === "Not set") {
              setBadge(dataSource[0].source);
            } else {
              setBadge(dataSource[0].source + '-' + dataSource[0].dataType.charAt(0).toUpperCase() + dataSource[0].dataType.slice(1));
            }
          }
        }
      }
    }
  }, []);
  

  useEffect(() => {
    props.onWeightageChange(sliderValue);
  }, [sliderValue]);

  return (
    <tr>
      <td>
        {props.name}<span className={Styles.badge}>{badge}</span>
      </td>
      <td>
        <div className={Styles.sliderContainer}>
          <div className={Styles.sliderWrapper}>
            <ReactSlider
              className={Styles.horizontalSlider}
              thumbClassName="thumb"
              trackClassName="track"
              value={isNaN(sliderValue) ? 0 : sliderValue}
              onChange={(val:any) => handleSliderValueChange(val)}
              renderThumb={(props: any, state:any) => <div {...props}><div className={Styles.thumbContainer}><div className={Styles.thumb}></div> <div className={Styles.thumbValue}>{state.valueNow}%</div></div></div>}
            />
          </div>
          <div className={'input-field-group ' + Styles.inputContainer}>
              {/* @ts-ignore */}
              <NumberFormat
                className={'input-field'}
                placeholder="%"
                maxLength={3}
                value={sliderValue}
                onChange={(e:any) => handleInputChange(e)}
                onKeyDown={handleKeyDown}
              />
          </div>
        </div>
      </td>
    </tr>
  )
}

export default DataSource;