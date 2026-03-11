import { useState } from "react";
// We import our custom 'signup' function directly from the api.js file we investigated earlier.
import { signup } from "../services/api"; 
import "../styles/login.css";

/**
 * SignupPage Component
 * 
 * Purpose: Allows new users to register an account in the system.
 * They must provide a name, email, password, and decide if they want to be 
 * a normal 'USER' looking for parking, or an 'OWNER' who wants to list parking spaces.
 */
function SignupPage({ onSignup }) {
  // -------- STATE --------
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  // Default new accounts to the normal "USER" role
  const [role, setRole] = useState("USER"); 
  const [error, setError] = useState("");

  // -------- FUNCTIONS --------

  // Triggered when the user clicks the "Sign Up" button
  const handleSignup = async () => {
    setError(""); // Clear any old error
    
    try {
      // 1. Package the Registration Data
      // Create a JavaScript object containing exactly what the backend expects
      const data = { name, email, password, role };
      
      // 2. Send to API
      // Calls the 'signup' function from our api.js, which sends a POST request to the backend
      // `await` stops and waits for the server to reply before moving to the next line.
      await signup(data);
      
      // 3. Success Workflow
      // If the backend replies with a 200 OK, we trigger the onSignup function passed from App.js.
      // (If you look in App.js, you'll see this function redirects them back to the Login page).
      onSignup();
    } catch {
      // If the API call fails (e.g., email already exists, or server is down), we show an error.
      setError("Signup failed. Try a different email.");
    }
  };

  // -------- UI RENDER --------
  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Sign Up</h2>
        
        {/* Show error message if one exists */}
        {error && <p className="error">{error}</p>}
        
        {/* Name Input */}
        <input
          placeholder="Name"
          value={name}
          onChange={e => setName(e.target.value)}
        />
        
        {/* Email Input */}
        <input
          placeholder="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}
        />
        
        {/* Password Input */}
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={e => setPassword(e.target.value)}
        />
        
        {/* Role Selection Dropdown */}
        {/* The selected value updates the `role` React state when changed */}
        <select value={role} onChange={e => setRole(e.target.value)}>
          <option value="USER">Normal User</option>
          <option value="OWNER">Owner</option>
        </select>

        <button onClick={handleSignup}>Sign Up</button>
      </div>
    </div>
  );
}

export default SignupPage;
