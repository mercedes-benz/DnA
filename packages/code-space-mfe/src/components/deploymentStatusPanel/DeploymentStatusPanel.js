import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './DeploymentStatusPanel.scss';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
 
// ---------------------------------------------------------------------------
// Build Execution Summary modal.
//
// Design constraints (per requirement):
//   - Info icon is ALWAYS visible on the card, regardless of build status.
//   - NO polling / NO auto-refresh / NO background GitHub calls.
//   - GitHub-backed data (workflowStatus) is fetched ONLY when the modal is
//     opened and when the user clicks the in-modal Refresh button. Both go
//     through the existing user-refresh path getWorkspaceById(id, true), which
//     already reconciles status on the stale threshold and calls GitHub.
//   - Logs are placeholder/dummy content for now (no log API wired yet).
// ---------------------------------------------------------------------------
 
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
  if (!ts) return '—';
  try {
    return new Date(ts).toLocaleString();
  } catch {
    return '—';
  }
};
 
// Canonical "important" steps shown to the user. Noise (skipped/internal/setup
// steps) is intentionally excluded — we only surface these four milestones and
// map them from whatever GitHub job/step names are present.
const IMPORTANT_STEPS = [
  { label: 'Update Git Run ID', match: ['update git job run id', 'update git', 'git job run', 'run id'] },
  // Build & Deploy is a single job in GitHub ("Build or Deploy workspace application").
  { label: 'Build or Deploy workspace application', match: ['build or deploy'] },
  { label: 'Update Status', match: ['update status'] },
];
 
// When several jobs/steps match a label, prefer the one that is furthest along
// so a real success/failure wins over a skipped sibling (e.g. "Update Status
// build_deploy" success vs "Update Status restart" skipped).
const STATE_PRIORITY = { done: 5, failed: 4, active: 3, skipped: 2, pending: 1 };
 
// Flatten jobs + their steps into a single searchable list of {name,status,conclusion,duration}.
const flattenActivities = (status) => {
  const items = [];
  (status?.jobs || []).forEach((job) => {
    items.push({
      name: job.name || '',
      status: job.status,
      conclusion: job.conclusion,
      durationSeconds: job.durationSeconds,
    });
    (job.steps || []).forEach((step) => {
      items.push({
        name: step.name || '',
        status: step.status,
        conclusion: step.conclusion,
        durationSeconds: step.durationSeconds,
      });
    });
  });
  return items;
};
 
const deriveStepState = (item) => {
  if (!item) return 'pending';
  const s = (item.status || '').toLowerCase();
  const c = (item.conclusion || '').toLowerCase();
  if (s === 'completed') {
    if (['success', 'neutral'].includes(c)) return 'done';
    if (c === 'skipped') return 'skipped';
    if (['failure', 'cancelled', 'timed_out', 'action_required'].includes(c)) return 'failed';
    return 'done';
  }
  if (s === 'in_progress') return 'active';
  return 'pending';
};
 
// Build the fixed list of important steps with a resolved state from live data.
const buildImportantSteps = (status) => {
  const activities = flattenActivities(status);
  return IMPORTANT_STEPS.map((def) => {
    const matches = activities.filter((a) => {
      const n = a.name.toLowerCase();
      return def.match.some((m) => n.includes(m));
    });
    let best = null;
    let bestScore = -1;
    matches.forEach((a) => {
      const st = deriveStepState(a);
      const score = STATE_PRIORITY[st] || 0;
      if (score > bestScore) {
        bestScore = score;
        best = { item: a, state: st };
      }
    });
    return {
      label: def.label,
      state: best ? best.state : 'pending',
      durationSeconds: best ? best.item.durationSeconds : null,
    };
  });
};
 
const StepIcon = ({ state }) => {
  if (state === 'done') return <span className={classNames(Styles.stepDot, Styles.done)}>✓</span>;
  if (state === 'active') return <span className={classNames(Styles.stepDot, Styles.active)}>▶</span>;
  if (state === 'failed') return <span className={classNames(Styles.stepDot, Styles.failed)}>✕</span>;
  if (state === 'skipped') return <span className={classNames(Styles.stepDot, Styles.skipped)}>–</span>;
  return <span className={Styles.stepDot}>○</span>;
};
 
const deriveWorkflowStatusLabel = (status) => {
  if (!status) return '—';
  const jobs = status.jobs || [];
  if (jobs.length === 0) return status.phase || status.overallStatus || '—';
  const anyFailed = jobs.some((j) => (j.status || '').toLowerCase() === 'completed'
    && ['failure', 'cancelled', 'timed_out', 'action_required'].includes((j.conclusion || '').toLowerCase()));
  if (anyFailed) return 'completed / failed';
  const anyRunning = jobs.some((j) => (j.status || '').toLowerCase() === 'in_progress');
  if (anyRunning) return 'in progress';
  const allDone = jobs.every((j) => (j.status || '').toLowerCase() === 'completed');
  if (allDone) return 'completed / success';
  return status.phase || 'queued';
};
 
// Placeholder log content until a real log source is wired in.
const buildDummyLogs = (status, projectName) => {
  const runId = status?.runId || 'N/A';
  const lines = [
    `[info] ---- Build Execution Log (preview) ----`,
    `[info] project        : ${projectName || '-'}`,
    `[info] workflow run id : ${runId}`,
    `[info] workflow        : ${status?.workflowName || '-'}`,
    `[info] branch          : ${status?.branch || '-'}`,
    `[info] triggered by    : ${status?.triggeredBy || '-'}`,
    `[info] ----------------------------------------`,
    `[10:52:56] ▶ Update Git Run ID`,
    `[10:52:58] ✔ Git run id updated (${runId})`,
    `[10:52:59] ▶ Build or Deploy workspace application`,
    `[10:53:48] ✔ Build/Deploy completed successfully`,
    `[10:54:31] ▶ Update Status`,
    `[10:54:32] ✔ Workspace status updated`,
    ``,
    `[note] These are placeholder logs. Live build logs will be wired in a later iteration.`,
  ];
  return lines.join('\n');
};
 
const DeploymentStatusPanel = ({
  codeSpaceId,
  projectName,
  environment,
  workspaceStatus,
  buildStatus,
  initialStatus,
  onData,
  disabled,
}) => {
  const [open, setOpen] = useState(false);
  const [status, setStatus] = useState(initialStatus || null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [loadedOnce, setLoadedOnce] = useState(false);
 
  // On-demand fetch — used on open and on manual Refresh only.
  const load = () => {
    if (!codeSpaceId) return;
    setLoading(true);
    setError(null);
    CodeSpaceApiClient.getWorkspaceById(codeSpaceId, true)
      .then((res) => {
        setLoading(false);
        setLoadedOnce(true);
        if (res && res.data) {
          setStatus(res.data.projectDetails?.workflowStatus || null);
          if (onData) onData(res.data);
        }
      })
      .catch(() => {
        setLoading(false);
        setLoadedOnce(true);
        setError('Unable to fetch the latest build & deploy status. Showing last known data.');
      });
  };
 
  const openModal = (e) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
    if (disabled) return;
    setOpen(true);
    // Fetch once when the modal opens (only GitHub call besides manual Refresh).
    load();
  };
 
  const closeModal = () => setOpen(false);
 
  const runStarted = !!(status && status.runStarted && status.runId);
  const isFailedStatus = ['BUILD_FAILED', 'DEPLOYMENT_FAILED', 'FAILED', 'RESTART_FAILED', 'APPROVAL_REJECTED']
    .includes((buildStatus || '').toUpperCase());
  const logs = buildDummyLogs(status, projectName);
 
  const copyLogs = () => {
    navigator.clipboard.writeText(logs).then(() => {
      Notification.show('Build logs copied to clipboard');
    });
  };
 
  const importantSteps = buildImportantSteps(status);
 
  const summaryRow = (label, value) => (
    <div className={Styles.summaryRow}>
      <span className={Styles.summaryLabel}>{label}</span>
      <span className={Styles.summaryValue}>{value}</span>
    </div>
  );
 
  const renderBody = () => {
    if (loading && !loadedOnce) {
      return <div className={Styles.stateMsg}>Loading build execution summary…</div>;
    }
    if (!runStarted) {
      if (isFailedStatus) {
        return (
          <div className={classNames(Styles.notStarted, Styles.failedState)}>
            <i className="icon mbc-icon alert circle" />
            <p>Build failed.</p>
            <span>
              No GitHub run ID was recorded for this build. It most likely failed
              because the workflow job was not picked up within the allotted time
              (build timeout). Please retrigger the build or check the pipeline
              configuration.
            </span>
          </div>
        );
      }
      return (
        <div className={Styles.notStarted}>
          <i className="icon mbc-icon alert circle" />
          <p>Build workflow has not started yet.</p>
          <span>GitHub workflow has not been picked up yet. Click Refresh once the build begins.</span>
        </div>
      );
    }
    return (
      <div className={Styles.bodyGrid}>
        <div className={Styles.leftCol}>
          <div className={Styles.section}>
            <h5 className={Styles.sectionTitle}>Build Execution Summary</h5>
            {summaryRow('Workspace status', workspaceStatus || '—')}
            {summaryRow('Build status', buildStatus || status?.overallStatus || '—')}
            {summaryRow('Build started', formatTs(status?.startedAt))}
            {summaryRow('Last updated', formatTs(status?.updatedAt))}
            {summaryRow('Run ID', status?.runId || '—')}
            {summaryRow('Workflow status', deriveWorkflowStatusLabel(status))}
          </div>
 
          <div className={Styles.section}>
            <h5 className={Styles.sectionTitle}>Workflow Steps</h5>
            <ul className={Styles.stepList}>
              {importantSteps.map((s) => (
                <li key={s.label} className={Styles.stepRow}>
                  <StepIcon state={s.state} />
                  <span className={Styles.stepName}>{s.label}</span>
                  <span className={Styles.stepDur}>
                    {s.state === 'pending' ? '—' : formatDuration(s.durationSeconds)}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        </div>
 
        <div className={Styles.rightCol}>
          <div className={classNames(Styles.section, Styles.logsSection)}>
            <div className={Styles.logsHeader}>
              <h5 className={Styles.sectionTitle}>Build Logs</h5>
              <button type="button" className={Styles.copyBtn} onClick={copyLogs}>Copy Logs</button>
            </div>
            <pre className={Styles.logViewer}>{logs}</pre>
          </div>
        </div>
      </div>
    );
  };
 
  return (
    <span className={Styles.infoWrapper}>
      <span
        className={classNames(Styles.infoIcon, disabled && Styles.infoDisabled)}
        tooltip-data="View build execution summary"
        onClick={openModal}
      >
        <i className="icon mbc-icon info" />
      </span>
 
      {open && (
        <div className={Styles.modalOverlay}>
          <div className={Styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={Styles.modalHeader}>
              <span className={Styles.modalTitle}>
                {projectName}
                {environment ? <span className={Styles.envTag}>{environment}</span> : null}
              </span>
              <span className={Styles.headerActions}>
                <button
                  type="button"
                  className={classNames(Styles.iconBtn, loading && Styles.spinning)}
                  title="Refresh"
                  disabled={loading}
                  onClick={load}
                >
                  <i className="icon mbc-icon refresh" />
                </button>
                <button type="button" className={Styles.iconBtn} title="Close" onClick={closeModal}>✕</button>
              </span>
            </div>
            {error && <div className={Styles.errorBanner}>{error}</div>}
            <div className={Styles.modalBody}>{renderBody()}</div>
          </div>
        </div>
      )}
    </span>
  );
};
 
export default DeploymentStatusPanel;