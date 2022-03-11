import { Condition, Status, STATUS_TYPE, K8sObject } from 'kubeflow';
import { V1Container } from '@kubernetes/client-node';
import {
  InferenceServiceK8s,
  PredictorSpec,
  PredictorType,
  PredictorExtensionSpec,
  ExplainerSpec,
} from '../types/kfserving/v1beta1';

/*
 * general util functions
 */
export function dictIsEmpty(obj: any): boolean {
  return Object.keys(obj).length === 0;
}

/*
 * kfserving helpers
 */
export function svcHasComponent(
  svc: InferenceServiceK8s,
  component: string,
): boolean {
  return !!svc.spec[component];
}

export function getSvcComponents(svc: InferenceServiceK8s): string[] {
  const components: string[] = [];

  ['predictor', 'transformer', 'explainer'].forEach(c => {
    if (!svcHasComponent(svc, c)) {
      return;
    }

    components.push(c);
  });

  return components;
}

export function getReadyCondition(obj: K8sObject): Condition {
  let cs: Condition[] = [];
  try {
    cs = obj.status.conditions;
  } catch (err) {
    return undefined;
  }

  if (!cs) {
    return undefined;
  }

  for (const c of cs) {
    if (c.type !== 'Ready') {
      continue;
    }

    return c;
  }
}

export function getK8sObjectUiStatus(obj: K8sObject): Status {
  const status: Status = { phase: '', state: '', message: '' };

  if (obj.metadata.deletionTimestamp) {
    status.phase = STATUS_TYPE.TERMINATING;
    status.message = `${obj.kind} is being deleted`;
    return status;
  }

  if (!obj.status) {
    status.phase = STATUS_TYPE.UNAVAILABLE;
    status.message = `${obj.kind} has no status`;
    return status;
  }

  const readyCondition = getReadyCondition(obj);
  if (readyCondition === undefined) {
    status.phase = STATUS_TYPE.WARNING;
    status.message = 'No Ready condition available';
    return status;
  }

  if (readyCondition.status === 'True') {
    status.phase = STATUS_TYPE.READY;
    status.message = `${obj.kind} is Ready`;
    return status;
  }

  status.phase = STATUS_TYPE.WAITING;
  status.message = readyCondition.message;
  return status;
}

export function getK8sObjectStatus(obj: K8sObject): [string, string] {
  const readyCondition = getReadyCondition(obj);

  if (readyCondition === undefined) {
    return [`Couldn't deduce the status. Missing Ready condition`, 'warning'];
  }

  if (readyCondition.status === 'True') {
    return ['Ready', 'check_circle'];
  }

  return [readyCondition.message, 'warning'];
}

// functions for processing the InferenceService spec
export function getPredictorType(predictor: PredictorSpec): PredictorType {
  for (const predictorType of Object.values(PredictorType)) {
    if (predictorType in predictor) {
      return predictorType;
    }
  }

  return PredictorType.Custom;
}

export function getPredictorExtensionSpec(
  predictor: PredictorSpec,
): PredictorExtensionSpec {
  for (const predictorType of Object.values(PredictorType)) {
    if (predictorType in predictor) {
      return predictor[predictorType];
    }
  }

  // In the case of Custom predictors, set the additional PredictorExtensionSpec fields
  // manually here
  const spec = predictor.containers[0] as PredictorExtensionSpec;
  spec.runtimeVersion = '';
  spec.protocolVersion = '';

  if (predictor.containers[0].env) {
    const storageUri = predictor.containers[0].env.find(
      envVar => envVar.name.toLowerCase() === 'storage_uri'
    );
    if (storageUri) {
      spec.storageUri = storageUri.value;
    }
  }

  return spec;
}

export function getExplainerContainer(explainer: ExplainerSpec): V1Container {
  if ('alibi' in explainer) {
    return explainer.alibi;
  }

  if ('aix' in explainer) {
    return explainer.aix;
  }

  return null;
}