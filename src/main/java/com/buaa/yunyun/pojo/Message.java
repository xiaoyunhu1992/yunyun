package com.buaa.yunyun.pojo;

public class Message {
	String id;
	String content;
	String senderid;
	String recipientid;
	public String getId() {
        return id;
    }
    public void setId(String id ) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getSenderid()
    {
    	return senderid;
    }
    public void setWeight(String senderid)
    {
    	this.senderid=senderid;
    }
    public String getRecipientid()
    {
    	return recipientid;
    }
    public void setRecipientid(String recipientid)
    {
    	this.recipientid=recipientid;
    }
}
