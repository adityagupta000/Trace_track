// src/components/MessageList.js
import { useState } from "react";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import toast from "react-hot-toast";

export default function MessageList({ messages }) {
  const [replies, setReplies] = useState({});
  const [sentReplies, setSentReplies] = useState({});
  const [sendingReply, setSendingReply] = useState({});

  const handleReply = async (msgId, senderId, itemId) => {
    const replyText = replies[msgId]?.trim();
    if (!replyText) {
      toast.error("Please enter a message");
      return;
    }

    if (replyText.length > 1000) {
      toast.error("Message cannot exceed 1000 characters");
      return;
    }

    setSendingReply((prev) => ({ ...prev, [msgId]: true }));

    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.REPLY}`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          receiverId: senderId,
          itemId: itemId,
          message: replyText,
        }),
      });

      const data = await res.json();

      if (res.ok && data.success) {
        toast.success(data.message || "Reply sent!");

        // Store the sent reply
        setSentReplies((prev) => ({
          ...prev,
          [msgId]: [
            ...(prev[msgId] || []),
            {
              text: replyText,
              timestamp: new Date().toLocaleString(),
            },
          ],
        }));

        // Clear the input
        setReplies((prev) => ({ ...prev, [msgId]: "" }));
      } else {
        toast.error(data.message || "Failed to send reply.");
      }
    } catch (error) {
      console.error("Reply error:", error);
      toast.error("Server error while sending reply.");
    } finally {
      setSendingReply((prev) => ({ ...prev, [msgId]: false }));
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (!messages.length)
    return (
      <p className="text-gray-500 text-sm italic text-center py-8">
        No messages yet.
      </p>
    );

  return (
    <div className="space-y-6">
      {messages.map((msg) => (
        <div
          key={msg.id}
          className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition"
        >
          <div className="mb-3 flex flex-wrap gap-x-4 gap-y-1">
            <p className="text-sm text-gray-600">
              <span className="font-medium text-gray-700">From:</span>{" "}
              {msg.senderName}
            </p>
            <p className="text-sm text-gray-600">
              <span className="font-medium text-gray-700">Regarding:</span>{" "}
              {msg.itemName}
            </p>
          </div>

          <div className="mb-4">
            <div className="bg-gray-50 border border-gray-100 rounded-xl p-3 text-gray-800 text-sm leading-relaxed">
              {msg.message}
            </div>
            <div className="text-xs text-gray-400 mt-1">
              {formatDate(msg.sentAt)}
            </div>
          </div>

          {sentReplies[msg.id]?.length > 0 && (
            <div className="mb-4 space-y-3">
              <div className="text-xs text-gray-500 font-medium uppercase tracking-wide">
                Your Replies
              </div>
              {sentReplies[msg.id].map((reply, index) => (
                <div key={index} className="flex justify-end">
                  <div className="max-w-xs lg:max-w-md">
                    <div className="bg-blue-600 text-white rounded-xl px-4 py-2 text-sm">
                      {reply.text}
                    </div>
                    <div className="text-xs text-gray-400 mt-1 text-right">
                      {reply.timestamp}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          <textarea
            placeholder="Write your reply..."
            className="w-full border border-gray-300 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition resize-none"
            rows="3"
            value={replies[msg.id] || ""}
            onChange={(e) =>
              setReplies((prev) => ({ ...prev, [msg.id]: e.target.value }))
            }
            maxLength={1000}
          />
          <div className="flex justify-between items-center mt-2">
            <span className="text-xs text-gray-500">
              {(replies[msg.id] || "").length}/1000 characters
            </span>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-4 py-2 rounded-xl transition disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={() => handleReply(msg.id, msg.senderId, msg.itemId)}
              disabled={
                !replies[msg.id]?.trim() || sendingReply[msg.id]
              }
            >
              {sendingReply[msg.id] ? "Sending..." : "Send Reply"}
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}