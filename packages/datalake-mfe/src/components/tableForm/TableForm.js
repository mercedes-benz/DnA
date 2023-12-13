import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, useFormContext, FormProvider } from "react-hook-form";
import Styles from './table-form.scss';
import SelectBox from 'dna-container/SelectBox';
import { useSelector, useDispatch } from 'react-redux';
import { setTables } from '../../redux/graphSlice';
import { datalakeApi } from '../../apis/datalake.api';


const TableFormItem = (props) => {
  const { register, formState: { errors } } = useFormContext();

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  /**
   * If the index of the current field is greater than 0, then swap the current field with the field
   * above it
   */
    const moveUp = () => {
      props.setFields(columns => {
          if (props.index > 0) {
              const _columns = [...columns];
              [_columns[props.index], _columns[props.index - 1]] = [
                  _columns[props.index - 1],
                  _columns[props.index],
              ];
              return _columns;
          }
          return columns;
      });
  };

  /**
   * It takes the current columns array, checks if the current index is less than the length of the
   * array, and if so, swaps the current index with the next index
   */
  const moveDown = () => {
      props.setFields(columns => {
          if (props.index < columns.length - 1) {
              const _columns = [...columns];
              [_columns[props.index], _columns[props.index + 1]] = [
                  _columns[props.index + 1],
                  _columns[props.index],
              ];
              return _columns;
          }
          return columns;
      });
  };

  const { field } = props;
  const index = `A${props.index}`;

  return (
    <div className={Styles.columnsWrapper}>
      <div className={Styles.columnContainer}>
        <input type="hidden" id={`${index}.name`} defaultValue={field.name} {...register(`${index}.name`)} />
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group include-error', errors[`${index}`] !== undefined && errors[`${index}`].columnName.message ? 'error' : '')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Name <sup>*</sup>
            </label>
            <div>
              <input
                type="text"
                className={classNames('input-field')}
                id={`${index}.columnName`}
                {...register(`${index}.columnName`, { required: '*Missing entry' })}
                placeholder="Type here"
                autoComplete="off"
                maxLength={55}
                defaultValue={field.columnName}
              />
              <span className={classNames('error-message')}>{errors[`${index}`] !== undefined && errors[`${index}`].columnName.message}</span>
            </div>
          </div>
          <div className={Styles.configurationContainer}>
            <div className={classNames('input-field-group include-error')}>
              <label id="typeLabel" htmlFor={`${index}.type`} className="input-label">
                Type <sup>*</sup>
              </label>
              <div className="custom-select">
                <select id={`${index}.dataType`} {...register(`${index}.dataType`)}>
                  <option value={'BOOLEAN'}>BOOLEAN</option>
                  <option value={'INTEGER'}>INTEGER</option>
                  <option value={'BYTE'}>BYTE</option>
                  <option value={'SHORT'}>SHORT</option>
                  <option value={'LONG'}>LONG</option>
                  <option value={'FLOAT'}>FLOAT</option>
                  <option value={'DOUBLE'}>DOUBLE</option>
                  <option value={'DECIMAL'}>DECIMAL</option>
                  <option value={'STRING'}>STRING</option>
                  <option value={'BINARY'}>BINARY</option>
                  <option value={'DATE'}>DATE</option>
                  <option value={'TIMESTAMP'}>TIMESTAMP</option>
                  <option value={'TIMESTAMPNTZ'}>TIMESTAMPNTZ</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div className={classNames('input-field-group')}>
            <label className={classNames(Styles.inputLabel, 'input-label')}>
              Comment
            </label>
            <div>
              <input
                type="text"
                className={classNames('input-field')}
                id={`${index}.comment`} 
                {...register(`${index}.comment`)}
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
                  <input type="checkbox" className="ff-only" id={`${index}.notNullConstraintEnabled`} {...register(`${index}.notNullConstraintEnabled`)} defaultChecked={true} />
                </span>
                <span className="label">Not Null</span>
              </label>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div className={Styles.flexLayout}>
            <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={moveUp}>↑ Move Up</button>
            <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={moveDown}>↓ Move Down</button>
          </div>
          <div className={Styles.flexLayout}>
            {console.log('columnname', props.field.columnName)}
            <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={() => {props.removeItem(props.field.columnName);}}>Remove field</button>
            <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={() => props.addItem(props.index)}>Add field after</button>
          </div>
        </div>
      </div>
    </div>
  );
}

const TableFormBase = () => {
  const { register, formState: { errors } } = useFormContext();
  const { editingTable } = useSelector(state => state.graph);
  return (
    <>
      <div className={Styles.flexLayout}>
        <div className={classNames('input-field-group include-error', errors?.tableName ? 'error' : '')}>
          <label className={classNames(Styles.inputLabel, 'input-label')}>
            Table Name <sup>*</sup>
          </label>
          <div>
            <input
              type="text"
              className={classNames('input-field')}
              id="tableName"
              {...register('tableName', { required: '*Missing entry' })}
              placeholder="Type here"
              autoComplete="off"
              maxLength={55}
              defaultValue={editingTable?.name}
            />
            <span className={classNames('error-message')}>{errors?.tableName?.message}</span>
          </div>
        </div>
        <div className={Styles.configurationContainer}>
          <div className={classNames('input-field-group include-error')}>
            <label id="formatLabel" htmlFor="tableFormat" className="input-label">
              Format <sup>*</sup>
            </label>
            <div className="custom-select">
              <select id="tableFormat" {...register('tableFormat')}>
                <option value={'Parquet'}>Parquet</option>
                <option value={'ORC'}>ORC</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <div className={classNames('input-field-group include-error', errors?.tableComment ? 'error' : '')}>
        <label className={classNames(Styles.inputLabel, 'input-label')}>
          Table Comment <sup>*</sup>
        </label>
        <div>
          <input
            type="text"
            className={classNames('input-field')}
            id="tableComment"
            {...register('tableComment', { required: '*Missing entry' })}
            placeholder="Type here"
            autoComplete="off"
            maxLength={55}
            defaultValue={editingTable?.comment}
          />
          <span className={classNames('error-message')}>{errors?.tableComment?.message}</span>
        </div>
      </div>
    </>
  )
}

const TableForm = ({setToggle}) => {
  const methods = useForm();
  const { handleSubmit } = methods;
  
  const { project } = useSelector(state => state.graph);
  const dispatch = useDispatch();

  const [,setConnectors] = useState();
  const [, setLoading] = useState(true);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    datalakeApi.getConnectors()
      .then((res) => {
        setConnectors(res.data);
        setLoading(false);
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error while fetching connectors',
          'alert',
        );
        setLoading(false);
      });
  }, []);

  const [columns, setFields] = useState([]);
  const { editingTable } = useSelector(state => state.graph);

  useEffect(() => {
      if (editingTable.columns) {
          setFields(editingTable.columns);
      } else {
        addItem(0);
        SelectBox.defaultSetup();
      }
      // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editingTable]);

  const onSubmit = (data) => {
    const { tableName, tableFormat, tableComment, ...colData } = data;
    const cols = [];
    for (const key in colData) {
      cols.push(colData[key]);
    }
    const tableData = {
      tableName: tableName,
      dataFormat: tableFormat,
      description: tableComment,
      xcoOrdinate: 10,
      ycoOrdinate: 10,
      columns: [...cols],
    };
    const projectTemp = {...project};
    projectTemp.tables = [...projectTemp.tables, tableData];
    dispatch(setTables(projectTemp.tables));
    setToggle();
  }

  const addItem = index => {
    const newState = [...columns];
    newState.splice(index + 1, 0, {
        columnName: 'new item' + newState.length,
        comment: '',
        dataType: '',
        notNullConstraintEnabled: true,
    });
    setFields(newState);
  };

  const removeItem = id => {
      setFields(state => {
          const columns = state.filter(item => item.columnName !== id);
          return columns.length ? columns : [];
      });
  };

  return (
    <FormProvider {...methods} >
        <div className={Styles.form}>
          <div className={Styles.formContent}>
            <TableFormBase />
            {columns.length > 0 && 
              columns.map((field, index) => (
                <TableFormItem
                    field={field}
                    key={field.name}
                    index={index}
                    addItem={addItem}
                    removeItem={removeItem}
                    setFields={setFields}
                />
            ))}
          </div>
          <div className="drawer-footer">
            <button className="btn btn-primary" onClick={setToggle}>Cancel</button>
            <button className="btn btn-tertiary" onClick={handleSubmit((values) => onSubmit(values))}>Save</button>
          </div>
        </div>
    </FormProvider>
  )
}

export default TableForm;