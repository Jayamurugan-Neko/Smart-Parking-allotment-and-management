import { useEffect, useState } from "react";
import { getMyProfile, updateMyProfile, getMyVehicles, createVehicle, deleteVehicle } from "../services/api";
import "../styles/Dashboard.css";
import "../styles/common.css";
import ParkingLoader from "../components/ParkingLoader";

function Dashboard() {
  const [vehicles, setVehicles] = useState([]);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isAddingVehicle, setIsAddingVehicle] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");
  const [vehicleMessage, setVehicleMessage] = useState("");
  const [vehicleMessageType, setVehicleMessageType] = useState("");

  const [formData, setFormData] = useState({
    name: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    zipCode: "",
    country: ""
  });

  const [vehicleFormData, setVehicleFormData] = useState({
    vehicleNumber: "",
    vehicleModel: "",
    vehicleType: "CAR",
    ownerName: ""
  });

  useEffect(() => {
    loadUserProfile();
    loadVehicles();
  }, []);

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      setMessage(""); // Clear any previous messages
      const res = await getMyProfile();
      const userData = res.data;
      setUser(userData);
      setFormData({
        name: userData.name || "",
        phone: userData.phone || "",
        address: userData.address || "",
        city: userData.city || "",
        state: userData.state || "",
        zipCode: userData.zipCode || "",
        country: userData.country || ""
      });
    } catch (err) {
      console.error("Failed to load user profile", err);
      // Only show error if it's not an authentication issue
      if (err.response?.status === 401 || err.response?.status === 403) {
        // Redirect to login if unauthorized
        localStorage.clear();
        window.location.href = '/login';
        return;
      }
      // For other errors, try to use localStorage data as fallback
      try {
        const userInfo = JSON.parse(localStorage.getItem('user'));
        if (userInfo) {
          setUser({
            id: userInfo.id,
            name: userInfo.name,
            email: userInfo.email,
            phone: "",
            address: "",
            city: "",
            state: "",
            zipCode: "",
            country: "",
            role: userInfo.role
          });
          setFormData({
            name: userInfo.name || "",
            phone: "",
            address: "",
            city: "",
            state: "",
            zipCode: "",
            country: ""
          });
          // Don't show error if we have fallback data
          return;
        }
      } catch (e) {
        console.error("Error loading fallback data:", e);
      }
      setMessage("Failed to load profile. Please refresh the page.");
      setMessageType("error");
      setTimeout(() => setMessage(""), 5000);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = async () => {
    try {
      setMessage("");
      const res = await updateMyProfile(formData);
      setUser(res.data);
      setIsEditing(false);
      setMessage("Profile updated successfully!");
      setMessageType("success");

      // Update localStorage user info
      try {
        const userInfo = JSON.parse(localStorage.getItem('user'));
        if (userInfo) {
          userInfo.name = res.data.name;
          localStorage.setItem('user', JSON.stringify(userInfo));
        }
      } catch (e) {
        console.error("Error updating localStorage:", e);
      }

      setTimeout(() => setMessage(""), 3000);
    } catch (err) {
      console.error("Failed to update profile", err);
      setMessage(err.response?.data?.message || "Failed to update profile");
      setMessageType("error");
      setTimeout(() => setMessage(""), 5000);
    }
  };

  const handleCancel = () => {
    if (user) {
      setFormData({
        name: user.name || "",
        phone: user.phone || "",
        address: user.address || "",
        city: user.city || "",
        state: user.state || "",
        zipCode: user.zipCode || "",
        country: user.country || ""
      });
    }
    setIsEditing(false);
    setMessage("");
  };

  const loadVehicles = async () => {
    try {
      const res = await getMyVehicles();
      setVehicles(res.data || []);
    } catch (err) {
      console.error("Failed to load vehicles", err);
      setVehicles([]);
    }
  };

  const handleVehicleInputChange = (e) => {
    const { name, value } = e.target;
    setVehicleFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAddVehicle = async () => {
    if (!vehicleFormData.vehicleNumber || !vehicleFormData.vehicleType) {
      setVehicleMessage("Vehicle number and type are required");
      setVehicleMessageType("error");
      setTimeout(() => setVehicleMessage(""), 3000);
      return;
    }

    try {
      setVehicleMessage("");
      const vehicleData = {
        vehicleNumber: vehicleFormData.vehicleNumber,
        vehicleModel: vehicleFormData.vehicleModel || "",
        vehicleType: vehicleFormData.vehicleType,
        ownerName: vehicleFormData.ownerName || user?.name || ""
      };
      await createVehicle(vehicleData);
      setVehicleMessage("Vehicle registered successfully!");
      setVehicleMessageType("success");
      setIsAddingVehicle(false);
      setVehicleFormData({
        vehicleNumber: "",
        vehicleModel: "",
        vehicleType: "CAR",
        ownerName: ""
      });
      loadVehicles();
      setTimeout(() => setVehicleMessage(""), 3000);
    } catch (err) {
      console.error("Failed to register vehicle", err);
      setVehicleMessage(err.response?.data?.message || "Failed to register vehicle");
      setVehicleMessageType("error");
      setTimeout(() => setVehicleMessage(""), 5000);
    }
  };

  const handleCancelVehicle = () => {
    setIsAddingVehicle(false);
    setVehicleFormData({
      vehicleNumber: "",
      vehicleModel: "",
      vehicleType: "CAR",
      ownerName: ""
    });
    setVehicleMessage("");
  };

  // Delete a vehicle after confirmation
  const handleDeleteVehicle = async (id) => {
    // Show a browser confirmation alert
    if (window.confirm("Are you sure you want to delete this vehicle?")) {
      try {
        // Call the backend API to delete the vehicle
        await deleteVehicle(id);

        // Show success message
        setVehicleMessage("Vehicle deleted successfully");
        setVehicleMessageType("success");

        // Refresh the list so the deleted vehicle disappears immediately
        loadVehicles();

        // Clear message after 3 seconds
        setTimeout(() => setVehicleMessage(""), 3000);
      } catch (err) {
        // Handle potential errors (e.g. server down, unauthorized)
        console.error("Failed to delete vehicle", err);
        setVehicleMessage("Failed to delete vehicle");
        setVehicleMessageType("error");
        setTimeout(() => setVehicleMessage(""), 3000);
      }
    }
  };


  if (loading) {
    return (
      <div className="dashboard-container" style={{ minHeight: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <ParkingLoader message="Loading profile..." />
      </div>
    );
  }

  return (
    <div className="dashboard-container fade-in">
      <div className="dashboard-content-wrapper">
        <h2 className="dashboard-title">Dashboard</h2>

        {/* User Profile Card */}
        <div className="dashboard-card">
          <div className="dashboard-section-title">
            <span>👤 Personal Information</span>
            {!isEditing && (
              <button
                className="btn-edit"
                onClick={() => setIsEditing(true)}
              >
                ✏️ Edit Profile
              </button>
            )}
          </div>

          {isEditing ? (
            <div>
              <div className="form-grid">
                <label className="form-label">
                  Name *
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    className="form-input"
                    required
                  />
                </label>
                <label className="form-label">
                  Phone
                  <input
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="+91 1234567890"
                  />
                </label>
                <label className="form-label">
                  Address
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="Street address"
                  />
                </label>
                <label className="form-label">
                  City
                  <input
                    type="text"
                    name="city"
                    value={formData.city}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="City"
                  />
                </label>
                <label className="form-label">
                  State
                  <input
                    type="text"
                    name="state"
                    value={formData.state}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="State"
                  />
                </label>
                <label className="form-label">
                  Zip Code
                  <input
                    type="text"
                    name="zipCode"
                    value={formData.zipCode}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="123456"
                  />
                </label>
                <label className="form-label">
                  Country
                  <input
                    type="text"
                    name="country"
                    value={formData.country}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="Country"
                  />
                </label>
              </div>
              <div className="button-group">
                <button
                  className="btn-primary dashboard-btn-primary"
                  onClick={handleSave}
                >
                  💾 Save Changes
                </button>
                <button
                  className="btn-secondary dashboard-btn-secondary"
                  onClick={handleCancel}
                >
                  ❌ Cancel
                </button>
              </div>
            </div>
          ) : (
            <div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">Name:</span>
                <span className="dashboard-info-value">{user?.name || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">Email:</span>
                <span className="dashboard-info-value">{user?.email || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">Phone:</span>
                <span className="dashboard-info-value">{user?.phone || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">Address:</span>
                <span className="dashboard-info-value">{user?.address || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">City:</span>
                <span className="dashboard-info-value">{user?.city || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">State:</span>
                <span className="dashboard-info-value">{user?.state || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row">
                <span className="dashboard-info-label">Zip Code:</span>
                <span className="dashboard-info-value">{user?.zipCode || 'Not set'}</span>
              </div>
              <div className="dashboard-info-row" style={{ borderBottom: 'none' }}>
                <span className="dashboard-info-label">Country:</span>
                <span className="dashboard-info-value">{user?.country || 'Not set'}</span>
              </div>
            </div>
          )}

          {message && (
            <div className={`message ${messageType === 'success' ? 'message-success' : 'message-error'}`}>
              {message}
            </div>
          )}
        </div>

        {/* Vehicles Card */}
        <div className="dashboard-card">
          <div className="dashboard-section-title">
            <span>🚗 Owned Vehicles</span>
            {!isAddingVehicle && (
              <button
                className="btn-edit"
                onClick={() => setIsAddingVehicle(true)}
              >
                ➕ Register Vehicle
              </button>
            )}
          </div>

          {isAddingVehicle ? (
            <div>
              <div className="form-grid">
                <label className="form-label">
                  Vehicle Number *
                  <input
                    type="text"
                    name="vehicleNumber"
                    value={vehicleFormData.vehicleNumber}
                    onChange={handleVehicleInputChange}
                    className="form-input"
                    placeholder="ABC-1234"
                    required
                  />
                </label>
                <label className="form-label">
                  Vehicle Model
                  <input
                    type="text"
                    name="vehicleModel"
                    value={vehicleFormData.vehicleModel}
                    onChange={handleVehicleInputChange}
                    className="form-input"
                    placeholder="Honda City"
                  />
                </label>
                <label className="form-label">
                  Vehicle Type *
                  <select
                    name="vehicleType"
                    value={vehicleFormData.vehicleType}
                    onChange={handleVehicleInputChange}
                    className="form-input"
                    required
                  >
                    <option value="CAR">Car</option>
                    <option value="BIKE">Bike</option>
                    <option value="TRUCK">Truck</option>
                  </select>
                </label>
                <label className="form-label">
                  Owner Name
                  <input
                    type="text"
                    name="ownerName"
                    value={vehicleFormData.ownerName}
                    onChange={handleVehicleInputChange}
                    className="form-input"
                    placeholder={user?.name || "Owner Name"}
                  />
                </label>
              </div>
              <div className="button-group">
                <button
                  className="btn-primary dashboard-btn-primary"
                  onClick={handleAddVehicle}
                >
                  💾 Register Vehicle
                </button>
                <button
                  className="btn-secondary dashboard-btn-secondary"
                  onClick={handleCancelVehicle}
                >
                  ❌ Cancel
                </button>
              </div>
              {vehicleMessage && (
                <div className={`message ${vehicleMessageType === 'success' ? 'message-success' : 'message-error'}`}>
                  {vehicleMessage}
                </div>
              )}
            </div>
          ) : (
            <div>
              {vehicles && vehicles.length > 0 ? (
                <div className="dashboard-vehicles-list">
                  {vehicles.map((v) => (
                    <div key={v.id} className="dashboard-vehicle-item" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <strong>{v.vehicleNumber}</strong>
                        {v.vehicleModel && <span> - {v.vehicleModel}</span>}
                        <span> ({v.vehicleType})</span>
                      </div>
                      <button
                        onClick={() => handleDeleteVehicle(v.id)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}
                        title="Delete Vehicle"
                      >
                        🗑️
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ color: '#888', fontSize: 'clamp(14px, 3vw, 16px)', textAlign: 'center', padding: '20px' }}>
                  No vehicles registered. Register a vehicle for quick booking!
                </p>
              )}
            </div>
          )}
        </div>

        {/* Booking History Card */}
        <div className="dashboard-card">
          <h3 className="dashboard-section-title">
            <span>📋 Booking History</span>
          </h3>
          <p style={{ color: '#555', fontSize: 'clamp(14px, 3vw, 16px)', marginBottom: '16px' }}>
            View and manage your parking bookings
          </p>
          <button
            className="btn-primary dashboard-btn-primary"
            onClick={() => window.location.href = '/bookings'}
          >
            View My Booking History
          </button>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
