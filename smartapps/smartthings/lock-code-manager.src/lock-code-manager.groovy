/**
 *  Copyright 2015 SmartThings
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
 *  Unlock It When I Arrive
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */

definition(
    name: "Lock Code Manager",
    namespace: "smartthings",
    author: "Ben Casse",
    description: "Manage Lock Codes",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
    page(name: "preferencesPage")
    page(name: "addUserCode")
    page(name: "deleteUserCode")
}

def preferencesPage() {
    dynamicPage(name: "preferencesPage", title: "Lock Code Manager", install:true, uninstall: true) {
        
        section("Select Lock"){
            input "lock", "capability.lockCodes", title: "Select A Lock", description: null, multiple: false, required: true
        }
        
        if(lock) {
        	state.lockCodes = parseJson(lock.currentValue("lockCodes") ?: "{}") ?: [:]
            section("Manage Users") {
                href(name: "href", title: "Add a user code", required: false, page: "addUserCode")
                href(name: "href", title: "Delete a user code", required: false, page: "deleteUserCode")
            }
        }
    }
}

def addUserCode() {
    dynamicPage(name: "addUserCode", title: "Add User Code", install:true) {
        section("Add User Code") {
            input(name: "name", type: "text", title: "Name", required: true)
            input(name: "code", type: "text", title: "Code", required: true)
        }
    }
}

def deleteUserCode() {
    dynamicPage(name: "deleteUserCode", title: "Delete User Code", install:true) {
    	section("Delete User Code") {
        	input(name: "deleteSlot", type: "enum", title: "Select user to remove", options:state.lockCodes, required: true)
        }
    }
}


def installed()
{
	log.trace("Installed with settings: ${settings}")
    initialize()
}

def updated()
{
	if(lock)
    {
    	state.lockCodes = parseJson(lock.currentValue("lockCodes") ?: "{}") ?: [:]
    	log.trace("${state.lockCodes}")
    
    	if(code && name) {
        	addCode()
        }
        
        if(deleteSlot) {
        	deleteCode()
        }
    }

	log.trace("Updated with settings: ${settings}")
    unsubscribe()
    initialize()
}

def initialize()
{
}

def addCode()
{
	if((code != "") && (name != ""))
    {
    	log.trace "Updating code for slot $slot"
        
    	if ((code.length() < 5) || (code.length() > 6)) {
        	throw "Invalid Code Length"
        }
        
        def slots = state.lockCodes.keySet()
        log.trace "Used code slots are: $slots"
        
        def index = 1
        slots.find { 
            if(it.toInteger() != index)
            	return true
            
            index = index + 1          
            return false
        }
        
        log.trace "First free slot: $index"
        if (index > 255)
        	throw "All Code Slots Full"
        
        lock.setCode(index, code, name);
        log.trace "Code for slot $slot updated"

        app.updateSetting("name", "")
        app.updateSetting("code", "")
    }
}

def deleteCode()
{
	if (deleteSlot != "")
    {
    	lock.deleteCode(deleteSlot.toInteger())
        log.trace "Code for slot $deleteSlot deleted"
        
        app.updateSetting("deleteSlot", "")
    }
}