import { useEffect, useState } from "react";
import ParkingMap from "../components/ParkingMap";
import SlotSidebar from "../components/SlotSidebar";
import OwnerSlotHistory from "../components/OwnerSlotHistory";
import SlotCard from "../components/SlotCard";
import { fetchSlotsForMap, fetchSlotAvailability } from "../services/slotService";
import "../styles/SlotsPage.css";
import "../styles/common.css";
import ParkingLoader from "../components/ParkingLoader";


function SlotsPage() {
  const [allSlots, setAllSlots] = useState([]);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [availability, setAvailability] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false); // Default closed as requested
  const [rightSidebarSlot, setRightSidebarSlot] = useState(null);
  const [searchActive, setSearchActive] = useState(false);

  // Determine user role for menu
  let user = null, role = 'user';
  try { user = JSON.parse(localStorage.getItem('user')); role = user?.role || 'user'; } catch { }

  // Load all slots on mount
  useEffect(() => {
    fetchSlotsForMap()
      .then(data => {
        setAllSlots(data);
        setSlots(data);
      })
      .catch(err => console.error("Error loading slots:", err))
      .finally(() => setLoading(false));
  }, []);

  // Refresh availability on booking update (same logic as before)
  useEffect(() => {
    const refreshAvailability = async () => {
      if (selectedSlot) {
        const data = await fetchSlotAvailability(selectedSlot.id);
        setAvailability(data);
      }
    };
    window.addEventListener("booking-updated", refreshAvailability);
    const onSlotCreated = (e) => {
      const slot = e.detail;
      setAllSlots(prev => {
        if (prev.some(s => s.id === slot.id)) return prev;
        return [...prev, slot];
      });
      setSlots(prev => (prev.some(s => s.id === slot.id) ? prev : [...prev, slot]));
    };
    window.addEventListener('slot-created', onSlotCreated);
    return () => {
      window.removeEventListener("booking-updated", refreshAvailability);
      window.removeEventListener('slot-created', onSlotCreated);
    };
  }, [selectedSlot]);

  // Slot click handler
  const handleSlotSelect = async (slot) => {
    setSelectedSlot(slot);
    setRightSidebarSlot(slot);
    // Do not close left sidebar automatically on desktop, maybe on mobile
    if (window.innerWidth < 768) setSidebarOpen(false);

    setAvailability(null);
    try {
      const data = await fetchSlotAvailability(slot.id);
      setAvailability(data);
    } catch (err) {
      console.error("Error fetching availability:", err);
    }
  };

  const handleSearch = (query) => {
    const q = query.trim().toLowerCase();
    if (!q) {
      setSlots(allSlots);
      return;
    }
    setSlots(
      allSlots.filter(slot =>
        (slot.location && slot.location.toLowerCase().includes(q)) ||
        (slot.city && slot.city.toLowerCase().includes(q)) ||
        (slot.village && slot.village.toLowerCase().includes(q))
      )
    );
    setSelectedSlot(null);
    setAvailability(null);
  };

  if (loading) {
    return (
      <div className="slots-loading" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg-color)' }}>
        <ParkingLoader message="Finding parking spots..." />
      </div>
    );
  }

  /* Logic for Menu Items based on Role */
  let menuItems;
  if (role === 'owner') {
    menuItems = [
      { label: 'Slot Maintenance', onClick: () => window.location.href = '/owner' },
      { label: 'Book Parking', onClick: () => window.location.href = '/slots' },
      { label: 'History', onClick: () => setRightSidebarSlot('history') },
      { label: 'Logout', onClick: () => { localStorage.clear(); window.location.href = '/login'; } },
    ];
  } else if (role === 'ADMIN') { // Admin View
    menuItems = [
      { label: 'Admin Panel', onClick: () => window.location.href = '/admin' }, // Changed Dashboard -> Admin Panel
      // Removed "Book Parking" and "Booking History" for Admin as requested
      { label: 'Logout', onClick: () => { localStorage.clear(); window.location.href = '/login'; } }
    ];
  } else { // Regular User
    menuItems = [
      { label: 'Dashboard', onClick: () => window.location.href = '/dashboard' },
      { label: 'Book Parking', onClick: () => window.location.href = '/slots' },
      { label: 'Booking History', onClick: () => window.location.href = '/bookings' },
      { label: 'Logout', onClick: () => { localStorage.clear(); window.location.href = '/login'; } }
    ];
  }

  return (
    <div className="slots-page-container">
      {/* Map Background - Full Screen & Static */}
      <div className="slots-map-container">
        <ParkingMap
          slots={slots}
          onSlotSelect={handleSlotSelect}
        />
      </div>

      {/* Floating Header: Menu Button (Left) + Animated Search Bar (Center) */}
      <div className="slots-floating-header">
        <button
          className="slots-menu-btn"
          onClick={() => {
            const willOpen = !sidebarOpen;
            setSidebarOpen(willOpen);
            if (!willOpen) setRightSidebarSlot(null); // Close details if sidebar closes
          }}
          aria-label="Toggle menu"
        >
          ☰
        </button>
      </div>

      {/* Animated Search Bar - Centered */}
      <div className={`search-wrapper ${searchActive ? 'active' : ''}`}>
        <div className="input-holder">
          <input
            type="text"
            className="search-input"
            placeholder="Type to search"
            onChange={(e) => handleSearch(e.target.value)}
          />
          <button
            className="search-icon"
            onClick={() => setSearchActive(true)}
          >
            <span></span>
          </button>
        </div>
        <span
          className="close"
          onClick={() => {
            setSearchActive(false);
            handleSearch(""); // Clear search on close
          }}
        ></span>
      </div>

      {/* Sidebar Drawer (Menu + Results) */}
      <div className={`slots-sidebar ${sidebarOpen ? 'open' : ''}`}>
        {/* Sidebar Header (Mobile Close Button) */}
        <div className="slots-sidebar-mobile-header">
          <h3>Menu & Results</h3>
          <button
            onClick={() => {
              setSidebarOpen(false);
              setRightSidebarSlot(null); // Also close detail panel
            }}
            className="slots-close-btn"
          >✕</button>
        </div>

        {/* Scrollable Content Area */}
        <div className="slots-sidebar-scroll-content">
          {/* Navigation Menu Items (Collapsible or just listed) */}
          <div className="slots-menu-section">
            {menuItems.map((item, idx) => (
              <button key={idx} onClick={item.onClick} className="slots-menu-chip">
                {item.label}
              </button>
            ))}
          </div>

          <div className="slots-divider"></div>

          {/* Results List */}
          <div className="slots-results-section">
            <h3 className="slots-section-title">
              Available Spots ({slots.length})
            </h3>
            <div className="slots-list-container">
              {slots.length === 0 ? (
                <div className="slots-empty-state">
                  <p>No spots found in this area.</p>
                </div>
              ) : (
                slots.map((slot, idx) => (
                  <SlotCard
                    key={slot.id || idx}
                    slot={slot}
                    onBook={() => {
                      if (role !== 'ADMIN') handleSlotSelect(slot);
                    }}
                    role={role}
                  />
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Right Sidebar (Details/Booking) */}
      {rightSidebarSlot && (
        <div className="slots-detail-panel">
          <div className="slots-detail-header">
            <button onClick={() => setRightSidebarSlot(null)} className="slots-back-btn">
              ← Back
            </button>
            <h3>{rightSidebarSlot === 'history' ? 'History' : 'Booking Details'}</h3>
          </div>
          <div className="slots-detail-content">
            {rightSidebarSlot === 'history' ? (
              <OwnerSlotHistory user={user} />
            ) : (
              <SlotSidebar slot={rightSidebarSlot} availability={availability} />
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default SlotsPage;
