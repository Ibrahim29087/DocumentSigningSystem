import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
    Container, Box, TextField, Button,
    Typography, Paper, Alert
} from '@mui/material'
import api from '../api/axios'

const Register = () => {

    const [fullName, setFullName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)

    const navigate = useNavigate()

    const handleRegister = async () => {
        setError('')
        setSuccess('')
        setLoading(true)
        try {
            await api.post('/api/auth/register', { fullName, email, password })
            setSuccess('Registration successful! Redirecting to login...')
            setTimeout(() => navigate('/login'), 2000)
        } catch (err) {
            setError(err.response?.data?.error || 'Registration failed')
        } finally {
            setLoading(false)
        }
    }

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 10 }}>
                <Paper elevation={3} sx={{ p: 4 }}>

                    <Typography variant="h5" fontWeight="bold" mb={3} textAlign="center">
                        Document Signing System
                    </Typography>

                    <Typography variant="h6" mb={2}>
                        Register
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    <TextField
                        fullWidth
                        label="Full Name"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        sx={{ mb: 2 }}
                    />

                    <TextField
                        fullWidth
                        label="Email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        sx={{ mb: 2 }}
                    />

                    <TextField
                        fullWidth
                        label="Password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        sx={{ mb: 3 }}
                    />

                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleRegister}
                        disabled={loading}
                        sx={{ mb: 2 }}
                    >
                        {loading ? 'Registering...' : 'Register'}
                    </Button>

                    <Typography textAlign="center">
                        Already have an account?{' '}
                        <Link to="/login">Login here</Link>
                    </Typography>

                </Paper>
            </Box>
        </Container>
    )
}

export default Register