package com.bootcamp.smarthome;

import com.bootcamp.smarthome.controller.HomeController;
import com.bootcamp.smarthome.device.SmartLight;
import com.bootcamp.smarthome.device.SmartLock;
import com.bootcamp.smarthome.device.SmartThermostat;
import com.bootcamp.smarthome.exception.DeviceNotFoundException;
import com.bootcamp.smarthome.exception.HomeAutomationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Smart Home Controller demo.
 *
 * Registers several smart devices, then runs a series of scenarios that
 * exercise normal operation as well as edge cases and error conditions.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HomeController controller = setupDevices();
        normalCommands(controller);
        setBrightness(controller);
        invalidTemperature(controller);
        offlineDevice(controller);
        unlockWithCorrectPin(controller);
        unlockWithNullPin(controller);
        findNonExistentDevice(controller);
        logger.info("=== All scenarios complete ===");
        controller.printAllDevices();
    }

    private static HomeController setupDevices() {
        logger.info("=== Setting up devices ===");
        HomeController controller = new HomeController();
        controller.addDevice(new SmartLight("LIGHT_01", "Living Room Light", true));
        controller.addDevice(new SmartLight("LIGHT_02", "Bedroom Light", true));
        controller.addDevice(new SmartThermostat("THERMO_01", "Main Thermostat", true));
        controller.addDevice(new SmartLock("LOCK_01", "Front Door Lock", true, "4321"));
        controller.addDevice(new SmartLight("LIGHT_03", "Kitchen Light", false)); // offline
        controller.addDevice(new SmartThermostat("THERMO_02", "Bedroom Thermostat", true));
        controller.addDevice(new SmartLight("LIGHT_04", "Hallway Light", true));
        controller.addDevice(new SmartLock("LOCK_02", "Back Door Lock", true, "9999"));
        // Array is now FULL (8/8 devices)
        controller.printAllDevices();
        return controller;
    }

    private static void normalCommands(HomeController controller) {
        logger.info("=== Scenario 1: Normal device commands ===");
        try {
            controller.sendCommand("LIGHT_01 TURN_ON");
            controller.sendCommand("LIGHT_02 TURN_ON");
            controller.sendCommand("THERMO_01 SET_TEMP 22.5");
        } catch (HomeAutomationException e) {
            logger.error("Could not run normal device commands", e);
        }
    }

    private static void setBrightness(HomeController controller) {
        logger.info("=== Scenario 2: Set brightness ===");
        try {
            controller.sendCommand("LIGHT_01 SET_BRIGHTNESS 80");
        } catch (HomeAutomationException e) {
            logger.error("Could not set brightness", e);
        }
    }

    private static void invalidTemperature(HomeController controller) {
        logger.info("=== Scenario 3: Invalid temperature ===");
        try {
            // Calls setTemperature() directly to isolate temperature validation
            SmartThermostat thermostat = (SmartThermostat) controller.findDevice("THERMO_01");
            thermostat.setTemperature(99.0);
        } catch (HomeAutomationException e) {
            logger.error("Could not set temperature", e);
        }
    }

    private static void offlineDevice(HomeController controller) {
        logger.info("=== Scenario 4: Offline device ===");
        try {
            // LIGHT_03 is offline, so the command should be skipped with a warning
            controller.sendCommand("LIGHT_03 TURN_ON");
        } catch (HomeAutomationException e) {
            logger.error("Could not send command to offline device", e);
        }
    }

    private static void unlockWithCorrectPin(HomeController controller) {
        logger.info("=== Scenario 5: Unlock with correct PIN ===");
        try {
            SmartLock frontDoor = (SmartLock) controller.findDevice("LOCK_01");
            frontDoor.validatePin("4321");
        } catch (HomeAutomationException e) {
            logger.error("Could not unlock with correct PIN", e);
        }
    }

    private static void unlockWithNullPin(HomeController controller) {
        logger.info("=== Scenario 6: Unlock with null PIN ===");
        try {
            controller.sendCommand("LOCK_02 UNLOCK");
        } catch (HomeAutomationException e) {
            logger.error("Could not unlock without PIN", e);
        }
    }

    private static void findNonExistentDevice(HomeController controller) {
        logger.info("=== Scenario 7: Find non-existent device ===");
        try {
            controller.sendCommand("SENSOR_99 TURN_ON");
        } catch (DeviceNotFoundException e) {
            logger.error("Device not found", e);
        } catch (HomeAutomationException e) {
            logger.error("Could not send command to device", e);
        }
    }
}
