import React, { useEffect, useState } from 'react';
import Styles from './tutorial.scss';
import { Envs } from '../../utilities/envs';

const Tutorial = ({onOk}) => {

  const [slider]  = useState(["first", "second", "third"]);
  const [activeIndex, setActiveIndex]  = useState(1);
  const [left] = useState(0);
  const localValue = (localStorage.getItem('showTutorial') === null || localStorage.getItem('showTutorial') === 'true') ? true : false;

  const [doNotShow, setDoNotShow] = useState(localValue);

  const handleDoNotShow = () => {
    setDoNotShow(state => !state);
  }

  useEffect(() => {
    localStorage.setItem('showTutorial', doNotShow);
  }, [doNotShow]);

  const prevSlide = () => {
    if (activeIndex === 1) {
      setActiveIndex(activeIndex => activeIndex + slider.length - 1);
    } else {
      setActiveIndex(activeIndex => activeIndex - 1);
    }
  }

  const nextSlide = () => {
    if (activeIndex === slider.length) {
      setActiveIndex(activeIndex => activeIndex - slider.length + 1);
    } else {
      setActiveIndex(activeIndex => activeIndex + 1);
    }
  }
  
  const clickIndicator = (e) => {
    setActiveIndex(parseInt(e.target.textContent));
  }

  let style = {
    left: left,
    width: '100%',
    height: 'auto'
  };

  return (
    <div className={Styles.container}>
      <p>Chronos is an automatic forecasting tool using Machine Learning, meaning it can predict future values of a time series based on past data. By default, Chronos uses an automatic mode adjusts the settings to the data you provided, trying out different Machine Learning methods. It also supports a lot of advanced features and customization.</p>
      <div  className="slider-wrapper">
        <div className="slider">
          <div style={style} className={1 === activeIndex ? 'slider-item' : 'hide'}>
            <div className={Styles.tutIcon}>
              <i className="icon mbc-icon chronos"></i>
            </div>
            <div className={Styles.tutDescription}>
              <p>To get started, you need to prepare your data in a way that Chronos can understand. The easiest way to do this is to create an Excel document with the first column containing your date/time index (formatted as an Excel datetime column) and other column(s) containing your data. You can download a template of this <a href={`/chronos-templates/Chronos_Forecasting_Template.xlsx`} download={true}>here</a>.</p>
            </div>
          </div>

          <div style={style} className={2 === activeIndex ? 'slider-item' : 'hide'}>
            <div className={Styles.tutIcon}>
              <i className="icon mbc-icon document"></i>
            </div>
            <div className={Styles.tutDescription}>
              <p>Once you have prepared your data, upload it on the project start page you will see after closing this introduction. Additionally, you need to specify which frequency (e.g. monthly) your data has in the run parameters. There, you can also set a run name and tell Chronos how many future values to predict (&ldquo;Forecast Horizon&rdquo;). Optionally, you can also select a different run configuration, although the default configuration is probably fine for your first run..</p>
            </div>
          </div>

          <div style={style} className={3 === activeIndex ? 'slider-item' : 'hide'}>
            <div className={Styles.tutIcon}>
              <i className="icon mbc-icon help"></i>
            </div>
            <div className={Styles.tutDescription}>
              <p>Please be aware that forecasting quality hugely depends on the type and quality of the data you provided. While it is possible to use Chronos without knowledge about Data Science methods, we strongly recommend discussing your use case with a Data Scientist or the Chronos team before relying on the results for actual business cases.</p>
              <p>If you want to learn more, visit the Chronos documentation <a href={Envs.CHRONOS_DOCUMENTATION_URL} target='_blank' rel='noopener noreferrer'>here</a>. Feel free to also contact the Chronos team via <a href={`mailto:${Envs.ADS_EMAIL}`}>{Envs.ADS_EMAIL}</a>. For a walkthrough of the Chronos application, see <a href={`/chronos-templates/Chronos_GUI_tutorial.pptx`} download={true}>this Powerpoint presentation</a></p>
            </div>
          </div>
        </div>
        <div className="buttons-wrapper">
          <button className="prev-button" onClick={prevSlide}><i className="icon mbc-icon arrow small left"></i></button>
          <button className="next-button" onClick={nextSlide}><i className="icon mbc-icon arrow small right"></i></button>
        </div>
      </div>
      <div className="indicators-wrapper">
        <ul className="indicators">
          {slider.map((item, index) => <li key={index} className={index+1 === activeIndex ? 'active-indicator' : ''} onClick={clickIndicator}>{index+1}</li>)}
        </ul>
      </div>
      <div className={Styles.action}>
        <div className={Styles.dataTransferSection}>
          <label className="checkbox">
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                onChange={handleDoNotShow}
                defaultChecked={!doNotShow}
              />
            </span>
            
            <span className="label" style={{ marginRight: '5px' }}>
              Do not show this again
            </span>
          </label>
        </div>
        <div className={Styles.btn}>
          <button className="btn btn-secondary" onClick={onOk}>Got it, Let&apos;s Go</button>
        </div>
      </div>
    </div>
  );
}

export default Tutorial;