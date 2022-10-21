// import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
// import tourGuideImage from '../../appIcon/guideTour.png';
import Styles from './styles.scss';
import Modal from 'dna-container/Modal';
import { LOCAL_STORAGE_KEYS } from '../../Utility/constants';
import { Envs } from '../../Utility/envs';
// import { consumerGuideData, providerGuideData } from './content';

const TourGuide = () =>
  // { type = 'ProviderForm' }
  {
    // const [step, setStep] = useState(0);
    const [showTourGuide, setShowTourGuide] = useState(false);
    const [hideTourGuideChecked, setHideTourGuideChecked] = useState(false);

    // const isProviderForm = type === 'ProviderForm';
    // const guideData = isProviderForm ? providerGuideData : consumerGuideData;

    useEffect(() => {
      const valueInLS = localStorage.getItem(LOCAL_STORAGE_KEYS.TOUR_GUIDE_STATUS);
      const showGuide = valueInLS !== null ? JSON.parse(valueInLS) : true;
      setShowTourGuide(showGuide);
    }, []);

    const guideContent = (
      <div className="tourGuideContainer">
        <div className={Styles.tourGuidesummary}>
          When sharing data internally, i.e. within MB Group, the{' '}
          <a href={Envs.INFORMATION_POLICY_LINK}>Global Data & Information Policy A22</a> requires the responsible
          Information Owner(s) usually coordinated by the corresponding business owner of application to:
          <ul style={{ margin: '10px 0px' }}>
            <li>Document minimum information for their data before sharing it (task on data providing side)</li>
            <li>
              Familiarize themselves with and implementing the minimum information of data they receive before
              processing it (task on data receiving side)
            </li>
          </ul>
          To enable you to comply with these requirements, we, the Data Governance Office FM together with our
          colleagues from DnA Platform are happy to introduce the latest version of the A22 Minimum Information Process.
        </div>
        {/* {guideData.map((item, index) => {
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
      })} */}
        {/* <div className={Styles.bullets}>
        {[...Array(guideData?.length)].map((i, ind) => (
          <span onClick={() => setStep(ind)} className={ind === step ? Styles.activeBullet : ''} key={ind}></span>
        ))}
      </div> */}
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
        // title={isProviderForm ? 'What purpose does this process serve' : 'What purpose does this process serve'}
        title="What purpose does this process serve ?"
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
