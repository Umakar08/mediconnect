import { useEffect, useState } from 'react';
import './patient-care.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function apiRequest(token, path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || data.error || `Request failed (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

function CarePageHeading({ eyebrow, title, description }) {
  return <section className="care-heading"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="page-subtitle">{description}</p></div></section>;
}

function Feedback({ error, notice }) {
  if (error) return <p className="care-feedback error" role="alert">{error}</p>;
  if (notice) return <p className="care-feedback success" role="status">{notice}</p>;
  return null;
}

function PatientCare({ token, role, section }) {
  const isStaff = role === 'STAFF' || role === 'ADMIN';
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [results, setResults] = useState([]);
  const [reports, setReports] = useState([]);
  const [patients, setPatients] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [admissions, setAdmissions] = useState([]);
  const [beds, setBeds] = useState([]);
  const [bedSelections, setBedSelections] = useState({});

  async function loadReports() {
    setLoading(true);
    setError('');
    try {
      if (isStaff) {
        const patientRows = await apiRequest(token, '/staff/patients');
        setPatients(patientRows);
        const patientId = selectedPatientId || (patientRows[0] ? String(patientRows[0].id) : '');
        if (patientId && patientRows.some((patient) => String(patient.id) === patientId)) {
          setSelectedPatientId(patientId);
          const [labResults, uploadedReports] = await Promise.all([
            apiRequest(token, `/staff/patients/${patientId}/lab-results`),
            apiRequest(token, `/staff/patients/${patientId}/lab-reports`),
          ]);
          setResults(labResults);
          setReports(uploadedReports);
        } else {
          setResults([]);
          setReports([]);
        }
      } else {
        const [labResults, uploadedReports] = await Promise.all([
          apiRequest(token, '/patient/lab-results'),
          apiRequest(token, '/patient/lab-reports'),
        ]);
        setResults(labResults);
        setReports(uploadedReports);
      }
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadAdmissions() {
    setLoading(true);
    setError('');
    try {
      if (isStaff) {
        const [admissionRows, bedRows] = await Promise.all([
          apiRequest(token, '/staff/admissions'),
          apiRequest(token, '/staff/beds'),
        ]);
        setAdmissions(admissionRows);
        setBeds(bedRows);
      } else {
        setAdmissions(await apiRequest(token, '/patient/admissions'));
      }
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    setNotice('');
    if (section === 'reports') void loadReports();
    if (section === 'admissions') void loadAdmissions();
  }, [section, token, role, selectedPatientId]);

  async function handleUpload(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const file = form.elements.report.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    setError('');
    setNotice('');
    try {
      await apiRequest(token, '/patient/lab-reports', { method: 'POST', body: formData });
      form.reset();
      setNotice('Your report has been uploaded to your patient record.');
      await loadReports();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleResultEntry(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form).entries());
    setError('');
    setNotice('');
    try {
      await apiRequest(token, `/staff/patients/${selectedPatientId}/lab-results`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values),
      });
      form.reset();
      setNotice('The result was added to the selected patient record.');
      await loadReports();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function downloadReport(reportId, filename) {
    setError('');
    try {
      const path = isStaff
        ? `/staff/lab-reports/${reportId}/download`
        : `/patient/lab-reports/${reportId}/download`;
      const response = await fetch(`${API_URL}${path}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || `Download failed (${response.status})`);
      }
      const url = URL.createObjectURL(await response.blob());
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.append(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function submitAdmission(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form).entries());
    setError('');
    setNotice('');
    try {
      await apiRequest(token, '/patient/admissions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values),
      });
      form.reset();
      setNotice('Your admission request was sent to the care team for review.');
      await loadAdmissions();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function addBed(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form).entries());
    setError('');
    setNotice('');
    try {
      await apiRequest(token, '/staff/beds', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values),
      });
      form.reset();
      setNotice('The bed is now in the allocation inventory.');
      await loadAdmissions();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function updateAdmission(admissionId, action, body) {
    setError('');
    setNotice('');
    try {
      await apiRequest(token, `/staff/admissions/${admissionId}/${action}`, {
        method: 'POST',
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined,
      });
      setNotice(action === 'assign' ? 'Bed assigned and marked occupied.' : action === 'reject' ? 'Admission request declined.' : 'Admission marked discharged; bed released.');
      await loadAdmissions();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function createStaff(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form).entries());
    setError('');
    setNotice('');
    try {
      await apiRequest(token, '/admin/staff', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(values),
      });
      form.reset();
      setNotice('Staff account created. Share its sign-in details securely.');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  if (section === 'reports') {
    return <div className="page-content care-page">
      <CarePageHeading eyebrow={isStaff ? 'Clinical workspace' : 'Your health records'} title={isStaff ? 'Blood test reports' : 'My blood test reports'} description={isStaff ? 'Review patient-submitted files and add verified results to the patient record.' : 'View results entered by your care team and upload a copy of a report.'} />
      <Feedback error={error} notice={notice} />
      {isStaff && <section className="care-panel">
        <h2>Patient record</h2>
        <label className="care-label">Choose a patient
          <select value={selectedPatientId} onChange={(event) => setSelectedPatientId(event.target.value)}>
            <option value="">Select a patient</option>
            {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.name} · {patient.email}</option>)}
          </select>
        </label>
        {!loading && patients.length === 0 && <p className="care-muted">No patient accounts have registered yet.</p>}
        <form className="care-form result-entry" onSubmit={handleResultEntry}>
          <h3>Enter a test result</h3>
          <div className="care-form-grid">
            <label className="care-label">Test name<input name="testName" maxLength="120" placeholder="Complete blood count" required /></label>
            <label className="care-label">Result<input name="resultValue" maxLength="120" placeholder="e.g. 5.2" required /></label>
            <label className="care-label">Unit<input name="unit" maxLength="40" placeholder="e.g. mmol/L" /></label>
            <label className="care-label">Reference range<input name="referenceRange" maxLength="120" placeholder="e.g. 3.9–5.5" /></label>
            <label className="care-label">Test date<input name="testedOn" type="date" max={new Date().toISOString().slice(0, 10)} required /></label>
            <label className="care-label care-field-wide">Notes<input name="notes" maxLength="1000" placeholder="Optional context from the care team" /></label>
          </div>
          <button className="primary-button" type="submit" disabled={!selectedPatientId}>Save test result</button>
        </form>
        {role === 'ADMIN' && <form className="care-form staff-entry" onSubmit={createStaff}>
          <h3>Create staff account</h3>
          <div className="care-form-grid">
            <label className="care-label">Full name<input name="name" maxLength="100" required /></label>
            <label className="care-label">Email<input name="email" type="email" maxLength="255" required /></label>
            <label className="care-label care-field-wide">Temporary password<input name="password" type="password" minLength="12" maxLength="72" autoComplete="new-password" required /></label>
          </div>
          <button className="outline-button" type="submit">Create staff account</button>
        </form>}
      </section>}
      {!isStaff && <form className="care-panel care-upload" onSubmit={handleUpload}>
        <div><h2>Upload a report</h2><p>PDF, PNG, or JPEG · up to 10 MB. Only you and authorized care staff can access it.</p></div>
        <label className="care-label">Report file<input name="report" type="file" accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg" required /></label>
        <button className="primary-button" type="submit">Upload report</button>
      </form>}
      {loading ? <div className="care-panel care-muted">Loading patient records…</div> : <>
        <section className="care-panel">
          <div className="care-panel-heading"><div><p className="eyebrow">Structured values</p><h2>Test results</h2></div><span>{results.length}</span></div>
          {results.length ? <div className="care-table-wrap"><table className="care-table"><thead><tr><th>Test</th><th>Result</th><th>Reference range</th><th>Date</th>{isStaff && <th>Entered by</th>}</tr></thead><tbody>{results.map((result) => <tr key={result.id}><td><strong>{result.testName}</strong>{result.notes && <small>{result.notes}</small>}</td><td>{result.resultValue} {result.unit}</td><td>{result.referenceRange || '—'}</td><td>{result.testedOn}</td>{isStaff && <td>{result.enteredBy}</td>}</tr>)}</tbody></table></div> : <p className="care-muted">No blood test results have been added{isStaff ? ' for this patient' : ' yet'}.</p>}
        </section>
        <section className="care-panel">
          <div className="care-panel-heading"><div><p className="eyebrow">Uploaded files</p><h2>Report documents</h2></div><span>{reports.length}</span></div>
          {reports.length ? <div className="care-report-list">{reports.map((report) => <article className="care-report" key={report.id}><span className="care-file-icon">PDF</span><div><strong>{report.originalFilename}</strong><small>{isStaff ? `${report.patientName} · ` : ''}Uploaded by {report.uploadedBy} · {new Date(report.uploadedAt).toLocaleDateString()}</small></div><button className="outline-button" type="button" onClick={() => downloadReport(report.id, report.originalFilename)}>Download</button></article>)}</div> : <p className="care-muted">No uploaded report files{isStaff ? ' for this patient' : ' yet'}.</p>}
        </section>
      </>}
      <p className="care-disclaimer">Results and uploaded files are stored as patient records. They do not replace professional interpretation or medical advice.</p>
    </div>;
  }

  if (section === 'admissions') {
    const availableBeds = beds.filter((bed) => bed.status === 'AVAILABLE');
    return <div className="page-content care-page">
      <CarePageHeading eyebrow={isStaff ? 'Clinical workspace' : 'Hospital care'} title={isStaff ? 'Admissions & beds' : 'Admission requests'} description={isStaff ? 'Review requests, manage the bed inventory, and track current admissions.' : 'Request an admission for review by the hospital care team.'} />
      <Feedback error={error} notice={notice} />
      {isStaff && <section className="care-panel">
        <div className="care-panel-heading"><div><p className="eyebrow">Bed inventory</p><h2>Available beds</h2></div><span>{availableBeds.length} available / {beds.length} total</span></div>
        <form className="care-form bed-entry" onSubmit={addBed}>
          <h3>Add a bed to the inventory</h3>
          <div className="care-form-grid">
            <label className="care-label">Bed code<input name="bedCode" maxLength="60" placeholder="e.g. W1-04" required /></label>
            <label className="care-label">Ward<input name="ward" maxLength="100" placeholder="e.g. Medical ward" required /></label>
            <label className="care-label">Bed type<input name="bedType" maxLength="60" placeholder="e.g. Standard" required /></label>
          </div>
          <button className="outline-button" type="submit">Add bed</button>
        </form>
        <div className="care-bed-list">{beds.map((bed) => <span className={`care-bed ${bed.status.toLowerCase()}`} key={bed.id}><strong>{bed.bedCode}</strong> · {bed.ward} · {bed.bedType} <small>{bed.status}</small></span>)}</div>
      </section>}
      {!isStaff && <form className="care-panel care-form" onSubmit={submitAdmission}>
        <h2>Request an admission</h2>
        <p className="care-muted">Submitting a request does not confirm an admission or reserve a bed. The care team will review it.</p>
        <div className="care-form-grid">
          <label className="care-label">Preferred ward<input name="requestedWard" maxLength="100" placeholder="e.g. Medical ward" required /></label>
          <label className="care-label care-field-wide">Reason for request<textarea name="reason" maxLength="1000" rows="3" placeholder="Share brief information for the admissions team" /></label>
        </div>
        <button className="primary-button" type="submit">Send admission request</button>
      </form>}
      {loading ? <div className="care-panel care-muted">Loading admissions…</div> : <section className="care-panel">
        <div className="care-panel-heading"><div><p className="eyebrow">{isStaff ? 'Request queue' : 'Your requests'}</p><h2>{isStaff ? 'Admission requests' : 'Admission history'}</h2></div><span>{admissions.length}</span></div>
        {admissions.length ? <div className="care-admission-list">{admissions.map((admission) => <article className="care-admission" key={admission.id}>
          <div className="care-admission-top"><div><strong>{isStaff ? admission.patientName : `Request for ${admission.requestedWard}`}</strong><small>{isStaff ? admission.patientEmail : `Preferred ward: ${admission.requestedWard}`} · {new Date(admission.requestedAt).toLocaleDateString()}</small></div><span className={`care-status ${admission.status.toLowerCase()}`}>{admission.status}</span></div>
          {admission.reason && <p>{admission.reason}</p>}
          {admission.bed && <p className="care-assigned-bed">Assigned: {admission.bed.bedCode} · {admission.bed.ward} · {admission.bed.bedType}</p>}
          {isStaff && admission.reviewedBy && <p className="care-muted">Last action by {admission.reviewedBy}{admission.reviewedAt ? ` · ${new Date(admission.reviewedAt).toLocaleString()}` : ''}</p>}
          {isStaff && admission.status === 'PENDING' && <div className="care-actions">
            <label className="care-label">Available bed<select value={bedSelections[admission.id] || ''} onChange={(event) => setBedSelections((current) => ({ ...current, [admission.id]: event.target.value }))}><option value="">Select a bed</option>{availableBeds.map((bed) => <option key={bed.id} value={bed.id}>{bed.bedCode} · {bed.ward} · {bed.bedType}</option>)}</select></label>
            <button className="primary-button" type="button" disabled={!bedSelections[admission.id]} onClick={() => updateAdmission(admission.id, 'assign', { bedId: Number(bedSelections[admission.id]) })}>Approve & assign bed</button>
            <button className="outline-button" type="button" onClick={() => updateAdmission(admission.id, 'reject')}>Decline</button>
          </div>}
          {isStaff && admission.status === 'ADMITTED' && <button className="outline-button care-discharge" type="button" onClick={() => updateAdmission(admission.id, 'discharge')}>Discharge & release bed</button>}
        </article>)}</div> : <p className="care-muted">No admission requests{isStaff ? ' have been received' : ' yet'}.</p>}
      </section>}
      <p className="care-disclaimer">A request is not a clinical assessment or a confirmed admission. Contact emergency services for urgent medical emergencies.</p>
    </div>;
  }

  return null;
}

export default PatientCare;
