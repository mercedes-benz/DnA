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

    // const [selectedTag, setSelectedTag] = useState('');

    useEffect(() => {        
    },[props.selectedTags]);
    

    const selectDeselectTagsFilter = (tag: string) => {
        const indexOfTag = props.selectedTags.indexOf(tag);
        const newArray = JSON.parse(JSON.stringify(props.selectedTags));
        if (indexOfTag > -1) { 
            newArray.splice(indexOfTag, 1); 
            props.setSeletedTags(newArray);
        } else {
            newArray.push(tag); 
            props.setSeletedTags(newArray);
        }        
    }

    const selectDeselectAllTags = (tag: string) => {
        const allTags = props?.tags;
        const shallowCloneAllTags = [ ...allTags ]; //Shallow cloning so that refernces will not be copied
        props.setSeletedTags(shallowCloneAllTags);
    }

    const selectAll = props?.tags?.length > 0 
    ? (props?.tags?.length === props?.selectedTags?.length) || 
    props?.selectedTags?.length === 0 ||
    (props?.selectedTags[0] === 'all')
        ? true 
        : false 
    : false;
    
    return (
        <div className={Styles.filterWrapper}>
            <span className={classNames(Styles.tagItem, selectAll ? Styles.selectedItem : '')} onClick={()=>selectDeselectAllTags('all')}>
                All ({props?.tags?.length})
            </span>
            {props?.tags?.map((tag, index: number) => {
                const shouldHighlightItem = (props?.selectedTags.includes(tag));
                return (
                    <span key={index} className={classNames(Styles.tagItem, shouldHighlightItem ? Styles.selectedItem : '')}
                    onClick={()=>selectDeselectTagsFilter(tag)}>
                        {tag}
                    </span>)
            })}
        </div>
    );
};

export default TagSection;