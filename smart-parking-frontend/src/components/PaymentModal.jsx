import React, { useState } from 'react';
import '../styles/PaymentModal.css';

const PaymentModal = ({ booking, onClose, onPaymentSuccess }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleRazorpayPayment = async () => {
        setLoading(true);
        setError('');
        try {
            // 1. Create Order
            const { data: orderData } = await import('../services/api').then(module => module.createPaymentOrder(booking.bookingId));

            // Parse orderData if it comes as a string, otherwise use as object
            const order = typeof orderData === 'string' ? JSON.parse(orderData) : orderData;

            // 2. Open Razorpay
            const options = {
                key: process.env.REACT_APP_RAZORPAY_KEY_ID, // Enter the Key ID generated from the Dashboard
                amount: order.amount,
                currency: order.currency,
                name: "Smart Parking",
                description: `Payment for Slot at ${booking.location}`,
                // image: "https://example.com/your_logo",
                order_id: order.id,
                handler: async function (response) {
                    // 3. Verify Payment
                    try {
                        const verifyData = {
                            razorpay_order_id: response.razorpay_order_id,
                            razorpay_payment_id: response.razorpay_payment_id,
                            razorpay_signature: response.razorpay_signature
                        };

                        const verifyModule = await import('../services/api'); // Dynamic import to avoid circular dep issues in some setups
                        await verifyModule.verifyPaymentSignature(verifyData);

                        // 4. Success Callback
                        onPaymentSuccess(response.razorpay_payment_id); // Pass payment ID instead of UTR
                        onClose();
                    } catch (verifyErr) {
                        console.error("Verification Error", verifyErr);
                        setError("Payment successful but verification failed. Contact support.");
                    }
                },
                prefill: {
                    name: booking.userName || "",
                    email: "user@example.com", // You can pass actual user email if available
                    contact: booking.userPhone || ""
                },
                theme: {
                    color: "#3399cc"
                }
            };

            const rzp1 = new window.Razorpay(options);
            rzp1.on('payment.failed', function (response) {
                console.error("Payment Failed", response.error);
                setError(`Payment Failed: ${response.error.description}`);
            });
            rzp1.open();

        } catch (err) {
            console.error("Payment Init Error", err);
            const backendMsg = err.response?.data;
            setError(typeof backendMsg === 'string' ? backendMsg : "Failed to initiate payment. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    // Razorpay minimum is ₹1. If total is less, we charge ₹1.
    const displayAmount = booking.totalPrice < 1 ? "1.00" : Number(booking.totalPrice).toFixed(2);

    return (
        <div className="payment-modal-overlay">
            <div className="payment-modal-content">
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Complete Payment</h2>
                <div style={{ textAlign: 'center', margin: '30px 0' }}>
                    <p className="amount-display">Amount: ₹{displayAmount}</p>
                    <p style={{ color: '#666', marginBottom: '20px' }}>Pay securely using Razorpay</p>

                    {error && <p style={{ color: 'red', marginBottom: '10px' }}>{error}</p>}

                    <button
                        className="action-btn"
                        onClick={handleRazorpayPayment}
                        disabled={loading}
                        style={{ width: '100%', padding: '15px', fontSize: '1.1em', background: '#3399cc' }}
                    >
                        {loading ? "Processing..." : "Pay Now 💳"}
                    </button>

                    <button
                        className="action-btn"
                        onClick={onClose}
                        style={{ width: '100%', marginTop: '10px', background: 'transparent', color: '#666', border: '1px solid #ccc' }}
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PaymentModal;
