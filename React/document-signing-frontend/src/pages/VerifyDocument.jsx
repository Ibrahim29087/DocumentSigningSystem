import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
    Container, Typography, Paper, Button,
    Box, Alert, Chip, Divider, Table,
    TableBody, TableCell, TableContainer,
    TableHead, TableRow
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import Navbar from '../components/Navbar'
import api from '../api/axios'

const truncateHash = (hash) => {
    if (!hash) return 'N/A'
    return hash.substring(0, 12) + '...' + hash.substring(hash.length - 6)
}

const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A'
    return new Date(dateStr).toLocaleString('en-PK', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    })
}

const VerifyDocument = () => {

    const { documentId } = useParams()
    const navigate = useNavigate()

    const [file, setFile] = useState(null)
    const [result, setResult] = useState(null)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const handleFileChange = (e) => {
        const selected = e.target.files[0]
        if (selected && selected.type !== 'application/pdf') {
            setError('Only PDF files are allowed')
            setFile(null)
            return
        }
        setError('')
        setFile(selected)
        setResult(null)
    }

    const handleVerify = async () => {
        if (!file) { setError('Please select a PDF file to verify'); return }
        setLoading(true)
        setError('')
        try {
            const formData = new FormData()
            formData.append('file', file)
            const response = await api.post(
                `/api/signatures/verify/${documentId}`,
                formData,
                { headers: { 'Content-Type': 'multipart/form-data' } }
            )
            setResult(response.data)
        } catch (err) {
            setError(err.response?.data?.error || 'Verification failed')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <Navbar />
            <Container maxWidth="md" sx={{ mt: 4 }}>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                    <Typography variant="h5" fontWeight="bold">
                        Verify Document
                    </Typography>
                    <Button variant="outlined" onClick={() => navigate('/dashboard')}>
                        Back to Dashboard
                    </Button>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {/* Upload Section */}
                <Paper sx={{ p: 3, mb: 3 }}>
                    <Typography variant="subtitle1" fontWeight="bold" mb={1}>
                        Upload Document to Verify
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={2}>
                        Upload the PDF file to check if it has been tampered with after signing.
                    </Typography>

                    <Box sx={{
                        border: '2px dashed #ccc',
                        borderRadius: 2,
                        p: 3,
                        textAlign: 'center',
                        mb: 2
                    }}>
                        <input
                            type="file"
                            accept=".pdf"
                            onChange={handleFileChange}
                            style={{ display: 'none' }}
                            id="verify-file-input"
                        />
                        <label htmlFor="verify-file-input">
                            <Button variant="outlined" component="span">
                                Choose PDF File
                            </Button>
                        </label>
                        {file && (
                            <Typography variant="body2" mt={1} color="text.secondary">
                                Selected: {file.name} ({(file.size / 1024).toFixed(1)} KB)
                            </Typography>
                        )}
                    </Box>

                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleVerify}
                        disabled={loading || !file}
                    >
                        {loading ? 'Verifying...' : 'Verify Document'}
                    </Button>
                </Paper>

                {/* Verification Result */}
                {result && (
                    <Paper sx={{ p: 3 }}>

                        {/* Integrity Status */}
                        <Box sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 2,
                            p: 3,
                            borderRadius: 2,
                            mb: 3,
                            bgcolor: result.isIntact ? '#e8f5e9' : '#ffebee'
                        }}>
                            {result.isIntact
                                ? <CheckCircleIcon sx={{ fontSize: 48, color: 'success.main' }} />
                                : <WarningAmberIcon sx={{ fontSize: 48, color: 'error.main' }} />
                            }
                            <Box>
                                <Typography variant="h6" fontWeight="bold"
                                    color={result.isIntact ? 'success.main' : 'error.main'}>
                                    {result.isIntact
                                        ? 'Document is INTACT — Not Tampered'
                                        : 'WARNING — Document Has Been TAMPERED'}
                                </Typography>
                                <Typography variant="body2">
                                    {result.documentTitle} — {result.documentFileName}
                                </Typography>
                            </Box>
                        </Box>

                        {/* Hash Comparison */}
                        <Typography variant="subtitle1" fontWeight="bold" mb={2}>
                            Hash Comparison
                        </Typography>

                        <Box sx={{ p: 2, bgcolor: '#f5f5f5', borderRadius: 1, mb: 3 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                <Typography variant="body2" color="text.secondary">
                                    Stored Hash (original):
                                </Typography>
                                <Typography
                                    variant="body2"
                                    sx={{ fontFamily: 'monospace' }}
                                    title={result.storedHash}
                                >
                                    {truncateHash(result.storedHash)}
                                </Typography>
                            </Box>
                            <Divider sx={{ my: 1 }} />
                            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                <Typography variant="body2" color="text.secondary">
                                    Uploaded Hash (current):
                                </Typography>
                                <Typography
                                    variant="body2"
                                    sx={{
                                        fontFamily: 'monospace',
                                        color: result.isIntact ? 'success.main' : 'error.main'
                                    }}
                                    title={result.uploadedHash}
                                >
                                    {truncateHash(result.uploadedHash)}
                                </Typography>
                            </Box>
                        </Box>

                        {/* Signing Events */}
                        <Typography variant="subtitle1" fontWeight="bold" mb={2}>
                            Signing History
                        </Typography>

                        {result.signingEvents.length === 0 ? (
                            <Alert severity="info">
                                No signing events recorded for this document.
                            </Alert>
                        ) : (
                            <TableContainer>
                                <Table size="small">
                                    <TableHead>
                                        <TableRow sx={{ bgcolor: '#f5f5f5' }}>
                                            <TableCell><b>Event</b></TableCell>
                                            <TableCell><b>Signed By</b></TableCell>
                                            <TableCell><b>Signature</b></TableCell>
                                            <TableCell><b>IP</b></TableCell>
                                            <TableCell><b>Date</b></TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {result.signingEvents.map((event, index) => (
                                            <TableRow key={index} hover>
                                                <TableCell>
                                                    <Chip
                                                        label={event.eventType}
                                                        color={event.eventType === 'SIGNED' ? 'success' : 'error'}
                                                        size="small"
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <Typography fontWeight="bold" variant="body2">
                                                        ✅ {event.signedBy}
                                                    </Typography>
                                                </TableCell>
                                                <TableCell>
                                                    <Typography
                                                        variant="body2"
                                                        sx={{ fontFamily: 'monospace' }}
                                                        title={event.signatureHash}
                                                    >
                                                        {truncateHash(event.signatureHash)}
                                                    </Typography>
                                                </TableCell>
                                                <TableCell>{event.ipAddress || 'N/A'}</TableCell>
                                                <TableCell>{formatDate(event.createdAt)}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}

                    </Paper>
                )}

            </Container>
        </>
    )
}

export default VerifyDocument