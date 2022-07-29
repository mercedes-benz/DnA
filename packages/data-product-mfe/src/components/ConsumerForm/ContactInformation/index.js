import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from '../common.styles.scss';

// components from container app
import SelectBox from 'dna-container/SelectBox';
import InfoModal from 'dna-container/InfoModal';

import { useFormContext } from 'react-hook-form';
import { hostServer } from '../../../server/api';

import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

const ContactInformation = ({ onSave, divisions, setSubDivisions, subDivisions }) => {
  const {
    register,
    formState: { errors, isSubmitting, dirtyFields },
    watch,
    resetField,
    handleSubmit,
    trigger,
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  useEffect(() => {
    const id = watch('division');
    if (id > 0) {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + id).then((res) => {
        setSubDivisions(res.data);
        (dirtyFields.division || dirtyFields.subDivision) && resetField('subDivision', { defaultValue: '0' });
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      });
    } else {
      resetField('subDivision', { defaultValue: '0' });
      (dirtyFields.division || dirtyFields.subDivision) && SelectBox.defaultSetup();
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [watch('division')]);

  useEffect(() => {
    SelectBox.defaultSetup();
    reset(watch());
    //eslint-disable-next-line
  }, []);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={classNames(Styles.firstPanel, 'descriptionSection')}>
          <div>
            <h3>Contact Information</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.businessOwner ? 'error' : '')}>
                <label id="businessOwnerLabel" htmlFor="businessOwnerInput" className="input-label">
                  Business and/or Information Owner <sup>*</sup>
                </label>
                <input
                  {...register('businessOwner', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="businessOwnerInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.businessOwner?.message}</span>
              </div>
              <div className={classNames(Styles.flexLayout)}>
                <div className={Styles.divisionContainer}>
                  <div
                    className={classNames(
                      `input-field-group include-error ${errors?.division?.message ? 'error' : ''}`,
                    )}
                  >
                    <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                      Division <sup>*</sup>
                    </label>
                    <div className="custom-select" onBlur={() => trigger('division')}>
                      <select
                        id="divisionField"
                        required={true}
                        required-error={'*Missing entry'}
                        {...register('division', {
                          validate: (value) => value !== '0' || '*Missing entry',
                          // onChange: () => resetField('subDivision'),
                        })}
                      >
                        <option id="divisionOption" value={0}>
                          Choose
                        </option>
                        {divisions?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span className={classNames('error-message')}>{errors?.division?.message}</span>
                  </div>
                </div>
                <div className={Styles.subDivisionContainer}>
                  <div className={classNames('input-field-group')}>
                    <label id="subDivisionLabel" htmlFor="subDivisionField" className="input-label">
                      Sub Division
                    </label>
                    <div className="custom-select">
                      <select id="subDivisionField" {...register('subDivision')}>
                        {subDivisions?.some((item) => item.id === '0' && item.name === 'None') ? (
                          <option id="subDivisionDefault" value={0}>
                            None
                          </option>
                        ) : (
                          <>
                            <option id="subDivisionDefault" value={0}>
                              Choose
                            </option>
                            {subDivisions?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                {obj.name}
                              </option>
                            ))}
                          </>
                        )}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.department ? 'error' : '')}>
                <label id="departmentLabel" htmlFor="departmentInput" className="input-label">
                  Department <sup>*</sup>
                </label>
                <input
                  {...register('department', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="departmentInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.department?.message}</span>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div
                className={classNames('input-field-group include-error', errors.LCONeededToBeInvolved ? 'error' : '')}
              >
                <label id="dLCONeededToBeInvolvedLabel" htmlFor="LCONeededToBeInvolvedInput" className="input-label">
                  Is LCO/LCR needed to be involved / has to check legal basis of usage of personal data? <sup>*</sup>
                </label>
                <div className={Styles.radioBtns}>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('LCONeededToBeInvolved', {
                          required: '*Missing entry',
                        })}
                        type="radio"
                        className="ff-only"
                        name="LCONeededToBeInvolved"
                        value="No"
                      />
                    </span>
                    <span className="label">No</span>
                  </label>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('LCONeededToBeInvolved', { required: '*Missing entry' })}
                        type="radio"
                        className="ff-only"
                        name="LCONeededToBeInvolved"
                        value="Yes"
                      />
                    </span>
                    <span className="label">Yes</span>
                  </label>
                </div>
                <span className={classNames('error-message')}>{errors?.LCONeededToBeInvolved?.message}</span>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.LCOOfficer ? 'error' : '')}>
                <label id="LCOOfficerLabel" htmlFor="LCOOfficerInput" className="input-label">
                  Local Compliance Officer / Local Compliance Responsible (LCO/LCR) <sup>*</sup>
                </label>
                <input
                  {...register('LCOOfficer', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="LCOOfficerInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.LCOOfficer?.message}</span>
              </div>
              <div className={classNames('input-field-group include-error', errors.planningIT ? 'error' : '')}>
                <label id="planningITLabel" htmlFor="planningITInput" className="input-label">
                  planningIT App-ID <sup>*</sup>
                </label>
                <input
                  {...register('planningIT', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="planningITInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.planningIT?.message}</span>
              </div>
            </div>
          </div>
          <div className="btnContainer">
            <button
              className="btn btn-primary"
              type="submit"
              disabled={isSubmitting}
              onClick={handleSubmit((data) => {
                console.log(data);
                onSave();
                reset(data, {
                  keepDirty: false,
                });
              })}
            >
              Save & Next
            </button>
          </div>
        </div>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
    </>
  );
};

export default ContactInformation;
