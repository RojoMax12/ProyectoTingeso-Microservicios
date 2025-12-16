import httpClient from "../http-commons"

const create = (data) =>{
    return httpClient.post("/api/Tools/", data)
}

const getAll = () => {
    return httpClient.get("/api/Tools/alltools")
}

const getid = id => {
    return httpClient.get(`/api/Tools/tool/${id}`)
}

const update = data => {
    return httpClient.put("/api/Tools/UpdateTool", data)
}

const deleteid = id  => {
    return httpClient.delete(`/api/Tools/${id}`)
}

const getinventory = data => {
    return httpClient.get("/api/Tools/inventory", data)
}

const unsuscribeTools = (idtool) =>{
    return httpClient.put(`/api/Tools/${idtool}`);
}

const repairtool = (idtool) => {
    return httpClient.put(`/api/Tools/inrepair/${idtool}`);
}



export default {getAll, create, getid, update, deleteid, getinventory, unsuscribeTools, repairtool}