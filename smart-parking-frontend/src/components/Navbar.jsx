import { Link, useLocation } from "react-router-dom";
import { useState } from "react";
import "../styles/layout.css";

/**
 * Navbar Component
 * 
 * Purpose: The top navigation bar visible across all pages once logged in.
 * It dynamically changes its links depending on whether the user is a normal User,
 * an Owner, or the System Admin. 
 * Includes a hamburger menu for a mobile-friendly mobile experience.
 */
function Navbar({ onLogout, userRole }) {
  const location = useLocation();
  const isSlotsPage = location.pathname === '/slots';
  
  // Track whether the mobile drop-down menu is open or closed
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
  const closeMenu = () => setIsMenuOpen(false);

  return (
    <nav className={`navbar ${isSlotsPage ? 'navbar-dynamic' : ''}`}>
      <h3>Smart Parking</h3>

      {/* The Hamburger Button for Mobile Phones */}
      <button className="mobile-menu-btn" onClick={toggleMenu} aria-label="Toggle Menu">
        <span className={`hamburger ${isMenuOpen ? 'open' : ''}`}></span>
      </button>

      {/* The actual navigation links */}
      <div className={`nav-links ${isMenuOpen ? 'active' : ''}`}>
        
        {/* Conditional Rendering: Show different links based on Role */}
        {userRole === "ADMIN" ? (
          <Link to="/admin" onClick={closeMenu}>Admin Panel</Link>
        ) : userRole === "OWNER" ? (
          <Link to="/owner" onClick={closeMenu}>Slot Maintenance</Link>
        ) : (
          <Link to="/dashboard" onClick={closeMenu}>Dashboard</Link>
        )}
        
        {/* Everyone can see the slots page */}
        <Link to="/slots" onClick={closeMenu}>Available Slots</Link>
        
        {/* Only Users and Owners have bookings, Admins don't need this */}
        {userRole !== "ADMIN" && <Link to="/bookings" onClick={closeMenu}>My Bookings</Link>}
        
        <button className="logout-btn" onClick={() => { onLogout(); closeMenu(); }}>Logout</button>
      </div>
    </nav>
  );
}

export default Navbar;
