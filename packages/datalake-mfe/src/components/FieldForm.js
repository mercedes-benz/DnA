import classNames from 'classnames';
import React, { useEffect } from 'react';
import SelectBox from 'dna-container/SelectBox';
import Styles from './field-form.scss';

/**
 * It renders a form for editing a table
 * @param props - The props passed to the component.
 * @returns A TableForm component
 */
const FieldForm = () => {
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
                    <option id="number" value={0}>NUMBER</option>
                    <option id="varchar" value={0}>VARCHAR</option>
                    <option id="integer" value={0}>INTEGER</option>
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
            <div className={classNames('input-field-group include-error')}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                Default <sup>*</sup>
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
            </div>
            <div className={Styles.flexLayout}>
            <div className={Styles.flexLayout}>
                <label className="checkbox">
                <span className="wrapper">
                    <input type="checkbox" className="ff-only" />
                </span>
                <span className="label">Primary</span>
                </label>
                <label className="checkbox">
                <span className="wrapper">
                    <input type="checkbox" className="ff-only" />
                </span>
                <span className="label">Unique</span>
                </label>
            </div>
            <div className={Styles.flexLayout}>
                <label className="checkbox">
                <span className="wrapper">
                    <input type="checkbox" className="ff-only" />
                </span>
                <span className="label">Not Null</span>
                </label>
                <label className="checkbox">
                <span className="wrapper">
                    <input type="checkbox" className="ff-only" />
                </span>
                <span className="label">Increment</span>
                </label>
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
    );
}

export default FieldForm;
