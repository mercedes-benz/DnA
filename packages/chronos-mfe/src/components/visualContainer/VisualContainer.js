import classNames from 'classnames';
import React from 'react';
import Styles from './visual-container.scss';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import ContextMenu from '../contextMenu/ContextMenu';
import Spinner from '../spinner/Spinner';
import Plot from 'react-plotly.js';
import { chronosApi } from '../../apis/chronos.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../../utilities/envs';

const VisualContainer = ({title, forecastRun, printRef, loading, forecastData, addTraces, layout, bucketName}) => {
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

  const handleBrowseInStorage = () => {
    if(bucketName) {
      window.open(`${Envs.STORAGE_MFE_APP_URL}/explorer/${bucketName}/${forecastRun.id}-${forecastRun.runName}`);
    } else {
      Notification.show('No folder path available for the given run', 'alert');
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
                  config={{ displaylogo: false }}
                  style={{width: '100%', height: '450px'}}
                />
              </div>
            </>
        }
      </div>
      <div className={Styles.footerBtns}>
        <button className={classNames('btn', Styles.mr)} onClick={handleBrowseInStorage}>Browse in Storage</button>
        <button className={'btn'} onClick={downloadPrediction}><i className="icon mbc-icon document"></i> .csv</button>
      </div>
    </div>
  );
}

export default VisualContainer;