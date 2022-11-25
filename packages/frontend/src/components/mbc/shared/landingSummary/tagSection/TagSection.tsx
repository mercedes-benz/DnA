import cn from 'classnames';
import React, { useEffect } from 'react';
import Styles from './TagSection.scss';

const classNames = cn.bind(Styles);


export interface ITagsProps {
  tags: string[];
  selectedTags: string[];
  setSeletedTags: (values: string[])=>void;
}


const TagSection = (props: ITagsProps) => {

    useEffect(() => {
        // selectTagToFilter();
    },[]);

    const selectTagToFilter = (tag: string) => {
        const indexOfTag = props.selectedTags.indexOf(tag);
        let newArray = props.selectedTags;
        if (indexOfTag > -1) { 
            newArray = props.selectedTags.splice(indexOfTag, 1); 
            props.setSeletedTags(newArray);
        } else {
            newArray.push(tag); 
            props.setSeletedTags(newArray);
        }        
    }

    const selectAllTags = (tag: string) => {
        props.setSeletedTags([tag]);
    }

    const selectAll = props?.tags?.length > 0 
    ? (props?.tags?.length === props?.selectedTags?.length) || (props?.selectedTags[0] === 'all')
        ? true 
        : false 
    : false;
    
    return (
        <div className={Styles.filterWrapper}>
            <span className={classNames(Styles.tagItem, selectAll ? Styles.selectedItem : '')} onClick={()=>selectAllTags('all')}>
                All ({props?.tags?.length})
            </span>
            {props?.tags?.map((tag, index: number) => {
                const shouldHighlightItem = props?.selectedTags.includes(tag);
                return (
                    <span key={index} className={classNames(Styles.tagItem, shouldHighlightItem ? Styles.selectedItem : '')}
                    onClick={()=>selectTagToFilter(tag)}>
                        {tag}
                    </span>)
            })}
        </div>
    );
};

export default TagSection;