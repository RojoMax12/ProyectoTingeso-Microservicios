import { useState, useEffect } from 'react';
import { 
    Box, Typography, Paper, IconButton, Drawer, List, ListItem, ListItemButton, 
    ListItemIcon, ListItemText, Button, TableContainer, Table, TableHead, 
    TableRow, TableCell, TableBody, Chip, Avatar, TextField, InputAdornment,
    Alert, Dialog, DialogTitle, DialogContent, DialogActions
} from "@mui/material";
import { useNavigate } from 'react-router-dom';
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import SearchIcon from "@mui/icons-material/Search";
import PersonIcon from "@mui/icons-material/Person";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import BadgeIcon from "@mui/icons-material/Badge";
import ClientServices from '../Services/ClientServices';
import { useKeycloak } from '@react-keycloak/web';
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';

const ClientList = () => {
    const [clients, setClients] = useState([]);
    const [filteredClients, setFilteredClients] = useState([]);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [deleteDialog, setDeleteDialog] = useState({ open: false, client: null });
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();
    const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");

    const sidebarOptions = [
        { text: "Inicio", icon: <HomeIcon />, path: "/" },
        { text: "Herramientas", icon: <BuildIcon />, path: "/ToolList" },
        { text: "Agregar Herramienta", icon: <LibraryAddIcon />, path: "/AddTools" },
        { text: "Ver Kardex", icon: <AssessmentIcon />, path: "/Kardex" },
        { text: "Registrar Cliente", icon: <PersonAddAltIcon />, path: "/RegisterClient" },
        { text: "Clientes", icon: <ContactsIcon />, path: "/ClientList" },
        { text: "Reportes", icon: <ReportIcon />, path: "/Reports" },
        ...(isAdmin ? [{ text: "Configuraciones", icon: <AdminPanelSettingsIcon />, path: "/Configuration" }] : [])
    ];

    useEffect(() => {
        fetchClients();
    }, []);

    useEffect(() => {
        const filtered = clients.filter(client =>
            client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            client.rut.toLowerCase().includes(searchTerm.toLowerCase()) ||
            client.email.toLowerCase().includes(searchTerm.toLowerCase())
        );
        setFilteredClients(filtered);
    }, [clients, searchTerm]);

    //Obtiene a todos los clientes
    const fetchClients = async () => {
        setLoading(true);
        try {
            const response = await ClientServices.getAll();
            setClients(response.data);
        } catch (error) {
            console.error("Error fetching clients:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (id) => {
        navigate(`/EditClient/${id}`);
    };

    const handleDeleteClick = (client) => {
        setDeleteDialog({ open: true, client });
    };

    const handleDeleteConfirm = async () => {
        try {
            await ClientServices.deleteClient(deleteDialog.client.id);
            setClients(clients.filter(client => client.id !== deleteDialog.client.id));
            setDeleteDialog({ open: false, client: null });
        } catch (error) {
            console.error("Error deleting client:", error);
        }
    };

    const getInitials = (name) => {
        return name
            .split(' ')
            .map(word => word[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const getStatusColor = (state) => {
        switch (state) {
            case 1: return { color: '#4caf50', bg: '#e8f5e8', text: 'Activo' };
            case 2: return { color: '#f44336', bg: '#ffebee', text: 'Inactivo' };
            default: return { color: '#757575', bg: '#f5f5f5', text: 'Desconocido' };
        }
    };

    return (
        <>
            {/* Botón para abrir la barra lateral */}
            <IconButton
                color="inherit"
                onClick={() => setDrawerOpen(true)}
                sx={{ 
                    position: "fixed", 
                    top: 16, 
                    left: 16, 
                    zIndex: 10, 
                    backgroundColor: "#FA812F", 
                    boxShadow: 3,
                    '&:hover': { backgroundColor: "#FA812F" }
                }}
            >
                <MenuIcon sx={{ color: "#FEF3E2" }} />
            </IconButton>

            {/* Barra lateral temporal */}
            <Drawer
                open={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                variant="temporary"
                sx={{
                    [`& .MuiDrawer-paper`]: { 
                        width: 240, 
                        boxSizing: "border-box", 
                        backgroundColor: "#FEF3E2" 
                    }
                }}
            >
                <List>
                    {sidebarOptions.map((option) => (
                        <ListItem key={option.text} disablePadding>
                            <ListItemButton 
                                onClick={() => { 
                                    navigate(option.path); 
                                    setDrawerOpen(false); 
                                }}
                                sx={{
                                    '&:hover': {
                                        backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                    },
                                    borderRadius: 1,
                                    mx: 1,
                                    my: 0.5
                                }}
                            >
                                <ListItemIcon sx={{ color: "#FA812F", minWidth: 40 }}>
                                    {option.icon}
                                </ListItemIcon>
                                <ListItemText 
                                    primary={option.text}
                                    sx={{
                                        '& .MuiListItemText-primary': {
                                            fontWeight: 500,
                                            fontSize: '0.95rem'
                                        }
                                    }}
                                />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Drawer>

            {/* Fondo que cubre toda la pantalla */}
            <Box
                sx={{
                    position: "fixed",
                    top: 0,
                    left: 0,
                    width: "100vw",
                    height: "100vh",
                    backgroundColor: "#FEF3E2",
                    zIndex: -1
                }}
            />

            {/* Contenido principal */}
            <Box sx={{
                minHeight: "100vh",
                backgroundColor: "#FEF3E2",
                py: 4,
                display: "flex",
                alignItems: "center",
                pt: 8
            }}>
                <Box
                    sx={{
                        maxWidth: 1200,
                        mx: "auto",
                        mt: 4,
                        backgroundColor: "#fff8f0",
                        borderRadius: 4,
                        boxShadow: 6,
                        p: 4,
                        border: "2px solid rgba(255, 94, 0, 0.2)",
                        width: '100%'
                    }}
                >
                    <Typography 
                        variant="h4" 
                        align="center" 
                        gutterBottom 
                        sx={{ 
                            fontWeight: "bold", 
                            mb: 4, 
                            color: "rgba(255, 94, 0, 1)" 
                        }}
                    >
                        👥 Lista de Clientes
                    </Typography>

                    {/* Barra de búsqueda y botón agregar */}
                    <Box sx={{ 
                        display: "flex", 
                        justifyContent: "space-between", 
                        alignItems: "center", 
                        mb: 3,
                        flexWrap: "wrap",
                        gap: 2
                    }}>
                        <TextField
                            placeholder="Buscar por nombre, RUT o email..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            sx={{ 
                                minWidth: 300,
                                '& .MuiOutlinedInput-root': {
                                    '&:hover fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 0.5)',
                                    },
                                    '&.Mui-focused fieldset': {
                                        borderColor: 'rgba(255, 94, 0, 1)',
                                    },
                                },
                            }}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon sx={{ color: "rgba(255, 94, 0, 0.7)" }} />
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={() => navigate("/RegisterClient")}
                            sx={{
                                backgroundColor: "rgba(255, 94, 0, 1)",
                                borderRadius: 3,
                                px: 3,
                                '&:hover': {
                                    backgroundColor: "rgba(255, 94, 0, 0.8)"
                                }
                            }}
                        >
                            Agregar Cliente
                        </Button>
                    </Box>

                    {/* Estadísticas rápidas */}
                    <Box sx={{ 
                        display: "flex", 
                        gap: 2, 
                        mb: 3,
                        flexWrap: "wrap"
                    }}>
                        <Paper sx={{ 
                            p: 2, 
                            flex: 1, 
                            minWidth: 200,
                            backgroundColor: 'rgba(255, 94, 0, 0.05)',
                            border: '1px solid rgba(255, 94, 0, 0.1)'
                        }}>
                            <Typography variant="h6" sx={{ color: 'rgba(255, 94, 0, 1)', fontWeight: 'bold' }}>
                                {clients.length}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Total Clientes
                            </Typography>
                        </Paper>
                        <Paper sx={{ 
                            p: 2, 
                            flex: 1, 
                            minWidth: 200,
                            backgroundColor: 'rgba(76, 175, 80, 0.05)',
                            border: '1px solid rgba(76, 175, 80, 0.1)'
                        }}>
                            <Typography variant="h6" sx={{ color: '#4caf50', fontWeight: 'bold' }}>
                                {clients.filter(c => c.state === 1).length}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Clientes Activos
                            </Typography>
                        </Paper>
                        <Paper sx={{ 
                            p: 2, 
                            flex: 1, 
                            minWidth: 200,
                            backgroundColor: 'rgba(33, 150, 243, 0.05)',
                            border: '1px solid rgba(33, 150, 243, 0.1)'
                        }}>
                            <Typography variant="h6" sx={{ color: '#2196f3', fontWeight: 'bold' }}>
                                {filteredClients.length}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Resultados Filtrados
                            </Typography>
                        </Paper>
                    </Box>

                    {/* Tabla de clientes */}
                    {loading ? (
                        <Box sx={{ textAlign: 'center', py: 4 }}>
                            <Typography>⏳ Cargando clientes...</Typography>
                        </Box>
                    ) : filteredClients.length === 0 ? (
                        <Paper sx={{ p: 4, textAlign: 'center' }}>
                            <PersonIcon sx={{ fontSize: 64, color: 'rgba(255, 94, 0, 0.3)', mb: 2 }} />
                            <Typography variant="h6" color="text.secondary" gutterBottom>
                                {searchTerm ? 'No se encontraron clientes' : 'No hay clientes registrados'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                {searchTerm ? 'Intenta con otros términos de búsqueda' : 'Comienza agregando tu primer cliente'}
                            </Typography>
                            <Button
                                variant="contained"
                                startIcon={<AddIcon />}
                                onClick={() => navigate("/RegisterClient")}
                                sx={{
                                    backgroundColor: "rgba(255, 94, 0, 1)",
                                    '&:hover': {
                                        backgroundColor: "rgba(255, 94, 0, 0.8)"
                                    }
                                }}
                            >
                                Agregar Primer Cliente
                            </Button>
                        </Paper>
                    ) : (
                        <TableContainer component={Paper} sx={{ boxShadow: 3, borderRadius: 2 }}>
                            <Table>
                                <TableHead>
                                    <TableRow sx={{ backgroundColor: "rgba(255, 94, 0, 1)" }}>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Cliente</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>RUT</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Contacto</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }}>Estado</TableCell>
                                        <TableCell sx={{ color: "#fff", fontWeight: 'bold' }} align="center">Acciones</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {filteredClients.map((client) => {
                                        const status = getStatusColor(client.state);
                                        return (
                                            <TableRow 
                                                key={client.id}
                                                sx={{ 
                                                    '&:hover': { 
                                                        backgroundColor: 'rgba(255, 94, 0, 0.03)' 
                                                    }
                                                }}
                                            >
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                                        <Avatar 
                                                            sx={{ 
                                                                backgroundColor: 'rgba(255, 94, 0, 0.1)',
                                                                color: 'rgba(255, 94, 0, 1)',
                                                                width: 40,
                                                                height: 40,
                                                                fontSize: '0.9rem',
                                                                fontWeight: 'bold'
                                                            }}
                                                        >
                                                            {getInitials(client.name)}
                                                        </Avatar>
                                                        <Box>
                                                            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                                                                {client.name}
                                                            </Typography>
                                                            <Typography variant="caption" color="text.secondary">
                                                                ID: {client.id}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                        <BadgeIcon sx={{ color: 'rgba(255, 94, 0, 0.7)', fontSize: 16 }} />
                                                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                                            {client.rut}
                                                        </Typography>
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                            <EmailIcon sx={{ color: 'rgba(255, 94, 0, 0.7)', fontSize: 14 }} />
                                                            <Typography variant="body2">
                                                                {client.email}
                                                            </Typography>
                                                        </Box>
                                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                            <PhoneIcon sx={{ color: 'rgba(255, 94, 0, 0.7)', fontSize: 14 }} />
                                                            <Typography variant="body2">
                                                                {client.phone}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </TableCell>
                                                <TableCell>
                                                    <Chip
                                                        label={status.text}
                                                        size="small"
                                                        sx={{
                                                            backgroundColor: status.bg,
                                                            color: status.color,
                                                            fontWeight: 'bold'
                                                        }}
                                                    />
                                                </TableCell>
                                                <TableCell align="center">
                                                    <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                                                        <IconButton
                                                            size="small"
                                                            onClick={() => handleEdit(client.id)}
                                                            sx={{ 
                                                                color: '#2196f3',
                                                                '&:hover': { backgroundColor: 'rgba(33, 150, 243, 0.1)' }
                                                            }}
                                                        >
                                                            <EditIcon fontSize="small" />
                                                        </IconButton>
                                                        {isAdmin && (
                                                            <IconButton
                                                                size="small"
                                                                onClick={() => handleDeleteClick(client)}
                                                                sx={{ 
                                                                    color: '#f44336',
                                                                    '&:hover': { backgroundColor: 'rgba(244, 67, 54, 0.1)' }
                                                                }}
                                                            >
                                                                <DeleteIcon fontSize="small" />
                                                            </IconButton>
                                                        )}
                                                    </Box>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </Box>
            </Box>

            {/* Dialog de confirmación de eliminación */}
            <Dialog
                open={deleteDialog.open}
                onClose={() => setDeleteDialog({ open: false, client: null })}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle sx={{ 
                    backgroundColor: 'rgba(244, 67, 54, 0.1)',
                    color: '#f44336',
                    fontWeight: 'bold'
                }}>
                    🗑️ Confirmar Eliminación
                </DialogTitle>
                <DialogContent sx={{ pt: 2 }}>
                    <Alert severity="warning" sx={{ mb: 2 }}>
                        Esta acción no se puede deshacer
                    </Alert>
                    <Typography>
                        ¿Estás seguro de que deseas eliminar al cliente{' '}
                        <strong>{deleteDialog.client?.name}</strong>?
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        RUT: {deleteDialog.client?.rut}
                    </Typography>
                </DialogContent>
                <DialogActions sx={{ p: 2 }}>
                    <Button 
                        onClick={() => setDeleteDialog({ open: false, client: null })}
                        variant="outlined"
                    >
                        Cancelar
                    </Button>
                    <Button 
                        onClick={handleDeleteConfirm}
                        variant="contained"
                        color="error"
                        startIcon={<DeleteIcon />}
                    >
                        Eliminar
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default ClientList;