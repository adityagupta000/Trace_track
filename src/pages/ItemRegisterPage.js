// src/pages/ItemRegisterPage.js
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CameraIcon, Loader2, X } from "lucide-react";
import { API_BASE_URL, API_ENDPOINTS } from "../config/api";
import toast, { Toaster } from "react-hot-toast";

export default function RegisterItemPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    location: "",
    status: "LOST", // Backend expects uppercase: LOST or FOUND
  });
  const [image, setImage] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file size (5MB max)
      if (file.size > 5 * 1024 * 1024) {
        toast.error("Image size must not exceed 5MB");
        return;
      }

      // Validate file type
      if (!file.type.startsWith("image/")) {
        toast.error("Only image files are allowed");
        return;
      }

      setImage(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const clearImage = () => {
    setImage(null);
    setPreviewUrl(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!image) {
      toast.error("Please select an image file.");
      return;
    }

    setLoading(true);

    const data = new FormData();
    data.append("name", formData.name.trim());
    data.append("description", formData.description.trim());
    data.append("location", formData.location.trim());
    data.append("status", formData.status);
    data.append("image", image);

    try {
      const res = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ITEMS}`, {
        method: "POST",
        body: data,
        credentials: "include", // Send cookies
        // Don't set Content-Type header for FormData - browser will set it with boundary
      });

      const result = await res.json();

      if (res.ok && result.success) {
        toast.success(result.message || "Item registered successfully!");
        setTimeout(() => navigate("/lost-found-items"), 2000);
      } else {
        // Handle validation errors
        if (result.errors) {
          Object.values(result.errors).forEach((error) => toast.error(error));
        } else {
          toast.error(result.message || "Failed to register item.");
        }
      }
    } catch (err) {
      console.error(err);
      toast.error("Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <Toaster position="top-center" />

      <div className="bg-white shadow-xl rounded-2xl p-8 border border-gray-200">
        <div className="text-center mb-6">
          <h2 className="text-3xl font-bold text-blue-600 flex justify-center items-center gap-2">
            <CameraIcon className="w-7 h-7" />
            Register Lost / Found Item
          </h2>
          <p className="text-sm text-gray-500 mt-2">
            Submit the details to help match items with their rightful owners.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Item Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Item Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none focus:border-transparent"
              placeholder="e.g., Black Wallet, iPhone 13"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description <span className="text-red-500">*</span>
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              required
              rows={4}
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none focus:border-transparent resize-none"
              placeholder="Provide details like color, brand, distinctive features, contents..."
            />
          </div>

          {/* Location */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Location <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="location"
              value={formData.location}
              onChange={handleChange}
              required
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none focus:border-transparent"
              placeholder="Where was it found/lost? e.g., Library 2nd Floor, Main Campus"
            />
          </div>

          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status <span className="text-red-500">*</span>
            </label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full border border-gray-300 px-4 py-2 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none focus:border-transparent"
            >
              <option value="LOST">Lost - I lost this item</option>
              <option value="FOUND">Found - I found this item</option>
            </select>
          </div>

          {/* Image Upload */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Upload Image <span className="text-red-500">*</span>
              <span className="text-xs text-gray-500 ml-2">(Max 5MB)</span>
            </label>
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              required={!image}
              className="w-full text-sm file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            />
          </div>

          {/* Preview */}
          {previewUrl && (
            <div className="relative">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Preview
              </label>
              <div className="relative">
                <img
                  src={previewUrl}
                  alt="Preview"
                  className="w-full max-h-[400px] object-contain border border-gray-300 rounded-lg shadow"
                />
                <button
                  type="button"
                  onClick={clearImage}
                  className="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full hover:bg-red-600 transition"
                  aria-label="Remove image"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}

          {/* Submit Button */}
          <div className="pt-4">
            <button
              type="submit"
              disabled={loading}
              className={`w-full flex justify-center items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition duration-200 ${
                loading ? "opacity-70 cursor-not-allowed" : ""
              }`}
            >
              {loading ? (
                <>
                  <Loader2 className="animate-spin w-5 h-5" />
                  Submitting...
                </>
              ) : (
                <>
                  <CameraIcon className="w-5 h-5" />
                  Register Item
                </>
              )}
            </button>
          </div>
        </form>

        <div className="mt-6 text-center">
          <button
            onClick={() => navigate("/lost-found-items")}
            className="text-sm text-blue-600 hover:underline"
          >
            ← Back to Browse Items
          </button>
        </div>
      </div>
    </div>
  );
}