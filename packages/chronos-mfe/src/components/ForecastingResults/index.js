import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { Line, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, ReferenceArea, Brush } from 'recharts';
import ContextMenu from '../shared/contextMenu/ContextMenu';
import Styles from './styles.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../shared/spinner/Spinner';

const COLORS = ['#00ADF0', '#33ADAC', '#E0144C', '#EED180', '#AA8B56', '#A8E890', '#6F38C5', '#9E7676', '#D0B8A8', '#7895B2'];

const ForecastingResults = () => {
  const history = useHistory();
  const goback = () => {
    history.goBack();
  }
  const { projectid: projectId, runid: runId } = useParams();

  const [nerd, setNerd] = useState(false);

  /* chart */
  const axisTextStyle = { fill: '#99A5B3', fontSize: 'var(--font-size-smallest)', fontFamily: 'Roboto-Medium' };
  // const tooltipCursorBackground = { fill: '#1f2124', opacity: 1 };

  const [columns, setColumns] = useState([]);

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

  const [loading, setLoading] = useState(true);
  const [forecastRun, setForecastRun] = useState([]);
  const [forecastData, setForecastData] = useState([]);
  useEffect(() => {
    getForecastRun();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const csvToJSON = (csv) => {
    let lines=csv.split("\n");
    let result = [];
    let headers=lines[0].split(",");
    for(let i=1;i<lines.length;i++){
      let obj = {};
      let currentline=lines[i].split(",");
      for(let j=0;j<headers.length;j++){
        obj[headers[j]] = currentline[j];
      }
      result.push(obj);
    }
    return result;
  }

  const getForecastRun = () => {
    ProgressIndicator.show();
    chronosApi.getForecastRun(projectId, runId).then((res) => {
      if(res.status === 204) {
        setForecastRun([]);
      } else {
        setForecastRun(res.data);
        const y = 'name' + res.data.y;
        const yObj = csvToJSON(y);
        const yPred = 'name' + res.data.yPred;
        const yPredObj = csvToJSON(yPred);
        const forecastObj = [...yObj, ...yPredObj].filter(obj => obj.name !== '');
        setForecastData(forecastObj);
        const cols = Object.keys(forecastObj[0]);
        const newCols = cols.filter(item => item !== 'name');
        setColumns(newCols.slice(0, 10));
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={Styles.backButtonWapper}>
        <button className="btn btn-text back arrow" type="submit" onClick={goback}>
          Result Overview
        </button>
        <div className={Styles.summeryBannerTitle}>
          <h2>Forecasting Results &quot;{!loading && forecastRun?.runName}&quot;</h2>
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
          <h3>Forecast</h3>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'trend'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel}>
          { loading && <Spinner /> }
          { !loading && forecastData.length === 0 && <p>No visualization for the given data.</p> }
          { !loading && forecastData.length > 0 &&
              <>
                <p className={Styles.chartLabel}>Forecast</p>
                <ResponsiveContainer width="100%" height={450}>
                  <LineChart
                    data={forecastData}
                    margin={{
                      top: 20,
                      right: 5,
                      left: -20,
                      bottom: 15,
                    }}
                    style={{ cursor: 'pointer' }}
                  >
                    {
                      columns.length > 0 && columns.map((column, index) => {
                        if(index < 10)
                          return <Line type="linear" key={column} dataKey={column} stroke={COLORS[index]} strokeWidth={2} dot={false} animationDuration={300} />
                      })
                    }
                    <CartesianGrid stroke="#383F49" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={axisTextStyle} />
                    <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} />
                    <ReferenceArea x1={forecastData[forecastData.length - parseInt(forecastRun.forecastHorizon)].name} x2={forecastData[forecastData.length - 1].name} stroke="gray" strokeOpacity={0.3} />
                    <Tooltip contentStyle={{backgroundColor: '#252a33'}} />
                    <Legend />
                    <Brush dataKey="name" height={30} />
                  </LineChart>
                </ResponsiveContainer>
              </>
          }
        </div>
      </div>
    </div>
  );
}
export default ForecastingResults;

