/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.mbc.dna.notifications.mailer;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BaseJMailer implements JMailer {
	
	@Value("${dna.notification.senderEmail}")
	private String senderEmailId;

	@Value("${dna.ui.uri}")
	private String dnaBaseUri;

	@Value("${dna.teams.uri}")
	private String TeamsUri;

	@Value("${dna.mattermost.uri}")
	private String MattermostUri;
	
	public BaseJMailer() {
		super();
	}

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void sendSimpleMail(String eventId, String to, String subject, String msgTxt) {
                
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
	        	
	        helper.setSubject("This is an HTML email");
	        helper.setFrom(senderEmailId);
	        helper.setTo(to);
	        helper.setSubject(subject);
	         
	        boolean html = true;
			if(subject.contains("Codespace Playground Project") || subject.contains("Codespace Project")){
				helper.setSubject("ACTION REQUIRED:" + subject);
				helper.setText("Dear User," +
				"<br/>" +
				"IMPORTANT!!!"+ "<br/>" + msgTxt
				+"<br/>" +
				"<h3>Need Assistance?</h3>" +
				"<p>Join our <a href=\""+ TeamsUri+ "\">Teams channel</a> or <a href=\""+ MattermostUri+ "\">Mattermost channel</a> for help or to discuss any concerns.</p>"
				+ "<br/>" + "<p>Thank you for your attention. We look forward to providing you with an enhanced Codespace experience!</p>"
				+ "<br>Best regards," +
				"<p><a href=\"" + dnaBaseUri+"\">DnA TEAM</a></p>" 
				+"<p> Email: <a href=\"mailto:dna@mercedes-benz.com\">dna@mercedes-benz.com</a></p>", html);
     
			}
			else{
	        helper.setText(""	        		
	        		+ msgTxt
					+ "<br/>-----<br/>"
	        		+ "<p> You received this auto-generated email from DNA as per your notification settings. You can change them <a href=\"" + dnaBaseUri + "/#/usersettings" + "\">here</a>. </p>", html);
			}
	        javaMailSender.send(message);
	        log.info("Mail sent successfully for eventRecord {} , please check notification by this id for more details", eventId);
	        
        }catch(Exception e){
        	log.info("Failed in sending eMail for eventRecord {} with exception {} , please check notification by this id for more details", eventId, e.getMessage());
        }
        log.info("Mail sent successfully for eventRecord {} , please check notification by this id for more details", eventId);
    }

	@Override
	public void sendMailWithAttachments(String to, String subject, String msgTxt, String attachmentsPath) {

	}
}
