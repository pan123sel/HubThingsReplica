/**
*  Copyright 2023 Bloodtick
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/
@SuppressWarnings('unused')
public static String version() {return "1.3.0"}

metadata 
{
    definition(name: "Replica August Lock", namespace: "replica", author: "pan123sel", importUrl:"")
    {
        capability "Actuator"
        capability "Battery"
        capability "Configuration"
        capability "Lock"
        capability "Refresh"
       
        capability "ContactSensor"

        /**
        capability "healthCheck"
        attribute "scanCodes", "string" //capability "lockCodes" in SmartThings
        attribute "minCodeLength", "number" //capability "lockCodes" in SmartThings
        attribute "maxCodeLength", "number" //capability "lockCodes" in SmartThings
        attribute "codeReport", "number" //capability "lockCodes" in SmartThings
        */
        attribute "healthStatus", "enum", ["offline", "online"]
        /**

        command "updateCodes", [[name: "codes*", type: "JSON_OBJECT", description: """Update to these codes as {"1":"Update","2":"Codes"}"""]] //capability "lockCodes" in SmartThings
        command "unlockWithTimeout" //capability "lockCodes" in SmartThings
        command "requestCode", [[name: "codeSlot*", type: "NUMBER", description: "Code Slot Number"]] //capability "lockCodes" in SmartThings
        command "nameSlot", [[name: "codeSlot*", type: "NUMBER", description: "Code Slot Number"],[name: "codeName*", type: "STRING", description: "Name of this Slot"]] //capability "lockCodes" in SmartThings
        command "reloadAllCodes" //capability "lockCodes" in SmartThings
        */
    }
    preferences {
        input(name:"deviceInfoDisable", type: "bool", title: "Disable Info logging:", defaultValue: false)
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()    
}

def initialize() {
    updateDataValue("triggers", groovy.json.JsonOutput.toJson(getReplicaTriggers()))
    updateDataValue("commands", groovy.json.JsonOutput.toJson(getReplicaCommands()))
}

def configure() {
    logInfo "${device.displayName} configured default rules"
    initialize()
    updateDataValue("rules", getReplicaRules())
    sendCommand("configure")
}

// Methods documented here will show up in the Replica Command Configuration. These should be mostly setter in nature. 
Map getReplicaCommands() {
    return ([ "setBatteryValue":[[name:"battery*",type:"NUMBER"]], "setLockValue":[[name:"lock*",type:"ENUM"]], "setLockLocked":[],
              "setLockUnlockedWithTimeout":[], "setLockUnLocked":[], "setLockUnknown":[], "setHealthStatusValue":[[name:"healthStatus*",type:"ENUM"]], 
               "setContactSensorValue":[[name:"contact*",type:"ENUM"]], "setContactSensorOpen" :[], "setContactSensorClosed" :[]])
}

def setBatteryValue(value) {
    String descriptionText = "${device.displayName} battery level is $value %"
    sendEvent(name: "battery", value: value, unit: "%", descriptionText: descriptionText)
    logInfo descriptionText
}

def setLockValue(value) {
    String descriptionText = "${device.displayName} is $value"
    sendEvent(name: "lock", value: value, descriptionText: descriptionText)
    logInfo descriptionText
}

def setContactSensorValue(value) {
    String descriptionText = "${device.displayName} contact is $value"
    sendEvent(name: "contact", value: value, descriptionText: descriptionText)
    logInfo descriptionText
}

def setLockLocked() {
    setLockValue("locked")
}

def setLockUnlockedWithTimeout() {
    setLockValue("unlocked with timeout")    
}

def setLockUnlocked() {
    setLockValue("unlocked")    
}

def setLockUnknown() {
    setLockValue("unknown")    
}

def setContactSensorOpen() {
    setContactSensorValue("open")
}

def setContactSensorClosed() {
    setContactSensorValue("closed")
}

def setHealthStatusValue(value) {    
    sendEvent(name: "healthStatus", value: value, descriptionText: "${device.displayName} healthStatus set to $value")
}

// Methods documented here will show up in the Replica Trigger Configuration. These should be all of the native capability commands
Map getReplicaTriggers() {
    return ([ "unlockWithTimeout":[], "lock":[], "unlock":[], "refresh":[]])
}

private def sendCommand(String name, def value=null, String unit=null, data=[:]) { 
    data.version=version()
    parent?.deviceTriggerHandler(device, [name:name, value:value, unit:unit, data:data, now:now()])
}

def lock() { 
    sendCommand("lock")
}

def unlock() {
    sendCommand("unlock")
}

void refresh() {
    sendCommand("refresh")
}
/*
String getReplicaRules() {
    return """{"version":1, "components":[{"trigger":{"name":"unlock","label":"command: unlock()","type":"command"},"command":{"name":"unlock","type":"command","capability":"lock",\
"label":"command: lock:unlock()"},"type":"hubitatTrigger"},{"trigger":{"name":"lock","label":"command: lock()","type":"command"},"command":{"name":"lock","type":"command","capability":"lock","label":"command: lock:lock()"},"type":"hubitatTrigger"}]}"""
}
*/
String getReplicaRules() {
    return """{"version":1,"components":[{"trigger":{"type":"attribute","properties":{"value":{"title":"ActivityState","type":"string"}},"additionalProperties":false,"required":["value"],"capability":"accelerationSensor","attribute":"acceleration","label":"attribute: acceleration.*"},\
	"command":{"name":"setAccelerationValue","label":"command: setAccelerationValue(acceleration*)","type":"command","parameters":[{"name":"acceleration*","type":"ENUM"}]},"type":"smartTrigger"},{"trigger":{"title":"IntegerPercent","type":"attribute",\
	"properties":{"value":{"type":"integer","minimum":0,"maximum":100},"unit":{"type":"string","enum":["%"],"default":"%"}},"additionalProperties":false,"required":["value"],"capability":"battery","attribute":"battery","label":"attribute: battery.*"},\
	"command":{"name":"setBatteryValue","label":"command: setBatteryValue(battery*)","type":"command","parameters":[{"name":"battery*","type":"NUMBER"}]},"type":"smartTrigger","mute":true},{"trigger":{"type":"attribute","properties":{"value":{"title":"ContactState",\
	"type":"string"}},"additionalProperties":false,"required":["value"],"capability":"contactSensor","attribute":"contact","label":"attribute: contact.*"},"command":{"name":"setContactSensorValue","label":"command: setContactSensorValue(contact*)",\
	"type":"command","parameters":[{"name":"contact*","type":"ENUM"}]},"type":"smartTrigger"},{"trigger":{"type":"attribute","properties":{"value":{"title":"HealthState","type":"string"}},"additionalProperties":false,"required":["value"],\
	"capability":"healthCheck","attribute":"healthStatus","label":"attribute: healthStatus.*"},"command":{"name":"setHealthStatusValue","label":"command: setHealthStatusValue(healthStatus*)","type":"command","parameters":[{"name":"healthStatus*","type":"ENUM"}]},\
	"type":"smartTrigger","mute":true},{"trigger":{"name":"refresh","label":"command: refresh()","type":"command"},"command":{"name":"refresh","type":"command","capability":"refresh","label":"command: refresh()"},"type":"hubitatTrigger"},\
	{"trigger":{"name":"unlockWithTimeout","label":"command: unlockWithTimeout()","type":"command"},"command":{"name":"unlockWithTimeout","type":"command","capability":"lockCodes","label":"command: unlockWithTimeout()"},"type":"hubitatTrigger"},\
	{"trigger":{"type":"attribute","properties":{"value":{"title":"LockState","type":"string"},"data":{"type":"object","additionalProperties":false,"required":[],"properties":{"method":{"enum":["manual","keypad","auto","command","rfid","fingerprint","bluetooth"],"type":"string"},"codeName":{"type":"string"},"codeId":{"type":"string"},\
	"timeout":{"pattern":"removed","title":"Iso8601Date","type":"string"}}}},"additionalProperties":false,"required":["value"],"capability":"lockCodes","attribute":"lock","label":"attribute: lock.*"},"command":{"name":"setLockValue","label":"command: setLockValue(lock*)","type":"command","parameters":[{"name":"lock*","type":"ENUM"}]},"type":"smartTrigger"},\
	{"trigger":{"name":"unlock","label":"command: unlock()","type":"command"},"command":{"name":"unlock","type":"command","capability":"lock","label":"command: lock:unlock()"},"type":"hubitatTrigger"},\
	{"trigger":{"name":"lock","label":"command: lock()","type":"command"},"command":{"name":"lock","type":"command","capability":"lock","label":"command: lock:lock()"},"type":"hubitatTrigger"}]}"""

/*
{"trigger":{"type":"attribute","properties":{"value":{"title":"HealthState","type":"string"}},"additionalProperties":false,"required":["value"],"capability":"healthCheck","attribute":"healthStatus","label":"attribute: healthStatus.*"},"command":{"name":"setHealthStatusValue","label":"command: setHealthStatusValue(healthStatus*)","type":"command","parameters":[{"name":"healthStatus*","type":"ENUM"}]},"type":"smartTrigger","mute":true}
{"trigger":{"title":"IntegerPercent","type":"attribute","properties":{"value":{"type":"integer","minimum":0,"maximum":100},"unit":{"type":"string","enum":["%"],"default":"%"}},"additionalProperties":false,"required":["value"],"capability":"battery","attribute":"battery","label":"attribute: battery.*"},"command":{"name":"setBatteryValue","label":"command: setBatteryValue(battery*)","type":"command","parameters":[{"name":"battery*","type":"NUMBER"}]},"type":"smartTrigger","mute":true}
*/
}

private logInfo(msg)  { if(settings?.deviceInfoDisable != true) { log.info  "${msg}" } }
private logDebug(msg) { if(settings?.deviceDebugEnable == true) { log.debug "${msg}" } }
private logTrace(msg) { if(settings?.deviceTraceEnable == true) { log.trace "${msg}" } }
private logWarn(msg)  { log.warn   "${msg}" }
private logError(msg) { log.error  "${msg}" }
