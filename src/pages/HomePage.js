// src/pages/HomePage.js
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import ItemTable from "../components/ItemTable";
import MessageList from "../components/MessageList";
import ClaimsTable from "../components/ClaimsTable";
import toast, { Toaster } from "react-hot-toast";

export default function HomePage() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [role, setRole] = useState("");
  const [items, setItems] = useState([]);
  const [claims, setClaims] = useState([]);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [feedbackText, setFeedbackText] = useState("");
  const [submittingFeedback, setSubmittingFeedback] = useState(false);

  useEffect(() => {
    fetchHomeData();
  }, []);

  const fetchHomeData = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.DASHBOARD}`, {
        method: "GET",
        credentials: "include", // CRITICAL: Send cookies
        headers: {
          'Accept': 'application/json',
        },
      });

      if (!res.ok) {
        console.error(`Dashboard fetch failed: ${res.status} ${res.statusText}`);
        
        if (res.status === 401 || res.status === 403) {
          toast.error("Please login to continue");
          navigate("/");
        } else if (res.status === 429) {
          toast.error("Too many requests. Please wait a moment.");
        } else {
          toast.error("Failed to load dashboard");
        }
        return;
      }

      const data = await res.json();
      console.log("Dashboard data:", data); // Debug log

      if (data.user) {
        setName(data.user.name);
        setRole(data.user.role);
        setItems(data.items || []);
        setClaims(data.claims || []);
        setMessages(data.messages || []);
      } else {
        navigate("/");
      }
    } catch (err) {
      console.error("Failed to fetch dashboard:", err);
      toast.error("Network error. Please check your connection.");
      // Don't auto-navigate on network errors
    } finally {
      setLoading(false);
    }
  };

  const handleFeedbackSubmit = async (e) => {
    e.preventDefault();

    if (!feedbackText.trim()) {
      toast.error("Please enter your feedback");
      return;
    }

    if (feedbackText.trim().length < 10) {
      toast.error("Feedback must be at least 10 characters long");
      return;
    }

    if (feedbackText.trim().length > 2000) {
      toast.error("Feedback cannot exceed 2000 characters");
      return;
    }

    setSubmittingFeedback(true);

    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.FEEDBACK}`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
        },
        body: JSON.stringify({ feedback: feedbackText.trim() }),
      });

      const data = await res.json();

      if (res.ok && data.success) {
        toast.success(data.message || "Thank you for your feedback!");
        setFeedbackText("");
      } else {
        toast.error(data.message || "Failed to submit feedback.");
      }
    } catch (err) {
      console.error(err);
      toast.error("Something went wrong while submitting feedback.");
    } finally {
      setSubmittingFeedback(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-gray-600">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p>Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-10 pb-20">
      <Toaster position="top-center" />

      <div className="text-center">
        <h1 className="text-3xl font-bold text-gray-800">Welcome, {name}!</h1>
        <p className="text-sm text-gray-500 mt-1">
          Here's an overview of your account and activity.
        </p>
      </div>

      {/* My Registered Items */}
      <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
        <h2 className="text-xl font-semibold text-gray-700 mb-4 border-b pb-2">
          My Registered Items
        </h2>
        <ItemTable items={items} />
      </section>

      {/* My Claims */}
      <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
        <h2 className="text-xl font-semibold text-gray-700 mb-4 border-b pb-2">
          My Claims
        </h2>
        <ClaimsTable claims={claims} />
      </section>

      {/* Messages */}
      <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
        <h2 className="text-xl font-semibold text-gray-700 mb-4 border-b pb-2">
          Messages
        </h2>
        <MessageList messages={messages} />
      </section>

      {/* Platform Feedback - Only for non-admin users */}
      {role !== "ADMIN" && (
        <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
          <h2 className="text-xl font-semibold text-gray-700 mb-4 border-b pb-2">
            Platform Feedback
          </h2>
          <form onSubmit={handleFeedbackSubmit} className="space-y-4">
            <textarea
              rows={4}
              className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              placeholder="Share your experience with us... (minimum 10 characters)"
              value={feedbackText}
              onChange={(e) => setFeedbackText(e.target.value)}
              required
              maxLength={2000}
            />
            <div className="flex justify-between items-center">
              <span className="text-xs text-gray-500">
                {feedbackText.length}/2000 characters
              </span>
              <button
                type="submit"
                disabled={submittingFeedback || feedbackText.trim().length < 10}
                className="bg-blue-600 text-white px-6 py-2 rounded-xl hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submittingFeedback ? "Submitting..." : "Submit Feedback"}
              </button>
            </div>
          </form>
        </section>
      )}
    </div>
  );
}