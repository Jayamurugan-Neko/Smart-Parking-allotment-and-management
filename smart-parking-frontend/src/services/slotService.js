/**
 * slotService.js
 * 
 * Purpose: This file contains specific network calls related to parking slots.
 * Interestingly, unlike `api.js` which uses the imported `axios` library, 
 * this file relies completely on the browser's built-in native `fetch()` API.
 */

// Determines the backend URL (either from the environment or localhost fallback)
const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";

// Fetches a list of all slots specifically formatted for drawing on the interactive map
export async function fetchSlotsForMap() {
  const res = await fetch(`${API_BASE}/api/slots/map`);
  // fetch() Requires us to manually extract JSON from the response object
  return res.json(); 
}

// Predicts real-time slot availability (e.g. 5 cars parked out of 10 max capacity)
export async function fetchSlotAvailability(slotId) {
  const res = await fetch(`${API_BASE}/api/slots/${slotId}/availability`);
  return res.json();
}

// Fetch all historical booking data for slots owned by a specific owner
export async function fetchOwnerSlotHistory(ownerId) {
  const res = await fetch(`${API_BASE}/api/owner/${ownerId}/slot-history`);
  
  // Important! Native `fetch()` doesn't automatically throw errors on 404/500 like Axios does.
  // We must explicitly check if `res.ok` (status code 200-299) before returning the JSON.
  if (!res.ok) throw new Error('Failed to fetch history');
  
  return res.json();
}
