import BookingForm from "./BookingForm";

/**
 * SlotSidebar Component
 * 
 * Purpose: A slide-out panel used on the SlotsPage map.
 * When a user clicks a parking marker on the map, this sidebar opens up to show
 * live availability (How many Cars/Bikes/Trucks can fit right now)
 * and displays the BookingForm so the user can reserve it.
 */
function SlotSidebar({ slot, availability }) {
  if (!availability) {
    return (
      <div style={{ padding: "16px", borderLeft: "1px solid #ccc", width: "320px" }}>
        <h3>{slot.location}</h3>
        <p>Loading availability...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: "16px", borderLeft: "1px solid #ccc", width: "320px" }}>
      <h3>{slot.location}</h3>

      {/* -------------------------------
          AVAILABILITY PREVIEW (PHASE 39)
         ------------------------------- */}
      <h4>Availability</h4>

      <p>
        🚗 Car:{" "}
        <b>
          {availability.carAvailable} / {availability.carCapacity}
        </b>
      </p>

      <p>
        🏍️ Bike:{" "}
        <b>
          {availability.bikeAvailable} / {availability.bikeCapacity}
        </b>
      </p>

      <p>
        🚚 Truck:{" "}
        <b>
          {availability.truckAvailable} / {availability.truckCapacity}
        </b>
      </p>

      <hr />

      {/* -------------------------------
          BOOKING FORM (NEXT PHASE)
         ------------------------------- */}

      {!slot.enabled === false ? (
        <BookingForm
          slot={slot}
          availability={availability}
        />
      ) : (
        <div className="slot-sidebar-closed-message" style={{
          padding: '12px',
          background: '#fee2e2',
          color: '#991b1b',
          borderRadius: '8px',
          marginTop: '16px',
          textAlign: 'center'
        }}>
          <h3>⛔ Temporarily Closed</h3>
          <p>This location is currently not accepting bookings.</p>
        </div>
      )}
    </div>
  );
}

export default SlotSidebar;
