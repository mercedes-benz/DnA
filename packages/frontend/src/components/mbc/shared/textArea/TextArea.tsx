import React from 'react';
import classNames from 'classnames';

export interface ITextAreaProps {
  controlId: string;
  name?: string;
  labelId?: string;
  label: string;
  rows?: number;
  maxlength?: number;
  required: boolean;
  onChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  onBlur?: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  value: string;
  errorText?: string;
  small?: boolean;
  containerId?: string;
}

const TextArea:React.FC<ITextAreaProps> = (props: ITextAreaProps) => {
  const isSmall = props.small !== undefined ? props.small : undefined;
  return (
    <div
      id={props.containerId}
      className={classNames(
        'input-field-group include-error',
        !isSmall && 'area',
        props.errorText && 'error',
      )}
    >
    <label 
      id={props.labelId !== undefined && props.labelId}
      htmlFor={props.controlId} 
      className="input-label"
    >
      {props.label}{ props.required && <sup>*</sup> }
    </label>
    <textarea
      className={classNames('input-field-area', isSmall && 'small')}
      id={props.controlId}
      name={props.name !== undefined ? props.name : props.controlId}
      value={props.value}
      rows={props.rows}
      maxLength={props.maxlength !== undefined ? props.maxlength : undefined}
      onChange={props.onChange}
      onBlur={props.onBlur !== undefined ? props.onBlur : undefined}
      required={props.required}
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

export default TextArea;