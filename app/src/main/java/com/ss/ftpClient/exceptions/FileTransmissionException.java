package com.ss.ftpClient.exceptions;

public class FileTransmissionException extends Exception{
    public FileTransmissionException(String filename){
        super(filename);
    }

}
