// src/components/ClaimsTable.js
import React from "react";

export default function ClaimsTable({ claims }) {
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
            <th className="px-4 py-3">LOCATION</th>
            <th className="px-4 py-3">CLAIMED AT</th>
          </tr>
        </thead>
        <tbody>
          {claims.length === 0 ? (
            <tr>
              <td colSpan={3} className="text-center py-8 text-gray-500">
                No claims yet.
              </td>
            </tr>
          ) : (
            claims.map((claim, idx) => (
              <tr
                key={idx}
                className="border-t text-sm text-gray-800 hover:bg-gray-50 transition"
              >
                <td className="px-4 py-3 font-medium">{claim.itemName}</td>
                <td className="px-4 py-3">{claim.location}</td>
                <td className="px-4 py-3 text-sm text-gray-600">
                  {formatDate(claim.claimedAt)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}