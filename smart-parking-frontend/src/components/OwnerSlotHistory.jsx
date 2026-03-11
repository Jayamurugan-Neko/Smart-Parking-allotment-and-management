import { useEffect, useState } from "react";
import { fetchOwnerSlotHistory } from "../services/slotService";

/**
 * OwnerSlotHistory Component
 * 
 * Purpose: A simple table visible to Owners that lists every single booking 
 * ever made for their parking slots.
 * Includes details like who booked it, what vehicle, and the exact times.
 */
function OwnerSlotHistory({ user }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!user) return;
    setLoading(true);
    fetchOwnerSlotHistory(user.id)
      .then(setHistory)
      .catch(err => setError("Failed to load history"))
      .finally(() => setLoading(false));
  }, [user]);

  if (!user) return <div>Please login as owner.</div>;
  if (loading) return <div>Loading history...</div>;
  if (error) return <div>{error}</div>;
  if (!history.length) return <div>No booking history found.</div>;

  return (
    <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
      <table style={{ width: '100%', fontSize: 14, borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f5f5f5' }}>
            <th style={{ padding: 6, border: '1px solid #eee' }}>Slot</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>Type</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>Booked By</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>Vehicle</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>From</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>To</th>
            <th style={{ padding: 6, border: '1px solid #eee' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {history.map((h, i) => (
            <tr key={i}>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.slotLocation || h.slotId}</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.slotType}</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.bookedByName} ({h.bookedByEmail})</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.vehicleType} {h.vehicleNumber}</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.startTime}</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.endTime}</td>
              <td style={{ padding: 6, border: '1px solid #eee' }}>{h.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default OwnerSlotHistory;
