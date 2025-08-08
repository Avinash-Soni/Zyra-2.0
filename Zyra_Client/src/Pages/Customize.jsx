import React, { useContext } from "react";
import Card from "../Components/Card";
import image1 from "../assets/image1.jpg";
import image2 from "../assets/image2.jpg";
import image3 from "../assets/image3.jpg";
import image4 from "../assets/image4.jpg";
import image5 from "../assets/image5.jpg";
import image6 from "../assets/image6.jpg";
import image7 from "../assets/image7.jpg";
// import { RiImageAddLine } from "react-icons/ri"; // ⛔ Custom upload icon removed
import { userDataContext } from "../Context/UserContext";
import { useNavigate } from "react-router-dom";
import { IoMdArrowRoundBack } from "react-icons/io";

function Customize() {
  const {
    serverUrl,
    userData,
    setUserData,
    // frontendImage, // ⛔ Removed
    // setFrontendImage, // ⛔ Removed
    // backendImage, // ⛔ Removed
    // setBackendImage, // ⛔ Removed
    selectedImage,
    setSelectedImage,
  } = useContext(userDataContext);

  // const inputImage = useRef(); // ⛔ Removed

  // ⛔ Removed custom image handler
  /*
  const handleImage = (e) => {
    const file = e.target.files[0];
    if (file) {
      setBackendImage(file);
      setFrontendImage(URL.createObjectURL(file));
    }
  };
  */

  const navigate = useNavigate();
  const imageList = [image1, image2, image3, image4, image5, image6, image7];

  return (
    <div className="w-full min-h-screen bg-gradient-to-t from-black to-[#030353] flex flex-col items-center justify-center px-4 sm:px-6 py-10 relative">
      <IoMdArrowRoundBack
        className="absolute top-5 left-5 sm:top-8 sm:left-8 text-white w-6 h-6 sm:w-7 sm:h-7 cursor-pointer"
        onClick={() => navigate("/")}
      />
      <h1 className="text-white mb-8 text-3xl text-center">
        Select your <span className="text-blue-500">Assistant Image</span>
      </h1>

      <div className="w-[90%] max-w-[900px] flex flex-wrap justify-center items-center gap-4">
        {imageList.map((img, idx) => (
          <Card key={idx} image={img} />
        ))}

        {/* ⛔ Custom Image Card Removed */}
        {/*
        <div
          className={`w-[70px] h-[140px] lg:w-[150px] lg:h-[200px] bg-[#030326] border-2 border-[#0000ff66] rounded-2xl overflow-hidden cursor-pointer flex items-center justify-center transition-all duration-200 
            ${selectedImage === "input" ? "border-4 border-white shadow-2xl shadow-blue-950" : "hover:shadow-2xl hover:shadow-blue-950 hover:border-4"}`}
          onClick={() => {
            setSelectedImage("input");
            inputImage.current?.click();
          }}
        >
          {!frontendImage ? (
            <RiImageAddLine className="text-white w-6 h-6" />
          ) : (
            <img src={frontendImage} className="h-full w-full object-cover" alt="Uploaded preview" />
          )}
        </div>

        <input
          type="file"
          accept="image/*"
          ref={inputImage}
          hidden
          onChange={handleImage}
        />
        */}
      </div>

      {selectedImage && (
        <button
          className="min-w-[150px] h-[50px] text-black font-semibold text-lg mt-8 bg-white rounded-full cursor-pointer"
          onClick={() => navigate("/customize2")}
        >
          Next
        </button>
      )}
    </div>
  );
}

export default Customize;
