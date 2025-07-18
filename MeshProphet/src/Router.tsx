import { RouterProvider, createBrowserRouter } from "react-router-dom";
import RootLayout from "./components/shared/RootLayout";
import Home from "./components/Home";

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    errorElement: <div>Not Found</div>,
    children: [{ index: true, element: <Home /> }]
  }
]);

const Router = () => {
  return <RouterProvider router={router} />;
};

export default Router;
