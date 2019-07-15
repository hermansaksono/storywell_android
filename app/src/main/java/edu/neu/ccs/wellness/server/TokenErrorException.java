package edu.neu.ccs.wellness.server;

import java.io.IOException;

public class TokenErrorException extends IOException {
    public TokenErrorException() {}
    public TokenErrorException(String msg) { super(msg); }
    public TokenErrorException(Throwable cause) { super(cause); }
    public TokenErrorException(String msg, Throwable cause) { super(msg, cause); }
}
