import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import styles from './forecasting-results.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../../components/spinner/Spinner';
import VisualContainer from '../../components/visualContainer/VisualContainer';

const ForecastingResults = () => {
  const printRef = React.useRef();

  const history = useHistory();
  const goback = () => {
    history.goBack();
  }
  const { projectid: projectId, runid: runId } = useParams();

  const [nerdStats, setNerdStats] = useState(false);
  const [html, setHtml] = useState('');
  const [htmlLoading, setHtmlLoading] = useState(true);
  const [bucketName, setBucketName] = useState('');

  const [columns, setColumns] = useState([]);

  const [loading, setLoading] = useState(true);
  const [forecastRun, setForecastRun] = useState([]);
  const [forecastData, setForecastData] = useState([]);
  const [trendData, setTrendData] = useState([]);
  const [decompositionData, setDecompositionData] = useState([]);
  useEffect(() => {
    getForecastRun();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setBucketName(res.data.bucketName);
    }).catch(() => { });
  }, [projectId]);

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

        let y = '';
        if(res.data.y.charAt(0) === ',') {
          y = 'date' + res.data.y;
        } else {
          const numOfCharsToDelete = res.data.y.indexOf(',');
          const newDataString = res.data.y.slice(numOfCharsToDelete);
          y = 'date' + newDataString;
        }
        const yObj = csvToJSON(y);
        let yPred = '';
        if(res.data.yPred.charAt(0) === ',') {
          yPred = 'date' + res.data.yPred;
        } else {
          const numOfCharsToDelete = res.data.yPred.indexOf(',');
          const newDataString = res.data.yPred.slice(numOfCharsToDelete);
          yPred = 'date' + newDataString;
        }
        const yPredObj = csvToJSON(yPred);
        const forecastObj = [...yObj, ...yPredObj].filter(obj => obj.date !== '');
        setForecastData(forecastObj);
        const cols = Object.keys(forecastObj[0]);
        const newCols = cols.filter(item => item !== 'date');
        setColumns(newCols.slice(0, 10));

        const eda = JSON.parse(res.data.eda);
        const stlTrend = JSON.parse(eda[newCols[0]].decomposition.stl_decomposition.stl_trend);
        const movingAverage = JSON.parse(eda[newCols[0]].moving_average.exponentially_weighted_moving_average);
        const stlSeasonal = JSON.parse(eda[newCols[0]].decomposition.stl_decomposition.stl_seasonal);
        const stlResid = JSON.parse(eda[newCols[0]].decomposition.stl_decomposition.stl_resid);
        
        let trend = {};
        for (let [key, value] of Object.entries(stlTrend)) {
          let d = new Date(parseInt(key));
          let y = d.getFullYear();
          let m = (d.getMonth() + 1).toString().padStart(2, "0");
          let keyName = y + '-' + m;
          trend[keyName] = value;
        }

        let seasonal = {};
        for (let [key, value] of Object.entries(stlSeasonal)) {
          let d = new Date(parseInt(key));
          let y = d.getFullYear();
          let m = (d.getMonth() + 1).toString().padStart(2, "0");
          let keyName = y + '-' + m;
          seasonal[keyName] = value;
        }

        let resid = {};
        for (let [key, value] of Object.entries(stlResid)) {
          let d = new Date(parseInt(key));
          let y = d.getFullYear();
          let m = (d.getMonth() + 1).toString().padStart(2, "0");
          let keyName = y + '-' + m;
          resid[keyName] = value;
        }

        let trendData = [];
        let count = 0;
        for (let [key, value] of Object.entries(trend)) {
          trendData.push({
            date: key,
            [newCols[0]]: forecastObj[count][newCols[0]],
            "Trend": value,
            "Moving Average": movingAverage[key]
          });
          count++;
        }
        setTrendData(trendData);

        let decompositionData = [];
        for (let [key, value] of Object.entries(trend)) {
          decompositionData.push({
            date: key,
            "Trend": value,
            "Seasonal": seasonal[key],
            "Residual": resid[key]
          });
        }
        setDecompositionData(decompositionData);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const handleNerdStats = () => {
    setNerdStats(!nerdStats);
    if(forecastRun) {
      chronosApi.getHTML(`${bucketName}`, `${forecastRun.id}-${forecastRun.runName}`, 'results.html').then((res) => {
        setHtml(res.data);
        setHtmlLoading(false);
      }).catch(() => {
        setHtmlLoading(false);
      });
    }
  }

  const layout = {  
    colorway: ['#00ADF0', '#33ADAC', '#E0144C', '#EED180', '#AA8B56', '#A8E890', '#6F38C5', '#9E7676', '#D0B8A8', '#7895B2'],
    margin: {b: 0, r: 20, t: 25},
    autosize: true, 
    title: '', 
    paper_bgcolor: 'transparent',
    plot_bgcolor: '#252a33',
    yaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    xaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    font: {
      color: '#99A5B3', 
      family: 'Roboto-Medium', 
      size: 'var(--font-size-smallest)'
    },
    showlegend: true,
    legend: {
      orientation: 'h', 
      font: {
        color: '#d9dfe4', 
        family: 'Roboto-Medium', 
        size: 'var(--font-size-smallest)'
      }
    },
    hoverlabel: {
      bgcolor: '#252a33',
      bordercolor: '#99A5B3'
    },
    modebar: {
      bgcolor: 'transparent',
      remove: ["lasso", "lasso2d"]
    },
    hovermode: "x unified",
    // dragmode: "pan",
    shapes: [
      {
        type: 'rect',
        xref: 'x',
        yref: 'paper',
        x0: (!loading && forecastData.length > 0) && forecastData[forecastData.length - parseInt(forecastRun.forecastHorizon)].date || '1959-01',
        y0: 0,
        x1: (!loading && forecastData.length > 0) && forecastData[forecastData.length - 1].date || '1960-01',
        y1: 1,
        fillcolor: '#383F49',
        opacity: 0.5,
        line: {
            width: 0
        }
      },
    ],
  };

  const addTraces = (data) => {
    let traces = [];
    
    let dates = [];
    let lines = {};

    columns.forEach(column => {
      lines[column] = {'y': []};
    });
    
    data.map(each => {
      dates.push(each.date);
      columns.forEach(column => {
        lines[column].y.push(each[column]);
      });
    });
    
    for(const [key, value] of Object.entries(lines)) {
      traces.push({
        type: 'scatter',
        mode: 'lines',
        x: dates,
        y: value.y,
        name: key
      });
    }
    
    return traces;
  }

  const layoutTrend = {  
    colorway: ['#00ADF0', '#33ADAC', '#979797'],
    margin: {b: 0, r: 20, t: 25},
    autosize: true, 
    title: '', 
    paper_bgcolor: 'transparent',
    plot_bgcolor: '#252a33',
    yaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    xaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    font: {
      color: '#99A5B3', 
      family: 'Roboto-Medium', 
      size: 'var(--font-size-smallest)'
    },
    showlegend: true,
    legend: {
      orientation: 'h', 
      font: {
        color: '#d9dfe4', 
        family: 'Roboto-Medium', 
        size: 'var(--font-size-smallest)'
      }
    },
    hoverlabel: {
      bgcolor: '#252a33',
      bordercolor: '#99A5B3'
    },
    modebar: {
      bgcolor: 'transparent',
      remove: ["lasso", "lasso2d"]
    },
    hovermode: "x unified",
    // dragmode: "pan",
  };

  const layoutDecomposition = {  
    grid: {
      rows: 3,
      columns: 1,
      pattern: 'independent',
      roworder: 'top to bottom'
    },
    colorway: ['#00ADF0', '#33ADAC', '#979797'],
    margin: {b: 0, r: 20, t: 25},
    autosize: true, 
    title: '', 
    paper_bgcolor: 'transparent',
    plot_bgcolor: '#252a33',
    yaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    xaxis: {
      gridcolor: '#383F49',
      automargin: true,
    },
    font: {
      color: '#99A5B3', 
      family: 'Roboto-Medium', 
      size: 'var(--font-size-smallest)'
    },
    showlegend: true,
    legend: {
      orientation: 'h', 
      font: {
        color: '#d9dfe4', 
        family: 'Roboto-Medium', 
        size: 'var(--font-size-smallest)'
      }
    },
    hoverlabel: {
      bgcolor: '#252a33',
      bordercolor: '#99A5B3'
    },
    modebar: {
      bgcolor: 'transparent',
      remove: ["lasso", "lasso2d"]
    },
    hovermode: "x unified",
    // dragmode: "pan",
  };

  const addTracesTrend = (data) => {
    let traces = [];
    
    let dates = [];
    let lines = {};

    const columnsTemp = [columns[0], 'Trend', 'Moving Average'];
    columnsTemp.forEach(column => {
      lines[column] = {'y': []};
    });
    
    data.map(each => {
      dates.push(each.date);
      columnsTemp.forEach(column => {
        lines[column].y.push(each[column]);
      });
    });
    
    for(const [key, value] of Object.entries(lines)) {
      if(key === 'Moving Average') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          line: {
            dash: 'dot',
          },
          x: dates,
          y: value.y,
          name: key
        });
      } else {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: key
        });
      }
    }
    
    return traces;
  }

  const addTracesDecomposition = (data) => {
    let traces = [];
    
    let dates = [];
    let lines = {};

    const columns = ['Trend', 'Seasonal', 'Residual'];
    columns.forEach(column => {
      lines[column] = {'y': []};
    });
    
    data.map(each => {
      dates.push(each.date);
      columns.forEach(column => {
        lines[column].y.push(each[column]);
      });
    });
    
    for(const [key, value] of Object.entries(lines)) {
      if(key === 'Trend') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: key
        });
      }
      if(key === 'Seasonal') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          xaxis: 'x2',
          yaxis: 'y2',
          name: key
        });
      }
      if(key === 'Residual') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          line: {
            dash: 'dot',
          },
          x: dates,
          y: value.y,
          xaxis: 'x3',
          yaxis: 'y3',
          name: key
        });
      }
    }

    return traces;
  }

  return (
    <div className={classNames(styles.mainPanel)}>
      <div className={styles.backButtonWapper}>
        <button className="btn btn-text back arrow" type="submit" onClick={goback}>
          Result Overview
        </button>
        <div className={styles.summeryBannerTitle}>
          <h2>Forecasting Results &quot;{!loading && forecastRun?.runName}&quot;</h2>
          <div className={styles.switch}>
            <label className={classNames('switch', nerdStats && 'on')}>
              <span className="label" style={{ marginRight: '5px' }}>
                Stats for Nerds
              </span>
              <span className="wrapper">
                <input
                  value={nerdStats}
                  type="checkbox"
                  className="ff-only"
                  onChange={handleNerdStats}
                  // checked={nerd}
                />
              </span>
            </label>
          </div>
        </div>
      </div>
      
      <VisualContainer
        title="Forecast"
        forecastRun={forecastRun}
        printRef={printRef}
        loading={loading}
        forecastData={forecastData}
        addTraces={addTraces}
        layout={layout}
      />

      <VisualContainer
        title="Trend"
        forecastRun={forecastRun}
        printRef={printRef}
        loading={loading}
        forecastData={trendData}
        addTraces={addTracesTrend}
        layout={layoutTrend}
      />

      <VisualContainer
        title="Decomposition"
        forecastRun={forecastRun}
        printRef={printRef}
        loading={loading}
        forecastData={decompositionData}
        addTraces={addTracesDecomposition}
        layout={layoutDecomposition}
      />

      {
        nerdStats &&
        <div className={styles.content}>
          <div className={styles.header}>
            <h3>Stats for Nerds</h3>
            {/* <div className={styles.actionMenu}>
              <ContextMenu id={'decomposition'} items={contextMenuItems} />
            </div> */}
          </div>
          <div className={styles.firstPanel} ref={printRef}>
            { htmlLoading && <Spinner /> }
            { !htmlLoading && html.length === 0 && <p>No visualization for the given data.</p> }
            { !htmlLoading && html.length > 0 &&
                <div className={styles.chartContainer}>
                  <iframe 
                    className={styles.iframe}
                    srcDoc={html}
                    width="100%"
                    height="450px"
                  ></iframe>
                </div>
            }
          </div>
        </div>
      }
      
    </div>
  );
}
export default ForecastingResults;
