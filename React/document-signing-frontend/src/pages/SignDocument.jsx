import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    Container, Typography, Paper, Button,
    Box, Alert, Chip, TextField, Divider
} from '@mui/material'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import Navbar from '../components/Navbar'
import api from '../api/axios'

const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A'
    return new Date(dateStr).toLocaleString('en-PK', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    })
}

const truncateHash = (hash) => {
    if (!hash) return 'N/A'
    return hash.substring(0, 8) + '...' + hash.substring(hash.length - 4)
}

const SignDocument = () => {

    const navigate = useNavigate()
    const [pendingSigners, setPendingSigners] = useState([])
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [loading, setLoading] = useState(false)
    const [privateKey, setPrivateKey] = useState('')
    const [showKeyInput, setShowKeyInput] = useState(false)
    const [selectedSigner, setSelectedSigner] = useState(null)
    const [hasKeys, setHasKeys] = useState(false)

    useEffect(() => {
        fetchPendingSignatures()
    }, [])

    const fetchPendingSignatures = async () => {
        try {
            const response = await api.get('/api/signatures/pending')
            setPendingSigners(response.data)
        } catch (err) {
            setError('Failed to load pending signatures')
        }
    }

    const generateKeys = async () => {
        setLoading(true)
        setError('')
        try {
            const response = await api.post('/api/users/generate-keys')
            const { privateKey: pk } = response.data
            setPrivateKey(pk)
            setHasKeys(true)
            alert('Keys generated! Your private key has been filled in. Save it safely — it will not be shown again.')
        } catch (err) {
            setError('Failed to generate keys')
        } finally {
            setLoading(false)
        }
    }

    const handleSign = async (signer) => {
        if (!privateKey) {
            setSelectedSigner(signer)
            setShowKeyInput(true)
            return
        }
        await signDocument(signer)
    }

    const handleView = async (documentId) => {
        try {
            const response = await api.get(`/api/documents/${documentId}/download`, {
                responseType: 'blob'
            })
            const blob = new Blob([response.data], { type: 'application/pdf' })
            const url = window.URL.createObjectURL(blob)
            window.open(url, '_blank')
        } catch (err) {
            setError('Failed to load document preview')
        }
    }

    const handleDecline = async (signer) => {
        if (!window.confirm(`Are you sure you want to decline signing "${signer.documentTitle}"?`)) {
            return
        }
        setLoading(true)
        setError('')
        try {
            await api.post(`/api/signatures/decline/${signer.signerId}`, null, {
                params: { remarks: 'Declined via Document Signing System' }
            })
            setSuccess(`Document "${signer.documentTitle}" declined.`)
            fetchPendingSignatures()
        } catch (err) {
            setError(err.response?.data?.error || 'Decline failed')
        } finally {
            setLoading(false)
        }
    }

    const signDocument = async (signer) => {
        setLoading(true)
        setError('')
        try {
            // Generate signature hash
            const sigResponse = await api.post('/api/signatures/generate-signature', {
                fileHash: signer.documentFileHash,
                privateKey: privateKey
            })

            // Sign the document
            await api.post('/api/signatures/sign', {
                signerId: signer.signerId,
                signatureHash: sigResponse.data.signatureHash,
                remarks: 'Signed via Document Signing System',
                ipAddress: '127.0.0.1'
            })

            setSuccess(`Document "${signer.documentTitle}" signed successfully!`)
            setShowKeyInput(false)
            setPrivateKey('')
            fetchPendingSignatures()

        } catch (err) {
            setError(err.response?.data?.error || 'Signing failed')
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
                        Pending Signatures
                    </Typography>
                    <Button variant="outlined" onClick={() => navigate('/dashboard')}>
                        Back to Dashboard
                    </Button>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                {/* Key Generation Box */}
                <Paper sx={{ p: 3, mb: 3, bgcolor: '#f0f7ff' }}>
                    <Typography variant="subtitle1" fontWeight="bold" mb={1}>
                        Your RSA Keys
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={2}>
                        You need RSA keys to sign documents. Generate them once and save your private key.
                    </Typography>
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={generateKeys}
                        disabled={loading}
                    >
                        Generate My RSA Keys
                    </Button>
                    {privateKey && (
                        <Box sx={{ mt: 2 }}>
                            <Alert severity="warning">
                                Save your private key — it will not be shown again after you leave this page.
                            </Alert>
                            <TextField
                                fullWidth
                                multiline
                                rows={3}
                                value={privateKey}
                                label="Your Private Key (save this)"
                                sx={{ mt: 1 }}
                                InputProps={{ readOnly: true }}
                            />
                        </Box>
                    )}
                </Paper>

                {/* Pending Documents */}
                {pendingSigners.length === 0 ? (
                    <Paper sx={{ p: 4, textAlign: 'center' }}>
                        <CheckCircleIcon sx={{ fontSize: 48, color: 'success.main', mb: 1 }} />
                        <Typography variant="h6">No Pending Signatures</Typography>
                        <Typography color="text.secondary">
                            You have no documents waiting for your signature.
                        </Typography>
                    </Paper>
                ) : (
                    pendingSigners.map((signer) => (
                        <Paper key={signer.signerId} sx={{ p: 3, mb: 2 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                <Box>
                                    <Typography variant="h6" fontWeight="bold">
                                        {signer.documentTitle}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {signer.documentFileName}
                                    </Typography>
                                    <Typography variant="body2" mt={1}>
                                        Requested by: <b>{signer.requestedBy}</b>
                                    </Typography>
                                    <Typography variant="body2">
                                        File Hash: <code>{truncateHash(signer.documentFileHash)}</code>
                                    </Typography>
                                    <Typography variant="body2">
                                        Requested on: {formatDate(signer.createdAt)}
                                    </Typography>
                                </Box>
                                <Chip label="PENDING" color="warning" />
                            </Box>

                            <Divider sx={{ my: 2 }} />

                            {showKeyInput && selectedSigner?.signerId === signer.signerId ? (
                                <Box>
                                    <TextField
                                        fullWidth
                                        multiline
                                        rows={3}
                                        label="Paste Your Private Key"
                                        value={privateKey}
                                        onChange={(e) => setPrivateKey(e.target.value)}
                                        sx={{ mb: 2 }}
                                    />
                                    <Box sx={{ display: 'flex', gap: 1 }}>
                                        <Button
                                            variant="contained"
                                            color="success"
                                            onClick={() => signDocument(signer)}
                                            disabled={loading || !privateKey}
                                        >
                                            {loading ? 'Signing...' : 'Confirm & Sign'}
                                        </Button>
                                        <Button
                                            variant="outlined"
                                            onClick={() => { setShowKeyInput(false); setSelectedSigner(null) }}
                                        >
                                            Cancel
                                        </Button>
                                    </Box>
                                </Box>
                            ) : (
                                <Box sx={{ display: 'flex', gap: 1 }}>
                                    <Button
                                        variant="outlined"
                                        onClick={() => handleView(signer.documentId)}
                                    >
                                        View Document
                                    </Button>
                                    <Button
                                        variant="contained"
                                        color="success"
                                        onClick={() => handleSign(signer)}
                                        disabled={loading}
                                    >
                                        Sign Document
                                    </Button>
                                    <Button
                                        variant="outlined"
                                        color="error"
                                        onClick={() => handleDecline(signer)}
                                        disabled={loading}
                                    >
                                        Decline
                                    </Button>
                                </Box>
                            )}
                        </Paper>
                    ))
                )}
            </Container>
        </>
    )
}

export default SignDocument