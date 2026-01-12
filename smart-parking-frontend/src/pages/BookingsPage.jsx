import { useEffect, useState, useCallback } from "react";
import { getMyBookings, getOwnerBookings, endBooking } from "../services/api";
import PaymentModal from "../components/PaymentModal";
import QRCodeModal from "../components/QRCodeModal";
import "../styles/BookingsPage.css";
import "../styles/common.css";

function BookingsPage() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [userRole, setUserRole] = useState(null);
    const [paymentBooking, setPaymentBooking] = useState(null); // State for modal
    const [qrBooking, setQrBooking] = useState(null); // State for QR Code modal

    // Get user role on mount
    useEffect(() => {
        try {
            const userStr = localStorage.getItem("user");
            if (userStr) {
                const user = JSON.parse(userStr);
                if (user.role) {
                    setUserRole(user.role);
                }
            }
        } catch (e) {
            console.error("Failed to parse user", e);
        }
    }, []);

    const fetchBookings = useCallback(() => {
        if (!userRole) {
            setLoading(false);
            return;
        }

        setLoading(true);
        const apiCall = userRole === "OWNER" ? getOwnerBookings : getMyBookings;

        apiCall()
            .then(res => setBookings(res.data))
            .catch(err => console.error("Failed to load bookings", err))
            .finally(() => setLoading(false));
    }, [userRole]);

    useEffect(() => {
        fetchBookings();
    }, [fetchBookings]);

    // Function for handling "End Booking"
    const handleEndBooking = async (bookingId) => {
        // Confirmation is key to prevent accidental clicks
        if (!window.confirm("Are you sure you want to end this booking?")) return;
        try {
            // Call backend to stop the timer and calculate price
            await endBooking(bookingId);
            alert("Booking ended successfully");
            fetchBookings(); // Refresh list to show updated price/time
        } catch (err) {
            console.error("Failed to end booking", err);
            alert("Failed to end booking");
        }
    };

    // Function to open the Payment Modal (the popup)
    const handlePayClick = (booking) => {
        // Check if the slot has a UPI ID configured (needed for payment)
        if (booking.slotUpiId) {
            if (userRole === "OWNER") {
                setQrBooking({ ...booking, upiId: booking.slotUpiId });
            } else {
                setPaymentBooking({ ...booking, upiId: booking.slotUpiId });
            }
        } else {
            // Fallback if no UPI ID is found
            alert("No UPI ID found for this parking slot. Please contact admin.");
        }
    };

    // Callback when payment popup says "Success"
    const handlePaymentSuccess = async (paymentRef) => {
        if (!paymentBooking) return;

        // Payment verification is already done in the modal via Razorpay API
        // We just need to notify user and refresh UI
        // Payment verification is already done in the modal via Razorpay API
        // We just need to notify user and refresh UI
        alert("Payment successful! Ref: " + paymentRef);
        fetchBookings();
        setPaymentBooking(null);
        setQrBooking(null);
    };

    return (
        <div className="bookings-page-container">
            <h2 className="bookings-page-title">
                {userRole === "OWNER" ? "Incoming Bookings" : "My Bookings"}
            </h2>
            import ParkingLoader from "../components/ParkingLoader";
            // ... (imports)

            // Inside render:
            {loading ? (
                <ParkingLoader message="Loading your bookings..." />
            ) : bookings.length === 0 ? (
                <p className="bookings-empty">No bookings found</p>
            ) : (
                <div className="bookings-content">
                    {bookings.map(b => (
                        <div
                            key={b.bookingId}
                            className="bookings-card"
                        >
                            <div>
                                <p className="bookings-info-row">
                                    <span role="img" aria-label="pin">📍</span> <span className="bookings-info-label">Location:</span> {b.location}
                                </p>
                                <p className="bookings-info-row">
                                    <span role="img" aria-label="car">🚗</span> <span className="bookings-info-label">Vehicle:</span> {b.vehicleNumber}
                                </p>
                                <p className="bookings-info-row">
                                    <span role="img" aria-label="model">🚘</span> <span className="bookings-info-label">Model:</span> {b.vehicleModel}
                                </p>
                                <p className="bookings-info-row">
                                    <span role="img" aria-label="type">🧾</span> <span className="bookings-info-label">Type:</span> {b.vehicleType}
                                </p>
                                {/* For owners, showing WHO booked might be useful */}
                                {userRole === "OWNER" && (
                                    <>
                                        <p className="bookings-info-row">
                                            <span role="img" aria-label="user">👤</span> <span className="bookings-info-label">Booked By:</span> {b.userName}
                                        </p>
                                        <p className="bookings-info-row">
                                            <span role="img" aria-label="phone">📞</span> <span className="bookings-info-label">Phone:</span> {b.userPhone || "N/A"}
                                        </p>
                                    </>
                                )}
                                <p className="bookings-info-row">
                                    <span role="img" aria-label="start">⏰</span> <span className="bookings-info-label">Start:</span> {new Date(b.startTime).toLocaleString()}
                                </p>
                                {b.endTime && (
                                    <p className="bookings-info-row">
                                        <span role="img" aria-label="end">🏁</span> <span className="bookings-info-label">End:</span> {new Date(b.endTime).toLocaleString()}
                                    </p>
                                )}
                            </div>
                            <div className="bookings-status-section">
                                <div className={`bookings-status ${b.active ? 'bookings-status-active' : 'bookings-status-completed'}`}>
                                    Status: <span>
                                        {b.active ? "Active" : "Completed"}
                                    </span>
                                </div>
                                {b.active && (
                                    <button
                                        className="bookings-end-button"
                                        onClick={() => handleEndBooking(b.bookingId)}
                                    >
                                        End Booking
                                    </button>
                                )}
                                {!b.active && (
                                    <div className="payment-status-wrapper">
                                        <p className="total-price-display">
                                            Total: ₹{b.totalPrice ? b.totalPrice.toFixed(2) : '0.00'}
                                        </p>
                                        {b.paymentStatus === 'COMPLETED' ? (
                                            <span className="payment-badge paid">
                                                {userRole === "OWNER" ? "Paid by User ✅" : "Paid ✅"}
                                            </span>
                                        ) : userRole === "OWNER" ? (
                                            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px' }}>
                                                <span className="payment-badge pending" style={{ color: '#f59e0b', fontWeight: 'bold' }}>
                                                    Payment Pending ⏳
                                                </span>
                                                <button
                                                    className="pay-now-button"
                                                    style={{ background: '#10b981', fontSize: '0.85em' }}
                                                    onClick={() => handlePayClick(b)}
                                                >
                                                    📱 Generate QR Code
                                                </button>
                                            </div>
                                        ) : (
                                            <button
                                                className="pay-now-button"
                                                onClick={() => handlePayClick(b)}
                                            >
                                                Pay Now 💳
                                            </button>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
            {paymentBooking && (
                <PaymentModal
                    booking={paymentBooking}
                    onClose={() => setPaymentBooking(null)}
                    onPaymentSuccess={handlePaymentSuccess}
                />
            )}
            {qrBooking && (
                <QRCodeModal
                    booking={qrBooking}
                    onClose={() => setQrBooking(null)}
                    onPaymentSuccess={handlePaymentSuccess}
                />
            )}
        </div>
    );
}

export default BookingsPage;
