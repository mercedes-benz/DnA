import React from 'react';

interface IProgressProps {
  show: boolean;
}
const Progress: React.FC<IProgressProps> = ({ show }: IProgressProps): JSX.Element => {
  return show ? (
    <div className="progress-block-wrapper">
      <div className="progress infinite" />
    </div>
  ) : (
    <React.Fragment />
  );
};
export default Progress;
