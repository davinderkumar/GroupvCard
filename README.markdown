# Group vCard Plugin Readme

Overview

This plugin is used to create vCard for Groups.

## Installation

Login into OpenFire Admin, go to "Plugins" and select groupvcard.jar file upload plugin section. If plugin is already installed then uninstall it first.

## Configuration

No configuration required.

## Using the Plugin

Openfire forwards messages to this plugin which are of following format
Here are IQ request formats 

#### Create Group vCard

    <iq type="set" to="OPENFIRE_SERVER" id="UNIQUE_ID">
    <query xmlns="urn:xmpp:groupvcard">
	<name>NAME OF GROUP</name>
	<jid>GROUP JID</jid>
	<photo>
	<type>IMAGE MIME TYPE</type>
	<binval>BASE 64 Encoded Binary String</binval>
	</photo>
    </ query>
    </ iq>

##### The return value

    <iq type="result" id="UNIQUE_ID" from="OPENFIRE_SERVER" to="USER_JID">
    <query xmlns="urn:xmpp:groupvcard">
    <jid>GROUP JID</jid>
    </query>
    </iq>

#### Get device token

    <iq type="get" to="OPENFIRE_SERVER" id="UNIQUE_ID">
	<query xmlns="urn:xmpp:groupvcard">
	<jid>GROUP JID</jid>
	</query>
    </iq>

##### The return value

	<iq type="result" id="UNIQUE_ID" from="OPENFIRE_SERVER" to="USER_JID">
	<query xmlns="urn:xmpp:groupvcard"><vCard xmlns="vcard-temp">
	<NICKNAME>NAME OF GROUP</NICKNAME>
	<JABBERID>GROUP JID</JABBERID>
	<USERID>GROUP JID</USERID>
	<PHOTO xmlns="">
	<TYPE>IMAGE MIME TYPE</TYPE>
	<BINVAL>BASE 64 Encoded Binary String</BINVAL>
	</PHOTO>
	</vCard>
	</query>
	</iq>

