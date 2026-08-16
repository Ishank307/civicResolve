import { useState } from 'react';
import { replayReports } from '../api/reports';
import type { ReportRequest, ReportResponse } from '../types/report';

const sampleDataset: ReportRequest[] = [
  {
    reportId: 'rpt-batch-001',
    userId: 'citizen-alice',
    timestamp: '2026-08-16T10:00:00Z',
    location: { lat: 28.6139, lng: 77.2090 },
    category: 'pothole',
    description: 'Large pothole near main traffic crossing',
    source: 'web',
    isDuplicate: false,
    isResolved: false,
    email: 'alice@example.com'
  },
  {
    reportId: 'rpt-batch-002',
    userId: 'citizen-alice-phone',
    timestamp: '2026-08-16T10:02:00Z',
    location: { lat: 28.6139, lng: 77.2090 },
    category: 'pothole',
    description: 'Confirmed from mobile camera',
    source: 'mobile',
    isDuplicate: false,
    isResolved: false,
    email: 'alice@example.com'
  },
  {
    reportId: 'rpt-batch-003',
    userId: 'citizen-bob',
    timestamp: '2026-08-16T10:05:00Z',
    location: { lat: 28.6139, lng: 77.2090 },
    category: 'pothole',
    description: 'Duplicate report of same pothole',
    source: 'web',
    isDuplicate: true,
    isResolved: false
  },
  {
    reportId: 'rpt-batch-004-late',
    userId: 'patrol-officer-9',
    timestamp: '2026-08-16T09:50:00Z',
    location: { lat: 28.6139, lng: 77.2090 },
    category: 'pothole',
    description: 'Out-of-order early log',
    source: 'mobile',
    isDuplicate: false,
    isResolved: false
  },
  {
    reportId: 'rpt-batch-005-resolved',
    userId: 'road-crew-12',
    timestamp: '2026-08-16T10:15:00Z',
    location: { lat: 28.6139, lng: 77.2090 },
    category: 'pothole',
    description: 'Asphalt patched and cured',
    source: 'mobile',
    isDuplicate: false,
    isResolved: true
  }
];

export function ReplayPage() {
  const [jsonText, setJsonText] = useState(JSON.stringify(sampleDataset, null, 2));
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState<ReportResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleRunReplay() {
    setError(null);
    setLoading(true);
    try {
      const parsed = JSON.parse(jsonText);
      if (!Array.isArray(parsed)) {
        throw new Error('Input must be a JSON array of reports');
      }
      const data = await replayReports(parsed);
      setResults(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Replay failed');
      setResults(null);
    } finally {
      setLoading(false);
    }
  }

  function handleLoadPreset(preset: 'standard' | 'edgecases' | 'outOfOrder') {
    if (preset === 'standard') {
      setJsonText(JSON.stringify(sampleDataset, null, 2));
    } else if (preset === 'outOfOrder') {
      // Reversed order to prove chronological reordering & determinism
      const shuffled = [...sampleDataset].reverse();
      setJsonText(JSON.stringify(shuffled, null, 2));
    } else {
      setJsonText(JSON.stringify(sampleDataset.slice(0, 3), null, 2));
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Replay Playground</h1>
          <p className="page-description">
            Test and verify deterministic replayability across out-of-order batches and conflicting submissions.
          </p>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 className="card-title" style={{ margin: 0 }}>Input Batch (JSON)</h2>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn btn-preset" type="button" onClick={() => handleLoadPreset('standard')}>
              Load 5 Edge Cases
            </button>
            <button className="btn btn-preset" type="button" onClick={() => handleLoadPreset('outOfOrder')}>
              Reverse Order (Test Determinism)
            </button>
          </div>
        </div>

        <textarea
          style={{
            width: '100%',
            height: '240px',
            fontFamily: 'monospace',
            fontSize: '0.85rem',
            background: 'rgba(15, 23, 42, 0.7)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-sm)',
            color: '#e2e8f0',
            padding: '1rem',
          }}
          value={jsonText}
          onChange={(e) => setJsonText(e.target.value)}
        />

        <div style={{ marginTop: '1.25rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button className="btn" type="button" disabled={loading} onClick={handleRunReplay}>
            {loading ? 'Replaying...' : '▶ Run Deterministic Replay'}
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {results && (
          <div style={{ marginTop: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <h3 style={{ margin: 0, color: '#fff', fontSize: '1.1rem' }}>Replay Execution Results ({results.length})</h3>
              <span className="badge badge-refined">Idempotent & Deterministic</span>
            </div>

            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Report ID</th>
                    <th>Issue ID</th>
                    <th>Action Taken</th>
                    <th>Resolved By (Identity)</th>
                    <th>Evidence Count</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((res) => (
                    <tr key={res.reportId}>
                      <td><code>{res.reportId}</code></td>
                      <td><code>{res.issueId}</code></td>
                      <td>
                        <span className={`badge ${
                          res.actionTaken === 'NEW_ISSUE' ? 'badge-new' :
                          res.actionTaken === 'REFINED' ? 'badge-refined' :
                          res.actionTaken === 'DUPLICATE' ? 'badge-duplicate' : 'badge-conflict'
                        }`}>
                          {res.actionTaken}
                        </span>
                      </td>
                      <td><code>{res.resolvedBy.slice(0, 8)}...</code></td>
                      <td>{res.evidence.length} reports</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
