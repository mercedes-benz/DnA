import React, { useEffect, useState } from 'react';
import Styles from './LandingSummary.scss';
import TagSection from './tagSection/TagSection';
import HeadingSection from './headingSection/HeadingSection';


export interface ILandingSummaryProps {
  title: string;
  subTitle?: string;
  children: any;
  tags?: string[];
}

const LandingSummary = (props: ILandingSummaryProps) => {

    const [selectedTags, setSelectedTags] = useState([]);

    useEffect(() => {
    },[])
    const setSelectedFilter = (values: string[]) => {
        setSelectedTags(values);
    }

    return (
        <div className={Styles.landingSummary}>
            <HeadingSection title={props.title} subTitle={props.subTitle}></HeadingSection>

            <TagSection tags={props.tags} selectedTags={selectedTags} setSeletedTags={setSelectedFilter}></TagSection>

            <div className={Styles.content}>
                {props.children}
            </div>
        </div>
    );
};

export default LandingSummary;