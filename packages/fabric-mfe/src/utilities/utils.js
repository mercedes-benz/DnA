import dagre from 'dagre';
import { FLOW_DIAGRAM_TYPES, FLOW_DIAGRAM_MEASUREMENTS } from "./constants";
import { Envs } from './envs';

// Function to generatae nodes and edges for the flow diagram
export const generateNodesAndEdges = (data) => {
  const { microsoftGroups, roles } = data;

  let nodeIdCounter = 1;
  const nodes = [];
  const edges = [];

  const createNode = (data) => {
    const id = nodeIdCounter++;
    nodes.push({ 
      id: String(id), 
      type: 'roleEntitlementNode', 
      data, 
      position: { x: 0, y: 0 },
      draggable: false
    });
    return id;
  };

  const createEdge = (id, source, target) => {
    edges.push({ 
      id, 
      source, 
      target, 
      animated: true, 
      type: 'smoothstep' 
    });
  }

  roles.forEach((role) => {
    // Create a single entitlement node for the role
    const entitlementNodeId = createNode({ 
      name: role?.entitlements[0]?.displayName === Envs.FABRIC_ENTITLEMENT_IGNORE ? role?.entitlements[1]?.displayName : role?.entitlements[0]?.displayName, 
      label: 'Entitlement', 
      type: FLOW_DIAGRAM_TYPES.ENTITLEMENT, 
      state: role?.entitlements[0]?.displayName === Envs.FABRIC_ENTITLEMENT_IGNORE ? role?.entitlements[1]?.state : role?.entitlements[0]?.state
    });

    // Create role node
    const roleNodeId = createNode({ 
      name: role?.name, 
      label: 'Role', 
      type: FLOW_DIAGRAM_TYPES.ROLE, 
      state: role.state 
    });
    
    // Create an intermediate node
    const intermediateNodeId = createNode({ type: FLOW_DIAGRAM_TYPES.INTERMEDIATE });

    // Link entitlement to intermediate node
    createEdge(`e${entitlementNodeId}-${intermediateNodeId}`, String(entitlementNodeId), String(intermediateNodeId));
    // Link role node to intermediate node
    createEdge(`e${roleNodeId}-${intermediateNodeId}`, String(roleNodeId), String(intermediateNodeId));

    // Create nodes for special privileges
    const roleEntitlementNodeId = createNode({ 
      name: role?.name, 
      label: 'Role with Entitlement', 
      type: FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE, 
      state: role?.assignEntitlementsState, 
      link: role?.link 
    });
    const roleOwnerNodeId = createNode({ 
      name: role?.name, 
      label: 'with Role Owner privileges', 
      type: FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE, 
      state: role?.roleOwner,
      subType: FLOW_DIAGRAM_TYPES.PRIVILEGE 
    });
    const globalRoleAssignerNodeId = createNode({ 
      name: role?.name, 
      label: 'with Global Role Assigner privileges', 
      type: FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE, 
      state: role?.globalRoleAssigner, 
      subType: FLOW_DIAGRAM_TYPES.PRIVILEGE
    });
    const roleApproverNodeId = createNode({ 
      name: role?.name, 
      label: 'with Role Approver privileges', 
      type: FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE, 
      state: role?.roleApprover,
      subType: FLOW_DIAGRAM_TYPES.PRIVILEGE
    });

    // Link intermediate node to special privilege nodes
    createEdge(`e${intermediateNodeId}-${roleEntitlementNodeId}`, String(intermediateNodeId), String(roleEntitlementNodeId));
    createEdge(`e${intermediateNodeId}-${roleOwnerNodeId}`, String(intermediateNodeId), String(roleOwnerNodeId));
    createEdge(`e${intermediateNodeId}-${globalRoleAssignerNodeId}`, String(intermediateNodeId), String(globalRoleAssignerNodeId));
    createEdge(`e${intermediateNodeId}-${roleApproverNodeId}`, String(intermediateNodeId), String(roleApproverNodeId));

    // Create and link group node
    const group = microsoftGroups.filter((group) => group.groupName.includes(role.name.split('_').pop()));
    const groupNodeId = createNode({ 
      name: group[0]?.groupName, 
      label: 'to workspace', 
      type: FLOW_DIAGRAM_TYPES.GROUP, 
      state: group[0]?.state 
    });

    createEdge(`e${roleEntitlementNodeId}-${groupNodeId}`, String(roleEntitlementNodeId), String(groupNodeId));
    createEdge(`e${roleOwnerNodeId}-${groupNodeId}`, String(roleOwnerNodeId), String(groupNodeId));
    createEdge(`e${globalRoleAssignerNodeId}-${groupNodeId}`, String(globalRoleAssignerNodeId), String(groupNodeId));
    createEdge(`e${roleApproverNodeId}-${groupNodeId}`, String(roleApproverNodeId), String(groupNodeId));
  });

  return { nodes, edges };
};

// Function to accommodate dynamic node and edge generation
export const getLayoutedElements = (data, nodes, edges) => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));

  dagreGraph.setGraph({
    rankdir: 'LR',
    nodesep: 10,
    ranksep: 100,
    acyclicer: 'greedy',
    ranker: 'tight-tree',
  });

  nodes.forEach((node) => {
    const isIntermediateNode = node.data.type === FLOW_DIAGRAM_TYPES.INTERMEDIATE;
    dagreGraph.setNode(node.id, {
      width: isIntermediateNode ? FLOW_DIAGRAM_MEASUREMENTS.INTERMEDIATE_NODE_WIDTH : FLOW_DIAGRAM_MEASUREMENTS.NODE_WIDTH,
      height: FLOW_DIAGRAM_MEASUREMENTS.NODE_HEIGHT,
    });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  const newNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    const isIntermediateNode = node.data.type === FLOW_DIAGRAM_TYPES.INTERMEDIATE;

    return {
      ...node,
      targetPosition: 'left',
      sourcePosition: 'right',
      position: {
        x: nodeWithPosition.x - (isIntermediateNode ? FLOW_DIAGRAM_MEASUREMENTS.INTERMEDIATE_NODE_WIDTH : FLOW_DIAGRAM_MEASUREMENTS.NODE_WIDTH) / 2,
        y: nodeWithPosition.y - FLOW_DIAGRAM_MEASUREMENTS.NODE_HEIGHT / 2,
      },
    };
  });

  // Post-processing to vertically center specific groups of nodes
  const centerGroup = (idsToCenter, referenceIds) => {
    const referenceNodes = newNodes.filter((n) => referenceIds.includes(n.id));
    const nodesToCenter = newNodes.filter((n) => idsToCenter.includes(n.id));

    const referenceYPositions = referenceNodes.map((n) => n.position.y + FLOW_DIAGRAM_MEASUREMENTS.NODE_HEIGHT / 2);
    const minY = Math.min(...referenceYPositions);
    const maxY = Math.max(...referenceYPositions);
    const centerY = (minY + maxY) / 2;

    const offset = FLOW_DIAGRAM_MEASUREMENTS.NODE_HEIGHT + 10; // Offset to avoid overlap, can be adjusted

    nodesToCenter.forEach((n, index) => {
      n.position.y = centerY - (nodesToCenter.length - 1) * offset / 2 + index * offset - FLOW_DIAGRAM_MEASUREMENTS.NODE_HEIGHT / 2;
    });
  };

  data.roles.forEach(role => {
    // Find the ROLE and ENTITLEMENT nodes for this group
    const roleNode = newNodes.find(node => node.data.type === FLOW_DIAGRAM_TYPES.ROLE && node.data.name === role.name);
    const entitlementNode = newNodes.find(node => node?.data?.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT && node?.data?.name?.includes(role?.name?.split('_').pop()));

    // Collect the ids of ENTITLEMENT_ROLE nodes
    const entitlementRoleNodes = newNodes.filter(node =>
      node.data.type === FLOW_DIAGRAM_TYPES.ENTITLEMENT_ROLE && node.data.name.includes(role.name.split('_').pop())
    ).map(node => node.id);

    if (roleNode && entitlementNode && entitlementRoleNodes.length > 0) {
      // Center the ROLE and ENTITLEMENT nodes with the ENTITLEMENT_ROLE nodes
      centerGroup([roleNode.id, entitlementNode.id], entitlementRoleNodes);
    }
  });

  return { nodes: newNodes, edges };
};

// Regional date and time conversion
export const regionalDateAndTimeConversionSolution = (dateString) => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language, {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: false,
  }).format(date);
};

// Get query parameter
export function getQueryParameterByName(name, url) {
  if (!url) {
    url = window.location.href;
  }
  name = name.replace(/[[\]]/g, '\\$&');
  const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
  const results = regex.exec(url);
  if (!results) {
    return null;
  }
  if (!results[2]) {
    return '';
  }
  return decodeURIComponent(results[2].replace(/\+/g, ' '));
}