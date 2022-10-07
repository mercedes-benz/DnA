import React from 'react';
import cn from 'classnames';
import Styles from '../DataFunction.scss';
import { IConnectionType, IDataClassification, IDataSourceMaster, ISingleDataSources } from 'globals/types';
import { ISingleDataSourceErrors } from '../DataFunction';

const classNames = cn.bind(Styles);

interface SingleDataSourceProps {
  dataSourceType: string;
  errors: ISingleDataSourceErrors;
  onDropdownChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
  requiredError: string;
  dataClassifications: IDataClassification[];
  dataSources: IDataSourceMaster[];
  connectionTypes: IConnectionType[];
  singleDataSourceInfo: ISingleDataSources;
}

export const SingleDataSource = ({
  dataSourceType,
  errors,
  requiredError,
  connectionTypes,
  dataClassifications,
  dataSources,
  singleDataSourceInfo,
  onDropdownChange,
}: SingleDataSourceProps) => {
  const singleSourceConnectionTypesValue = singleDataSourceInfo.connectionTypes
    ?.map((connectionType: IConnectionType) => {
      return connectionType.name;
    })
    ?.toString();

  const singleSourceDataClassificationsValue = singleDataSourceInfo.dataClassifications
    ?.map((dataClassification: IDataClassification) => {
      return dataClassification.name;
    })
    ?.toString();  

  const singleSourceDataSourceValue = singleDataSourceInfo.dataSources?.map((dataSource: IDataSourceMaster) => {
    return dataSource.name;
  });

  return (
    dataSourceType === 'singledatasource' && (
      <>
        <div className={Styles.flexLayout}>
          <div>
            <div>
              <div className={classNames('input-field-group include-error', errors.dataSources ? 'error' : '')}>
                <label id="commonFunctionLabel" htmlFor="commonFunctionInput" className="input-label">
                  Data Source<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="commonFunctionField"
                    multiple={true}
                    required={true}
                    required-error={requiredError}
                    name="dataSources"
                    value={singleSourceDataSourceValue}
                    onChange={onDropdownChange}
                  >
                    {dataSources?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', errors.dataSources ? '' : 'hide')}>
                  {errors.dataSources}
                </span>
              </div>
            </div>
          </div>
          <div>
            <div>
              <div
                className={classNames(
                  'input-field-group include-error',
                  errors.dataClassification ? 'error' : ''
                )}
              >
                <label id="dataClassificationsLabel" htmlFor="dataClassificationsInput" className="input-label">
                  Data Classification<sup>*</sup>
                </label>
                <div className={`custom-select ${dataClassifications?.length ? '' : ''}`}>
                  <select
                    id="dataClassificationsField"
                    name="dataClassifications"
                    value={singleSourceDataClassificationsValue}
                    onChange={onDropdownChange}
                    required={true}
                    required-error={requiredError}
                  >
                    {<option value="">Choose</option>}
                    {dataClassifications?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', errors.dataClassification ? '' : 'hide')}>
                  {errors.dataClassification}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.flexLayout}>
          <div>
            <div>
              <div className={classNames('input-field-group include-error', errors.connectionTypes ? 'error' : '')}>
                <label id="queriesLabel" htmlFor="queriesInput" className="input-label">
                  Connection Type<sup>*</sup>
                </label>
                <div className="custom-select">
                  <select
                    id="queriesField"
                    required={true}
                    required-error={requiredError}
                    name="connectionTypes"
                    value={singleSourceConnectionTypesValue}
                    onChange={onDropdownChange}
                  >
                    <option value="">Choose</option>
                    {connectionTypes?.map((obj) => (
                      <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', errors.connectionTypes ? '' : 'hide')}>
                  {errors.connectionTypes}
                </span>
              </div>
            </div>
          </div>
        </div>
      </>
    )
  );
};
