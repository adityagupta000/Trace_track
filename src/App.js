import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import { useEffect } from "react";
import { Toaster } from "react-hot-toast";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import HomePage from "./pages/HomePage";
import AdminPage from "./pages/AdminPage";
import ItemRegisterPage from "./pages/ItemRegisterPage";
import ItemsPage from "./pages/ItemsPage";
import Header from "./components/Header";

// Wrapper component to conditionally show header
function LayoutWrapper() {
  const location = useLocation();

  // Define paths where header should be hidden
  const hideHeaderPaths = ["/", "/register"];
  const shouldHideHeader = hideHeaderPaths.includes(location.pathname);

  // Optional: Scroll to top on route change
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);

  return (
    <>
      <Toaster position="top-center" reverseOrder={false} />
      {!shouldHideHeader && <Header />}
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/register-item" element={<ItemRegisterPage />} />
        <Route path="/lost-found-items" element={<ItemsPage />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <Router>
      <LayoutWrapper />
    </Router>
  );
}
