import { useEffect, useState } from "react";
import OwnerSlotCard from "../components/OwnerSlotCard";
import { getOwnerSlots, createSlot, getOwnerSummary } from "../services/api";
import "../styles/OwnerDashboard.css";
import "../styles/common.css";
import ParkingLoader from "../components/ParkingLoader";

function OwnerDashboard() {
  const [user] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("user"));
    } catch {
      return null;
    }
  });
  // State to hold the registered parking slots for this owner
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);

  // State controls for "Add Slot" form visibility
  const [showCreate, setShowCreate] = useState(false);

  // Form data for creating a new slot (initially empty)
  const [newSlot, setNewSlot] = useState({
    location: "",
    latitude: 0,
    longitude: 0,
    carCapacity: 0,
    bikeCapacity: 0,
    truckCapacity: 0,
    carPricePerHour: 0,
    bikePricePerHour: 0,
    truckPricePerHour: 0,
    imageUrl: "",
    address: "",
    city: "",
    reviews: "",
    upiId: ""
  });

  // Fetch slots from the backend API
  const loadSlots = () => {
    setLoading(true);
    getOwnerSlots()
      .then(res => setSlots(res.data))
      .catch(err => console.error("Failed to load slots", err))
      .finally(() => setLoading(false));
  };

  // Dashboard summary stats (Total Revenue, Active Bookings, etc.)
  const [summary, setSummary] = useState(null);
  const loadSummary = () => {
    getOwnerSummary().then(res => setSummary(res.data)).catch(() => { });
  };

  // Load data when the component mounts (starts)
  useEffect(() => {
    if (!user) return;
    loadSlots();
    loadSummary();

    // If it's a new owner with no slots, automatically show the "Create" form
    if (user.hasSlots === false) {
      setShowCreate(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return (
      <div className="owner-dashboard-container" style={{ minHeight: '80vh', display: 'flex' }}>
        <ParkingLoader message="Loading owner dashboard..." />
      </div>
    );
  }

  const handleCreateSlot = async () => {
    try {
      const res = await createSlot(newSlot);
      setShowCreate(false);
      setNewSlot({
        location: "",
        latitude: 0,
        longitude: 0,
        carCapacity: 0,
        bikeCapacity: 0,
        truckCapacity: 0,
        carPricePerHour: 0,
        bikePricePerHour: 0,
        truckPricePerHour: 0,
        imageUrl: "",
        address: "",
        city: "",
        reviews: "",
        upiId: ""
      });
      loadSlots();
      loadSummary();
      if (res && res.data) {
        window.dispatchEvent(new CustomEvent('slot-created', { detail: res.data }));
      }
    } catch (err) {
      console.error("Failed to create slot", err);
    }
  };

  return (
    <div className="owner-dashboard-container">
      <div className="owner-dashboard-content">
        <h1 className="owner-dashboard-header">🚗 Slot Maintenance</h1>

        <div className="owner-dashboard-main-grid">
          {/* Summary Card */}
          {summary && (
            <div className="owner-dashboard-summary-card">
              <h3 className="owner-dashboard-summary-title">📊 Dashboard Summary</h3>
              <div className="owner-dashboard-summary-item">
                <span>Total Slots:</span>
                <span className="owner-dashboard-summary-value">{summary.totalSlots}</span>
              </div>
              <div className="owner-dashboard-summary-item">
                <span>Car Capacity:</span>
                <span className="owner-dashboard-summary-value">{summary.totalCarCapacity}</span>
              </div>
              <div className="owner-dashboard-summary-item">
                <span>Bike Capacity:</span>
                <span className="owner-dashboard-summary-value">{summary.totalBikeCapacity}</span>
              </div>
              <div className="owner-dashboard-summary-item">
                <span>Truck Capacity:</span>
                <span className="owner-dashboard-summary-value">{summary.totalTruckCapacity}</span>
              </div>
              <div className="owner-dashboard-summary-item">
                <span>Active Bookings:</span>
                <span className="owner-dashboard-summary-value">{summary.activeBookings}</span>
              </div>
              <div className="owner-dashboard-summary-item" style={{ borderBottom: 'none' }}>
                <span>Total Revenue:</span>
                <span className="owner-dashboard-summary-value">₹{summary.totalRevenue?.toFixed(2) || '0.00'}</span>
              </div>
            </div>
          )}

          {/* Create Slot Section */}
          <div className="owner-dashboard-summary-card">
            <button
              onClick={() => setShowCreate(s => !s)}
              className="owner-dashboard-create-button"
            >
              {showCreate ? '❌ Cancel' : '➕ Add Another Slot'}
            </button>

            {showCreate && (
              <div className="owner-dashboard-create-form">
                <h4 className="owner-dashboard-form-title">Register New Parking Slot</h4>
                <div className="owner-dashboard-form-grid">
                  <label className="owner-dashboard-form-label">
                    Location
                    <input
                      className="owner-dashboard-form-input"
                      placeholder="Mall Parking Lot A"
                      value={newSlot.location}
                      onChange={e => setNewSlot(s => ({ ...s, location: e.target.value }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    City
                    <input
                      className="owner-dashboard-form-input"
                      placeholder="Bengaluru"
                      value={newSlot.city}
                      onChange={e => setNewSlot(s => ({ ...s, city: e.target.value }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Address
                    <input
                      className="owner-dashboard-form-input"
                      placeholder="123 Main St, Area"
                      value={newSlot.address}
                      onChange={e => setNewSlot(s => ({ ...s, address: e.target.value }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    UPI ID (for payments)
                    <input
                      className="owner-dashboard-form-input"
                      placeholder="username@bank"
                      value={newSlot.upiId || ''}
                      onChange={e => setNewSlot(s => ({ ...s, upiId: e.target.value }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Latitude
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="12.9716"
                      value={newSlot.latitude}
                      onChange={e => setNewSlot(s => ({ ...s, latitude: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Longitude
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="77.5946"
                      value={newSlot.longitude}
                      onChange={e => setNewSlot(s => ({ ...s, longitude: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Car Capacity
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="20"
                      value={newSlot.carCapacity}
                      onChange={e => setNewSlot(s => ({ ...s, carCapacity: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Car Price / hr
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="50.0"
                      value={newSlot.carPricePerHour}
                      onChange={e => setNewSlot(s => ({ ...s, carPricePerHour: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Bike Capacity
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="10"
                      value={newSlot.bikeCapacity}
                      onChange={e => setNewSlot(s => ({ ...s, bikeCapacity: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Bike Price / hr
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="10.0"
                      value={newSlot.bikePricePerHour}
                      onChange={e => setNewSlot(s => ({ ...s, bikePricePerHour: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Truck Capacity
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="2"
                      value={newSlot.truckCapacity}
                      onChange={e => setNewSlot(s => ({ ...s, truckCapacity: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label">
                    Truck Price / hr
                    <input
                      type="number"
                      className="owner-dashboard-form-input"
                      placeholder="100.0"
                      value={newSlot.truckPricePerHour}
                      onChange={e => setNewSlot(s => ({ ...s, truckPricePerHour: Number(e.target.value) }))}
                    />
                  </label>
                  <label className="owner-dashboard-form-label" style={{ gridColumn: '1 / -1' }}>
                    Image URL (optional)
                    <input
                      className="owner-dashboard-form-input"
                      placeholder="https://.../image.jpg"
                      value={newSlot.imageUrl}
                      onChange={e => setNewSlot(s => ({ ...s, imageUrl: e.target.value }))}
                    />
                  </label>
                </div>
                <button
                  onClick={handleCreateSlot}
                  className="btn-primary owner-dashboard-create-button"
                >
                  Create Slot
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Registered Slots List */}
        <div className="owner-dashboard-slots-section">
          <h3 className="owner-dashboard-slots-title">My Registered Slots</h3>
          {slots.length === 0 ? (
            <div className="owner-dashboard-empty-state">
              <p>You don't have any registered parking slots yet.</p>
              <p>Use "Add Another Slot" to register one.</p>
            </div>
          ) : (
            <div className="owner-dashboard-slots-list">
              {slots.map(slot => (
                <OwnerSlotCard key={slot.id} slot={slot} refreshSlots={loadSlots} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default OwnerDashboard;
