import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './table-form-temp.scss';
import SelectBox from 'dna-container/SelectBox';

function TableFormTemp() {
  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);
  return (
    <form>
      <div className={Styles.flexLayout}>
        <div className={classNames('input-field-group include-error')}>
          <label className={classNames(Styles.inputLabel, 'input-label')}>
            Table Name <sup>*</sup>
          </label>
          <div>
            <input
              type="text"
              className={classNames('input-field')}
              id="projectName"
              placeholder="Type here"
              autoComplete="off"
              maxLength={55}
            />
          </div>
        </div>
        <div className={Styles.configurationContainer}>
          <div className={classNames('input-field-group include-error')}>
            <label id="formatLabel" htmlFor="formatField" className="input-label">
              Format <sup>*</sup>
            </label>
            <div className="custom-select">
              <select>
                <option value={'ORC'}>ORC</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <div className={classNames('input-field-group include-error')}>
        <label className={classNames(Styles.inputLabel, 'input-label')}>
          Table Comment <sup>*</sup>
        </label>
        <div>
          <input
            type="text"
            className={classNames('input-field')}
            id="projectName"
            placeholder="Type here"
            autoComplete="off"
            maxLength={55}
          />
        </div>
      </div>
      <div className={Styles.columnContainer}>
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group include-error')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Name <sup>*</sup>
            </label>
            <div>
              <input
                type="text"
                className={classNames('input-field')}
                id="projectName"
                placeholder="Type here"
                autoComplete="off"
                maxLength={55}
              />
            </div>
          </div>
          <div className={Styles.configurationContainer}>
            <div className={classNames('input-field-group include-error')}>
              <label id="configurationLabel" htmlFor="configurationField" className="input-label">
                Type <sup>*</sup>
              </label>
              <div className="custom-select">
                <select>
                  <option value={'BOOLEAN'}>BOOLEAN</option>
                  <option value={'TINYINT'}>TINYINT</option>
                  <option value={'SMALLINT'}>SMALLINT</option>
                  <option value={'INTEGER'}>INTEGER</option>
                  <option value={'BIGINT'}>BIGINT</option>
                  <option value={'REAL'}>REAL</option>
                  <option value={'DOUBLE'}>DOUBLE</option>
                  <option value={'DECIMAL'}>DECIMAL</option>
                  <option value={'VARCHAR'}>VARCHAR</option>
                  <option value={'CHAR'}>CHAR</option>
                  <option value={'VARBINARY'}>VARBINARY</option>
                  <option value={'JSON'}>JSON</option>
                  <option value={'DATE'}>DATE</option>
                  <option value={'TIME'}>TIME</option>
                  <option value={'TIMESTAMP'}>TIMESTAMP</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group include-error')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Comment <sup>*</sup>
            </label>
            <div>
              <input
                type="text"
                className={classNames('input-field')}
                id="projectName"
                placeholder="Type here"
                autoComplete="off"
                maxLength={55}
              />
            </div>
          </div>
          <div className={classNames('input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Constraint
            </label>
            <div>
              <label className="checkbox">
                <span className="wrapper">
                  <input type="checkbox" className="ff-only" />
                </span>
                <span className="label">Not Null</span>
              </label>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div className={Styles.flexLayout}>
            <button className={classNames('btn btn-primary', Styles.btnActions)}>Move Up</button>
            <button className={classNames('btn btn-primary', Styles.btnActions)}>Move Down</button>
          </div>
          <div className={Styles.flexLayout}>
            <button className={classNames('btn btn-primary', Styles.btnActions)}>Remove field</button>
            <button className={classNames('btn btn-primary', Styles.btnActions)}>Add field after</button>
          </div>
        </div>
      </div>
    </form>
  )
}

export default TableFormTemp;