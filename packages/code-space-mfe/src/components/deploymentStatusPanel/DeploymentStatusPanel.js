import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './DeploymentStatusPanel.scss';
import { buildGitJobLogViewAWSURL } from '../../Utility/utils';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
 
const PIPELINE_PHASES = ['QUEUED', 'BUILDING', 'DEPLOYING', 'DONE'];
 
const formatDuration = (seconds) => {
  if (seconds === null || seconds === undefined) return '—';
  const s = Math.max(0, Math.floor(seconds));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  const pad = (n) => String(n).padStart(2, '0');
  return h > 0 ? `${h}:${pad(m)}:${pad(sec)}` : `${m}:${pad(sec)}`;
};
 
const formatTs = (ts) => {
  if (!ts) return '';
  try {
    return new Date(ts).toLocaleString();
  } catch {
    return '';
  }
};
 
// Derive a UI state from the aggregated backend DTO.
const deriveUiState = (status) => {
  if (!status) return 'LOADING';
  const overall = (status.overallStatus || '').toUpperCase();
  const phase = (status.phase || '').toUpperCase();
 
  // Cancelled / timed_out are surfaced via job conclusions from GitHub.
  const jobConclusions = (status.jobs || []).map((j) => (j.conclusion || '').toLowerCase());
  if (jobConclusions.includes('cancelled')) return 'CANCELLED';
  if (jobConclusions.includes('timed_out')) return 'TIMEOUT';
  if (overall === 'BUILD_FAILED' && status.message && /not generated|within/i.test(status.message)) return 'TIMEOUT';
 
  if (phase === 'FAILED') return 'FAILED';
  if (phase === 'DONE') return 'SUCCESS';
  if (phase === 'QUEUED' || status.runStarted === false) return 'QUEUE';
  if (phase === 'BUILDING' || phase === 'DEPLOYING' || phase === 'RUNNING') return 'RUNNING';
  if (overall === 'NONE') return 'EMPTY';
  return 'RUNNING';
};
 
const CHIP = {
  LOADING: { label: 'Loading…', cls: 'chipLoading' },
  EMPTY: { label: 'No activity', cls: 'chipLoading' },
  QUEUE: { label: 'Queued', cls: 'chipQueued' },
  RUNNING: { label: 'Running', cls: 'chipRunning' },
  SUCCESS: { label: 'Succeeded', cls: 'chipSuccess' },
  FAILED: { label: 'Failed', cls: 'chipFailed' },
  CANCELLED: { label: 'Cancelled', cls: 'chipCancelled' },
  TIMEOUT: { label: 'Timed out', cls: 'chipTimeout' },
};
 
const StepIcon = ({ state }) => {
  if (state === 'done') return <span className={classNames(Styles.dot, Styles.done)}>✓</span>;
  if (state === 'active') return <span className={classNames(Styles.dot, Styles.active)}>▶</span>;
  if (state === 'failed') return <span className={classNames(Styles.dot, Styles.failed)}>✕</span>;
  return <span className={Styles.dot}>○</span>;
};
 
const PipelineStepper = ({ status, uiState }) => {
  const currentPhase = (status?.phase || '').toUpperCase();
  const currentIdx = PIPELINE_PHASES.indexOf(currentPhase === 'RUNNING' ? 'BUILDING' : currentPhase);
  return (
    <ul className={Styles.stepper}>
      {PIPELINE_PHASES.map((p, idx) => {
        let state = 'pending';
        if (uiState === 'FAILED' && idx === currentIdx) state = 'failed';
        else if (idx < currentIdx || uiState === 'SUCCESS') state = 'done';
        else if (idx === currentIdx) state = 'active';
        const label = p === 'DONE' ? 'Complete' : p.charAt(0) + p.slice(1).toLowerCase();
        return (
          <li key={p} className={Styles.step}>
            <StepIcon state={state} />
            <span>{label}</span>
          </li>
        );
      })}
    </ul>
  );
};
 
const GREEN = '#0ae6ab';
const RED = '#e84d47';
const BLUE = '#00adef';
const MUTED = '#5a6470';
 
const FAIL_LABELS = {
  failure: 'Failed',
  cancelled: 'Cancelled',
  timed_out: 'Timed out',
  action_required: 'Action required',
};
 
const JobCard = ({ job }) => {
  const status = (job.status || '').toLowerCase();
  const conclusion = (job.conclusion || '').toLowerCase();
  const isCompleted = status === 'completed';
  const isSuccess = isCompleted && ['success', 'neutral'].includes(conclusion);
  const isSkipped = isCompleted && conclusion === 'skipped';
  const isFailed = isCompleted && ['failure', 'cancelled', 'timed_out', 'action_required'].includes(conclusion);
  const isRunning = status === 'in_progress';
 
  // Bar: green + full when passed, red + full when failed, blue (progress) while
  // running, empty when not started yet. Colour is set inline so it can never be
  // overridden by CSS-module class ordering.
  let fillWidth = 0;
  let fillColor = 'transparent';
  if (isSuccess) {
    fillWidth = 100;
    fillColor = GREEN;
  } else if (isFailed) {
    fillWidth = 100;
    fillColor = RED;
  } else if (isRunning) {
    fillWidth = job.progress || 0;
    fillColor = BLUE;
  }
 
  const TRACK_BG = '#1c2029';
 
  // Force colours with `!important` via refs so they can't be beaten by any
  // (possibly stale/cached) stylesheet rule, and force an explicit height so the
  // bar is visible even if the `.progressFill { height:100% }` rule didn't load.
  const trackRef = useRef(null);
  const fillRef = useRef(null);
  useEffect(() => {
    const track = trackRef.current;
    if (track) {
      // Fully colour the track for terminal states (guaranteed-visible 6px box);
      // keep it dark while running (the fill shows blue progress) / not started.
      const trackColor = isSuccess ? GREEN : isFailed ? RED : isSkipped ? MUTED : TRACK_BG;
      track.style.setProperty('background', trackColor, 'important');
      track.style.setProperty('background-color', trackColor, 'important');
    }
    const el = fillRef.current;
    if (el) {
      el.style.setProperty('height', '100%', 'important');
      el.style.setProperty('min-height', '6px', 'important');
      el.style.setProperty('width', `${fillWidth}%`, 'important');
      el.style.setProperty('background', fillColor, 'important');
      el.style.setProperty('background-color', fillColor, 'important');
    }
  }, [fillWidth, fillColor, isSuccess, isFailed, isSkipped]);
 
  // Only passed jobs display "completed". Failed jobs show their outcome; jobs
  // that are still running / not started never show "completed".
  let meta;
  const dur = job.durationSeconds != null ? ` · ${formatDuration(job.durationSeconds)}` : '';
  if (isSuccess) {
    meta = `completed${dur}`;
  } else if (isSkipped) {
    meta = 'skipped';
  } else if (isFailed) {
    meta = `${FAIL_LABELS[conclusion] || 'Failed'}${dur}`;
  } else if (isRunning) {
    meta = `running${dur}`;
  } else {
    meta = 'waiting';
  }
 
  return (
    <div className={Styles.jobCard}>
      <div className={Styles.jobHeader}>
        <span className={Styles.jobName} title={job.name}>{job.name}</span>
        <span className={classNames(Styles.jobMeta, isFailed && Styles.jobMetaFailed)}>{meta}</span>
      </div>
      <div ref={trackRef} className={Styles.progressTrack} style={{ minHeight: 6 }}>
        <div ref={fillRef} className={Styles.progressFill} style={{ height: '100%', minHeight: 6, width: `${fillWidth}%`, background: fillColor }} />
      </div>
      {(job.totalSteps > 0 || (isRunning && job.currentStep)) && (
        <div className={Styles.jobSub}>
          {job.totalSteps > 0 ? `${job.completedSteps || 0}/${job.totalSteps} steps` : ''}
          {isRunning && job.currentStep ? `${job.totalSteps > 0 ? ' · ' : ''}${job.currentStep}` : ''}
        </div>
      )}
    </div>
  );
};
 
const StepList = ({ steps }) => (
  <ul className={Styles.stepList}>
    {(steps || []).map((s) => {
      const c = (s.conclusion || '').toLowerCase();
      const state = s.status === 'completed'
        ? (c === 'success' || c === 'skipped' ? 'done' : 'failed')
        : (s.status === 'in_progress' ? 'active' : 'pending');
      return (
        <li key={s.number} className={Styles.stepRow}>
          <StepIcon state={state} />
          <span className={Styles.stepName}>{s.name}</span>
          <span className={Styles.stepDur}>{formatDuration(s.durationSeconds)}</span>
        </li>
      );
    })}
  </ul>
);
 
const DeploymentStatusPanel = ({ projectName, disabled, workflowStatus }) => {
  const [open, setOpen] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const containerRef = useRef(null);
 
  // Presentational only — no fetch, no polling, no SSE. The workflow status is
  // supplied by the parent from the workspace refresh response (getById on the
  // user-triggered Refresh, which already reconciles status on the stale
  // threshold). This panel just renders whatever data it is handed.
  const status = workflowStatus || null;
 
  // Close popover on outside click (drawer has its own overlay).
  useEffect(() => {
    if (!open || expanded) return undefined;
    const onDocClick = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', onDocClick);
    return () => document.removeEventListener('mousedown', onDocClick);
  }, [open, expanded]);
 
  const uiState = !status ? 'LOADING' : deriveUiState(status);
  const inProgress = uiState === 'QUEUE' || uiState === 'RUNNING';
  const chip = CHIP[uiState] || CHIP.LOADING;
  const runId = status?.runId;
  const logUrl = runId ? buildGitJobLogViewAWSURL(runId) : null;
 
  const copySummary = () => {
    if (!status) return;
    const lines = [
      `Deployment status: ${projectName}${status.environment ? ` (${status.environment})` : ''}`,
      `Status: ${status.overallStatus || '-'} (${chip.label})`,
      status.runId ? `Run: #${status.runNumber || status.runId}` : 'Run: not started yet',
      status.branch ? `Branch: ${status.branch}` : null,
      status.commitSha ? `Commit: ${status.commitSha}` : null,
      status.triggeredBy ? `Triggered by: ${status.triggeredBy}` : null,
      status.lastError ? `Error: ${status.lastError}` : null,
    ].filter(Boolean);
    navigator.clipboard.writeText(lines.join('\n')).then(() => {
      Notification.show('Deployment status summary copied');
    });
  };
 
  const renderBody = () => {
    if (uiState === 'LOADING') {
      return <div className={Styles.stateMsg}>Loading deployment status…</div>;
    }
    if (uiState === 'EMPTY') {
      return <div className={Styles.stateMsg}>No build or deploy activity yet for this workspace.</div>;
    }
    if (uiState === 'QUEUE') {
      return (
        <div className={Styles.queueBox}>
          <p className={Styles.queueTitle}>Build request received</p>
          <p>Waiting for GitHub to start the workflow…</p>
          <p className={Styles.queueHint}>Build is in queue. <strong>The build has not started yet.</strong></p>
          <p className={Styles.queueHint}>Estimated next step: GitHub is provisioning a runner.</p>
          {status?.message && <p className={Styles.queueMsg}>{status.message}</p>}
        </div>
      );
    }
    return (
      <>
        <PipelineStepper status={status} uiState={uiState} />
        {status?.stale && <div className={Styles.staleBanner}>Live GitHub status temporarily unavailable — showing last known state.</div>}
        {(status?.jobs || []).map((job) => (
          <React.Fragment key={job.id || job.name}>
            <JobCard job={job} />
            {expanded && <StepList steps={job.steps} />}
          </React.Fragment>
        ))}
        {status?.lastError && (uiState === 'FAILED') && (
          <div className={Styles.errorBox}>{status.lastError}</div>
        )}
        {expanded && status?.activity?.length > 0 && (
          <div className={Styles.activity}>
            <h5>Activity</h5>
            <ul>
              {status.activity.map((a, i) => (
                <li key={i}><span className={Styles.activityTs}>{formatTs(a.ts)}</span> {a.message}</li>
              ))}
            </ul>
          </div>
        )}
      </>
    );
  };
 
  const header = (
    <div className={Styles.panelHeader}>
      <span className={classNames(Styles.chip, Styles[chip.cls])}>{chip.label}</span>
      {status?.elapsedSeconds != null && uiState !== 'QUEUE' && (
        <span className={Styles.elapsed}>{formatDuration(status.elapsedSeconds)} elapsed</span>
      )}
      <span className={Styles.headerActions}>
        {!expanded && (
          <button type="button" className={Styles.iconBtn} title="Expand" onClick={() => setExpanded(true)}>⤢</button>
        )}
        <button type="button" className={Styles.iconBtn} title="Close" onClick={() => { setExpanded(false); setOpen(false); }}>✕</button>
      </span>
    </div>
  );
 
  const meta = status?.runStarted && (
    <div className={Styles.meta}>
      {status.workflowName && <span>Workflow: <strong>{status.workflowName}</strong></span>}
      {status.runNumber && <span>Run #{status.runNumber}</span>}
      {status.branch && <span>branch: {status.branch}</span>}
      {status.commitSha && <span title={status.commitSha}>commit: {String(status.commitSha).slice(0, 7)}</span>}
      {status.triggeredBy && <span>by: {status.triggeredBy}</span>}
      {status.repository && <span>repo: {status.repository}</span>}
      {status.startedAt && <span>started: {formatTs(status.startedAt)}</span>}
    </div>
  );
 
  const actions = (
    <div className={Styles.actions}>
      {logUrl && (
        <a href={logUrl} target="_blank" rel="noreferrer" className={Styles.actionBtn}>View Logs ↗</a>
      )}
      {status?.githubActionUrl && (
        <a href={status.githubActionUrl} target="_blank" rel="noreferrer" className={Styles.actionBtn}>Open GitHub Action ↗</a>
      )}
      <button type="button" className={Styles.actionBtn} onClick={copySummary}>Copy summary</button>
    </div>
  );
 
  return (
    <span className={Styles.infoWrapper} ref={containerRef}>
      <span
        className={classNames(Styles.infoIcon, inProgress && Styles.infoActive, disabled && Styles.infoDisabled)}
        tooltip-data="View deployment status"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          if (disabled) return;
          setOpen((o) => !o);
        }}
      >
        <i className="icon mbc-icon info" />
        {inProgress && <span className={Styles.liveDot} />}
      </span>
 
      {open && !expanded && (
        <div className={Styles.popover} onClick={(e) => e.stopPropagation()}>
          {header}
          {meta}
          {renderBody()}
          {actions}
        </div>
      )}
 
      {open && expanded && (
        <div className={Styles.drawerOverlay} onClick={() => { setExpanded(false); setOpen(false); }}>
          <div className={Styles.drawer} onClick={(e) => e.stopPropagation()}>
            <div className={Styles.drawerTitle}>Deployment Status — {projectName}</div>
            {header}
            {meta}
            <div className={Styles.drawerBody}>{renderBody()}</div>
            {actions}
          </div>
        </div>
      )}
    </span>
  );
};
 
export default DeploymentStatusPanel;