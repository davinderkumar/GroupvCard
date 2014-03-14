package org.jivesoftware.openfire.plugin.groupvcard;

import java.io.File;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class GroupvCard extends IQHandler implements Plugin {

	private static final Logger Log = LoggerFactory.getLogger(GroupvCard.class);

	private XMPPServer server;

	private IQHandlerInfo info;

	private VCardManager vcardManager;

	private String gvNameSpace = "urn:xmpp:groupvcard";

	private String gvJID = "jid";
	private String gvName = "name";		
	private String gvQuery = "query";		
	private String gvPhoto = "photo";	
	private String gvPhotoType = "type";	
	private String gvPhotoBinval = "binval";	
	
	public GroupvCard() {
		super("GroupvCard IQ Handler");
		server = XMPPServer.getInstance();
		this.vcardManager = VCardManager.getInstance();
		info = new IQHandlerInfo("query", gvNameSpace);
	}

	public void initializePlugin(PluginManager pManager, File pluginDirectory) {

		IQRouter iqRouter = server.getIQRouter();
		iqRouter.addHandler(this);
	}

	public void destroyPlugin() {
		server = null;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		IQ result = IQ.createResultIQ(packet);
		IQ.Type type = packet.getType();
		
		Element responseElement = DocumentHelper.createElement(QName.get("query", gvNameSpace));
		Element receivedPacket = packet.getElement();	
		
		//Log.info("request = " + receivedPacket.asXML());
		
		if (type.equals(IQ.Type.get)) {

			String jid = receivedPacket.element(gvQuery).elementText(gvJID).toLowerCase();
			if ( jid == null ) {
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_acceptable);
				return result;
			}

			JID groupJID = new JID(jid);
			String groupId = groupJID.getNode();
			
			//Log.error("Getting vcard for JID : " + groupId);
			Element vcard = vcardManager.getVCard(groupId);

			if (vcard == null) {
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.item_not_found);
				return result;
			} 
			
			responseElement.add(vcard);
			result.setChildElement(responseElement);

		} else if (type.equals(IQ.Type.set)) {

			String groupname 	= receivedPacket.element(gvQuery).elementText(gvName);
			String jid 			= receivedPacket.element(gvQuery).elementText(gvJID);
			Element photo 		= receivedPacket.element(gvQuery).element(gvPhoto);
			String delete 		= receivedPacket.element(gvQuery).elementText("delete");
			
			if (jid == null ) {
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_acceptable);
				return result;
			}
			
			JID groupJID = new JID(jid);
			String groupId = groupJID.getNode();
			
			if ( delete != null ) {
				//Log.error("Deleting vcard for JID : " + groupId);
				vcardManager.deleteVCard(groupId);	
				result.setChildElement(receivedPacket.createCopy());	
				return result;
			} 
				
			if ( groupname == null) {
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_acceptable);
				return result;
			}
			
			Element vCardElement = DocumentHelper.createElement(QName.get("vCard", "vcard-temp"));

			vCardElement.addElement("NICKNAME").setText(groupname);
			vCardElement.addElement("JABBERID").setText(jid);
			vCardElement.addElement("USERID").setText(jid);
			
			boolean retainPicture = true;
			if (photo != null) {
				retainPicture = false;
				String photoType = photo.elementText(gvPhotoType);
				String photoBinval = photo.elementText(gvPhotoBinval);

				if ( photoType != null && photoBinval != null ) {
					Element elem = DocumentHelper.createElement("PHOTO");
					elem.addElement("TYPE").setText(photoType);
					elem.addElement("BINVAL").setText(photoBinval);
					vCardElement.add(elem);
				} else {
					result.setChildElement(packet.getChildElement().createCopy());
					result.setError(PacketError.Condition.not_acceptable);
					return result;						
				}
			}

			//retain existing group photo
			if ( retainPicture ) {
				String vPhotoType = vcardManager.getVCardProperty(groupId, "PHOTO:TYPE");
				String vPhotoBinVal = vcardManager.getVCardProperty(groupId, "PHOTO:BINVAL");
				if ( vPhotoType != null && vPhotoBinVal != null ) {
					Element elem = DocumentHelper.createElement("PHOTO");
					elem.addElement("TYPE").setText(vPhotoType);
					elem.addElement("BINVAL").setText(vPhotoBinVal);
					vCardElement.add(elem);
				} 					
			}
			
			try {				
				vcardManager.setVCard(groupId, vCardElement);
			} catch (Exception e) {
				Log.error("Exception : " + e.getMessage());
			}
			
			responseElement.addElement(gvJID).setText(jid);
			result.setChildElement(responseElement);

		} else {
			result.setChildElement(packet.getChildElement().createCopy());
			result.setError(PacketError.Condition.not_acceptable);
		}

		return result;
	}
}