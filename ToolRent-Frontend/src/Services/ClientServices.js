import httpClient from "../http-commons";

// Obtener todos los clientes
const getAll = () => {
    return httpClient.get("/api/Client/Allclient");
}

// Crear un cliente
const create = data => {
    return httpClient.post("/api/Client/", data);
}

// Actualizar un cliente
const update = data => {
    return httpClient.put("/api/Client/UpdateClient", data);
}

// Eliminar un cliente por id
const deleteClient = idclient => {
    return httpClient.delete(`/api/Client/Deleteclient/${idclient}`);
}

const getByRut = rut => {
    return httpClient.get(`/api/Client/rut/${rut}`);
}

const getByid = id => {
    return httpClient.get(`/api/Client/${id}`);
}

const getAllClientLoanLate = () => {
    return httpClient.get("/api/Client/AllClientLoanLate");
}

export default { getAll, create, update, deleteClient, getByRut, getByid, getAllClientLoanLate };