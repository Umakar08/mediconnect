import { useMemo, useState } from 'react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const doctors = [
  { id: 1, name: 'Rohith', specialty: 'Orthopedics', credentials: 'Unverified profile', tagline: 'Bones, joints & movement', color: 'rose', initials: 'R', next: 'Today, 10:30 AM' },
  { id: 2, name: 'Umakar', specialty: 'Cardiology', credentials: 'Unverified profile', tagline: 'Heart & vascular care', color: 'blue', initials: 'U', next: 'Today, 11:15 AM' },
  { id: 3, name: 'Mrudhvan', specialty: 'Dermatology', credentials: 'Unverified profile', tagline: 'Skin, hair & nails', color: 'lavender', initials: 'M', next: 'Today, 1:00 PM' },
  { id: 4, name: 'Vishnu', specialty: 'Neurology', credentials: 'Unverified profile', tagline: 'Brain & nervous system', color: 'peach', initials: 'V', next: 'Tomorrow, 9:30 AM' },
  { id: 5, name: 'Vinay', specialty: 'Mentalist', credentials: 'Entertainment profile', tagline: 'Mind reading & entertainment · not medical care', color: 'mint', initials: 'V', next: 'Today, 2:30 PM' },
];

const specialties = [
  { name: 'Orthopedics', icon: '⌁', detail: 'Bones, joints & movement' },
  { name: 'Cardiology', icon: '♡', detail: 'Heart & vascular care' },
  { name: 'Dermatology', icon: '✳', detail: 'Skin, hair & nails' },
  { name: 'Neurology', icon: '⌘', detail: 'Brain & nervous system' },
  { name: 'Mentalist', icon: '✦', detail: 'Entertainment only · not medical care' },
];

function appointmentMinutes(time) {
  const [clock, period] = time.split(' ');
  const [hour, minute] = clock.split(':').map(Number);
  return (hour % 12 + (period === 'PM' ? 12 : 0)) * 60 + minute;
}

function Icon({ name, size = 18 }) {
  const paths = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M16 3v4M8 3v4M3 10h18" /></>,
    search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    pin: <><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></>,
    close: <><path d="m18 6-12 12M6 6l12 12" /></>,
    check: <path d="m5 12 4 4L19 6" />,
    star: <path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-3-5.6 3 1.1-6.2L3 9.6l6.2-.9L12 3Z" />,
    video: <><rect x="3" y="6" width="13" height="12" rx="2" /><path d="m16 10 5-3v10l-5-3" /></>,
  };

  return <svg aria-hidden="true" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>;
}

function DoctorAvatar({ doctor, large = false }) {
  return <div className={`doctor-avatar ${doctor.color}${large ? ' large' : ''}`} aria-label={doctor.name}>{doctor.initials}</div>;
}

function App() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const [section, setSection] = useState('overview');
  const [search, setSearch] = useState('');
  const [specialty, setSpecialty] = useState('All specialties');
  const [bookingDoctor, setBookingDoctor] = useState(null);
  const [bookingDate, setBookingDate] = useState('');
  const [bookingTime, setBookingTime] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [bookingNotice, setBookingNotice] = useState('');
  const isRegistering = mode === 'register';

  const filteredDoctors = useMemo(() => doctors.filter((doctor) => {
    const matchesSearch = `${doctor.name} ${doctor.specialty}`.toLowerCase().includes(search.trim().toLowerCase());
    const matchesSpecialty = specialty === 'All specialties' || doctor.specialty === specialty;
    return matchesSearch && matchesSpecialty;
  }), [search, specialty]);

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });
    try {
      const response = await fetch(`${API_URL}/auth/${isRegistering ? 'register' : 'login'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(isRegistering ? form : { email: form.email, password: form.password }),
      });
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        const message = data.message
          || (response.status === 401 ? 'Invalid email or password.' : data.error)
          || 'Something went wrong. Please try again.';
        throw new Error(message);
      }
      if (isRegistering) {
        setStatus({ type: 'success', message: 'Your account is ready. Sign in to continue.' });
        setMode('login');
      } else {
        setUser({ name: data.name || form.email.split('@')[0], email: data.email || form.email });
        setSection('overview');
      }
    } catch (error) {
      setStatus({ type: 'error', message: error.message.includes('Failed to fetch') ? 'Could not reach the server. Make sure the backend is running.' : error.message });
    } finally {
      setLoading(false);
    }
  }

  function openBooking(doctor) {
    setBookingDoctor(doctor);
    setBookingDate('');
    setBookingTime('');
  }

  function confirmBooking(event) {
    event.preventDefault();
    const appointment = {
      id: Date.now(),
      doctor: bookingDoctor,
      date: bookingDate,
      time: bookingTime,
    };
    setAppointments((existing) => [...existing, appointment].sort((a, b) => a.date.localeCompare(b.date) || appointmentMinutes(a.time) - appointmentMinutes(b.time)));
    setBookingDoctor(null);
    setSection('appointments');
    setBookingNotice(`Saved ${appointment.doctor.name}'s illustrative time in this browser only. No appointment has been booked.`);
    window.setTimeout(() => setBookingNotice(''), 5000);
  }

  function signOut() {
    setUser(null);
    setAppointments([]);
    setSearch('');
    setSpecialty('All specialties');
    setSection('overview');
    setStatus({ type: '', message: '' });
  }

  function enterPreview() {
    setUser({ name: 'Jordan Lee', email: 'preview@mediconnect.example', previewAccess: true });
    setSection('overview');
  }

  if (!user) {
    return (
      <main className="auth-shell">
        <section className="auth-layout">
          <div className="brand-panel">
            <a className="brand-lockup" href="#" aria-label="MediConnect home"><span className="brand-mark">m</span><span>MediConnect</span></a>
            <div className="brand-copy">
              <p className="eyebrow">Care, made personal</p>
              <h1>Better health starts with feeling heard.</h1>
              <p className="intro">Find your people, book the care you need, and feel at home in your health journey.</p>
            </div>
            <div className="brand-footer"><span className="signal-dot" /> Thoughtful care, right when you need it</div>
            <div className="brand-orbit orbit-one" /><div className="brand-orbit orbit-two" />
          </div>
          <div className="auth-panel">
            <div className="auth-heading">
              <p className="eyebrow">Your care, all in one place</p>
              <h2>{isRegistering ? 'Create your account' : 'Welcome back'}</h2>
              <p>{isRegistering ? 'A healthier you is just around the corner.' : 'Sign in to pick up where you left off.'}</p>
            </div>
            <div className="tabs" role="tablist" aria-label="Account access">
              <button className={mode === 'login' ? 'active' : ''} onClick={() => { setMode('login'); setStatus({ type: '', message: '' }); }} type="button" role="tab" aria-selected={mode === 'login'}>Sign in</button>
              <button className={mode === 'register' ? 'active' : ''} onClick={() => { setMode('register'); setStatus({ type: '', message: '' }); }} type="button" role="tab" aria-selected={mode === 'register'}>Create account</button>
            </div>
            <form onSubmit={submit}>
              {isRegistering && <label>Full name<input name="name" value={form.name} onChange={updateField} placeholder="Your name" autoComplete="name" required /></label>}
              <label>Email address<input name="email" type="email" value={form.email} onChange={updateField} placeholder="you@example.com" autoComplete="email" required /></label>
              <label>Password<input name="password" type="password" value={form.password} onChange={updateField} placeholder="At least 8 characters" autoComplete={isRegistering ? 'new-password' : 'current-password'} minLength={isRegistering ? 8 : undefined} required /></label>
              {status.message && <p className={`status ${status.type}`} role="status">{status.message}</p>}
              <button className="submit-button" disabled={loading} type="submit">{loading ? 'Please wait...' : isRegistering ? 'Create account' : 'Sign in'} <Icon name="arrow" size={18} /></button>
            </form>
            {!isRegistering && <button className="preview-access-button" type="button" onClick={enterPreview}>Explore the care directory <Icon name="arrow" size={16} /></button>}
            <p className="fine-print">Your information is protected and only used to support your care.</p>
          </div>
        </section>
      </main>
    );
  }

  function setSpecialtyAndBrowse(name) {
    setSpecialty(name);
    setSection('doctors');
  }

  return (
    <div className="dashboard-shell">
      <aside className="sidebar">
        <a className="brand-lockup sidebar-brand" href="#" onClick={(event) => { event.preventDefault(); setSection('overview'); }}><span className="brand-mark">m</span><span>MediConnect</span></a>
        <div className="side-caption">MENU</div>
        <nav className="side-nav" aria-label="Main navigation">
          <button className={section === 'overview' ? 'active' : ''} onClick={() => setSection('overview')}><Icon name="grid" /> Overview</button>
          <button className={section === 'doctors' ? 'active' : ''} onClick={() => { setSpecialty('All specialties'); setSection('doctors'); }}><span className="nav-symbol">✚</span> Find a doctor</button>
          <button className={section === 'appointments' ? 'active' : ''} onClick={() => setSection('appointments')}><Icon name="calendar" /> Appointments{appointments.length > 0 && <span className="nav-count">{appointments.length}</span>}</button>
        </nav>
        <div className="sidebar-help">
          <div className="help-icon">✦</div>
          <h3>Need a little help?</h3>
          <p>Our care team is here for you, every step of the way.</p>
          <a href="mailto:care@mediconnect.example">Contact care team <Icon name="arrow" size={15} /></a>
        </div>
        <button className="profile-button" onClick={signOut}>
          <span className="profile-avatar">{user.name.charAt(0).toUpperCase()}</span>
          <span className="profile-copy"><strong>{user.name}</strong><small>{user.previewAccess ? 'Preview access' : 'Patient account'}</small></span>
          <span className="profile-menu" aria-hidden="true">···</span>
        </button>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><span>/</span><strong>{section === 'overview' ? 'Overview' : section === 'doctors' ? 'Find a doctor' : 'Appointments'}</strong></div>
          <div className="topbar-actions">
            <label className="search-box"><Icon name="search" size={18} /><input aria-label="Search doctors" value={search} onChange={(event) => { setSearch(event.target.value); if (event.target.value) setSection('doctors'); }} placeholder="Search doctors..." /><kbd>⌘ K</kbd></label>
            <button className="icon-button notification-button" aria-label="Notifications"><Icon name="bell" size={19} /><span /></button>
            <span className="topbar-avatar">{user.name.charAt(0).toUpperCase()}</span>
          </div>
        </header>

        {section === 'overview' && (
          <div className="page-content">
            <section className="welcome-row">
              <div><p className="eyebrow">Your health, your way</p><h1>Good morning, {user.name.split(' ')[0]} <span className="wave">✳</span></h1><p className="page-subtitle">A little care goes a long way. Here's your health at a glance.</p></div>
              <button className="primary-button" onClick={() => { setSection('doctors'); setSpecialty('All specialties'); }}><Icon name="plus" size={17} /> Book an appointment</button>
            </section>

            <section className="welcome-banner">
              <div className="banner-copy"><span className="banner-tag"><span /> HERE FOR YOU</span><h2>Care that fits<br />your life.</h2><p>From a quick check-in to finding a new specialist, your next step starts here.</p><button onClick={() => { setSection('doctors'); setSpecialty('All specialties'); }}>Find your doctor <Icon name="arrow" size={17} /></button></div>
              <div className="banner-graphic" aria-hidden="true"><div className="banner-sun" /><div className="banner-arch" /><div className="banner-shape shape-one" /><div className="banner-shape shape-two" /><div className="banner-plant"><i /><i /><i /></div><span className="banner-sparkle sparkle-one">✳</span><span className="banner-sparkle sparkle-two">✳</span></div>
            </section>

            <section className="stat-grid" aria-label="Care overview">
              <article className="stat-card"><span className="stat-icon stat-icon-teal"><Icon name="calendar" /></span><p>Upcoming visits</p><strong>{appointments.length}</strong><small>{appointments.length ? 'You’re all set for your next visit' : 'Ready when you are'}</small></article>
              <article className="stat-card"><span className="stat-icon stat-icon-lilac">✚</span><p>Departments</p><strong>{specialties.length}</strong><small>Profile categories</small></article>
              <article className="stat-card"><span className="stat-icon stat-icon-peach">✦</span><p>Care profiles</p><strong>{doctors.length}</strong><small>Browse provider profiles</small></article>
            </section>

            <section className="section-block">
              <div className="section-heading"><div><p className="eyebrow">Browse profile categories</p><h2>Explore departments</h2></div><button className="text-button" onClick={() => setSpecialtyAndBrowse('All specialties')}>View all <Icon name="arrow" size={16} /></button></div>
              <div className="specialty-grid">
                {specialties.map((item, index) => <button className="specialty-card" key={item.name} onClick={() => setSpecialtyAndBrowse(item.name)}><span className={`specialty-icon specialty-${index}`}>{item.icon}</span><span className="specialty-name">{item.name}</span><span className="specialty-detail">{item.detail}</span><Icon name="arrow" size={15} /></button>)}
              </div>
            </section>

            <section className="section-block doctors-section">
              <div className="section-heading"><div><p className="eyebrow">Fictional profiles · not verified providers</p><h2>Meet the team</h2></div><button className="text-button" onClick={() => setSection('doctors')}>See all profiles <Icon name="arrow" size={16} /></button></div>
              <div className="doctor-grid">{doctors.slice(0, 3).map((doctor) => <DoctorCard doctor={doctor} key={doctor.id} onBook={openBooking} />)}</div>
            </section>

            <section className="section-block upcoming-section">
              <div className="section-heading"><div><p className="eyebrow">Stay on top of your care</p><h2>Your upcoming visits</h2></div><button className="text-button" onClick={() => setSection('appointments')}>All appointments <Icon name="arrow" size={16} /></button></div>
              {appointments.length ? <AppointmentList appointments={appointments.slice(0, 2)} /> : <div className="empty-inline"><span className="empty-calendar"><Icon name="calendar" size={21} /></span><div><strong>No visits booked yet</strong><p>When you book a visit, you’ll find the details here.</p></div><button className="outline-button" onClick={() => setSection('doctors')}>Find a doctor <Icon name="arrow" size={15} /></button></div>}
            </section>
            <footer className="page-footer">MediConnect · A little more care, a little more connected.</footer>
          </div>
        )}

        {section === 'doctors' && (
          <div className="page-content">
            <section className="welcome-row directory-heading"><div><p className="eyebrow">Fictional profiles · not verified providers</p><h1>Meet the team</h1><p className="page-subtitle">These names and availability are fictional and are not verified medical providers. Listed times are illustrative only. Mentalism is entertainment, not medical care.</p></div><div className="directory-count"><strong>{filteredDoctors.length}</strong><span>profiles</span></div></section>
            <div className="filter-row"><div className="filter-pills" aria-label="Filter by department"><button className={specialty === 'All specialties' ? 'selected' : ''} onClick={() => setSpecialty('All specialties')}>All profiles</button>{specialties.map((item) => <button key={item.name} className={specialty === item.name ? 'selected' : ''} onClick={() => setSpecialty(item.name)}>{item.name}</button>)}</div></div>
            {filteredDoctors.length ? <div className="doctor-grid directory-grid">{filteredDoctors.map((doctor) => <DoctorCard doctor={doctor} key={doctor.id} onBook={openBooking} />)}</div> : <div className="empty-state"><span className="empty-calendar"><Icon name="search" size={23} /></span><h2>No doctors found</h2><p>Try another name or choose a different specialty.</p><button className="outline-button" onClick={() => { setSearch(''); setSpecialty('All specialties'); }}>Clear filters</button></div>}
            <footer className="page-footer">MediConnect · A little more care, a little more connected.</footer>
          </div>
        )}

        {section === 'appointments' && (
          <div className="page-content">
            <section className="welcome-row"><div><p className="eyebrow">Your schedule planner</p><h1>Appointments</h1><p className="page-subtitle">Saved entries stay in this browser session. They do not contact a clinic or create real appointments.</p></div><button className="primary-button" onClick={() => setSection('doctors')}><Icon name="plus" size={17} /> Choose a time</button></section>
            {bookingNotice && <div className="booking-notice" role="status"><span><Icon name="check" size={17} /></span>{bookingNotice}</div>}
            {appointments.length ? <div className="appointment-page-list"><AppointmentList appointments={appointments} /></div> : <div className="empty-state appointment-empty"><span className="empty-calendar"><Icon name="calendar" size={24} /></span><h2>Your next visit starts here</h2><p>Browse our care team and book a time that works for you.</p><button className="primary-button" onClick={() => setSection('doctors')}><Icon name="search" size={17} /> Find a doctor</button></div>}
            <footer className="page-footer">MediConnect · A little more care, a little more connected.</footer>
          </div>
        )}
      </main>

      {bookingDoctor && <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setBookingDoctor(null); }}>
        <section className="booking-modal" role="dialog" aria-modal="true" aria-labelledby="booking-title">
          <div className="modal-top"><span className="modal-kicker">SCHEDULE PLANNER</span><button className="icon-button modal-close" onClick={() => setBookingDoctor(null)} aria-label="Close booking"><Icon name="close" /></button></div>
          <h2 id="booking-title">Choose a time.</h2><p className="modal-intro">Add a time to your personal planner.</p>
          <div className="booking-doctor"><DoctorAvatar doctor={bookingDoctor} /><div><strong>{bookingDoctor.name}</strong><span>{bookingDoctor.specialty} · {bookingDoctor.credentials}</span></div><span className="availability-dot" title="Illustrative time" /></div>
          <form className="booking-form" onSubmit={confirmBooking}>
            <label>Appointment date<input type="date" value={bookingDate} onChange={(event) => setBookingDate(event.target.value)} min={new Date().toLocaleDateString('en-CA')} required /></label>
            <label>Available time<select value={bookingTime} onChange={(event) => setBookingTime(event.target.value)} required><option value="">Choose a time</option><option>9:00 AM</option><option>10:30 AM</option><option>11:15 AM</option><option>1:00 PM</option><option>2:30 PM</option><option>3:45 PM</option></select></label>
            <p className="booking-disclaimer">{bookingDoctor.specialty === 'Mentalist' ? 'Vinay is an entertainment profile, not a medical or mental-health provider. The listed time is illustrative; saving it does not create an appointment.' : 'This profile is fictional and unverified. Saving a time does not contact a clinic or create a real appointment; your entry stays in this browser session.'}</p>
            <button className="primary-button modal-submit" type="submit"><Icon name="calendar" size={17} /> Save to planner</button>
          </form>
        </section>
      </div>}
    </div>
  );
}

function DoctorCard({ doctor, onBook }) {
  return (
    <article className="doctor-card">
      <div className="doctor-card-top"><DoctorAvatar doctor={doctor} large /><span className="availability-pill"><i /> Illustrative time</span></div>
      <h3>{doctor.name}</h3><p className="doctor-specialty">{doctor.specialty} <span>·</span> {doctor.credentials}</p>
      <p className="doctor-tagline">{doctor.tagline}</p>
      <div className="doctor-next"><span><Icon name="clock" size={15} /> Next available</span><strong>{doctor.next}</strong></div>
      <button className="doctor-book-button" onClick={() => onBook(doctor)}>Book appointment <Icon name="arrow" size={16} /></button>
    </article>
  );
}

function AppointmentList({ appointments }) {
  return <div className="appointment-list">{appointments.map((appointment) => {
    const date = new Date(`${appointment.date}T12:00:00`);
    return <article className="appointment-card" key={appointment.id}><div className="appointment-date"><strong>{date.toLocaleDateString('en-US', { day: '2-digit' })}</strong><span>{date.toLocaleDateString('en-US', { month: 'short' })}</span></div><DoctorAvatar doctor={appointment.doctor} /><div className="appointment-info"><strong>{appointment.doctor.name}</strong><span>{appointment.doctor.specialty} · Planner entry</span></div><div className="appointment-time"><span><Icon name="clock" size={15} /> {appointment.time}</span><span><Icon name="pin" size={15} /> Illustrative time</span></div><span className="appointment-status"><i /> Saved locally</span></article>;
  })}</div>;
}

export default App;
