import { ReportForm } from '../components/ReportForm';

export function SubmitReportPage() {
  return (
    <>
      <h1 className="page-title">Submit Report</h1>
      <p className="page-description">
        Ingest a civic issue report. The backend resolves identity, detects conflicts, and
        returns a deterministic action.
      </p>
      <ReportForm />
    </>
  );
}
