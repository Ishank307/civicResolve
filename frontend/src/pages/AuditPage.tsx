import { useState, type FormEvent } from 'react';
import { getAuditTrail } from '../api/reports';
import type { AuditEntrySummary } from '../types/report';

export function AuditPage() {
  const [issueId, setIssueId] = useState('');
  const [entries, setEntries] = useState<AuditEntrySummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLookup(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await getAuditTrail(issueId.trim());
      setEntries(data);
      if (data.length === 0) {
        setError('No audit entries found for issue ID: ' + issueId.trim());
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lookup failed');
      setEntries([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Decision Audit Trail</h1>
          <p className="page-description">
            Complete, immutable decision audit history and state transition trail for any issue.
          </p>
        </div>
      </div>

      <div className="card">
        <form className="form-grid" onSubmit={handleLookup}>
          <label>
            Issue ID
            <input
              required
              placeholder="e.g. issue-b22138bfe4d7b6f1"
              value={issueId}
              onChange={(e) => setIssueId(e.target.value)}
            />
          </label>
          <button className="btn" type="submit" disabled={loading}>
            {loading ? 'Searching…' : '🔍 Lookup Decision Trail'}
          </button>
        </form>

        {error && <div className="alert alert-error">{error}</div>}

        {entries.length > 0 && (
          <div style={{ marginTop: '2rem' }}>
            <h3 style={{ fontSize: '1.1rem', color: '#fff', marginBottom: '1rem' }}>
              Decision History ({entries.length} Events)
            </h3>
            <div style={{ display: 'grid', gap: '1rem' }}>
              {entries.map((entry, idx) => (
                <div key={idx} style={{
                  background: 'rgba(15, 23, 42, 0.6)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1.25rem',
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
                      <span className="badge badge-refined">Step {idx + 1}</span>
                      <strong style={{ fontSize: '1.05rem', color: '#fff' }}>Action: {entry.action}</strong>
                    </div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {new Date(entry.resolvedAt).toLocaleString()}
                    </span>
                  </div>

                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
                    Resolved by Identity: <code>{entry.resolvedBy}</code> | Evidence: {entry.inputReports.join(', ')}
                  </div>

                  {entry.stateBefore && (
                    <details style={{ marginTop: '0.75rem', fontSize: '0.8rem' }}>
                      <summary style={{ cursor: 'pointer', color: 'var(--accent-cyan)' }}>View State Before</summary>
                      <pre style={{ background: '#020617', padding: '0.75rem', borderRadius: '6px', overflowX: 'auto', marginTop: '0.35rem' }}>
                        {entry.stateBefore}
                      </pre>
                    </details>
                  )}

                  <details style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}>
                    <summary style={{ cursor: 'pointer', color: 'var(--accent-emerald)' }}>View State After</summary>
                    <pre style={{ background: '#020617', padding: '0.75rem', borderRadius: '6px', overflowX: 'auto', marginTop: '0.35rem' }}>
                      {entry.stateAfter}
                    </pre>
                  </details>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </>
  );
}
