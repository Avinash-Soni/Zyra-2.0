import React, { useContext } from "react";
import { userDataContext } from "../Context/UserContext";

function Card({ image, altText = "card image" }) {
  
    const {serverUrl ,userData,setUserData,frontendImage,setFrontendImage,backendImage,setBackendImage,selectedImage,setSelectedImage} = useContext(userDataContext);
  return (
    <div className={`w-[70px] h-[140px] lg:w-[150px] lg:h-[200px] bg-[#030326] border-2 border-[#0000ff66] rounded-2xl overflow-hidden hover:shadow-2xl hover:shadow-blue-950 cursor-pointer hover:border-4 ${selectedImage == image?"border-4 border-white shadow-2xl shadow-blue-950":null  }`}
    onClick={()=>{
      setSelectedImage(image);
      setBackendImage(null);
      setFrontendImage(null)
    }}
    >
      <img 
        src={image} 
        alt={altText} 
        className="w-full h-full object-cover" 
      />
    </div>
  );
}

export default Card;
