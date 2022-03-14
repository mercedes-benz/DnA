import cn from 'classnames';
import * as React from 'react';
import { IDataSource, IDataVolume, ITag } from '../../../../globals/types';
import Tags from '../../../formElements/tags/Tags';
import Styles from './DataSources.scss';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
const classNames = cn.bind(Styles);

export interface IDataSourcesProps {
  dataSource: IDataSource;
  dataSourcesTags: ITag[];
  dataVolumes: IDataVolume[];
  modifyDataSources: (dataSources: IDataSource) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
}

export interface IDataSourcesState {
  dataSourceNames: string[];
  dataVolumeValue: IDataVolume;
}

export default class DataSources extends React.Component<IDataSourcesProps, IDataSourcesState> {
  public static getDerivedStateFromProps(props: IDataSourcesProps, state: IDataSourcesState) {
    if (props.dataSource && props.dataSource.dataSources && props.dataSource.dataSources.length !== 0) {
      return {
        dataSourceNames: props.dataSource.dataSources,
        dataVolumeValue: props.dataSource.dataVolume,
      };
    }
    return null;
  }
  constructor(props: IDataSourcesProps) {
    super(props);
    this.state = {
      dataSourceNames: [],
      dataVolumeValue: null,
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    const dataSourcesInSolution = this.props.dataSource.dataSources ? this.props.dataSource.dataSources : [];
    const totalDataValumeInSolution = this.props.dataSource.dataVolume;
    return (
      <React.Fragment>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>List of data sources</h3>
            <div className={classNames(Styles.formWrapper)}>
              <div className={Styles.flexLayout}>
                <div>
                  <Tags
                    title={'Data Sources'}
                    max={100}
                    chips={dataSourcesInSolution ? dataSourcesInSolution : this.state.dataSourceNames}
                    setTags={this.setDataSources}
                    tags={this.props.dataSourcesTags}
                    showMissingEntryError={false}
                    {...this.props}
                  />
                </div>
                <div>
                  <div id="totalDataVolumeContainer" className="input-field-group">
                    <label id="divisionLabel" className="input-label" htmlFor="totalDataVolumeSelect">
                      Total Data Volume
                    </label>
                    <div id="totalDataVolumeCustomSelect" className="custom-select">
                      <select
                        id="totalDataVolumeSelect"
                        onChange={this.onTotalDataVolumeChange}
                        required={false}
                        value={
                          totalDataValumeInSolution
                            ? totalDataValumeInSolution.id
                              ? totalDataValumeInSolution.id
                              : 0
                            : this.state.dataVolumeValue
                            ? this.state.dataVolumeValue.id
                            : 0
                        }
                      >
                        <option id="totalDataVolumeDefault" value={0}>
                          Choose
                        </option>
                        {this.props.dataVolumes.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="btnConatiner">
          <button className="btn btn-primary" type="button" onClick={this.onDataSourcesSubmit}>
            Save & Next
          </button>
        </div>
      </React.Fragment>
    );
  }
  public resetChanges = () => {
    if (this.props.dataSource) {
      let dataSourceNames = this.state.dataSourceNames;
      if (this.props.dataSource.dataSources && this.props.dataSource.dataSources.length > 0) {
        dataSourceNames = this.props.dataSource.dataSources;
      } else if (this.props.dataSource.dataSources === null && dataSourceNames && dataSourceNames.length > 0) {
        dataSourceNames.forEach((ds) => dataSourceNames.pop());
      }
      const dataVolumeValue = this.state.dataVolumeValue;
      if (this.props.dataSource && this.props.dataSource.dataVolume) {
        dataVolumeValue.id = this.props.dataSource.dataVolume.id;
        dataVolumeValue.name = this.props.dataSource.dataVolume.name;
      }
      this.setState({
        dataSourceNames,
        dataVolumeValue,
      });
    }
  };
  protected setDataSources = (arr: string[]) => {
    this.setState({
      dataSourceNames: arr,
    });
    this.props.modifyDataSources({
      dataSources: this.state.dataSourceNames,
      dataVolume: this.state.dataVolumeValue,
    });
  };

  protected onTotalDataVolumeChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const dataVolume: IDataVolume = {};
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        dataVolume.id = option.value;
        dataVolume.name = option.textContent;
      });
    }
    this.setState({ dataVolumeValue: dataVolume.id ? dataVolume : {} });
    this.props.modifyDataSources({
      dataSources: this.state.dataSourceNames,
      dataVolume,
    });
  };

  protected onDataSourcesSubmit = () => {
    const dataSourcesInSolution = this.props.dataSource.dataSources;
    this.props.modifyDataSources({
      dataSources: dataSourcesInSolution ? dataSourcesInSolution : this.state.dataSourceNames,
      dataVolume: this.state.dataVolumeValue,
    });
    this.props.onSaveDraft('datasources');
  };
}
