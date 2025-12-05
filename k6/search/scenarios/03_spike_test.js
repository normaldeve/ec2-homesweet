import { check } from "k6";
import { search } from "../utils/http_client.js";
import { pickKeyword } from "../utils/random.js";

export const options = {
    scenarios: {
        spike: {
            executor: "constant-arrival-rate",
            rate: 10000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 15000,
            maxVUs: 30000
        }
    }
};

export default function () {
    const keyword = pickKeyword();
    const res = search(keyword);

    check(res, { "status 200": (r) => r.status === 200 });
}
