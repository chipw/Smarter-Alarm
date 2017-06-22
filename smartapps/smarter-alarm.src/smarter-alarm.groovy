/**
 *  Smarter Alarm is a multi-zone virtual alarm panel, featuring customizable
 *  security zones. Setting of an alarm can activate sirens, turn on light
 *  switches, push notification and text message. Alarm is armed and disarmed
 *  simply by setting SmartThings location 'mode'.
 *
 *  Please visit <http://statusbits.github.io/smartalarm/> for more
 *  information.
 *
 *  Version 2.6.4 (6/21/2017)
 *
 *  The latest version of this file can be found on GitHub at:
 *  <https://github.com/statusbits/smartalarm/blob/master/SmartAlarm.groovy>
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2017 Statusbits.com + CHIPW
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import groovy.json.JsonSlurper

definition(
    name: "Smarter Alarm",
    namespace: "chipw",
    author: "chip.warner@gmail.com",
    description: '''A multi-zone virtual alarm panel, featuring customizable\
 security zones. Setting of an alarm can activate sirens, turn on light\
 switches, push notification and text message. Alarm is armed and disarmed\
 simply by setting Smart Home Monitor 'status' OR Hub 'Mode'.''',
    category: "Safety & Security",
    iconUrl: "http://statusbits.github.io/icons/SmartAlarm-128.png",
    iconX2Url: "http://statusbits.github.io/icons/SmartAlarm-256.png",
    oauth: [displayName:"Smarter Alarm", displayLink:"http://www.smartthings.com"]
)

private def getVersion() {
    return "2.6.4"
}

private def textCopyright() {
    def text = "Copyright © 2017 Statusbits.com + CHIPW"
}

private def textLicense() {
    def text =
        "This program is free software: you can redistribute it and/or " +
        "modify it under the terms of the GNU General Public License as " +
        "published by the Free Software Foundation, either version 3 of " +
        "the License, or (at your option) any later version.\n\n" +
        "This program is distributed in the hope that it will be useful, " +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
        "General Public License for more details.\n\n" +
        "You should have received a copy of the GNU General Public License " +
        "along with this program. If not, see <http://www.gnu.org/licenses/>."
}

preferences {
    page name:"pageSetup"
    page name:"pageAbout"
    page name:"pageUninstall"
    page name:"pageStatus"
    page name:"pageHistory"
    page name:"pageSelectZones"
    page name:"pageConfigureZones"
    page name:"pageArmingOptions"
    page name:"pageAlarmOptions"
    page name:"pageNotifications"
	page(name: "pagePB", title: "Pushbullet Configuration", nextPage: "pageNotifications") {
	section("Pushbullet Devices") {
        	input(name: "pushbullets", type: "device.pushbullet", title: "Which Pushbullet devices?", multiple: true, submitOnChange: true, required: false)
    }
	section("Pushbullet Notifications") {
			input(name: "pushbulletAlarm", type: "bool", title: "Notify on Alarm", defaultValue: true)
			input(name: "pushbulletStatus", type: "bool", title: "Notify on Status Change", defaultValue: true)
        }
	}    
    page name:"pageRemoteOptions"
    page name:"pageRestApiOptions"
}

mappings {
    path("/armaway") {
        action: [ GET: "apiArmAway" ]
    }

    path("/armaway/:pincode") {
        action: [ GET: "apiArmAway" ]
    }

    path("/armstay") {
        action: [ GET: "apiArmStay" ]
    }

    path("/armstay/:pincode") {
        action: [ GET: "apiArmStay" ]
    }

    path("/disarm") {
        action: [ GET: "apiDisarm" ]
    }

    path("/disarm/:pincode") {
        action: [ GET: "apiDisarm" ]
    }

    path("/panic") {
        action: [ GET: "apiPanic" ]
    }

    path("/status") {
        action: [ GET: "apiStatus" ]
    }
}

// Show setup page
def pageSetup() {
    LOG("pageSetup()")

    if (state.version != getVersion()) {
        return setupInit() ? pageAbout() : pageUninstall()
    }

    if (getNumZones() == 0) {
        return pageSelectZones()
    }

    def alarmStatus = "Alarm is ${getAlarmStatus()} in ${getHubMode()} mode"

    def pageProperties = [
        name:       "pageSetup",
        //title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section("Status") {
            if (state.zones.size() > 0) {
                href "pageStatus", title:alarmStatus, description:"Tap for more information"
            } else {
                paragraph alarmStatus
            }
            if (state.history.size() > 0) {
                href "pageHistory", title:"Event History", description:"Tap to view"
            }
        }
        section("Setup Menu") {
            href "pageSelectZones", title:"Add/Remove Sensors", description:""
            href "pageConfigureZones", title:"Configure Zones (for Sensors)", description:""
            href "pageArmingOptions", title:"Arming/Disarming Options", description:""
            href "pageAlarmOptions", title:"Alarm Options", description:""
            href "pageNotifications", title:"Notification Options", description:""
        }
		section("About") {
            href "pageAbout", title:"About Smarter Alarm", description:""
   	    }
        section(hideable: true, hidden: true, "Advanced") {
            href "pageRemoteOptions", title:"Remote Control Options", description:""
            href "pageRestApiOptions", title:"REST API Options", description:""
       	    label title:"Assign a name", required:false
        }
    }
}

// Show "About" page
def pageAbout() {
    LOG("pageAbout()")

    def textAbout =
        "Version ${getVersion()}\n${textCopyright()}\n\n" +
        "You can contribute to the development of this app by making " +
        "donation to the project."

    def hrefInfo = [
        url:        "https://github.com/chipw/Smarter-Alarm",
        style:      "embedded",
        title:      "Tap here for more information...",
        description:"https://github.com/chipw/Smarter-Alarm",
        required:   false
    ]

    def pageProperties = [
        name:       "pageAbout",
        //title:      "About",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("About") {
            paragraph textAbout
            href hrefInfo
        }
        section("License") {
            paragraph textLicense()
        }
    }
}

// Show "Uninstall" page
def pageUninstall() {
    LOG("pageUninstall()")

    def text =
        "Smarter Alarm version ${getVersion()} is not backward compatible " +
        "with the currently installed version. Please uninstall the " +
        "current version by tapping the Uninstall button below, then " +
        "re-install Smarter Alarm from the Dashboard. We are sorry for the " +
        "inconvenience."

    def pageProperties = [
        name:       "pageUninstall",
        title:      "Warning!",
        nextPage:   null,
        uninstall:  true,
        install:    false
    ]

    return dynamicPage(pageProperties) {
        section("Uninstall Required") {
            paragraph text
        }
    }
}

// Show "Status" page
def pageStatus() {
    LOG("pageStatus()")

    def pageProperties = [
        name:       "pageStatus",
        //title:      "Status",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Status") {
            paragraph "Alarm is ${getAlarmStatus()}"
            paragraph "Hub Mode is ${getHubMode()}"
        }

        if (settings.z_contact) {
            section("Contact Sensors") {
                settings.z_contact.each() {
                    def text = getZoneStatus(it, "contact")
                    if (text) {
                        paragraph text
                    }
                }
            }
        }

        if (settings.z_motion) {
            section("Motion Sensors") {
                settings.z_motion.each() {
                    def text = getZoneStatus(it, "motion")
                    if (text) {
                        paragraph text
                    }
                }
            }
        }

        if (settings.z_movement) {
            section("Movement Sensors") {
                settings.z_movement.each() {
                    def text = getZoneStatus(it, "acceleration")
                    if (text) {
                        paragraph text
                    }
                }
            }
        }

        if (settings.z_smoke) {
            section("Smoke & CO Sensors") {
                settings.z_smoke.each() {
                    def text = getZoneStatus(it, "smoke")
                    if (text) {
                        paragraph text
                    }
                }
            }
        }

        if (settings.z_water) {
            section("Moisture Sensors") {
                settings.z_water.each() {
                    def text = getZoneStatus(it, "water")
                    if (text) {
                        paragraph text
                    }
                }
            }
        }
    }
}

// Show "History" page
def pageHistory() {
    LOG("pageHistory()")

    def pageProperties = [
        name:       "pageHistory",
        //title:      "Event History",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    def history = atomicState.history

    return dynamicPage(pageProperties) {
        section("Event History") {
            if (history.size() == 0) {
                paragraph "No history available."
            } else {
            	history.each() {
                	def text = ""  + new Date(it.time + location.timeZone.rawOffset ).format("yyyy-MM-dd HH:mm") + ": " + it.event + " - " + it.description
                	paragraph text
                }
            }
        }
    }
}

// Show "Add/Remove Zones (Sensors)" page
def pageSelectZones() {
    LOG("pageSelectZones()")

    def helpPage =
        "A security zone is an area of your property protected by a Sensor " +
        "(contact, motion, movement, moisture or smoke)."

    def inputContact = [
        name:       "z_contact",
        type:       "capability.contactSensor",
        title:      "Which contact sensors?",
        multiple:   true,
        required:   false
    ]

    def inputMotion = [
        name:       "z_motion",
        type:       "capability.motionSensor",
        title:      "Which motion sensors?",
        multiple:   true,
        required:   false
    ]

    def inputMovement = [
        name:       "z_movement",
        type:       "capability.accelerationSensor",
        title:      "Which movement sensors?",
        multiple:   true,
        required:   false
    ]

    def inputSmoke = [
        name:       "z_smoke",
        type:       "capability.smokeDetector",
        title:      "Which smoke & CO sensors?",
        multiple:   true,
        required:   false
    ]

    def inputMoisture = [
        name:       "z_water",
        type:       "capability.waterSensor",
        title:      "Which moisture sensors?",
        multiple:   true,
        required:   false
    ]

    def pageProperties = [
        name:       "pageSelectZones",
        //title:      "Add/Remove Zones",
        nextPage:   "pageConfigureZones",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Add/Remove Zones (Sensors)") {
            paragraph helpPage
            input inputContact
            input inputMotion
            input inputMovement
            input inputSmoke
            input inputMoisture
        }
    }
}

// Show "Configure Zones & Sensors" page
def pageConfigureZones() {
    LOG("pageConfigureZones()")

    def helpZones =
        "Security zones can be configured as either Exterior, Interior, " +
        "Alert or Bypass. Exterior zones are armed in both Away and Stay " +
        "modes, while Interior zones are armed only in Away mode, allowing " +
        "you to move freely inside the premises while the alarm is armed " +
        "in Stay mode. Alert zones are always armed and are typically used " +
        "for smoke and flood alarms. Bypass zones are never armed. This " +
        "allows you to temporarily exclude a zone from your security " +
        "system.\n\n" +
        "You can disable Entry and Exit Delays for individual zones."

    def zoneTypes = ["exterior", "interior", "alert", "bypass"]

    def pageProperties = [
        name:       "pageConfigureZones",
        //title:      "Configure Zones",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Configure Zones (for Sensors)") {
            paragraph helpZones
        }

        if (settings.z_contact) {
            def devices = settings.z_contact.sort {it.displayName}
            devices.each() {
                def devId = it.id
                section("${it.displayName} (contact)") {
                    input "type_${devId}", "enum", title:"Zone Type", metadata:[values:zoneTypes], defaultValue:"exterior"
                    input "delay_${devId}", "bool", title:"Entry/Exit Delays", defaultValue:true
                    input "chime_${devId}", "bool", title:"Chime on open", defaultValue:true
                }
            }
        }

        if (settings.z_motion) {
            def devices = settings.z_motion.sort {it.displayName}
            devices.each() {
                def devId = it.id
                section("${it.displayName} (motion)") {
                    input "type_${devId}", "enum", title:"Zone Type", metadata:[values:zoneTypes], defaultValue:"interior"
                    input "delay_${devId}", "bool", title:"Entry/Exit Delays", defaultValue:false
                    input "chime_${devId}", "bool", title:"Chime on motion", defaultValue:false
                }
            }
        }

        if (settings.z_movement) {
            def devices = settings.z_movement.sort {it.displayName}
            devices.each() {
                def devId = it.id
                section("${it.displayName} (movement)") {
                    input "type_${devId}", "enum", title:"Zone Type", metadata:[values:zoneTypes], defaultValue:"interior"
                    input "delay_${devId}", "bool", title:"Entry/Exit Delays", defaultValue:false
                    input "chime_${devId}", "bool", title:"Chime on movement", defaultValue:false
                }
            }
        }

        if (settings.z_smoke) {
            def devices = settings.z_smoke.sort {it.displayName}
            devices.each() {
                def devId = it.id
                section("${it.displayName} (smoke)") {
                    input "type_${devId}", "enum", title:"Zone Type", metadata:[values:zoneTypes], defaultValue:"alert"
                    input "delay_${devId}", "bool", title:"Entry/Exit Delays", defaultValue:false
                    input "chime_${devId}", "bool", title:"Chime on smoke", defaultValue:false
                }
            }
        }

        if (settings.z_water) {
            def devices = settings.z_water.sort {it.displayName}
            devices.each() {
                def devId = it.id
                section("${it.displayName} (moisture)") {
                    input "type_${devId}", "enum", title:"Zone Type", metadata:[values:zoneTypes], defaultValue:"alert"
                    input "delay_${devId}", "bool", title:"Entry/Exit Delays", defaultValue:false
                    input "chime_${devId}", "bool", title:"Chime on water", defaultValue:false
                }
            }
        }
    }
}

// Show "Arming/Disarming Options" page
def pageArmingOptions() {
    LOG("pageArmingOptions()")

    def helpArming =
        "Smarter Alarm can be armed and disarmed by syncing the SHM status. " +
        "There are two arming modes - Stay and Away. Interior zones are " +
        "not armed in Stay mode, allowing you to move freely inside your " +
        "home."

    def helpDelay =
        "Exit and entry delay allows you to exit the premises after arming " +
        "your alarm system and enter the premises while the alarm system " +
        "is armed without setting off an alarm. You can optionally disable " +
        "entry and exit delay when the alarm is armed in Stay mode."

	def inputKeypads = [
    	name:		"keypads",
        type: 		"capability.lockCodes",
        title: 		"Keypads for Exit / Entry delay",
        multiple:	true,
        required:	false
    ]

    def inputAwayModes = [
        name:       "awayModes",
        type:       "mode",
        title:      "Arm 'Away' in these Modes",
        multiple:   true,
        required:   false
    ]

    def inputStayModes = [
        name:       "stayModes",
        type:       "mode",
        title:      "Arm 'Stay' in these Modes",
        multiple:   true,
        required:   false
    ]

    def inputDisarmModes = [
        name:       "disarmModes",
        type:       "mode",
        title:      "Disarm in these Modes",
        multiple:   true,
        required:   false
    ]
    
    def inputAwayModeHub = [
        name:       "awayModeHub",
        type:       "mode",
        title:      "Set Hub Mode to this when Armed/Away",
        multiple:   false,
        required:   false
    ]

    def inputStayModeHub = [
        name:       "stayModeHub",
        type:       "mode",
        title:      "Set Hub Mode to this when Armed/Stay",
        multiple:   false,
        required:   false
    ]

    def inputDisarmModeHub = [
        name:       "disarmModeHub",
        type:       "mode",
        title:      "Set Hub Mode to this when Disarmed",
        multiple:   false,
        required:   false
    ]    

    def inputExitEntryModeHub = [
        name:       "exitentryModeHub",
        type:       "mode",
        title:      "Set Hub Mode to this when Exit or Entry Delay",
        multiple:   false,
        required:   false
    ]    

    def inputAlarmModeHub = [
        name:       "alarmModeHub",
        type:       "mode",
        title:      "Set Hub Mode to this when Alarm activated",
        multiple:   false,
        required:   false
    ]    

    def inputDelay = [
        name:       "delay",
        type:       "enum",
        metadata:   [values:["30","45","60","90"]],
        title:      "Delay (in seconds)",
        defaultValue: "30",
        required:   true
    ]

    def inputDelayStay = [
        name:       "stayDelayOff",
        type:       "bool",
        title:      "Disable alarm (entry) delay in Stay mode",
        defaultValue: false,
        required:   true
    ]
    
    def inputExitDelayStay = [
    	name:			"stayExitDelayOff",
        type:			"bool",
        title:			"Disable arming (exit) delay in Stay mode",
        defaultValue: 	true,
        required:		true
    ]
    
    def inputSyncWithSHM = [
    	name:			"SyncWithSHM",
        type:			"bool",
        title:			"Sync status with Smart Home Monitor",
        defaultValue: 	true,
        submitOnChange:	true,
        required:		true
    ]    
        
	def inputTriggerSHM = [
    	name:			"TriggerSHM",
        type:			"bool",
        title:			"Trigger SHM ALARM using Sensor",
        defaultValue: 	false,
        submitOnChange:	true,
        required:		true
    ]    

	def inputVirtualswitchSHM = [
    	name:		"VirtualswitchSHM",
        type: 		"capability.doorControl",
        title: 		"Virtual Door Sensor for SHM ALARM trigger",
        multiple:	false,
        required:	false
    ]
    
    def inputSwitchesOnArmed = [
        name:           "SwitchesOnArmed",
        type:           "capability.switch",
        title:          "Set switches: Armed=ON, Disarmed=OFF",
        multiple:       true,
        required:       false
    ]        
        
    def inputChimeDevices = [
    	name:			"chimeDevices",
        type:           "capability.tone",
        title:          "Which Chime Devices?",
        multiple:       true,
        required:       false
    ]

    def inputChimeOnStatusChange = [
    	name:			"ChimeOnStatusChange",
        type:			"bool",
        title:			"Chime on Arm/Disarm Update",
        defaultValue: 	true,
        submitOnChange:	true,
        required:		true
    ]    
    
    def pageProperties = [
        name:       "pageArmingOptions",
        //title:      "Arming/Disarming Options",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Arming/Disarming Options") {
            paragraph helpArming
        }

		section("Sync 'armed' status with Smart Home Monitor (SHM)") {
        	input inputSyncWithSHM
            input inputTriggerSHM
            if (TriggerSHM) {
            	input inputVirtualswitchSHM
            }
        }

		section("Set Hub Mode based on Arm/Disarm status") {
            input inputAwayModeHub
            input inputStayModeHub
            input inputDisarmModeHub
            input inputExitEntryModeHub
            input inputAlarmModeHub
        }

		section("Keypads") {
        	input inputKeypads
        }

		section("Exit and Entry Delay") {
            paragraph helpDelay
            input inputDelay
            input inputDelayStay
			input inputExitDelayStay
        }
        section("Chime Devices") {
			input inputChimeDevices
            input inputChimeOnStatusChange
        }
        if (ChimeOnStatusChange == true) {
        section("Quiet Hours (do not Chime for Arm/Disarm Change)") {
        	input "QuietfromTime", "time", title: "From", required: true
        	input "QuiettoTime", "time", title: "To", required: true
        }
		}
        
		section("Switches") {
			input inputSwitchesOnArmed
        }        
        
		section(hideable: true, hidden: settings.SyncWithSHM, (settings.SyncWithSHM) ? "Auto Arm/Disarm when Hub Modes change (Caution: Dont Use This When Using SHM Sync)" : "Auto Arm/Disarm when Hub Modes change (Optional)") {
            input inputAwayModes
            input inputStayModes
            input inputDisarmModes
        }
    }
}

// Show "Alarm Options" page
def pageAlarmOptions() {
    LOG("pageAlarmOptions()")

    def helpAlarm =
        "You can configure Smarter Alarm to take several actions when an " +
        "alarm is set off, such as turning on sirens and light switches, " +
        "taking camera snapshots and executing a 'Hello, Home' action."

    def inputAlarms = [
        name:           "alarms",
        type:           "capability.alarm",
        title:          "Which sirens?",
        multiple:       true,
        submitOnChange:	true,
        required:       false
    ]

    def inputSirenMode = [
        name:           "sirenMode",
        type:           "enum",
        metadata:       [values:["Off","Siren","Strobe","Both"]],
        title:          "Choose siren mode",
        defaultValue:   "Both"
    ]

    def inputSirenEntryStrobe = [
    	name:           "sirenEntryStrobe",
        type:           "bool",
        title:          "Strobe siren during entry delay",
        defaultValue:   true,
        required:       true
    ]
    
   def inputSirenOnWaterAlert = [
        name:       "sirenOnWaterAlert",
        type:       "bool",
        title:      "Use Siren for Water Leak?",
        defaultValue: true,
        required:   true
    ]
     
   def inputSirenOnSmokeAlert = [
        name:       "sirenOnSmokeAlert",
        type:       "bool",
        title:      "Use Siren for Smoke Alert?",
        defaultValue: true,
        required:   true
    ]
    
   def inputSirenOnIntrusionAlert = [
        name:       "sirenOnIntrusionAlert",
        type:       "bool",
        title:      "Use Siren for Intrusion Alarm?",
        defaultValue: true,
        required:   true
    ]

    def inputCameras = [
        name:           "cameras",
        type:           "capability.imageCapture",
        title:          "Which cameras?",
        multiple:       true,
        required:       false
    ]

    def hhActions = getHelloHomeActions()
    def inputHelloHome = [
        name:           "helloHomeAction",
        type:           "enum",
        title:          "Which 'Hello, Home' action?",
        metadata:       [values: hhActions],
        required:       false
    ]

def inputSwitches = [
        name:           "switches",
        type:           "capability.switch",
        title:          "Set switches: Alarm=ON, No Alarms=OFF",
        multiple:       true,
        required:       false
    ]        

    def pageProperties = [
        name:       "pageAlarmOptions",
        //title:      "Alarm Options",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Alarm Options") {
            paragraph helpAlarm
        }
        section("Sirens for Alarms") {
            input inputAlarms
            input inputSirenMode
            input inputSirenEntryStrobe
            if (inputAlarms) {
            input inputSirenOnWaterAlert
            input inputSirenOnSmokeAlert
            input inputSirenOnIntrusionAlert
            }
        }
        section("Cameras") {
            input inputCameras
        }
        section("'Hello, Home' Actions") {
            input inputHelloHome
        }
		section("Switches") {
            input inputSwitches
        }        
    }
}

// Show "Notification Options" page
def pageNotifications() {
    LOG("pageNotifications()")

    def helpAbout =
        "You can configure Smarter Alarm to notify you when it is armed, " +
        "disarmed or when an alarm is set off. Notifications can be send " +
        "using either Push messages, SMS (text) messages and Pushbullet " +
        "messaging service. Smarter Alarm can also notify you with sounds " +
        "or voice alerts using compatible audio devices, such as Sonos. " +
        "Or using a Smarter Alarm dashboard virtual device."
    
    def inputNotificationDevice = [
        name:       "notificationDevice",
        type:       "capability.notification",
        title:      "Which Smarter Alarm notification device?",
        multiple:   false,
        required:   false
    ]

    def inputPushAlarm = [
        name:           "pushMessage",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   true
    ]

    def inputPushStatus = [
        name:           "pushStatusMessage",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   true
    ]

    def inputPhone1 = [
        name:           "phone1",
        type:           "phone",
        title:          "Send to this number",
        submitOnChange:	true,
        required:       false
    ]

    def inputPhone1Alarm = [
        name:           "smsAlarmPhone1",
        type:           "bool",
        title:          "Notify on Alarm",
        submitOnChange:	true,
        defaultValue:   false
    ]

    def inputPhone1Status = [
        name:           "smsStatusPhone1",
        type:           "bool",
        title:          "Notify on Status Change",
        submitOnChange:	true,
        defaultValue:   false
    ]

    def inputPhone2 = [
        name:           "phone2",
        type:           "phone",
        title:          "Send to this number",
        submitOnChange:	true,
        required:       false
    ]

    def inputPhone2Alarm = [
        name:           "smsAlarmPhone2",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone2Status = [
        name:           "smsStatusPhone2",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPhone3 = [
        name:           "phone3",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone3Alarm = [
        name:           "smsAlarmPhone3",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone3Status = [
        name:           "smsStatusPhone3",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPhone4 = [
        name:           "phone4",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone4Alarm = [
        name:           "smsAlarmPhone4",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone4Status = [
        name:           "smsStatusPhone4",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputAudioPlayers = [
        name:           "audioPlayer",
        type:           "capability.musicPlayer",
        title:          "Which audio players?",
        multiple:       true,
        submitOnChange:	true,
        required:       false
    ]

    def inputSpeechOnAlarm = [
        name:           "speechOnAlarm",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   true
    ]

    def inputSpeechOnStatus = [
        name:           "speechOnStatus",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   true
    ]

    def inputSpeechTextAlarm = [
        name:           "speechText",
        type:           "text",
        title:          "Alarm Phrase",
        required:       false
    ]

    def inputSpeechTextArmedAway = [
        name:           "speechTextArmedAway",
        type:           "text",
        title:          "Armed Away Phrase",
        required:       false
    ]

    def inputSpeechTextArmedStay = [
        name:           "speechTextArmedStay",
        type:           "text",
        title:          "Armed Stay Phrase",
        required:       false
    ]

    def inputSpeechTextDisarmed = [
        name:           "speechTextDisarmed",
        type:           "text",
        title:          "Disarmed Phrase",
        required:       false
    ]
    
	def inputHues = [
			name: "hues",
            type: "capability.colorControl",
            title: "Use which hue/color light bulbs?",
            multiple: true,
        	submitOnChange:	true,            
            required: false
     ]
       
   def colorsWithDisabled=["Disabled","Blue","Purple","Red","Pink","Orange","Yellow","Green","White", "Daylight", "Soft White", "Warm White", "Default (Initial Color)", "[TURN OFF]"]
  
  
	def inputWaterHueColor = [
        name:           "WaterHueColor",
        type:           "enum",
        title:          "Color for Water Leak Alert?",
        options:      	 colorsWithDisabled,
        required:       false,
        defaultValue: 	"Disabled",
        multiple: 		false
    ]
    
    
	def inputSmokeHueColor = [
        name:           "SmokeHueColor",
        type:           "enum",
        title:          "Color for Smoke/CO2 Alert?",
        options:      	 colorsWithDisabled,
        required:       false,
        defaultValue: 	"Disabled",
        multiple: 		false
    ]
     
	def inputIntrusionHueColor = [
        name:           "IntrusionHueColor",
        type:           "enum",
        title:          "Color for Intrusion Alarm?",
        options:      	 colorsWithDisabled,
        required:       false,
        defaultValue: 	"Disabled",
        multiple: 		false
    ]

	def inputArmedHueColor = [
        name:           "ArmedHueColor",
        type:           "enum",
        title:          "Color for Armed status?",
        options:      	 colorsWithDisabled,
        required:       false,
        defaultValue: 	"Disabled",
        multiple: 		false
    ]

	def inputDisarmedHueColor = [
        name:           "DisarmedHueColor",
        type:           "enum",
        title:          "Color for Disarmed status?",
        options:      	 colorsWithDisabled,
        required:       false,
        defaultValue: 	"Disabled",
        multiple: 		false
    ]

	def inputHueBrightness = [
  	 name: "hueBrightnessLevel", 
  	 type: "number", 
   	 title: "Hue Brightness Level (1-100)?", 
   	 required:false, defaultValue:100 
   ]

    def pageProperties = [
        name:       "pageNotifications",
        //title:      "Notification Options",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Notification Options") {
            paragraph helpAbout
        }
        section("Push Notifications") {
            input inputPushAlarm
            input inputPushStatus
        }
        section("Text Message (SMS)") {
            input inputPhone1
            input inputPhone1Alarm
            input inputPhone1Status
        }
        if (phone1) {
        section("Text Message (SMS) #2") {
            input inputPhone2
            input inputPhone2Alarm
            input inputPhone2Status
        }
        section("Text Message (SMS) #3") {
            input inputPhone3
            input inputPhone3Alarm
            input inputPhone3Status
        }
        section("Text Message (SMS) #4") {
            input inputPhone4
            input inputPhone4Alarm
            input inputPhone4Status
        }
        }
        section("Audio Notifications") {
            input inputAudioPlayers
            if (audioPlayer) {
            input inputSpeechOnAlarm
            input inputSpeechOnStatus
            input inputSpeechTextAlarm
            input inputSpeechTextArmedAway
            input inputSpeechTextArmedStay
            input inputSpeechTextDisarmed
            }
        }
        section("Pushbullet Notifications") {
        	href(name: "Configure Pushbullet",
                 title: "Configure Pushbullet",
                 required: false,
                 page: "pagePB")
        }         
        section("Notification Device")
        {
            input inputNotificationDevice
        }
        
        section("Hues/Color Light Bulbs for Alarms and Status") {
        	input inputHues
            if (hues) {
            input inputWaterHueColor
            input inputSmokeHueColor
            input inputIntrusionHueColor
            input inputArmedHueColor
            input inputDisarmedHueColor
            input inputHueBrightness
            
            }
        }        
    }
}

// Show "Remote Control Options" page
def pageRemoteOptions() {
    LOG("pageRemoteOptions()")

    def helpRemote =
        "You can arm and disarm Smarter Alarm using any compatible remote " +
        "control, for example Aeon Labs Minimote."

    def inputRemotes = [
        name:       "remotes",
        type:       "capability.button",
        title:      "Which remote controls?",
        multiple:   true,
        required:   false
    ]

    def inputArmAwayButton = [
        name:       "buttonArmAway",
        type:       "number",
        title:      "Which button?",
        required:   false
    ]

    def inputArmAwayHold = [
        name:       "holdArmAway",
        type:       "bool",
        title:      "Hold to activate",
        defaultValue: false,
        required:   true
    ]

    def inputArmStayButton = [
        name:       "buttonArmStay",
        type:       "number",
        title:      "Which button?",
        required:    false
    ]

    def inputArmStayHold = [
        name:       "holdArmStay",
        type:       "bool",
        title:      "Hold to activate",
        defaultValue: false,
        required:   true
    ]

    def inputDisarmButton = [
        name:       "buttonDisarm",
        type:       "number",
        title:      "Which button?",
        required:   false
    ]

    def inputDisarmHold = [
        name:       "holdDisarm",
        type:       "bool",
        title:      "Hold to activate",
        defaultValue: false,
        required:   true
    ]

    def inputPanicButton = [
        name:       "buttonPanic",
        type:       "number",
        title:      "Which button?",
        required:   false
    ]

    def inputPanicHold = [
        name:       "holdPanic",
        type:       "bool",
        title:      "Hold to activate",
        defaultValue: false,
        required:   true
    ]

    def pageProperties = [
        name:       "pageRemoteOptions",
        //title:      "Remote Control Options",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("Remote Control Options") {
            paragraph helpRemote
            input inputRemotes
        }

        section("Arm Away Button") {
            input inputArmAwayButton
            input inputArmAwayHold
        }

        section("Arm Stay Button") {
            input inputArmStayButton
            input inputArmStayHold
        }

        section("Disarm Button") {
            input inputDisarmButton
            input inputDisarmHold
        }

        section("Panic Button") {
            input inputPanicButton
            input inputPanicHold
        }
    }
}

// Show "REST API Options" page
def pageRestApiOptions() {
    LOG("pageRestApiOptions()")

    def textHelp =
        "Smarter Alarm can be controlled remotely by any Web client using " +
        "REST API. Please refer to Smarter Alarm documentation for more " +
        "information."

    def textPincode =
        "You can specify optional PIN code to protect arming and disarming " +
        "Smarter Alarm via REST API from unauthorized access. If set, the " +
        "PIN code is always required for disarming Smarter Alarm, however " +
        "you can optionally turn it off for arming Smarter Alarm."

    def inputRestApi = [
        name:           "restApiEnabled",
        type:           "bool",
        title:          "Enable REST API",
        defaultValue:   false
    ]

    def inputPincode = [
        name:           "pincode",
        type:           "number",
        title:          "PIN Code",
        required:       false
    ]

    def inputArmWithPin = [
        name:           "armWithPin",
        type:           "bool",
        title:          "Require PIN code to arm",
        defaultValue:   true
    ]

    def pageProperties = [
        name:       "pageRestApiOptions",
        //title:      "REST API Options",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section("REST API Options") {
            paragraph textHelp
            input inputRestApi
        }

        section("PIN Code") {
            paragraph textPincode
            input inputPincode
            input inputArmWithPin
        }

        if (isRestApiEnabled()) {
            section("REST API Info") {
                paragraph "App ID:\n${app.id}"
                paragraph "Access Token:\n${state.accessToken}"
            }
        }
    }
}

def installed() {
    //LOG("installed()")

    initialize()
    state.installed = true
}

def updated() {
    //LOG("updated()")

    unschedule()
    unsubscribe()
    initialize()
}

private def setupInit() {
    LOG("setupInit()")

    if (state.installed == null) {
        state.installed = false
        state.armed = false
        state.entrydelay = false
        state.hadalarm = false
        state.hadalarmColor = null
        state.ColorDefaultHue = null
        state.ColorDefaultSaturation = null        
        state.ColorDefaultTemperature = null                
        state.zones = []
        state.alarms = []
        state.history = []
        state.alertType = "None"
    } else {
        def version = state.version as String
        if (version == null || version.startsWith('1')) {
            return false
        }
    }

    state.version = getVersion()
    return true
}

private def initialize() {
    log.info "Smarter Alarm. Version ${getVersion()}. ${textCopyright()}"
    LOG("settings: ${settings}")

    clearAlarm()
    state.delay = settings.delay?.toInteger() ?: 30
    state.offSwitches = []
    
    state.ColorDefaultHue = null
    state.ColorDefaultSaturation = null
    state.ColorDefaultTemperature = null
    
    //state.history = []
    
	//fetch SHM Alarm Status
    def currentSHM = location.currentState("alarmSystemStatus").value.toLowerCase()
    //LOG("current alarm state: $currentSHM")
    
    if (currentSHM == "away") {
        atomicState.armed = true
        atomicState.stay = false
    } else if (currentSHM == "stay") {
        atomicState.armed = true
        atomicState.stay = true
    } else {
        atomicState.armed = false
        atomicState.stay = false
    }

    initZones()
    initButtons()
    initRestApi()
    subscribe(location,"alarmSystemStatus",alarmStatusHandler)
    subscribe(location, onLocation)
    
    if (settings.notificationDevice)
    {
        subscribe(settings.notificationDevice, "switch.off", gotDismissMessage)
    }

    subscribe(settings.keypads, "CodeEntryCallback", gotKeypadStatusUpdateSyncMessage)

    //STATE()
    reportStatus()
}

def gotDismissMessage(evt)
{
    //log.debug "Got the dismiss message from the notification device.. clearing alarm!"
    clearAlarm()
}

def gotKeypadStatusUpdateSyncMessage(evt)
{
    //log.debug "Got the status update message from Keypad! Value: '${evt.value}' Status: ${atomicState.armed}"
	if (atomicState.armed == true) {
		if (atomicState.stay == true) {
   		     keypads?.each() { it.setArmedStay() }
		    //log.debug "Keypad Armed Stay"
 	  	 } else {
  		   	keypads?.each() { it.setArmedAway() }
  	    	//log.debug "Keypad Armed Away"
 	   }
    } else {
     	keypads?.each() { it.setDisarmed() }
        //log.debug "Keypad Disarmed"
    }
    
}

private def clearAlarm() {
    //LOG("clearAlarm()")
    state.alarms = []
    settings.alarms*.off()

    // Turn off only those switches that we've turned on
    def switchesOff = state.offSwitches
    if (switchesOff) {
        //LOG("switchesOff: ${switchesOff}")
        settings.switches.each() {
            if (switchesOff.contains(it.id)) {
                it.off()
            }
        }
        state.offSwitches = []
    }
    //Close Virtual Door Control (if it is being used as a tripwire to activate SHM ALARM)
    if (settings.TriggerSHM == true) {
		VirtualswitchSHM?.each() { 
           	it.close()
		}
	}    
    reportStatus()
}

private def initZones() {
    //LOG("initZones()")

    state.zones = []

    state.zones << [
        deviceId:   null,
        sensorType: "panic",
        zoneType:   "alert",
        delay:      false
    ]

    if (settings.z_contact) {
        settings.z_contact.each() {
            state.zones << [
                deviceId:   it.id,
                sensorType: "contact",
                zoneType:   settings["type_${it.id}"] ?: "exterior",
                delay:      settings["delay_${it.id}"],
                chime:		settings["chime_${it.id}"]
            ]
        }
        subscribe(settings.z_contact, "contact.open", onContact)
    }

    if (settings.z_motion) {
        settings.z_motion.each() {
            state.zones << [
                deviceId:   it.id,
                sensorType: "motion",
                zoneType:   settings["type_${it.id}"] ?: "interior",
                delay:      settings["delay_${it.id}"],
                chime:		settings["chime_${it.id}"]
            ]
        }
        subscribe(settings.z_motion, "motion.active", onMotion)
    }

    if (settings.z_movement) {
        settings.z_movement.each() {
            state.zones << [
                deviceId:   it.id,
                sensorType: "acceleration",
                zoneType:   settings["type_${it.id}"] ?: "interior",
                delay:      settings["delay_${it.id}"],
                chime:		settings["chime_${it.id}"]
            ]
        }
        subscribe(settings.z_movement, "acceleration.active", onMovement)
    }

    if (settings.z_smoke) {
        settings.z_smoke.each() {
            state.zones << [
                deviceId:   it.id,
                sensorType: "smoke",
                zoneType:   settings["type_${it.id}"] ?: "alert",
                delay:      settings["delay_${it.id}"],
                chime:		settings["chime_${it.id}"]
            ]
        }
        subscribe(settings.z_smoke, "smoke.detected", onSmoke)
        subscribe(settings.z_smoke, "smoke.tested", onSmoke)
        subscribe(settings.z_smoke, "carbonMonoxide.detected", onSmoke)
        subscribe(settings.z_smoke, "carbonMonoxide.tested", onSmoke)
    }

    if (settings.z_water) {
        settings.z_water.each() {
            state.zones << [
                deviceId:   it.id,
                sensorType: "water",
                zoneType:   settings["type_${it.id}"] ?: "alert",
                delay:      settings["delay_${it.id}"],
                chime:		settings["chime_${it.id}"]
            ]
        }
        subscribe(settings.z_water, "water.wet", onWater)
    }

    state.zones.each() {
        def zoneType = it.zoneType

        if (zoneType == "alert") {
            it.armed = true
        } else if (zoneType == "exterior") {
            it.armed = state.armed
        } else if (zoneType == "interior") {
            it.armed = state.armed && !state.stay
        } else {
            it.armed = false
        }
    }
}

private def initButtons() {
    //LOG("initButtons()")

    state.buttonActions = []
    if (settings.remotes) {
        if (settings.buttonArmAway) {
            def button = settings.buttonArmAway.toInteger()
            def event = settings.holdArmAway ? "held" : "pushed"
            state.buttonActions << [button:button, event:event, action:"armAway"]
        }

        if (settings.buttonArmStay) {
            def button = settings.buttonArmStay.toInteger()
            def event = settings.holdArmStay ? "held" : "pushed"
            state.buttonActions << [button:button, event:event, action:"armStay"]
        }

        if (settings.buttonDisarm) {
            def button = settings.buttonDisarm.toInteger()
            def event = settings.holdDisarm ? "held" : "pushed"
            state.buttonActions << [button:button, event:event, action:"disarm"]
        }

        if (settings.buttonPanic) {
            def button = settings.buttonPanic.toInteger()
            def event = settings.holdPanic ? "held" : "pushed"
            state.buttonActions << [button:button, event:event, action:"panic"]
        }

        if (state.buttonActions) {
            subscribe(settings.remotes, "button", onButtonEvent)
        }
    }
}

private def initRestApi() {
    if (settings.restApiEnabled) {
        if (!state.accessToken) {
            def token = createAccessToken()
            LOG("Created new access token: ${token})")
        }
        state.url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/"
        log.info "REST API enabled"
    } else {
        state.url = ""
        log.info "REST API disabled"
    }
}

private def isRestApiEnabled() {
    return settings.restApiEnabled && state.accessToken
}

def onContact(evt)  { onZoneEvent(evt, "contact") }
def onMotion(evt)   { onZoneEvent(evt, "motion") }
def onMovement(evt) { onZoneEvent(evt, "acceleration") }
def onSmoke(evt)    { onZoneEvent(evt, "smoke") }
def onWater(evt)    { onZoneEvent(evt, "water") }

private def onZoneEvent(evt, sensorType) {
    //LOG("onZoneEvent(${evt.displayName}, ${sensorType})")

    state.alertType = sensorType
    def zone = getZoneForDevice(evt.deviceId, sensorType)
    if (!zone) {
        log.warn "Cannot find zone for device ${evt.deviceId}"
        state.alertType = "None"
        return
    }

    if (zone.armed) {
        state.alarms << evt.displayName
        if (zone.zoneType == "alert" || !zone.delay || (atomicState.stay && settings.stayDelayOff)) {
            history("Alarm", "Alarm triggered by ${sensorType} sensor ${evt.displayName}")
            activateAlarm()
        } else {
            history("Entry Delay", "Entry delay triggered by ${sensorType} sensor ${evt.displayName}")
        	if(settings.sirenEntryStrobe)
            {
        		settings.alarms*.strobe()
            }

			if (atomicState.entrydelay == false) {
            	atomicState.entrydelay = true
            	keypads?.each() { it.setEntryDelay(state.delay) }
    			
                if (settings.exitentryModeHub) {
    				setLocationMode(settings.exitentryModeHub)
    			}                
                
	            myRunIn(state.delay, activateAlarm)
            }
        }
    }
    else if (zone.chime)
    {
    	chimeDevices?.each() { 
               	it.beep()
            }
    }
    
    
}

def onLocation(evt) {
   //LOG("onLocation(${evt.value})")

    String mode = evt.value
    if (settings.awayModes?.contains(mode)) {
        armAway()
    } else if (settings.stayModes?.contains(mode)) {
        armStay()
    } else if (settings.disarmModes?.contains(mode)) {
        disarm()
    }
}

def onButtonEvent(evt) {
    //LOG("onButtonEvent(${evt.displayName})")

    if (!state.buttonActions || !evt.data) {
        return
    }

    def slurper = new JsonSlurper()
    def data = slurper.parseText(evt.data)
    def button = data.buttonNumber?.toInteger()
    if (button) {
        LOG("Button '${button}' was ${evt.value}.")
        def item = state.buttonActions.find {
            it.button == button && it.event == evt.value
        }

        if (item) {
            //LOG("Executing '${item.action}' button action")
            "${item.action}"()
        }
    }
}

def armAway() {
    //LOG("armAway()")
	history("Armed Away", "Alarm armed away")

    if (!atomicState.armed || atomicState.stay) {
        armPanel(false)
    	sendLocationEvent(name: "alarmSystemStatus", value: "away")
        
        if (settings.awayModeHub) {
       		setLocationMode(settings.awayModeHub)
        }
    }
	
    reportStatus()
}

def armStay() {
    //LOG("armStay()")
	history("Armed Stay", "Alarm armed stay")

    if (!atomicState.armed || !atomicState.stay) {
        armPanel(true)
    	sendLocationEvent(name: "alarmSystemStatus", value: "stay")
        
        if (settings.stayModeHub) {
       		setLocationMode(settings.stayModeHub)
        }
        
    }
	
	reportStatus()
    
}

def disarm() {
    //LOG("disarm()")
	history("Disarmed", "Alarm disarmed")
    
    if (atomicState.armed || atomicState.stay) {
    	//LOG("disarm() -- was armed")
        atomicState.armed = false
        atomicState.stay = false
		atomicState.entrydelay = false

        state.zones.each() {
            if (it.zoneType != "alert") {
                it.armed = false
            }
        }
        
        keypads?.each() { it.setDisarmed() }
        
        if (settings.ChimeOnStatusChange == true) {
            def between = timeOfDayIsBetween(QuietfromTime, QuiettoTime, new Date(), location.timeZone)
    		if (!between || (!QuietfromTime && !QuiettoTime)) {
    	    	//LOG("disarm() -- CHIME")
	    		chimeDevices?.each() { 
               	it.beep()
        	    }
	    	}
		}
        
		SwitchesOnArmed?.each() { 
	       	it.off()
		}            
        
        //Close Virtual Contact Sensor to prepare SHM Alarm trigger
   		if (settings.TriggerSHM == true) {
			VirtualswitchSHM?.each() { 
	           	it.close()
			}
		}

		sendLocationEvent(name: "alarmSystemStatus", value: "off")
		
        if (settings.disarmModeHub) {
       		setLocationMode(settings.disarmModeHub)
        }

		reset()
        
        if (settings.DisarmedHueColor != "Disabled" && settings.DisarmedHueColor != null) {
        	sendColor()
        }
        
	} else {
    	reportStatus()
    }
    

}

def panic() {
    //LOG("panic()")
	atomicState.armed = true
    state.alarms << "Panic"
    activateAlarm()
}

def reset() {
    //LOG("reset()")

    unschedule()
    clearAlarm()

    // Send notification
    def msg = "${location.name} is "
    if (atomicState.armed) {
        msg += "ARMED "
        msg += atomicState.stay ? "STAY" : "AWAY"
    } else {
        msg += "DISARMED."
    }

    notify(msg)
    notifyVoice()
    reportStatus()
}

def exitDelayExpired() {

    def armed = atomicState.armed
    def stay = atomicState.stay

	//LOG("exitDelayExpired(${armed})")
    
    if (!armed) {
        log.debug "exitDelayExpired: unexpected state!"
        //STATE()
        return
    }

    state.zones.each() {
        def zoneType = it.zoneType
        if (zoneType == "exterior" || (zoneType == "interior" && !stay)) {
            it.armed = true
        }
    }
    
	atomicState.entrydelay = false
    
	if(stay) {
        keypads?.each() { it.setArmedStay() }
    } else {
     	keypads?.each() { it.setArmedAway() }
    }

    def msg = "${location.name}: all "
    if (stay) {
        msg += "exterior "
    }
    msg += "zones are armed."

    //reportStatus()
    notify(msg)
}

private def armPanel(stay) {
    //LOG("armPanel(${stay})")

    unschedule()
    clearAlarm()

    atomicState.armed = true
    atomicState.stay = stay
    atomicState.entrydelay = false

    def armDelay = false
    state.zones.each() {
        def zoneType = it.zoneType
        if (zoneType == "exterior") {
            if (it.delay && !(stay && settings.stayExitDelayOff)) {
                it.armed = false
                armDelay = true
            } else {
                it.armed = true
            }
        } else if (zoneType == "interior") {
            if (stay) {
                it.armed = false
            } else if (it.delay) {
                it.armed = false
                armDelay = true
            } else {
                it.armed = true
            }
        }
    }

    //LOG("armDelay ${armDelay})")
    //LOG("settings.stayExitDelayOff ${settings.stayExitDelayOff})")
    def delay = armDelay && !(stay && settings.stayExitDelayOff) ? atomicState.delay : 0
    if (delay) {
    	//LOG("setting Exit Delay")
    	keypads?.each() { it.setExitDelay(delay) }
        myRunIn(delay, exitDelayExpired)
    }
    else
    {
    	//LOG("No Exit Delay -- arming now")
    	if (stay) {
    		keypads?.each() { it.setArmedStay() }
        } else {
        	keypads?.each() { it.setArmedAway() }
        }
        
        if (settings.ChimeOnStatusChange == true) {
            def between = timeOfDayIsBetween(QuietfromTime, QuiettoTime, new Date(), location.timeZone)
    		if (!between || (!QuietfromTime && !QuiettoTime)) {
    	    	//LOG("disarm() -- CHIME")
	    		chimeDevices?.each() { 
               	it.beep()
        	    }
	    	}
		}

    }

	SwitchesOnArmed?.each() { 
       	it.on()
	}    
    
    def mode = stay ? "STAY" : "AWAY"
    def msg = "${location.name} "
    if (delay) {
        msg += "will arm ${mode} in ${state.delay} seconds."
    } else {
        msg += "is ARMED ${mode}."
    }

	if ((settings.ArmedHueColor != "Disabled" && settings.ArmedHueColor != null) || atomicState.hadalarm) {
	    	
            atomicState.hadalarm = false
    		atomicState.hadalarmColor = null
        	sendColor()
    }


	notify(msg)
    notifyVoice()
    
}

// .../armaway REST API endpoint
def apiArmAway() {
    //LOG("apiArmAway()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode && settings.armWithPin && (params.pincode != settings.pincode.toString())) {
        log.error "Invalid PIN code '${params.pincode}'"
        return httpError(403, "Access denied")
    }

    armAway()
    return apiStatus()
}

// .../armstay REST API endpoint
def apiArmStay() {
    //LOG("apiArmStay()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode && settings.armWithPin && (params.pincode != settings.pincode.toString())) {
        log.error "Invalid PIN code '${params.pincode}'"
        return httpError(403, "Access denied")
    }

    armStay()
    return apiStatus()
}

// .../disarm REST API endpoint
def apiDisarm() {
    //LOG("apiDisarm()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode && (params.pincode != settings.pincode.toString())) {
        log.error "Invalid PIN code '${params.pincode}'"
        return httpError(403, "Access denied")
    }

    disarm()
    return apiStatus()
}

// .../panic REST API endpoint
def apiPanic() {
    //LOG("apiPanic()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    panic()
    return apiStatus()
}

// .../status REST API endpoint
def apiStatus() {
    //LOG("apiStatus()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    def status = [
        status: atomicState.armed ? (atomicState.stay ? "armed stay" : "armed away") : "disarmed",
        alarms: state.alarms
    ]

    return status
}

def activateAlarm() {
    //LOG("activateAlarm()")

    if (state.alarms.size() == 0) {
        log.warn "activateAlarm: false alarm"
        return
    }
    if (atomicState.armed == false) {
        log.warn "activateAlarm: not armed (disarmed during delay??)"
        return
    }

	history("Alarm", "Alarm Triggered")

    if (settings.alarmModeHub) {
    	setLocationMode(settings.alarmModeHub)
    }

    if(settings.sirenEntryStrobe)
    {
    	settings.alarms*.off()
    }
    
    def atype = atomicState.alertType

    if ((atype == "water" && settings.sirenOnWaterAlert) ||
        (atype == "smoke" && settings.sirenOnSmokeAlert) ||
       ((atype == "contact" || atype == "acceleration" || atype == "motion") && settings.sirenOnIntrusionAlert))
    {
        switch (settings.sirenMode) {
        case "Siren":
            settings.alarms*.siren()
            break

        case "Strobe":
            settings.alarms*.strobe()
            break
            
        case "Both":
            settings.alarms*.both()
            break
        }
    } else { 
    	log.debug "No siren for $atype Alert"
    }
    
    if (atype == "contact" || atype == "acceleration" || atype == "motion") {
  		//Open Virtual Contact Sensor to set off SHM Alarm
   		if (settings.TriggerSHM == true) {
			VirtualswitchSHM?.each() { 
	           	it.open()
			}
		}
    }
    
	// Only turn on those switches that are currently off
    def switchesOn = settings.switches?.findAll { it?.currentSwitch == "off" }
    //LOG("switchesOn: ${switchesOn}")
    if (switchesOn) {
        switchesOn*.on()
        state.offSwitches = switchesOn.collect { it.id }
    }
	
    SwitchesOnArmed?.each() { 
       	it.on()
	}
    /* turn on and set color to hues */
    atomicState.hadalarm = true
  	sendColor()
	settings.cameras*.take()

    if (settings.helloHomeAction) {
        //log.info "Executing HelloHome action '${settings.helloHomeAction}'"
        location.helloHome.execute(settings.helloHomeAction)
    }

    def msg = "Alarm at ${location.name}!"
    state.alarms.each() {
        msg += "\n${it}"
    }

    notify(msg)
    notifyVoice()
   	history(msg)
   
	//turn OFF alarm and reset after 2 hours
    myRunIn(7200, reset)
    
}


private def notify(msg) {
    //LOG("notify(${msg})")

    log.info msg

    if (state.alarms.size()) {
        // Alarm notification
        if (settings.pushMessage) {
            mySendPush(msg)
        } else {
            sendNotificationEvent(msg)
        }
        
        if (settings.smsAlarmPhone1) {
        if (settings.smsAlarmPhone1 && settings.phone1) {
            sendSms(phone1, msg)
        }

		if (settings.smsAlarmPhone2 && settings.phone2) {
            sendSms(phone2, msg)
        }

        if (settings.smsAlarmPhone3 && settings.phone3) {
            sendSms(phone3, msg)
        }

        if (settings.smsAlarmPhone4 && settings.phone4) {
            sendSms(phone4, msg)
        }
        }

        if (settings.pushbulletAlarm && settings.pushbullet) {
            settings.pushbullets*.push(location.name, msg)
        }   
    } else {
        // Status change notification
        if (settings.pushStatusMessage) {
            mySendPush(msg)
        } else {
            sendNotificationEvent(msg)
        }
        if (settings.smsStatusPhone1) {
        if (settings.smsStatusPhone1 && settings.phone1) {
            sendSms(phone1, msg)
        }

        if (settings.smsStatusPhone2 && settings.phone2) {
            sendSms(phone2, msg)
        }

        if (settings.smsStatusPhone3 && settings.phone3) {
            sendSms(phone3, msg)
        }

        if (settings.smsStatusPhone4 && settings.phone4) {
            sendSms(phone4, msg)
        }
        }
        if (settings.pushbulletStatus && settings.pushbullets) {
            settings.pushbullets*.push(location.name, msg)
        }
    }
}

private def notifyVoice() {
    //LOG("notifyVoice()")

    if (!settings.audioPlayer) {
        return
    }

    def phrase = null
    if (state.alarms.size()) {
        // Alarm notification
        if (settings.speechOnAlarm) {
            phrase = settings.speechText ?: getStatusPhrase()
        }
    } else {
        // Status change notification
        if (settings.speechOnStatus) {
            if (atomicState.armed) {
                if (atomicState.stay) {
                    phrase = settings.speechTextArmedStay ?: getStatusPhrase()
                } else {
                    phrase = settings.speechTextArmedAway ?: getStatusPhrase()
                }
            } else {
                phrase = settings.speechTextDisarmed ?: getStatusPhrase()
            }
        }
    }

    if (phrase) {
        settings.audioPlayer*.playText(phrase)
    }
}

def reportStatus()
{
    //log.debug "in report status"
    //log.debug "notification device = ${settings.notificationDevice}"

    if (settings.notificationDevice)
    {
        def phrase = ""
        if (state.alarms.size())
        {
            phrase = "Alert: Alarm at ${location.name}!"
            notificationDevice.deviceNotification(phrase)
            //log.debug "sending notification alert: = $phrase"
            def zones = "Zones: "
            state.alarms.each()
            {
                //log.debug "in loop it"
                //log.debug "it = $it"
                zones = "Zones: "
                zones += " $it" +"\n"
            }
            notificationDevice.deviceNotification(zones)
            //log.debug "sending nofication zones = $zones" 
                
            // send zone type
            phrase = "AlertType: "
            def atype = state.alertType
            if (atype == null)
                atype = "None"
            phrase += " $atype"
            notificationDevice.deviceNotification(phrase)
            //log.debug "sending nofication alert type = $phrase" 
        }
        else
        {
            phrase = "Status: "
            if (atomicState.armed)
              {
                def mode = atomicState.stay ? "Armed - Stay" : "Armed - Away"
                phrase += "${mode}"
            } else {
                phrase += "Disarmed"
            }
            //log.debug "sending notification status = $phrase"
        notificationDevice.deviceNotification(phrase)
       }
    }
 }

private def history(String event, String description = "") {
    //LOG("history(${event}, ${description})")

    def history = atomicState.history
    history << [time: now(), event: event, description: description]
    if (history.size() > 20) {
        history = history.sort{it.time}
        history = history[1..-1]
    }

    //LOG("history: ${history}")
    state.history = history
}

private def getStatusPhrase() {
    //LOG("getStatusPhrase()")

    def phrase = ""
    if (state.alarms.size()) {
        phrase = "Alarm at ${location.name}!"
        state.alarms.each() {
            phrase += " ${it}."
        }
    } else {
        phrase = "${location.name} security is "
        if (atomicState.armed) {
            def mode = atomicState.stay ? "stay" : "away"
            phrase += "armed in ${mode} mode."
        } else {
            phrase += "disarmed."
        }
    }

    return phrase
}

private def getHelloHomeActions() {
    def actions = location.helloHome?.getPhrases().collect() { it.label }
    return actions.sort()
}

private def getAlarmStatus() {
    def alarmStatus

    if (atomicState.armed) {
        alarmStatus = "ARMED "
        alarmStatus += atomicState.stay ? "STAY" : "AWAY"
    } else {
        alarmStatus = "DISARMED"
    }

    return alarmStatus
}

private def getHubMode() {
    def hubMode

	hubMode = location.mode

	return hubMode
}

private def getZoneStatus(device, sensorType) {

    def zone = getZoneForDevice(device.id, sensorType)
    if (!zone) {
        return null
    }

    def str = "${device.displayName}: ${zone.zoneType}, "
    str += zone.armed ? "armed, " : "disarmed, "
    str += device.currentValue(sensorType)

    return str
}

private def getZoneForDevice(id, sensorType) {
    return state.zones.find() { it.deviceId == id && it.sensorType == sensorType }
}

private def isZoneReady(device, sensorType) {
    def ready

    switch (sensorType) {
    case "contact":
        ready = "closed".equals(device.currentValue("contact"))
        break

    case "motion":
        ready = "inactive".equals(device.currentValue("motion"))
        break

    case "acceleration":
        ready = "inactive".equals(device.currentValue("acceleration"))
        break

    case "smoke":
        ready = "clear".equals(device.currentValue("smoke"))
        break

    case "water":
        ready = "dry".equals(device.currentValue("water"))
        break

    default:
        ready = false
    }

    return ready
}

private def getDeviceById(id, sensorType) {
    switch (sensorType) {
    case "contact":
        return settings.z_contact?.find() { it.id == id }

    case "motion":
        return settings.z_motion?.find() { it.id == id }

    case "acceleration":
        return settings.z_movement?.find() { it.id == id }

    case "smoke":
        return settings.z_smoke?.find() { it.id == id }

    case "water":
        return settings.z_water?.find() { it.id == id }
    }

    return null
}

private def getNumZones() {
    def numZones = 0

    numZones += settings.z_contact?.size() ?: 0
    numZones += settings.z_motion?.size() ?: 0
    numZones += settings.z_movement?.size() ?: 0
    numZones += settings.z_smoke?.size() ?: 0
    numZones += settings.z_water?.size() ?: 0

    return numZones
}

private def myRunIn(delay_s, func) {
    if (delay_s > 0) {
        def date = new Date(now() + (delay_s * 1000))
        runOnce(date, func)
        //LOG("scheduled '${func}' to run at ${date}")
    }
}

private def mySendPush(msg) {
    // sendPush can throw an exception
    try {
        sendPush(msg)
    } catch (e) {
        log.error e
    }
}

private def LOG(message) {
    //log.trace message
}

private def STATE() {
    //log.trace "state: ${state}"
}

def alarmStatusHandler(evt) {
  
    //LOG("Current SHM status: '${location.currentState("alarmSystemStatus")?.value}'")
    //LOG("Alarm System Status has been changed to '${evt.value}'")
  
  	if (settings.SyncWithSHM == true) {
    
    def armed = atomicState.armed
    def stay = atomicState.stay
    
    String mode = evt.value.toLowerCase()
    if (mode == "away") {
        if (!armed || stay) {
        	armAway()
        }
    } else if (mode == "stay") {
        if (!stay) {
        	armStay()
        }
    } else if (mode == "off") {
        if (armed || stay) {
        	disarm()
		}
    } 
	}
}

def sendColor() {
	//Initialize the hue and saturation
	def hueColor = 0
	def saturation = 100
    def temperature = 0

	//Use the user specified brightness level. If they exceeded the min or max values, overwrite the brightness with the actual min/max
	if (settings.hueBrightnessLevel == null)
    	settings.hueBrightnessLevel = 100
    
    if (settings.hueBrightnessLevel<1) {
		settings.hueBrightnessLevel=1
	} else if (settings.hueBrightnessLevel>100) {
		settings.hueBrightnessLevel=100
	}
    
    if (settings.WaterHueColor == null)
    	settings.WaterHueColor = "Disabled"
    if (settings.SmokeHueColor == null)
    	settings.SmokeHueColor = "Disabled"
    if (settings.IntrusionHueColor == null)
    	settings.IntrusionHueColor = "Disabled"
    if (settings.ArmedHueColor == null)
    	settings.ArmedHueColor = "Disabled"
    if (settings.DisarmedHueColor == null)
    	settings.DisarmedHueColor = "Disabled"
      
      
	def color
    
    def armed = atomicState.armed
    
    log.debug "in notify atype = $atype"
    
    
    if (armed) {
      color = settings.ArmedHueColor
    } else {
      color = settings.DisarmedHueColor
    }

    def atype = state.alertType
    
    if (atomicState.hadalarm && atomicState.hadalarmColor == null) {

    if (atype == "smoke")
      color = settings.SmokeHueColor
    if (atype == "water")
       color = settings.WaterHueColor
    if (atype == "contact" || atype == "acceleration" || atype == "motion")
    	color = settings.IntrusionHueColor

		atomicState.hadalarmColor = color
    
    } else if (atomicState.hadalarm) {
    	color = atomicState.hadalarmColor
    }
    
	log.debug "in notify by color color = $color"

   	if (atomicState.ColorDefaultHue == null) {
    //If we have not changed the color YET, then save the current values as Default
    
        	if (hues) {
                def hueAttr
                hues.each {
                log.debug "[ Setting Light Color Defaults!! ] "
                
                hueAttr = it.currentColorMode
                log.debug "Current ColorMode: ${hueAttr}"
                def cMode = hueAttr as String
            
            if (cMode.toUpperCase().startsWith("W")) {
                
	                atomicState.ColorDefaultTemperature = 0
                
					hueAttr = it.currentHue
        	        log.debug "Current hue: ${hueAttr}"
            	    atomicState.ColorDefaultHue = hueAttr
                
                	hueAttr = it.currentSaturation
                	log.debug "Current saturation: ${hueAttr}"
	                atomicState.ColorDefaultSaturation = hueAttr
                
                } else {

					atomicState.ColorDefaultHue = 0 
                
	                hueAttr = it.currentColorTemperature
    	            log.debug "Current colorTemperature: ${hueAttr}"
        	        atomicState.ColorDefaultTemperature = hueAttr
                
                }


    			}            
            	
            }
    	}

                log.debug "Default Hue: ${atomicState.ColorDefaultHue}"
                log.debug "Default Saturation: ${atomicState.ColorDefaultSaturation}"
                log.debug "Default Temperature: ${atomicState.ColorDefaultTemperature}"
                
                 
                

	//Set the hue and saturation for the specified color.
    if (color != "Disabled")
    {

	switch(color) {
    	
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
	    case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Default (Initial Color)":
        	temperature = atomicState.ColorDefaultTemperature
            if (temperature == 0 || temperature == null) {
	            hueColor = atomicState.ColorDefaultHue
    	        saturation = atomicState.ColorDefaultSaturation
            }
			break;
        case "[TURN OFF]":
			hueColor = "TURNOFF"
			break;
	}
    
		if (hueColor != "TURNOFF") {

	        	if (temperature > 0) {
            	
                	hues*.setColorTemperature(temperature)
                
                } else {
					//Change the color of the light
					def newValue = [hue: hueColor, saturation: saturation, level: settings.hueBrightnessLevel]  
    				hues*.setColor(newValue)
            }
   		} else {
        	//Turn off light
    		hues*.off()
		}
	}
}