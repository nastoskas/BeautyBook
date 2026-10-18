import { useParams, Link, useNavigate} from 'react-router-dom'
import { artists } from '../data/artists'
import { services } from '../data/services'

function ArtistProfile() {
    const navigate = useNavigate()

    const { id } = useParams()

    const artist = artists.find((artist) => artist.id === Number(id))

    const artistServices = services.filter(
        (service) => service.artistId === Number(id)
    )

    if (!artist) {
        return (
            <main className="profile-not-found">
                <h1>Artist not found</h1>
                <Link to="/" className="primary-button">
                    Back to home
                </Link>
            </main>
        )
    }

    return (
        <main className="profile-page">
            <Link to="/" className="back-link">
                ← Back to artists
            </Link>

            <section className="profile-card">
                <img
                    className="profile-image"
                    src={artist.image}
                    alt={artist.name}
                />

                <div className="profile-details">
                    <p className="eyebrow">MEET YOUR ARTIST</p>

                    <h1>{artist.name}</h1>

                    <p className="profile-location">
                        {artist.location}
                    </p>

                    <p className="profile-rating">
                        ★ {artist.rating} · {artist.reviews} reviews
                    </p>

                    <div className="profile-specialty">
                        {artist.specialty}
                    </div>

                    <p className="profile-description">
                        Discover your perfect look with {artist.name}.
                        Explore makeup services and find the right style
                        for your next special occasion.
                    </p>

                    <button
                        className="primary-button"
                        onClick={() =>
                            alert('Appointment booking — coming soon!')
                        }
                    >
                        Book an appointment
                    </button>
                </div>
            </section>

    <section className="services-section">
        <p className="eyebrow">WHAT I OFFER</p>
        <h2>Explore my services</h2>
        <p className="services-intro">
            Find the perfect service for your next occasion.
        </p>

        <div className="services-grid">
            {artistServices.map((service) => (
                <article className="service-card" key={service.id}>
                    <img
                        className="service-image"
                        src={service.image}
                        alt={service.name}
                    />

                    <div className="service-info">
                        <h3>{service.name}</h3>

                        <p className="service-description">
                            {service.description}
                        </p>

                        <div className="service-meta">
                            <span>◷ {service.duration} min</span>
                            <strong>
                                {service.price.toLocaleString('de-DE')} ден.
                            </strong>
                        </div>

                        <button
                            className="service-book-button"
                            onClick={() =>
                                navigate(`/artists/${artist.id}/book?serviceId=${service.id}`)
                            }
                        >
                            Select service
                        </button>
                    </div>
                </article>
            ))}
        </div>
    </section>

</main>
    )
}

export default ArtistProfile