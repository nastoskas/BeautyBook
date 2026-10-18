import { artists } from '../data/artists'
import { Link } from 'react-router-dom'

function HomePage() {
    return (
        <>
            <section className="hero-section">
                <p className="eyebrow">YOUR BEAUTY, YOUR MOMENT</p>

                <h1>
                    Discover your
                    <br />
                    <span>perfect look.</span>
                </h1>

                <p className="hero-description">
                    Find your makeup artist, explore beauty services,
                    and book your next appointment with ease.
                </p>

                <a className="primary-button" href="#artists">
                    Find your makeup artist
                </a>
            </section>

            <section id="artists" className="artists-section">
                <p className="eyebrow">MEET OUR EXPERTS</p>
                <h2>Find your perfect makeup artist</h2>

                <p className="section-description">
                    Discover talented artists and find the perfect match for your next look.
                </p>

                <div className="artists-grid">
                    {artists.map((artist) => (
                        <article className="artist-card" key={artist.id}>
                            <img
                                className="artist-image"
                                src={artist.image}
                                alt={artist.name}
                            />

                            <div className="artist-info">
                                <div className="artist-heading">
                                    <h3>{artist.name}</h3>
                                    <span className="artist-rating">
                    ★ {artist.rating}
                  </span>
                                </div>

                                <p className="artist-location">
                                    {artist.location} · {artist.reviews} reviews
                                </p>

                                <p className="artist-specialty">
                                    {artist.specialty}
                                </p>

                                <Link
                                    className="artist-button"
                                    to={`/artists/${artist.id}`}
                                >
                                    View profile
                                </Link>
                            </div>
                        </article>
                    ))}
                </div>
            </section>
        </>
    )
}

export default HomePage