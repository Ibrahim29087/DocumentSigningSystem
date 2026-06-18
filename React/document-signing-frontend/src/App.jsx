import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import UploadDocument from './pages/UploadDocument'
import SignatureRequest from './pages/SignatureRequest'
import AuditLog from './pages/AuditLog'
import SignDocument from './pages/SignDocument'
import VerifyDocument from './pages/VerifyDocument'

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/upload" element={<UploadDocument />} />
                <Route path="/signature-request/:documentId" element={<SignatureRequest />} />
                <Route path="/audit/:documentId" element={<AuditLog />} />
                <Route path="/sign" element={<SignDocument />} />
                <Route path="/verify/:documentId" element={<VerifyDocument />} />
            </Routes>
        </Router>
    )
}

export default App