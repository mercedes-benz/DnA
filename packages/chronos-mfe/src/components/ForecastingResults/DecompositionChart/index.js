import React, { useState } from 'react';
import Styles from './styles.scss';
import ContextMenu from '../../shared/contextMenu/ContextMenu';
import Spinner from '../../shared/spinner/Spinner';
import Plot from 'react-plotly.js';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';

const DecompositionChart = ({forecastRun, forecastData}) => {
  const printRef = React.useRef();

  const [columns, setColumns] = useState([]);

  const [loading, setLoading] = useState(true);

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
  return (
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
            </>
        }
      </div>
    </div>
  )
}

export default DecompositionChart;