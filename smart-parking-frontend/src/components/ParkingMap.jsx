import { MapContainer, TileLayer, Marker, Popup, ZoomControl, CircleMarker, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import { useEffect, useState } from "react";

/**
 * ParkingMap Component (Wrapper & Inner)
 * 
 * Purpose: Renders the interactive map shown on the SlotsPage using the Leaflet library.
 * It displays:
 * 1. Markers (Green for Available, Red for Full) for every parking slot.
 * 2. A "Locate Me" button to find the user's current GPS location.
 * 3. Listens for events to automatically center the map on a specific slot when clicked in the sidebar.
 */

// Listen for center events globally and move map securely
function useCenterOnSlot(mapInstance) {
  useEffect(() => {
    if (!mapInstance) return;
    const handler = (e) => {
      const slot = e.detail;
      if (slot && slot.latitude != null && slot.longitude != null) {
        mapInstance.setView([slot.latitude, slot.longitude], 16);
      }
    };
    window.addEventListener('center-on-slot', handler);
    return () => window.removeEventListener('center-on-slot', handler);
  }, [mapInstance]);
}

// Helper to access map instance in React Leaflet v3/v4+
function MapInstanceSetter({ setMapInstance }) {
  const map = useMap();
  useEffect(() => {
    setMapInstance(map);
  }, [map, setMapInstance]);
  return null;
}

// Hook wrapper inside default export
function ParkingMapWrapper(props) {
  const [mapInstance, setMapInstance] = useState(null);
  return <ParkingMapInner {...props} mapInstance={mapInstance} setMapInstance={setMapInstance} />;
}

function ParkingMapInner({ slots, onSlotSelect, mapInstance, setMapInstance }) {
  useCenterOnSlot(mapInstance);

  // re-create icons here to avoid issues
  const greenIcon = new L.Icon({
    iconUrl: "https://maps.gstatic.com/mapfiles/ms2/micons/green-dot.png",
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  const redIcon = new L.Icon({
    iconUrl: "https://maps.gstatic.com/mapfiles/ms2/micons/red-dot.png",
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  const isAnyAvailable = (slot) => {
    return (
      slot.carCapacity > 0 ||
      slot.bikeCapacity > 0 ||
      slot.truckCapacity > 0
    );
  };

  const [userLocation, setUserLocation] = useState(null);
  const [locationLoading, setLocationLoading] = useState(false);

  // -------- ACTION HANDLERS --------

  // Asks the browser for the user's current GPS coordinates and flies the map there.
  const handleLocateMe = () => {
    // If map isn't ready, we can't fly there, but we can still try to get location if we want to update state.
    // But since we want to flyTo, we need map instance.
    if (!mapInstance) {
      console.warn("Map instance not ready");
      return;
    }

    setLocationLoading(true);

    if (!navigator.geolocation) {
      alert("Geolocation is not supported by your browser");
      setLocationLoading(false);
      return;
    }

    // This will trigger the browser's permission prompt
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const latLng = [latitude, longitude];
        setUserLocation(latLng);
        mapInstance.flyTo(latLng, 16);
        setLocationLoading(false);
      },
      (error) => {
        console.error("Error getting location", error);
        let msg = "Unable to retrieve your location";
        if (error.code === 1) msg = "Location permission denied. Please allow access in site settings.";
        else if (error.code === 2) msg = "Location unavailable.";
        else if (error.code === 3) msg = "Location request timed out.";

        alert(msg);
        setLocationLoading(false);
      }
    );
  };

  return (
    <div style={{ width: "100%", height: "100%", position: "relative" }}>
      <MapContainer
        center={[12.9165, 77.6101]}
        zoom={13}
        zoomControl={false} // Disable default controls
        style={{ height: "100%", width: "100%" }}
      >
        <MapInstanceSetter setMapInstance={setMapInstance} />
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ZoomControl position="bottomright" />

        {/* User Location Marker */}
        {userLocation && (
          <CircleMarker
            center={userLocation}
            pathOptions={{ color: 'white', fillColor: '#4285F4', fillOpacity: 1 }}
            radius={8}
            weight={2}
          >
            <Popup>You are here</Popup>
          </CircleMarker>
        )}

        {slots.map(slot => (
          <Marker
            key={slot.id}
            position={[slot.latitude, slot.longitude]}
            icon={isAnyAvailable(slot) ? greenIcon : redIcon}
            eventHandlers={{
              click: () => onSlotSelect(slot),
            }}
          >
            <Popup>
              <b>{slot.location}</b><br />
              Status: {isAnyAvailable(slot) ? "Available" : "Full"}
            </Popup>
          </Marker>
        ))}
      </MapContainer>

      {/* Locate Me Button */}
      <button
        onClick={handleLocateMe}
        style={{
          position: "absolute",
          bottom: "120px",
          right: "10px",
          zIndex: 1000,
          background: "white",
          border: "2px solid rgba(0,0,0,0.2)",
          borderRadius: "4px",
          width: "34px",
          height: "34px",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          cursor: "pointer",
          boxShadow: "0 1px 5px rgba(0,0,0,0.65)"
        }}
        title="Show my location"
      >
        {locationLoading ? (
          <span style={{ fontSize: '12px' }}>⏳</span>
        ) : (
          <span style={{ fontSize: '18px' }}>🎯</span>
        )}
      </button>
    </div>
  );
}

export default ParkingMapWrapper;
