import cn from 'classnames';
import Modal from 'components/formElements/modal/Modal';
import React, { useEffect, useState } from 'react';
import Styles from './TagSection.scss';

const classNames = cn.bind(Styles);


export interface ITagsProps {
  tags: string[];
  selectedTags: string[];
  setSeletedTags: (values: string[])=>void;
}


const TagSection = (props: ITagsProps) => {

    // const [selectedTag, setSelectedTag] = useState('');
    const [showModal, setShowModal] = useState(false);

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
        // const allTags = props?.tags;
        // const shallowCloneAllTags = [ ...allTags ]; //Shallow cloning so that refernces will not be copied
        props.setSeletedTags([]);
    }

    const selectAll = props?.tags?.length > 0 
    ? (props?.tags?.length === props?.selectedTags?.length) || 
    props?.selectedTags?.length === 0 ||
    (props?.selectedTags[0] === 'all')
        ? true 
        : false 
    : false;

    const cancelModal = () => {
        setShowModal(false);
    }

    // Following line we are filtering list which are already selected so that we can concatenate
    // selected tags with not selected tags
    // const subtractedArray = props?.tags.filter( ( el ) => !props?.selectedTags.includes( el ) );

    // In following line we are concatenating selected with not selected tags so that we can 
    // show selected tags first

    // const listToDisplay = props?.selectedTags.concat(subtractedArray);

    const modalContent: React.ReactNode = (
        <ul>
            <li className={classNames(Styles.tagItem, selectAll ? Styles.selectedItem : '')} onClick={()=>selectDeselectAllTags('all')}>
                All ({props?.tags?.length})
            </li>
            {props?.tags?.map((tag, index: number) => {
                const shouldHighlightItem = (props?.selectedTags.includes(tag));
                return (
                    <li key={index} className={classNames(Styles.tagItem, shouldHighlightItem ? Styles.selectedItem : '')}
                    onClick={()=>selectDeselectTagsFilter(tag)}>
                        {tag}
                    </li>)
            })}
        </ul>
    );

    const threeDotContent: React.ReactNode = (
        <li className={Styles.threeDots} onClick={()=>setShowModal(true)}>
        more... {props?.selectedTags?.length > 0 ? '( '+props?.selectedTags?.length+' selected )' : ''}
        </li>
    );

    const content: React.ReactNode = (
        <ul>
            <li className={classNames(Styles.tagItem, selectAll ? Styles.selectedItem : '')} onClick={()=>selectDeselectAllTags('all')}>
                All {props?.tags?.length && <>({props?.tags?.length})</>}
            </li>
            {props?.tags?.reduce((result, tag, index: number) => {
                const shouldHighlightItem = (props?.selectedTags.includes(tag));
                if(index <= 15){
                result.push(
                    <li key={index} className={classNames(Styles.tagItem, shouldHighlightItem ? Styles.selectedItem : '')}
                    onClick={()=>selectDeselectTagsFilter(tag)}>
                        {tag}
                    </li>)
                }
                return result;    
            },[])}
            {props?.tags?.length > 15 ? threeDotContent : ''}
        </ul>
    );
    
    return (
        <div className={Styles.filterWrapper}>
            {content}
            <Modal
                title={'Tags'}
                modalWidth={'60%'}
                showAcceptButton={false}
                showCancelButton={false}
                buttonAlignment="center"
                show={showModal}
                content={modalContent}
                scrollableContent={true}
                onCancel={cancelModal}
            />
        </div>
    );
};

export default TagSection;