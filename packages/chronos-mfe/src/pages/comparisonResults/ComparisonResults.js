import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import Styles from './comparison-results.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../apis/chronos.api';
import Spinner from '../../components/spinner/Spinner';
// import SelectBox from 'dna-container/SelectBox';

const ComparisonResults = () => {
  const printRef = React.useRef();

  const history = useHistory();

  const { projectid: projectId, comparisonid: comparisonId } = useParams();
  const goback = () => {
    history.push(`/project/${projectId}/comparisons`);
  }

  const [html, setHtml] = useState('');
  const [comparisonName, setComparisonName] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getComparisonDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getComparisonDetails = () => {
    ProgressIndicator.show();
    chronosApi.getComparisonHtml(projectId, comparisonId).then((res) => {
      if(res.status === 204) {
        setHtml('');
      } else {
        setHtml(res.data.comparisonData);
        setComparisonName(res.data.comparisonName);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(() => {
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
          <h2>Run Comparison{!loading && comparisonName?.length > 0 && ' "' + comparisonName + '"'}</h2>
          {/* <button>Edit</button> */}
        </div>
      </div>

      <div className={Styles.content}>
        <div className={Styles.firstPanel} ref={printRef}>
          { loading && <Spinner /> }
          { !loading && html?.length === 0 && <p>No visualization for the given data.</p> }
          { !loading && html?.length > 0 &&
              <div className={Styles.chartContainer}>
                <iframe 
                  className={Styles.iframe}
                  srcDoc={html}
                  width="100%"
                  height="850px"
                ></iframe>
              </div>
          }
        </div>
      </div>
    </div>
  );
}
export default ComparisonResults;
