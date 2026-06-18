import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
    Container, Typography, Paper, Button,
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Alert, Chip, Box
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import Navbar from '../components/Navbar'
import api from '../api/axios'

const truncateHash = (hash) => {
    if (!hash) return 'N/A'
    return hash.substring(0, 8) + '...' + hash.substring(hash.length - 4)
}

const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A'
    return new Date(dateStr).toLocaleString('en-PK', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    })
}

const AuditLog = () => {

    const { documentId } = useParams()
    const navigate = useNavigate()

    const [events, setEvents] = useState([])
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        fetchAuditLog()
    }, [])

    const fetchAuditLog = async () => {
        try {
            const response = await api.get(`/api/signatures/audit/${documentId}`)
            setEvents(response.data)
        } catch (err) {
            setError('Failed to load audit log')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <Navbar />
            <Container maxWidth="lg" sx={{ mt: 4 }}>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                    <Typography variant="h5" fontWeight="bold">
                        Audit Log
                    </Typography>
                    <Button variant="outlined" onClick={() => navigate('/dashboard')}>
                        Back to Dashboard
                    </Button>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {events.length === 0 && !loading ? (
                    <Paper sx={{ p: 4, textAlign: 'center' }}>
                        <Typography color="text.secondary">
                            No signing events yet for this document.
                        </Typography>
                    </Paper>
                ) : (
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><b>#</b></TableCell>
                                    <TableCell><b>Event</b></TableCell>
                                    <TableCell><b>Signed By</b></TableCell>
                                    <TableCell><b>Signature Hash</b></TableCell>
                                    <TableCell><b>IP Address</b></TableCell>
                                    <TableCell><b>Remarks</b></TableCell>
                                    <TableCell><b>Date & Time</b></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {events.map((event, index) => (
                                    <TableRow key={event.id} hover>
                                        <TableCell>{index + 1}</TableCell>
                                        <TableCell>
                                            <Chip
                                                icon={event.eventType === 'SIGNED'
                                                    ? <CheckCircleIcon />
                                                    : <CancelIcon />}
                                                label={event.eventType}
                                                color={event.eventType === 'SIGNED' ? 'success' : 'error'}
                                                size="small"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <Typography fontWeight="bold" variant="body2">
                                                {event.userFullName}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography
                                                variant="body2"
                                                title={event.signatureHash}
                                                sx={{ fontFamily: 'monospace', cursor: 'help' }}
                                            >
                                                {truncateHash(event.signatureHash)}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>{event.ipAddress || 'N/A'}</TableCell>
                                        <TableCell>{event.remarks || 'N/A'}</TableCell>
                                        <TableCell>{formatDate(event.createdAt)}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}

            </Container>
        </>
    )
}

export default AuditLog