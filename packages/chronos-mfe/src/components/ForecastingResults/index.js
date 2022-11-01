import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory, useParams } from 'react-router-dom';
// import { Line, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, ReferenceArea, Brush } from 'recharts';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import ContextMenu from '../shared/contextMenu/ContextMenu';
import Styles from './styles.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../shared/spinner/Spinner';
import Plot from 'react-plotly.js';

// const COLORS = ['#00ADF0', '#33ADAC', '#E0144C', '#EED180', '#AA8B56', '#A8E890', '#6F38C5', '#9E7676', '#D0B8A8', '#7895B2'];

const ForecastingResults = () => {
  const printRef = React.useRef();

  const history = useHistory();
  const goback = () => {
    history.goBack();
  }
  const { projectid: projectId, runid: runId } = useParams();

  const [nerd, setNerd] = useState(false);

  /* chart */
  // const axisTextStyle = { fill: '#99A5B3', fontSize: 'var(--font-size-smallest)', fontFamily: 'Roboto-Medium' };
  // const tooltipCursorBackground = { fill: '#1f2124', opacity: 1 };

  const [columns, setColumns] = useState([]);

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
        const y = 'date' + res.data.y;
        const yObj = csvToJSON(y);
        const yPred = 'date' + res.data.yPred;
        const yPredObj = csvToJSON(yPred);
        const forecastObj = [...yObj, ...yPredObj].filter(obj => obj.date !== '');
        setForecastData(forecastObj);
        const cols = Object.keys(forecastObj[0]);
        const newCols = cols.filter(item => item !== 'date');
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

  const exportToPdf = async () => {
    const element = printRef.current;
    const canvas = await html2canvas(element);
    const data = canvas.toDataURL('image/png');

    const pdf = new jsPDF();
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
                {/* <ResponsiveContainer width="100%" height={450}>
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
                    <XAxis dataKey="date" axisLine={false} tickLine={false} tick={axisTextStyle} />
                    <YAxis axisLine={false} tickLine={false} tick={axisTextStyle} />
                    <ReferenceArea x1={forecastData[forecastData.length - parseInt(forecastRun.forecastHorizon)].name} x2={forecastData[forecastData.length - 1].name} stroke="gray" strokeOpacity={0.3} />
                    <Tooltip contentStyle={{backgroundColor: '#252a33'}} />
                    <Legend />
                    <Brush dataKey="date" height={30} />
                  </LineChart>
                </ResponsiveContainer> */}
              </>
          }
        </div>
      </div>
    </div>
  );
}
export default ForecastingResults;

