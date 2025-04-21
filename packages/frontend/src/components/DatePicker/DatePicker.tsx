// @ts-nocheck
import classNames from 'classnames';
import React from 'react';

import Styles from './DatePicker.scss';

import 'dayjs/locale/en-gb';
import 'dayjs/locale/en';
import 'dayjs/locale/de';
import 'dayjs/locale/fr';
import 'dayjs/locale/it';
import 'dayjs/locale/pl';
import 'dayjs/locale/sv';
import 'dayjs/locale/tr';
import 'dayjs/locale/nl';
import 'dayjs/locale/nb';
import 'dayjs/locale/ko';

import dayjs from 'dayjs';
import { DatePicker as MUIDatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';

const language = ['en-gb', 'en', 'de', 'fr', 'it', 'pl', 'sv', 'tr', 'nl', 'nb', 'ko'];

type IDatePickerProps = {
  label: string;
  value: string;
  onChange: (value: string) => void;
  requiredError: string;
  minDate?: typeof Date;
};

const DatePicker = ({ label, value, onChange, minDate, requiredError, ...restProps }: IDatePickerProps) => {
  const browserLang = navigator.language.toLowerCase();
  const datePickerLang = language.includes(browserLang) ? browserLang : navigator.language.split('-')?.[0];

  return (
    <>
      <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={datePickerLang}>
        <MUIDatePicker
          // label={label}
          value={value ? dayjs(value) : null}
          disablePast={minDate ? true : false}
          views={['year', 'month', 'day']}
          onChange={(newValue) => onChange(newValue)}
          renderInput={({ inputRef, inputProps, InputProps }) => {
            return (
              <div className={classNames(Styles.datePicker, requiredError ? 'CalendarIcon-position' : '')}>
                <input className="input-field" ref={inputRef} {...inputProps} />
                {InputProps?.endAdornment}
              </div>
            );
          }}
        />
      </LocalizationProvider>
      {/* <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={datePickerLang}>
        <MUIDatePicker
          label={label}
          value={value || ''}
          views={['year', 'month', 'day']}
          // {...restProps}
          onChange={(newValue: string) => {
            onChange(newValue);
          }}
          renderInput={({ inputRef, inputProps, InputProps }) => {
            return (
              <div className={classNames(Styles.datePicker, requiredError ? 'CalendarIcon-position' : '')}>
                <input className="input-field" ref={inputRef} {...inputProps} />
                {InputProps?.endAdornment}
              </div>
            );
          }}
        /> 
      </LocalizationProvider>*/}
    </>
  );
};

export default DatePicker;
