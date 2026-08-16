export type ReportSource = 'web' | 'mobile';

export type ActionTaken = 'DUPLICATE' | 'REFINED' | 'CONFLICT' | 'NEW_ISSUE';

export interface Location {
  lat: number;
  lng: number;
}

export interface ReportRequest {
  reportId: string;
  userId: string;
  timestamp: string;
  location: Location;
  category: string;
  description: string;
  source: ReportSource;
  isDuplicate: boolean;
  isResolved: boolean;
  email?: string;
  deviceFingerprint?: string;
}

export interface ReportResponse {
  reportId: string;
  identityId: string;
  issueId: string;
  actionTaken: ActionTaken;
  resolvedBy: string;
  resolutionTimestamp: string;
  evidence: string[];
}

export interface ReportEntity {
  id: string;
  reportId: string;
  userId: string;
  identityId: string;
  timestamp: string;
  latitude: number;
  longitude: number;
  category: string;
  description: string;
  source: ReportSource;
  duplicate: boolean;
  resolved: boolean;
  issueId: string;
  createdAt: string;
}

export interface IssueSummary {
  issueId: string;
  category: string;
  latitude: number;
  longitude: number;
  latestVersion: number;
  actionTaken: ActionTaken;
  isResolved: boolean;
  reportCount: number;
  lastModified: string;
  resolvedBy: string;
}

export interface ResolutionSummary {
  issueId: string;
  version: number;
  actionTaken: ActionTaken;
  resolvedBy: string;
  lastModified: string;
  evidence: string[];
}

export interface AuditEntrySummary {
  issueId: string;
  action: string;
  inputReports: string[];
  resolvedAt: string;
  resolvedBy: string;
  stateBefore?: string;
  stateAfter: string;
}

