import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import ReactSlider from 'react-slider';
import { IDataSource, IDataSources, IDataVolume, ITag } from 'globals/types';
import Tags from 'components/formElements/tags/Tags';
import Styles from './DataSources.scss';
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import DataSource from './dataSource/DataSource';
const classNames = cn.bind(Styles);

export interface IDataSourcesProps {
  dataSource: IDataSource;
  dataSourcesTags: ITag[];
  dataVolumes: IDataVolume[];
  modifyDataSources: (dataSources: IDataSource) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
}

export interface IDataSourcesState {
  dataSources: IDataSources[];
  dataVolumeValue: IDataVolume;
  totalWeightage: number;
}

export default class DataSources extends React.Component<IDataSourcesProps, IDataSourcesState> {
  public static getDerivedStateFromProps(props: IDataSourcesProps, state: IDataSourcesState) {
    if (props.dataSource && props.dataSource.dataSources) {
      return {
        dataSources: props.dataSource.dataSources,
        dataVolumeValue: props.dataSource.dataVolume,
      };
    }
    return null;
  }
  constructor(props: IDataSourcesProps) {
    super(props);
    this.state = {
      dataSources: [],
      dataVolumeValue: null,
      totalWeightage: 0,
    };
  }

  public componentDidMount() {
    SelectBox.defaultSetup();
  }

  public render() {
    const dataSourcesInSolution =
      this.props.dataSource.dataSources !== null
        ? this.props.dataSource.dataSources.map((item: any) => item.dataSource)
        : [];
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
                    chips={
                      dataSourcesInSolution
                        ? dataSourcesInSolution
                        : this.state.dataSources.map((item: any) => item.dataSource)
                    }
                    setTags={this.setDataSources}
                    removeTag={this.removeDataSource}
                    tags={this.props.dataSourcesTags}
                    showMissingEntryError={false}
                    isDataSource={true}
                    suggestionPopupHeight={300}
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
          <div className={Styles.weightageTableContainer}>
            {this.state.dataSources.length !== 0 ? (
              <div className={Styles.dataSourceWeightage}>
                <table>
                  <thead>
                    <tr>
                      <th>Data Source</th>
                      <th>Weightage</th>
                    </tr>
                  </thead>
                  <tbody>
                    {this.state.dataSources.map((item: any, index: any) => (
                      <DataSource
                        key={item.dataSource}
                        name={item.dataSource}
                        weightage={item.weightage}
                        list={this.props.dataSourcesTags}
                        onWeightageChange={this.handleWeightageChange(index)}
                      />
                    ))}
                    <tr>
                      <td>&nbsp;</td>
                      <td style={{ textAlign: 'right' }}>
                        <p className={Styles.totalWeightage}>
                          Total Weightage:{' '}
                          <b className={this.state.totalWeightage !== 100 ? Styles.error : Styles.success}>
                            {this.state.totalWeightage}%
                          </b>
                        </p>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <p className={Styles.note}>Total Weightage should be 100% only.</p>
              </div>
            ) : null}
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
      let dataSources = this.state.dataSources;
      if (this.props.dataSource.dataSources && this.props.dataSource.dataSources.length > 0) {
        dataSources = this.props.dataSource.dataSources;
      } else if (this.props.dataSource.dataSources === null && dataSources && dataSources.length > 0) {
        dataSources.forEach((ds) => dataSources.pop());
      }
      const dataVolumeValue = this.state.dataVolumeValue;
      if (this.props.dataSource && this.props.dataSource.dataVolume) {
        dataVolumeValue.id = this.props.dataSource.dataVolume.id;
        dataVolumeValue.name = this.props.dataSource.dataVolume.name;
      }
      this.setState({
        dataSources,
        dataVolumeValue,
      });
    }
  };

  public handleWeightageChange = (index: number) => (val: number) => {
    const dataSources = [...this.state.dataSources];
    if (isNaN(val)) {
      dataSources[index].weightage = 0;
    } else {
      dataSources[index].weightage = val;
    }
    const total = dataSources.map((ds) => ds.weightage).reduce((current, next) => current + next);
    this.setState({
      dataSources,
      totalWeightage: total,
    });
  };
  protected setDataSources = (arr: string[]) => {
    const dataSources = Array.isArray(this.state.dataSources) ? [...this.state.dataSources] : [];

    arr.forEach((element) => {
      const result = this.state.dataSources.some((i) => i.dataSource.includes(element));
      if (!result) {
        dataSources.push({ dataSource: element, weightage: dataSources.length === 0 ? 100 : 0 });
      }
    });

    const totalWeightage = dataSources.reduce((current, next) => current + next.weightage, 0);
    this.setState({
      dataSources,
      totalWeightage,
    });

    this.props.modifyDataSources({
      dataSources,
      dataVolume: this.state.dataVolumeValue,
    });
  };

  protected removeDataSource = (index: number) => {
    const dataSources = this.state.dataSources.filter((ds, dsIndex) => index !== dsIndex);

    if (dataSources.length > 0) {
      const totalWeightage = dataSources.map((i) => i.weightage).reduce((current, next) => current + next);
      this.setState({
        totalWeightage,
      });
    } else {
      this.setState({
        totalWeightage: 0,
      });
    }

    this.props.modifyDataSources({
      dataSources,
      dataVolume: this.state.dataVolumeValue,
    });
  };

  protected onTotalDataVolumeChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const dataVolume: IDataVolume = {};
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        dataVolume.id = option.value;
        dataVolume.name = option.label;
      });
    }
    this.setState({ dataVolumeValue: dataVolume.id ? dataVolume : {} });
    this.props.modifyDataSources({
      dataSources: this.state.dataSources,
      dataVolume,
    });
  };

  protected onDataSourcesSubmit = () => {
    let isZero = false;
    if (this.state.dataSources.length > 0) {
      this.state.dataSources.map((dataSource) => {
        if (dataSource.weightage === 0) {
          isZero = true;
        }
      });
    }

    if (isZero) {
      Notification.show("Data source weightage can't be 0. Please remove it or set the weightage.", 'alert');
    } else if (this.state.totalWeightage === 100 || this.state.dataSources.length === 0) {
      const dataSourcesInSolution = this.props.dataSource.dataSources;
      this.props.modifyDataSources({
        dataSources: dataSourcesInSolution ? dataSourcesInSolution : this.state.dataSources,
        dataVolume: this.state.dataVolumeValue,
      });
      this.props.onSaveDraft('datasources');
    } else {
      Notification.show('Total Weightage should be 100% only.', 'alert');
    }
  };
}
