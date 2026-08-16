import { useState, type FormEvent } from 'react';
import { submitReport } from '../api/reports';
import type { ReportRequest, ReportResponse, ReportSource } from '../types/report';

const emptyForm = (): ReportRequest => ({
  reportId: 'rpt-' + crypto.randomUUID().slice(0, 8),
  userId: '',
  timestamp: new Date().toISOString(),
  location: { lat: 28.6139, lng: 77.2090 },
  category: 'pothole',
  description: '',
  source: 'mobile',
  isDuplicate: false,
  isResolved: false,
  email: '',
  deviceFingerprint: '',
});

export function ReportForm() {
  const [form, setForm] = useState<ReportRequest>(emptyForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ReportResponse | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await submitReport(form);
      setResult(response);
      setForm(emptyForm());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Submission failed');
    } finally {
      setLoading(false);
    }
  }

  function applyPreset(presetType: 'new' | 'alias' | 'dup' | 'late' | 'resolve') {
    const baseId = 'rpt-' + Math.floor(Math.random() * 9000 + 1000);
    if (presetType === 'new') {
      setForm({
        reportId: baseId,
        userId: 'citizen-alice',
        timestamp: new Date().toISOString(),
        location: { lat: 28.6139, lng: 77.2090 },
        category: 'pothole',
        description: 'New pothole reported on Central Avenue',
        source: 'web',
        isDuplicate: false,
        isResolved: false,
        email: 'alice@city.gov',
        deviceFingerprint: '',
      });
    } else if (presetType === 'alias') {
      setForm({
        reportId: baseId,
        userId: 'alice-mobile-tablet',
        timestamp: new Date().toISOString(),
        location: { lat: 28.6139, lng: 77.2090 },
        category: 'pothole',
        description: 'Confirmed from tablet device',
        source: 'mobile',
        isDuplicate: false,
        isResolved: false,
        email: 'alice@city.gov',
        deviceFingerprint: 'device-xyz-987',
      });
    } else if (presetType === 'dup') {
      setForm({
        reportId: baseId,
        userId: 'citizen-bob',
        timestamp: new Date().toISOString(),
        location: { lat: 28.6139, lng: 77.2090 },
        category: 'pothole',
        description: 'Duplicate report of same pothole',
        source: 'web',
        isDuplicate: true,
        isResolved: false,
        email: '',
        deviceFingerprint: '',
      });
    } else if (presetType === 'late') {
      // 15 minutes in the past
      const past = new Date(Date.now() - 15 * 60 * 1000).toISOString();
      setForm({
        reportId: baseId,
        userId: 'inspector-clark',
        timestamp: past,
        location: { lat: 28.6139, lng: 77.2090 },
        category: 'pothole',
        description: 'Earlier inspection report synced late',
        source: 'mobile',
        isDuplicate: false,
        isResolved: false,
        email: 'clark@inspector.gov',
        deviceFingerprint: '',
      });
    } else if (presetType === 'resolve') {
      setForm({
        reportId: baseId,
        userId: 'crew-lead-99',
        timestamp: new Date().toISOString(),
        location: { lat: 28.6139, lng: 77.2090 },
        category: 'pothole',
        description: 'Repairs completed and verified by municipal crew',
        source: 'mobile',
        isDuplicate: false,
        isResolved: true,
        email: 'crew@publicworks.gov',
        deviceFingerprint: '',
      });
    }
  }

  return (
    <div className="card">
      <div style={{ marginBottom: '1.25rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, marginRight: '0.25rem' }}>
          Quick Edge-Case Presets:
        </span>
        <button type="button" className="btn btn-preset" onClick={() => applyPreset('new')}>
          1. New Issue
        </button>
        <button type="button" className="btn btn-preset" onClick={() => applyPreset('alias')}>
          2. Identity Alias (Same Email)
        </button>
        <button type="button" className="btn btn-preset" onClick={() => applyPreset('dup')}>
          3. Duplicate Flag
        </button>
        <button type="button" className="btn btn-preset" onClick={() => applyPreset('late')}>
          4. Late Arrival Report
        </button>
        <button type="button" className="btn btn-preset" onClick={() => applyPreset('resolve')}>
          5. Verified Resolution (Mobile)
        </button>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <div className="form-row">
          <label>
            Report ID
            <input
              required
              value={form.reportId}
              onChange={(e) => setForm({ ...form, reportId: e.target.value })}
            />
          </label>
          <label>
            User ID
            <input
              required
              placeholder="e.g. user-123"
              value={form.userId}
              onChange={(e) => setForm({ ...form, userId: e.target.value })}
            />
          </label>
        </div>

        <div className="form-row">
          <label>
            Email (for Identity Resolution)
            <input
              type="email"
              placeholder="e.g. user@example.com"
              value={form.email || ''}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </label>
          <label>
            Device Fingerprint
            <input
              placeholder="e.g. dev-abc-987"
              value={form.deviceFingerprint || ''}
              onChange={(e) => setForm({ ...form, deviceFingerprint: e.target.value })}
            />
          </label>
        </div>

        <div className="form-row">
          <label>
            Category
            <input
              required
              placeholder="pothole, streetlight, drainage"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
            />
          </label>
          <label>
            Source
            <select
              value={form.source}
              onChange={(e) => setForm({ ...form, source: e.target.value as ReportSource })}
            >
              <option value="mobile">Mobile (High Priority)</option>
              <option value="web">Web (Standard)</option>
            </select>
          </label>
        </div>

        <label>
          Description
          <textarea
            required
            rows={2}
            placeholder="Detailed description of civic issue..."
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </label>

        <div className="form-row">
          <label>
            Latitude
            <input
              type="number"
              step="any"
              required
              value={form.location.lat}
              onChange={(e) =>
                setForm({ ...form, location: { ...form.location, lat: +e.target.value } })
              }
            />
          </label>
          <label>
            Longitude
            <input
              type="number"
              step="any"
              required
              value={form.location.lng}
              onChange={(e) =>
                setForm({ ...form, location: { ...form.location, lng: +e.target.value } })
              }
            />
          </label>
        </div>

        <div className="form-row">
          <label>
            Timestamp (UTC)
            <input
              type="datetime-local"
              required
              value={form.timestamp.slice(0, 16)}
              onChange={(e) =>
                setForm({ ...form, timestamp: new Date(e.target.value).toISOString() })
              }
            />
          </label>
          <div className="checkbox-row" style={{ marginTop: '1.5rem' }}>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={form.isDuplicate}
                onChange={(e) => setForm({ ...form, isDuplicate: e.target.checked })}
              />
              Mark as duplicate
            </label>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={form.isResolved}
                onChange={(e) => setForm({ ...form, isResolved: e.target.checked })}
              />
              Mark as resolved
            </label>
          </div>
        </div>

        <button className="btn" type="submit" disabled={loading} style={{ marginTop: '0.5rem' }}>
          {loading ? 'Submitting to Resolution Engine…' : '⚡ Ingest & Process Report'}
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      {result && (
        <div className="alert alert-success" style={{ display: 'block' }}>
          <div style={{ fontWeight: 700, marginBottom: '0.25rem' }}>
            Report Successfully Processed: Action <code>{result.actionTaken}</code>
          </div>
          <div style={{ fontSize: '0.85rem' }}>
            Issue ID: <strong>{result.issueId}</strong> | Identity ID: <code>{result.identityId}</code> | Evidence count: {result.evidence.length}
          </div>
        </div>
      )}
    </div>
  );
}
