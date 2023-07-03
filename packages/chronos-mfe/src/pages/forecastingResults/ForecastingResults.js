import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import Styles from './forecasting-results.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../../components/spinner/Spinner';
// import SelectBox from 'dna-container/SelectBox';
import VisualContainer from '../../components/visualContainer/VisualContainer';
import { Envs } from '../../utilities/envs';

const ForecastingResults = () => {
  const printRef = React.useRef();

  const history = useHistory();
  const { projectid: projectId, runid: runId } = useParams();
  const goback = () => {
    history.push(`/project/${projectId}/forecastResults`);
  }

  const [nerdStats, setNerdStats] = useState(false);
  const [html, setHtml] = useState('');
  const [htmlLoading, setHtmlLoading] = useState(true);
  const [bucketName, setBucketName] = useState('');

  const [loading, setLoading] = useState(true);
  const [forecastRun, setForecastRun] = useState([]);
  useEffect(() => {
    getForecastRun();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const colOneSelect = useRef("");
  const colTwoSelect = useRef("");
  const [forecastDataA, setForecastDataA] = useState([]);
  const [decompositionDataA, setDecompositionDataA] = useState([]);
  const [outlierDataA, setOutlierDataA] = useState([]);

  // useEffect(() => {
  //   SelectBox.defaultSetup();
  // }, []);

  const [colOne, setColOne] = useState([]);
  const [colTwo, setColTwo] = useState([]);
  const [decompositionMethods, setDecompositionMethods] = useState([]);
  const [outlierMethods, setOutlierMethods] = useState([]);

  const handleColOne = (e) => {
    const dataColumns = [...colOne];
    const index = dataColumns.indexOf(e.target.value);
    if (index > -1) {
      dataColumns.splice(index, 1);
    }
    setColTwo([...dataColumns]);
    setCharts(JSON.parse(forecastRun.visualsData));
  }

  const handleColTwo = () => {
    setCharts(JSON.parse(forecastRun.visualsData));
  }

  const setCharts = (myData) => {
    let forecastDataArray = [];
    let colOneArray = [];
    let colTwoArray = [];
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].data.actual)) {
      colOneArray.push({date: key, [colOneSelect.current.value]: value});
    }
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].data.prediction)) {
      colOneArray.push({date: key, [colOneSelect.current.value]: value});
    }
    
    if(colTwoSelect.current.value !== '0') {
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].data.actual)) {
        colTwoArray.push({date: key, [colTwoSelect.current.value]: value});
      }
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].data.prediction)) {
        colTwoArray.push({date: key, [colTwoSelect.current.value]: value});
      }
    }
    for(let i=0; i<colOneArray.length; i++) {
      forecastDataArray.push({
       ...colOneArray[i], 
       ...colTwoArray[i]
      });
    }    
    setForecastDataA([...forecastDataArray]);

    // decomposition chart
    if(myData[colOneSelect.current.value].decomposition) {
      const deMethods = Object.keys(myData[colOneSelect.current.value].decomposition); 
      setDecompositionMethods([...deMethods]);
      let decompositionDataArray = [];
      let decolOneArray = [];
      let decolTwoArray = [];
      for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition.stl_decomposition.trend)) {
        decolOneArray.push({date: key, trend: value});
      }
      for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition.stl_decomposition.seasonal)) {
        decolOneArray.push({date: key, seasonal: value});
      }
      for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition.stl_decomposition.residual)) {
        decolOneArray.push({date: key, residual: value});
      }
      if(colTwoSelect.current.value !== '0') {
        for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition.stl_decomposition.trend)) {
          decolTwoArray.push({date: key, trend2: value});
        }
        for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition.stl_decomposition.seasonal)) {
          decolTwoArray.push({date: key, seasonal2: value});
        }
        for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition.stl_decomposition.residual)) {
          decolTwoArray.push({date: key, residual2: value});
        }
      }
      for(let i=0; i<decolOneArray.length; i++) {
        decompositionDataArray.push({
        ...decolOneArray[i], 
        ...decolTwoArray[i]
        });
      }    
      setDecompositionDataA([...decompositionDataArray]);
    }
    // outlier chart
    if(myData[colOneSelect.current.value].anomalies) {
      const outMethods = Object.keys(myData[colOneSelect.current.value].anomalies); 
      setOutlierMethods([...outMethods]);
      let outlierDataArray = [];
      let outlierColOneArray = [];
      let outlierColTwoArray = [];
      for (let [key, value] of Object.entries(myData[colOneSelect.current.value].data.actual)) {
        outlierColOneArray.push({date: key, [colOneSelect.current.value]: value});
      }
      for (let [key, value] of Object.entries(myData[colOneSelect.current.value].anomalies[outMethods[0]])) {
        outlierColOneArray.push({date: key, outlier: value});
      }
      if(colTwoSelect.current.value !== '0') {
        for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].data.actual)) {
          outlierColTwoArray.push({date: key, [colTwoSelect.current.value]: value});
        }
        for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].anomalies[outMethods[0]])) {
          outlierColTwoArray.push({date: key, outlier2: value});
        }
      }
      for(let i=0; i<outlierColOneArray.length; i++) {
        outlierDataArray.push({
        ...outlierColOneArray[i], 
        ...outlierColTwoArray[i]
        });
      }    
      setOutlierDataA([...outlierDataArray]);
    }
  }

  const handleDecompositionMethod = (e) => {
    const myData = JSON.parse(forecastRun.visualsData);
    // decomposition chart
    const deMethods = Object.keys(myData[colOneSelect.current.value].decomposition); 
    setDecompositionMethods([...deMethods]);
    let decompositionDataArray = [];
    let decolOneArray = [];
    let decolTwoArray = [];
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition[e.target.value].trend)) {
      decolOneArray.push({date: key, trend: value});
    }
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition[e.target.value].seasonal)) {
      decolOneArray.push({date: key, seasonal: value});
    }
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].decomposition[e.target.value].residual)) {
      decolOneArray.push({date: key, residual: value});
    }
    if(colTwoSelect.current.value !== '0') {
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition[e.target.value].trend)) {
        decolTwoArray.push({date: key, trend2: value});
      }
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition[e.target.value].seasonal)) {
        decolTwoArray.push({date: key, seasonal2: value});
      }
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].decomposition[e.target.value].residual)) {
        decolTwoArray.push({date: key, residual2: value});
      }
    }
    for(let i=0; i<decolOneArray.length; i++) {
      decompositionDataArray.push({
       ...decolOneArray[i], 
       ...decolTwoArray[i]
      });
    }    
    setDecompositionDataA([...decompositionDataArray]);
  }

  const handleOutlierMethod = (e) => {
    const myData = JSON.parse(forecastRun.visualsData);
    // outlier chart
    const outMethods = Object.keys(myData[colOneSelect.current.value].anomalies); 
    setOutlierMethods([...outMethods]);
    let outlierDataArray = [];
    let outlierColOneArray = [];
    let outlierColTwoArray = [];
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].data.actual)) {
      outlierColOneArray.push({date: key, [colOneSelect.current.value]: value});
    }
    for (let [key, value] of Object.entries(myData[colOneSelect.current.value].anomalies[e.target.value])) {
      outlierColOneArray.push({date: key, outlier: value});
    }
    if(colTwoSelect.current.value !== '0') {
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].data.actual)) {
        outlierColTwoArray.push({date: key, [colTwoSelect.current.value]: value});
      }
      for (let [key, value] of Object.entries(myData[colTwoSelect.current.value].anomalies[e.target.value])) {
        outlierColTwoArray.push({date: key, outlier2: value});
      }
    }
    // for(let i=0; i<outlierColOneArray.length; i++) {
    //   outlierDataArray.push({
    //    ...outlierColOneArray[i],
    //    ...(outlierColTwoArray[i].find((itmInner) => itmInner.date === outlierColOneArray[i].date))
    //   });
    // }    
    outlierDataArray.push(...outlierColOneArray, ...outlierColTwoArray);
    setOutlierDataA([...outlierDataArray]);
  }

  useEffect(() => {
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setBucketName(res.data.bucketName);
    }).catch(() => { });
  }, [projectId]);

  const getForecastRun = () => {
    ProgressIndicator.show();
    chronosApi.getForecastRun(projectId, runId).then((res) => {
      if(res.status === 204) {
        setForecastRun([]);
      } else {
        setForecastRun(res.data);
        const myData = JSON.parse(res.data.visualsData);
        setCharts(myData);
        const dataColumns = Object.keys(myData);
        setColOne([...dataColumns]);
        const index = dataColumns.indexOf(dataColumns[0]);
        if (index > -1) {
          dataColumns.splice(index, 1);
        }
        setColTwo([...dataColumns]);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(() => {
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
    colorway: ['#00ADF0', '#979797', '#E0144C', '#EED180', '#AA8B56', '#A8E890', '#6F38C5', '#9E7676', '#D0B8A8', '#7895B2'],
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
        x0: (!loading && forecastDataA.length > 0) && forecastDataA[forecastDataA.length - parseInt(forecastRun.forecastHorizon)].date || '1959-01',
        y0: 0,
        x1: (!loading && forecastDataA.length > 0) && forecastDataA[forecastDataA.length - 1].date || '1960-01',
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

    const cols = [colOneSelect.current.value];
    if(colTwoSelect.current.value !== '0') cols.push(colTwoSelect.current.value);

    cols.forEach(column => {
      lines[column] = {'y': []};
    });
    
    data.map(each => {
      dates.push(each.date);
      cols.forEach(column => {
        lines[column].y.push(each[column]);
      });
    });
    
    for(const [key, value] of Object.entries(lines)) {
      if(key === cols[0]) {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: key
        });
      }
      if(colTwoSelect.current.value !== '0') {
        if(key === cols[1]) {
          traces.push({
            type: 'scatter',
            mode: 'lines',
            x: dates,
            y: value.y,
            name: key,
            line: {
              dash: 'dot',
              color: '#979797',
            },
          });
        }
      }
    }
    return traces;
  }

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
    showlegend: false,
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

  const addTracesDecomposition = (data) => {
    let traces = [];
    
    let dates = [];
    let lines = {};

    const columns = ['trend', 'seasonal', 'residual', 'trend2', 'seasonal2', 'residual2'];
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
      if(key === 'trend') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: colOneSelect.current.value,
        });
      }
      if(key === 'trend2') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: colTwoSelect.current.value,
          line: {
            dash: 'dot',
            color: '#979797',
          },
        });
      }
      if(key === 'seasonal') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          xaxis: 'x2',
          yaxis: 'y2',
          name: colOneSelect.current.value,
          line: {
            color: '#00ADF0',
          },
        });
      }
      if(key === 'seasonal2') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          xaxis: 'x2',
          yaxis: 'y2',
          name: colTwoSelect.current.value,
          line: {
            dash: 'dot',
            color: '#979797',
          },
        });
      }
      if(key === 'residual') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          line: {
            color: '#00ADF0',
          },
          x: dates,
          y: value.y,
          xaxis: 'x3',
          yaxis: 'y3',
          name: colOneSelect.current.value,
        });
      }
      if(key === 'residual2') {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          xaxis: 'x3',
          yaxis: 'y3',
          name: colTwoSelect.current.value,
          line: {
            dash: 'dot',
            color: '#979797',
          },
        });
      }
    }

    return traces;
  }

  const layoutOutlier = {  
    colorway: ['#00ADF0', '#33ADAC', '#EED180', '#AA8B56', '#A8E890', '#6F38C5', '#9E7676', '#D0B8A8', '#7895B2'],
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

  const addTracesOutlier = (data) => {
    let traces = [];
    
    let dates = [];
    let lines = {};

    const cols = [colOneSelect.current.value, 'outlier']
    if(colTwoSelect.current.value !== '0') cols.push(colTwoSelect.current.value, 'outlier2');

    cols.forEach(column => {
      lines[column] = {'y': []};
    });
    
    data.map(each => {
      dates.push(each.date);
      cols.forEach(column => {
        lines[column].y.push(each[column]);
      });
    });
    
    for(const [key, value] of Object.entries(lines)) {
      if(key === colOneSelect.current.value) {
        traces.push({
          type: 'scatter',
          mode: 'lines',
          x: dates,
          y: value.y,
          name: key
        });
      }
      if(key === 'outlier') {
        traces.push({
          mode: 'markers',
          x: dates,
          y: value.y,
          name: 'outlier-' + colOneSelect.current.value,
          marker: {
            color: '#00ADF0',
          },
        });
      }
      if(colTwoSelect.current.value !== '0') {
        if(key === colTwoSelect.current.value) {
          traces.push({
            type: 'scatter',
            mode: 'lines',
            x: dates,
            y: value.y,
            name: key,
            line: {
              dash: 'dot',
              color: '#979797',
            }
          });
        }

        if(key === 'outlier2') {
          traces.push({
            mode: 'markers',
            x: dates,
            y: value.y,
            name: 'outlier-' + colTwoSelect.current.value,
            marker: {
              dash: 'dot',
              color: '#979797',
            },
          });
        }
      }
    }

    return traces;
  }

  const downloadPrediction = () => {
    ProgressIndicator.show();
    if(forecastRun) {
      chronosApi.getFile(`${bucketName}`, `${forecastRun.id}-${forecastRun.runName}`, 'y_pred.csv').then((res) => {
        let csvContent = "data:text/csv;charset=utf-8," + res.data;
        let encodedUri = encodeURI(csvContent);
        let link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "y_pred.csv");
        document.body.appendChild(link);
        link.click();
        link.remove();
        ProgressIndicator.hide();
      }).catch(() => {
        ProgressIndicator.hide();
      });
    }
  }

  const downloadExcel = () => {
    ProgressIndicator.show();
    if(forecastRun) {
      chronosApi.getExcelFile(`${bucketName}`, `${forecastRun.id}-${forecastRun.runName}`, 'RESULT.xlsx').then((res) => {
        var excelBlob = new Blob([res.data]);     
        var url = window.URL.createObjectURL(excelBlob);

        let link = document.createElement("a");
        link.setAttribute("href", url);
        link.setAttribute("download", "RESULT.xlsx");
        document.body.appendChild(link);
        link.click();
        link.remove();
        ProgressIndicator.hide();
      }).catch(() => {
        ProgressIndicator.hide();
        Notification.show('Unable to download the file', 'alert');
      });
    }
  }

  const handleBrowseInStorage = () => {
    if(bucketName) {
      window.open(`${Envs.CONTAINER_APP_URL}/#/storage/explorer/${bucketName}/${forecastRun.id}-${forecastRun.runName}`);
    } else {
      Notification.show('No folder path available for the given run', 'alert');
    }
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
        <div className={Styles.footerBtns}>
          <button className={classNames('btn', Styles.mr)} onClick={handleBrowseInStorage}>Browse in Storage</button>
          <button className={classNames('btn', Styles.mr)} onClick={downloadExcel}><i className="icon mbc-icon document"></i> .xlsx</button>
          <button className={'btn'} onClick={downloadPrediction}><i className="icon mbc-icon document"></i> .csv</button>
        </div>
        { colOne.length === 1 ? null :
          <div className={Styles.compareBtns}>
            <p>Compare columns: </p>
            <div className={classNames(`input-field-group`)}>
              <div>
                <select id="colOneField" onChange={handleColOne} ref={colOneSelect} className={Styles.customSelect}>
                  <>
                    {colOne.length > 0 &&
                      colOne.map((name) => (
                        <option id={name} key={name} value={name}>
                          {name}
                        </option>
                    ))}
                  </>
                </select>
              </div>
            </div>
            <div className={classNames(`input-field-group`)}>
              <div 
                // onBlur={() => trigger('frequency')}
                >
                <select id="colTwoField" onChange={handleColTwo} ref={colTwoSelect} className={Styles.customSelect}>
                  <>
                    <option id={'choose'} key={'choose'} value={0}>
                      Choose column
                    </option>
                    {colTwo.length > 0 &&
                      colTwo.map((name) => (
                        <option id={'col-' + name} key={'col-' + name} value={name}>
                          {name}
                        </option>
                    ))}
                  </>
                </select>
              </div>
            </div>
          </div>
        }
      </div>
      
      { forecastDataA.length > 0 &&
        <VisualContainer
          title="Forecast"
          forecastRun={forecastRun}
          printRef={printRef}
          loading={loading}
          forecastData={forecastDataA}
          addTraces={addTraces}
          layout={layout}
          bucketName={bucketName}
          isForecast={true}
        />
      }

      { decompositionDataA.length > 0 &&
        <VisualContainer
          title="Decomposition"
          forecastRun={forecastRun}
          printRef={printRef}
          loading={loading}
          forecastData={decompositionDataA}
          addTraces={addTracesDecomposition}
          layout={layoutDecomposition}
          bucketName={bucketName}
          isDecomposition={true}
          handleDecompositionMethod={handleDecompositionMethod}
          decompositionMethods={decompositionMethods}
          legend={
              <div className={Styles.legendContainer}>
                <div className={Styles.legend}>
                  <span className={Styles.line}></span>
                  <span>{colOneSelect.current.value}</span>
                </div>
                { colTwoSelect.current.value !== '0' && 
                  <div className={Styles.legend}>
                    <span className={classNames(Styles.line, Styles.dotted)}></span>
                    <span>{colTwoSelect.current.value}</span>
                  </div>
                }
              </div>
            }
        />
      }

      { outlierDataA.length > 0 &&
        <VisualContainer
          title="Outlier"
          forecastRun={forecastRun}
          printRef={printRef}
          loading={loading}
          forecastData={outlierDataA}
          addTraces={addTracesOutlier}
          layout={layoutOutlier}
          bucketName={bucketName}
          isOutlier={true}
          handleOutlierMethod={handleOutlierMethod}
          outlierMethods={outlierMethods}
        />
      }

      {
        nerdStats &&
        <div className={Styles.content}>
          <div className={Styles.header}>
            <h3>Stats for Nerds</h3>
            {/* <div className={Styles.actionMenu}>
              <ContextMenu id={'decomposition'} items={contextMenuItems} />
            </div> */}
          </div>
          <div className={Styles.firstPanel} ref={printRef}>
            { htmlLoading && <Spinner /> }
            { !htmlLoading && html.length === 0 && <p>No visualization for the given data.</p> }
            { !htmlLoading && html.length > 0 &&
                <div className={Styles.chartContainer}>
                  <iframe 
                    className={Styles.iframe}
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
