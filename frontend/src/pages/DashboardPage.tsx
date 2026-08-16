import { useEffect, useState } from 'react';
import { getIssues, getRecentReports, getResolutions, getAuditTrail } from '../api/reports';
import type { IssueSummary, ReportEntity, ResolutionSummary, AuditEntrySummary } from '../types/report';

export function DashboardPage() {
  const [issues, setIssues] = useState<IssueSummary[]>([]);
  const [recentReports, setRecentReports] = useState<ReportEntity[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'issues' | 'reports'>('issues');
  const [selectedIssueId, setSelectedIssueId] = useState<string | null>(null);
  const [resolutions, setResolutions] = useState<ResolutionSummary[]>([]);
  const [auditEntries, setAuditEntries] = useState<AuditEntrySummary[]>([]);
  const [modalLoading, setModalLoading] = useState(false);

  async function loadData() {
    setLoading(true);
    try {
      const [issuesData, reportsData] = await Promise.all([
        getIssues(),
        getRecentReports(),
      ]);
      setIssues(issuesData);
      setRecentReports(reportsData);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 8000);
    return () => clearInterval(interval);
  }, []);

  async function openIssueDetail(issueId: string) {
    setSelectedIssueId(issueId);
    setModalLoading(true);
    try {
      const [resData, auditData] = await Promise.all([
        getResolutions(issueId),
        getAuditTrail(issueId),
      ]);
      setResolutions(resData);
      setAuditEntries(auditData);
    } catch (err) {
      console.error('Failed to load issue details:', err);
    } finally {
      setModalLoading(false);
    }
  }

  const totalReports = recentReports.length;
  const totalIssues = issues.length;
  const resolvedIssues = issues.filter((i) => i.isResolved).length;
  const activeConflicts = issues.filter((i) => i.actionTaken === 'CONFLICT').length;

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Real-Time Resolution Dashboard</h1>
          <p className="page-description">
            Live ingestion monitoring, identity mapping correlation, and deterministic state resolution.
          </p>
        </div>
        <button className="btn btn-secondary" onClick={loadData} disabled={loading}>
          {loading ? 'Refreshing...' : '↻ Refresh'}
        </button>
      </div>

      {/* Metric Cards Grid */}
      <div className="metrics-grid">
        <div className="metric-card cyan">
          <span className="metric-label">Total Ingested Reports</span>
          <span className="metric-value">{totalReports}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">Unified Issues</span>
          <span className="metric-value">{totalIssues}</span>
        </div>
        <div className="metric-card emerald">
          <span className="metric-label">Resolved Issues</span>
          <span className="metric-value">{resolvedIssues}</span>
        </div>
        <div className="metric-card rose">
          <span className="metric-label">Conflicts Detected</span>
          <span className="metric-value">{activeConflicts}</span>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="card">
        <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
          <button
            className={activeTab === 'issues' ? 'btn' : 'btn btn-secondary'}
            onClick={() => setActiveTab('issues')}
            style={{ fontSize: '0.85rem', padding: '0.45rem 1rem' }}
          >
            Tracked Issues ({issues.length})
          </button>
          <button
            className={activeTab === 'reports' ? 'btn' : 'btn btn-secondary'}
            onClick={() => setActiveTab('reports')}
            style={{ fontSize: '0.85rem', padding: '0.45rem 1rem' }}
          >
            Raw Ingested Reports ({recentReports.length})
          </button>
        </div>

        {activeTab === 'issues' && (
          <div className="table-container">
            {issues.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem' }}>
                No civic issues recorded yet. Submit a report or run a batch replay to get started!
              </p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Issue ID</th>
                    <th>Category</th>
                    <th>Coordinates</th>
                    <th>Version</th>
                    <th>Latest Action</th>
                    <th>Status</th>
                    <th>Reports</th>
                    <th>Last Modified</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {issues.map((issue) => (
                    <tr key={issue.issueId}>
                      <td><code>{issue.issueId}</code></td>
                      <td>
                        <span style={{ fontWeight: 600, textTransform: 'capitalize' }}>{issue.category}</span>
                      </td>
                      <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {issue.latitude.toFixed(4)}, {issue.longitude.toFixed(4)}
                      </td>
                      <td>
                        <span style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>v{issue.latestVersion}</span>
                      </td>
                      <td>
                        <span className={`badge ${
                          issue.actionTaken === 'NEW_ISSUE' ? 'badge-new' :
                          issue.actionTaken === 'REFINED' ? 'badge-refined' :
                          issue.actionTaken === 'DUPLICATE' ? 'badge-duplicate' : 'badge-conflict'
                        }`}>
                          {issue.actionTaken}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${issue.isResolved ? 'badge-resolved' : 'badge-new'}`}>
                          {issue.isResolved ? 'RESOLVED' : 'OPEN'}
                        </span>
                      </td>
                      <td>{issue.reportCount}</td>
                      <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                        {new Date(issue.lastModified).toLocaleTimeString()}
                      </td>
                      <td>
                        <button
                          className="btn btn-preset"
                          onClick={() => openIssueDetail(issue.issueId)}
                        >
                          Inspect
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'reports' && (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Report ID</th>
                  <th>User ID</th>
                  <th>Identity ID</th>
                  <th>Category</th>
                  <th>Source</th>
                  <th>Timestamp</th>
                  <th>Flags</th>
                </tr>
              </thead>
              <tbody>
                {recentReports.map((r) => (
                  <tr key={r.reportId}>
                    <td><code>{r.reportId}</code></td>
                    <td>{r.userId}</td>
                    <td><code>{r.identityId ? r.identityId.slice(0, 8) + '...' : '-'}</code></td>
                    <td><span style={{ textTransform: 'capitalize' }}>{r.category}</span></td>
                    <td>
                      <span className={`badge ${r.source === 'mobile' ? 'badge-refined' : 'badge-new'}`}>
                        {r.source}
                      </span>
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(r.timestamp).toLocaleString()}
                    </td>
                    <td>
                      {r.duplicate && <span className="badge badge-duplicate" style={{ marginRight: '0.25rem' }}>DUP</span>}
                      {r.resolved && <span className="badge badge-resolved">RESOLVED</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal / Inspector Drawer for selected issue */}
      {selectedIssueId && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 100,
          backdropFilter: 'blur(4px)',
        }}>
          <div className="card" style={{ maxWidth: '800px', width: '90%', maxHeight: '85vh', overflowY: 'auto', position: 'relative' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <div>
                <h2 style={{ margin: 0, fontSize: '1.3rem', color: '#fff' }}>
                  Issue Inspector: <code>{selectedIssueId}</code>
                </h2>
                <p style={{ margin: '0.25rem 0 0', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Complete version history & audit decision log
                </p>
              </div>
              <button
                className="btn btn-secondary"
                style={{ padding: '0.35rem 0.75rem' }}
                onClick={() => setSelectedIssueId(null)}
              >
                ✕ Close
              </button>
            </div>

            {modalLoading ? (
              <p style={{ color: 'var(--text-muted)', padding: '2rem', textAlign: 'center' }}>Loading issue timeline...</p>
            ) : (
              <>
                <h3 style={{ fontSize: '1rem', color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>
                  Version Timeline ({resolutions.length} versions)
                </h3>
                <div style={{ display: 'grid', gap: '0.75rem', marginBottom: '1.5rem' }}>
                  {resolutions.map((res) => (
                    <div key={res.version} style={{
                      background: 'rgba(15, 23, 42, 0.6)',
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '0.9rem',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}>
                      <div>
                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                          <span style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>Version {res.version}</span>
                          <span className={`badge ${
                            res.actionTaken === 'NEW_ISSUE' ? 'badge-new' :
                            res.actionTaken === 'REFINED' ? 'badge-refined' :
                            res.actionTaken === 'DUPLICATE' ? 'badge-duplicate' : 'badge-conflict'
                          }`}>
                            {res.actionTaken}
                          </span>
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                          Evidence reports: {res.evidence.join(', ')}
                        </div>
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textAlign: 'right' }}>
                        {new Date(res.lastModified).toLocaleTimeString()}
                      </div>
                    </div>
                  ))}
                </div>

                <h3 style={{ fontSize: '1rem', color: 'var(--accent-emerald)', marginBottom: '0.5rem' }}>
                  Decision Audit Trail ({auditEntries.length} events)
                </h3>
                <div style={{ display: 'grid', gap: '0.75rem' }}>
                  {auditEntries.map((entry, idx) => (
                    <div key={idx} style={{
                      background: 'rgba(15, 23, 42, 0.6)',
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '0.9rem',
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.35rem' }}>
                        <strong style={{ color: '#fff' }}>Action: {entry.action}</strong>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {new Date(entry.resolvedAt).toLocaleString()}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        Resolved by identity: <code>{entry.resolvedBy}</code>
                      </div>
                      {entry.stateBefore && (
                        <details style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          <summary style={{ cursor: 'pointer', color: 'var(--text-secondary)' }}>State Before</summary>
                          <pre style={{ background: '#020617', padding: '0.5rem', borderRadius: '4px', overflowX: 'auto' }}>
                            {entry.stateBefore}
                          </pre>
                        </details>
                      )}
                      <details style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        <summary style={{ cursor: 'pointer', color: 'var(--text-secondary)' }}>State After</summary>
                        <pre style={{ background: '#020617', padding: '0.5rem', borderRadius: '4px', overflowX: 'auto' }}>
                          {entry.stateAfter}
                        </pre>
                      </details>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </>
  );
}
