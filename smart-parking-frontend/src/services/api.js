import axios from "axios";

// -------------------------------
// Central Axios Instance
// -------------------------------
const API = axios.create({
  baseURL: process.env.REACT_APP_API_URL || "http://localhost:8080"
});

// -------------------------------
// Attach JWT Automatically
// -------------------------------
API.interceptors.request.use(
  config => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// -------------------------------
// SLOT APIs
// -------------------------------
export const getSlots = () =>
  API.get("/api/slots");

export const getOwnerSlots = () =>
  API.get("/api/slots/owner");

export const getOwnerSummary = () =>
  API.get("/api/slots/owner/summary");

export const getSlotsForMap = () =>
  API.get("/api/slots/map");

export const getSlotAvailability = (slotId) =>
  API.get(`/api/slots/${slotId}/availability`);

// -------------------------------
// BOOKING APIs
// -------------------------------
export const bookSlot = (data) =>
  API.post("/api/bookings", data);

export const getMyBookings = () =>
  API.get("/api/bookings/me");

export const getOwnerBookings = () =>
  API.get("/api/bookings/owner");

export const endBooking = (bookingId) =>
  API.post(`/api/bookings/${bookingId}/end`);

export const payBooking = (bookingId, paymentRef) =>
  API.post(`/api/bookings/${bookingId}/pay`, paymentRef, {
    headers: { 'Content-Type': 'text/plain' }
  });

// -------------------------------
// AUTH APIs (OPTIONAL FRONTEND USE)
// -------------------------------
export const signup = (data) =>
  API.post("/api/users/signup", data);

export const login = (data) =>
  API.post("/api/users/login", data);

export const updateSlotCapacity = (slotId, car, bike, truck) =>
  API.put(`/api/slots/${slotId}/capacity`, null, {
    params: { car, bike, truck }
  });

export const enableSlot = (slotId) =>
  API.put(`/api/slots/${slotId}/enable`, {});

export const disableSlot = (slotId) =>
  API.put(`/api/slots/${slotId}/disable`, {});

export const createSlot = (data) =>
  API.post(`/api/slots`, data);

export const updateSlotPrice = (slotId, carPrice, bikePrice, truckPrice) =>
  API.put(`/api/slots/${slotId}/price`, null, {
    params: { carPrice, bikePrice, truckPrice }
  });

export const updateSlotUpiId = (slotId, upiId) =>
  API.put(`/api/slots/${slotId}/upi`, null, {
    params: { upiId }
  });

export const deleteSlot = (slotId) =>
  API.delete(`/api/slots/${slotId}`);

// -------------------------------
// USER PROFILE APIs
// -------------------------------
export const getMyProfile = () =>
  API.get("/api/users/me");

export const updateMyProfile = (data) =>
  API.put("/api/users/me", data);

// -------------------------------
// VEHICLE APIs
// -------------------------------
export const getMyVehicles = () =>
  API.get("/api/vehicles/me");

export const deleteVehicle = (vehicleId) =>
  API.delete(`/api/vehicles/${vehicleId}`);

export const createVehicle = (data) =>
  API.post("/api/vehicles", data);


// -------------------------------
// ADMIN APIs
// -------------------------------
export const getAdminStats = async () => {
  const response = await API.get("/api/admin/stats");
  return response.data;
};

export const getOwnerPayouts = async () => {
  const response = await API.get("/api/admin/payouts");
  return response.data;
};

export const processPayout = async (ownerId, amount) => {
  const response = await API.post("/api/admin/payout", { ownerId, amount });
  return response.data;
};

export const getPendingSlots = async () => {
  const response = await API.get("/api/admin/slots/pending");
  return response.data;
};

export const verifySlot = async (slotId, status, comments) => {
  const response = await API.put(`/api/admin/slots/${slotId}/verify`, { status, comments });
  return response.data;
};

export const getAdminProfile = async () => {
  const response = await API.get("/api/admin/profile");
  return response.data;
};

export const updateAdminProfile = async (upiId) => {
  const response = await API.put("/api/admin/profile", { upiId });
  return response.data;
};

// -------------------------------
// USER MANAGEMENT APIs
// -------------------------------
export const getAllUsers = async () => {
  const response = await API.get("/api/admin/users");
  return response.data;
};

export const deleteUser = async (userId) => {
  const response = await API.delete(`/api/admin/users/${userId}`);
  return response.data;
};



// -------------------------------
// PAYMENT APIs (RAZORPAY)
// -------------------------------
export const createPaymentOrder = (bookingId) =>
  API.post("/api/payment/create-order", { bookingId });

export const verifyPaymentSignature = (data) =>
  API.post("/api/payment/verify-payment", data);

export default API;
