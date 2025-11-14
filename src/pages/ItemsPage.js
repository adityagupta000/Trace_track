// src/pages/ItemsPage.js
import { useEffect, useState } from "react";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import toast, { Toaster } from "react-hot-toast";

export default function ItemsPage() {
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState(""); // Backend expects "" for all, "LOST", "FOUND", or "CLAIMED"
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const [showMessageBox, setShowMessageBox] = useState(false);
  const [showClaimModal, setShowClaimModal] = useState(false);
  const [currentItem, setCurrentItem] = useState(null);
  const [messageText, setMessageText] = useState("");
  const [sendingMessage, setSendingMessage] = useState(false);
  const [claimingItem, setClaimingItem] = useState(false);

  useEffect(() => {
    fetchUser();
    fetchItems();
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      fetchItems();
    }, 60000); // Refresh every minute
    return () => clearInterval(interval);
  }, []);

  const fetchUser = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ME}`, {
        method: "GET",
        credentials: "include",
        headers: {
          Accept: "application/json",
        },
      });
      if (res.ok) {
        const data = await res.json();
        if (data.success && data.data) {
          setCurrentUser(data.data); // Backend returns data in 'data' field
        }
      }
    } catch (err) {
      console.error("Failed to fetch user", err);
    }
  };

  const fetchItems = async () => {
    try {
      const params = new URLSearchParams();
      if (search) params.append("search", search);
      if (filter) params.append("status", filter);

      const res = await fetch(
        `${API_BASE_URL}${API_ENDPOINTS.ITEMS}?${params.toString()}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            Accept: "application/json",
          },
        }
      );

      if (!res.ok) throw new Error("Failed to fetch items");

      const data = await res.json();
      setItems(data.items || []);
    } catch (err) {
      console.error(err);
      toast.error("Failed to load items");
    } finally {
      setLoading(false);
    }
  };

  // Trigger search when search or filter changes
  useEffect(() => {
    const debounce = setTimeout(() => {
      fetchItems();
    }, 500);
    return () => clearTimeout(debounce);
  }, [search, filter]);

  const handleClaim = async (itemId) => {
    setClaimingItem(true);
    try {
      const res = await fetch(
        `${API_BASE_URL}${API_ENDPOINTS.CLAIM_ITEM(itemId)}`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            Accept: "application/json",
          },
        }
      );

      const data = await res.json();

      if (res.ok && data.success) {
        toast.success(data.message || "Item claimed successfully!");
        setShowClaimModal(false);
        fetchItems(); // Refresh items list
      } else {
        toast.error(data.message || "Failed to claim item.");
      }
    } catch (err) {
      console.error("Claim failed", err);
      toast.error("Something went wrong.");
    } finally {
      setClaimingItem(false);
    }
  };

  const handleSendMessage = async () => {
    if (!currentItem || !messageText.trim()) {
      toast.error("Please enter a message");
      return;
    }

    if (messageText.trim().length > 1000) {
      toast.error("Message cannot exceed 1000 characters");
      return;
    }

    setSendingMessage(true);

    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.MESSAGES}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          receiverId: currentItem.createdBy,
          itemId: currentItem.id,
          message: messageText.trim(),
        }),
      });

      const data = await res.json();

      if (res.ok && data.success) {
        toast.success(data.message || "Message sent!");
        setShowMessageBox(false);
        setMessageText("");
        setCurrentItem(null);
      } else {
        toast.error(data.message || "Failed to send message.");
      }
    } catch (err) {
      console.error("Message failed", err);
      toast.error("Something went wrong.");
    } finally {
      setSendingMessage(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading items...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6">
      <Toaster position="top-center" reverseOrder={false} />

      <h1 className="text-3xl font-bold text-blue-600 text-center">
        Lost and Found Items
      </h1>

      {/* Search and Filter */}
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
        <input
          type="text"
          placeholder="Search by name, location, or description..."
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[150px]"
        >
          <option value="">All Items</option>
          <option value="LOST">Lost</option>
          <option value="FOUND">Found</option>
          <option value="CLAIMED">Claimed</option>
        </select>
      </div>

      {/* Items Grid */}
      <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item) => (
          <div
            key={item.id}
            className="border border-gray-200 rounded-xl overflow-hidden shadow-sm hover:shadow-md transition bg-white flex flex-col"
          >
            {item.image && (
              <img
                src={`${API_BASE_URL}/${item.image}`}
                alt={item.name}
                className="w-full h-48 object-cover"
              />
            )}
            <div className="p-4 space-y-2 flex-1 flex flex-col justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-800">
                  {item.name}
                </h2>
                <p className="text-sm text-gray-500 flex items-center gap-1">
                  <span>📍</span> {item.location}
                </p>
                <p className="text-sm text-gray-600 mt-2 line-clamp-2">
                  {item.description}
                </p>
              </div>

              <div className="text-xs text-gray-500 mt-2 space-y-1">
                <p>
                  Posted by: <strong>{item.creatorName}</strong>
                </p>
                <p>On: {formatDate(item.createdAt)}</p>
              </div>

              <div className="mt-2">
                <span
                  className={`inline-block text-xs px-3 py-1 rounded-full font-medium ${
                    item.status === "FOUND"
                      ? "bg-green-100 text-green-700"
                      : item.status === "LOST"
                      ? "bg-yellow-100 text-yellow-700"
                      : "bg-blue-100 text-blue-700"
                  }`}
                >
                  {item.status}
                </span>
              </div>

              {/* Action Buttons */}
              <div className="mt-3 flex gap-2">
                {item.status === "CLAIMED" ? (
                  <p className="text-sm text-red-600 font-medium">
                    This item has been claimed
                  </p>
                ) : currentUser && item.createdBy === currentUser.id ? (
                  <p className="text-sm text-gray-600 italic">
                    You posted this item
                  </p>
                ) : item.status === "FOUND" ? (
                  <>
                    <button
                      onClick={() => {
                        setCurrentItem(item);
                        setShowClaimModal(true);
                      }}
                      className="flex-1 text-sm bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition font-medium"
                    >
                      Claim Item
                    </button>
                    <button
                      onClick={() => {
                        setCurrentItem(item);
                        setShowMessageBox(true);
                      }}
                      className="flex-1 text-sm border border-blue-600 text-blue-600 py-2 rounded-lg hover:bg-blue-50 transition font-medium"
                    >
                      Message
                    </button>
                  </>
                ) : item.status === "LOST" ? (
                  <button
                    onClick={() => {
                      setCurrentItem(item);
                      setShowMessageBox(true);
                    }}
                    className="w-full text-sm border border-blue-600 text-blue-600 py-2 rounded-lg hover:bg-blue-50 transition font-medium"
                  >
                    Message Owner
                  </button>
                ) : null}
              </div>
            </div>
          </div>
        ))}
      </div>

      {items.length === 0 && (
        <div className="text-center py-20">
          <p className="text-gray-500 text-lg">No items found.</p>
          <p className="text-gray-400 text-sm mt-2">
            Try adjusting your search or filter criteria.
          </p>
        </div>
      )}

      {/* Message Modal */}
      {showMessageBox && currentItem && (
        <div className="fixed inset-0 bg-black bg-opacity-50 px-4 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-xl shadow-xl w-full max-w-md space-y-4">
            <h3 className="text-lg font-bold text-blue-600">Send Message</h3>
            <p className="text-sm text-gray-600">
              To: <strong>{currentItem.creatorName}</strong> <br />
              Item: <strong>{currentItem.name}</strong>
            </p>
            <textarea
              rows={4}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              placeholder="Type your message... (max 1000 characters)"
              value={messageText}
              onChange={(e) => setMessageText(e.target.value)}
              maxLength={1000}
            />
            <div className="text-xs text-gray-500 text-right">
              {messageText.length}/1000 characters
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => {
                  setShowMessageBox(false);
                  setMessageText("");
                  setCurrentItem(null);
                }}
                className="text-sm px-4 py-2 bg-gray-200 rounded-lg hover:bg-gray-300 transition"
              >
                Cancel
              </button>
              <button
                onClick={handleSendMessage}
                disabled={sendingMessage || !messageText.trim()}
                className="text-sm px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {sendingMessage ? "Sending..." : "Send"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Claim Modal */}
      {showClaimModal && currentItem && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
          <div className="bg-white p-6 rounded-xl shadow-xl w-full max-w-md space-y-4">
            <h3 className="text-lg font-bold text-blue-600">
              Claim Item: {currentItem.name}
            </h3>
            <p className="text-sm text-gray-600">
              Your registered name and email will be used for this claim. The
              item owner will be notified.
            </p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => {
                  setShowClaimModal(false);
                  setCurrentItem(null);
                }}
                className="text-sm px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
              >
                Cancel
              </button>
              <button
                onClick={() => handleClaim(currentItem.id)}
                disabled={claimingItem}
                className="text-sm px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {claimingItem ? "Claiming..." : "Confirm Claim"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
