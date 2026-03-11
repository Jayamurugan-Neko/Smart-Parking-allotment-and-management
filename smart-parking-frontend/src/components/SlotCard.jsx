import { useState, useEffect } from "react";
import "../styles/SlotCard.css";

/**
 * SlotCard Component
 * 
 * Purpose: Displays a compact card summarizing a parking slot (Location, Capacity, Price).
 * Used mostly in the sidebar of the SlotsPage to show search results.
 * It can expand to show more details or trigger the booking dialog.
 */
function SlotCard({ slot, onBook, role }) {
  const [expanded, setExpanded] = useState(false);

  // -------- EVENT LISTENERS --------
  // Allows other components (like clicking a pin on the map) to trigger an event that magically 
  // expands this specific card in the sidebar if its ID matches.
  useEffect(() => {
    // Listen for expand-slot-card event to expand this card if slotId matches
    const handler = (e) => {
      if (e.detail && e.detail.slotId === slot.id) setExpanded(true);
    };
    window.addEventListener('expand-slot-card', handler);
    // Cleanup the listener when the component is removed
    return () => window.removeEventListener('expand-slot-card', handler);
  }, [slot.id]);

  // Get capacity and pricing info
  const getCapacityInfo = () => {
    const capacities = [];
    if (slot.carCapacity > 0) {
      capacities.push({ type: '🚗', label: 'Car', count: slot.carCapacity, price: slot.carPricePerHour || 0 });
    }
    if (slot.bikeCapacity > 0) {
      capacities.push({ type: '🏍️', label: 'Bike', count: slot.bikeCapacity, price: slot.bikePricePerHour || 0 });
    }
    if (slot.truckCapacity > 0) {
      capacities.push({ type: '🚚', label: 'Truck', count: slot.truckCapacity, price: slot.truckPricePerHour || 0 });
    }
    return capacities;
  };

  const capacities = getCapacityInfo();

  // Handle legacy/missing enabled field as true by default
  const isClosed = slot.enabled === false;

  return (
    <div
      className={`slot-card ${expanded ? 'expanded' : ''} ${isClosed ? 'slot-card-disabled' : ''}`}
    >
      <div className="slot-card-header">
        <div className="slot-card-title-section" onClick={() => setExpanded(e => !e)}>
          <h3 className="slot-card-title">
            {slot.location || 'Unnamed Slot'}
            {isClosed && <span className="slot-card-status-badge">⛔ Closed</span>}
          </h3>
          <div className="slot-card-location">
            📍 {slot.city || slot.village || slot.address || 'Location not specified'}
          </div>

          {/* Capacity Info - Always Visible */}
          {capacities.length > 0 && (
            <div className="slot-card-capacity-info">
              {capacities.map((cap, idx) => (
                <span key={idx} className="slot-card-capacity-badge">
                  {cap.type} {cap.count}
                </span>
              ))}
            </div>
          )}

          {/* Price Info - Always Visible */}
          {capacities.length > 0 && (
            <div className="slot-card-price-info">
              {capacities.map((cap, idx) => (
                <span key={idx} className="slot-card-price-badge">
                  {cap.label}: ₹{cap.price}/hr
                </span>
              ))}
            </div>
          )}
        </div>
        {role !== 'ADMIN' && (
          <button
            className={`slot-card-book-button ${isClosed ? 'disabled' : ''}`}
            disabled={isClosed}
            onClick={e => {
              e.stopPropagation();
              if (!isClosed) onBook();
            }}
          >
            {!isClosed ? 'Book' : 'Closed'}
          </button>
        )}
      </div>

      {expanded && (
        <div className="slot-card-expanded-content">
          {slot.address && (
            <div className="slot-card-detail-row">
              <span className="slot-card-detail-label">📍 Address:</span>
              <span className="slot-card-detail-value">{slot.address}</span>
            </div>
          )}
          {capacities.length > 0 && (
            <div className="slot-card-detail-row">
              <span className="slot-card-detail-label">🚗 Capacity:</span>
              <span className="slot-card-detail-value">
                {capacities.map(cap => `${cap.label}: ${cap.count}`).join(', ')}
              </span>
            </div>
          )}
          {capacities.length > 0 && (
            <div className="slot-card-detail-row">
              <span className="slot-card-detail-label">💰 Pricing:</span>
              <span className="slot-card-detail-value">
                {capacities.map(cap => `${cap.label}: ₹${cap.price}/hr`).join(', ')}
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default SlotCard;
