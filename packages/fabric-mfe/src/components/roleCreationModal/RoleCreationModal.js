import classNames from "classnames";
import React from "react";
import { ReactFlow, Controls, ConnectionLineType, Background, useNodesState, useEdgesState, Handle, Position } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import Styles from './role-creation-modal.scss';
import Spinner from "../spinner/Spinner";
import Notification from "../../common/modules/uilab/js/src/notification";
import { FLOW_DIAGRAM_STATES, FLOW_DIAGRAM_TYPES } from "../../utilities/constants";
import { generateNodesAndEdges, getLayoutedElements } from "../../utilities/utils";

// RoleEntitlementNode component
const RoleEntitlementNode = ({ data }) => {
  const isRoleOrEntitlementStatePending = data?.state === FLOW_DIAGRAM_STATES.PENDING && (data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT || data?.type === FLOW_DIAGRAM_TYPES.ROLE);
  const isRoleOrEntitlementStateCreated = data?.state === FLOW_DIAGRAM_STATES.CREATED && (data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT || data?.type === FLOW_DIAGRAM_TYPES.ROLE);
  const isUpdateRoleEntitlementPending = data?.state === FLOW_DIAGRAM_STATES.PENDING && data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE;
  const isUpdateRoleEntitlementCreated = data?.state === FLOW_DIAGRAM_STATES.CREATED && data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE;
  const isUpdateRoleEntitlementAssigned = data?.state === FLOW_DIAGRAM_STATES.ASSIGNED && data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE;
  const isGroupStatePending = data?.state === FLOW_DIAGRAM_STATES.PENDING && data?.type === FLOW_DIAGRAM_TYPES.GROUP;
  const isGroupStateCreated = data?.state === FLOW_DIAGRAM_STATES.CREATED && data?.type === FLOW_DIAGRAM_TYPES.GROUP;
  const isGroupStateAssigned = data?.state === FLOW_DIAGRAM_STATES.ASSIGNED && data?.type === FLOW_DIAGRAM_TYPES.GROUP;
  const isPriviledgeRoleNull = (data?.subType === FLOW_DIAGRAM_TYPES.PRIVILEGE && data?.state === FLOW_DIAGRAM_STATES.NULL);
  const isPriviledgeRole = (data?.subType === FLOW_DIAGRAM_TYPES.PRIVILEGE && data?.state !== FLOW_DIAGRAM_STATES.NULL);

  return (
    data?.type === FLOW_DIAGRAM_TYPES.INTERMEDIATE ? 
    <>
      <Handle type="target" position={Position.Left} id="b" className={Styles.intermediate} />
      <div>&nbsp;</div>
      <Handle type="source" position={Position.Right} id="a" className={Styles.intermediate} />
    </> : 
    <>
      { (data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE || data?.type === FLOW_DIAGRAM_TYPES.GROUP) && <Handle type="target" position={Position.Left} id="b" /> }
      <div className={
        classNames(
          Styles.roleEntitlementNode,
          (data?.state === FLOW_DIAGRAM_STATES.CREATED || isPriviledgeRole) && Styles.created, 
          (data?.state === FLOW_DIAGRAM_STATES.ASSIGNED && Styles.assigned), 
          (data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE || data?.type === FLOW_DIAGRAM_TYPES.GROUP) && Styles.w300
        )}>
        {data?.type === FLOW_DIAGRAM_TYPES.GROUP &&
          <div className={Styles.notice}>
            <i className="icon mbc-icon info"></i><span>Takes upto 5hrs</span>
          </div>
        }
        <div className={Styles.nodeContent}>
          <div className={Styles.icon}>
            {(data?.state === FLOW_DIAGRAM_STATES.PENDING || isPriviledgeRoleNull) && <Spinner /> }
            {(data?.state === FLOW_DIAGRAM_STATES.CREATED || data?.state === FLOW_DIAGRAM_STATES.ASSIGNED || isPriviledgeRole) && <i className="icon mbc-icon check circle"></i> }
          </div>
          <div className={Styles.text}>
            {isRoleOrEntitlementStatePending && 'Creating '}
            {isRoleOrEntitlementStateCreated &&  'Created '}
            {(isUpdateRoleEntitlementPending || isPriviledgeRoleNull) && 'Updating '}
            {(isUpdateRoleEntitlementCreated || isPriviledgeRole) && 'Updated '}
            {isUpdateRoleEntitlementAssigned && 'Assigned '}
            {isGroupStatePending && 'Assigning '}
            {isGroupStateCreated && 'Created '}
            {isGroupStateAssigned && 'Assigned '}
            <span>{data.name} <i className={classNames('icon mbc-icon copy', Styles.copyIcon)} onClick={() => navigator.clipboard.writeText(data.name).then(() => Notification.show('Copied to Clipboard'))}></i></span> {data.label}
            {(isUpdateRoleEntitlementCreated || isUpdateRoleEntitlementAssigned) && <a href={data?.link} target="_blank" rel="noreferrer noopener">[Alice Link <i className="icon mbc-icon new-tab"></i>]</a>}
          </div>
        </div>
      </div>
      { (data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE || data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT || data?.type === FLOW_DIAGRAM_TYPES.ROLE) && <Handle type="source" position={Position.Right} id="a" /> }
    </>
  );
};

const RoleCreationModal = ({workspace, onClose}) => {
  const { nodes: initialNodes, edges: initialEdges } = generateNodesAndEdges(workspace?.status);
  const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(workspace?.status, initialNodes, initialEdges);

  // eslint-disable-next-line
  const [nodes, setNodes, onNodesChange] = useNodesState(layoutedNodes);
  // eslint-disable-next-line
  const [edges, setEdges, onEdgesChange] = useEdgesState(layoutedEdges);

  return (
    <div className={Styles.modal}>
      <div className={Styles.header}>
        <h3>{workspace?.name}</h3>
        <p>Entitlements, Roles, Microsoft Groups</p>
        <div className={Styles.overallStatusContainer}>
          <div>{workspace?.status?.state === 'IN_PROGRESS' ? <Spinner /> : <i className="icon mbc-icon check circle"></i>} <span>Creating Roles & Entitlements</span></div>
          <div>{workspace?.status?.state === 'IN_PROGRESS' ? <Spinner /> : <i className="icon mbc-icon check circle"></i>} <span>Updating Roles with Entitlements</span></div>
          <div>{workspace?.status?.state === 'IN_PROGRESS' ? <Spinner /> : <i className="icon mbc-icon check circle"></i>} <span>Assigning Roles to Microsoft Groups</span></div>
        </div>
      </div>
      <div className={Styles.content}>
        <div style={{ width: '100%', height: '100%' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            fitView
            fitViewOptions={{ minZoom: 0.1 }}
            connectionLineType={ConnectionLineType.SmoothStep}
            nodeTypes={{ 
              roleEntitlementNode: RoleEntitlementNode
            }}
          >
            <Controls />
            <Background color="#aaa" gap={16} />
          </ReactFlow>
        </div>
      </div>
      <div className={Styles.footer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={onClose}
        >
          Okay
        </button>
      </div>
    </div>
  );
}

export default RoleCreationModal;