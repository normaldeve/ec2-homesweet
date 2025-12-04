import http from "k6/http";
import { check } from "k6";
import { sleep } from "k6";


const token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSIsImVtYWlsIjoianVubnVraW0xMDA3QGdtYWlsLmNvbSIsIm5hbWUiOiLquYDspIDsmrAiLCJwcm92aWRlciI6Imdvb2dsZSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY0ODMxNjIwLCJleHAiOjE3NjQ4NDk2MjB9.xqQ-U4uYUvPjK3nRwJERjJLrWurDH51_9pxnM9Bj6uhh94GSn0rljReg7YxWGr0FXZDEhs6OdIlMjsnzHhHQrw"
export const options = {
    scenarios: {
        search_rps_test: {
            executor: "constant-arrival-rate",
            rate: 2000,
            timeUnit: "1s",
            duration: "3m",
            preAllocatedVUs: 200,
            maxVUs: 5000,
        }
    }
};


export default function () {
    const keyword = encodeURIComponent("엘린하우스");

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
}