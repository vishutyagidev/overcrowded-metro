import { createTheme } from "@mantine/core";

const theme = createTheme({
  fontFamily: '"Jura", sans-serif;',
  headings: {
    fontFamily: '"Jura", sans-serif;',
    fontWeight: "bold"
  },
  colors: {
    primary: [
      "#ece1fd",
      "#d9c2fb",
      "#c69ff8",
      "#b77ef6",
      "#a959f3",
      "#9929ea",
      "#721cb0",
      "#4f117c",
      "#2f064c",
      "#200337"
    ],
    secondary: [
      "#f2dbf6",
      "#e5b7ed",
      "#d992e4",
      "#cc66da",
      "#ac4db9",
      "#8d3e97",
      "#6b2d73",
      "#4b1d51",
      "#2d0f31",
      "#1e0821"
    ],
    accent: [
      "#faeb92",
      "#ddce6a",
      "#bfb25b",
      "#a3974c",
      "#847a3c",
      "#69612f",
      "#504a22",
      "#383315",
      "#1f1c09",
      "#131104"
    ]
  },
  primaryColor: "primary"
});

export default theme;
