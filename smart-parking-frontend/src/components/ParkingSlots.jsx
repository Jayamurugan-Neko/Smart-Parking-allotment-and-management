import { useEffect, useState } from "react";
import ParkingMap from "./ParkingMap";
import SlotSidebar from "./SlotSidebar";
import {
  getSlots,
  getSlotAvailability
} from "../services/api";

/**
 * ParkingSlots Component
 * 
 * Purpose: An older layout component for the interactive map (now mostly replaced 
 * by the more modern SlotsPage). It coordinates the map and the sidebar.
 */
function ParkingSlots() {
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [availability, setAvailability] = useState(null);
  const [loading, setLoading] = useState(true);

  // -------------------------------
  // Fetch all parking slots
  // -------------------------------
  const fetchSlots = async () => {
    try {
      const res = await getSlots();
      setSlots(res.data);
    } catch (err) {
      console.error("Failed to load slots", err);
    } finally {
      setLoading(false);
    }
  };

  // -------------------------------
  // Initial load
  // -------------------------------
  useEffect(() => {
    fetchSlots();
  }, []);

  // -------------------------------
  // Fetch availability when slot selected
  // -------------------------------
  useEffect(() => {
    if (!selectedSlot) {
      setAvailability(null);
      return;
    }

    const fetchAvailability = async () => {
      try {
        const res = await getSlotAvailability(selectedSlot.id);
        setAvailability(res.data);
      } catch (err) {
        console.error("Failed to load availability", err);
      }
    };

    fetchAvailability();
  }, [selectedSlot]);

  // -------------------------------
  // Refresh after booking
  // -------------------------------
  useEffect(() => {
    const refreshAfterBooking = async () => {
      await fetchSlots();

      if (selectedSlot) {
        const res = await getSlotAvailability(selectedSlot.id);
        setAvailability(res.data);
      }
    };

    window.addEventListener("booking-updated", refreshAfterBooking);

    return () => {
      window.removeEventListener("booking-updated", refreshAfterBooking);
    };
  }, [selectedSlot]);

  if (loading) {
    return <p>Loading parking slots...</p>;
  }

  return (
    <div style={{ display: "flex", height: "100%" }}>
      {/* ---------------- Map ---------------- */}
      <div style={{ flex: 1 }}>
        <ParkingMap
          slots={slots}
          onSlotSelect={setSelectedSlot}
        />
      </div>

      {/* ---------------- Sidebar ---------------- */}
      {selectedSlot && (
        <div
          style={{
            width: "320px",
            padding: "16px",
            borderLeft: "1px solid #ddd",
            background: "#fafafa"
          }}
        >
          <SlotSidebar
            slot={selectedSlot}
            availability={availability}
          />
        </div>
      )}
    </div>
  );
}

export default ParkingSlots;
