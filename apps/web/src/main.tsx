import React from "react";
import ReactDOM from "react-dom/client";
import { App } from "./app/App";
import { AdminLogin } from "./components/layout/AdminLogin";
import "./app/styles.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <AdminLogin>
      <App />
    </AdminLogin>
  </React.StrictMode>
);
