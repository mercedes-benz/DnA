import React, { useState, useEffect } from 'react';
// @ts-ignore
import ReactSlider from 'react-slider';
import NumberFormat from 'react-number-format';
import Styles from './DataSource.scss';

export interface IDataSourceProps {
  name: string;
  weightage: number;
  onWeightageChange: (index: number) => void;
}

const DataSource = (props: IDataSourceProps) => {

  const [sliderValue, setSliderValue] = useState(props.weightage);

  const handleSliderValueChange = (val: any) => {
    setSliderValue(val);
  }

  const handleInputChange = (e: any) => {
    setSliderValue(parseInt(e.target.value));
  }

  useEffect(() => {
    props.onWeightageChange(sliderValue);
  }, [sliderValue]);

  return (
    <tr>
      <td>
          {props.name}
      </td>
      <td>
        <div className={Styles.sliderContainer}>
          <div className={Styles.sliderWrapper}>
            <ReactSlider
              className={Styles.horizontalSlider}
              thumbClassName="thumb"
              trackClassName="track"
              value={sliderValue}
              onChange={(val:any) => handleSliderValueChange(val)}
              renderThumb={(props: any, state:any) => <div {...props}><div className={Styles.thumbContainer}><div className={Styles.thumb}></div> <div className={Styles.thumbValue}>{state.valueNow}%</div></div></div>}
            />
          </div>
          <div className={'input-field-group ' + Styles.inputContainer}>
              <NumberFormat
                className="input-field"
                placeholder="%"
                value={sliderValue}
                onChange={(e:any) => handleInputChange(e)}
              />
          </div>
        </div>
      </td>
    </tr>
  )
}

export default DataSource;