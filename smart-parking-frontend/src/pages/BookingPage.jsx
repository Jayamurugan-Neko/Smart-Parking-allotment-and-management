import BookingForm from "../components/BookingForm";

/**
 * BookingPage Component
 * 
 * Purpose: A simple wrapper page that displays the "Book Parking Slot" title 
 * and then loads the actual interactive BookingForm component.
 */
function BookingPage()
{
    return (
        <>
            <h2>Book Parking Slot</h2>
            <BookingForm />
        </>
    )
}

export default BookingPage;