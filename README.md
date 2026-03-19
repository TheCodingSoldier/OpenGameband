# OpenGameband
Replacement for the Gameband Launcher 
This project is an open source replacement of the Gameband + Minecraft launcher.

# Status
So far it can launch the Minecraft launcher on macOS and Windows.
Build/runtime compatibility targets Java 17+, which aligns with modern Minecraft launcher environments.
At some point a PixelForge replacement will be implemented, though this requires some reverse engineering effort.

# Web USB App (No Download Required)
This repository now includes a browser-based interface in `/web` so users can connect to a Gameband from a web page without installing the desktop launcher.

## Open the website app
If you host this repository as a static website, opening the site root will automatically redirect to the web app.

## Run locally
From the project root:

```bash
python3 -m http.server 8080
```

Then open either:

```
http://localhost:8080/
```

or:

```
http://localhost:8080/web/
```

## How it works
- Uses the browser's [WebUSB](https://wicg.github.io/webusb/) API
- Requests the Gameband USB device (`vendorId: 0x2a90`, `productId: 0x0021`)
- Allows connecting, sending hex payloads, and reading responses

## Notes
- WebUSB requires a secure context (`https://` or `http://localhost`)
- Browser support is currently best in Chromium-based browsers
