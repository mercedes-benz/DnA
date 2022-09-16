import classNames from 'classnames';
import React from 'react';

import Styles from './styles.scss';

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

import { DatePicker as MUIDatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import dayjs from 'dayjs';

let language = ['en-gb', 'en', 'de', 'fr', 'it', 'pl', 'sv', 'tr', 'nl', 'nb', 'ko'];

const DatePicker = ({ label, value, name, onChange, requiredError }) => {
  const browserLang = navigator.language.toLowerCase();
  const datePickerLang = language.includes(browserLang) ? browserLang : navigator.language.split('-')?.[0];

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={datePickerLang}>
      <MUIDatePicker
        label={label}
        value={value || ''}
        name={name}
        maxDate={dayjs()}
        onChange={(newValue) => {
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
    </LocalizationProvider>
  );
};

export default DatePicker;
