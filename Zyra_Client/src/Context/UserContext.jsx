import React, { createContext, useEffect, useState } from 'react';
import axios from "axios";

export const userDataContext = createContext();

function UserContext({ children }) {
  const [frontendImage, setFrontendImage] = useState(null);
  const [backendImage, setBackendImage] = useState(null);
  const [selectedImage, setSelectedImage] = useState(null);
  const serverUrl = "https://zyra-2-0.onrender.com";
  const [userData, setUserData] = useState(null);

  const handleCurrentUser = async () => {
    try {
      const result = await axios.get(`${serverUrl}/api/user/current`, {
        withCredentials: true,
      });
      setUserData(result.data);
      console.log(result.data);
    } catch (error) {
      if (error.response?.status !== 401) {
        console.log("Error fetching current user:", error);
      }
    }
  };

  const getGeminiResponse = async (command) => {
    if (!command || command.trim() === "") {
      console.log("Gemini Error: Empty command");
      return null;
    }

    try {
      const token = localStorage.getItem("token");
      const result = await axios.post(
        `${serverUrl}/api/user/asktoassistant`,
        { command },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          withCredentials: true,
        }
      );
      return result.data;
    } catch (error) {
      console.log("Gemini Error:", error.response?.data || error.message);
      return null;
    }
  };

  useEffect(() => {
    handleCurrentUser();
  }, []);

  const value = {
    serverUrl,
    userData,
    setUserData,
    frontendImage,
    setFrontendImage,
    backendImage,
    setBackendImage,
    selectedImage,
    setSelectedImage,
    getGeminiResponse,
  };

  return (
    <userDataContext.Provider value={value}>
      {children}
    </userDataContext.Provider>
  );
}

export default UserContext;
