import { getIconButtonUtilityClass } from "@mui/material"
import httpClient from "../http-commons"

const create = data => {
    return httpClient.post("/api/roles/", data)
}

const getAll = () => { 
    return httpClient.get("/api/roles/Allroles")
}

const getid = id => {
    return httpClient.get(`/api/roles/${id}`)
}

const deleteid = id=> {
    return httpClient.delete(`/api/roles/${id}`)
}

const update = data => {
    return httpClient.put("/api/roles/", data)
}

export default { create, getAll, getid, deleteid, update}