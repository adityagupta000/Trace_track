// src/components/ItemTable.js
import React from "react";

export default function ItemTable({ items }) {
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

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200">
      <table className="min-w-full table-auto">
        <thead>
          <tr className="bg-gray-100 text-left text-sm font-semibold text-gray-700">
            <th className="px-4 py-3">ITEM</th>
            <th className="px-4 py-3">STATUS</th>
            <th className="px-4 py-3">LOCATION</th>
            <th className="px-4 py-3">CREATED AT</th>
          </tr>
        </thead>
        <tbody>
          {items.length === 0 ? (
            <tr>
              <td colSpan={4} className="text-center py-8 text-gray-500">
                No items registered yet.
              </td>
            </tr>
          ) : (
            items.map((item, idx) => (
              <tr
                key={idx}
                className="border-t text-sm text-gray-800 hover:bg-gray-50 transition"
              >
                <td className="px-4 py-3 font-medium">{item.name}</td>
                <td className="px-4 py-3">
                  <span
                    className={`inline-block px-2 py-1 rounded-full text-xs font-semibold ${
                      item.status === "FOUND"
                        ? "bg-green-100 text-green-800"
                        : item.status === "LOST"
                        ? "bg-yellow-100 text-yellow-800"
                        : "bg-blue-100 text-blue-800"
                    }`}
                  >
                    {item.status}
                  </span>
                </td>
                <td className="px-4 py-3">{item.location}</td>
                <td className="px-4 py-3 text-sm text-gray-600">
                  {formatDate(item.createdAt)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}