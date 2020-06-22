package io.matel.app.config.tools;

import io.matel.app.config.Global;
import io.matel.app.domain.ContractBasic;
import io.matel.app.state.ProcessorState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private JavaMailSender javaMailSender;


    @Autowired
    public MailService(JavaMailSender javaMailSender)
    {
        this.javaMailSender = javaMailSender;
    }

    public void sendMessage(ProcessorState processorState, ContractBasic contract)
            throws MailException {
        if (Global.send_email) {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("mcolsenet@gmail.com");
            if(processorState != null && contract != null) {
                if(processorState.getEvtype().equals("NONE")){
                    mail.setSubject("(" + processorState.getIdcontract() + ") " + contract.getSymbol() + " >> (" + processorState.getFreq() + ") " + processorState.getEventType());
                }else{
                    mail.setSubject(processorState.getEvtype() + " - (" + processorState.getIdcontract() + ") " + contract.getSymbol() + " >> (" + processorState.getFreq() + ") " + processorState.getEventType());
                }
                mail.setText((new StringBuilder("Value=")).append(processorState.getValue()).append(" Target=").append(processorState.getTarget()).toString());
            }else{
                mail.setSubject("");
                mail.setText("");
            }
            javaMailSender.send(mail);
        }
    }

    public void sendMessage(String subject, String body, boolean urgent) throws MailException{
        if (Global.send_email) {
            SimpleMailMessage mail = new SimpleMailMessage();
            if(urgent){
                String[] adress = new String[2];
                adress[0] = "mcolsenet@gmail.com";
                adress[1] = "matel222@hotmail.com";
                mail.setTo(adress);
            }else{
                mail.setTo("mcolsenet@gmail.com");

            }
                mail.setSubject(subject);
                mail.setText(body);
            javaMailSender.send(mail);
        }
    }

}

