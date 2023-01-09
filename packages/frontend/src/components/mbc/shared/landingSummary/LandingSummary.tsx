import React, { useEffect, useState } from 'react';
import Styles from './LandingSummary.scss';
import TagSection from './tagSection/TagSection';
import HeadingSection from './headingSection/HeadingSection';

export interface ILandingSummaryProps {
  title: string;
  subTitle?: string;
  children: any;
  tags?: string[];
  headerImage?: string;
  isBackButton?: boolean;
  isTagsFilter?: boolean;
}

const LandingSummary = (props: ILandingSummaryProps) => {
  const [selectedTags, setSelectedTags] = useState([]);

  useEffect(() => {}, []);

  const setSelectedFilter = (values: string[]) => {
    setSelectedTags(values);
  };

  return (
    <div className={Styles.landingSummary}>
      <HeadingSection
        title={props.title}
        subTitle={props.subTitle}
        headerImage={props.headerImage}
        isBackButton={props.isBackButton}
      />
      {props.isTagsFilter && (
        <div className={Styles.tagsSectionWrapper}>
          <TagSection tags={props.tags} selectedTags={selectedTags} setSeletedTags={setSelectedFilter} />
        </div>
      )}
      <div className={Styles.content}>{props.children}</div>
    </div>
  );
};

export default LandingSummary;
