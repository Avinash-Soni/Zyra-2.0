import React, { useContext, useState } from "react";
import { userDataContext } from "../Context/UserContext";
import axios from "axios";
import { IoMdArrowRoundBack } from "react-icons/io";
import { useNavigate } from "react-router-dom";

function Customize2() {
  const {
    userData,
    backendImage,
    selectedImage,
    serverUrl,
    setUserData,
  } = useContext(userDataContext);

  const [assistantName, setAssistantName] = useState(
    userData?.assistantName || ""
  );
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleUpdateAssistant = async () => {
  try {
    setLoading(true);
    let formData = new FormData();
    formData.append("assistantName", assistantName);

    if (backendImage) {
      formData.append("assistantImage", backendImage);
    } else {
      formData.append("imageUrl", selectedImage);
    }

    const token = localStorage.getItem("token"); // Get JWT token
    console.log("Token:", token);

    const result = await axios.post(
      `${serverUrl}/api/user/update`,
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`, // Attach JWT
        },
        withCredentials: true,
      }
    );

    console.log(result.data);
    setUserData(result.data);
    navigate("/home");
  } catch (error) {
    console.log(error);
    setLoading(false);
  }
};

  return (
    <div className="w-full min-h-screen bg-gradient-to-t from-black to-[#030353] flex flex-col items-center justify-center px-4 sm:px-6 py-10 relative">
      {/* Back Button */}
      <IoMdArrowRoundBack
        className="absolute top-5 left-5 sm:top-8 sm:left-8 text-white w-6 h-6 sm:w-7 sm:h-7 cursor-pointer"
        onClick={() => navigate("/customize")}
      />

      {/* Heading */}
      <h1 className="text-white mb-8 text-2xl sm:text-3xl text-center font-semibold">
        Enter Your <span className="text-blue-500">Assistant Name</span>
      </h1>

      {/* Input */}
      <input
        name="text"
        type="text"
        placeholder="e.g. Zyra 2.0"
        className="w-full max-w-[600px] h-[50px] sm:h-[60px] border border-white bg-transparent text-white placeholder-gray-300 px-5 py-2 rounded-full text-base sm:text-lg outline-none"
        onChange={(e) => setAssistantName(e.target.value)}
        value={assistantName}
      />

      {/* Submit Button */}
      {assistantName && (
        <button
          className="mt-8 min-w-[200px] sm:min-w-[300px] h-[45px] sm:h-[50px] px-6 text-black font-semibold text-base sm:text-lg bg-white rounded-full cursor-pointer"
          disabled={loading}
          onClick={handleUpdateAssistant}
        >
          {loading ? "Loading..." : "Finally Create Your Assistant"}
        </button>
      )}
    </div>
  );
}

export default Customize2;
