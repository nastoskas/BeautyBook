import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import HomePage from './pages/HomePage'
import ArtistProfilePage from './pages/ArtistProfilePage'
import BookingPage from './pages/BookingPage.jsx'
import './App.css'

function App() {
    return (
        <BrowserRouter>
            <div className="app">
                <header className="navbar">
                    <Link className="brand" to="/">
                        BeautyBook
                    </Link>

                    <nav>
                        <a href="/#artists">Makeup Artists</a>
                        <a href="/#services">Services</a>
                        <a href="/#about">About Us</a>
                    </nav>

                    <button
                        className="login-button"
                        onClick={() => alert('Login — coming soon!')}
                    >
                        Log in
                    </button>
                </header>

                <main>
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route
                            path="/artists/:id"
                            element={<ArtistProfilePage />}
                        />
                        <Route
                            path="/artists/:id/book"
                            element={<BookingPage />}
                        />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    )
}

export default App