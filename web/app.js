const VENDOR_ID = 0x2a90;
const PRODUCT_ID = 0x0021;
const INTERFACE_NUMBER = 0;
const ENDPOINT_OUT = 2;
const ENDPOINT_IN = 1;

let device = null;

const statusElement = document.getElementById("status");
const logElement = document.getElementById("log");
const payloadInput = document.getElementById("payloadInput");

const connectButton = document.getElementById("connectButton");
const disconnectButton = document.getElementById("disconnectButton");
const sendButton = document.getElementById("sendButton");
const readButton = document.getElementById("readButton");
const clearLogButton = document.getElementById("clearLogButton");

function setStatus(text) {
    statusElement.textContent = `Status: ${text}`;
}

function appendLog(line) {
    const timestamp = new Date().toISOString();
    logElement.textContent += `[${timestamp}] ${line}\n`;
    logElement.scrollTop = logElement.scrollHeight;
}

function parseHexPayload(input) {
    const trimmed = input.trim();
    if (!trimmed) {
        throw new Error("Payload is empty.");
    }

    const parts = trimmed.split(/\s+/);
    const bytes = parts.map((part) => {
        if (!/^[0-9a-fA-F]{1,2}$/.test(part)) {
            throw new Error(`Invalid hex byte: ${part}`);
        }
        return parseInt(part, 16);
    });

    return new Uint8Array(bytes);
}

async function ensureDeviceReady() {
    if (!device) {
        throw new Error("No connected device.");
    }
    if (!device.opened) {
        await device.open();
    }

    if (device.configuration === null) {
        await device.selectConfiguration(1);
    }

    await device.claimInterface(INTERFACE_NUMBER);
}

async function connect() {
    if (!("usb" in navigator)) {
        throw new Error("WebUSB is not supported in this browser.");
    }

    device = await navigator.usb.requestDevice({
        filters: [{vendorId: VENDOR_ID, productId: PRODUCT_ID}]
    });

    await ensureDeviceReady();
    setStatus(`Connected to ${device.productName || "Gameband"}`);
    appendLog("Connected.");
}

async function disconnect() {
    if (!device) {
        setStatus("Not connected");
        return;
    }
    if (device.opened) {
        try {
            await device.releaseInterface(INTERFACE_NUMBER);
        } catch (error) {
            appendLog(`Interface release warning: ${error.message}`);
        }
        await device.close();
    }
    device = null;
    setStatus("Not connected");
    appendLog("Disconnected.");
}

async function sendPayload() {
    await ensureDeviceReady();
    const payload = parseHexPayload(payloadInput.value);
    await device.transferOut(ENDPOINT_OUT, payload);
    appendLog(`Sent ${payload.length} byte(s): ${payloadInput.value.trim()}`);
}

function toHexString(dataView) {
    const bytes = [];
    for (let i = 0; i < dataView.byteLength; i += 1) {
        bytes.push(dataView.getUint8(i).toString(16).padStart(2, "0"));
    }
    return bytes.join(" ");
}

async function readOnce() {
    await ensureDeviceReady();
    const result = await device.transferIn(ENDPOINT_IN, 64);
    if (result.status !== "ok" || !result.data) {
        appendLog(`Read failed with status: ${result.status}`);
        return;
    }
    appendLog(`Read ${result.data.byteLength} byte(s): ${toHexString(result.data)}`);
}

connectButton.addEventListener("click", async () => {
    try {
        await connect();
    } catch (error) {
        setStatus("Connection failed");
        appendLog(`Connect error: ${error.message}`);
    }
});

disconnectButton.addEventListener("click", async () => {
    try {
        await disconnect();
    } catch (error) {
        appendLog(`Disconnect error: ${error.message}`);
    }
});

sendButton.addEventListener("click", async () => {
    try {
        await sendPayload();
    } catch (error) {
        appendLog(`Send error: ${error.message}`);
    }
});

readButton.addEventListener("click", async () => {
    try {
        await readOnce();
    } catch (error) {
        appendLog(`Read error: ${error.message}`);
    }
});

clearLogButton.addEventListener("click", () => {
    logElement.textContent = "";
});

navigator.usb?.addEventListener("disconnect", (event) => {
    if (device && event.device === device) {
        appendLog("Device disconnected by system.");
        device = null;
        setStatus("Not connected");
    }
});
