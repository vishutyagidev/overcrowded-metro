import { AppShell, Text, Title } from "@mantine/core";
import { Outlet } from "react-router-dom";
import { Icon } from "@iconify/react";

const RootLayout = () => {
  return (
    <AppShell header={{ height: 60 }} padding="md" bg="black">
      <AppShell.Header bg="secondary.9">
        <div className="flex items-center gap-4 h-[60px]">
          <Icon icon="mage:box-3d-fill" className="text-3xl text-primary-400" />
          <div className="">
            <Title order={3} c="primary">
              Mesh Prophet
            </Title>
            <Text c="secondary" size="xs" fw="bold">
              Quas. Wex. Exort. Export.
            </Text>
          </div>
        </div>
      </AppShell.Header>
      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
};

export default RootLayout;
