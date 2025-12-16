import './App.css';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Home from "./Component/Home";
import ToolList from "./Component/ToolList";
import AddTools from "./Component/AddTools";
import EditTools from "./Component/EditTools";
import { useKeycloak } from "@react-keycloak/web";
import Kardex from "./Component/Kardex";
import Reports from "./Component/Reports";
import RegisterClient from "./Component/RegisterClient";
import ClientList from "./Component/ClientList";
import LoanTool from "./Component/LoanTool";
import Configuration from "./Component/Configuration";

function App() {

  const { keycloak, initialized } = useKeycloak();

  if (!initialized) return <div>Cargando...</div>;

  const isLoggedIn = keycloak.authenticated;
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];

  const PrivateRoute = ({ element, rolesAllowed }) => {
    if (!isLoggedIn) {
      keycloak.login();
      return null;
    }
    if (rolesAllowed && !rolesAllowed.some(r => roles.includes(r))) {
      return <h2>No tienes permiso para ver esta página</h2>;
    }
    return element;
  };

  if (!isLoggedIn) { 
    keycloak.login(); 
    return null; 
  }  

  return (
    <Router>
      <div className="container">
        <Routes>
          <Route path="/" element={<Navigate to="/Home" />} />        
          <Route path="/Home" element={<Home />} />
          <Route
            path="/Home"
            element={<PrivateRoute element={<Home/>} rolesAllowed={["USER","ADMIN"]} />}
          />
          <Route path ="/ToolList" element= {<ToolList/>} rolesAllowed={["USER","ADMIN"]} 
          />
          <Route path="/AddTools" element={<AddTools />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/EditTools/:id" element={<EditTools />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/Kardex" element={<Kardex />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/Reports" element={<Reports />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/RegisterClient" element={<RegisterClient />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/ClientList" element={<ClientList />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/LoanTool/:id" element={<LoanTool />} rolesAllowed={["USER","ADMIN"]} />

          <Route path="/Configuration" element={<Configuration />} rolesAllowed={["ADMIN"]} />

          <Route path="*" element={<h2>Página no encontrada</h2>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
