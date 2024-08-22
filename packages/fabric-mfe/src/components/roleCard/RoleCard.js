import classNames from "classnames";
import React, { useState } from "react";
import Styles from './role-card.scss';
import DatePicker from 'dna-container/DatePicker';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const RoleCard = ({ role, onAdd, type }) => {
  const [validFrom, setValidFrom] = useState('');
  const [validUntil, setValidUntil] = useState('');
  const [toggle, setToggle] = useState(false);

  const handleAddRole = () => {
    setToggle(!toggle);
    validFrom && validUntil && onAdd({roleID: role?.id, validFrom: validFrom, validTo: validUntil});
  }

  const handleToggle = () => {
    type === undefined && setToggle(!toggle);
  }

  return (
    <>
      <div className={Styles.col}>
        <div className={classNames(Styles.roleContainer, !toggle && validFrom && Styles.selected)} onClick={handleToggle}>
          <div className={Styles.roleContent}>
            <h3>{type === 'display' ? role?.roleID : role?.id}</h3>
            <p>{type === 'display' ? role?.roleID?.split('_')[1] : role?.id?.split('_')[1]} role for workspace {type === 'display' ? role?.roleID?.split('_')[0] : role?.id?.split('_')[0]}</p>
            {!toggle && validFrom && 
              <p>Validity: {validFrom} to {validUntil}</p>
            }
            {type === 'display' && 
              <p>Validity: {role?.validFrom} to {role?.validTo}</p>
            }
          </div>
          {type === undefined && 
            <div className={Styles.roleCheck}>
              <i className="icon mbc-icon check circle"></i>
            </div>
          }
        </div>
      </div>
      {toggle && 
        <div className={classNames(Styles.col, Styles.validityForm)}>
          <p>Validity</p>
          <div className={Styles.flex}>
            <div className={Styles.col2}>
              <div className={'input-field-group'}>
                <label htmlFor="validFrom" className="input-label">
                  Valid From
                </label>
                <DatePicker
                  label="Valid From"
                  name={'validFrom'}
                  // minDate={minDate}
                  value={validFrom}
                  onChange={(value) => {
                    setValidFrom(regionalDateAndTimeConversionSolution(new Date(value).toISOString()).split(',')[0]);
                  }}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={'input-field-group'}>
                <label htmlFor="validUntil" className="input-label">
                  Valid Until
                </label>
                  <DatePicker
                    label="Valid Until"
                    name={'validUntil'}
                    // minDate={minDate}
                    value={validUntil}
                    onChange={(value) => {
                      setValidUntil(regionalDateAndTimeConversionSolution(new Date(value).toISOString()).split(',')[0]);
                    }}
                  />
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-primary"
              type="button"
              onClick={handleAddRole}
            >
              Add Role
            </button>
          </div>
        </div>
      }
    </>
  )
}

export default RoleCard;