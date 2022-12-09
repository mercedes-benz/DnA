import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import ContextMenu from '../shared/contextMenu/ContextMenu';
import Styles from './styles.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../shared/spinner/Spinner';
import Plot from 'react-plotly.js';

const ForecastingResults = () => {
  const printRef = React.useRef();

  const history = useHistory();
  const goback = () => {
    history.goBack();
  }
  const { projectid: projectId, runid: runId } = useParams();

  const [nerd, setNerd] = useState(false);

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
          y = res.data.y;
        }
        const yObj = csvToJSON(y);
        let yPred = '';
        if(res.data.yPred.charAt(0) === ',') {
          yPred = 'date' + res.data.yPred;
        } else {
          yPred = res.data.yPred;
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
          console.log(`${key}: ${value}`);
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

  const exportToPdf = async () => {
    const element = printRef.current;
    const canvas = await html2canvas(element);
    const data = canvas.toDataURL('image/png');

    const pdf = new jsPDF('l');
    const imgProperties = pdf.getImageProperties(data);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight =
      (imgProperties.height * pdfWidth) / imgProperties.width;

    pdf.addImage(data, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(forecastRun?.runName + '.pdf');
  }
  const exportToPng = async () => {
    const element = printRef.current;
    const canvas = await html2canvas(element);

    const data = canvas.toDataURL('image/png');
    const link = document.createElement('a');

    if (typeof link.download === 'string') {
      link.href = data;
      link.download = forecastRun?.runName + '.png';

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      window.open(data);
    }
  }
  const contextMenuItems = [
    {
      title: 'Export to PDF',
      onClickFn: exportToPdf
    },
    {
      title: 'Export to PNG',
      onClickFn: exportToPng
    }
  ];

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
    dragmode: "pan",
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
    dragmode: "pan",
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
    dragmode: "pan",
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
            <ContextMenu id={'forecast'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel} ref={printRef}>
          { loading && <Spinner /> }
          { !loading && forecastData.length === 0 && <p>No visualization for the given data.</p> }
          { !loading && forecastData.length > 0 &&
              <>
                <p className={Styles.chartLabel}>Forecast</p>
                <div className={Styles.chartContainer}>
                  <Plot
                    data={addTraces(forecastData)}
                    layout={layout}
                    useResizeHandler
                    config={{ scrollZoom: true, displaylogo: false }}
                    style={{width: '100%', height: '450px'}}
                  />
                </div>
              </>
          }
        </div>
      </div>

      <div className={Styles.content}>
        <div className={Styles.header}>
          <h3>Trend</h3>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'trend'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel} ref={printRef}>
          { loading && <Spinner /> }
          { !loading && trendData.length === 0 && <p>No visualization for the given data.</p> }
          { !loading && trendData.length > 0 &&
              <div className={Styles.chartContainer}>
                <Plot
                  data={addTracesTrend(trendData)}
                  layout={layoutTrend}
                  useResizeHandler
                  config={{ scrollZoom: true, displaylogo: false }}
                  style={{width: '100%', height: '450px'}}
                />
              </div>
          }
        </div>
      </div>

      <div className={Styles.content}>
        <div className={Styles.header}>
          <h3>Decomposition</h3>
          <div className={Styles.actionMenu}>
            <ContextMenu id={'decomposition'} items={contextMenuItems} />
          </div>
        </div>
        <div className={Styles.firstPanel} ref={printRef}>
          { loading && <Spinner /> }
          { !loading && decompositionData.length === 0 && <p>No visualization for the given data.</p> }
          { !loading && decompositionData.length > 0 &&
              <div className={Styles.chartContainer}>
                <Plot
                  data={addTracesDecomposition(decompositionData)}
                  layout={layoutDecomposition}
                  useResizeHandler
                  config={{ scrollZoom: true, displaylogo: false }}
                  style={{width: '100%', height: '450px'}}
                />
              </div>
          }
        </div>
      </div>
    </div>
  );
}
export default ForecastingResults;

