import React from 'react';
import cn from 'classnames/bind';

// @ts-ignore
import Button from '../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';


import Styles from './FilterWrapper.scss';
const classNames = cn.bind(Styles);

type SolutionsFilterType = {
  openFilters?: boolean;
  children?: any;
};

/**
 * Solutions Filter Panel
 * @param {string} openFilters in boolean
 * @param {Function} children any child elements
 * @returns
 */

const FilterWrapper = ({
  openFilters,
  children,
}: SolutionsFilterType) => {

if(openFilters){
  if(document.getElementById("filterContainer")){
    const height = document?.getElementById('filterContainerDiv')?.clientHeight; // taking height of child div
    document.getElementById("filterContainer").setAttribute("style", "height:"+height+"px"); //assigning height to parent div
  }
}else{
  if(document.getElementById("filterContainer")){
    document.getElementById("filterContainer").setAttribute("style", "height:"+0+"px");
  } 
}


  return (
    <div id='filterContainer' className={classNames(Styles.filterWrapper, openFilters ? Styles.filterOpen : Styles.filterClose)}>
      <div id='filterContainerDiv' className={classNames(Styles.panelWrapper, openFilters ? 'open' : '')}>
        <div className={Styles.panelContent}>
          {/* <h4>Filter</h4> */}
            <div className={Styles.filterPanel}>
                {children}
            </div>
        </div>
      </div>
    </div>
  );
};

export default FilterWrapper;
