import React, { useEffect } from 'react';
import classNames from 'classnames';
import Styles from './TextBox.scss';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';

export interface ITextBoxProps {
  type: string;
  controlId: string;
  name?: string;
  labelId?: string;
  label: string;
  required: boolean;
  maxLength: number;
  placeholder: string;
  onChange: (e: React.FormEvent<HTMLInputElement>) => void;
  onBlur?: (e: React.FormEvent<HTMLInputElement>) => void;
  onKeyUp?: (e: React.FormEvent<HTMLInputElement>) => void;
  value: string;
  errorText?: string;
  infoTip?: string;
}

const TextBox:React.FC<ITextBoxProps> = (props: ITextBoxProps) => {
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [props.infoTip]);

  return (
    <div
      className={classNames(
        'input-field-group include-error',
        props.errorText && 'error',
      )}
    >
      <label 
        id={props.labelId ? props.labelId : undefined} 
        htmlFor={props.controlId} 
        className="input-label"
      >
        {props.label} { props.required && <sup>*</sup> }
        {props.infoTip && <i className={classNames('icon mbc-icon info', Styles.infoTip)} tooltip-data={props.infoTip} />}
      </label>
      <input
        className="input-field"
        type={props.type}
        id={props.controlId}
        name={props.name !== undefined ? props.name : props.controlId}
        placeholder={props.placeholder}
        value={props.value}
        onChange={props.onChange}
        onBlur={props.onBlur ? props.onBlur : undefined}
        onKeyUp={props.onKeyUp ? props.onKeyUp : undefined}
        required={props.required}
        maxLength={props.maxLength}
        autoComplete="off"
      />
      {
        props.errorText &&
          <span className="error-message">
            {props.errorText}
          </span>
      }
    </div>
  );
};

export default TextBox;