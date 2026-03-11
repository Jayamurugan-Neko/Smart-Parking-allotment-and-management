import React, { useEffect, useState } from 'react';
import { getMyBookings, endBooking } from "../services/api";

/**
 * BookingList Component
 * 
 * Purpose: An older, simpler version of the BookingsPage. 
 * Kept for reference or simple views. Lists bookings and handles ending them.
 */
function BookingList() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [paymentModal, setPaymentModal] = useState(null);
    const [showQR, setShowQR] = useState(false);

    const [currentUserId, setCurrentUserId] = useState(1); // Placeholder for current user ID

    const fetchBookings = () => {
        getMyBookings()
            .then(response => {
                setBookings(response.data);
                setLoading(false);
            })
            .catch(() => setLoading(false));
    };

    const endBookingHandler = (bookingId) => {
        endBooking(bookingId)
            .then((response) => {
                window.dispatchEvent(new Event("booking-updated"));
                if (response.data && response.data.totalPrice > 0) {
                    setPaymentModal(response.data);
                    setShowQR(false); // Reset QR view
                } else {
                    alert("Booking Ended. No payment required.");
                }
            })
            .catch(err => {
                if (err.response && err.response.data) {
                    alert(err.response.data);
                } else {
                    alert("Not allowed");
                }
            });
    };


    useEffect(() => {
        fetchBookings();
        window.dispatchEvent(new Event("booking-updated")); // Force refresh on mount
        window.addEventListener("booking-updated", fetchBookings);

        return () => {
            window.removeEventListener("booking-updated", fetchBookings);
        };
    }, []);

    if (loading) return <p>Loading bookings...</p>;

    const myBookings = bookings.filter(
        booking => booking.userId === currentUserId);
    return (
        <div>
            {myBookings.length === 0 && <p>No bookings found</p>}

            {bookings.map((booking) => (
                <div
                    key={booking.bookingId}
                    style={{
                        border: "1px solid gray",
                        margin: "10px",
                        padding: "10px"
                    }}
                >
                    <p><b>Booking ID:</b> {booking.bookingId}</p>
                    <p><b>Slot:</b> {booking.location}</p>
                    <p><b>Vehicle:</b> {booking.vehicleNumber}</p>
                    <p><b>Vehicle Type:</b> {booking.vehicleType}</p>
                    <p><b>Start Time:</b> {booking.startTime}</p>
                    <p><b>Status:</b> {booking.active ? "Active" : "Ended"}</p>

                    {booking.active && (
                        <button
                            onClick={() => endBookingHandler(booking.bookingId)}
                            style={{
                                background: "#ef4444",
                                color: "white",
                                padding: "8px 16px",
                                border: "none",
                                borderRadius: "4px",
                                cursor: "pointer"
                            }}
                        >
                            End Booking
                        </button>
                    )}
                </div>

            ))}

            {/* Payment Modal */}
            {paymentModal && (
                <div style={{
                    position: "fixed", top: 0, left: 0, width: "100%", height: "100%",
                    background: "rgba(0,0,0,0.5)", display: "flex", justifyContent: "center", alignItems: "center"
                }}>
                    <div style={{ background: "white", padding: "20px", borderRadius: "8px", textAlign: "center", minWidth: "300px" }}>
                        <h3>Payment Required</h3>
                        <p style={{ fontSize: "1.2em", fontWeight: "bold" }}>Amount: ₹{paymentModal.totalPrice.toFixed(2)}</p>

                        {!showQR ? (
                            <button
                                onClick={() => setShowQR(true)}
                                style={{
                                    background: "#16a34a", color: "white", padding: "10px 20px",
                                    border: "none", borderRadius: "4px", cursor: "pointer", margin: "10px 0",
                                    fontSize: "1em", display: "flex", alignItems: "center", justifyContent: "center", width: "100%"
                                }}
                            >
                                📱 Show Payment QR Code
                            </button>
                        ) : (
                            paymentModal.slotUpiId ? (
                                <div style={{ margin: "20px 0" }}>
                                    <img
                                        src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=upi://pay?pa=${paymentModal.slotUpiId}&pn=SmartParking&am=${paymentModal.totalPrice}&cu=INR`}
                                        alt="Payment QR Code"
                                        style={{ border: "1px solid #ddd", padding: "5px" }}
                                    />
                                    <p style={{ fontSize: "0.9em", color: "#666", marginTop: "10px" }}>Scan with any UPI App</p>
                                    <p style={{ fontSize: "0.8em", color: "#888" }}>UPI ID: {paymentModal.slotUpiId}</p>
                                </div>
                            ) : (
                                <p style={{ color: "orange" }}>UPI ID not configured for this slot.</p>
                            )
                        )}

                        <button
                            onClick={() => setPaymentModal(null)}
                            style={{
                                background: "#3b82f6", color: "white", padding: "8px 16px",
                                border: "none", borderRadius: "4px", cursor: "pointer", marginTop: "10px"
                            }}
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default BookingList;