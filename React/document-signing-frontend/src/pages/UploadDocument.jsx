import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    Container, Box, Typography, Button,
    TextField, Paper, Alert, LinearProgress
} from '@mui/material'
import Navbar from '../components/Navbar'
import api from '../api/axios'

const UploadDocument = () => {

    const [title, setTitle] = useState('')
    const [file, setFile] = useState(null)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)

    const navigate = useNavigate()

    const handleFileChange = (e) => {
        const selected = e.target.files[0]
        if (selected && selected.type !== 'application/pdf') {
            setError('Only PDF files are allowed')
            setFile(null)
            return
        }
        setError('')
        setFile(selected)
    }

    const handleUpload = async () => {
        if (!title) { setError('Title is required'); return }
        if (!file) { setError('Please select a PDF file'); return }

        setError('')
        setLoading(true)

        try {
            const formData = new FormData()
            formData.append('file', file)
            formData.append('title', title)

            await api.post('/api/documents/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            })

            setSuccess('Document uploaded successfully!')
            setTimeout(() => navigate('/dashboard'), 1500)

        } catch (err) {
            setError(err.response?.data?.error || 'Upload failed')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <Navbar />
            <Container maxWidth="sm" sx={{ mt: 4 }}>
                <Paper elevation={3} sx={{ p: 4 }}>

                    <Typography variant="h5" fontWeight="bold" mb={3}>
                        Upload Document
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    <TextField
                        fullWidth
                        label="Document Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        sx={{ mb: 3 }}
                    />

                    <Box sx={{
                        border: '2px dashed #ccc',
                        borderRadius: 2,
                        p: 3,
                        textAlign: 'center',
                        mb: 3,
                        cursor: 'pointer'
                    }}>
                        <input
                            type="file"
                            accept=".pdf"
                            onChange={handleFileChange}
                            style={{ display: 'none' }}
                            id="file-input"
                        />
                        <label htmlFor="file-input">
                            <Button variant="outlined" component="span">
                                Choose PDF File
                            </Button>
                        </label>
                        {file && (
                            <Typography variant="body2" mt={1} color="text.secondary">
                                Selected: {file.name} ({(file.size / 1024).toFixed(1)} KB)
                            </Typography>
                        )}
                        {!file && (
                            <Typography variant="body2" mt={1} color="text.secondary">
                                Only PDF files accepted
                            </Typography>
                        )}
                    </Box>

                    {loading && <LinearProgress sx={{ mb: 2 }} />}

                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleUpload}
                        disabled={loading}
                    >
                        {loading ? 'Uploading...' : 'Upload Document'}
                    </Button>

                    <Button
                        fullWidth
                        variant="text"
                        onClick={() => navigate('/dashboard')}
                        sx={{ mt: 1 }}
                    >
                        Back to Dashboard
                    </Button>

                </Paper>
            </Container>
        </>
    )
}

export default UploadDocument