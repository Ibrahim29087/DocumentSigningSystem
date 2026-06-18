import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const Navbar = () => {

    const { user, logout } = useAuth()
    const navigate = useNavigate()

    const handleLogout = () => {
        logout()
        navigate('/login')
    }

    return (
        <AppBar position="static">
            <Toolbar>

                <Typography
                    variant="h6"
                    fontWeight="bold"
                    sx={{ flexGrow: 1, cursor: 'pointer' }}
                    onClick={() => navigate('/dashboard')}
                >
                    Document Signing System
                </Typography>

                {user && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>

                        <Typography variant="body1">
                            Hello, {user.fullName}
                        </Typography>

                        <Button
                            color="inherit"
                            onClick={() => navigate('/dashboard')}
                        >
                            Dashboard
                        </Button>

                        <Button
                            color="inherit"
                            onClick={() => navigate('/upload')}
                        >
                            Upload
                        </Button>

                        <Button
                            color="inherit"
                            onClick={handleLogout}
                        >
                            Logout
                        </Button>

                        <Button
                            color="inherit"
                            onClick={() => navigate('/sign')}
                        >
                            Sign Documents
                        </Button>

                    </Box>
                )}

            </Toolbar>
        </AppBar>
    )
}

export default Navbar