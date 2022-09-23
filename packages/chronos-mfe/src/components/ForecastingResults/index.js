import classNames from 'classnames';
import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import { Line, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, Legend } from 'recharts';
import ContextMenu from '../shared/contextMenu/ContextMenu';
import Styles from './styles.scss';

const data = [
	{ name: "Jan 2017", kpi: 2000, trend: 3700, movingAverage: 6000 },
	{ name: "Feb 2017", kpi: 4200, trend: 3700, movingAverage: 6000 },
	{ name: "Mar 2017", kpi: 8200, trend: 3700, movingAverage: 6000 }, 
	{ name: "Apr 2017", kpi: 13000, trend: 1100, movingAverage: 2300 },
	{ name: "May 2017", kpi: 8200, trend: 3700, movingAverage: 6000 },
	{ name: "Jun 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Jul 2017", kpi: 1200, trend: 3700, movingAverage: 6000 },
	{ name: "Aug 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Sep 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Oct 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Nov 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Dec 2017", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Jan 2018", kpi: 18000, trend: 3700, movingAverage: 6000 },
	{ name: "Feb 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Mar 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Apr 2018", kpi: 3200, trend: 1100, movingAverage: 2300 },
	{ name: "May 2018", kpi: 1500, trend: 3700, movingAverage: 6000 },
	{ name: "Jun 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Jul 2018", kpi: 10000, trend: 3700, movingAverage: 6000 },
	{ name: "Aug 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Sep 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Oct 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Nov 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
	{ name: "Dec 2018", kpi: 3200, trend: 3700, movingAverage: 6000 },
  { name: "Jan 2019", kpi: 20000, trend: 3700, movingAverage: 6000 },
];

const data2 = [
  {
    "month": "January",
    "A": -150,
    "B": 110,
    "fullMark": 150
  },
  {
    "month": "February",
    "A": 98,
    "B": 130,
    "fullMark": 150
  },
  {
    "month": "March",
    "A": 86,
    "B": 130,
    "fullMark": 150
  },
  {
    "month": "April",
    "A": 99,
    "B": 100,
    "fullMark": 150
  },
  {
    "month": "May",
    "A": 85,
    "B": 90,
    "fullMark": 150
  },
  {
    "month": "June",
    "A": 65,
    "B": 85,
    "fullMark": 150
  },
  {
    "month": "July",
    "A": 120,
    "B": 110,
    "fullMark": 150
  },
  {
    "month": "August",
    "A": 98,
    "B": 130,
    "fullMark": 150
  },
  {
    "month": "September",
    "A": 86,
    "B": 130,
    "fullMark": 150
  },
  {
    "month": "October",
    "A": 99,
    "B": 100,
    "fullMark": 150
  },
  {
    "month": "November",
    "A": 85,
    "B": 90,
    "fullMark": 150
  },
  {
    "month": "December",
    "A": 65,
    "B": 85,
    "fullMark": 150
  },
];

const ForecastingResults = () => {
  const history = useHistory();
  const goback = () => {
    history.goBack();
  }

  const [nerd, setNerd] = useState(false);

  /* chart */
  const axisTextStyle = { fill: '#99A5B3', fontSize: 'var(--font-size-smallest)', fontFamily: 'Roboto-Medium' };
  const tooltipCursorBackground = { fill: 'white', opacity: 0.1 };

  const onDummyClick = () => {
    console.log('dummy click');
  }
  const contextMenuItems = [
    {
      title: 'Export to PDF',
      onClickFn: onDummyClick
    },
    {
      title: 'Export to PNG',
      onClickFn: onDummyClick
    }
  ];

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={Styles.backButtonWapper}>
        <button className="btn btn-text back arrow" type="submit" onClick={goback}>
          Result Overview
        </button>
        <div className={Styles.summeryBannerTitle}>
          <h2>Forecasting Results &quot;2022-07-29_Test-Run&quot;</h2>
          <div className={Styles.switch}>
            <label className={classNames('switch', nerd && 'on')}>
              <span className="label" style={{ marginRight: '5px' }}>
                Stats for Nerds
              </span>
              <span className="wrapper">
                <input
                  value={nerd}
                  type="checkbox"
                  className="ff-only"
                  onChange={() => setNerd(!nerd)}
                  // checked={nerd}
                />
              </span>
            </label>
          </div>
        </div>
      </div>
      <div className={Styles.content}>
        <div className={Styles.header}>
          <h3>Trend</h3>
          <p>Trend component, determined based on different approaches</p>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'trend'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel}>
          <ResponsiveContainer width="100%" height={450}>
            <LineChart
              data={data}
              margin={{
                top: 20,
                right: 5,
                left: -20,
                bottom: 15,
              }}
              style={{ cursor: 'pointer' }}
            >
              <Line type="linear" dataKey="kpi" stroke="#00adf0" strokeWidth={2} dot={false} />
              <Line type="linear" dataKey="trend" stroke="#33adac" strokeWidth={2} dot={false} />
              <Line type="linear" dataKey="movingAverage" stroke="#979797" strokeDasharray="5 5" dot={false} />
              <CartesianGrid stroke="#383F49" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={axisTextStyle} />
              <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} />
              <Tooltip cursor={tooltipCursorBackground} />
              <Legend />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
      <div className={Styles.content}>
        <div className={Styles.header}>
          <h3>Decomposition</h3>
          <p>Decomposing the target series into a trend, seasonal<br />and residual component</p>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'decomposition'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel}>
          <ResponsiveContainer width="100%" height={450}>
            <LineChart
              data={data}
              margin={{
                top: 20,
                right: 5,
                left: -20,
                bottom: 15,
              }}
              style={{ cursor: 'pointer' }}
            >
              <Line type="linear" dataKey="kpi" stroke="#00adf0" strokeWidth={2} dot={false} />
              <Line type="linear" dataKey="trend" stroke="#33adac" strokeWidth={2} dot={false} />
              <Line type="linear" dataKey="movingAverage" stroke="#979797" strokeDasharray="5 5" dot={false} />
              <CartesianGrid stroke="#383F49" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={axisTextStyle} />
              <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} />
              <Tooltip cursor={tooltipCursorBackground} />
              <Legend />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
      <div className={Styles.content}>
        <div className={Styles.header}>
          <h3>Monthly Seasonality</h3>
          <p>Allows for conclusions regarding the strength of<br/>the seasonal component</p>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'seasonality'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel}>
          <RadarChart outerRadius={300} width={1080} height={750} data={data2} startAngle={150} endAngle={-210}>
            <PolarGrid stroke="#383F49" />
            <PolarAngleAxis dataKey="month" tick={axisTextStyle} stroke="#383F49" />
            <PolarRadiusAxis angle={0} domain={[0, 150]} tick={axisTextStyle} stroke="#383F49" />
            <Radar name="Mike" dataKey="A" stroke="#00adf0" fill="#8884d8" fillOpacity={0} />
            <Radar name="Lily" dataKey="B" stroke="#33adac" fill="#82ca9d" fillOpacity={0} />
            <Tooltip />
          </RadarChart>
        </div>
      </div>
    </div>
  );
}
export default ForecastingResults;

