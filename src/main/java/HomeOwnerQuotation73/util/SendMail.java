/**
 * 
 */
package HomeOwnerQuotation73.util;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.mail.internet.InternetAddress;



/**
 * @author jonathanfviverosmoreno
 *
 */
public class SendMail {
	
	private static final Log _log = LogFactoryUtil.getLog(SendMail.class);
	
	public void SendMAil(String[] mails, String body,  String subject){
		try {
			String fromMail = "portal_agentes@tokiomarine.com.mx";
			MailMessage ms = new MailMessage();
			
			
			InternetAddress fromAddress = new InternetAddress(fromMail);
			InternetAddress toAddress = null;
			InternetAddress[] bulkAddresses = new InternetAddress[mails.length];

			ms.setFrom(fromAddress);
			ms.setSubject(subject);
			ms.setBody(body);
			ms.setHTMLFormat(true);

			for (int i = 0; i < mails.length; i++) {
				toAddress = new InternetAddress(mails[i]);
				bulkAddresses[i] = toAddress;
			}
			ms.setBulkAddresses(bulkAddresses);

			MailServiceUtil.sendEmail(ms);
			_log.debug("Envio de mail: " + subject);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
