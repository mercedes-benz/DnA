import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import tourGuideImage from '../../appIcon/guideTour.png';
import Styles from './styles.scss';
import Modal from 'dna-container/Modal';
import { LOCAL_STORAGE_KEYS } from '../../Utility/constants';
import { consumerGuideData, providerGuideData } from './content';

const TourGuide = ({ type = 'ProviderForm' }) => {
  const [step, setStep] = useState(0);
  const [showTourGuide, setShowTourGuide] = useState(false);
  const [hideTourGuideChecked, setHideTourGuideChecked] = useState(false);

  const isProviderForm = type === 'ProviderForm';
  const guideData = isProviderForm ? providerGuideData : consumerGuideData;

  useEffect(() => {
    const valueInLS = localStorage.getItem(LOCAL_STORAGE_KEYS.TOUR_GUIDE_STATUS);
    const showGuide = valueInLS !== null ? JSON.parse(valueInLS) : true;
    setShowTourGuide(showGuide);
  }, []);

  const guideContent = (
    <div className="tourGuideContainer">
      {guideData.map((item, index) => {
        return (
          <div key={index} className={index === step ? '' : 'hide'}>
            <div className={Styles.tourGuidesummary} dangerouslySetInnerHTML={{ __html: item.summary }} />
            <div className={Styles.tourGuidecontent}>
              <button
                className={classNames('btn btn-text back arrow', index === 0 ? 'hide' : '', Styles.backBtn)}
                type="submit"
                onClick={() => setStep(step - 1)}
              ></button>
              <img src={tourGuideImage} style={{ width: 300, margin: '0 20px' }} />
              <div className={Styles.tourGuidedescription}>
                <h6>Step {index + 1}</h6>
                <h4>{item.title}</h4>
                <p>{item.description}</p>
              </div>
              <button
                className={classNames(
                  'btn btn-text arrow',
                  index === guideData.length - 1 ? 'hide' : '',
                  Styles.fwdBtn,
                )}
                type="submit"
                onClick={() => setStep(step + 1)}
              ></button>
            </div>
          </div>
        );
      })}
      <div className={Styles.bullets}>
        {[...Array(guideData?.length)].map((i, ind) => (
          <span onClick={() => setStep(ind)} className={ind === step ? Styles.activeBullet : ''} key={ind}></span>
        ))}
      </div>
    </div>
  );

  const guideFooter = (
    <div className={Styles.footerContainer}>
      <div>
        <label className="checkbox">
          <span className="wrapper">
            <input
              value={hideTourGuideChecked}
              type="checkbox"
              className="ff-only"
              onChange={() => setHideTourGuideChecked(!hideTourGuideChecked)}
              defaultChecked={false}
            />
          </span>
          <span className="label">Don&apos;t show again</span>
        </label>
      </div>
      <button
        className="btn btn-secondary"
        onClick={() => {
          if (hideTourGuideChecked) {
            localStorage.setItem(LOCAL_STORAGE_KEYS.TOUR_GUIDE_STATUS, 'false');
          }
          setShowTourGuide(false);
        }}
      >
        Got it
      </button>
    </div>
  );

  return (
    <Modal
      title={isProviderForm ? 'How to easily setup a Data Transfer' : 'How to finalize the Data Transfer'}
      showAcceptButton={false}
      showCancelButton={false}
      onCancel={() => setShowTourGuide(false)}
      modalStyle={{
        width: '60%',
      }}
      buttonAlignment="right"
      show={showTourGuide}
      content={guideContent}
      scrollableContent={false}
      footer={guideFooter}
    />
  );
};

export default TourGuide;
