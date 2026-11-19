package com.bootcamp.smarthome.controller;

import com.bootcamp.smarthome.device.Device;
import com.bootcamp.smarthome.exception.DeviceNotFoundException;
import com.bootcamp.smarthome.exception.DeviceOfflineException;
import com.bootcamp.smarthome.exception.HomeAutomationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central hub that manages all registered smart devices.
 * <p>
 * Devices are stored in a fixed-size array (maximum {@value #MAX_DEVICES}).
 * The controller routes commands to devices by their ID.
 */
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    public static final int MAX_DEVICES = 8;

    private final Device[] devices = new Device[MAX_DEVICES];
    private int deviceCount = 0;

    // -------------------------------------------------------------------------
    // Device registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new device with the controller.
     * The controller accepts at most {@value #MAX_DEVICES} devices.
     *
     * @param device the device to register
     * @throws IllegalStateException if the device limit has been reached
     */
    public void addDevice(Device device) {
        if (deviceCount >= MAX_DEVICES) {
            throw new IllegalStateException(
                    "Cannot add device '" + device.getDeviceId() +
                            "': controller is at maximum capacity (" + MAX_DEVICES + ").");
        }
        devices[deviceCount] = device;
        deviceCount++;
        logger.info("Device '{}' added", device.getDeviceId());
    }

    // -------------------------------------------------------------------------
    // Device lookup
    // -------------------------------------------------------------------------

    /**
     * Finds a registered device by its ID.
     * <p>
     * Returns {@code null} when no matching device is found.
     */
    public Device findDevice(String deviceId) {
        for (int i = 0; i < deviceCount; i++) {
            if (devices[i] != null && devices[i].getDeviceId().equals(deviceId)) {
                return devices[i];
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Command routing
    // -------------------------------------------------------------------------

    /**
     * Parses {@code fullCommand}, resolves the target device, and delegates
     * execution to {@link Device#executeCommand(String)}.
     * <p>
     * Full command format: {@code "DEVICE_ID ACTION [VALUE]"}
     * Example: {@code "LIGHT_01 SET_BRIGHTNESS 75"}
     *
     * @param fullCommand the full command string
     */
    public void sendCommand(String fullCommand) throws HomeAutomationException {
        String deviceId = CommandParser.extractDeviceId(fullCommand);
        String command = CommandParser.extractCommand(fullCommand);

        try {
            logger.debug("Command recieved: deviceId={}, command={}", deviceId, fullCommand);
            Device device = findDevice(deviceId);

            if (device == null) {
                throw new DeviceNotFoundException("Device not found: " + deviceId);
            }

            if (!device.isOnline()) {
                logger.warn("Device not online, command skipped: deviceId={}", deviceId);
                throw new DeviceOfflineException("Device is offline: " + deviceId);
            }

            device.executeCommand(command);
            logger.info("Command finished successfully: deviceId={}, command={}", deviceId, command);
        } catch (HomeAutomationException e) {
            throw new HomeAutomationException(
                    "Command '" + fullCommand + "' failed for device '" + deviceId + "'", e);
        } finally {
            logger.info("Command processing ended for device {}", deviceId);
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Prints the status of every registered device.
     */
    public void printAllDevices() {
        logger.info("Registered devices: {}/{}", deviceCount, MAX_DEVICES);
        for (int i = 0; i < deviceCount; i++) {
            logger.info("  {}", devices[i]);
        }
    }

    public int getDeviceCount() {
        return deviceCount;
    }
}
