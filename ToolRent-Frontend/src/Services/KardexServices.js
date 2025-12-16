import httpClient from "../http-commons";

//```usar cuando paso osea Id
const getAll = () => {
    return httpClient.get("/api/kardex/Allkardex");
}

const create = data => {
    return httpClient.post("/api/kardex/", data);
}

const deletekarex = id => {
    return httpClient.delete(`/api/kardex/${id}`);
}

const update = data => {
    return httpClient.put("/api/kardex/", data)
}

const kardexByDateRange = (inicio, fin) => {
    return httpClient.get(`/api/kardex/Range/${inicio}/${fin}`);
}


const kardexBytoolname = (nombre) => {
    return httpClient.get(`/api/kardex/History/${nombre}`);
}

const getTopTools = () => {
    return httpClient.get("/api/kardex/TopTool");
}

export default {getAll, create, deletekarex, update, kardexByDateRange, kardexBytoolname, getTopTools}