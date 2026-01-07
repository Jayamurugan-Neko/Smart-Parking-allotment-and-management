import React from 'react';
import { QRCodeSVG } from 'qrcode.react';
import '../styles/PaymentModal.css'; // Reuse payment modal styles for consistency

const QRCodeModal = ({ booking, onClose, onPaymentSuccess }) => {
    // Construct UPI Intent Link
    // upi://pay?pa=UPI_ID&pn=NAME&am=AMOUNT&cu=INR&tn=NOTE
    const upiId = booking.slotUpiId;
    const payeeName = "Smart Parking Owner"; // Could be dynamic if we had owner name
    const amount = booking.totalPrice ? booking.totalPrice.toFixed(2) : "0.00";
    const note = `Parking at ${booking.location}`;

    const upiUrl = `upi://pay?pa=${upiId}&pn=${encodeURIComponent(payeeName)}&am=${amount}&cu=INR&tn=${encodeURIComponent(note)}`;

    return (
        <div className="payment-modal-overlay">
            <div className="payment-modal-content">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Scan to Pay</h2>
                <div style={{ textAlign: 'center', margin: '30px 0' }}>
                    <div style={{ background: 'white', padding: '16px', display: 'inline-block', borderRadius: '8px', border: '1px solid #ddd' }}>
                        <QRCodeSVG value={upiUrl} size={200} level={"H"} />
                    </div>

                    <p className="amount-display" style={{ marginTop: '20px' }}>Amount: ₹{amount}</p>
                    <p style={{ color: '#666', fontSize: '0.9em' }}>
                        Scan using any UPI App (GPay, PhonePe, Paytm)<br />
                        <span style={{ fontSize: '0.8em', color: '#999' }}>Paying to: {upiId}</span>
                    </p>

                    <div style={{ marginTop: '20px', borderTop: '1px solid #eee', paddingTop: '15px' }}>
                        <p style={{ fontSize: '0.9em', color: '#d97706', marginBottom: '10px' }}>
                            ⚠️ After receiving payment in your bank app, click below to mark this booking as completed.
                        </p>
                        <button
                            className="action-btn"
                            onClick={() => {
                                if (window.confirm("Confirm that you have received ₹" + amount + " in your bank account?")) {
                                    onPaymentSuccess("MANUAL_UPI_" + Date.now());
                                }
                            }}
                            style={{ width: '100%', padding: '12px', background: '#10b981' }}
                        >
                            Mark as Paid ✅
                        </button>
                    </div>

                    <button
                        className="action-btn"
                        onClick={onClose}
                        style={{ width: '100%', marginTop: '10px', background: 'transparent', color: '#666', border: '1px solid #ccc' }}
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

export default QRCodeModal;
