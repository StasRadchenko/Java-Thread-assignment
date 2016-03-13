package main.java.bgu.spl.mics.example.messages;

import main.java.bgu.spl.mics.Request;

public class ExampleRequest implements Request<String>{

    private String senderName;

    public ExampleRequest(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}
