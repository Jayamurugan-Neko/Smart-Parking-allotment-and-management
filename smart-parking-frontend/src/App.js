import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useState, useEffect } from "react";

import Navbar from "./components/Navbar";
import SlotPage from "./pages/SlotsPage";
import BookingsPage from "./pages/BookingsPage";
import Dashboard from "./pages/Dashboard";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import OwnerDashboard from "./pages/OwnerDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import ThemeToggle from "./components/ThemeToggle"; // Import

/**
 * App.js Component
 * 
 * Purpose: This is the central brain of your React Frontend. It controls navigation (Routing),
 * overall application state (like knowing if someone is logged in), and wrapping the entire
 * website with standard components like the Navbar.
 * 
 * Key Concepts used here:
 * 1. React Router (`BrowserRouter`, `Routes`, `Route`): Determines which page to show based on the URL (e.g. '/login').
 * 2. useState: Variables that force the screen to redraw when they change (like `isAuthenticated`).
 * 3. useEffect: Runs background tasks automatically when the App loads or when a state changes (like the auto-logout timer).
 */
function App() {

  // -------- STATE --------
  // `isAuthenticated` remembers if the user is currently logged in. 
  // It checks the browser's `localStorage` for a saved "token" to decide its initial true/false value.
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem("token")
  );

  // -------- FUNCTIONS --------

  // Called after a successful login in the LoginPage component.
  // It flips the state to true, causing the App to redraw and show protected pages.
  const handleLogin = () => {
    setIsAuthenticated(true);
  };

  // Called when a user clicks logout or when inactivity forces it.
  // Deletes the security token from browser memory and resets the state to false.
  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  };

  // Determine user role (USER, OWNER, ADMIN) for dashboard routing
  let userRole = null;
  try {
    const user = JSON.parse(localStorage.getItem("user"));
    userRole = user?.role; // Uses optional chaining (?.) to safely read the role if the user exists
  } catch { }

  // -------- EFFECTS --------

  // Auto Logout Logic: Logs the user out if they don't move their mouse or type for 10 minutes.
  // useEffect is perfect for this because we want to set up event listeners exactly once.
  useEffect(() => {
    // If not logged in, no need for an inactivity timer
    if (!isAuthenticated) return;

    let timeoutId;

    // Resets the 10-minute timer countdown back to zero
    const resetTimer = () => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        console.log("Auto-logging out due to inactivity");
        handleLogout();
        window.location.href = "/login"; // Force redirect by reloading the browser window
      }, 10 * 60 * 1000); // 10 minutes (10 mins * 60 secs * 1000 ms)
    };

    // Fired whenever the user does something
    const handleActivity = () => {
      resetTimer();
    };

    // Listeners for activity (Mouse move, keyboard press, click, mouse scroll)
    window.addEventListener("mousemove", handleActivity);
    window.addEventListener("keydown", handleActivity);
    window.addEventListener("click", handleActivity);
    window.addEventListener("scroll", handleActivity);

    // Initialize timer for the very first time
    resetTimer();

    // CLEANUP FUNCTION: Very important in React!
    // When the component dies (or if `isAuthenticated` changes), this cleanup code runs to strip 
    // out the old event listeners. If you don't do this, you get memory leaks and duplicate timers.
    return () => {
      clearTimeout(timeoutId);
      window.removeEventListener("mousemove", handleActivity);
      window.removeEventListener("keydown", handleActivity);
      window.removeEventListener("click", handleActivity);
      window.removeEventListener("scroll", handleActivity);
    };
  }, [isAuthenticated]); // Only re-run this entire useEffect block if `isAuthenticated` changes.

  // -------- UI RENDER --------
  return (
    // <BrowserRouter> wraps everything to enable URL routing features
    <BrowserRouter>
      {/* If the user is logged in, draw the Navbar at the very top. Pass it the logout function and user role as props. */}
      {isAuthenticated && <Navbar onLogout={handleLogout} userRole={userRole} />}
      
      {/* Global dark/light mode button */}
      <ThemeToggle /> 

      {/* <Routes> acts like a switchboard. It finds the first <Route> whose path matches the URL. */}
      <Routes>
        
        {/* LOGIN ROUTE */}
        <Route
          path="/login"
          // If already logged in, instantly redirect (Navigate) to their specific dashboard based on their role
          // If not logged in, show the actual LoginPage and pass the `handleLogin` function to it
          element={
            isAuthenticated
              ? <Navigate to={userRole === "ADMIN" ? "/admin" : userRole === "OWNER" ? "/owner" : "/dashboard"} />
              : <LoginPage onLogin={handleLogin} />
          }
        />
        
        {/* SIGNUP ROUTE */}
        <Route
          path="/signup"
          element={isAuthenticated ? <Navigate to="/dashboard" /> : <SignupPage onSignup={() => window.location.replace('/login')} />}
        />
        
        {/* PROTECTED ROUTES: Note how every component here is guarded by `isAuthenticated ? <Component /> : <Navigate to="/login" />` */}
        
        <Route
          path="/slots"
          element={isAuthenticated ? <SlotPage /> : <Navigate to="/login" />}
        />
        
        {/* Normal User Dashboard Route */}
        <Route
          path="/dashboard"
          element={isAuthenticated && userRole !== "OWNER" && userRole !== "ADMIN" ? <Dashboard /> : <Navigate to={isAuthenticated ? (userRole === "ADMIN" ? "/admin" : "/owner") : "/login"} />}
        />
        
        {/* Parking Lot Owner Dashboard Route */}
        <Route
          path="/owner"
          element={isAuthenticated && userRole === "OWNER" ? <OwnerDashboard /> : <Navigate to={isAuthenticated ? "/dashboard" : "/login"} />}
        />
        
        {/* System Admin Dashboard Route */}
        <Route
          path="/admin"
          element={isAuthenticated && userRole === "ADMIN" ? <AdminDashboard /> : <Navigate to="/login" />}
        />
        
        <Route
          path="/bookings"
          element={isAuthenticated ? <BookingsPage /> : <Navigate to="/login" />}
        />
        
        {/* DEFAULT (CATCH-ALL) ROUTE */}
        {/* The asterisk (*) path matches absolutely anything else. Used here as a redirector if they type a nonsense URL. */}
        <Route
          path="*"
          element={<Navigate to={isAuthenticated ? (userRole === "ADMIN" ? "/admin" : userRole === "OWNER" ? "/owner" : "/slots") : "/login"} />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
