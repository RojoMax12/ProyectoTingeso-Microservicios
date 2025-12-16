import httpClient from "../http-commons"

const create = data => {
    return httpClient.post("/api/stateuser/", data)
}

const getid = id => { 
    return httpClient.get(`/api/stateuser/${id}`)
}

const getAll = () => {
    return httpClient.get("/api/stateuser/")
}

const deleteid = id => {
    return httpClient.delete(`/api/stateuser/${id}`)
}

const update = data => {
    return httpClient.put("/api/stateuser/UpdateStateUsers", data)
}


export default { create, getAll, getid, deleteid, update}