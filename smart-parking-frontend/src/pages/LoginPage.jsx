import { useState } from "react";
import axios from "axios";
import "../styles/login.css"; // Import the styling for the login page

/**
 * LoginPage Component
 * 
 * Purpose: Provides the user interface for existing users (Users, Owners, Admins) to log into the system.
 * It takes an email and password, sends it to the backend to verify, and if successful, saves the secure 
 * login token and user details to the browser's memory (localStorage).
 * 
 * @param {function} onLogin - A function passed down from App.js that gets called upon successful login.
 */
function LoginPage({ onLogin }) {
  // -------- STATE --------
  // useState hooks to track what the user is typing into the inputs.
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(""); // Tracks error messages (e.g. "Invalid password")

  // -------- FUNCTIONS --------

  // Triggered when the user clicks the "Login" button
  const handleLogin = async () => {
    setError(""); // Clear any previous errors before trying again
    
    try {
      // 1. Send Login Request
      // We send the email and password to the backend API.
      const res = await axios.post(
        `${process.env.REACT_APP_API_URL || "http://localhost:8080"}/api/users/login`,
        { email, password }
      );

      // 2. Save Secure Token
      // The backend responds with a JWT (JSON Web Token) if the password is correct.
      // We save this token in `localStorage`, which is a small database inside the user's browser.
      // Now, even if they refresh the page, the browser remembers who they are.
      localStorage.setItem("token", res.data.token);

      // 3. Save User Profile Details
      // We also save non-sensitive profile information like their name and role.
      // App.js uses this role to decide which dashboard to send them to (Admin, Owner, or normal User).
      localStorage.setItem(
        "user",
        JSON.stringify({
          id: res.data.id, // Database ID
          name: res.data.name, 
          email: res.data.email, 
          role: res.data.role, // e.g. "USER", "OWNER", "ADMIN"
          hasSlots: res.data.hasSlots // Helps the Owner dashboard know if this owner has created any slots yet
        })
      );

      // 4. Trigger App Update
      // Call the function passed from App.js to officially tell the app "We are logged in now!"
      onLogin();
    } catch {
      // If the backend returns an error (like a 401 Unauthorized because of a bad password)
      // the code jumps straight down here into the catch block.
      setError("Invalid email or password");
    }
  };

  // -------- UI RENDER --------
  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Smart Parking Login</h2>

        {/* Conditional Rendering: Only show the error paragraph if the 'error' state has text in it */}
        {error && <p className="error">{error}</p>}

        {/* Email Input Field */}
        {/* value={email} forces the input to always match our React state */}
        {/* onChange tells React to update the state every time the user types a single letter */}
        <input
          placeholder="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}
        />

        {/* Password Input Field */}
        <input
          type="password" // Masks the characters with dots
          placeholder="Password"
          value={password}
          onChange={e => setPassword(e.target.value)}
        />

        <button onClick={handleLogin}>Login</button>
        
        {/* Link to the Signup Page for new users */}
        <p style={{ marginTop: 16 }}>
          Don't have an account? <a href="/signup">Sign up</a>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
