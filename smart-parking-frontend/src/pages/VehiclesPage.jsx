import VehicleForm from "../components/vehicleForm";

/**
 * VehiclePage Component
 * 
 * Purpose: A simple wrapper page that displays the "Vehicle Management" title
 * and then loads the actual interactive VehicleForm component.
 */
function VehiclePage()
{
    return (
        <>
            <h2>Vehicle Management</h2>
            <VehicleForm />
        </>
    );
}

export default VehiclePage;