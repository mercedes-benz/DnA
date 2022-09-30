import * as React from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { IBarChartDataItem } from 'globals/types';

export interface IBarChartWidgetProps {
  data: IBarChartDataItem[];
  xAxisLabel: string;
  xAxisSubLabel?: string;
  yAxisLabel: string;
  yAxisSubLabel?: string;
  tooltipContentComponent: React.StatelessComponent<any>;
  onChartBarClick: (value: any) => void;
}

export default class BarChartWidget extends React.Component<IBarChartWidgetProps, {}> {
  constructor(props: IBarChartWidgetProps) {
    super(props);
  }
  public render() {
    const axisTextStyle = { fill: '#99A5B3', fontSize: 'var(--font-size-smallest)', fontFamily: 'Roboto-Medium' };
    const tooltipCursorBackground = { fill: 'white', opacity: 0.1 };
    const { data, xAxisLabel, xAxisSubLabel, yAxisLabel, yAxisSubLabel } = this.props;
    return (
      <div className="chart-wrapper">
        <div className="chart-label yaxis">
          <label>{yAxisLabel}</label> {yAxisSubLabel ? <span className="sub-title-text">{yAxisSubLabel}</span> : ''}
        </div>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={data}
            margin={{
              top: 20,
              right: 5,
              left: -20,
              bottom: 15,
            }}
            style={{ cursor: 'pointer' }}
            onClick={this.props.onChartBarClick}
          >
            <CartesianGrid vertical={false} stroke="#383F49" />
            <XAxis dataKey="labelValue" axisLine={false} tickLine={false} tick={axisTextStyle} />
            <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} />
            <Tooltip content={this.props.tooltipContentComponent} cursor={tooltipCursorBackground} />
            <Bar dataKey="barValue" maxBarSize={10} cursor="pointer">
              {data.map((entry) => (
                <Cell key={entry.id} fill={entry.barFillColor} />
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
          <label>{xAxisLabel}</label>
        </div>
      </div>
    );
  }
}
