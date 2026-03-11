import React, {useState} from "react";
import { addVehicle } from "../services/api";

/**
 * VehicleForm Component
 * 
 * Purpose: A simple older form used to add a vehicle to a user's profile.
 * Mostly replaced by the modern Dashboard forms, but kept for compatibility.
 */
function VehicleForm() 
{
    const [vehicleNumber, setVehicleNumber] = useState("");
    const [ownerName, setOwnerName] = useState("");

    const submitVehicle = () =>
    {
        addVehicle({ vehicleNumber, ownerName})
        .then(() => alert("Vehicle Added"));
    };

    return (
        <div>
            <h2>Add Vehicle</h2>
            <input placeholder = "Vehicle Number" 
                onChange={e => setVehicleNumber(e.target.value)} />
                <input placeholder = "Owner Name"
                onChange={e => setOwnerName(e.target.value)} />
                <button onClick={submitVehicle}>Add</button>

        </div>
    );
}

export default VehicleForm;
