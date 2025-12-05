import http from "k6/http";

const BASE_URL = "http://3.35.228.233:8080/api/v1/search/products";

const TOKEN = "Bearer <YOUR_JWT_TOKEN>";

export function search(keyword, params = "") {
    return http.get(
        `${BASE_URL}?keyword=${encodeURIComponent(keyword)}${params}`,
        {
            headers: { Authorization: TOKEN }
        }
    );
}
