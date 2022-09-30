import * as React from 'react';
import { CartesianGrid, ResponsiveContainer, Scatter, ScatterChart, Tooltip, XAxis, YAxis, ZAxis } from 'recharts';
import { IScatterChartDataItem } from 'globals/types';

export interface IScatterChartWidgetProps {
  data: IScatterChartDataItem[];
  xAxisLabel: string;
  xAxisSubLabel?: string;
  yAxisLabel: string;
  yAxisSubLabel?: string;
  tooltipContentComponent: React.StatelessComponent<any>;
  onScatterChartBubbleClick: (value: any) => void;
}

export default class ScatterChartWidget extends React.Component<IScatterChartWidgetProps, {}> {
  constructor(props: IScatterChartWidgetProps) {
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
          <ScatterChart
            margin={{
              top: 20,
              right: 5,
              left: -20,
              bottom: 15,
            }}
          >
            <CartesianGrid vertical={false} stroke="#383F49" />
            <XAxis
              dataKey="xAxisValue"
              type="number"
              axisLine={false}
              tickLine={false}
              tick={axisTextStyle}
              domain={['auto', 'auto']}
            />
            <YAxis
              dataKey="yAxisValue"
              scale="log"
              domain={['auto', 'auto']}
              axisLine={false}
              tickLine={false}
              tick={axisTextStyle}
            />
            <ZAxis dataKey="zAxisValue" range={[100, 400]} />
            <Tooltip content={this.props.tooltipContentComponent} cursor={tooltipCursorBackground} />
            <Scatter data={data} fill="#00ADEF" cursor="pointer" onClick={this.props.onScatterChartBubbleClick} />
          </ScatterChart>
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
