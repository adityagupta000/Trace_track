// src/pages/AdminPage.js
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import AdminTable from "../components/AdminTable";
import toast, { Toaster } from "react-hot-toast";
import Swal from "sweetalert2";
import withReactContent from "sweetalert2-react-content";

const MySwal = withReactContent(Swal);

export default function AdminPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState([]);
  const [claims, setClaims] = useState([]);
  const [users, setUsers] = useState([]);
  const [feedback, setFeedback] = useState([]);
  const [stats, setStats] = useState({});

  // Validate Admin Access
  const validateAdminAccess = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ME}`, {
        credentials: "include",
      });

      if (!res.ok) {
        toast.error("Authentication required");
        navigate("/");
        return false;
      }

      const response = await res.json();
      const user = response.data;

      if (!user || user.role !== "ADMIN") {
        toast.error("Access denied. Admin privileges required.");
        navigate("/");
        return false;
      }
      return true;
    } catch (err) {
      toast.error("Error validating admin access.");
      navigate("/");
      return false;
    }
  };

  // Fetch Admin Dashboard Data
  const fetchAdminData = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ADMIN_DASHBOARD}`, {
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 403) {
          toast.error("Access denied");
          navigate("/");
        }
        throw new Error("Failed to load dashboard");
      }

      const data = await res.json();
      
      if (data.success) {
        setItems(data.items || []);
        setClaims(data.claims || []);
        setUsers(data.users || []);
        setFeedback(data.feedback || []);
        setStats(data.stats || {});
      }
    } catch (err) {
      toast.error("Failed to load admin data.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const init = async () => {
      const isAdmin = await validateAdminAccess();
      if (isAdmin) await fetchAdminData();
    };
    init();
  }, []);

  // Delete Handler with Confirmation
  const handleDelete = async (type, id) => {
    const confirmed = await MySwal.fire({
      title: `Delete this ${type}?`,
      text: `This action cannot be undone.`,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#3085d6",
      confirmButtonText: "Yes, delete it!",
      cancelButtonText: "Cancel",
    });

    if (!confirmed.isConfirmed) return;

    let endpoint;
    switch (type) {
      case "item":
        endpoint = API_ENDPOINTS.ADMIN_DELETE_ITEM(id);
        break;
      case "claim":
        endpoint = API_ENDPOINTS.ADMIN_DELETE_CLAIM(id);
        break;
      case "user":
        endpoint = API_ENDPOINTS.ADMIN_DELETE_USER(id);
        break;
      case "feedback":
        endpoint = API_ENDPOINTS.ADMIN_DELETE_FEEDBACK(id);
        break;
      default:
        return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: "DELETE",
        credentials: "include",
      });

      const data = await res.json();

      if (res.ok && data.success) {
        toast.success(data.message || `${type} deleted successfully.`);
        await fetchAdminData(); // Refresh data
      } else {
        toast.error(data.message || `Failed to delete ${type}.`);
      }
    } catch (err) {
      console.error(err);
      toast.error(`Error deleting ${type}.`);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Verifying admin access...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-6 py-8 space-y-8">
      <Toaster position="top-center" />

      <div className="text-center">
        <h1 className="text-3xl font-bold text-blue-600 mb-2">
          Admin Dashboard
        </h1>
        <p className="text-gray-600">Manage all platform activities</p>
      </div>

      {/* Statistics Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
            <h3 className="text-sm font-medium text-blue-600">Total Items</h3>
            <p className="text-2xl font-bold text-blue-700 mt-1">
              {stats.totalItems || 0}
            </p>
          </div>
          <div className="bg-green-50 border border-green-200 rounded-xl p-4">
            <h3 className="text-sm font-medium text-green-600">Total Claims</h3>
            <p className="text-2xl font-bold text-green-700 mt-1">
              {stats.totalClaims || 0}
            </p>
          </div>
          <div className="bg-purple-50 border border-purple-200 rounded-xl p-4">
            <h3 className="text-sm font-medium text-purple-600">Total Users</h3>
            <p className="text-2xl font-bold text-purple-700 mt-1">
              {stats.totalUsers || 0}
            </p>
          </div>
          <div className="bg-orange-50 border border-orange-200 rounded-xl p-4">
            <h3 className="text-sm font-medium text-orange-600">Total Feedback</h3>
            <p className="text-2xl font-bold text-orange-700 mt-1">
              {stats.totalFeedback || 0}
            </p>
          </div>
        </div>
      )}

      {/* Items Table */}
      <AdminTable
        title="Registered Items"
        type="item"
        data={items}
        headers={["ID", "Name", "Location", "Status", "Created By", "Actions"]}
        rowRenderer={(item) => [
          item.id,
          item.name,
          item.location,
          <span
            className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
              item.status === "FOUND"
                ? "bg-green-100 text-green-700"
                : item.status === "LOST"
                ? "bg-yellow-100 text-yellow-700"
                : "bg-blue-100 text-blue-700"
            }`}
          >
            {item.status}
          </span>,
          item.creatorName,
          <button
            onClick={() => handleDelete("item", item.id)}
            className="text-red-600 hover:text-red-800 text-sm font-medium"
          >
            Delete
          </button>,
        ]}
      />

      {/* Claims Table */}
      <AdminTable
        title="Claims"
        type="claim"
        data={claims}
        headers={["ID", "Item", "Claimant", "Email", "Claimed At", "Actions"]}
        rowRenderer={(claim) => [
          claim.id,
          claim.itemName,
          claim.claimantName,
          claim.claimantEmail,
          formatDate(claim.claimedAt),
          <button
            onClick={() => handleDelete("claim", claim.id)}
            className="text-red-600 hover:text-red-800 text-sm font-medium"
          >
            Delete
          </button>,
        ]}
      />

      {/* Users Table */}
      <AdminTable
        title="Users"
        type="user"
        data={users}
        headers={["ID", "Name", "Email", "Role", "Registered", "Actions"]}
        rowRenderer={(user) => [
          user.id,
          user.name,
          user.email,
          <span
            className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
              user.role === "ADMIN"
                ? "bg-red-100 text-red-700"
                : "bg-gray-100 text-gray-700"
            }`}
          >
            {user.role}
          </span>,
          formatDate(user.createdAt),
          user.role === "ADMIN" ? (
            <span className="text-gray-400 text-sm">Protected</span>
          ) : (
            <button
              onClick={() => handleDelete("user", user.id)}
              className="text-red-600 hover:text-red-800 text-sm font-medium"
            >
              Delete
            </button>
          ),
        ]}
      />

      {/* Feedback Table */}
      <AdminTable
        title="Feedback"
        type="feedback"
        data={feedback}
        headers={["ID", "User", "Feedback", "Submitted", "Actions"]}
        rowRenderer={(fb) => [
          fb.id,
          fb.userName || "Anonymous",
          <div className="max-w-md truncate">{fb.feedbackText}</div>,
          formatDate(fb.submittedAt),
          <button
            onClick={() => handleDelete("feedback", fb.id)}
            className="text-red-600 hover:text-red-800 text-sm font-medium"
          >
            Delete
          </button>,
        ]}
      />
    </div>
  );
}