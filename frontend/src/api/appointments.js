
const API_BASE_URL = 'http://localhost:8080/api'

export async function createAppointment(appointmentData) {
    const response = await fetch(`${API_BASE_URL}/appointments`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(appointmentData),
    })

    if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText || 'Failed to create appointment.')
    }

    return response.json()
}