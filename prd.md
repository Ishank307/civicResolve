# Real-Time Civic Issue Resolution with Identity Resolution and Temporal Conflict Handling

# Real-Time Civic Issue Resolution with Identity Resolution and Temporal Conflict Handling

Title:
Real-Time Civic Issue Resolution with Identity Resolution and Temporal Conflict Handling

Background:
You are tasked with building a critical component of a civic issue resolution platform that must process real-time reports from citizens, officers, and administrators. The system receives reports from multiple sources, including mobile and web interfaces, and must resolve conflicting or overlapping reports efficiently. Each report includes location, timestamp, and category, but may be duplicated, delayed, or submitted by different identities representing the same user or issue. The system must ensure that duplicate reports do not trigger redundant alerts and that delayed reports do not invalidate earlier decisions. This problem is grounded in your experience building the Civic Issue Reporting Platform and your expertise in full-stack development, GUIs, and database management.

Problem Statement:
Develop a real-time civic issue resolution engine that ingests reports from multiple sources, resolves identity conflicts across users, and handles temporal inconsistencies (e.g., late or out-of-order reports). The system must determine whether a new report is a duplicate, a refinement, or a conflict with an existing report. It must maintain a consistent, auditable state and provide a deterministic resolution for each report. The final state must be replayable and idempotent under repeated processing.

Scope:
The system must process incoming civic issue reports, resolve conflicting or overlapping submissions, and maintain a unified resolution state. It must support multiple user roles and handle edge cases such as identity confusion, delayed reports, and conflicting resolutions. The system must be deterministic and auditable, with a clear decision trail for every report.

MVP Scope:
The MVP must implement the following core components:  
1. **Identity Resolution Engine**: Map reports to real users by resolving ambiguous identities (e.g., same email but different device IDs, or same location and time but different names).  
2. **Temporal Conflict Resolver**: Detect and reconcile overlapping or conflicting reports based on time, location, and category, handling late or out-of-order submissions.  
3. **Stateful Resolution Engine**: Maintain a persistent, versioned state for each issue, ensuring that updates and deletions do not break prior decisions.  

The system must support the following inputs:  
- Reports with fields: `reportId`, `userId`, `timestamp`, `location`, `category`, `description`, `source` (web/mobile), `isDuplicate`, `isResolved`.  
- Duplicate reports with different `reportId` but same `location`, `category`, and similar `description`.  
- Late reports with `timestamp` earlier than the last known resolution.  
- Reports from different identities (`userId`) but same `location`, `category`, and `timestamp`.  
- Resolved reports being updated with conflicting details.  

The system must output a single, consistent resolution for each issue, including:  
- `issueId` (generated from location, category, and time window),  
- `resolvedBy`,  
- `resolutionTimestamp`,  
- `actionTaken` (e.g., "Duplicate", "Refined", "Conflict", "New Issue"),  
- `evidence` (list of report IDs considered).

Advanced/Bonus Scope:
No additional advanced or bonus scope is required for the MVP.

Functional Requirements:
1. **Input Processing**: Accept POST /reports with JSON body containing:  
   ```json
   {
     "reportId": "string",
     "userId": "string",
     "timestamp": "ISO 8601 timestamp",
     "location": {"lat": float, "lng": float},
     "category": "string",
     "description": "string",
     "source": "string",
     "isDuplicate": boolean,
     "isResolved": boolean
   }
   ```  
   Return 201 on success, 400 on malformed input, 409 if conflict detected and resolution pending.  

2. **Identity Resolution**:  
   - Map `userId` to a canonical `identityId` using:  
     - Same email → same identity  
     - Same device fingerprint → same identity  
     - Same location + timestamp + category → same identity if within 10 minutes  
   - Output `identityId` in response.  

3. **Temporal Conflict Detection**:  
   - Detect if a report overlaps with existing reports in the same `location` and `category`  
   - If `timestamp` is earlier than last resolution, replay the resolution logic  
   - If `timestamp` is later, update state only if new evidence contradicts prior resolution  

4. **Stateful Resolution**:  
   - Maintain a `resolutions` table with:  
     - `issueId` (derived from `location`, `category`, and `timestamp` window),  
     - `version` (incremental),  
     - `resolution`,  
     - `evidence`,  
     - `lastModified`  
   - On update, check for conflict with prior version and resolve using tie-breaking rules  

5. **Conflict Resolution Logic**:  
   - If multiple reports in same `issueId` window:  
     - Prefer `source: mobile` over `source: web`  
     - Prefer `isResolved: true` over `false`  
     - Prefer latest `timestamp` if all else equal  
   - If no clear preference, return `Conflict` with list of conflicting reports  

6. **Audit Trail**:  
   - For every resolution, store:  
     - `action`,  
     - `inputReports`,  
     - `resolvedAt`,  
     - `resolvedBy`,  
     - `stateBefore`,  
     - `stateAfter`  

7. **Replayability**:  
   - Given a list of reports, the system must produce the same resolution state on replay

Non-Functional Requirements:
1. **Determinism**: Identical input → identical output (state and audit)  
2. **Idempotency**: Same report twice → same result  
3. **Auditability**: All decisions must be traceable to input reports  
4. **Performance**: Process 100 reports/sec on local machine  
5. **Memory**: ≤ 500MB RAM usage

Constraints:
1. Use only: HTML, CSS, JavaScript, React JS, TypeScript, Spring Boot, PostgreSQL, MongoDB  
2. No external APIs or services  
3. No ML/LLM models  
4. No distributed systems (K8s, Kafka, etc.)  
5. All logic must be implemented in Spring Boot and React

Deliverables:
1. Submission — Public GitHub repository URL (required)  
2. Repository contents — Backend (Spring Boot) and frontend (React) code; sample reports covering ≥5 edge cases; audit output for each decision; demo of replaying inputs  
3. Test Suite — Automated tests for identity resolution, temporal conflict, and replay  
4. Documentation — README with clone → setup → run → test instructions; where to find fixtures and audit outputs
