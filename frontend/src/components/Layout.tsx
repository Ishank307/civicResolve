import { NavLink, Outlet } from 'react-router-dom';
import './Layout.css';

export function Layout() {
  return (
    <div className="layout">
      <header className="layout-header">
        <div className="layout-brand">
          <div className="layout-logo-badge">CR</div>
          <div className="layout-title-group">
            <span className="layout-logo">CivicResolve</span>
            <span className="layout-subtitle">Real-Time Issue & Identity Engine</span>
          </div>
        </div>
        <nav className="layout-nav">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            Dashboard
          </NavLink>
          <NavLink to="/submit" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            Submit Report
          </NavLink>
          <NavLink to="/replay" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            Replay Demo
          </NavLink>
          <NavLink to="/audit" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            Audit Trail
          </NavLink>
          <div className="live-indicator">
            <span className="pulse-dot"></span> Live Engine
          </div>
        </nav>
      </header>
      <main className="layout-main">
        <Outlet />
      </main>
    </div>
  );
}

