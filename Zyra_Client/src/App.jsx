import React, { useContext } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import SignUp from "./Pages/SignUp.jsx";
import Signin from "./Pages/SignIn.jsx";  
import Customize from "./Pages/Customize.jsx";
import Home from "./Pages/Home.jsx";
import { userDataContext } from "./Context/UserContext";
import Customize2 from "./Pages/Customize2.jsx";

function App() {
  const { userData } = useContext(userDataContext);

  return (
    <Routes>
      <Route
        path="/home"
        element={
          userData?.assistantImage && userData.assistantName ? (
            <Home />
          ) : (
            <Navigate to="/customize" />
          )
        }
      />
      <Route
        path="/signup"
        element={!userData ? <SignUp /> : <Navigate to="/home" />}
      />
      <Route
        path="/signin"
        element={!userData ? <Signin /> : <Navigate to="/home" />}
      />
      <Route
        path="/customize"
        element={userData ? <Customize /> : <Navigate to="/signin" />}
      />
      <Route
        path="/customize2"
        element={userData ? <Customize2 /> : <Navigate to="/signin" />}
      />
      <Route path="*" element={<Navigate to="/signin" />} />

    </Routes>
  );
}

export default App;
