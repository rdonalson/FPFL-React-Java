import axios from "axios";
import { attachInterceptors } from "./interceptors";

const http = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "", 
  headers: { "Content-Type": "application/json" },
  withCredentials: false,
});

attachInterceptors(http);

export default http;
