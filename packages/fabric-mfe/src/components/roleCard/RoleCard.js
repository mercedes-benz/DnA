import classNames from "classnames";
import React, { useState } from "react";
import Styles from './role-card.scss';
import DatePicker from 'dna-container/DatePicker';
import Notification from "../../common/modules/uilab/js/src/notification";

const RoleCard = ({ role, onAdd, type }) => {
  const [validFrom, setValidFrom] = useState(role?.isSelected ? role?.validFrom: '');
  const [validTo, setValidTo] = useState(role?.isSelected ? role?.validTo: '');
  const [toggle, setToggle] = useState(false);

  const minDate = new Date();

  const handleAddRole = () => {
    if(validFrom && validTo) {
      setToggle(!toggle);
      onAdd({roleID: role?.id, validFrom: validFrom, validTo: validTo});
    } else {
      Notification.show('Please select Valid From and Valid Until to add role', 'alert');
    }
  }

  const handleToggle = () => {
    type === undefined && setToggle(!toggle);
  }

  return (
    <>
      <div className={Styles.col}>
        <div className={classNames(Styles.roleContainer, ((!toggle && validFrom) || role?.isSelected) && Styles.selected, (type !== 'display' && role?.state !== 'CREATED') && Styles.disabled)} onClick={handleToggle}>
          <div className={Styles.roleContent}>
            <h3>{type === 'display' ? role?.roleID : role?.id}</h3>
            <p>{type === 'display' ? role?.roleID?.split('_').pop() : role?.id?.split('_').pop()} role for workspace {type === 'display' ? role?.roleID?.split('_')[0] : role?.id?.split('_')[0]}</p>
            {((!toggle && validFrom) || role?.isSelected) && 
              <p>Validity: {validFrom} to {validTo}</p>
            }
            {type === 'display' && 
              <p>Validity: {role?.validFrom} to {role?.validTo}</p>
            }
          </div>
          {type === undefined && ((!toggle && validFrom) || role?.isSelected) &&
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
                  minDate={minDate}
                  value={validFrom}
                  onChange={(value) => {
                    setValidFrom(new Date(value).toLocaleDateString('en-CA'));
                  }}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={'input-field-group'}>
                <label htmlFor="validTo" className="input-label">
                  Valid Until
                </label>
                  <DatePicker
                    label="Valid Until"
                    name={'validTo'}
                    minDate={minDate}
                    value={validTo}
                    onChange={(value) => {
                      setValidTo(new Date(value).toLocaleDateString('en-CA'));
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