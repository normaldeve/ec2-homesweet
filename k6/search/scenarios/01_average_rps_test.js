import { check, sleep } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    scenarios: {
        average_rps: {
            executor: "constant-arrival-rate",
            rate: 100,
            timeUnit: "1s",
            duration: "3m",
            preAllocatedVUs: 600,
            maxVUs: 2000
        }
    }
};

export default function () {
    const keyword = pickKeyword();

    const res = search(keyword, "&size=12&sortType=RECOMMENDED");

    check(res, {
        "status 200": (r) => r.status === 200,
        "latency < 500ms": (r) => r.timings.duration < 500,
        "no ES timeout": (r) => !String(r.body).includes("timeout"),
    });

    sleep(1.2); // 평균 사용자 think time
}
