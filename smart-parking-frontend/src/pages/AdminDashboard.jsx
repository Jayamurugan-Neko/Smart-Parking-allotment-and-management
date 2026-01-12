import React, { useEffect, useState } from "react";
import { getAdminStats, getOwnerPayouts, processPayout, getPendingSlots, verifySlot, getAdminProfile, updateAdminProfile, getAllUsers, deleteUser } from "../services/api";
import "../styles/admin.css"; // We will create this next
import ParkingLoader from "../components/ParkingLoader";

function AdminDashboard() {
    const [activeTab, setActiveTab] = useState("overview");
    const [stats, setStats] = useState(null);
    const [payouts, setPayouts] = useState([]);
    const [pendingSlots, setPendingSlots] = useState([]);
    const [adminProfile, setAdminProfile] = useState({ upiId: "" });
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    // Load Initial Data
    useEffect(() => {
        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTab]);

    const fetchData = async () => {
        setLoading(true);
        try {
            if (activeTab === "overview") {
                const data = await getAdminStats();
                setStats(data);
            } else if (activeTab === "payouts") {
                const data = await getOwnerPayouts();
                setPayouts(data);
            } else if (activeTab === "verification") {
                const data = await getPendingSlots();
                setPendingSlots(data);
            } else if (activeTab === "settings") {
                const data = await getAdminProfile();
                setAdminProfile(data || { upiId: "" });
            } else if (activeTab === "users") {
                const data = await getAllUsers();
                setUsers(data);
            }
        } catch (error) {
            console.error("Failed to load admin data", error);
        } finally {
            setLoading(false);
        }
    };

    const handlePay = async (owner) => {
        if (window.confirm(`Confirm payout of ₹${owner.pendingAmount} to ${owner.ownerName}?`)) {
            try {
                await processPayout(owner.ownerId, owner.pendingAmount);
                alert("Payout Successful!");
                fetchData(); // Refresh list
            } catch (e) {
                alert("Payout Failed");
            }
        }
    };

    const handleVerify = async (slotId, status) => {
        let comments = "";
        if (status === "REJECTED") {
            comments = prompt("Enter rejection reason (optional):");
            if (comments === null) return; // Cancelled
        }

        try {
            await verifySlot(slotId, status, comments);
            alert(`Slot ${status}`);
            fetchData(); // Refresh list
        } catch (e) {
            alert("Action Failed");
        }
    };
    const handleUpdateUpi = async () => {
        try {
            await updateAdminProfile(adminProfile.upiId);
            alert("Admin UPI Updated Successfully!");
        } catch (e) {
            console.error("Failed to update UPI:", e);
            if (e.response && e.response.status === 404) {
                alert("Admin user not found. Please contact support.");
            } else {
                alert(e.response?.data?.message || "Failed to update UPI. Check console for details.");
            }
        }
    };

    const handleDeleteUser = async (userId, customMessage) => {
        if (window.confirm(customMessage || "Are you sure you want to remove this user from the system?")) {
            try {
                await deleteUser(userId);
                alert("User removed successfully");
                fetchData(); // Refresh list
            } catch (e) {
                alert("Failed to remove user");
            }
        }
    };

    return (
        <div className="admin-container">
            <div className="admin-sidebar">
                <h2>Admin Panel</h2>
                <button className={activeTab === "overview" ? "active" : ""} onClick={() => setActiveTab("overview")}>Overview</button>
                <button className={activeTab === "payouts" ? "active" : ""} onClick={() => setActiveTab("payouts")}>Payouts</button>
                <button className={activeTab === "users" ? "active" : ""} onClick={() => setActiveTab("users")}>Users</button>
                <button className={activeTab === "verification" ? "active" : ""} onClick={() => setActiveTab("verification")}>Verification ({stats?.pendingSlots || 0})</button>
                <button className={activeTab === "settings" ? "active" : ""} onClick={() => setActiveTab("settings")}>Settings</button>
            </div>

            <div className="admin-content">
                {loading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
                        <ParkingLoader message={`Loading ${activeTab}...`} />
                    </div>
                ) : (
                    <>
                        {/* OVERVIEW TAB */}
                        {activeTab === "overview" && stats && (
                            <div className="stats-grid">
                                <div className="stat-card">
                                    <h3>Total Revenue</h3>
                                    <p>₹{stats.totalRevenue}</p>
                                </div>
                                <div className="stat-card">
                                    <h3>Total Bookings</h3>
                                    <p>{stats.totalBookings}</p>
                                </div>
                                <div className="stat-card">
                                    <h3>Active Users</h3>
                                    <p>{stats.totalUsers}</p>
                                </div>
                                <div className="stat-card">
                                    <h3>Pending Slots</h3>
                                    <p>{stats.pendingSlots}</p>
                                </div>
                            </div>
                        )}

                        {/* PAYOUTS TAB */}
                        {activeTab === "payouts" && (
                            <table className="admin-table">
                                <thead>
                                    <tr>
                                        <th>Owner</th>
                                        <th>Email</th>
                                        <th>UPI ID</th>
                                        <th>Total Earned</th>
                                        <th>Paid</th>
                                        <th>Pending</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {payouts.map(owner => (
                                        <tr key={owner.ownerId}>
                                            <td>{owner.ownerName}</td>
                                            <td>{owner.ownerEmail}</td>
                                            <td>{owner.upiId || "N/A"}</td>
                                            <td>₹{owner.totalEarned}</td>
                                            <td>₹{owner.paidAmount}</td>
                                            <td className="pending-amount">₹{owner.pendingAmount}</td>
                                            <td>
                                                {owner.pendingAmount > 0 ? (
                                                    <button
                                                        className="pay-btn"
                                                        onClick={() => handlePay(owner)}
                                                        disabled={!owner.upiId || owner.upiId === "N/A"}
                                                        title={(!owner.upiId || owner.upiId === "N/A") ? "Owner UPI not setup" : "Process Payment"}
                                                    >
                                                        Pay Now
                                                    </button>
                                                ) : (
                                                    <span className="paid-badge">Settled</span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}

                        {/* USERS TAB */}
                        {activeTab === "users" && (
                            <div className="users-management-section">
                                <h3>User Management</h3>
                                <table className="admin-table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Name</th>
                                            <th>Email</th>
                                            <th>Role</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {users.map(user => (
                                            <tr key={user.id}>
                                                <td>{user.id}</td>
                                                <td>{user.name}</td>
                                                <td>{user.email}</td>
                                                <td>
                                                    <span className={`role-badge ${user.role?.toLowerCase()}`}>
                                                        {user.role}
                                                    </span>
                                                </td>
                                                <td>
                                                    {user.role !== "ADMIN" && (
                                                        <button
                                                            className="reject-btn"
                                                            onClick={() => handleDeleteUser(user.id)}
                                                            title="Remove User"
                                                        >
                                                            Remove
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        {/* VERIFICATION TAB */}
                        {activeTab === "verification" && (
                            <div className="slots-grid">
                                {pendingSlots.length === 0 ? <p>No pending slots.</p> : pendingSlots.map(slot => (
                                    <div key={slot.id} className="slot-verify-card">
                                        {slot.imageUrl ? (
                                            <img src={slot.imageUrl} alt="Slot" onError={(e) => e.target.src = "https://placehold.co/400x200?text=No+Image"} />
                                        ) : (
                                            <div style={{
                                                width: '100%',
                                                height: '200px',
                                                background: '#eee',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                color: '#666'
                                            }}>
                                                <span>No Image Preview (API Key Required for Map)</span>
                                            </div>
                                        )}
                                        <div className="slot-info">
                                            <h4>{slot.address}, {slot.city}</h4>
                                            <p><strong>Owner:</strong> {slot.owner?.name}</p>
                                            <p><strong>Price:</strong> ₹{slot.carPricePerHour}/hr</p>
                                            <div className="slot-actions">
                                                <a
                                                    href={`https://www.google.com/maps/search/?api=1&query=${slot.latitude},${slot.longitude}`}
                                                    target="_blank"
                                                    rel="noreferrer"
                                                    className="map-link-btn"
                                                >
                                                    View on Map
                                                </a>
                                                <button className="approve-btn" onClick={() => handleVerify(slot.id, "APPROVED")}>Approve</button>
                                                <button className="reject-btn" onClick={() => handleVerify(slot.id, "REJECTED")}>Reject</button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {/* SETTINGS TAB */}
                        {activeTab === "settings" && (
                            <div className="settings-container">
                                <h3>Admin Configuration</h3>
                                <div className="setting-card">
                                    <label>Central UPI ID (For receiving payments)</label>
                                    <input
                                        type="text"
                                        placeholder="Enter UPI ID (e.g. admin@upi)"
                                        value={adminProfile.upiId}
                                        onChange={(e) => setAdminProfile({ ...adminProfile, upiId: e.target.value })}
                                    />
                                    <button onClick={handleUpdateUpi} className="save-btn">Update UPI</button>
                                </div>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div >
    );
}

export default AdminDashboard;
