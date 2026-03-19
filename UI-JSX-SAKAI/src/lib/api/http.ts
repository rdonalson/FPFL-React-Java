import axios from "axios";
import { attachInterceptors } from "./interceptors";

const http = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:9000",
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

attachInterceptors(http);

export default http;
