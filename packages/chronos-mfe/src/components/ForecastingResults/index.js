import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { Line, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar, Legend } from 'recharts';
import Styles from './styles.scss';

let isTouch = false;

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

  /* Context Menu */
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [showLocationsContextMenu, setShowLocationsContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);

    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleContextMenuOutside = (event) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target;
    const elemClasses = target.classList;
    const cardDivElement = document.querySelector('#card-');
    const contextMenuWrapper = cardDivElement.querySelector('.contextMenuWrapper');
    const locationContextMenuWrapper = cardDivElement.querySelector('.contextMenuWrapper');

    if (
      cardDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement.contains(target) === false) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (!locationContextMenuWrapper?.contains(target)) {
      setShowLocationsContextMenu(false);
    }

    if (
      (showContextMenu || showLocationsContextMenu) &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowLocationsContextMenu(false);
    setShowContextMenu(!showContextMenu);
  };

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
          <div id={'card-'} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)} tooltip-data="More Action">
                <i className="icon mbc-icon listItem context" />
              </span>
              <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  left: contextMenuOffsetLeft + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.showMenu : 'hide')}
              >
                <ul className="contextList">
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PDF</span>
                  </li>
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PNG</span>
                  </li>
                </ul>
              </div>
            </div>
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
          <div id={'card-'} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)} tooltip-data="More Action">
                <i className="icon mbc-icon listItem context" />
              </span>
              {/* <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  left: contextMenuOffsetLeft + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.showMenu : 'hide')}
              >
                <ul className="contextList">
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PDF</span>
                  </li>
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PNG</span>
                  </li>
                </ul>
              </div> */}
            </div>
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
          <div id={'card-'} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)} tooltip-data="More Action">
                <i className="icon mbc-icon listItem context" />
              </span>
              {/* <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  left: contextMenuOffsetLeft + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.showMenu : 'hide')}
              >
                <ul className="contextList">
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PDF</span>
                  </li>
                  <li className="contextListItem" onClick={() => { console.log('dummy action')}}>
                    <span>Export as PNG</span>
                  </li>
                </ul>
              </div> */}
            </div>
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

