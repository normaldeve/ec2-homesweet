import http from "k6/http";
import { check } from "k6";
import { sleep } from "k6";


const token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSIsImVtYWlsIjoianVubnVraW0xMDA3QGdtYWlsLmNvbSIsIm5hbWUiOiLquYDspIDsmrAiLCJwcm92aWRlciI6Imdvb2dsZSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY0ODMxNjIwLCJleHAiOjE3NjQ4NDk2MjB9.xqQ-U4uYUvPjK3nRwJERjJLrWurDH51_9pxnM9Bj6uhh94GSn0rljReg7YxWGr0FXZDEhs6OdIlMjsnzHhHQrw"
export const options = {
    stages: [
        {duration: '1m', target: 100}
    ]
};

export default function () {
    const keyword = encodeURIComponent("엘린하우스 침대");

    const response = http.get(
        `http://3.35.228.233:8080/api/v1/search/products?size=12&keyword=${keyword}&sortType=RECOMMENDED`,
        {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        }
    );

    check(response, {
        "is status 200": (r) => r.status === 200,
    });

    sleep(1);
}