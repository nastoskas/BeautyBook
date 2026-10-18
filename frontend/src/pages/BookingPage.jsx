import { Link, useParams, useSearchParams } from 'react-router-dom'
import { artists } from '../data/artists'
import { services } from '../data/services'
import { useState } from 'react'

function BookingPage() {
    const { id } = useParams()
    const [searchParams] = useSearchParams()

    const artist = artists.find(
        (artist) => artist.id === Number(id)
    )

    const serviceId = Number(searchParams.get('serviceId'))

    const service = services.find(
        (service) =>
            service.id === serviceId &&
            service.artistId === Number(id)
    )

    const [selectedDate, setSelectedDate] = useState('')

    const [selectedTime, setSelectedTime] = useState('')

    if (!artist || !service) {
        return (
            <main className="profile-not-found">
                <h1>Booking details not found</h1>
                <Link to="/" className="primary-button">
                    Back to home
                </Link>
            </main>
        )
    }

    return (
        <main className="booking-page">
            <Link to={`/artists/${artist.id}`} className="back-link">
                ← Back to artist
            </Link>

            <section className="booking-card">
                <p className="eyebrow">YOUR BEAUTY MOMENT</p>
                <h1>Book your appointment</h1>

                <p className="booking-intro">
                    Review your selected artist and service.
                </p>

                <div className="booking-summary">
                    <img src={service.image} alt={service.name} />

                    <div>
                        <h2>{service.name}</h2>
                        <p>Makeup artist: {artist.name}</p>
                        <p>Location: {artist.location}</p>
                        <p>Duration: {service.duration} minutes</p>
                        <strong>
                            {service.price.toLocaleString('de-DE')} ден.
                        </strong>
                    </div>
                </div>

                <div className="booking-form">
                    <h2>Choose your date</h2>
                    <p>Select the day you would like to book your appointment.</p>

                    <label htmlFor="appointment-date">Appointment date</label>
                    <input
                        id="appointment-date"
                        type="date"
                        min={new Date().toLocaleDateString('en-CA')}
                        value={selectedDate}
                        onChange={(event) => setSelectedDate(event.target.value)}
                    />

                    {selectedDate && (
                        <p className="selected-date-message">
                            Selected date: {selectedDate}
                        </p>
                    )}


                    {selectedDate && (
                        <div className="time-slots-section">
                            <h2>Choose your time</h2>
                            <p>Available appointment times for your selected date.</p>

                            <div className="time-slots-grid">
                                {['09:00', '10:30', '12:00', '13:30', '15:00', '16:30'].map((time) => (

                                    <button
                                        key={time}
                                        type="button"
                                        className={`time-slot ${selectedTime === time ? 'selected' : ''}`}
                                        onClick={() => setSelectedTime(time)}
                                    >
                                        {time}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}


                    {selectedTime && (
                        <div className="booking-continue">
                            <p>
                                Your appointment: <strong>{selectedDate}</strong> at{' '}
                                <strong>{selectedTime}</strong>
                            </p>

                            <button
                                type="button"
                                className="primary-button"
                                onClick={() => {
                                    alert(
                                        `Selected appointment: ${selectedDate} at ${selectedTime}`
                                    )
                                }}
                            >
                                Continue to booking →
                            </button>
                        </div>
                    )}

                </div>

            </section>
        </main>
    )
}

export default BookingPage