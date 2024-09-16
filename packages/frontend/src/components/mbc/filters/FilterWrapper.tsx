import React from 'react';
import cn from 'classnames/bind';
import Styles from './FilterWrapper.scss';
const classNames = cn.bind(Styles);

type SolutionsFilterType = {
  openFilters?: boolean;
  children?: any;
  divisionAdminFilter?: boolean;
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
  divisionAdminFilter,
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
    <div id='filterContainer' className={classNames(divisionAdminFilter? Styles.divisionAdminFilterWrapper : Styles.filterWrapper, openFilters ? Styles.filterOpen : Styles.filterClose)}>
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
