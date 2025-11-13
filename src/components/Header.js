// src/components/Header.js
import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import toast from "react-hot-toast";

export default function Header() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchUser();
  }, []);

  const fetchUser = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ME}`, {
        method: "GET",
        credentials: "include", // Send cookies with request
        headers: {
          Accept: "application/json",
        },
      });

      if (res.ok) {
        const response = await res.json();
        // Backend returns: { success: true, message: "...", data: { id, name, email, role } }
        if (response.success && response.data) {
          setUser(response.data);
        } else {
          setUser(null);
        }
      } else {
        // Don't redirect here - just clear user state
        setUser(null);
      }
    } catch (err) {
      console.error("Error fetching user:", err);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.LOGOUT}`, {
        method: "POST",
        credentials: "include", // Send cookies
      });

      if (res.ok) {
        setUser(null);
        toast.success("Logged out successfully");
        navigate("/");
      } else {
        toast.error("Logout failed");
      }
    } catch (err) {
      console.error("Logout error:", err);
      toast.error("Logout failed");
    }
  };

  if (loading) return null;

  return (
    <>
      <header className="bg-white text-gray-800 shadow-md sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          {/* Logo */}
          <Link to={user?.role === "ADMIN" ? "/admin" : "/home"}>
            <h1 className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition">
              Lost & Found
            </h1>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-6 text-sm">
            <Link
              to="/home"
              className="hover:text-blue-600 transition font-medium"
            >
              Home
            </Link>

            {user ? (
              <>
                <Link
                  to="/register-item"
                  className="hover:text-blue-600 transition font-medium"
                >
                  Register Item
                </Link>
                <Link
                  to="/lost-found-items"
                  className="hover:text-blue-600 transition font-medium"
                >
                  Browse Items
                </Link>
                {user.role === "ADMIN" && (
                  <Link
                    to="/admin"
                    className="hover:text-blue-600 transition font-medium"
                  >
                    Admin Dashboard
                  </Link>
                )}
                <div className="flex items-center gap-3 ml-2">
                  <span className="text-sm text-gray-600 bg-gray-100 px-3 py-1 rounded-full">
                    Hi, {user.name}
                  </span>
                  <button
                    onClick={handleLogout}
                    className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition font-medium"
                  >
                    Logout
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link
                  to="/"
                  className="hover:text-blue-600 transition font-medium"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium"
                >
                  Register
                </Link>
              </>
            )}
          </nav>

          {/* Mobile Hamburger */}
          <button
            className="md:hidden text-gray-800 focus:outline-none"
            onClick={() => setSidebarOpen(true)}
            aria-label="Open menu"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          </button>
        </div>
      </header>

      {/* Sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-40 z-40"
          onClick={() => setSidebarOpen(false)}
        ></div>
      )}

      {/* Sidebar */}
      <div
        className={`fixed top-0 left-0 h-full w-64 bg-white shadow-lg z-50 transform transition-transform duration-300 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        } md:hidden`}
      >
        <div className="p-4 flex items-center justify-between border-b">
          <h2 className="text-lg font-bold text-blue-600">Menu</h2>
          <button
            className="text-gray-600 text-2xl"
            onClick={() => setSidebarOpen(false)}
            aria-label="Close menu"
          >
            ×
          </button>
        </div>
        <div className="flex flex-col gap-4 p-4 text-sm">
          {user ? (
            <>
              <div className="pb-3 border-b">
                <p className="text-xs text-gray-500">Logged in as</p>
                <p className="font-medium text-gray-800">{user.name}</p>
              </div>
              <Link
                to="/home"
                onClick={() => setSidebarOpen(false)}
                className="hover:text-blue-600 py-2"
              >
                Home
              </Link>
              <Link
                to="/register-item"
                onClick={() => setSidebarOpen(false)}
                className="hover:text-blue-600 py-2"
              >
                Register Item
              </Link>
              <Link
                to="/lost-found-items"
                onClick={() => setSidebarOpen(false)}
                className="hover:text-blue-600 py-2"
              >
                Browse Items
              </Link>
              {user.role === "ADMIN" && (
                <Link
                  to="/admin"
                  onClick={() => setSidebarOpen(false)}
                  className="hover:text-blue-600 py-2"
                >
                  Admin Dashboard
                </Link>
              )}
              <button
                onClick={() => {
                  handleLogout();
                  setSidebarOpen(false);
                }}
                className="text-left text-red-600 hover:underline py-2"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/"
                onClick={() => setSidebarOpen(false)}
                className="hover:text-blue-600 py-2"
              >
                Login
              </Link>
              <Link
                to="/register"
                onClick={() => setSidebarOpen(false)}
                className="hover:text-blue-600 py-2"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </>
  );
}
