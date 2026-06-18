import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
    Container, Box, Typography, Button,
    Paper, Alert, TextField, Chip,
    List, ListItem, ListItemText, IconButton, Divider
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import Navbar from '../components/Navbar'
import api from '../api/axios'

const SignatureRequest = () => {

    const { documentId } = useParams()
    const navigate = useNavigate()

    const [document, setDocument] = useState(null)
    const [signerEmail, setSignerEmail] = useState('')
    const [signers, setSigners] = useState([])
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)
    const [searchResult, setSearchResult] = useState(null)

    useEffect(() => {
        fetchDocument()
    }, [])

    const fetchDocument = async () => {
        try {
            const response = await api.get(`/api/documents/${documentId}`)
            setDocument(response.data)
        } catch (err) {
            setError('Failed to load document')
        }
    }

    const searchUser = async () => {
        if (!signerEmail) { setError('Enter an email to search'); return }
        setError('')
        try {
            const response = await api.get(`/api/users/find?email=${signerEmail}`)
            setSearchResult(response.data)
        } catch (err) {
            setError('User not found with this email')
            setSearchResult(null)
        }
    }

    const addSigner = () => {
        if (!searchResult) return
        if (signers.find(s => s.userId === searchResult.userId)) {
            setError('This user is already added')
            return
        }
        setSigners([...signers, searchResult])
        setSearchResult(null)
        setSignerEmail('')
        setError('')
    }

    const removeSigner = (userId) => {
        setSigners(signers.filter(s => s.userId !== userId))
    }

    const handleSubmit = async () => {
        if (signers.length === 0) { setError('Add at least one signer'); return }
        setLoading(true)
        setError('')
        try {
            await api.post('/api/signature-requests', {
                documentId: documentId,
                signerUserIds: signers.map(s => s.userId),
                signingOrder: 1
            })
            setSuccess('Signature request created successfully!')
            setTimeout(() => navigate('/dashboard'), 1500)
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to create request')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <Navbar />
            <Container maxWidth="sm" sx={{ mt: 4 }}>
                <Paper elevation={3} sx={{ p: 4 }}>

                    <Typography variant="h5" fontWeight="bold" mb={1}>
                        Request Signatures
                    </Typography>

                    {document && (
                        <Box sx={{ mb: 3, p: 2, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                            <Typography variant="body2" color="text.secondary">Document</Typography>
                            <Typography fontWeight="bold">{document.title}</Typography>
                            <Typography variant="body2">{document.fileName}</Typography>
                        </Box>
                    )}

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    <Typography variant="subtitle1" fontWeight="bold" mb={1}>
                        Add Signers by Email
                    </Typography>

                    <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                        <TextField
                            fullWidth
                            label="Signer Email"
                            type="email"
                            value={signerEmail}
                            onChange={(e) => setSignerEmail(e.target.value)}
                            size="small"
                        />
                        <Button variant="outlined" onClick={searchUser}>
                            Search
                        </Button>
                    </Box>

                    {searchResult && (
                        <Box sx={{ p: 2, border: '1px solid #e0e0e0', borderRadius: 1, mb: 2 }}>
                            <Typography variant="body2" color="text.secondary">Found:</Typography>
                            <Typography fontWeight="bold">{searchResult.fullName}</Typography>
                            <Typography variant="body2">{searchResult.email}</Typography>
                            <Button
                                size="small"
                                variant="contained"
                                onClick={addSigner}
                                sx={{ mt: 1 }}
                            >
                                Add as Signer
                            </Button>
                        </Box>
                    )}

                    {signers.length > 0 && (
                        <Box sx={{ mb: 3 }}>
                            <Typography variant="subtitle2" mb={1}>
                                Signers ({signers.length})
                            </Typography>
                            <List dense>
                                {signers.map((signer, index) => (
                                    <Box key={signer.userId}>
                                        <ListItem
                                            secondaryAction={
                                                <IconButton onClick={() => removeSigner(signer.userId)}>
                                                    <DeleteIcon fontSize="small" />
                                                </IconButton>
                                            }
                                        >
                                            <ListItemText
                                                primary={signer.fullName}
                                                secondary={signer.email}
                                            />
                                        </ListItem>
                                        {index < signers.length - 1 && <Divider />}
                                    </Box>
                                ))}
                            </List>
                        </Box>
                    )}

                    <Button
                        fullWidth
                        variant="contained"
                        onClick={handleSubmit}
                        disabled={loading || signers.length === 0}
                        sx={{ mb: 1 }}
                    >
                        {loading ? 'Sending...' : 'Send Signature Request'}
                    </Button>

                    <Button
                        fullWidth
                        variant="text"
                        onClick={() => navigate('/dashboard')}
                    >
                        Back to Dashboard
                    </Button>

                </Paper>
            </Container>
        </>
    )
}

export default SignatureRequest