import classNames from 'classnames';
import { IMarketingRole, INeededMarketingRoleObject, IRelatedProduct } from 'globals/types';
import React, { useState, useEffect } from 'react';
import Styles from './RoleSelect.scss';
import { InputFields } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import InputFieldsUtils from 'components/formElements/InputFields/InputFieldsUtils';
// import NumberFormat from 'react-number-format';
import AddRelatedProductModal from 'components/mbc/createNewSolution/description/addRelatedProductModal/AddRelatedProductModal';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

export interface IPersonaSelectProps {
    neededRoleMaster: IMarketingRole[];
    neededRoles: INeededMarketingRoleObject[];
    onRoleChange?: (e:any) => void;
}

const RoleSelect = (props: IPersonaSelectProps) => {
  const [roleCountFieldList, setRoleCountFieldList] = useState([]);
  const [neededRoleMaster, setNeededRoleMaster] = useState(props?.neededRoleMaster);
  const [neededRoleValue, setNeededRoleValue]  = useState(props?.neededRoles?.map(item => item.role));
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [roleToDelete, setRoleToDelete] = useState({});
  const [showAddNeededRoleModal, setShowAddNeededRoleModal] = useState(false);

  const addNeededRoleModalRef = React.createRef<AddRelatedProductModal>();

  useEffect(() => {
    props.onRoleChange(roleCountFieldList);
    setNeededRoleMaster(neededRoleMaster);
    setNeededRoleValue(neededRoleValue);
  }, [neededRoleValue]); 

  useEffect(() => {
    SelectBox.defaultSetup();
    setRoleCountFieldList(props.neededRoles);
  },[]);

  
  const onNeededRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.label);
      });
    }
    changeInRoleCount(selectedValues);
  };

  const changeInRoleCount = (selectedValues: string[]) => {
    const tempRoleCountFieldList: INeededMarketingRoleObject[] = [];
    if (roleCountFieldList) {
      roleCountFieldList.forEach((item, index) => {
        if (!selectedValues.includes(item.role)) {
          roleCountFieldList.splice(index, 1);
        }
      });
    }

    const tempArr = roleCountFieldList ? roleCountFieldList.map((item) => item.role) : [];
    const missing = selectedValues.filter((item) => tempArr.indexOf(item) < 0);

    neededRoleMaster.forEach((item: any) => {
      if (selectedValues.includes(item.name) && missing.length !== 0) {
        const tempObj = {
          fromDate: '',
          role: item.name,
          requestedFTECount: '0',
          toDate: '',
        };
        tempRoleCountFieldList.push(tempObj);
      }
    });

    const a = roleCountFieldList ? roleCountFieldList : [];
    const b = tempRoleCountFieldList ? tempRoleCountFieldList : [];

    if (a.length > 0 && b.length > 0) {
      // A comparer used to determine if two entries are equal.
      const isSameUser = (a: INeededMarketingRoleObject, b: INeededMarketingRoleObject) => a.role == b.role;

      // Get items that only occur in the left array,
      // using the compareFunction to determine equality.
      const onlyInLeft = (left: any, right: any, compareFunction: any) =>
        left.filter((leftValue: any) => !right.some((rightValue: any) => compareFunction(leftValue, rightValue)));

      const onlyInA = onlyInLeft(a, b, isSameUser);
      const onlyInB = onlyInLeft(b, a, isSameUser);

      const result = [...onlyInA, ...onlyInB];
      roleCountFieldList.push(...result);
      setRoleCountFieldList(roleCountFieldList);
      setNeededRoleValue(selectedValues);
    } else {
      /************** Setting default value when loads while edit ***************/
      setRoleCountFieldList(tempRoleCountFieldList.length > 0 ? tempRoleCountFieldList : roleCountFieldList);
      setNeededRoleValue(selectedValues);
      InputFields.defaultSetup();
    
    }
  }

  const openDeleteModal = (role: INeededMarketingRoleObject) => {
    return () => {
        setShowDeleteModal(true);
        setRoleToDelete(role);
    //   this.setState({ showDeleteModal: true, roleToDelete: role });
    };
  };

  const onAccept = () => {
    deleteRole(roleToDelete);
    setShowDeleteModal(false);
  };

  const onInfoModalCancel = () => {
    setShowDeleteModal(false);
  };

  const deleteRole = (selectedRole: any) => {
    const tempArr = neededRoleValue.filter((item, index) => {
      if (selectedRole.role != item) {
        return item;
      }
    });
    setNeededRoleValue(tempArr);
    /** following timeout is the substitute of callback function of setState()
     * where setting up select box as default after updating master role list
     */
     setTimeout(()=>{
      SelectBox.defaultSetup(false);
    }, 10);
    changeInRoleCount(tempArr);
    // this.setState({ neededRoleValue: tempArr }, () => {
    //   SelectBox.defaultSetup(false);
    //   this.changeInRoleCount(tempArr);
    // });
  };

  const onNeededRoleChangeUpdate = (data: string[]) => {
    const neededRoles: IRelatedProduct[] = data
      .filter(
        (o1) => !neededRoleMaster.some((o2: { name: string }) => o1.toLowerCase() === o2.name.toLowerCase()),
      )
      .map((str) => {
        return {
          id: str,
          name: str,
        };
      });
    neededRoleMaster.push(...neededRoles);
    const selectedNeededRoles: string[] = [];
    data.forEach((o1) => {
      neededRoleMaster.forEach((o2) => {
        if (o1.toLowerCase() === o2.name.toLowerCase()) {
          selectedNeededRoles.push(o2.name);
        }
      });
    });

    const selectedValues: string[] = selectedNeededRoles;
    neededRoleValue.push(...selectedValues);
    setNeededRoleValue(neededRoleValue);
    InputFieldsUtils.resetErrors('#roleWrapper'); // reseting the parent filed //
    setNeededRoleMaster(neededRoleMaster);
    /** following timeout is the substitute of callback function of setState()
     * where setting up select box as default after updating master role list
     */
    setTimeout(()=>{
      SelectBox.defaultSetup(false);
    }, 10);
    
    changeInRoleCount(neededRoleValue);
  };

  const showAddNeededRoleModalView = () => {
    setShowAddNeededRoleModal(true);
  };

  const onAddNeededRoleModalCancel = () => {
    setShowAddNeededRoleModal(false);
  };
  
//   const handleChange = (values: any, sourceInfo: any) => {
//         const { value } = values;
//         const name: string = sourceInfo?.event?.target?.name;
//         const tempVal: any = value !== null && value !== '' ? value : 0;
//         const index = Number(sourceInfo?.event?.target?.id.split('-')[1]);
//         // const { roleCountFieldList } = this.state;
//         roleCountFieldList.forEach((item, itemIndex) => {
//           if (index === itemIndex) {
//             item[name] = tempVal;
//           }
//           return item;
//         });
//         setRoleCountFieldList(roleCountFieldList);
//         // this.setState({ roleCountFieldList }, () => {
//         //   const tempTeamsObj = { team: this.state.teamMembers };
//         //   this.props.modifyTeam(tempTeamsObj, this.state.roleCountFieldList);
//         // });
//       };  

  return (
    <>        
        <div className={classNames(Styles.wrapperNeededRole)}>
          <div className={classNames(Styles.firstPanel)}>
            <h3>Marketing Roles</h3>
            {/* <i className={classNames('icon mbc-icon info', Styles.roleInfo)} onClick={showNeededRoleInfoModal} /> */}
            <div id="roleWrapper" className={Styles.roleWrapperContent}>
              <div id="roleContainer" className={classNames('input-field-group')}>
                <label id="roleLabel" className={classNames('input-label', Styles.roleLabel)} htmlFor="roleSelect">
                  Select Roles{' '}
                </label>
                <div id="role" className="custom-select">
                  <select
                    id="roleSelect"
                    multiple={true}
                    required={false}
                    onChange={onNeededRoleChange}
                    value={neededRoleValue}
                  >
                    {neededRoleMaster
                      ? neededRoleMaster?.map((obj:any) => (
                          <option id={obj.name} key={obj.name} value={obj.name}>
                            {obj.name}
                          </option>
                        ))
                      : ''}
                  </select>
                </div>
                <div>
                  <button className={classNames(Styles.roleAddButton)} onClick={showAddNeededRoleModalView}>
                    <i className="icon mbc-icon plus" />
                    &nbsp;
                    <span>Add new roles</span>
                  </button>
                </div>

                <div className={Styles.roleCountFieldList}>
                  {roleCountFieldList ? (
                    roleCountFieldList.length > 0 ? (
                      <div>
                        <table className="ul-table users">
                          <thead>
                            <tr className="header-row">
                              <th>
                                <label>Roles</label>
                              </th>

                              {/* <th>
                                <label>Number of FTE</label>
                              </th> */}

                              <th className="text-center">
                                <label>Action</label>
                              </th>
                            </tr>
                          </thead>
                          <tbody>
                            {roleCountFieldList &&
                              roleCountFieldList.map((item: INeededMarketingRoleObject, index: number) => {
                                return (
                                  <tr className="data-row" key={item.role + '-' + index}>
                                    <td>{item.role}</td>
                                    {/* <td>
                                      <div> */}
                                        {/* @ts-ignore */}
                                        {/* <NumberFormat
                                          className={classNames('input-field', Styles.fteField)}
                                          id={'numberOfRequestedFTE-' + index}
                                          name="requestedFTECount"
                                          value={new Intl.NumberFormat(navigator.language).format(
                                            Number(item.requestedFTECount),
                                          )}
                                          thousandSeparator={false}
                                          decimalScale={2}
                                          decimalSeparator={
                                            navigator.language == 'de-DE' ||
                                            navigator.language == 'de' ||
                                            navigator.language == 'DE'
                                              ? ','
                                              : '.'
                                          }
                                          onValueChange={(values, sourceInfo) => handleChange(values, sourceInfo)}
                                        /> */}
                                      {/* </div>
                                    </td> */}
                                    <td className="text-center">
                                      <i
                                        onClick={openDeleteModal(item)}
                                        className={classNames(Styles.deleteIcon, 'icon delete')}
                                      />
                                    </td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      ''
                    )
                  ) : (
                    ''
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {showAddNeededRoleModal && (
          <AddRelatedProductModal
            modalTitleText={'Add marketing role'}
            fieldTitleText={'Marketing Role'}
            ref={addNeededRoleModalRef}
            showAddRelatedProductModal={showAddNeededRoleModal}
            relatedProduct={[]}
            onAddRelatedProductModalCancel={onAddNeededRoleModalCancel}
            max={100}
            chips={[]}
            onRelatedProductChangeUpdate={onNeededRoleChangeUpdate}
          />
        )}

        {showDeleteModal && (
          <ConfirmModal
            title={''}
            showAcceptButton={true}
            showCancelButton={true}
            acceptButtonTitle={'Confirm'}
            cancelButtonTitle={'Cancel'}
            show={showDeleteModal}
            removalConfirmation={true}
            content={
              <div style={{ margin: '35px 0', textAlign: 'center' }}>
                <div>Remove Role</div>
                <div className={classNames(Styles.removeConfirmationContent, 'hide')}>
                  Are you sure to remove the Role?
                </div>
              </div>
            }
            onCancel={onInfoModalCancel}
            onAccept={onAccept}
          />
        )}
    </>
  );
};

export default RoleSelect;