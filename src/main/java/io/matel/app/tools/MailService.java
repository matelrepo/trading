package io.matel.app.tools;

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

    public void sendEmail(ProcessorState processorState, ContractBasic contract)
            throws MailException {
        if (Global.send_email) {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("mcolsenet@gmail.com");
            mail.setSubject("(" + processorState.getIdcontract() + ") " + contract.getSymbol() + " >> (" + processorState.getFreq() + ") " + processorState.getEvent());
            mail.setText((new StringBuilder("Value=")).append(processorState.getValue()).append(" Target=").append(processorState.getTarget()).toString());
            javaMailSender.send(mail);
        }
    }

}

