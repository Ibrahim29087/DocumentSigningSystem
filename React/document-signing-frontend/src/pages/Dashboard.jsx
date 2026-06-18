import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    Container, Box, Typography, Button,
    Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper, Chip, Alert
} from '@mui/material'
import Navbar from '../components/Navbar'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

const statusColors = {
    DRAFT: 'default',
    PENDING: 'warning',
    IN_PROGRESS: 'info',
    COMPLETED: 'success',
    EXPIRED: 'error',
    CANCELLED: 'error'
}

const truncateHash = (hash) => {
    if (!hash) return ''
    return hash.substring(0, 8) + '...' + hash.substring(hash.length - 4)
}

const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A'
    return new Date(dateStr).toLocaleString('en-PK', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    })
}

const Dashboard = () => {

    const [documents, setDocuments] = useState([])
    const [error, setError] = useState('')
    const { user } = useAuth()
    const navigate = useNavigate()

    useEffect(() => {
        fetchDocuments()
    }, [])

    const fetchDocuments = async () => {
        try {
            const response = await api.get('/api/documents/my')
            setDocuments(response.data)
        } catch (err) {
            setError('Failed to load documents')
        }
    }

    return (
        <>
            <Navbar />
            <Container maxWidth="lg" sx={{ mt: 4 }}>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                    <Typography variant="h5" fontWeight="bold">
                        My Documents
                    </Typography>
                    <Button
                        variant="contained"
                        onClick={() => navigate('/upload')}
                    >
                        Upload New Document
                    </Button>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {documents.length === 0 ? (
                    <Paper sx={{ p: 4, textAlign: 'center' }}>
                        <Typography color="text.secondary">
                            No documents yet. Upload your first document.
                        </Typography>
                    </Paper>
                ) : (
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><b>#</b></TableCell>
                                    <TableCell><b>Title</b></TableCell>
                                    <TableCell><b>File Name</b></TableCell>
                                    <TableCell><b>Hash</b></TableCell>
                                    <TableCell><b>Status</b></TableCell>
                                    <TableCell><b>Uploaded</b></TableCell>
                                    <TableCell><b>Actions</b></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {documents.map((doc, index) => (
                                    <TableRow key={doc.id} hover>
                                        <TableCell>{index + 1}</TableCell>
                                        <TableCell>{doc.title}</TableCell>
                                        <TableCell>{doc.fileName}</TableCell>
                                        <TableCell>
                                            <Typography
                                                variant="body2"
                                                title={doc.fileHash}
                                                sx={{ fontFamily: 'monospace', cursor: 'help' }}
                                            >
                                                {truncateHash(doc.fileHash)}
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Chip
                                                label={doc.status}
                                                color={statusColors[doc.status]}
                                                size="small"
                                            />
                                        </TableCell>
                                        <TableCell>{formatDate(doc.createdAt)}</TableCell>
                                        <TableCell>
                                            <Box sx={{ display: 'flex', gap: 1 }}>
                                                <Button
                                                    size="small"
                                                    variant="outlined"
                                                    onClick={() => navigate(`/signature-request/${doc.id}`)}
                                                >
                                                    Request Sign
                                                </Button>
                                                <Button
                                                    size="small"
                                                    variant="outlined"
                                                    color="info"
                                                    onClick={() => navigate(`/audit/${doc.id}`)}
                                                >
                                                    Audit Log
                                                </Button>
                                                <Button
                                                    size="small"
                                                    variant="outlined"
                                                    color="success"
                                                    onClick={() => navigate(`/verify/${doc.id}`)}
                                                >
                                                    Verify
                                                </Button>
                                            </Box>
                                        </TableCell>
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

export default Dashboard