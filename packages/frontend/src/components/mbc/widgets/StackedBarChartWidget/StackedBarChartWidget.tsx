import * as React from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { IStackedBarChartDataItem } from 'globals/types';
import { DataFormater } from '../../../../services/utils';

export interface IStackedBarChartWidgetProps {
  data: IStackedBarChartDataItem[];
  xAxisLabel: string;
  xAxisSubLabel?: string;
  yAxisLabel: string;
  yAxisSubLabel?: string;
  tooltipContentComponent: React.StatelessComponent<any>;
  onChartBarClick: (value: any) => void;
}

export interface IStackedBarChartWidgetState {
  dataItemsPerView: number;
  totalNumberOfPages: number;
  currentPage: number;
}

export default class StackedBarChartWidget extends React.Component<
  IStackedBarChartWidgetProps,
  IStackedBarChartWidgetState
> {
  constructor(props: IStackedBarChartWidgetProps) {
    super(props);
    this.state = {
      dataItemsPerView: 5,
      totalNumberOfPages: 2,
      currentPage: 1,
    };
  }
  public render() {
    const axisTextStyle = { fill: '#99A5B3', fontSize: 'var(--font-size-smallest)', fontFamily: 'Roboto-Medium' };
    const tooltipCursorBackground = { fill: 'white', opacity: 0.1 };
    const { data, xAxisLabel, xAxisSubLabel, yAxisLabel, yAxisSubLabel } = this.props;
    const { dataItemsPerView, totalNumberOfPages, currentPage } = this.state;
    const dataToShow = data.slice((currentPage - 1) * dataItemsPerView, dataItemsPerView * currentPage);
    return (
      <div className="chart-wrapper">
        <div className="chart-label yaxis">
          <label>{yAxisLabel}</label> {yAxisSubLabel ? <span className="sub-title-text">{yAxisSubLabel}</span> : ''}
        </div>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={dataToShow}
            margin={{
              top: 20,
              right: 5,
              left: -5,
              bottom: 15,
            }}
            style={{ cursor: 'pointer' }}
          >
            <CartesianGrid vertical={false} stroke="#383F49" />
            <XAxis dataKey="labelValue" axisLine={false} tickLine={false} tick={axisTextStyle} />
            <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} tickFormatter={DataFormater} />
            <Tooltip content={this.props.tooltipContentComponent} cursor={tooltipCursorBackground} />
            <Bar
              dataKey="fifthBarValue"
              maxBarSize={10}
              cursor="pointer"
              stackId="stack"
              onClick={this.props.onChartBarClick}
            >
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.fifthBarFillColor} />
              ))}
            </Bar>
            <Bar
              dataKey="fourthBarValue"
              maxBarSize={10}
              cursor="pointer"
              stackId="stack"
              onClick={this.props.onChartBarClick}
            >
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.fourthBarFillColor} />
              ))}
            </Bar>
            <Bar
              dataKey="thirdBarValue"
              maxBarSize={10}
              cursor="pointer"
              stackId="stack"
              onClick={this.props.onChartBarClick}
            >
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.thirdBarFillColor} />
              ))}
            </Bar>
            <Bar
              dataKey="secondBarValue"
              maxBarSize={10}
              cursor="pointer"
              stackId="stack"
              onClick={this.props.onChartBarClick}
            >
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.secondBarFillColor} />
              ))}
            </Bar>
            <Bar
              dataKey="firstBarValue"
              maxBarSize={10}
              cursor="pointer"
              stackId="stack"
              onClick={this.props.onChartBarClick}
            >
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.firstBarFillColor} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
        <div className="chart-label xaxis">
          {xAxisSubLabel ? (
            <React.Fragment>
              <span className="sub-title-text">{xAxisSubLabel}</span>&nbsp;&nbsp;
            </React.Fragment>
          ) : (
            ''
          )}{' '}
          <label>{xAxisLabel}</label>{' '}
          <i
            className={'icon mbc-icon arrow small left pointer' + (currentPage === 1 ? ' inactive' : '')}
            onClick={this.onPreviousClick}
          />
          {currentPage}/{totalNumberOfPages}{' '}
          <i
            className={
              'icon mbc-icon arrow small right pointer' + (currentPage === totalNumberOfPages ? ' inactive' : '')
            }
            onClick={this.onNextClick}
          />
        </div>
      </div>
    );
  }

  protected onPreviousClick = () => {
    this.setState({
      currentPage: this.state.currentPage - 1,
    });
  };

  protected onNextClick = () => {
    this.setState({
      currentPage: this.state.currentPage + 1,
    });
  };
}
