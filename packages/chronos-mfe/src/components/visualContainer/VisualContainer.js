import classNames from 'classnames';
import React from 'react';
import Styles from './visual-container.scss';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import ContextMenu from '../contextMenu/ContextMenu';
import Spinner from '../spinner/Spinner';
import Plot from 'react-plotly.js';

const VisualContainer = ({title, forecastRun, printRef, loading, forecastData, addTraces, layout, isForecast, isDecomposition, handleDecompositionMethod, legend, decompositionMethods,
  isOutlier, handleOutlierMethod, outlierMethods}) => {
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
    },
  ];

  return (
    <div className={Styles.content}>
      <div className={Styles.header}>
        <h3>{title}</h3>
        <div className={Styles.actionMenu}>
          <ContextMenu id={'visual'} items={contextMenuItems} />
        </div>
      </div>
      { isDecomposition && <p>Method</p> }
      {
        isDecomposition &&
          <div className={classNames(`input-field-group`)}>
            <div 
              // onBlur={() => trigger('frequency')}
              >
              <select id="colTwoField" onChange={handleDecompositionMethod} className={Styles.customSelect}>
                <>
                  {decompositionMethods.length > 0 &&
                    decompositionMethods.map((name) => (
                      <option id={name} key={name} value={name}>
                        {name}
                      </option>
                  ))}
                </>
              </select>
            </div>
          </div>
      }
      { isOutlier && <p>Method</p> }
      {
        isOutlier &&
          <div className={classNames(`input-field-group`)}>
            <div 
              // onBlur={() => trigger('frequency')}
              >
              <select id="colTwoField" onChange={handleOutlierMethod} className={Styles.customSelect}>
                <>
                  {outlierMethods.length > 0 &&
                    outlierMethods.map((name) => (
                      <option id={name} key={name} value={name}>
                        {name}
                      </option>
                  ))}
                </>
              </select>
            </div>
          </div>
      }
      <div className={Styles.firstPanel} ref={printRef}>
        { loading && <Spinner /> }
        { !loading && forecastData.length === 0 && <p>No visualization for the given data.</p> }
        { !loading && forecastData.length > 0 &&
            <>
              { isForecast && <p className={Styles.chartLabel}>Forecast</p> }
              <div className={Styles.chartContainer}>
                <Plot
                  data={addTraces(forecastData)}
                  layout={layout}
                  useResizeHandler
                  config={{ displaylogo: false }}
                  style={{width: '100%', height: '450px'}}
                />
                { isDecomposition && legend }
              </div>
            </>
        }
      </div>
    </div>
  );
}

export default VisualContainer;