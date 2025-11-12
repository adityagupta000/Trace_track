import { useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import "./index.css";
import "./App.css";

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="min-h-screen flex bg-blue-500 text-blue text-3xl font-bold">
      Tailwind v4 is working 🎉
    </div>
  );
}

export default App;
