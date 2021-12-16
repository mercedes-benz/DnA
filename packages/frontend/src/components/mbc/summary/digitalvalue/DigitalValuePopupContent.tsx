import * as React from 'react';
import Styles from './DigitalValueSummary.scss';

export default function DigitalValuePopupContent() {
  return (
    <React.Fragment>
      <div className={Styles.digitalValueContentsection}>
        <h5>Digital Value at 100% </h5>
        <p>
          This KPI shows the year when all digital value drivers reach 100% of their full potential and implementation
          for the first time.For instance, if there are two value drivers, one value driver unlocks its full value
          stream in 2025 and the other in 2026; the KPI shows 2026 as the digital value at 100%.
        </p>
        <br />
        <h5>Cost Drivers</h5>
        <p>This KPI shows the total and absolute sum of all costs for the given time period.</p>
        <br />
        <h5>Value Drivers</h5>
        <p>This KPI shows the total and absolute sum of all values for the given time period.</p>
        <br />
        <h5>Break Even Point</h5>
        <p>
          This KPI shows the first year when the digital value outweighs its costs. However, this does not necessary
          mean that it also reaches the digital value at 100% in the same year. It could be that the overall digital
          value reaches its full potential and implementation a few years after its actual break even point.
        </p>
        <p>
          Here, you need to average everything out as well. That’s why we have this kind of summary because it’s the sum
          and average of all shown value and cost drivers above.
        </p>
      </div>
    </React.Fragment>
  );
}
