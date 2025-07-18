import Chat from "./Chat";
import ThreeDRender from "./ThreeDRender";

const Home = () => {
  return (
    <div className="grid grid-cols-[1fr_420px] h-[calc(100dvh-92px)] gap-4">
      <ThreeDRender />
      <Chat />
    </div>
  );
};

export default Home;
