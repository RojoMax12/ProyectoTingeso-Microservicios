import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ThemeProvider, CssBaseline, AppBar, Toolbar, Typography, Stack, Avatar, Button,
  Container, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  TextField, IconButton, Box, Paper
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import BuildIcon from "@mui/icons-material/Build";
import MenuIcon from "@mui/icons-material/Menu";
import { createTheme } from "@mui/material/styles";
import { useKeycloak } from "@react-keycloak/web";
import AmountsandratesServices from "../Services/AmountsandratesServices"; // <-- Importación agregada
import LibraryAddIcon from "@mui/icons-material/LibraryAdd"; // 📚➕ Para Agregar Herramienta
import AssessmentIcon from "@mui/icons-material/Assessment"; // 📊 Para Ver Kardex (reporte
import ContactsIcon from "@mui/icons-material/Contacts";     // 📇 Para Clientes)
import PersonAddAltIcon from '@mui/icons-material/PersonAddAlt';
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings"; // 🛡️⚙️ Para Configuraciones (admin)
import ReportIcon from '@mui/icons-material/Report'; // 📈 Para Reportes


const Configuration = () => {
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const photo = "";
  const { keycloak } = useKeycloak();
  const name = keycloak.tokenParsed?.name || "Usuario";
  const isAdmin = keycloak?.tokenParsed?.realm_access?.roles?.includes("ADMIN");

  // Estados para los campos del formulario
  const [dailyrentalrate, setDailyrentalrate] = useState("");
  const [dailylatefeefine, setDailylatefeefine] = useState("");
  const [reparationcharge, setReparationcharge] = useState("");

    const sidebarOptions = [
  { text: "Inicio", icon: <HomeIcon />, path: "/" },
  { text: "Herramientas", icon: <BuildIcon />, path: "/ToolList" },
  { text: "Agregar Herramienta", icon: <LibraryAddIcon />, path: "/AddTools" },
  { text: "Ver Kardex", icon: <AssessmentIcon />, path: "/Kardex" },
  { text: "Registrar Cliente", icon: <PersonAddAltIcon />, path: "/RegisterClient" },
  { text: "Clientes", icon: <ContactsIcon />, path: "/ClientList" },
  { text: "Reportes", icon: <ReportIcon />, path: "/Reports" },
  // Solo mostrar Configuraciones si es admin
  ...(isAdmin ? [{ text: "Configuraciones", icon: <AdminPanelSettingsIcon />, path: "/Configuration" }] : [])
];

  const theme = createTheme({
    palette: {
      mode: "light",
      primary: {
        main: "#FEF3E2"
      },
      secondary: {
        main: "#FA812F"
      },
      background: {
        default: "#FEF3E2"
      }
    }
  });


  const updateAmountsAndRates = () => {
    const data = {
      dailyrentalrate: parseFloat(dailyrentalrate) || 0,
      dailylatefeefine: parseFloat(dailylatefeefine) || 0,
      reparationcharge: parseFloat(reparationcharge) || 0
    };

    AmountsandratesServices.update(data)
      .then((response) => {
        alert("Montos y tarifas actualizados");
        console.log("Configuración actualizada:", response.data);
      })
      .catch((error) => {
        console.error("Error al actualizar montos y tarifas:", error);
        alert("Error al actualizar la configuración");
      });
  };

  const initializeAmountsAndRates = () => {
    AmountsandratesServices.create()
      .then((response) => {
        alert("Montos y tarifas inicializados");
        console.log("Montos y tarifas inicializados:", response.data);
      }
      )
      .catch((error) => {
        console.error("Error al inicializar montos y tarifas:", error);
        alert("Error al inicializar montos y tarifas");
      });
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

          <IconButton
              color="primary"
              onClick={() => setDrawerOpen(true)}
              sx={{ position: "fixed", top: 16, left: 16, zIndex: 10, backgroundColor: "#FA812F", boxShadow: 3 , '&:hover': { backgroundColor: "#FA812F" }}}
          >
              <MenuIcon />
          </IconButton>

  

      {/* SIDEBAR */}
      <Drawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        variant="temporary"
        sx={{
          [`& .MuiDrawer-paper`]: { width: 220, boxSizing: "border-box", backgroundColor: "#FEF3E2" }
        }}
      >
        <List>
          {sidebarOptions.map((option) => (
            <ListItem key={option.text} disablePadding>
              <ListItemButton onClick={() => { navigate(option.path); setDrawerOpen(false); }}>
                <ListItemIcon sx={{ color: "#FA812F" }}>{option.icon}</ListItemIcon>
                <ListItemText primary={option.text} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>

      {/* CONTENIDO PRINCIPAL */}
      <Container
        sx={{
          mt: 12,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          minHeight: "80vh"
    

        }}
      >
        <Typography variant="h4" align="center" sx={{ fontWeight: "bold", color: "rgba(255, 94, 0, 1)", mb: 4 }}>
          Configuración del Sistema
        </Typography>
        
        <Paper
          sx={{
            p: 4,
            boxShadow: 3,
            minWidth: 400,
            maxWidth: 600,
            width: "100%",
            borderRadius: 4, // <-- Súper redondo (32px)
            backgroundColor: "#fff8f0",
            border: "2px solid rgba(255, 94, 0, 0.2)"
          }}
        >
          {/*Configuracion de los cargo y tarifas*/}
          <Stack spacing={3}>
            <Typography variant="h6" sx={{ fontWeight: "bold", mb: 2 , color: "rgba(255, 94, 0, 1)" }}>
              Configuraciones de tarifas y cargos
            </Typography>
            
            <TextField
              label="Tarifa diaria de préstamo"
              variant="outlined"
              fullWidth
              placeholder="Ej: 2000"
              type="number"
              value={dailyrentalrate}
              onChange={(e) => setDailyrentalrate(e.target.value)}
            />
            
            <TextField
              label="Multa por día de atraso"
              variant="outlined"
              fullWidth
              placeholder="Ej: 5000"
              type="number"
              value={dailylatefeefine}
              onChange={(e) => setDailylatefeefine(e.target.value)}
            />

            <TextField
              label="Cargo por reparación"
              variant="outlined"
              fullWidth
              placeholder="Ej: 10000"
              type="number"
              value={reparationcharge}
              onChange={(e) => setReparationcharge(e.target.value)}
            />
          
            <Button 
              variant="contained" 
              color="primary" 
              fullWidth
              sx={{ mt: 3, color: "#fff", backgroundColor: "rgba(255, 94, 0, 1)" }}
              onClick={updateAmountsAndRates}
            >
              Guardar Configuración
            </Button>

            <Button 
              variant="contained" 
              color="primary" 
              fullWidth
              sx={{ mt: 1 , color: "#fff", backgroundColor: "rgba(255, 94, 0, 1)" }}
              onClick={initializeAmountsAndRates}
            >
              Inicializar montos y tarifas
            </Button>
          </Stack>
        </Paper>
      </Container>
    </ThemeProvider>
  );
};

export default Configuration;