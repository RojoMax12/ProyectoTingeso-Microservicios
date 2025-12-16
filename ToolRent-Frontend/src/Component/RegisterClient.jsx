import { useState } from 'react';
import { TextField, Button, Box, Typography, Paper, Stack, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton, Alert, InputAdornment } from "@mui/material";
import { useNavigate } from 'react-router-dom';
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import PersonIcon from "@mui/icons-material/Person";
import BadgeIcon from "@mui/icons-material/Badge";
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import ClientServices from '../Services/ClientServices';  
import { useKeycloak } from '@react-keycloak/web'; 
import LibraryAddIcon from "@mui/icons-material/LibraryAdd";
import AssessmentIcon from "@mui/icons-material/Assessment";
import ContactsIcon from "@mui/icons-material/Contacts";
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import ReportIcon from '@mui/icons-material/Report';

const RegisterClient = () => {
    const navigate = useNavigate();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [formData, setFormData] = useState({
        name: "",
        rut: "",
        email: "",
        phone: ""
    });
    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

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

    // SOLO formatear RUT con puntos y guión - SIN validación
    const formatRut = (value) => {
        if (!value) return "";
        
        // Remover caracteres no numéricos excepto 'k' o 'K'
        let cleaned = value.replace(/[^0-9kK]/g, '');
        
        // Si es muy corto, devolver tal como está
        if (cleaned.length <= 1) return cleaned;
        
        // Separar cuerpo y dígito verificador
        const body = cleaned.slice(0, -1);
        const dv = cleaned.slice(-1).toUpperCase();
        
        // Formatear según la longitud del cuerpo
        let formattedBody = body;
        
        if (body.length >= 7) {
            // Para 7 dígitos: X.XXX.XXX
            if (body.length === 7) {
                formattedBody = `${body.slice(0, 1)}.${body.slice(1, 4)}.${body.slice(4)}`;
            }
            // Para 8 dígitos: XX.XXX.XXX
            else if (body.length === 8) {
                formattedBody = `${body.slice(0, 2)}.${body.slice(2, 5)}.${body.slice(5)}`;
            }
        } else if (body.length >= 4) {
            // Para RUTs más cortos: XXX.XXX
            formattedBody = `${body.slice(0, -3)}.${body.slice(-3)}`;
        }
        
        return formattedBody + (dv ? `-${dv}` : '');
    };

    // Formatear número de teléfono
    const formatPhone = (value) => {
        // Si el valor ya tiene +569, extraer solo los dígitos después
        if (value.startsWith('+569')) {
            const digits = value.slice(4).replace(/\D/g, '').slice(0, 8);
            return digits ? `+569${digits}` : '+569';
        }
        
        // Si no tiene +569, extraer dígitos y agregar prefijo
        const digits = value.replace(/\D/g, '').slice(0, 8);
        return digits ? `+569${digits}` : '';
    };

    // Validar email
    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    // CORREGIDO: Validar formulario completo - SIN validación de RUT
    const validateForm = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = "El nombre es requerido";
        } else if (formData.name.trim().length < 2) {
            newErrors.name = "El nombre debe tener al menos 2 caracteres";
        }

        // MODIFICADO: Solo validar que el RUT tenga formato básico
        if (!formData.rut.trim()) {
            newErrors.rut = "El RUT es requerido";
        } else if (formData.rut.length < 9) {
            newErrors.rut = "El RUT debe tener al menos 8 caracteres más el dígito verificador";
        }

        if (!formData.email.trim()) {
            newErrors.email = "El email es requerido";
        } else if (!validateEmail(formData.email)) {
            newErrors.email = "El email ingresado no es válido";
        }

        if (!formData.phone.trim()) {
            newErrors.phone = "El teléfono es requerido";
        } else {
            const phoneDigits = formData.phone.replace(/^\+569/, '').replace(/\D/g, '');
            if (phoneDigits.length !== 8) {
                newErrors.phone = "El número de teléfono debe tener exactamente 8 dígitos";
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleInputChange = (field, value) => {
        let processedValue = value;
        
        if (field === 'rut') {
            processedValue = formatRut(value);
        } else if (field === 'phone') {
            if (value === '') {
                processedValue = '';
            } else {
                processedValue = formatPhone(value);
            }
        }
        
        setFormData(prev => ({
            ...prev,
            [field]: processedValue
        }));

        // Limpiar error específico cuando el usuario empiece a corregir
        if (errors[field]) {
            setErrors(prev => ({
                ...prev,
                [field]: ""
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);
        setSuccessMessage("");
        setErrors({});

        try {
            const newClient = {
                name: formData.name.trim(),
                rut: formData.rut,
                email: formData.email.trim().toLowerCase(),
                phone: formData.phone
            };

            // AGREGADO: Console logs para debugging
            console.log("📤 Enviando cliente:", newClient);

            const response = await ClientServices.create(newClient);
            
            console.log("✅ Respuesta del servidor:", response);
            
            setSuccessMessage("✅ Cliente registrado exitosamente!");
            alert("Cliente registrado correctamente")
            
            
            setTimeout(() => {
                setFormData({
                    name: "",
                    rut: "",
                    email: "",
                    phone: ""
                });
                setSuccessMessage("");
            }, 2000);
            
        } catch (error) {
            console.error("❌ Error completo:", error);
            console.error("📊 Status:", error.response?.status);
            console.error("📨 Data:", error.response?.data);
            
            // Manejo mejorado de errores
            let errorMessage = "";
            
            if (error.response?.data) {
                if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                } else if (error.response.data.message && typeof error.response.data.message === 'string') {
                    errorMessage = error.response.data.message;
                } else {
                    errorMessage = JSON.stringify(error.response.data);
                }
            } else if (error.message) {
                errorMessage = error.message;
            } else {
                errorMessage = "Error de conexión desconocido";
            }
            
            console.error("🔤 Error message processed:", errorMessage);
            
            // Manejo específico por tipo de error
            if (errorMessage.includes("RUT_DUPLICATED") || errorMessage.toLowerCase().includes('rut')) {
                setErrors({
                    rut: "❌ Este RUT ya está registrado. Verifica el número ingresado."
                });
            } else if (errorMessage.includes("EMAIL_DUPLICATED") || errorMessage.toLowerCase().includes('email')) {
                setErrors({
                    email: "❌ Este email ya está registrado. Usa otro correo electrónico."
                });
            } else if (error.response?.status === 400) {
                setErrors({
                    submit: "❌ Datos inválidos. Verifica que todos los campos estén correctos."
                });
            } else if (error.response?.status === 500) {
                setErrors({
                    submit: "❌ Error interno del servidor. Intenta nuevamente."
                });
            } else if (!error.response) {
                setErrors({
                    submit: "❌ Error de conexión. Verifica tu internet y que el servidor esté funcionando."
                });
            } else {
                setErrors({
                    submit: `❌ Error al registrar el cliente: ${errorMessage}`
                });
            }
        } finally {
            setIsSubmitting(false);
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

            {/* Contenedor principal */}
            <Box sx={{ 
                minHeight: "100vh", 
                backgroundColor: "#FEF3E2", 
                py: 4, 
                display: "flex", 
                alignItems: "center",
                pt: 8
            }}>
                <Paper
                    sx={{
                        maxWidth: 600,
                        mx: "auto",
                        p: 4,
                        borderRadius: 4,
                        boxShadow: "0 8px 32px rgba(255, 94, 0, 0.1)",
                        border: "2px solid rgba(255, 94, 0, 0.1)",
                        backgroundColor: "#fff"
                    }}
                >
                    <Typography 
                        variant="h4" 
                        align="center" 
                        gutterBottom 
                        sx={{ 
                            fontWeight: "bold", 
                            color: "rgba(255, 94, 0, 1)",
                            mb: 3
                        }}
                    >
                        👤 Registrar Cliente
                    </Typography>

                    {/* Mensaje de éxito */}
                    {successMessage && (
                        <Alert 
                            severity="success" 
                            sx={{ mb: 3, borderRadius: 2 }}
                        >
                            {successMessage}
                        </Alert>
                    )}

                    {/* Error general */}
                    {errors.submit && (
                        <Alert 
                            severity="error" 
                            sx={{ mb: 3, borderRadius: 2 }}
                        >
                            {errors.submit}
                        </Alert>
                    )}

                    <Box component="form" onSubmit={handleSubmit}>
                        <Stack spacing={3}>
                            {/* Campo Nombre */}
                            <TextField
                                label="Nombre Completo"
                                value={formData.name}
                                onChange={(e) => handleInputChange('name', e.target.value)}
                                fullWidth
                                required
                                error={!!errors.name}
                                helperText={errors.name}
                                placeholder="Ej: Juan Pérez González"
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <PersonIcon sx={{ color: "rgba(255, 94, 0, 0.7)" }} />
                                        </InputAdornment>
                                    ),
                                }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        '&:hover fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 0.5)',
                                        },
                                        '&.Mui-focused fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 1)',
                                        },
                                    },
                                    '& .MuiInputLabel-root.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            />

                            {/* Campo RUT - MODIFICADO */}
                            <TextField
                                label="RUT"
                                value={formData.rut}
                                onChange={(e) => handleInputChange('rut', e.target.value)}
                                fullWidth
                                required
                                error={!!errors.rut}
                                helperText={errors.rut || "Formato: 12.345.678-9 (cualquier RUT)"}
                                placeholder="12.345.678-9"
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <BadgeIcon sx={{ color: "rgba(255, 94, 0, 0.7)" }} />
                                        </InputAdornment>
                                    ),
                                }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        '&:hover fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 0.5)',
                                        },
                                        '&.Mui-focused fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 1)',
                                        },
                                    },
                                    '& .MuiInputLabel-root.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            />

                            {/* Campo Email */}
                            <TextField
                                label="Correo Electrónico"
                                type="email"
                                value={formData.email}
                                onChange={(e) => handleInputChange('email', e.target.value)}
                                fullWidth
                                required
                                error={!!errors.email}
                                helperText={errors.email}
                                placeholder="ejemplo@correo.com"
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <EmailIcon sx={{ color: "rgba(255, 94, 0, 0.7)" }} />
                                        </InputAdornment>
                                    ),
                                }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        '&:hover fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 0.5)',
                                        },
                                        '&.Mui-focused fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 1)',
                                        },
                                    },
                                    '& .MuiInputLabel-root.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            />

                            {/* Campo Teléfono */}
                            <TextField
                                label="Número de Teléfono"
                                value={formData.phone}
                                onChange={(e) => handleInputChange('phone', e.target.value)}
                                fullWidth
                                required
                                error={!!errors.phone}
                                helperText={errors.phone || "Formato: +56912345678 (se agregará +569 automáticamente)"}
                                placeholder="87654321"
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <PhoneIcon sx={{ color: "rgba(255, 94, 0, 0.7)" }} />
                                        </InputAdornment>
                                    ),
                                }}
                                sx={{
                                    '& .MuiOutlinedInput-root': {
                                        '&:hover fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 0.5)',
                                        },
                                        '&.Mui-focused fieldset': {
                                            borderColor: 'rgba(255, 94, 0, 1)',
                                        },
                                    },
                                    '& .MuiInputLabel-root.Mui-focused': {
                                        color: 'rgba(255, 94, 0, 1)',
                                    },
                                }}
                            />

                            {/* Botones */}
                            <Stack direction="row" spacing={2}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={isSubmitting}
                                    sx={{
                                        flex: 1,
                                        backgroundColor: "rgba(255, 94, 0, 1)",
                                        borderRadius: 3,
                                        py: 1.5,
                                        '&:hover': {
                                            backgroundColor: "rgba(255, 94, 0, 0.8)"
                                        },
                                        '&:disabled': {
                                            backgroundColor: "rgba(255, 94, 0, 0.3)"
                                        }
                                    }}
                                >
                                    {isSubmitting ? "⏳ Registrando..." : "✅ Registrar Cliente"}
                                </Button>

                                <Button
                                    variant="outlined"
                                    onClick={() => navigate("/ClientList")}
                                    sx={{
                                        flex: 1,
                                        borderColor: "rgba(255, 94, 0, 1)",
                                        color: "rgba(255, 94, 0, 1)",
                                        borderRadius: 3,
                                        py: 1.5,
                                        '&:hover': {
                                            backgroundColor: "rgba(255, 94, 0, 0.1)",
                                            borderColor: "rgba(255, 94, 0, 0.8)"
                                        }
                                    }}
                                >
                                    📋 Ver Clientes
                                </Button>
                            </Stack>
                        </Stack>
                    </Box>
                </Paper>
            </Box>
        </>
    );
};

export default RegisterClient;