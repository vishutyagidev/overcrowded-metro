import { ActionIcon, ScrollArea, TextInput } from "@mantine/core";
import { Icon } from "@iconify/react";
import * as yup from "yup";
import { FormProvider, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { useState } from "react";
import { v4 as uuid } from "uuid";
import { cn } from "../theme/utils";

interface IMessage {
  id: string;
  message: string;
  type: "bot" | "user";
}

const schema = yup.object({
  message: yup.string().trim().required("Required")
});

const Chat = () => {
  const [messages, setMessages] = useState<Array<IMessage>>([]);

  const form = useForm({
    resolver: yupResolver(schema),
    defaultValues: { message: "" }
  });

  const handleSubmit = form.handleSubmit(async (data) => {
    setMessages((prev) => [
      ...prev,
      { id: uuid(), message: data.message, type: "user" } satisfies IMessage
    ]);
  });

  return (
    <section className="flex flex-col border-4 bg-accent-50/10 border-accent-100 rounded-3xl p-4">
      <ScrollArea.Autosize mah={300}>
        <div className="flex flex-col">
          {messages.map((message) => (
            <div
              key={message.id}
              className={cn(
                "bg-secondary-600 p-3 mb-3 rounded-2xl font-semibold text-accent-50",
                {
                  "self-end": message.type === "user"
                }
              )}>
              {message.message}
            </div>
          ))}
        </div>
      </ScrollArea.Autosize>
      <FormProvider {...form}>
        <form className="mt-auto" onSubmit={handleSubmit}>
          <div className="flex items-start gap-2">
            <TextInput
              variant="filled"
              className="flex-1"
              {...form.register("message")}
              error={form.formState.errors.message?.message}
            />
            <ActionIcon color="accent.1" c="accent.9" size="lg" type="submit">
              <Icon icon="ic:round-send" />
            </ActionIcon>
          </div>
        </form>
      </FormProvider>
    </section>
  );
};

export default Chat;
