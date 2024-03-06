import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, useFormContext, FormProvider,useFieldArray } from "react-hook-form";
import Styles from './table-form.scss';
import SelectBox from 'dna-container/SelectBox';
import { useSelector, useDispatch } from 'react-redux';
import { setTables } from '../../redux/graphSlice';
import { calcXY } from '../../utilities/utils';
import Notification from '../../common/modules/uilab/js/src/notification';

const TableFormItem = (props) => {
  const { register, unregister, control,  formState: { errors } } = useForm();
  const {  remove } = useFieldArray({
    control,
    name: 'items',
  });
  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const { field, columnDatatypes } = props;
  const index = `A${props.index}`;


  useEffect(() => {
    SelectBox.defaultSetup();
  }, [columnDatatypes]);

  const  handleRemoveField = (index)=>{
    unregister(`items[${index}]`);
    remove(index);
    props.removeItem(props.key);
  }

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
                className={classNames("input-field")}
                id={`${index}.columnName`}
                {...register(`${index}.columnName`, {
                  required: "*Missing entry",
                  pattern: /^[a-z][a-z0-9_]*$/ ,
                  onChange: (e) => {
                    const val = e.target.value;
                    props.setColumnValues(props.index,val,'columnName' ); // Update local state
                  },
                })}
                placeholder="Type here"
                autoComplete="off"
                maxLength={55}
                autoFocus={true}
                value={field.columnName} 
              />
              <span className={classNames('error-message')}>
                {errors[`${index}`] !== undefined && errors[`${index}`].columnName.message}
                {errors[`${index}`] !== undefined && errors[`${index}`].columnName?.type === 'pattern' && 'column names can only consist of lowercase letters, numbers, and underscores ( _ ) and cannot start with numbers.'}
              </span>
            </div>
          </div>
          <div className={Styles.configurationContainer}>
            <div className={classNames('input-field-group include-error')}>
              <label id="typeLabel" htmlFor={`${index}.type`} className="input-label">
                Type <sup>*</sup>
              </label>
              <div className="custom-select">
                <select
                  id={`${index}.dataType`}
                  {...register(`${index}.dataType`,{
                    onChange:(e) => {
                      const val = e.target.value;
                      props.setColumnValues(props.index, val,'dataType'); 
                    },
                  })}

                >
                  {columnDatatypes.length > 0 &&
                    columnDatatypes?.map((datatype) => {
                      return (
                        <option id={datatype} key={datatype} value={datatype}>
                          {datatype}
                        </option>
                      )
                    })}
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
                {...register(`${index}.comment`,{
                  onChange:(e) => {
                    const val = e.target.value;
                    props.setColumnValues(props.index, val,'comment'); 
                  },
                })}
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
                  <input
                    type="checkbox"
                    className="ff-only"
                    id={`${index}.notNullConstraintEnabled`}
                    {...register(`${index}.notNullConstraintEnabled`,{
                      onChange:(e) => {
                        const val = e.target.checked;
                        props.setColumnValues(props.index, val,'notNullConstraintEnabled'); 
                      },
                    })}
                    defaultChecked={true}
                  />
                </span>
                <span className="label">Not Null</span>
              </label>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={() => handleRemoveField(index)}>Remove field</button>
          <button className={classNames('btn btn-primary', Styles.btnActions)} onClick={() => props.addItem(props.index)}>Add field after</button>
        </div>
      </div>
    </div>
  );
}

const TableFormBase = ({formats}) => {
  useEffect(() => {
    SelectBox.defaultSetup();
  }, [formats]);

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
              {...register('tableName', { required: '*Missing entry', pattern: /^[a-z][a-z0-9_]*$/ })}
              placeholder="Type here"
              autoComplete="off"
              maxLength={55}
              defaultValue={editingTable?.name}
            />
            <span className={classNames('error-message')}>{errors?.tableName?.message}{errors.tableName?.type === 'pattern' && 'Table names can only consist of lowercase letters, numbers, and underscores ( _ ) and cannot start with numbers.'}</span>
          </div>
        </div>
        <div className={Styles.configurationContainer}>
          <div className={classNames('input-field-group include-error')}>
            <label id="formatLabel" htmlFor="tableFormat" className="input-label">
              Format <sup>*</sup>
            </label>
            <div className="custom-select">
              <select id="tableFormat" {...register('tableFormat')}>
                {formats.length > 0 && 
                  formats?.map((format) => {
                    return (
                    <option id={format} key={format} value={format}>
                      {format}
                    </option>
                    )
                })}
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

const TableForm = ({setToggle, formats, dataTypes}) => {
  const methods = useForm();
  const { handleSubmit } = methods;
  
  const { project, box, editingTable } = useSelector(state => state.graph);
  const dispatch = useDispatch();

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const [columns, setFields] = useState([]);

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
    const { tableName, tableFormat, tableComment}  = data;
    const [x, y] = calcXY([...project.tables], box);
    const tableData = {
      tableName: tableName,
      dataFormat: tableFormat,
      description: tableComment,
      xcoOrdinate: x,
      ycoOrdinate: y,
      columns: [...columns],
    };
    const projectTemp = {...project};
    if(projectTemp.tables.filter(table => table.tableName === tableName).length > 0) {
      Notification.show(`Table with the name ${tableName} already exists.`, 'alert');
    } else {
      projectTemp.tables = [...projectTemp.tables, tableData];
      dispatch(setTables(projectTemp.tables));
      setToggle();
    }
  }

  const addItem = index => {  
    const newState = [...columns];
    newState.splice(index + 1, 0, {      
   columnName: 'new_column' + newState.length,
   comment: '',     
   dataType: '', 
   notNullConstraintEnabled: true,  
    });  
      setFields(newState); 
   };

  const removeItem = (id) => {
  
    if(columns.length > 1){
      const newt = columns.filter(item => item.columnName !== id);
      columns.length ? setFields([...newt]) : setFields([]);
    }else{
      Notification.show('Table should contain atleast one column', 'alert');
    }
  };

  const onColumnValueChange = (index,value,field) => {
    const cols = [...columns];
    cols[index] = {
      ...cols[index],
      [field]: value,
    };
    setFields(cols);
  };



  return (
    <FormProvider {...methods} >
        <div className={Styles.form}>
          <div className={Styles.formContent}>
            <TableFormBase formats={formats} />
            {columns.length > 0 && 
              columns.map((field, index) => (
                <TableFormItem
                    field={field}
                    key={field.columnName}
                    index={index}
                    addItem={addItem}
                    removeItem={removeItem}
                    setFields={setFields}
                    columnDatatypes={dataTypes}
                    setColumnValues = {onColumnValueChange}
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