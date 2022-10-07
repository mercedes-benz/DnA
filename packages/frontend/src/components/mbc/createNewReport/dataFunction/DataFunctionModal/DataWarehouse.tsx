import React from 'react';
import cn from 'classnames';
import Styles from '../DataFunction.scss';
import { IDataWarehouse, IDataWarehouseInUse } from 'globals/types';
import { IDataWarehouseErrors } from '../DataFunction';

const classNames = cn.bind(Styles);

interface DataWarehouseProps {
  dataSourceType: string;
  errors: IDataWarehouseErrors;
  onDataWarehouseChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  onDropdownChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  requiredError: string;
  dataWarehouses: IDataWarehouse[];
  commonFunctions: string[];
  connectionTypes: string[];
  dataClassifications: string[];
  dataWarehouseInUseInfo: IDataWarehouseInUse;
}

export const DataWarehouse = ({
  dataSourceType,
  dataWarehouseInUseInfo,
  errors,
  onDataWarehouseChange,
  onDropdownChange,
  requiredError,
  dataWarehouses,
  commonFunctions,
  dataClassifications,
  connectionTypes,
}: DataWarehouseProps) => {
  const commonFunctionsError = errors.commonFunctions || '';
  // const specificFunctionsError = errors.specificFunctions || '';
  // const queriesError = errors.queries || '';
  // const originalDataSourcesError = errors.dataSources || '';
  const connectTypesError = errors.connectionTypes || '';
  const dataClassificationError = errors.dataClassification || '';

  const selectedFilterValues = dataWarehouseInUseInfo;

  const dataWarehouseValue =
    dataSourceType === 'datawarehouse'
      ? selectedFilterValues.dataWarehouse?.toString()
      : selectedFilterValues.dataWarehouse;

  const commonFunctionsValue = selectedFilterValues.commonFunctions;
  // const specificFunctionsValue = selectedFilterValues.specificFunctions;
  // const queriesValue = selectedFilterValues.queries;
  // const originalDataSourcesValue = selectedFilterValues.dataSources;
  const connectionTypesValue = selectedFilterValues.connectionTypes?.toString();
  const dataClassificationValue = selectedFilterValues.dataClassification?.toString();

  const isCarla = selectedFilterValues.dataWarehouse?.toLowerCase() === 'carla';
  const conntectionTypesDropdown = isCarla
    ? connectionTypes?.filter((item) => item?.toLowerCase() === 'live connection')
    : connectionTypes;
  
  const dataClassificationDropdown = dataClassifications;

  return (
    dataSourceType === 'datawarehouse' && (
      <>
        <div className={Styles.flexLayout}>
          <div>
            <div className={classNames('input-field-group include-error', errors.dataWarehouse ? 'error' : '')}>
              <label id="dataWarehouseLabel" htmlFor="dataWarehouseInput" className="input-label">
                Data Warehouse<sup>*</sup>
              </label>
              <div className="custom-select">
                <select
                  id="dataWarehouseInput"
                  name="dataWarehouse"
                  required={true}
                  required-error={requiredError}
                  onChange={onDataWarehouseChange}
                  value={dataWarehouseValue}
                >
                  <option value={''}>Choose</option>
                  {dataWarehouses?.map((item) => (
                    <option id={item.dataWarehouse + item.id} key={item.id} value={item.dataWarehouse}>
                      {item.dataWarehouse}
                    </option>
                  ))}
                </select>
              </div>
              <span className={classNames('error-message', errors.dataWarehouse ? '' : 'hide')}>
                {errors.dataWarehouse}
              </span>
            </div>
          </div>
          <div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  connectTypesError ? 'error' : '',
                  connectionTypes?.length ? '' : 'disabled',
                )}
              >
                <label id="connectionTypeLabel" htmlFor="connectionTypeInput" className="input-label">
                  Connection Type<sup>*</sup>
                </label>
                <div className={`custom-select ${connectionTypes?.length ? '' : 'disabled'}`}>
                  <select
                    id="connectionTypeField"
                    name="connectionTypes"
                    value={connectionTypesValue}
                    onChange={onDropdownChange}
                    required={!isCarla}
                    required-error={!isCarla ? requiredError : ''}
                  >
                    {!isCarla && <option value="">Choose</option>}
                    {conntectionTypesDropdown?.map((item, ind) => (
                      <option id={item + ind} key={ind} value={item}>
                        {item}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', connectTypesError ? '' : 'hide')}>
                  {connectTypesError}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  commonFunctionsError ? 'error' : '',
                  commonFunctions?.length ? '' : 'disabled',
                )}
              >
                <label id="commonFunctionLabel" htmlFor="commonFunctionInput" className="input-label">
                  Common Functions<sup>*</sup>
                </label>
                <div className={`custom-select ${commonFunctions.length ? '' : 'disabled'}`}>
                  <select
                    id="commonFunctionField"
                    multiple={true}
                    required={true}
                    required-error={requiredError}
                    name="commonFunctions"
                    value={commonFunctionsValue}
                    onChange={onDropdownChange}
                  >
                    {commonFunctions?.map((item, ind) => (
                      <option id={item + ind} key={ind} value={item}>
                        {item}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', commonFunctionsError ? '' : 'hide')}>
                  {commonFunctionsError}
                </span>
              </div>
            </div>
          </div>
          <div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  dataClassificationError ? 'error' : '',
                  dataClassifications?.length ? '' : 'disabled',
                )}
              >
                <label id="dataClassificationLabel" htmlFor="dataClassificationInput" className="input-label">
                  Data Classification<sup>*</sup>
                </label>
                <div className={`custom-select ${dataClassifications?.length ? '' : 'disabled'}`}>
                  <select
                    id="dataClassificationField"
                    name="dataClassification"
                    value={dataClassificationValue}
                    onChange={onDropdownChange}
                    required={!isCarla}
                    required-error={!isCarla ? requiredError : ''}
                  >
                    {!isCarla && <option value="">Choose</option>}
                    {dataClassificationDropdown?.map((item, ind) => (
                      <option id={item + ind} key={ind} value={item}>
                        {item}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', dataClassificationError ? '' : 'hide')}>
                  {dataClassificationError}
                </span>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  );
};
