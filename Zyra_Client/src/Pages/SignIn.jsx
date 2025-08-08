import React, { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { IoIosEye, IoIosEyeOff } from "react-icons/io";

import bg from "../assets/bg.jpg";
import { userDataContext } from "../Context/UserContext";

function SignIn() {
  const { serverUrl, setUserData } = useContext(userDataContext);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSignIn = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await axios.post(
        `${serverUrl}/api/auth/signin`,
        formData,
        { withCredentials: true }
      );

      // Store token in localStorage
      localStorage.setItem("token", response.data.token);

      setUserData(response.data);
      setLoading(false);
      navigate("/");
      console.log(response.data);
    } catch (err) {
      console.log(err);
      setError(err?.response?.data?.message || "Sign In failed");
      setUserData(null);
      setLoading(false);
    }
  };

  return (
    <div
      className="w-full min-h-screen bg-cover bg-center flex justify-center items-center px-4 sm:px-6"
      style={{ backgroundImage: `url(${bg})` }}
    >
      <form
        onSubmit={handleSignIn}
        className="w-full sm:w-[90%] md:w-[500px] bg-[#00000062] backdrop-blur-lg rounded-xl shadow-lg shadow-black flex flex-col items-center justify-center gap-5 px-5 py-10"
      >
        <h1 className="text-white text-2xl sm:text-3xl font-semibold text-center">
          <span className="text-blue-400">Zyra 2.0</span>
        </h1>

        <input
          name="email"
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={handleChange}
          required
          className="w-full h-[50px] sm:h-[60px] border border-white bg-transparent text-white placeholder-gray-300 px-5 py-2 rounded-full text-base sm:text-lg outline-none"
        />

        <div className="w-full h-[50px] sm:h-[60px] border-2 border-white bg-transparent text-white rounded-full text-base sm:text-lg relative">
          <input
            name="password"
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
            className="w-full h-full rounded-full outline-none bg-transparent placeholder-gray-300 px-5 py-2"
          />
          {showPassword ? (
            <IoIosEyeOff
              className="absolute top-3.5 sm:top-4 right-5 text-white w-5 h-5 sm:w-6 sm:h-6 cursor-pointer"
              onClick={() => setShowPassword(false)}
            />
          ) : (
            <IoIosEye
              className="absolute top-3.5 sm:top-4 right-5 text-white w-5 h-5 sm:w-6 sm:h-6 cursor-pointer"
              onClick={() => setShowPassword(true)}
            />
          )}
        </div>

        {error && (
          <p className="text-red-500 text-sm sm:text-base text-center">{error}</p>
        )}

        <button
          type="submit"
          className="w-full sm:min-w-[150px] h-[45px] sm:h-[50px] text-black font-semibold text-base sm:text-lg mt-4 bg-white rounded-full cursor-pointer"
          disabled={loading}
        >
          {loading ? "Loading..." : "Sign In"}
        </button>

        <p className="text-white text-sm sm:text-lg text-center mt-2">
          Want to create a new account?{" "}
          <span
            onClick={() => navigate("/signup")}
            className="text-blue-400 cursor-pointer"
          >
            Sign Up
          </span>
        </p>
      </form>
    </div>
  );
}

export default SignIn;
