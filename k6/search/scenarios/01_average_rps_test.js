import { check } from "k6";
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
        "status 200": (r) => r.status === 200
    });
}
