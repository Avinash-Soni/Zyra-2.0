import React, { useContext, useEffect, useRef, useState } from "react";
import { userDataContext } from "../Context/UserContext";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import aiImg from "../assets/ai.gif";
import userImg from "../assets/user.gif";
import { TfiMenu } from "react-icons/tfi";
import { ImCross } from "react-icons/im";

function Home() {
  // Access user-related data and functions from context
  const { userData, serverUrl, setUserData, getGeminiResponse } = useContext(userDataContext);
  const navigate = useNavigate();

  // State hooks for UI and functionality
  const [listening, setListening] = useState(false);
  const [userText, setUserText] = useState("");
  const [aiText, setAiText] = useState("");
  const [language, setLanguage] = useState("en-US");
  const [ham, setHam] = useState(false); // Hamburger menu toggle

  // References for recognition and speech
  const isSpeakingRef = useRef(false);
  const recognitionRef = useRef(null);
  const isRecognizingRef = useRef(false);
  const synth = window.speechSynthesis;
  const voicesRef = useRef([]);

  // Load available speech synthesis voices on component mount
  useEffect(() => {
    const loadVoices = () => {
      voicesRef.current = synth.getVoices();
    };
    if (synth.onvoiceschanged !== undefined) {
      synth.onvoiceschanged = loadVoices;
    }
    loadVoices();
  }, []);

  // Logout function
  const handleLogOut = async () => {
    try {
      await axios.get(`${serverUrl}/api/auth/logout`, { withCredentials: true });
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      setUserData(null);
      navigate("/signin");
    }
  };

  // Speak response using Web Speech API
  const speak = (text) => {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = language;

    const voices = voicesRef.current;

    // Try selecting a female voice for a more assistant-like feel
    const femaleVoice = voices.find(
      (v) =>
        v.lang === language &&
        (v.name.toLowerCase().includes("female") ||
         v.name.toLowerCase().includes("woman") ||
         v.name.toLowerCase().includes("zira") ||
         v.name.toLowerCase().includes("samantha") ||
         v.name.toLowerCase().includes("google हिन्दी"))
    );

    // Assign voice if available
    if (femaleVoice) {
      utterance.voice = femaleVoice;
    } else {
      const fallbackVoice = voices.find((v) => v.lang === language);
      if (fallbackVoice) utterance.voice = fallbackVoice;
    }

    // Mark as speaking
    isSpeakingRef.current = true;
    utterance.onend = () => {
      setAiText("");
      isSpeakingRef.current = false;
    };

    synth.speak(utterance);
  };

  // Handle certain command types (like opening YouTube or Google)
  const handleCommand = ({ type, userInput }) => {
    const openInNewTab = (url) => {
      setTimeout(() => window.open(url, "_blank"), 100);
    };

    const encodedQuery = encodeURIComponent(userInput || "");

    switch (type) {
      case "google_search":
        openInNewTab(`https://www.google.com/search?q=${encodedQuery}`);
        break;
      case "calculator_open":
        openInNewTab(`https://www.google.com/search?q=calculator`);
        break;
      case "instagram_open":
        openInNewTab(`https://www.instagram.com/`);
        break;
      case "facebook_open":
        openInNewTab(`https://www.facebook.com/`);
        break;
      case "weather_show":
        openInNewTab(`https://www.google.com/search?q=weather`);
        break;
      case "youtube_search":
      case "youtube_play":
        openInNewTab(`https://www.youtube.com/results?search_query=${encodedQuery}&autoplay=1`);
        break;
      default:
        break;
    }
  };

  // Main speech recognition and interaction logic
  useEffect(() => {
    if (!userData?.assistantName) return;

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.lang = language;
    recognitionRef.current = recognition;

    // Function to safely start recognition
    const safeRecognition = () => {
      if (!isSpeakingRef.current && !isRecognizingRef.current) {
        try {
          recognition.start();
          console.log("Recognition safely restarted");
        } catch (err) {
          if (err.name !== "InvalidStateError") {
            console.error("Safe start error:", err);
          }
        }
      }
    };

    recognition.onstart = () => {
      isRecognizingRef.current = true;
      setListening(true);
      console.log("Recognition started");
    };

    recognition.onend = () => {
      isRecognizingRef.current = false;
      setListening(false);
      console.log("Recognition ended");
    };

    recognition.onerror = (event) => {
      isRecognizingRef.current = false;
      setListening(false);
      console.warn("Recognition error:", event.error);
    };

    recognition.onresult = async (e) => {
      const transcript = e.results[e.results.length - 1][0].transcript.trim();
      console.log("Heard:", transcript);

      // Language switching via voice command
      if (transcript.toLowerCase().includes("speak in hindi")) {
        setLanguage("hi-IN");
        speak("अब मैं हिंदी में बात करूंगा");
        return;
      } else if (transcript.toLowerCase().includes("speak in english")) {
        setLanguage("en-US");
        speak("Now I will speak in English");
        return;
      }

      try {
        setUserText(transcript);
        setAiText("");
        recognition.stop();
        isRecognizingRef.current = false;
        setListening(false);

        const response = await getGeminiResponse(transcript);
        console.log("Gemini Response:", response);

        if (response?.response) {
          speak(response.response);
          handleCommand(response);
          setAiText(response.response);
          setUserText("");
        }
      } catch (err) {
        console.error("Gemini response error:", err);
        speak(language === "hi-IN" ? "माफ़ कीजिए, मैं नहीं समझ पाया।" : "Sorry, I didn't understand that.");
      }
    };

    // Fallback interval in case recognition stops
    const fallbackInterval = setInterval(() => {
      safeRecognition();
    }, 5000);

    safeRecognition();

    return () => {
      recognition.abort();
      recognition.stop();
      setListening(false);
      isRecognizingRef.current = false;
      clearInterval(fallbackInterval);
    };
  }, [userData?.assistantName, language]);

  // UI Rendering
  return (
    <div className="w-full min-h-screen overflow-hidden bg-gradient-to-t from-black to-[#030353] flex flex-col items-center justify-center gap-4">
  {/* Hamburger icon (for mobile) */}
  <TfiMenu
    className="lg:hidden text-white absolute top-[20px] right-[20px] w-[25px] h-[25px]"
    onClick={() => setHam(true)}
  />

  {/* Hamburger menu panel (Mobile) */}
  <div
    className={`absolute lg:hidden top-0 left-0 w-full h-full bg-black/60 backdrop-blur-md flex flex-col px-6 py-8 gap-6 z-50 transition-transform duration-300 ease-in-out ${
      ham ? "translate-x-0" : "-translate-x-full"
    }`}
  >
    <ImCross
      className="text-white absolute top-4 right-4 w-6 h-6 cursor-pointer"
      onClick={() => setHam(false)}
    />

    <div className="flex flex-col gap-4 mt-10">
      <button
        className="w-full py-3 bg-white text-black text-base font-semibold rounded-full shadow-md hover:bg-gray-200 transition cursor-pointer"
        onClick={handleLogOut}
      >
        Log out
      </button>
      <button
        className="w-full py-3 bg-white text-black text-base font-semibold rounded-full shadow-md hover:bg-gray-200 transition cursor-pointer"
        onClick={() => navigate("/customize")}
      >
        Customize your assistant
      </button>
    </div>

    <div className="w-full h-px bg-gray-400 my-4" />

    <h2 className="text-white text-lg font-semibold">History</h2>
    <div className="w-full flex-1 overflow-y-auto bg-white/10 rounded-lg p-4 shadow-inner">
      {userData.history?.length ? (
        userData.history.map((his, index) => (
          <p
            key={index}
            className="text-white text-sm mb-2 break-words border-b border-white/20 pb-1"
          >
            {his}
          </p>
        ))
      ) : (
        <p className="text-white text-sm italic">No history available.</p>
      )}
    </div>
  </div>

  {/* Hamburger icon (for desktop) */}
  {!ham && (
    <TfiMenu
      className="hidden lg:block text-white fixed top-5 right-5 z-50 w-6 h-6 cursor-pointer"
      onClick={() => setHam(true)}
    />
  )}

  {/* Sidebar (Desktop) */}
  {ham && (
    <div className="fixed top-0 right-0 h-full w-[300px] bg-black/60 backdrop-blur-md px-6 py-8 gap-6 z-50 shadow-lg flex-col hidden lg:flex">
      <ImCross
        className="text-white absolute top-4 right-4 w-6 h-6 cursor-pointer"
        onClick={() => setHam(false)}
      />

      <div className="flex flex-col gap-4 mt-12">
        <button
          className="w-full py-3 bg-white text-black text-base font-semibold rounded-full shadow-md hover:bg-gray-200 transition cursor-pointer"
          onClick={handleLogOut}
        >
          Log out
        </button>
        <button
          className="w-full py-3 bg-white text-black text-base font-semibold rounded-full shadow-md hover:bg-gray-200 transition cursor-pointer"
          onClick={() => navigate("/customize")}
        >
          Customize your assistant
        </button>
      </div>

      <div className="w-full h-px bg-gray-400 my-4" />

      <h2 className="text-white text-lg font-semibold">History</h2>
      <div className="w-full flex-1 overflow-y-auto bg-white/10 rounded-lg p-4 shadow-inner">
        {userData.history?.length ? (
          userData.history.map((his, index) => (
            <p
              key={index}
              className="text-white text-sm mb-2 break-words border-b border-white/20 pb-1"
            >
              {his}
            </p>
          ))
        ) : (
          <p className="text-white text-sm italic">No history available.</p>
        )}
      </div>
    </div>
  )}

  {/* Assistant avatar */}
  <div className="w-[90%] max-w-[300px] h-[400px] flex justify-center items-center overflow-hidden rounded-[2rem] shadow-lg">
    <img
      src={userData?.assistantImage}
      alt="Assistant"
      className="h-full object-cover"
    />
  </div>

  {/* Assistant name display */}
  <h1 className="text-white text-lg font-semibold text-center">
    I'm {userData?.assistantName || "your assistant"}
  </h1>

  {/* Display AI or user image depending on response */}
  {!aiText && <img src={userImg} alt="" className="w-[200px]" />}
  {aiText && <img src={aiImg} alt="" className="w-[200px]" />}

  {/* Display user input or AI response */}
  <h1 className="text-white text-[18px] font-semibold text-center px-4 break-words">
    {userText ? userText : aiText ? aiText : null}
  </h1>
</div>
  );
}

export default Home;
