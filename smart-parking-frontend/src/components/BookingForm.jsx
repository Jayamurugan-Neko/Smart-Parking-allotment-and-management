import { useState } from "react";
import { bookSlot } from "../services/api";
import "../App.css"; // Ensure styles are loaded

/**
 * BookingForm Component
 * 
 * Purpose: A multi-step form where a user actually books a parking slot.
 * Step 1: User selects vehicle type (Car, Bike, Truck) and sees live availability.
 * Step 2: User enters their specific vehicle number and model.
 * Finally: Confirms the booking with the backend server.
 */
function BookingForm({ slot, availability }) {
  const [vehicleType, setVehicleType] = useState(null);
  const [vehicleNumber, setVehicleNumber] = useState("");
  const [vehicleModel, setVehicleModel] = useState("");
  const [ownerName, setOwnerName] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  // -------- ACTION HANDLERS --------

  // Collects the form data and sends it to the backend to create the booking in the database.
  const handleBooking = () => {
    if (!vehicleType || !vehicleNumber || !vehicleModel) {
      setMessage("Please fill all required fields.");
      return;
    }

    setLoading(true);
    const bookingData = {
      slotId: slot.id,
      vehicleType,
      vehicleNumber,
      vehicleModel,
      ownerName
    };

    bookSlot(bookingData)
      .then(() => {
        setMessage("Booking Confirm! 🎉");
        window.dispatchEvent(new Event("booking-updated"));
        // Reset form
        setVehicleType(null);
        setVehicleNumber("");
        setVehicleModel("");
      })
      .catch((err) => {
        setMessage(err.response?.data?.message || "Booking failed. Try again.");
      })
      .finally(() => setLoading(false));
  };

  // -------- UI HELPERS --------

  // Helper function to figure out how many free spots are left for a specific vehicle type.
  const getCapacity = (type) => {
    switch (type) {
      case "CAR": return { total: availability.carCapacity, free: availability.carAvailable };
      case "BIKE": return { total: availability.bikeCapacity, free: availability.bikeAvailable };
      case "TRUCK": return { total: availability.truckCapacity, free: availability.truckAvailable };
      default: return { total: 0, free: 0 };
    }
  };

  // Renders a clickable button for a vehicle type (e.g. "Car 🚗"). 
  // Disables the button visually and functionally if the spot is FULL.
  const renderTypeOption = (type, label, icon) => {
    const { free } = getCapacity(type);
    const isDisabled = free === 0;
    const isSelected = vehicleType === type;

    return (
      <button
        key={type}
        className={`btn btn-outline ${isSelected ? "selected" : ""}`}
        style={{
          flex: 1,
          margin: "0 5px",
          opacity: isDisabled ? 0.5 : 1,
          cursor: isDisabled ? "not-allowed" : "pointer",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          padding: "15px"
        }}
        onClick={() => !isDisabled && setVehicleType(type)}
        disabled={isDisabled}
      >
        <span style={{ fontSize: "1.5rem", marginBottom: "5px" }}>{icon}</span>
        <span>{label}</span>
        <span style={{ fontSize: "0.8rem", color: isDisabled ? "red" : "green" }}>
          {isDisabled ? "FULL" : `${free} left`}
        </span>
      </button>
    );
  };

  return (
    <div className="fade-in" style={{ marginTop: "20px" }}>
      <h3 style={{ marginBottom: "15px" }}>1. Select Vehicle Type</h3>
      <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", marginBottom: "20px" }}>
        {renderTypeOption("CAR", "Car", "🚗")}
        {renderTypeOption("BIKE", "Bike", "🏍️")}
        {renderTypeOption("TRUCK", "Truck", "🚚")}
      </div>

      {vehicleType && (
        <div className="fade-in card" style={{ padding: "20px", border: "1px solid #e2e8f0" }}>
          <h3 style={{ marginTop: 0 }}>2. Vehicle Details</h3>

          <label>Vehicle Number *</label>
          <input
            className="input-premium"
            placeholder="e.g. TN-38-AB-1234"
            value={vehicleNumber}
            onChange={(e) => setVehicleNumber(e.target.value)}
          />

          <label>Vehicle Model *</label>
          <input
            className="input-premium"
            placeholder="e.g. Hyundai i20"
            value={vehicleModel}
            onChange={(e) => setVehicleModel(e.target.value)}
          />

          <label>Owner Name (Optional)</label>
          <input
            className="input-premium"
            placeholder="Your Name"
            value={ownerName}
            onChange={(e) => setOwnerName(e.target.value)}
          />

          <div style={{ marginTop: "10px" }}>
            <p><strong>Total Price:</strong> Calculated at exit based on duration.</p>
          </div>

          <button
            className="btn btn-primary"
            style={{ width: "100%", marginTop: "10px" }}
            onClick={handleBooking}
            disabled={loading}
          >
            {loading ? "Processing..." : "Confirm Booking"}
          </button>

        </div>
      )}

      {message && (
        <div className={`card fade-in`} style={{
          marginTop: "20px",
          backgroundColor: message.includes("Confirm") ? "#dcfce7" : "#fee2e2",
          color: message.includes("Confirm") ? "#166534" : "#991b1b"
        }}>
          {message}
        </div>
      )}
    </div>
  );
}

export default BookingForm;
