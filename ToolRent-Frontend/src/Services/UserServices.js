import httpClient from "../http-commons"

const create = data => {
    return httpClient.post("/api/user/", data)
}

const getAll = () => {
    return httpClient.get("/api/user/Alluser")
}

const updateUser = data =>{
    return httpClient.put("/api/user/UpdateUser", data)
}

const deleteUser = id => {
    return httpClient.delete(`/api/user/${id}`)
}

const Login = data => {
    return httpClient.post("/api/user/login", data)
}

const ReplacementCost = (nametool, userid, cost ) => {
    return httpClient.put("/api/user/replacement/", {params:{nametool, userid, cost}})
}

export default { create, getAll, updateUser, deleteUser, Login, ReplacementCost}