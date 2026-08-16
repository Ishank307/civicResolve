import { apiFetch } from './client';
import type {
  AuditEntrySummary,
  IssueSummary,
  ReportEntity,
  ReportRequest,
  ReportResponse,
  ResolutionSummary,
} from '../types/report';

export function submitReport(report: ReportRequest): Promise<ReportResponse> {
  return apiFetch<ReportResponse>('/reports', {
    method: 'POST',
    body: JSON.stringify(report),
  });
}

export function replayReports(reports: ReportRequest[]): Promise<ReportResponse[]> {
  return apiFetch<ReportResponse[]>('/replay', {
    method: 'POST',
    body: JSON.stringify({ reports }),
  });
}

export function getIssues(): Promise<IssueSummary[]> {
  return apiFetch<IssueSummary[]>('/issues');
}

export function getRecentReports(): Promise<ReportEntity[]> {
  return apiFetch<ReportEntity[]>('/reports');
}

export function getResolutions(issueId: string): Promise<ResolutionSummary[]> {
  return apiFetch<ResolutionSummary[]>(`/resolutions/${issueId}`);
}

export function getAuditTrail(issueId: string): Promise<AuditEntrySummary[]> {
  return apiFetch<AuditEntrySummary[]>(`/audit/${issueId}`);
}

