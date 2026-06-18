import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
    Container, Box, TextField, Button,
    Typography, Paper, Alert
} from '@mui/material'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

const Login = () => {

    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const { login } = useAuth()
    const navigate = useNavigate()

    const handleLogin = async () => {
        setError('')
        setLoading(true)
        try {
            const response = await api.post('/api/auth/login', { email, password })
            const { token, userId, fullName, email: userEmail, role } = response.data
            login({ userId, fullName, email: userEmail, role }, token)
            navigate('/dashboard')
        } catch (err) {
            setError(err.response?.data?.error || 'Invalid email or password')
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
                        Login
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

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
                        onClick={handleLogin}
                        disabled={loading}
                        sx={{ mb: 2 }}
                    >
                        {loading ? 'Logging in...' : 'Login'}
                    </Button>

                    <Typography textAlign="center">
                        Don't have an account?{' '}
                        <Link to="/register">Register here</Link>
                    </Typography>

                </Paper>
            </Box>
        </Container>
    )
}

export default Login